from worker import formatar


def test_mensagem_com_aproveitamento():
    msg = formatar({"cursoTitulo": "Docker", "nota": 8.0, "usuarioId": 1, "aproveitamento": True})
    assert "ganhou 3 creditos" in msg


def test_mensagem_sem_aproveitamento():
    msg = formatar({"cursoTitulo": "Docker", "nota": 5.0, "usuarioId": 1, "aproveitamento": False})
    assert "Com nota 7 ou mais" in msg
