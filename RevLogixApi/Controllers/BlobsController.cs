using Google.Cloud.Storage.V1;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using StorageObject = Google.Apis.Storage.v1.Data.Object;

namespace RevLogixApi.Controllers;

/// <summary>Blob storage for receipt photos attached to expenses.</summary>
[ApiController]
[Authorize]
[Route("api/blobs")]
public class BlobsController : ControllerBase
{
    private readonly StorageClient _storage;
    private readonly string _bucket;
    private readonly ILogger<BlobsController> _logger;

    public BlobsController(StorageClient storage, IConfiguration configuration, ILogger<BlobsController> logger)
    {
        _storage = storage;
        _logger = logger;
        // Newer Firebase projects use <project>.firebasestorage.app, older ones <project>.appspot.com
        var configured = configuration["Firebase:StorageBucket"];
        _bucket = string.IsNullOrWhiteSpace(configured)
            ? $"{configuration["Firebase:ProjectId"]}.firebasestorage.app"
            : configured;
    }

    [HttpPost]
    [RequestSizeLimit(10_000_000)]
    public async Task<IActionResult> Upload(IFormFile file)
    {
        if (file == null || file.Length == 0) return BadRequest("No file received.");
        if (!file.ContentType.StartsWith("image/")) return BadRequest("Only images can be uploaded.");

        var uid = User.FindFirst("user_id")?.Value ?? User.FindFirst("sub")?.Value;
        if (uid == null) return Unauthorized();

        var objectName = $"receipts/{uid}/{Guid.NewGuid():N}.jpg";

        // Firebase Storage serves any object that has a download token in its metadata
        var downloadToken = Guid.NewGuid().ToString();
        var storageObject = new StorageObject
        {
            Bucket = _bucket,
            Name = objectName,
            ContentType = file.ContentType,
            Metadata = new Dictionary<string, string> { ["firebaseStorageDownloadTokens"] = downloadToken }
        };

        await using var stream = file.OpenReadStream();
        await _storage.UploadObjectAsync(storageObject, stream);
        _logger.LogInformation("Stored receipt {Name}", objectName);

        var url = $"https://firebasestorage.googleapis.com/v0/b/{_bucket}/o/{Uri.EscapeDataString(objectName)}?alt=media&token={downloadToken}";
        return Ok(new { url });
    }
}
