/* =========================================================
   Ajedrez TPO — cliente web del adaptador REST
   Toda la lógica de reglas vive en el servidor (core Java);
   este archivo solo dibuja el estado y envía jugadas.
   ========================================================= */
(() => {
    'use strict';

    const API = '/api/game';
    const FILES = 'abcdefgh';
    const TEXT_VS = '\uFE0E'; // fuerza el glifo como texto (no emoji)
    const GLYPHS = { KING: '♚', QUEEN: '♛', ROOK: '♜', BISHOP: '♝', KNIGHT: '♞', PAWN: '♟' };
    const LETTER = { KING: 'R', QUEEN: 'D', ROOK: 'T', BISHOP: 'A', KNIGHT: 'C', PAWN: '' };
    const VALUE = { PAWN: 1, KNIGHT: 3, BISHOP: 3, ROOK: 5, QUEEN: 9, KING: 0 };
    const COLOR_ES = { WHITE: 'Blancas', BLACK: 'Negras' };

    const $ = (id) => document.getElementById(id);
    const boardEl = $('board');

    let state = null;
    let selected = null;
    let targets = [];
    let flipped = false;
    let busy = false;
    const squareEls = new Map();

    /* ---------------- HTTP ---------------- */
    async function api(path = '', options = {}) {
        const res = await fetch(API + path, {
            headers: { 'Content-Type': 'application/json' },
            ...options,
        });
        const body = await res.json().catch(() => ({}));
        if (!res.ok) {
            const err = new Error(body.message || 'Error de comunicación con el servidor');
            err.code = body.code;
            throw err;
        }
        return body;
    }

    /* ---------------- Board construction ---------------- */
    function buildBoard() {
        boardEl.innerHTML = '';
        squareEls.clear();
        for (let displayRow = 0; displayRow < 8; displayRow++) {
            for (let displayCol = 0; displayCol < 8; displayCol++) {
                const rank = flipped ? displayRow + 1 : 8 - displayRow;
                const fileIdx = flipped ? 7 - displayCol : displayCol;
                const square = FILES[fileIdx] + rank;

                const el = document.createElement('div');
                el.className = 'square ' + ((fileIdx + rank) % 2 === 0 ? 'light' : 'dark');
                el.id = 'sq-' + square;
                el.dataset.square = square;
                el.setAttribute('role', 'gridcell');
                el.setAttribute('aria-label', square);

                if (displayCol === 0) el.appendChild(coord('rank', rank));
                if (displayRow === 7) el.appendChild(coord('file', FILES[fileIdx]));

                el.addEventListener('click', () => onSquareClick(square));
                boardEl.appendChild(el);
                squareEls.set(square, el);
            }
        }
    }

    function coord(kind, text) {
        const span = document.createElement('span');
        span.className = 'coord ' + kind;
        span.textContent = text;
        return span;
    }

    /* ---------------- Rendering ---------------- */
    function render(animateMove = null) {
        if (!state) return;
        const pieces = new Map(state.pieces.map((p) => [p.square, p]));
        const last = state.history[state.history.length - 1];
        const checkedKing = (state.status === 'CHECK' || state.status === 'CHECKMATE')
            ? state.pieces.find((p) => p.type === 'KING' && p.color === state.turn)?.square
            : null;

        squareEls.forEach((el, square) => {
            el.querySelector('.piece')?.remove();
            el.classList.remove('last-move', 'selected', 'target', 'capture', 'in-check', 'movable');

            const piece = pieces.get(square);
            if (piece) {
                const span = document.createElement('span');
                span.className = 'piece ' + piece.color.toLowerCase();
                span.textContent = GLYPHS[piece.type] + TEXT_VS;
                el.appendChild(span);
                if (piece.color === state.turn && !isFinished()) el.classList.add('movable');
            }
            if (last && (square === last.from || square === last.to)) el.classList.add('last-move');
            if (square === selected) el.classList.add('selected');
            if (targets.includes(square)) {
                el.classList.add('target');
                if (piece) el.classList.add('capture');
            }
            if (square === checkedKing) el.classList.add('in-check');
        });

        if (animateMove) animatePiece(animateMove.from, animateMove.to, animateMove.fromRect);
        renderStatus();
        renderPlayers();
        renderHistory();
        renderGameOver();
    }

    /** Animación FLIP: la pieza "viaja" desde la casilla de origen a la de destino. */
    function animatePiece(from, to, fromRect) {
        const toEl = squareEls.get(to);
        const piece = toEl?.querySelector('.piece');
        if (!piece || !fromRect) return;
        const toRect = toEl.getBoundingClientRect();
        const dx = fromRect.left - toRect.left;
        const dy = fromRect.top - toRect.top;
        piece.style.transition = 'none';
        piece.style.transform = `translate(${dx}px, ${dy}px)`;
        piece.style.zIndex = 20;
        requestAnimationFrame(() => requestAnimationFrame(() => {
            piece.style.transition = 'transform .32s cubic-bezier(.22,1,.36,1)';
            piece.style.transform = '';
            piece.addEventListener('transitionend', () => {
                piece.style.transition = '';
                piece.style.zIndex = '';
            }, { once: true });
        }));
    }

    function isFinished() {
        return state.status === 'CHECKMATE' || state.status === 'STALEMATE';
    }

    function renderStatus() {
        const dot = $('turn-dot');
        dot.className = 'turn-dot ' + state.turn.toLowerCase();
        $('turn-text').textContent = COLOR_ES[state.turn];

        const badge = $('status-badge');
        const hint = $('hint');
        switch (state.status) {
            case 'CHECK':
                badge.className = 'badge badge-check';
                badge.textContent = '¡Jaque!';
                hint.textContent = `El rey de las ${COLOR_ES[state.turn].toLowerCase()} está en jaque: solo se permiten jugadas que lo saquen del ataque.`;
                break;
            case 'CHECKMATE':
                badge.className = 'badge badge-over';
                badge.textContent = 'Jaque mate';
                hint.textContent = `Ganan las ${COLOR_ES[state.winner].toLowerCase()}.`;
                break;
            case 'STALEMATE':
                badge.className = 'badge badge-over';
                badge.textContent = 'Ahogado';
                hint.textContent = 'Tablas: el jugador con el turno no tiene jugadas legales y no está en jaque.';
                break;
            default:
                badge.className = 'badge badge-neutral';
                badge.textContent = 'En juego';
                hint.textContent = selected
                    ? `${targets.length} movimiento${targets.length === 1 ? '' : 's'} legal${targets.length === 1 ? '' : 'es'} desde ${selected}.`
                    : 'Tocá una pieza para ver sus movimientos legales.';
        }
    }

    function renderPlayers() {
        const topColor = flipped ? 'WHITE' : 'BLACK';
        const bottomColor = flipped ? 'BLACK' : 'WHITE';
        setPlayer('top', topColor);
        setPlayer('bottom', bottomColor);
    }

    function setPlayer(slot, color) {
        const bar = $('player-' + slot);
        const avatar = bar.querySelector('.player-avatar');
        avatar.className = 'player-avatar ' + color.toLowerCase();
        avatar.textContent = color === 'WHITE' ? '♔' : '♚';
        $('name-' + slot).textContent = COLOR_ES[color];
        bar.classList.toggle('active', state.turn === color && !isFinished());

        // Piezas que ESTE jugador capturó (son del color contrario).
        const captured = state.history.filter((m) => m.color === color && m.captured).map((m) => m.captured);
        captured.sort((a, b) => VALUE[b] - VALUE[a]);
        const material = (c) => state.pieces.filter((p) => p.color === c).reduce((s, p) => s + VALUE[p.type], 0);
        const diff = material(color) - material(color === 'WHITE' ? 'BLACK' : 'WHITE');

        const box = $('captured-' + slot);
        box.innerHTML = captured.map((t) => `<span class="glyph">${GLYPHS[t]}${TEXT_VS}</span>`).join('')
            + (diff > 0 ? `<span class="advantage">+${diff}</span>` : '');
    }

    function renderHistory() {
        const list = $('history');
        const moves = state.history;
        $('move-count').textContent = `${moves.length} jugada${moves.length === 1 ? '' : 's'}`;
        $('history-empty').classList.toggle('hidden', moves.length > 0);

        list.innerHTML = '';
        for (let i = 0; i < moves.length; i += 2) {
            const li = document.createElement('li');
            li.innerHTML = `<span class="num">${i / 2 + 1}.</span>${ply(moves[i], i === moves.length - 1)}${moves[i + 1] ? ply(moves[i + 1], i + 1 === moves.length - 1) : '<span></span>'}`;
            list.appendChild(li);
        }
        list.scrollTop = list.scrollHeight;
    }

    function ply(m, latest) {
        const notation = `${LETTER[m.piece]}${m.from}${m.captured ? '×' : '–'}${m.to}${m.promotion ? '=D' : ''}`;
        return `<span class="ply${latest ? ' latest' : ''}"><span class="glyph">${GLYPHS[m.piece]}${TEXT_VS}</span>${notation}</span>`;
    }

    function renderGameOver() {
        const overlay = $('game-over');
        if (!isFinished()) {
            overlay.classList.add('hidden');
            return;
        }
        overlay.classList.remove('hidden');
        if (state.status === 'CHECKMATE') {
            $('game-over-icon').textContent = (state.winner === 'WHITE' ? '♔' : '♚') + TEXT_VS;
            $('game-over-title').textContent = 'Jaque mate';
            $('game-over-text').textContent = `Ganan las ${COLOR_ES[state.winner].toLowerCase()} en ${Math.ceil(state.history.length / 2)} jugadas.`;
        } else {
            $('game-over-icon').textContent = '½';
            $('game-over-title').textContent = 'Tablas por ahogado';
            $('game-over-text').textContent = 'Nadie gana esta vez.';
        }
    }

    /* ---------------- Interaction ---------------- */
    async function onSquareClick(square) {
        if (!state || busy || isFinished()) return;

        if (selected && targets.includes(square)) {
            await playMove(selected, square);
            return;
        }

        const piece = state.pieces.find((p) => p.square === square);
        if (!piece || square === selected) {
            clearSelection();
            return;
        }
        if (piece.color !== state.turn) {
            clearSelection();
            toast(`Es el turno de las ${COLOR_ES[state.turn].toLowerCase()}.`);
            return;
        }

        try {
            const res = await api('/moves?from=' + square);
            selected = square;
            targets = res.moves;
            render();
            if (targets.length === 0) toast('Esa pieza no tiene movimientos legales.');
        } catch (e) {
            toast(e.message);
        }
    }

    async function playMove(from, to) {
        busy = true;
        const fromRect = squareEls.get(from).getBoundingClientRect();
        try {
            state = await api('/move', { method: 'POST', body: JSON.stringify({ from, to }) });
            selected = null;
            targets = [];
            render({ from, to, fromRect });
        } catch (e) {
            boardEl.classList.remove('shake');
            void boardEl.offsetWidth;
            boardEl.classList.add('shake');
            toast(e.message);
            clearSelection();
        } finally {
            busy = false;
        }
    }

    function clearSelection() {
        selected = null;
        targets = [];
        render();
    }

    async function resetGame() {
        try {
            state = await api('/reset', { method: 'POST' });
            selected = null;
            targets = [];
            render();
        } catch (e) {
            toast(e.message);
        }
    }

    function flipBoard() {
        flipped = !flipped;
        buildBoard();
        render();
    }

    let toastTimer;
    function toast(message) {
        const el = $('toast');
        el.textContent = message;
        el.classList.add('show');
        clearTimeout(toastTimer);
        toastTimer = setTimeout(() => el.classList.remove('show'), 2600);
    }

    /* ---------------- Boot ---------------- */
    $('reset-btn').addEventListener('click', resetGame);
    $('play-again-btn').addEventListener('click', resetGame);
    $('flip-btn').addEventListener('click', flipBoard);
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') clearSelection();
        if (e.key.toLowerCase() === 'f' && !e.ctrlKey && !e.metaKey) flipBoard();
    });

    buildBoard();
    api().then((s) => { state = s; render(); })
        .catch(() => toast('No se pudo conectar con el servidor de ajedrez.'));
})();
