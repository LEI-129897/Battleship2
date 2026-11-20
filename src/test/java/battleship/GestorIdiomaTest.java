package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GestorIdiomaTest {

    @Test
    @DisplayName("Deve permitir criar uma instância de GestorIdioma")
    void deveCriarInstancia() {
        assertNotNull(new GestorIdioma());
    }

    @AfterEach
    void reporIdioma() {
        GestorIdioma.setIdioma("pt");
    }

    @Test
    @DisplayName("Deve devolver mensagem em português")
    void deveDevolverMensagemPortugues() {
        GestorIdioma.setIdioma("pt");

        assertEquals("ajuda",
                GestorIdioma.getMensagem("cmdAjuda"));
    }

    @Test
    @DisplayName("Deve devolver mensagem em inglês")
    void deveDevolverMensagemIngles() {
        GestorIdioma.setIdioma("en");

        assertEquals("help",
                GestorIdioma.getMensagem("cmdAjuda"));
    }

    @Test
    @DisplayName("Deve devolver a chave quando não existe")
    void deveDevolverChaveInexistente() {
        GestorIdioma.setIdioma("pt");

        assertEquals("chaveInexistente",
                GestorIdioma.getMensagem("chaveInexistente"));
    }

    @Test
    @DisplayName("Deve formatar mensagem em português")
    void deveFormatarMensagemPortugues() {
        GestorIdioma.setIdioma("pt");

        assertEquals(
                "Deve inserir exatamente 3 posições!",
                GestorIdioma.getMensagemFormatada("numeroPosicoes", 3)
        );
    }

    @Test
    @DisplayName("Deve formatar mensagem em inglês")
    void deveFormatarMensagemIngles() {
        GestorIdioma.setIdioma("en");

        assertEquals(
                "You must enter exactly 3 positions!",
                GestorIdioma.getMensagemFormatada("numeroPosicoes", 3)
        );
    }
}