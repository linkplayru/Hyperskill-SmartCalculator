import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator {

    private Pattern numberRgx = Pattern.compile("\\d+");
    private Pattern variableRgx = Pattern.compile("[a-z]+");
    private Pattern operatorRgx = Pattern.compile("[*/+\\-^]");
    private Pattern bracketRgx = Pattern.compile("[()]");
    private Pattern equalRgx = Pattern.compile("=");
    private Pattern cmdRgx = Pattern.compile("/.*");

    private InstructionComparator comparator = new InstructionComparator();

    private static Map<String, String> variables = new HashMap<>();

    public boolean input(String input) {
        if (input.isEmpty()) {
            //do nothing
            return true;
        } else if (cmdRgx.matcher(input).matches()) {
            return handleCmd(input);
        } else {
            handleExp(input);
            return true;
        }
    }

    private boolean handleCmd(String cmd) {
        switch (cmd) {
            case "/exit":
                System.out.println("Bye!");
                return false;
            case "/help":
                System.out.println("really helpful help here");
                return true;
            default:
                System.out.println("Unknown command");
                return true;
        }
    }

    private void handleExp(String exp) {
        Matcher numberMtc = numberRgx.matcher(exp);
        Matcher variableMtc = variableRgx.matcher(exp);
        Matcher operatorMtc = operatorRgx.matcher(exp);
        Matcher bracketMtc = bracketRgx.matcher(exp);
        Matcher equalMtc = equalRgx.matcher(exp);

        ArrayList<Instruction> parsedExp = new ArrayList<>();
        while (numberMtc.find()) {
            parsedExp.add(new Instruction(numberMtc.group(), numberMtc.start(), InstructionType.NUM));
        }
        while (variableMtc.find()) {
            parsedExp.add(new Instruction(variableMtc.group(), variableMtc.start(), InstructionType.VAR));
        }
        while (operatorMtc.find()) {
            parsedExp.add(new Instruction(operatorMtc.group(), operatorMtc.start(), InstructionType.OP));
        }
        while (bracketMtc.find()) {
            parsedExp.add(new Instruction(bracketMtc.group(), bracketMtc.start(), InstructionType.BR));
        }
        while (equalMtc.find()) {
            parsedExp.add(new Instruction(equalMtc.group(), equalMtc.start(), InstructionType.EQ));
        }
        parsedExp.sort(comparator);

        prepareExp(parsedExp);

        if (isAssign(parsedExp)) {
            handleAssign(parsedExp);
        } else {
            handleCalc(parsedExp);
        }
    }

    private void handleAssign(ArrayList<Instruction> assign) {
        ArrayList<Instruction> exp = getExpFromAssign(assign);
        if (!expHaveErrors(exp)) {
            variables.put(assign.get(0).getData(), calc(exp).toString());
            variables.size();
        }
    }

    private void handleCalc(ArrayList<Instruction> exp) {
        if (!expHaveErrors(exp)) {
            System.out.println(calc(exp));
        }
    }

    private BigInteger calc(ArrayList<Instruction> exp) {
        ArrayList<Instruction> npeExp = new ArrayList<>();
        Deque<Instruction> stack = new ArrayDeque<>();
        for (Instruction ins : exp) {
            if (ins.getType() == InstructionType.NUM || ins.getType() == InstructionType.VAR) {
                npeExp.add(ins);
            } else if (ins.getType() == InstructionType.OP) {
                if (stack.isEmpty() || stack.peekLast().getPriority() < ins.getPriority()) {
                    stack.offerLast(ins);
                } else {
                    while (!stack.isEmpty() && stack.peekLast().getPriority() >= ins.getPriority()) {
                        npeExp.add(stack.pollLast());
                    }
                    stack.offerLast(ins);
                }
            } else if (ins.getType() == InstructionType.BR) {
                if (ins.isLeft()) {
                    stack.offerLast(ins);
                } else {
                    while (stack.peekLast().getType() != InstructionType.BR && !stack.peekLast().isLeft()) {
                        npeExp.add(stack.pollLast());
                    }
                    stack.pollLast();
                }
            }
        }
        while (!stack.isEmpty()) {
            npeExp.add(stack.pollLast());
        }
        replaceVarsWithVals(npeExp);

        Deque<BigInteger> calcStack = new ArrayDeque<>();
        for (Instruction ins : npeExp) {
            if (ins.getType() == InstructionType.NUM) {
                calcStack.offerLast(new BigInteger(ins.getData()));
            } else if (ins.getType() == InstructionType.OP) {
                BigInteger num1 = calcStack.peekLast() == null ? BigInteger.ZERO : calcStack.pollLast();
                BigInteger num2 = calcStack.peekLast() == null ? BigInteger.ZERO : calcStack.pollLast();
                BigInteger res = BigInteger.ZERO;
                switch (ins.getData()) {
                    case "+":
                        res = num2.add(num1);
                        break;
                    case "-":
                        res = num2.subtract(num1);
                        break;
                    case "*":
                        res = num2.multiply(num1);
                        break;
                    case "/":
                        res = num2.divide(num1);
                        break;
                    case "^":
                        res = num2.modPow(num1, BigInteger.TEN);
                        break;
                }
                calcStack.offerLast(res);
            }
        }

        return calcStack.pollLast();
    }

    private boolean isAssign(ArrayList<Instruction> exp) {
        for (Instruction ins : exp) {
            if (ins.getType() == InstructionType.EQ) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<Instruction> getExpFromAssign(ArrayList<Instruction> assign) {
        ArrayList<Instruction> exp = new ArrayList<>();
        boolean add = false;
        for (Instruction ins : assign) {
            if (add) {
                exp.add(ins);
            }
            if (ins.getType() == InstructionType.EQ) {
                add = true;
            }
        }
        return exp;
    }

    private boolean expHaveErrors(ArrayList<Instruction> exp) {
        //left BR == right BR
        int leftBRCounter = 0;
        int rightBRCounter = 0;
        for (Instruction ins : exp) {
            if (ins.getType() == InstructionType.BR) {
                if (ins.isLeft()) {
                    leftBRCounter++;
                } else {
                    rightBRCounter++;
                }
            }
        }
        if (leftBRCounter != rightBRCounter) {
            System.out.println("Invalid expression");
            return true;
        }
        //unknown variables
        for (Instruction ins : exp) {
            if (ins.getType() == InstructionType.VAR && !variables.containsKey(ins.getData())) {
                System.out.println("Unknown variable");
                return true;
            }
        }
        //mul one after another
        for (int i = 0; i < exp.size()-1; i++) {
            if (exp.get(i).getData().equals("*") && exp.get(i+1).getData().equals("*")) {
                System.out.println("Invalid expression");
                return true;
            }
        }
        return false;
    }

    private void replaceVarsWithVals(ArrayList<Instruction> exp) {
        for (Instruction ins : exp) {
            if (ins.getType() == InstructionType.VAR) {
                ins.setData(variables.get(ins.getData()));
                ins.setType(InstructionType.NUM);
            }
        }
    }

    private void prepareExp(ArrayList<Instruction> exp) {
        for (int i = 0; i < exp.size()-1; i++) {
            if (exp.get(i).getData().equals("+") && exp.get(i+1).getData().equals("+")) {
                exp.remove(i+1);
            } else if (exp.get(i).getData().equals("-") && exp.get(i+1).getData().equals("-")) {
                exp.remove(i+1);
                exp.get(i).setData("+");
            }
        }
    }

}
