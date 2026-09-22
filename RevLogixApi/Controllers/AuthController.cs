using Microsoft.AspNetCore.Mvc;

namespace RevLogixApi.Controllers;

[ApiController]
[Route("api/auth")]
public class AuthController : ControllerBase
{
    public record SsoRequest(string ProviderId, string IdentityToken);

    [HttpPost("sso")]
    public IActionResult Sso(SsoRequest request) =>
        Ok(new { userId = 1, fullName = "Local Test User", token = "stub-token" });
}