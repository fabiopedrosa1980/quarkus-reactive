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
- PostgreSQL disponível (local, em container, ou via Dev Services do Quarkus em modo dev)
- Não é necessário ter o Maven instalado — use o wrapper `./mvnw`

## Executando em modo de desenvolvimento

Você pode rodar a aplicação em modo dev, com live coding:

```shell script
./mvnw quarkus:dev
```

> **NOTA:** o Quarkus disponibiliza uma Dev UI, acessível apenas em modo dev, em <http://localhost:8080/q/dev/>.

Em modo dev, se nenhum banco estiver configurado, o Quarkus sobe automaticamente um container PostgreSQL via **Dev Services**.

## Configuração

As configurações do datasource ficam em `src/main/resources/application.properties`:

```properties
quarkus.datasource.db-kind = postgresql

%prod.quarkus.datasource.username = hibernate
%prod.quarkus.datasource.password = hibernate
%prod.quarkus.datasource.reactive.url = vertx-reactive:postgresql://localhost/products

%prod.quarkus.hibernate-orm.schema-management.strategy = create
```

Ajuste usuário, senha e URL de acordo com o seu ambiente de produção. Em `import.sql` há dados de exemplo (tabela `Product` com alguns produtos) usados para popular o banco.

## Endpoints

Recurso base: `/products`

| Método | Caminho          | Descrição                          | Corpo da requisição                | Resposta                     |
|--------|------------------|-------------------------------------|-------------------------------------|-------------------------------|
| GET    | `api/v1/products`     | Lista todos os produtos             | —                                    | `200 OK` + lista de produtos  |
| GET    | `api/v1/products/{id}` | Busca um produto pelo id            | —                                    | `200 OK` ou `404 Not Found`   |
| POST   | `api/v1/products`     | Cria um novo produto                | `{ "name": "...", "price": 0.0 }`   | `201 Created`                 |
| PUT    | `api/v1/products/{id}` | Atualiza um produto existente       | `{ "name": "...", "price": 0.0 }`   | `200 OK` ou `404 Not Found`   |
| DELETE | `api/v1/products/{id}` | Remove um produto                   | —                                    | `204 No Content` ou `404 Not Found` |

Exemplos com `curl`:

```shell script
# Listar produtos
curl http://localhost:8080/api/v1/products

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

## Empacotando e executando a aplicação

A aplicação pode ser empacotada com:

```shell script
./mvnw package
```

Isso gera o arquivo `quarkus-run.jar` no diretório `target/quarkus-app/`. Note que não é um _über-jar_: as dependências são copiadas para `target/quarkus-app/lib/`.

A aplicação pode então ser executada com:

```shell script
java -jar target/quarkus-app/quarkus-run.jar
```

Se você quiser construir um _über-jar_, execute:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

A aplicação, empacotada como _über-jar_, pode ser executada com `java -jar target/*-runner.jar`.

## Criando um executável nativo

Você pode criar um executável nativo com:

```shell script
./mvnw package -Dnative
```

Ou, caso não tenha o GraalVM instalado, pode rodar o build nativo em um container:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

Depois, execute o binário nativo com:

```shell script
./target/quarkus-reactive-1.0.0-SNAPSHOT-runner
```

Para saber mais sobre executáveis nativos, consulte <https://quarkus.io/guides/maven-tooling>.

## Estrutura do projeto

```
src/main/java/br/com/pedrosa/
├── entity/     # Entidades Panache (ProductEntity)
├── request/    # DTOs de entrada (ProductRequest)
├── response/   # DTOs de saída (ProductResponse)
├── resource/   # Endpoints REST (ProductResource)
├── service/    # Regras de negócio (ProductService)
└── exception/  # Mapeadores de exceção (NotFoundExceptionMapper)
```

## Guias relacionados

- [RESTEasy Classic's REST Client Mutiny support](https://quarkus.io/guides/resteasy-client): habilita Mutiny para o REST client
- [Reactive PostgreSQL client](https://quarkus.io/guides/reactive-sql-clients): conecta ao PostgreSQL usando o padrão reativo
- [Hibernate Reactive com Panache](https://quarkus.io/guides/hibernate-reactive-panache): simplifica a persistência reativa

## Saiba mais sobre o Quarkus

Para saber mais sobre o Quarkus, visite: <https://quarkus.io/>.