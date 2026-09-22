using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using RevLogixApi.Data;
using RevLogixApi.Models;

namespace RevLogixApi.Controllers;

[ApiController]
[Route("api/fuel")]
public class FuelController : ControllerBase
{
    private readonly AppDbContext _db;
    public FuelController(AppDbContext db) { _db = db; }

    [HttpGet("{vehicleId}")]
    public async Task<ActionResult<List<FuelLog>>> GetForVehicle(int vehicleId) =>
        await _db.FuelLogs.Where(f => f.VehicleId == vehicleId).ToListAsync();

    [HttpPost]
    public async Task<ActionResult<FuelLog>> Create(FuelLog log)
    {
        _db.FuelLogs.Add(log);
        await _db.SaveChangesAsync();
        return Ok(log);
    }
}