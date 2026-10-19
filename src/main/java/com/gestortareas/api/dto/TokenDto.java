package com.gestortareas.api.dto;

public record TokenDto(String token, String tipo, long expiraEnSegundos) {
}