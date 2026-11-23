package com.tpo.chess.config;

import com.tpo.chess.core.Game;
import com.tpo.chess.core.rules.RuleValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra el dominio como beans sin introducir anotaciones de Spring en el núcleo.
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
