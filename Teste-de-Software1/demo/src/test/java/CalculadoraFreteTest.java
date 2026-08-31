import org.junit.jupiter.api.BeforeEach;

import com.example.CalculadoraFrete;

public class CalculadoraFreteTest {
    
    CalculadoraFrete calculadora;

    @BeforeEach
    void preparar() {
        calculadora = new CalculadoraFrete();
    }

    @Test
    public void deveCobrarFrete() {
        assertEquals(20.0, calculadora.calcular(0, false));
    }

    @Test
    public void naoDeveCobrarFrete() {
        assertEquals(0, calculadora.calcular(200.00, false));
    }
}
