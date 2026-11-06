using Google.Cloud.Firestore;

namespace RevLogixApi.Services;

/// <summary>
/// Server-side version of the dual-trigger rule from Part 1 (FR-06): a service is due when
/// either its date or its odometer reading is reached, whichever comes first.
/// </summary>
public static class MaintenanceRules
{
    private const long SoonWindowMs = 7L * 24 * 60 * 60 * 1000; // one week
    private const long SoonKm = 500;

    public enum Status { None, DueSoon, Overdue }

    public static Status Evaluate(long? nextDueDate, long nowMs, long? nextDueOdometer, long? currentOdometer)
    {
        var overdueByDate = nextDueDate != null && nowMs >= nextDueDate;
        var overdueByOdometer = nextDueOdometer != null && currentOdometer != null && currentOdometer >= nextDueOdometer;
        if (overdueByDate || overdueByOdometer)
        {
            return Status.Overdue;
        }

        var soonByDate = nextDueDate != null && nextDueDate - nowMs <= SoonWindowMs;
        var soonByOdometer = nextDueOdometer != null && currentOdometer != null && nextDueOdometer - currentOdometer <= SoonKm;
        return soonByDate || soonByOdometer ? Status.DueSoon : Status.None;
    }
}

/// <summary>Sends a push notification when a saved maintenance record is overdue or about to be.</summary>
public class MaintenanceAlertService
{
    private readonly FirestoreDb _db;
    private readonly PushService _push;

    public MaintenanceAlertService(FirestoreDb db, PushService push)
    {
        _db = db;
        _push = push;
    }

    public async Task CheckAsync(string uid, Dictionary<string, object> record)
    {
        var nextDueDate = AsLong(record, "nextDueDate");
        var nextDueOdometer = AsLong(record, "nextDueOdometer");
        var vehicleId = AsLong(record, "vehicleId");

        // The vehicle's current odometer lives on the vehicle document
        long? currentOdometer = null;
        if (nextDueOdometer != null && vehicleId != null)
        {
            var vehicle = await _db.Collection("users").Document(uid)
                .Collection("vehicles").Document(vehicleId.Value.ToString()).GetSnapshotAsync();
            if (vehicle.Exists && vehicle.TryGetValue<long>("currentOdometer", out var odometer))
            {
                currentOdometer = odometer;
            }
        }

        var status = MaintenanceRules.Evaluate(
            nextDueDate, DateTimeOffset.UtcNow.ToUnixTimeMilliseconds(), nextDueOdometer, currentOdometer);
        if (status == MaintenanceRules.Status.None)
        {
            return;
        }

        var service = record.TryGetValue("serviceType", out var type) ? type?.ToString() : null;
        if (string.IsNullOrWhiteSpace(service))
        {
            service = "A service";
        }

        if (status == MaintenanceRules.Status.Overdue)
        {
            await _push.SendToUserAsync(uid, "Service overdue", $"{service} is overdue. Open RevLogix to log it.");
        }
        else
        {
            await _push.SendToUserAsync(uid, "Service coming up", $"{service} is due soon.");
        }
    }

    private static long? AsLong(Dictionary<string, object> record, string key)
    {
        if (!record.TryGetValue(key, out var value))
        {
            return null;
        }
        return value switch
        {
            long l => l,
            double d => (long)d,
            _ => null
        };
    }
}
