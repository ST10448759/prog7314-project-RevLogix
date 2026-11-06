using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RevLogixApi.Services;

namespace RevLogixApi.Controllers;

[ApiController]
[Authorize]
[Route("api/notifications")]
public class NotificationsController : ControllerBase
{
    private readonly PushService _push;

    public NotificationsController(PushService push)
    {
        _push = push;
    }

    // Used by the "Send test notification" button in Settings
    [HttpPost("test")]
    public async Task<IActionResult> Test()
    {
        var uid = User.FindFirst("user_id")?.Value ?? User.FindFirst("sub")?.Value;
        if (uid == null) return Unauthorized();

        var sent = await _push.SendToUserAsync(uid, "RevLogix", "Push notifications are working.");
        return Ok(new { sent });
    }
}
