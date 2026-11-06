using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace RevLogixApi.Controllers;

[ApiController]
[Authorize]
[Route("api/auth")]
public class AuthController : ControllerBase
{
    // The sign-in itself happens in the app (Google -> Firebase Auth).
    // This endpoint lets the app confirm that the API accepts its token.
    [HttpGet("me")]
    public IActionResult Me() => Ok(new
    {
        userId = User.FindFirst("user_id")?.Value,
        email = User.FindFirst("email")?.Value
    });
}
