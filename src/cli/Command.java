package cli;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Command extends AbstractCommand {

    private static final int spaceStrip = 5;
    public static final int customArity = -1;

    private static final String NO_ACTION_AT_ADDING_ARITIES = "The actions is not set. There's nothing that can have the arity. Pleases add an action first";
    private static final String TOO_MANY_ACTIONS_WHILE_SETTING_ARITIES = "It's ambiguous what action, should have which arity. Please reconsider setting proper arities to particular actions or making one action with multiple arities";

    private Command parent;
    private final List<Command> commands = new ArrayList<>();
    final List<Option> options = new ArrayList<>();
    private Map<Integer, Consumer<String[]>> actions = new HashMap<>();
    private Command help;

    public Command(String... names){
        super(names);
    }

    public Command(Collection<String> names) {
        super(names);
    }

    public Command(Command command) {
        names = command.names;
        actions = command.actions;
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

    void clear(){
        options.forEach(Option::clear);
    }

    public Command addCommand(String... names) {
        return addCommand(new Command(names));
    }

    public Command addCommand(Collection<String> names) {
        return addCommand(new Command(names));
    }

    public Command addCommand(Command command) {
        command.setParent(this);
        if (!command.isHelp())
            command.updateHelp(help.names.toArray(new String[]{}));
        commands.add(command);
        return command;
    }

    public void removeCommands(String[] names) {
        Stream<Command> toRemove = Arrays.stream(names).flatMap(n -> commands.stream().filter(c -> c.names.contains(n)));
        removeCommands(toRemove);
    }

    public void removeCommands(Command[] commands) {
        removeCommands(Arrays.stream(commands));
    }

    public void removeCommands(Collection<Command> commands) {
        removeCommands(commands.stream());
    }

    private void removeCommands(Stream<Command> commands) {
        commands.forEach(this.commands::remove);
    }

    public boolean hasCommand(String name) {
        return commands.stream().anyMatch(c -> c.names.contains(name));
    }

    public Command getCommand(String name) {
        return commands.stream().filter(c -> c.names.contains(name)).findFirst().orElseThrow();
    }

    public Option addOption(String... names) {
        return addOption(new Option(names));
    }

    public Option addOption(Collection<String> names) {
        return addOption(new Option(names));
    }

    public Option addOption(Option option){
        options.add(option);
        return option;
    }

    public String getOptionValue(String name){
        return options.stream().filter(o -> o.names.contains(name)).findAny().map(Option::get).orElse("");
    }

    @Override
    public Command setDescription(String description) {
        return (Command) super.setDescription(description);
    }

    public boolean isExecutable() {
        return !actions.isEmpty();
    }

    public boolean isOfArity(int arity) {
        if (actions.containsKey(arity) || actions.containsKey(customArity))
            return true;
        var customArity = getCustomArgumentArity();
        return customArity.isPresent() && customArity.get() >= arity;
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
        if (arity < 0 && getCustomArgumentArity().isPresent())
            throw new IllegalArgumentException("A custom arity already exists");
        else
            actions.put(arity, action);
        return this;
    }

    public void execute(String... args) {
        if (actions.containsKey(args.length))
            actions.get(args.length).accept(args);
        else {
            Optional<Integer> arity = getCustomArgumentArity();
            if (arity.isPresent() && arity.get() <= args.length) {
                actions.get(mapArityKey(arity.get())).accept(args);
            }
        }
    }

    public void updateHelp(String[] names) {
        updateHelp(names, getCli().getPrinter());
    }

    void updateHelp(String[] names, PrintStream printer) {  //TODO make user update help after setting a printer
        createNewHelp(names, printer);
        commands.stream().filter(c -> !c.isHelp()).forEach(command -> command.updateHelp(names, printer));
    }

    private void createNewHelp(String[] names, PrintStream printer) {
        help = new Command(names);
        help.addAction(args -> printHelp(printer));
        help.setAsHelp();
    }

    void printHelp(PrintStream printer) {
        printInfoAboutSelf(printer);
        if (!options.isEmpty())
            printOptions(printer);
        if (!commands.isEmpty())
            printSubCommands(printer);
    }

    private void printInfoAboutSelf(PrintStream printer) {
        String commandNames = getCommandNamesString();
        printer.println(commandNames);
        if (description.length() > 0)
            printer.println(description);
        printer.println();
    }

    private void printOptions(PrintStream printer){
        printer.println(OPTIONS);
        printCommandsWithSpaces(printer, options);
        printer.println();
    }

    private void printSubCommands(PrintStream printer) {
        printer.println(COMMANDS);
        printCommandsWithSpaces(printer, commands);
        printer.println();
    }

    private void printCommandsWithSpaces(PrintStream printer, List<? extends AbstractCommand> commands) {
        List<String> names = commands.stream().map(AbstractCommand::getCommandNamesString).collect(Collectors.toList());
        List<String> namesWithSpaces = putSpaces(names);
        List<String> namesWithDescriptions = putDescriptions(namesWithSpaces, commands);
        namesWithDescriptions.forEach(printer::println);
    }

    private List<String> putSpaces(List<String> list) {
        int maxLength = list.stream().map(String::length).max(Integer::compareTo).orElse(0);
        return list.stream().map(s -> s + spaces(maxLength + spaceStrip - s.length())).collect(Collectors.toList());
    }

    private List<String> putDescriptions(List<String> list, List<? extends AbstractCommand> commands) {
        return IntStream.range(0, commands.size())
                .mapToObj(i -> list.get(i) + commands.get(i).getDescription())
                .collect(Collectors.toList());
    }

    private String spaces(int n) {
        return IntStream.range(0, n).mapToObj(i -> " ").reduce("", String::concat);
    }

    private Optional<Integer> getCustomArgumentArity(){
        return actions.keySet().stream().filter(a -> a < 0).map(Command::mapArityKey).findFirst();
    }

    private static int mapArityKey(int value){
        return -(value+1);
    }
}
