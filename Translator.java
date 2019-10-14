import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.HashMap;

/**
 * A class that translates Hack pseudo VM code to functional VM code.
 *
 * @author Johan Holmberg, Malmö University
 */
public class Translator {
	private HashMap<String, Integer> variables = new HashMap<String, Integer>();
	private int nextVariable = 0;
	private final StringBuffer output = new StringBuffer();
	private Stream<String> infile = null;
	private Path outfile = null;

	/**
	 * Runs the program. It takes two arguments:
	 *
	 * - infile: the name of the file containing the pseudo code.
	 * - outfile: the name of the file where the translated code will be written.
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Usage: java Translator infile outfile");
			System.exit(0);
		}

		Translator t = new Translator(args[0], args[1]);
		t.translateAndSave();
	}

	/**
	 * Cretates an instance of this class.
	 *
	 * @param infile The name of the file containing the pseudo code.
	 * @param outfile The name of the file where the translated code will be written.
	 */
	Translator(String infile, String outfile) {
		try {
			this.infile = Files.lines(Paths.get(infile));
			this.outfile = Paths.get(outfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Translates the pseudo code and writes it to the output file.
	 */
	public void translateAndSave() {
		infile.forEach(line -> {
			output.append(translateLine(line));
		});
		writeToFile();
	}

	/**
	 * Translates a single line.
	 *
	 * @param line A string to translate.
	 *
	 * @return A translated line.
	 */
	private String translateLine(String line) {
		if (line.contains("//")) {
			line = line.substring(0, line.indexOf("//"));
		}
		String[] chunks = line.trim().split(" ");
		String translated = null;

		switch (chunks[0]) {
			case "push":
				translated = translatePush(chunks);
				break;
			case "pop":
				translated = translatePop(chunks);
				break;
			case "print":
				if (chunks.length == 1) {
					translated = translatePrint();
				} else {
					translated = translatePrint(chunks[1]);
				}
				break;
			default:
				translated = line;
		}

		return translated + "\n";
	}

	/**
	 * "Translates" a line containing a variable by subsituting the variable
	 * name with its id number. This method assumes an instruction of three
	 * keywords, not substituting the second word.
	 *
	 * @param chunks Three chunks that make up this command.
	 *
	 * @return A translated line.
	 */
	private String translateDefault(String[] chunks) {
		if (isVariable(chunks[2])) {
			return "  " + chunks[0] + " " + chunks[1] + " " + getVarId(chunks[2]);
		} else {
			return "  " + chunks[0] + " " + chunks[1] + " " + chunks[2];
		}
	}

	/**
	 * Translates a print statement by pushing the value/variable to be
	 * printed and then popping it to the first address of the Hack
	 * computer's video memory.
	 *
	 * @param line A string to translate.
	 *
	 * @return A translated line.
	 */
	private String translatePrint(String chunk) {
		String print = "  push ";

		if (isVariable(chunk)) {
			print += "static " + getVarId(chunk) + "\n";
		} else {
			print += "constant " + chunk + "\n";
		}

		print += translatePrint();

		return print;
	}

	/**
	 * Translates a print statement by popping the last value on the stack
	 * to the first address of the Hack computer's video memory.
	 *
	 * @return A translated line.
	 */
	private String translatePrint() {
		return "  pop that 16384";
	}

	/**
	 * Translates a line containing a variable by subsituting the variable
	 * name with its id number and inserting the "static" keyword before
	 * the variable id.
	 *
	 * @param chunks Two chunks that make up this command.
	 *
	 * @return A translated line.
	 */
	private String translateVariableLine(String[] chunks) {
		int varId = getVarId(chunks[1]);

		return "  " + chunks[0] + " static " + varId;
	}

	/**
	 * Translates a push command.
	 *
	 * @param chunks Two or three chunks that make up this command.
	 *
	 * @return A translated line.
	 */
	private String translatePush(String[] chunks) {
		if (chunks.length > 2 &&
			!(chunks[2].equals("") && chunks[2].substring(0,2).equals("//"))) {
			return translateDefault(chunks);
		} else if (isVariable(chunks[1])) {
			return pushVariable(chunks);
		} else {
			return pushConstant(chunks);
		}
	}

	/**
	 * Translates a pop command.
	 *
	 * @param chunks Two or three chunks that make up this command.
	 *
	 * @return A translated line.
	 */
	private String translatePop(String[] chunks) {
		if (chunks.length > 2 &&
			!(chunks[2].equals("") && chunks[2].substring(0,2).equals("//"))) {
			return translateDefault(chunks);
		} else if (isVariable(chunks[1])) {
			return popVariable(chunks);
		} else {
			return popMemoryCell(chunks);
		}
	}

	/**
	 * Translates a push constant value command.
	 *
	 * @param chunks Two chunks that make up this command.
	 *
	 * @return A translated line.
	 */
	private String pushConstant(String[] chunks) {
		return "  " + chunks[0] + " constant " + chunks[1];
	}

	/**
	 * Translates a push variable value command.
	 *
	 * @param chunks Two chunks that make up this command.
	 *
	 * @return A translated line.
	 */
	private String pushVariable(String[] chunks) {
		return translateVariableLine(chunks);
	}

	/**
	 * Translates a pop value to memory cell command.
	 *
	 * @param chunks Two chunks that make up this command.
	 *
	 * @return A translated line.
	 */
	private String popMemoryCell(String[] chunks) {
		return "  " + chunks[0] + " that " + chunks[1];
	}

	/**
	 * Translates a pop to variable value command.
	 *
	 * @param chunks Two chunks that make up this command.
	 *
	 * @return A translated line.
	 */
	private String popVariable(String[] chunks) {
		return translateVariableLine(chunks);
	}

	/**
	 * Tries to find a variable and return its reference number. If no such
	 * variable exists, it is added to the variable reference table.
	 *
	 * @param variable The name of the variable.
	 *
	 * @return A reference number to a memory cell.
	 */
	private int getVarId(String variable) {
 		Integer index = variables.get(variable);
		if (index == null) {
			variables.put(variable, nextVariable);
			index = nextVariable++;
		}
		return index;
	}

	/**
	 * Tries to guess whether a chunk is a constant or a variable name by
	 * check whether it is an integer or not. If no, then it's a variable.
	 *
	 * @param chunk A string that may be either a constant or a variable.
	 *
	 * @return True if the chunk is a variable.
	 */
	private boolean isVariable(String chunk) {
		try {
			Integer.parseInt(chunk);
			return false;
		} catch (Exception e) {
		}

		return true;
	}

	/**
	 * Writes the translated file to disk.
	 */
	private void writeToFile() {
		try {
			Files.write(outfile, output.toString().getBytes());
		} catch (IOException e) {}
	}
}
