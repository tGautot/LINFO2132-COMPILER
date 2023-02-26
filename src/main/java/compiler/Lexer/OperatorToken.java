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
    SMLR_OR_EQUAL("<=");


    public final String label;

    OperatorToken(String label) {
        this.label = label;
    }

}
