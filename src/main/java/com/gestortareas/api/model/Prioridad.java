package com.gestortareas.api.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum Prioridad {
    Baja(1), Media(2), Alta(3);

    private final int valor;

    Prioridad(int valor) { this.valor = valor; }

    public int getValor() { return valor; }

    public static Prioridad desdeValor(int valor) {
        for (Prioridad p : values()) {
            if (p.valor == valor) return p;
        }
        throw new IllegalArgumentException("Prioridad desconocida: " + valor);
    }

    /** Guarda el enum como int (1, 2, 3), igual que EF en C#. */
    @Converter
    public static class Conv implements AttributeConverter<Prioridad, Integer> {
        @Override
        public Integer convertToDatabaseColumn(Prioridad p) { return p == null ? null : p.valor; }

        @Override
        public Prioridad convertToEntityAttribute(Integer v) { return v == null ? null : desdeValor(v); }
    }
}