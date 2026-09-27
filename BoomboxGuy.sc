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
//  Instalacao:
//     CLEO/BoomboxGuy.cs
//     CLEO/BoomboxGuy/som1.mp3 ... som10.mp3
//
//  Como compilar:
//     gta3sc --config=gtasa --guesser --cs -fcleo -fno-entity-tracking \
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
    // Quantas faixas usar (som1.mp3 ... somN.mp3), no maximo 10.
    CONST_INT   CFG_TRACKS          10
    // Distancias (os valores D2 sao o quadrado da distancia, em metros).
    CONST_FLOAT CFG_APPEAR_DIST     4.0     // onde ele aparece/teleporta
    CONST_FLOAT CFG_FOLLOW_D2       9.0     // (3 m) comeca a te seguir
    CONST_FLOAT CFG_SPRINT_D2       400.0   // (20 m) corre mais rapido
    CONST_FLOAT CFG_LOST_D2         3600.0  // (60 m) player longe demais
    CONST_FLOAT CFG_STOP_DIST       2.5     // raio em que ele para
    CONST_INT   CFG_TELEPORT_MS     3000    // longe por quanto tempo
    CONST_INT   CFG_STUCK_MS        6000    // preso por quanto tempo
    CONST_INT   CFG_RETASK_MS       1000    // intervalo entre tarefas
    CONST_INT   CFG_LOAD_MS         6000    // timeout ao carregar modelos
    CONST_INT   CFG_AUDIO_RETRY_MS  20000   // espera para tentar audio
// ===========================================================================

    LVAR_INT   gstate pedBox objBox boxStream curTrack boxTries
    LVAR_INT   playerChar loadTick taskTick farTick stuckTick tmpInt posOk
    LVAR_INT   dt boxTick
    LVAR_FLOAT px py pz nx ny nz dx dy dz d2 gz
    LVAR_FLOAT heading spawnAng oldD2 lastX lastY

    // gstate: 0 = esperando o cheat | 1 = carregando modelos | 2 = ativo
    gstate      = 0
    pedBox      = 0
    objBox      = 0
    boxStream   = 0
    curTrack    = 0
    boxTries    = 0
    playerChar  = 0
    taskTick    = 0
    farTick     = -1
    stuckTick   = -1
    oldD2       = 0.0
    lastX       = 0.0
    lastY       = 0.0

    WHILE TRUE
        WAIT 0

        IF gstate = 0
            // ------------------ parado: espera o cheat ------------------
            IF IS_PLAYER_PLAYING 0
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
    GET_GAME_TIMER boxTick
    boxTries = 0
    gstate = 1
    PRINT_STRING "~y~Boombox Guy: chamando o cara do som..." 2000
    RETURN

    // =======================================================================
    //  Espera os modelos ficarem prontos e cria o NPC
    // =======================================================================
bbg_wait_models:
    IF HAS_MODEL_LOADED CFG_PED_MODEL
    AND HAS_MODEL_LOADED CFG_BOX_MODEL
        GOSUB bbg_spawn
        RETURN
    ENDIF
    GET_GAME_TIMER tmpInt
    tmpInt = tmpInt - loadTick
    IF tmpInt > CFG_LOAD_MS
        PRINT_STRING "~r~Boombox Guy: nao consegui carregar os modelos. Tente de novo." 4000
        GOSUB bbg_release_models
        gstate = 0
    ENDIF
    RETURN

    // =======================================================================
    //  Cria o NPC e a caixa de som
    // =======================================================================
bbg_spawn:
    GET_PLAYER_CHAR 0 playerChar
    GET_CHAR_COORDINATES playerChar px py pz
    GET_CHAR_HEADING playerChar heading

    posOk = 0
    spawnAng = heading
    GOSUB bbg_find_pos
    IF posOk = 0
        spawnAng = heading + 90.0
        GOSUB bbg_find_pos
    ENDIF
    IF posOk = 0
        spawnAng = heading - 90.0
        GOSUB bbg_find_pos
    ENDIF
    IF posOk = 0
        spawnAng = heading + 180.0
        GOSUB bbg_find_pos
    ENDIF
    IF posOk = 0
        nx = px
        ny = py
        nz = pz
    ENDIF

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
    GOSUB bbg_new_track

    GET_GAME_TIMER boxTick
    GET_GAME_TIMER taskTick
    lastX = px
    lastY = py
    farTick = -1
    stuckTick = -1
    oldD2 = 0.0
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
    IF IS_PLAYER_PLAYING 0
        GET_PLAYER_CHAR 0 playerChar
    ELSE
        playerChar = 0
    ENDIF
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

    // Deu problema para criar a caixa? Tenta de novo a cada 500 ms.
    IF objBox = 0
        GET_GAME_TIMER tmpInt
        IF tmpInt > boxTick
            GET_GAME_TIMER boxTick
            boxTick = boxTick + 500
            GOSUB bbg_make_box
        ENDIF
    ENDIF

    // Distancia (ao quadrado) entre o NPC e o player.
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

    // ------------------------------- seguir -------------------------------
    IF d2 > CFG_FOLLOW_D2
        GET_GAME_TIMER tmpInt
        IF taskTick = 0
            taskTick = tmpInt
        ENDIF
        dt = tmpInt - taskTick
        IF dt > CFG_RETASK_MS
            // so da a tarefa de novo se o player se mexeu, ou se ja faz
            // muito tempo que ele esta andando atras
            dx = px - lastX
            dy = py - lastY
            dx = dx * dx
            dy = dy * dy
            dz = dx + dy
            IF dz > 4.0
            OR dt > 4000
                GET_GAME_TIMER taskTick
                lastX = px
                lastY = py
                IF d2 > CFG_SPRINT_D2
                    TASK_FOLLOW_PATH_NODES_TO_COORD_WITH_RADIUS pedBox px py pz PEDMOVE_SPRINT -1 CFG_STOP_DIST
                ELSE
                    TASK_FOLLOW_PATH_NODES_TO_COORD_WITH_RADIUS pedBox px py pz PEDMOVE_RUN -1 CFG_STOP_DIST
                ENDIF
            ENDIF
        ENDIF
    ELSE
        taskTick = 0
    ENDIF

    // ----------------------------- teleporte ------------------------------
    IF d2 > CFG_LOST_D2
        GOSUB bbg_lost_check
    ELSE
        farTick = -1
    ENDIF

    // ---------------- ficou preso sem conseguir chegar perto? ------------
    IF d2 > CFG_FOLLOW_D2
        GET_GAME_TIMER tmpInt
        IF stuckTick < 0
            stuckTick = tmpInt
            oldD2 = d2
        ENDIF
        dt = tmpInt - stuckTick
        IF dt > CFG_STUCK_MS
            IF d2 < oldD2
                // chegou mais perto: continua andando, reinicia a contagem
                stuckTick = tmpInt
                oldD2 = d2
            ELSE
                // nao saiu do lugar: teleporta para perto do player
                GOSUB bbg_teleport
                RETURN
            ENDIF
        ENDIF
    ELSE
        stuckTick = -1
        oldD2 = d2
    ENDIF

    // -------------------------------- som --------------------------------
    IF NOT boxStream = 0
        // som 3D na altura da mao (a caixa fica presa no osso da mao)
        GET_OFFSET_FROM_CHAR_IN_WORLD_COORDS pedBox CFG_SND_OFF_X CFG_SND_OFF_Y CFG_SND_OFF_Z dx dy dz
        SET_PLAY_3D_AUDIO_STREAM_AT_COORDS boxStream dx dy dz
        GET_AUDIO_STREAM_STATE boxStream tmpInt
        IF tmpInt < 1
            // a musica terminou: sorteia outra
            GOSUB bbg_new_track
        ENDIF
    ELSE
        GET_GAME_TIMER tmpInt
        IF tmpInt > loadTick
            GET_GAME_TIMER loadTick
            loadTick = loadTick + CFG_AUDIO_RETRY_MS
            GOSUB bbg_new_track
        ENDIF
    ENDIF
    RETURN

    // =======================================================================
    //  Player longe demais: so teleporta com ele a pe e no chao
    // =======================================================================
bbg_lost_check:
    IF IS_CHAR_IN_ANY_CAR playerChar
        farTick = -1
        RETURN
    ENDIF
    IF IS_CHAR_IN_WATER playerChar
        farTick = -1
        RETURN
    ENDIF
    IF IS_CHAR_REALLY_IN_AIR playerChar
        farTick = -1
        RETURN
    ENDIF
    GET_GAME_TIMER tmpInt
    IF farTick < 0
        farTick = tmpInt
    ENDIF
    tmpInt = tmpInt - farTick
    IF tmpInt > CFG_TELEPORT_MS
        GOSUB bbg_teleport
    ENDIF
    RETURN

    // =======================================================================
    //  Teleporta o NPC para perto do player (atras, ou no lado que der)
    // =======================================================================
bbg_teleport:
    GET_CHAR_COORDINATES playerChar px py pz
    GET_CHAR_HEADING playerChar heading

    posOk = 0
    spawnAng = heading
    GOSUB bbg_find_pos
    IF posOk = 0
        spawnAng = heading + 90.0
        GOSUB bbg_find_pos
    ENDIF
    IF posOk = 0
        spawnAng = heading - 90.0
        GOSUB bbg_find_pos
    ENDIF
    IF posOk = 0
        spawnAng = heading + 180.0
        GOSUB bbg_find_pos
    ENDIF
    IF posOk = 0
        nx = px
        ny = py
        nz = pz
    ENDIF

    CLEAR_CHAR_TASKS pedBox
    SET_CHAR_COORDINATES_SIMPLE pedBox nx ny nz
    SET_CHAR_HEADING pedBox heading
    FIX_CHAR_GROUND_BRIGHTNESS_AND_FADE_IN pedBox TRUE TRUE FALSE
    TASK_FOLLOW_PATH_NODES_TO_COORD_WITH_RADIUS pedBox px py pz PEDMOVE_RUN -1 CFG_STOP_DIST

    GET_GAME_TIMER taskTick
    lastX = px
    lastY = py
    farTick = -1
    stuckTick = -1
    oldD2 = 0.0
    RETURN

    // =======================================================================
    //  Procura um lugar valido em volta do player
    //  entrada: px py pz (player), spawnAng, CFG_APPEAR_DIST
    //  saida:   nx ny nz, posOk
    // =======================================================================
bbg_find_pos:
    GET_COORD_FROM_ANGLED_DISTANCE px py spawnAng CFG_APPEAR_DIST nx ny
    nz = pz
    posOk = 0
    // procura o chao na vertical; se o chao estiver muito longe do chao do
    // player (telhado, ponte, interior), descarta essa posicao
    gz = 9999.0
    dx = pz + 2.0
    GET_GROUND_Z_FOR_3D_COORD nx ny dx gz
    dy = pz - gz
    IF dy < 3.0
        IF dy > -3.0
            nz = gz + 0.5
            posOk = 1
        ENDIF
    ENDIF
    RETURN

    // =======================================================================
    //  Som: sorteia uma faixa, carrega e toca
    // =======================================================================
bbg_new_track:
    GOSUB bbg_stop_audio
    boxTries = 0
    WHILE boxTries < 3
    AND boxStream = 0
        boxTries = boxTries + 1
        GENERATE_RANDOM_INT_IN_RANGE 0 CFG_TRACKS curTrack
        GOSUB bbg_load_track
    ENDWHILE
    IF boxStream = 0
        PRINT_STRING "~r~Boombox Guy: nenhum MP3 em CLEO\BoomboxGuy\ (som1.mp3...som10.mp3)." 5000
        RETURN
    ENDIF
    SET_AUDIO_STREAM_VOLUME boxStream CFG_VOLUME
    SET_AUDIO_STREAM_STATE boxStream 1
    tmpInt = curTrack + 1
    PRINT_FORMATTED_NOW "~y~Boombox Guy~n~~w~Tocando: som%d.mp3" 3000 tmpInt
    RETURN

    // Carrega o arquivo da faixa atual (curTrack). boxStream = 0 se falhar.
    // Os caminhos sao fixos de proposito: nada de montar string em buffer.
bbg_load_track:
    boxStream = 0
    SWITCH curTrack
        CASE 0
            LOAD_3D_AUDIO_STREAM "CLEO\BoomboxGuy\som1.mp3" boxStream
            BREAK
        CASE 1
            LOAD_3D_AUDIO_STREAM "CLEO\BoomboxGuy\som2.mp3" boxStream
            BREAK
        CASE 2
            LOAD_3D_AUDIO_STREAM "CLEO\BoomboxGuy\som3.mp3" boxStream
            BREAK
        CASE 3
            LOAD_3D_AUDIO_STREAM "CLEO\BoomboxGuy\som4.mp3" boxStream
            BREAK
        CASE 4
            LOAD_3D_AUDIO_STREAM "CLEO\BoomboxGuy\som5.mp3" boxStream
            BREAK
        CASE 5
            LOAD_3D_AUDIO_STREAM "CLEO\BoomboxGuy\som6.mp3" boxStream
            BREAK
        CASE 6
            LOAD_3D_AUDIO_STREAM "CLEO\BoomboxGuy\som7.mp3" boxStream
            BREAK
        CASE 7
            LOAD_3D_AUDIO_STREAM "CLEO\BoomboxGuy\som8.mp3" boxStream
            BREAK
        CASE 8
            LOAD_3D_AUDIO_STREAM "CLEO\BoomboxGuy\som9.mp3" boxStream
            BREAK
        CASE 9
            LOAD_3D_AUDIO_STREAM "CLEO\BoomboxGuy\som10.mp3" boxStream
            BREAK
        DEFAULT
            boxStream = 0
            BREAK
    ENDSWITCH
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
