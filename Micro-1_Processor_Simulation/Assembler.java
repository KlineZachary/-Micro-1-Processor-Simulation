/**
 * Assembler
 */
// Kevin==============================================

public class Assembler {

    public static String[] instructions = { "halt", "load", "loadc", "store", "add", "mul", "sub", "div", "and", "or",
            "not", "lshift", "rshift", "bwc", "bwd", "if" };

    public int translate(String instr, int a, int b) {
        for (int i = 0; i < instructions.length; i++) {
            if (instr.equals(instructions[i])) {
                return Integer.parseInt(Integer.toHexString(i) + Integer.toHexString(a) + Integer.toHexString(b), 16);
            }
        }
        return -1;// error
    }
}
// ==============================================
