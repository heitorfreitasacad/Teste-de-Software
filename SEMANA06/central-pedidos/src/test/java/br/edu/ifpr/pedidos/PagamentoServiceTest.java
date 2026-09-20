package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PagamentoServiceTest {

    @Test
    void deveRejeitarTotalNaoPositivo() {
        PagamentoService service = new PagamentoService(total -> true);

        assertThrows(IllegalArgumentException.class, () -> service.pagar(0, 1));
    }

    @Test
    void deveRejeitarLimiteDeTentativasForaDoIntervalo() {
        PagamentoService service = new PagamentoService(total -> true);

        assertThrows(IllegalArgumentException.class, () -> service.pagar(1_000, 0));
        assertThrows(IllegalArgumentException.class, () -> service.pagar(1_000, 4));
    }

    @Test
    void deveAprovarNaPrimeiraTentativa() {
        int[] chamadas = {0};
        PagamentoService service = new PagamentoService(total -> { chamadas[0]++; return true; });

        assertTrue(service.pagar(1_000, 3));
        assertEquals(1, chamadas[0]);
    }

    @Test
    void deveRecusarImediatamenteSemRepetirQuandoProcessadorRetornaFalso() {
        int[] chamadas = {0};
        PagamentoService service = new PagamentoService(total -> { chamadas[0]++; return false; });

        assertFalse(service.pagar(1_000, 3));
        assertEquals(1, chamadas[0]);
    }

    @Test
    void deveRepetirAposIndisponibilidadeEDepoisAprovar() {
        int[] chamadas = {0};
        PagamentoService service = new PagamentoService(total -> {
            chamadas[0]++;
            if (chamadas[0] < 3) throw new IllegalStateException("indisponivel");
            return true;
        });

        assertTrue(service.pagar(1_000, 3));
        assertEquals(3, chamadas[0]);
    }

    @Test
    void deveRetornarFalsoAoEsgotarTentativasComIndisponibilidade() {
        int[] chamadas = {0};
        PagamentoService service = new PagamentoService(total -> {
            chamadas[0]++;
            throw new IllegalStateException("indisponivel");
        });

        assertFalse(service.pagar(1_000, 3));
        assertEquals(3, chamadas[0]);
    }

    @Test
    void devePropagarExcecoesDiferentesDeIndisponibilidade() {
        PagamentoService service = new PagamentoService(total -> {
            throw new RuntimeException("erro inesperado");
        });

        assertThrows(RuntimeException.class, () -> service.pagar(1_000, 3));
    }

    @Test
    void deveRepassarOValorTotalCorretoAoProcessador() {
        long[] recebido = {0};
        PagamentoService service = new PagamentoService(total -> { recebido[0] = total; return true; });

        service.pagar(11_200, 1);

        assertEquals(11_200L, recebido[0]);
    }
}
