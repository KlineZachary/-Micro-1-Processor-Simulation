public class Memory {
    private int cap;
    private int[] cell = new int[cap];

    // Do we need cap in initializer
    public Memory(int cap) {
        this.cell = new int[this.cap = cap];
    }

    public int read(int addr) {
        return cell[addr];
    }

    public void write(int addr, int data) {
        cell[addr] = data;
    }

    public void dump() {
        for (int i = 0; i < cap; i++) {
            System.out.println("cell[" + Integer.toHexString(i) + "] = " + Integer.toHexString(cell[i]));
        }
    }

    public void setCap(int cap) {
        this.cap = cap;
    }

    public int getCap() {
        return cap;
    }

}