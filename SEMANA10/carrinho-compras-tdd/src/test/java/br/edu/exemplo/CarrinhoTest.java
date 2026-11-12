package br.edu.exemplo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/*
 * Um teste por requisito de REQUISITOS.md, escrito antes da implementação
 * correspondente em Carrinho (ciclo RED -> GREEN -> REFACTOR).
 */
class CarrinhoTest {

    private static final double DELTA = 0.001;

    // Requisito 1 — Carrinho vazio
    @Test
    void carrinhoNovoDeveTerTotalZero() {
        Carrinho carrinho = new Carrinho();

        assertEquals(0.0, carrinho.getTotal(), DELTA);
    }

    // Requisito 2 — Adicionar um produto
    @Test
    void deveAdicionarUmProduto() {
        Carrinho carrinho = new Carrinho();

        carrinho.adicionar(new Produto("Teclado", 100.0));

        assertEquals(100.0, carrinho.getTotal(), DELTA);
    }

    // Requisito 3 — Adicionar vários produtos
    @Test
    void deveSomarVariosProdutos() {
        Carrinho carrinho = new Carrinho();

        carrinho.adicionar(new Produto("Teclado", 100.0));
        carrinho.adicionar(new Produto("Mouse", 50.0));

        assertEquals(150.0, carrinho.getTotal(), DELTA);
    }

    // Requisito 4 — Remover produto
    @Test
    void deveRemoverProduto() {
        Carrinho carrinho = new Carrinho();
        Produto teclado = new Produto("Teclado", 100.0);
        Produto mouse = new Produto("Mouse", 50.0);
        carrinho.adicionar(teclado);
        carrinho.adicionar(mouse);

        carrinho.remover(mouse);

        assertEquals(100.0, carrinho.getTotal(), DELTA);
    }

    // Requisito 5 — Produto com preço inválido
    // Decisão: o carrinho lança IllegalArgumentException e não altera o total.
    @Test
    void naoDeveAceitarProdutoComPrecoNegativo() {
        Carrinho carrinho = new Carrinho();

        assertThrows(IllegalArgumentException.class,
                () -> carrinho.adicionar(new Produto("Teclado", -100.0)));

        assertEquals(0.0, carrinho.getTotal(), DELTA);
    }

    // Requisito 6 — Desconto de 10% a partir de R$ 500,00
    @Test
    void deveAplicarDescontoDe10PorCentoAcimaDe500() {
        Carrinho carrinho = new Carrinho();

        carrinho.adicionar(new Produto("Monitor", 600.0));

        assertEquals(600.0, carrinho.getSubtotal(), DELTA);
        assertEquals(540.0, carrinho.getTotal(), DELTA);
    }

    // Requisito 6 — fronteira: logo abaixo de R$ 500,00 não tem desconto
    @Test
    void naoDeveAplicarDescontoEm499e99() {
        Carrinho carrinho = new Carrinho();

        carrinho.adicionar(new Produto("Cadeira", 499.99));

        assertEquals(499.99, carrinho.getTotal(), DELTA);
    }

    // Requisito 6 — fronteira: exatamente R$ 500,00 já tem desconto
    @Test
    void deveAplicarDescontoEm500() {
        Carrinho carrinho = new Carrinho();

        carrinho.adicionar(new Produto("Cadeira", 500.0));

        assertEquals(450.0, carrinho.getTotal(), DELTA);
    }

    // Requisito 6 — fronteira: logo acima de R$ 500,00
    @Test
    void deveAplicarDescontoEm500e01() {
        Carrinho carrinho = new Carrinho();

        carrinho.adicionar(new Produto("Cadeira", 500.01));

        assertEquals(450.009, carrinho.getTotal(), DELTA);
    }
}
