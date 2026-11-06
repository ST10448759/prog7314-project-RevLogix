using System.Text.Json;

namespace RevLogixApi.Services;

/// <summary>Turns the JSON sent by the app into plain values Firestore can store.</summary>
public static class JsonConversion
{
    public static Dictionary<string, object> ToMap(JsonElement element)
    {
        var map = new Dictionary<string, object>();
        foreach (var property in element.EnumerateObject())
        {
            var value = ToValue(property.Value);
            if (value != null)
            {
                map[property.Name] = value;
            }
        }
        return map;
    }

    private static object? ToValue(JsonElement element)
    {
        switch (element.ValueKind)
        {
            case JsonValueKind.String:
                return element.GetString();
            case JsonValueKind.Number:
                // Whole numbers stay whole (odometer, ids, dates); everything else is a double
                if (element.TryGetInt64(out var whole))
                {
                    return whole;
                }
                return element.GetDouble();
            case JsonValueKind.True:
                return true;
            case JsonValueKind.False:
                return false;
            case JsonValueKind.Object:
                return ToMap(element);
            case JsonValueKind.Array:
                return element.EnumerateArray().Select(ToValue).Where(v => v != null).ToList();
            default:
                return null; // null / undefined values are simply not stored
        }
    }
}
