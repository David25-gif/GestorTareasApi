package com.gestortareas.api.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum EstadoTarea {
    Pendiente(1), EnProgreso(2), Completada(3), Cancelada(4);

    private final int valor;

    EstadoTarea(int valor) { this.valor = valor; }

    public int getValor() { return valor; }

    public static EstadoTarea desdeValor(int valor) {
        for (EstadoTarea e : values()) {
            if (e.valor == valor) return e;
        }
        throw new IllegalArgumentException("Estado desconocido: " + valor);
    }

    @Converter
    public static class Conv implements AttributeConverter<EstadoTarea, Integer> {
        @Override
        public Integer convertToDatabaseColumn(EstadoTarea e) { return e == null ? null : e.valor; }

        @Override
        public EstadoTarea convertToEntityAttribute(Integer v) { return v == null ? null : desdeValor(v); }
    }
}