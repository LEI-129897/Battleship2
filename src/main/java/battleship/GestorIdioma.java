package battleship;

import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.util.ULocale;

import java.util.HashMap;
import java.util.Map;

public class GestorIdioma {

    private static ULocale idioma = ULocale.forLanguageTag("pt");

    private static final Map<String, String> mensagensPT = new HashMap<>();
    private static final Map<String, String> mensagensEN = new HashMap<>();

    static {
        // Português
        mensagensPT.put("cmdAjuda", "ajuda");
        mensagensPT.put("cmdGeraFrota", "gerafrota");
        mensagensPT.put("cmdLeFrota", "lefrota");
        mensagensPT.put("cmdDesistir", "desisto");
        mensagensPT.put("cmdRajada", "rajada");
        mensagensPT.put("cmdTiros", "tiros");
        mensagensPT.put("cmdMapa", "mapa");
        mensagensPT.put("cmdEstado", "estado");
        mensagensPT.put("cmdSimula", "simula");
        mensagensPT.put("despedida", "Bons ventos!");
        mensagensPT.put("comandoInvalido", "Que comando é esse??? Repete ...");
        mensagensPT.put("ajudaTitulo", "======================= AJUDA DO MENU =========================");
        mensagensPT.put("ajudaIntroducao", "Digite um dos comandos abaixo para interagir com o jogo:");
        mensagensPT.put("geraFrota", "Gera uma frota aleatória de navios.");
        mensagensPT.put("leFrota", "Permite criar e carregar uma frota personalizada.");
        mensagensPT.put("estado", "Mostra o estado atual da frota.");
        mensagensPT.put("mapa", "Exibe o mapa da frota.");
        mensagensPT.put("rajada", "Realiza uma rajada de disparos.");
        mensagensPT.put("simula", "Simula um jogo completo.");
        mensagensPT.put("tiros", "Lista os tiros válidos realizados (* = tiro em navio, o = tiro na água).");
        mensagensPT.put("desistir", "Encerra o jogo.");
        mensagensPT.put("ajudaFim", "===============================================================");
        mensagensPT.put("legenda", "LEGENDA");
        mensagensPT.put("navio", "navio");
        mensagensPT.put("adjacenteNavio", "adjacente a navio");
        mensagensPT.put("agua", "água");
        mensagensPT.put("tiroCerteiro", "tiro certeiro");
        mensagensPT.put("tiroAgua", "tiro na água");
        mensagensPT.put("posicaoIncompleta", "Posição incompleta! A coluna");
        mensagensPT.put("numeroPosicoes", "Deve inserir exatamente {0} posições!");
        mensagensPT.put("numeroTiros", "Deve disparar exatamente {0} tiros por jogada.");
        mensagensPT.put("fimJogo", "Maldito sejas, Java Sparrow, eu voltarei!");

        // Inglês
        mensagensEN.put("cmdAjuda", "help");
        mensagensEN.put("cmdGeraFrota", "randomfleet");
        mensagensEN.put("cmdLeFrota", "loadfleet");
        mensagensEN.put("cmdDesistir", "quit");
        mensagensEN.put("cmdRajada", "fire");
        mensagensEN.put("cmdTiros", "shots");
        mensagensEN.put("cmdMapa", "map");
        mensagensEN.put("cmdEstado", "status");
        mensagensEN.put("cmdSimula", "simulate");
        mensagensEN.put("despedida", "Fair winds!");
        mensagensEN.put("comandoInvalido", "What kind of command is that??? Try again ...");
        mensagensEN.put("ajudaTitulo", "======================= MENU HELP =========================");
        mensagensEN.put("ajudaIntroducao", "Enter one of the following commands to interact with the game:");
        mensagensEN.put("geraFrota", "Generates a random fleet of ships.");
        mensagensEN.put("leFrota", "Allows you to create and load a custom fleet.");
        mensagensEN.put("estado", "Shows the current fleet status.");
        mensagensEN.put("mapa", "Displays the fleet map.");
        mensagensEN.put("rajada", "Performs a burst of shots.");
        mensagensEN.put("simula", "Simulates a complete game.");
        mensagensEN.put("tiros", "Lists valid shots (* = hit a ship, o = hit water).");
        mensagensEN.put("desistir", "Ends the game.");
        mensagensEN.put("ajudaFim", "===============================================================");
        mensagensEN.put("legenda", "LEGEND");
        mensagensEN.put("navio", "ship");
        mensagensEN.put("adjacenteNavio", "adjacent to a ship");
        mensagensEN.put("agua", "water");
        mensagensEN.put("tiroCerteiro", "hit");
        mensagensEN.put("tiroAgua", "miss");
        mensagensEN.put("posicaoIncompleta", "Incomplete position! Column");
        mensagensEN.put("numeroPosicoes", "You must enter exactly {0} positions!");
        mensagensEN.put("numeroTiros", "You must fire exactly {0} shots per move.");
        mensagensEN.put("fimJogo", "Damn you, Java Sparrow, I will return!");
    }

    public static void setIdioma(String codigoIdioma) {
        idioma = ULocale.forLanguageTag(codigoIdioma);
    }

    public static String getMensagem(String chave) {

        Map<String, String> mensagens;

        if (idioma.getLanguage().equals("en")) {
            mensagens = mensagensEN;
        } else {
            mensagens = mensagensPT;
        }

        return mensagens.getOrDefault(chave, chave);
    }

    public static String getMensagemFormatada(
            String chave, Object... parametros) {

        String mensagem = getMensagem(chave);

        MessageFormat formato = new MessageFormat(mensagem, idioma);

        return formato.format(parametros);
    }
}