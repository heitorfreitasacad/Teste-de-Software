package br.edu.ifpr.pedidos;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PedidoServiceTest {
    @Test
    void deveFecharPedidoDeClienteComumComFreteDoParanaEPagamentoAprovado() {
        // 1. Preparar: cliente comum, uma compra anterior e item disponível de R$ 100,00.
        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido item = new ItemPedido("LIVRO-JAVA", 10_000, 1, 5, 1_000, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);

        // Simula o pagamento e registra as cobranças, sem banco ou serviço externo.
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return true;
        });

        // 2. Executar: percorrer um caminho completo do fechamento.
        ResultadoPedido resultado = service.fechar(pedido, cliente);

        // 3. Verificar: sem desconto; frete de R$ 12,00; total de R$ 112,00.
        assertAll(
            () -> assertEquals("PAGO", resultado.status()),
            () -> assertEquals(10_000L, resultado.subtotalCentavos()),
            () -> assertEquals(0L, resultado.descontoCentavos()),
            () -> assertEquals(1_200L, resultado.freteCentavos()),
            () -> assertEquals(11_200L, resultado.totalCentavos()),
            // A lista comprova uma única cobrança, com o valor correto.
            () -> assertEquals(List.of(11_200L), cobrancas)
        );
    }

    @Test
    void deveRejeitarPedidoNulo() {
        PedidoService service = new PedidoService(total -> true);
        Cliente cliente = new Cliente(false, false, 0);

        assertThrows(NullPointerException.class, () -> service.fechar(null, cliente));
    }

    @Test
    void deveRejeitarClienteNulo() {
        PedidoService service = new PedidoService(total -> true);
        ItemPedido item = new ItemPedido("LIVRO-JAVA", 10_000, 1, 5, 1_000, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);

        assertThrows(NullPointerException.class, () -> service.fechar(pedido, null));
    }

    @Test
    void clienteBloqueadoRetornaBloqueadoSemCobrancaEComValoresZerados() {
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> { cobrancas.add(total); return true; });
        Cliente cliente = new Cliente(false, true, 5);
        ItemPedido item = new ItemPedido("LIVRO-JAVA", 10_000, 1, 5, 1_000, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("BLOQUEADO", resultado.status()),
            () -> assertEquals(0L, resultado.subtotalCentavos()),
            () -> assertEquals(0L, resultado.descontoCentavos()),
            () -> assertEquals(0L, resultado.freteCentavos()),
            () -> assertEquals(0L, resultado.totalCentavos()),
            () -> assertTrue(cobrancas.isEmpty())
        );
    }

    @Test
    void pedidoSemItensAtivosLancaExcecaoDeSubtotalZero() {
        PedidoService service = new PedidoService(total -> true);
        Cliente cliente = new Cliente(false, false, 0);
        ItemPedido itemInativo = new ItemPedido("LIVRO-JAVA", 10_000, 0, 5, 1_000, false);
        Pedido pedido = new Pedido(List.of(itemInativo), "PR", false, null);

        assertThrows(IllegalArgumentException.class, () -> service.fechar(pedido, cliente));
    }

    @Test
    void faltaDeEstoqueRetornaSemEstoqueSemCobrancaEComValoresZerados() {
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> { cobrancas.add(total); return true; });
        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido item = new ItemPedido("LIVRO-JAVA", 10_000, 2, 1, 1_000, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("SEM_ESTOQUE", resultado.status()),
            () -> assertEquals(0L, resultado.subtotalCentavos()),
            () -> assertEquals(0L, resultado.totalCentavos()),
            () -> assertTrue(cobrancas.isEmpty())
        );
    }

    @Test
    void riscoEmRevisaoRetornaValoresCalculadosSemCobranca() {
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> { cobrancas.add(total); return true; });
        Cliente cliente = new Cliente(false, false, 0);
        ItemPedido item = new ItemPedido("NOTEBOOK", 150_000, 1, 5, 500, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("REVISAO", resultado.status()),
            () -> assertEquals(150_000L, resultado.subtotalCentavos()),
            () -> assertEquals(7_500L, resultado.descontoCentavos()),
            () -> assertEquals(0L, resultado.freteCentavos()),
            () -> assertEquals(142_500L, resultado.totalCentavos()),
            () -> assertTrue(cobrancas.isEmpty())
        );
    }

    @Test
    void pagamentoRecusadoRetornaValoresCalculadosComUmaUnicaCobranca() {
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> { cobrancas.add(total); return false; });
        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido item = new ItemPedido("LIVRO-JAVA", 10_000, 1, 5, 500, false);
        Pedido pedido = new Pedido(List.of(item), "SP", false, null);

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("PAGAMENTO_RECUSADO", resultado.status()),
            () -> assertEquals(10_000L, resultado.subtotalCentavos()),
            () -> assertEquals(0L, resultado.descontoCentavos()),
            () -> assertEquals(2_000L, resultado.freteCentavos()),
            () -> assertEquals(12_000L, resultado.totalCentavos()),
            () -> assertEquals(List.of(12_000L), cobrancas)
        );
    }

    @Test
    void cupomDesconhecidoInterrompeFechamentoEProcessadorNaoEChamado() {
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> { cobrancas.add(total); return true; });
        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido item = new ItemPedido("LIVRO-JAVA", 10_000, 1, 5, 500, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, "CUPOM-INVALIDO");

        assertThrows(IllegalArgumentException.class, () -> service.fechar(pedido, cliente));
        assertTrue(cobrancas.isEmpty());
    }
}
