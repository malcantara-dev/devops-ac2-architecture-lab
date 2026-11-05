"""Simulador de dispositivos dos laboratorios (Desafio 5).

Publica eventos de presenca, inicio e conclusao de atividade no broker MQTT, com QoS 1 e um
eventId unico por evento. Com --duplicar, reenvia cada evento (como um dispositivo que nao
recebeu o PUBACK e tenta de novo), para provar a idempotencia do Core.

Uso: python simulador.py --broker localhost --eventos 10 [--duplicar]
"""
import argparse
import json
import random
import time
import uuid
from datetime import datetime, timezone

import paho.mqtt.client as mqtt

TIPOS = ["PRESENCA", "INICIO_ATIVIDADE", "CONCLUSAO_ATIVIDADE"]


def novo_evento(laboratorio: str, dispositivo: str, usuario_id: int) -> dict:
    return {
        "eventId": str(uuid.uuid4()),
        "dispositivoId": dispositivo,
        "laboratorio": laboratorio,
        "tipo": random.choice(TIPOS),
        "usuarioId": usuario_id,
        "ocorridoEm": datetime.now(timezone.utc).isoformat(timespec="seconds"),
    }


def topico(evento: dict) -> str:
    return f"labs/{evento['laboratorio']}/dispositivos/{evento['dispositivoId']}/eventos"


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--broker", default="localhost")
    p.add_argument("--eventos", type=int, default=10)
    p.add_argument("--duplicar", action="store_true")
    a = p.parse_args()

    cliente = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id="simulador-labs")
    cliente.connect(a.broker, 1883)
    cliente.loop_start()
    for i in range(a.eventos):
        evento = novo_evento("LAB-1", f"catraca-{i % 3 + 1}", usuario_id=1 + i % 2)
        vezes = 2 if a.duplicar else 1
        for _ in range(vezes):
            cliente.publish(topico(evento), json.dumps(evento), qos=1).wait_for_publish()
        print(f"[simulador] {evento['tipo']:<20} {evento['eventId']} x{vezes}", flush=True)
        time.sleep(0.2)
    cliente.loop_stop()
    cliente.disconnect()


if __name__ == "__main__":
    main()
