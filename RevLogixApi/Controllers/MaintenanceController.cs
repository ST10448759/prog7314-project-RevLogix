using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using RevLogixApi.Data;
using RevLogixApi.Models;

namespace RevLogixApi.Controllers;

[ApiController]
[Route("api/maintenance")]
public class MaintenanceController : ControllerBase
{
    private readonly AppDbContext _db;
    public MaintenanceController(AppDbContext db) { _db = db; }

    [HttpGet("{vehicleId}")]
    public async Task<ActionResult<List<MaintenanceRecord>>> GetForVehicle(int vehicleId) =>
        await _db.MaintenanceRecords.Where(m => m.VehicleId == vehicleId).ToListAsync();

    [HttpPost]
    public async Task<ActionResult<MaintenanceRecord>> Create(MaintenanceRecord record)
    {
        _db.MaintenanceRecords.Add(record);
        await _db.SaveChangesAsync();
        return Ok(record);
    }
}