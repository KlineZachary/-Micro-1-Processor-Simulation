
/**
 * Compiler
 */
import java.util.*;

public class MyCompiler {

    String[] reservedWords = { "if", "while", "end" };
    HashMap<String, Integer> variables = new HashMap<>();
    Stack<Integer> jumpStack = new Stack<>();
    int jumpCount = 1;

    // x = 5 -> loadc 0; #x_addr; loadc 1; 5; store 0 1;
    // y = x -> loadc 0; #y_addr; loadc 1; #x_addr; load 1 1; store 0 1;
    // x = 3 + 2 -> loadc 0; #x_addr; loadc 1; 3; loadc 2; 2; add 1 2; store 0 1;
    // x = 9 + 8 + 7 -> loadc 0; #x_addr; loadc 1; 9; loadc 2; 8; add 1 2; loadc 2;
    // 7; add 1 2; store 0 1;

    int index = 0;
    String previousToken = null;
    boolean isExpectingBool = false;

    public String evaluate(String line) throws Exception {
        if (!line.matches(".*[a-zA-Z0-9].*")) {
            throw new Exception("Empty Expression");
        }

        if (line.equals("if")) {
            isExpectingBool = true;
            return null;
        } else if (line.equals("while")) {
            isExpectingBool = true;
            String out = ":lb" + Integer.toHexString(jumpCount) + "\n";
            jumpStack.push(-1 * (jumpCount++));
            return out;
        } else if (line.equals("end")) {
            String out = "";
            int elseNum = jumpStack.pop();
            if (!jumpStack.isEmpty()) {
                int whileNum = jumpStack.pop();
                if (whileNum > 0) {
                    jumpStack.push(whileNum);
                } else {
                    out += "loadc 1\n1\nloadc 0\n:go" + Integer.toHexString(-whileNum) + "\nif 1 0\n";
                    System.out.println(":go" + Integer.toHexString(-whileNum));

                }
            }
            out += ":lb" + Integer.toHexString(elseNum) + "\n";
            return out;
        }
        StringBuilder out = new StringBuilder();

        String expression;

        if (line.matches(".*=.*")) {
            if (line.matches("(.*=){2}.*")) {
                throw new Exception("Two Equal Signs in one Statement");
            }

            String[] parts = line.split("=");
            if (parts.length == 1) {
                throw new Exception("Empty Expression");
            }
            expression = parts[1];

        } else {
            expression = line;
        }

        if (!expression.matches(".*[a-zA-Z0-9].*")) {
            throw new Exception("Invalid Expression");
        }
        out.append(handleExpression(expression));
        if (line.contains("=")) {
            String var = line.split("=")[0];
            out.append("loadc 0\n" + Integer.toHexString(variables.get(var)) + "\nstore 0 1\n");
        } else if (isExpectingBool) {
            out.append("loadc 0\n").append(":go" + Integer.toHexString(jumpCount) + "\n").append("not 1 1\nif 1 0\n");
            jumpStack.push(jumpCount++);
            isExpectingBool = false;
        } else {
            throw new Exception("Invalid Statement");
        }

        return out.toString();
    }

    public String handleExpression(String expression) throws Exception {
        StringBuilder out = new StringBuilder();
        index = 0;
        Queue<String> q = postFix(expression);
        Stack<String> nums = new Stack<>();
        boolean loadFirst = false;
        while (!q.isEmpty()) {
            String token = q.poll();
            // System.out.println("Token: " + token);
            if (token.matches("[a-zA-Z0-9]+")) {
                nums.add(token);
            } else {
                if (!loadFirst) {
                    out.append(load(nums.pop(), 0));
                    loadFirst = true;
                }
                out.append(load(nums.pop(), 1));
                // out.append(load(nums.isEmpty() ? "0" : nums.pop(), 0));
                out.append(getOperator(token));
                out.append(" 1 0\n");
            }
        }
        if (!nums.isEmpty()) {
            out.append(load(nums.pop(), 1));
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

    public Queue<String> postFix(String expression) throws Exception {
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
                        throw new Exception("Unbalanced parentheses");
                }
                operators.pop();

            } else {
                if (!operators.isEmpty()) {
                    int a = precedence(token), b = precedence(operators.peek());
                    while ((a < b || a == b && b != 2) && !operators.peek().equals("(")) {
                        out.add(operators.pop());
                        if (operators.isEmpty())
                            break;
                        b = precedence(operators.peek());
                    }
                }
                operators.push(token);
            }
            previousToken = token;
        }
        while (!operators.isEmpty()) {
            if (operators.peek().equals("(")) {
                throw new Exception("Unbalanced parentheses");
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
        default:
            return -1;
        }
    }

    public String getOperator(String op) {
        switch (op) {
        case "+":
            return "add";
        case "-":
            return "sub";
        case "*":
            return "mul";
        case "/":
            return "div";
        case "&&":
            return "and";
        case "||":
            return "or";
        case "!":
            return "not";
        case "<<":
            return "lshift";
        case ">>":
            return "rshift";
        case "&":
            return "bwc";
        case "|":
            return "bwd";
        case "^":
            return "";
        default:
            return "";
        }
    }

    public boolean isOperator(String s) {
        return s.matches("\\+|\\-|\\*|/|!|&&|\\|\\||<<|>>|&|\\||\\^|\\(|\\)");
    }

    public String nextToken(String expression) throws Exception {
        int startIndex = index;
        if (Character.isLetterOrDigit(expression.charAt(index))) {
            while (++index < expression.length() && Character.isLetterOrDigit(expression.charAt(index))) {
            }
        } else {
            index = expression.length();
            while (!isOperator(expression.substring(startIndex, index--))) {
                // System.out.println("Checking:" + expression.substring(startIndex, index +
                // 1));
            }
            index++;
        }
        String token = expression.substring(startIndex, index);
        if (isReserved(token))
            throw new Exception("Illegal use of '" + token + "'");
        return token;
    }

    public void insertVariable(String var, int addr) throws Exception {
        if (isReserved(var) || !var.matches(".*[a-zA-Z]+.*"))
            throw new Exception("'" + var + "' is not a valid variable");
        variables.put(var, addr);
    }

    public boolean containsVariable(String var) {
        return variables.containsKey(var);
    }

    public int getVariable(String var) {
        return variables.get(var);
    }

    public Set<String> getAllVariables() {
        return variables.keySet();
    }

    public boolean isReserved(String var) {
        for (String reserve : reservedWords) {
            if (var.equals(reserve)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        MyCompiler compile = new MyCompiler();
        compile.variables.put("x", 15);
        compile.variables.put("y", 255);

        System.out.println(compile.evaluate("x=3+7"));
    }
}