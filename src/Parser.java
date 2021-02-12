import java.util.Scanner;
import java.util.Stack;

/**
 * An implementation of <i>Recursive descent parser.</i><br>
 * Productions are:
 * 
 * <pre>
 * S -> aAB | bBA
 * A -> bC | a
 * B -> ccSbc | epsilon
 * C -> AA
 * </pre>
 * 
 * Terminal signs <i>(a,b,c)</i> are entered through {@link System#in}<br>
 * If input sequence is parsable, <i>'DA'</i> is printed, otherwise
 * <i>'NE'.</i><br>
 * If input sequence is not parsable, parser should stop as soon as possible.
 * 
 * @author dbrcina
 *
 */
public class Parser {

	/**
	 * Underlaying stack used for storing terminal signs.
	 */
	private static Stack<String> stack = new Stack<>();

	/**
	 * Main entry of this program.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try (Scanner sc = new Scanner(System.in)) {
			String[] input = sc.nextLine().split("");
			for (int i = input.length - 1; i > -1; i--) {
				stack.push(input[i]);
			}
			System.out.println("\n" + (productionS() && stack.isEmpty() ? "DA" : "NE"));
		}
	}

	/**
	 * An implementation of <i>S - production</i>.
	 * 
	 * @return <code>true</code> if input sequence is parsable, otherwise
	 *         <code>false</.
	 */
	private static boolean productionS() {
		System.out.print("S");
		if (stack.isEmpty()) {
			return false;
		}
		String sign = stack.pop();
		if (sign.equals("a")) {
			return productionA() ? productionB() : false;
		} else if (sign.equals("b")) {
			return productionB() ? productionA() : false;
		}
		return false;
	}

	/**
	 * An implementation of <i>A - production</i>.
	 * 
	 * @return <code>true</code> if input sequence is parsable, otherwise
	 *         <code>false</.
	 */
	private static boolean productionA() {
		System.out.print("A");
		if (stack.empty()) {
			return false;
		}
		String sign = stack.peek();
		if (sign.equals("a")) {
			stack.pop();
			return true;
		} else if (sign.equals("b")) {
			stack.pop();
			return productionC();
		}
		return false;
	}

	/**
	 * An implementation of <i>B - production</i>.
	 * 
	 * @return <code>true</code> if input sequence is parsable, otherwise
	 *         <code>false</.
	 */
	private static boolean productionB() {
		System.out.print("B");

		if (stack.size() < 2) {
			return true;
		}

		if (!stack.peek().equals("c")) {
			return true;
		}
		stack.pop();

		if (!stack.peek().equals("c")) {
			return false;
		}
		stack.pop();

		if (!(productionS() && stack.peek().equals("b"))) {
			return false;
		}
		stack.pop();

		if (!stack.peek().equals("c")) {
			return false;
		}
		stack.pop();

		return true;
	}

	/**
	 * An implementation of <i>C - production</i>.
	 * 
	 * @return <code>true</code> if input sequence is parsable, otherwise
	 *         <code>false</.
	 */
	private static boolean productionC() {
		System.out.print("C");
		boolean acceptable = productionA();
		return acceptable ? productionA() : false;
	}
}
