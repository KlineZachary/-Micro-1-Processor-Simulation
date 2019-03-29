public class Processor{
    private int[] reg;
    private int PC;
    private int IR;

    public Processor(int[] reg, int PC, int IR){
        this.reg = reg;
        this.PC = PC;
        this.IR = IR;
    }

    public boolean step(){
        //This is temp just to stop the error
        return true;
    }

    public void dump(){

    }

    public void setReg(int[] reg){
        this.reg = reg;
    }
    public void setPC(int PC){
        this.PC = PC;
    }
    public void setIR(int IR){
        this.IR = IR;
    }

    public int[] getReg(){
        return reg;
    }
    public int getPC(){
        return PC;
    }
    public int getIR(){
        return IR;
    }






    



}