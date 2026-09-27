// ===========================================================================
//  90s Boombox Guy  --  remake do "90s Boombox" (Guidopdu)
//  gta3script + CLEO+  /  GTA San Andreas
// ---------------------------------------------------------------------------
//  Um NPC civil ("o cara do som") e chamado por cheat, aparece LONGE (atras
//  de voce, fora da camera), corre ate o player carregando uma caixa de som
//  e passa a te acompanhar com musica 3D saindo da caixa. Ele tambem entra
//  com voce nos interiores (aparece logo a frente do CJ) e sobe junto no
//  seu veiculo. Andando, ele para a 2,5 m de voce (nunca entra no seu corpo);
//  parado, fica sempre virado para voce.
//
//  Cheats:
//     BOOBOX      chama o NPC
//     BOOBOXD     dispensa o NPC e para a musica
//     BBGUYTUNE   (opcional) liga/desliga o modo de ajuste da caixa na mao
//
//  Sem tecla nenhuma: com o NPC do seu lado, de um SOCO nele (de maos
//  livres, sem arma na mao) e a musica troca na hora - e o "proxima faixa"
//  do mod, feito no meio do jogo. Soco nao mata ele: a vida volta ao normal.
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
//        model=low_hi_fi_3  <- objeto da caixa: nome do DFF (sem .dff) ou o ID
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
//  qualquer skin/objeto: vanilla (male01, wmybu, bmycr, low_hi_fi_3...) ou de
//  mod, inclusive os instalados por ModLoader. O script pergunta ao jogo de
//  que tipo e o modelo: pedestre vira NPC, o resto vira objeto na mao dele -
//  por isso o "model=" funciona em qualquer secao e nunca troca as bolas.
//  Se o nome nao existir, se o modelo nao for do tipo certo para o lugar
//  (NPC = pedestre, caixa = objeto) ou se o arquivo dele nem estiver no jogo,
//  o padrao de reserva continua valendo, sem aviso nenhum na tela.
//
//  Requisitos: CLEO 4 + CLEO+ v1.2 ou mais novo (o script checa e avisa)
//
//  Como compilar (veja tambem build.sh / Makefile):
//     gta3sc --config=gtasa --guesser --cs -fcleo -fno-entity-tracking \
//            --add-config=<caminho>/tools/cleo-plus.xml \
//            -o BoomboxGuy.cs BoomboxGuy.sc
// ===========================================================================

SCRIPT_START
{
// ===================== CONFIGURACAO (pode mexer aqui) ======================
    // Modelo padrao do NPC. 7 = male01, o civil mais generico do jogo.
    // Serve so de reserva: quem manda e a chave "model=" do BoomboxGuy.ini
    // (nome do DFF ou ID). Se o ini nao existir/estiver invalido, usa isto.
    CONST_INT   CFG_PED_MODEL       7
    // Caixa de som: 2226 = low_hi_fi_3 (objeto nativo do jogo, sem mods).
    CONST_INT   CFG_BOX_MODEL       2226
    // Onde fica guardado o modelo da caixa escolhido no ini. As 32 LVARs
    // estao todas em uso, entao o valor mora numa "variavel do CLEO"
    // (comandos 0AB3/0AB4, as mesmas que o Sanny chama de var 0/1/2...):
    // sao 1024 espacos numerados, que sobrevivem de um frame para o outro.
    // O valor e conferido TODA vez antes de usar (veja bbg_box_model), entao
    // se algum outro script escrever por cima dele o pior que pode acontecer
    // e a caixa voltar para CFG_BOX_MODEL - nunca um modelo invalido.
    CONST_INT   CFG_VAR_BOX         1023
    // Handle do arquivo .ini (outra var do CLEO). O parser das linhas usa
    // tmpInt, dt e loadTick como rascunho, entao o handle NAO pode morar numa
    // LVAR: guardar o handle em tmpInt foi exatamente o que derrubou o jogo
    // quando a linha "model=" foi lida (o handle virava 7 e o fgets caia em
    // cima de um ponteiro invalido). Aqui ele fica fora do alcance do parser.
    CONST_INT   CFG_VAR_INI         1022
    // "Area" (interior) em que o NPC esta: 0 = rua, 1..18 = interiores. Outra
    // var do CLEO, pelo mesmo motivo das de cima. Serve para perceber a troca
    // de ambiente e leva-lo junto na hora (veja bbg_interior).
    CONST_INT   CFG_VAR_AREA        1021
    // Vida do NPC vista no quadro anterior (para saber quando ele LEVOU dano
    // agora) e o relogio do ultimo soco contado como troca de musica. Outras
    // vars do CLEO, pelo mesmo motivo das de cima.
    CONST_INT   CFG_VAR_HP          1020
    CONST_INT   CFG_VAR_PUNCH       1019
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
    // ---- interior ----
    // Onde ele aparece (e reaparece) dentro de um interior: logo a frente do
    // CJ. La o teste de solo nao vale - o piso do ambiente nao responde ao
    // GET_GROUND_Z_FOR_3D_COORD -, entao dentro de interior ele vai direto
    // para este ponto, no mesmo Z do player.
    CONST_FLOAT CFG_FRONT_DIST      1.5
    // Dentro do ambiente o piso e um so (o mesmo do player): se ele ficar
    // mais que isso ABAIXO, saiu da area com chao e esta caindo - era assim
    // que ele morria de queda. Resgata na hora.
    CONST_FLOAT CFG_INSIDE_FALL_Z   3.0
    // "Perto o bastante" para ele PARAR de andar - o quadrado, em metros
    // (6,25 = 2,5 m). Com o CFG_FOLLOW_D2 (3 m, quando ele volta a andar)
    // sobra uma faixa morta de meio metro entre andar e parar: sem ela ele
    // ficaria ligando e desligando a caminhada a cada quadro.
    CONST_FLOAT CFG_NEAR_D2         6.25
    // Reposicionamento de menos de 1,5 m nem acontece (1,5^2 = 2,25): mover
    // o NPC um passo apareceria como um pulinho do nada na tela.
    CONST_FLOAT CFG_CLOSE_D2        2.25
    // De quanto em quanto tempo ele se vira de novo para o player (ms).
    CONST_INT   CFG_FACE_MS         1500
    // Intervalo minimo entre dois socos contados (ms): uma sequencia rapida
    // de socos conta como um comando so.
    CONST_INT   CFG_PUNCH_MS        700
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
    // O modelo do ped (nome OU id) foi resolvido na leitura do ini. Ultima
    // conferencia antes de pedir: existe mesmo e e de pedestre? Criar um
    // char com modelo que nao e de gente e pedido de crash.
    IF GET_MODEL_DOESNT_EXIST_IN_RANGE pedModel pedModel tmpInt
        RETURN
    ENDIF
    GET_MODEL_TYPE pedModel tmpInt
    IF NOT tmpInt = MODEL_TYPE_PED
        RETURN
    ENDIF
    // A caixa: pega o objeto escolhido no ini, ja conferido (bbg_box_model).
    // Se o arquivo dele nao estiver no jogo, cai no padrao: melhor um NPC com
    // a caixa original do que NPC nenhum.
    GOSUB bbg_box_model
    IF NOT IS_MODEL_IN_CDIMAGE nextAudioTick
        nextAudioTick = CFG_BOX_MODEL
        IF NOT IS_MODEL_IN_CDIMAGE nextAudioTick
            RETURN
        ENDIF
    ENDIF
    REQUEST_MODEL pedModel
    REQUEST_MODEL nextAudioTick
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
    // modelo da caixa (lido do ini e conferido agora de novo, por seguranca)
    GOSUB bbg_box_model
    IF HAS_MODEL_LOADED pedModel
    AND HAS_MODEL_LOADED nextAudioTick
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

    // Nasce ja pertencendo a "area" do player (0 = rua, 1..18 = interior). O
    // jogo so desenha - e so mantem a colisao - de quem esta na mesma area:
    // sem isso, dentro de um interior ele aparece na rua, atravessa o piso
    // (sem colisao) e cai no vazio.
    GET_AREA_VISIBLE tmpInt
    SET_CHAR_AREA_VISIBLE pedBox tmpInt
    SET_CLEO_SHARED_VAR CFG_VAR_AREA tmpInt
    SET_CLEO_SHARED_VAR CFG_VAR_HP 100

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

    // ja manda ele vir correndo atras de voce (dentro de interior ele vai em
    // linha reta: la nao existe malha de navegacao - veja bbg_go_player)
    dz = 0.0
    GOSUB bbg_go_player
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
    GOSUB bbg_box_model              // nextAudioTick = objeto escolhido no ini
    CREATE_RENDER_OBJECT_TO_CHAR_BONE pedBox nextAudioTick CFG_BOX_BONE boxOX boxOY boxOZ boxRX boxRY boxRZ objBox
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
    //  Interior: mantem o NPC na mesma "area" do player (o jogo so desenha e
    //  so carrega a colisao da area atual) e, nas trocas, leva ele na hora
    //  para a frente do CJ. Veja bbg_interior.
    // ---------------------------------------------------------------------
    GOSUB bbg_interior

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

    // ---------------------------------------------------------------------
    //  O player deu um SOCO nele? Troca a musica. E o "proxima faixa" do
    //  mod sem tecla nenhuma: o comando e o proprio soco.
    //  Como o jogo entrega isso: a vida dele caiu desde o quadro anterior (ou
    //  seja, levou dano AGORA), quem bateu foi o player (051A) e a "arma" na
    //  mao do player e o proprio punho (0 = maos livres). Tiro, arma branca,
    //  carro, bomba e soco de outro NPC nao contam.
    //  Soco nao mata o Boombox Guy: a vida volta para 100 na mesma hora.
    // ---------------------------------------------------------------------
    GET_CHAR_HEALTH pedBox tmpInt
    GET_CLEO_SHARED_VAR CFG_VAR_HP tries
    IF NOT tmpInt = tries
        SET_CLEO_SHARED_VAR CFG_VAR_HP tmpInt      // cura/cheat: so sincroniza
    ENDIF
    IF tmpInt < tries                              // levou dano neste quadro
        GET_CURRENT_CHAR_WEAPON playerChar dt
        IF dt = 0
        AND NOT IS_CHAR_IN_ANY_CAR playerChar
            IF HAS_CHAR_BEEN_DAMAGED_BY_CHAR pedBox playerChar
                // ...e ele estava ao alcance do braco: 051A guarda "quem me
                // machucou por ultimo", entao sem esta checagem um tombo do
                // NPC contaria como soco se o player tivesse socado antes.
                GET_CHAR_COORDINATES playerChar px py pz
                GET_CHAR_COORDINATES pedBox nx ny nz
                dx = px - nx
                dy = py - ny
                dx = dx * dx
                dy = dy * dy
                dx = dx + dy                   // distancia 2D ao quadrado
                IF dx < CFG_NEAR_D2
                    GET_GAME_TIMER dt
                    GET_CLEO_SHARED_VAR CFG_VAR_PUNCH loadTick
                    dt = dt - loadTick
                    IF dt > CFG_PUNCH_MS
                        GET_GAME_TIMER dt
                        SET_CLEO_SHARED_VAR CFG_VAR_PUNCH dt
                        GOSUB bbg_play_track   // proxima faixa, na hora
                        SET_CHAR_HEALTH pedBox 100
                        SET_CLEO_SHARED_VAR CFG_VAR_HP 100
                    ENDIF
                ENDIF
            ENDIF
        ENDIF
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
        // excecao: se ele ainda esta entrando (ou saindo), deixa a animacao
        // terminar - limpar a tarefa no meio dela o deixa de pe atravessando
        // o carro. No quadro seguinte, ja sentado, ele sai normalmente.
        IF NOT IS_CHAR_ENTERING_ANY_CAR pedBox
        AND NOT IS_CHAR_EXITING_ANY_CAR pedBox
            CLEAR_CHAR_TASKS pedBox
            TASK_LEAVE_ANY_CAR pedBox
            GET_GAME_TIMER taskTick
        ENDIF
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
            GOSUB bbg_go_player
        ENDIF
    ELSE
        // Ja esta perto: se ele ainda vinha andando, para (bbg_stop_walk).
        // Sem isso ele termina a caminhada DENTRO do CJ - empurra o player,
        // que fica preso - e o jogo "fecha" o ultimo trecho da caminhada no
        // ponto final, o que em interior aparece como um teleporte do nada.
        // Parando antes dos dois, nenhum dos dois acontece. A faixa entre
        // CFG_NEAR_D2 e CFG_FOLLOW_D2 e morta, so para nao tremer.
        IF dz < CFG_NEAR_D2
            GOSUB bbg_stop_walk
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

    // ------------------------- virado para o player -----------------------
    GOSUB bbg_face_player

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
    SET_CHAR_VELOCITY pedBox 0.0 0.0 0.0      // nao herda queda/salto antigo
    // caminho por nodes na rua, linha reta dentro de interior (bbg_go_player)
    dz = 0.0
    GOSUB bbg_go_player

    GET_GAME_TIMER taskTick
    farTick = -1
    stuckTick = -1
    RETURN

    // =======================================================================
    //  Onde ele aparece quando e chamado (longe, atras do player se der)
    // =======================================================================
bbg_pick_spawn:
    // Dentro de um interior o mundo la fora nao vale: o piso do ambiente nao
    // responde ao teste de solo e ele acabaria nascendo na rua, longe de
    // voce. La ele nasce logo a frente do CJ, no mesmo piso.
    GOSUB bbg_in_interior
    IF flag = 1
        GOSUB bbg_pick_front
        RETURN
    ENDIF
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
    // Mesma regra do nascimento: em interior, na frente do CJ e no piso dele.
    GOSUB bbg_in_interior
    IF flag = 1
        GOSUB bbg_pick_front
        RETURN
    ENDIF
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
    //  O player esta dentro de um interior?  saida: flag (1 = sim)
    //  O 09E8 responde "este char esta fora do mundo normal" - seja qual for
    //  o numero do interior -, entao ele e o teste seguro para decidir se o
    //  ambiente e interior. O id da area (para vincular o NPC) vem do 077E.
    // =======================================================================
bbg_in_interior:
    flag = 0
    GET_CHAR_AREA_VISIBLE playerChar tmpInt
    IF NOT tmpInt = 0
        flag = 1
    ENDIF
    RETURN

    // =======================================================================
    //  Posicao logo a frente do CJ, no mesmo piso dele. E o ponto usado nos
    //  interiores, onde o teste de solo nao acha o piso do ambiente.
    //  Entrada: px py pz = posicao do player.  Saida: nx ny nz
    // =======================================================================
bbg_pick_front:
    dx = 0.0
    dy = CFG_FRONT_DIST
    dz = 0.0
    GET_OFFSET_FROM_CHAR_IN_WORLD_COORDS playerChar dx dy dz nx ny nz
    nz = pz
    RETURN

    // =======================================================================
    //  Manda o NPC ir ate a posicao do player (px py pz).
    //  Entrada: px py pz e dz = distancia ao quadrado (0 = colado nele).
    //  Na rua ele usa a malha de navegacao (desvia de muro, segue calcada);
    //  dentro de um interior essa malha nao existe - ele simplesmente nao
    //  anda -, entao la ele vai em linha reta, como qualquer NPC em sala.
    // =======================================================================
bbg_go_player:
    GOSUB bbg_in_interior
    IF flag = 1
        GOSUB bbg_go_inside
        RETURN
    ENDIF
    IF dz > CFG_SPRINT_D2
        TASK_FOLLOW_PATH_NODES_TO_COORD_WITH_RADIUS pedBox px py pz PEDMOVE_SPRINT -1 CFG_STOP_DIST
    ELSE
        TASK_FOLLOW_PATH_NODES_TO_COORD_WITH_RADIUS pedBox px py pz PEDMOVE_RUN -1 CFG_STOP_DIST
    ENDIF
    RETURN

    // =======================================================================
    //  Andar dentro de interior: em linha reta (la dentro nao existe malha
    //  de navegacao de pedestre) e com o destino NA ALTURA DELE - o piso do
    //  ambiente, que e o mesmo do player. Quem o faz parar e o bbg_stop_walk,
    //  a 2,5 m de voce; ele nunca chega a tocar no destino, entao o destino
    //  poder ser a sua posicao nao causa nada. O tempo da tarefa e longo de
    //  proposito: se ela expirasse no meio do caminho, daria solavanco.
    // =======================================================================
bbg_go_inside:
    GET_CHAR_COORDINATES pedBox nx ny nz      // nz = altura do NPC (o piso)
    CLEAR_CHAR_TASKS pedBox
    TASK_GO_STRAIGHT_TO_COORD pedBox px py nz PEDMOVE_RUN 20000
    flag = 1
    RETURN

    // =======================================================================
    //  Perto do player e ainda andando? Para. E o que impede ele de entrar
    //  DENTRO do CJ (empurrando o player e travando ele nos cantos) e de o
    //  jogo "fechar" a caminhada no ponto final com um pulinho.
    // =======================================================================
bbg_stop_walk:
    IF IS_CHAR_IN_ANY_CAR pedBox
    OR IS_CHAR_ENTERING_ANY_CAR pedBox
    OR IS_CHAR_EXITING_ANY_CAR pedBox
        RETURN
    ENDIF
    GET_CHAR_MOVE_STATE pedBox tmpInt
    IF tmpInt >= MOVE_STATE_WALK_START
        CLEAR_CHAR_TASKS pedBox
    ENDIF
    RETURN

    // =======================================================================
    //  Sempre virado para o player - dentro e fora de interior.
    //  So age quando ele NAO esta andando: caminhando, o corpo ja aponta para
    //  onde ele vai, e trocar a tarefa no meio do passo daria solavanco.
    //  A tarefa termina sozinha quando ele ja esta de frente, entao ela e
    //  reenviada de tempos em tempos - cada envio e so um ajuste fino.
    // =======================================================================
bbg_face_player:
    IF IS_CHAR_IN_ANY_CAR pedBox
    OR IS_CHAR_ENTERING_ANY_CAR pedBox
    OR IS_CHAR_EXITING_ANY_CAR pedBox
        RETURN
    ENDIF
    IF IS_CHAR_IN_ANY_CAR playerChar
        RETURN
    ENDIF
    GET_CHAR_MOVE_STATE pedBox tmpInt
    IF tmpInt >= MOVE_STATE_WALK_START
        RETURN
    ENDIF
    GET_GAME_TIMER tmpInt
    dt = tmpInt - taskTick
    IF dt > CFG_FACE_MS
        GET_GAME_TIMER taskTick
        TASK_TURN_CHAR_TO_FACE_CHAR pedBox playerChar
    ENDIF
    RETURN

    // =======================================================================
    //  Poe o NPC na frente do CJ agora - mesmo piso do player, sem passar
    //  pelo teste de chao - e manda ele te seguir. E o que atende tanto a
    //  troca de interior quanto o resgate de quem esta caindo.
    // =======================================================================
bbg_place_front:
    GET_CHAR_COORDINATES playerChar px py pz
    GOSUB bbg_pick_front                      // nx ny nz = ponto na frente do CJ
    CLEAR_CHAR_TASKS pedBox
    // Ele ja esta praticamente nesse ponto? Entao NAO encosta na posicao: um
    // reposicionamento de um passo ou menos apareceria como um pulinho do
    // nada na tela. So a tarefa de seguir e reenviada.
    GET_CHAR_COORDINATES pedBox dx dy dz
    dx = dx - nx
    dy = dy - ny
    dz = dz - nz
    dx = dx * dx
    dy = dy * dy
    dz = dz * dz
    dx = dx + dy
    dx = dx + dz                              // dx = distancia (3D) ao quadrado
    IF dx > CFG_CLOSE_D2
        SET_CHAR_COORDINATES_SIMPLE pedBox nx ny nz
        SET_CHAR_VELOCITY pedBox 0.0 0.0 0.0  // corta a queda que ele trazia
        FIX_CHAR_GROUND_BRIGHTNESS_AND_FADE_IN pedBox TRUE TRUE FALSE
    ENDIF
    dz = 0.0
    GOSUB bbg_go_player
    GET_GAME_TIMER taskTick
    farTick = -1
    stuckTick = -1
    flag = 1
    RETURN

    // =======================================================================
    //  Interior (0 = rua, 1..18 = interiores): o jogo so desenha - e so
    //  mantem a colisao carregada - das entidades que estao na mesma "area"
    //  do player. Um NPC com a area errada aparece na rua em vez de dentro do
    //  ambiente, atravessa o piso (nao ha colisao ali) e morre de queda
    //  quando o ambiente descarrega / ele sai. Aqui ele segue a area do
    //  player quadro a quadro e, quando a area muda (voce entrou, saiu ou
    //  trocou de sala), vai na hora para a frente do CJ. Se ainda assim ele
    //  ficar abaixo do piso do ambiente, e resgatado.
    // =======================================================================
bbg_interior:
    // area do player: 09E8 = "esta fora do mundo normal?" (0 = nao). Se ele
    // devolver o proprio id (algumas builds devolvem), melhor ainda; se
    // devolver so "sim" (1), o id sai do 077E - a area que o jogo mostra.
    GET_CHAR_AREA_VISIBLE playerChar tmpInt
    IF tmpInt = 0
        dt = 0
    ELSE
        IF tmpInt > 1
            dt = tmpInt
        ELSE
            GET_AREA_VISIBLE dt
        ENDIF
    ENDIF
    SET_CHAR_AREA_VISIBLE pedBox dt    // NPC passa a pertencer a mesma area
    GET_CLEO_SHARED_VAR CFG_VAR_AREA tries
    IF NOT tries = dt
        // entrou, saiu ou trocou de sala: leva o NPC junto na hora
        SET_CLEO_SHARED_VAR CFG_VAR_AREA dt
        IF IS_CHAR_IN_ANY_CAR pedBox
        OR IS_CHAR_ENTERING_ANY_CAR pedBox
        OR IS_CHAR_EXITING_ANY_CAR pedBox
            // no veiculo ele ja vai junto com o carro (e no meio da
            // animacao de entrar/sair nao se mexe nas tarefas dele)
            RETURN
        ENDIF
        GOSUB bbg_place_front
        RETURN
    ENDIF
    IF dt = 0
        RETURN
    ENDIF
    IF IS_CHAR_IN_ANY_CAR pedBox
        RETURN
    ENDIF
    // Mesma area: confere se ele nao esta atravessando o piso. Dentro do
    // ambiente o piso e plano (mesmo Z do player); bem abaixo disso, ele saiu
    // da parte com chao e esta caindo - resgata antes de virar dano.
    GET_CHAR_COORDINATES playerChar px py pz
    GET_CHAR_COORDINATES pedBox nx ny nz
    dz = pz - nz
    IF dz > CFG_INSIDE_FALL_Z
        GOSUB bbg_place_front
    ENDIF
    RETURN

    // =======================================================================
    //  Entra no veiculo do player (se tiver cadeira de passageiro livre)
    // =======================================================================
bbg_enter_car:
    flag = 0
    GET_GAME_TIMER taskTick
    // Ele ja esta indo/entrando no carro? Entao nao encosta nele: limpar as
    // tarefas (ou mandar a tarefa de novo) no meio da animacao de entrar
    // deixa ele DE PE dentro do carro, atravessando a lataria.
    IF IS_CHAR_ENTERING_ANY_CAR pedBox
        flag = 1
        RETURN
    ENDIF
    STORE_CAR_CHAR_IS_IN_NO_SAVE playerChar tries
    IF tries = 0
        RETURN
    ENDIF
    // Procura cadeira de carona livre, comecando pela 0: em carro de quatro
    // lugares ela e a da frente (1 e 2 sao os de tras) e em carro de dois
    // lugares - e na moto - a garupa tambem e a 0. Era por isso que ele nao
    // subia na moto: a procura comecava na 1 e nunca achava lugar.
    dt = -1                       // cadeira livre (-1 = nenhuma)
    tmpInt = 0
    WHILE tmpInt <= 3
    AND dt < 0
        IF IS_CAR_PASSENGER_SEAT_FREE tries tmpInt
            dt = tmpInt
        ELSE
            tmpInt = tmpInt + 1
        ENDIF
    ENDWHILE
    IF dt < 0
        // O teste por cadeira nao achou nada - em moto e quadriciclo ele
        // costuma falhar. A ultima palavra e dos contadores do jogo: se
        // ainda cabe passageiro, tenta a garupa (cadeira 0) de todo jeito.
        GET_MAXIMUM_NUMBER_OF_PASSENGERS tries tmpInt
        GET_NUMBER_OF_PASSENGERS tries loadTick
        IF loadTick < tmpInt
            dt = 0
        ELSE
            // lotado: ele vai a pe atras do player
            RETURN
        ENDIF
    ENDIF
    CLEAR_CHAR_TASKS pedBox
    TASK_ENTER_CAR_AS_PASSENGER pedBox tries 20000 dt
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

    // -----------------------------------------------------------------------
    //  Le UMA linha do ini e devolve em 'dt': 1 = leu, 0 = acabou (ou o
    //  handle sumiu). O handle fica guardado na var do CLEO CFG_VAR_INI e e
    //  re-buscado aqui a cada linha: assim 'tmpInt' pode ser usado como
    //  rascunho pelo parser sem derrubar a leitura (era o bug do crash).
    //  Handle de arquivo de verdade e um ponteiro: valor pequeno = lixo, e
    //  melhor parar de ler do que passar um ponteiro qualquer para o fgets.
    // -----------------------------------------------------------------------
bbg_ini_read:
    GET_CLEO_SHARED_VAR CFG_VAR_INI tmpInt
    dt = 0
    IF tmpInt > 65536
        IF READ_STRING_FROM_FILE tmpInt bufPath CFG_PATH_SIZE
            dt = 1
        ENDIF
    ENDIF
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
        tmpInt = 0
        SET_CLEO_SHARED_VAR CFG_VAR_BOX tmpInt   // idem para a caixa
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
    // o handle vai para a var do CLEO: o parser pode usar as LVARs a vontade
    SET_CLEO_SHARED_VAR CFG_VAR_INI tmpInt
    tries = 0
    // (o buffer do caminho das musicas tambem serve de buffer de linha)
    GOSUB bbg_ini_read
    WHILE dt = 1
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
        GOSUB bbg_ini_read
    ENDWHILE
    // fecha o arquivo (com a mesma conferencia do handle: fechar um ponteiro
    // invalido tambem derruba o jogo)
    GET_CLEO_SHARED_VAR CFG_VAR_INI tmpInt
    IF tmpInt > 65536
        CLOSE_FILE tmpInt
    ENDIF
    // (o modelo do ped e o da caixa ja foram conferidos linha por linha:
    //  veja bbg_ini_line e bbg_box_model)
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
    //  model= <nome do DFF ou ID numerico>
    //     Em [Ped] escolhe a skin do NPC, em [Caixa] o objeto da caixa. Quem
    //     decide qual e qual e o TIPO do modelo (0E7F): pedestre vira NPC, o
    //     resto vira objeto na mao dele. Assim a linha funciona em qualquer
    //     secao e um modelo de pedestre nunca vai parar na caixa (nem um
    //     objeto no lugar do NPC).
    //  Como funciona: o "%n" do scanner devolve em que caractere o valor
    //  comeca. Se o valor comeca com digito, e ID; se nao, cortamos a string
    //  no fim do nome (tirando fim de linha, espacos e comentario), deixamos
    //  em minusculas (o nome no jogo e minusculo) e entregamos para o jogo
    //  procurar (0E9C, que cobre modelo de mod/ModLoader).
    //  Qualquer coisa estranha e ignorada: o que estava antes continua valendo.
    // -----------------------------------------------------------------------
    loadTick = 0
    SCAN_STRING $bufPath " %*[mM]odel%*[^A-Za-z0-9_]%n%c" dt nextAudioTick loadTick
    IF dt = 1
        nextAudioTick = bufPath + nextAudioTick      // inicio do valor
        IF loadTick > 47
        AND loadTick < 58
            // ---- ID numerico ----
            SCAN_STRING $bufPath " %*[mM]odel%*[^0-9-]%d" dt loadTick
        ELSE
            // ---- nome do DFF ----
            loadTick = -1                            // -1 = nao achou nada
            tmpInt = 0                               // (se o scan falhar, nao mexe)
            SCAN_STRING $bufPath " %*[mM]odel%*[^A-Za-z0-9_]%*[A-Za-z0-9_]%n" dt tmpInt
            IF tmpInt > 0
                tmpInt = bufPath + tmpInt            // fim do nome
                WRITE_MEMORY tmpInt 1 0 0            // fecha a string aqui
                SET_STRING_LOWER nextAudioTick       // low_hi_fi_3, male01...
                IF GET_MODEL_BY_NAME $nextAudioTick tmpInt
                    loadTick = tmpInt
                ENDIF
            ENDIF
        ENDIF
        // ---- existe? e de que tipo? ----
        IF loadTick > -1
            GET_MODEL_TYPE loadTick tmpInt
            IF tmpInt = MODEL_TYPE_PED
                pedModel = loadTick                  // pedestre: e o NPC
            ELSE
                IF tmpInt > MODEL_TYPE_INVALID
                AND NOT tmpInt = MODEL_TYPE_VEHICLE
                    SET_CLEO_SHARED_VAR CFG_VAR_BOX loadTick   // o resto: caixa
                ENDIF
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
    // ATENCAO: daqui para baixo tmpInt e o handle do arquivo de escrita.
    // Nada chamado dentro deste IF pode usar tmpInt (bbg_box_model usa so
    // 'tries' e 'dt' de proposito).
    IF OPEN_FILE "CLEO\BoomboxGuy\BoomboxGuy.ini" "w" tmpInt
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "; 90s Boombox Guy - configuracao%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "; Use ponto decimal (0.5), nao virgula. Nao mude o nome das chaves.%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "; Comentario comeca com ; ou #.%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "; As linhas model= podem ser o nome do DFF (sem .dff) ou o ID;%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "; quem decide o que e skin de NPC e o que e objeto e o tipo do modelo.%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "[Ped]%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "; aparencia do NPC: nome do DFF de um PEDESTRE (ex.: male01, wmybu,%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "; bmycr) ou o ID (ex.: 7). Serve skin vanilla ou de mod (ModLoader%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "; incluido). Se o nome nao existir, o NPC usa o modelo padrao.%c" 10
        GET_MODEL_NAME_POINTER pedModel dt
        IF dt = 0
            WRITE_FORMATTED_STRING_TO_FILE tmpInt "model=%d%c" pedModel 10
        ELSE
            WRITE_FORMATTED_STRING_TO_FILE tmpInt "model=%s%c" $dt 10
        ENDIF
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "[Caixa]%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "; objeto da caixa: nome do DFF de um OBJETO (ex.: low_hi_fi_3)%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "; ou o ID (ex.: 2226). Qualquer objeto do jogo serve. Se o nome nao%c" 10
        WRITE_FORMATTED_STRING_TO_FILE tmpInt "; existir (ou o modelo nao for um objeto), a caixa usa o padrao.%c" 10
        GOSUB bbg_box_model
        GET_MODEL_NAME_POINTER nextAudioTick dt
        IF dt = 0
            WRITE_FORMATTED_STRING_TO_FILE tmpInt "model=%d%c" nextAudioTick 10
        ELSE
            WRITE_FORMATTED_STRING_TO_FILE tmpInt "model=%s%c" $dt 10
        ENDIF
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
    GOSUB bbg_box_model
    MARK_MODEL_AS_NO_LONGER_NEEDED nextAudioTick
    RETURN

    // -----------------------------------------------------------------------
    //  Modelo efetivo da caixa, ja conferido: devolve em 'nextAudioTick'.
    //  Le o que o jogador escolheu no ini (guardado em CFG_VAR_BOX), pergunta
    //  ao jogo que tipo de modelo e (0E7F) e SO aceita os tipos que sao
    //  objeto de verdade: atomico puro (1), com hora do dia (3), clump (5,
    //  objeto de varios pedacos) e LOD (8). Pedestre e veiculo ficam de fora:
    //  o comando que monta a caixa nao checa o modelo, e criar um carro ou
    //  uma pessoa "como objeto" derruba o jogo.
    //  Valor vazio, apagado por outro script, nome errado, modelo de ped:
    //  cai em CFG_BOX_MODEL (2226), sem avisar ninguem.
    //  Rascunho: 'tries' e 'dt' (livres em todos os pontos de chamada).
    // -----------------------------------------------------------------------
bbg_box_model:
    GET_CLEO_SHARED_VAR CFG_VAR_BOX tries
    nextAudioTick = CFG_BOX_MODEL
    IF tries > 0
        GET_MODEL_TYPE tries dt
        IF dt = MODEL_TYPE_ATOMIC
        OR dt = MODEL_TYPE_TIMED
        OR dt = MODEL_TYPE_CLUMP
        OR dt = MODEL_TYPE_LODATOMIC
            nextAudioTick = tries
        ENDIF
    ENDIF
    RETURN
}
SCRIPT_END
