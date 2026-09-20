package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraFreteTest {

    private final CalculadoraFrete calculadora = new CalculadoraFrete();
    private final Cliente comum = new Cliente(false, false, 1);
    private final Cliente vip = new Cliente(true, false, 1);

    private Pedido pedidoComPeso(int pesoGramas, String uf, boolean expresso, boolean fragil) {
        ItemPedido item = new ItemPedido("SKU-1", 1_000, 1, 5, pesoGramas, fragil);
        return new Pedido(List.of(item), uf, expresso, null);
    }

    @Test
    void deveRejeitarValorLiquidoNegativo() {
        Pedido pedido = pedidoComPeso(1_000, "PR", false, false);

        assertThrows(IllegalArgumentException.class,
            () -> calculadora.calcular(pedido, comum, -1));
    }

    @Test
    void baseParanaSemExcedenteDePeso() {
        Pedido pedido = pedidoComPeso(1_000, "PR", false, false);

        assertEquals(1_200L, calculadora.calcular(pedido, comum, 1_000));
    }

    @Test
    void baseSaoPaulo() {
        Pedido pedido = pedidoComPeso(1_000, "SP", false, false);

        assertEquals(2_000L, calculadora.calcular(pedido, comum, 1_000));
    }

    @Test
    void baseRioDeJaneiro() {
        Pedido pedido = pedidoComPeso(1_000, "RJ", false, false);

        assertEquals(2_000L, calculadora.calcular(pedido, comum, 1_000));
    }

    @Test
    void baseDemaisEstados() {
        Pedido pedido = pedidoComPeso(1_000, "MG", false, false);

        assertEquals(3_000L, calculadora.calcular(pedido, comum, 1_000));
    }

    @Test
    void semAdicionalDePesoNoLimiteExatoDeDoisQuilos() {
        Pedido pedido = pedidoComPeso(2_000, "PR", false, false);

        assertEquals(1_200L, calculadora.calcular(pedido, comum, 1_000));
    }

    @Test
    void adicionalDePesoComUmGramaAcimaDoLimite() {
        Pedido pedido = pedidoComPeso(2_001, "PR", false, false);

        assertEquals(1_500L, calculadora.calcular(pedido, comum, 1_000));
    }

    @Test
    void adicionalDePesoCobraFracaoComoQuiloCompleto() {
        Pedido pedido = pedidoComPeso(2_500, "PR", false, false);

        assertEquals(1_500L, calculadora.calcular(pedido, comum, 1_000));
    }

    @Test
    void adicionalDePesoComVariasIteracoes() {
        Pedido pedido = pedidoComPeso(4_500, "PR", false, false);

        assertEquals(2_100L, calculadora.calcular(pedido, comum, 1_000));
    }

    @Test
    void freteGratisComLiquidoNoLimiteEEntregaNormal() {
        Pedido pedido = pedidoComPeso(1_000, "PR", false, false);

        assertEquals(0L, calculadora.calcular(pedido, comum, 30_000));
    }

    @Test
    void freteNaoFicaGratisLogoAbaixoDoLimite() {
        Pedido pedido = pedidoComPeso(1_000, "PR", false, false);

        assertEquals(1_200L, calculadora.calcular(pedido, comum, 29_999));
    }

    @Test
    void freteNaoFicaGratisComEntregaExpressaMesmoAcimaDoLimite() {
        Pedido pedido = pedidoComPeso(1_000, "PR", true, false);

        assertEquals(1_200L + 1_500L, calculadora.calcular(pedido, comum, 30_000));
    }

    @Test
    void freteZeradoAindaSomaAdicionalDeFragilidade() {
        Pedido pedido = pedidoComPeso(3_000, "PR", false, true);

        assertEquals(500L, calculadora.calcular(pedido, comum, 30_000));
    }

    @Test
    void clienteVipPagaMetadeDoFrete() {
        Pedido pedido = pedidoComPeso(1_000, "PR", false, false);

        assertEquals(600L, calculadora.calcular(pedido, vip, 1_000));
    }

    @Test
    void entregaExpressaAcrescentaQuinzeReais() {
        Pedido pedido = pedidoComPeso(1_000, "PR", true, false);

        assertEquals(1_200L + 1_500L, calculadora.calcular(pedido, comum, 1_000));
    }

    @Test
    void itemFragilAcrescentaCincoReais() {
        Pedido pedido = pedidoComPeso(1_000, "PR", false, true);

        assertEquals(1_200L + 500L, calculadora.calcular(pedido, comum, 1_000));
    }

    @Test
    void fragilidadeSoAcrescentaUmaVezComVariosItensFrageis() {
        ItemPedido item1 = new ItemPedido("SKU-1", 1_000, 1, 5, 500, true);
        ItemPedido item2 = new ItemPedido("SKU-2", 1_000, 1, 5, 500, true);
        Pedido pedido = new Pedido(List.of(item1, item2), "PR", false, null);

        assertEquals(1_200L + 500L, calculadora.calcular(pedido, comum, 1_000));
    }

    @Test
    void vipExpressoEFragilCombinados() {
        Pedido pedido = pedidoComPeso(1_000, "PR", true, true);

        assertEquals(600L + 1_500L + 500L, calculadora.calcular(pedido, vip, 1_000));
    }
}
