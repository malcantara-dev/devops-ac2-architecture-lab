"""Gateway MQTT -> Core (Desafio 5).

Assina os eventos dos laboratorios com sessao persistente (clean_start=False e client_id fixo):
se o gateway cair, o broker guarda as mensagens QoS 1 e entrega quando ele voltar. Cada evento
valido vira um POST /api/eventos-iot no Core; o Core descarta duplicatas pelo eventId.

Uso: python gateway.py --broker localhost --core http://localhost:8080
"""
import argparse
import json
import time
import urllib.error
import urllib.request

import paho.mqtt.client as mqtt

CAMPOS = ("eventId", "dispositivoId", "laboratorio", "tipo", "usuarioId", "ocorridoEm")
TIPOS = {"PRESENCA", "INICIO_ATIVIDADE", "CONCLUSAO_ATIVIDADE"}


def validar(evento: dict) -> list[str]:
    erros = [f"campo ausente: {c}" for c in CAMPOS if c not in evento]
    if evento.get("tipo") not in TIPOS:
        erros.append(f"tipo invalido: {evento.get('tipo')}")
    return erros


def enviar_ao_core(core: str, evento: dict, tentativas: int = 5) -> str:
    corpo = json.dumps(evento).encode()
    for tentativa in range(1, tentativas + 1):
        try:
            pedido = urllib.request.Request(f"{core}/api/eventos-iot", data=corpo,
                                            headers={"Content-Type": "application/json"}, method="POST")
            with urllib.request.urlopen(pedido, timeout=3) as resposta:
                return json.loads(resposta.read())["status"]
        except (urllib.error.URLError, TimeoutError) as falha:
            espera = min(2 ** tentativa, 30)
            print(f"[gateway] Core indisponivel ({falha}); nova tentativa em {espera}s", flush=True)
            time.sleep(espera)
    raise RuntimeError("Core indisponivel apos varias tentativas")


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--broker", default="localhost")
    p.add_argument("--core", default="http://localhost:8080")
    a = p.parse_args()

    def ao_receber(_cliente, _dados, mensagem):
        try:
            evento = json.loads(mensagem.payload)
        except ValueError:
            print("[gateway] descartado: JSON invalido", flush=True)
            return
        erros = validar(evento)
        if erros:
            print(f"[gateway] descartado: {erros}", flush=True)
            return
        status = enviar_ao_core(a.core, evento)
        print(f"[gateway] {evento['eventId']} -> {status}", flush=True)

    cliente = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id="gateway-core",
                          protocol=mqtt.MQTTv5)
    cliente.on_message = ao_receber
    cliente.connect(a.broker, 1883, clean_start=False,
                    properties=_sessao_persistente())
    cliente.subscribe("labs/+/dispositivos/+/eventos", qos=1)
    print("[gateway] assinando labs/+/dispositivos/+/eventos (QoS 1, sessao persistente)", flush=True)
    cliente.loop_forever()


def _sessao_persistente():
    from paho.mqtt.packettypes import PacketTypes
    from paho.mqtt.properties import Properties
    props = Properties(PacketTypes.CONNECT)
    props.SessionExpiryInterval = 24 * 3600
    return props


if __name__ == "__main__":
    main()
