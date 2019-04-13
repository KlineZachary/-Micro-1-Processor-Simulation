public class Processor {
    private int[] reg = new int[8];
    private int PC = 0;// the program counter, contains the address of the next instruction to execute.
    private int IR = 0;// the instruction register, contains the hexadecimal representation of the
                       // current instruction
    private Memory memory;

    public boolean step() throws Exception {// true = halt program
        // Kevin==============================================
        IR = memory.read(PC++);
        if (IR == 0 || !execute()) {
            return true;
        }
        return false;
        // ==============================================
    }

    public void dump() {// show all registers
        // Kevin==============================================
        for (int i = 0; i < reg.length; i++) {
            System.out.println("reg[" + Integer.toString(i) + "] = " + Integer.toHexString(reg[i]));
        }
        System.out.println("PC = " + Integer.toHexString(PC));
        System.out.println("IR = " + Integer.toHexString(IR));
        // ==============================================
    }

    public boolean execute() throws Exception {
        // Kevin==============================================
        int decoder = 15;// get token P, A, B
        int b = IR & decoder;// read first 4
        int a = (IR & (decoder <<= 4)) >> 4;// read next 4
        int p = (IR & (decoder <<= 4)) >> 8;// read next 4
        // System.out.println("ML: " + p + " " + a + " " + b);
        boolean debug = false;
        switch (p) {// command list + execution code
        case 1:
            if (debug)
                System.out.print("REG[" + a + "] = MEM[" + reg[b] + "] = ");
            reg[a] = memory.read(reg[b]);
            if (debug)
                System.out.println(reg[a]);
            break;
        case 2:
            reg[a] = memory.read(PC++);
            if (debug)
                System.out.println("REG[" + a + "] = CONST " + reg[a]);
            break;
        case 3:
            memory.write(reg[a], reg[b]);
            if (debug)
                System.out.println("MEM[" + reg[a] + "] = REG[" + b + "] =" + reg[b]);
            break;
        // ==============================================
        // Christopher =====================================
        case 4:// add
            if (debug)
                System.out.println("REG[" + a + "] = " + reg[a] + "+" + reg[b]);

            reg[a] += reg[b];
            break;
        case 5:// mul
            if (debug)
                System.out.println("REG[" + a + "] = " + reg[a] + "x" + reg[b]);

            reg[a] *= reg[b];
            break;
        // ===================================================
        // Zach==============================================
        case 6:// sub
            if (debug)
                System.out.println("REG[" + a + "] = " + reg[a] + "-" + reg[b]);

            reg[a] -= reg[b];
            break;
        case 7:// div
            if (reg[b] == 0) {
                System.out.println("Division Error: address " + b + " is zero.");
                return false;
            }
            if (debug)
                System.out.println("REG[" + a + "] = " + reg[a] + "/" + reg[b]);

            reg[a] /= reg[b];
            break;
        case 8:// and
            if (debug)
                System.out.println("REG[" + a + "] = " + reg[a] + "&&" + reg[b]);

            reg[a] = (reg[a] > 0 && reg[b] > 0) ? 1 : 0;
            break;
        case 9:// or
            if (debug)
                System.out.println("REG[" + a + "] = " + reg[a] + "||" + reg[b]);

            reg[a] = (reg[a] > 0 || reg[b] > 0) ? 1 : 0;
            break;
        case 10:// not
            if (debug)
                System.out.println("REG[" + a + "] = !" + reg[b]);

            reg[a] = (reg[b] > 0) ? 0 : 1;
            break;
        // ==============================================
        case 11:// lshift
            // Christopher ==============================================================
            if (debug)
                System.out.println("REG[" + a + "] = << " + reg[b]);

            reg[a] = reg[b] << 1;

            break;
        case 12:// rshift
            if (debug)
                System.out.println("REG[" + a + "] = >> " + reg[b]);

            reg[a] = reg[b] >> 1;
            break;
        case 13:// bwc
            if (debug)
                System.out.println("REG[" + a + "] = " + reg[a] + "&" + reg[b]);

            reg[a] = reg[a] & reg[b];
            break;
        case 14:// bwd
            if (debug)
                System.out.println("REG[" + a + "] = " + reg[a] + "|" + reg[b]);

            reg[a] = reg[a] | reg[b];
            break;
        case 15:// if
            if (debug && reg[a] <= 0)
                System.out.println("PC = REG[" + b + "] = " + reg[b]);

            if (reg[a] <= 0)
                PC = reg[b];
            break;
        // ==============================================================
        }
        return true;
    }

    ///// Think Zach cant remember
    public String[] guiDump() {
        String[] regNumbers = new String[reg.length];
        for (int i = 0; i < 8; i++) {
            regNumbers[i] = Integer.toHexString(reg[i]);
        }
        return regNumbers;

    }

    // Zach==============================================
    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    public void setReg(int[] reg) {
        this.reg = reg;
    }

    public void setPC(int PC) {
        this.PC = PC;
    }

    public void setIR(int IR) {
        this.IR = IR;
    }

    public int[] getReg() {
        return reg;
    }

    public int getPC() {
        return PC;
    }

    public int getIR() {
        return IR;
    }

    public Memory getMemory() {
        return memory;
    }
    // ==============================================

}