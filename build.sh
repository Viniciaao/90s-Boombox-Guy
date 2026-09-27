#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# 90s Boombox Guy - build do script CLEO
#
# Uso:
#   ./build.sh                        (procura o gta3sc no PATH ou em $GTA3SC)
#   GTA3SC=/caminho/gta3sc ./build.sh
#
# Saida: BoomboxGuy.cs  (copie para a pasta CLEO/ do GTA San Andreas)
# ---------------------------------------------------------------------------
set -euo pipefail

cd "$(dirname "$0")"
ROOT="$PWD"

SRC="BoomboxGuy.sc"
OUT="BoomboxGuy.cs"
# definicoes de opcodes do CLEO+ usadas pelo script (vai junto no repositorio)
CLEOPLUS_XML="$ROOT/tools/cleo-plus.xml"

# 1) $GTA3SC  2) PATH  3) caminhos comuns no sandbox de desenvolvimento
GTA3SC="${GTA3SC:-}"
if [ -z "$GTA3SC" ]; then
    if command -v gta3sc >/dev/null 2>&1; then
        GTA3SC="$(command -v gta3sc)"
    elif [ -x "$HOME/work/gta3sc/gta3sc" ]; then
        GTA3SC="$HOME/work/gta3sc/gta3sc"
    else
        echo "erro: gta3sc nao encontrado." >&2
        echo "      baixe em https://github.com/thelink2012/gta3sc" >&2
        echo "      ou defina GTA3SC=/caminho/para/gta3sc" >&2
        exit 1
    fi
fi

if [ ! -f "$CLEOPLUS_XML" ]; then
    echo "erro: nao achei $CLEOPLUS_XML" >&2
    exit 1
fi

echo ">> compilando $SRC com $GTA3SC"
"$GTA3SC" --config=gtasa --guesser --cs -fcleo -fno-entity-tracking \
    --add-config="$CLEOPLUS_XML" \
    -o "$ROOT/$OUT" "$ROOT/$SRC"

echo ">> ok: $OUT ($(wc -c < "$ROOT/$OUT") bytes)"
echo "   copie $ROOT/$OUT para  <GTA>/CLEO/"
