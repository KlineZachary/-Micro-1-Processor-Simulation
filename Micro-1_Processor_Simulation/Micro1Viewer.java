
/**
 * Micro1Viewer
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

//Moslty all zach
public class Micro1Viewer {

    // Vars
    static Console console = new Console(1024);
    // static JTextArea textArea = new JTextArea(5, 20);
    static int textAreas = 4;
    static int lineNum = 0;
    static String machineString = "";
    static String assemblyString = "";
    static String compileString = "";
    static String memoryString = "";

    static JTextArea machineTextArea = new JTextArea(5, 20);
    static JTextArea assemblyTextArea = new JTextArea(5, 20);
    static JTextArea compileTextArea = new JTextArea(5, 20);
    static JTextArea memoryTextArea = new JTextArea(5, 20);

    // 0 - HL, 1- Assembly, 2- MC, 3- Mem
    static JTextArea[] textAreaArray = { compileTextArea, assemblyTextArea, machineTextArea, memoryTextArea };
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
        String[] titles = { "MC", "ASM", "CMP", "✄", "⤻", "⤹", "OUT", "ARR", "ALL", "BUG", "RUN", "?" };
        String[] tooltips = { "Load Machine Code", "Load Assembly", "Load Compiler", "Empty Text", "Step",
                "Dump Memory", "Display compiled var", "Display compiled arry", "Display all compiled vars",
                "Debug each line", "Run entire file", "Help" };
        String[] labels = { "High Level Code", "Assembly Code", "Machine Code", "Memory" };
        Button.loadListener();
        Button.addAll(titles, tooltips, frame);

        // Frame Color //Edited by zach
        Color customColor = Color.decode("#202021");
        frame.getContentPane().setBackground(customColor);

        // Add labels

        // Create multiple text areas and labels fro those text areas
        int x = -240;
        int y = 240;
        for (int i = 0; i < textAreas; i++) {
            x += 260;
            // Add labels and set properties
            JLabel label = new JLabel();
            label.setBounds(x + 10, y - 40, 150, 20);
            label.setText(labels[i]);
            label.setForeground(Color.white);
            frame.add(label);

            // Add and configure textAreas
            textAreaArray[i].setEditable(false); // disable
            JScrollPane scrollPane = new JScrollPane(textAreaArray[i]);
            scrollPane.setBounds(x, y, width / 6, height - 280);
            frame.add(scrollPane);

        }

        // Load dimensions of registers and add them
        DisplayRegister.loadDimensions(width, height);
        DisplayRegister.addAll(frame, 8);

        // Frame Properties
        frame.setSize(width, height);
        frame.setLayout(null);// using no layout managers
        frame.setVisible(true);// making the frame visible
        update();
    }

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

    // Zach=============
    // Button Actions
    static class Clicklistener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Button button = (Button) (e.getSource());
            String path, input;

            try {

                switch (button.tag) {
                case 0:// machine code
                    lineNum = 0;
                    path = JOptionPane.showInputDialog(button.getParent(), "Enter the path to the Machine Code File:");
                    if (path != null) {

                        // Print file to MC textArea
                        machineString = (console.load(path));

                        textAreaArray[2].setText(machineString);

                        // Dump new memory data
                        textAreaArray[3].setText(console.getMemory().dump());

                        // Scroll back to top of mem
                        textAreaArray[3].setCaretPosition(0);

                    }

                    break;
                case 1:// assembly code

                    lineNum = 0;

                    // This prevents empty error message if user clicks cancel
                    path = JOptionPane.showInputDialog(button.getParent(), "Enter the path to the Assembly Code File:");
                    if (path != null) {
                        assemblyString = console.assemble(path);
                    }

                    break;
                case 2: // Compiler

                    lineNum = 0;

                    path = JOptionPane.showInputDialog(button.getParent(),
                            "Enter the path to the File that you would like to compile:");

                    // This prevents empty error message if user clicks cancel
                    if (path != null) {
                        compileString = console.compile(path);
                        assemblyString = console.assemble(console.changeFileExtension(new File(path), ".asm"));
                        machineString = console.getMemory().dumpInstructions();
                        update();
                    }

                    break;
                case 3:// Empty Text
                    clear();
                    break;
                case 4:// Step //

                    // Ask how many steps and then step through
                    createInputDialog();

                    // Dump new memory data
                    machineString = console.getMemory().dump();

                    break;
                case 5:// Memory dumps to textArea
                    memoryString = console.getMemory().dump();

                    break;
                case 6: // prints out variable as chosen by user
                    input = JOptionPane.showInputDialog(button.getParent(),
                            "Enter the var name you would like to print");
                    textAreaArray[0].setText(console.print(input));

                    break;
                case 7: // prints out array and length that is chosen by user
                    input = JOptionPane.showInputDialog(button.getParent(), "Enter the array variable");
                    int sizeInput = (Integer.parseInt(JOptionPane.showInputDialog(button.getParent(),
                            "Enter the number of elements you would like to see")));
                    textAreaArray[0].setText(console.getArr(input, sizeInput));
                    break;
                case 8: // print all variables
                    textAreaArray[0].setText(console.printAll());

                    break;
                case 9: // DEBUG
                    step(1);
                    break;
                case 10: // RUN
                    step(Integer.MAX_VALUE);
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

            update();

        }
        // =============
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

    /**
     * Get lines of machine code based on is it is debuging or not If debuging then
     * each time user hits debug button the line will increase by one and that line
     * will be stepped through every time they click the button else means they are
     * running it and every line will be stepped through
     * 
     * @param lines
     * @param isDebug
     * @return
     * @throws Exception
     */
    public static String getLinesAfter(String lines) throws Exception {
        String[] arr = lines.split("\n");
        StringBuilder out = new StringBuilder();

        // New out without the lines that have been run
        for (int i = lineNum + 1; i < arr.length; i++) {
            out.append(arr[i]).append("\n");
        }

        return out.toString();
    }

    /**
     * Step through and tell user when program is terminated
     */

    public static boolean step(int numSteps) throws Exception {
        boolean halt = false;
        for (int i = 0; i < numSteps && !halt; i++, lineNum++) {
            if (!halt) {
                halt = console.getCPU().step();

                // Update Registers and memory after stepping
                assemblyString = getLinesAfter(assemblyString);
                machineString = getLinesAfter(machineString);
                update();

            } else {

                return false;
            }
        }
        System.out.println("done");
        return true;
        // ===========================================
    }

    /**
     * Emptys all text areas with an empty string
     */
    public static void clear() {
        // textAreas -1 because dont want to empty memory textArea
        compileString = "";
        assemblyString = "";
        machineString = "";

    }

    /**
     * Updates registers and memory
     */
    public static void update() {
        String[] allStrings = { compileString, assemblyString, machineString, memoryString };
        DisplayRegister.updateRegisters();
        for (int i = 0; i < textAreaArray.length; i++) {
            textAreaArray[i].setText(allStrings[i]);
            textAreaArray[i].setCaretPosition(0);
        }

    }

    // Cleaned up code so all the methods there were here arent needed feels like
    // spring cleaning :)

    // ===========================================
}
