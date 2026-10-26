# Ajedrez TPO — Java + Spring Boot (Arquitectura Hexagonal)

[![Jugar Online](https://img.shields.io/badge/🎮%20JUGAR%20ONLINE-chess--is.onrender.com-success?style=for-the-badge&logo=googlechrome&logoColor=white)](https://chess-is.onrender.com)
[![Status](https://img.shields.io/badge/Status-Online%20en%20Render-blue?style=for-the-badge)](https://chess-is.onrender.com)

> ♟️ **Demo en vivo:** Podés jugar directamente desde el navegador ingresando a **[https://chess-is.onrender.com](https://chess-is.onrender.com)**.

Diseño completo y justificaciones: ver [implementation_plan.md](implementation_plan.md).

## Requisitos
- JDK 17+ (no hace falta instalar Maven: se usa el wrapper `mvnw`).

## Comandos
```powershell
.\mvnw.cmd test              # tests (núcleo con JUnit 5 puro + adaptador con MockMvc)
.\mvnw.cmd spring-boot:run   # levanta la app en http://localhost:8080
```

## Estructura
| Paquete | Rol | ¿Depende de Spring? |
|---|---|---|
| `core.models` | `Position` (record), `Piece`, `Color`, `PieceType`, `Move`, `GameStatus`, `MoveResult` | No |
| `core.board` | `Board`, `BoardFactory`, `PieceFactory` (compone estrategias) | No |
| `core.movement` | `MovementStrategy` + `Linear`, `Diagonal`, `Knight`, `Pawn` | No |
| `core.rules` | `RuleValidator` (legalidad, jaque, mate, ahogado) | No |
| `core.Game` | Fachada: turnos, coronación, historial | No |
| `config` | `ChessAppConfig` (`@Bean` del núcleo) | Sí |
| `adapter` | `GameController` REST + DTOs + notación algebraica | Sí |

`CoreArchitectureTest` falla si algún archivo de `core` importa `org.springframework`, `jakarta.` o `javax.`.

## API REST
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/game` | Estado (piezas, turno, estado, ganador, historial) |
| GET | `/api/game/moves?from=e2` | Movimientos legales de una pieza |
| POST | `/api/game/move` `{"from":"e2","to":"e4"}` | Jugar (422 ilegal, 409 turno/partida terminada, 400 notación inválida) |
| POST | `/api/game/reset` | Nueva partida |

## Fuera del alcance del MVP
Enroque, captura al paso, elegir la pieza al coronar (se corona siempre a Dama) y tablas por repetición o por la regla de 50 movimientos.
