package compiler.Lexer;

public class ValueToken implements Symbol{

    public enum ValueType {
        BOOL,
        INT,
        REAL,
        STRING
    }

    public ValueType valType;
    public String value;

    public ValueToken(ValueType valType, String value){
        this.valType = valType;
        this.value = value;
    }

    @Override
    public String toString() {
        return "ValueToken{" +
                "valType=" + valType +
                ", value='" + value + '\'' +
                '}';
    }


    @Override
    public boolean equals(Object obj) {
        return (obj instanceof  ValueToken)
                && ((ValueToken) obj).value.equals(this.value)
                && ((ValueToken) obj).valType.equals(this.valType);
    }
}
