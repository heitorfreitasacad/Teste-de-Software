package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    private ItemPedido item(String sku, int quantidade, int estoque, int pesoGramas, boolean fragil) {
        return new ItemPedido(sku, 1_000, quantidade, estoque, pesoGramas, fragil);
    }

    @Test
    void deveRejeitarListaDeItensNula() {
        assertThrows(IllegalArgumentException.class,
            () -> new Pedido(null, "PR", false, null));
    }

    @Test
    void deveRejeitarMaisDeCemLinhas() {
        List<ItemPedido> itens = Collections.nCopies(101, item("SKU-1", 1, 5, 500, false));

        assertThrows(IllegalArgumentException.class,
            () -> new Pedido(itens, "PR", false, null));
    }

    @Test
    void deveAceitarNoLimiteDeCemLinhas() {
        List<ItemPedido> itens = Collections.nCopies(100, item("SKU-1", 1, 5, 500, false));

        Pedido pedido = new Pedido(itens, "PR", false, null);

        assertEquals(100, pedido.itens().size());
    }

    @Test
    void deveAceitarListaVaziaNaConstrucao() {
        Pedido pedido = new Pedido(List.of(), "PR", false, null);

        assertEquals(0L, pedido.subtotalCentavos());
    }

    @Test
    void deveRejeitarUfNula() {
        assertThrows(IllegalArgumentException.class,
            () -> new Pedido(List.of(item("SKU-1", 1, 5, 500, false)), null, false, null));
    }

    @Test
    void deveRejeitarUfComFormatoInvalido() {
        assertThrows(IllegalArgumentException.class,
            () -> new Pedido(List.of(item("SKU-1", 1, 5, 500, false)), "pr", false, null));
        assertThrows(IllegalArgumentException.class,
            () -> new Pedido(List.of(item("SKU-1", 1, 5, 500, false)), "PRR", false, null));
    }

    @Test
    void subtotalIgnoraItensInativos() {
        ItemPedido ativo = item("SKU-1", 2, 5, 500, false);
        ItemPedido inativo = item("SKU-2", 0, 5, 500, false);
        Pedido pedido = new Pedido(List.of(ativo, inativo), "PR", false, null);

        assertEquals(2_000L, pedido.subtotalCentavos());
    }

    @Test
    void pesoGramasSomaPesoDeTodosOsItensAtivos() {
        ItemPedido item1 = item("SKU-1", 2, 5, 300, false);
        ItemPedido item2 = item("SKU-2", 1, 5, 700, false);
        Pedido pedido = new Pedido(List.of(item1, item2), "PR", false, null);

        assertEquals(2 * 300 + 700, pedido.pesoGramas());
    }

    @Test
    void temFragilRetornaVerdadeiroComItemAtivoEFragil() {
        Pedido pedido = new Pedido(List.of(item("SKU-1", 1, 5, 500, true)), "PR", false, null);

        assertTrue(pedido.temFragil());
    }

    @Test
    void temFragilRetornaFalsoComItemAtivoNaoFragil() {
        Pedido pedido = new Pedido(List.of(item("SKU-1", 1, 5, 500, false)), "PR", false, null);

        assertFalse(pedido.temFragil());
    }

    @Test
    void temFragilIgnoraItemInativoMesmoQuandoFragil() {
        // quantidade 0: o curto-circuito do && nunca avalia fragil().
        Pedido pedido = new Pedido(List.of(item("SKU-1", 0, 5, 500, true)), "PR", false, null);

        assertFalse(pedido.temFragil());
    }

    @Test
    void estoqueSuficienteRetornaVerdadeiroQuandoTodosDisponiveis() {
        Pedido pedido = new Pedido(List.of(item("SKU-1", 2, 5, 500, false)), "PR", false, null);

        assertTrue(pedido.estoqueSuficiente());
    }

    @Test
    void estoqueSuficienteRetornaFalsoQuandoUltimoItemFaltaEstoque() {
        ItemPedido disponivel = item("SKU-1", 1, 5, 500, false);
        ItemPedido indisponivel = item("SKU-2", 3, 1, 500, false);
        Pedido pedido = new Pedido(List.of(disponivel, indisponivel), "PR", false, null);

        assertFalse(pedido.estoqueSuficiente());
    }

    @Test
    void estoqueSuficienteRetornaFalsoQuandoPrimeiroItemFaltaEstoqueEInterrompeAntesDosDemais() {
        ItemPedido indisponivel = item("SKU-1", 3, 1, 500, false);
        ItemPedido disponivel = item("SKU-2", 1, 5, 500, false);
        Pedido pedido = new Pedido(List.of(indisponivel, disponivel), "PR", false, null);

        assertFalse(pedido.estoqueSuficiente());
    }
}
