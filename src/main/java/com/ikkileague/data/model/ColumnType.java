package com.ikkileague.data.model;

import java.util.HashMap;
import java.util.Map;

public enum ColumnType {
    STRING("chaîne"),
    DATE("date"),
    NUMERIC("numérique");

    private final String name;

    ColumnType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Recherche inverse : name → ColumnType
    private static final Map<String, ColumnType> NAME_MAP = new HashMap<>();

    static {
        for (ColumnType type : values()) {
            NAME_MAP.put(type.getName().toLowerCase(), type);
        }
    }

    public static ColumnType fromName(String name) {
        ColumnType type = NAME_MAP.get(name.trim().toLowerCase());
        if (type == null) {
            throw new IllegalArgumentException(
                    "Unknown column type : '" + name + "'. Expected " + getAllNamesAsString());
        }
        return type;
    }

    public static String getAllNamesAsString() {
        return NAME_MAP.keySet().toString();

    }
}