from fastapi.testclient import TestClient

from app.main import app

cliente = TestClient(app)


def test_health():
    assert cliente.get("/health").json() == {"status": "UP"}


def test_recomenda_cursos_parecidos_com_os_concluidos():
    resposta = cliente.post("/recomendacoes", json={
        "usuarioId": 1,
        "concluidos": ["Spring Boot Fundamentos"],
        "catalogo": ["Docker na Pratica", "Spring Boot Avancado", "Spring Boot Fundamentos", "Kubernetes"],
    }).json()

    cursos = [r["curso"] for r in resposta["recomendacoes"]]
    assert cursos[0] == "Spring Boot Avancado"
    assert "Spring Boot Fundamentos" not in cursos
    assert len(cursos) == 3


def test_sem_historico_mantem_a_ordem_do_catalogo():
    resposta = cliente.post("/recomendacoes", json={
        "usuarioId": 2, "concluidos": [], "catalogo": ["A1", "B2", "C3", "D4"], "limite": 2,
    }).json()

    assert [r["curso"] for r in resposta["recomendacoes"]] == ["A1", "B2"]


def test_assistente_responde_com_a_regra_do_premium_e_cita_a_fonte():
    resposta = cliente.post("/assistente/perguntar",
                            json={"pergunta": "Quando meu plano vira Premium e ganho moedas?"}).json()

    assert "Premium" in resposta["resposta"]
    assert "Plano Premium (cenarios 7 e 8)" in resposta["fontes"]


def test_assistente_admite_quando_nao_sabe():
    resposta = cliente.post("/assistente/perguntar", json={"pergunta": "xyzzy"}).json()

    assert resposta["fontes"] == []
    assert resposta["resposta"].startswith("Nao encontrei")
