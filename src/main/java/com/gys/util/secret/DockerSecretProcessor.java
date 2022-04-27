package com.gys.util.secret;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class DockerSecretProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        Optional.ofNullable(environment.getProperty("docker.secret.path"))
                .ifPresent(dir -> {

                    try {
                        Map<String, Object> propertyMap = Files.list(Paths.get(dir))
                                .collect(Collectors.toMap(
                                        path -> "docker.secret." + path.toFile().getName(),
                                        path -> {
                                            try {
                                                return new String(Files.readAllBytes(path));
                                            } catch (Exception e) {
                                                throw new RuntimeException(e);
                                            }
                                        }));

                        System.out.println(String.format("Found %d docker secret(s); processing...", propertyMap.size()));

                        environment.getPropertySources()
                                .addLast(new MapPropertySource("docker.secret", propertyMap));

                    } catch (Exception e) {
                        System.out.println("Docker secrets cannot be parsed due to: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
    }
}