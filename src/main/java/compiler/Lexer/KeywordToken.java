package compiler.Lexer;

public enum KeywordToken implements Symbol{
    FUNCTION("proc"),
    RECORD("record"),
    CONST("const"),
    VARIABLE("var"),
    VALUE("val"),
    FOR_LOOP("for"),
    TO("to"),
    BY("by"),
    WHILE("while"),
    IF("if"),
    ELSE("else"),
    RETURN("return"),
    AND("and"),
    OR("or"),
    DELETE("delete");


    public final String label;

    KeywordToken(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "KeywordToken{" +
                "label='" + label + '\'' +
                '}';
    }
}
