package compiler.parser;

import java.util.ArrayList;
import java.util.Objects;

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StatementList that = (StatementList) o;
            return statements.equals(that.statements);
        }

        @Override
        public int hashCode() {
            return Objects.hash(statements);
        }
    }
    static public abstract class Statement {
        @Override
        public abstract String toString();

        @Override
        public abstract boolean equals(Object obj);
    }

    static public class Type{
        public String type;
        public boolean isArray;

        public Type() {}

        public Type(String type, boolean isArray) {
            this.type = type;
            this.isArray = isArray;
        }

        @Override
        public String toString() {
            return "Type( " + type + (isArray ? "[]" : "" ) + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Type type1 = (Type) o;
            return isArray == type1.isArray && type.equals(type1.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, isArray);
        }
    }

    static public class FunctionDef extends Statement {
        String identifier;
        Type returnType;
        ArrayList<Param> paramList;
        StatementList functionCode;

        public FunctionDef() {
        }

        public FunctionDef(String identifier, Type returnType, ArrayList<Param> paramList, StatementList functionCode) {
            this.identifier = identifier;
            this.returnType = returnType;
            this.paramList = paramList;
            this.functionCode = functionCode;
        }

        @Override
        public String toString() {
            return "FunctionDef{" + "\n" +
                    "identifier='" + identifier + '\'' + "\n" +
                    "returnType=" + returnType + "\n" +
                    ", paramList=" + paramList + "\n" +
                    ", functionCode=" + functionCode + "\n" +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FunctionDef that = (FunctionDef) o;
            return Objects.equals(identifier, that.identifier) && Objects.equals(returnType, that.returnType) && Objects.equals(paramList, that.paramList) && Objects.equals(functionCode, that.functionCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier, returnType, paramList, functionCode);
        }
    }
    static public class Param {
        Type type;
        String identifier;

        public Param() {
        }

        public Param(Type type, String identifier) {
            this.type = type;
            this.identifier = identifier;
        }

        @Override
        public String toString() {
            return "Param{" + "\n" +
                    "type=" + type + "\n" +
                    ", identifier='" + identifier + '\'' + "\n" +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Param param = (Param) o;
            return Objects.equals(type, param.type) && Objects.equals(identifier, param.identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, identifier);
        }
    }

    static public class VarCreation extends Statement {
        String identifier;
        Type type;
        Expression varExpr;

        public VarCreation() {
        }

        public VarCreation(String identifier, Type type, Expression varExpr) {
            this.identifier = identifier;
            this.type = type;
            this.varExpr = varExpr;
        }

        @Override
        public String toString() {
            return "VarCreation{" + "\n" +
                    "identifier='" + identifier + '\'' + "\n" +
                    ", type=" + type + "\n" +
                    ", varExpr=" + varExpr + "\n" +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VarCreation that = (VarCreation) o;
            return Objects.equals(identifier, that.identifier) && Objects.equals(type, that.type) && Objects.equals(varExpr, that.varExpr);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier, type, varExpr);
        }
    }
    static public class ValCreation extends Statement {
        String identifier;
        Type type;
        Expression valExpr;

        public ValCreation() {
        }

        public ValCreation(String identifier, Type type, Expression valExpr) {
            this.identifier = identifier;
            this.type = type;
            this.valExpr = valExpr;
        }

        @Override
        public String toString() {
            return "ValCreation{" + "\n" +
                    "identifier='" + identifier + '\'' + "\n" +
                    ", type=" + type + "\n" +
                    ", valExpr=" + valExpr + "\n" +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ValCreation that = (ValCreation) o;
            return Objects.equals(identifier, that.identifier) && Objects.equals(type, that.type) && Objects.equals(valExpr, that.valExpr);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier, type, valExpr);
        }
    }
    static public class ConstCreation extends Statement {
        String identifier;
        Type type;
        Expression initExpr;

        public ConstCreation() {
        }

        public ConstCreation(String identifier, Type type, Expression initExpr) {
            this.identifier = identifier;
            this.type = type;
            this.initExpr = initExpr;
        }

        @Override
        public String toString() {
            return "ConstCreation{" +
                    "identifier='" + identifier + '\'' +
                    ", type=" + type +
                    ", valExpr=" + initExpr +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConstCreation that = (ConstCreation) o;
            return Objects.equals(identifier, that.identifier) && Objects.equals(type, that.type) && Objects.equals(initExpr, that.initExpr);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier, type, initExpr);
        }
    }

    static public class DeleteStt extends Statement {
        RefToValue toDelete;

        public DeleteStt() {
        }

        public DeleteStt(RefToValue toDelete) {
            this.toDelete = toDelete;
        }

        @Override
        public String toString() {
            return "DeleteStt{" +
                    "toDelete=" + toDelete +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeleteStt deleteStt = (DeleteStt) o;
            return Objects.equals(toDelete, deleteStt.toDelete);
        }

        @Override
        public int hashCode() {
            return Objects.hash(toDelete);
        }
    }

    static public class VarAssign extends Statement{
        public RefToValue ref;
        public Expression value;

        public VarAssign() {
        }

        public VarAssign(RefToValue ref, Expression value) {
            this.ref = ref;
            this.value = value;
        }

        @Override
        public String toString() {
            return "RefVarAssign{" + "\n" +
                    "ref=" + ref + "\n" +
                    ", value=" + value + "\n" +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VarAssign that = (VarAssign) o;
            return Objects.equals(ref, that.ref);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), ref);
        }
    }

    static public class Record extends Statement {
        String identifier;
        ArrayList<RecordVar> recordVars;

        public Record() {
        }

        public Record(String identifier, ArrayList<RecordVar> recordVars) {
            this.identifier = identifier;
            this.recordVars = recordVars;
        }

        @Override
        public String toString() {
            return "Record{" + "\n" +
                    "identifier='" + identifier + '\'' + "\n" +
                    ", recordVars=" + recordVars + "\n" +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Record record = (Record) o;
            return Objects.equals(identifier, record.identifier) && Objects.equals(recordVars, record.recordVars);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier, recordVars);
        }
    }
    static public class RecordVar {
        String identifier;
        Type type;

        public RecordVar() {
        }

        public RecordVar(String identifier, Type type) {
            this.identifier = identifier;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RecordVar recordVar = (RecordVar) o;
            return Objects.equals(identifier, recordVar.identifier) && Objects.equals(type, recordVar.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier, type);
        }
    }

    static public class FunctionCall extends Expression {
        String identifier;
        ArrayList<Expression> paramVals;

        public FunctionCall() {
        }

        public FunctionCall(String identifier, ArrayList<Expression> paramVals) {
            this.identifier = identifier;
            this.paramVals = paramVals;
        }

        @Override
        public String toString() {
            return "FunctionCall{" + "\n" +
                    "identifier='" + identifier + '\'' + "\n" +
                    ", paramVals=" + paramVals + "\n" +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FunctionCall that = (FunctionCall) o;
            return Objects.equals(identifier, that.identifier) && Objects.equals(paramVals, that.paramVals);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier, paramVals);
        }
    }

    static public class IfCond extends Statement {
        Expression condition;
        StatementList codeBlock;
        StatementList elseCodeBlock;

        public IfCond() {
        }

        public IfCond(Expression condition, StatementList codeBlock, StatementList elseCodeBlock) {
            this.condition = condition;
            this.codeBlock = codeBlock;
            this.elseCodeBlock = elseCodeBlock;
        }

        @Override
        public String toString() {
            return "IfCond{" + "\n" +
                    "condition=" + condition + "\n" +
                    ", codeBlock=" + codeBlock + "\n" +
                    ", elseCodeBlock=" + elseCodeBlock + "\n" +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IfCond ifCond = (IfCond) o;
            return Objects.equals(condition, ifCond.condition) && Objects.equals(codeBlock, ifCond.codeBlock) && Objects.equals(elseCodeBlock, ifCond.elseCodeBlock);
        }

        @Override
        public int hashCode() {
            return Objects.hash(condition, codeBlock, elseCodeBlock);
        }
    }

    static public class ForLoop extends Statement {
        RefToValue loopVal;
        Expression initValExpr;
        Expression endValExpr;
        Expression increment;
        StatementList codeBlock;

        public ForLoop() {
        }

        public ForLoop(RefToValue loopVal, Expression initValExpr, Expression endValExpr, Expression increment, StatementList codeBlock) {
            this.loopVal = loopVal;
            this.initValExpr = initValExpr;
            this.endValExpr = endValExpr;
            this.increment = increment;
            this.codeBlock = codeBlock;
        }

        @Override
        public String toString() {
            return "ForLoop{" + "\n" +
                    "loopVal='" + loopVal + '\'' + "\n" +
                    ", initValExpr=" + initValExpr + "\n" +
                    ", endValExpr=" + endValExpr + "\n" +
                    ", increment=" + increment + "\n" +
                    ", codeBlock=" + codeBlock + "\n" +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ForLoop forLoop = (ForLoop) o;
            return Objects.equals(loopVal, forLoop.loopVal) && Objects.equals(initValExpr, forLoop.initValExpr) && Objects.equals(endValExpr, forLoop.endValExpr) && Objects.equals(increment, forLoop.increment) && Objects.equals(codeBlock, forLoop.codeBlock);
        }

        @Override
        public int hashCode() {
            return Objects.hash(loopVal, initValExpr, endValExpr, increment, codeBlock);
        }
    }
    static public class WhileLoop extends Statement {
        Expression condition;
        StatementList codeBlock;

        public WhileLoop() {
        }

        public WhileLoop(Expression condition, StatementList codeBlock) {
            this.condition = condition;
            this.codeBlock = codeBlock;
        }

        @Override
        public String toString() {
            return "WhileLoop{" + "\n" +
                    "condition=" + condition + "\n" +
                    ", codeBlock=" + codeBlock + "\n" +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WhileLoop whileLoop = (WhileLoop) o;
            return Objects.equals(condition, whileLoop.condition) && Objects.equals(codeBlock, whileLoop.codeBlock);
        }

        @Override
        public int hashCode() {
            return Objects.hash(condition, codeBlock);
        }
    }

    static public class ReturnExpr extends Statement {
        Expression expr;

        public ReturnExpr() {
        }

        public ReturnExpr(Expression expr) {
            this.expr = expr;
        }

        @Override
        public String toString() {
            return "ReturnExpr{" + "\n" +
                    "expr=" + expr + "\n" +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReturnExpr that = (ReturnExpr) o;
            return Objects.equals(expr, that.expr);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expr);
        }
    }

    static public abstract class Expression extends Statement {
        @Override
        public abstract String toString();
    }

    public static class DirectValue extends Expression {
        public String value;
        public Type type;

        public DirectValue() {
        }

        public DirectValue(String value, Type type) {
            this.value = value;
            this.type = type;
        }

        @Override
        public String toString() {
            return "DirectValue{" + "\n" +
                    "value='" + value + '\'' + "\n" +
                    ", type=" + type + "\n" +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DirectValue that = (DirectValue) o;
            return Objects.equals(value, that.value) && Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, type);
        }
    }
    static public abstract class RefToValue extends  Expression {
    }
    static public class Identifier extends RefToValue{
        String id;

        public Identifier() {
        }

        public Identifier(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "Identifier{" +
                    "id='" + id + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Identifier that = (Identifier) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
    static public class ObjectAccess extends RefToValue {
        public RefToValue object;
        public String accessIdentifier;

        public ObjectAccess() {
        }

        public ObjectAccess(RefToValue object, String accessIdentifier) {
            this.object = object;
            this.accessIdentifier = accessIdentifier;
        }

        @Override
        public String toString() {
            return "ObjectAccessFromRef{" + "\n" +
                    "object=" + object + "\n" +
                    ", accessIdentifier='" + accessIdentifier + '\'' + "\n" +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ObjectAccess that = (ObjectAccess) o;
            return Objects.equals(object, that.object) && Objects.equals(accessIdentifier, that.accessIdentifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(object, accessIdentifier);
        }
    }
    static public class ArrayAccess extends RefToValue {
        public RefToValue ref;
        public Expression arrayIndex;

        public ArrayAccess() {
        }

        public ArrayAccess(RefToValue ref, Expression arrayIndex) {
            this.ref = ref;
            this.arrayIndex = arrayIndex;
        }

        @Override
        public String toString() {
            return "ArrayAccessFromRef{" + "\n" +
                    "ref='" + ref + '\'' + "\n" +
                    ", arrayIndex=" + arrayIndex + "\n" +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ArrayAccess that = (ArrayAccess) o;
            return Objects.equals(ref, that.ref) && Objects.equals(arrayIndex, that.arrayIndex);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ref, arrayIndex);
        }
    }

    static public class ArrayCreation extends Expression {
        public String typeIdentifier;
        public Expression arraySize;

        public ArrayCreation() {
        }

        public ArrayCreation(String typeIdentifier, Expression arraySize) {
            this.typeIdentifier = typeIdentifier;
            this.arraySize = arraySize;
        }

        @Override
        public String toString() {
            return "ArrayCreation{" + "\n" +
                    "typeIdentifier='" + typeIdentifier + '\'' + "\n" +
                    ", arraySize=" + arraySize + "\n" +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ArrayCreation that = (ArrayCreation) o;
            return Objects.equals(typeIdentifier, that.typeIdentifier) && Objects.equals(arraySize, that.arraySize);
        }

        @Override
        public int hashCode() {
            return Objects.hash(typeIdentifier, arraySize);
        }
    }

    // Actually useless, object creation are represented using FunctionCall
    static public class ObjectCreation extends Expression {
        String objectIdentifier;
        ArrayList<Expression> vals;

        public ObjectCreation() {
        }

        public ObjectCreation(String objectIdentifier, ArrayList<Expression> vals) {
            this.objectIdentifier = objectIdentifier;
            this.vals = vals;
        }

        @Override
        public String toString() {
            return "ObjectCreation{" + "\n" +
                    "objectIdentifier='" + objectIdentifier + '\'' + "\n" +
                    ", vals=" + vals + "\n" +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ObjectCreation that = (ObjectCreation) o;
            return Objects.equals(objectIdentifier, that.objectIdentifier) && Objects.equals(vals, that.vals);
        }

        @Override
        public int hashCode() {
            return Objects.hash(objectIdentifier, vals);
        }
    }

    static public abstract class Comparison extends Expression {
        Expression expr1; Expression expr2;

        public Comparison() {
        }

        public Comparison(Expression expr1, Expression expr2) {
            this.expr1 = expr1;
            this.expr2 = expr2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Comparison comp = (Comparison) o;
            return Objects.equals(expr1, comp.expr1) && Objects.equals(expr2, comp.expr2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expr1, expr2);
        }
    }
    // a==b
    static public class EqComp extends Comparison {

        public EqComp() {
        }

        public EqComp(Expression expr1, Expression expr2) {
            super(expr1, expr2);
        }

        @Override
        public String toString() {
            return expr1 + " == " + expr2;
        }
    }
    // a<>b
    static public class NotEqComp extends Comparison {

        public NotEqComp() {
        }

        public NotEqComp(Expression expr1, Expression expr2) {
            super(expr1, expr2);
        }


        @Override
        public String toString() {
            return expr1 + " <> " + expr2;
        }
    }
    // a>=b
    static public class GrEqComp extends Comparison {

        public GrEqComp() {
        }

        public GrEqComp(Expression expr1, Expression expr2) {
            super(expr1, expr2);
        }

        @Override
        public String toString() {
            return expr1 + " >= " + expr2;
        }
    }
    // a<=b
    static public class SmEqComp extends Comparison {

        public SmEqComp() {
        }

        public SmEqComp(Expression expr1, Expression expr2) {
            super(expr1, expr2);
        }

        @Override
        public String toString() {
            return expr1 + " <= " + expr2;
        }

    }
    // a>b
    static public class GrComp extends Comparison {

        public GrComp() {
        }

        public GrComp(Expression expr1, Expression expr2) {
            super(expr1, expr2);
        }

        @Override
        public String toString() {
            return expr1 + " > " + expr2;
        }

    }
    // a<b
    static public class SmComp extends Comparison {

        public SmComp() {
        }

        public SmComp(Expression expr1, Expression expr2) {
            super(expr1, expr2);
        }

        @Override
        public String toString() {
            return expr1 + " < " + expr2;
        }

    }
    static public class AndComp extends Comparison {

        public AndComp() {
        }

        public AndComp(Expression expr1, Expression expr2) {
            super(expr1, expr2);
        }

        @Override
        public String toString() {
            return expr1 + " and " + expr2;
        }
    }

    static public class OrComp extends Comparison {

        public OrComp() {
        }

        public OrComp(Expression expr1, Expression expr2) {
            super(expr1, expr2);
        }

        @Override
        public String toString() {
            return expr1 + " or " + expr2;
        }

    }

    static public abstract class MathExpr extends Expression {}
    // -a
    static public class NegateExpr extends MathExpr {
        public Expression expr;

        public NegateExpr() {
        }

        public NegateExpr(Expression expr) {
            this.expr = expr;
        }

        @Override
        public String toString() {
            return "-" + expr;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NegateExpr that = (NegateExpr) o;
            return Objects.equals(expr, that.expr);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expr);
        }
    }


    // Shouldn't be used, represented in AST by going deeper
    static public class PrioExpr extends MathExpr {
        Expression expr;

        public PrioExpr() {
        }

        public PrioExpr(Expression expr) {
            this.expr = expr;
        }

        @Override
        public String toString() {
            return "(" + expr + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PrioExpr prioExpr = (PrioExpr) o;
            return Objects.equals(expr, prioExpr.expr);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expr);
        }
    }
    static public class AddExpr extends MathExpr {
        public Expression expr1; public Expression expr2;

        public AddExpr() {
        }

        public AddExpr(Expression expr1, Expression expr2) {
            this.expr1 = expr1;
            this.expr2 = expr2;
        }

        @Override
        public String toString() {
            return expr1 + " + " + expr2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AddExpr addExpr = (AddExpr) o;
            return Objects.equals(expr1, addExpr.expr1) && Objects.equals(expr2, addExpr.expr2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expr1, expr2);
        }
    }
    static public class SubExpr extends MathExpr {
        public Expression expr1; public Expression expr2;

        public SubExpr() {
        }

        public SubExpr(Expression expr1, Expression expr2) {
            this.expr1 = expr1;
            this.expr2 = expr2;
        }

        @Override
        public String toString() {
            return expr1 + " - " + expr2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SubExpr subExpr = (SubExpr) o;
            return Objects.equals(expr1, subExpr.expr1) && Objects.equals(expr2, subExpr.expr2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expr1, expr2);
        }
    }
    static public class DivExpr extends MathExpr {
        public Expression expr1; public Expression expr2;

        public DivExpr() {
        }

        public DivExpr(Expression expr1, Expression expr2) {
            this.expr1 = expr1;
            this.expr2 = expr2;
        }

        @Override
        public String toString() {
            return expr1 + "/" + expr2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DivExpr divExpr = (DivExpr) o;
            return Objects.equals(expr1, divExpr.expr1) && Objects.equals(expr2, divExpr.expr2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expr1, expr2);
        }
    }
    static public class MultExpr extends MathExpr {
        public Expression expr1; public Expression expr2;

        public MultExpr() {
        }

        public MultExpr(Expression expr1, Expression expr2) {
            this.expr1 = expr1;
            this.expr2 = expr2;
        }

        @Override
        public String toString() {
            return expr1 + "*" + expr2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MultExpr multExpr = (MultExpr) o;
            return Objects.equals(expr1, multExpr.expr1) && Objects.equals(expr2, multExpr.expr2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expr1, expr2);
        }
    }
    static public class ModExpr extends MathExpr {
        public Expression expr1; public Expression expr2;

        public ModExpr() {
        }

        public ModExpr(Expression expr1, Expression expr2) {
            this.expr1 = expr1;
            this.expr2 = expr2;
        }

        @Override
        public String toString() {
            return expr1 + " % " + expr2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ModExpr modExpr = (ModExpr) o;
            return Objects.equals(expr1, modExpr.expr1) && Objects.equals(expr2, modExpr.expr2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expr1, expr2);
        }
    }


}
