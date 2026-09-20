package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnaliseRiscoTest {

    private final AnaliseRisco risco = new AnaliseRisco();

    @Test
    void deveRejeitarTotalNegativo() {
        Cliente cliente = new Cliente(false, false, 0);

        assertThrows(IllegalArgumentException.class,
            () -> risco.avaliar(cliente, -1, false));
    }

    @Test
    void clienteBloqueadoERecusadoMesmoComTotalBaixo() {
        Cliente cliente = new Cliente(false, true, 5);

        assertEquals("RECUSADO", risco.avaliar(cliente, 1_000, true));
    }

    @Test
    void clienteNovoComTotalETotalBaixoEEntregaNormalEAprovado() {
        Cliente cliente = new Cliente(false, false, 0);

        assertEquals("APROVADO", risco.avaliar(cliente, 100_000, false));
    }

    @Test
    void clienteNovoComTotalAcimaDoLimiteVaiParaRevisao() {
        Cliente cliente = new Cliente(false, false, 0);

        assertEquals("REVISAO", risco.avaliar(cliente, 100_001, false));
    }

    @Test
    void clienteNovoComEntregaExpressaVaiParaRevisaoMesmoComTotalBaixo() {
        Cliente cliente = new Cliente(false, false, 0);

        assertEquals("REVISAO", risco.avaliar(cliente, 1_000, true));
    }

    @Test
    void clienteComHistoricoETotalAltoNaoVipVaiParaRevisao() {
        Cliente cliente = new Cliente(false, false, 3);

        assertEquals("REVISAO", risco.avaliar(cliente, 500_001, false));
    }

    @Test
    void clienteComHistoricoENoLimiteExatoEAprovado() {
        Cliente cliente = new Cliente(false, false, 3);

        assertEquals("APROVADO", risco.avaliar(cliente, 500_000, false));
    }

    @Test
    void clienteComHistoricoVipComTotalAltoEAprovado() {
        Cliente cliente = new Cliente(true, false, 3);

        assertEquals("APROVADO", risco.avaliar(cliente, 500_001, false));
    }
}
