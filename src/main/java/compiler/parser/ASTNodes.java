package compiler.parser;

import compiler.Lexer.TypeToken;

import java.util.ArrayList;

public class ASTNodes {
    static public class StatementList {
        public ArrayList<Statement> statements;

        @Override
        public String toString() {
            String s = "Statement list, size" + statements.size() + "\n";
            for(Statement stt : statements){
                s += stt.toString() + "\n";
            }
            return s;
        }
    }
    static public abstract class Statement {
        @Override
        public abstract String toString();
    }

    static public class Type{
        String type;
        boolean isArray;

        @Override
        public String toString() {
            return "Type( " + type + (isArray ? "[]" : "" ) + ")";
        }
    }
    /*static public class BaseType extends Type {
        String type;
    }
    static public class ArrayType extends Type {
        String type;
    }*/

    static public class FunctionDef extends Statement {
        String identifier;
        ArrayList<Param> paramList;
        StatementList functionCode;

        @Override
        public String toString() {
            return "FunctionDef{" + "\n" +
                    "identifier='" + identifier + '\'' + "\n" +
                    ", paramList=" + paramList + "\n" +
                    ", functionCode=" + functionCode + "\n" +
                    '}';
        }
    }
    static public class Param {
        Type type; // Maybe something else than token from lexer?
        String identifier;

        @Override
        public String toString() {
            return "Param{" + "\n" +
                    "type=" + type + "\n" +
                    ", identifier='" + identifier + '\'' + "\n" +
                    '}';
        }
    }

    static public class VarCreation extends Statement {
        String identifier;
        Type type;
        Expression varExpr;

        @Override
        public String toString() {
            return "VarCreation{" + "\n" +
                    "identifier='" + identifier + '\'' + "\n" +
                    ", type=" + type + "\n" +
                    ", varExpr=" + varExpr + "\n" +
                    '}';
        }
    }
    static public class ValCreation extends Statement {
        String identifier;
        Type type;
        Expression valExpr;

        @Override
        public String toString() {
            return "ValCreation{" + "\n" +
                    "identifier='" + identifier + '\'' + "\n" +
                    ", type=" + type + "\n" +
                    ", valExpr=" + valExpr + "\n" +
                    '}';
        }
    }

    static public class Record extends Statement {
        String identifier;
        ArrayList<RecordVar> recordVars;

        @Override
        public String toString() {
            return "Record{" + "\n" +
                    "identifier='" + identifier + '\'' + "\n" +
                    ", recordVars=" + recordVars + "\n" +
                    '}';
        }
    }
    static public class RecordVar {
        String identifier;
        Type type;
    }

    static public class FunctionCall extends Statement {
        String identifier;
        ArrayList<Expression> paramVals;

        @Override
        public String toString() {
            return "FunctionCall{" + "\n" +
                    "identifier='" + identifier + '\'' + "\n" +
                    ", paramVals=" + paramVals + "\n" +
                    '}';
        }
    }

    static public class IfCond extends Statement {
        Expression condition;
        StatementList codeBlock;
        StatementList elseCodeBlock;

        @Override
        public String toString() {
            return "IfCond{" + "\n" +
                    "condition=" + condition + "\n" +
                    ", codeBlock=" + codeBlock + "\n" +
                    ", elseCodeBlock=" + elseCodeBlock + "\n" +
                    '}';
        }
    }

    static public class ForLoop extends Statement {
        String loopVarIdentifier;
        Expression initValExpr;
        Expression endValExpr;
        Expression increment;
        StatementList codeBlock;

        @Override
        public String toString() {
            return "ForLoop{" + "\n" +
                    "loopVarIdentifier='" + loopVarIdentifier + '\'' + "\n" +
                    ", initValExpr=" + initValExpr + "\n" +
                    ", endValExpr=" + endValExpr + "\n" +
                    ", increment=" + increment + "\n" +
                    ", codeBlock=" + codeBlock + "\n" +
                    '}';
        }
    }
    static public class WhileLoop extends Statement {
        Expression condition;
        StatementList codeBlock;

        @Override
        public String toString() {
            return "WhileLoop{" + "\n" +
                    "condition=" + condition + "\n" +
                    ", codeBlock=" + codeBlock + "\n" +
                    '}';
        }
    }

    static public class ReturnExpr extends Statement {
        Expression expr;

        @Override
        public String toString() {
            return "ReturnExpr{" + "\n" +
                    "expr=" + expr + "\n" +
                    '}';
        }
    }

    static public abstract class Expression {
        @Override
        public abstract String toString();
    }
    public class DirectValue extends Expression {
        String value;
        Type type; // Only primitive type, change later

        @Override
        public String toString() {
            return "DirectValue{" + "\n" +
                    "value='" + value + '\'' + "\n" +
                    ", type=" + type + "\n" +
                    '}';
        }
    }
    static public abstract class RefToValue extends  Expression {
    }
    static public class ObjectAccessFromId extends RefToValue {
        String identifier;
        String accessIdentifier;

        @Override
        public String toString() {
            return "ObjectAccessFromId{" + "\n" +
                    "identifier='" + identifier + '\'' + "\n" +
                    ", accessIdentifier='" + accessIdentifier + '\'' + "\n" +
                    '}';
        }
    }
    static public class ObjectAccessFromRef extends RefToValue {
        RefToValue object;
        String accessIdentifier;

        @Override
        public String toString() {
            return "ObjectAccessFromRef{" + "\n" +
                    "object=" + object + "\n" +
                    ", accessIdentifier='" + accessIdentifier + '\'' + "\n" +
                    '}';
        }
    }
    static public class ArrayAccess extends RefToValue {
        String arrayId;
        Expression arrayIndex;

        @Override
        public String toString() {
            return "ArrayAccess{" + "\n" +
                    "arrayId='" + arrayId + '\'' + "\n" +
                    ", arrayIndex=" + arrayIndex + "\n" +
                    '}';
        }
    }

    static public class ArrayCreation extends Expression {
        String typeIdentifier;
        Expression arraySize;

        @Override
        public String toString() {
            return "ArrayCreation{" + "\n" +
                    "typeIdentifier='" + typeIdentifier + '\'' + "\n" +
                    ", arraySize=" + arraySize + "\n" +
                    '}';
        }
    }
    static public class ObjectCreation extends Expression {
        String objectIdentifier;
        ArrayList<Expression> vals;

        @Override
        public String toString() {
            return "ObjectCreation{" + "\n" +
                    "objectIdentifier='" + objectIdentifier + '\'' + "\n" +
                    ", vals=" + vals + "\n" +
                    '}';
        }
    }

    static public abstract class Comparison extends Expression {
    }
    // a==b
    static public class EqComp extends Comparison {
        Expression expr1; Expression expr2;

        @Override
        public String toString() {
            return expr1 + " == " + expr2;
        }
    }
    // a<>b
    static public class NotEqComp extends Comparison {
        Expression expr1; Expression expr2;

        @Override
        public String toString() {
            return expr1 + " <> " + expr2;
        }
    }
    // a>=b
    static public class GrEqComp extends Comparison {
        Expression expr1; Expression expr2;

        @Override
        public String toString() {
            return expr1 + " >= " + expr2;
        }
    }
    // a<=b
    static public class SmEqComp extends Comparison {
        Expression expr1; Expression expr2;

        @Override
        public String toString() {
            return expr1 + " <= " + expr2;
        }
    }
    // a>b
    static public class GrComp extends Comparison {
        Expression expr1; Expression expr2;

        @Override
        public String toString() {
            return expr1 + " > " + expr2;
        }
    }
    // a<b
    static public class SmComp extends Comparison {
        Expression expr1; Expression expr2;

        @Override
        public String toString() {
            return expr1 + " < " + expr2;
        }
    }

    static public abstract class MathExpr extends Expression {}
    // -a
    static public class NegateExpr extends MathExpr {
        Expression expr;

        @Override
        public String toString() {
            return "-" + expr;
        }
    }
    // (a)
    static public class PrioExpr extends MathExpr {
        Expression expr;

        @Override
        public String toString() {
            return "(" + expr + ")";
        }
    }
    static public class AddExpr extends MathExpr {
        Expression expr1; Expression expr2;

        @Override
        public String toString() {
            return expr1 + " + " + expr2;
        }
    }
    static public class SubExpr extends MathExpr {
        Expression expr1; Expression expr2;

        @Override
        public String toString() {
            return expr1 + " - " + expr2;
        }
    }
    static public class DivExpr extends MathExpr {
        Expression expr1; Expression expr2;

        @Override
        public String toString() {
            return expr1 + "/" + expr2;
        }
    }
    static public class MultExpr extends MathExpr {
        Expression expr1; Expression expr2;

        @Override
        public String toString() {
            return expr1 + "*" + expr2;
        }
    }
    static public class ModExpr extends MathExpr {
        Expression expr1; Expression expr2;

        @Override
        public String toString() {
            return expr1 + " % " + expr2;
        }
    }

}
