# IoT (Desafio 5)

| Arquivo | Papel |
|---|---|
| `simulador.py` | Dispositivos dos laboratórios publicando eventos no Mosquitto (QoS 1, `eventId` único) |
| `gateway.py` | Assina os tópicos com sessão persistente, valida e encaminha ao Core (`POST /api/eventos-iot`) |

```bash
pip install -r requirements.txt
python gateway.py --broker localhost --core http://localhost:8080
python simulador.py --broker localhost --eventos 10 --duplicar
pytest test_gateway.py
```

Tópico: `labs/{laboratorio}/dispositivos/{dispositivo}/eventos`.
