# Quarkus Reactive — API de Produtos

API REST reativa de CRUD de produtos, construída com [Quarkus](https://quarkus.io/), Hibernate Reactive com Panache e o cliente reativo do PostgreSQL. Toda a stack (HTTP + acesso a dados) é não bloqueante, usando `Uni`/`Mutiny` de ponta a ponta.

## Stack

- **Java 25**
- **Quarkus 3.39.3**
- **RESTEasy Reactive** (`quarkus-rest` + `quarkus-rest-jackson`) para os endpoints HTTP
- **Hibernate Reactive com Panache** (`quarkus-hibernate-reactive-panache`) para persistência
- **Reactive PostgreSQL Client** (`quarkus-reactive-pg-client`) para acesso não bloqueante ao banco
- **Maven** como build tool (via wrapper `mvnw`)

## Pré-requisitos

- JDK 25+
- Docker
- PostgreSQL disponível (local, em container, ou via Dev Services do Quarkus em modo dev)
- Não é necessário ter o Maven instalado — use o wrapper `./mvnw`

## Executando em modo de desenvolvimento

Você pode rodar a aplicação em modo dev, com live coding:

```
./mvnw quarkus:dev
```
> **NOTA:** o Quarkus disponibiliza uma Dev UI, acessível apenas em modo dev, em <http://localhost:8080/q/dev/>.

Em modo dev, se nenhum banco estiver configurado, o Quarkus sobe automaticamente um container PostgreSQL via **Dev Services**.

## Configuração

As configurações do datasource ficam em `src/main/resources/application.properties`:

```
quarkus.datasource.db-kind = postgresql

%prod.quarkus.datasource.username = hibernate
%prod.quarkus.datasource.password = hibernate
%prod.quarkus.datasource.reactive.url = vertx-reactive:postgresql://localhost/products

%prod.quarkus.hibernate-orm.schema-management.strategy = create
```

Ajuste usuário, senha e URL de acordo com o seu ambiente de produção. Em `import.sql` há dados de exemplo (tabela `Product` com alguns produtos) usados para popular o banco.

> **NOTA:** se o script `import.sql` inserir produtos com `id` fixo, ajuste a sequence do Postgres após os inserts (ex.: `SELECT setval('products_seq', (SELECT MAX(id) FROM Product));`) para evitar conflito de chave primária na primeira inserção feita pela aplicação.

## Endpoints

Recurso base: `/api/v1/products`

| Método | Caminho          | Descrição                     | Corpo da requisição               | Resposta                            |
| ------ | ---------------- | ----------------------------- | --------------------------------- | ----------------------------------- |
| GET    | `/products`      | Lista produtos de forma paginada | —                               | `200 OK` + página de produtos       |
| GET    | `/products/{id}` | Busca um produto pelo id      | —                                 | `200 OK` ou `404 Not Found`         |
| POST   | `/products`      | Cria um novo produto          | `{ "name": "...", "price": 0.0 }` | `201 Created`                       |
| PUT    | `/products/{id}` | Atualiza um produto existente | `{ "name": "...", "price": 0.0 }` | `200 OK` ou `404 Not Found`         |
| DELETE | `/products/{id}` | Remove um produto             | —                                 | `204 No Content` ou `404 Not Found` |

### Paginação em `GET /products`

A listagem de produtos é paginada e ordenada por `name`. A página é `1`-based (a primeira página é `page=1`) e é validada para não aceitar valores menores que `1`.

**Query params:**

| Parâmetro | Tipo | Padrão | Descrição                                  |
| --------- | ---- | ------ | ------------------------------------------- |
| `page`    | int  | `1`    | Número da página, começando em `1`         |
| `size`    | int  | `20`   | Quantidade de itens por página (máx. `100`) |

**Resposta (`PagedResponse<ProductResponse>`):**

```json
{
  "content": [
    { "id": 1, "name": "Macbook Pro", "price": 23000.00 }
  ],
  "totalElements": 3,
  "totalPages": 1,
  "pageIndex": 1,
  "pageSize": 20
}
```

Exemplos com `curl`:

```
# Listar produtos (página 1, tamanho padrão)
curl http://localhost:8080/api/v1/products

# Listar produtos com paginação explícita
curl "http://localhost:8080/api/v1/products?page=2&size=10"

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

Requisições para um produto inexistente retornam `404 Not Found`, tratado pelo `NotFoundExceptionMapper`. Requisições com `page` menor que `1` retornam `400 Bad Request`.

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
├── entity/     # Entidades Panache (ProductEntity)
├── resource/   # Endpoints REST (ProductResource)
│   ├── request/   # DTOs de entrada (ProductRequest)
│   └── response/  # DTOs de saída (ProductResponse, PagedResponse)
├── service/    # Regras de negócio (ProductService)
└── exception/  # Mapeadores de exceção (NotFoundExceptionMapper)
```

## Guias relacionados

- [RESTEasy Classic's REST Client Mutiny support](https://quarkus.io/guides/resteasy-client): habilita Mutiny para o REST client
- [Reactive PostgreSQL client](https://quarkus.io/guides/reactive-sql-clients): conecta ao PostgreSQL usando o padrão reativo
- [Hibernate Reactive com Panache](https://quarkus.io/guides/hibernate-reactive-panache): simplifica a persistência reativa

## Saiba mais sobre o Quarkus

Para saber mais sobre o Quarkus, visite: <https://quarkus.io/>.