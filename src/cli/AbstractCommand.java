package cli;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AbstractCommand {

    protected static String OPTIONS = "Options:";
    protected static String COMMANDS = "Commands:";

    @Getter
    protected String description = "";
    @Getter
    protected List<String> names;
    protected boolean isHelp;

    public AbstractCommand(String... names) {
        this.names = List.of(names);
    }

    public AbstractCommand(Collection<String> names) {
        this.names = new ArrayList<>(names);
    }

    String getCommandNamesString() {
        String string = names.stream().reduce("", (s, n) -> s + ", " + n);
        return string.substring(2);
    }

    boolean isHelp() {
        return isHelp;
    }

    void setAsHelp() {
        isHelp = true;
    }

    public AbstractCommand setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getFirstName() {
        return !names.isEmpty() ? names.get(0) : "";
    }

}
