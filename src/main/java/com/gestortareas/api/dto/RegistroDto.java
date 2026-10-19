package com.gestortareas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroDto(
        @NotBlank(message = "El nombre de usuario es obligatorio.")
        @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres.")
        String nombreUsuario,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres.")
        String clave) {
}