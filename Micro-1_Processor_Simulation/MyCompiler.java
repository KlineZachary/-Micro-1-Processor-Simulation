
/**
 * Compiler
 */
import java.util.*;

public class MyCompiler {

    HashMap<String, Integer> variables = new HashMap<>();

    // x = 5 -> loadc 0; #x_addr; loadc 1; 5; store 0 1;
    // y = x -> loadc 0; #y_addr; loadc 1; #x_addr; load 1 1; store 0 1;
    // x = 3 + 2 -> loadc 0; #x_addr; loadc 1; 3; loadc 2; 2; add 1 2; store 0 1;
    // x = 9 + 8 + 7 -> loadc 0; #x_addr; loadc 1; 9; loadc 2; 8; add 1 2; loadc 2;
    // 7; add 1 2; store 0 1;

    int index = 0;

    public String evaluate(String expression) {
        StringBuilder out = new StringBuilder();
        index = 0;
        if (expression.contains("=")) {
            String[] expressions = expression.split("=");
            Queue<String> post = postFix(expressions[1]);
            if (post == null)
                return null;
            Stack<String> nums = new Stack<>();
            boolean loadFirst = false;
            while (!post.isEmpty()) {
                String token = post.poll();
                // System.out.println("Token: " + token);
                if (token.matches("[a-zA-Z0-9]+")) {
                    nums.add(token);
                } else {
                    if (!loadFirst) {
                        out.append(load(nums.pop(), 1));
                        loadFirst = true;
                    }
                    out.append(load(nums.pop(), 0));
                    out.append(command(token));
                    out.append(" 1 0\n");
                }
            }
            if (!nums.isEmpty()) {
                out.append(load(nums.pop(), 1));
            }
            String var = expressions[0];
            out.append("loadc 0\n" + Integer.toHexString(variables.get(var)) + "\nstore 0 1\n");
        }
        return out.toString();
    }

    public String load(String num, int reg) {
        String base = "loadc " + reg + "\n";
        if (num.matches("[0-9]+"))
            return base + parse(num) + "\n";
        if (variables.containsKey(num))
            return base + Integer.toHexString(variables.get(num)) + "\nload " + reg + " " + reg + "\n";
        return load("0", reg);
    }

    public String parse(String num) {
        return Integer.toHexString(Integer.parseInt(num));
    }

    public Queue<String> postFix(String expression) {
        Queue<String> out = new LinkedList<>();
        Stack<String> operators = new Stack<>();
        while (index < expression.length()) {
            String token = nextToken(expression);
            if (token.matches("[a-zA-Z0-9]+")) {
                out.add(token);
            } else if (token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.peek().equals("(")) {
                    out.add(operators.pop());
                    if (operators.isEmpty())
                        return null;
                }
                operators.pop();

            } else {
                if (!operators.isEmpty()) {
                    int a = precedence(token), b = precedence(operators.peek());
                    while ((a < b || a == b && b != 2) && b != -1) {
                        out.add(operators.pop());
                        if (operators.isEmpty())
                            break;
                        b = precedence(operators.peek());
                    }
                }
                operators.push(token);
            }
        }
        while (!operators.isEmpty()) {
            if (operators.peek().equals("(")) {
                System.out.println("Operator at end has (");
                return null;
            }
            out.add(operators.pop());

        }
        return out;
    }

    public int precedence(String op) {
        switch (op) {
        case "+":
        case "-":
            return 0;
        case "*":
        case "/":
            return 1;
        case "^":
            return 2;
        case "(":
            return -1;
        case ")":
            return -2;
        default:
            return 3;
        }
    }

    public String command(String op) {
        switch (op) {
        case "+":
            return "add";
        case "-":
            return "sub";
        case "*":
            return "mul";
        case "/":
            return "div";
        case "^":
            return "";
        default:
            return "";
        }
    }

    public String nextToken(String expression) {
        if (!Character.isLetterOrDigit(expression.charAt(index)))
            return expression.substring(index, ++index);
        int startIndex = index;
        while (index < expression.length() && Character.isLetterOrDigit(expression.charAt(index))) {
            index++;
        }

        return expression.substring(startIndex, index);
    }

    public void insertVariable(String var, int addr) {
        variables.put(var, addr);
    }

    public boolean containsVariable(String var) {
        return variables.containsKey(var);
    }

    public static void main(String[] args) {
        MyCompiler compile = new MyCompiler();
        compile.variables.put("x", 15);
        compile.variables.put("y", 255);

        System.out.println(compile.evaluate("x=((10+5)*7)"));
    }
}