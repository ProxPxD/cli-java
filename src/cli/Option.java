package cli;

import java.util.Collection;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Supplier;

public class Option extends AbstractCommand {

    private final Queue<String> values = new PriorityQueue<>();
    Supplier<String> defaultValueSupplier = () -> "";

    public Option(String... names) {
        super(names);
    }

    public Option(Collection<String> names) {
        super(names);
    }

    void add(String value) {
        values.add(value);
    }

    boolean hasValues() {
        return !values.isEmpty();
    }

    public String get() {
        return hasValues() ? values.poll() : defaultValueSupplier.get();
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
        values.clear();
    }
}
