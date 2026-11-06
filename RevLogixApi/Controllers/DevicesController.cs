using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RevLogixApi.Services;

namespace RevLogixApi.Controllers;

[ApiController]
[Authorize]
[Route("api/devices")]
public class DevicesController : ControllerBase
{
    public record DeviceRegistration(string Token);

    private readonly PushService _push;

    public DevicesController(PushService push)
    {
        _push = push;
    }

    // The app sends its Firebase Cloud Messaging token here so the API knows where to push to
    [HttpPost]
    public async Task<IActionResult> Register(DeviceRegistration request)
    {
        if (string.IsNullOrWhiteSpace(request.Token)) return BadRequest("Token is required.");

        var uid = User.FindFirst("user_id")?.Value ?? User.FindFirst("sub")?.Value;
        if (uid == null) return Unauthorized();

        await _push.RegisterTokenAsync(uid, request.Token);
        return NoContent();
    }
}
