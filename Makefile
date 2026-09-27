# 90s Boombox Guy - Makefile
#   make        -> compila BoomboxGuy.cs
#   make clean  -> remove o compilado
#
# Precisa do gta3sc: https://github.com/thelink2012/gta3sc
# Se ele nao estiver no PATH:  make GTA3SC=/caminho/para/gta3sc

GTA3SC ?= gta3sc
FLAGS  := --config=gtasa --guesser --cs -fcleo -fno-entity-tracking \
          --add-config=$(CURDIR)/tools/cleo-plus.xml

all: BoomboxGuy.cs

BoomboxGuy.cs: BoomboxGuy.sc tools/cleo-plus.xml
	$(GTA3SC) $(FLAGS) -o $@ $<

clean:
	rm -f BoomboxGuy.cs

.PHONY: all clean
