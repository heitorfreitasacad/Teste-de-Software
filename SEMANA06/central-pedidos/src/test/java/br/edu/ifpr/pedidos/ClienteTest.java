package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    @Test
    void deveAceitarHistoricoZero() {
        Cliente cliente = new Cliente(false, false, 0);

        assertEquals(0, cliente.comprasAnteriores());
    }

    @Test
    void deveAceitarHistoricoPositivoEExporGettersCorretos() {
        Cliente cliente = new Cliente(true, false, 5);

        assertAll(
            () -> assertTrue(cliente.vip()),
            () -> assertFalse(cliente.bloqueado()),
            () -> assertEquals(5, cliente.comprasAnteriores())
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -10})
    void deveRejeitarHistoricoNegativo(int comprasAnteriores) {
        assertThrows(IllegalArgumentException.class,
            () -> new Cliente(false, false, comprasAnteriores));
    }
}
