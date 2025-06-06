# Qcscribe
Semantic RAG-powered AI tool that converts user stories into actionable test cases.

---

## Requirements
* JDK 24
* **Qdrant Cloud Or Local:** Vector database for semantic retrieval
* **Ollama:** Local model server for embedding and chat model inference

---

## Configuration

```yaml
spring:
  application:
    name: qcscribe-service
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: qwen3:1.7b # You can use models like gemma3:1b
      embedding:
        options:
          model: nomic-embed-text
    vectorstore:
      qdrant:
        host: <qdrant host>
        port: <qdrant grpc port>
        collection-name: <collection name>
        api-key: <qdrant api key>
        use-tls: true
qcscribe:
  search:
    topK: 10
    threshold: 0.0
```

* **ollama.base-url:** Points to the Ollama server (for embedding + chat models).
* **chat.options.model:** Language model for generating story responses.
* **embedding.options.model:** Embedding model used to generate vector representations.
* **vectorstore.qdrant:** Qdrant Cloud configuration.

  * `use-tls` should be `true` for Qdrant Cloud.
  * You need to provide an API key in the `api-key` field.

---

## Initial Setup
Before running the application, ensure the Qdrant collection is created. If you're using **Qdrant Cloud**, you must pass the API key in the `api-key` header.
Use the following `curl` command to create the collection `qcscribe`:

```bash
curl -X PUT <qdrant-host>/collections/qcscribe \
  -H "Content-Type: application/json" \
  -H "api-key: <your-qdrant-api-key>" \
  -d '{
    "vectors": {
      "size": 768,
      "distance": "Cosine"
    }
  }'
```

---

## Running the Project

```bash
./mvnw spring-boot:run
```

### First-Time Embedding Load
If this is your first time running the project, you need to load the Hoppscotch docs into the vector store:

Uncomment this line in `MarkdownLoader.java`:
```java
@PostConstruct
public void init() {}
```

Make sure `.mdx` documentation files are placed under:
```
src/main/resources/hoppscotch/
```
You can download them from:
[https://github.com/hoppscotch/docs/tree/main/documentation](https://github.com/hoppscotch/docs/tree/main/documentation)

---

## API Usage

### POST `/generate`
Generates test cases based on the provided feature title and criteria.

```bash
curl -X POST http://localhost:8080/generate \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Add GraphQL support",
    "criteria": [
      "User can select 'GraphQL' as a request type.",
      "When 'GraphQL' is selected, a dedicated query editor and variables editor are displayed.",
      "User can enter a valid GraphQL query and variables. User can send a GraphQL request to a specified endpoint.",
      "The GraphQL response (data and errors) is clearly displayed in the response pane.",
      "Error messages are displayed for invalid GraphQL query syntax before sending the request."
    ]
  }'
```

---

## 🔜 Coming Soon
- In-depth tutorial and walkthrough
- Testing

---
