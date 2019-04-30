
/**
 * Compiler
 */
import java.util.*;

public class MyCompiler {// Kevin

    private String[] reservedWords = { "if", "while", "end", "goto", "label", "halt" };// illegal variable names that
                                                                                       // are used
    // as commands
    private HashMap<String, Integer> variables = new HashMap<>();// Look up table for variable
    private Stack<Integer> jumpStack = new Stack<>();// manages the goto - label pairs
    private int jumpCount = 1;// labels are identified by a num. This var tracks the current jump number
    private int stackPointer;// pointer that starts at the bottom of the memory. Holds variable/array data +
    // functions as a stack for push/pop

    private int index = 0;// char index of the line being currently parsed
    private boolean isExpectingBool = false;// after a while/if statement. The following statement will be used as a
                                            // bool to
    // determine if should jump
    private Queue<String> insert;// Used to handle negative numbers. Allows extra num/operators to be inserted
    // during postfix()

    /**
     * Parses a high level line of code recursively and the stores the result in the
     * specified register
     * 
     * @param line    A high level line of code
     * @param reg     The register that will store the result
     * @param tempReg A temporary register that can be used as needed
     * @return Multlined assembly code for the parsed line
     * @throws Exception Exception if the high level code is invalid. Ex: Empty
     *                   expression, unbalanced [], ()...
     */
    public String evaluate(String line, int reg, int tempReg) throws Exception {
        if (!line.matches(".*[a-zA-Z0-9].*")) {// if there are no numbers or variables then its empty
            throw new Exception("Empty Expression");
        }

        StringBuilder out = new StringBuilder();// holds assembly code
        if (line.equals("halt")) {
            out.append("halt\n");
        } else if (line.equals("if")) {
            if (isExpectingBool)
                throw new Exception("missing condition for statement");
            isExpectingBool = true;
        } else if (line.equals("while")) {// creating label, adding num to stack for goto
            if (isExpectingBool)
                throw new Exception("missing condition for statement");
            isExpectingBool = true;
            jumpStack.push(-jumpCount);// neg number means that it sneeds a goto
            out.append(addLabel(jumpCount++));// add a label: check cond again after code runs
        } else if (line.equals("end")) {// end of while or if
            if (isExpectingBool)
                throw new Exception("missing condition for statement");
            if (jumpStack.isEmpty()) {
                throw new Exception("Missing condition for jump statement");
            }
            int labelAddr = jumpStack.pop();// label when cond fail. This is popped here but added below to invert order
                                            // of stack
            if (!jumpStack.isEmpty() && jumpStack.peek() < 0) {// Check if pair was for a while statement
                out.append(loadConst(0, reg));// always jump back to beginning of while
                out.append(addGoTo(-jumpStack.pop(), reg, tempReg));// add the go to
            }
            out.append(addLabel(labelAddr));// inserting label when if/while cond fails to skip code block
        } else {
            boolean isBoolNext = isExpectingBool;// true = previous statement was a if/while//storing it here so it
                                                 // doesn't affect the handleExpression() below
            isExpectingBool = false;

            String[] parts = divideStatement(line);// divide statement by '='// VAR = EXPRESSION => {VAR,EXPRESSION}

            if (parts == null) {// No equal sign was found - base case for recursive call below
                out.append(handleExpression(line, reg, tempReg));// handle expression by itself and save result to reg
            } else {
                String var = parts[0];
                String expression = parts[1];
                out.append(evaluate(expression, reg, tempReg));// recursively call evaluate() for inner statements (ex:
                                                               // x=y=1)
                out.append(saveVar(var, reg, tempReg));// save result from above to a var
            }
            if (isBoolNext) {// if previous statement was if/while - use reg as cond and add goto
                out.append(addGoTo(jumpCount, reg, tempReg));// the label pair will be added with the end command
                jumpStack.push(jumpCount++);
            }
        }
        // out.append(pop(reg));
        return out.toString();
    }

    /**
     * Separates a statement to the variable and expression. If not possible returns
     * null
     * 
     * @param line The statement to be separated
     * @return string array with two elements. {VAR,EXPRESSION} or null for an
     *         indivible statement
     * @throws Exception unbalanced statement with []
     */
    public String[] divideStatement(String line) throws Exception {
        int bracket = 0;// prevents separating statement within the [...] (ex: a[i=0]=1 -> should
                        // separate a[i=0], 1 instead of a[i ,]=1)
        for (int i = 0; i < line.length(); i++) {
            switch (line.charAt(i)) {
            case '[':
                bracket++;// positive value means within a [...]
                break;
            case ']':
                if (bracket-- <= 0)// unbalanced
                    throw new Exception("unexpected ']'");

                break;
            case '=':
                if (bracket == 0 && !isComparsionOperator(line.substring(i, i + 2))
                        && !isComparsionOperator(line.substring(i - 1, i + 1))) {
                    // checking if = is not within [...] and not part of '==' or '<='..(comparsion
                    // operators)
                    String[] out = { line.substring(0, i), line.substring(i + 1) };
                    return out;
                }
            }
        }
        if (bracket != 0)// unbalanced
            throw new Exception("expecting ']'");
        return null;
    }

    /**
     * Takes an expression (w/o =) and computes a single value and saves to a
     * register
     * 
     * @param expression Expression to be computed
     * @param reg        The register that the value will be saved to
     * @param tempReg    A temporary register for computing
     * @return Multilined assembly code for the expression that will be computed
     * @throws Exception Exception if the high level code is invalid. Ex: variable
     *                   was not initalized before the expression
     */
    public String handleExpression(String expression, int reg, int tempReg) throws Exception {
        StringBuilder out = new StringBuilder();
        Queue<String> q = postFix(expression);// goes through expression and returns the post fix ordering
        int pushCount = 0;
        while (!q.isEmpty()) {// go through all elements
            String token = q.poll();
            // System.out.println("Token: " + token);
            if (isValued(token)) {// if the token is a variable or constant or array element EX: x,0,x[0]
                out.append(load(token, reg, tempReg));// get value of token and load to reg (if var, get value of var)
                out.append(push(reg, tempReg));// push value to stack
                pushCount++;
            } else {// If the token is an operator
                if ((pushCount -= 2) < 0) {
                    throw new Exception("invalid syntax");
                }
                out.append(pop(tempReg));// get the previous two number to compute together from stack
                out.append(pop(reg));
                if (isComparsionOperator(token)) {// if it is a comparsion operator '==' or '<'...
                    out.append(handleComparsion(token, reg, tempReg));// save result in reg
                } else {
                    out.append(getOperator(token)).append(" ").append(reg).append(" ").append(tempReg).append("\n");
                    // handle op and store result in reg
                }
                out.append(push(reg, tempReg));// push result to the stack
                pushCount++;
            }
        }
        out.append(pop(reg));// get the remaining value from stack as solution to reg
        if (pushCount != 1) {
            throw new Exception("invalid syntax");
        }
        return out.toString();

    }

    /**
     * Loads variable or const to reg
     * 
     * @param val     Variable or const that will be evaluated and stored
     * @param reg     Register that will store the result
     * @param tempReg Temporary register for computing
     * @return Multilined assembly code for loading
     * @throws Exception Exception if the high level code is invalid. Ex: variable
     *                   was not initalized before the expression
     */
    public String load(String val, int reg, int tempReg) throws Exception {
        return isVariable(val) ? loadVar(val, reg, tempReg) : loadConst(val, reg);// check if var or const and evaluate
    }

    /**
     * Given the name of a variable, evaluate value and store to reg
     * 
     * @param var     Name of var
     * @param reg     Register to store value
     * @param tempReg Temporary register that may be used
     * @return Multilined Assembly code for loading
     * @throws Exception Exception if the high level code is invalid. Ex: variable
     *                   was not initalized before the expression
     */
    public String loadVar(String var, int reg, int tempReg) throws Exception {
        StringBuilder out = new StringBuilder();
        if (isIndexed(var)) {// if the var is refering to an array element EX: x[1]
            String[] parts = divideIndexedVariable(var);// divide variable to array name and index
            var = parts[0];
            String index = parts[1];
            out.append(evaluate(index, reg, tempReg));// Call evaluate to get value into reg
            out.append(loadAddress(var, tempReg));// get address of array
            out.append(add(reg, tempReg));// add array + index value to get element address

        } else {// Not array element
            out.append(loadAddress(var, reg));// just load variable address
        }
        out.append("load ").append(Integer.toHexString(reg)).append(" ").append(Integer.toHexString(reg)).append("\n");
        // load address to reg
        return out.toString();
    }

    /**
     * Store the value in reg to the variable var in memory
     * 
     * @param var     The variable that will store the value
     * @param reg     The register that contains the value
     * @param tempReg A temporary register for computations
     * @return Multiline assembly code to store the value to memory
     * @throws Exception Exception if the high level code is invalid. Ex: Unbalanced
     *                   brackets
     */
    public String saveVar(String var, int reg, int tempReg) throws Exception {
        StringBuilder out = new StringBuilder();
        // System.out.println("SAVE VAR: " + var);
        if (isIndexed(var)) {// if the var is an array element
            String[] parts = divideIndexedVariable(var);// divide the variable into array and index expression
            var = parts[0];
            String index = parts[1];
            out.append(push(reg, tempReg));// push value for storing to stack
            out.append(evaluate(index, reg, tempReg));// store value of index expression into reg
            out.append(loadAddress(var, tempReg));// get address of array reference to tempReg
            out.append(add(tempReg, reg));// value at tempReg += value at reg
            out.append(pop(reg));// get value from stack
        } else {// if var is not an array element
            out.append(loadAddress(var, tempReg));// save address to tempReg
        }
        out.append(store(tempReg, reg));// store value in reg to addr in tempReg
        return out.toString();
    }

    /**
     * Assuming that the string is a variable, checks if it referring to an array
     * element with []
     * 
     * @param s A variable that may be referring to an array element
     * @return true - it is an array element reference
     */
    public boolean isIndexed(String s) {
        return s.matches(".*[\\[\\]].*");
    }

    /**
     * Checks if the string is a variable
     * 
     * @param s Any string: constant, variable or other
     * @return true if the string is a variable
     * @throws Exception the variable was not initialized
     */
    public boolean isVariable(String s) throws Exception {
        if (!isValued(s) || isConst(s)) {// check if it is a constant or other
            return false;
        }
        s = isIndexed(s) ? divideIndexedVariable(s)[0] : s;// get array reference if it refers to an array element
        if (variables.containsKey(s))// found variable
            return true;
        throw new Exception(s + " was not initialized.");
    }

    /**
     * Checks if string is a variable or constant
     * 
     * @param s Any string: constant, variable, operator, or other
     * @return true if the string is a variable or constant otherwise false
     */
    public boolean isValued(String s) {
        return s.matches("[a-zA-Z0-9]+\\[.+\\]|[a-zA-Z0-9]+");
    }

    /**
     * Check if string is a constant
     * 
     * @param s Any string: constant, variable or other
     * @return true if the string is a constant otherwise false
     */
    public boolean isConst(String s) {
        return s.matches("[0-9]+");
    }

    /**
     * Divide array element into array reference and index expression
     * 
     * @param var the array element EX: x[2*i+1]
     * @return 2 element array as {ARRAY REF, INDEX EXPRESSION}
     * @throws Exception Not a valid array element
     */
    public String[] divideIndexedVariable(String var) throws Exception {
        if (!var.matches("[a-zA-Z0-9]+\\[.*\\]")) {// Check if valid (array_name[index] or array_name[] - declaration)
            throw new Exception("Invalid array element: " + var);
        }
        String[] parts = var.split("\\[", 2);// split string by first left bracket
        parts[1] = parts[1].substring(0, parts[1].length() - 1);// remove last character - the last right bracket
        return parts;
    }

    /**
     * Load a constant as a string or goto command
     * 
     * @param num A string for a constant or goto command
     * @param reg The register to load the value to
     * @return The multilined assembly code to load the value to the register
     */
    public String loadConst(String num, int reg) {
        if (isConst(num))
            return loadConst(Integer.parseInt(num), reg);// Constant
        return "loadc " + Integer.toHexString(reg) + "\n" + num + "\n";// Go To
    }

    /**
     * Load a constant as a int
     * 
     * @param num An int for a constant
     * @param reg The register to load the value to
     * @return The multilined assembly code to load the value to the register
     */
    public String loadConst(int num, int reg) {
        return "loadc " + Integer.toHexString(reg) + "\n0" + Integer.toHexString(num) + "\n";
    }

    /**
     * Load the address of a variable to a register
     * 
     * @param var The variable name to retrieve the address of
     * @param reg The register to load the address to
     * @return The multilined assembly code to load the address to the register
     * @throws Exception Invalid variable access. EX: The variable does not exist
     */
    public String loadAddress(String var, int reg) throws Exception {
        return "loadc " + Integer.toHexString(reg) + "\n" + addressAsHex(var) + "\n";
    }

    /**
     * Store the value from a register to the address stored in memory in another
     * register
     * 
     * @param addrReg Register that stores the address
     * @param valReg  Register that stores the value
     * @return The multilined assembly code to store the value to memory
     */
    public String store(int addrReg, int valReg) {
        StringBuilder out = new StringBuilder(0);
        return out.append("store ").append(Integer.toHexString(addrReg)).append(" ").append(Integer.toHexString(valReg))
                .append("\n").toString();
    }

    /**
     * Converts a base 10 string to a base 16 string
     * 
     * @param num The base 10 string
     * @return A base 16 string
     */
    public String numToHex(String num) {
        return Integer.toHexString(Integer.parseInt(num));
    }

    /**
     * Gets the address in hex of a variable name
     * 
     * @param var The variable name to get the address of
     * @return The address in memory in hex
     */
    public String addressAsHex(String var) throws Exception {
        if (!containsVariable(var)) {
            throw new Exception("variable " + var + " was not initialized");
        }
        return Integer.toHexString(getAddress(var));
    }

    /**
     * Uses the label number to write the label command in assembly
     * 
     * @param num The number for the label-goto pair
     * @return One line of assembly code to create a label
     */
    public String addLabel(int num) {
        return "label" + Integer.toHexString(num) + "\n";
    }

    /**
     * Uses the goto number to write the goto and jump command in assembly
     * 
     * @param num     The number for the label-goto pair
     * @param condReg The register that holds the condition to jump
     * @param tempReg A temporary register to hold the program line number
     * @return Multilined assembly code to create a goto and jump with a condition
     */
    public String addGoTo(int num, int condReg, int tempReg) {
        return loadConst("goto" + Integer.toHexString(num), tempReg) + "if " + condReg + " " + tempReg + "\n";
    }

    /**
     * Converts infix as a string to postfix as a queue
     * 
     * @param expression The expression in the infix form
     * @return The expression in the postfix form as a queue
     * @throws Exception Invalid expression. EX: unbalanced parenthesis
     */
    public Queue<String> postFix(String expression) throws Exception {
        Queue<String> out = new LinkedList<>();
        insert = new LinkedList<>();// Used to handle negative numbers. Allows extra num/operators to be inserted
        Stack<String> operators = new Stack<>();// handles the operators in the correct order
        String prev = null;
        index = 0;// Start looking at the expression from the first character. Used in nextToken()
        while (index < expression.length() || !insert.isEmpty()) {// while there are more characters in the expression
                                                                  // or items to be inserted
            String token = nextToken(expression);
            if (isValued(token)) {// constant or variable
                out.add(token);
            } else if (token.equals("(")) {// start ()
                operators.push(token);
            } else if (token.equals(")")) {// found end of () -> search for (
                if (operators.isEmpty()) {
                    throw new Exception("expecting '('");
                }
                while (!operators.peek().equals("(")) {
                    out.add(operators.pop());
                    if (operators.isEmpty())// could not find (
                        throw new Exception("expecting '('");
                }
                operators.pop();

            } else if (token.equals("-") && (prev == null || (isOperator(prev) && !prev.equals(")")))) {
                // detecting - as negative numbers, instead of subtraction
                // converting negative operator to a subtraction
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
        while (!operators.isEmpty()) {// add any remaining operators
            if (operators.peek().equals("(")) {
                throw new Exception("Expecting ')'");
            }
            out.add(operators.pop());

        }
        return out;
    }

    /**
     * Determines the rank of an operator for postfix conversion
     * 
     * @param op The operator to determine rank
     * @return The rank of the operator to compare with other operators
     * @throws Exception Unknown operator
     */
    public int precedence(String op) throws Exception {
        switch (op) {// Precedence is based on the Java Programming Language
        case "=":
            return 0;
        case "||":
            return 3;
        case "&&":
            return 4;
        case "|":
            return 5;
        case "&":
            return 7;
        case "==":
        case "!=":
            return 8;
        case ">":
        case "<":
        case ">=":
        case "<=":
            return 9;
        case ">>":
        case "<<":
            return 10;
        case "+":
        case "-":
            return 11;
        case "*":
        case "/":
            return 12;
        case "(":
        case ")":
        case "[":
        case "]":
            return 16;
        default:
            throw new Exception("Unknown operator: " + op);
        }
    }

    /**
     * Retrieves the assembly command to the corresponding operator symbol
     * 
     * @param op The operator as a symbol
     * @return The assembly command corresponding to the symbol
     * @throws Exception Unknown operator
     */
    public String getOperator(String op) throws Exception {
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
        default:
            throw new Exception("Unknown operator: " + op);
        }
    }

    /**
     * Compares the value in two registers based on the comparsion operator and
     * stores the result in the first register
     * 
     * @param op   The comparsion operator to compare the values
     * @param regA The register that holds the first value and stores the result
     * @param regB The register that holds the second value
     * @return Multiline assembly code to compare the registers and store the result
     * @throws Exception Unknown comparsion operator
     */
    public String handleComparsion(String op, int regA, int regB) throws Exception {
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
            throw new Exception("Unknown comparsion operator: " + op);
        }
    }

    /**
     * Determines if a string is an operator
     * 
     * @param s The operator, variable or constant that is being checked
     * @return true - the string is an operator, otherwise - false
     */
    public boolean isOperator(String s) {
        return s.matches("\\+|\\-|\\*|/|!|&&|\\|\\||<<|>>|&|\\||\\^|\\(|\\)") || isComparsionOperator(s);
    }

    /**
     * Determines if the string is a comparsion operator
     * 
     * @param s The operator, variable or constant that is being checked
     * @return true - the string is a comparsion operator, otherwise - false
     */
    public boolean isComparsionOperator(String s) {
        return s.matches(">|<|==|!=|>=|<=");
    }

    /**
     * Retrieves the next token of the expression based on the current index(class
     * var)
     * 
     * @param expression The expression to extract a token from
     * @return The next token of the expression
     * @throws Exception Illegal expression. EX: use of reserved words
     */
    public String nextToken(String expression) throws Exception {
        if (!insert.isEmpty())// check if any tokens were inserted. EX: Negative number handling
            return insert.poll();
        int startIndex = index;// store start of token index
        int bracket = 0;// track [] to prevent dividing an array element
        if (Character.isLetterOrDigit(expression.charAt(index))) {// handles constants and variables
            while (++index < expression.length() && (Character.isLetterOrDigit(expression.charAt(index))// go to end of
                                                                                                        // constant or
                                                                                                        // variable
                    || expression.charAt(index) == '[' || bracket > 0)) {
                if (expression.charAt(index) == '[') {// handle []
                    bracket++;
                } else if (expression.charAt(index) == ']') {// handle []
                    if (--bracket == 0) {
                        index++;
                        break;
                    }
                }
            }
        } else {// handle operators
            index = expression.length();
            while (!isOperator(expression.substring(startIndex, index--))) {// search for largest valid operator from
                                                                            // the end of the expression
            }
            index++;
        }
        String token = expression.substring(startIndex, index);// retrieve token
        if (isReserved(token))// check if variable name is valid
            throw new Exception("Illegal use of '" + token + "'");
        return token;
    }

    /**
     * Assembly code for adding two values in registers
     * 
     * @param regA The register that holds the first value
     * @param regB The register that holds the second value
     * @return The assembly code for adding two registers
     */
    public String add(int regA, int regB) {
        return "add " + regA + " " + regB + "\n";
    }

    /**
     * Assembly code for subtracting two values in registers
     * 
     * @param regA The register that holds the first value
     * @param regB The register that holds the second value
     * @return The assembly code for subtracting two registers
     */
    public String sub(int regA, int regB) {
        return "sub " + regA + " " + regB + "\n";
    }

    /**
     * Assembly code for oring two values in registers
     * 
     * @param regA The register that holds the first value
     * @param regB The register that holds the second value
     * @return The assembly code for oring two registers
     */
    public String or(int regA, int regB) {
        return "or " + regA + " " + regB + "\n";
    }

    /**
     * Assembly code for bitwise oring two values in registers
     * 
     * @param regA The register that holds the first value
     * @param regB The register that holds the second value
     * @return The assembly code for bitwise oring two registers
     */
    public String bitOr(int regA, int regB) {
        return "bwd " + regA + " " + regB + "\n";
    }

    /**
     * Assembly code for anding two values in registers
     * 
     * @param regA The register that holds the first value
     * @param regB The register that holds the second value
     * @return The assembly code for anding two registers
     */
    public String and(int regA, int regB) {
        return "and " + regA + " " + regB + "\n";
    }

    /**
     * Toogles a boolean in a register
     * 
     * @param reg The register that holds the obolean
     * @return The assembly code for noting the register
     */
    public String not(int reg) {
        return not(reg, reg);
    }

    /**
     * Toggles a boolean in a register and stores it in another register
     * 
     * @param ansReg The register that stores the result
     * @param reg    The register that holds the inital value
     * @return The assembly code for noting a value to a register
     */
    public String not(int ansReg, int reg) {
        return "not " + ansReg + " " + reg + "\n";
    }

    /**
     * Move the value from a register to another register
     * 
     * @param regA The source register
     * @param regB The destination register
     * @return The assembly code for moving the value
     */
    public String move(int regA, int regB) {
        return loadConst(0, regB) + add(regB, regA);
    }

    /**
     * Determines if a register is greater than another register and stores the
     * result in regA
     * 
     * @param regA The register for comparision and storing the result
     * @param regB The register for comparsion
     * @return Assembly code that will store a value greater then 0 if comparsion is
     *         true otherwise less then 1
     */
    public String greaterThan(int regA, int regB) {// regB , regA need to be pushed in that order
        return sub(regA, regB);// a=a-b
    }

    /**
     * Determines if a register is less than another register and stores the result
     * in regA
     * 
     * @param regA The register for comparision and storing the result
     * @param regB The register for comparsion
     * @return Assembly code that will store a value greater then 0 if comparsion is
     *         true otherwise less then 1
     */
    public String lessThan(int regA, int regB) {
        return sub(regB, regA) + move(regB, regA);// a=b-a
    }

    /**
     * Determines if a register is equal to another register and stores the result
     * in regA
     * 
     * @param regA The register for comparision and storing the result
     * @param regB The register for comparsion
     * @return Assembly code that will store a value greater then 0 if comparsion is
     *         true otherwise less then 1
     */
    public String equalTo(int regA, int regB) {
        return greaterThan(regA, regB) + not(regA);// a=!(a-b)
    }

    /**
     * Determines if a register is not equal another register and stores the result
     * in regA
     * 
     * @param regA The register for comparision and storing the result
     * @param regB The register for comparsion
     * @return Assembly code that will store a value greater then 0 if comparsion is
     *         true otherwise less then 1
     */
    public String notEqual(int regA, int regB) {
        return equalTo(regA, regB) + not(regA);// a=!!(a-b)//This works because either it is nonzero -> zero -> one or
                                               // zero -> one -> zero
    }

    /**
     * Determines if a register is less than or equal toanother register and stores
     * the result in regA
     * 
     * @param regA The register for comparision and storing the result
     * @param regB The register for comparsion
     * @return Assembly code that will store a value greater then 0 if comparsion is
     *         true otherwise less then 1
     */
    public String lessEqual(int regA, int regB) {
        StringBuilder out = new StringBuilder();
        out.append(lessThan(regA, regB));// if regA < regB ? regA > 0 else regA <= 0
        out.append(loadConst(1, regB));
        out.append(bitOr(regA, regB));// (+ -> + or - -> - or 0 -> +)
        return out.toString();
    }

    /**
     * Determines if a register is greater than or equal another register and stores
     * the result in regA
     * 
     * @param regA The register for comparision and storing the result
     * @param regB The register for comparsion
     * @return Assembly code that will store a value greater then 0 if comparsion is
     *         true otherwise less then 1
     */
    public String greaterEqual(int regA, int regB) {
        StringBuilder out = new StringBuilder();
        out.append(greaterThan(regA, regB));// if regA > regB ? regA > 0 else regA <= 0
        out.append(loadConst(1, regB));
        out.append(bitOr(regA, regB));// (+ -> + or - -> - or 0 -> +)
        return out.toString();
    }

    /**
     * Dedicates a length of space of memory for a variable and stores the address.
     * 
     * @param var  The name of variable to store
     * @param size The size of the space to dedicate. Standard variables are 1.
     *             Arrays can be any valid length.
     * @throws Exception The name of variable is invalid
     */
    public void insertVariable(String var, int size) throws Exception {
        if (isReserved(var) || !var.matches("[a-zA-Z]+[a-zA-Z0-9]*"))
            throw new Exception("'" + var + "' is not a valid variable");
        variables.put(var, stackPointer -= size);
    }

    /**
     * Checks if the variable has been stored in memory
     * 
     * @param var The name of variable in check
     * @return //true if the variable has been added otherwise false
     */
    public boolean containsVariable(String var) {
        return variables.containsKey(var);
    }

    /**
     * Get the address of a variable as a base 10 int
     * 
     * @param var The name of variable
     * @return The address as a base 10 int
     */
    public int getAddress(String var) {
        return variables.get(var);
    }

    /**
     * Retrieves all the names of the variables in a set
     * 
     * @return The names of all the variables
     */
    public Set<String> getAllVariables() {
        return variables.keySet();
    }

    /**
     * Checks if the variable name is also a reserved word
     * 
     * @param var The name of variable in check
     * @return true if the name is reserved, otherwise false
     */
    public boolean isReserved(String var) {
        for (String reserve : reservedWords) {
            if (var.equals(reserve)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifies if any variables have been inserted
     * 
     * @return true if a variable exists, otherwise false
     */
    public boolean hasVariables() {
        return !variables.isEmpty();
    }

    /**
     * Pushes the value in reg to the stack
     * 
     * @param reg     The register that holds the value
     * @param tempReg A temporary register to complete the process
     * @return Multiline assembly code to push the value from the register to the
     *         stack
     */
    public String push(int reg, int tempReg) {
        StringBuilder out = new StringBuilder();
        out.append(loadConst(--stackPointer, tempReg));// get address in the stack
        out.append(store(tempReg, reg));
        return out.toString();
    }

    /**
     * Retrieve the value from the stack to a reg
     * 
     * @param reg The register that stores the value
     * @return Multiline assembly code to pull the value from the stack to the
     *         register
     */
    public String pop(int reg) {
        StringBuilder out = new StringBuilder();
        out.append(loadConst(stackPointer++, reg));// move the address to the next position
        out.append("load ").append(reg).append(" ").append(reg).append("\n");
        return out.toString();
    }

    /**
     * Verifies that the compiler isn't expecting any additional elements
     * 
     * @throws Exception if the if/while statement didn't close
     */
    public void close() throws Exception {
        if (!jumpStack.isEmpty()) {
            throw new Exception("expected 'end'");
        }
    }

    /**
     * Creates a compiler
     * 
     * @param stackPointer The address for the top of the stack in memory
     */
    public MyCompiler(int stackPointer) {
        this.stackPointer = stackPointer;
    }
}