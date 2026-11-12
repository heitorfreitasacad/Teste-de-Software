# Relatório — Práticas de TDD (Semana 10)

Integrantes: Heitor Henrique Scramim de Freitas
RA: 24190382-2

## Como executar

Cada pasta é um projeto Maven independente (Java 17 + JUnit 5). Dentro de cada uma:

```bash
mvn test
```

## Resumo

| Projeto | Testes | Origem dos testes | Resultado |
| --- | --- | --- | --- |
| `bubblesort-tdd` | 3 | fornecidos pelo professor | 3/3 passando |
| `conta-bancaria-tdd-maven` | 4 | fornecidos pelo professor | 4/4 passando |
| `estoque-tdd` | 6 | fornecidos pelo professor | 6/6 passando |
| `carrinho-compras-tdd` | 9 | escritos a partir do `REQUISITOS.md` | 9/9 passando |

Nos três primeiros projetos, os testes já vieram prontos e não foram alterados (era uma restrição da atividade). No carrinho, os testes foram escritos antes da classe `Carrinho`, um por requisito.

---

## 1. `bubblesort-tdd`

**RED** — antes da implementação, `ordenar()` não fazia nada:

```text
BubbleSortTest.deveOrdenarVetorDesordenado:16 array contents differ at index [0], expected: <1> but was: <5>
BubbleSortTest.deveOrdenarVetorComValoresRepetidos:42 array contents differ at index [0], expected: <1> but was: <3>
Tests run: 3, Failures: 2, Errors: 0, Skipped: 0
```

O teste do vetor já ordenado passou "de graça", porque não mexer no vetor já dá o resultado certo. Ele só começa a provar algo depois que existe implementação: garante que a ordenação não bagunça um vetor que já estava certo.

**GREEN** — `ordenar()` faz as passadas do Bubble Sort, trocando vizinhos fora de ordem. A cada passada, o maior elemento restante vai para o fim, então o limite `fim` diminui.

**REFACTOR** — a troca foi extraída para o método `trocar(i, j)`, e o laço para cedo quando uma passada inteira não troca nada (o vetor já está ordenado). Os testes continuaram passando.

```text
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

## 2. `conta-bancaria-tdd-maven`

**RED** — `depositar` e `sacar` vazios, saldo sempre 0:

```text
ContaBancariaTest.deveDepositarValorNaConta:12 expected: <100.0> but was: <0.0>
ContaBancariaTest.deveAcumularDepositos:20 expected: <150.0> but was: <0.0>
ContaBancariaTest.deveSacarValorDaConta:28 expected: <60.0> but was: <0.0>
ContaBancariaTest.naoDevePermitirSaqueMaiorQueSaldo:36 expected: <100.0> but was: <0.0>
Tests run: 4, Failures: 4, Errors: 0, Skipped: 0
```

**GREEN** — `depositar` soma o valor ao saldo e `sacar` subtrai, mas só quando o valor não passa do saldo. O saque maior que o saldo é ignorado e o saldo não muda, que é o que `naoDevePermitirSaqueMaiorQueSaldo` espera.

**REFACTOR** — as duas operações também ignoram valores menores ou iguais a zero. Nenhum teste pede isso, mas um depósito negativo funcionaria como um saque escondido, então a regra foi deixada explícita.

```text
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

## 3. `estoque-tdd`

**RED** — com os métodos vazios, 4 dos 6 testes falharam:

```text
ProdutoTest.deveAdicionarProdutoAoEstoque:22 expected: <10> but was: <0>
ProdutoTest.deveAcumularQuantidadeEmEstoque:32 expected: <15> but was: <0>
ProdutoTest.deveRetirarProdutoDoEstoque:42 expected: <7> but was: <0>
ProdutoTest.naoDeveRetirarQuantidadeMaiorQueEstoque:52 expected: <10> but was: <0>
Tests run: 6, Failures: 4, Errors: 0, Skipped: 0
```

Os testes de estoque inicial zero (requisito 1) e de entrada negativa (requisito 6) passaram sem implementação, porque "não fazer nada" já mantém o estoque em 0. Assim como no bubble sort, eles só passam a ter valor depois que existe código: garantem que a implementação não quebra esses casos.

**GREEN** — `adicionarEstoque` soma só quantidades positivas (requisito 6 pede para recusar valores negativos ou zero). `retirarEstoque` subtrai só quando há quantidade suficiente (requisito 5), sem alterar o estoque numa retirada inválida.

```text
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
```

## 4. `carrinho-compras-tdd`

Aqui os testes não vieram prontos. Cada requisito do `REQUISITOS.md` virou um teste em `CarrinhoTest`, escrito antes do código de `Carrinho`.

| Requisito | Teste | O que verifica |
| --- | --- | --- |
| 1 — Carrinho vazio | `carrinhoNovoDeveTerTotalZero` | total de um carrinho novo é R$ 0,00 |
| 2 — Adicionar um produto | `deveAdicionarUmProduto` | Teclado R$ 100,00 → total R$ 100,00 |
| 3 — Vários produtos | `deveSomarVariosProdutos` | Teclado + Mouse → total R$ 150,00 |
| 4 — Remover produto | `deveRemoverProduto` | remove o Mouse → total R$ 100,00 |
| 5 — Preço inválido | `naoDeveAceitarProdutoComPrecoNegativo` | preço negativo lança `IllegalArgumentException` e o total continua R$ 0,00 |
| 6 — Desconto | `deveAplicarDescontoDe10PorCentoAcimaDe500` | subtotal R$ 600,00 → total R$ 540,00 |
| 6 — Fronteira | `naoDeveAplicarDescontoEm499e99` | R$ 499,99 → sem desconto |
| 6 — Fronteira | `deveAplicarDescontoEm500` | R$ 500,00 → R$ 450,00 |
| 6 — Fronteira | `deveAplicarDescontoEm500e01` | R$ 500,01 → R$ 450,009 |

**RED** — com a classe `Carrinho` vazia, o projeto nem compilou, porque os métodos ainda não existiam:

```text
[ERROR] symbol:   method getTotal()
[ERROR] symbol:   method adicionar(br.edu.exemplo.Produto)
[ERROR] symbol:   method remover(br.edu.exemplo.Produto)
[ERROR] symbol:   method getSubtotal()
```

**GREEN** — `Carrinho` guarda uma lista de `Produto`. `getSubtotal()` soma os preços e `getTotal()` aplica 10% de desconto quando o subtotal é maior ou igual a R$ 500,00.

**REFACTOR** — o valor mínimo (500) e o percentual (10%) viraram constantes com nome, em vez de números soltos no código.

```text
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
```

### Decisão do requisito 5

O requisito pede para decidir entre ignorar a operação ou lançar uma exceção. Escolhi **lançar `IllegalArgumentException`**: um produto com preço negativo é um erro de quem chamou o método, e ignorar em silêncio esconderia esse erro. O teste também confere que o carrinho não muda depois da tentativa.

### Fronteira do desconto

Os três valores de fronteira (R$ 499,99, R$ 500,00 e R$ 500,01) pegam o erro mais comum nessa regra: usar `>` em vez de `>=`. Com `>`, o teste de R$ 500,00 falharia.
