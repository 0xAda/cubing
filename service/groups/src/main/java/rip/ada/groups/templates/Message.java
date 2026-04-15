package rip.ada.groups.templates;

public record Message(String message, Type type) {
    public enum Type {
        SUCCESS,
        WARNING,
        ERROR;
        public final String name = name().toLowerCase();
    }
}
