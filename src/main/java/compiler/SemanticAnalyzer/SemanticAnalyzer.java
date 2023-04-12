package compiler.SemanticAnalyzer;

import compiler.Lexer.Lexer;
import compiler.Lexer.TypeToken;
import compiler.parser.ASTNodes;
import compiler.parser.Parser;
import compiler.parser.ParserException;

import java.io.StringReader;
import java.util.*;

public class SemanticAnalyzer {

    public ASTNodes.StatementList statementList;

    public SymbolTable symbolTable;

    //public Map<String, ArrayList<ASTNodes.RecordVar>> recordTable;

    public Map<String, ArrayList<ASTNodes.Param>> functionTable;

    public SemanticAnalyzer(ASTNodes.StatementList statementList) throws SemanticAnalyzerException {
        this.statementList = statementList;
        this.symbolTable = new SymbolTable();
        //this.recordTable = new HashMap<>();
        this.functionTable = new HashMap<>();
        ArrayList<ASTNodes.Param> p = new ArrayList<>();
        //new ASTNodes.Type("bool",false);
        p.add(new ASTNodes.Param(new ASTNodes.Type("bool",false),"neg"));
        functionTable.put("not",p);
        symbolTable.add("not",new ASTNodes.Type("bool",false));

        p = new ArrayList<>();
        p.add(new ASTNodes.Param(new ASTNodes.Type("string",false),"value"));
        functionTable.put("chr",p);
        symbolTable.add("chr",new ASTNodes.Type("int",false));

        p = new ArrayList<>();
        p.add(new ASTNodes.Param(new ASTNodes.Type("real",false),"value"));
        functionTable.put("floor",p);
        symbolTable.add("floor",new ASTNodes.Type("int",false));

        p = new ArrayList<>();
        p.add(new ASTNodes.Param(new ASTNodes.Type("string",false),"value"));
        functionTable.put("len",p);
        symbolTable.add("len",new ASTNodes.Type("int",false));

    }

    public void analyze(ASTNodes.StatementList statementList, SymbolTable table) throws SemanticAnalyzerException {
        // first scan to know all the functions, var and val
        for (ASTNodes.Statement s : statementList.statements) {
            if (s instanceof ASTNodes.FunctionDef) {
                ASTNodes.FunctionDef f = (ASTNodes.FunctionDef) s;
                if (functionTable.containsKey(f.identifier)) {
                    throw new SemanticAnalyzerException("double definition of function : "+f.identifier);
                }
                table.add(f.identifier,f.returnType);
                functionTable.put(f.identifier,f.paramList);
            } else if (s instanceof ASTNodes.VarCreation) {
                ASTNodes.VarCreation creation = (ASTNodes.VarCreation) s;
                table.add(creation.identifier,creation.type);
            } else if (s instanceof ASTNodes.ValCreation) {
                ASTNodes.ValCreation creation = (ASTNodes.ValCreation) s;
                table.add(creation.identifier,creation.type);
            }
            else if (s instanceof ASTNodes.Record) {
                ASTNodes.Record record = (ASTNodes.Record) s;
                ASTNodes.Type type = new ASTNodes.Type(record.identifier,false);
                table.add(record.identifier, type);
                if (functionTable.containsKey(record.identifier)) {
                    throw new SemanticAnalyzerException("double definition of record : "+record.identifier);

                }
                ArrayList<ASTNodes.Param> params = new ArrayList<>();
                for (ASTNodes.RecordVar var : record.recordVars) {
                    params.add(new ASTNodes.Param(var.type, var.identifier));
                }
                functionTable.put(record.identifier,params);
            }
        }

        for (ASTNodes.Statement s : statementList.statements) {
            analyzeStatement(s,table);
        }

    }

    public void analyzeStatement(ASTNodes.Statement s,SymbolTable table) throws SemanticAnalyzerException {
        if (s instanceof ASTNodes.FunctionDef) {
            ASTNodes.FunctionDef f = (ASTNodes.FunctionDef) s;
            analyzeFunctionDef(f,new SymbolTable(table));
        } else if (s instanceof ASTNodes.VarCreation) {
            ASTNodes.VarCreation creation = (ASTNodes.VarCreation) s;
            //if (table != symbolTable)
              //  table.add(creation.identifier,creation.type);
            analyzeVarCreation(creation,table);
        } else if (s instanceof ASTNodes.ValCreation) {
            ASTNodes.ValCreation creation = (ASTNodes.ValCreation) s;
            //if (table != symbolTable)
              //  table.add(creation.identifier,creation.type);
            analyzeValCreation(creation,table);
        } else if (s instanceof ASTNodes.ConstCreation) {
            ASTNodes.ConstCreation creation = (ASTNodes.ConstCreation) s;
            table.add(creation.identifier,creation.type);
            analyzeConstCreation(creation,table);
        } else if (s instanceof ASTNodes.VarAssign) {
            ASTNodes.VarAssign assign = (ASTNodes.VarAssign) s;
            analyzeVarAssign(assign,table);
        } else if (s instanceof ASTNodes.IfCond) {
            ASTNodes.IfCond cond = (ASTNodes.IfCond) s;
            analyzeIfCond(cond,new SymbolTable(table));
        } else if (s instanceof ASTNodes.ForLoop) {
            ASTNodes.ForLoop loop = (ASTNodes.ForLoop) s;
            analyzeForLoop(loop,new SymbolTable(table));
        } else if (s instanceof ASTNodes.WhileLoop) {
            ASTNodes.WhileLoop loop = (ASTNodes.WhileLoop) s;
            analyzeWhileLoop(loop,new SymbolTable(table));
        } else if (s instanceof ASTNodes.ReturnExpr) {
            analyzeExpression(((ASTNodes.ReturnExpr) s).expr,table);
        } else if (s instanceof ASTNodes.Expression) {
            analyzeExpression((ASTNodes.Expression) s,table);
        } else if (s instanceof ASTNodes.DeleteStt) {
            ASTNodes.DeleteStt del = (ASTNodes.DeleteStt) s;
            // will throw an error if the variable to delete is unknown
            ASTNodes.Type type = analyzeRefToValue(del.toDelete,table);
        }

    }


    public void analyzeForLoop(ASTNodes.ForLoop loop, SymbolTable table) throws SemanticAnalyzerException {
        ASTNodes.Type initType = analyzeExpression(loop.initValExpr,table);
        if (initType.type != "int") {
            throw new SemanticAnalyzerException("loop index should be a int");
        }
        ASTNodes.Type endType = analyzeExpression(loop.endValExpr, table);
        if (endType.type != "int") {
            throw new SemanticAnalyzerException("end index in forLoop should be a int");
        }
        ASTNodes.Type incType = analyzeExpression(loop.increment, table);
        if (incType.type != "int") {
            throw new SemanticAnalyzerException("increment in forLoop should be a int");
        }

        if (!(loop.loopVal instanceof ASTNodes.Identifier)) {
            throw new SemanticAnalyzerException("iteration variable should cannot come from an array or object");
        }
        if (!table.contain(((ASTNodes.Identifier) loop.loopVal).id)) {
            throw new SemanticAnalyzerException("unknown loop variable");
        }
        if (table.get(((ASTNodes.Identifier) loop.loopVal).id).type != "int") {
            throw new SemanticAnalyzerException("loop variable should be an int");
        }

        analyze(loop.codeBlock,table);

    }
    public void analyzeWhileLoop(ASTNodes.WhileLoop loop, SymbolTable table) throws SemanticAnalyzerException {
        ASTNodes.Type type = analyzeExpression(loop.condition,table);
        if (type.type != "bool") {
            throw new SemanticAnalyzerException("while condition should have a boolean condition");
        }
        analyze(loop.codeBlock,table);
    }

    public void analyzeIfCond(ASTNodes.IfCond cond, SymbolTable table) throws SemanticAnalyzerException {
        ASTNodes.Type type = analyzeExpression(cond.condition,table);
        if (type.type != "bool") {
            throw new SemanticAnalyzerException("if condition should have a boolean condition");
        }
        analyze(cond.codeBlock,table);

        if (cond.elseCodeBlock != null) {
            analyze(cond.elseCodeBlock, table);
        }
    }

    public ASTNodes.Type analyzeFunctionCall(ASTNodes.FunctionCall call, SymbolTable table) throws SemanticAnalyzerException {
        if (!table.contain(call.identifier)) {
            throw new SemanticAnalyzerException("unknown identifier : " + call.identifier);
        }
        ArrayList<ASTNodes.Param> tParam = functionTable.get(call.identifier);

        for (int i = 0; i < tParam.size(); i++) {
            ASTNodes.Type type = analyzeExpression(call.paramVals.get(i), table);
            if (call.identifier.equals("len")) {
                if (type.isArray != true && !type.equals(new ASTNodes.Type("string",false))) {
                    throw new SemanticAnalyzerException("wrong type for argument in function call " + call.identifier);
                }
            } else {
                if (!tParam.get(i).type.equals(type)) {
                    throw new SemanticAnalyzerException("wrong type for argument in function call " + call.identifier);

                }
            }
        }

        return table.get(call.identifier);
    }

    public ASTNodes.Type analyzeRefToValue(ASTNodes.RefToValue ref, SymbolTable table) throws SemanticAnalyzerException {
        Stack<Object> stack = new Stack<>();
        ASTNodes.RefToValue cur = ref;

        while (!(cur instanceof ASTNodes.Identifier)) {
            if (cur instanceof ASTNodes.ArrayAccess) {
                // check for index array now ?
                //int index = evaluateExpression(((ASTNodes.ArrayAccess) cur).arrayIndex);
                stack.add((Integer) 0);
                cur = ((ASTNodes.ArrayAccess) cur).ref;
            } else if (cur instanceof ASTNodes.ObjectAccess) {
                stack.add(((ASTNodes.ObjectAccess) cur).accessIdentifier);
                cur = ((ASTNodes.ObjectAccess) cur).object;
            }
        }
        stack.add(((ASTNodes.Identifier) cur).id);

        //Object ele = queue.remove();
        String symbol = (String) stack.pop();
        ASTNodes.Type type = table.get(symbol);

        while (!stack.isEmpty()) {
            Object ele = stack.pop();
            if (ele instanceof String) {
                if (functionTable.get(type.type) == null) {
                    throw new SemanticAnalyzerException("no record " + type.type);
                }
                boolean flag = false;
                for (ASTNodes.Param var : functionTable.get(type.type)) {
                    if (((String) ele).equals(var.identifier)) {
                        type = var.type;
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    throw new SemanticAnalyzerException("record " + type.type + " does not contains field : " + (String) ele);
                }

            } else if (ele instanceof Integer) {
                // nothing, maybe something if extension of arrays
            }
        }

        return type;
    }

    public void analyzeVarAssign(ASTNodes.VarAssign assign, SymbolTable table) throws SemanticAnalyzerException {
        ASTNodes.Type exprType = analyzeExpression(assign.value,table);

        ASTNodes.Type type = analyzeRefToValue(assign.ref,table);

        if (!type.type.equals(exprType.type)) { // ".type" because if array access, it this condition would be false
            throw new SemanticAnalyzerException("tried to assign " + exprType.type + "to a " + type.type + "variable");
        }

    }

    public void analyzeValCreation(ASTNodes.ValCreation creation,SymbolTable table) throws SemanticAnalyzerException {
        if (creation.type.type.equals("void")) {
            throw new SemanticAnalyzerException("Val type cannot be void");
        }
        if (creation.valExpr != null) {
            if (!creation.type.equals(analyzeExpression(creation.valExpr,table))) {
                throw new SemanticAnalyzerException("types are not matching");
            }
        }
    }

    public void analyzeVarCreation(ASTNodes.VarCreation creation,SymbolTable table) throws SemanticAnalyzerException {
        if (creation.type.type.equals("void")) {
            throw new SemanticAnalyzerException("Var type cannot be void");
        }
        if (creation.varExpr != null) {
            ASTNodes.Type type = analyzeExpression(creation.varExpr,table);
            if (!creation.type.equals(type)) {
                throw new SemanticAnalyzerException("types are not matching");
            }
        }
        //return creation.type;
    }

    public void analyzeConstCreation(ASTNodes.ConstCreation creation,SymbolTable table) throws SemanticAnalyzerException {
        if (creation.type.type.equals("void")) {
            throw new SemanticAnalyzerException("Const type cannot be void");
        }
        if (creation.type.type != "int" && creation.type.type != "real" &&
                creation.type.type != "string" && creation.type.type != "bool") {
            throw new SemanticAnalyzerException("Const type must be a base type");

        }
        if (creation.initExpr != null) {
            if (!creation.type.equals(analyzeExpression(creation.initExpr,table))) {
                throw new SemanticAnalyzerException("types are not matching");
            }
        }
    }

    public ASTNodes.Type analyzeFunctionDef(ASTNodes.FunctionDef functionDef, SymbolTable table) throws SemanticAnalyzerException {
        ASTNodes.Type type = functionDef.returnType;
        ASTNodes.StatementList block = functionDef.functionCode;
        if (type.type.equals("void")) {
            if (block.statements.get(block.statements.size()-1) instanceof ASTNodes.ReturnExpr) {
                throw new SemanticAnalyzerException("void function cannot return anything");
            }
            for (ASTNodes.Param p: functionDef.paramList) {
                table.add(p.identifier,p.type);
            }
            analyze(block,table);
            return type;
        } else {
            if (!(block.statements.get(block.statements.size()-1) instanceof ASTNodes.ReturnExpr)) {
                throw new SemanticAnalyzerException("non void function must return");
            }
            for (ASTNodes.Param p: functionDef.paramList) {
                table.add(p.identifier,p.type);
            }
            analyze(block,table);
            ASTNodes.ReturnExpr last = (ASTNodes.ReturnExpr)block.statements.get(block.statements.size()-1);
            ASTNodes.Type actualReturnType = analyzeExpression(last.expr,table);
            if (!type.equals(actualReturnType)) {
                throw new SemanticAnalyzerException("function returns wrong type");
            }
            return type;
        }
    }

    /*
    public ASTNodes.Type analyzeObjectCreation(ASTNodes.ObjectCreation creation, SymbolTable table) throws SemanticAnalyzerException {
        if (!recordTable.containsKey(creation.objectIdentifier)) {
            throw new SemanticAnalyzerException("unknown record : " + creation.objectIdentifier);
        }

        ArrayList<ASTNodes.RecordVar> tParam = recordTable.get(creation.objectIdentifier);
        for (int i = 0; i < tParam.size(); i++) {
            if (!tParam.get(i).equals(analyzeExpression(creation.vals.get(i),table))) {
                throw new SemanticAnalyzerException("wrong type for argument in record creation");

            }
        }
        return table.get(creation.objectIdentifier);
    }
    */

    public ASTNodes.Type analyzeMathExpr(ASTNodes.MathExpr expr, SymbolTable table) throws SemanticAnalyzerException {
        if (expr instanceof ASTNodes.NegateExpr) {
            ASTNodes.NegateExpr e = (ASTNodes.NegateExpr) expr;
            String type = analyzeExpression(e.expr, table).type;
            if (type != "int" && type != "real") {
                throw new SemanticAnalyzerException("tried to negate a type " + type);
            }
            return analyzeExpression(e.expr, table);
        } else if (expr instanceof ASTNodes.AddExpr) {
            ASTNodes.AddExpr e = (ASTNodes.AddExpr) expr;
            ASTNodes.Type type1 = analyzeExpression(e.expr1,table);
            ASTNodes.Type type2 = analyzeExpression(e.expr2,table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types in addition should be the same");
            }
            if (type1.type != "int" && type1.type != "real") {
                throw new SemanticAnalyzerException("add operation not supported on : " + type1.type);
            }
            return type1;
        } else if (expr instanceof ASTNodes.SubExpr) {
            ASTNodes.SubExpr e = (ASTNodes.SubExpr) expr;
            ASTNodes.Type type1 = analyzeExpression(e.expr1,table);
            ASTNodes.Type type2 = analyzeExpression(e.expr2,table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types in subtraction should be the same");
            }
            if (type1.type != "int" && type1.type != "real") {
                throw new SemanticAnalyzerException("sub operation not supported on : " + type1.type);
            }
            return type1;
        } else if (expr instanceof ASTNodes.DivExpr) {
            ASTNodes.DivExpr e = (ASTNodes.DivExpr) expr;
            ASTNodes.Type type1 = analyzeExpression(e.expr1,table);
            ASTNodes.Type type2 = analyzeExpression(e.expr2,table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types in division should be the same");
            }
            if (type1.type != "int" && type1.type != "real") {
                throw new SemanticAnalyzerException("division operation not supported on : " + type1.type);
            }
            return type1;

        } else if (expr instanceof ASTNodes.MultExpr) {
            ASTNodes.MultExpr e = (ASTNodes.MultExpr) expr;
            ASTNodes.Type type1 = analyzeExpression(e.expr1,table);
            ASTNodes.Type type2 = analyzeExpression(e.expr2,table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types in multiplication should be the same");
            }
            if (type1.type != "int" && type1.type != "real") {
                throw new SemanticAnalyzerException("multiplication operation not supported on : " + type1.type);
            }
            return type1;

        } else if (expr instanceof ASTNodes.ModExpr) {
            ASTNodes.ModExpr e = (ASTNodes.ModExpr) expr;
            ASTNodes.Type type1 = analyzeExpression(e.expr1,table);
            ASTNodes.Type type2 = analyzeExpression(e.expr2,table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types in modulo should be the same");
            }
            if (type1.type != "int") {
                throw new SemanticAnalyzerException("modulo operation not supported on : " + type1.type);
            }
            return type1;

        } else {
            throw new SemanticAnalyzerException("unknown math expression");
        }
    }

    public ASTNodes.Type analyzeComparison(ASTNodes.Comparison cmp, SymbolTable table) throws SemanticAnalyzerException {
        if (cmp instanceof ASTNodes.EqComp) {
            ASTNodes.EqComp c = (ASTNodes.EqComp) cmp;
            ASTNodes.Type type1 = analyzeExpression(c.expr1, table);
            ASTNodes.Type type2 = analyzeExpression(c.expr2, table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types should be the same in equality cmp");
            }
            if (type1.type != "int" && type1.type != "real" && type1.type != "bool" && type1.type != "string") {
                throw new SemanticAnalyzerException("type in eq comparison should be a base type but got : " + type1.type);
            }
            return new ASTNodes.Type("bool",false);
        } else if (cmp instanceof ASTNodes.NotEqComp) {
            ASTNodes.NotEqComp c = (ASTNodes.NotEqComp) cmp;
            ASTNodes.Type type1 = analyzeExpression(c.expr1, table);
            ASTNodes.Type type2 = analyzeExpression(c.expr2, table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types should be the same in inequality cmp");
            }
            if (type1.type != "int" && type1.type != "real" && type1.type != "bool" && type1.type != "string") {
                throw new SemanticAnalyzerException("type in ineq comparison should be a base type but got : " + type1.type);
            }
            return new ASTNodes.Type("bool",false);
        } else if (cmp instanceof ASTNodes.GrEqComp) {
            ASTNodes.GrEqComp c = (ASTNodes.GrEqComp) cmp;
            ASTNodes.Type type1 = analyzeExpression(c.expr1, table);
            ASTNodes.Type type2 = analyzeExpression(c.expr2, table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types should be the same in greater or eq cmp");
            }
            if (type1.type != "int" && type1.type != "real") {
                throw new SemanticAnalyzerException("type in greater or eq comparison should be int or real but got : " + type1.type);
            }
            return new ASTNodes.Type("bool",false);
        } else if (cmp instanceof ASTNodes.SmEqComp) {
            ASTNodes.SmEqComp c = (ASTNodes.SmEqComp) cmp;
            ASTNodes.Type type1 = analyzeExpression(c.expr1, table);
            ASTNodes.Type type2 = analyzeExpression(c.expr2, table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types should be the same in smaller or eq cmp");
            }
            if (type1.type != "int" && type1.type != "real") {
                throw new SemanticAnalyzerException("type in smaller or eq comparison should be int or real but got : " + type1.type);
            }
            return new ASTNodes.Type("bool",false);
        } else if (cmp instanceof ASTNodes.GrComp) {
            ASTNodes.GrComp c = (ASTNodes.GrComp) cmp;
            ASTNodes.Type type1 = analyzeExpression(c.expr1, table);
            ASTNodes.Type type2 = analyzeExpression(c.expr2, table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types should be the same in strict greater cmp");
            }
            if (type1.type != "int" && type1.type != "real" && type1.type != "bool" && type1.type != "string") {
                throw new SemanticAnalyzerException("type in strict greater comparison should be a base type but got : " + type1.type);
            }
            return new ASTNodes.Type("bool",false);

        } else if (cmp instanceof ASTNodes.SmComp) {
            ASTNodes.SmComp c = (ASTNodes.SmComp) cmp;
            ASTNodes.Type type1 = analyzeExpression(c.expr1, table);
            ASTNodes.Type type2 = analyzeExpression(c.expr2, table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types should be the same in strict smaller cmp");
            }
            if (type1.type != "int" && type1.type != "real" && type1.type != "bool" && type1.type != "string") {
                throw new SemanticAnalyzerException("type in strict smaller comparison should be a base type but got : " + type1.type);
            }
            return new ASTNodes.Type("bool",false);

        } else if (cmp instanceof ASTNodes.AndComp) {
            ASTNodes.AndComp c = (ASTNodes.AndComp) cmp;
            ASTNodes.Type type1 = analyzeExpression(c.expr1, table);
            ASTNodes.Type type2 = analyzeExpression(c.expr2, table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types should be the same in and operator");
            }
            if (type1.type != "bool") {
                throw new SemanticAnalyzerException("type in and operator should be a bool but got : " + type1.type);
            }
            return new ASTNodes.Type("bool",false);
        } else if (cmp instanceof ASTNodes.OrComp) {
            ASTNodes.OrComp c = (ASTNodes.OrComp) cmp;
            ASTNodes.Type type1 = analyzeExpression(c.expr1, table);
            ASTNodes.Type type2 = analyzeExpression(c.expr2, table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types should be the same in or operator");
            }
            if (type1.type != "bool") {
                throw new SemanticAnalyzerException("type in or operator should be a bool but got : " + type1.type);
            }
            return new ASTNodes.Type("bool",false);
        } else {
            throw new SemanticAnalyzerException("unknown comparison");
        }
    }

    public ASTNodes.Type analyzeExpression(ASTNodes.Expression expr, SymbolTable table) throws SemanticAnalyzerException {
        if (expr instanceof ASTNodes.DirectValue) {
            /*
            if (!table.contain(((ASTNodes.DirectValue) expr).value)) {
                throw new SemanticAnalyzerException("unknown identifier " + ((ASTNodes.DirectValue) expr).value);
            }
            */

            return ((ASTNodes.DirectValue) expr).type;
        } else if (expr instanceof ASTNodes.FunctionCall) {
            ASTNodes.FunctionCall call = (ASTNodes.FunctionCall) expr;
            return analyzeFunctionCall(call,table);
        } else if (expr instanceof ASTNodes.ArrayCreation) {
            ASTNodes.ArrayCreation creation = (ASTNodes.ArrayCreation) expr;
            if (creation.typeIdentifier.equals("void")) {
                throw new SemanticAnalyzerException("void type not allowed for array");
            }

            ASTNodes.Type type = analyzeExpression(creation.arraySize,table);
            if (type.type != "int") {
                throw new SemanticAnalyzerException("array size should be int but got " + type.type);
            }

            ASTNodes.Type actualType = new ASTNodes.Type(creation.typeIdentifier, true);
            return actualType;
        //} else if (expr instanceof ASTNodes.ObjectCreation) {
            //ASTNodes.ObjectCreation creation = (ASTNodes.ObjectCreation) expr;
            //return analyzeObjectCreation(creation,table);
        } else if (expr instanceof ASTNodes.MathExpr) {
            return analyzeMathExpr((ASTNodes.MathExpr) expr,table);
        } else if (expr instanceof ASTNodes.Comparison) {
            return analyzeComparison((ASTNodes.Comparison) expr, table);
        } else if (expr instanceof ASTNodes.RefToValue) {
            ASTNodes.RefToValue ref = (ASTNodes.RefToValue) expr;
            return analyzeRefToValue(ref,table);
        }

        throw new SemanticAnalyzerException("unknown thing in analyzeExpression");
    }



    public static void main(String[] args) throws ParserException {
        String input = "record Point {\n" +
                "    x int;\n" +
                "    y int;\n" +
                "}\n" +
                "\n" +
                "record Coord {\n" +
                "    valeur int;\n" +
                "    pt Point;\n" +
                "}\n" +
                "record Person {\n" +
                "    name string;\n" +
                "    location Coord;\n" +
                "    history int[];\n" +
                "}\n" +
                "var a int = 3;\n" +
                "val e int = a*2;\n" +
                "var c int[] = int[](5);  \n" +
                "var d Person = Person(\"me\", Coord(10,Point(3,7)), int[](a*2)); \n" +
                "d.history[0]=1000\n";



        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);

        Parser parser = new Parser(lexer);
        ASTNodes.StatementList sl;
        sl = parser.parseCode();


        int k = 10;

    }
}
