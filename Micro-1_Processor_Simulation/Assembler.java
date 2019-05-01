import java.util.HashMap;

/**
 * Assembler
 */
// Kevin==============================================

public class Assembler {

    /**
     * All the instructions in an array. The index corresponds to instruction
     * number(p)
     */
    public static String[] instructions = { "halt", "load", "loadc", "store", "add", "mul", "sub", "div", "and", "or",
            "not", "lshift", "rshift", "bwc", "bwd", "if" };

    /**
     * Stores the address for each label string
     */
    private HashMap<String, Integer> labels = new HashMap<>();

    /**
     * Translates instruction name and 2 register indicies to machine code
     * 
     * @param instr The instruction name
     * @param a     The first register index
     * @param b     The second register index
     * @return The machine code for the assembly inputted
     * @throws Exception The instruction name was not found
     */
    public int translate(String instr, int a, int b) throws Exception {
        for (int i = 0; i < instructions.length; i++) {
            if (instr.equals(instructions[i])) {
                return Integer.parseInt(Integer.toHexString(i) + Integer.toHexString(a) + Integer.toHexString(b), 16);// formatting
            }
        }
        throw new Exception("Uknown assembly command: " + instr);
    }

    /**
     * Add new label name with line number
     * 
     * @param label The name of the label
     * @param line  The line code for the label
     * @throws Exception No label was given
     */
    public void insertLabel(String label, int line) throws Exception {
        if (label.length() == 0) {
            throw new Exception("Empty label");
        }
        labels.put(label, line);
    }

    /**
     * Retrieve the line number for a label
     * 
     * @param label The name of the label
     * @return The line number for the label
     * @throws Exception Unknown label
     */
    public int getLine(String label) throws Exception {
        if (!labels.containsKey(label)) {
            throw new Exception("Unknown label: '" + label + "'");
        }
        return labels.get(label);
    }
}
// ==============================================
