package cli;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AbstractCommand {

    protected static String OPTIONS = "Options:"
    protected static String COMMANDS = "Commands:"

    @Getter
    protected String description = "";
    protected List<String> names;

    public AbstractCommand(String... names) {
        this.names = List.of(names);
    }

    public AbstractCommand(Collection<String> names) {
        this.names = new ArrayList<>(names);
    }

}
