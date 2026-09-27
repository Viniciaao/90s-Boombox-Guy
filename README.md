# 90s Boombox Guy

Remake do clássico **90s Boombox** (de *Guidopdu*), reescrito de zero em
**gta3script + CLEO+**.

A diferença de conceito: em vez de o CJ carregar a caixa de som, um **NPC
civil** (o "cara do som") é chamado por cheat, **aparece longe de você**
(de preferência atrás, fora da câmera), **vem correndo** até o player
carregando a caixa de som nas mãos e passa a te acompanhar com a música
saindo da caixa em **áudio 3D**. A aparência do NPC **e o objeto da caixa**
são escolhidos no `BoomboxGuy.ini` — pelo **nome do DFF** ou pelo ID do
modelo.

---

## Como usar

| Cheat | O que faz |
| --- | --- |
| `BOOBOX` | Chama o carregador de caixa de som |
| `BOOBOXD` | Dispensa o NPC e desliga a música |
| `BBGUYTUNE` | (opcional) liga/desliga o modo de ajuste da caixa na mão |

Depois de digitar `BOOBOX`, o NPC aparece **a uns 45 metros de você**,
geralmente atrás (do lado oposto ao que a câmera está olhando), e vem
correndo até você. Quando chega perto, começa a tocar uma faixa sorteada
entre as suas músicas. Quando a música acaba, ele sorteia outra
automaticamente — **sempre aleatório**, nunca em ordem, e sem repetir a
mesma faixa duas vezes seguidas.

**Na tela só aparecem 2 avisos:** "nenhuma música encontrada" (quando você
não tem nenhum `som*.mp3` na pasta) e "Cleo+ não encontrado / desatualizado".
Todo o resto é silencioso — digitar o cheat, chegar, começar a tocar,
dispensar, morrer: nada disso mostra texto.

- **Veículo**: se você entrar em qualquer veículo **com cadeira de
  passageiro livre** (carro, táxi, ônibus, barco, helicóptero, avião…), o
  NPC entra junto e viaja com você. Ele espera o carro dar uma parada,
  senta na carona e sai do carro quando você sai. Se o veículo estiver
  lotado (ou não tiver carona, tipo moto), ele continua te seguindo a pé.
- **Interiores**: se você entrar (ou sair) de qualquer interior, ele vai
  junto. O script percebe o "salto" de posição do player e leva o NPC na
  hora, com as mesmas checagens de chão e colisão.
- **Fuga (carro/avião) sem lugar para ele**: se você se afastar muito e
  **pisar de volta no chão**, ele é teleportado para junto de você (também
  atrás, para não "aparecer do nada" na sua cara).
- **NPC preso**: se ele ficar parado num canto (parede, cerca, beco) por
  6 segundos longe de você, ele também volta a aparecer perto.
- **Sem blip no mapa**, sem marcador, sem ícone.
- Ele **não reage ao mundo**: não foge, não se assusta, não briga, não
  pertence à sua gangue. Isso é feito com um *decision maker* vazio
  (`DM_PED_EMPTY`) e com o scanner de ameaças desligado — ele só faz o que o
  script manda.
- **Vida padrão** (100). Se ele morrer, acabou: a música para e você precisa
  digitar o cheat de novo.
- Se você morrer, ele vai embora sozinho.
- **Em cutscene** (da campanha ou de missão com script) a música é
  **pausada** e volta de onde parou quando a cutscene acaba — para não
  sobrepor os diálogos nem interferir no fluxo da missão.

---

## Instalação

1. Tenha o **CLEO 4** e o **CLEO+** instalados. O script usa opcodes CLEO+
   (caixa na mão, áudio 3D, detecção de cutscene, leitura do estado do NPC).
   **Se o CLEO+ não estiver instalado (ou estiver desatualizado), o script
   avisa na tela e se encerra sozinho, sem crashar o jogo.**
2. Copie `BoomboxGuy.cs` para a pasta `CLEO/` do GTA San Andreas.
3. Crie a pasta `CLEO/BoomboxGuy/` e coloque suas músicas:

```
GTA San Andreas/
  CLEO/
    BoomboxGuy.cs
    BoomboxGuy/
      som1.mp3
      som2.mp3
      ...
      som50.mp3
```

**Quantidade de músicas: de 1 até 50.** O script **varre a pasta e conta
quais arquivos existem** (`som1.mp3` … `som50.mp3`) na primeira vez que você
chama o NPC e usa só as que estão lá — funciona com 1, 2, 10 ou 50 músicas,
e até com numeração "esburacada" (por exemplo só `som1`, `som4` e `som9`).
Arquivos que faltarem são simplesmente ignorados.

Formatos: MP3 (recomendado), OGG, WAV, AIFF — o que o BASS do CLEO
conseguir abrir. Se nenhum arquivo for encontrado, o script mostra o aviso
"nenhuma musica encontrada" e tenta de novo mais tarde (você pode até
colocar os arquivos com o jogo aberto, sem sair do jogo).

Não é necessário nenhum arquivo DFF/TXD: por padrão a caixa de som é o
**objeto nativo do jogo** `2226 (low_hi_fi_3)`. Se quiser outra caixa, é só
apontar `[Caixa] model=` para o **nome do DFF** de qualquer objeto que já
esteja no seu jogo (vanilla ou de mod) — nada de `.dff`/`.txd` solto na
pasta do CLEO.

Na primeira vez que o jogo abre, o script cria também o
`CLEO/BoomboxGuy/BoomboxGuy.ini` (modelo do NPC, posição/rotação da caixa e
volume) — veja a
seção **Arquivo de configuração** mais abaixo. Tem uma cópia de exemplo
pronta em `BoomboxGuy/BoomboxGuy.ini` no repositório.
Um detalhe: o script **não** usa o plugin `IniFiles.cleo`, e sim os comandos
de arquivo do próprio CLEO 4 (`0A9A/0AD4/0AD7/0AD9/0A9B`) — então o `.ini`
funciona em qualquer instalação de CLEO 4, sem plugin extra. Já a **busca do
modelo pelo nome** usa o opcode `0E9C`, e a checagem de tipo (`0E7F`) vem
do mesmo CLEO+ — os dois existem a partir da v1.2, a mesma versão que o
script já exige na checagem de dependência.

---

## Configuração

Todo o "painel de controle" fica no topo de `BoomboxGuy.sc`, em constantes
fáceis de achar (`CONST_INT` / `CONST_FLOAT`). Depois de mudar, recompile
com `./build.sh` (ou `make`).

| Constante | Padrão | Para que serve |
| --- | --- | --- |
| `CFG_PED_MODEL` | `7` (male01) | Skin **padrão** do NPC — só é usada se o `.ini` não tiver um `model=` válido |
| `CFG_BOX_MODEL` | `2226` (low_hi_fi_3) | **Objeto padrão** da caixa de som (idem: reserva do `.ini`) |
| `CFG_VAR_BOX` | `1023` | Onde fica guardado, entre um quadro e outro, o objeto da caixa escolhido no `.ini` (uma "var compartilhada" do CLEO — as 32 `LVAR` do script estão todas em uso) |
| `CFG_BOX_BONE` | `24` (`BONE_R_HAND`) | Osso onde a caixa é presa |
| `CFG_BOX_OFF_X/Y/Z` | `0.40 / 0.02 / 0.02` | Posição da caixa na mão (**padrão do .ini**) |
| `CFG_BOX_ROT_X/Y/Z` | `0 / -90 / 0` | Rotação da caixa (**padrão do .ini**) |
| `CFG_BOX_SCALE` | `1.0` | Tamanho da caixa (`0.7` deixa a caixa menor) |
| `CFG_INI_MAX_LINES` | `200` | Trava de segurança na leitura do `.ini` |
| `CFG_TUNE_POS` / `CFG_TUNE_ROT` | `0.02` / `5.0` | Tamanho do passo no modo `BBGUYTUNE` |
| `CFG_TUNE_KEY` | `46` (Delete) | Tecla que grava o ajuste da caixa em arquivo |
| `CFG_SND_OFF_X/Y/Z` | `0.25 / 0.10 / 0.75` | Onde o som 3D nasce em relação ao corpo |
| `CFG_VOLUME` | `1.0` | Volume padrão da música (**o `.ini` pode mudar**) |
| `CFG_MAX_TRACKS` | `50` | Número máximo de faixas que o script procura (`som1..som50`) |
| `CFG_PATH_SIZE` | `64` | Tamanho do buffer do caminho do MP3 |
| `CFG_CLEOPLUS_MIN` | `16908288` (`0x01020000` = v1.2.0.0) | Versão mínima do CLEO+ aceita |
| `CFG_SPAWN_BACK` / `CFG_SPAWN_SIDE` | `-45.0` | Distâncias de spawn (negativo em Y = atrás do player) |
| `CFG_SPAWN_MID` / `CFG_SPAWN_NEAR` | `-30.0` / `-15.0` | Spawn mais perto, se os pontos acima não tiverem chão |
| `CFG_APPEAR_BACK` / `CFG_APPEAR_SIDE` / `CFG_APPEAR_FRONT` | `-6.0 / -6.0 / 6.0` | Pontos de teleporte |
| `CFG_FOLLOW_D2` | `9.0` | Distância² (3 m) em que ele começa a te seguir |
| `CFG_SPRINT_D2` | `400.0` | Distância² (20 m) em que ele corre mais rápido |
| `CFG_LOST_D2` | `3600.0` | Distância² (60 m) considerado "longe demais" |
| `CFG_FALL_Z` | `25.0` | Se o NPC ficar 25 m abaixo de você, é resgatado |
| `CFG_STOP_DIST` | `2.5` | Raio em que ele para de andar |
| `CFG_TELEPORT_MS` | `1500` | Quanto tempo longe (e a pé) antes de teleportar |
| `CFG_STUCK_MS` | `6000` | Quanto tempo parado longe antes de teleportar |
| `CFG_RETASK_MS` | `1000` | Intervalo entre comandos de seguir |
| `CFG_CAR_RETASK_MS` | `2000` | Intervalo entre tentativas de entrar no seu veículo |
| `CFG_LOAD_MS` | `10000` | Timeout ao carregar modelos/mundo |
| `CFG_AUDIO_RETRY_MS` | `20000` | Espera entre tentativas se nenhum MP3 abrir |

### Arquivo de configuração: `CLEO/BoomboxGuy/BoomboxGuy.ini`

**Aparência do NPC, posição/rotação da caixa e volume** moram num arquivo
`.ini`, que o próprio script **cria sozinho** (já com os valores padrão) na
primeira vez que o jogo abre:

```ini
; 90s Boombox Guy - configuracao
; Use ponto decimal (0.5), nao virgula. Nao mude o nome das chaves.
; Comentario comeca com ; ou #.
; As linhas model= podem ser o nome do DFF (sem .dff) ou o ID;
; quem decide o que e skin de NPC e o que e objeto e o tipo do modelo.
[Ped]
; aparencia do NPC: nome do DFF de um PEDESTRE (ex.: male01, wmybu,
; bmycr) ou o ID (ex.: 7). Serve skin vanilla ou de mod (ModLoader
; incluido). Se o nome nao existir, o NPC usa o modelo padrao.
model=male01
[Caixa]
; objeto da caixa: nome do DFF de um OBJETO (ex.: low_hi_fi_3)
; ou o ID (ex.: 2226). Qualquer objeto do jogo serve. Se o nome nao
; existir (ou o modelo nao for um objeto), a caixa usa o padrao.
model=low_hi_fi_3
posX=0.40
posY=0.02
posZ=0.02
rotX=0
rotY=-90
rotZ=0
[Som]
; volume de 0.0 (mudo) ate 1.0
volume=1
```

Como funciona:

- **`model` (nos dois lugares)** — aceita o **nome do DFF** (sem `.dff`:
  `male01`, `low_hi_fi_3`, `wmybu`…) **ou o ID numérico** (`7`, `2226`). A
  busca pelo nome é feita pelo próprio jogo (via CLEO+), então funciona com
  **qualquer modelo** — vanilla ou de mod, inclusive os instalados por
  ModLoader/IMG — e não liga para maiúsculas/minúsculas (`Model=MALE01`
  funciona).
- **`[Ped] model`** — a **skin do NPC**. Só vale para modelos de
  **pedestre**: se o nome apontar para um objeto, o NPC continua com o
  modelo padrão. Se o nome não existir (typo, mod removido), o NPC
  simplesmente usa `CFG_PED_MODEL`, **sem aviso e sem erro**.
- **`[Caixa] model`** — o **objeto da caixa de som**. Aqui é o contrário:
  só vale para **objeto** (aceita os tipos de objeto do jogo: atômico,
  clump, com hora do dia e LOD), nunca pedestre nem veículo. Se o nome não existir, for um ped ou
  um carro, ou o arquivo do modelo não estiver no jogo, a caixa volta para
  `CFG_BOX_MODEL` (o `low_hi_fi_3` original), **sem aviso e sem erro**.
- **`[Caixa] posX/posY/posZ` e `rotX/rotY/rotZ`** — onde a caixa fica presa
  na mão do NPC. O deslocamento usa os eixos **do osso da mão** (não são os eixos do mundo, por isso os números
  parecem estranhos e a rotação em Y é `-90`). Os valores acima já são um
  encaixe testado no jogo.
- **`[Som] volume`** — de `0.0` (mudo) a `1.0` (volume máximo). É lido a cada
  troca de faixa, então dá para mudar o volume e ouvir na música seguinte.
- **Editar**: mexa no arquivo e digite o cheat `BOOBOX` de novo (tudo é
  relido a cada chamada — nem precisa reiniciar o jogo). Passo a passo:
  abra `CLEO/BoomboxGuy/BoomboxGuy.ini` num editor de texto simples, mude o
  que quiser, salve, e digite `BOOBOX` no jogo.
- Quem decide o que é skin de NPC e o que é objeto da caixa é o **tipo** do
  modelo, não a seção: `model=` funciona em qualquer lugar do arquivo. Em
  `[Ped]` use um pedestre; em `[Caixa]` use um objeto.
- Valores inválidos, chaves escritas errado, linhas em branco e comentários
  (linhas começando com `;` ou `#`) são simplesmente ignorados: a chave que
  não for encontrada continua com o valor padrão, **nunca dá erro nem
  trava**. Chaves com a primeira letra maiúscula (`PosX`, `Volume`,
  `Model`) também são aceitas, e pode ter espaços em volta do `=`.
- Para voltar tudo ao padrão, é só apagar o arquivo: o script cria outro na
  próxima vez que abrir o jogo (com `model=male01`).

### Ajustando a caixa ao vivo (cheat `BBGUYTUNE`)

Se quiser achar os números da caixa sem ficar editando e recarregando:

1. Chame o NPC (`BOOBOX`) e digite `BBGUYTUNE`.
2. Com o modo ligado, os valores atuais aparecem na tela e:

   | Teclas | O que fazem |
   | --- | --- |
   | `A` / `D` | move a caixa no eixo X |
   | `W` / `S` | move a caixa no eixo Y |
   | `Q` / `E` | move a caixa no eixo Z |
   | `SHIFT` + `A/D/W/S/Q/E` | gira a caixa (X/Y/Z) |
   | `DELETE` | salva no `BoomboxGuy.ini` na hora |

   (O CJ anda um pouco ao apertar WASD — pare, ajuste e vá testando.)
3. Ao **sair** do modo (digite `BBGUYTUNE` de novo) o arquivo é **salvo
   sozinho** — nem precisa apertar `DELETE`. Em jogo normal o modo não fica
   ativo e nada aparece na tela.

A tecla de salvar é a constante `CFG_TUNE_KEY` (código VK do Windows):
`46` = Delete (padrão), `45` = Insert, `36` = Home, `35` = End,
`34` = Page Down, `33` = Page Up, `9` = Tab, `13` = Enter, `32` = Espaço —
nada de `F5`/`F-keys`, que em muitos teclados de notebook estão quebradas.

---

## Compilando

O script é escrito em **gta3script** e compilado com o
[gta3sc](https://github.com/thelink2012/gta3sc):

```bash
./build.sh                                  # ou: make
GTA3SC=/caminho/para/gta3sc ./build.sh      # se o compilador não estiver no PATH
```

O comando usado é:

```
gta3sc --config=gtasa --guesser --cs -fcleo -fno-entity-tracking \
       --add-config=tools/cleo-plus.xml \
       -o BoomboxGuy.cs BoomboxGuy.sc
```

- `--cs` = script CLEO (já implica `-fcleo`).
- `--guesser` = habilita recursos que a comunidade ainda não mapeou 100%
  (necessário para `-fcleo` neste parser).
- `-fno-entity-tracking` = não valida o tipo "entidade" das variáveis
  (handles de char/objeto/áudio) em tempo de compilação — sem isso o
  compilador reclama que `AUDIO_STREAM` não confere com `INT`.
- **`--add-config=tools/cleo-plus.xml`** = carrega as definições dos opcodes
  do CLEO+ (o gta3sc puro não conhece `0E2E`, `0EF0`, `0ECB`…). O arquivo é
  o `cleo.xml` oficial do CLEO+ (MIT), com os enums que já existem na
  configuração `gtasa` removidos — o gta3sc não aceita o mesmo enum
  definido duas vezes. Ele vai junto no repositório em `tools/`.
- `-fconst`, `-fswitch`, `-farrays` já são ligados automaticamente pelo
  `config/gtasa/commandline.txt` do gta3sc, por isso as `CONST_*` funcionam
  sem flag extra.

---

## Estrutura do repositório

```
BoomboxGuy.sc          fonte (gta3script) — é aqui que você mexe
BoomboxGuy.cs          compilado (vai para CLEO/)
build.sh / Makefile    builds
tools/cleo-plus.xml    opcodes do CLEO+ para o gta3sc (MIT, Junior_Djjr)
BoomboxGuy/            pasta-exemplo para os MP3 (som1.mp3 ... som50.mp3)
```

---

## Como foi feito / notas de segurança

O script foi escrito pensando em **não crashar**, que era o problema do mod
original (que chegou a ter correções por causa de variáveis globais
bagunçadas):

- **Checagem de dependência**: na inicialização o script carrega
  `CLEO+.cleo` com `LOAD_DYNAMIC_LIBRARY`, pergunta a versão
  (`GetCleoPlusVersion`) e, se a DLL não existir ou for mais velha que
  `1.2.0.0`, mostra o único alerta de erro e termina com
  `TERMINATE_THIS_CUSTOM_SCRIPT` — ou seja, ele nunca chama um opcode do
  CLEO+ que não existe, o que seria um crash certo.
- **Checagem de colisão**: antes de criar ou teleportar o NPC o script
  exige `NOT IS_CHAR_WAITING_FOR_WORLD_COLLISION` (o mundo já carregou em
  volta do player), pede a colisão do ponto de destino com
  `REQUEST_COLLISION` e confere o chão com `GET_GROUND_Z_FOR_3D_COORD`. A
  posição só é aceita se o chão estiver a ±3 m do chão do player — isso
  evita nascer em telhado, ponte, dentro de prédio ou **cair no vazio
  embaixo do mapa**. Se nada servir, ele tenta pontos cada vez mais perto
  (30 m, 15 m, 6 m) e, como última alternativa, o próprio lugar do player
  (onde o chão com certeza existe).
  Além disso, se o NPC acabar 25 m abaixo de você (caiu no vazio), o script
  o resgata.
- **Veículo**: a entrada como passageiro usa
  `IS_CAR_PASSENGER_SEAT_FREE` antes de `TASK_ENTER_CAR_AS_PASSENGER`
  (cadeiras 1..3). O pedido é refeito a cada 2 s enquanto o player estiver
  dirigindo (o carro pode estar em movimento, e o jogo só embarca quando dá
  — barco, helicóptero e avião também entram nessa checagem). Quando o
  player sai do veículo, o script manda `TASK_LEAVE_ANY_CAR` e volta para o
  modo a pé.
- **Interiores**: entrar/sair de interior (ou qualquer teleporte do player)
  faz a posição do CJ "saltar" mais de 25 m num único quadro. O script
  detecta esse salto e leva o NPC junto, respeitando as checagens de
  colisão acima.
- **Cutscenes**: durante `IS_ON_CUTSCENE` ou `IS_ON_SCRIPTED_CUTSCENE` a
  música é pausada (`0AAD` ação 2) e retomada de onde parou (ação 3) no
  fim, e o script evita mexer no NPC no meio da cena.
- **Nada de variáveis globais do jogo.** O trabalho é todo feito com as
  `LVAR_*` locais do script — 32, exatamente o limite do CLEO (por isso o
  script evita variáveis "de enfeite"). O único valor que precisa sobreviver
  de um quadro para o outro além delas, o **objeto da caixa escolhido no
  `.ini`**, mora numa *var compartilhada do CLEO* (`0AB3`/`0AB4`, índice
  `CFG_VAR_BOX`): são 1024 espaços numerados do próprio CLEO, não são
  variáveis do savegame do jogo, e o valor é conferido de novo toda vez que
  é usado — se outro script escrever por cima, a caixa só volta ao padrão.
- **Modelos**: os dois modelos (NPC e caixa) são pedidos com `REQUEST_MODEL`
  e só depois de `HAS_MODEL_LOADED` o ped é criado; se estourar o timeout,
  o script volta ao estado inicial em silêncio. Os modelos são liberados com
  `MARK_MODEL_AS_NO_LONGER_NEEDED` sempre em par com o pedido.
- **Áudio**: `LOAD_3D_AUDIO_STREAM` tem o retorno checado, e o stream é
  liberado com `REMOVE_AUDIO_STREAM` antes de carregar o próximo, ao
  dispensar o NPC e se ele morrer. O caminho do MP3 é montado num buffer
  reservado com `ALLOCATE_MEMORY` (liberado automaticamente pelo CLEO quando
  o script sai) e formatado com `STRING_FORMAT` — nada de overflow de
  string no script space.
- **Som por coordenadas, não por link**: o som 3D é reposicionado todo frame
  com `SET_PLAY_3D_AUDIO_STREAM_AT_COORDS` em cima da mão do NPC. As
  variantes `..._AT_CHAR`/`..._AT_OBJECT` guardam um ponteiro para a
  entidade, e o `Process()` do CLEO4 usa esse ponteiro sem validar — se o
  NPC for apagado antes, é crash. Por isso eles não são usados aqui.
- **Caixa na mão**: `CREATE_RENDER_OBJECT_TO_CHAR_BONE` tem o retorno
  checado; se falhar, o script tenta de novo a cada meio segundo. A caixa é
  auto-removida quando o NPC é deletado (o CLEO+ apaga os render objects
  junto com o ped, por isso o script **nunca** chama
  `DELETE_RENDER_OBJECT` depois que o NPC morre — seria ponteiro inválido).
- **Handle do ped**: todo acesso ao NPC é precedido de `DOES_CHAR_EXIST` e
  `IS_CHAR_DEAD`. O script nunca mexe em um handle morto.
- **Modelo vindo do `.ini`**: o nome é resolvido pelo próprio jogo
  (`0E9C`) e o resultado passa por três checagens antes de virar coisa na
  tela — (1) existe mesmo e não é slot vazio, (2) é do **tipo** certo para o
  lugar, (3) o arquivo do modelo está no jogo (`IS_MODEL_IN_CDIMAGE`).
  Para o NPC o tipo tem que ser **pedestre**; para a caixa, um dos tipos de
  **objeto** (atômico, com hora do dia, clump ou LOD). Pedestre e veículo
  são recusados na caixa de propósito: o comando que monta o objeto na mão
  não confere o modelo, e tratar um carro ou uma pessoa como objeto
  derrubaria o jogo. Nome não encontrado, tipo errado ou valor maluco ⇒
  volta para o padrão em silêncio.
- O loop principal tem `WAIT 0`, e o script volta ao estado inicial se o
  player morrer, se o char sumir do pool, se o NPC morrer ou se for
  dispensado.
- **Save**: como todo ped criado por script, o NPC pode acabar indo parar no
  seu savegame (sem a caixa, que é um objeto de render e não é salvo). Se
  isso incomodar, dispense ele com `BOOBOXD` antes de salvar.

---

## Histórico de versões

- **v6** — escolha do **objeto da caixa** pelo `.ini`: `[Caixa] model=` aceita
  o **nome do DFF** (resolvido pelo jogo com `0E9C`) ou o ID, e o modelo é
  validado pelo **tipo** com o opcode `0E7F` — só entra objeto (atômico,
  clump, LOD); pedestre, veículo ou nome inexistente caem no `low_hi_fi_3`
  padrão sem avisar. O mesmo `model=` em `[Ped]` ganhou a checagem de tipo
  (tem que ser pedestre). O valor escolhido é guardado numa var
  compartilhada do CLEO (`0AB3`/`0AB4`), já que as 32 `LVAR` do script estão
  em uso; o `.ini` é reescrito com o nome real do objeto (`0F17`).
- **v5** — escolha da skin do NPC pelo `.ini`: `[Ped] model=` aceita o **nome
  do DFF** (resolvido pelo próprio jogo com o opcode `0E9C` do CLEO+, então
  vale para skins de mod, inclusive via ModLoader) ou o ID numérico; modelo
  inválido cai no padrão sem avisar. Leitura do `.ini` mais tolerante:
  comentários com `;`/`#`, espaços antes do `=`, chave indentada.
- **v4** — configuração em `CLEO/BoomboxGuy/BoomboxGuy.ini`: posição e
  rotação da caixa (valores testados no jogo) e volume, lidos com comandos
  de arquivo do próprio CLEO 4 (sem plugin extra); o arquivo é criado sozinho
  com os valores padrão e relido a cada `BOOBOX`; o `BBGUYTUNE` agora salva
  direto no `.ini` (tecla `DELETE`, além do autosave ao sair do modo).
- **v3** — spawn longe e fora da câmera (vem correndo até você); NPC entra
  no seu veículo como passageiro quando tem lugar; só as 2 mensagens de
  erro/aviso na tela (todo o resto silencioso); modo de ajuste da caixa na
  mão com o cheat `BBGUYTUNE`.
- **v2** — cutscene pausa a música; teleporte só com colisão pronta;
  detecção de 1..50 faixas em runtime; sorteio aleatório sem repetir;
  checagem do CLEO+ na inicialização.
- **v1** — primeira versão jogável (NPC segue, caixa na mão, áudio 3D).

---

## Créditos

- Mod original: **90s Boombox** por *Guidopdu*
  ([MixMods](https://www.mixmods.com.br/2016/02/90s-boombox-andar-ouvindo-radio/))
  — 10 faixas, som direcional e a ideia de "andar ouvindo rádio".
- CLEO+ por *Junior_Djjr* (MIT) — inclusive as definições de opcodes em
  `tools/cleo-plus.xml`.
- Este remake: escrito em gta3script/CLEO+ para GTA San Andreas.
