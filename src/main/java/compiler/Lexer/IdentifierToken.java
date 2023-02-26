package compiler.Lexer;
public class IdentifierToken implements Symbol{
    public String label;
    public IdentifierToken(String label){
        this.label = label;
    }

    @Override
    public String toString() {
        return "IdentifierToken{" +
                "label='" + label + '\'' +
                '}';
    }
}
