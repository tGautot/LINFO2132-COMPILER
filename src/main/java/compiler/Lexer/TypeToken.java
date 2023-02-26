package compiler.Lexer;

public enum TypeToken implements Symbol{
    INT("int"),
    DOUBLE("real"),
    BOOL("bool"),
    STRING("string");


    public final String label;

    TypeToken(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "TypeToken{" +
                "label='" + label + '\'' +
                '}';
    }
}
