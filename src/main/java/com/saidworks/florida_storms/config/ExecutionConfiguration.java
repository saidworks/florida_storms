/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ExecutionConfiguration {
    @Value("${cyclone.data.hurdat2}")
    private Resource sourceFile;

//    @Bean
//    public CycloneProcessingOrchestrator cycloneProcessingOrchestrator() {
//        return new CycloneProcessingOrchestrator();
//    }
//
//    @Bean
//    public CycloneDataExample cycloneDataExample() {
//        return new CycloneDataExample(new CycloneDataParser());
//    }
//
//    @Bean
//    public CommandLineRunner commandLineRunner(CycloneDataExample cycloneDataExample) {
//        return _ -> {
//            cycloneDataExample.start();
//        };
//    }
}
