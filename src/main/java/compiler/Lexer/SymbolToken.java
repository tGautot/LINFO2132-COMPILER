package compiler.Lexer;

public enum SymbolToken implements Symbol{

    OPEN_CB("{"),
    CLOSE_CB("}"),
    OPEN_BRACKET("["),
    CLOSE_BRACKET("]"),
    OPEN_PARENTHESIS("("),
    CLOSE_PARENTHESIS(")"),
    COMMA(","),
    SEMICOLON(";"),
    DOT("."),
    END_OF_FILE("");

    public final String label;

    SymbolToken(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "SymbolToken{" +
                "label='" + label + '\'' +
                '}';
    }
}
