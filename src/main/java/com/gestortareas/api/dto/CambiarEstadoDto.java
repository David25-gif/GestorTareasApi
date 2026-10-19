package com.gestortareas.api.dto;

import com.gestortareas.api.model.EstadoTarea;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoDto(
        @NotNull(message = "El estado es obligatorio.")
        EstadoTarea estado) {
}