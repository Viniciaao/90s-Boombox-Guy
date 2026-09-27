# 90s Boombox Guy

Remake do clássico **90s Boombox** (de *Guidopdu*), reescrito de zero em
**gta3script + CLEO+**.

A diferença de conceito: em vez de o CJ carregar a caixa de som, um **NPC
civil** (o "cara do som") é chamado por cheat, aparece perto de você,
**corre até o player** com a caixa de som nas mãos e passa a te acompanhar
com a música saindo da caixa em **áudio 3D**.

---

## Como usar

| Cheat | O que faz |
| --- | --- |
| `BOOBOX` | Chama o carregador de caixa de som |
| `BOOBOXD` | Dispensa o NPC e desliga a música |

Depois de digitar `BOOBOX`, o NPC aparece a poucos metros, corre até você e
começa a tocar uma faixa sorteada entre as suas músicas. Quando a música
acaba, ele sorteia outra automaticamente — **sempre aleatório**, nunca em
ordem, e sem repetir a mesma faixa duas vezes seguidas.

- **Interiores**: se você entrar (ou sair) de qualquer interior, ele vai
  junto. O script percebe o "salto" de posição do player e leva o NPC na
  hora, com as mesmas checagens de chão e colisão.
- **Fuga de carro/moto/helicóptero/avião**: se você se afastar muito e
  **pisar de volta no chão**, ele é teleportado para junto de você.
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
chama o NPC, mostra na tela quantas encontrou e usa só as que estão lá —
funciona com 1, 2, 10 ou 50 músicas, e até com numeração "esburacada"
(por exemplo só `som1`, `som4` e `som9`). Arquivos que faltarem são
simplesmente ignorados.

Formatos: MP3 (recomendado), OGG, WAV, AIFF — o que o BASS do CLEO
conseguir abrir. Se nenhum arquivo for encontrado, o script avisa na tela e
tenta de novo mais tarde (você pode até colocar os arquivos com o jogo
aberto, sem sair do jogo).

Não é necessário nenhum arquivo DFF/TXD: a caixa de som é o **objeto nativo
do jogo** `2226 (low_hi_fi_3)`.

---

## Configuração

Todo o "painel de controle" fica no topo de `BoomboxGuy.sc`, em constantes
fáceis de achar (`CONST_INT` / `CONST_FLOAT`). Depois de mudar, recompile
com `./build.sh` (ou `make`).

| Constante | Padrão | Para que serve |
| --- | --- | --- |
| `CFG_PED_MODEL` | `7` (male01) | Skin do NPC. Troque pelo ID que você quiser |
| `CFG_BOX_MODEL` | `2226` | Objeto da caixa de som |
| `CFG_BOX_BONE` | `24` (`BONE_R_HAND`) | Osso onde a caixa é presa |
| `CFG_BOX_OFF_X/Y/Z` | `0.22 / 0.10 / 0.05` | Posição fina da caixa na mão |
| `CFG_BOX_ROT_X/Y/Z` | `0 / 90 / 0` | Rotação fina da caixa |
| `CFG_SND_OFF_X/Y/Z` | `0.25 / 0.10 / 0.75` | Onde o som 3D nasce em relação ao corpo |
| `CFG_VOLUME` | `1.0` | Volume da música |
| `CFG_MAX_TRACKS` | `50` | Número máximo de faixas que o script procura (`som1..som50`) |
| `CFG_PATH_SIZE` | `64` | Tamanho do buffer do caminho do MP3 |
| `CFG_CLEOPLUS_MIN` | `16908288` (`0x01020000` = v1.2.0.0) | Versão mínima do CLEO+ aceita |
| `CFG_APPEAR_DIST` | `4.0` | Distância em que ele aparece/teleporta |
| `CFG_FOLLOW_D2` | `9.0` | Distância² (3 m) em que ele começa a te seguir |
| `CFG_SPRINT_D2` | `400.0` | Distância² (20 m) em que ele corre mais rápido |
| `CFG_LOST_D2` | `3600.0` | Distância² (60 m) considerado "longe demais" |
| `CFG_JUMP_D2` | `625.0` | Distância² (25 m) de "salto" do player = troca de interior |
| `CFG_FALL_Z` | `25.0` | Se o NPC ficar 25 m abaixo de você, é resgatado |
| `CFG_STOP_DIST` | `2.5` | Raio em que ele para de andar |
| `CFG_TELEPORT_MS` | `3000` | Quanto tempo longe (e a pé) antes de teleportar |
| `CFG_STUCK_MS` | `6000` | Quanto tempo parado longe antes de teleportar |
| `CFG_RETASK_MS` | `1000` | Intervalo entre comandos de seguir |
| `CFG_LOAD_MS` | `10000` | Timeout ao carregar modelos/mundo |
| `CFG_AUDIO_RETRY_MS` | `20000` | Espera entre tentativas se nenhum MP3 abrir |

### Ajustando a caixa na mão

A caixa é presa no osso da mão direita com um deslocamento/rotação
configurável. Os valores padrão foram escolhidos por cálculo, **não testados
in-game** — se a caixa aparecer torta ou longe da mão, ajuste
`CFG_BOX_OFF_*` (posição, em metros, no espaço do osso) e `CFG_BOX_ROT_*`
(graus) e recompile. Dica: `CFG_BOX_ROT_Y 90.0` costuma ser necessário para
a caixa ficar "de pé" na mão.

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
  `1.2.0.0`, mostra um alerta explicativo e termina com
  `TERMINATE_THIS_CUSTOM_SCRIPT` — ou seja, ele nunca chama um opcode do
  CLEO+ que não existe, o que seria um crash certo.
- **Checagem de colisão**: antes de criar ou teleportar o NPC o script
  exige `NOT IS_CHAR_WAITING_FOR_WORLD_COLLISION` (o mundo já carregou em
  volta do player), pede a colisão do ponto de destino com
  `REQUEST_COLLISION` e confere o chão com `GET_GROUND_Z_FOR_3D_COORD`. A
  posição só é aceita se o chão estiver a ±3 m do chão do player — isso
  evita nascer em telhado, ponte, dentro de prédio ou **cair no vazio
  embaixo do mapa**. Se nada servir, a última alternativa é o próprio lugar
  do player (onde o chão com certeza existe).
  Além disso, se o NPC acabar 25 m abaixo de você (caiu no vazio), o script
  o resgata.
- **Interiores**: entrar/sair de interior (ou qualquer teleporte do player)
  faz a posição do CJ "saltar" mais de 25 m num único quadro. O script
  detecta esse salto e leva o NPC junto, respeitando as checagens de
  colisão acima.
- **Cutscenes**: durante `IS_ON_CUTSCENE` ou `IS_ON_SCRIPTED_CUTSCENE` a
  música é pausada (`0AAD` ação 2) e retomada de onde parou (ação 3) no
  fim, e o script evita mexer no NPC no meio da cena.
- **Nada de variáveis globais do jogo.** Só `LVAR_*` locais do script
  (31 locais, dentro do limite de 32 do CLEO).
- **Modelos**: os dois modelos (NPC e caixa) são pedidos com `REQUEST_MODEL`
  e só depois de `HAS_MODEL_LOADED` o ped é criado; se estourar o timeout,
  o script avisa e volta ao estado inicial. Os modelos são liberados com
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
- O loop principal tem `WAIT 0`, e o script volta ao estado inicial se o
  player morrer, se o char sumir do pool, se o NPC morrer ou se for
  dispensado.
- **Save**: como todo ped criado por script, o NPC pode acabar indo parar no
  seu savegame (sem a caixa, que é um objeto de render e não é salvo). Se
  isso incomodar, dispense ele com `BOOBOXD` antes de salvar.

---

## Créditos

- Mod original: **90s Boombox** por *Guidopdu*
  ([MixMods](https://www.mixmods.com.br/2016/02/90s-boombox-andar-ouvindo-radio/))
  — 10 faixas, som direcional e a ideia de "andar ouvindo rádio".
- CLEO+ por *Junior_Djjr* (MIT) — inclusive as definições de opcodes em
  `tools/cleo-plus.xml`.
- Este remake: escrito em gta3script/CLEO+ para GTA San Andreas.
