package com.gestortareas.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDto(
        @NotBlank(message = "El nombre de usuario es obligatorio.")
        String nombreUsuario,

        @NotBlank(message = "La contraseña es obligatoria.")
        String clave) {
}