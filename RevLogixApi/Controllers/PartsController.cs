using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using RevLogixApi.Data;
using RevLogixApi.Models;

namespace RevLogixApi.Controllers;

[ApiController]
[Route("api/parts")]
public class PartsController : ControllerBase
{
    private readonly AppDbContext _db;
    public PartsController(AppDbContext db) { _db = db; }

    [HttpGet("{vehicleId}")]
    public async Task<ActionResult<List<CustomPart>>> GetForVehicle(int vehicleId) =>
        await _db.CustomParts.Where(p => p.VehicleId == vehicleId).ToListAsync();

    [HttpPost]
    public async Task<ActionResult<CustomPart>> Create(CustomPart part)
    {
        _db.CustomParts.Add(part);
        await _db.SaveChangesAsync();
        return Ok(part);
    }
}