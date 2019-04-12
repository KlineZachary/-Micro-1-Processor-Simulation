
/**
 * Compiler
 */
import java.util.*;

public class MyCompiler {// Kevin

    String[] reservedWords = { "if", "while", "end" };
    HashMap<String, Integer> variables = new HashMap<>();
    Stack<Integer> jumpStack = new Stack<>();
    int jumpCount = 1;
    int stackPointer;

    // x = 5 -> loadc 0; #x_addr; loadc 1; 5; store 0 1;
    // y = x -> loadc 0; #y_addr; loadc 1; #x_addr; load 1 1; store 0 1;
    // x = 3 + 2 -> loadc 0; #x_addr; loadc 1; 3; loadc 2; 2; add 1 2; store 0 1;
    // x = 9 + 8 + 7 -> loadc 0; #x_addr; loadc 1; 9; loadc 2; 8; add 1 2; loadc 2;
    // 7; add 1 2; store 0 1;

    int index = 0;
    boolean isExpectingBool = false;
    Queue<String> insert;

    public String evaluate(String line, int reg, int tempReg) throws Exception {
        if (!line.matches(".*[a-zA-Z0-9].*")) {
            throw new Exception("Empty Expression");
        }
        StringBuilder out = new StringBuilder();
        if (line.equals("if")) {
            isExpectingBool = true;
        } else if (line.equals("while")) {
            isExpectingBool = true;
            out.append(addLabel(jumpCount));
            jumpStack.push(-1 * (jumpCount++));
        } else if (line.equals("end")) {
            int elseNum = jumpStack.pop();
            if (!jumpStack.isEmpty()) {
                int whileNum = jumpStack.pop();
                if (whileNum > 0) {
                    jumpStack.push(whileNum);
                } else {
                    out.append(loadConst(1, reg)).append(loadConst("goto " + Integer.toHexString(-whileNum), tempReg))
                            .append("if " + reg + " " + tempReg + "\n");
                }
            }
            out.append(addLabel(elseNum));
        } else if (!line.contains("=")) {
            out.append(handleExpression(line, reg, tempReg));
            if (isExpectingBool) {
                out.append(loadConst("goto " + Integer.toHexString(jumpCount), tempReg));
                out.append(not(reg));
                out.append("if ").append(reg).append(" ").append(tempReg).append("\n");
                jumpStack.push(jumpCount++);
                isExpectingBool = false;
            }
        } else {
            String[] parts = divideStatement(line);
            String var = parts[0];
            String expression = parts[1];
            out.append(evaluate(expression, reg, tempReg));
            out.append(saveVar(var, reg, tempReg));
        }
        // out.append(pop(reg));
        return out.toString();
    }

    public String[] divideStatement(String line) throws Exception {
        String[] parts = line.split("=", 2);
        if (parts.length == 1) {
            throw new Exception("Empty Expression");
        }
        return parts;
    }

    public String handleExpression(String expression, int reg, int tempReg) throws Exception {
        StringBuilder out = new StringBuilder();
        index = 0;
        Queue<String> q = postFix(expression);
        while (!q.isEmpty()) {
            String token = q.poll();
            if (isValued(token)) {
                out.append(load(token, reg, tempReg));
                out.append(push(reg, tempReg));
            } else {
                out.append(pop(tempReg));
                out.append(pop(reg));
                if (isComparsionOperator(token)) {
                    out.append(handleComparsion(token, reg, tempReg));
                } else {
                    out.append(getOperator(token)).append(" ").append(reg).append(" ").append(tempReg).append("\n");
                }
                out.append(push(reg, tempReg));

            }
        }
        out.append(pop(reg));
        return out.toString();

    }

    public String load(String num, int reg, int tempReg) throws Exception {
        return isVariable(num) ? loadVar(num, reg, tempReg) : loadConst(num, reg);
    }

    public String loadVar(String var, int reg, int tempReg) throws Exception {
        StringBuilder out = new StringBuilder();
        if (isIndexed(var)) {
            String[] parts = divideIndexedVariable(var);
            var = parts[0];
            String index = parts[1];
            out.append(evaluate(index, reg, tempReg));
            out.append(loadAddress(var, tempReg));
            out.append(add(reg, tempReg));

        } else {
            out.append(loadAddress(var, reg));
        }
        out.append("load ").append(Integer.toHexString(reg)).append(" ").append(Integer.toHexString(reg)).append("\n");
        return out.toString();
    }

    public String saveVar(String var, int reg, int tempReg) throws Exception {
        StringBuilder out = new StringBuilder();
        if (isIndexed(var)) {
            String[] parts = divideIndexedVariable(var);
            var = parts[0];
            String index = parts[1];

            out.append(push(reg, tempReg));
            out.append(push(reg, tempReg));
            out.append(evaluate(index, reg, tempReg));
            out.append(push(reg, tempReg));
            out.append(loadAddress(var, reg));
            out.append(pop(tempReg));
            out.append(add(reg, tempReg));
            out.append(pop(tempReg));
            out.append("store ").append(Integer.toHexString(reg)).append(" ").append(Integer.toHexString(tempReg))
                    .append("\n");
        } else {
            out.append(push(reg, tempReg));
            out.append(push(reg, tempReg));
            out.append(loadAddress(var, reg));
            out.append(pop(tempReg));
            out.append("store ").append(Integer.toHexString(reg)).append(" ").append(Integer.toHexString(tempReg))
                    .append("\n");
        }
        out.append(pop(reg));
        return out.toString();
    }

    public boolean isIndexed(String s) {
        return s.matches(".*[\\[\\]].*");
    }

    public boolean isVariable(String s) throws Exception {
        if (!isValued(s)) {
            return false;
        }
        s = isIndexed(s) ? divideIndexedVariable(s)[0] : s;
        return variables.containsKey(s);
    }

    public boolean isValued(String s) throws Exception {
        return s.matches("[a-zA-Z0-9]+\\[.+\\]|[a-zA-Z0-9]+");
    }

    public String[] divideIndexedVariable(String var) throws Exception {
        if (!var.matches("[a-zA-Z0-9]+\\[.*\\]")) {
            throw new Exception("Invalid array element: " + var);
        }
        String[] parts = var.split("\\[", 2);
        parts[1] = parts[1].substring(0, parts[1].length() - 1);
        return parts;
    }

    public String loadConst(String num, int reg) throws Exception {
        if (num.matches("[0-9]+"))
            return loadConst(Integer.parseInt(num), reg);
        return "loadc " + Integer.toHexString(reg) + "\n" + num + "\n";
    }

    public String loadConst(int num, int reg) {
        return "loadc " + Integer.toHexString(reg) + "\n" + Integer.toHexString(num) + "\n";
    }

    public String loadAddress(String var, int reg) {
        return "loadc " + Integer.toHexString(reg) + "\n" + parseVar(var) + "\n";
    }

    public String parse(String num) {
        return Integer.toHexString(Integer.parseInt(num));
    }

    public String parseVar(String var) {
        return Integer.toHexString(variables.get(var));
    }

    public String addLabel(int num) {
        return "label" + Integer.toHexString(num) + "\n";
    }

    public Queue<String> postFix(String expression) throws Exception {
        Queue<String> out = new LinkedList<>();
        insert = new LinkedList<>();
        Stack<String> operators = new Stack<>();
        String prev = null;
        while (index < expression.length() || !insert.isEmpty()) {
            String token = nextToken(expression);
            if (isValued(token)) {
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

            } else if (token.equals("-") && (prev == null || (isOperator(prev) && !prev.equals(")")))) {
                insert.add("0");
                insert.add("-");
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
            prev = token;
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
            return "halt";
        default:
            return "halt";
        }
    }

    public String handleComparsion(String op, int regA, int regB) {
        switch (op) {
        case ">":
            return greaterThan(regA, regB);
        case "<":
            return lessThan(regA, regB);
        case ">=":
            return greaterEqual(regA, regB);
        case "<=":
            return lessEqual(regA, regB);
        case "==":
            return equalTo(regA, regB);
        case "!=":
            return notEqual(regA, regB);
        default:
            return "halt";
        }
    }

    public boolean isOperator(String s) {
        return s.matches("\\+|\\-|\\*|/|!|&&|\\|\\||<<|>>|&|\\||\\^|\\(|\\)") || isComparsionOperator(s);
    }

    public boolean isComparsionOperator(String s) {
        return s.matches(">|<|==|!=|>=|<=");
    }

    public String nextToken(String expression) throws Exception {
        if (!insert.isEmpty())
            return insert.poll();
        int startIndex = index;
        int bracket = 0;
        if (Character.isLetterOrDigit(expression.charAt(index))) {
            while (++index < expression.length() && (Character.isLetterOrDigit(expression.charAt(index))
                    || expression.charAt(index) == '[' || bracket > 0)) {
                if (expression.charAt(index) == '[') {
                    bracket++;
                } else if (expression.charAt(index) == ']') {
                    if (--bracket == 0) {
                        index++;
                        break;
                    }
                }
            }
        } else {
            index = expression.length();
            while (!isOperator(expression.substring(startIndex, index--))) {
            }
            index++;
        }
        String token = expression.substring(startIndex, index);
        if (isReserved(token))
            throw new Exception("Illegal use of '" + token + "'");
        return token;
    }

    public String add(int regA, int regB) {
        return "add " + regA + " " + regB + "\n";
    }

    public String sub(int regA, int regB) {
        return "sub " + regA + " " + regB + "\n";
    }

    public String or(int regA, int regB) {
        return "or " + regA + " " + regB + "\n";
    }

    public String bitOr(int regA, int regB) {
        return "bwd " + regA + " " + regB + "\n";
    }

    public String and(int regA, int regB) {
        return "and " + regA + " " + regB + "\n";
    }

    public String not(int reg) {
        return not(reg, reg);
    }

    public String not(int ansReg, int reg) {
        return "not " + ansReg + " " + reg + "\n";
    }

    public String move(int regA, int regB) {
        return loadConst(0, regB) + add(regB, regA);
    }

    public String greaterThan(int regA, int regB) {// regB , regA need to be pushed in that order
        return sub(regA, regB);
    }

    public String lessThan(int regA, int regB) {
        return sub(regB, regA) + move(regB, regA);
    }

    public String equalTo(int regA, int regB) {
        return greaterThan(regA, regB) + not(regA);
    }

    public String notEqual(int regA, int regB) {
        return equalTo(regA, regB) + not(regA);
    }

    public String lessEqual(int regA, int regB) {
        StringBuilder out = new StringBuilder();
        out.append(lessThan(regA, regB));
        out.append(loadConst(1, regB));
        out.append(bitOr(regA, regB));
        return out.toString();
    }

    public String greaterEqual(int regA, int regB) {
        StringBuilder out = new StringBuilder();
        out.append(greaterThan(regA, regB));
        out.append(loadConst(1, regB));
        out.append(bitOr(regA, regB));
        return out.toString();
    }

    public void insertVariable(String var, int size) throws Exception {
        if (isReserved(var) || !var.matches(".*[a-zA-Z]+.*"))
            throw new Exception("'" + var + "' is not a valid variable");
        variables.put(var, stackPointer -= size);
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

    public String push(int reg, int tempReg) {
        StringBuilder out = new StringBuilder();
        out.append(loadConst(--stackPointer, tempReg));
        out.append("store ").append(tempReg).append(" ").append(reg).append("\n");
        return out.toString();
    }

    public String pop(int reg) {
        StringBuilder out = new StringBuilder();
        out.append(loadConst(stackPointer++, reg));
        out.append("load ").append(reg).append(" ").append(reg).append("\n");
        return out.toString();
    }

    public MyCompiler(int stackPointer) {
        this.stackPointer = stackPointer;
    }

    public static void main(String[] args) throws Exception {
        MyCompiler compile = new MyCompiler(256);
        String[] vars = { "c" };
        int i = 255;
        for (String var : vars)
            compile.variables.put(var, i--);

        System.out.println("RESULTS:\n" + compile.evaluate("c=7+3-2", 0, 1));
    }
}