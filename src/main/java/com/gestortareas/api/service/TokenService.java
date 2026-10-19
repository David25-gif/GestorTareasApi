package com.gestortareas.api.service;

import com.gestortareas.api.dto.TokenDto;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {

    private static final long HORAS_VIGENCIA = 2;

    private final JwtEncoder encoder;

    public TokenService(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    public TokenDto generar(String nombreUsuario) {
        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("gestortareas-api")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(HORAS_VIGENCIA, ChronoUnit.HOURS))
                .subject(nombreUsuario)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new TokenDto(token, "Bearer", HORAS_VIGENCIA * 3600);
    }
}