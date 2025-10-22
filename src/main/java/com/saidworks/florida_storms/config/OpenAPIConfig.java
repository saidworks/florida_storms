/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Value("${florida.storms.openapi.dev-url:http://localhost:8080}")
    private String devUrl;

    @Value("${florida.storms.openapi.prod-url:https://api.florida-storms.com}")
    private String prodUrl;

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Server URL in Development environment");

        Server prodServer = new Server();
        prodServer.setUrl(prodUrl);
        prodServer.setDescription("Server URL in Production environment");

        Contact contact = new Contact();
        contact.setEmail("saidworks@example.com");
        contact.setName("Said Zitouni");

        License mitLicense =
                new License()
                        .name("Open Source")
                        .url("https://github.com/saidworks/florida_storms");

        Info info =
                new Info()
                        .title("Florida Storms API")
                        .version("1.0")
                        .contact(contact)
                        .description(
                                "This API provides access to historical cyclone and landfall data"
                                        + " for Florida storms. You can filter storms by area,"
                                        + " coordinates, and generate detailed reports.")
                        .license(mitLicense);

        return new OpenAPI().info(info).servers(List.of(devServer, prodServer));
    }
}
