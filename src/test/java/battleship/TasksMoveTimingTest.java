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
				new InputChunk("gerafrota\n", 0),
				new InputChunk("ajuda\nestado\nmapa\ntiros\ninvalido\n", 150),
				new InputChunk("rajada ", 0),
				new InputChunk("A1 B2 C3\n", 120),
				new InputChunk("rajada A 1 D 4 E 5\n", 30),
				new InputChunk("desisto\n", 0)));

		Tasks.menu();

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
				new InputChunk("gerafrota\n", 0),
				new InputChunk("rajada A1 B2 C3\n", 50),
				new InputChunk("desisto\n", 0)));

		Tasks.menu();

		List<Double> times = moveTimes();
		assertEquals(1, times.size());
		assertTrue(times.getFirst() >= 0.040);
		assertTrue(times.getFirst() < 0.300, "Exclude the 350 ms spent displaying results");
	}

	@Test
	void startsANewMeasurementWhenTheFleetIsReplaced() {
		System.setIn(new ScriptedInput(
				new InputChunk("gerafrota\n", 0),
				new InputChunk("gerafrota\n", 250),
				new InputChunk("rajada A1 B2 C3\n", 20),
				new InputChunk("desisto\n", 0)));

		Tasks.menu();

		List<Double> times = moveTimes();
		assertEquals(1, times.size());
		assertTrue(times.getFirst() < 0.200, "Exclude time spent in the replaced game");
	}

	@Test
	void doesNotReportTimeForCommandsWithoutAMove() {
		System.setIn(new ScriptedInput(new InputChunk(
				"rajada\najuda\ngerafrota\nestado\nmapa\ntiros\ndesisto\n", 0)));

		Tasks.menu();

		assertTrue(moveTimes().isEmpty());
	}

	@Test
	void preservesValidationForAnEmptySubmittedMove() {
		System.setIn(new ScriptedInput(new InputChunk("gerafrota\nrajada\ndesisto\n", 0)));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, Tasks::menu);

		assertEquals("Você deve inserir exatamente 3 posições!", exception.getMessage());
		assertTrue(moveTimes().isEmpty());
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

	private record InputChunk(String text, long delayMillis) {}

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
				bytes = chunk.text().getBytes(StandardCharsets.UTF_8);
				offset = 0;
			}
			return true;
		}
	}
}
