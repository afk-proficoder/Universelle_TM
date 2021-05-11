import java.util.*;

public class UTMEncoder
{
    private final static int CENTER_INDEX_OF_BAND = 31;
    private final static String SEARCH_AT_END_REGEX = "(?!.*1)";
    private final static String PART_WITH_FIRST_NUMBER_ONE_INCLUSIVE_REGEX = "^([^1]+)1";
    private final static String BYTECODE_LINEFEED = "11";
    private final static String HEAD_RIGHT = "00";
    private final static String DISASSEMBLE_SEPARATOR_SYMBOL = "1";
    private final static String INPUT_SEPARATOR_INCLUSIVE_INPUT_REGEX = "1{3}(.*)";
    private final static String TURING_MACHINE_EXCLUSIVE_SEPARATOR_INPUT_REGEX = ".+?(?=111)111";
    private final static String LAST_CHARACTER = "\\\\*.$";
    private final static String EVERYTHING_EXCEPT_LAST_CHARACTER = ".+?(?=.).";
    private final static char STEP_MODE = '0';
    private final static char RUN_MODE = '1';
    private final String byteCode;
    private String remainingTMProperties;
    private String currentState;
    private String symbolsToRead;
    private String nextState;
    private String symbolsToWrite;
    private String printHeadDirection;
    private char userInput;
    private int calculationCount = 0;
    private final String[] tape = new String[62];
    private Scanner scanner = new Scanner(System.in);
    private final HashMap<Integer, HashMap<String, String>> transitionFunctions = new HashMap<>();

    public UTMEncoder(String byteCode)
    {
        this.byteCode = byteCode;
    }

    private int counterForZeros(String symbols)
    {
        char[] symbolsArray = symbols.toCharArray();
        int counter = 0;
        for (int i = 0; i < symbolsArray.length; i++)
        {
            if (symbolsArray[i] == '0')
            {
                counter++;
            }
        }
        return counter;
    }

    private String determineSymbols(String symbols)
    {
        String stringToReturn;
        switch (symbols)
        {
            case "0":
                stringToReturn = "0";
                break;
            case "00":
                stringToReturn = "_";
                break;
            case "000":
                stringToReturn = "C";
                break;
            case "0000":
                stringToReturn = "X";
                break;
            case "00000":
                stringToReturn = "Y";
                break;
            default:
                stringToReturn = " ";
        }
        return stringToReturn;
    }

    private String determineState(String state)
    {
        String stringToReturn;
        switch (state)
        {
            case "0":
                stringToReturn = "q0";
                break;
            case "00":
                stringToReturn = "q1";
                break;
            case "000":
                stringToReturn = "q2";
                break;
            case "0000":
                stringToReturn = "q3";
                break;
            case "00000":
                stringToReturn = "q4";
                break;
            case "000000":
                stringToReturn = "q5";
                break;
            case "0000000":
                stringToReturn = "q6";
                break;
            case "00000000":
                stringToReturn = "q7";
                break;
            case "000000000":
                stringToReturn = "q8";
                break;
            case "0000000000":
                stringToReturn = "q9";
                break;
            case "00000000000":
                stringToReturn = "q10";
                break;
            case "000000000000":
                stringToReturn = "q11";
                break;
            case "0000000000000":
                stringToReturn = "q12";
                break;
            case "00000000000000":
                stringToReturn = "q13";
                break;
            default:
                stringToReturn = " ";
        }
        return stringToReturn;
    }

    private void run()
    {
        userInput = selectMode();
        initializeTape();
        if (isBytecodeBinary())
        {
            System.out.println(
                    "-------------------------------------------------------------------------------------------");
            StringBuilder transitionFunction = new StringBuilder();
            currentState = "";
            symbolsToRead = "";
            nextState = "";
            symbolsToWrite = "";
            int currentTransition = 0;

            String[] transitionFunctionByteCodes;
            String formattedByteCode = removeFirstChar();
            String turingMachineInput = formattedByteCode.replaceAll(TURING_MACHINE_EXCLUSIVE_SEPARATOR_INPUT_REGEX, "");
            writeInputToBand(turingMachineInput);
            transitionFunctionByteCodes = separateAndRemoveInput(formattedByteCode);
            for (String transitionFunctionByteCode : transitionFunctionByteCodes)
            {
                HashMap<String, String> tmProperties = new HashMap<>();
                long count = transitionFunctionByteCode.chars().filter(ch -> ch == '1').count();
                for (int disassembleSeparator = 0; disassembleSeparator < count; disassembleSeparator++)
                {
                    currentState = removeSeparatorSymbol(disassembleTransition(transitionFunctionByteCode));
                    symbolsToRead = removeSeparatorSymbol(disassembleTransition(remainingTMProperties));
                    nextState = removeSeparatorSymbol(disassembleTransition(remainingTMProperties));
                    symbolsToWrite = removeSeparatorSymbol(disassembleTransition(remainingTMProperties));
                }
                printHeadDirection = readPrintHeadDirection(transitionFunctionByteCode);
                printInfo(transitionFunctionByteCode);
                transitionFunction.append("(")
                        .append(determineState(currentState))
                        .append(",")
                        .append(determineSymbols(symbolsToRead))
                        .append(") -> (")
                        .append(determineState(nextState))
                        .append(",")
                        .append(determineSymbols(symbolsToWrite))
                        .append(",")
                        .append(printHeadDirection)
                        .append(")" + "\n");

                System.out.println(
                        "-------------------------------------------------------------------------------------------");

                tmProperties.put("currentState", determineState(currentState));
                tmProperties.put("symbolsToRead", determineSymbols(symbolsToRead));
                tmProperties.put("nextState", determineState(nextState));
                tmProperties.put("symbolsToWrite", determineSymbols(symbolsToWrite));
                tmProperties.put("printHeadDirection", printHeadDirection);
                transitionFunctions.put(currentTransition++, tmProperties);
            }
            System.out.println("Transition function: " + "\n" + transitionFunction + "\n" + "Tape visualization: ");
            calculateInput();
            System.out.println("Number of calculations: " + calculationCount);
        }
        else
        {
            System.err.println("Error: Bytecode is not binary (does not start with '1')");
        }
    }

    private String[] separateAndRemoveInput(String formattedByteCode)
    {
        String formattedByteCodeWithoutInput = formattedByteCode.replaceAll(INPUT_SEPARATOR_INCLUSIVE_INPUT_REGEX, "");
        return formattedByteCodeWithoutInput.split(BYTECODE_LINEFEED);
    }

    private void printInfo(String separatedByteCode)
    {
        System.out.println("transitionFunctionByteCode: " + separatedByteCode + "\n" + "currentState: " + currentState + "\n" +
                "symbolsToRead: " + symbolsToRead + "\n" + "nextState: " + nextState + "\n" + "symbolsToWrite: " + symbolsToWrite +
                "\n" + "printHeadDirection: " + printHeadDirection);
    }

    private String removeFirstChar()
    {
        return byteCode.substring(1);
    }

    private void writeInputToBand(String input)
    {
        int currentHeadLocation = CENTER_INDEX_OF_BAND;
        for (String symbol : input.split(""))
        {
            tape[currentHeadLocation++] = symbol;
        }
    }

    private void calculateInput()
    {
        int currentBandPosition = CENTER_INDEX_OF_BAND;
        boolean calculationDone = false;

        initializeStartState();
        System.out.println(Arrays.toString(tape));

        while (!calculationDone)
        {
            Iterator<Map.Entry<Integer, HashMap<String, String>>> iterator = transitionFunctions.entrySet().iterator();
            while (iterator.hasNext())
            {
                for (Map.Entry<Integer, HashMap<String, String>> transition : transitionFunctions.entrySet())
                {
                    if ((Character.toString(tape[currentBandPosition].charAt(tape[currentBandPosition].length()-1))
                            .equals(transition.getValue().get("symbolsToRead")))
                            && tape[currentBandPosition].replaceAll(LAST_CHARACTER, "")
                            .equals(transition.getValue().get("currentState")))
                    {
                        tape[currentBandPosition] = transition.getValue().get("symbolsToWrite");
                        if (transition.getValue().get("printHeadDirection").equals("R"))
                        {
                            currentBandPosition++;
                        }
                        else
                        {
                            currentBandPosition--;
                        }
                        tape[currentBandPosition] = transition.getValue().get("nextState") + tape[currentBandPosition];
                        System.out.println(Arrays.toString(tape));
                        calculationCount++;
                        if(userInput == STEP_MODE)
                        {
                          System.out.println("Press any key and ENTER to continue the calculation...");
                            scanner = new Scanner(System.in);
                            scanner.next().charAt(0);
                        }
                    }
                    else
                    {
                        calculationDone = true;
                    }
                }
                iterator.next();
            }
        }
    }

    private void initializeTape()
    {
        Arrays.fill(tape, "_");
    }

    private String removeSeparatorSymbol(String tmProperty)
    {
        return tmProperty.replaceAll(DISASSEMBLE_SEPARATOR_SYMBOL, "");
    }

    private String disassembleTransition(String remainingByteCode)
    {
        remainingTMProperties = remainingByteCode.replaceAll(PART_WITH_FIRST_NUMBER_ONE_INCLUSIVE_REGEX, "");
        return remainingByteCode.replaceAll(remainingTMProperties + SEARCH_AT_END_REGEX, "");
    }

    private void initializeStartState()
    {
        int currentBandPosition = CENTER_INDEX_OF_BAND;
        for (Map.Entry<Integer, HashMap<String, String>> transition : transitionFunctions.entrySet())
        {
            if (transition.getValue().get("currentState").equals("q0")
                    && !(tape[currentBandPosition].contains(transition.getValue().get("currentState"))))
            {
                tape[currentBandPosition] = transition.getValue().get("currentState") + tape[currentBandPosition];
            }
        }
    }

    private boolean isBytecodeBinary()
    {
        return byteCode.startsWith("1");
    }

    private char selectMode()
    {
        char inputToReturn = ' ';
        boolean modeSelected = false;

        System.out.println("""
                Select the mode to run the calculations. Possible mods are:
                0: Step-Mode
                1: Run-Mode""");
        while (!modeSelected)
        {
            char input = scanner.next().charAt(0);
            if (input == STEP_MODE || input == RUN_MODE)
            {
                inputToReturn = input;
                modeSelected = true;
            }
            else
            {
                System.out.println("Invalid input: please try again");
                System.out.println("""
                        Select the mode to run the calculations. Possible mods are:
                        0: Step-Mode
                        1: Run-Mode""");
            }
        }
        return inputToReturn;
    }

    private String readPrintHeadDirection(String transitionFunctionByteCode)
    {
        String direction;
        if (transitionFunctionByteCode.endsWith(HEAD_RIGHT))
        {
            direction = "R";
        }
        else
        {
            direction = "L";
        }
        return direction;
    }

    public static void main(String[] args)
    {
        if (!(args.length == 0))
        {
            UTMEncoder utmEncoder = new UTMEncoder(args[0]);
            utmEncoder.run();
        }
        else
        {
            System.out.println("No Turing Machine with input specified as argument. aborting...");
        }
    }
}
