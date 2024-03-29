package compiler.Lexer;

public enum OperatorToken implements Symbol{
    PLUS("+"),
    MINUS("-"),
    TIMES("*"),
    DIVIDE("/"),
    MODULUS("%"),
    ASSIGN("="),
    EQUALS("=="),
    DIFFERENT("<>"),
    GREATER(">"),
    SMALLER("<"),
    GRTR_OR_EQUAL(">="),
    SMLR_OR_EQUAL("<="),
    PLUS_ASSIGN("+="),
    MINUS_ASSIGN("-="),
    TIMES_ASSIGN("*="),
    DIVIDE_ASSIGN("/="),
    MOD_ASSIGN("%=");


    public final String label;

    OperatorToken(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "OperatorToken{" +
                "label='" + label + '\'' +
                '}';
    }
}
