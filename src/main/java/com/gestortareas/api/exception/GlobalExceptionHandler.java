package com.gestortareas.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ProblemDetail credencialesInvalidas(CredencialesInvalidasException ex) {
        return problema(HttpStatus.UNAUTHORIZED, "No autorizado", ex.getMessage());
    }

    @ExceptionHandler(TareaNoEncontradaException.class)
    public ProblemDetail noEncontrada(TareaNoEncontradaException ex) {
        return problema(HttpStatus.NOT_FOUND, "No encontrado", ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail reglaNegocio(ReglaNegocioException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Regla de negocio incumplida", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacion(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .collect(Collectors.joining(" "));
        return problema(HttpStatus.BAD_REQUEST, "Datos inválidos", detalle);
    }

    /** JSON mal formado, enum o fecha con valor inválido en el body. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail cuerpoInvalido(HttpMessageNotReadableException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Solicitud inválida",
                "El cuerpo de la solicitud no es válido (revisa el JSON, los estados, prioridades y fechas).");
    }

    /** Valor inválido en la URL o en los parámetros (por ejemplo ?estado=Foo). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Parámetro inválido",
                "El valor del parámetro '" + ex.getName() + "' no es válido.");
    }

    private ProblemDetail problema(HttpStatus status, String titulo, String detalle) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detalle);
        pd.setTitle(titulo);
        return pd;
    }
}