# Testes de integração com Spring Boot e JUnit 5 (Semana 11)

Integrantes: Heitor Henrique Scramim de Freitas
RA: 24190382-2

Enunciado completo: [`../lista_exercicios_integracao_springboot.md`](../lista_exercicios_integracao_springboot.md).

**Tecnologias:** Java 17, Maven, Spring Boot 3.3, Spring Data JPA, H2 em memória, JUnit 5 e MockMvc.

## Como executar

```bash
mvn test
```

## Resultado

```text
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in br.edu.exemplo.ProdutoControllerIntegrationTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in br.edu.exemplo.ProdutoServiceIntegrationTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Nenhum mock foi usado. `ProdutoService`, `ProdutoRepository` e o banco H2 são os componentes reais, criados pelo Spring.

## Casos de teste

### Exercício 1 — Service + Repository + H2 (`ProdutoServiceIntegrationTest`)

| Caso | Método | Como foi testado | Resultado |
| --- | --- | --- | --- |
| CT01 — Cadastro válido | `deveCadastrarERecuperarProduto` | Cadastra `Teclado` por `150.0` pelo serviço, confere que recebeu ID, sincroniza com o banco e busca com `repository.findById()`. Confere nome e preço do registro lido. | Passou |
| CT02 — Nome inválido | `naoDevePersistirProdutoComNomeEmBranco` | Conta os registros, tenta cadastrar nome `"   "` com preço `80.0`, espera `IllegalArgumentException("Nome obrigatório")` e confere que a contagem não mudou. | Passou |
| CT03 — Atualização (desafio) | `deveAtualizarPrecoPersistido` | Cadastra `Mouse` por `80.0`, chama `atualizarPreco(id, 95.0)`, sincroniza com o banco e busca de novo pelo repositório. O preço lido é `95.0`. | Passou |

### Exercício 2 — HTTP com MockMvc (`ProdutoControllerIntegrationTest`)

| Caso | Método | Como foi testado | Resultado |
| --- | --- | --- | --- |
| CT04 — Cadastro via API | `deveCadastrarProdutoPelaApi` | `POST /produtos` com `{"nome":"Mouse","preco":80.0}`. Espera HTTP 200 e, no JSON, `id` numérico, `nome` = `Mouse` e `preco` = `80.0`. | Passou |
| CT05 — Consulta via API | `deveBuscarProdutoPelaApi` | Faz o POST, lê o corpo da resposta com `ObjectMapper`/`JsonNode` para pegar o ID e chama `GET /produtos/{id}`. Espera HTTP 200 e o mesmo `id`, `nome` e `preco`. | Passou |

### Os testes pegam erro de verdade?

Para conferir, alterei o `ProdutoService` de propósito, numa cópia fora do projeto, e rodei a suíte de novo:

| Defeito introduzido | Resultado |
| --- | --- |
| Remover a validação de nome em branco | CT02 falhou |
| `atualizarPreco` não altera o preço | CT03 falhou |
| `cadastrar` não salva no repositório | CT01, CT03, CT04 e CT05 falharam |

O código original foi mantido; essa checagem só mostrou que cada teste depende de verdade do comportamento que ele diz verificar.

## Detalhe importante: o cache do JPA

Dentro de um teste `@Transactional`, o JPA guarda os objetos já carregados num cache (o *contexto de persistência*). Se o teste chamar `repository.findById()` logo depois de salvar, o JPA pode devolver **o mesmo objeto da memória**, sem fazer nenhum `SELECT` no H2. Aí o teste passaria mesmo se o banco não tivesse recebido nada.

Por isso, CT01 e CT03 chamam `sincronizarComBanco()` antes de consultar:

```java
repository.flush();      // envia os INSERT/UPDATE pendentes para o H2
entityManager.clear();   // esvazia o cache: a próxima busca vira SELECT de verdade
```

O CT01 ainda confere com `assertNotSame` que o objeto lido é outro, criado a partir do banco.

## Discussão

### Exercício 1

**Por que `repository.findById()` é uma evidência diferente de olhar só o objeto devolvido pelo serviço?**

O objeto que `service.cadastrar()` devolve só prova que o serviço montou um `Produto` em memória. Ele não prova que o registro foi gravado: o serviço poderia, por exemplo, devolver `new Produto(...)` sem chamar `save` (foi um dos defeitos testados acima). Buscar pelo repositório, depois de sincronizar com o banco, mostra que o dado fez o caminho completo: serviço → repositório → JPA → H2, e que volta igual quando é lido de novo. Esse caminho completo é justamente o que o teste de integração existe para verificar.

**O que `@Transactional` faz ao final de cada teste?**

Cada método de teste roda dentro de uma transação que o Spring **desfaz (rollback)** no final, em vez de confirmar. Então o `Teclado` do CT01 e o `Mouse` do CT03 somem do H2 quando o teste termina. Isso deixa os testes **isolados**: um não vê os dados do outro, a ordem de execução não importa e o `repository.count()` do CT02 não é afetado pelos outros testes. O `flush()` usado no CT01 e no CT03 envia os comandos SQL ao banco **dentro** da transação; o rollback desfaz esses comandos do mesmo jeito.

### Exercício 2

**Quais componentes são exercitados?**

O caminho inteiro da aplicação: o Spring MVC (`DispatcherServlet`) recebe a requisição, o Jackson converte o JSON em `ProdutoRequest`, o `ProdutoController` chama o `ProdutoService` real, que usa o `ProdutoRepository` real, que grava no H2 via JPA/Hibernate. Na volta, o Jackson converte o `Produto` em JSON e o MockMvc confere o status e o corpo da resposta.

**Por que o MockMvc não precisa de um servidor HTTP real?**

O MockMvc chama o `DispatcherServlet` do Spring diretamente, dentro da mesma JVM do teste, usando objetos de requisição e resposta simulados (`MockHttpServletRequest` e `MockHttpServletResponse`). Não abre porta nem passa pela rede, mas toda a parte do Spring (mapeamento de rotas, conversão de JSON, tratamento da resposta) roda igual. Por isso o teste é rápido e não depende de rede, e mesmo assim testa a camada web de verdade. Como roda na mesma thread, o `@Transactional` do teste também cobre a requisição, e o rollback limpa o que o POST gravou.

**Qual a diferença para um teste unitário do controller com dependências simuladas?**

Num teste unitário, o `ProdutoService` seria um mock (por exemplo, com `@WebMvcTest` + `@MockBean`): o teste diria ao mock o que devolver e só verificaria se o controller repassa os dados e monta a resposta. Ele não descobriria problemas com o banco, com o mapeamento JPA da entidade, com a geração do ID ou com a regra de validação do serviço, porque nada disso roda. O teste de integração deste projeto executa essas peças juntas. Ele pega problemas de **encaixe** entre as camadas, em troca de ser mais lento e de apontar com menos precisão qual camada falhou.
