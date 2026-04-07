package fr.wdes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class LegacyPropertyMapSerializer
  implements JsonSerializer<PropertyMap>
{
  public JsonElement serialize(PropertyMap src, Type typeOfSrc, JsonSerializationContext context)
  {
    JsonObject result = new JsonObject();
    for (String key : src.keySet())
    {
      JsonArray values = new JsonArray();
      for (Property property : src.get(key)) {
        values.add(new JsonPrimitive(property.getValue()));
      }
      result.add(key, values);
    }
    return result;
  }
}