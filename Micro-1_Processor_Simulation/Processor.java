public class Processor {

    /*
        Number of registers
    */
    private int[] reg = new int[8];

    /*
        the program counter, contains the address of the next instruction to execute.
        Initilize PC with 0
    */
    private int PC = 0;

    /*
        the instruction register, contains the hexadecimal representation of the
        current instruction
        Initilize IR with 0
    */
    private int IR = 0;

    /*
        Memory class instance
    */
    private Memory memory;


    /**
     * Execute each instruction register halt when IR == 0
     * @return false = halt program
     * @throws Exception 
     */
    public boolean step() throws Exception {
        // Kevin==============================================
        IR = memory.read(PC++);
        if (IR == 0)
            return false;
        execute();
        return true;
        // ==============================================
    }

    /**
     * Dump values in registers (in hex)
     */
    public void dump() {// show all registers
        // Kevin==============================================
        for (int i = 0; i < reg.length; i++) {
            System.out.println("reg[" + Integer.toString(i) + "] = " + Integer.toHexString(reg[i]));
        }
        System.out.println("PC = " + Integer.toHexString(PC));
        System.out.println("IR = " + Integer.toHexString(IR));
        // ==============================================
    }

    /**
     * Execute commands 
     * P = command
     * A = register index
     * B = regiser index
     * switch statments uses p to determine which exact command should be executed
     * @throws Exception
     */
    public void execute() throws Exception {
        // Kevin==============================================
        int b = IR & 15;// read first 4
        int a = (IR >>= 4) & 15;// read next 4
        int p = (IR >>= 4) & 15;// read next 4
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
        // Chris =====================================
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
                throw new Exception("Division Error: address " + b + " is zero.");
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

            reg[a] <<= reg[b];

            break;
        case 12:// rshift
            if (debug)
                System.out.println("REG[" + a + "] = >> " + reg[b]);

            reg[a] >>= reg[b];
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
    }

    /**
     * Clear each register with value 0
     */
    public void clear() {// Kevin
        for (int i = 0; i < reg.length; i++) {
            reg[i] = 0;
        }
    }


    /**
     * Dump all values in registers in the form of a string
     * @return returns a string with all registers values (in hex)
     */
    /////Zach========================
    public String[] guiDump() {
        String[] regNumbers = new String[reg.length];
        for (int i = 0; i < 8; i++) {
            regNumbers[i] = Integer.toHexString(reg[i]);
        }
        return regNumbers;

    }
    //==============================

    // Zach==============================================

    /**
     * Set memory object
     * @param memory 
     */
    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    /**
     * Set program counter
     * @param PC
     */
    public void setPC(int PC) {
        this.PC = PC;
    }

    /**
     * Return register object
     * @return regiser
     */
    public int[] getReg() {
        return reg;
    }

    /**
     * Return Program counter
     * @return program coutner
     */
    public int getPC() {
        return PC;
    }

    /**
     * Return instruction register
     * @return instruction register
     */
    public int getIR() {
        return IR;
    }

    /**
     * Return memory object
     * @return memory object
     */
    public Memory getMemory() {
        return memory;
    }
    // ==============================================

}