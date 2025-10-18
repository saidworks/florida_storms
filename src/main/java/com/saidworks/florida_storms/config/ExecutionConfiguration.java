/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.config;

import com.saidworks.florida_storms.io.CycloneDataExample;
import com.saidworks.florida_storms.io.CycloneDataParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ExecutionConfiguration {
    @Value("${cyclone.data.hurdat2}")
    private Resource sourceFile;
    @Bean
    public CycloneDataExample cycloneDataExample() {
        return new CycloneDataExample(new CycloneDataParser(sourceFile));
    }

    @Bean
    public CommandLineRunner commandLineRunner(CycloneDataExample cycloneDataExample) {
        return _ -> {
            cycloneDataExample.start();
        };
    }
}
