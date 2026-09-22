using System.ComponentModel.DataAnnotations;

namespace RevLogixApi.Models;

public class MaintenanceRecord
{
    [Key]
    public int MaintenanceId { get; set; }
    public int VehicleId { get; set; }
    public string ServiceType { get; set; } = "";
    public long ServiceDate { get; set; }
    public int Odometer { get; set; }
    public double Cost { get; set; }
    public long? NextDueDate { get; set; }
    public int? NextDueOdometer { get; set; }
}