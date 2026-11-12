package br.edu.exemplo;

import br.edu.exemplo.model.Produto;
import br.edu.exemplo.repository.ProdutoRepository;
import br.edu.exemplo.service.ProdutoService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProdutoServiceIntegrationTest {

    @Autowired
    ProdutoService service;

    @Autowired
    ProdutoRepository repository;

    @Autowired
    EntityManager entityManager;

    /**
     * Grava as alterações pendentes no H2 e esvazia o cache do JPA, para que a
     * próxima consulta venha de um SELECT de verdade, e não do objeto em memória.
     */
    private void sincronizarComBanco() {
        repository.flush();
        entityManager.clear();
    }

    // CT01 — Cadastro válido
    @Test
    void deveCadastrarERecuperarProduto() {
        Produto salvo = service.cadastrar("Teclado", 150.0);

        assertNotNull(salvo.getId(), "o produto deveria receber um ID ao ser salvo");

        sincronizarComBanco();
        Produto doBanco = repository.findById(salvo.getId()).orElse(null);

        assertNotNull(doBanco, "o produto deveria estar no banco");
        assertNotSame(salvo, doBanco, "a consulta deveria criar um objeto novo a partir do banco");
        assertEquals("Teclado", doBanco.getNome());
        assertEquals(150.0, doBanco.getPreco(), 0.001);
    }

    // CT02 — Nome inválido
    @Test
    void naoDevePersistirProdutoComNomeEmBranco() {
        long antes = repository.count();

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar("   ", 80.0));

        assertEquals("Nome obrigatório", erro.getMessage());
        assertEquals(antes, repository.count(), "nenhum registro deveria ter sido criado");
    }

    // CT03 — Atualização (desafio)
    @Test
    void deveAtualizarPrecoPersistido() {
        Produto mouse = service.cadastrar("Mouse", 80.0);
        Long id = mouse.getId();

        service.atualizarPreco(id, 95.0);

        sincronizarComBanco();
        Produto doBanco = repository.findById(id).orElseThrow();

        assertEquals("Mouse", doBanco.getNome());
        assertEquals(95.0, doBanco.getPreco(), 0.001);
    }
}
