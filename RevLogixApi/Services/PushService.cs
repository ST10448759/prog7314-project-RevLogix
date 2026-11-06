using System.Security.Cryptography;
using System.Text;
using FirebaseAdmin.Messaging;
using Google.Cloud.Firestore;

namespace RevLogixApi.Services;

/// <summary>Stores each user's phone tokens and sends them push notifications through FCM.</summary>
public class PushService
{
    private readonly FirestoreDb _db;
    private readonly ILogger<PushService> _logger;

    public PushService(FirestoreDb db, ILogger<PushService> logger)
    {
        _db = db;
        _logger = logger;
    }

    private CollectionReference Devices(string uid) =>
        _db.Collection("users").Document(uid).Collection("devices");

    public async Task RegisterTokenAsync(string uid, string token)
    {
        // The document id is a hash of the token so odd characters can never break the path
        var id = Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(token)));
        await Devices(uid).Document(id).SetAsync(new Dictionary<string, object>
        {
            ["token"] = token,
            ["registeredAt"] = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()
        });
        _logger.LogInformation("Registered device token for user {Uid}", uid);
    }

    /// <returns>How many devices the notification was delivered to FCM for.</returns>
    public async Task<int> SendToUserAsync(string uid, string title, string body)
    {
        var snapshot = await Devices(uid).GetSnapshotAsync();
        var documents = snapshot.Documents.ToList();
        if (documents.Count == 0)
        {
            _logger.LogInformation("User {Uid} has no registered devices", uid);
            return 0;
        }

        var message = new MulticastMessage
        {
            Tokens = documents.Select(d => d.GetValue<string>("token")).ToList(),
            Notification = new Notification { Title = title, Body = body },
            Android = new AndroidConfig
            {
                Notification = new AndroidNotification { ChannelId = "maintenance_alerts" }
            }
        };

        var response = await FirebaseMessaging.DefaultInstance.SendEachForMulticastAsync(message);

        // Tokens for uninstalled apps come back as "unregistered" - tidy those up
        for (var i = 0; i < response.Responses.Count; i++)
        {
            var result = response.Responses[i];
            if (!result.IsSuccess && result.Exception?.MessagingErrorCode == MessagingErrorCode.Unregistered)
            {
                await documents[i].Reference.DeleteAsync();
            }
        }

        _logger.LogInformation("Push sent to {Count} device(s) for user {Uid}", response.SuccessCount, uid);
        return response.SuccessCount;
    }
}
