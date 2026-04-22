# 🐇 Projeto Coelho

> Microsserviço produtor de mensagens com **Spring Boot** + **RabbitMQ**

Em vez de processar pedidos, preços e estoques de forma síncrona, o projeto publica mensagens em filas específicas do RabbitMQ. Consumidores independentes leem essas mensagens e as processam em seu próprio ritmo — sem bloquear quem publicou.

---

## 📋 Sumário

- [Contexto de Negócio](#-contexto-de-negócio)
- [Tecnologias](#-tecnologias)
- [Arquitetura](#-arquitetura)
- [Estrutura de Pacotes](#-estrutura-de-pacotes)
- [Configuração](#-configuração)
- [Endpoints REST](#-endpoints-rest)
- [Mensageria RabbitMQ](#-mensageria-rabbitmq)
- [Frontend](#-frontend)
- [Como Executar](#-como-executar)
- [Glossário](#-glossário)

---

## 💼 Contexto de Negócio

Uma empresa de e-commerce precisa tornar o processamento de pedidos mais escalável e resiliente. Com mensageria assíncrona, quando um cliente finaliza uma compra, cada etapa é desacoplada:

| Evento | Fila Destino |
|--------|-------------|
| Registrar pedido | `PEDIDO` |
| Baixar itens do estoque | `ESTOQUE` |
| Ajustar promoções de preço | `PRECO` |

O sistema não trava enquanto aguarda cada serviço terminar — cada um processa no seu ritmo.

---

## 🛠 Tecnologias

| Tecnologia | Versão | Papel no Projeto |
|---|---|---|
| Java | 17+ (LTS) | Linguagem de programação principal |
| Spring Boot | 3.x | Framework do microsserviço REST |
| Spring AMQP | 3.x | Integração com RabbitMQ |
| RabbitMQ | 3.x | Broker de mensagens |
| Docker | Qualquer | Execução do RabbitMQ em container |
| Maven | 3.x | Build e dependências |
| HTML / CSS / JS | Vanilla | Interface web de testes |

---

## 🏗 Arquitetura

```
┌──────────────────────────────────────────────────────────┐
│          CAMADA DE APRESENTAÇÃO (Frontend)               │
│                  index.html + fetch()                    │
└────────────────────────┬─────────────────────────────────┘
                         │  HTTP POST/PUT (JSON)
                         ▼
┌──────────────────────────────────────────────────────────┐
│           CAMADA DE CONTROLE (Controllers)               │
│   PedidoControler │ EstoqueControler │ PrecoControler    │
└────────────────────────┬─────────────────────────────────┘
                         │  Chama RabbitmqService
                         ▼
┌──────────────────────────────────────────────────────────┐
│           CAMADA DE SERVIÇO (RabbitmqService)            │
│        rabbitTemplate.convertAndSend(fila, obj)          │
└────────────────────────┬─────────────────────────────────┘
                         │  AMQP (protocolo)
                         ▼
┌──────────────────────────────────────────────────────────┐
│                    RABBITMQ BROKER                       │
│  Exchange: amq.direct                                    │
│  ┌────────┐  ┌────────┐  ┌────────┐                     │
│  │ESTOQUE │  │ PRECO  │  │ PEDIDO │  ← Filas            │
│  └────────┘  └────────┘  └────────┘                     │
└──────────────────────────────────────────────────────────┘
```

**Fluxo de dados:**
1. Usuário preenche o formulário HTML e clica em Enviar
2. O navegador envia uma requisição HTTP ao backend (porta `8087`)
3. O Controller desserializa o JSON no DTO correspondente
4. O Controller chama `RabbitmqService.enviarMensagem(nomeFila, objeto)`
5. O `RabbitTemplate` serializa e publica no exchange `amq.direct`
6. O RabbitMQ roteia para a fila correta via **routing key** (= nome da fila)
7. A mensagem aguarda na fila até um consumidor lê-la

---

## 📦 Estrutura de Pacotes

```
com.example.coelho/
├── CoelhoApplication.java
├── constantes/
│   └── RabbitMQConstantes.java
├── conections/
│   └── RabbitMQConection.java
├── service/
│   └── RabbitmqService.java
├── DTO/
│   ├── PedidoDTO.java
│   ├── EstoqueDTO.java
│   └── PrecoDTO.java
└── controler/
    ├── PedidoControler.java
    ├── EstoqueControler.java
    └── PrecoControler.java
```

| Pacote | Responsabilidade |
|--------|-----------------|
| `constantes/` | Centraliza os nomes das filas como constantes estáticas (evita *magic strings*) |
| `conections/` | Declara filas, exchange e bindings no RabbitMQ ao iniciar a aplicação |
| `service/` | Encapsula a publicação de mensagens, isolando o `RabbitTemplate` dos controllers |
| `DTO/` | Mapeia o JSON da requisição HTTP para objetos Java |
| `controler/` | Recebe requisições HTTP e delega ao serviço de mensageria |

---

## ⚙️ Configuração

### application.properties

```properties
spring.application.name=coelho
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=insira_seu_login_aqui
spring.rabbitmq.password=insira_sua_senha_aqui
server.port=8087
```

| Propriedade | Padrão | Descrição |
|---|---|---|
| `spring.rabbitmq.host` | `localhost` | Endereço do broker |
| `spring.rabbitmq.port` | `5672` | Porta AMQP padrão |
| `spring.rabbitmq.username` | `insira_seu_login_aqui` | Usuário de desenvolvimento |
| `spring.rabbitmq.password` | `insira_sua_senha_aqui` | Senha de desenvolvimento |
| `server.port` | `8087` | Porta da API REST |

### Subindo o RabbitMQ com Docker

```bash
docker run -d --name rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  rabbitmq:3-management
```

Painel de administração disponível em **http://localhost:15672** (seu_login / sua_senha).

---

## 🌐 Endpoints REST

| Método | Rota | Body | Resposta | Fila |
|--------|------|------|----------|------|
| `POST` | `/pedido` | `PedidoDTO` | `200` + JSON `{status, mensagem}` | `PEDIDO` |
| `PUT` | `/estoque` | `EstoqueDTO` | `200` sem body | `ESTOQUE` |
| `PUT` | `/preco` | `PrecoDTO` | `200` sem body | `PRECO` |

### POST `/pedido`

```bash
curl -X POST http://localhost:8087/pedido \
  -H "Content-Type: application/json" \
  -d '{"id":"PED-001","cliente":"Maria Silva","valor":149.90}'
```

**Resposta:**
```json
{
  "status": "sucesso",
  "mensagem": "Pedido PED-001 enviado para a fila com sucesso!"
}
```

### PUT `/estoque`

```bash
curl -X PUT http://localhost:8087/estoque \
  -H "Content-Type: application/json" \
  -d '{"codigoproduto":"PROD-42","quantidade":100}'
```

### PUT `/preco`

```bash
curl -X PUT http://localhost:8087/preco \
  -H "Content-Type: application/json" \
  -d '{"codigoproduto":"PROD-42","preco":"29.99"}'
```

---

## 🐰 Mensageria RabbitMQ

| Conceito | Valor no Projeto | O que é |
|---|---|---|
| Exchange | `amq.direct` | Ponto de entrada das mensagens; decide para qual fila rotear |
| Tipo de troca | Direct | Roteia pela routing key **exata** (sem wildcard) |
| Routing Key | `= nome da fila` | Chave que combina mensagem com fila destino |
| Filas | `ESTOQUE`, `PRECO`, `PEDIDO` | `durable=true` — sobrevivem ao reinício do broker |
| Binding | fila ← exchange via routing key | Regra que liga a fila ao exchange |

**Como o roteamento funciona:**

```
Mensagem com routing key = "PEDIDO"
         │
         ▼
   [amq.direct exchange]
         │
         ├── binding "ESTOQUE" → ✗ não corresponde
         ├── binding "PRECO"   → ✗ não corresponde
         └── binding "PEDIDO"  → ✓ entrega na fila PEDIDO
```

---

## 🖥️ Frontend

Página HTML estática (`index.html`) para testar o envio de pedidos via `fetch()`.

| Campo | Tipo | Mapeamento |
|-------|------|-----------|
| `id` | `text` | `PedidoDTO.id` |
| `cliente` | `text` | `PedidoDTO.cliente` |
| `valor` | `number` | `PedidoDTO.valor` |

> ⚠️ **CORS:** Para que o navegador acesse a API em `localhost:8087`, adicione `@CrossOrigin` nos controllers ou configure um `WebMvcConfigurer` global no Spring Boot.

---

## 🚀 Como Executar

### Pré-requisitos

| Ferramenta | Versão Mínima |
|---|---|
| Java JDK | 17 |
| Maven | 3.8 |
| Docker | Qualquer |

### Passo a Passo

**1. Suba o RabbitMQ:**
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

**2. Clone o repositório:**
```bash
git clone https://github.com/vexedray/estudosDocker.git
cd coelho
```

**3. Execute a aplicação:**
```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

**4. Verifique o funcionamento:**
- Painel RabbitMQ → http://localhost:15672 (as filas `ESTOQUE`, `PRECO` e `PEDIDO` devem aparecer)
- Abra o `index.html` no navegador e envie um pedido
- Em **Queues → PEDIDO** no painel, a mensagem estará lá

---
