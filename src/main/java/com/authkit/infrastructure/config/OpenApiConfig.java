package com.authkit.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI / Swagger UI.
 * Agrega soporte para Bearer token en Swagger — puedes hacer clic en "Authorize",
 * pegar tu access token y ejecutar los endpoints protegidos directamente desde la UI.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Auth Starter Kit API")
                        .version("1.0.0")
                        .description("""
                            **JWT + TOTP 2FA Authentication API**
                            
                            ### Flujo de autenticación
                            1. `POST /auth/register` — Crear cuenta
                            2. `POST /auth/login` — Si `totpRequired=false` ya tienes tokens.
                               Si `totpRequired=true` → paso 3.
                            3. `POST /auth/login/totp` — Verificar código del authenticator
                            
                            ### Flujo 2FA setup
                            1. `POST /auth/totp/setup` 🔒 — Obtener QR + secret
                            2. Escanear QR con Google Authenticator / Authy
                            3. `POST /auth/totp/enable` 🔒 — Confirmar con código
                            
                            🔒 = Requiere `Authorization: Bearer <access_token>`
                            """))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local dev")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Pega aquí tu access_token para probar los endpoints protegidos")
                        )
                );
    }
}
