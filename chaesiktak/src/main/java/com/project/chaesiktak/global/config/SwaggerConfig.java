package com.project.chaesiktak.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("채식탁 API 문서")
                        .version("1.0.0")
                        .description("채식탁 프로젝트 API 문서입니다.")
                        .contact(new Contact()
                                .name("채식탁BE 윤준석, 홍서현")
                                .url("https://github.com/chaesiktak/chaesiktak-BE"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
