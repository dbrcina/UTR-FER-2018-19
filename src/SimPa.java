import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

/**
 * Program that simulates non-deterministic pushdown automata.
 * 
 * @author dbrcina.
 *
 */
public class SimPa {

	/**
	 * Constant used for separating input text.
	 */
	private static final String TEXT_SEPARATOR = "|";

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
	private static final String STACK_SEPARATOR = "#";

	/**
	 * Constant representing separator between transitions.
	 */
	private static final String TRANSITION_SEPARATOR = "->";

	/**
	 * Constant representing automat failure.
	 */
	private static final String FAIL = "fail|";
	
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
	 * Stack symbols.
	 */
	private static Set<String> stackSymbols;

	/**
	 * Acceptable states by this machine.
	 */
	private static Set<String> acceptableStates;

	/**
	 * Initial state.
	 */
	private static String initialState;

	/**
	 * Initial stack state;
	 */
	private static String initialStackState;

	/**
	 * Map where all transitions are registered. Key values are left side of
	 * expression and map values are the right side of expression.
	 */
	private static Map<String, String> transitions;

	/**
	 * Underlaying stack.
	 */
	private static Stack<String> stack;

	/**
	 * Current state.
	 */
	private static String currentState;
	
	/**
	 * Current stack state.
	 */
	private static String currentStackState;
	
	/**
	 * Static initialization block.
	 */
	static {
		inputText = new ArrayList<>();
		allStates = new TreeSet<>();
		symbols = new TreeSet<>();
		stackSymbols = new TreeSet<>();
		acceptableStates = new TreeSet<>();
		transitions = new HashMap<>();
		stack = new Stack<>();
	}

	/**
	 * Main entry of this program.
	 * 
	 * @param args args given through command line.
	 */
	public static void main(String[] args) {
		
		try {
			initalizePA();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		simulation();
	}

	/**
	 * Helper method used for initialization of pushdown automata.
	 */
	private static void initalizePA() {
		try (Scanner sc = new Scanner(System.in)) {

			// read 1. line..input text
			String line = sc.nextLine().trim();
			String[] parts = line.split("\\" + TEXT_SEPARATOR);
			inputText.addAll(Arrays.asList(parts));

			// read 2. line..input all states
			line = sc.nextLine().trim();
			parts = line.split(SYMBOL_SEPARATOR);
			allStates.addAll(Arrays.asList(parts));

			// read 3. line..all symbols
			line = sc.nextLine().trim();
			parts = line.split(SYMBOL_SEPARATOR);
			symbols.addAll(Arrays.asList(parts));

			// read 4. line..all stack symbols
			line = sc.nextLine().trim();
			parts = line.split(SYMBOL_SEPARATOR);
			stackSymbols.addAll(Arrays.asList(parts));

			// read 5. line..acceptable states
			line = sc.nextLine().trim();
			parts = line.split(SYMBOL_SEPARATOR);
			acceptableStates.addAll(Arrays.asList(parts));

			// read 6. and 7. lines...initial states
			initialState = sc.nextLine().trim();
			initialStackState = sc.nextLine().trim();

			// read from 8. line...transitions
			while (sc.hasNextLine()) {
				line = sc.nextLine().trim();
				
				if (line.isEmpty()) {
					break;
				}
				// split by -> sign
				parts = line.split(TRANSITION_SEPARATOR);
				transitions.put(parts[0], parts[1]);
			}
		}
	}

	/**
	 * Entry point of PA simulation. All results from simulation are print onto
	 * {@link System#out}.
	 */
	private static void simulation() {
		for (String sequence : inputText) {
			simulationResults(sequence.split(SYMBOL_SEPARATOR)).forEach(System.out::print);
			System.out.println();
		}
	}
	
	/**
	 * Calculates and generates simulation results.
	 * 
	 * @param sequence input sequence.
	 * @return list of results.
	 */
	private static List<String> simulationResults(String[] sequence) {
		List<String> results = new ArrayList<>();
		results.add(initialState + STACK_SEPARATOR + initialStackState + TEXT_SEPARATOR);

		currentState = initialState;
		currentStackState = initialStackState;
		
		int i = 0;
		boolean failed = false;
		boolean acceptable = false;

		while (i < sequence.length) {
			String transition = currentState 
					+ SYMBOL_SEPARATOR
					+ sequence[i] 
					+ SYMBOL_SEPARATOR
					+ currentStackState;
			String epsilonTransition = currentState 
					+ SYMBOL_SEPARATOR
					+ EPSILON
					+ SYMBOL_SEPARATOR
					+ currentStackState;
			
			String postTransition = transitions.get(transition);
			String postEpsilonTransition = transitions.get(epsilonTransition);
			
			if (postTransition != null) {
				updateStackState(postTransition);
				i++;
			} else if (postEpsilonTransition != null) {
				updateStackState(postEpsilonTransition);
			} else {
				results.add(FAIL);
				failed = true;
				break;
			}
			
			currentStackState = stack.peek();
			updateResults(results);
		}
		
		if (acceptableStates.contains(currentState)) {
			acceptable = true;
		}
		
		// check for epsilon transitions
		while (!acceptable) {
			acceptable = true;
			String epsilonTransition = currentState 
					+ SYMBOL_SEPARATOR
					+ EPSILON
					+ SYMBOL_SEPARATOR
					+ currentStackState;
			String postEpsilonTransition = transitions.get(epsilonTransition);
			
			if (postEpsilonTransition != null) {
				updateStackState(postEpsilonTransition);
				currentStackState = stack.peek();
				updateResults(results);
				acceptable = false;
			}
			
			if (acceptableStates.contains(currentState)) {
				acceptable = true;
			}
		}
		
		results.add(acceptableStates.contains(currentState) && !failed ? "1" : "0");
		stack.clear();
		return results;
	}

	/**
	 * Updates stack data as determined by <code>transition</code>.
	 * 
	 * @param transition transition.
	 */
	private static void updateStackState(String transition) {
		if (!stack.isEmpty()) {
			stack.pop();
		}
		
		String[] parts = transition.split(SYMBOL_SEPARATOR);
		currentState = parts[0];
		
		String[] stackSymbols = parts[1].split("");
		
		for (int i = stackSymbols.length - 1; i > -1; i--) {
			stack.push(stackSymbols[i]);
		}
		
		if (stack.peek().equals(EPSILON) && stack.size() != 1) {
			stack.pop();
		}
	
	}
	
	/**
	 * Appends results into list of results <code>results</code>.
	 * 
	 * @param results list of results.
	 */
	private static void updateResults(List<String> results) {
		StringBuilder sb = new StringBuilder();

		if (stack.isEmpty()) {
			sb.append(EPSILON);
		} else {
			String[] stackElems = stack.toArray(new String[stack.size()]);
			for (int i = stackElems.length - 1; i > -1; i--) {
				sb.append(stackElems[i]);
			}
		}
		
		results.add(currentState 
				+ STACK_SEPARATOR 
				+ sb.toString() 
				+ TEXT_SEPARATOR
		);
	}
}
