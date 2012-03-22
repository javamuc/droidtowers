package com.unhappyrobot.jackson;

import com.badlogic.gdx.math.Vector2;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

public class Vector2Serializer extends JsonSerializer<Vector2> {
  @Override
  public void serialize(Vector2 vector2, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    jsonGenerator.writeStartObject();
    jsonGenerator.writeNumberField("x", vector2.x);
    jsonGenerator.writeNumberField("y", vector2.y);
    jsonGenerator.writeEndObject();
  }

  @Override
  public Class<Vector2> handledType() {
    return Vector2.class;
  }
}