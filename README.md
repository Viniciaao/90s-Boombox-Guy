<div align="center">

# 🎧 90s Boombox Guy

### Remake do clássico *90s Boombox* (Guidopdu), reescrito do zero em gta3script + CLEO+

<img alt="GTA San Andreas 1.0 US" src="https://img.shields.io/badge/GTA_San_Andreas-1.0_US-2f2f2f?style=for-the-badge">
<img alt="Requer CLEO 4 + CLEO+ v1.2+" src="https://img.shields.io/badge/requer-CLEO_4_%2B_CLEO%2B_v1.2+-blue?style=for-the-badge">
<img alt="Feito com gta3script" src="https://img.shields.io/badge/feito_com-gta3script-orange?style=for-the-badge">
<img alt="Versão v6.7" src="https://img.shields.io/badge/vers%C3%A3o-v6.7-success?style=for-the-badge">

**Um NPC "cara do som" aparece longe de você, vem correndo com a caixa de som<br>
e passa a te acompanhar com música em áudio 3D — na rua, no interior<br>
e até na garupa da moto.**

</div>

---

## 📑 Índice

- [🎮 Como usar](#-como-usar)
- [🚀 Instalação](#-instalação)
- [🎵 Músicas](#-músicas)
- [⚙️ Configuração](#-configuração)
- [🎛️ Ajuste ao vivo da caixa (`BBGUYTUNE`)](#-ajustando-a-caixa-ao-vivo-cheat-bbguyttune)
- [❓ Problemas comuns](#-problemas-comuns)
- [📦 Estrutura do repositório](#-estrutura-do-repositório)
- [🔧 Compilando](#-compilando)
- [🧠 Como foi feito / notas de segurança](#-como-foi-feito--notas-de-segurança)
- [📜 Histórico de versões](#-histórico-de-versões)
- [🙏 Créditos](#-créditos)

---

## 🎮 Como usar

| Cheat | O que faz |
| --- | --- |
| `BOOBOX` | Chama o NPC (o cara da caixa de som) |
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
  passageiro livre** (carro, táxi, ônibus, barco, helicóptero, avião… e
  **moto**, na garupa), o NPC entra junto e viaja com você. Ele espera o
  carro dar uma parada, sobe e sai do veículo quando você sai. A procura da
  cadeira começa pela **0** — que é a carona da frente em carro de quatro
  lugares e a **garupa em carro de dois lugares e na moto** (começar pela 1
  era o que impedia a moto). Se o veículo estiver lotado, ele continua te
  seguindo a pé.
- **Interiores**: se você entrar (ou sair) de qualquer interior, ele vai
  junto **na hora** e aparece **logo à frente do CJ**. Interiores no GTA
  são "áreas" separadas, e o jogo só desenha — e só mantém a colisão
  carregada — de quem está na mesma área do player. O script mantém o NPC
  sempre na sua área e, quando ela muda (você entrou, saiu ou trocou de
  sala), o resgate é imediato, no mesmo piso. Lá dentro ele anda em linha
  reta: a malha de navegação de pedestres não cobre interiores.
- **Ele para a 2,5 m de você.** Nada de entrar dentro do CJ: a caminhada é
  cancelada antes de chegar em você (vale na rua e no interior), então ele
  não te empurra mais para dentro de parede nem trava você num canto — e
  não existe aquele "pulinho" no fim do trajeto. **Parado, ele fica sempre
  virado para você**, acompanhando com o corpo para onde você for.
- **Soco = próxima faixa.** De um **soco** nele (de mãos livres, sem arma na
  mão) e a música **muda na hora** — é o "próxima faixa" do mod, sem tecla
  nenhuma, feito de dentro do jogo. Tiro, faca, carro e soco de outro NPC
  não contam; soco não mata o Boombox Guy (a vida dele volta ao normal, ele
  continua do seu lado).
- **Fuga (carro/avião) sem lugar para ele**: se você se afastar muito e
  **pisar de volta no chão**, ele é teleportado para junto de você (também
  atrás, para não "aparecer do nada" na sua cara).
- **NPC preso / "ele parou e não vem mais"**: se ele travar num canto (parede,
  cerca, porta, escada, ponto que a malha de navegação não cobre) o script
  percebe pela **velocidade** dele e reage em três passos, do mais discreto
  para o mais visível: reenvia a ordem de seguir (1 s); se em 0,7 s ele não
  saiu do lugar, passa a ir **em linha reta** até você, ignorando a malha de
  navegação — sem nada aparecer na tela; e se em 3,5 s ainda não saiu do
  lugar, ele reaparece perto de você. A rota normal volta sozinha quando ele
  chega perto.
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

## 🚀 Instalação

1. Tenha o **CLEO 4** e o **CLEO+** instalados. O script usa opcodes CLEO+
   (caixa na mão, áudio 3D, detecção de cutscene, leitura do estado do NPC).
   **Se o CLEO+ não estiver instalado (ou estiver desatualizado), o script
   avisa na tela e se encerra sozinho, sem crashar o jogo.**
2. Copie `BoomboxGuy.cs` para a pasta `CLEO/` do GTA San Andreas.
3. Crie a pasta `CLEO/BoomboxGuy/` e coloque suas músicas:

```
GTA San Andreas/
  CLEO/
    BoomboxGuy.cs            <- o mod
    BoomboxGuy/
      LEIA-ME.txt            <- instruções (vão junto)
      BoomboxGuy.ini         <- configuração (o script cria sozinho)
      som1.mp3
      som2.mp3
      ...
      som999.mp3
      radio.dff              <- (opcional) caixa por arquivo, sem ID
      radio.txd              <- (opcional) idem
```

Não é necessário nenhum arquivo DFF/TXD: por padrão a caixa de som é o
**objeto nativo do jogo** `2226 (low_hi_fi_3)`. Se quiser outra caixa, há dois
caminhos (detalhes em [Modelo da caixa: por ID ou por arquivo](#-modelo-da-caixa-por-id-ou-por-arquivo)):

- **O objeto já existe no jogo** (vanilla, ou registrado num `.ide`, inclusive
  o de ModLoader): aponte `[Caixa] model=` para o **nome do DFF** ou o ID.
- **O objeto não está em nenhum `.ide`** (sem ID): ponha o `.dff` e o `.txd`
  soltos em `CLEO/BoomboxGuy/` (ex.: `radio.dff` + `radio.txd`) e escreva
  `model=radio`. O script lê os arquivos direto do disco com o
  `LOAD_SPECIAL_MODEL` do CLEO+.

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

## 🎵 Músicas

Nome dos arquivos: `som1.mp3`, `som2.mp3` … `som999.mp3` (não precisa ser MP3:
valem OGG, WAV e AIFF também — o que o BASS do CLEO abrir).

- **Quantidade: de 1 até 999.** O script **varre a pasta e guarda o número de
  cada arquivo que existe** e usa só o que está lá — funciona com 1, 2, 10 ou
  999 músicas, e até com numeração "esburacada" (por exemplo só `som1`,
  `som4` e `som9`). Arquivos que faltarem são simplesmente ignorados. O teto
  é a constante `CFG_MAX_TRACKS` (`999`); para mais que isso, mude ela e
  recompile (custa 4 bytes de memória por faixa e um teste de arquivo por
  número na varredura).
- **A ordem é sempre aleatória**, nunca sequencial, e ele **não repete a
  mesma faixa duas vezes seguidas**. Quando a música acaba, ele sorteia
  outra sozinho.
- Dá para **colocar/tirar arquivos com o jogo aberto**: não precisa
  reiniciar o jogo.
- Se nenhum arquivo for encontrado, aparece na tela "nenhuma musica
  encontrada" — um dos **dois únicos avisos** do mod (o outro é o de CLEO+
  faltando).

---

## ⚙️ Configuração

Todo o "painel de controle" fica no topo de `BoomboxGuy.sc`, em constantes
fáceis de achar (`CONST_INT` / `CONST_FLOAT`). Depois de mudar, recompile
com `./build.sh` (ou `make`).

| Constante | Padrão | Para que serve |
| --- | --- | --- |
| `CFG_PED_MODEL` | `7` (male01) | Skin **padrão** do NPC — só é usada se o `.ini` não tiver um `model=` válido |
| `CFG_BOX_MODEL` | `2226` (low_hi_fi_3) | **Objeto padrão** da caixa de som (idem: reserva do `.ini`) |
| `CFG_VAR_BOX` / `CFG_VAR_INI` | `1023` / `1022` | "Vars compartilhadas" do CLEO (`0AB3`/`0AB4`) que guardam o objeto da caixa escolhido no `.ini` e o **handle do arquivo** — as 32 `LVAR` do script estão todas em uso, e o parser do `.ini` usa `tmpInt`/`dt`/`loadTick` como rascunho |
| `CFG_BOX_BONE` | `24` (`BONE_R_HAND`) | Osso onde a caixa é presa |
| `CFG_BOX_OFF_X/Y/Z` | `0.40 / 0.02 / 0.02` | Posição da caixa na mão (**padrão do .ini**) |
| `CFG_BOX_ROT_X/Y/Z` | `0 / -90 / 0` | Rotação da caixa (**padrão do .ini**) |
| `CFG_BOX_SCALE` | `1.0` | Tamanho da caixa (`0.7` deixa a caixa menor) |
| `CFG_INI_MAX_LINES` | `200` | Trava de segurança na leitura do `.ini` |
| `CFG_TUNE_POS` / `CFG_TUNE_ROT` | `0.02` / `5.0` | Tamanho do passo no modo `BBGUYTUNE` |
| `CFG_TUNE_KEY` | `46` (Delete) | Tecla que grava o ajuste da caixa em arquivo |
| `CFG_SND_OFF_X/Y/Z` | `0.25 / 0.10 / 0.75` | Onde o som 3D nasce em relação ao corpo |
| `CFG_VOLUME` | `1.0` | Volume padrão da música (**o `.ini` pode mudar**) |
| `CFG_MAX_TRACKS` | `999` | **Teto** de faixas que o script procura (`som1..som999`) — não é uma lista obrigatória: quem tem 3 músicas usa 3 |
| `CFG_TRACK_SCAN_MS` | `10000` | Intervalo mínimo entre duas varreduras da pasta de músicas (a lista é refeita depois disso) |
| `CFG_PATH_SIZE` | `128` | Tamanho do buffer de linha do `.ini` / caminho do MP3 (cabe uma lista de uns 15 peds) |
| `CFG_PED_MAX` | `16` | Quantos peds cabem na lista `[Ped] model=a,b,c` |
| `CFG_NAME_MAX` | `40` | Maior nome de arquivo (sem extensão) aceito na caixa por arquivo |
| `CFG_MEM_*` | — | Mapa do bloco de memória único do script (linha do `.ini`, ids dos peds, nomes do DFF/TXD, caminhos e a **lista das faixas que existem**, 4 bytes por uma) — fica em `bufPath` |
| `CFG_VAR_SECT` / `_PEDN` / `_LASTPED` | `1017` / `1016` / `1015` | Vars do CLEO: seção do `.ini` que está sendo lida, nº de peds na lista e último ped sorteado |
| `CFG_VAR_SPECIAL` / `_CACHE_H` / `_CACHE_K` | `1014` / `1013` / `1012` | Vars do CLEO: handle do modelo especial em uso, e o cache (handle + hash dos nomes) do último carregado |
| `CFG_VAR_TRACKN` / `_TRACKT` | `1011` / `1010` | Vars do CLEO: quantas músicas a última varredura achou e quando ela aconteceu |
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
| `CFG_STUCK_MS` | `3500` | Quanto tempo **sem sair do lugar** antes do resgate |
| `CFG_STILL_SPEED` | `1.0` | Abaixo desta velocidade (o `06AC` já vem ×50) ele conta como parado |
| `CFG_STRAIGHT_MS` | `700` | Quanto tempo travado até passar a ir em linha reta |
| `CFG_FACE_RANGE_D2` | `400.0` | Distância² (20 m) até onde ele se vira para você |
| `CFG_RETASK_MS` | `1000` | Intervalo entre comandos de seguir |
| `CFG_CAR_RETASK_MS` | `2000` | Intervalo entre tentativas de entrar no seu veículo |
| `CFG_LOAD_MS` | `10000` | Timeout ao carregar modelos/mundo |
| `CFG_AUDIO_RETRY_MS` | `20000` | Espera entre tentativas se nenhum MP3 abrir |

### 📄 Arquivo de configuração: `CLEO/BoomboxGuy/BoomboxGuy.ini`

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
; Pode ser uma LISTA separada por virgula: a cada BOOBOX o script
; sorteia uma (ex.: model=wmybu,male01,fam1,bfori). Ate 16 skins.
model=male01
[Caixa]
; objeto da caixa: nome do DFF de um OBJETO (ex.: low_hi_fi_3)
; ou o ID (ex.: 2226). Qualquer objeto do jogo serve. Se o nome nao
; existir (ou o modelo nao for um objeto), a caixa usa o padrao.
; Objeto que NAO esta em nenhum .ide (sem ID): ponha o DFF e o TXD
; soltos na pasta CLEO\BoomboxGuy\ e escreva aqui o nome deles, sem
; extensao (radio.dff + radio.txd = model=radio). Se o TXD tiver outro
; nome, acrescente a linha txd=nomedotxd. Acerte a pose com BBGUYTUNE.
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
- **Variações de NPC (lista)** — `model=` aceita **vários itens separados
  por vírgula**, misturando nomes e IDs:

  ```ini
  [Ped]
  model=wmybu,male01,fam1,bfori
  ```

  A cada `BOOBOX` o script **sorteia uma** das skins da lista e **evita
  repetir a anterior**. Itens inválidos (nome que o jogo não conhece, modelo
  que não é pedestre, DFF ausente do jogo) são **ignorados** sem afetar os
  outros; se nenhum item servir, vale o `CFG_PED_MODEL`. Cabem até 16 skins
  (`CFG_PED_MAX`). O texto da linha é preservado quando o `BBGUYTUNE` regrava
  o `.ini`. Skin de ped **precisa de ID** (registrada num `.ide`): o jogo não
  cria pedestre a partir de um DFF solto.
- **`[Caixa] model`** — o **objeto da caixa de som**. Aqui é o contrário:
  só vale para **objeto** (aceita os tipos de objeto do jogo: atômico,
  clump, com hora do dia e LOD), nunca pedestre nem veículo. Se o nome não
  existir, for um ped ou um carro, ou o arquivo do modelo não estiver no
  jogo, a caixa volta para `CFG_BOX_MODEL` (o `low_hi_fi_3` original),
  **sem aviso e sem erro** — a não ser que exista um `nome.dff` + `nome.txd`
  em `CLEO/BoomboxGuy/`: aí vale a **caixa por arquivo** (próxima seção).
- **`[Caixa] txd`** — (opcional) nome do TXD da caixa por arquivo, quando ele
  não tem o mesmo nome do DFF.
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
  `[Ped]` use um pedestre; em `[Caixa]` use um objeto. (Exceção: a caixa **por
  arquivo** só vale em `[Caixa]`, porque um nome sem ID não tem tipo para o
  script conferir.)
- Valores inválidos, chaves escritas errado, linhas em branco e comentários
  (linhas começando com `;` ou `#`) são simplesmente ignorados: a chave que
  não for encontrada continua com o valor padrão, **nunca dá erro nem
  trava**. Chaves com a primeira letra maiúscula (`PosX`, `Volume`,
  `Model`) também são aceitas, e pode ter espaços em volta do `=`.
- Para voltar tudo ao padrão, é só apagar o arquivo: o script cria outro na
  próxima vez que abrir o jogo (com `model=male01`).

### 📦 Modelo da caixa: por ID ou por arquivo

**Por nome/ID (`model=low_hi_fi_3` ou `model=2226`)** — o nome é procurado
pelo jogo com o opcode `0E9C` (`GET_MODEL_BY_NAME`), que consulta a tabela de
modelos **registrados** (`.ide`, inclusive o de ModLoader/fastman92). Ou seja:
**só funciona se o modelo tiver um ID**. Um DFF que não está em nenhum `.ide`
não aparece nessa tabela, e o nome não é resolvido.

**Por arquivo (`model=radio`)** — para o objeto **sem ID**. O script usa o
`LOAD_SPECIAL_MODEL` do CLEO+, que lê um DFF e um TXD direto do disco e
devolve um *modelo especial* (sem ID, fora do streaming do jogo); o
`CREATE_RENDER_OBJECT_TO_CHAR_BONE_FROM_SPECIAL` prende esse modelo na mão do
NPC. Como usar:

1. Coloque `radio.dff` e `radio.txd` em `CLEO/BoomboxGuy/`.
2. No `.ini`, em `[Caixa]`: `model=radio` (sem extensão). Se o TXD tiver outro
   nome, acrescente `txd=nomedotxd`.
3. Digite `BOOBOX`. A pose (`posX…rotZ`) é a **da caixa original** — use o
   `BBGUYTUNE` para acertar a do seu modelo e salve.

Regras:

- **Ordem de prioridade**: nome/ID que o jogo conhece → arquivo em
  `CLEO/BoomboxGuy/` → caixa padrão (`low_hi_fi_3`). Se faltar um dos dois
  arquivos, o DFF não abrir ou der qualquer erro, cai para a próxima opção,
  **sem aviso**.
- Os **dois arquivos são conferidos antes** de chamar o CLEO+: o
  `LOAD_SPECIAL_MODEL` com TXD ausente/errado é do tipo que trava o jogo.
- O modelo é carregado **uma vez** e reaproveitado nos `BOOBOX` seguintes
  (o comando recarrega o TXD por cima do anterior a cada chamada, vazando
  memória). Se você trocar o nome no `.ini`, o novo é carregado no próximo
  `BOOBOX`; se você **editar o conteúdo** do mesmo `.dff`/`.txd`, reabra o
  jogo.
- **Só para a caixa.** Skin de pedestre precisa de ID: o jogo não cria ped a
  partir de um DFF solto. Para skin de mod, registre-a num `.ide` (ModLoader
  faz isso com a pasta certa) e use o nome/ID em `[Ped]`.
- Requer CLEO+ v1.2 (o mesmo que o script já exige). O DFF precisa ter ao
  menos um atômico (qualquer DFF de objeto comum tem). Nomes de arquivo usam
  letras, números e `_` (até 40 caracteres).

### 🎛️ Ajustando a caixa ao vivo (cheat `BBGUYTUNE`)

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

## 🔧 Compilando

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

### Testando sem abrir o jogo

O sorteio de faixas dá para conferir fora do jogo: o `tools/ir2_sim.py`
**executa o IR2 que o compilador emite** (não é uma reimplementação em
Python) com a pasta de músicas, a memória, o relógio e o sorteio imitados.

```bash
gta3sc --config=gtasa --guesser --cs -fcleo -fno-entity-tracking \
       --add-config=tools/cleo-plus.xml -emit-ir2 -o /tmp/bbg.ir2 BoomboxGuy.sc
python3 tools/ir2_sim.py /tmp/bbg.ir2
```

Ele chama `bbg_scan_tracks` e `bbg_pick_track` do próprio bytecode e confere:
quantas faixas a varredura achou, se o sorteio é uniforme, se nunca repete a
faixa anterior, o que acontece com 1 faixa / numeração esburacada / pasta
vazia / 999 faixas, quando a pasta é revarrida e se nada lê ou escreve fora
do bloco de memória.

---

## ❓ Problemas comuns

| Sintoma | O que fazer |
| --- | --- |
| "Cleo+ não encontrado" / "CLEO+ desatualizado" | O `CLEO+.cleo` não está na pasta `CLEO/` ou é mais velho que a **v1.2**. Instale/atualize e entre no jogo de novo. |
| "Nenhuma musica encontrada" | Não existe nenhum `som*.mp3` em `CLEO/BoomboxGuy/`. Confira o nome dos arquivos. |
| Digito `BOOBOX` e nada acontece | O `BoomboxGuy.cs` está em `CLEO/`? O jogo tem CLEO 4? (Se o CLEO+ faltar, o aviso aparece na tela.) |
| A skin do NPC não mudou | O `BOOBOX` relê o `.ini`: confira o nome do DFF (sem `.dff`) e se o modelo é um **pedestre**. Nome errado cai no `male01` sem avisar. |
| Pus `model=radio` e continua saindo o `low_hi_fi_3` | Confira se `radio.dff` **e** `radio.txd` estão em `CLEO/BoomboxGuy/`, se a linha está em `[Caixa]` e se o nome tem só letras/números/`_` (sem `.dff`). Qualquer falha cai na caixa padrão sem aviso. |
| Só aparece uma skin de NPC mesmo com lista | Os outros itens não foram aceitos: cada nome tem que ser um **pedestre com ID** (registrado em `.ide`) e com o DFF no jogo. Itens inválidos são ignorados. |
| A caixa está torta ou fora da mão | Use o modo `BBGUYTUNE` (seção acima) e salve com `DELETE`. |
| A música não toca | Veja o `volume=` no `[Som]` do `.ini` (`0.0` = mudo) e teste outro arquivo. O som sai **da caixa**, em 3D: de longe ele é abafado de propósito. |
| O NPC ficou para trás | Fugiu de carro/avião sem lugar para ele? Ele volta quando você descer e **pisar no chão**. Travou num canto? O script resolve sozinho em alguns segundos (linha reta e, em último caso, reaparecendo perto). |

Tudo isso também está no **`LEIA-ME.txt`** que vai junto com o mod, dentro de
`CLEO/BoomboxGuy/` (pronto para consultar sem sair do jogo).

---

## 📦 Estrutura do repositório

```
BoomboxGuy.sc            fonte (gta3script) — é aqui que você mexe
BoomboxGuy.cs            compilado (vai para CLEO/)
build.sh / Makefile      builds
tools/cleo-plus.xml      opcodes do CLEO+ para o gta3sc (MIT, Junior_Djjr)
tools/ir2_sim.py         teste do sorteio de faixas sem abrir o jogo (Python 3)
BoomboxGuy/BoomboxGuy.ini  cópia de exemplo do arquivo de configuração
BoomboxGuy/LEIA-ME.txt     instruções que vão junto com o mod (em texto puro)
README.md                este arquivo
```

---

## 🧠 Como foi feito / notas de segurança

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
- **Interiores**: cada interior do GTA vive numa "área" própria (0 = rua,
  1..18 = interiores) e o jogo só desenha — e só mantém a colisão carregada
  — das entidades que estão na área do player. O script lê a área do player
  (`09E8`), o id da área que o jogo está mostrando (`077E`) e mantém o NPC
  na mesma (`0860` — é o mesmo truque que os spawners usam com `0840` para
  carros, e resolve os três sintomas de uma vez: NPC aparecendo na rua em
  vez de dentro do ambiente, preso e morrendo de queda). Quando a área muda,
  o NPC vai para a frente do CJ na hora, sem passar pelo teste de solo (que
  não encontra o piso de dentro do ambiente) — nas ruas continua valendo o
  salto de 25 m com as checagens de colisão de sempre. Se ainda assim ele
  ficar mais de 3 m abaixo do player dentro do ambiente (atravessando o
  piso), é resgatado antes de virar dano de queda. E lá dentro ele anda em
  linha reta (`TASK_GO_STRAIGHT_TO_COORD`): a malha de navegação não existe
  em interiores, e com o `TASK_FOLLOW_PATH_NODES` ele ficava parado.
- **Parar antes de chegar**: o destino dele é a sua posição, e quem o faz
  parar é o script — perto de você (< 2,5 m) e ainda andando, ele recebe um
  `CLEAR_CHAR_TASKS` (`GET_CHAR_MOVE_STATE`, CLEO+ `0ECB`, diz se ele está
  andando). Assim ele nunca encosta no destino: não empurra o player e não
  aparece o "fechamento" da tarefa de andar no último metro (o pulinho que
  se via em interior). Entre 2,5 m (quando ele para) e 3 m (quando ele volta
  a andar) existe uma faixa morta de meio metro, que evita ligar/desligar a
  caminhada a cada quadro. Já o reposicionamento por troca de área só
  acontece se ele estiver a mais de 1,5 m do ponto: mover um passo
  apareceria como teleporte, então nesse caso o script só reenvia a tarefa.
- **Nunca fica plantado**: quem cuida de "ele deveria estar vindo e não sai do
  lugar" é o `bbg_stuck_check`. O teste antigo era só o estado do motor
  (`GET_CHAR_MOVE_STATE`, CLEO+ `0ECB`), e isso só enxerga **um** dos dois
  jeitos de ele travar: quem está sem tarefa nenhuma. Quem ficou com a tarefa
  de correr presa em algum canto (porta, escada, ponto sem nó de caminho)
  aparece como "correndo" para o motor — ele ficava parado para sempre, sem
  nem o teleporte de resgate vir. Agora a conta usa a **velocidade de
  verdade** (`GET_CHAR_SPEED`, `0x6AC`, que o jogo devolve já multiplicada por
  50), que é zero nos dois casos, e o cronômetro só corre enquanto ele está
  longe (> 3 m). Ordem das providências: (1) a tarefa é reenviada a cada 1 s
  de qualquer forma; (2) 0,7 s travado ⇒ liga o "modo linha reta"
  (`CFG_VAR_ROUTE`, uma var do CLEO, porque as 32 vars locais do script já
  estão todas em uso), que troca
  `TASK_FOLLOW_PATH_NODES_TO_COORD_WITH_RADIUS` por
  `TASK_GO_STRAIGHT_TO_COORD` — a mesma tarefa dos interiores — sem piscar
  nada na tela; (3) 3,5 s travado ⇒ resgate por teleporte (o de sempre). O
  modo linha reta volta a 0 assim que ele chega perto de você, quando é
  reposicionado ou quando o NPC é criado de novo.
- **Sempre virado para você**: `TASK_TURN_CHAR_TO_FACE_CHAR` (`0639`,
  interno `CTaskComplexTurnToFaceEntityOrCoord`) é reenviado a cada 1,5 s
  **só quando ele está parado** — andando, o corpo já aponta para onde ele
  vai. Isso só vale **até 20 m** (`CFG_FACE_RANGE_D2`): a virada troca a
  tarefa primária do NPC, e de longe a prioridade dele é andar — quem cuida
  de quem não sai do lugar é o `bbg_stuck_check`. A tarefa segura uma
  referência do CJ com contagem de referência do próprio jogo
  (`SafeRegisterRef`), então não sobra ponteiro solto.
- **Soco = troca de faixa**: o script olha a **vida dele caindo desde o
  quadro anterior** (`GET_CHAR_HEALTH` `0x226` contra uma var do CLEO), e o
  evento só conta se, naquele instante, as mãos do player estiverem livres
  (`GET_CURRENT_CHAR_WEAPON` `0x470` = 0), o player estiver **a pé**
  (nada de atropelar contando como soco) e o **último a machucá-lo** tiver
  sido o player (`HAS_CHAR_BEEN_DAMAGED_BY_CHAR` `0x51A`) com ele ao alcance
  do braço. Aí sorteia outra faixa na hora e devolve a vida dele a 100 —
  soco é comando, não briga. Uma sequência rápida de socos conta como um
  comando só (`CFG_PUNCH_MS` = 700 ms).
- **Cutscenes**: durante `IS_ON_CUTSCENE` ou `IS_ON_SCRIPTED_CUTSCENE` a
  música é pausada (`0AAD` ação 2) e retomada de onde parou (ação 3) no
  fim, e o script evita mexer no NPC no meio da cena.
- **Nada de variáveis globais do jogo.** O trabalho é todo feito com as
  `LVAR_*` locais do script — 32, exatamente o limite do CLEO (por isso o
  script evita variáveis "de enfeite"). O único valor que precisa sobreviver
  de um quadro para o outro além delas mora em *vars compartilhadas do CLEO*
  (`0AB3`/`0AB4`): o **objeto da caixa escolhido no `.ini`** (`CFG_VAR_BOX`)
  e o **handle do arquivo `.ini`** (`CFG_VAR_INI`). São 1024 espaços
  numerados do próprio CLEO, não são variáveis do savegame do jogo. Os dois
  são revalidados a cada uso: o handle é reconferido a cada linha lida
  (valor pequeno = lixo ⇒ o script para de ler e fica com os padrões) e o
  modelo da caixa passa pelo teste de tipo — se outro script escrever por
  cima, nada quebra.
- **Handle de arquivo fora das LVARs.** O parser do `.ini` usa
  `tmpInt`/`dt`/`loadTick` como rascunho; enquanto o handle do arquivo
  morava em `tmpInt`, a primeira linha `model=` sobrescrevia o handle e o
  `fgets` era chamado com um ponteiro inválido — crash. Hoje o handle fica
  na var `CFG_VAR_INI` e é re-lido antes de cada linha.
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
- **Lista de peds e caixa por arquivo**: tudo vive num **único bloco**
  (`ALLOCATE_MEMORY`) guardado em `bufPath` — o bloco carrega o buffer de
  linha, os IDs, os nomes e os caminhos. Nada de ponteiro em var compartilhada
  (a memória alocada pelo CLEO morre com o script e o ponteiro ficaria
  pendurado quando o jogo recarrega os scripts). Cada item da lista passa
  pelas mesmas checagens do modelo único (existe, tipo de pedestre, DFF no
  jogo, ID < 20000) antes de entrar no sorteio. Para a caixa por arquivo:
  os arquivos são conferidos com `DOES_FILE_EXIST`; o handle do
  `LOAD_SPECIAL_MODEL` só é usado se veio diferente de zero; o render object
  tem referência própria do CLEO+ (o modelo especial é contado), então não há
  ponteiro solto nem `REMOVE_SPECIAL_MODEL` em cima de render object vivo; e
  se a criação falhar, a caixa do jogo assume na mesma chamada.
- **Lista de músicas**: o mesmo bloco de memória guarda o número de cada
  `som<N>.mp3` que existe (4 bytes por faixa, `CFG_MEM_TRACKS`), e a contagem
  fica numa var do CLEO (`1011`) com o relógio da varredura (`1010`) — as duas
  são zeradas na inicialização, porque o bloco é outro a cada reinício do
  script e uma contagem antiga apontaria para memória nova. O sorteio lê uma
  posição dessa lista: nenhum teste de arquivo na hora de tocar, e nenhuma
  faixa fica de fora por azar do sorteio. A pasta é revarrida na primeira
  música, quando um arquivo não abre (o `bbg_play_track` zera a contagem) e
  depois de `CFG_TRACK_SCAN_MS`.
- O loop principal tem `WAIT 0`, e o script volta ao estado inicial se o
  player morrer, se o char sumir do pool, se o NPC morrer ou se for
  dispensado.
- **Save**: como todo ped criado por script, o NPC pode acabar indo parar no
  seu savegame (sem a caixa, que é um objeto de render e não é salvo). Se
  isso incomodar, dispense ele com `BOOBOXD` antes de salvar.

---

## 📜 Histórico de versões

- **v6.7** — até 999 músicas:
  - **Teto de 50 → 999 faixas** (`CFG_MAX_TRACKS`): `som1.mp3` … `som999.mp3`.
    Continua valendo qualquer quantidade entre 1 e o teto, com numeração
    esburacada ou não.
  - **Sorteio por lista, não por teste de arquivo.** O script varre a pasta
    uma vez e guarda o número de cada faixa que existe no seu bloco de
    memória (4 bytes por faixa); na hora de tocar ele sorteia uma posição
    dessa lista. Era o que faltava para o teto subir: sortear um número de
    1..999 e testar se o arquivo existe quase nunca cai numa música de quem
    tem poucas faixas, e o plano B (a primeira que existir) acabava tocando
    sempre a mesma. Agora o sorteio é uniforme e a troca de faixa não testa
    arquivo nenhum.
  - A pasta é **revarrida** na primeira música, quando uma faixa some (o
    arquivo não abre) e a cada `CFG_TRACK_SCAN_MS` (10 s) — colocar/tirar
    músicas com o jogo aberto continua valendo, e o soco de "próxima faixa"
    não paga uma varredura inteira.
  - Novas vars do CLEO: `1011` (quantas faixas a varredura achou) e `1010`
    (quando ela aconteceu). As duas são zeradas na inicialização, porque a
    lista em si mora no bloco de memória, que é outro a cada reinício do
    script.
- **v6.6** — variações de NPC e caixa sem ID:
  - **Lista de peds**: `[Ped] model=wmybu,male01,fam1,bfori` — a cada
    `BOOBOX` o script sorteia uma das skins (sem repetir a anterior). Itens
    inválidos são ignorados; nomes e IDs podem se misturar; até 16.
  - **Caixa por arquivo (`LOAD_SPECIAL_MODEL`)**: o `model=` por nome só
    acha o que está registrado num `.ide` (o `0E9C` consulta essa tabela).
    Para um objeto **sem ID**, `[Caixa] model=radio` (+ `txd=` opcional)
    carrega `CLEO/BoomboxGuy/radio.dff` + `radio.txd` direto do disco com o
    `LOAD_SPECIAL_MODEL` do CLEO+ e prende na mão do NPC
    (`CREATE_RENDER_OBJECT_TO_CHAR_BONE_FROM_SPECIAL`). Cache do modelo
    carregado, arquivos conferidos antes e caixa padrão como reserva.
  - O parser do `.ini` agora conhece as **seções** (`[Caixa]`), o buffer de
    linha subiu de 64 para 128 bytes e o `BBGUYTUNE` regrava a lista de peds
    e a caixa por arquivo como o jogador escreveu.
- **v6.5** — ele nunca mais fica plantado:
  - **"Saiu do interior e não segue mais"**: o resgate de quem ficava para
    trás olhava só o estado do motor (`GET_CHAR_MOVE_STATE`). Isso só pega
    quem está **sem** tarefa nenhuma; quem ficou com a tarefa de correr presa
    em algum canto (porta, escada, canto de calçada, ponto que a malha de
    navegação não cobre) já estava "correndo" para o motor e ficava parado
    para sempre — sem nem o teleporte de resgate acontecer. Agora a conta usa
    a **velocidade de verdade** (`0x6AC`), zero nos dois casos, e o tempo de
    tolerância caiu de 6 s para 3,5 s.
  - **Três remédios em ordem, do mais discreto para o mais visível**:
    reenvio da tarefa a cada 1 s (como já era); **0,7 s** sem sair do lugar ⇒
    ele passa a ir **em linha reta** até você, ignorando a malha de navegação
    (mesma tarefa usada dentro de interior) — sai do encrave sem nada
    aparecer na tela; **3,5 s** sem sair do lugar ⇒ aparece de novo perto de
    você. A rota normal volta sozinha quando ele chega perto.
  - **A virada para o player agora só vale de perto** (até 20 m): de longe a
    prioridade dele é andar, e a virada troca a tarefa primária do NPC.
- **v6.4** — convivência:
  - **Ele parava dentro do CJ / te prendia**: o destino da caminhada é a sua
    posição, então ele andava até encostar em você e ficava te empurrando
    (dava para ficar preso em canto de parede). Agora, quando chega a 2,5 m
    e ainda está andando, a tarefa é cancelada: ele para ao seu lado. Faixa
    morta de meio metro entre "para" (2,5 m) e "volta a andar" (3 m) para
    não tremer.
  - **Pulinho no fim do trajeto (interior)**: era o mesmo destino — o jogo
    "fechava" o último trecho da caminhada no ponto final, e o ponto final
    era você. Com ele parando antes, o último trecho não existe mais. De
    quebra, dentro de interior o destino passa a usar **a altura dele** (o
    piso do ambiente) em vez da sua, e o reposicionamento por troca de área
    virou no-op quando ele já está a menos de 1,5 m do ponto (mover um passo
    parecia teleporte).
  - **Sempre virado para você** (rua e interior): parado, ele recebe
    `TASK_TURN_CHAR_TO_FACE_CHAR` a cada 1,5 s e acompanha você com o corpo;
    andando, nada muda (o corpo já aponta para onde ele vai).
  - **Soco troca a música** — sem tecla nenhuma: um soco de mãos livres
    (só do player, a pé, ao alcance) sorteia outra faixa na hora e devolve a
    vida dele a 100. Tiro, arma branca, atropelamento e soco de outro NPC
    não contam. Detecção: vida caindo desde o quadro anterior +
    `0x470` = mãos livres + `0x51A` = o player foi quem bateu.
- **v6.3** — interiores:
  - **Área errada (aparecia na rua / preso / morte por queda)**: o GTA
    divide o mundo em "áreas" — `0` = rua, `1..18` = interiores — e só
    desenha, além de só manter carregada a colisão, das entidades que estão
    na mesma área do player. O NPC vivia na área `0`, então dentro de um
    interior ele aparecia na rua (do lado de fora, para quem estava na
    sala), atravessava o piso por falta de colisão e morria de queda quando
    o ambiente descarregava. Agora o script lê a área do player (`09E8`) e a
    área mostrada pelo jogo (`077E`) e mantém o NPC sempre na mesma
    (`0860`) — o mesmo truque que os spawners de carro usam com `0840`.
  - **Aparecer logo à frente do CJ**: quando a área muda (você entrou, saiu
    ou trocou de sala), o NPC vai **na hora** para **1,5 m à frente do CJ**,
    no mesmo piso, sem passar pelo teste de solo — que não acha o piso de
    dentro de um interior. Vale também para o cheat chamado com você lá
    dentro: ele nasce (e reaparece) na sua frente, e não mais na rua.
  - **Andar dentro de casa**: lá dentro ele passa a usar
    `TASK_GO_STRAIGHT_TO_COORD` (linha reta). A malha de navegação de
    pedestres não cobre interiores, e o `TASK_FOLLOW_PATH_NODES` deixava ele
    parado esperando um caminho que não existe.
  - **Resgate sem queda**: antes de reposicionar, a velocidade dele é zerada
    (`083C`) para não continuar caindo depois do resgate; e dentro de
    interior, se ele ficar mais de 3 m abaixo do player (atravessando o
    piso), é puxado de volta na hora, antes de virar dano de queda.
  - **Nem entrar, nem sair do carro no meio da animação**: as ordens de
    "entra no carro" e "sai do carro" agora esperam a animação de embarque
    ou desembarque terminar (e o resgate de interior também), porque limpar
    a tarefa no meio dela era o que deixava o NPC de pé atravessando a
    lataria.
- **v6.2** — dois acertos no embarque:
  - **Moto (e carro de dois lugares)**: a procura da cadeira de carona
    começava na `1` — que é lugar de trás de carro de quatro portas. Em
    veículo de dois lugares a única carona é a `0` (garupa), então ele
    simplesmente não achava lugar e ficava a pé. Agora a procura começa na
    `0`, e se o teste por cadeira falhar (acontece em moto/quadriciclo) os
    contadores do jogo (`0x1E9`/`0x1EA`) servem de desempate: se ainda cabe
    passageiro, ele tenta a garupa.
  - **Sentado de pé dentro do carro**: a cada 2 segundos o script repetia
    `CLEAR_CHAR_TASKS` + "entrar no carro" enquanto ele ainda estava
    **entrando**. Limpar a tarefa no meio da animação de entrar deixa o NPC
    fisicamente dentro do veículo, mas de pé, atravessando a lataria (e a
    repetição impedia a animação de terminar). Agora, enquanto
    `IS_CHAR_ENTERING_ANY_CAR` (CLEO+ `0xE49`) for verdadeiro, o script não
    encosta nas tarefas dele — nem para embarcar, nem quando você sai do
    carro no meio do embarque.
- **v6.1** — correção de crash: o handle do arquivo `.ini` estava numa
  variável que o parser das linhas usava como rascunho. Ao ler a primeira
  linha `model=`, o handle virava outro número (o tipo do modelo, `7`) e a
  leitura seguinte caía num ponteiro inválido. Agora o handle mora numa var
  do CLEO (`CFG_VAR_INI`) e é conferido antes de cada linha. Também saiu o
  `SCRIPT_NAME` do script (opcional no CLEO — o nome que aparece nos logs
  vem do nome do arquivo `.cs`).
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

## 🙏 Créditos

- Mod original: **90s Boombox** por *Guidopdu*
  ([MixMods](https://www.mixmods.com.br/2016/02/90s-boombox-andar-ouvindo-radio/))
  — 10 faixas, som direcional e a ideia de "andar ouvindo rádio".
- CLEO+ por *Junior_Djjr* (MIT) — inclusive as definições de opcodes em
  `tools/cleo-plus.xml`.
- Este remake: escrito em gta3script/CLEO+ para GTA San Andreas.
