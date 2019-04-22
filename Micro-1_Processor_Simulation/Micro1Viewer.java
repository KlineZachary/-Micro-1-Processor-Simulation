
/**
 * Micro1Viewer
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Micro1Viewer {

    // Vars
    static Console console = new Console(1024);
    //static JTextArea textArea = new JTextArea(5, 20);
    static int textAreas = 4;

    //0 - HL, 1- Assembly, 2- MC, 3- Mem
    static JTextArea[] textAreaArray = new JTextArea[textAreas];
    static JFrame frame = new JFrame(); // creating instance of JFrame

    // Main
    public static void main(String[] args) {
        new Micro1Viewer();
    }

    // Create frame and objects //Edited by zach
    public Micro1Viewer() {
        
        
        int width = 1400, height = 840;
        JLabel title = new JLabel("Micro1 - Viewer", SwingConstants.CENTER);
        title.setForeground(Color.white);
        title.setFont(new Font("Times New Roman", 0, 32));
        title.setBounds(100 / 2, 25, width - 100, 50);
        frame.add(title);

        // Buttons //Edited by zach
        String[] titles = { "MC", "ASM", "CMP", "✄", "⤻", "⤹", "OUT", "ARR", "ALL", "BUG","RUN", "?" };
        String[] tooltips = { "Load Machine Code", "Load Assembly", "Load Compiler", "Empty Text", "Step",
                "Dump Memory", "Display compiled var", "Display compiled arry", "Display all compiled vars","Debug each line", "Run entire file","Help" };
        String[] labels = {"High Level Code", "Assembly Code","Machine Code", "Memory"};
        Button.loadListener();
        Button.addAll(titles, tooltips, frame);

        // Frame Color //Edited by zach
        Color customColor = Color.decode("#202021");
        frame.getContentPane().setBackground(customColor);


        //Add labels


       
        //Create multiple text areas and labels fro those text areas
        int x = -240;
        int y = 240;
        for(int i = 0; i < textAreas;i++){
            x+= 260;
            //Add labels and set properties
            JLabel label = new JLabel();
            label.setBounds(x + 10, y - 40 , 150, 20);
            label.setText(labels[i]);
            label.setForeground(Color.white);
            frame.add(label);


            //Add and configure textAreas
            textAreaArray[i] = new JTextArea(5,20);
            textAreaArray[i].setEditable(false); //disable
            JScrollPane scrollPane = new JScrollPane(textAreaArray[i]);
            scrollPane.setBounds(x, y, width / 6, height - 280);
            frame.add(scrollPane);
        
        }
       
        //Dump memory
        textAreaArray[3].setText(console.getMemory().dump());

        // Load dimensions of registers and add them
        DisplayRegister.loadDimensions(width, height);
        DisplayRegister.addAll(frame, 8);

        // Frame Properties
        frame.setSize(width, height);
        frame.setLayout(null);// using no layout managers
        frame.setVisible(true);// making the frame visible

    }

    // ??
    // public static boolean isWindows() {
    // String os = System.getProperty("os.name").toLowerCase();
    // // windows
    // return (os.indexOf("win") >= 0);
    // }

    // public String getFile(){
    // Runtime.getRuntime().exec(isWindows() ? "Explorer.exe \"C:\\\"":"open
    // /users/");
    // }

    // Creating Buttons
    static class Button extends JButton {
        private static final long serialVersionUID = 1L;
        private static final int constantX = 50;
        static int x = constantX, y = 30, length = 50, dx = length + 5, index;
        static Clicklistener click;
        int tag;

        // Button Listener
        public static void loadListener() {
            click = new Clicklistener();
        }

        // Button Properties
        public Button(String title, String tooltip) {
            super(title);
            this.setBounds(x, y, length, length);// x axis, y axis, width, height
            this.setToolTipText(tooltip);
            this.addActionListener(click);
            this.tag = index++;
            x += dx;
        }

        // Add Buttons to frame
        static void addAll(String[] titles, String[] tooltips, JFrame frame) {
            for (int i = 0; i < titles.length; i++) {
                // Create new row of buttons
                if (i % 6 == 0) {
                    y += 50;
                    x = constantX;
                }
                frame.add(new Button(titles[i], tooltips[i]));
            }
        }

    }

    // Register textfields
    static class DisplayRegister {

        static int x, y = Button.y + Button.length + 50, width, height = 30, dy, index;
        static ArrayList<JTextField> textFieldList = new ArrayList<JTextField>();

        // Dimensions
        public static void loadDimensions(int w, int h) {
            x = w / 2 + 350;
            width = w / 12;
            dy = height + 15;
        }

        // Add Register label and text fields //Edited by zach
        public static void addAll(JFrame f, int count) {
            for (int i = 0; i < count; i++) {
                JLabel label = new JLabel("Register " + i);
                label.setForeground(Color.white);
                label.setBounds(x, y, 500, height);
                f.add(label);
                JTextField textField = new JTextField(Integer.toHexString(console.getCPU().getReg()[i]));
                textFieldList.add(textField);
                textField.setEditable(false); // Registers are for display only
                textField.setBounds(x + 75, y, width, height);
                f.add(textField);
                y += dy;
            }
        }

        // Zach ====================
        // Update register values on gui
        public static void updateRegisters() {
            String[] regNumbers = console.getCPU().guiDump();
            for (int i = 0; i < textFieldList.size(); i++) {
                textFieldList.get(i).setText(regNumbers[i]);
                System.out.println(regNumbers[i]);
            }
        }
        // ====================

    }

    // Button Actions
    static class Clicklistener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Button button = (Button) (e.getSource());
            String path, input;
            // Zach=============
            try {

                switch (button.tag) {
                case 0:// machine code
                    path = JOptionPane.showInputDialog(button.getParent(), "Enter the path to the Machine Code File:");
                    console.load(path);
                    textAreaArray[2].setText(console.printFile(path));
                    break;
                case 1:// assembly code
                    path = JOptionPane.showInputDialog(button.getParent(), "Enter the path to the Assembly Code File:");
                    console.assemble(path);
                    textAreaArray[1].setText(console.printFile(path));
                    break;
                case 2: // Compiler
                    path = JOptionPane.showInputDialog(button.getParent(),
                            "Enter the path to the File that you would like to compile:");
                    console.compile(path);
                    textAreaArray[0].setText(console.printFile(path));

                    break;
                case 3:// Empty Text
                    emptyTextAreas();
                    break;
                case 4:// Step //

                    // Ask how many steps and then step through
                    createInputDialog();

                    // Update Registers after stepping
                    DisplayRegister.updateRegisters();
                    
                    //Dump new memory data
                    textAreaArray[3].setText(console.getMemory().dump());

                    //Scroll back to top
                    textAreaArray[3].setCaretPosition(0);
                    break;
                case 5:// Memory dumps to textArea
                    textAreaArray[3].setText(console.getMemory().dump());

                    //Scroll back to top
                    textAreaArray[3].setCaretPosition(0);
                    break;
                case 6: // prints out variable as chosen by user
                    input = JOptionPane.showInputDialog(button.getParent(),
                            "Enter the var name you would like to print");
                    textAreaArray[0].setText(console.print(input ));

                    break;
                case 7: // prints out array and length that is chosen by user
                    input = JOptionPane.showInputDialog(button.getParent(), "Enter the array variable");
                    int sizeInput = (Integer.parseInt(JOptionPane.showInputDialog(button.getParent(),
                            "Enter the number of elements you would like to see")));
                    textAreaArray[0].setText(console.getArr(input, sizeInput));
                    break;
                case 8: // print all variables
                    textAreaArray[0].setText(console.printAll());;
                    break;

                case 9: //DEBUG
                    break;
                case 10: // RUN

                    break;
                case 11:// Help //Edited by Chris
                    JOptionPane.showMessageDialog(null,
                            "Built for Computer Organization class taught by Dr. Zhu\nDeveloped By: Zachary A. Kline, Kevin Chevalier, and Christopher Aranda\n\n"
                                    + "Help Catalog:\n\nMC: Loads a Machine Code File\nASM: Loads an assembly file\nCMP: Loads a compiler\n✄: Empty text\n"
                                    + "⤻: Step\n⤹: Dumps memory into text field\nOUT: Displays the compiled variables\nARR: Displays the compiled array\nALL: Displays all compiled variables\n",
                            "The Micro-1 Processor Simulation", JOptionPane.INFORMATION_MESSAGE);
                    ;
                    break;
                }
            } catch (Exception error) {
                JOptionPane.showMessageDialog(null, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            // =============

        }
    }

    // Zach ====================
    /**
     * Create Input dialog for num of steps Then call step method with number that
     * was inputed
     * 
     */
    public static void createInputDialog() throws Exception {
        String userInput = JOptionPane.showInputDialog("Please enter number of steps you would like to execute");
        int numSteps = Integer.parseInt(userInput);
        if (!step(numSteps))
            ; // break
    }

    // public static void scanAllLines(JTextArea textArea){
    //     int lines = textArea.getLineCount();
    //     String [] linesString = textArea.getText().split("\n");
    //     textArea.setText("");;
    //     for(int i = 0; i < lines; i++){
    //         textArea.append(linesString[i] + "\n");
    //         textArea.setSelectedTextColor(Color.green);
    //     }

    // }
    /**
     * Step through and tell user when program is terminated
     */

    public static boolean step(int numSteps) throws Exception {
        boolean halt = false;
        for (int i = 0; i < numSteps && !halt; i++) {
            if (!halt) {
                halt = console.getCPU().step(); // Something seems to be wrong here?
            } else {

                return false;
            }
        }
        System.out.println("done");
        return true;
        // ===========================================
    }

    public static void emptyTextAreas(){
        for(int i = 0; i<textAreas;i++){
            textAreaArray[i].setText("");
        }

    }

    
        //Cleaned up code so all the methods there were here arent needed feels like spring cleaning :)

    // ===========================================
}
