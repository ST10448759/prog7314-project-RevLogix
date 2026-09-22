namespace RevLogixApi.Models;

public class FuelLog
{
    public int FuelLogId { get; set; }
    public int VehicleId { get; set; }
    public long FillUpDate { get; set; }
    public int Odometer { get; set; }
    public double Litres { get; set; }
    public double TotalCost { get; set; }
    public string FuelType { get; set; } = "";
    public bool FullTank { get; set; }
}