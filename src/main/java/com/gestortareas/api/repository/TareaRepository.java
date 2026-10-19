package com.gestortareas.api.repository;

import com.gestortareas.api.model.EstadoTarea;
import com.gestortareas.api.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TareaRepository extends JpaRepository<Tarea, Integer> {

    /** Tareas que NO están en la papelera. */
    List<Tarea> findByFechaEliminacionIsNull();

    /** Tareas en la papelera, las más recientes primero. */
    List<Tarea> findByFechaEliminacionIsNotNullOrderByFechaEliminacionDesc();

    boolean existsByTituloAndIdTareaNotAndFechaEliminacionIsNullAndEstadoIn(
            String titulo, Integer idTarea, Collection<EstadoTarea> estados);
}