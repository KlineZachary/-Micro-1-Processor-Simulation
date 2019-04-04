
/**
 * Micro1Viewer
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Scanner;
import java.util.ArrayList;
public class Micro1Viewer {

    static Console console = new Console();



    public static void main(String[] args) {
        Micro1Viewer viewer = new Micro1Viewer();
        
    }

    public Micro1Viewer() {
        int width = 700, height = 750;
        JFrame f = new JFrame();// creating instance of JFrame

        JLabel title = new JLabel("Micro1 - Viewer", SwingConstants.CENTER);
        title.setFont(new Font("Times New Roman", 0, 32));
        title.setBounds(100 / 2, 25, width - 100, 50);
        f.add(title);

        String[] titles = { "MC", "ASM", "↩", "⤻", "⇪", "⤹" };
        String[] tooltips = { "Load Machine Code", "Load Assembly", "Reset", "Step", "Load to Memory", "Dump Memory" };
        Button.loadListener();
        Button.addAll(titles, tooltips, f);

        JTextArea textArea = new JTextArea(5, 20);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBounds(50, Button.y + Button.length, width / 2, height - 225);
        f.add(scrollPane);

        DisplayRegister.loadDimensions(width, height);
        DisplayRegister.addAll(f,8);

        f.setSize(width, height);
        f.setLayout(null);// using no layout managers
        f.setVisible(true);// making the frame visible
    }

    public static boolean isWindows() {

        String os = System.getProperty("os.name").toLowerCase();
        // windows
        return (os.indexOf("win") >= 0);

    }

    // public String getFile(){
    // Runtime.getRuntime().exec(isWindows() ? "Explorer.exe \"C:\\\"":"open
    // /users/");
    // }

    static class Button extends JButton {

        private static final long serialVersionUID = 1L;
        static int x = 50, y = 100, length = 50, dx = length + 5, index;
        static Clicklistener click;
        int tag;

        public static void loadListener() {
            click = new Clicklistener();
        }

        public Button(String title, String tooltip) {
            super(title);
            this.setBounds(x, y, length, length);// x axis, y axis, width, height
            this.setToolTipText(tooltip);
            this.addActionListener(click);
            this.tag = index++;
            x += dx;
        }

        static void addAll(String[] titles, String[] tooltips, JFrame f) {
            for (int i = 0; i < titles.length; i++) {
                f.add(new Button(titles[i], tooltips[i]));
            }
        }

    }

    static class DisplayRegister {        
        static int x, y = Button.y + Button.length, width, height = 30, dy, index;
        static ArrayList<JTextField> textFieldList = new ArrayList<JTextField>();
        
        public static void loadDimensions(int w, int h) {
            x = w / 2 + 75;
            width = w / 8;
            dy = height + 15;
        }

       

        public static void addAll(JFrame f, int count) {
            
            for (int i = 0; i < count; i++) {
                JLabel label = new JLabel("Register " + i);
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

        public static void updateRegisters(){
            String[] regNumbers = console.getCPU().guiDump();
            for(int i = 0; i < textFieldList.size(); i++){
                int[] regArray = new int[console.getCPU().getReg().length];
                textFieldList.get(i).setText(regNumbers[i]);
                System.out.println(regNumbers[i]);
            }
        }


    }

    static class Clicklistener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Button button = (Button) (e.getSource());
            String path;
            switch (button.tag) {
            case 0:// machine code
                path = JOptionPane.showInputDialog(button.getParent(), "Enter the path to the Machine Code File:");
                console.load(path);
                break;
            case 1:// assembly code
                path = JOptionPane.showInputDialog(button.getParent(), "Enter the path to the Assembly Code File:");
                console.assemble(path);
                break;
            case 2:// Reset
                break;

            //Zach
            case 3:// Step
                createInputDialog();
                DisplayRegister.updateRegisters();
                JOptionPane.showMessageDialog(null,"Program Terminated");
                break;
            case 4:// Memory load
                console.getCPU().dump();
                break;
            case 5:// Memory dumps
                console.getMemory().dump();
                break;
            }
        }
    }
    
    //Zach ====================
    /**
     * Create Input dialog for num of steps
     * Then call step method with number that was inputed
     * 
     */
    public static void createInputDialog(){
        String userInput = JOptionPane.showInputDialog("Please enter number of steps you would like to execute");
        int numSteps = Integer.parseInt(userInput);
        if (!step(numSteps)) ; //break
    }

    /**
     * Step through and tell user when program is terminated
     */

    public static boolean step(int numSteps) {
		boolean halt = false;
		for (int i = 0; i < numSteps && !halt; i++) {
			if (!halt) halt = console.getCPU().step();
        
			if (halt) {
                System.out.println("program terminated");
				return false;
			}
		}
		System.out.println("done");
		return true;
    }
 
    //===========================================
}

