package hu.webandmore.todo.api.model;

public enum Priority {
    HIGH,
    MEDIUM,
    LOW;

    @Override
    public String toString() {
        return Character.toUpperCase(name().charAt(0)) + name().toLowerCase().substring(1);
    }
}
