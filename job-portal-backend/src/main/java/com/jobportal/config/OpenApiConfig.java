package com.jobportal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI jobPortalOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Job Portal API")
                        .description("Versioned API docs for Job Portal backend")
                        .version("v1")
                        .contact(new Contact().name("Job Portal Team").email("support@jobportal.local"))
                        .license(new License().name("Apache 2.0")));
    }
}
