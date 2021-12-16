package test;

import cli.Command;
import cli.CommandLineInterface;


public class ApplicationManager {

    private boolean isWorking = false;
    private CommandLineInterface cli;

    public ApplicationManager() {
        createCli();
    }

    public boolean isOn() {
        return isWorking;
    }

    public void turnOn() {
        isWorking = true;
    }

    public void turnOff() {
        isWorking = false;
    }

    public void readInstruction() {
        cli.readInstruction();
    }

    public void executeInstruction() {
        cli.executeInstruction();
    }

    public void printState(){
        cli.printState();
    }

    private void createCli() {
        initCli();
        initCliOptions();
    }

    private void initCli() {
        cli = new CommandLineInterface();
        cli.setInputStream(System.in);
        cli.setOutputStream(System.out);
    }

    private void initCliOptions() {
        initCliAddOption();
        initCliSetOption();
        initCliExitOption();
    }

    private void initCliAddOption() {
        Command add = cli.addCommand(Constants.ADD).setDescription(Constants.ADD_DESCRIPTION);
        Command addLang = add.addCommand(Constants.LANGUAGE, Constants.LANG).setDescription(Constants.ADD_LANGUAGE_DESCRIPTION);
        addLang.addAction(1, args -> {cli.setStateValue(Constants.LANGUAGE, args[0]);});
    }

    private void initCliSetOption() {
        Command set = cli.addCommand(Constants.SET).setDescription(Constants.SET_DESCRIPTION);
        Command setLang = set.addCommand(Constants.LANGUAGE, Constants.LANG).setDescription(Constants.SET_LANG_DESCRIPTION);
        setLang.addAction(1, args -> cli.setStateValue(Constants.LANGUAGE, args[0]));
    }

    private void initCliExitOption() {
        Command exit = cli.addCommand(Constants.EXIT, Constants.QUIT, Constants.Q).setDescription(Constants.EXIT_DESCRIPTION);
        exit.addAction(args -> turnOff());
    }
}
