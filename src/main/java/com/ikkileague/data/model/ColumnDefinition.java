package com.ikkileague.data.model;

public class ColumnDefinition {
    private final String name;
    private final int length;
    private final ColumnType type;

    public ColumnDefinition(String name, int length, ColumnType type) {
        this.name = name;
        this.length = length;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    public ColumnType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ColumnDefinition that = (ColumnDefinition) o;
        return length == that.length && name.equals(that.name) && type == that.type;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, length, type);
    }

    @Override
    public String toString() {
        return String.format("ColumnDefinition{name='%s', type='%s',  length=%d}",
                name, type, length);
    }
}