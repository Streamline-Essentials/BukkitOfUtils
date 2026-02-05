package host.plas.bou.compat;

public class EmptyHolder extends HeldHolder {
    public EmptyHolder(String identifier) {
        super(identifier, () -> null);
        setEnabled(false);
    }
}
