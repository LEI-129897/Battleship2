package battleship;

import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * The type Tasks.
 */
public class Tasks {
	/**
	 * The constant LOGGER.
	 */
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * The constant GOODBYE_MESSAGE.
	 */
	//private static final String GOODBYE_MESSAGE = "Bons ventos!";

	/**
	 * Strings to be used by the user
	 */

	private static String AJUDA;
	private static String GERAFROTA;
	private static String LEFROTA;
	private static String DESISTIR;
	private static String RAJADA;
	private static String TIROS;
	private static String MAPA;
	private static String STATUS;
	private static String SIMULA;

	public static void languageMenu(){
		Scanner in = new Scanner(System.in);

		System.out.println("Choose your language / Escolha o idioma:");
		System.out.println("1 - Português");
		System.out.println("2 - English");
		System.out.print("> ");

		String escolha = in.next();

		if (escolha.equals("2")) {
			GestorIdioma.setIdioma("en");
		} else {
			GestorIdioma.setIdioma("pt");
		}
	}

	private static void carregarComandos() {
		AJUDA = GestorIdioma.getMensagem("cmdAjuda");
		GERAFROTA = GestorIdioma.getMensagem("cmdGeraFrota");
		LEFROTA = GestorIdioma.getMensagem("cmdLeFrota");
		DESISTIR = GestorIdioma.getMensagem("cmdDesistir");
		RAJADA = GestorIdioma.getMensagem("cmdRajada");
		TIROS = GestorIdioma.getMensagem("cmdTiros");
		MAPA = GestorIdioma.getMensagem("cmdMapa");
		STATUS = GestorIdioma.getMensagem("cmdEstado");
		SIMULA = GestorIdioma.getMensagem("cmdSimula");
	}

	/**
	 * This task also tests the fighting element of a round of three shots
	 */
	public static void menu() {
		languageMenu();
		carregarComandos();

		Scanner in = new Scanner(System.in);


		IFleet myFleet = null;
		IGame game = null;
		menuHelp();

		System.out.print("> ");
		//Scanner in = new Scanner(System.in);
		String command = in.next();
		while (!command.equals(DESISTIR)) {

			if (command.equals(GERAFROTA)) {
				myFleet = Fleet.createRandom();
				game = new Game(myFleet);
				game.printMyBoard(false, true);

			} else if (command.equals(LEFROTA)) {
				myFleet = buildFleet(in);
				game = new Game(myFleet);
				game.printMyBoard(false, true);

			} else if (command.equals(STATUS)) {
				if (myFleet != null) {
					myFleet.printStatus();
				}

			} else if (command.equals(MAPA)) {
				if (myFleet != null) {
					game.printMyBoard(false, true);
				}

			} else if (command.equals(RAJADA)) {
				if (game != null) {
					game.readEnemyFire(in);
					myFleet.printStatus();
					game.printMyBoard(true, false);

					if (game.getRemainingShips() == 0) {
						game.over();
						System.exit(0);
					}
				}

			} else if (command.equals(SIMULA)) {
				if (game != null) {
					while (game.getRemainingShips() > 0) {
						game.randomEnemyFire();
						myFleet.printStatus();
						game.printMyBoard(true, false);

						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}

					if (game.getRemainingShips() == 0) {
						game.over();
						System.exit(0);
					}
				}

			} else if (command.equals(TIROS)) {
				if (game != null) {
					game.printMyBoard(true, true);
				}

			} else if (command.equals(AJUDA)) {
				menuHelp();

			} else {
				System.out.println(GestorIdioma.getMensagem("comandoInvalido"));
			}

			System.out.print("> ");
			command = in.next();
		}
		System.out.println(GestorIdioma.getMensagem("despedida"));
	}

	/**
	 * This function provides help information about the menu commands.
	 */
	public static void menuHelp() {
		System.out.println(GestorIdioma.getMensagem("ajudaTitulo"));
		System.out.println(GestorIdioma.getMensagem("ajudaIntroducao"));

		System.out.println("- " + GERAFROTA + ": "
				+ GestorIdioma.getMensagem("geraFrota"));

		System.out.println("- " + LEFROTA + ": "
				+ GestorIdioma.getMensagem("leFrota"));

		System.out.println("- " + STATUS + ": "
				+ GestorIdioma.getMensagem("estado"));

		System.out.println("- " + MAPA + ": "
				+ GestorIdioma.getMensagem("mapa"));

		System.out.println("- " + RAJADA + ": "
				+ GestorIdioma.getMensagem("rajada"));

		System.out.println("- " + SIMULA + ": "
				+ GestorIdioma.getMensagem("simula"));

		System.out.println("- " + TIROS + ": "
				+ GestorIdioma.getMensagem("tiros"));

		System.out.println("- " + DESISTIR + ": "
				+ GestorIdioma.getMensagem("desistir"));

		System.out.println(GestorIdioma.getMensagem("ajudaFim"));
	}
	/**
	 * This operation allows the build up of a fleet, given user data
	 *
	 * @param in The scanner to read from
	 * @return The fleet that has been built
	 */
	public static Fleet buildFleet(Scanner in) {

		assert in != null;

		Fleet fleet = new Fleet();
		int i = 0; // i represents the total of successfully created ships
		while (i < Fleet.FLEET_SIZE) {
			IShip s = readShip(in);
			if (s != null) {
				boolean success = fleet.addShip(s);
				if (success)
					i++;
				else
					LOGGER.info("Falha na criacao de {} {} {}", s.getCategory(), s.getBearing(), s.getPosition());
			} else {
				LOGGER.info("Navio desconhecido!");
			}
		}
		LOGGER.info("{} navios adicionados com sucesso!", i);
		return fleet;
	}

	/**
	 * This operation reads data about a ship, build it and returns it
	 *
	 * @param in The scanner to read from
	 * @return The created ship based on the data that has been read
	 */
	public static Ship readShip(Scanner in) {

		assert in != null;

		String shipKind = in.next();
		Position pos = readPosition(in);
		char c = in.next().charAt(0);
		Compass bearing = Compass.charToCompass(c);
		return Ship.buildShip(shipKind, bearing, pos);
	}

	/**
	 * This operation allows reading a position in the map
	 *
	 * @param in The scanner to read from
	 * @return The position that has been read
	 */
	public static Position readPosition(Scanner in) {

		assert in != null;

		int row = in.nextInt();
		int column = in.nextInt();
		return new Position(row, column);
	}

	/**
	 * This operation allows reading a position in the map
	 *
	 * @param in The scanner to read from
	 * @return The classic position that has been read
	 */
	public static IPosition readClassicPosition(@NotNull Scanner in) {
		// Verifica se ainda há tokens disponíveis
		if (!in.hasNext()) {
			throw new IllegalArgumentException("Nenhuma posição válida encontrada!");
		}

		String part1 = in.next(); // Primeiro token
		String part2 = null;

		if (in.hasNextInt()) {
			part2 = in.next(); // Segundo token, se disponível
		}

		String input = (part2 != null) ? part1 + part2 : part1;

		// Normalizar o input para tratar letras maiúsculas e minúsculas
		input = input.toUpperCase();

		// Verificar os dois formatos possíveis: compactos e com espaço
		if (input.matches("[A-Z]\\d+")) {
			char column = input.charAt(0); // Extrair a coluna
			int row = Integer.parseInt(input.substring(1)); // Extrair a linha
			return new Position(column, row);
		} else if (part2 != null && part1.matches("[A-Z]") && part2.matches("\\d+")) {
			char column = part1.charAt(0); // Extrair a coluna
			int row = Integer.parseInt(part2); // Extrair a linha
			return new Position(column, row);
		} else {
			throw new IllegalArgumentException("Formato inválido. Use 'A3', 'A 3' ou similar.");
		}
	}

}