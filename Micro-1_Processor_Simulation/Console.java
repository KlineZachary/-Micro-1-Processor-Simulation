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
	public void load(String fName) {
		try {
			File f = new File(fName);
			Scanner scan = new Scanner(f);
			int address = 0;
			while (scan.hasNext()) {
				memory.write(address++, scan.nextInt(16));
			}
			cpu.setPC(0);
			scan.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * 
	 * 
	 * @param fName the name of a file containing assembly code
	 */
	// Kevin==============================================

	public void assemble(String fName) {
		try {
			File f = new File(fName);
			Scanner scan = new Scanner(f);
			Assembler assembler = new Assembler();
			int address = 0;
			while (scan.hasNext()) {
				String instr = scan.next();
				if (instr.matches("[0-9a-f]") && !instr.equals("add")) {
					memory.write(address++, scan.nextInt(16));
					continue;
				}
				int a = instr.matches("halt") ? 0 : scan.nextInt(16);
				int b = instr.matches("halt|loadc") ? 0 : scan.nextInt(16);
				System.out.println(address + ". " + instr + " " + a + " " + b);
				memory.write(address++, assembler.translate(instr, a, b));
			}
			cpu.setPC(0);
			scan.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void compile(String fName) {
		try {
			File f = new File(fName);
			Scanner scan = new Scanner(f);
			MyCompiler compiler = new MyCompiler();
			StringBuilder b = new StringBuilder();
			while (scan.hasNext()) {
				b.append(scan.nextLine());
			}
			scan.close();

			String[] lines = b.toString().replace("\\w", "").split(";");

			b = new StringBuilder();
			int stackPointer = memory.getCap();
			for (String line : lines) {
				if (line.contains("=")) {
					String var = line.split("=")[0];
					if (!compiler.containsVariable(var)) {
						compiler.insertVariable(var, --stackPointer);
					}
				}
				b.append(compiler.evaluate(line));
				// b.append("halt");
			}
			System.out.println("Compiled successfully");
			String asmName = f.getAbsolutePath().split("\\.")[0] + ".asm2";
			PrintWriter writer = new PrintWriter(asmName);
			writer.println(b.toString());
			writer.close();
			assemble(asmName);
		} catch (Exception e) {
			System.out.println(e);
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
		System.out.println("help \t\t displays this message");
		System.out.println("quit \t\t terminate console");
	}

	public boolean step() {
		int num;
		if (!kbd.hasNextInt()) {
			num = 0;
			kbd.nextLine();
			System.out.println("invalid number of steps");
		} else {
			num = kbd.nextInt();
			boolean halt = false;
			for (int i = 0; i < num && !halt; i++) {
				if (!halt)
					halt = cpu.step();
				if (halt) {
					System.out.println("program terminated");
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
				memory.dump();
			} else if (cmmd.equals("registers")) {
				cpu.dump();
			} else if (cmmd.equals("step")) {
				if (!step()) {
					// break;
				}
			} else {
				System.out.println("unrecognized command: " + cmmd);
				if (kbd.hasNext())
					kbd.nextLine();
			}
		}
		System.out.println("Bye! Drink tea, take a warm shower, don't stress, and get a good night's sleep.");
	}

	/**
	 * Creates a console (with memory and CPU), then starts the console's control
	 * loop.
	 */
	public static void main(String[] args) {
		Console console = new Console();
		console.controlLoop();
	}

}