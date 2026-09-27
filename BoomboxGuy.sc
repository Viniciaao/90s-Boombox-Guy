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
//  CONFIGURACAO: CLEO/BoomboxGuy/BoomboxGuy.ini
//     O script cria esse arquivo sozinho na primeira vez, ja com os valores
//     padrao, e depois passa a usar o que estiver nele. Da para editar com o
//     jogo fechado (ou aberto) e so digitar BOOBOX de novo para valer:
//
//        [Ped]
//        model=male01     <- aparencia do NPC: nome do DFF (sem .dff) ou o ID
//
//        [Caixa]
//        posX=0.40        <- posicao da caixa na mao
//        posY=0.02
//        posZ=0.02
//        rotX=0           <- rotacao da caixa (graus)
//        rotY=-90
//        rotZ=0
//        [Som]
//        volume=1         <- 0.0 (mudo) a 1.0
//
//     AJUSTE AO VIVO (opcional, para achar os numeros da caixa):
//        Digite BBGUYTUNE com o NPC chamado. Com o modo ligado:
//           WASD + Q/E          move a caixa (X, Y, Z)
//           SHIFT + WASD + Q/E  gira a caixa
//           DELETE              salva no BoomboxGuy.ini na hora
//        Os valores aparecem na tela; ao sair do modo (BBGUYTUNE de novo) o
//        arquivo e salvo automaticamente. Nada disso aparece em jogo normal.
//
//  O nome do DFF e resolvido pelo proprio jogo (via CLEO+), entao serve
//  qualquer skin: vanilla (male01, wmybu, bmycr...) ou de mod, inclusive as
//  instaladas por ModLoader. Se o nome nao existir, o NPC usa o modelo
//  padrao do script, sem aviso nenhum na tela.
//
//  Requisitos: CLEO 4 + CLEO+ v1.2 ou mais novo (o script checa e avisa)
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
    // Modelo padrao do NPC. 7 = male01, o civil mais generico do jogo.
    // Serve so de reserva: quem manda e a chave "model=" do BoomboxGuy.ini
    // (nome do DFF ou ID). Se o ini nao existir/estiver invalido, usa isto.
    CONST_INT   CFG_PED_MODEL       7
    CONST_INT   CFG_PED_MAX_ID      400     // faixa de IDs aceita como pedestre
    // Caixa de som: 2226 = low_hi_fi_3 (objeto nativo do jogo, sem mods).
    CONST_INT   CFG_BOX_MODEL       2226
    // Osso onde a caixa e presa: 24 = mao direita (BONE_R_HAND no Sanny).
    CONST_INT   CFG_BOX_BONE        24

    // ---- pose da caixa na mao ----
    // ATENCAO 1: o deslocamento e a rotacao usam os eixos DO OSSO da mao, que
    // ficam tortos em relacao ao mundo (por isso os numeros parecem
    // estranhos e a rotacao em Y e -90).
    // ATENCAO 2: os valores abaixo sao apenas o PADRAO. Quem manda e o
    // arquivo CLEO\BoomboxGuy\BoomboxGuy.ini (criado na primeira vez, com
    // exatamente estes numeros). Para mudar, mexa no ini - ou use o modo
    // BBGUYTUNE, que salva no ini na hora.
    CONST_FLOAT CFG_BOX_OFF_X       0.40
    CONST_FLOAT CFG_BOX_OFF_Y       0.02
    CONST_FLOAT CFG_BOX_OFF_Z       0.02
    CONST_FLOAT CFG_BOX_ROT_X       0.0
    CONST_FLOAT CFG_BOX_ROT_Y      -90.0
    CONST_FLOAT CFG_BOX_ROT_Z       0.0
    // tamanho da caixa (1.0 = tamanho original do objeto do jogo)
    CONST_FLOAT CFG_BOX_SCALE       1.0
    // tamanho do passo no modo de ajuste (por toque de tecla)
    CONST_FLOAT CFG_TUNE_POS        0.02
    CONST_FLOAT CFG_TUNE_ROT        5.0
    // tecla que grava o ajuste em arquivo (codigo VK do Windows).
    //   46 = Delete   45 = Insert   36 = Home   35 = End
    //   34 = Page Down   33 = Page Up   9 = Tab   13 = Enter   32 = Espaco
    //   (o gta3script nao aceita VK_DELETE aqui, por isso vai o numero)
    CONST_INT   CFG_TUNE_KEY        46


    // Onde o som 3D nasce, em relacao ao corpo do NPC (altura da mao).
    CONST_FLOAT CFG_SND_OFF_X       0.25
    CONST_FLOAT CFG_SND_OFF_Y       0.10
    CONST_FLOAT CFG_SND_OFF_Z       0.75
    CONST_FLOAT CFG_VOLUME          1.0     // volume padrao (o ini pode mudar)
    CONST_INT   CFG_INI_MAX_LINES   200     // travas de seguranca na leitura do ini
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
    LVAR_INT   bufPath pedModel lastTrack flag tmpInt dt tries
    LVAR_FLOAT px py pz nx ny nz dx dy dz
    LVAR_FLOAT boxOX boxOY boxOZ boxRX boxRY boxRZ

    // gstate: 0 = esperando o cheat | 1 = carregando modelos
    //         2 = NPC ativo        | 3 = NPC ativo + modo de ajuste da caixa
    // pedModel: modelo do NPC - padrao CFG_PED_MODEL, sobrescrito pelo ini
    gstate       = 0
    pedBox       = 0
    objBox       = 0
    boxStream    = 0
    playerChar   = 0
    bufPath      = 0
    pedModel     = CFG_PED_MODEL
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
    // le - ou cria, na primeira vez - o arquivo BoomboxGuy.ini
    flag = 0
    GOSUB bbg_ini_load

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
    // relê o ini: da para editar o arquivo e so digitar o cheat de novo
    flag = 0
    GOSUB bbg_ini_load
    // o modelo do ped (nome OU id) foi resolvido na leitura do ini; aqui so
    // confirmamos que ele existe mesmo antes de pedir para carregar
    IF GET_MODEL_DOESNT_EXIST_IN_RANGE pedModel pedModel tmpInt
        RETURN
    ENDIF
    IF NOT IS_MODEL_IN_CDIMAGE CFG_BOX_MODEL
        RETURN
    ENDIF
    REQUEST_MODEL pedModel
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
    IF HAS_MODEL_LOADED pedModel
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

    CREATE_CHAR PEDTYPE_CIVMALE pedModel nx ny nz pedBox
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

    // (a pose da caixa ja veio do BoomboxGuy.ini, lido no cheat)
    GOSUB bbg_make_box
    GET_GAME_TIMER loadTick          // timer das tentativas de criar a caixa

    GOSUB bbg_play_track             // varre as musicas e sorteia a primeira
    IF lastTrack = 0
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
        GOSUB bbg_ini_write      // ligou ou desligou: ja salva no BoomboxGuy.ini
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
    GOSUB bbg_pick_track          // 0 = nenhum som*.mp3 encontrado
    IF lastTrack = 0
        RETURN
    ENDIF
    tries = lastTrack
    GOSUB bbg_build_path
    LOAD_3D_AUDIO_STREAM $bufPath boxStream
    IF boxStream = 0
        RETURN
    ENDIF
    // volume: vem do ini (se a chave nao existir, fica o padrao CFG_VOLUME)
    flag = 1
    GOSUB bbg_ini_load
    IF dx < 0.0
        dx = 0.0
    ENDIF
    IF dx > 1.0
        dx = 1.0
    ENDIF
    SET_AUDIO_STREAM_VOLUME boxStream dx
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
    IF IS_KEY_JUST_PRESSED CFG_TUNE_KEY
        GOSUB bbg_ini_write      // salva na hora
    ENDIF
    // (se trocar a tecla em CFG_TUNE_KEY, troque o "DELETE" desta linha tambem)
    PRINT_FORMATTED_NOW "~y~caixa~n~~w~pos %.2f %.2f %.2f~n~rot %.0f %.0f %.0f~n~WASD/QE move | SHIFT gira | DELETE salva no ini" 300 boxOX boxOY boxOZ boxRX boxRY boxRZ
    RETURN

    // =======================================================================
    //  Arquivo de configuracao: CLEO\BoomboxGuy\BoomboxGuy.ini
    //     [Caixa]  posX posY posZ rotX rotY rotZ
    //     [Som]    volume
    //  O script cria o arquivo sozinho na primeira vez, com os valores
    //  padrao, e depois passa a usar o que estiver nele. A leitura usa
    //  apenas os comandos de arquivo do proprio CLEO 4 (0A9A abrir, 0AD7
    //  ler linha, 0AD4 interpretar, 0AD9 escrever, 0A9B fechar) - nenhum
    //  plugin extra -. Qualquer linha estranha e simplesmente ignorada:
    //  o arquivo nunca derruba o jogo.
    //     'flag' escolhe o que ler:  0 = pose da caixa   1 = volume
    // =======================================================================
bbg_ini_load:
    IF flag = 0
        boxOX = CFG_BOX_OFF_X
        boxOY = CFG_BOX_OFF_Y
        boxOZ = CFG_BOX_OFF_Z
        boxRX = CFG_BOX_ROT_X
        boxRY = CFG_BOX_ROT_Y
        boxRZ = CFG_BOX_ROT_Z
        pedModel = CFG_PED_MODEL          // reserva, caso o ini nao tenha "model="
    ELSE
        dx = CFG_VOLUME
    ENDIF
    IF bufPath = 0
        GOSUB bbg_alloc_path
        IF bufPath = 0
            RETURN
        ENDIF
    ENDIF
    IF NOT OPEN_FILE "CLEO\BoomboxGuy\BoomboxGuy.ini" "r" tmpInt
        IF flag = 0
            GOSUB bbg_ini_write      // primeira vez: cria o arquivo
        ENDIF
        RETURN
    ENDIF
    tries = 0
    // (o buffer do caminho das musicas tambem serve de buffer de linha)
    WHILE READ_STRING_FROM_FILE tmpInt bufPath CFG_PATH_SIZE
    AND tries < CFG_INI_MAX_LINES
        tries = tries + 1
        // linha comecando com ';' ou '#' e comentario: nao e lida
        READ_MEMORY bufPath 1 0 loadTick
        IF NOT loadTick = 59
        AND NOT loadTick = 35
            IF flag = 0
                GOSUB bbg_ini_line
            ELSE
                SCAN_STRING $bufPath " %*[vV]olume%*[^-.0-9]%f" dt dx
            ENDIF
        ENDIF
    ENDWHILE
    CLOSE_FILE tmpInt
    // -----------------------------------------------------------------------
    //  Valida o modelo do ped: se nao for um ID de pedestre que existe mesmo,
    //  volta para o padrao do script. Assim um nome errado no ini nunca vira
    //  um ped invalido (nem crash) - so um NPC com a skin padrao.
    // -----------------------------------------------------------------------
    IF flag = 0
        IF pedModel < 0
            pedModel = CFG_PED_MODEL
        ELSE
            IF pedModel > CFG_PED_MAX_ID
                pedModel = CFG_PED_MODEL
            ELSE
                IF GET_MODEL_DOESNT_EXIST_IN_RANGE pedModel pedModel loadTick
                    pedModel = CFG_PED_MODEL
                ENDIF
            ENDIF
        ENDIF
    ENDIF
    RETURN

    // -----------------------------------------------------------------------
    //  Uma linha do ini: se a chave casar, o valor entra na variavel
    //  (o SCAN_STRING so escreve na variavel quando a linha casa mesmo,
    //   entao linha comentada, secao, chave escrita errado ou valor podre
    //   simplesmente nao mexem em nada)
    // -----------------------------------------------------------------------
bbg_ini_line:
    SCAN_STRING $bufPath " %*[pP]osX%*[^-.0-9]%f" dt boxOX
    SCAN_STRING $bufPath " %*[pP]osY%*[^-.0-9]%f" dt boxOY
    SCAN_STRING $bufPath " %*[pP]osZ%*[^-.0-9]%f" dt boxOZ
    SCAN_STRING $bufPath " %*[rR]otX%*[^-.0-9]%f" dt boxRX
    SCAN_STRING $bufPath " %*[rR]otY%*[^-.0-9]%f" dt boxRY
    SCAN_STRING $bufPath " %*[rR]otZ%*[^-.0-9]%f" dt boxRZ

    // -----------------------------------------------------------------------
    //  [Ped] model= <nome do DFF ou ID>
    //     "model=male01"  -> procura a skin pelo nome (via CLEO+, cobre mods)
    //     "model=7"       -> usa o ID direto
    //  Como funciona: o "%n" do scanner devolve em que caractere o valor
    //  comeca. Se o valor comeca com digito, e ID; se nao, cortamos a string
    //  no fim do nome (tirando o fim de linha, espacos e comentario) e
    //  entregamos o nome para o jogo procurar.
    //  Qualquer coisa estranha e ignorada: o modelo segue o padrao.
    // -----------------------------------------------------------------------
    loadTick = 0
    SCAN_STRING $bufPath " %*[mM]odel%*[^A-Za-z0-9_]%n%c" dt nextAudioTick loadTick
    IF dt = 1
        nextAudioTick = bufPath + nextAudioTick      // inicio do valor
        IF loadTick > 47
        AND loadTick < 58
            // ---- ID numerico ----
            SCAN_STRING $bufPath " %*[mM]odel%*[^0-9-]%d" dt loadTick
            pedModel = loadTick
        ELSE
            // ---- nome do DFF ----
            SCAN_STRING $bufPath " %*[mM]odel%*[^A-Za-z0-9_]%*[A-Za-z0-9_]%n" dt tmpInt
            tmpInt = bufPath + tmpInt                // fim do nome
            WRITE_MEMORY tmpInt 1 0 0
            IF GET_MODEL_BY_NAME $nextAudioTick loadTick
                pedModel = loadTick
            ENDIF
        ENDIF
    ENDIF
    RETURN

    // -----------------------------------------------------------------------
    //  Grava o arquivo inteiro: a pose atual da caixa + o volume que ja
    //  estava no arquivo (para nao perde-lo ao salvar um ajuste da caixa)
    // -----------------------------------------------------------------------
bbg_ini_write:
    IF bufPath = 0
        GOSUB bbg_alloc_path
        IF bufPath = 0
            RETURN
        ENDIF
    ENDIF
    flag = 1
    GOSUB bbg_ini_load           // busca o volume atual (vai para dx)
    tmpInt = 0
    IF OPEN_FILE "CLEO\BoomboxGuy\BoomboxGuy.ini" "w" tmpInt
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "; 90s Boombox Guy - configuracao%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "; Use ponto decimal (0.5), nao virgula. Nao mude o nome das chaves.%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "; Comentario comeca com ; ou #.%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "[Ped]%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "; aparencia do NPC: nome do DFF (sem .dff, ex.: male01, wmybu, bmycr)%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "; ou o ID numerico do modelo (ex.: 7). Se o nome nao existir, o%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "; script usa o modelo padrao dele.%c" 10
        GET_MODEL_NAME_POINTER pedModel dt
        IF dt = 0
            WRITE_FORMATTED_STRING_TO_FILE tmpInt "model=%d%c" pedModel 10
        ELSE
            WRITE_FORMATTED_STRING_TO_FILE tmpInt "model=%s%c" $dt 10
        ENDIF
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "[Caixa]%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "posX=%g%c" boxOX 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "posY=%g%c" boxOY 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "posZ=%g%c" boxOZ 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "rotX=%g%c" boxRX 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "rotY=%g%c" boxRY 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "rotZ=%g%c" boxRZ 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "[Som]%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "; volume de 0.0 (mudo) ate 1.0%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "volume=%g%c" dx 10
        CLOSE_FILE tmpInt
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
    MARK_MODEL_AS_NO_LONGER_NEEDED pedModel
    MARK_MODEL_AS_NO_LONGER_NEEDED CFG_BOX_MODEL
    RETURN
}
SCRIPT_END
