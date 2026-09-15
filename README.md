# Quarkus Reactive — API de Produtos e Vendas

API REST reativa construída com [Quarkus](https://quarkus.io/), Hibernate Reactive com Panache, cliente reativo do PostgreSQL e Reactive Messaging com Kafka. Toda a stack (HTTP + acesso a dados + mensageria) é não bloqueante, usando `Uni`/`Multi` (Mutiny) de ponta a ponta.

## Stack

- **Java 25**
- **Quarkus 3.39.3**
- **RESTEasy Reactive** (`quarkus-rest` + `quarkus-rest-jackson`) para os endpoints HTTP
- **Hibernate Reactive com Panache** (`quarkus-hibernate-reactive-panache`) para persistência
- **Reactive PostgreSQL Client** (`quarkus-reactive-pg-client`) para acesso não bloqueante ao banco
- **Hibernate Validator** (`quarkus-hibernate-validator`) para validação de requests (Bean Validation)
- **SmallRye Reactive Messaging + Kafka** (`quarkus-messaging-kafka`) para o fluxo assíncrono de vendas
- **Maven** como build tool (via wrapper `mvnw`)

## Pré-requisitos

- JDK 25+
- PostgreSQL disponível (local, em container, ou via Dev Services do Quarkus em modo dev)
- Kafka disponível (local, em container, ou via Dev Services do Quarkus em modo dev)
- Não é necessário ter o Maven instalado — use o wrapper `./mvnw`

## Executando em modo de desenvolvimento

Você pode rodar a aplicação em modo dev, com live coding:

```
./mvnw quarkus:dev
```

> **NOTA:** o Quarkus disponibiliza uma Dev UI, acessível apenas em modo dev, em <http://localhost:8080/q/dev/>.

Em modo dev, se nenhum banco ou broker Kafka estiver configurado, o Quarkus sobe automaticamente containers de PostgreSQL e Kafka via **Dev Services**.

## Configuração

As configurações do datasource e da mensageria ficam em `src/main/resources/application.properties`:

```properties
quarkus.datasource.db-kind = postgresql

%prod.quarkus.datasource.username = hibernate
%prod.quarkus.datasource.password = hibernate
%prod.quarkus.datasource.reactive.url = vertx-reactive:postgresql://localhost/products

%prod.quarkus.hibernate-orm.schema-management.strategy =

mp.messaging.incoming.sales.topic=sale-requests
mp.messaging.incoming.sales.auto.offset.reset=earliest

quarkus.kafka.devservices.topic-partitions.sale-requests=3
```

Ajuste usuário, senha e URL de acordo com o seu ambiente de produção. Em `import.sql` há dados de exemplo (tabela `Product` com alguns produtos) usados para popular o banco.

## Endpoints

### Produtos

Recurso base: `/api/v1/products`

| Método | Caminho                  | Descrição                     | Corpo da requisição               | Resposta                                                |
| ------ | ------------------------ | ------------------------------ | ---------------------------------- | -------------------------------------------------------- |
| GET    | `/api/v1/products`       | Lista produtos paginados       | —                                  | `200 OK` + `PaginationResponse<ProductResponse>`          |
| GET    | `/api/v1/products/{id}`  | Busca um produto pelo id       | —                                  | `200 OK` ou `404 Not Found`                               |
| POST   | `/api/v1/products`       | Cria um novo produto           | `{ "name": "...", "price": 0.0 }`  | `201 Created` ou `400 Bad Request` (validação)            |
| PUT    | `/api/v1/products/{id}`  | Atualiza um produto existente  | `{ "name": "...", "price": 0.0 }`  | `200 OK`, `400 Bad Request` (validação) ou `404 Not Found` |
| DELETE | `/api/v1/products/{id}`  | Remove um produto              | —                                  | `204 No Content` ou `404 Not Found`                       |

**Paginação** (`GET /api/v1/products`): aceita os query params `page` (padrão `1`) e `size` (padrão `20`), ambos validados como `>= 1`. A resposta traz `content`, `totalElements`, `totalPages`, `pageIndex` e `pageSize`.

**Validação** (`POST`/`PUT`): `name` não pode estar em branco e `price` deve ser não nulo e positivo. Violações retornam `400 Bad Request` com o detalhe do erro.

Exemplos com `curl`:

```bash
# Listar produtos (paginado)
curl "http://localhost:8080/api/v1/products?page=1&size=20"

# Buscar produto por id
curl http://localhost:8080/api/v1/products/1

# Criar produto
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Teclado mecânico", "price": 350.00}'

# Atualizar produto
curl -X PUT http://localhost:8080/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Teclado mecânico RGB", "price": 399.90}'

# Remover produto
curl -X DELETE http://localhost:8080/api/v1/products/1
```

Requisições para um produto inexistente retornam `404 Not Found`, tratado pelo `NotFoundExceptionMapper`.

### Vendas (assíncrono via Kafka)

Recurso base: `/sales`

| Método | Caminho  | Descrição                                              | Corpo da requisição                    | Resposta                                    |
| ------ | -------- | ------------------------------------------------------- | ---------------------------------------- | --------------------------------------------- |
| POST   | `/sales` | Publica uma solicitação de venda no tópico Kafka        | `{ "idProduct": 1, "quantity": 2 }`      | `201 Created` ou `500 Internal Server Error`  |

O fluxo funciona assim:

1. `SaleResource` recebe o `POST /sales`, empacota o `SaleRequest` e o envia ao canal `sale-requests` (`Emitter<SaleRequest>`). Se o envio falhar, a falha é convertida em `InternalServerErrorException` e tratada pelo `SaleExceptionMapper`, retornando um `ErrorResponse` com `500`.
2. `SalesProcessor` consome as mensagens do tópico Kafka `sale-requests` (canal `sales`), busca o produto pelo id (`ProductEntity`) e calcula o total da venda. Se o produto não existir, lança `NotFoundException`. O resultado é publicado no canal `sales-received` como um `Sale` (nome do produto, quantidade e total).
3. `SalePanelResource` expõe um **Server-Sent Events (SSE)** em `GET /sales-panel`, transmitindo em tempo real cada `Sale` processada — útil para um painel/dashboard de vendas ao vivo.

Exemplos com `curl`:

```bash
# Solicitar uma venda (publica no tópico Kafka)
curl -X POST http://localhost:8080/sales \
  -H "Content-Type: application/json" \
  -d '{"idProduct": 1, "quantity": 2}'

# Acompanhar as vendas processadas em tempo real (SSE)
curl -N http://localhost:8080/sales-panel
```

## Empacotando e executando a aplicação

A aplicação pode ser empacotada com:

```
./mvnw package
```

Isso gera o arquivo `quarkus-run.jar` no diretório `target/quarkus-app/`. Note que não é um *über-jar*: as dependências são copiadas para `target/quarkus-app/lib/`.

A aplicação pode então ser executada com:

```
java -jar target/quarkus-app/quarkus-run.jar
```

Se você quiser construir um *über-jar*, execute:

```
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

A aplicação, empacotada como *über-jar*, pode ser executada com `java -jar target/*-runner.jar`.

## Criando um executável nativo

Você pode criar um executável nativo com:

```
./mvnw package -Dnative
```

Ou, caso não tenha o GraalVM instalado, pode rodar o build nativo em um container:

```
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

Depois, execute o binário nativo com:

```
./target/quarkus-reactive-1.0.0-SNAPSHOT-runner
```

Para saber mais sobre executáveis nativos, consulte <https://quarkus.io/guides/maven-tooling>.

## Estrutura do projeto

```
src/main/java/br/com/pedrosa/
├── entity/               # Entidades Panache (ProductEntity)
├── model/                # Modelos de domínio publicados via Reactive Messaging (Sale)
├── processor/            # Processadores de mensageria (SalesProcessor)
├── resource/
│   ├── request/          # DTOs de entrada (ProductRequest, SaleRequest)
│   ├── response/         # DTOs de saída (ProductResponse, PaginationResponse, ErrorResponse)
│   ├── ProductResource.java     # Endpoints REST de produtos (CRUD + paginação)
│   ├── SaleResource.java        # Endpoint REST para solicitar vendas (publica no Kafka)
│   └── SalePanelResource.java   # Endpoint SSE com o stream de vendas processadas
├── service/              # Regras de negócio (ProductService)
└── exception/            # Mapeadores de exceção (NotFoundExceptionMapper, SaleExceptionMapper)
```

## Guias relacionados

- [RESTEasy Classic's REST Client Mutiny support](https://quarkus.io/guides/resteasy-client): habilita Mutiny para o REST client
- [Reactive PostgreSQL client](https://quarkus.io/guides/reactive-sql-clients): conecta ao PostgreSQL usando o padrão reativo
- [Hibernate Reactive com Panache](https://quarkus.io/guides/hibernate-reactive-panache): simplifica a persistência reativa
- [Hibernate Validator](https://quarkus.io/guides/validation): validação de beans (Bean Validation) para os DTOs de entrada
- [SmallRye Reactive Messaging - Kafka](https://quarkus.io/guides/kafka-reactive-getting-started): mensageria assíncrona e reativa com Kafka
- [Server-Sent Events (SSE)](https://quarkus.io/guides/resteasy-reactive#reactive-routes): streaming de eventos usado pelo painel de vendas

## Saiba mais sobre o Quarkus

Para saber mais sobre o Quarkus, visite: <https://quarkus.io/>.