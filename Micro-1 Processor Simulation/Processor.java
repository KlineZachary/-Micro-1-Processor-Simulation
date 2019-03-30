public class Processor {
    private int[] reg = new int[8];
    private int PC = 0;// the program counter, contains the address of the next instruction to execute.
    private int IR = 0;// the instruction register, contains the hexadecimal representation of the
                       // current instruction
    private Memory memory;

    public boolean step() {// true = halt program
        IR = memory.read(PC++);
        if (IR == 0) {
            return true;
        }
        execute();
        return false;
    }

    public void dump() {// show all registers
        for (int i = 0; i < 8; i++) {
            System.out.println("reg[" + Integer.toHexString(i) + "] = " + Integer.toHexString(reg[i]));
        }
        System.out.println("PC = " + Integer.toHexString(PC));
        System.out.println("IR = " + Integer.toHexString(IR));
    }

    public void execute() {
        int decoder = 15;// get token P, A, B
        int b = IR & decoder;// read first 4
        int a = (IR & (decoder <<= 4)) >> 4;// read next 4
        int p = (IR & (decoder <<= 4)) >> 8;// read next 4
        System.out.println("P: " + p + " A: " + a + " B: " + b);
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
        case 4:// add
            memory.write(reg[a], reg[a] + reg[b]);
            break;
        case 5:// mul
            memory.write(reg[a], reg[a] * reg[b]);
            break;
        case 6:// sub
            break;
        case 7:// div
            break;
        case 8:// and
            break;
        case 9:// or
            break;
        case 10:// not
            break;
        case 11:// lshift
            break;
        case 12:// rshift
            break;
        case 13:// bwc
            break;
        case 14:// bwd
            break;
        case 15:// if
            break;
        }
    }

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

}