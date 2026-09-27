// ===========================================================================
//  90s Boombox Guy  --  remake do "90s Boombox" (Guidopdu)
//  gta3script + CLEO+  /  GTA San Andreas
// ---------------------------------------------------------------------------
//  Um NPC civil ("o cara do som") e chamado por cheat, aparece perto do
//  player, corre ate ele e passa a acompanhar carregando uma caixa de som,
//  com musica 3D saindo da caixa.
//
//  Cheats:
//     BOOBOX    chama o NPC
//     BOOBOXD   dispensa o NPC e para a musica
//
//  Musicas: CLEO/BoomboxGuy/som1.mp3 ... som50.mp3
//     O script CONFERE quais arquivos existem e usa so os que estao la.
//     Pode ter 1, 2, 10, 50 - funciona com qualquer quantidade (ate 50),
//     inclusive com numeracao esburacada (som1, som4, som9...).
//     A ordem e sempre aleatoria (nunca sequencial) e evita repetir a
//     mesma faixa duas vezes seguidas.
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
    // Osso onde a caixa e presa: 24 = mao direita (BONE_R_HAND).
    CONST_INT   CFG_BOX_BONE        24
    // Posicao/rotacao fina da caixa na mao.
    CONST_FLOAT CFG_BOX_OFF_X       0.22
    CONST_FLOAT CFG_BOX_OFF_Y       0.10
    CONST_FLOAT CFG_BOX_OFF_Z       0.05
    CONST_FLOAT CFG_BOX_ROT_X       0.0
    CONST_FLOAT CFG_BOX_ROT_Y       90.0
    CONST_FLOAT CFG_BOX_ROT_Z       0.0
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
    // Distancias (os valores D2 sao o quadrado da distancia, em metros).
    CONST_FLOAT CFG_APPEAR_DIST     4.0     // onde ele aparece/teleporta
    CONST_FLOAT CFG_FOLLOW_D2       9.0     // (3 m) comeca a te seguir
    CONST_FLOAT CFG_SPRINT_D2       400.0   // (20 m) corre mais rapido
    CONST_FLOAT CFG_LOST_D2         3600.0  // (60 m) player longe demais
    CONST_FLOAT CFG_JUMP_D2         625.0   // (25 m) "salto" do player = interior
    CONST_FLOAT CFG_FALL_Z          25.0    // NPC abaixo disso = caiu no vazio
    CONST_FLOAT CFG_STOP_DIST       2.5     // raio em que ele para
    CONST_INT   CFG_TELEPORT_MS     3000    // longe por quanto tempo
    CONST_INT   CFG_STUCK_MS        6000    // parado por quanto tempo
    CONST_INT   CFG_RETASK_MS       1000    // intervalo entre tarefas
    CONST_INT   CFG_LOAD_MS         10000   // timeout ao carregar modelos/mundo
    CONST_INT   CFG_AUDIO_RETRY_MS  20000   // espera para tentar audio de novo
// ===========================================================================

    LVAR_INT   gstate pedBox objBox boxStream playerChar
    LVAR_INT   loadTick nextAudioTick farTick stuckTick taskTick
    LVAR_INT   bufPath trackCount lastTrack flag tmpInt dt tries
    LVAR_FLOAT px py pz nx ny nz dx dy dz d2
    LVAR_FLOAT heading spawnAng prevPX prevPY

    // gstate: 0 = esperando o cheat | 1 = carregando modelos | 2 = ativo
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
    prevPX       = 0.0
    prevPY       = 0.0

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
            IF gstate = 1
                // ------------------ carregando os modelos ----------------
                GOSUB bbg_wait_models
            ELSE
                // ------------------ NPC ativo ----------------------------
                GOSUB bbg_active
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
    IF bufPath = 0
        PRINT_STRING "~r~Boombox Guy: falha ao reservar memoria para os caminhos." 6000
    ENDIF
    RETURN

    // =======================================================================
    //  Cheat BOOBOX: pede os modelos e passa para a fase de carregamento
    // =======================================================================
bbg_request:
    IF NOT IS_MODEL_IN_CDIMAGE CFG_PED_MODEL
        PRINT_FORMATTED_NOW "~r~Boombox Guy: o modelo %d nao existe neste GTA." 4000 CFG_PED_MODEL
        RETURN
    ENDIF
    IF NOT IS_MODEL_IN_CDIMAGE CFG_BOX_MODEL
        PRINT_FORMATTED_NOW "~r~Boombox Guy: o objeto %d nao existe neste GTA." 4000 CFG_BOX_MODEL
        RETURN
    ENDIF
    REQUEST_MODEL CFG_PED_MODEL
    REQUEST_MODEL CFG_BOX_MODEL
    GET_GAME_TIMER loadTick
    nextAudioTick = 0
    gstate = 1
    PRINT_STRING "~y~Boombox Guy: chamando o cara do som..." 2000
    RETURN

    // =======================================================================
    //  Espera os modelos E o mundo (colisao) carregarem antes de criar o NPC
    // =======================================================================
bbg_wait_models:
    GET_GAME_TIMER tmpInt
    dt = tmpInt - loadTick
    IF dt > CFG_LOAD_MS
        PRINT_STRING "~r~Boombox Guy: nao consegui carregar os modelos. Tente de novo." 4000
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
    GET_CHAR_HEADING playerChar heading

    GOSUB bbg_pick_spawn

    CREATE_CHAR PEDTYPE_CIVMALE CFG_PED_MODEL nx ny nz pedBox
    IF pedBox = 0
        PRINT_STRING "~r~Boombox Guy: nao consegui criar o NPC. Tente de novo." 4000
        GOSUB bbg_release_models
        gstate = 0
        RETURN
    ENDIF

    // Vida padrao de NPC e zero reacao ao mundo (decision maker vazio).
    SET_CHAR_MAX_HEALTH pedBox 100
    SET_CHAR_HEALTH pedBox 100
    SET_CHAR_HEADING pedBox heading
    SET_CHAR_DECISION_MAKER pedBox DM_PED_EMPTY
    TASK_TOGGLE_PED_THREAT_SCANNER pedBox FALSE FALSE FALSE

    GOSUB bbg_make_box
    GET_GAME_TIMER loadTick          // timer das tentativas de criar a caixa
    GOSUB bbg_play_track             // conta as musicas e sorteia a primeira
    IF trackCount = 0
        PRINT_STRING "~r~Boombox Guy: nenhum MP3 encontrado.~n~~w~Coloque som1.mp3 ... som50.mp3 em CLEO\BoomboxGuy\" 6000
    ENDIF
    IF bufPath = 0
        GOSUB bbg_alloc_path
    ENDIF

    // ja manda ele vir correndo atras de voce
    TASK_FOLLOW_PATH_NODES_TO_COORD_WITH_RADIUS pedBox px py pz PEDMOVE_RUN -1 CFG_STOP_DIST
    GET_GAME_TIMER taskTick
    prevPX = px
    prevPY = py
    farTick = -1
    stuckTick = -1
    gstate = 2
    PRINT_STRING "~y~Boombox Guy~n~~w~Ele esta indo ate voce. (BOOBOXD dispensa)" 4500
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
    CREATE_RENDER_OBJECT_TO_CHAR_BONE pedBox CFG_BOX_MODEL CFG_BOX_BONE CFG_BOX_OFF_X CFG_BOX_OFF_Y CFG_BOX_OFF_Z CFG_BOX_ROT_X CFG_BOX_ROT_Y CFG_BOX_ROT_Z objBox
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

    // Ja esta com voce? Avisa (o TEST_CHEAT so dispara se o cheat foi digitado)
    IF TEST_CHEAT "BOOBOX"
        PRINT_STRING "~y~Boombox Guy: ele ja esta te acompanhando." 2500
    ENDIF
    IF TEST_CHEAT "BOOBOXD"
        GOSUB bbg_dismiss
        RETURN
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
        prevPX = px
        prevPY = py
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

    // Posicoes atuais
    GET_CHAR_COORDINATES playerChar px py pz
    GET_CHAR_COORDINATES pedBox nx ny nz
    dx = px - nx
    dy = py - ny
    dz = pz - nz
    dx = dx * dx
    dy = dy * dy
    dz = dz * dz
    d2 = dx + dy
    d2 = d2 + dz

    // ---------------------------------------------------------------------
    //  INTERIOR: quando o player entra/sai de um interior (ou e teleportado),
    //  a posicao dele "salta" de uma vez. Detectando esse salto, o NPC vai
    //  junto na hora, em vez de ficar do lado de fora.
    // ---------------------------------------------------------------------
    dx = px - prevPX
    dy = py - prevPY
    dx = dx * dx
    dy = dy * dy
    dz = dx + dy
    prevPX = px
    prevPY = py
    IF dz > CFG_JUMP_D2
        GOSUB bbg_can_teleport
        IF flag = 1
            GOSUB bbg_teleport
            RETURN
        ENDIF
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

    // ------------------------------- seguir -------------------------------
    IF d2 > CFG_FOLLOW_D2
        GET_GAME_TIMER tmpInt
        dt = tmpInt - taskTick
        IF dt > CFG_RETASK_MS
            GET_GAME_TIMER taskTick
            IF d2 > CFG_SPRINT_D2
                TASK_FOLLOW_PATH_NODES_TO_COORD_WITH_RADIUS pedBox px py pz PEDMOVE_SPRINT -1 CFG_STOP_DIST
            ELSE
                TASK_FOLLOW_PATH_NODES_TO_COORD_WITH_RADIUS pedBox px py pz PEDMOVE_RUN -1 CFG_STOP_DIST
            ENDIF
        ENDIF
    ENDIF

    // ----------------------------- teleporte ------------------------------
    // Longe demais? So teleporta quando o player estiver a pe, no chao,
    // fora da agua e com o mundo (colisao) ja carregado.
    IF d2 > CFG_LOST_D2
        GOSUB bbg_lost_check
    ELSE
        farTick = -1
    ENDIF

    // ------------------------- preso / parado longe -----------------------
    IF d2 > CFG_FOLLOW_D2
        GOSUB bbg_stuck_check
    ELSE
        stuckTick = -1
    ENDIF

    // -------------------------------- som --------------------------------
    GOSUB bbg_audio
    RETURN

    // =======================================================================
    //  Player longe demais: espera alguns segundos (a pe e no chao) e teleporta
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
    GET_CHAR_HEADING playerChar heading

    GOSUB bbg_pick_spawn

    CLEAR_CHAR_TASKS pedBox
    SET_CHAR_COORDINATES_SIMPLE pedBox nx ny nz
    SET_CHAR_HEADING pedBox heading
    FIX_CHAR_GROUND_BRIGHTNESS_AND_FADE_IN pedBox TRUE TRUE FALSE
    TASK_FOLLOW_PATH_NODES_TO_COORD_WITH_RADIUS pedBox px py pz PEDMOVE_RUN -1 CFG_STOP_DIST

    GET_GAME_TIMER taskTick
    prevPX = px
    prevPY = py
    farTick = -1
    stuckTick = -1
    RETURN

    // =======================================================================
    //  Escolhe um lugar valido em volta do player (atras, laterais, frente)
    //  entrada: px py pz (player), heading
    //  saida:   nx ny nz
    // =======================================================================
bbg_pick_spawn:
    flag = 0
    spawnAng = heading
    GOSUB bbg_try_pos
    IF flag = 0
        spawnAng = heading + 90.0
        GOSUB bbg_try_pos
    ENDIF
    IF flag = 0
        spawnAng = heading - 90.0
        GOSUB bbg_try_pos
    ENDIF
    IF flag = 0
        spawnAng = heading + 180.0
        GOSUB bbg_try_pos
    ENDIF
    IF flag = 0
        // ultimo recurso: no proprio lugar do player (sempre tem chao ali)
        nx = px
        ny = py
        nz = pz
    ENDIF
    RETURN

    // Testa UMA posicao: so aceita se achar o chao perto do chao do player
    // (evita telhado, ponte ou dentro de predio) - e pede a colisao antes.
bbg_try_pos:
    GET_COORD_FROM_ANGLED_DISTANCE px py spawnAng CFG_APPEAR_DIST nx ny
    nz = pz
    flag = 0
    REQUEST_COLLISION nx ny
    dx = pz + 2.0
    dy = 9999.0
    GET_GROUND_Z_FOR_3D_COORD nx ny dx dy
    dz = pz - dy
    IF dz < 3.0
        IF dz > -3.0
            nz = dy + 0.5
            flag = 1
        ENDIF
    ENDIF
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
    IF trackCount > 0
        PRINT_FORMATTED_NOW "~y~Boombox Guy~n~~w~%d musica(s) encontrada(s) em CLEO\BoomboxGuy\" 4000 trackCount
    ENDIF
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
        PRINT_STRING "~r~Boombox Guy: nao consegui abrir o MP3 sorteado." 4000
        RETURN
    ENDIF
    SET_AUDIO_STREAM_VOLUME boxStream CFG_VOLUME
    SET_AUDIO_STREAM_STATE boxStream 1
    PRINT_FORMATTED_NOW "~y~Boombox Guy~n~~w~Tocando: som%d.mp3" 3000 lastTrack
    RETURN

    // =======================================================================
    //  Fim do NPC
    // =======================================================================
bbg_npc_died:
    // Vida padrao: morreu, acabou. A caixa fica com o corpo e o CLEO+ apaga
    // ela junto com o ped. So volta a tocar se digitar o cheat de novo.
    PRINT_STRING "~r~Boombox Guy: ele morreu. O som parou." 4000
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
    PRINT_STRING "~y~Boombox Guy: ate logo." 2500
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
