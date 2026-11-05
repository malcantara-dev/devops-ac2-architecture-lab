#!/usr/bin/env bash
# Gera o mapa de dependencias entre pacotes do Core e a lista de onde estao as classes
# de cada capacidade. Usado como evidencia de antes/depois nos Desafios 1 e 2.
#
# Uso: ./scripts/dependencias.sh <pasta-de-saida> <prefixo>
#   ex.: ./scripts/dependencias.sh docs/evidencias/desafio-1 antes
set -euo pipefail

SAIDA="${1:?informe a pasta de saida}"
PREFIXO="${2:?informe o prefixo (antes ou depois)}"
BASE="org.example.grupo_7_praticaatdd"

mkdir -p "$SAIDA"
./mvnw -B -q compile

{
  echo "# Dependencias entre pacotes do Core ($PREFIXO)"
  echo "# Gerado por scripts/dependencias.sh com jdeps"
  echo "# Leitura: origem -> destino (prefixo $BASE omitido)"
  echo
  jdeps -verbose:package -e "org\.example\..*" target/classes \
    | grep -- "->" | grep -v "target" \
    | sed -E "s/$BASE\.?//g; s/[[:space:]]+classes$//; s/^[[:space:]]+//; s/[[:space:]]+->[[:space:]]+/ -> /" \
    | awk '{ if ($1 == "") $1 = "(raiz)"; print }' \
    | sort -u
} > "$SAIDA/$PREFIXO-pacotes.txt"

{
  echo "# Estrutura do Core ($PREFIXO)"
  echo
  (cd src/main/java/org/example/grupo_7_praticaatdd && find . -name '*.java' | sed 's|^\./||' | sort)
  echo
  for capacidade in ${CAPACIDADES:-Matricula Ranking}; do
    pastas=$(cd src/main/java/org/example/grupo_7_praticaatdd && find . -name "*${capacidade}*.java" | sed 's|^\./||; s|/[^/]*$||' | sort -u)
    total=$(printf '%s\n' "$pastas" | grep -c . || true)
    echo "## $capacidade: classes espalhadas em $total pasta(s)"
    printf '%s\n' "$pastas" | grep . | sed 's/^/- /' || echo "- (nenhuma classe)"
    echo
  done
} > "$SAIDA/$PREFIXO-estrutura.txt"

echo "Gerado: $SAIDA/$PREFIXO-pacotes.txt e $SAIDA/$PREFIXO-estrutura.txt"
