using System.Text.Json;
using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RevLogixApi.Services;

namespace RevLogixApi.Controllers;

/// <summary>
/// Stores the app's records in Cloud Firestore under users/{uid}/{collection}/{id}.
/// The id is the id the phone gave the record, so sending the same change twice (which can
/// happen when a sync is retried) just overwrites the document instead of duplicating it.
/// </summary>
[ApiController]
[Authorize]
[Route("api/data")]
public class DataController : ControllerBase
{
    private static readonly string[] Collections =
        { "vehicles", "fuelLogs", "expenses", "maintenanceRecords", "customParts" };

    // Collections whose documents belong to a vehicle through "vehicleId"
    private static readonly string[] VehicleChildren =
        { "fuelLogs", "expenses", "maintenanceRecords", "customParts" };

    private readonly FirestoreDb _db;
    private readonly MaintenanceAlertService _alerts;
    private readonly ILogger<DataController> _logger;

    public DataController(FirestoreDb db, MaintenanceAlertService alerts, ILogger<DataController> logger)
    {
        _db = db;
        _alerts = alerts;
        _logger = logger;
    }

    // Firebase ID tokens carry the user's id in "user_id" (and in the standard "sub" claim)
    private string Uid => User.FindFirst("user_id")?.Value
                          ?? User.FindFirst("sub")?.Value
                          ?? throw new InvalidOperationException("Token has no user id.");

    private CollectionReference UserCollection(string collection) =>
        _db.Collection("users").Document(Uid).Collection(collection);

    [HttpGet("{collection}")]
    public async Task<IActionResult> List(string collection)
    {
        if (!Collections.Contains(collection)) return NotFound();

        var snapshot = await UserCollection(collection).GetSnapshotAsync();
        return Ok(snapshot.Documents.Select(d => d.ToDictionary()));
    }

    [HttpPut("{collection}/{id:int}")]
    public async Task<IActionResult> Upsert(string collection, int id, [FromBody] JsonElement body)
    {
        if (!Collections.Contains(collection)) return NotFound();
        if (body.ValueKind != JsonValueKind.Object) return BadRequest("Body must be a JSON object.");

        var document = JsonConversion.ToMap(body);
        document["updatedAt"] = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        await UserCollection(collection).Document(id.ToString()).SetAsync(document);
        _logger.LogInformation("Saved {Collection}/{Id} for user {Uid}", collection, id, Uid);

        if (collection == "maintenanceRecords")
        {
            try
            {
                await _alerts.CheckAsync(Uid, document);
            }
            catch (Exception ex)
            {
                // A failed notification must never make the data sync fail
                _logger.LogWarning(ex, "Could not send maintenance alert");
            }
        }

        return NoContent();
    }

    [HttpDelete("{collection}/{id:int}")]
    public async Task<IActionResult> Remove(string collection, int id)
    {
        if (!Collections.Contains(collection)) return NotFound();

        await UserCollection(collection).Document(id.ToString()).DeleteAsync();

        // Deleting a vehicle on the phone also deletes its records there (cascade), so mirror that
        if (collection == "vehicles")
        {
            foreach (var child in VehicleChildren)
            {
                var snapshot = await UserCollection(child).WhereEqualTo("vehicleId", id).GetSnapshotAsync();
                foreach (var document in snapshot.Documents)
                {
                    await document.Reference.DeleteAsync();
                }
            }
        }

        _logger.LogInformation("Deleted {Collection}/{Id} for user {Uid}", collection, id, Uid);
        return NoContent();
    }
}
