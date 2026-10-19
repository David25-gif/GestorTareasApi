package com.gestortareas.api.dto;

import java.math.BigDecimal;

public record ResumenTareas(
        int total,
        int pendientes,
        int enProgreso,
        int completadas,
        int canceladas,
        int vencidas,
        BigDecimal porcentajeAvance) {
}