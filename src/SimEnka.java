import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 * Program that simulates epsilon-NFA.
 * 
 * @author dbrcina
 * @version 1.0
 *
 */
public class SimEnka {

	/**
	 * Input sequence.
	 */
	private static List<String> inputText;

	/**
	 * Every possible state.
	 */
	private static Set<String> allStates;

	/**
	 * Alphabet symbols.
	 */
	private static Set<String> symbols;

	/**
	 * Acceptable states by this machine.
	 */
	private static Set<String> acceptableStates;

	/**
	 * Initial state.
	 */
	private static String initialState;

	/**
	 * Map used for defined transitions for every state. Key of this map is
	 * {@link String} value that represents current state and value of this map is
	 * another instance of {@link HashMap}. In this inner map, keys are
	 * {@link String} values which represent symbols used for transitions and values
	 * are also {@link String} values representing next states.
	 */
	private static Map<String, HashMap<String, String>> transitions;

	/**
	 * Initialization static block.
	 */
	static {
		inputText = new ArrayList<>();
		allStates = new TreeSet<>();
		symbols = new TreeSet<>();
		acceptableStates = new TreeSet<>();
		transitions = new HashMap<String, HashMap<String, String>>();
	}

	/**
	 * Constant used for separating input text.
	 */
	private static final String INPUT_TEXT_SEPARATOR = "|";

	/**
	 * Constant used for separating symbols inside text.
	 */
	private static final String SYMBOL_SEPARATOR = ",";

	/**
	 * Constant used for epsilon sign.
	 */
	private static final String EPSILON = "$";

	/**
	 * Constant representing empty state set.
	 */
	private static final String EMPTY_STATE = "#";

	/**
	 * Constant representing separator between transitions.
	 */
	private static final String TRANSITION_SEPARATOR = "->";

	/**
	 * Main entry of this program.
	 * 
	 * @param args arguments given through command line.
	 */
	public static void main(String[] args) {

		initializeEnkaMachine();

		simulation();
	}

	/**
	 * Method used for initialization of automat's properties, reading from
	 * {@link System#in} etc.
	 */
	private static void initializeEnkaMachine() {
		try (Scanner sc = new Scanner(System.in)) {

			// read first line..input text
			String line = sc.nextLine().trim();
			String[] parts = line.split("\\" + INPUT_TEXT_SEPARATOR);
			for (String part : parts) {
				inputText.add(part);
			}

			// read second line..input all states
			line = sc.nextLine().trim();
			parts = line.split(SYMBOL_SEPARATOR);
			for (String part : parts) {
				allStates.add(part);
			}

			// read third line..all symbols
			line = sc.nextLine().trim();
			parts = line.split(SYMBOL_SEPARATOR);
			for (String part : parts) {
				symbols.add(part);
			}

			// read fourth line..acceptable states
			line = sc.nextLine().trim();
			parts = line.split(SYMBOL_SEPARATOR);
			for (String part : parts) {
				acceptableStates.add(part);
			}

			// read fifth line..initial state
			initialState = sc.nextLine().trim();

			///////////////////////////////////////////////////

			// initialize map for transitions
			initializeTransitionMap();

			// read from sixth line...
			while (sc.hasNextLine()) {
				line = sc.nextLine().trim();

				if (line.isEmpty()) {
					break;
				}

				// split by -> sign
				parts = line.split(TRANSITION_SEPARATOR);

				// split left side by , sign
				String[] leftSide = parts[0].split(SYMBOL_SEPARATOR);
				String currentState = leftSide[0];
				String symbol = leftSide[1];

				// update transitions for current state
				HashMap<String, String> helperMap = transitions.get(currentState);
				helperMap.put(symbol, parts[1]);
				transitions.put(currentState, helperMap);
			}
		}
	}

	/**
	 * Helper method used for initialization of {@link #transitions} map. For every
	 * state from {@link #allStates}, next state is set to {@link #EMPTY_STATE} no
	 * matter which symbol from {@link #symbols} is given.
	 */
	private static void initializeTransitionMap() {
		for (String state : allStates) {
			HashMap<String, String> helperMap = new HashMap<>();
			for (String symbol : symbols) {
				helperMap.put(symbol, EMPTY_STATE);
			}
			transitions.put(state, helperMap);
		}
	}

	/**
	 * Entry point of simulation process. It prints result to {@link System#out}.
	 */
	private static void simulation() {
		try {
			for (String text : inputText) {
				String[] parts = text.split(SYMBOL_SEPARATOR);
				List<String> resultStates = calculateResultStates(parts);
				resultStates.forEach(System.out::print);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method used for calculating final result states for every input. Firstly,
	 * epsilon transition is calculated by {@link #epsilonTransition(Set)} method,
	 * then regular transition by {@link #transition(Set, Set, String[], int)}
	 * method and then again epsilon transition.
	 * 
	 * @param inputText input sequence.
	 * @return list of {@link String} values that represent final output.
	 */
	private static List<String> calculateResultStates(String[] inputText) {
		TreeSet<String> currentStates = new TreeSet<>();
		TreeSet<String> nextStates = new TreeSet<>();
		List<String> result = new ArrayList<>();

		currentStates.add(initialState);

		for (int i = 0; i < inputText.length; i++) {
			epsilonTransition(currentStates);

			if (currentStates.contains(EMPTY_STATE)) {
				currentStates.remove(EMPTY_STATE);
			}

			transition(currentStates, nextStates, inputText, i);

			if (nextStates.isEmpty()) {
				nextStates.add(EMPTY_STATE);
			}
			if (nextStates.size() != 1 && nextStates.contains(EMPTY_STATE)) {
				nextStates.remove(EMPTY_STATE);
			}

			epsilonTransition(nextStates);

			generateResult(currentStates, nextStates, result);

			// current states now become next states.
			currentStates = new TreeSet<>(nextStates);
			nextStates.clear();
		}
		// remove the last | sign
		result.remove(result.size() - 1);
		// add \n for new line after everything is printed
		result.add("\n");
		return result;
	}

	/**
	 * Calculates epsilon transition for given states <code>currentStates</code>.
	 * 
	 * @param currentStates currentStates.
	 */
	private static void epsilonTransition(Set<String> currentStates) {
		while (true) {
			Set<String> epsilonTransitions = new TreeSet<>();
			for (String state : currentStates) {
				if (!state.equals(EMPTY_STATE)) {
					epsilonForOneState(state, epsilonTransitions);
				}
			}

			// check whether there is not any epsilon transitions
			if (epsilonTransitions.size() == 0) {
				break;
			}

			int sizeBefore = currentStates.size();

			// add for in current states..duplicates are ignored
			for (String state : epsilonTransitions) {
				currentStates.add(state);
			}

			// check if there were not any modifications
			if (currentStates.size() == sizeBefore) {
				break;
			}
		}
	}

	/**
	 * Calculates epsilon transition for given {@link String} <code>state</code>.
	 * 
	 * @param state              state.
	 * @param epsilonTransitions {@link Set} where result of transitions are stored.
	 */
	private static void epsilonForOneState(String state, Set<String> epsilonTransitions) {
		String tmp = transitions.get(state).get(EPSILON);
		if (tmp == null) {
			return;
		}
		String[] parts = tmp.split(SYMBOL_SEPARATOR);
		for (String part : parts) {
			if (part.equals(EMPTY_STATE)) {
				break;
			}
			epsilonTransitions.add(part);
		}
	}

	/**
	 * Calculates transition for <code>currentStates</code> as determined by
	 * <code>inputText</code> and {@link #transitions} map.
	 * 
	 * @param currentStates currentStates.
	 * @param nextStates    {@link Set} where result of transitions are stored.
	 * @param inputText     an array representing input sequence.
	 * @param index         position in {@code inputText} from where symbol is
	 *                      taken.
	 */
	private static void transition(Set<String> currentStates, Set<String> nextStates, String[] inputText, int index) {
		Object[] array = currentStates.toArray();
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(EMPTY_STATE)) {
				continue;
			}
			String states = transitions.get(array[i]).get(inputText[index]);
			if (states != null) {
				String[] parts = states.split(SYMBOL_SEPARATOR);
				for (String part : parts) {
					nextStates.add(part);
				}
			}
		}
	}

	/**
	 * Helper method used for creating {@link String} representation of whole
	 * process.
	 * 
	 * @param currentStates currentStates
	 * @param nextStates    nextStates
	 * @param result        list that stores {@link String} representation.
	 */
	private static void generateResult(TreeSet<String> currentStates, TreeSet<String> nextStates, List<String> result) {
		int counter = 0;
		boolean added = false;
		for (String state : currentStates) {
			if (!result.contains(state)) {
				result.add(state);
				counter++;
				added = true;
				if (counter < currentStates.size()) {
					result.add(SYMBOL_SEPARATOR);
				}
			}
		}
		if (added) {
			result.add(INPUT_TEXT_SEPARATOR);
		}
		counter = 0;
		for (String state : nextStates) {
			result.add(state);
			counter++;
			if (counter < nextStates.size()) {
				result.add(SYMBOL_SEPARATOR);
			}
		}
		result.add(INPUT_TEXT_SEPARATOR);
	}

}