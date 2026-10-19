package com.gestortareas.api.dto;

import com.gestortareas.api.model.Prioridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TareaGuardarDto(
        @NotBlank(message = "El título es obligatorio.")
        @Size(max = 100, message = "El título no puede superar 100 caracteres.")
        String titulo,

        @Size(max = 500, message = "La descripción no puede superar 500 caracteres.")
        String descripcion,

        Prioridad prioridad,       // si no viene, se usa Media

        LocalDate fechaLimite) {
}