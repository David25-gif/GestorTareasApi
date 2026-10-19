package com.gestortareas.api.controller;

import com.gestortareas.api.dto.LoginDto;
import com.gestortareas.api.dto.RegistroDto;
import com.gestortareas.api.dto.TokenDto;
import com.gestortareas.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    // POST /api/auth/registro  -> crea el usuario y devuelve un token
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenDto registro(@Valid @RequestBody RegistroDto dto) {
        return service.registrar(dto);
    }

    // POST /api/auth/login  -> devuelve un token
    @PostMapping("/login")
    public TokenDto login(@Valid @RequestBody LoginDto dto) {
        return service.login(dto);
    }
}