public class Memory{
    private int[] cell;
    private int cap = 256;

    //Do we need cap in initializer
    public Memory(int[] cell){
        this.cell = cell;
    }

    public int read(int addr){
        //Temporary so no error
        return 0;
    }

    public void write(int addr,int data){

    }

    public void dump(){

    }

    public void setCell(int[] cell){
        this.cell = cell;
    }
    public void setCap(int cap){
        this.cap = cap;
    }
    public int[] getCell(){
        return cell;
    }
    public int getCap(){
        return cap;
    }



}