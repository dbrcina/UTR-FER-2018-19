import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Program that simulates minimization of DKA.
 * 
 * @author dbrcina
 *
 */
public class MinDka {

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
	 * Map that stores all possible transitions for this DKA. Key value is current
	 * state and map value is another instance of {@link Map} whose key value is
	 * transition symbol and map value is next transition state.
	 */
	private static Map<String, TreeMap<String, String>> transitions;

	/**
	 * Initialization static block.
	 */
	static {
		allStates = new TreeSet<>();
		symbols = new TreeSet<>();
		acceptableStates = new TreeSet<>();
		transitions = new TreeMap<>();
	}

	/**
	 * Constant used for separating symbols inside text.
	 */
	private static final String SYMBOL_SEPARATOR = ",";

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
		try {
			initializeDKA();
		} catch (Exception e) {
			e.printStackTrace();
		}
		removeUnreachableStates();
		removeIdenticalStates();
		generateOutput().forEach(System.out::println);
		
//		try (PrintWriter pwr = new PrintWriter(Files.newOutputStream(Paths.get("test.txt")))) {
//			generateOutput().forEach(s -> pwr.println(s));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * Method used for initialization of automat's properties, reading from
	 * {@link System#in} etc.
	 */
	private static void initializeDKA() {
		try (Scanner sc = new Scanner(System.in)) {

			// read first line..input all states
			String line = sc.nextLine().trim();
			String[] parts = line.split(SYMBOL_SEPARATOR);
			for (String part : parts) {
				allStates.add(part);
			}

			// read second line..input all symbols
			line = sc.nextLine().trim();
			parts = line.split(SYMBOL_SEPARATOR);
			for (String part : parts) {
				symbols.add(part);
			}

			// read third line..acceptable states
			line = sc.nextLine().trim();
			parts = line.split(SYMBOL_SEPARATOR);
			for (String part : parts) {
				acceptableStates.add(part);
			}

			// read fourth line..initial state
			initialState = sc.nextLine().trim();

			///////////////////////////////////////////////////

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

				TreeMap<String, String> transitionMap = transitions.get(currentState);
				if (transitionMap == null) {
					transitionMap = new TreeMap<>();
				}

				transitionMap.put(symbol, parts[1]);
				transitions.put(currentState, transitionMap);
			}
		}
	}

	/**
	 * Method used for clearing unreachable states in DKA.
	 */
	private static void removeUnreachableStates() {
		Set<String> reachableStates = new HashSet<>();
		reachableStates.add(initialState);

		Set<String> helperSet = new HashSet<>();

		while (!reachableStates.equals(helperSet)) {
			helperSet.addAll(reachableStates);
			for (String reachableState : helperSet) {
				for (Map.Entry<String, TreeMap<String, String>> entry : transitions.entrySet()) {
					if (reachableState.equals(entry.getKey())) {
						entry.getValue().values().stream().forEach(s -> reachableStates.add(s));
					}
				}
			}
		}

		refreshStates(reachableStates);
	}

	/**
	 * Method used for removing all states that are not reachable from this automat.
	 * 
	 * @param reachableStates collection of reachable states.
	 */
	private static void refreshStates(Set<String> reachableStates) {

		// remove unreachable states from all states
		allStates = new TreeSet<>(reachableStates);

		// remove unreachable states from acceptable states
		acceptableStates.removeIf(state -> !reachableStates.contains(state));

		// remove undoable transitions
		Iterator<Map.Entry<String, TreeMap<String, String>>> it = transitions.entrySet().iterator();
		while (it.hasNext()) {
			if (!reachableStates.contains(it.next().getKey())) {
				it.remove();
			}
		}
	}

	/**
	 * Method used for removing identical states.
	 */
	private static void removeIdenticalStates() {
		Set<String> nonIdenticalStates = new TreeSet<>();
		List<String> allStatesList = new ArrayList<>(allStates);

		firstStep(nonIdenticalStates, allStatesList);

		doAlgorithm(nonIdenticalStates, allStatesList);

		finalStep(nonIdenticalStates, allStatesList);
	}

	/**
	 * Helper method used for making first step in this algorithm. Non identical
	 * states are those that are not identical. Identical states are states that are
	 * <b>both</b> acceptable or not.
	 * <p>
	 * All non identical states are stored in <code>nonIdenticalStates</code> as one
	 * pair like <code>(qi,qj)</code>.
	 * </p>
	 * 
	 * @param nonIdenticalStates collection of non identical states.
	 * @param allStates          collection of all states.
	 */
	private static void firstStep(Set<String> nonIdenticalStates, List<String> allStatesList) {
		for (int i = 0; i < allStatesList.size(); i++) {
			String qi = allStatesList.get(i);
			for (int j = i + 1; j < allStatesList.size(); j++) {
				String qj = allStatesList.get(j);
				if (acceptableStates.contains(qi) ^ acceptableStates.contains(qj)) {
					nonIdenticalStates.add(qi + SYMBOL_SEPARATOR + qj);
				}
			}
		}
	}

	/**
	 * Method used for executing minimization algorithm.
	 * <p>
	 * Every pair of states from <code>allStates</code> is checked whether it is in
	 * <code>nonIdenticalStates</code>. If pair isn't marked as non identical, then
	 * every symbol transition pair from {@link #transitions} is checked. If atleast
	 * one pair is marked as non identical, then the initial pair is also marked as
	 * non identical and put in <code>nonIdenticalStates</code>.
	 * </p>
	 * 
	 * @param nonIdenticalStates collection of non identical states.
	 * @param allStates          collection of all states.
	 * @see #firstStep(Set, List)
	 */
	private static void doAlgorithm(Set<String> nonIdenticalStates, List<String> allStatesList) {
		Set<String> helperSet = new HashSet<>();
		while (!nonIdenticalStates.equals(helperSet)) {
			helperSet.addAll(nonIdenticalStates);
			for (int i = 0; i < allStatesList.size(); i++) {
				String qi = allStatesList.get(i);
				for (int j = i + 1; j < allStatesList.size(); j++) {
					String qj = allStatesList.get(j);
					if (!nonIdenticalStates.contains(qi + SYMBOL_SEPARATOR + qj)) {

						// check for every transition
						for (String symbol : symbols) {
							String qis = transitions.get(qi).get(symbol);
							String qjs = transitions.get(qj).get(symbol);
							if (nonIdenticalStates.contains(qis + SYMBOL_SEPARATOR + qjs)
									|| nonIdenticalStates.contains(qjs + SYMBOL_SEPARATOR + qis)) {

								// atleast one is found as non identical
								nonIdenticalStates.add(qi + SYMBOL_SEPARATOR + qj);
								break;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Method used for removing all identical states from remaining states.
	 * <p>
	 * For example: if q1 = q2 = q3, q2 and q3 are replaced with q1 and q2, q3 are
	 * removed from {@link #allStates} and {@link #transitions}.
	 * </p>
	 * 
	 * @param nonIdenticalStates collection of non identical states.
	 * @param allStates          collection of all states.
	 */
	private static void finalStep(Set<String> nonIdenticalStates, List<String> allStatesList) {
		for (int i = 0; i < allStatesList.size(); i++) {
			String qi = allStatesList.get(i);
			for (int j = i + 1; j < allStatesList.size(); j++) {
				String qj = allStatesList.get(j);
				if (!nonIdenticalStates.contains(qi + SYMBOL_SEPARATOR + qj)) {
					allStates.remove(qj);
					acceptableStates.remove(qj);
					transitions.remove(qj);

					// change initial state if neccessary
					if (initialState.equals(qj)) {
						initialState = qi;
					}

					// refresh every transition state qj with qi
					for (Map.Entry<String, TreeMap<String, String>> entry : transitions.entrySet()) {
						TreeMap<String, String> map = entry.getValue();
						map.keySet().forEach(key -> {
							if (map.get(key).equals(qj)) {
								map.put(key, qi);
							}
						});
					}
				}
			}
		}
	}

	/**
	 * Generates {@link String} representation of minimized DKA.
	 * 
	 * @return list of strings representing each row of DKA.
	 */
	private static List<String> generateOutput() {
		List<String> output = new ArrayList<>();
		output.add(generate(allStates));
		output.add(generate(symbols));
		output.add(generate(acceptableStates));
		output.add(initialState);

		for (Map.Entry<String, TreeMap<String, String>> entry : transitions.entrySet()) {
			for (Map.Entry<String, String> transitionState : entry.getValue().entrySet()) {
				output.add(entry.getKey() + SYMBOL_SEPARATOR + transitionState.getKey() + TRANSITION_SEPARATOR
						+ transitionState.getValue());
			}
		}

		return output;
	}

	/**
	 * Generates {@link String} representation of provided collection
	 * <code>col</code>.
	 * 
	 * @param col collection.
	 * @return {@link String} representation of {@code col}.
	 */
	private static String generate(Collection<String> col) {
		StringBuilder sb = new StringBuilder();
		int size = col.size();
		for (String s : col) {
			sb.append(s);
			if (size != 1) {
				sb.append(SYMBOL_SEPARATOR);
			}
			size--;
		}
		return sb.toString();
	}
}