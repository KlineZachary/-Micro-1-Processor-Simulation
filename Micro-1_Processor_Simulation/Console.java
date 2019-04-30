import java.util.*;
import java.io.*;

/**
 * Instances of this class represent console user interfaces to a simulated
 * computer equipped with a Micro-1 processor.
 */
public class Console {
	/**
	 * Representation of the keyboard
	 */
	private Scanner kbd = new Scanner(System.in);
	/**
	 * Main memory of the simulated computer
	 */
	private Memory memory;
	/**
	 * Processor of the simulated computer
	 */
	private Processor cpu;

	private Assembler assembler;

	private MyCompiler compiler;

	private boolean hasHalt = false;

	// Zach =============
	/**
	 * Constructs a memory with specified number of cells, and constructs an
	 * associated processor.
	 * 
	 * @param cap the sepcified amount of memory
	 */
	public Console(int cap) {
		memory = new Memory(cap);
		cpu = new Processor();
		cpu.setMemory(memory);
	}

	/**
	 * Constructs a processor and a memory with 256 cells
	 */
	public Console() {
		this(256);
	}

	// =============
	/**
	 * Loads hexadecimal numbers stored in fName into memory starting at address 0.
	 * Resets PC to 0.
	 * 
	 * @param fName the name of a file containing hex numbers
	 */
	public String load(String fName) throws Exception {
		File f = new File(fName);
		Scanner scan = new Scanner(f);
		int address = 0;
		StringBuilder out = new StringBuilder();
		while (scan.hasNext()) {
			String line = scan.next();
			memory.write(address++, Integer.parseInt(line, 16));
			out.append(line).append("\n");
		}
		cpu.setPC(0);
		scan.close();
		return out.toString();
	}

	/**
	 * 
	 * 
	 * @param fName the name of a file containing assembly code
	 */
	// Kevin==============================================

	public String assemble(String fName) throws Exception {
		String current = "";
		StringBuilder out = new StringBuilder();
		try {
			File f = new File(fName);
			Scanner scan = new Scanner(f);
			assembler = new Assembler();
			int address = 0;
			ArrayList<String> tokens = new ArrayList<String>();
			boolean reachHalt = false;
			for (int lineNum = 0; scan.hasNext(); lineNum++) {
				String token = current = scan.next();
				if (token.matches("[0-9a-f]+") && !token.equals("add") && !token.matches("[0-9][0-9a-f]*")) {
					throw new Exception("Hexidecimal constant starting digit is not 0-9");
				}
				out.append(token).append("\n");
				if (reachHalt) {
					if (token.matches("[0-9a-f]+")) {
						tokens.add(token);
					} else {
						throw new Exception("Invalid hexadecimal constant after halt");
					}
				} else if (token.startsWith("label")) {
					assembler.insertLabel(Integer.parseInt(token.substring(5), 16), lineNum--);

				} else {
					tokens.add(token);
					if (token.startsWith("goto") || (token.matches("[0-9a-f]+") && !token.equals("add")))
						continue;
					if (token.equals("halt")) {
						reachHalt = true;
						continue;
					}
					tokens.add(scan.next());
					if (!token.equals("loadc"))
						tokens.add(scan.next());
				}

			}
			for (int i = 0; i < tokens.size();) {
				String instr = current = tokens.get(i++);
				if (instr.startsWith("goto")) {
					memory.write(address++, assembler.getLine(Integer.parseInt(instr.substring(4), 16)));
					continue;
				}
				if (instr.matches("[0-9a-f]+") && !instr.equals("add")) {
					memory.write(address++, Integer.parseInt(instr, 16));
					continue;
				}
				int a = instr.equals("halt") ? 0 : Integer.parseInt(tokens.get(i++));
				int b = instr.matches("halt|loadc") ? 0 : Integer.parseInt(tokens.get(i++));
				current = instr + " " + Integer.toHexString(a) + " " + Integer.toHexString(b);
				// System.out.println(address + ". " + instr + " " + a + " " + b);
				memory.write(address++, assembler.translate(instr, a, b));
				if (address >= memory.getCap()) {
					throw new Exception("Out of Memory");
				}
			}
			cpu.setPC(0);
			scan.close();
		} catch (Exception e) {
			throw new Exception("ASM Error: " + e.getMessage() + " with line '" + current + "'");
		}
		return out.toString();
	}

	public String compile(String fName) throws Exception {
		int lineNumber = 0;
		String line = "";
		File f = new File(fName);
		StringBuilder linesBuilder = new StringBuilder();
		StringBuilder assemblyLines = new StringBuilder();
		try {

			Scanner scan = new Scanner(f);
			compiler = new MyCompiler(memory.getCap());
			while (scan.hasNext()) {
				line = scan.nextLine().replaceAll("\\s+", "");
				if (line.length() > 0) {
					if (!line.endsWith(";"))
						throw new Exception("missing ';'");
					if ((line.matches(".*[0-9a-zA-Z\\[\\]] [0-9a-zA-Z\\[\\]].*")))
						throw new Exception("missing operator");
					linesBuilder.append(line).append("\n");
				}
			}
			scan.close();
			String[] lines = linesBuilder.toString().replace("true", "1").replace("false", "0").replace("\n", "")
					.split(";");
			while (lineNumber < lines.length) {
				line = lines[lineNumber];
				boolean isArrayDeclaration = false;
				String[] parts = compiler.divideStatement(line);
				if (parts != null) {
					String var = parts[0];
					if (compiler.isIndexed(var))
						var = compiler.divideIndexedVariable(var)[0];
					int size = 1;
					if (line.matches(".*\\[\\].*")) {
						if (line.matches("[0-9a-zA-Z]+\\[\\]=.*[a-zA-Z].*")) {
							throw new Exception("Array declaration must include a constant length");
						}
						if (!line.matches("[0-9a-zA-Z]+\\[\\]=[0-9]+")) {
							throw new Exception("Invalid array declaration.");
						}
						isArrayDeclaration = true;
						int arrLen = Integer.parseInt(parts[1]);
						size = arrLen;
					}
					if (!compiler.containsVariable(var)) {
						compiler.insertVariable(var, size);
					}
				}
				if (!isArrayDeclaration) {
					String evaluated = compiler.evaluate(line, 1, 0);
					if (evaluated.length() > 0) {
						assemblyLines.append(evaluated);
					}
				}
				lineNumber++;
			}
			assemblyLines.append("halt");
			compiler.close();

		} catch (Exception e) {
			// e.printStackTrace();
			throw new Exception("Compile Error: " + e.getMessage() + " at line " + lineNumber + ": '" + line + "'");
		}
		// String asmName = "assembled.assm";
		PrintWriter writer = new PrintWriter(changeFileExtension(f, ".asm"));
		writer.println(assemblyLines.toString());
		writer.close();
		return linesBuilder.toString();
	}

	public String changeFileExtension(File file, String newExt) {
		return file.getAbsolutePath().split("\\.")[0] + newExt;

	}

	/**
	 * This prints out the variable and its variable as requested by the user
	 * 
	 * @param var
	 * @return variable.toString() this is the final format of the variable to use
	 *         to print
	 * @throws Exception the file was not compiled so variables do not exist or the
	 *                   variable is not valid
	 */
	public String print(String var) throws Exception {
		if (compiler == null)
			throw new Exception("No file was compiled");
		if (!compiler.containsVariable(var))
			throw new Exception("Variable does not exist");
		StringBuilder variable = new StringBuilder("");
		variable.append(var + "=" + memory.read(compiler.getAddress(var)));
		return variable.toString();
	}

	/**
	 * Prints the specified array and length as given by the user
	 * 
	 * @param var
	 * @param len
	 * @return
	 * @throws Exception
	 */
	public String getArr(String var, int len) throws Exception {
		if (compiler == null)
			throw new Exception("No file was compiled");
		if (!compiler.containsVariable(var))
			throw new Exception("Variable does not exist");
		StringBuilder out = new StringBuilder();
		out.append(var).append("=[");
		if (len > 0)
			out.append(memory.read(compiler.getAddress(var)));
		for (int i = 1; i < len; i++) {
			out.append("," + memory.read(compiler.getAddress(var) + i));
		}
		return out.append("]").toString();

	}

	/**
	 * Prints out all variables
	 * 
	 * @return this is the final format of printing all vars
	 * @throws Exception
	 */
	public String printAll() throws Exception {
		if (compiler == null)
			throw new Exception("no file was compiled");
		if (!compiler.hasVariables())
			throw new Exception("no variables found");
		StringBuilder all = new StringBuilder("");
		Iterator<String> vars = compiler.getAllVariables().iterator();
		while (vars.hasNext()) {
			String var = vars.next();
			all.append(var + "=" + memory.read(compiler.getAddress(var)) + "\n");
		}
		return all.toString();

	}
	// ==============================================

	/**
	 * Displays synopsis of all commands in the console windowrq
	 */
	public void help() {
		System.out.println("load fileName \t loads machine code as hex into memory");
		System.out.println("asm fileName \t loads assembly code into memory");
		System.out.println("cmp fileName \t compiles high level code to assembly and then to memory");
		System.out.println("memory \t\t dumps memory to console");
		System.out.println("registers \t dumps registers to console");
		System.out.println("step N \t\t executes next N instructions or until halt");
		System.out.println("out varName\t displays a compiled variable");
		System.out.println("arr var size\t displays a compiled array");
		System.out.println("allout \t\t displays all compiled variables");
		System.out.println("help \t\t displays this message");
		System.out.println("quit \t\t terminate console");
	}

	public boolean step(int num) throws Exception {
		if (hasHalt)
			return false;
		for (int i = 0; i < num; i++) {
			if (!cpu.step()) {
				hasHalt = true;
				return false;
			}
		}
		return true;
	}

	public Processor getCPU() {
		return cpu;
	}

	public Memory getMemory() {
		return memory;
	}

	public MyCompiler getCompiler() {
		return compiler;
	}

	/**
	 * This is the read-execute-print loop for the console. It perpetually
	 * 
	 * 1) displays a prompt 2) reads a command from the keyboard 3) executes the
	 * command 4) displays the result Commands include quit, help, load (a program
	 * from a file), memory (display contents of memory), registers (display
	 * contents of registers), and step N (execute the next N instructions.
	 */
	public void controlLoop() {
		System.out.println("type \"help\" for commands");
		while (true) {
			try {
				System.out.print("-> ");
				String cmmd = kbd.next();
				if (cmmd.equals("quit")) {
					break;
				} else if (cmmd.equals("help")) {
					help();
				} else if (cmmd.equals("load")) {
					assembler = null;
					compiler = null;
					hasHalt = false;
					load(kbd.next());
					System.out.println("done");
				} else if (cmmd.equals("asm")) {
					compiler = null;
					hasHalt = false;
					assemble(kbd.next());
					System.out.println("Assembled successfully");
					System.out.println("done");
				} else if (cmmd.equals("cmp")) {
					String path = kbd.next();
					hasHalt = false;
					compile(path);
					System.out.println("Compiled successfully");
					assemble(changeFileExtension(new File(path), ".asm"));
					System.out.println("Assembled successfully");
					System.out.println("done");
				} else if (cmmd.equals("memory")) {
					String dump = memory.dump();
					System.out.println(dump);
				} else if (cmmd.equals("registers")) {
					cpu.dump();
				} else if (cmmd.equals("step")) {
					if (!kbd.hasNextInt()) {
						kbd.next();
						throw new Exception("invalid number of steps");
					}
					if (!step(kbd.nextInt())) {
						System.out.println("program terminated");
					} else {
						System.out.println("done");
					}
				} else if (cmmd.equals("out")) {
					System.out.println(print(kbd.next()));
				} else if (cmmd.equals("arr")) {
					String var = kbd.next();
					if (!kbd.hasNextInt()) {
						System.out.println("not a valid length");
						kbd.next();
					} else {
						System.out.println(getArr(var, kbd.nextInt()));
					}
				} else if (cmmd.equals("allout")) {
					System.out.println(printAll());
				} else {
					System.out.println("unrecognized command: " + cmmd);
					if (kbd.hasNext())
						kbd.nextLine();
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		System.out.println("Bye! Drink tea, take a warm shower, don't stress, and get a good night's sleep.");
	}

	public static void main(String[] args) {
		Console console = new Console(1024);
		console.controlLoop();
	}

}