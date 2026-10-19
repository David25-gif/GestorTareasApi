package com.gestortareas.api.service;

import com.gestortareas.api.dto.LoginDto;
import com.gestortareas.api.dto.RegistroDto;
import com.gestortareas.api.dto.TokenDto;
import com.gestortareas.api.exception.CredencialesInvalidasException;
import com.gestortareas.api.exception.ReglaNegocioException;
import com.gestortareas.api.model.Usuario;
import com.gestortareas.api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuthService {

    private final UsuarioRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UsuarioRepository repo, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public TokenDto registrar(RegistroDto dto) {
        String nombre = dto.nombreUsuario().trim();

        if (nombre.length() < 3)
            throw new ReglaNegocioException("El nombre de usuario debe tener al menos 3 caracteres.");
        if (repo.existsByNombreUsuario(nombre))
            throw new ReglaNegocioException("Ese nombre de usuario ya está en uso.");

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombre);
        usuario.setClaveHash(passwordEncoder.encode(dto.clave()));
        usuario.setFechaRegistro(LocalDateTime.now());
        repo.save(usuario);

        return tokenService.generar(usuario.getNombreUsuario());
    }

    public TokenDto login(LoginDto dto) {
        Usuario usuario = repo.findByNombreUsuario(dto.nombreUsuario().trim())
                .orElseThrow(() -> new CredencialesInvalidasException("Usuario o contraseña incorrectos."));

        if (!passwordEncoder.matches(dto.clave(), usuario.getClaveHash()))
            throw new CredencialesInvalidasException("Usuario o contraseña incorrectos.");

        return tokenService.generar(usuario.getNombreUsuario());
    }
}