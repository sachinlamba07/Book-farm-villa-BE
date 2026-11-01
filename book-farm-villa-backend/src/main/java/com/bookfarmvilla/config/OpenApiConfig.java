package com.bookfarmvilla.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Adds a "Authorize" button to Swagger UI so a JWT can be pasted once and is then
 * automatically attached (as Authorization: Bearer <token>) to every "Try it out"
 * request made from the docs - without this, Swagger UI has no way to send auth
 * headers and every protected endpoint returns 403 when tested from the UI.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI bookFarmVillaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Book Farm Villa API")
                        .description("Farmhouse, villa, wedding-lawn, holiday-home and vendor listing platform - admin/staff managed, call-to-book model.")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT returned by POST /api/auth/login (without the word 'Bearer')")));
    }
}
