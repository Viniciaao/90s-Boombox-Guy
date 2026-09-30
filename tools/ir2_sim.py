#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Simulador do IR2 que o gta3sc emite para o BoomboxGuy.sc.

Nao e uma reimplementacao do sorteio: este programa EXECUTA as rotinas que o
compilador gerou (bbg_scan_tracks, bbg_pick_track, bbg_build_path e
bbg_alloc_path - achadas no IR2 pelo conteudo, porque o numero do rotulo
MAIN_n muda a cada edicao do fonte), interpretando o IR2 linha por linha.
Do jogo ele so imita o que essas rotinas tocam:

  STRING_FORMAT / DOES_FILE_EXIST  -> pasta de musicas falsa
  WRITE_MEMORY / READ_MEMORY       -> memoria do bloco (com conferencia de limites)
  ALLOCATE_MEMORY                  -> ponteiro novo
  GET_GAME_TIMER                   -> relogio controlado pelo teste
  GENERATE_RANDOM_INT_IN_RANGE     -> sorteio (semente fixa)
  SET/GET_CLEO_SHARED_VAR          -> vars do CLEO

Uso:  python3 tools/ir2_sim.py /caminho/do/ir2
"""
import random
import re
import sys

MEM_SIZE = 1 << 20
TOKEN = re.compile(r'"(?:[^"\\]|\\.)*"|%[A-Za-z_][A-Za-z0-9_]*|-?\d+@|-?\d+i\d+|0x[0-9a-fA-F.+-]+p[+-]?\d+f|-?\d+(?:\.\d+)?|[A-Za-z_][A-Za-z0-9_]*')


class Crash(Exception):
    pass


class Machine(object):
    def __init__(self, lines, files, seed=1):
        self.code = {}          # label -> indice da linha
        self.lines = []
        label = None
        for ln in lines:
            ln = ln.strip()
            if not ln:
                continue
            if ln.endswith(':'):
                label = ln[:-1]
                self.code[label] = len(self.lines)
                continue
            if label is None:
                label = 'MAIN'
                self.code['MAIN'] = len(self.lines)
            self.lines.append(ln)
        self.lvar = [0] * 32
        self.cleo = {}
        self.mem = bytearray(MEM_SIZE)
        self.heap = 0x10000
        self.files = set(files)      # ex.: "CLEO\\BoomboxGuy\\som7.mp3"
        self.timer = 0
        self.rnd = random.Random(seed)
        self.stats = {'exists': 0, 'alloc': 0}
        self.block = None            # (inicio, fim) do bloco alocado
        self.trace = []

    # ------------------------- operandos -------------------------
    def val(self, tok):
        if tok.startswith('"'):
            return tok[1:-1].replace('\\\\', '\\')
        if tok.startswith('%'):
            return tok[1:]
        if re.match(r'^-?\d+i\d+$', tok):
            return int(tok[:tok.index('i')])
        if tok.endswith('f'):
            return float.fromhex(tok[:-1]) if 'p' in tok else float(tok[:-1])
        if tok.endswith('@'):
            return self.lvar[int(tok[:-1])]
        try:
            return int(tok)
        except ValueError:
            return float(tok)

    def setvar(self, tok, v):
        if not tok.endswith('@'):
            raise Crash('destino nao e LVAR: %r' % tok)
        self.lvar[int(tok[:-1])] = int(v) & 0xFFFFFFFF
        if self.lvar[int(tok[:-1])] >= 0x80000000:
            self.lvar[int(tok[:-1])] -= 0x100000000

    # ------------------------- memoria -------------------------
    def check(self, addr, size):
        if self.block is None or not (self.block[0] <= addr and addr + size <= self.block[1]):
            raise Crash('memoria fora do bloco: addr=0x%X size=%d bloco=%s'
                        % (addr, size, self.block))

    def rstr(self, addr):
        end = self.mem.index(0, addr)
        return self.mem[addr:end].decode('latin-1')

    # ------------------------- execucao -------------------------
    def run(self, entry, limit=400000):
        pc = self.code[entry]
        stack = []
        cond = True
        pending = 0
        negate = False
        steps = 0
        while True:
            steps += 1
            if steps > limit:
                raise Crash('laco sem fim em %s' % entry)
            line = self.lines[pc]
            self.trace.append(line)
            negate = False
            if line.startswith('NOT '):
                negate = True
                line = line[4:]
            parts = TOKEN.findall(line)
            op = parts[0]
            args = parts[1:]

            if op == 'ANDOR':
                pending = self.val(args[0]) + 1
                cond = None
                pc += 1
                continue

            if op.startswith('IS_') and ('EQUAL' in op or 'GREATER' in op):
                a, b = self.val(args[0]), self.val(args[1])
                # ordem importa: 'EQUAL_TO' tambem esta dentro de
                # 'GREATER_OR_EQUAL_TO'
                if 'GREATER_OR_EQUAL' in op:
                    r = a >= b
                elif 'GREATER_THAN' in op:
                    r = a > b
                elif 'NOT_EQUAL' in op:
                    r = a != b
                else:
                    r = a == b
            elif op == 'SET_LVAR_INT':
                self.setvar(args[0], self.val(args[1])); r = None
            elif op == 'SET_LVAR_INT_TO_LVAR_INT':
                self.setvar(args[0], self.val(args[1])); r = None
            elif op == 'ADD_VAL_TO_INT_LVAR':
                self.setvar(args[0], self.val(args[0]) + self.val(args[1])); r = None
            elif op == 'ADD_INT_LVAR_TO_INT_LVAR':
                self.setvar(args[0], self.val(args[0]) + self.val(args[1])); r = None
            elif op == 'SUB_INT_LVAR_FROM_INT_LVAR':
                self.setvar(args[0], self.val(args[0]) - self.val(args[1])); r = None
            elif op == 'MULT_INT_LVAR_BY_VAL':
                self.setvar(args[0], self.val(args[0]) * self.val(args[1])); r = None
            elif op == 'SET_CLEO_SHARED_VAR':
                self.cleo[self.val(args[0])] = self.val(args[1]); r = None
            elif op == 'GET_CLEO_SHARED_VAR':
                self.setvar(args[1], self.cleo.get(self.val(args[0]), 0)); r = None
            elif op == 'GET_GAME_TIMER':
                self.setvar(args[0], self.timer); r = None
            elif op == 'ALLOCATE_MEMORY':
                size = self.val(args[0])
                self.heap = (self.heap + 15) & ~15
                self.setvar(args[1], self.heap)
                self.block = (self.heap, self.heap + size)
                self.heap += size
                self.stats['alloc'] += 1
                r = None
            elif op == 'STRING_FORMAT':
                dest, fmt = self.val(args[0]), self.val(args[1])
                vals = [self.val(a) for a in args[2:]]
                out = re.sub(r'%(\d*d|s|g)', lambda m: str(vals.pop(0)), fmt.replace('%%', '\x00'))
                self.check(dest, len(out) + 1)
                self.mem[dest:dest + len(out) + 1] = out.encode('latin-1') + b'\x00'
                r = None
            elif op == 'DOES_FILE_EXIST':
                path = self.rstr(self.val(args[0]))
                self.stats['exists'] += 1
                r = path in self.files
            elif op == 'WRITE_MEMORY':
                addr, size, value = self.val(args[0]), self.val(args[1]), self.val(args[2])
                self.check(addr, size)
                for i in range(size):
                    self.mem[addr + i] = (value >> (8 * i)) & 0xFF
                r = None
            elif op == 'READ_MEMORY':
                addr, size = self.val(args[0]), self.val(args[1])
                self.check(addr, size)
                v = 0
                for i in range(size):
                    v |= self.mem[addr + i] << (8 * i)
                self.setvar(args[3], v); r = None
            elif op == 'GENERATE_RANDOM_INT_IN_RANGE':
                lo, hi = self.val(args[0]), self.val(args[1])
                self.setvar(args[2], self.rnd.randrange(lo, hi)); r = None
            elif op == 'GOTO_IF_FALSE':
                ok = cond
                if negate:
                    ok = not ok
                pc = self.code[self.val(args[0])] if not ok else pc + 1
                cond = True
                pending = 0
                continue
            elif op == 'GOTO':
                pc = self.code[self.val(args[0])]
                continue
            elif op == 'GOSUB':
                stack.append(pc + 1)
                pc = self.code[self.val(args[0])]
                continue
            elif op == 'RETURN':
                if not stack:
                    return
                pc = stack.pop()
                continue
            else:
                raise Crash('opcode nao suportado pelo simulador: %s' % line)

            if r is not None:
                if negate:
                    r = not r
                cond = r if cond is None or pending == 0 else (cond and r if pending > 0 else cond)
                if pending > 0:
                    pending -= 1
            pc += 1


def rotinas(m):
    """Acha as rotinas pelo conteudo (o numero MAIN_n muda a cada edicao).

    Uma rotina tem varios rotulos internos (os IF/WHILE viram blocos), entao o
    corpo dela vai ate o comeco da PROXIMA rotina - e rotina e o que aparece
    como destino de algum GOSUB.
    """
    nomes = {}
    entradas = set()
    for ln in m.lines:
        if ln.startswith('GOSUB '):
            entradas.add(ln.split('%')[1].strip())
    ordem = sorted(((l, p) for l, p in m.code.items() if l in entradas), key=lambda kv: kv[1])
    for i, (label, inicio) in enumerate(ordem):
        fim = ordem[i + 1][1] if i + 1 < len(ordem) else len(m.lines)
        corpo = '\n'.join(m.lines[inicio:fim])
        if 'ALLOCATE_MEMORY 4540' in corpo:
            nomes['alloc'] = label
        if 'som%d.mp3' in corpo:
            nomes['build_path'] = label
        if 'DOES_FILE_EXIST' in corpo and 'SET_CLEO_SHARED_VAR 1011' in corpo:
            nomes['scan'] = label
        if 'GENERATE_RANDOM_INT_IN_RANGE' in corpo:
            nomes['pick'] = label
    faltam = {'alloc', 'build_path', 'scan', 'pick'} - set(nomes)
    if faltam:
        raise Crash('nao achei no IR2: %s' % ', '.join(sorted(faltam)))
    return nomes


def lista(m):
    """Le a lista de faixas que a varredura gravou no bloco."""
    n = m.cleo.get(1011, 0)
    base = m.block[0] + 544
    out = []
    for i in range(n):
        v = 0
        for j in range(4):
            v |= m.mem[base + i * 4 + j] << (8 * j)
        out.append(v)
    return out


def pasta(*numeros):
    if len(numeros) == 1 and not isinstance(numeros[0], int):
        numeros = numeros[0]          # pasta(range(1, 1000))
    return set('CLEO\\BoomboxGuy\\som%d.mp3' % n for n in numeros)


R = {}


def nova_maquina(files, seed=1, timer=0):
    m = Machine(open(sys.argv[1], encoding='utf-8').read().splitlines(), files, seed)
    m.timer = timer
    R.update(rotinas(m))
    m.run(R['alloc'])           # bbg_alloc_path: reserva o bloco
    return m


def sorteios(m, n, avanca_ms=0):
    """Chama bbg_pick_track n vezes e devolve a sequencia de lastTrack (12@)."""
    seq = []
    for _ in range(n):
        m.timer += avanca_ms
        m.run(R['pick'])
        seq.append(m.lvar[12])
    return seq


def main():
    falhas = []

    def ok(nome, cond, extra=''):
        print('  [%s] %s%s' % ('ok' if cond else 'FALHOU', nome, extra))
        if not cond:
            falhas.append(nome)

    # ---- 1) 3 musicas num teto de 999: sorteio uniforme, sem repetir -------
    print('1) 3 musicas (som1..som3), teto 999')
    m = nova_maquina(pasta(1, 2, 3))
    m.run(R['scan'])                      # primeira varredura
    ok('varredura achou 3', m.cleo[1011] == 3, ' -> %s' % lista(m))
    ok('lista = [1, 2, 3]', lista(m) == [1, 2, 3])
    varridas = m.stats['exists']
    seq = sorteios(m, 6000)
    ok('todas as faixas tocam', set(seq) == {1, 2, 3}, ' -> %s' % sorted(set(seq)))
    ok('nunca repete a faixa anterior', all(seq[i] != seq[i + 1] for i in range(len(seq) - 1)))
    freq = [seq.count(i) / float(len(seq)) for i in (1, 2, 3)]
    ok('sorteio uniforme (0,33 cada)', all(0.29 < f < 0.38 for f in freq),
       ' -> %s' % ['%.3f' % f for f in freq])
    ok('nao revarre a pasta a cada faixa (%d testes p/ 6000 faixas)' % m.stats['exists'],
       m.stats['exists'] == varridas, ' -> %d' % m.stats['exists'])

    # ---- 2) numeracao esburacada -----------------------------------------
    print('2) numeracao esburacada (som1, som4, som999)')
    m = nova_maquina(pasta(1, 4, 999))
    m.run(R['scan'])
    ok('achou as 3', lista(m) == [1, 4, 999], ' -> %s' % lista(m))
    seq = sorteios(m, 3000)
    ok('toca as tres, sem repetir', set(seq) == {1, 4, 999}
       and all(seq[i] != seq[i + 1] for i in range(len(seq) - 1)))

    # ---- 3) uma musica so -------------------------------------------------
    print('3) uma musica so (som7)')
    m = nova_maquina(pasta(7))
    m.run(R['scan'])
    seq = sorteios(m, 200)
    ok('repete a unica que existe', set(seq) == {7})

    # ---- 4) pasta vazia ---------------------------------------------------
    print('4) pasta sem musica nenhuma')
    m = nova_maquina(pasta())
    m.run(R['scan'])
    ok('contagem 0', m.cleo[1011] == 0)
    m.lvar[12] = 5
    m.stats['exists'] = 0
    m.run(R['pick'])
    ok('lastTrack = 0 (nenhuma faixa encontrada)', m.lvar[12] == 0)
    ok('varreu os 999 numeros', m.stats['exists'] == 999, ' -> %d' % m.stats['exists'])

    # ---- 5) pasta cheia: 999 faixas --------------------------------------
    print('5) 999 musicas (teto)')
    m = nova_maquina(pasta(range(1, 1000)))
    m.run(R['scan'])
    ok('achou 999', m.cleo[1011] == 999)
    seq = sorteios(m, 20000)
    ok('sorteia em toda a lista', min(seq) == 1 and max(seq) == 999)
    ok('nunca repete a anterior', all(seq[i] != seq[i + 1] for i in range(len(seq) - 1)))
    top = max(seq.count(i) for i in range(1, 1000)) / 20000.0
    ok('sem faixa privilegiada (esperado 1/999 = 0,001)', top < 0.004, ' -> max %.4f' % top)

    # ---- 6) colocar musica com o jogo aberto -----------------------------
    print('6) musica nova com o jogo aberto')
    m = nova_maquina(pasta(1, 2, 3))
    m.run(R['scan'])
    antes = m.stats['exists']
    m.files.add('CLEO\\BoomboxGuy\\som500.mp3')
    sorteios(m, 50)                            # 50 faixas seguidas, sem esperar
    ok('nao revarre antes dos 10 s', m.stats['exists'] == antes)
    seq = sorteios(m, 200, avanca_ms=11000)    # agora o relogio passa dos 10 s
    ok('revarre depois de CFG_TRACK_SCAN_MS', m.stats['exists'] > antes)
    ok('a faixa nova entra no sorteio', 500 in seq, ' -> %s' % sorted(set(seq)))

    # ---- 7) musica apagada: o 0 de bbg_play_track ------------------------
    print('7) lista invalidada (como faz bbg_play_track quando o arquivo nao abre)')
    m = nova_maquina(pasta(1, 2, 3))
    m.run(R['scan'])
    m.files.discard('CLEO\\BoomboxGuy\\som2.mp3')
    m.cleo[1011] = 0                           # <- o que o play_track grava
    m.run(R['pick'])
    ok('varre de novo na hora', m.stats['exists'] >= 999)
    seq = sorteios(m, 500)
    ok('a faixa apagada some do sorteio', 2 not in seq and set(seq) == {1, 3})

    # ---- 8) bloco de memoria: nada le/ escreve fora dele ------------------
    print('8) limites do bloco (%d bytes)' % (m.block[1] - m.block[0]))
    ok('tamanho = 544 + 4*999 = 4540', m.block[1] - m.block[0] == 4540)

    print('')
    if falhas:
        print('FALHOU: %s' % ', '.join(falhas))
        return 1
    print('tudo certo: o bytecode gerado pelo gta3sc se comportou como esperado')
    return 0


if __name__ == '__main__':
    sys.exit(main())
