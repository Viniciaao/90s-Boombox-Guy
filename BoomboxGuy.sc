// ===========================================================================
//  90s Boombox Guy  --  remake do "90s Boombox" (Guidopdu)
//  gta3script + CLEO+  /  GTA San Andreas
// ---------------------------------------------------------------------------
//  Um NPC civil ("o cara do som") e chamado por cheat, aparece LONGE (atras
//  de voce, fora da camera), corre ate o player carregando uma caixa de som
//  e passa a te acompanhar com musica 3D saindo da caixa.
//
//  Cheats:
//     BOOBOX      chama o NPC
//     BOOBOXD     dispensa o NPC e para a musica
//     BBGUYTUNE   (opcional) liga/desliga o modo de ajuste da caixa na mao
//
//  Musicas: CLEO/BoomboxGuy/som1.mp3 ... som50.mp3
//     O script CONFERE quais arquivos existem e usa so os que estao la.
//     Pode ter 1, 2, 10, 50 - funciona com qualquer quantidade (ate 50),
//     inclusive com numeracao esburacada (som1, som4, som9...).
//     A ordem e sempre aleatoria (nunca sequencial) e evita repetir a
//     mesma faixa duas vezes seguidas.
//
//  Na tela so aparecem 2 avisos: "nenhuma musica encontrada" e
//  "CLEO+ nao instalado / desatualizado". Todo o resto e silencioso.
//
//  AJUSTE DA CAIXA (opcional, so para quem quer mexer no encaixe da mao):
//     Digite BBGUYTUNE para ligar o modo de ajuste. Com ele ligado:
//        WASD + Q/E          move a caixa (X, Y, Z)
//        SHIFT + WASD + Q/E  gira a caixa
//        F5                  grava os valores em CLEO/BoomboxGuy/ajuste-caixa.txt
//     Os valores aparecem na tela. Anote os que ficarem bons e coloque nas
//     constantes CFG_BOX_* aqui em cima (ou me mande os numeros).
//     Digite BBGUYTUNE de novo para sair do modo de ajuste.
//
//  Requisitos: CLEO 4 + CLEO+ (o script checa o CLEO+ e avisa se faltar)
//
//  Como compilar (veja tambem build.sh / Makefile):
//     gta3sc --config=gtasa --guesser --cs -fcleo -fno-entity-tracking \
//            --add-config=<caminho>/tools/cleo-plus.xml \
//            -o BoomboxGuy.cs BoomboxGuy.sc
// ===========================================================================

SCRIPT_START
SCRIPT_NAME bbguy
{
// ===================== CONFIGURACAO (pode mexer aqui) ======================
    // Skin do NPC (0..311). 7 = male01, o civil mais generico do jogo.
    CONST_INT   CFG_PED_MODEL       7
    // Caixa de som: 2226 = low_hi_fi_3 (objeto nativo do jogo, sem mods).
    CONST_INT   CFG_BOX_MODEL       2226
    // Osso onde a caixa e presa: 24 = mao direita (BONE_R_HAND no Sanny).
    CONST_INT   CFG_BOX_BONE        24

    // ---- pose da caixa na mao (ajustavel no jogo: cheat BBGUYTUNE) ----
    // ATENCAO: o deslocamento e a rotacao usam os eixos DO OSSO da mao, que
    // ficam tortos em relacao ao mundo (por isso os numeros parecem
    // estranhos e a rotacao em Y e -90). Se a caixa ficar torta ou longe da
    // mao no seu jogo, use o BBGUYTUNE: e o jeito certo de acertar isso.
    CONST_FLOAT CFG_BOX_OFF_X      -0.12
    CONST_FLOAT CFG_BOX_OFF_Y       0.0
    CONST_FLOAT CFG_BOX_OFF_Z       0.0
    CONST_FLOAT CFG_BOX_ROT_X       0.0
    CONST_FLOAT CFG_BOX_ROT_Y      -90.0
    CONST_FLOAT CFG_BOX_ROT_Z       0.0
    // tamanho da caixa (1.0 = tamanho original do objeto do jogo)
    CONST_FLOAT CFG_BOX_SCALE       1.0
    // tamanho do passo no modo de ajuste (por toque de tecla)
    CONST_FLOAT CFG_TUNE_POS        0.02
    CONST_FLOAT CFG_TUNE_ROT        5.0


    // Onde o som 3D nasce, em relacao ao corpo do NPC (altura da mao).
    CONST_FLOAT CFG_SND_OFF_X       0.25
    CONST_FLOAT CFG_SND_OFF_Y       0.10
    CONST_FLOAT CFG_SND_OFF_Z       0.75
    CONST_FLOAT CFG_VOLUME          1.0
    // Musicas: som1.mp3 ... somCFG_MAX_TRACKS.mp3 (maximo 50).
    CONST_INT   CFG_MAX_TRACKS      50
    CONST_INT   CFG_PATH_SIZE       64      // tamanho do buffer do caminho
    // Versao minima do CLEO+ exigida (0x01020000 = v1.2.0.0).
    CONST_INT   CFG_CLEOPLUS_MIN    16908288
    // ---- onde ele aparece quando o cheat e digitado (longe de voce) ----
    // Valores negativos em Y = atras do player (a camera fica na frente
    // dele, entao essa e a posicao que costuma ficar fora do campo de visao).
    CONST_FLOAT CFG_SPAWN_BACK    -45.0     // 45 m atras (1a escolha)
    CONST_FLOAT CFG_SPAWN_SIDE    -45.0     // 45 m ao lado (2a escolha)
    CONST_FLOAT CFG_SPAWN_MID     -30.0     // 30 m atras (3a escolha)
    CONST_FLOAT CFG_SPAWN_NEAR    -15.0     // 15 m atras (4a escolha)
    // ---- onde ele reaparece quando e teleportado (perto de voce) ----
    CONST_FLOAT CFG_APPEAR_BACK    -6.0     //  6 m atras (1a escolha)
    CONST_FLOAT CFG_APPEAR_SIDE    -6.0     //  6 m ao lado (2a escolha)
    CONST_FLOAT CFG_APPEAR_FRONT    6.0     //  6 m na frente (3a escolha)
    // ---- outras distancias (os valores D2 sao o quadrado, em metros) ----
    CONST_FLOAT CFG_FOLLOW_D2       9.0     // (3 m) comeca a te seguir
    CONST_FLOAT CFG_SPRINT_D2       400.0   // (20 m) corre mais rapido
    CONST_FLOAT CFG_LOST_D2         3600.0  // (60 m) player longe demais
    CONST_FLOAT CFG_FALL_Z          25.0    // NPC abaixo disso = caiu no vazio
    CONST_FLOAT CFG_STOP_DIST       2.5     // raio em que ele para
    CONST_INT   CFG_TELEPORT_MS     1500    // longe por quanto tempo antes de teleportar
    CONST_INT   CFG_STUCK_MS        6000    // parado por quanto tempo
    CONST_INT   CFG_RETASK_MS       1000    // intervalo entre tarefas de seguir
    CONST_INT   CFG_CAR_RETASK_MS   2000    // intervalo entre tentativas de entrar no carro
    CONST_INT   CFG_LOAD_MS         10000   // timeout ao carregar modelos/mundo
    CONST_INT   CFG_AUDIO_RETRY_MS  20000   // espera para tentar audio de novo
// ===========================================================================

    LVAR_INT   gstate pedBox objBox boxStream playerChar
    LVAR_INT   loadTick nextAudioTick farTick stuckTick taskTick
    LVAR_INT   bufPath trackCount lastTrack flag tmpInt dt tries
    LVAR_FLOAT px py pz nx ny nz dx dy dz
    LVAR_FLOAT boxOX boxOY boxOZ boxRX boxRY boxRZ

    // gstate: 0 = esperando o cheat | 1 = carregando modelos
    //         2 = NPC ativo        | 3 = NPC ativo + modo de ajuste da caixa
    gstate       = 0
    pedBox       = 0
    objBox       = 0
    boxStream    = 0
    playerChar   = 0
    bufPath      = 0
    trackCount   = 0
    lastTrack    = 0
    loadTick     = 0
    nextAudioTick= 0
    taskTick     = 0
    farTick      = -1
    stuckTick    = -1
    flag         = 0
    tmpInt       = 0
    dt           = 0
    tries        = 0
    px           = 0.0
    py           = 0.0
    pz           = 0.0
    boxOX        = CFG_BOX_OFF_X
    boxOY        = CFG_BOX_OFF_Y
    boxOZ        = CFG_BOX_OFF_Z
    boxRX        = CFG_BOX_ROT_X
    boxRY        = CFG_BOX_ROT_Y
    boxRZ        = CFG_BOX_ROT_Z

    // ---------------------------------------------------------------
    //  Inicializacao: confere o CLEO+ e reserva o buffer do caminho
    // ---------------------------------------------------------------
    GOSUB bbg_check_deps
    GOSUB bbg_alloc_path

    WHILE TRUE
        WAIT 0

        // O handle do CJ muda quando ele morre e renasce: atualiza sempre.
        IF IS_PLAYER_PLAYING 0
            GET_PLAYER_CHAR 0 playerChar
        ELSE
            playerChar = 0
        ENDIF

        IF gstate = 0
            // ------------------ parado: espera o cheat ------------------
            IF NOT playerChar = 0
            AND TEST_CHEAT "BOOBOX"
                GOSUB bbg_request
            ENDIF
        ELSE
            // ------------------ cheat BOOBOXD: manda embora -------------
            IF TEST_CHEAT "BOOBOXD"
                GOSUB bbg_dismiss
            ENDIF
            IF gstate = 1
                // ------------------ carregando os modelos ----------------
                GOSUB bbg_wait_models
            ELSE
                IF NOT gstate = 0
                    // ------------------ NPC ativo ------------------------
                    GOSUB bbg_active
                ENDIF
            ENDIF
        ENDIF
    ENDWHILE

    TERMINATE_THIS_CUSTOM_SCRIPT

    // =======================================================================
    //  Checagem de dependencia: o mod precisa do CLEO+ instalado.
    //  Se faltar (ou estiver velho), avisa na tela e encerra sem crashar.
    // =======================================================================
bbg_check_deps:
    IF LOAD_DYNAMIC_LIBRARY "CLEO+.cleo" (tmpInt)
        IF GET_DYNAMIC_LIBRARY_PROCEDURE "GetCleoPlusVersion" tmpInt (dt)
            CALL_FUNCTION_RETURN dt 0 0 ()(tries)
            FREE_DYNAMIC_LIBRARY tmpInt
            IF tries < CFG_CLEOPLUS_MIN
                PRINT_STRING "~r~Boombox Guy~n~~w~Seu CLEO+ esta desatualizado.~n~Atualize o CLEO+ e entre de novo." 9000
                TERMINATE_THIS_CUSTOM_SCRIPT
            ENDIF
            RETURN
        ENDIF
        FREE_DYNAMIC_LIBRARY tmpInt
    ENDIF
    PRINT_STRING "~r~Boombox Guy precisa do CLEO+~n~~w~Instale o CLEO+ (CLEO+.cleo) na pasta CLEO~n~e digite o cheat de novo." 9000
    TERMINATE_THIS_CUSTOM_SCRIPT
    RETURN

    // =======================================================================
    //  Buffer onde o caminho da musica e montado (ex.: CLEO\BoomboxGuy\som7.mp3)
    // =======================================================================
bbg_alloc_path:
    bufPath = 0
    ALLOCATE_MEMORY CFG_PATH_SIZE bufPath
    RETURN

    // =======================================================================
    //  Cheat BOOBOX: pede os modelos e passa para a fase de carregamento
    // =======================================================================
bbg_request:
    IF NOT IS_MODEL_IN_CDIMAGE CFG_PED_MODEL
        RETURN
    ENDIF
    IF NOT IS_MODEL_IN_CDIMAGE CFG_BOX_MODEL
        RETURN
    ENDIF
    REQUEST_MODEL CFG_PED_MODEL
    REQUEST_MODEL CFG_BOX_MODEL
    GET_GAME_TIMER loadTick
    nextAudioTick = 0
    gstate = 1
    RETURN

    // =======================================================================
    //  Espera os modelos E o mundo (colisao) carregarem antes de criar o NPC
    // =======================================================================
bbg_wait_models:
    GET_GAME_TIMER tmpInt
    dt = tmpInt - loadTick
    IF dt > CFG_LOAD_MS
        GOSUB bbg_release_models
        gstate = 0
        RETURN
    ENDIF
    IF HAS_MODEL_LOADED CFG_PED_MODEL
    AND HAS_MODEL_LOADED CFG_BOX_MODEL
        // checagem de colisao: nao cria o NPC antes do mundo estar pronto
        IF IS_CHAR_WAITING_FOR_WORLD_COLLISION playerChar
            RETURN
        ENDIF
        GOSUB bbg_spawn
    ENDIF
    RETURN

    // =======================================================================
    //  Cria o NPC e a caixa de som
    // =======================================================================
bbg_spawn:
    GET_PLAYER_CHAR 0 playerChar
    GET_CHAR_COORDINATES playerChar px py pz

    // aparece longe, de preferencia atras do player (fora da camera)
    GOSUB bbg_pick_spawn

    CREATE_CHAR PEDTYPE_CIVMALE CFG_PED_MODEL nx ny nz pedBox
    IF pedBox = 0
        GOSUB bbg_release_models
        gstate = 0
        RETURN
    ENDIF

    // Vida padrao de NPC e zero reacao ao mundo (decision maker vazio).
    SET_CHAR_MAX_HEALTH pedBox 100
    SET_CHAR_HEALTH pedBox 100
    SET_CHAR_DECISION_MAKER pedBox DM_PED_EMPTY
    TASK_TOGGLE_PED_THREAT_SCANNER pedBox FALSE FALSE FALSE

    // pose da caixa nos valores configurados
    boxOX = CFG_BOX_OFF_X
    boxOY = CFG_BOX_OFF_Y
    boxOZ = CFG_BOX_OFF_Z
    boxRX = CFG_BOX_ROT_X
    boxRY = CFG_BOX_ROT_Y
    boxRZ = CFG_BOX_ROT_Z
    GOSUB bbg_make_box
    GET_GAME_TIMER loadTick          // timer das tentativas de criar a caixa

    GOSUB bbg_play_track             // conta as musicas e sorteia a primeira
    IF trackCount = 0
        PRINT_STRING "~r~Boombox Guy: nenhuma musica encontrada." 7000
    ENDIF
    IF bufPath = 0
        GOSUB bbg_alloc_path
    ENDIF

    // ja manda ele vir correndo atras de voce
    TASK_FOLLOW_PATH_NODES_TO_COORD_WITH_RADIUS pedBox px py pz PEDMOVE_RUN -1 CFG_STOP_DIST
    GET_GAME_TIMER taskTick
    farTick = -1
    stuckTick = -1
    gstate = 2
    RETURN

    // =======================================================================
    //  Cria a caixa na mao do NPC
    //  OBS: o CLEO+ apaga os render objects quando o ped e apagado, por isso
    //  este script NUNCA chama DELETE_RENDER_OBJECT (evita ponteiro invalido).
    // =======================================================================
bbg_make_box:
    objBox = 0
    IF NOT DOES_CHAR_EXIST pedBox
        RETURN
    ENDIF
    CREATE_RENDER_OBJECT_TO_CHAR_BONE pedBox CFG_BOX_MODEL CFG_BOX_BONE boxOX boxOY boxOZ boxRX boxRY boxRZ objBox
    IF NOT objBox = 0
        SET_RENDER_OBJECT_SCALE objBox CFG_BOX_SCALE CFG_BOX_SCALE CFG_BOX_SCALE
    ENDIF
    RETURN

    // =======================================================================
    //  Todo o comportamento do NPC ativo
    // =======================================================================
bbg_active:
    // O jogo pode apagar o NPC sozinho (save, limpeza do mundo, etc).
    IF NOT DOES_CHAR_EXIST pedBox
        GOSUB bbg_forget
        RETURN
    ENDIF
    IF IS_CHAR_DEAD pedBox
        GOSUB bbg_npc_died
        RETURN
    ENDIF

    // Player morreu/recarregou? Dispensa o NPC.
    IF playerChar = 0
        GOSUB bbg_dismiss
        RETURN
    ENDIF

    // ---------------------------------------------------------------------
    //  Modo de ajuste da caixa (cheat BBGUYTUNE) - liga/desliga
    // ---------------------------------------------------------------------
    IF TEST_CHEAT "BBGUYTUNE"
        IF gstate = 2
            gstate = 3
        ELSE
            gstate = 2
        ENDIF
    ENDIF

    // ---------------------------------------------------------------------
    //  CUTSCENE (da campanha ou com script): pausa a musica para nao
    //  sobrepor os dialogos e nao interfere no fluxo da missao.
    // ---------------------------------------------------------------------
    IF IS_ON_CUTSCENE
    OR IS_ON_SCRIPTED_CUTSCENE
        IF NOT boxStream = 0
            SET_AUDIO_STREAM_STATE boxStream 2       // pausa
        ENDIF
        RETURN
    ENDIF

    // Deu problema para criar a caixa? Tenta de novo a cada meio segundo.
    IF objBox = 0
        GET_GAME_TIMER tmpInt
        IF tmpInt > loadTick
            GET_GAME_TIMER loadTick
            loadTick = loadTick + 500
            GOSUB bbg_make_box
        ENDIF
    ENDIF

    // ---------------------------------------------------------------------
    //  O player esta num veiculo? Entao o NPC entra junto (se tiver lugar).
    // ---------------------------------------------------------------------
    IF IS_CHAR_IN_ANY_CAR playerChar
        farTick = -1
        stuckTick = -1
        IF IS_CHAR_IN_ANY_CAR pedBox
            // ele ja esta no veiculo (entrando ou sentado)
            flag = 1
        ELSE
            GET_GAME_TIMER tmpInt
            dt = tmpInt - taskTick
            IF dt > CFG_CAR_RETASK_MS
                GOSUB bbg_enter_car
            ELSE
                flag = 1
            ENDIF
        ENDIF
        IF flag = 1
            IF gstate = 3
                GOSUB bbg_tune
            ENDIF
            GOSUB bbg_audio
            RETURN
        ENDIF
        // nao tem lugar no veiculo: ele vai a pe atras do player
    ENDIF
    // Player saiu do veiculo: o NPC tambem sai.
    IF IS_CHAR_IN_ANY_CAR pedBox
        CLEAR_CHAR_TASKS pedBox
        TASK_LEAVE_ANY_CAR pedBox
        GET_GAME_TIMER taskTick
        RETURN
    ENDIF

    // Posicoes atuais (dz = distancia ao quadrado)
    GET_CHAR_COORDINATES playerChar px py pz
    GET_CHAR_COORDINATES pedBox nx ny nz
    dx = px - nx
    dy = py - ny
    dz = pz - nz
    dx = dx * dx
    dy = dy * dy
    dz = dz * dz
    dx = dx + dy
    dz = dx + dz          // dz = distancia ao quadrado

    // ------------------------------- seguir -------------------------------
    IF dz > CFG_FOLLOW_D2
        GET_GAME_TIMER tmpInt
        dt = tmpInt - taskTick
        IF dt > CFG_RETASK_MS
            GET_GAME_TIMER taskTick
            IF dz > CFG_SPRINT_D2
                TASK_FOLLOW_PATH_NODES_TO_COORD_WITH_RADIUS pedBox px py pz PEDMOVE_SPRINT -1 CFG_STOP_DIST
            ELSE
                TASK_FOLLOW_PATH_NODES_TO_COORD_WITH_RADIUS pedBox px py pz PEDMOVE_RUN -1 CFG_STOP_DIST
            ENDIF
        ENDIF
    ENDIF

    // ----------------------------- teleporte ------------------------------
    // Longe demais (interior, fuga de carro/aviao...)? So teleporta quando o
    // player estiver a pe, no chao, fora da agua e com o mundo ja carregado.
    IF dz > CFG_LOST_D2
        GOSUB bbg_lost_check
    ELSE
        farTick = -1
    ENDIF

    // ------------------------- preso / parado longe -----------------------
    IF dz > CFG_FOLLOW_D2
        GOSUB bbg_stuck_check
    ELSE
        stuckTick = -1
    ENDIF

    // ---------------------------------------------------------------------
    //  NPC caiu no vazio (embaixo do mapa)? Resgata ele.
    // ---------------------------------------------------------------------
    dz = pz - nz
    IF dz > CFG_FALL_Z
        GOSUB bbg_can_teleport
        IF flag = 1
            GOSUB bbg_teleport
            RETURN
        ENDIF
    ENDIF

    // -------------------------------- som --------------------------------
    GOSUB bbg_audio

    // -------------------------- ajuste da caixa --------------------------
    IF gstate = 3
        GOSUB bbg_tune
    ENDIF
    RETURN

    // =======================================================================
    //  Player longe demais: espera um pouco (a pe e no chao) e teleporta
    // =======================================================================
bbg_lost_check:
    GOSUB bbg_can_teleport
    IF flag = 0
        farTick = -1
        RETURN
    ENDIF
    GET_GAME_TIMER tmpInt
    IF farTick < 0
        farTick = tmpInt
    ENDIF
    dt = tmpInt - farTick
    IF dt > CFG_TELEPORT_MS
        GOSUB bbg_teleport
    ENDIF
    RETURN

    // =======================================================================
    //  NPC parado (sem andar) e longe por varios segundos? Ele travou em
    //  algum canto: teleporta para perto do player.
    // =======================================================================
bbg_stuck_check:
    GET_CHAR_MOVE_STATE pedBox tmpInt
    IF tmpInt < MOVE_STATE_WALK_START
        GET_GAME_TIMER tmpInt
        IF stuckTick < 0
            stuckTick = tmpInt
        ENDIF
        dt = tmpInt - stuckTick
        IF dt > CFG_STUCK_MS
            GOSUB bbg_can_teleport
            IF flag = 1
                GOSUB bbg_teleport
            ENDIF
        ENDIF
    ELSE
        stuckTick = -1
    ENDIF
    RETURN

    // =======================================================================
    //  O teleporte e seguro agora?
    //  (player a pe, no chao, fora da agua, fora de veiculo e com o mundo
    //   ja carregado - isso evita o NPC nascer no vacuo)
    // =======================================================================
bbg_can_teleport:
    flag = 0
    IF IS_CHAR_WAITING_FOR_WORLD_COLLISION playerChar
        RETURN
    ENDIF
    IF IS_CHAR_IN_ANY_CAR playerChar
        RETURN
    ENDIF
    IF IS_CHAR_REALLY_IN_AIR playerChar
        RETURN
    ENDIF
    IF IS_CHAR_IN_WATER playerChar
        RETURN
    ENDIF
    flag = 1
    RETURN

    // =======================================================================
    //  Teleporta o NPC para perto do player
    // =======================================================================
bbg_teleport:
    GET_CHAR_COORDINATES playerChar px py pz
    GOSUB bbg_pick_tp

    CLEAR_CHAR_TASKS pedBox
    SET_CHAR_COORDINATES_SIMPLE pedBox nx ny nz
    FIX_CHAR_GROUND_BRIGHTNESS_AND_FADE_IN pedBox TRUE TRUE FALSE
    TASK_FOLLOW_PATH_NODES_TO_COORD_WITH_RADIUS pedBox px py pz PEDMOVE_RUN -1 CFG_STOP_DIST

    GET_GAME_TIMER taskTick
    farTick = -1
    stuckTick = -1
    RETURN

    // =======================================================================
    //  Onde ele aparece quando e chamado (longe, atras do player se der)
    // =======================================================================
bbg_pick_spawn:
    // Atras do player (a camera fica na frente dele, entao nascer atras
    // costuma ficar fora do campo de visao).
    dx = 0.0
    dy = CFG_SPAWN_BACK
    dz = 0.0
    GOSUB bbg_try_off
    IF flag = 0
        dx = CFG_SPAWN_SIDE
        dy = 0.0
        GOSUB bbg_try_off
    ENDIF
    IF flag = 0
        // nada deu certo a 45 m: tenta distancias menores, sempre atras
        dx = 0.0
        dy = CFG_SPAWN_MID
        GOSUB bbg_try_off
    ENDIF
    IF flag = 0
        dx = 0.0
        dy = CFG_SPAWN_NEAR
        GOSUB bbg_try_off
    ENDIF
    IF flag = 0
        dx = 0.0
        dy = CFG_APPEAR_BACK
        GOSUB bbg_try_off
    ENDIF
    IF flag = 0
        // ultimo recurso: no proprio lugar do player (sempre tem chao ali)
        nx = px
        ny = py
        nz = pz
    ENDIF
    RETURN

    // =======================================================================
    //  Onde ele reaparece quando e teleportado (perto, mas atras do player)
    // =======================================================================
bbg_pick_tp:
    dx = 0.0
    dy = CFG_APPEAR_BACK
    dz = 0.0
    GOSUB bbg_try_off
    IF flag = 0
        dx = CFG_APPEAR_SIDE
        dy = 0.0
        GOSUB bbg_try_off
    ENDIF
    IF flag = 0
        dx = 0.0
        dy = CFG_APPEAR_FRONT
        GOSUB bbg_try_off
    ENDIF
    IF flag = 0
        nx = px
        ny = py
        nz = pz
    ENDIF
    RETURN

    // =======================================================================
    //  Testa um deslocamento em relacao ao player (dx dy dz, no espaco local
    //  dele). So aceita se achar o chao perto do chao do player - evita
    //  telhado, ponte, dentro de predio ou o vazio embaixo do mapa.
    //  saida: nx ny nz, flag (1 = posicao valida)
    // =======================================================================
bbg_try_off:
    flag = 0
    GET_OFFSET_FROM_CHAR_IN_WORLD_COORDS playerChar dx dy dz nx ny nz
    REQUEST_COLLISION nx ny
    nz = pz + 2.0
    GET_GROUND_Z_FOR_3D_COORD nx ny nz dx
    dy = pz - dx
    IF dy < 3.0
        IF dy > -3.0
        AND dx > -5.0           // descarta o 0.0 que o jogo devolve sem chao
            nz = dx + 0.5
            flag = 1
        ENDIF
    ENDIF
    RETURN

    // =======================================================================
    //  Entra no veiculo do player (se tiver cadeira de passageiro livre)
    // =======================================================================
bbg_enter_car:
    flag = 0
    GET_GAME_TIMER taskTick
    STORE_CAR_CHAR_IS_IN_NO_SAVE playerChar tries
    IF tries = 0
        RETURN
    ENDIF
    // procura uma cadeira livre: 1 = carona da frente, 2 e 3 = atras
    dt = 1
    tmpInt = 0
    WHILE dt <= 3
    AND tmpInt = 0
        IF IS_CAR_PASSENGER_SEAT_FREE tries dt
            tmpInt = dt
        ELSE
            dt = dt + 1
        ENDIF
    ENDWHILE
    IF tmpInt = 0
        // lotado (ou banco sem carona, tipo moto esportiva): fica de fora
        RETURN
    ENDIF
    CLEAR_CHAR_TASKS pedBox
    TASK_ENTER_CAR_AS_PASSENGER pedBox tries 20000 tmpInt
    flag = 1
    RETURN

    // =======================================================================
    //  Som 3D: segue a mao do NPC, pausa em cutscene e troca de faixa
    // =======================================================================
bbg_audio:
    IF boxStream = 0
        GET_GAME_TIMER tmpInt
        IF tmpInt > nextAudioTick
            GET_GAME_TIMER nextAudioTick
            nextAudioTick = nextAudioTick + CFG_AUDIO_RETRY_MS
            GOSUB bbg_play_track
        ENDIF
        RETURN
    ENDIF
    GET_AUDIO_STREAM_STATE boxStream tmpInt
    IF tmpInt = 2
        // estava pausado por cutscene: retoma de onde parou
        SET_AUDIO_STREAM_STATE boxStream 3
        RETURN
    ENDIF
    IF tmpInt < 1
        // a musica terminou: sorteia outra (nunca em ordem)
        GOSUB bbg_play_track
        RETURN
    ENDIF
    // som na altura da mao (a caixa fica presa no osso da mao)
    GET_OFFSET_FROM_CHAR_IN_WORLD_COORDS pedBox CFG_SND_OFF_X CFG_SND_OFF_Y CFG_SND_OFF_Z dx dy dz
    SET_PLAY_3D_AUDIO_STREAM_AT_COORDS boxStream dx dy dz
    RETURN

    // =======================================================================
    //  Detecta quais MP3 existem em CLEO\BoomboxGuy\ (som1..som50)
    // =======================================================================
bbg_scan_tracks:
    trackCount = 0
    IF bufPath = 0
        RETURN
    ENDIF
    tries = 1
    WHILE tries <= CFG_MAX_TRACKS
        GOSUB bbg_build_path
        IF DOES_FILE_EXIST $bufPath
            trackCount = trackCount + 1
        ENDIF
        tries = tries + 1
    ENDWHILE
    RETURN

    // Monta o caminho da faixa de numero 'tries' no buffer
bbg_build_path:
    STRING_FORMAT bufPath "CLEO\BoomboxGuy\som%d.mp3" tries
    RETURN

    // =======================================================================
    //  Sorteia uma faixa que exista (evita repetir a anterior)
    //  saida: lastTrack (0 = nenhuma faixa encontrada)
    // =======================================================================
bbg_pick_track:
    tmpInt = 0
    dt = 0
    WHILE dt < 12
    AND tmpInt = 0
        dt = dt + 1
        // 0209 sorteia de 'min' ate 'max'-1, entao sorteia 0..49 e soma 1
        GENERATE_RANDOM_INT_IN_RANGE 0 CFG_MAX_TRACKS tries
        IF tries >= CFG_MAX_TRACKS
            tries = 0
        ENDIF
        tries = tries + 1
        GOSUB bbg_build_path
        IF DOES_FILE_EXIST $bufPath
            IF NOT tries = lastTrack
                tmpInt = tries
            ENDIF
        ENDIF
    ENDWHILE
    IF tmpInt = 0
        // numeracao esburacada / azar: varre e pega a primeira que existir
        tries = 1
        WHILE tries <= CFG_MAX_TRACKS
        AND tmpInt = 0
            GOSUB bbg_build_path
            IF DOES_FILE_EXIST $bufPath
                tmpInt = tries
            ENDIF
            tries = tries + 1
        ENDWHILE
    ENDIF
    lastTrack = tmpInt
    RETURN

    // =======================================================================
    //  Carrega e toca a faixa sorteada
    // =======================================================================
bbg_play_track:
    GOSUB bbg_stop_audio
    IF bufPath = 0
        // o ALLOCATE_MEMORY pode ter falhado antes: tenta reservar de novo
        GOSUB bbg_alloc_path
        IF bufPath = 0
            RETURN
        ENDIF
    ENDIF
    IF trackCount = 0
        GOSUB bbg_scan_tracks
        IF trackCount = 0
            RETURN
        ENDIF
    ENDIF
    GOSUB bbg_pick_track
    IF lastTrack = 0
        RETURN
    ENDIF
    tries = lastTrack
    GOSUB bbg_build_path
    LOAD_3D_AUDIO_STREAM $bufPath boxStream
    IF boxStream = 0
        RETURN
    ENDIF
    SET_AUDIO_STREAM_VOLUME boxStream CFG_VOLUME
    SET_AUDIO_STREAM_STATE boxStream 1
    RETURN

    // =======================================================================
    //  Modo de ajuste da caixa na mao (cheat BBGUYTUNE)
    //    WASD + Q/E            move a caixa (X, Y, Z)
    //    SHIFT + WASD + Q/E    gira a caixa
    //  Os valores aparecem na tela; anote e coloque nas constantes CFG_BOX_*
    // =======================================================================
bbg_tune:
    IF IS_KEY_PRESSED VK_LSHIFT
        // ---- rotacao ----
        IF IS_KEY_JUST_PRESSED VK_KEY_A
            boxRX = boxRX - CFG_TUNE_ROT
        ENDIF
        IF IS_KEY_JUST_PRESSED VK_KEY_D
            boxRX = boxRX + CFG_TUNE_ROT
        ENDIF
        IF IS_KEY_JUST_PRESSED VK_KEY_W
            boxRY = boxRY - CFG_TUNE_ROT
        ENDIF
        IF IS_KEY_JUST_PRESSED VK_KEY_S
            boxRY = boxRY + CFG_TUNE_ROT
        ENDIF
        IF IS_KEY_JUST_PRESSED VK_KEY_Q
            boxRZ = boxRZ - CFG_TUNE_ROT
        ENDIF
        IF IS_KEY_JUST_PRESSED VK_KEY_E
            boxRZ = boxRZ + CFG_TUNE_ROT
        ENDIF
    ELSE
        // ---- posicao ----
        IF IS_KEY_JUST_PRESSED VK_KEY_A
            boxOX = boxOX - CFG_TUNE_POS
        ENDIF
        IF IS_KEY_JUST_PRESSED VK_KEY_D
            boxOX = boxOX + CFG_TUNE_POS
        ENDIF
        IF IS_KEY_JUST_PRESSED VK_KEY_W
            boxOY = boxOY + CFG_TUNE_POS
        ENDIF
        IF IS_KEY_JUST_PRESSED VK_KEY_S
            boxOY = boxOY - CFG_TUNE_POS
        ENDIF
        IF IS_KEY_JUST_PRESSED VK_KEY_Q
            boxOZ = boxOZ - CFG_TUNE_POS
        ENDIF
        IF IS_KEY_JUST_PRESSED VK_KEY_E
            boxOZ = boxOZ + CFG_TUNE_POS
        ENDIF
    ENDIF
    IF NOT objBox = 0
        SET_RENDER_OBJECT_POSITION objBox boxOX boxOY boxOZ
        SET_RENDER_OBJECT_ROTATION objBox boxRX boxRY boxRZ
    ENDIF
    IF IS_KEY_JUST_PRESSED VK_F5
        GOSUB bbg_save_tune
    ENDIF
    PRINT_FORMATTED_NOW "~y~caixa~n~~w~pos %.2f %.2f %.2f~n~rot %.0f %.0f %.0f~n~WASD/QE move | SHIFT gira" 300 boxOX boxOY boxOZ boxRX boxRY boxRZ
    RETURN

    // =======================================================================
    //  Grava o ajuste atual em CLEO\BoomboxGuy\ajuste-caixa.txt (tecla F5)
    //  Assim da para copiar os numeros e fixar nas constantes CFG_BOX_*
    // =======================================================================
bbg_save_tune:
    IF bufPath = 0
        RETURN
    ENDIF
    tries = 0
    OPEN_FILE "CLEO\BoomboxGuy\ajuste-caixa.txt" "w" tries
    IF NOT tries = 0
        STRING_FORMAT bufPath "offset %.3f %.3f %.3f  rot %.1f %.1f %.1f" boxOX boxOY boxOZ boxRX boxRY boxRZ
        WRITE_STRING_TO_FILE tries $bufPath
        CLOSE_FILE tries
    ENDIF
    RETURN

    // =======================================================================
    //  Fim do NPC
    // =======================================================================
bbg_npc_died:
    // Vida padrao: morreu, acabou. A caixa fica com o corpo e o CLEO+ apaga
    // ela junto com o ped. So volta a tocar se digitar o cheat de novo.
    GOSUB bbg_stop_audio
    MARK_CHAR_AS_NO_LONGER_NEEDED pedBox
    pedBox = 0
    objBox = 0
    GOSUB bbg_release_models
    gstate = 0
    RETURN

    // O jogo apagou o NPC sem ser por nossa conta: so esquece os handles.
bbg_forget:
    GOSUB bbg_stop_audio
    pedBox = 0
    objBox = 0
    GOSUB bbg_release_models
    gstate = 0
    RETURN

    // Cheat BOOBOXD (ou player morto): manda o NPC embora e para o som
bbg_dismiss:
    GOSUB bbg_stop_audio
    IF DOES_CHAR_EXIST pedBox
        IF NOT objBox = 0
            SET_RENDER_OBJECT_VISIBLE objBox FALSE
        ENDIF
        MARK_CHAR_AS_NO_LONGER_NEEDED pedBox
        REMOVE_CHAR_ELEGANTLY pedBox
    ENDIF
    pedBox = 0
    objBox = 0
    GOSUB bbg_release_models
    gstate = 0
    RETURN

    // =======================================================================
    //  Utilitarios
    // =======================================================================
bbg_stop_audio:
    IF NOT boxStream = 0
        REMOVE_AUDIO_STREAM boxStream
        boxStream = 0
    ENDIF
    RETURN

bbg_release_models:
    // pedido e liberado sempre em par (mesmo se NPC e caixa forem o mesmo
    // modelo, a contagem continua certa)
    MARK_MODEL_AS_NO_LONGER_NEEDED CFG_PED_MODEL
    MARK_MODEL_AS_NO_LONGER_NEEDED CFG_BOX_MODEL
    RETURN
}
SCRIPT_END
