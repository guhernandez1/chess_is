package com.tpo.chess.core;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test de arquitectura: garantiza la consigna "el núcleo no debe depender de ningún
 * framework". Falla si algún archivo de {@code core} importa Spring o Jakarta.
 */
class CoreArchitectureTest {

    private static final Path CORE_SOURCES = Path.of("src", "main", "java", "com", "tpo", "chess", "core");
    private static final List<String> FORBIDDEN = List.of("org.springframework", "jakarta.", "javax.");

    @Test
    void coreDoesNotDependOnAnyFramework() throws IOException {
        assertTrue(Files.isDirectory(CORE_SOURCES), "No se encontró el código del núcleo en " + CORE_SOURCES);
        try (Stream<Path> files = Files.walk(CORE_SOURCES)) {
            List<Path> javaFiles = files.filter(p -> p.toString().endsWith(".java")).toList();
            assertFalse(javaFiles.isEmpty());
            for (Path file : javaFiles) {
                String source = Files.readString(file, StandardCharsets.UTF_8);
                for (String forbidden : FORBIDDEN) {
                    assertFalse(source.contains(forbidden),
                            file + " depende de '" + forbidden + "', violando la independencia del núcleo");
                }
            }
        }
    }
}
