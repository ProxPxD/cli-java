package cli;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class CommandLineInterface  extends Command{

    private Scanner scanner;
    private PrintStream printer;
    private final State state = new State();
    private String instruction;
    private Command help = new Command("--help");

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
        Command command = findCommandToExecute();
        //TODO handle options
        String[] args = splitInstructionIntoParameters();
        if (command.isExecutable() && command.isOfArity(args.length))
            command.execute(args);
        else
            command.printHelp(printer);
    }

    private Command findCommandToExecute() {
        Command currentCommand = this;
        String commandName = splitInstruction();
        while (currentCommand.hasCommand(commandName)) {
            currentCommand = currentCommand.getCommand(commandName);
            commandName = splitInstruction();
        }
        return currentCommand;
    }

    private String splitInstruction() {
        String[] split = instruction.split(" ", 2);
        instruction = split.length > 1 ? split[1] : "";
        return split[0];
    }

    private String[] splitInstructionIntoParameters() {
        return instruction.split(" ");
    }


    public void printState() {
        printer.println(state);
    }
}
