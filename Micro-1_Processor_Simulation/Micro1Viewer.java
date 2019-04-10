
/**
 * Micro1Viewer
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class Micro1Viewer {

    // Vars
    static Console console = new Console();
    static JTextArea textArea = new JTextArea(5, 20);

    static JFrame frame = new JFrame(); // creating instance of JFrame

    // Main
    public static void main(String[] args) {
        Micro1Viewer viewer = new Micro1Viewer();

    }

    // Create frame and objects
    public Micro1Viewer() {

        // Title //Edited by zach
        int width = 700, height = 840;
        JLabel title = new JLabel("Micro1 - Viewer", SwingConstants.CENTER);
        title.setForeground(Color.white);
        title.setFont(new Font("Times New Roman", 0, 32));
        title.setBounds(100 / 2, 25, width - 100, 50);
        frame.add(title);

        // Buttons //Edited by zach
        String[] titles = { "MC", "ASM", "CMP", "✄", "⤻", "⤹", "OUT", "ARR", "ALL", "?", "X" };
        String[] tooltips = { "Load Machine Code", "Load Assembly", "Load Compiler", "Empty Text", "Step", "Dump Memory",
                "Display compiled var", "Display compiled arry", "Display all compiled vars", "Help", "Quit" };
        Button.loadListener();
        Button.addAll(titles, tooltips, frame);

        // Frame Color //Edited by zach
        Color customColor = Color.decode("#202021");
        frame.getContentPane().setBackground(customColor);

        // Add Scrolling to Frame
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBounds(50, Button.y + Button.length, width / 2, height - 280);
        frame.add(scrollPane);

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
                if (i % 5 == 0) {
                    y += 50;
                    x = constantX;
                }
                frame.add(new Button(titles[i], tooltips[i]));
            }
        }

    }

    // Register textfields
    static class DisplayRegister {

        static int x, y = Button.y + Button.length, width, height = 30, dy, index;
        static ArrayList<JTextField> textFieldList = new ArrayList<JTextField>();

        // Dimensions
        public static void loadDimensions(int w, int h) {
            x = w / 2 + 75;
            width = w / 8;
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
            String path;
            // Zach=============
            switch (button.tag) {
            case 0:// machine code
            
                path = JOptionPane.showInputDialog(button.getParent(), "Enter the path to the Machine Code File:");
                console.load(path);
                
                
                break;
            case 1:// assembly code
                path = JOptionPane.showInputDialog(button.getParent(), "Enter the path to the Assembly Code File:");
                console.assemble(path);
                break;

            case 2: // Compiler
                path = JOptionPane.showInputDialog(button.getParent(),
                        "Enter the path to the File that you would like to compile:");
                console.compile(path);
                break;
            case 3:// Empty Text
                textArea.setText("");
                break;
            case 4:// Step
                try {
                    createInputDialog();
                    
                    // Update Registers after stepping
                    DisplayRegister.updateRegisters();
                    JOptionPane.showMessageDialog(null, "Program Terminated");
                } catch (Exception error) {
                    JOptionPane.showMessageDialog(null, "Error steping through memory", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

                
                break;
            case 5:// Memory dumps
                guiMemDump();
                break;
            case 6: // out
                try {
                    String input = JOptionPane.showInputDialog(button.getParent(),
                        "Enter the var name you would like to print");
                    guiPrint(input);
                 } catch (Exception error) {
                        JOptionPane.showMessageDialog(null, "Error printing variable", "Error",
                            JOptionPane.ERROR_MESSAGE);
                 }
                break;
            case 7: // arr
                 try {
                    String input = JOptionPane.showInputDialog(button.getParent(),
                        "Enter the array variable");
                    int sizeInput = (Integer.parseInt(JOptionPane.showInputDialog(button.getParent(),
                    "Enter the number of elements you would like to see")));
                    guiPrintArr(input, sizeInput);
                }catch (Exception error) {
                        JOptionPane.showMessageDialog(null, "Error printing array value", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                break;
            case 8: // all
                try{
                    guiPrintAll();
                }catch(Exception error){
                    JOptionPane.showMessageDialog(null, "Error printing all compiled variables", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                break;
            case 9:// Help
                JOptionPane.showMessageDialog(null,
                        "Built for Computer Organization class taught by Dr. Zhu\nDeveloped By: Zachary A. Kline, Kevin Chevalier, and Chris Aranda",
                        "The Micro-1 Processor Simulation", JOptionPane.INFORMATION_MESSAGE);
                ;
                break;
            case 11: // Quit
                frame.dispose();
                break;
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
        if (!guiStep(numSteps)) ; // break
           
    }

    /**
     * Step through and tell user when program is terminated
     */

    public static boolean guiStep(int numSteps) throws Exception {
        boolean halt = false;
        for (int i = 0; i < numSteps && !halt; i++) {
            if (!halt)
                halt = console.getCPU().step();

            if (halt) {
                System.out.println("program terminated");
                return false;
            }
        }
        System.out.println("done");
        return true;
    }

    // Dump memory to textArea
    public static void guiMemDump() {
        int[] cells = console.getMemory().getCells();
        for (int i = 0; i < console.getMemory().getCap(); i++) {
            textArea.append("cell[" + Integer.toHexString(i) + "] = " + Integer.toHexString(cells[i]) + "\n");
        }
    }
    //Displays compiled var to textArea
    public static void guiPrint(String var) throws Exception {
		if (console.getCompiler() == null) {
            JOptionPane.showMessageDialog(null, "No file was compiled", "Error", JOptionPane.ERROR_MESSAGE);
		} else if (console.getCompiler().containsVariable(var)) {
            textArea.setText( var + "=" + console.getMemory().read(console.getCompiler().getVariable(var)));
		} else {
            JOptionPane.showMessageDialog(null, "Variable does not exist", "Error", JOptionPane.ERROR_MESSAGE);

		}
    }
    //Displays elements in array
    public static void guiPrintArr(String var, int len) throws Exception {
		if (console.getCompiler() == null) {
            JOptionPane.showMessageDialog(null, "No file was compiled", "Error", JOptionPane.ERROR_MESSAGE);
		} else if (console.getCompiler().containsVariable(var)) {
			for (int i = 0; i < len; i++) {
                textArea.append(var + "[" + i + "]=" + console.getMemory().read(console.getCompiler().getVariable(var) + i) + "\n");
			}
		} else {
            JOptionPane.showMessageDialog(null, "Variable does not exist", "Error", JOptionPane.ERROR_MESSAGE);
		}
    }
    
    public static void guiPrintAll() throws Exception {
		if (console.getCompiler() == null) {
            JOptionPane.showMessageDialog(null, "No file was compiled", "Error", JOptionPane.ERROR_MESSAGE);
		} else {
			Iterator<String> vars = console.getCompiler().getAllVariables().iterator();
			while (vars.hasNext()) {
                String var = vars.next();
                textArea.append(var + "=" + console.getMemory().read(console.getCompiler().getVariable(var)) + "\n");
			}
		}
	}

    // ===========================================
}
