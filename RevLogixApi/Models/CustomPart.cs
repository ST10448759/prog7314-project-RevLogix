using System.ComponentModel.DataAnnotations;

namespace RevLogixApi.Models;

public class CustomPart
{
    [Key]
    public int PartId { get; set; }
    public int VehicleId { get; set; }
    public string Category { get; set; } = "";
    public string PartName { get; set; } = "";
    public string Brand { get; set; } = "";
    public string PartNumber { get; set; } = "";
    public string SerialNumber { get; set; } = "";
    public string TechnicalSpecs { get; set; } = "";
    public string Vendor { get; set; } = "";
    public double Cost { get; set; }
}