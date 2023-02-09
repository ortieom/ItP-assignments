import java.util.Scanner;

public class Main {
    /**
     * Console scanner input.
     */
    private final Scanner scanner = new Scanner(System.in);

    /**
     * main.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Main main = new Main();  // kludge to make everything non-static available for static main()

        // array of different implementations of calculator
        Calculator[] calculators = {new IntegerCalculator(), new DoubleCalculator(), new StringCalculator()};

        // reading type of calculator
        CalculatorType calculatorType = main.readCalculator();
        if (calculatorType == CalculatorType.INCORRECT) {
            main.reportFatalError("Wrong calculator type");
        }

        // choosing required calculator
        Calculator calculator = calculators[calculatorType.ordinal()];

        // reading total amount of commands
        int commandsNumber = main.readCommandsNumber();
        if (!(1 <= commandsNumber && commandsNumber <= 50)) {  // invalid value
            main.reportFatalError("Amount of commands is Not a Number");
        }

        // reading operations and evaluating them
        for (int i = 0; i < commandsNumber; i++) {
            String line = main.scanner.nextLine().replace("\n", "");
            String[] command = line.split(" ");
            OperationType operation = main.parseOperation(command[0]);
            System.out.println(operation.eval(calculator, command[1], command[2]));
        }

    }

    /**
     * Reads input and returns CalculatorType.
     *
     * @return type of calculator
     */
    private CalculatorType readCalculator() {
        String line = scanner.nextLine().replace("\n", "");
        for (CalculatorType calculator : CalculatorType.values()) {
            if (line.equals(calculator.toString())) {  // hit calculator type
                return calculator;
            }
        }
        // no such calculator type
        return CalculatorType.INCORRECT;
    }

    /**
     * Reads input and returns int.
     *
     * @return number of commands
     */
    private int readCommandsNumber() {
        String line = scanner.nextLine().replace("\n", "");
        // checking if line consists only of numbers
        boolean isCorrect = true;
        for (int i = 0; i < line.length() && isCorrect; i++) {
            char c = line.charAt(i);
            if (!('0' <= c && c <= '9')) {
                isCorrect = false;
                break;
            }
        }

        if (isCorrect) {
            return Integer.parseInt(line);
        } else {
            return -1;
        }
    }

    /**
     * Writes error to console and finishes program.
     *
     * @param err string that should be displayed in console
     */
    private void reportFatalError(String err) {
        System.out.println(err);
        System.exit(0);
    }

    /**
     * Reads operation.
     *
     * @param operation string with operation from input
     * @return type of operation
     */
    private OperationType parseOperation(String operation) {
        if (operation.length() != 1) {  // all operations must be 1 char long
            return OperationType.INCORRECT;
        }
        for (OperationType operationType : OperationType.values()) {
            if (operation.charAt(0) == operationType.getRepresentativeSymbol()) {  // hit operation type
                return operationType;
            }
        }
        // miss operation type
        return OperationType.INCORRECT;
    }
}

/**
 * Indicates type of calculator.
 */
enum CalculatorType {
    /**
     * INTEGER indicates that IntegerCalculator should be used.
     */
    INTEGER,
    /**
     * DOUBLE indicates that DoubleCalculator should be used.
     */
    DOUBLE,
    /**
     * STRING indicates that StringCalculator should be used.
     */
    STRING,
    /**
     * INCORRECT indicates unexpected datatype.
     */
    INCORRECT
}

/**
 * Indicates type of operation.
 */
enum OperationType {
    /**
     * ADDITION indicates that summation operation should be used.
     */
    ADDITION('+') {
        @Override
        String eval(Calculator calc, String a, String b) {
            return calc.add(a, b);
        }
    },
    /**
     * SUBTRACTION indicates that subtraction operation should be used.
     */
    SUBTRACTION('-') {
        @Override
        String eval(Calculator calc, String a, String b) {
            return calc.subtract(a, b);
        }
    },
    /**
     * MULTIPLICATION indicates that multiplication operation should be used.
     */
    MULTIPLICATION('*') {
        @Override
        String eval(Calculator calc, String a, String b) {
            return calc.multiply(a, b);
        }
    },
    /**
     * DIVISION indicates that division operation should be used.
     */
    DIVISION('/') {
        @Override
        String eval(Calculator calc, String a, String b) {
            return calc.divide(a, b);
        }
    },
    /**
     * INCORRECT indicates unexpected operation type.
     */
    INCORRECT('!') {
        @Override
        String eval(Calculator calc, String a, String b) {
            return "Wrong operation type";
        }
    };

    abstract String eval(Calculator calc, String a, String b);  // eval function to execute corresponding operation

    /**
     * Contains symbol that is associated with operation in an input command.
     * getter: getRepresentativeSymbol()
     */
    private final char representativeSymbol;

    /**
     * Getter for representativeSymbol.
     *
     * @return representativeSymbol
     */
    public char getRepresentativeSymbol() {
        return representativeSymbol;
    }

    OperationType(char symbol) {
        representativeSymbol = symbol;
    }
}

/**
 * Abstract calculator.
 */
abstract class Calculator {
    /**
     * @param a string, 1st argument
     * @param b string, 2nd argument
     * @return a + b
     */
    public abstract String add(String a, String b);

    /**
     * @param a string, 1st argument
     * @param b string, 2nd argument
     * @return a - b
     */
    public abstract String subtract(String a, String b);

    /**
     * @param a string, 1st argument
     * @param b string, 2nd argument
     * @return a * b
     */
    public abstract String multiply(String a, String b);

    /**
     * @param a string, 1st argument
     * @param b string, 2nd argument
     * @return a / b
     */
    public abstract String divide(String a, String b);
}

/**
 * Implementation of calculator for integers.
 */
class IntegerCalculator extends Calculator {
    /**
     * Checks if argument is a valid integer.
     *
     * @param a input argument
     * @return boolean
     */
    private boolean checkArgument(String a) {
        boolean isCorrect = true;
        for (int i = 0; i < a.length() && isCorrect; i++) {
            char c = a.charAt(i);
            if (!(('0' <= c && c <= '9') || c == '-')) {
                isCorrect = false;
            }
        }
        return isCorrect;
    }

    @Override
    public String add(String a, String b) {
        if (!checkArgument(a) || !checkArgument(b)) {
            return "Wrong argument type";
        }
        int c1 = Integer.parseInt(a);
        int c2 = Integer.parseInt(b);
        return String.valueOf(c1 + c2);
    }

    @Override
    public String subtract(String a, String b) {
        if (!checkArgument(a) || !checkArgument(b)) {
            return "Wrong argument type";
        }
        int c1 = Integer.parseInt(a);
        int c2 = Integer.parseInt(b);
        return String.valueOf(c1 - c2);
    }

    @Override
    public String multiply(String a, String b) {
        if (!checkArgument(a) || !checkArgument(b)) {
            return "Wrong argument type";
        }
        int c1 = Integer.parseInt(a);
        int c2 = Integer.parseInt(b);
        return String.valueOf(c1 * c2);
    }

    @Override
    public String divide(String a, String b) {
        if (!checkArgument(a) || !checkArgument(b)) {
            return "Wrong argument type";
        }
        int c1 = Integer.parseInt(a);
        int c2 = Integer.parseInt(b);
        if (c2 == 0) {
            return "Division by zero";
        } else {
            return String.valueOf(c1 / c2);
        }
    }
}

/**
 * Implementation of calculator for doubles.
 */
class DoubleCalculator extends Calculator {
    /**
     * Checks if argument is a valid double.
     *
     * @param a input argument
     * @return boolean
     */
    private boolean checkArgument(String a) {
        boolean isCorrect = true;
        for (int i = 0; i < a.length() && isCorrect; i++) {
            char c = a.charAt(i);
            if (!(('0' <= c && c <= '9') || c == '-' || c == '.')) {
                isCorrect = false;
                break;
            }
        }
        return isCorrect;
    }

    @Override
    public String add(String a, String b) {
        if (!checkArgument(a) || !checkArgument(b)) {
            return "Wrong argument type";
        }
        double c1 = Double.parseDouble(a);
        double c2 = Double.parseDouble(b);
        return String.valueOf(c1 + c2);
    }

    @Override
    public String subtract(String a, String b) {
        if (!checkArgument(a) || !checkArgument(b)) {
            return "Wrong argument type";
        }
        double c1 = Double.parseDouble(a);
        double c2 = Double.parseDouble(b);
        return String.valueOf(c1 - c2);
    }

    @Override
    public String multiply(String a, String b) {
        if (!checkArgument(a) || !checkArgument(b)) {
            return "Wrong argument type";
        }
        double c1 = Double.parseDouble(a);
        double c2 = Double.parseDouble(b);
        return String.valueOf(c1 * c2);
    }

    @Override
    public String divide(String a, String b) {
        if (!checkArgument(a) || !checkArgument(b)) {
            return "Wrong argument type";
        }
        double c1 = Double.parseDouble(a);
        double c2 = Double.parseDouble(b);
        if (c2 == 0) {
            return "Division by zero";
        } else {
            return String.valueOf(c1 / c2);
        }
    }
}

/**
 * Implementation of calculator for strings.
 */
class StringCalculator extends Calculator {
    @Override
    public String add(String a, String b) {
        return a + b;
    }

    @Override
    public String subtract(String a, String b) {
        return "Unsupported operation for strings";
    }

    @Override
    public String multiply(String a, String b) {
        // checking if 2nd argument is positive integer
        boolean isCorrect = true;
        for (int i = 0; i < b.length() && isCorrect; i++) {
            char c = b.charAt(i);
            if (!('0' <= c && c <= '9')) {
                isCorrect = false;
            }
        }
        if (!isCorrect) {
            return "Wrong argument type";
        }
        int repeatCnt = Integer.parseInt(b);
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < repeatCnt; i++) {
            res.append(a);
        }
        return res.toString();
    }

    @Override
    public String divide(String a, String b) {
        return "Unsupported operation for strings";
    }
}
