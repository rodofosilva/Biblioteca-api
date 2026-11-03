# 📚 API Biblioteca (Livros e Empréstimos)

API REST em **Spring Boot 3 / Java 17** para gerenciar livros e empréstimos de uma biblioteca. Trabalho de Programação Backend.

**Vídeo de demonstração:** _cole aqui o link (YouTube não listado ou Drive)_

## Tecnologias
Spring Web, Spring Data JPA, Bean Validation, H2 (em memória), Lombok, Springdoc OpenAPI (Swagger) e front-end em HTML + Bootstrap + JavaScript (ponto extra).

## Como executar
Requisitos: **JDK 17+** e **Maven**.

```bash
mvn spring-boot:run
```

| Recurso | URL |
|---|---|
| Telas (front-end) | http://localhost:8080/ |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Console H2 | http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:biblioteca`, user `sa`, sem senha) |

Os dados são recriados a cada execução, com 4 livros de exemplo (`data.sql`).

## Modelo de dados
Relacionamento **1:N**: um `Livro` possui vários `Emprestimo` (`@OneToMany` / `@ManyToOne`, FK `livro_id`).

- **livro:** id, titulo, autor, categoria, quantidade_total, quantidade_disponivel
- **emprestimo:** id, nome_usuario, data_emprestimo, data_prevista_devolucao, data_devolucao, status (ATIVO | ATRASADO | DEVOLVIDO), multa, livro_id

## Endpoints
| Método | Rota | Descrição | Status |
|---|---|---|---|
| GET | `/livros?categoria=&autor=&disponivel=` | Lista com filtros | 200 |
| GET | `/livros/{id}` | Busca por id | 200 / 404 |
| POST | `/livros` | Cadastra | 201 / 400 |
| PUT | `/livros/{id}` | Atualiza | 200 / 400 / 404 / 409 |
| DELETE | `/livros/{id}` | Exclui | 204 / 404 / 409 |
| GET | `/emprestimos?status=&usuario=&livroId=&de=&ate=` | Lista com filtros | 200 / 400 |
| GET | `/emprestimos/{id}` | Busca por id | 200 / 404 |
| POST | `/emprestimos` | Realiza empréstimo | 201 / 400 / 404 / 409 |
| PUT | `/emprestimos/{id}` | Altera usuário e data prevista | 200 / 400 / 404 / 409 |
| PATCH | `/emprestimos/{id}/devolucao` | Registra devolução | 200 / 404 / 409 |
| DELETE | `/emprestimos/{id}` | Exclui (apenas devolvidos) | 204 / 404 / 409 |

## Regras de negócio
1. Não é possível emprestar livro **sem estoque** (409).
2. Ao emprestar, `quantidadeDisponivel` diminui; ao devolver, aumenta.
3. Cada usuário pode ter no máximo **3 empréstimos em aberto** (409).
4. O mesmo usuário não pode pegar **o mesmo livro duas vezes** em aberto (409).
5. Prazo do empréstimo entre **1 e 30 dias**.
6. Empréstimos vencidos passam automaticamente a **ATRASADO**.
7. Devolução com atraso gera **multa de R$ 2,00 por dia**; devolver duas vezes é bloqueado (409).
8. Empréstimo devolvido não pode ser editado; só devolvidos podem ser excluídos (409).
9. **Livro com empréstimos em aberto (ou histórico) não pode ser excluído** (409).
10. Ao alterar `quantidadeTotal` de um livro, o disponível é recalculado e não pode ficar negativo (409).

## Tratamento de erros
`@RestControllerAdvice` (`GlobalExceptionHandler`) padroniza as respostas:
```json
{ "status": 409, "erro": "Conflict", "mensagem": "Livro sem estoque disponível", "timestamp": "2026-10-22T10:00:00" }
```
400 (validação / formato inválido), 404 (não encontrado), 409 (regra de negócio), 500 (erro inesperado).

## Estrutura
```
src/main/java/br/com/biblioteca
├── controller   # endpoints REST + anotações Swagger
├── service      # regras de negócio
├── repository   # Spring Data JPA
├── model        # entidades e enum
├── dto          # requests/validações e resposta de erro
└── exception    # exceções e handler global
src/main/resources/static/index.html   # telas
```

## Roteiro sugerido para o vídeo (8–12 min)
1. Apresentação do tema · 2. Estrutura de pastas · 3. Entidades e relacionamento (console H2)
4. CRUD no Swagger · 5. Filtros · 6. Regras de negócio (provocar 409: sem estoque, limite de 3, excluir livro emprestado, devolução com multa)
7. Exceções (404, 400, 409) e `GlobalExceptionHandler` · 8. Telas conectadas à API · 9. Encerramento

## Autor(es)
_Nome(s) do grupo_
