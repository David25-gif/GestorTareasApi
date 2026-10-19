package com.gestortareas.api.controller;

import com.gestortareas.api.dto.CambiarEstadoDto;
import com.gestortareas.api.dto.ResumenTareas;
import com.gestortareas.api.dto.TareaGuardarDto;
import com.gestortareas.api.model.EstadoTarea;
import com.gestortareas.api.model.Prioridad;
import com.gestortareas.api.model.Tarea;
import com.gestortareas.api.service.TareaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tareas")
@SecurityRequirement(name = "bearerAuth")
public class TareasController {

    private final TareaService service;

    public TareasController(TareaService service) {
        this.service = service;
    }

    // GET /api/tareas?estado=Pendiente&prioridad=Alta&texto=informe&vencidas=true
    @GetMapping
    public List<Tarea> listar(@RequestParam(required = false) EstadoTarea estado,
                              @RequestParam(required = false) Prioridad prioridad,
                              @RequestParam(required = false) String texto,
                              @RequestParam(defaultValue = "false") boolean vencidas) {
        return service.listar(estado, prioridad, texto, vencidas);
    }

    @GetMapping("/{id}")
    public Tarea obtener(@PathVariable int id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<Tarea> crear(@Valid @RequestBody TareaGuardarDto dto) {
        Tarea tarea = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/tareas/" + tarea.getIdTarea())).body(tarea);
    }

    @PutMapping("/{id}")
    public Tarea editar(@PathVariable int id, @Valid @RequestBody TareaGuardarDto dto) {
        return service.editar(id, dto);
    }

    // PATCH /api/tareas/5/estado   body: { "estado": "EnProgreso" }
    @PatchMapping("/{id}/estado")
    public Tarea cambiarEstado(@PathVariable int id, @Valid @RequestBody CambiarEstadoDto dto) {
        return service.cambiarEstado(id, dto.estado());
    }

    // DELETE /api/tareas/5  -> la manda a la papelera
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restaurar")
    public Tarea restaurar(@PathVariable int id) {
        return service.restaurar(id);
    }

    // DELETE /api/tareas/5/definitivo  -> borra para siempre (solo si está en la papelera)
    @DeleteMapping("/{id}/definitivo")
    public ResponseEntity<Void> eliminarDefinitivo(@PathVariable int id) {
        service.eliminarDefinitivo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/historial")
    public List<Tarea> historial() {
        return service.historial();
    }

    @GetMapping("/papelera")
    public List<Tarea> papelera() {
        return service.papelera();
    }

    @GetMapping("/vencidas")
    public List<Tarea> vencidas() {
        return service.vencidas();
    }

    @GetMapping("/resumen")
    public ResumenTareas resumen() {
        return service.resumen();
    }
}