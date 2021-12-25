package cli;

import java.util.Collection;
import java.util.function.Supplier;

public class Option extends AbstractCommand {

    private String value = null;
    Supplier<String> defaultValueSupplier = () -> "";

    public Option(String... names) {
        super(names);
    }

    public Option(Collection<String> names) {
        super(names);
    }

    void set(String value){
        this.value = value;
    }

    public String get() {
        return value == null ? defaultValueSupplier.get() : value;
    }

    public Option setDefault(String value) {
        return setDefault(() -> value);
    }

    public Option setDefault(Supplier<String> supplier) {
        defaultValueSupplier = supplier;
        return this;
    }

    @Override
    public Option setDescription(String description) {
        return (Option) super.setDescription(description);
    }

    void clear() {
        value = null;
    }
}
