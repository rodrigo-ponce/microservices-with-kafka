package com.example.lib.commons.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class JacksonUtil {

    static class OffsetDeserializer extends JsonDeserializer<OffsetDateTime> {


        @Override
        public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            try {
                return OffsetDateTime.parse(p.getText(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } catch (DateTimeParseException e) {
                LocalDateTime localDateTime = LocalDateTime.parse(p.getText(), DateTimeFormatter.ISO_DATE_TIME);
                return localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
            }
        }
    }

    public static final ObjectMapper mapper;
    public static final ObjectReader reader;
    public static final ObjectWriter writer;

    static {

        mapper = new ObjectMapper();
        // setup defaults
        final JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(OffsetDateTime.class, new OffsetDeserializer());
        mapper.registerModule(javaTimeModule);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        reader = mapper.reader();
        writer = mapper.writer();

    }


    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return reader.forType(clazz).readValue(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJson(Object object) {
        try {
            return writer.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

