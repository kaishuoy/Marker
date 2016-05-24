package src;

/**
 * Compares two files and finds the differences between them
 * 
 * Credits to Princeton for original version, minor modifications by Kai
 * http://introcs.cs.princeton.edu/java/96optimization/Diff.java.html
 *
 */

public class Diff {
	static int prevAff = 3; //tracks changes in addition/deletion state

	public static void diff(String[] args) {
		prevAff = 3; 
		//debugging only, program should never actually get here...
		if (args.length == 0){
			args = new String[] { "Castling.java", "Castlinga.java" };
			System.out.println("WARNING:No arguments supplied for diff(), using debug case...");
		}
		// read in lines of each file
		In in0 = new In(args[0]);
		In in1 = new In(args[1]);
		String[] x = in0.readAllLines();
		String[] y = in1.readAllLines();

		// number of lines of each file
		int M = x.length;
		int N = y.length;

		// opt[i][j] = length of LCS of x[i..M] and y[j..N]
		int[][] opt = new int[M + 1][N + 1];

		// compute length of LCS and all subproblems via dynamic programming
		for (int i = M - 1; i >= 0; i--) {
			for (int j = N - 1; j >= 0; j--) {
				if (x[i].equals(y[j]))
					opt[i][j] = opt[i + 1][j + 1] + 1;
				else
					opt[i][j] = Math.max(opt[i + 1][j], opt[i][j + 1]);
			}
		}

		// recover LCS itself and print out non-matching lines to standard
		// output
		int i = 0, j = 0;
		while (i < M && j < N) {
			if (x[i].equals(y[j]) || equalityWithoutBlanks(x[i],y[j])) {
				i++;
				j++;
			} else {
				if (opt[i + 1][j] >= opt[i][j + 1]) {
					if (prevAff != 0) {
						System.out.println("DELETIONS:");
						prevAff = 0;
					}
					System.out.println("< " + x[i++]);
				} else {
					if (prevAff != 1) {
						System.out.println("ADDITIONS:");
						prevAff = 1;
					}
					System.out.println("> " + y[j++]);
				}
			}
		}

		// dump out one remainder of one string if the other is exhausted
		while (i < M || j < N) {
			if (i == M) {
				if (prevAff != 0) {
					System.out.println("DELETIONS:");
					prevAff = 0;
				}
				System.out.println("> " + y[j++]);
			} else if (j == N){
				if (prevAff != 1) {
					System.out.println("ADDITIONS:");
					prevAff = 1;
				}
				System.out.println("< " + x[i++]);
			}
		}
	}

	/**
	 * Checks two strings to see if they are the same after removing tabs,newlines and returns
	 * @param string first string
	 * @param string2 second string
	 * @return whether or not they are equal
	 */
	private static boolean equalityWithoutBlanks(String string, String string2) {
		String input = string.replaceAll("\t|\n|\r", "");
		String input2 = string2.replaceAll("\t|\n|\r", "");
		if(input.equals(input2))return true;
		else return false;
	}

	//debugging purposes
	public static void main(String[] args) {
		diff(new String[] { "testdiff", "testdiff2" });
	}

}
