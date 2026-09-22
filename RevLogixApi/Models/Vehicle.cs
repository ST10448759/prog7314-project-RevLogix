namespace RevLogixApi.Models;

public class Vehicle
{
    public int VehicleId { get; set; }
    public int UserId { get; set; }
    public string Make { get; set; } = "";
    public string Model { get; set; } = "";
    public int Year { get; set; }
    public string BodyStyle { get; set; } = "";
    public string RegistrationNumber { get; set; } = "";
    public int CurrentOdometer { get; set; }
}