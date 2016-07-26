package controller.utils;

import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class JsonNullableGenerator implements JsonGenerator {
    private final JsonGenerator jg;

    public JsonNullableGenerator(JsonGeneratorFactory factory, OutputStream out) {
        jg = factory.createGenerator(out, StandardCharsets.UTF_8);
    }

    @Override
    public JsonGenerator writeStartObject() {
        jg.writeStartObject();
        return this;
    }

    @Override
    public JsonGenerator writeStartObject(String name) {
        jg.writeStartObject(name);
        return this;
    }

    @Override
    public JsonGenerator writeStartArray() {
        jg.writeStartArray();
        return this;
    }

    @Override
    public JsonGenerator writeStartArray(String name) {
        jg.writeStartArray(name);
        return this;
    }

    @Override
    public JsonGenerator write(String name, JsonValue value) {
        if (value != null) jg.write(name, value);
        return this;
    }

    @Override
    public JsonGenerator write(String name, String value) {
        if (value != null) jg.write(name, value);
        return this;
    }

    @Override
    public JsonGenerator write(String name, BigInteger value) {
        if (value != null) jg.write(name, value);
        return this;
    }

    @Override
    public JsonGenerator write(String name, BigDecimal value) {
        if (value != null) jg.write(name, value);
        return this;
    }

    @Override
    public JsonGenerator write(String name, int value) {
        jg.write(name, value);
        return this;
    }

    @Override
    public JsonGenerator write(String name, long value) {
        jg.write(name, value);
        return this;
    }

    @Override
    public JsonGenerator write(String name, double value) {
        jg.write(name, value);
        return this;
    }

    @Override
    public JsonGenerator write(String name, boolean value) {
        jg.write(name, value);
        return this;
    }

    @Override
    public JsonGenerator writeNull(String name) {
        jg.writeNull(name);
        return this;
    }

    @Override
    public JsonGenerator writeEnd() {
        jg.writeEnd();
        return this;
    }

    @Override
    public JsonGenerator write(JsonValue value) {
        if (value != null) jg.write(value);
        return this;
    }

    @Override
    public JsonGenerator write(String value) {
        if (value != null) jg.write(value);
        return this;
    }

    @Override
    public JsonGenerator write(BigDecimal value) {
        if (value != null) jg.write(value);
        return this;
    }

    @Override
    public JsonGenerator write(BigInteger value) {
        if (value != null) jg.write(value);
        return this;
    }

    @Override
    public JsonGenerator write(int value) {
        jg.write(value);
        return this;
    }

    @Override
    public JsonGenerator write(long value) {
        jg.write(value);
        return this;
    }

    @Override
    public JsonGenerator write(double value) {
        jg.write(value);
        return this;
    }

    @Override
    public JsonGenerator write(boolean value) {
        jg.write(value);
        return this;
    }

    @Override
    public JsonGenerator writeNull() {
        jg.writeNull();
        return this;
    }

    @Override
    public void close() {
        jg.close();
    }

    @Override
    public void flush() {
        jg.flush();
    }
}
