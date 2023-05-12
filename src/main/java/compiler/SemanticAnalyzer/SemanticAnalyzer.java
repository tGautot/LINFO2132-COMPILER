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

    public int start;

    public SymbolTable symbolTable;

    public Map<String, ArrayList<ASTNodes.Param>> functionTable;


    public SemanticAnalyzer(ASTNodes.StatementList statementList) throws SemanticAnalyzerException {
        this.statementList = statementList;
        this.symbolTable = new SymbolTable();
        this.functionTable = new HashMap<>();

        // add all the predefined functions
        ArrayList<ASTNodes.Param> p = new ArrayList<>();
        p.add(new ASTNodes.Param(new ASTNodes.Type("bool",false),"neg"));
        functionTable.put("not",p);
        symbolTable.add("not",new ASTNodes.Type("bool",false));

        p = new ArrayList<>();
        p.add(new ASTNodes.Param(new ASTNodes.Type("int",false),"value"));
        functionTable.put("chr",p);
        symbolTable.add("chr",new ASTNodes.Type("string",false));

        p = new ArrayList<>();
        p.add(new ASTNodes.Param(new ASTNodes.Type("real",false),"value"));
        functionTable.put("floor",p);
        symbolTable.add("floor",new ASTNodes.Type("int",false));

        p = new ArrayList<>();
        p.add(new ASTNodes.Param(new ASTNodes.Type("string",false),"value"));
        functionTable.put("len",p);
        symbolTable.add("len",new ASTNodes.Type("int",false));

        p = new ArrayList<>();
        functionTable.put("readInt",p);
        symbolTable.add("readInt",new ASTNodes.Type("int",false));

        p = new ArrayList<>();
        functionTable.put("readReal",p);
        symbolTable.add("readReal",new ASTNodes.Type("real",false));

        p = new ArrayList<>();
        functionTable.put("readString",p);
        symbolTable.add("readString",new ASTNodes.Type("string",false));

        p = new ArrayList<>();
        p.add(new ASTNodes.Param(new ASTNodes.Type("int",false),"value"));
        functionTable.put("writeInt",p);
        symbolTable.add("writeInt",new ASTNodes.Type("bool",false));

        p = new ArrayList<>();
        p.add(new ASTNodes.Param(new ASTNodes.Type("real",false),"value"));
        functionTable.put("writeReal",p);
        symbolTable.add("writeReal",new ASTNodes.Type("bool",false));

        p = new ArrayList<>();
        p.add(new ASTNodes.Param(new ASTNodes.Type("string",false),"value"));
        functionTable.put("write",p);
        symbolTable.add("write",new ASTNodes.Type("bool",false));

        p = new ArrayList<>();
        p.add(new ASTNodes.Param(new ASTNodes.Type("string",false),"value"));
        functionTable.put("writeln",p);
        symbolTable.add("writeln",new ASTNodes.Type("bool",false));

        // first scan to register all the functions and records
        // to allow forward references
        for (ASTNodes.Statement s : statementList.statements) {
            if (s instanceof ASTNodes.FunctionDef) {
                ASTNodes.FunctionDef f = (ASTNodes.FunctionDef) s;
                if (functionTable.containsKey(f.identifier)) {
                    throw new SemanticAnalyzerException("double definition of function : "+f.identifier);
                }
                symbolTable.add(f.identifier,f.returnType);
                functionTable.put(f.identifier,f.paramList);
            } else if (s instanceof ASTNodes.Record) {
                ASTNodes.Record record = (ASTNodes.Record) s;
                ASTNodes.Type type = new ASTNodes.Type(record.identifier,false);
                symbolTable.add(record.identifier, type);
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

        // second scan to register all the constants
        for (int i = 0; i < statementList.statements.size(); i++) {
            ASTNodes.Statement s = statementList.statements.get(i);
            if (s instanceof ASTNodes.ConstCreation) {
                ASTNodes.ConstCreation creation = (ASTNodes.ConstCreation) s;
                symbolTable.add(creation.identifier,creation.type);
                analyzeConstCreation(creation,symbolTable);
                //statementList.statements.remove(i);
                //i--;
                start++;
            } else break;
        }
    }

    public void analyze(ASTNodes.StatementList statementList, SymbolTable table, boolean firstRead) throws SemanticAnalyzerException {
        if (firstRead) {
            for (int i = start; i < statementList.statements.size(); i++) {
                analyzeStatement(statementList.statements.get(i), table);
            }
        } else {
            for (int i = 0; i < statementList.statements.size(); i++) {
                analyzeStatement(statementList.statements.get(i), table);
            }
        }
        /*
        for (ASTNodes.Statement s : statementList.statements) {
            analyzeStatement(s,table);
        }*/
    }

    public void analyzeStatement(ASTNodes.Statement s,SymbolTable table) throws SemanticAnalyzerException {
        if (s instanceof ASTNodes.FunctionDef) {
            ASTNodes.FunctionDef f = (ASTNodes.FunctionDef) s;
            analyzeFunctionDef(f,new SymbolTable(table));
        } else if (s instanceof ASTNodes.VarCreation) {
            ASTNodes.VarCreation creation = (ASTNodes.VarCreation) s;
            analyzeVarCreation(creation,table);
            table.add(creation.identifier, creation.type);
            table.addVar(creation.identifier);
        } else if (s instanceof ASTNodes.ValCreation) {
            ASTNodes.ValCreation creation = (ASTNodes.ValCreation) s;
            analyzeValCreation(creation,table);
            table.add(creation.identifier, creation.type);
        } else if (s instanceof ASTNodes.ConstCreation) {
            throw new SemanticAnalyzerException("const must be defined at the top of the file");
        } else if (s instanceof ASTNodes.VarAssign) {
            ASTNodes.VarAssign assign = (ASTNodes.VarAssign) s;
            analyzeVarAssign(assign,table);
        } else if (s instanceof ASTNodes.IfCond) {
            ASTNodes.IfCond cond = (ASTNodes.IfCond) s;
            analyzeIfCond(cond,table);
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
        if (!initType.equals(new ASTNodes.Type("int",false))) {
            throw new SemanticAnalyzerException("loop index should be a int but got : " + initType);
        }
        ASTNodes.Type endType = analyzeExpression(loop.endValExpr, table);
        if (!endType.equals(new ASTNodes.Type("int",false))) {
            throw new SemanticAnalyzerException("end index in forLoop should be a int nut got : " + endType);
        }
        ASTNodes.Type incType = analyzeExpression(loop.increment, table);
        if (!incType.equals(new ASTNodes.Type("int",false))) {
            throw new SemanticAnalyzerException("increment in forLoop should be a int but got : " + incType);
        }

        if (!(loop.loopVal instanceof ASTNodes.Identifier)) {
            throw new SemanticAnalyzerException("iteration variable should cannot come from an array or object");
        }
        if (!table.contain(((ASTNodes.Identifier) loop.loopVal).id)) {
            throw new SemanticAnalyzerException("unknown loop variable : " + ((ASTNodes.Identifier) loop.loopVal).id);
        }
        ASTNodes.Type loopValType = table.get(((ASTNodes.Identifier) loop.loopVal).id);
        if (!loopValType.equals(new ASTNodes.Type("int",false))) {
            throw new SemanticAnalyzerException("loop variable should be an int but got : " + table.get(((ASTNodes.Identifier) loop.loopVal).id));
        }
        analyze(loop.codeBlock,table,false);
    }
    public void analyzeWhileLoop(ASTNodes.WhileLoop loop, SymbolTable table) throws SemanticAnalyzerException {
        ASTNodes.Type type = analyzeExpression(loop.condition,table);
        if (type.type != "bool" || type.isArray) {
            throw new SemanticAnalyzerException("while condition should have a boolean condition but got : " + type);
        }
        analyze(loop.codeBlock,table,false);
    }

    public void analyzeIfCond(ASTNodes.IfCond cond, SymbolTable table) throws SemanticAnalyzerException {
        ASTNodes.Type type = analyzeExpression(cond.condition,table);
        if (type.type != "bool" || type.isArray) {
            throw new SemanticAnalyzerException("if condition should have a boolean condition but got : " + type);
        }
        analyze(cond.codeBlock,new SymbolTable(table),false);

        if (cond.elseCodeBlock != null) {
            analyze(cond.elseCodeBlock, new SymbolTable(table),false);
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
                if (!type.isArray && !type.equals(new ASTNodes.Type("string",false))) {
                    throw new SemanticAnalyzerException("wrong type for argument " + tParam.get(i).identifier + " in function call " + call.identifier + ", got : "+type);
                }
            } else {
                if (!tParam.get(i).type.equals(type)) {
                    throw new SemanticAnalyzerException("wrong type for argument " + tParam.get(i).identifier + " in function call " + call.identifier + ", got : "+type);
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
                stack.add((Integer) 0);
                cur = ((ASTNodes.ArrayAccess) cur).ref;
            } else if (cur instanceof ASTNodes.ObjectAccess) {
                stack.add(((ASTNodes.ObjectAccess) cur).accessIdentifier);
                cur = ((ASTNodes.ObjectAccess) cur).object;
            }
        }
        stack.add(((ASTNodes.Identifier) cur).id);

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
                // type is no more an array because 2D arrays not allowed
                type = new ASTNodes.Type(type.type,false);
            }
        }

        return type;
    }

    public void analyzeVarAssign(ASTNodes.VarAssign assign, SymbolTable table) throws SemanticAnalyzerException {

        if (assign.ref instanceof ASTNodes.Identifier) {
            ASTNodes.Identifier id = (ASTNodes.Identifier)assign.ref;
            if (table.containConstVal(id.id)) { // TODO
                if (table.getConstVal(id.id).equals("const")) {
                    throw new SemanticAnalyzerException("const cannot be modified");
                } else if (table.getConstVal(id.id).equals("val")) {
                    throw new SemanticAnalyzerException("val cannot be modified");
                } else if (table.getConstVal(id.id).equals("empty")) {
                    table.update(id.id,"val");
                }
            }
        }

        ASTNodes.Type exprType = analyzeExpression(assign.value,table);

        ASTNodes.Type type = analyzeRefToValue(assign.ref,table);

        if (!type.equals(exprType)) {
            throw new SemanticAnalyzerException("tried to assign " + exprType + " to a " + type + " variable");
        }

    }

    public void analyzeValCreation(ASTNodes.ValCreation creation,SymbolTable table) throws SemanticAnalyzerException {
        if (creation.type.type.equals("void")) {
            throw new SemanticAnalyzerException("Val type cannot be void");
        }
        if (creation.valExpr != null) {
            table.addVal(creation.identifier);
            ASTNodes.Type assignedType = analyzeExpression(creation.valExpr,table);
            if (!creation.type.equals(assignedType)) {
                throw new SemanticAnalyzerException("tried to assign " + assignedType + " to " + creation.type + " value " + creation.identifier);
            }
        } else {
            table.constValTable.put(creation.identifier, "empty");
        }
    }

    public void analyzeVarCreation(ASTNodes.VarCreation creation,SymbolTable table) throws SemanticAnalyzerException {
        if (creation.type.type.equals("void")) {
            throw new SemanticAnalyzerException("Var type cannot be void");
        }
        if (creation.varExpr != null) {
            ASTNodes.Type type = analyzeExpression(creation.varExpr,table);
            if (!creation.type.equals(type)) {
                throw new SemanticAnalyzerException("tried to assign " + type + " to " + creation.type + " variable " + creation.identifier);

            }
        }
    }

    public void analyzeConstCreation(ASTNodes.ConstCreation creation,SymbolTable table) throws SemanticAnalyzerException {
        if (creation.type.type.equals("void")) {
            throw new SemanticAnalyzerException("Const type cannot be void");
        }
        if (creation.initExpr == null) {
            throw new SemanticAnalyzerException("Const should be directly initialised");
        }
        if ((creation.type.type != "int" && creation.type.type != "real" &&
                creation.type.type != "string" && creation.type.type != "bool") || creation.type.isArray) {
            throw new SemanticAnalyzerException("Const type must be a base type but is : " + creation);

        }
        if (creation.initExpr != null) {
            table.addConst(creation.identifier);
            ASTNodes.Type type = analyzeExpression(creation.initExpr,table);
            if (!creation.type.equals(type)) {
                throw new SemanticAnalyzerException("tried to assign " + type + " to " + creation.type + " const " + creation.identifier);
            }
        }
    }

    public ASTNodes.Type analyzeFunctionDef(ASTNodes.FunctionDef functionDef, SymbolTable table) throws SemanticAnalyzerException {
        ASTNodes.Type type = functionDef.returnType;
        ASTNodes.StatementList block = functionDef.functionCode;
        if (type.equals(new ASTNodes.Type("void",false))) {
            if (block.statements.get(block.statements.size()-1) instanceof ASTNodes.ReturnExpr) {
                throw new SemanticAnalyzerException("void function " + functionDef.identifier +  "cannot return anything");
            }
            for (ASTNodes.Param p: functionDef.paramList) {
                table.add(p.identifier,p.type);
            }
            analyze(block,table,false);
            return type;
        } else {
            if (!(block.statements.get(block.statements.size()-1) instanceof ASTNodes.ReturnExpr)) {
                throw new SemanticAnalyzerException("non void function " + functionDef.identifier  + " must return");
            }
            for (ASTNodes.Param p: functionDef.paramList) {
                table.add(p.identifier,p.type);
            }
            analyze(block,table,false);
            ASTNodes.ReturnExpr last = (ASTNodes.ReturnExpr)block.statements.get(block.statements.size()-1);
            ASTNodes.Type actualReturnType = analyzeExpression(last.expr,table);
            if (!type.equals(actualReturnType)) {
                throw new SemanticAnalyzerException("function " + functionDef.identifier +  " returns wrong type : " + actualReturnType + " instead of " + type);
            }
            return type;
        }
    }


    public ASTNodes.Type analyzeMathExpr(ASTNodes.MathExpr expr, SymbolTable table) throws SemanticAnalyzerException {
        if (expr instanceof ASTNodes.NegateExpr) {
            ASTNodes.NegateExpr e = (ASTNodes.NegateExpr) expr;
            ASTNodes.Type type = analyzeExpression(e.expr, table);
            if ((type.type != "int" && type.type != "real") || type.isArray) {
                throw new SemanticAnalyzerException("tried to negate a " + type);
            }
            return analyzeExpression(e.expr, table);
        } else if (expr instanceof ASTNodes.AddExpr) {
            ASTNodes.AddExpr e = (ASTNodes.AddExpr) expr;
            ASTNodes.Type type1 = analyzeExpression(e.expr1,table);
            ASTNodes.Type type2 = analyzeExpression(e.expr2,table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types in addition should be the same : " + type1 + " and " + type2);
            }
            if ((type1.type != "int" && type1.type != "real") || type1.isArray) {
                throw new SemanticAnalyzerException("add operation not supported on : " + type1);
            }
            return type1;
        } else if (expr instanceof ASTNodes.SubExpr) {
            ASTNodes.SubExpr e = (ASTNodes.SubExpr) expr;
            ASTNodes.Type type1 = analyzeExpression(e.expr1,table);
            ASTNodes.Type type2 = analyzeExpression(e.expr2,table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types in subtraction should be the same : " + type1 + " and " + type2);
            }
            if ((type1.type != "int" && type1.type != "real") || type1.isArray) {
                throw new SemanticAnalyzerException("sub operation not supported on : " + type1);
            }
            return type1;
        } else if (expr instanceof ASTNodes.DivExpr) {
            ASTNodes.DivExpr e = (ASTNodes.DivExpr) expr;
            ASTNodes.Type type1 = analyzeExpression(e.expr1,table);
            ASTNodes.Type type2 = analyzeExpression(e.expr2,table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types in division should be the same : " + type1 + " and " + type2);
            }
            if ((type1.type != "int" && type1.type != "real") || type1.isArray) {
                throw new SemanticAnalyzerException("division operation not supported on : " + type1);
            }
            return type1;

        } else if (expr instanceof ASTNodes.MultExpr) {
            ASTNodes.MultExpr e = (ASTNodes.MultExpr) expr;
            ASTNodes.Type type1 = analyzeExpression(e.expr1,table);
            ASTNodes.Type type2 = analyzeExpression(e.expr2,table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types in multiplication should be the same : " + type1 + " and " + type2);
            }
            if ((type1.type != "int" && type1.type != "real") || type1.isArray) {
                throw new SemanticAnalyzerException("multiplication operation not supported on : " + type1);
            }
            return type1;

        } else if (expr instanceof ASTNodes.ModExpr) {
            ASTNodes.ModExpr e = (ASTNodes.ModExpr) expr;
            ASTNodes.Type type1 = analyzeExpression(e.expr1,table);
            ASTNodes.Type type2 = analyzeExpression(e.expr2,table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types in modulo should be the same : " + type1 + " and " + type2);
            }
            if (type1.type != "int" || type1.isArray) {
                throw new SemanticAnalyzerException("modulo operation not supported on : " + type1);
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
                throw new SemanticAnalyzerException("types should be the same in equality cmp : " + type1 + " and " + type2);
            }
            if ((type1.type != "int" && type1.type != "real" && type1.type != "bool" && type1.type != "string") || type1.isArray) {
                throw new SemanticAnalyzerException("type in eq comparison should be a base type but got : " + type1);
            }
            return new ASTNodes.Type("bool",false);
        } else if (cmp instanceof ASTNodes.NotEqComp) {
            ASTNodes.NotEqComp c = (ASTNodes.NotEqComp) cmp;
            ASTNodes.Type type1 = analyzeExpression(c.expr1, table);
            ASTNodes.Type type2 = analyzeExpression(c.expr2, table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types should be the same in inequality cmp : " + type1 + " and " + type2);
            }
            if ((type1.type != "int" && type1.type != "real" && type1.type != "bool" && type1.type != "string") || type1.isArray) {
                throw new SemanticAnalyzerException("type in ineq comparison should be a base type but got : " + type1);
            }
            return new ASTNodes.Type("bool",false);
        } else if (cmp instanceof ASTNodes.GrEqComp) {
            ASTNodes.GrEqComp c = (ASTNodes.GrEqComp) cmp;
            ASTNodes.Type type1 = analyzeExpression(c.expr1, table);
            ASTNodes.Type type2 = analyzeExpression(c.expr2, table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types should be the same in greater or eq cmp : " + type1 + " and " + type2);
            }
            if ((type1.type != "int" && type1.type != "real") || type1.isArray) {
                throw new SemanticAnalyzerException("type in greater or eq comparison should be int or real but got : " + type1);
            }
            return new ASTNodes.Type("bool",false);
        } else if (cmp instanceof ASTNodes.SmEqComp) {
            ASTNodes.SmEqComp c = (ASTNodes.SmEqComp) cmp;
            ASTNodes.Type type1 = analyzeExpression(c.expr1, table);
            ASTNodes.Type type2 = analyzeExpression(c.expr2, table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types should be the same in smaller or eq cmp : " + type1 + " and " + type2);
            }
            if ((type1.type != "int" && type1.type != "real") || type1.isArray) {
                throw new SemanticAnalyzerException("type in smaller or eq comparison should be int or real but got : " + type1);
            }
            return new ASTNodes.Type("bool",false);
        } else if (cmp instanceof ASTNodes.GrComp) {
            ASTNodes.GrComp c = (ASTNodes.GrComp) cmp;
            ASTNodes.Type type1 = analyzeExpression(c.expr1, table);
            ASTNodes.Type type2 = analyzeExpression(c.expr2, table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types should be the same in strict greater cmp : " + type1 + " and " + type2);
            }
            if ((type1.type != "int" && type1.type != "real" && type1.type != "bool" && type1.type != "string") || type1.isArray) {
                throw new SemanticAnalyzerException("type in strict greater comparison should be a base type but got : " + type1);
            }
            return new ASTNodes.Type("bool",false);

        } else if (cmp instanceof ASTNodes.SmComp) {
            ASTNodes.SmComp c = (ASTNodes.SmComp) cmp;
            ASTNodes.Type type1 = analyzeExpression(c.expr1, table);
            ASTNodes.Type type2 = analyzeExpression(c.expr2, table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types should be the same in strict smaller cmp : " + type1 + " and " + type2);
            }
            if ((type1.type != "int" && type1.type != "real" && type1.type != "bool" && type1.type != "string") || type1.isArray) {
                throw new SemanticAnalyzerException("type in strict smaller comparison should be a base type but got : " + type1);
            }
            return new ASTNodes.Type("bool",false);

        } else if (cmp instanceof ASTNodes.AndComp) {
            ASTNodes.AndComp c = (ASTNodes.AndComp) cmp;
            ASTNodes.Type type1 = analyzeExpression(c.expr1, table);
            ASTNodes.Type type2 = analyzeExpression(c.expr2, table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types should be the same in and operator : " + type1 + " and " + type2);
            }
            if (type1.type != "bool"|| type1.isArray) {
                throw new SemanticAnalyzerException("type in and operator should be a bool but got : " + type1);
            }
            return new ASTNodes.Type("bool",false);
        } else if (cmp instanceof ASTNodes.OrComp) {
            ASTNodes.OrComp c = (ASTNodes.OrComp) cmp;
            ASTNodes.Type type1 = analyzeExpression(c.expr1, table);
            ASTNodes.Type type2 = analyzeExpression(c.expr2, table);
            if (!type1.equals(type2)) {
                throw new SemanticAnalyzerException("types should be the same in or operator : " + type1 + " and " + type2);
            }
            if (type1.type != "bool" || type1.isArray) {
                throw new SemanticAnalyzerException("type in or operator should be a bool but got : " + type1);
            }
            return new ASTNodes.Type("bool",false);
        } else {
            throw new SemanticAnalyzerException("unknown comparison");
        }
    }

    public ASTNodes.Type analyzeExpression(ASTNodes.Expression expr, SymbolTable table) throws SemanticAnalyzerException {
        if (expr instanceof ASTNodes.DirectValue) {
            return expr.exprType = ((ASTNodes.DirectValue) expr).type;
        } else if (expr instanceof ASTNodes.FunctionCall) {
            ASTNodes.FunctionCall call = (ASTNodes.FunctionCall) expr;
            return expr.exprType =  analyzeFunctionCall(call,table);
        } else if (expr instanceof ASTNodes.ArrayCreation) {
            ASTNodes.ArrayCreation creation = (ASTNodes.ArrayCreation) expr;
            if (creation.typeIdentifier.equals("void")) {
                throw new SemanticAnalyzerException("void type not allowed for array");
            }

            ASTNodes.Type type = analyzeExpression(creation.arraySize,table);
            if (type.type != "int") {
                throw new SemanticAnalyzerException("array size should be int but got " + type);
            }

            ASTNodes.Type actualType = new ASTNodes.Type(creation.typeIdentifier, true);
            return expr.exprType =  actualType;
        } else if (expr instanceof ASTNodes.MathExpr) {
            return expr.exprType =  analyzeMathExpr((ASTNodes.MathExpr) expr,table);
        } else if (expr instanceof ASTNodes.Comparison) {
            return expr.exprType =  analyzeComparison((ASTNodes.Comparison) expr, table);
        } else if (expr instanceof ASTNodes.RefToValue) {
            ASTNodes.RefToValue ref = (ASTNodes.RefToValue) expr;
            return expr.exprType =  analyzeRefToValue(ref,table);
        }

        throw new SemanticAnalyzerException("unknown thing in analyzeExpression");
    }
}
