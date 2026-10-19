package com.gestortareas.api.service;

import com.gestortareas.api.dto.ResumenTareas;
import com.gestortareas.api.dto.TareaGuardarDto;
import com.gestortareas.api.exception.ReglaNegocioException;
import com.gestortareas.api.exception.TareaNoEncontradaException;
import com.gestortareas.api.model.EstadoTarea;
import com.gestortareas.api.model.Prioridad;
import com.gestortareas.api.model.Tarea;
import com.gestortareas.api.repository.TareaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class TareaService {

    private static final int MAX_TITULO = 100;
    private static final int MAX_DESCRIPCION = 500;
    private static final Set<EstadoTarea> ACTIVOS = EnumSet.of(EstadoTarea.Pendiente, EstadoTarea.EnProgreso);

    private final TareaRepository repo;

    public TareaService(TareaRepository repo) {
        this.repo = repo;
    }

    // ================= Operaciones =================

    public Tarea crear(TareaGuardarDto dto) {
        String titulo = validarTitulo(dto.titulo());
        String descripcion = validarDescripcion(dto.descripcion());
        Prioridad prioridad = dto.prioridad() != null ? dto.prioridad() : Prioridad.Media;

        LocalDate hoy = LocalDate.now();
        validarFechaLimite(hoy, dto.fechaLimite());

        if (existeTituloActivo(titulo, 0))
            throw new ReglaNegocioException("Ya existe una tarea activa con ese título.");

        Tarea tarea = new Tarea();
        tarea.setTitulo(titulo);
        tarea.setDescripcion(descripcion);
        tarea.setPrioridad(prioridad);
        tarea.setEstado(EstadoTarea.Pendiente);
        tarea.setFechaCreacion(hoy);
        tarea.setFechaLimite(dto.fechaLimite());
        return repo.save(tarea);
    }

    @Transactional(readOnly = true)
    public Tarea obtener(int id) {
        return repo.findById(id)
                .orElseThrow(() -> new TareaNoEncontradaException("La tarea no existe."));
    }

    public Tarea editar(int id, TareaGuardarDto dto) {
        Tarea tarea = obtener(id);
        exigirNoEliminada(tarea);

        if (tarea.getEstado() == EstadoTarea.Completada || tarea.getEstado() == EstadoTarea.Cancelada)
            throw new ReglaNegocioException("No se puede editar una tarea completada o cancelada.");

        String titulo = validarTitulo(dto.titulo());
        String descripcion = validarDescripcion(dto.descripcion());
        Prioridad prioridad = dto.prioridad() != null ? dto.prioridad() : Prioridad.Media;
        validarFechaLimite(tarea.getFechaCreacion(), dto.fechaLimite());

        if (existeTituloActivo(titulo, id))
            throw new ReglaNegocioException("Ya existe otra tarea activa con ese título.");

        tarea.setTitulo(titulo);
        tarea.setDescripcion(descripcion);
        tarea.setPrioridad(prioridad);
        tarea.setFechaLimite(dto.fechaLimite());
        return repo.save(tarea);
    }

    public Tarea cambiarEstado(int id, EstadoTarea nuevoEstado) {
        Tarea tarea = obtener(id);
        exigirNoEliminada(tarea);

        if (!transicionValida(tarea.getEstado(), nuevoEstado))
            throw new ReglaNegocioException(
                    "No se puede pasar de " + tarea.getEstado() + " a " + nuevoEstado + ".");

        if (nuevoEstado == EstadoTarea.Completada) {
            LocalDate hoy = LocalDate.now();
            if (hoy.isBefore(tarea.getFechaCreacion()))
                throw new ReglaNegocioException("La fecha de finalización no puede ser anterior a la creación.");
            tarea.setFechaCompletada(hoy);
        }

        tarea.setEstado(nuevoEstado);
        return repo.save(tarea);
    }

    /** Mueve la tarea a la papelera (borrado lógico). */
    public void eliminar(int id) {
        Tarea tarea = obtener(id);

        if (tarea.getFechaEliminacion() != null)
            throw new ReglaNegocioException("La tarea ya está en la papelera.");
        if (tarea.getEstado() == EstadoTarea.EnProgreso)
            throw new ReglaNegocioException("No se puede eliminar una tarea en progreso.");

        tarea.setFechaEliminacion(LocalDateTime.now());
        repo.save(tarea);
    }

    public Tarea restaurar(int id) {
        Tarea tarea = obtener(id);

        if (tarea.getFechaEliminacion() == null)
            throw new ReglaNegocioException("La tarea no está en la papelera.");

        if (ACTIVOS.contains(tarea.getEstado()) && existeTituloActivo(tarea.getTitulo(), id))
            throw new ReglaNegocioException("No se puede restaurar: ya existe una tarea activa con ese título.");

        tarea.setFechaEliminacion(null);
        return repo.save(tarea);
    }

    /** Borra para siempre una tarea que ya está en la papelera. */
    public void eliminarDefinitivo(int id) {
        Tarea tarea = obtener(id);

        if (tarea.getFechaEliminacion() == null)
            throw new ReglaNegocioException("Solo se pueden eliminar definitivamente las tareas de la papelera.");

        repo.delete(tarea);
    }

    // ================= Consultas =================

    /** Filtra y ordena: prioridad alta primero, luego fecha límite más próxima (sin fecha al final). */
    @Transactional(readOnly = true)
    public List<Tarea> listar(EstadoTarea estado, Prioridad prioridad, String texto, boolean soloVencidas) {
        String buscar = (texto == null || texto.isBlank()) ? null : texto.trim().toLowerCase();
        LocalDate hoy = LocalDate.now();

        Comparator<Tarea> orden = Comparator.comparingInt((Tarea t) -> t.getPrioridad().getValor())
                .reversed()
                .thenComparing(Tarea::getFechaLimite, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Tarea::getIdTarea);

        return repo.findByFechaEliminacionIsNull().stream()
                .filter(t -> estado == null || t.getEstado() == estado)
                .filter(t -> prioridad == null || t.getPrioridad() == prioridad)
                .filter(t -> buscar == null
                        || t.getTitulo().toLowerCase().contains(buscar)
                        || (t.getDescripcion() != null && t.getDescripcion().toLowerCase().contains(buscar)))
                .filter(t -> !soloVencidas || estaVencida(t, hoy))
                .sorted(orden)
                .toList();
    }

    /** Tareas completadas o canceladas, las más recientes primero. */
    @Transactional(readOnly = true)
    public List<Tarea> historial() {
        return repo.findByFechaEliminacionIsNull().stream()
                .filter(t -> t.getEstado() == EstadoTarea.Completada || t.getEstado() == EstadoTarea.Cancelada)
                .sorted(Comparator.comparing(
                                (Tarea t) -> t.getFechaCompletada() != null ? t.getFechaCompletada() : t.getFechaCreacion())
                        .reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Tarea> papelera() {
        return repo.findByFechaEliminacionIsNotNullOrderByFechaEliminacionDesc();
    }

    @Transactional(readOnly = true)
    public List<Tarea> vencidas() {
        LocalDate hoy = LocalDate.now();
        return repo.findByFechaEliminacionIsNull().stream()
                .filter(t -> estaVencida(t, hoy))
                .sorted(Comparator.comparing(Tarea::getFechaLimite).thenComparing(Tarea::getIdTarea))
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumenTareas resumen() {
        List<Tarea> lista = repo.findByFechaEliminacionIsNull();
        LocalDate hoy = LocalDate.now();

        int total = lista.size();
        int pendientes = contar(lista, EstadoTarea.Pendiente);
        int enProgreso = contar(lista, EstadoTarea.EnProgreso);
        int completadas = contar(lista, EstadoTarea.Completada);
        int canceladas = contar(lista, EstadoTarea.Cancelada);
        int vencidas = (int) lista.stream().filter(t -> estaVencida(t, hoy)).count();

        int baseCalculo = total - canceladas;
        BigDecimal avance = baseCalculo <= 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(completadas).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(baseCalculo), 2, RoundingMode.HALF_UP);

        return new ResumenTareas(total, pendientes, enProgreso, completadas, canceladas, vencidas, avance);
    }

    // ================= Reglas privadas =================

    private String validarTitulo(String titulo) {
        String limpio = titulo == null ? "" : titulo.trim();
        if (limpio.isEmpty())
            throw new ReglaNegocioException("El título es obligatorio.");
        if (limpio.length() > MAX_TITULO)
            throw new ReglaNegocioException("El título no puede superar " + MAX_TITULO + " caracteres.");
        return limpio;
    }

    private String validarDescripcion(String descripcion) {
        String limpia = descripcion == null ? "" : descripcion.trim();
        if (limpia.isEmpty()) return null;
        if (limpia.length() > MAX_DESCRIPCION)
            throw new ReglaNegocioException("La descripción no puede superar " + MAX_DESCRIPCION + " caracteres.");
        return limpia;
    }

    private void validarFechaLimite(LocalDate fechaCreacion, LocalDate fechaLimite) {
        if (fechaLimite != null && fechaLimite.isBefore(fechaCreacion))
            throw new ReglaNegocioException("La fecha límite no puede ser anterior a la creación.");
    }

    private void exigirNoEliminada(Tarea tarea) {
        if (tarea.getFechaEliminacion() != null)
            throw new ReglaNegocioException("La tarea está en la papelera. Restáurala para poder modificarla.");
    }

    private boolean transicionValida(EstadoTarea actual, EstadoTarea nuevo) {
        return switch (actual) {
            case Pendiente -> nuevo == EstadoTarea.EnProgreso || nuevo == EstadoTarea.Cancelada;
            case EnProgreso -> nuevo == EstadoTarea.Completada
                    || nuevo == EstadoTarea.Pendiente
                    || nuevo == EstadoTarea.Cancelada;
            default -> false;
        };
    }

    /** Vencida = activa, fuera de la papelera y con fecha límite anterior a hoy. */
    private boolean estaVencida(Tarea t, LocalDate hoy) {
        return ACTIVOS.contains(t.getEstado())
                && t.getFechaEliminacion() == null
                && t.getFechaLimite() != null
                && t.getFechaLimite().isBefore(hoy);
    }

    private boolean existeTituloActivo(String titulo, int excluirId) {
        return repo.existsByTituloAndIdTareaNotAndFechaEliminacionIsNullAndEstadoIn(titulo, excluirId, ACTIVOS);
    }

    private int contar(List<Tarea> lista, EstadoTarea estado) {
        return (int) lista.stream().filter(t -> t.getEstado() == estado).count();
    }
}