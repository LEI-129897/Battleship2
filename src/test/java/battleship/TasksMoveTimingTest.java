package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(5)
class TasksMoveTimingTest {

	private static final Pattern MOVE_TIME = Pattern.compile(
			"Tempo da jogada nº(\\d+): (\\d+\\.\\d{3}) segundos\\.");

	private InputStream originalIn;
	private PrintStream originalOut;
	private Locale originalLocale;
	private ByteArrayOutputStream output;

	@BeforeEach
	void setUp() {
		originalIn = System.in;
		originalOut = System.out;
		originalLocale = Locale.getDefault();
		output = new ByteArrayOutputStream();
		System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
	}

	@AfterEach
	void tearDown() {
		System.setIn(originalIn);
		System.setOut(originalOut);
		Locale.setDefault(originalLocale);
	}

	@Test
	void measuresDecisionAndInputTimeSeparatelyForEachMove() {
		Locale.setDefault(Locale.GERMANY);
		System.setIn(new ScriptedInput(
				new InputChunk("1\ngerafrota\n", 0),
				new InputChunk("ajuda\nestado\nmapa\ntiros\ninvalido\n", 150),
				new InputChunk("rajada ", 0),
				new InputChunk("A1 B2 C3\n", 120),
				new InputChunk("rajada A 1 D 4 E 5\n", 30),
				new InputChunk("desisto\n", 0)));

		runMenu();

		List<Double> times = moveTimes();
		assertEquals(2, times.size());
		assertTrue(times.get(0) >= 0.250, "Include the decision and coordinate entry delays");
		assertTrue(times.get(1) >= 0.020, "Measure the second move's input independently");
		assertTrue(times.get(1) < times.get(0), "Do not accumulate previous move time");
		String console = output.toString(StandardCharsets.UTF_8);
		assertTrue(console.contains("\"repeatedShots\" : 1"), "Keep existing repeated shot detection");
		assertTrue(console.indexOf("Jogada nº1 ->") < console.indexOf("Tempo da jogada nº1:"));
		assertTrue(console.indexOf("Jogada nº2 ->") < console.indexOf("Tempo da jogada nº2:"));
		assertTrue(console.endsWith("Bons ventos!" + System.lineSeparator()));
	}

	@Test
	void stopsAtSubmissionBeforeProcessingAndPrintingResults() {
		System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8) {
			@Override
			public void println(String message) {
				if (message.startsWith("Jogada nº1 ->")) {
					try {
						Thread.sleep(350);
					} catch (InterruptedException exception) {
						Thread.currentThread().interrupt();
						throw new AssertionError(exception);
					}
				}
				super.println(message);
			}
		});
		System.setIn(new ScriptedInput(
				new InputChunk("1\ngerafrota\n", 0),
				new InputChunk("rajada A1 B2 C3\n", 50),
				new InputChunk("desisto\n", 0)));

		runMenu();

		List<Double> times = moveTimes();
		assertEquals(1, times.size());
		assertTrue(times.getFirst() >= 0.040);
		assertTrue(times.getFirst() < 0.300, "Exclude the 350 ms spent displaying results");
	}

	@Test
	void startsANewMeasurementWhenTheFleetIsReplaced() {
		System.setIn(new ScriptedInput(
				new InputChunk("1\ngerafrota\n", 0),
				new InputChunk("gerafrota\n", 250),
				new InputChunk("rajada A1 B2 C3\n", 20),
				new InputChunk("desisto\n", 0)));

		runMenu();

		List<Double> times = moveTimes();
		assertEquals(1, times.size());
		assertTrue(times.getFirst() < 0.200, "Exclude time spent in the replaced game");
	}

	@Test
	void doesNotReportTimeForCommandsWithoutAMove() {
		System.setIn(new ScriptedInput(new InputChunk(
				"1\nrajada\najuda\ngerafrota\nestado\nmapa\ntiros\ndesisto\n", 0)));

		runMenu();

		assertTrue(moveTimes().isEmpty());
	}

	@Test
	void preservesValidationForAnEmptySubmittedMove() {
		System.setIn(new ScriptedInput(new InputChunk("1\ngerafrota\nrajada\ndesisto\n", 0)));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, this::runMenu);

		assertEquals(GestorIdioma.getMensagemFormatada("numeroPosicoes", Game.NUMBER_SHOTS),
				exception.getMessage());
		assertTrue(moveTimes().isEmpty());
	}

	@Test
	void terminalAndGuiMovesShareTheSameHistoryAndTimer() {
		System.setIn(new ScriptedInput(new InputChunk(
				"1\ngerafrota\nrajada D4 E5 F6\ndesisto\n", 0)));
		AtomicReference<GamePlaySession> shared = new AtomicReference<>();
		try (Scanner in = new Scanner(System.in)) {
			Tasks.menu(in, session -> {
				if (shared.compareAndSet(null, session)) {
					GamePlaySession.BoardSnapshot board = session.snapshot();
					session.fire(List.of(new Position('A', 1), new Position('B', 2), new Position('C', 3)),
							board.generation(), board.nextMove());
				}
			});
		}

		assertEquals(2, moveTimes().size());
		assertEquals(3, shared.get().snapshot().nextMove());
		double displayedGuiTime = Double.parseDouble(String.format(Locale.ROOT, "%.3f",
				shared.get().snapshot().lastMove().seconds()));
		assertEquals(displayedGuiTime, moveTimes().getLast(), "GUI and terminal display the same final duration");
	}

	@Test
	void terminalStaysOpenIfGuiFinishesWhileCoordinatesAreBeingEntered() {
		AtomicReference<GamePlaySession> shared = new AtomicReference<>();
		System.setIn(new ScriptedInput(
				new InputChunk("1\ngerafrota\n", 0),
				new InputChunk("rajada ", 0),
				new InputChunk("D4 E5 F6\n", 0, () -> {
					GamePlaySession session = shared.get();
					GamePlaySession.BoardSnapshot board = session.snapshot();
					session.fire(List.of(new Position('A', 1), new Position('B', 2), new Position('C', 3)),
							board.generation(), board.nextMove());
				}),
				new InputChunk("desisto\n", 0)));
		try (Scanner in = new Scanner(System.in)) {
			Tasks.menu(in, session -> {
				if (shared.compareAndSet(null, session)) {
					Fleet fleet = new Fleet();
					fleet.addShip(new Barge(Compass.NORTH, new Position('A', 1)));
					session.setGame(new Game(fleet));
				}
			});
		}

		assertEquals(1, moveTimes().size());
		assertTrue(shared.get().snapshot().finished());
		String console = output.toString(StandardCharsets.UTF_8);
		assertTrue(console.contains(GestorIdioma.getMensagem("guiFinished")));
		assertTrue(console.endsWith("Bons ventos!" + System.lineSeparator()));
	}

	private void runMenu() {
		try (Scanner in = new Scanner(System.in)) {
			Tasks.menu(in, game -> {});
		}
	}

	private List<Double> moveTimes() {
		Matcher matcher = MOVE_TIME.matcher(output.toString(StandardCharsets.UTF_8));
		List<Double> times = new ArrayList<>();
		while (matcher.find()) {
			assertEquals(times.size() + 1, Integer.parseInt(matcher.group(1)));
			times.add(Double.parseDouble(matcher.group(2)));
		}
		return times;
	}

	private record InputChunk(String text, long delayMillis, Runnable beforeRead) {
		InputChunk(String text, long delayMillis) {
			this(text, delayMillis, () -> {});
		}
	}

	/** Supplies input in stages to model a player deciding and then submitting. */
	private static class ScriptedInput extends InputStream {
		private final InputChunk[] chunks;
		private int nextChunk;
		private byte[] bytes = new byte[0];
		private int offset;

		ScriptedInput(InputChunk... chunks) {
			this.chunks = chunks;
		}

		@Override
		public int read() throws IOException {
			return prepareChunk() ? bytes[offset++] & 0xff : -1;
		}

		@Override
		public int read(byte[] buffer, int start, int length) throws IOException {
			if (length == 0) {
				return 0;
			}
			if (!prepareChunk()) {
				return -1;
			}
			int count = Math.min(length, bytes.length - offset);
			System.arraycopy(bytes, offset, buffer, start, count);
			offset += count;
			return count;
		}

		private boolean prepareChunk() throws IOException {
			while (offset == bytes.length) {
				if (nextChunk == chunks.length) {
					return false;
				}
				InputChunk chunk = chunks[nextChunk++];
				try {
					Thread.sleep(chunk.delayMillis());
				} catch (InterruptedException exception) {
					Thread.currentThread().interrupt();
					throw new IOException(exception);
				}
				chunk.beforeRead().run();
				bytes = chunk.text().getBytes(StandardCharsets.UTF_8);
				offset = 0;
			}
			return true;
		}
	}
}
