# Justificación de Decisiones de Diseño

Este documento detalla únicamente las **decisiones propias tomadas por el grupo** sobre el diseño del motor de ajedrez (dejando fuera las restricciones impuestas por la cátedra, como el aislamiento del núcleo respecto del framework o la preferencia por composición sobre herencia).

---

### 1. Representación del tablero con `Map<Position, Piece>` en lugar de matriz fija `Piece[8][8]`

- **Qué:** `Board` almacena las piezas ocupadas en un `HashMap<Position, Piece>`, usando el `record Position(int row, int col)` como clave inmutable; las casillas vacías simplemente no existen en el mapa.
- **Por qué:** Simplifica consultas frecuentes del motor como `getPiecesOf(Color)` o `findKing(Color)` sin recorrer 64 casillas llenas de `null`, elimina riesgos de `NullPointerException` al iterar piezas vivas y vuelve trivial la copia superficial del tablero (`new Board(grid)`). Además, al ser `Position` un `record`, `equals` y `hashCode` vienen garantizados por el lenguaje.
- **Cuándo se rompería:** Si el motor evolucionara hacia una IA con búsqueda alfa-beta profunda (millones de nodos por segundo), donde el overhead de hashing, boxing y objetos `Position` en memoria degradaría el rendimiento y obligaría a migrar a bitboards (`long`) o arreglos primitivos de tamaño fijo.

---

### 2. Simulación de jaques por copia superficial (`Board.copy()`) en lugar de `makeMove` / `unmakeMove` in-place

- **Qué:** Para verificar si una jugada deja al rey propio en jaque (`RuleValidator.wouldBeInCheck`), se clona el `Board` con `board.copy()`, se ejecuta `movePiece` sobre la copia y se descarta al finalizar.
- **Por qué:** Como `Piece` y `Position` son completamente inmutables, copiar solo el mapa interno es seguro, barato para el volumen de jugadas de una partida interactiva y **elimina una fuente clásica de bugs**: olvidar restaurar piezas capturadas, promociones o estado previo al deshacer una jugada en `unmakeMove`.
- **Cuándo se rompería:** En motores de cálculo intensivo (bot con minimax/Monte Carlo) que evalúan decenas de miles de tableros por turno, donde asignar un `HashMap` nuevo por cada movimiento pseudo-legal generaría presión excesiva sobre el Garbage Collector. También se rompería si en el futuro `Piece` pasara a ser mutable (por ejemplo, guardando su propio flag `hasMoved`).

---

### 3. Estrategias de movimiento sin estado compartidas en `PieceFactory` + `RayCaster` por delegación

- **Qué:** Las implementaciones de `MovementStrategy` (`LinearMovement`, `DiagonalMovement`, `KnightMovement`, `PawnMovement`) no guardan estado de la pieza ni del tablero. `PieceFactory` reutiliza instancias estáticas compartidas entre todas las piezas (por ejemplo, una única `LinearMovement(1)` y `DiagonalMovement(1)` para ambos reyes), y `LinearMovement`/`DiagonalMovement` delegan el trazado de rayos a la clase utilitaria `RayCaster` en vez de heredar de una clase base abstracta `SlidingMovement`.
- **Por qué:** Evita instanciar estrategias repetidas por cada pieza, elimina jerarquías de herencia innecesarias incluso dentro del paquete `movement` y mantiene cada estrategia enfocada en definir únicamente sus vectores y límites de paso.
- **Cuándo se rompería:** Si se incorporaran piezas con estado propio ligado a su historial de movimiento (por ejemplo, enroque que requiera saber si rey/torre ya se movieron, o captura al paso que dependa de la jugada inmediatamente anterior). En ese caso, la firma `getValidMoves(Board, Position, Piece)` o las estrategias stateless quedarían cortas y habría que pasar un contexto de partida/historial o estado adicional.

---

### 4. Reutilización de `getPseudoLegalMoves` para detectar ataques (`RuleValidator.isSquareAttacked`)

- **Qué:** Para saber si una casilla (en particular la del rey) está atacada por el bando contrario, `isSquareAttacked` consulta si alguna pieza enemiga incluye esa casilla en su `getPseudoLegalMoves(board, pos)`.
- **Por qué:** Evita duplicar la lógica geométrica de amenazas por cada tipo de pieza. Funciona exactamente para el jaque porque la casilla del rey siempre está ocupada por una pieza enemiga del atacante, por lo que el peón solo la alcanza con su captura diagonal y nunca con su avance frontal a casillas vacías.
- **Cuándo se rompería:** Al implementar el **enroque**, donde hay que verificar que el rey no *pase* por casillas **vacías** atacadas por el rival: en una casilla vacía, `PawnMovement.getPseudoLegalMoves` devolvería el avance vertical (que no es un ataque) y *no* incluiría la amenaza diagonal del peón (que exige pieza enemiga en destino). En ese escenario habría que separar explícitamente `getAttackedSquares` de `getPseudoLegalMoves`.

---

### 5. Retorno con enum `MoveResult` en `Game.tryMove` en lugar de excepciones de dominio

- **Qué:** `Game.tryMove(start, end)` devuelve un valor tipado de `MoveResult` (`SUCCESS`, `GAME_OVER`, `OUT_OF_BOARD`, `NO_PIECE`, `NOT_YOUR_TURN`, `ILLEGAL_MOVE`) para informar rechazos de jugadas, reservando las excepciones únicamente para argumentos nulos o invariantes rotos.
- **Por qué:** En un juego por turnos, intentar mover una pieza rival o hacer un movimiento ilegal es un flujo normal de interacción, no un fallo excepcional del sistema. Devolver `MoveResult` permite al adaptador REST mapear cada caso de negocio a códigos HTTP semánticos (`409 Conflict` para turno/partida terminada, `422 Unprocessable Content` para jugada ilegal) sin usar `try/catch` como control de flujo ni acoplar el núcleo a excepciones HTTP.
- **Cuándo se rompería:** Si las reglas de negocio crecieran en complejidad y cada rechazo necesitara transportar datos dinámicos ricos (por ejemplo, coordenadas exactas de la pieza clavada o múltiples motivos simultáneos), momento en que un `enum` plano quedaría corto y convendría migrar a una jerarquía sellada (`sealed interface MoveResult` con `record` de error).

---

### 6. Fachada `Game` sincronizada como singleton en memoria (`synchronized` + `@Bean`)

- **Qué:** `ChessAppConfig` expone una única instancia de `Game` como `@Bean` singleton y `Game` sincroniza sus métodos públicos (`synchronized`) devolviendo copias defensivas (`board.copy()`, `List.copyOf(history)`).
- **Por qué:** Para el alcance del MVP (una partida activa servida por Spring Boot y consumida desde la UI web), evita introducir base de datos, manejo de sesiones o repositorios de partidas, garantizando al mismo tiempo consistencia ante requests concurrentes (por ejemplo, doble clic en el frontend) y protegiendo el estado interno frente a mutaciones externas.
- **Cuándo se rompería:** Si se requiriera soportar múltiples partidas simultáneas entre distintos usuarios, persistencia ante reinicios del servidor o escalado horizontal con más de una instancia de la aplicación; en ese caso el estado debería salir del singleton en memoria hacia un repositorio indexado por `gameId` y la sincronización pasaría a nivel de partida o almacenamiento externo.

