# Jenkins do laboratório

```bash
docker compose -f infra/jenkins/docker-compose.jenkins.yml up -d --build
```

Acesse http://localhost:8081 (admin / admin). Os jobs `core` e `ia-service` já nascem criados pelo
`casc.yaml`, cada um apontando para o seu `Jenkinsfile`.
