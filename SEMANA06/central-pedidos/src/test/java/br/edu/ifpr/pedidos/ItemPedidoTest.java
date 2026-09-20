package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class ItemPedidoTest {

    @Test
    void deveCriarItemValidoECalcularTotal() {
        ItemPedido item = new ItemPedido("SKU-1", 1_000, 2, 5, 500, false);

        assertAll(
            () -> assertEquals(2_000L, item.totalCentavos()),
            () -> assertTrue(item.disponivel())
        );
    }

    @Test
    void deveRejeitarSkuNulo() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido(null, 1_000, 1, 5, 500, false));
    }

    @Test
    void deveRejeitarSkuEmBranco() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("   ", 1_000, 1, 5, 500, false));
    }

    @ParameterizedTest
    @CsvSource({"0", "-1", "1000001"})
    void deveRejeitarPrecoForaDoIntervalo(long preco) {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", preco, 1, 5, 500, false));
    }

    @ParameterizedTest
    @CsvSource({"1", "1000000"})
    void deveAceitarPrecoNosLimites(long preco) {
        ItemPedido item = new ItemPedido("SKU-1", preco, 1, 5, 500, false);

        assertEquals(preco, item.precoCentavos());
    }

    @ParameterizedTest
    @CsvSource({"-1", "101"})
    void deveRejeitarQuantidadeForaDoIntervalo(int quantidade) {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 1_000, quantidade, 5, 500, false));
    }

    @ParameterizedTest
    @CsvSource({"0", "100"})
    void deveAceitarQuantidadeNosLimites(int quantidade) {
        ItemPedido item = new ItemPedido("SKU-1", 1_000, quantidade, 100, 500, false);

        assertEquals(quantidade, item.quantidade());
    }

    @Test
    void deveRejeitarEstoqueNegativo() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 1_000, 1, -1, 500, false));
    }

    @Test
    void deveAceitarEstoqueZero() {
        ItemPedido item = new ItemPedido("SKU-1", 1_000, 0, 0, 500, false);

        assertEquals(0, item.estoque());
    }

    @ParameterizedTest
    @CsvSource({"0", "-1", "100001"})
    void deveRejeitarPesoForaDoIntervalo(int peso) {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 1_000, 1, 5, peso, false));
    }

    @ParameterizedTest
    @CsvSource({"1", "100000"})
    void deveAceitarPesoNosLimites(int peso) {
        ItemPedido item = new ItemPedido("SKU-1", 1_000, 1, 5, peso, false);

        assertEquals(peso, item.pesoGramas());
    }

    @Test
    void disponivelDeveSerFalsoQuandoQuantidadeMaiorQueEstoque() {
        ItemPedido item = new ItemPedido("SKU-1", 1_000, 5, 2, 500, false);

        assertFalse(item.disponivel());
    }

    @Test
    void disponivelDeveSerVerdadeiroNoLimiteExatoDoEstoque() {
        ItemPedido item = new ItemPedido("SKU-1", 1_000, 5, 5, 500, false);

        assertTrue(item.disponivel());
    }
}
