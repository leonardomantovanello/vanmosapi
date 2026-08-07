# VanMos API

API backend do **VanMos**, sistema de gestão de transporte escolar desenvolvido como projeto de TCC.

## Tecnologias
Java, Spring Boot, WebSocket/STOMP (chat e localização em tempo real) e Docker.

## Funcionalidades
- Autenticação de usuários
- Chat em tempo real entre motorista e responsáveis
- Compartilhamento de localização em tempo real
- CORS configurado para o app mobile (Expo)

## Como rodar com Docker
```bash
docker build -t vanmosapi .
docker run -p 8080:8080 vanmosapi
```

## Projetos relacionados
- [VanMos](https://github.com/leonardomantovanello/VanMos) — frontend web
- [vanmosapp-novo](https://github.com/leonardomantovanello/vanmosapp-novo) — app mobile (Expo/React Native)
