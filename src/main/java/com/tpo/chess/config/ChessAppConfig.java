package com.tpo.chess.config;

import com.tpo.chess.core.Game;
import com.tpo.chess.core.rules.RuleValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Único punto de contacto entre Spring y el núcleo: fabrica los objetos del dominio
 * como beans, manteniendo al paquete {@code core} libre de anotaciones del framework.
 */
@Configuration
public class ChessAppConfig {

    @Bean
    public RuleValidator ruleValidator() {
        return new RuleValidator();
    }

    @Bean
    public Game game(RuleValidator ruleValidator) {
        return new Game(ruleValidator);
    }
}
