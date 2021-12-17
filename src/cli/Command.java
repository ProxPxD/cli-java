package cli;

import lombok.Getter;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Command {

    private static final int spaceStrip = 5;
    public static final int customArity = -1;

    private static final String NO_ACTION_AT_ADDING_ARITIES = "The actions is not set. There's nothing that can have the arity. Pleases add an action first";
    private static final String TOO_MANY_ACTIONS_WHILE_SETTING_ARITIES = "It's ambiguous what action, should have which arity. Please reconsider setting proper arities to particular actions or making one action with multiple arities";


    private final List<String> names;
    @Getter
    private String description = "";
    private Command parent;
    private final List<Command> commands = new ArrayList<>();
    private HashMap<Integer, Consumer<String[]>> actions;
//    private Consumer<String[]> action;
//    private List<Integer> arities = new ArrayList<>();

    public Command(String... names) {
        this.names = List.of(names);
    }

    public Command(Collection<String> names) {
        this.names = new ArrayList<>(names);
    }

    public Command(Command command) {
        names = command.names;
        actions = command.actions;
//        arities = command.arities;
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
        return !actions.isEmpty();
    }

    public boolean isOfArity(int arity) {
        return actions.containsKey(arity) || actions.containsKey(customArity);
    }

    public Command setPossibleArities(Integer... arities){ // TODO idea: allow more things
        if (actions.isEmpty())
            throw new IllegalStateException(NO_ACTION_AT_ADDING_ARITIES);
        if (actions.size() > 1)
            throw new IllegalStateException(TOO_MANY_ACTIONS_WHILE_SETTING_ARITIES);

        Consumer<String[]> action = actions.values().stream().findFirst().get();
        actions.clear();
        for(int arity: arities) {
            actions.put(arity, action);
        }
        return this;
    }

    public Command setAction(ThrowingConsumer<String[]> action){
        return setAction(customArity, action);
    }

    public Command setAction(int arity, ThrowingConsumer<String[]> action){
        actions.clear();
        return addAction(arity, action);
    }

    public Command addAction(ThrowingConsumer<String[]> action) {
        return addAction(customArity, action);
    }

    public Command addAction(int arity, ThrowingConsumer<String[]> action){
        if (actions.containsKey(arity))
            throw new IllegalArgumentException("That arity already exists");
        else
            actions.put(arity, action);
        return this;
    }

    public void execute(String... args) {
        if (actions.containsKey(args.length))
            actions.get(args.length).accept(args);
        else if (actions.containsKey(customArity))
            actions.get(customArity).accept(args);
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
        List<String> names = commands.stream().map(Command::getCommandNamesString).collect(Collectors.toList());
        List<String> namesWithSpaces = putSpaces(names);
        List<String> namesWithDescriptions = putDescriptions(namesWithSpaces);
        namesWithDescriptions.forEach(printer::println);
    }

    private List<String> putSpaces(List<String> list){
        int maxLength = list.stream().map(String::length).max(Integer::compareTo).orElse(0);
        return list.stream().map(s -> s + spaces(maxLength + spaceStrip - s.length())).collect(Collectors.toList());
    }

    private List<String> putDescriptions(List<String> list){
        return IntStream.range(0, commands.size())
                .mapToObj(i -> list.get(i) + commands.get(i).getDescription())
                .collect(Collectors.toList());
    }

    String getCommandNamesString() {
        String string = names.stream().reduce("", (s, n) -> s + ", " + n);
        return string.substring(2);
    }

    private String spaces(int n) {
        return IntStream.range(0, n).mapToObj(i -> " ").reduce("", String::concat);
    }
}
