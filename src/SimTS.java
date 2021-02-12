import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * A program which simulates <i>Turing machine</i>.
 * 
 * @author dbrcina
 *
 */
public class SimTS {

	/**
	 * Separator for input text.
	 */
	private static final String INPUT_SEPARATOR = ",";
	/**
	 * Separator for transitions.
	 */
	private static final String TRANSITION_SEPARATOR = "->";
	/**
	 * Separator for output text.
	 */
	private static final String OUTPUT_SEPARATOR = "|";
	/**
	 * Minimum index position.
	 */
	private static final int MIN_POSITION = 0;
	/**
	 * Maximum index position.
	 */
	private static final int MAX_POSITION = 69;
	/**
	 * All states.
	 */
	@SuppressWarnings("unused")
	private static Set<String> states;
	/**
	 * All input symbols.
	 */
	@SuppressWarnings("unused")
	private static Set<String> inputSymbols;
	/**
	 * All tape symbols.
	 */
	@SuppressWarnings("unused")
	private static Set<String> tapeSymbols;
	/**
	 * Empty tape symbol.
	 */
	@SuppressWarnings("unused")
	private static String emptyTapeSymbol;
	/**
	 * An array of tape records.
	 */
	private static String[] tapeRecord;
	/**
	 * Acceptable states.
	 */
	private static Set<String> acceptableStates;
	/**
	 * Initial state.
	 */
	private static String initialState;
	/**
	 * Initial position.
	 */
	private static Integer initialPosition;
	/**
	 * All transitions.
	 */
	private static Map<String, String> transitions;
	/**
	 * Function used for converting an array into TreeSet.
	 */
	private static Function<String[], Set<String>> function = array -> new TreeSet<String>(Arrays.asList(array));

	/**
	 * Main entry of this program.
	 * 
	 * @param args arguments.
	 */
	public static void main(String[] args) {
		try {
			TSInitialization();
		} catch (Exception e) {
			System.out.println("Error occured while initializing TS automata");
			System.exit(-1);
		}
		TSSimulation();
	}

	/**
	 * Initializatio of <i>Turing machine</i>.
	 */
	private static void TSInitialization() {
		try (Scanner sc = new Scanner(System.in)) {

			// all states
			states = function.apply(sc.nextLine().split(INPUT_SEPARATOR));

			// input symbols
			inputSymbols = function.apply(sc.nextLine().split(INPUT_SEPARATOR));

			// track symbols
			tapeSymbols = function.apply(sc.nextLine().split(INPUT_SEPARATOR));

			// empty track symbol
			emptyTapeSymbol = sc.nextLine();

			// tape record
			tapeRecord = sc.nextLine().trim().split("");

			// acceptable states
			acceptableStates = function.apply(sc.nextLine().split(INPUT_SEPARATOR));

			// initial state
			initialState = sc.nextLine();

			// initial position of head
			initialPosition = Integer.parseInt(sc.nextLine());

			initTransitionsMap(sc);
		}

	}

	/**
	 * Initialization of transitions map.
	 * 
	 * @param sc scanner.
	 */
	private static void initTransitionsMap(Scanner sc) {
		transitions = new TreeMap<>();
		while (sc.hasNextLine()) {
			String line = sc.nextLine();

			if (line.isEmpty())
				break;

			String[] parts = line.split(TRANSITION_SEPARATOR);
			transitions.put(parts[0], parts[1]);
		}
	}

	/**
	 * Simulation of <i>Turing machine.</i>
	 */
	private static void TSSimulation() {
		String currentState = initialState;
		int currentPosition = initialPosition;
		String currentTapeSymbol = tapeRecord[currentPosition];
		String transitionResult = transitions.get(currentState + INPUT_SEPARATOR + currentTapeSymbol);

		while (transitionResult != null) {
			String[] resultParts = transitionResult.split(INPUT_SEPARATOR);
			String direction = resultParts[2];

			if (currentPosition == MIN_POSITION && direction.equals("L")
					|| currentPosition == MAX_POSITION && direction.equals("R"))
				break;

			currentState = resultParts[0];
			tapeRecord[currentPosition] = resultParts[1];
			currentPosition = direction.equals("R") ? ++currentPosition : --currentPosition;
			currentTapeSymbol = tapeRecord[currentPosition];
			transitionResult = transitions.get(currentState + INPUT_SEPARATOR + currentTapeSymbol);
		}

		System.out.print(currentState + OUTPUT_SEPARATOR + currentPosition + OUTPUT_SEPARATOR);
		for (String record : tapeRecord) {
			System.out.print(record);
		}
		System.out.print(OUTPUT_SEPARATOR + (acceptableStates.contains(currentState) ? 1 : 0));
	}
}
