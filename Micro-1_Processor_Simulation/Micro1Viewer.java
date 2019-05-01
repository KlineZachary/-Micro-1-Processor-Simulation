
/**
 * Micro1Viewer
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

//About 95% of this gui was Zach Kline
//Touch ups and File Chooser was Chris Aranda
public class Micro1Viewer {

    // Vars
    static Console console = new Console(1024);
    static int textAreas = 4;

    static String machineString = "";
    static String assemblyString = "";
    static String compileString = "";

    static JTextArea machineTextArea = new JTextArea(5, 20);
    static JTextArea assemblyTextArea = new JTextArea(5, 20);
    static JTextArea compileTextArea = new JTextArea(5, 20);
    static JTextArea memoryTextArea = new JTextArea(5, 20);

    // 0 - Compile, 1- Assembly, 2- MC, 3- Mem
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

        // Zach
        String[] titles = { "MC", "ASM", "CMP", "✄", "OUT", "ARR", "ALL", "BUG", "RUN", "?" };
        
        String[] tooltips = { "Load Machine Code", 
                              "Load Assembly", 
                              "Load Compiler", 
                              "Clear", 
                              "Display compiled var", 
                              "Display compiled arry", 
                              "Display all compiled vars",
                              "Debug each line", 
                              "Run entire file", 
                              "Help" };


        String[] labels = { "High Level Code", "Assembly Code", "Machine Code", "Memory" };
        Button.loadListener();
        Button.addAll(titles, tooltips, frame);

        // Zach
        Color customColor = Color.decode("#202021");
        frame.getContentPane().setBackground(customColor);

        // Add labels

        // Create labels for textAreas and give them properties
        //Give text area properties and give it a scroll pane
        //Add lables and textAreas to frame

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

        //Update elements in frame
        try {
            update();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    // Zach==================================================

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

        // Button Properties // Zach
        public Button(String title, String tooltip) {
            super(title);
            this.setBounds(x, y, length, length);// x axis, y axis, width, height
            this.setToolTipText(tooltip);
            this.addActionListener(click);
            this.tag = index++;
            x += dx;
        }

        // Add Buttons to frame // Zach
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

    // ==================================================
    // Register textfields
    static class DisplayRegister {
        // Zach==================================================
        static int x, y = Button.y + Button.length + 50, width, height = 30, dy, index;
        static ArrayList<JTextField> textFieldList = new ArrayList<JTextField>();

        // Dimensions
        public static void loadDimensions(int w, int h) {
            x = w / 2 + 350;
            width = w / 12;
            dy = height + 15;
        }

        // Add Register label and text fields //Zach
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

        // ==================================================
        // Zach ====================
        // Update register values on gui
        public static void updateRegisters() {
            String[] regNumbers = console.getCPU().guiDump();
            for (int i = 0; i < textFieldList.size(); i++) {
                textFieldList.get(i).setText(regNumbers[i]);
            }
        }

        // ====================

    }

    // Zach============= (Zach Did all actions except chris did  if statement for cases 0 - 2 that applies file chooser)
    // Button Actions 
    static class Clicklistener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Button button = (Button) (e.getSource());
            JButton open = new JButton(); // Creat button
            JFileChooser fc = new JFileChooser(); // Create File chooser
            String path, input;

            // Sets current directory to project location
            fc.setCurrentDirectory(new java.io.File(".")); // Chris

            fc.setFileSelectionMode(JFileChooser.FILES_ONLY); //Chris

            try {

                switch (button.tag) {
              
                case 0:// machine code
                    if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        clear();
                        machineString = console.load(fc.getSelectedFile().getAbsolutePath());
                    }
                    break;
                case 1:// assembly code
                    if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        clear();
                        assemblyString = console
                                .assemble(fc.getSelectedFile().getAbsolutePath().replaceAll("/\\.", ""));
                        machineString = console.getMemory().dumpInstructions();
                    }
                    break;
                case 2: // Compiler
                    if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        clear();
                        path = fc.getSelectedFile().getAbsolutePath().replaceAll("/\\.", "");
                        compileString = console.compile(path);
                        path = console.changeFileExtension(new File(path), ".asm");
                        assemblyString = console.assemble(path);
                        machineString = console.getMemory().dumpInstructions();
                    }

                    break;
      
                case 3:// Empty Text
                    clear();
                    break;
                case 4: // prints out variable as chosen by user
                    input = JOptionPane.showInputDialog(button.getParent(),
                            "Enter the var name you would like to print");
                    JOptionPane.showMessageDialog(null, console.print(input), "Selected Var",
                            JOptionPane.DEFAULT_OPTION);
                    break;
                case 5: // prints out array and length that is chosen by user
                    input = JOptionPane.showInputDialog(button.getParent(), "Enter the array variable");
                    int sizeInput = (Integer.parseInt(JOptionPane.showInputDialog(button.getParent(),
                            "Enter the number of elements you would like to see")));
                    JOptionPane.showMessageDialog(null, console.getArr(input, sizeInput), "Selected Array and elements",
                            JOptionPane.DEFAULT_OPTION);
                    break;
                case 6: // print all variables
                    JOptionPane.showMessageDialog(null, console.printAll(), "Variables", JOptionPane.DEFAULT_OPTION);

                    break;
                case 7: // DEBUG
                    if (!machineString.isEmpty()) {
                        step(1);
                    } else {
                        JOptionPane.showMessageDialog(null, "No machine code to run", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }

                    break;
                case 8: // RUN
                    if (!machineString.isEmpty()) {
                        step(Integer.MAX_VALUE);
                    } else {
                        JOptionPane.showMessageDialog(null, "No machine code to run", "Error",
                                JOptionPane.ERROR_MESSAGE);

                    }
                    break;
                case 9:// Help //Edited by Chris
                    JOptionPane.showMessageDialog(null,
                            "Built for Computer Organization class taught by Dr. Zhu\nDeveloped By: Zachary A. Kline, Kevin Chevalier, and Christopher Aranda\n\n"
                                    + "Help Catalog:\nMC: Loads a Machine Code File\nASM: Loads an assembly file\nCMP: Loads a High Language File\n✄: Empty text\n"
                                    + "BUG:Runs through one line of machine code at a time\nRUN: Runs every line of machine code\nARR: Displays array chosen by user\nALL: Displays all compiled variables\n",
                            "The Micro-1 Processor Simulation", JOptionPane.INFORMATION_MESSAGE);
                    ;
                    break;
                }
                update();
            } catch (Exception error) {
                JOptionPane.showMessageDialog(null, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

        }

    }

    // =======================================
    // Zach ==================================================
     /**
      * Create Input dialog for num of steps Then call step method with number that
     * was inputed
      * @throws Exception
      */
    public static void createInputDialog() throws Exception {
        String userInput = JOptionPane.showInputDialog("Please enter number of steps you would like to execute");
        int numSteps = Integer.parseInt(userInput);
        step(numSteps);
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
    public static String getLinesAfter(String lines, boolean isAssembly) throws Exception {
        String[] arr = lines.split("\n");
        StringBuilder out = new StringBuilder();

        // New out without the lines that have been run
        int PC = console.getCPU().getPC();
        for (int i = PC; i < arr.length; i++) {
            if (!isAssembly
                    && ((i == 0 || (i > 0 && !arr[i - 1].matches("[0]*2[0-9a-fA-F]{2}"))) && arr[i].matches("[0]+"))) {
                break;
            }
            if (!arr[i].startsWith("label") && !arr[i].startsWith("goto"))
                out.append(arr[i]).append("\n");
        }
        for (int i = 0; i < 15; i++) {
            out.append("\n");
        }

        return out.toString();
    }

     /**
      * Step through (execute instructions in memory)
      * @param numSteps how many times you would like to step through
      * @throws Exception
      */
    public static void step(int numSteps) throws Exception {
        for (int i = 0; i < numSteps; i++) {
            if (!console.step(1)) {
                return;
            }
            update();
        }
        return;
    }

    /**
     * Emptys all text areas with an empty strings
     * Resets console
     * Clears memory and registers
     */
    public static void clear() {
        compileString = "";
        assemblyString = "";
        machineString = "";
        console.reset();
        console.getCPU().clear();
        console.getMemory().clear();
    }

    /**
     * Updates all text areas (HL,Assembly,MC, Mem), and registers
     * * @throws Exception
     */

    public static void update() throws Exception {
        String memory = "";
        for (int i = 0; i < 15; i++) {
            memory += "\n";
        }
        memory = console.getMemory().dump() + memory;

        String assembly = getLinesAfter(assemblyString, true);
        String machine = getLinesAfter(machineString, false);
        String[] allStrings = { compileString, assembly, machine, memory };

        // Update registers
        DisplayRegister.updateRegisters();

        // Fill textAreas with new string values
        for (int i = 0; i < textAreaArray.length; i++) {
            textAreaArray[i].setText(allStrings[i]);
            // Bring text area cursor back to top
            textAreaArray[i].setCaretPosition(0);
        }

    }
}
