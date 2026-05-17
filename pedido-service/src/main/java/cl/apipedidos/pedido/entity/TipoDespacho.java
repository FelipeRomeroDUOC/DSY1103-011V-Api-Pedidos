package cl.apipedidos.pedido.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoDespacho {
    RETIRO("RETIRO"),
    RM("RM"),
    REGION("REGION");

    private final String value;

    TipoDespacho(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TipoDespacho fromValue(String value) {
        if (value == null) {
            return null;
        }

        return switch (value.trim().toUpperCase()) {
            case "DOMICILIO", "RM" -> RM;
            case "REGION" -> REGION;
            case "RETIRO" -> RETIRO;
            default -> throw new IllegalArgumentException("TipoDespacho inválido: " + value);
        };
    }
}