package cli;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

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

    PrintStream getPrinter(){
        return printer;
    }


    public void readInstruction() {
        if (scanner == null)
            scanner = new Scanner(System.in);
        readInstruction(scanner);
    }

    public void readInstruction(Scanner sc) {
        instruction = sc.nextLine();
    }

    public void executeInstruction() {
        splitInstruction();
        Command command = findCommandToExecute();
        //TODO handle options
        if (command.isExecutable() && command.isOfArity(arguments.length))
            command.execute(arguments);
        else
            command.printHelp(printer);
    }

    private Command findCommandToExecute() {
        Command currentCommand = this;
        int i = 0;
        while (i < arguments.length && currentCommand.hasCommand(arguments[i])) {
            currentCommand = currentCommand.getCommand(arguments[i++]);
        }
        arguments = Arrays.copyOfRange(arguments, i, arguments.length);
        return currentCommand;
    }

    private void splitInstruction(){
        arguments = instruction.split(" ");
    }

    public void printState() {
        printer.println(state);
    }
}
