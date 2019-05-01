import java.util.ArrayList;
import java.util.HashMap;

/**
 * Assembler
 */
// Kevin==============================================

public class Assembler {

    public static String[] instructions = { "halt", "load", "loadc", "store", "add", "mul", "sub", "div", "and", "or",
            "not", "lshift", "rshift", "bwc", "bwd", "if" };

    private HashMap<String, Integer> labels = new HashMap<>();

    public int translate(String instr, int a, int b) throws Exception {
        for (int i = 0; i < instructions.length; i++) {
            if (instr.equals(instructions[i])) {
                return Integer.parseInt(Integer.toHexString(i) + Integer.toHexString(a) + Integer.toHexString(b), 16);
            }
        }
        throw new Exception("Uknown assembly command: " + instr);
    }

    public void insertLabel(String label, int line) throws Exception {
        if (label.length() == 0) {
            throw new Exception("Empty label");
        }
        labels.put(label, line);
    }

    public int getLine(String label) throws Exception {
        if (!labels.containsKey(label)) {
            throw new Exception("Unknown label: '" + label + "'");
        }
        return labels.get(label);
    }
}
// ==============================================
