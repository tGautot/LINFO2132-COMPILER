package compiler.parser;

import compiler.Lexer.TypeToken;

import java.util.ArrayList;

public class ASTNodes {
    public class StatementList {
        public ArrayList<Statement> statements;
    }
    public class Statement { }

    public class Type{
        String type;
        boolean isArray;
    }

    public class FunctionDef extends Statement {
        String identifier;
        ArrayList<Param> paramList;
        StatementList functionCode;
    }
    public class Param {
        Type type; // Maybe something else than token from lexer?
        String identifier;
    }

    public class VarCreation extends Statement {
        String identifier;
        Type type;
    }
    public class VarCreationInit extends VarCreation {
        Expression initValExpr;
    }
    public class ValCreation extends Statement {
        String identifier;
        Type type;
        Expression valExpr;
    }

    public class Record extends Statement {
        String identifier;
        ArrayList<RecordVar> recordVars;
    }
    public class RecordVar {
        String identifier;
        Type type;
    }

    public class FunctionCall extends Statement {
        String identifier;
        ArrayList<Expression> paramVals;
    }

    public class IfCond extends Statement {
        Expression condition;
        StatementList codeBlock;
        StatementList elseCodeBlock;
    }

    public class ForLoop extends Statement {
        String loopVarIdentifier;
        Expression initValExpr;
        Expression endValExpr;
        Expression increment;
        StatementList codeBlock;
    }
    public class WhileLoop extends Statement {
        Expression condition;
        StatementList codeBlock;
    }

    public class ReturnExpr extends Statement {
        Expression expr;
    }

    public class Expression {}
    public class DirectValue extends Expression {
        String value;
        TypeToken type; // Only primitive type, change later
    }
    public class RefToValue extends  Expression {}
    public class ObjectAccessFromId extends RefToValue {
        String identifier;
        String accessIdentifier;
    }
    public class ObjectAccessFromRef extends RefToValue {
        RefToValue object;
        String accessIdentifier;
    }
    public class ArrayAccess extends RefToValue {
        String arrayId;
        Expression arrayIndex;
    }

    public class ArrayCreation extends Expression {
        String typeIdentifier;
        Expression arraySize;
    }
    public class ObjectCreation extends Expression {
        String objectIdentifier;
        ArrayList<Expression> vals;
    }

    public class Comparison extends Expression {}
    // a==b
    public class EqComp extends Comparison { Expression expr1; Expression expr2; }
    // a<>b
    public class NotEqComp extends Comparison { Expression expr1; Expression expr2; }
    // a>=b
    public class GrEqComp extends Comparison { Expression expr1; Expression expr2; }
    // a<=b
    public class SmEqComp extends Comparison { Expression expr1; Expression expr2; }
    // a>b
    public class GrComp extends Comparison { Expression expr1; Expression expr2; }
    // a<b
    public class SmComp extends Comparison { Expression expr1; Expression expr2; }

    public class MathExpr extends Expression {}
    // -a
    public class NegateExpr extends MathExpr {Expression expr;}
    // (a)
    public class PrioExpr extends MathExpr {Expression expr;}
    public class AddExpr extends MathExpr { Expression expr1; Expression expr2; }
    public class SubExpr extends MathExpr { Expression expr1; Expression expr2; }
    public class DivExpr extends MathExpr { Expression expr1; Expression expr2; }
    public class MultExpr extends MathExpr { Expression expr1; Expression expr2; }
    public class ModExpr extends MathExpr { Expression expr1; Expression expr2; }

}
