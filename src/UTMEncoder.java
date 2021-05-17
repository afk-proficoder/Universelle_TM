import java.lang.reflect.Array;
import java.util.*;

/**
 * Emuliert eine Universelle Turingmaschine, welche Turingmaschinen berechnet, die eine Multiplikation aus zwei
 * positiven natürlichen Zahlen ausrechnet.
 * Um grössere Multiplikationen zu ermöglichen, wird mit einem "subarray" gearbeitet.
 * Diese Universelle Turingmaschine akzeptiert als Input unäre Faktoren zur Berrechnung des Produktes und als
 * Multiplikationszeichen einen Underscore "_"
 *
 * @Author Pascal Kunz
 */
public class UTMEncoder
{
    private final static int CENTER_INDEX_OF_BAND = 31;
    private final static int START_OF_VIEW = 16;
    private final static int END_OF_VIEW = 78;
    private final static String SEARCH_AT_END_REGEX = "(?!.*1)";
    private final static String PART_WITH_FIRST_NUMBER_ONE_INCLUSIVE_REGEX = "^([^1]+)1";
    private final static String BYTECODE_LINEFEED = "11";
    private final static String HEAD_RIGHT = "00";
    private final static String DISASSEMBLE_SEPARATOR_SYMBOL = "1";
    private final static String INPUT_SEPARATOR_INCLUSIVE_INPUT_REGEX = "1{3}(.*)";
    private final static String TURING_MACHINE_EXCLUSIVE_SEPARATOR_INPUT_REGEX = ".+?(?=111)111";
    private final static String LAST_CHARACTER = "\\\\*.$";
    private final static char STEP_MODE = '0';
    private final static char RUN_MODE = '1';
    private final static int RUN_MODE_NO_TAPE_VISUALIZATION = '2';
    private final String byteCode;
    private String remainingTMProperties;
    private char userInput;
    private int calculationCount = 0;
    private final String[] tape = new String[1000000];
    private final String[] tapeView;
    private Scanner scanner = new Scanner(System.in);
    private final HashMap<Integer, HashMap<String, String>> transitionFunctions = new HashMap<>();
    private final ArrayList<HashMap<String, String>> test = new ArrayList<>();
    private final ArrayList<String> acceptingStates;

    public UTMEncoder(String byteCode)
    {
        acceptingStates = new ArrayList<>();
        acceptingStates.add("q17");
        acceptingStates.add("q18");
        acceptingStates.add("q19");
        this.byteCode = byteCode;
        initializeTape();
        tapeView = new String[END_OF_VIEW - START_OF_VIEW + 1];
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
            case "000000000000000":
                stringToReturn = "q14";
                break;
            case "0000000000000000":
                stringToReturn = "q15";
                break;
            case "00000000000000000":
                stringToReturn = "q16";
                break;
            case "000000000000000000":
                stringToReturn = "q17";
                break;
            case "0000000000000000000":
                stringToReturn = "q18";
                break;
            case "00000000000000000000":
                stringToReturn = "q19";
                break;
            default:
                stringToReturn = " ";
        }
        return stringToReturn;
    }

    private void run()
    {
        userInput = selectMode();
        if (isBytecodeBinary())
        {
            System.out.println(
                    "-------------------------------------------------------------------------------------------");
            StringBuilder transitionFunction = new StringBuilder();
            String currentState = "";
            String symbolsToRead = "";
            String nextState = "";
            String symbolsToWrite = "";
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
                String printHeadDirection = readPrintHeadDirection(transitionFunctionByteCode);
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

                tmProperties.put("currentState", determineState(currentState));
                tmProperties.put("symbolsToRead", determineSymbols(symbolsToRead));
                tmProperties.put("nextState", determineState(nextState));
                tmProperties.put("symbolsToWrite", determineSymbols(symbolsToWrite));
                tmProperties.put("printHeadDirection", printHeadDirection);
                transitionFunctions.put(currentTransition++, tmProperties);
                test.add(tmProperties);
            }
            System.out.println("Transition function: " + "\n" + transitionFunction);
            System.out.println("Result of multiplication: "+calculateInput());
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

    private int calculateInput()
    {
        int multiplicationResult = 0;
        int currentBandPosition = CENTER_INDEX_OF_BAND;
        boolean calculationDone = false;
        initializeStartState();
        if (!(userInput == RUN_MODE_NO_TAPE_VISUALIZATION))
        {
            System.arraycopy(tape,START_OF_VIEW,tapeView,0,tapeView.length);
            System.out.println("Tape visualization: "+"\n"+Arrays.toString(tapeView));
        }

       while (!calculationDone)
        {
            for (HashMap<String, String> transition : test)
            {
                if ((Character.toString(tape[currentBandPosition].charAt(tape[currentBandPosition].length() - 1))
                        .equals(transition.get("symbolsToRead"))) && tape[currentBandPosition].replaceAll(LAST_CHARACTER, "")
                        .equals(transition.get("currentState")))
                {
                    {
                        tape[currentBandPosition] = transition.get("symbolsToWrite");
                        if (transition.get("printHeadDirection").equals("R"))
                        {
                            currentBandPosition++;
                        }
                        else
                        {
                            currentBandPosition--;
                        }
                        tape[currentBandPosition] = transition.get("nextState") + tape[currentBandPosition];
                        if (!(userInput == RUN_MODE_NO_TAPE_VISUALIZATION))
                        {
                            System.arraycopy(tape,START_OF_VIEW,tapeView,0,tapeView.length);
                            System.out.println(Arrays.toString(tapeView));
                        }
                        calculationCount++;
                        if(isNextStateAcceptingState(transition))
                        {
                            for (int currentTapeIndex = 0; currentTapeIndex < tape.length;currentTapeIndex++)
                            {
                                if (Array.get(tape,currentTapeIndex).equals("0"))
                                {
                                    multiplicationResult++;
                                }
                            }
                            calculationDone = true;
                        }
                        if (userInput == STEP_MODE)
                        {
                            System.out.println("Press any key and ENTER to continue the calculation...");
                            scanner = new Scanner(System.in);
                            scanner.next().charAt(0);
                        }
                    }
                }
            }
        }
       return multiplicationResult;
    }

    private boolean isNextStateAcceptingState(HashMap<String, String> transition)
    {
        boolean isAtAcceptingState = false;
        for (String acceptingState : acceptingStates)
        {
            if(transition.get("nextState").equals(acceptingState))
            {
                isAtAcceptingState = true;
            }
        }
        return isAtAcceptingState;
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
                        1: Run-Mode
                        2: Run-Mode (no tape visualization)""");
        while (!modeSelected)
        {
            char input = scanner.next().charAt(0);
            if (input == STEP_MODE || input == RUN_MODE || input == RUN_MODE_NO_TAPE_VISUALIZATION)
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
                        1: Run-Mode
                        2: Run-Mode (no tape visualization)""");
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
