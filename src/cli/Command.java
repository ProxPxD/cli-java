package cli;

import lombok.Getter;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Command {

    private static final int spaceStrip = 5;

    private final List<String> names;
    @Getter
    private String description = "";
    private Command parent;
    private final List<Command> commands = new ArrayList<>();
    private Consumer<String[]> action;
    private List<Integer> arities = new ArrayList<>();

    public Command(String... names) {
        this.names = List.of(names);
    }

    public Command(Collection<String> names) {
        this.names = new ArrayList<>(names);
    }

    public Command(Command command) {
        names = command.names;
        action = command.action;
        arities = command.arities;
        description = command.description;
        parent = null;
    }

    void setParent(Command parent) {
        this.parent = parent;
    }

    public Command getParent() {
        return parent;
    }

    public CommandLineInterface getCli(){
        Command currParent = parent;
        while (!currParent.isCli())
            currParent = currParent.getParent();
        return (CommandLineInterface) currParent;
    }

    protected boolean isCli(){
        return false;
    }

    public Command addCommand(String... names) {
        return addCommand(new Command(names));
    }

    public Command addCommand(Collection<String> names) {
        return addCommand(new Command(names));
    }

    public Command addCommand(Command command) {
        command.setParent(this);
        commands.add(command);
        return command;
    }

    public boolean hasCommand(String name) {
        return commands.stream().anyMatch(c -> c.names.contains(name));
    }

    public Command getCommand(String name) {
        return commands.stream().filter(c -> c.names.contains(name)).findFirst().orElseThrow();
    }

    public Command setDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean isExecutable() {
        return action != null;
    }

    public boolean isOfArity(int arity) {
        return arities.contains(arity);
    }

    public void addAction(ThrowingConsumer<String[]> action) {
        addAction(action, 0);
    }

    public void addAction(ThrowingConsumer<String[]> action, int arity) {
        addAction(action, new Integer[]{arity});
    }

    public void addAction(ThrowingConsumer<String[]> action, Integer... arities) {
        this.action = action;
        this.arities = Arrays.asList(arities);
    }

    public void execute(String... args) {
        action.accept(args);
    }

    public void updateHelp(Command help){
        updateHelp(help, getCli().getPrinter());
    }

    void updateHelp(Command help, PrintStream printer) {
        Command ownHelp = addCommand(help.names);
        ownHelp.addAction(args -> printHelp(printer));
        commands.forEach(command -> command.updateHelp(help, printer));
    }

    void printHelp(PrintStream printer) {
        printInfoAboutSelf(printer);
        printSubCommands(printer);
    }

    private void printInfoAboutSelf(PrintStream printer){
        String commandNames = getCommandNamesString();
        printer.println(commandNames);
        if (description.length() > 0)
            printer.println(description);
        printer.println();
    }

    private void printSubCommands(PrintStream printer){
        List<String> namesWithSpaces = getCommandNamesWithSpaces();
        List<String> namesWithDescriptions = IntStream.range(0, commands.size())
                .mapToObj(i -> namesWithSpaces.get(i) + commands.get(i).getDescription())
                .collect(Collectors.toList());
        namesWithDescriptions.forEach(printer::println);
    }

    private List<String> getCommandNamesWithSpaces(){
        Stream<String> names = commands.stream().map(Command::getCommandNamesString);
        int maxLength = names.map(String::length).max(Integer::compareTo).orElse(0);
        return names.map(s -> s + spaces(maxLength + spaceStrip - s.length())).collect(Collectors.toList());
    }

    String getCommandNamesString() {
        String string = names.stream().reduce("", (s, n) -> s + n + ", ");
        return string.substring(0, string.length() - 2);
    }

    private String spaces(int n) {
        return IntStream.range(0, n).mapToObj(i -> " ").reduce("", String::concat);
    }
}
