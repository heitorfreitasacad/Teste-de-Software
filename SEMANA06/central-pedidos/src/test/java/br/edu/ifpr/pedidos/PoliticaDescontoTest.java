package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PoliticaDescontoTest {

    private final PoliticaDesconto politica = new PoliticaDesconto();

    @Test
    void deveRejeitarSubtotalNegativo() {
        Cliente cliente = new Cliente(false, false, 1);

        assertThrows(IllegalArgumentException.class,
            () -> politica.calcular(cliente, -1, null));
    }

    @Test
    void clienteVipRecebeDezPorCento() {
        Cliente cliente = new Cliente(true, false, 1);

        assertEquals(1_000L, politica.calcular(cliente, 10_000, null));
    }

    @Test
    void clienteComumComSubtotalNoLimiteRecebeCincoPorCento() {
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(2_500L, politica.calcular(cliente, 50_000, null));
    }

    @Test
    void clienteComumAbaixoDoLimiteNaoRecebeDesconto() {
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(0L, politica.calcular(cliente, 49_999, null));
    }

    @Test
    void cupomEmBrancoMantemDescontoBase() {
        Cliente cliente = new Cliente(true, false, 1);

        assertEquals(1_000L, politica.calcular(cliente, 10_000, "   "));
    }

    @Test
    void cupomBemVindoSomaVinteReaisParaClienteNovo() {
        Cliente cliente = new Cliente(false, false, 0);

        assertEquals(2_000L, politica.calcular(cliente, 10_000, "bemvindo"));
    }

    @Test
    void cupomBemVindoNaoSomaParaClienteComHistorico() {
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(0L, politica.calcular(cliente, 10_000, "BEMVINDO"));
    }

    @Test
    void cupomBemVindoNaoSomaAbaixoDoSubtotalMinimo() {
        Cliente cliente = new Cliente(false, false, 0);

        assertEquals(0L, politica.calcular(cliente, 9_999, "BEMVINDO"));
    }

    @Test
    void cupomExtra10SomaDezPorCentoNoLimiteMinimo() {
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(2_000L, politica.calcular(cliente, 20_000, " extra10 "));
    }

    @Test
    void cupomExtra10NaoSomaAbaixoDoLimiteMinimo() {
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(0L, politica.calcular(cliente, 19_999, "EXTRA10"));
    }

    @Test
    void cupomDesconhecidoLancaExcecao() {
        Cliente cliente = new Cliente(false, false, 1);

        assertThrows(IllegalArgumentException.class,
            () -> politica.calcular(cliente, 10_000, "INVALIDO"));
    }

    @Test
    void descontoCombinadoRespeitaTetoDeVintePorCento() {
        Cliente cliente = new Cliente(true, false, 0);

        assertEquals(2_000L, politica.calcular(cliente, 10_000, "BEMVINDO"));
    }
}
