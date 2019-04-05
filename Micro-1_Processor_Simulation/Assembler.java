import java.util.ArrayList;

/**
 * Assembler
 */
// Kevin==============================================

public class Assembler {

    public static String[] instructions = { "halt", "load", "loadc", "store", "add", "mul", "sub", "div", "and", "or",
            "not", "lshift", "rshift", "bwc", "bwd", "if" };

    private ArrayList<Integer> labels = new ArrayList<>();

    public int translate(String instr, int a, int b) {
        for (int i = 0; i < instructions.length; i++) {
            if (instr.equals(instructions[i])) {
                return Integer.parseInt(Integer.toHexString(i) + Integer.toHexString(a) + Integer.toHexString(b), 16);
            }
        }
        return -1;// error
    }

    public void insertLabel(int label, int line) {
        while (labels.size() <= label) {
            labels.add(0);
        }
        labels.set(label, line);
    }

    public int getLine(int label) throws Exception {
        if (label >= labels.size()) {
            throw new Exception("Unknown label");
        }
        return labels.get(label);
    }
}
// ==============================================
