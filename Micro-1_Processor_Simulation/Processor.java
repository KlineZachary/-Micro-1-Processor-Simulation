public class Processor {
    private int[] reg = new int[8];
    private int PC = 0;// the program counter, contains the address of the next instruction to execute.
    private int IR = 0;// the instruction register, contains the hexadecimal representation of the
                       // current instruction
    private Memory memory;

    public boolean step() {// true = halt program
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
        
        System.out.println("PC = " + Integer.toHexString(PC));
        System.out.println("IR = " + Integer.toHexString(IR));
        // ==============================================
    }

    public boolean execute() {
        // Kevin==============================================
        int decoder = 15;// get token P, A, B
        int b = IR & decoder;// read first 4
        int a = (IR & (decoder <<= 4)) >> 4;// read next 4
        int p = (IR & (decoder <<= 4)) >> 8;// read next 4
        // System.out.println(p + " " + a + " " + b);
        switch (p) {// command list + execution code
        case 1:
            reg[a] = memory.read(reg[b]);
            break;
        case 2:
            reg[a] = memory.read(PC++);
            break;
        case 3:
            memory.write(reg[a], reg[b]);
            break;
        // ==============================================
        // Christopher =====================================
        case 4:// add
            reg[a] += reg[b];
            break;
        case 5:// mul
            reg[a] *= reg[b];
            break;
        // ===================================================
        // Zach==============================================
        case 6:// sub
            reg[a] -= reg[b];
            break;
        case 7:// div
            if (reg[b] == 0) {
                System.out.println("Division Error: address " + b + " is zero.");
                return false;
            }
            reg[a] /= reg[b];
            break;
        case 8:// and
            reg[a] = (reg[a] != 0 && reg[b] != 0) ? 1 : 0;
            break;
        case 9:// or
            reg[a] = (reg[a] != 0 || reg[b] != 0) ? 1 : 0;
            break;
        case 10:// not
            reg[a] = (reg[b] != 0) ? 0 : 1;
            break;
        // ==============================================
        case 11:// lshift
            // Christopher ==============================================================
            reg[a] = reg[b] << 1;
            break;
        case 12:// rshift
            reg[a] = reg[b] >> 1;
            break;
        case 13:// bwc
            reg[a] = reg[a] & reg[b];
            break;
        case 14:// bwd
            reg[a] = reg[a] | reg[b];
            break;
        case 15:// if
            if (reg[a] != 0)
                PC = reg[b];
            break;
        // ==============================================================
        }
        return true;
    }

    public String[] guiDump(){
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