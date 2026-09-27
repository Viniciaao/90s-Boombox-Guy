# 90s Boombox Guy - Makefile
# make        -> compila BoomboxGuy.cs
# make clean  -> remove o compilado

GTA3SC ?= gta3sc
FLAGS  := --config=gtasa --guesser --cs -fcleo -fno-entity-tracking

all: BoomboxGuy.cs

BoomboxGuy.cs: BoomboxGuy.sc
	$(GTA3SC) $(FLAGS) -o $@ $<

clean:
	rm -f BoomboxGuy.cs

.PHONY: all clean
