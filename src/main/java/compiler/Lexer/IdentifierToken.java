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

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof  IdentifierToken) && ((IdentifierToken) obj).label.equals(this.label);
    }
}
