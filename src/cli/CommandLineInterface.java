package cli;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

public class CommandLineInterface  extends Command{

    private Scanner scanner;
    private PrintStream printer;
    private final State state = new State();
    private String instruction;
    private String[] arguments;
    private Command help = new Command("--help");

    public CommandLineInterface(){
        super("CLI");
    }

    @Override
    protected boolean isCli(){
        return true;
    }
    public void setHelpOptions(String... names) {
        help = new Command(names);
        updateHelp(help, printer);
    }

    public void setStateValue(String variable, String value) {
        state.put(variable, value);
    }

    public String getStateValue(String variable) {
        return state.get(variable);
    }

    public void setInputStream(InputStream in) {
        scanner = new Scanner(in);
    }

    public void setOutputStream(OutputStream out) {
        printer = new PrintStream(out);
    }

    public void print(Object message){
        printer.println(message);
    }

    PrintStream getPrinter(){
        return printer;
    }

    public void printState() {
        printer.println(state);
    }

    public void readInstruction() {
        if (scanner == null)
            scanner = new Scanner(System.in);
        readInstruction(scanner);
    }

    public void readInstruction(Scanner sc) {
        instruction = sc.nextLine();
    }

    public void putInstruction(String instruction){
        this.instruction = instruction;
    }

    public void executeInstruction() {
        arguments = splitInstruction();
        Command command = findCommandToExecute();
        setOptionsFor(command);
        handleExecution(command);
        command.clear();
    }

    private void setOptionsFor(Command command){
        List<Integer> optionIndices = new ArrayList<>();
        for(int i = 0; i < arguments.length; i++){
            int j = i;
            Optional<Option> correspondingOption = command.options.stream().filter(option -> option.names.contains(arguments[j])).findAny();
            if (correspondingOption.isPresent()){
                correspondingOption.get().set(arguments[j+1]);
                optionIndices.add(i++);
            }
        }
        removeIndicesFromArguments(optionIndices);
    }

    private void removeIndicesFromArguments(List<Integer> indices){
        List<String> arguments = Arrays.asList(this.arguments);
        int i = indices.size();
        while (i > 0){
            int index = indices.get(--i);
            arguments.remove(index+1);
            arguments.remove(index);
        }
        this.arguments = arguments.toArray(new String[]{});
    }

    private String[] splitInstruction(){
        return instruction.split(" ");
    }

    private Command findCommandToExecute() {
        Command currentCommand = this;
        int i = 0;
        while (i < arguments.length && currentCommand.hasCommand(arguments[i])) {
            currentCommand = currentCommand.getCommand(arguments[i++]);
        }
        arguments = cutCommandsFromArguments(i);
        return currentCommand;
    }

    private String[] cutCommandsFromArguments(int from){
        return Arrays.copyOfRange(arguments, from, arguments.length);
    }

    private void handleExecution(Command command){
        if (command.isExecutable() && command.isOfArity(arguments.length))
            command.execute(arguments);
        else
            command.printHelp(printer);
    }
}
