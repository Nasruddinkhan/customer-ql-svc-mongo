# 🧩 Customer GraphQL Service (`customer-ql-svc`)

A **Spring Boot Reactive GraphQL** microservice for managing customer data using **MongoDB** and **Project Reactor (Flux/Mono)**.  
Includes support for **real-time subscriptions** via WebSocket and **query caching** for optimized GraphQL performance.

---

## 🚀 Features

- **Reactive GraphQL API** using Spring Boot 3 and GraphQL Java.
- **MongoDB (Reactive)** repository with `ReactiveMongoRepository`.
- **Reactive Streams (Flux/Mono)** for non-blocking I/O.
- **GraphQL Subscriptions** for real-time updates using WebSocket.
- **In-memory GraphQL Query Cache** (`PreparsedDocumentProvider`).
- **Event-driven architecture** using Reactor `Sinks.Many` for emitting events.
- **Auto reloadable schema** (`classpath:schema/`).

---

## 🧱 Project Structure

```
customer-ql-svc/
├── src/main/java/com/alinma/customer/qlsvc/
│   ├── config/
│   │   └── GraphQlCachingConfig.java
│   ├── controller/
│   │   └── CustomerGraphQLController.java
│   ├── repository/
│   │   └── CustomerRepository.java
│   ├── service/
│   │   ├── EventService.java
│   │   └── impl/CustomerServiceImpl.java
│   ├── exception/
│   │   ├── CustomerNotFoundException.java
│   │   └── GraphQLExceptionResolver.java
│   ├── model/
│   │   ├── documents/Customer.java
│   │   ├── dto/CustomerDto.java
│   │   └── dto/CustomerEvent.java
│   └── mapper/CustomerMapper.java
│
├── src/main/resources/
│   ├── schema/customer.graphqls
│   └── application.yml
│
└── pom.xml
```

---

## ⚙️ Configuration

```yaml
server:
  port: 8082

spring:
  application:
    name: customer-ql-svc

  data:
    mongodb:
      uri: mongodb://admin:admin@localhost:27017/customerdb?authSource=admin

  graphql:
    schema:
      locations: classpath:schema/
    graphiql:
      enabled: true
      path: /graphiql
    path: /graphql
    websocket:
      path: /graphql
```

---

## ⚡ Exception Handling

### 📁 `exception/CustomerNotFoundException.java`
```java
package com.alinma.customer.qlsvc.exception;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String msg) {
        super(msg);
    }
}
```

### ⚙️ `exception/GraphQLExceptionResolver.java`
```java
package com.alinma.customer.qlsvc.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Component
public class GraphQLExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {

        if (ex instanceof CustomerNotFoundException) {
            return GraphqlErrorBuilder.newError(env)
                    .message(ex.getMessage())
                    .errorType(graphql.ErrorType.DataFetchingException)
                    .build();
        }

        if (ex instanceof ResponseStatusException rse) {
            return GraphqlErrorBuilder.newError(env)
                    .message(rse.getReason())
                    .errorType(graphql.ErrorType.ValidationError)
                    .build();
        }

        return GraphqlErrorBuilder.newError(env)
                .message("Internal server error: " + Optional.ofNullable(ex.getMessage()).orElse("Unexpected error"))
                .errorType(graphql.ErrorType.ExecutionAborted)
                .build();
    }
}
```

### 🧩 Example Usage

```java
@Override
public Mono<CustomerDto> getById(String id) {
    return repository.findById(id)
            .switchIfEmpty(Mono.error(new CustomerNotFoundException("Customer not found with id: " + id)))
            .map(customerMapper::toDto);
}
```

**GraphQL Query:**
```graphql
query {
  customerById(id: "999")
}
```

**Response:**
```json
{
  "data": { "customerById": null },
  "errors": [
    {
      "message": "Customer not found with id: 999",
      "path": ["customerById"],
      "extensions": { "classification": "DataFetchingException" }
    }
  ]
}
```

---

## 📡 Example Queries

### Create a New Customer
```graphql
mutation {
  createCustomer(input: {
    firstName: "Nasruddin",
    lastName: "Khan",
    email: "nasruddinkhan@alinma.com",
    phone: "555-1234"
  }) {
    id
    firstName
    email
  }
}
```

### Subscribe to Real-Time Events
```graphql
subscription {
  customerEvents {
    id
    msg
  }
}
```

---

## 🧪 Running the Application

### Start MongoDB
```bash
docker run -d --name mongo   -p 27017:27017   -e MONGO_INITDB_ROOT_USERNAME=admin   -e MONGO_INITDB_ROOT_PASSWORD=admin   mongo
```

### Run the Service
```bash
mvn spring-boot:run
```

### Open GraphiQL
🔗 [http://localhost:8082/graphiql](http://localhost:8082/graphiql)

---

## 🧰 Tech Stack

| Layer | Technology |
|--------|-------------|
| Language | Java 17+ |
| Framework | Spring Boot 3.x |
| GraphQL | Spring GraphQL |
| Reactive | Project Reactor |
| Database | MongoDB Reactive |
| Error Handling | DataFetcherExceptionResolver |
| Build Tool | Maven |
| API Explorer | GraphiQL |

---

## 🧩 Future Enhancements

- Add **JWT-based authentication** for GraphQL.
- Introduce **Kafka-based event streaming**.
- Add **pagination** for customer list queries.
- Include **unit and integration tests**.

---

## 👨‍💻 Author
**Nasruddin Khan**  
Backend Developer — Alinma Bank  
💡 Specialized in Reactive Spring Boot, Microservices, and GraphQL APIs.
