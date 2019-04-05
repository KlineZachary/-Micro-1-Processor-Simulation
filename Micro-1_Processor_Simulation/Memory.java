public class Memory {
    private int cap;
    private int[] cell = new int[cap];

    // Do we need cap in initializer
    public Memory(int cap) { // Zach I think
        this.cell = new int[this.cap = cap];
    }

    public int read(int addr) {
        return cell[addr]; // Zach
    }

    public void write(int addr, int data) {
        cell[addr] = data;// Kevin
    }

    public void dump() {
        for (int i = 0; i < cap; i++) {// Kevin
            System.out.println("cell[" + Integer.toHexString(i) + "] = " + Integer.toHexString(cell[i]));
        }
    }

    public void setCap(int cap) {
        this.cap = cap; // Zach
    }

    public int getCap() {
        return cap; // Zach
    }

}