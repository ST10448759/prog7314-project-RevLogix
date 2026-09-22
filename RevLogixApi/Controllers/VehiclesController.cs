using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using RevLogixApi.Data;
using RevLogixApi.Models;

namespace RevLogixApi.Controllers;

[ApiController]
[Route("api/vehicles")]
public class VehiclesController : ControllerBase
{
    private readonly AppDbContext _db;
    public VehiclesController(AppDbContext db) { _db = db; }

    [HttpGet]
    public async Task<ActionResult<List<Vehicle>>> GetAll() => await _db.Vehicles.ToListAsync();

    [HttpPost]
    public async Task<ActionResult<Vehicle>> Create(Vehicle vehicle)
    {
        _db.Vehicles.Add(vehicle);
        await _db.SaveChangesAsync();
        return Ok(vehicle);
    }
}