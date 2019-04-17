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

	/**
	 * Loads hexadecimal numbers stored in fName into memory starting at address 0.
	 * Resets PC to 0.
	 * 
	 * @param fName the name of a file containing hex numbers
	 */
	public void load(String fName) throws Exception {

		File f = new File(fName);
		Scanner scan = new Scanner(f);
		int address = 0;
		while (scan.hasNext()) {
			memory.write(address++, scan.nextInt(16));
		}
		cpu.setPC(0);
		scan.close();

	}

	/**
	 * 
	 * 
	 * @param fName the name of a file containing assembly code
	 */
	// Kevin==============================================

	public void assemble(String fName) throws Exception {
		String current = "";
		try {
			File f = new File(fName);
			Scanner scan = new Scanner(f);
			assembler = new Assembler();
			int address = 0;
			ArrayList<String> tokens = new ArrayList<String>();
			for (int lineNum = 0; scan.hasNext(); lineNum++) {
				String token = current = scan.next();
				if (token.startsWith("label")) {
					assembler.insertLabel(Integer.parseInt(token.substring(5), 16), lineNum--);

				} else {
					tokens.add(token);

					if (token.startsWith("goto")) {
						continue;
					}
					if (token.matches("[0-9a-f]+") && !token.equals("add")) {
						continue;
					}
					if (!token.equals("halt"))
						tokens.add(scan.next());
					if (!token.matches("halt|loadc"))
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
			System.out.println("Assembled successfully");
		} catch (Exception e) {
			throw new Exception("ASM Error: " + e.getMessage() + " with line '" + current + "'");
		}
	}

	public void compile(String fName) throws Exception {
		int lineNumber = 0;
		String line = "";
		File f = new File(fName);
		StringBuilder b = new StringBuilder();

		try {
			Scanner scan = new Scanner(f);
			compiler = new MyCompiler(memory.getCap());
			while (scan.hasNext()) {
				b.append(scan.nextLine());
			}
			scan.close();

			String[] lines = b.toString().replaceAll("\\s+", "").replace("true", "1").replace("false", "0").split(";");

			b = new StringBuilder();
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
						b.append(evaluated);
					}
				}
				lineNumber++;
			}
			b.append("halt");
			System.out.println("Compiled successfully");

		} catch (Exception e) {
			throw new Exception("Compile Error: " + e.getMessage() + " at line " + lineNumber + ": '" + line + "'");
		}
		String asmName = f.getAbsolutePath().split("\\.")[0] + ".asm";
		// String asmName = "assembled.assm";
		PrintWriter writer = new PrintWriter(asmName);
		writer.println(b.toString());
		writer.close();
		assemble(asmName);
	}

	public void print(String var) throws Exception {
		if (compiler == null) {
			System.out.println("No file was compiled");
		} else if (compiler.containsVariable(var)) {
			System.out.println(var + "=" + memory.read(compiler.getVariable(var)));
		} else {
			System.out.println("Variable does not exist");
		}
	}

	public String getArr(String var, int len) throws Exception {
		if (compiler == null)
			throw new Exception("No file was compiled");

		if (!compiler.containsVariable(var)) {
			throw new Exception("Variable does not exist");
		}
		StringBuilder out = new StringBuilder();
		out.append(var).append("=[");
		if (len > 0)
			out.append(memory.read(compiler.getVariable(var)));
		for (int i = 1; i < len; i++) {
			out.append("," + memory.read(compiler.getVariable(var) + i));
		}
		return out.append("]").toString();

	}

	public void printAll() throws Exception {
		if (compiler == null) {
			System.out.println("No file was compiled");
		} else {
			Iterator<String> vars = compiler.getAllVariables().iterator();
			while (vars.hasNext()) {
				String var = vars.next();
				System.out.println(var + "=" + memory.read(compiler.getVariable(var)));
			}
		}
	}
	// ==============================================

	/**
	 * Displays synopsis of all commands in the console window
	 */
	public void help() {
		System.out.println("load fileName \t loads hex memory image into memory");
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

	public boolean step() throws Exception {
		int num;
		if (!kbd.hasNextInt()) {
			num = 0;
			kbd.nextLine();
			System.out.println("invalid number of steps");
		} else {
			num = kbd.nextInt();
			boolean halt = false;
			for (int i = 0; i < num && !halt; i++) {
				if (!halt) {
					halt = cpu.step();
				}

				if (halt) {
					System.out.println("program terminated at step " + (i + 1));
					return false;
				}
			}
			System.out.println("done");
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
					load(kbd.next());
					System.out.println("done");
				} else if (cmmd.equals("asm")) {
					assemble(kbd.next());
					System.out.println("done");
				} else if (cmmd.equals("cmp")) {
					compile(kbd.next());
					System.out.println("done");
				} else if (cmmd.equals("memory")) {
					String dump = memory.dump();
					System.out.println(dump);
				} else if (cmmd.equals("registers")) {
					cpu.dump();
				} else if (cmmd.equals("step")) {
					if (!step()) {
						// break;
					}
				} else if (cmmd.equals("out")) {
					print(kbd.next());
				} else if (cmmd.equals("arr")) {
					String var = kbd.next();
					if (!kbd.hasNextInt()) {
						System.out.println("not a valid length");
						kbd.nextLine();
					} else {
						System.out.println(getArr(var, kbd.nextInt()));
					}
				} else if (cmmd.equals("allout")) {
					printAll();
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

	/**
	 * Creates a console (with memory and CPU), then starts the console's control
	 * loop.
	 */
	public static void main(String[] args) {
		Console console = new Console(1024);
		console.controlLoop();
	}

}