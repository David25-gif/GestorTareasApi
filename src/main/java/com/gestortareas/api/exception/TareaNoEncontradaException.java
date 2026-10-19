package com.gestortareas.api.exception;

public class TareaNoEncontradaException extends RuntimeException {
    public TareaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}