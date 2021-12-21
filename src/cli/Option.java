package cli;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

public class Option extends AbstractCommand {

    private String value = null;
    @Setter@Getter
    private String defaultValue;

    public Option(String... names) {
        super(names);
    }

    public Option(Collection<String> names) {
        super(names);
    }

    void set(String value){
        this.value = value;
    }

    public String get(){
        return value == null ? defaultValue: value;
    }

    public Option setToDefault(){
        value = defaultValue;
        return this;
    }

    public Option setDefault(String value){
        defaultValue = value;
        return this;
    }

    public Option setDefaultIfNotSet(String value){
        if (isNotSet())
            setDefault(value);
        return this;
    }

    public boolean isSet(){
        return value != null;
    }

    public boolean isNotSet(){
        return !isSet();
    }
}
