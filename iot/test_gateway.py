from gateway import validar


def evento(**extra):
    base = {"eventId": "e1", "dispositivoId": "catraca-1", "laboratorio": "LAB-1",
            "tipo": "PRESENCA", "usuarioId": 1, "ocorridoEm": "2026-11-04T20:00:00+00:00"}
    base.update(extra)
    return base


def test_evento_valido():
    assert validar(evento()) == []


def test_tipo_invalido():
    assert validar(evento(tipo="ABRIR_PORTA")) == ["tipo invalido: ABRIR_PORTA"]


def test_campo_ausente():
    e = evento()
    del e["eventId"]
    assert "campo ausente: eventId" in validar(e)
