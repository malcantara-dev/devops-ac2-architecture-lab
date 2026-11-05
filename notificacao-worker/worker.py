"""notificacao-worker (Desafio 4).

Consome a fila notificacoes.curso-concluido do RabbitMQ e "notifica" o aluno (log).
Tambem expoe POST /notificar na porta 8001, usado so no modo sync do mini-lab.
"""
import json
import os
import threading
from http.server import BaseHTTPRequestHandler, HTTPServer

FILA = os.getenv("FILA", "notificacoes.curso-concluido")
RABBIT = os.getenv("RABBITMQ_HOST", "localhost")


def formatar(evento: dict) -> str:
    if evento.get("aproveitamento"):
        return (f"Parabens! Voce concluiu '{evento['cursoTitulo']}' com nota {evento['nota']} "
                f"e ganhou 3 creditos (usuario {evento['usuarioId']}).")
    return (f"Voce concluiu '{evento['cursoTitulo']}' com nota {evento['nota']}. "
            f"Com nota 7 ou mais voce ganha creditos (usuario {evento['usuarioId']}).")


def notificar(corpo: bytes) -> str:
    mensagem = formatar(json.loads(corpo))
    print("[notificacao]", mensagem, flush=True)
    return mensagem


class Http(BaseHTTPRequestHandler):
    def do_POST(self):
        if self.path != "/notificar":
            self.send_response(404)
            self.end_headers()
            return
        tamanho = int(self.headers.get("Content-Length", 0))
        notificar(self.rfile.read(tamanho))
        self.send_response(202)
        self.end_headers()

    def log_message(self, *args):
        pass


def consumir():
    import pika
    conexao = pika.BlockingConnection(pika.ConnectionParameters(host=RABBIT))
    canal = conexao.channel()
    canal.basic_qos(prefetch_count=1)

    def ao_receber(ch, metodo, _props, corpo):
        try:
            notificar(corpo)
            ch.basic_ack(delivery_tag=metodo.delivery_tag)
        except (ValueError, KeyError):
            # mensagem invalida vai para a DLQ em vez de travar a fila
            ch.basic_nack(delivery_tag=metodo.delivery_tag, requeue=False)

    canal.basic_consume(queue=FILA, on_message_callback=ao_receber)
    print(f"[worker] consumindo {FILA} em {RABBIT}", flush=True)
    canal.start_consuming()


if __name__ == "__main__":
    threading.Thread(target=lambda: HTTPServer(("0.0.0.0", 8001), Http).serve_forever(), daemon=True).start()
    consumir()
