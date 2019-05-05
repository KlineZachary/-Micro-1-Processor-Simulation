public class Memory {
    /**
     * The capacity for memory
     */
    private int cap;
    /**
     * The storage for memory
     */
    private int[] cell = new int[cap];

    /**
     * The number of hex digits needed to represent the addresses Used for display
     * only
     */
    private int hexDigitCount;

    /**
     * Creates a new memory with a capacity
     * 
     * @param cap The capacity for memory
     */
    public Memory(int cap) { // Zach I think
        this.cell = new int[this.cap = cap];
        hexDigitCount = Integer.toHexString(cap).length();
    }

    /**
     * Reads a value from an address in memory and returns it
     * 
     * @param addr The address to reference in memory
     * @return The value in memory
     * @throws Exception Invalid address
     */
    public int read(int addr) throws Exception {
        isValidAddress(addr);
        return cell[addr]; // Zach
    }

    /**
     * Write a value to memory at a address
     * 
     * @param addr The address to reference in memory
     * @param data The value to save to memory
     * @throws Exception Invalid address
     */
    public void write(int addr, int data) throws Exception {
        isValidAddress(addr);
        cell[addr] = data;// Kevin
    }

    /**
     * Retrieve all the values in memory with a hex representation Format: cell[#]=#
     * 
     * @return The values in memory with a formatted view
     */
    public String dump() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < cap; i++) {// Kevin
            b.append("cell[").append(String.format("%0" + hexDigitCount + "x", i)).append("] = ")
                    .append(String.format("%08x", cell[i])).append("\n");
        }
        return b.toString();
    }

    // Zach==============================================
    /**
     * Retrieve all the values in memory with a hex representation without
     * formatting
     * 
     * @return The values in memory with a unformatted view
     */
    public String dumpInstructions() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < cap; i++) {
            b.append(String.format("%08x", cell[i])).append("\n");
        }
        return b.toString();
    }

    /**
     * Clear memory for GUI
     */
    public void clear() {
        for (int i = 0; i < cap; i++) {
            cell[i] = 0;
        }
    }

    /**
     * Get that capacity for memory
     * 
     * @return The capacity
     */
    public int getCap() {
        return cap;
    }

    // Kevin==============================================

    /**
     * Verify if the address is valid
     * 
     * @param addr The address in check
     * @throws Exception Invalid address
     */
    public void isValidAddress(int addr) throws Exception {
        if (addr < 0 || addr >= cap)
            throw new Exception("Invalid memory access at " + addr);
    }

}