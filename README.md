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
começa a tocar uma faixa aleatória das suas 10. Quando a música acaba, ele
sorteia outra automaticamente.

- Se você cruzar a cidade de carro, moto, helicóptero ou avião e **pisar de
  volta no chão**, ele é teleportado para junto de você (só quando você está
  a pé, no chão e fora da água) e volta a te seguir.
- Se ele ficar **preso** em algum canto (parede, cerca, beco) enquanto você
  se afasta, ele também volta a aparecer perto — o script percebe que ele
  não chegou mais perto em 6 segundos e teleporta.
- **Sem blip no mapa**, sem marcador, sem ícone.
- Ele **não reage ao mundo**: não foge, não se assusta, não briga, não
  pertence à sua gangue. Isso é feito com um *decision maker* vazio
  (`DM_PED_EMPTY`) e com o scanner de ameaças desligado — ele só faz o que o
  script manda.
- **Vida padrão** (100). Se ele morrer, acabou: a música para e você precisa
  digitar o cheat de novo.
- Se você morrer, ele vai embora sozinho.

---

## Instalação

1. Tenha o **CLEO 4** e o **CLEO+** instalados (o script usa opcodes CLEO+,
   principalmente para prender a caixa na mão e para o áudio 3D).
2. Copie `BoomboxGuy.cs` para a pasta `CLEO/` do GTA San Andreas.
3. Crie a pasta `CLEO/BoomboxGuy/` e coloque de 1 a 10 músicas com os nomes
   **fixos**:

```
GTA San Andreas/
  CLEO/
    BoomboxGuy.cs
    BoomboxGuy/
      som1.mp3
      som2.mp3
      ...
      som10.mp3
```

   Formatos: MP3 (recomendado), OGG, WAV, AIFF — o que o BASS do CLEO
   conseguir abrir. Arquivos que faltarem são simplesmente pulados (o script
   sorteia outra faixa); se nenhum for encontrado, ele avisa na tela.

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
| `CFG_TRACKS` | `10` | Quantas faixas usar (`som1`..`som10`) |
| `CFG_APPEAR_DIST` | `4.0` | Distância em que ele aparece/teleporta |
| `CFG_FOLLOW_D2` | `9.0` | Distância² (3 m) em que ele começa a te seguir |
| `CFG_SPRINT_D2` | `400.0` | Distância² (20 m) em que ele corre mais rápido |
| `CFG_LOST_D2` | `3600.0` | Distância² (60 m) considerado "longe demais" |
| `CFG_STOP_DIST` | `2.5` | Raio em que ele para de andar |
| `CFG_TELEPORT_MS` | `3000` | Quanto tempo longe (e a pé) antes de teleportar |
| `CFG_STUCK_MS` | `6000` | Quanto tempo preso antes de teleportar |
| `CFG_RETASK_MS` | `1000` | Intervalo mínimo entre comandos de seguir |
| `CFG_LOAD_MS` | `6000` | Timeout ao carregar os modelos |
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
       -o BoomboxGuy.cs BoomboxGuy.sc
```

- `--cs` = script CLEO (já implica `-fcleo`).
- `--guesser` = habilita recursos que a comunidade ainda não mapeou 100%
  (necessário para `SWITCH` e para o `-fcleo` neste parser).
- `-fno-entity-tracking` = não valida o tipo "entidade" das variáveis
  (handles de char/objeto/áudio) em tempo de compilação — sem isso o
  compilador reclama que `AUDIO_STREAM` não confere com `INT`.
- `-fconst`, `-fswitch`, `-farrays` já são ligados automaticamente pelo
  `config/gtasa/commandline.txt` do gta3sc, por isso as `CONST_*` funcionam
  sem flag extra.

---

## Estrutura do repositório

```
BoomboxGuy.sc        fonte (gta3script) — é aqui que você mexe
BoomboxGuy.cs        compilado (vai para CLEO/)
build.sh / Makefile  builds
BoomboxGuy/          pasta-exemplo para os MP3 (som1.mp3 ... som10.mp3)
```

---

## Como foi feito / notas de segurança

O script foi escrito pensando em **não crashar**, que era o problema do mod
original (que chegou a ter correções por causa de variáveis globais
bagunçadas):

- **Nada de variáveis globais do jogo.** Só `LVAR_*` locais do script
  (31 locais, dentro do limite de 32 do CLEO).
- **Modelos**: os dois modelos (NPC e caixa) são pedidos com `REQUEST_MODEL`
  e só depois de `HAS_MODEL_LOADED` o ped é criado; se estourar o timeout,
  o script avisa e volta ao estado inicial. Os modelos são liberados com
  `MARK_MODEL_AS_NO_LONGER_NEEDED` sempre em par com o pedido.
- **Áudio**: `LOAD_3D_AUDIO_STREAM` tem o retorno checado, e o stream é
  liberado com `REMOVE_AUDIO_STREAM` antes de carregar o próximo e ao
  dispensar o NPC.
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
- **Teleporte** só acontece com o player a pé, **no chão**
  (`IS_CHAR_REALLY_IN_AIR` falso), **fora da água** e fora de veículo.
  As posições candidatas em volta do player passam por
  `GET_GROUND_Z_FOR_3D_COORD` e só são aceitas se o chão estiver perto do
  chão do player (evita nascer em telhado/ponte/interior); se nenhuma
  servir, ele cai na própria posição do player.
- **Quatro posições candidatas** (atrás, laterais e frente) são testadas, do
  jeito que o `GET_COORD_FROM_ANGLED_DISTANCE` do CLEO+ calcula.
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
- CLEO+ por *Junior_Djjr* (MIT).
- Este remake: escrito em gta3script/CLEO+ para GTA San Andreas.
