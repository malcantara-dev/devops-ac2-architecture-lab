"""Normalizacao simples de texto usada pela recomendacao e pelo assistente."""
import re
import unicodedata

PALAVRAS_VAZIAS = {
    "a", "o", "as", "os", "de", "da", "do", "das", "dos", "e", "em", "na", "no", "para", "por",
    "com", "um", "uma", "que", "como", "qual", "quais", "eu", "meu", "minha", "se", "ao", "e",
}


def tokens(texto: str) -> set[str]:
    sem_acento = unicodedata.normalize("NFKD", texto).encode("ascii", "ignore").decode()
    palavras = re.findall(r"[a-z0-9]+", sem_acento.lower())
    return {p for p in palavras if p not in PALAVRAS_VAZIAS and len(p) > 1}


def similaridade(a: set[str], b: set[str]) -> float:
    """Indice de Jaccard entre dois conjuntos de palavras."""
    if not a or not b:
        return 0.0
    return len(a & b) / len(a | b)
