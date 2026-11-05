# Reconexão e duplicidade de eventos IoT (Desafio 5)

Hipótese: com MQTT (QoS 1 + sessão persistente) e idempotência no Core, um gateway que cai não
perde eventos e um evento entregue duas vezes é contado uma vez só.

## 1. Idempotência no Core (`./mvnw test -Dtest=EventoIotApiTest`)

```
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in org.example.grupo_7_praticaatdd.api.EventoIotApiTest
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Entrega | Resposta do Core |
|---|---|
| `eventId` novo | 201 `registrado` |
| Mesmo `eventId` de novo (reentrega do QoS 1) | 200 `duplicado`, nada gravado |
| Evento sem `eventId` | 400 |

A restrição `unique` em `eventos_iot.event_id` também resolve duas entregas simultâneas.

## 2. Validação no gateway (`cd iot && pytest test_gateway.py -v`)

```
test_gateway.py::test_evento_valido PASSED
test_gateway.py::test_tipo_invalido PASSED
test_gateway.py::test_campo_ausente PASSED
============================== 3 passed in 0.25s ==============================
```

## 3. Reconexão sem perda

O gateway conecta com `client_id` fixo (`gateway-core`), `clean_start=False` e sessão de 24 h.
Enquanto ele está fora, o Mosquitto guarda as mensagens QoS 1 da assinatura e as entrega quando
ele volta. Se o Core estiver fora, o gateway tenta de novo com espera crescente (2, 4, 8... até 30 s).

Roteiro com os containers:

```bash
docker compose up -d --build mosquitto app
pip install -r iot/requirements.txt
python iot/gateway.py --core http://localhost:8080          # terminal 1
# Ctrl+C no gateway (fora do ar)
python iot/simulador.py --eventos 10 --duplicar              # terminal 2: 20 publicações
python iot/gateway.py --core http://localhost:8080          # volta e recebe as 20 mensagens
# saída esperada: 10 x "registrado" e 10 x "duplicado"
```
