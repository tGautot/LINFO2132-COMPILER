package compiler.parser;

import compiler.Lexer.*;

import java.rmi.UnexpectedException;
import java.util.ArrayList;

public class Parser {
    Symbol nxtToken; Symbol lookAhead;
    Lexer lexer;

    public Parser(Lexer lexer){
        this.lexer = lexer;
        readSymbol(); // Place first token in lookahead
        readSymbol();
    }

    private void readSymbol(){
        nxtToken = lookAhead;
        lookAhead = lexer.getNextSymbol();
    }
    public ASTNodes.StatementList parseCode() throws ParserException {
        System.out.println("Parsing code");
        ASTNodes.StatementList sl = new ASTNodes.StatementList();
        sl.statements = new ArrayList<>();
        while(true){
            ASTNodes.Statement s = parseStatement();
            if(s == null){
                return sl;
            }
            sl.statements.add(s);
        }
    }

    public ASTNodes.Statement parseStatement() throws ParserException {
        while (nxtToken == SymbolToken.SEMICOLON){
            readSymbol();
        }
        System.out.println("Parsing statement " + nxtToken);
        if(nxtToken == KeywordToken.VARIABLE){
            return parseVarCreation();
        }
        else if(nxtToken == KeywordToken.VALUE){
            return parseValCreation();
        }
        else if(nxtToken == KeywordToken.CONST){
            return parseConstCreation();
        }
        else if(nxtToken instanceof IdentifierToken){
            // Either VarAssign or FunctionCall
            if(lookAhead == SymbolToken.OPEN_PARENTHESIS){
                // FunctionCall
                return parseFunctionCall();
            }
            else{
                return parseVarAssign();
            }

        }
        else if(nxtToken == KeywordToken.FUNCTION){
            // FunctionDef
            return parseFunctionDef();
        }
        else if(nxtToken == KeywordToken.FOR_LOOP){
            // For loop
            return parseForLoop();
        }
        else if(nxtToken == KeywordToken.WHILE){
            // While loop
            return parseWhileLoop();
        }
        else if(nxtToken == KeywordToken.RECORD){
            // Record
            return parseRecord();
        }
        else if(nxtToken == KeywordToken.RETURN){
            return parseReturn();
        }
        else if(nxtToken == KeywordToken.IF){
            return parseIfCond();
        }
        else if(nxtToken == KeywordToken.DELETE){
            return parseDelete();
        }
        return null;
        //throw new ParserException("Cannot begin statement with " + nxtToken.toString());
    }

    private ASTNodes.DeleteStt parseDelete() throws ParserException {
        readSymbol();
        ASTNodes.DeleteStt node = new ASTNodes.DeleteStt();
        node.toDelete = parseRefToValue();
        return node;
    }

    private ASTNodes.ConstCreation parseConstCreation() throws ParserException {

        ASTNodes.ConstCreation node = new ASTNodes.ConstCreation();
        node.initExpr = null;
        readSymbol();
        if(!(nxtToken instanceof IdentifierToken)){
            throw new ParserException("Expected identifier after keyword var, but got " + nxtToken.toString());
        }
        node.identifier = ((IdentifierToken) nxtToken).label;
        readSymbol();
        node.type = parseType();

        if (nxtToken == OperatorToken.ASSIGN) {
            readSymbol();
            node.initExpr = parseExpression();
        } else {
            throw new ParserException("Const value needs to be initialized, but got " + nxtToken);
        }

        return node;
    }

    private ASTNodes.ValCreation parseValCreation() throws ParserException {
        // ValCreation and VarCreation have the exact same parsing mechanism, so just re-use it
        ASTNodes.ValCreation node = new ASTNodes.ValCreation();
        ASTNodes.VarCreation sub = parseVarCreation();
        node.identifier = sub.identifier;
        node.type = sub.type;
        node.valExpr = sub.varExpr;
        return node;
    }

    public ASTNodes.VarCreation parseVarCreation() throws ParserException {
        System.out.println("Parsing VarCreation");

        // When in here, var token is already in nxtSymbol
        ASTNodes.VarCreation varCreationNode = new ASTNodes.VarCreation();
        varCreationNode.varExpr = null;
        readSymbol();
        if(!(nxtToken instanceof IdentifierToken)){
            throw new ParserException("Expected identifier after keyword var, but got " + nxtToken.toString());
        }
        varCreationNode.identifier = ((IdentifierToken) nxtToken).label;
        readSymbol();
        varCreationNode.type = parseType();

        if(nxtToken == SymbolToken.SEMICOLON){
            // End of var creation
            readSymbol();
        } else if (nxtToken == OperatorToken.ASSIGN) {
            readSymbol();
            varCreationNode.varExpr = parseExpression();
        }

        return varCreationNode;
    }

    public ASTNodes.Type parseType() throws ParserException {
        System.out.println("Parsing Type");

        if(!(nxtToken instanceof TypeToken || nxtToken instanceof IdentifierToken)){
            throw new ParserException("Expected type, but got " + nxtToken.toString());
        }
        ASTNodes.Type type = new ASTNodes.Type();

        if(nxtToken instanceof TypeToken)
            type.type = ((TypeToken) nxtToken).label;
        if(nxtToken instanceof IdentifierToken)
            type.type = ((IdentifierToken) nxtToken).label;

        if(lookAhead == SymbolToken.OPEN_BRACKET){
            type.isArray = true;
            readSymbol();
            if(lookAhead != SymbolToken.CLOSE_BRACKET){
                throw new ParserException("Expected closing bracket ( ] ) after type[, but got " + lookAhead.toString());
            }
            readSymbol();

        }
        readSymbol();
        return type;
    }

    public ASTNodes.FunctionCall parseFunctionCall() throws ParserException {
        System.out.println("Parsing FunctionCall");

        // Should currently have function identifier in nxtSymbol
        ASTNodes.FunctionCall node = new ASTNodes.FunctionCall();
        node.identifier = ( (IdentifierToken) nxtToken).label;
        readSymbol(); readSymbol();
        node.paramVals = parseParamVals();
        return node;
    }

    public ArrayList<ASTNodes.Expression> parseParamVals() throws ParserException {
        System.out.println("Parsing ParamVals");

        ArrayList<ASTNodes.Expression> vals = new ArrayList<>();
        if(nxtToken == SymbolToken.CLOSE_PARENTHESIS){
            readSymbol();
            return vals;
        }
        while(true){
            vals.add(parseExpression());
            if(nxtToken == SymbolToken.CLOSE_PARENTHESIS){
                readSymbol();
                break;
            }
            if(nxtToken != SymbolToken.COMMA){
                throw new ParserException("Exprected comma to delimit parameters in function call (or parenthesis " +
                        "to close it) but got " + nxtToken.toString());
            }
            readSymbol();
        }
        return vals;
    }

    public ASTNodes.FunctionDef parseFunctionDef() throws ParserException {
        System.out.println("Parsing FunctionDef");

        // Keyword proc in nxtToken
        ASTNodes.FunctionDef node = new ASTNodes.FunctionDef();
        readSymbol();
        if( !(nxtToken instanceof IdentifierToken) ){
            throw new ParserException("Expected identifier after keyword proc but got " + nxtToken.toString());
        }
        node.identifier = ((IdentifierToken) nxtToken).label;
        readSymbol();
        if(nxtToken != SymbolToken.OPEN_PARENTHESIS){
            throw new ParserException("Expected `(` in function definition after name but got " + nxtToken.toString());
        }
        readSymbol();
        node.paramList = parseParamList();
        node.returnType = parseType();
        if(nxtToken != SymbolToken.OPEN_CB){
            throw new ParserException("Expected `{` after function prototype but got " + nxtToken.toString());
        }
        readSymbol();
        node.functionCode = parseCode();
        if(nxtToken != SymbolToken.CLOSE_CB){
            throw new ParserException("Expected `}` after function code but got " + nxtToken.toString());
        }
        readSymbol();
        return node;
    }

    public ArrayList<ASTNodes.Param> parseParamList() throws ParserException {
        System.out.println("Parsing ParamList");

        ArrayList<ASTNodes.Param> params = new ArrayList<>();
        if(nxtToken == SymbolToken.CLOSE_PARENTHESIS){
            readSymbol();
            return params;
        }
        while(true){
            ASTNodes.Param p = new ASTNodes.Param();
            if( !(nxtToken instanceof IdentifierToken)){
                throw new ParserException("Expected name for function param but got" + nxtToken.toString());
            }
            p.identifier = ((IdentifierToken) nxtToken).label;
            readSymbol();
            p.type = parseType();
            params.add(p);
            if(nxtToken == SymbolToken.CLOSE_PARENTHESIS){
                readSymbol();
                break;
            }
            if(nxtToken != SymbolToken.COMMA){
                throw new ParserException("Exprected comma to delimit parameters in function def (or parenthesis " +
                        "to close it) but got " + nxtToken.toString());
            }
            readSymbol();
        }
        return params;
    }

    private ASTNodes.WhileLoop parseWhileLoop() throws ParserException {
        // while token in nxtToken
        ASTNodes.WhileLoop node = new ASTNodes.WhileLoop();
        readSymbol();
        node.condition = parseExpression();
        if(nxtToken != SymbolToken.OPEN_CB){
            throw new ParserException("Expected { after while loop condition but got " + nxtToken);
        }
        readSymbol();
        node.codeBlock = parseCode();
        if(nxtToken != SymbolToken.CLOSE_CB){
            throw new ParserException("Expected } after while loop codeblock but got " + nxtToken);
        }
        return node;
    }

    public ASTNodes.ForLoop parseForLoop() throws ParserException {
        System.out.println("Parsing for loop");
        // nxt symbol should be `for`
        ASTNodes.ForLoop node = new ASTNodes.ForLoop();
        readSymbol();
        if(!(nxtToken instanceof IdentifierToken)){
            throw new ParserException("For loop expects a variable");
        }
        node.loopVarIdentifier = ((IdentifierToken) nxtToken).label;
        readSymbol();
        if(nxtToken != OperatorToken.ASSIGN){
            throw new ParserException("Unexpected token in init of for loop: " + nxtToken);
        }
        readSymbol();
        node.initValExpr = parseExpression();
        if(nxtToken != KeywordToken.TO){
            throw new ParserException("Expected keyword `to` in for loop but got " + nxtToken);
        }
        readSymbol();
        node.endValExpr = parseExpression();
        if(nxtToken != KeywordToken.BY){
            throw new ParserException("Expected keyword `by` in for loop but got " + nxtToken);
        }
        readSymbol();
        node.increment = parseExpression();

        if(nxtToken != SymbolToken.OPEN_CB){
            throw new ParserException("Expected symbol `{` after for loop to start code block but got " + nxtToken);
        }
        readSymbol();
        node.codeBlock = parseCode();

        if(nxtToken != SymbolToken.CLOSE_CB){
            throw new ParserException("Expected symbol `}` to finish codeblock of for loop but got " + nxtToken);
        }
        readSymbol();
        return node;
    }

    public ASTNodes.Record parseRecord() throws ParserException {
        System.out.println("Parsing record");

        ASTNodes.Record node = new ASTNodes.Record();

        readSymbol();
        if(!(nxtToken instanceof IdentifierToken)){
            throw new ParserException("Expected name of record but got " + nxtToken);
        }
        node.identifier = ((IdentifierToken) nxtToken).label;
        readSymbol();
        if(nxtToken != SymbolToken.OPEN_CB){
            throw new ParserException("Expected symbol `{` after record but got " + nxtToken);
        }
        readSymbol();
        node.recordVars = parseRecordVars();
        if(nxtToken != SymbolToken.CLOSE_CB){
            throw new ParserException("Expected `}` after end of record");
        }
        readSymbol();
        return node;
    }

    public ArrayList<ASTNodes.RecordVar> parseRecordVars() throws ParserException {
        System.out.println("Parsing recordvars");
        ArrayList<ASTNodes.RecordVar> vars = new ArrayList<>();
        while(true){
            while(nxtToken == SymbolToken.SEMICOLON) {
                readSymbol();
            }
            if(!(nxtToken instanceof IdentifierToken)){
                break;
            }
            ASTNodes.RecordVar var = new ASTNodes.RecordVar();
            var.identifier = ((IdentifierToken) nxtToken).label;
            readSymbol();
            var.type = parseType();
            /*if(nxtToken != SymbolToken.SEMICOLON){
                throw new ParserException("Expected `;` after record parameter definition, but got " + nxtToken);
            }
            readSymbol();*/
            vars.add(var);
        }
        return vars;
    }

    public ASTNodes.ReturnExpr parseReturn() throws ParserException {
        System.out.println("Parsing return statement");
        readSymbol();
        ASTNodes.ReturnExpr node = new ASTNodes.ReturnExpr();
        node.expr = parseExpression();
        /*if(nxtToken != SymbolToken.SEMICOLON){
            throw new ParserException("Expected `;` after return statement but got " + nxtToken);
        }*/
        return node;
    }

    public ASTNodes.IfCond parseIfCond() throws ParserException {
        System.out.println("Parsing IfCond");
        // If in nxtToken
        readSymbol();
        if(nxtToken != SymbolToken.OPEN_PARENTHESIS){
            throw new ParserException("Expected `(` after `if` keyword but got " + nxtToken);
        }
        readSymbol();
        ASTNodes.IfCond node = new ASTNodes.IfCond();
        node.condition = parseExpression();
        if(nxtToken != SymbolToken.CLOSE_PARENTHESIS){
            throw new ParserException("Expected `)` after end of condition in `if` but got " + nxtToken);
        }

        readSymbol();
        if(nxtToken != SymbolToken.OPEN_CB){
            throw new ParserException("Expected `{` to open codeblock after `if` but got " + nxtToken);
        }
        readSymbol();
        node.codeBlock = parseCode();
        if(nxtToken != SymbolToken.CLOSE_CB){
            throw new ParserException("Expected `}` to close codeblock after `if` but got " + nxtToken);
        }
        readSymbol();
        if(nxtToken != KeywordToken.ELSE){
            return node;
        }

        readSymbol();
        if(nxtToken != SymbolToken.OPEN_CB){
            throw new ParserException("Expected `{` to open codeblock after `else` but got " + nxtToken);
        }
        readSymbol();
        node.elseCodeBlock = parseCode();
        if(nxtToken != SymbolToken.CLOSE_CB){
            throw new ParserException("Expected `}` to close codeblock after `else` but got " + nxtToken);
        }
        return node;
    }

    public ASTNodes.VarAssign parseVarAssign() throws ParserException {
        ASTNodes.VarAssign node = null;
        if(lookAhead == OperatorToken.ASSIGN){
            node = new ASTNodes.DirectVarAssign();
            ((ASTNodes.DirectVarAssign) node).identifier = ((IdentifierToken) nxtToken).label;
            readSymbol();
            readSymbol();
            node.value = parseExpression();
        }
        else{
            node = new ASTNodes.RefVarAssign();
            ((ASTNodes.RefVarAssign) node).ref = parseRefToValue();
            readSymbol();
            node.value = parseExpression();
        }

        /*if(nxtToken != SymbolToken.SEMICOLON){
            throw new ParserException("Expected semicolon after var assign but got " + nxtToken);
        }
        readSymbol();*/


        return node;
    }

    public ASTNodes.RefToValue parseRefToValue() throws ParserException {

        ArrayList<ASTNodes.RefToValue> q = new ArrayList<>();

        if( !(nxtToken instanceof IdentifierToken)){
            throw new ParserException("Expected identifier but got " + nxtToken);
        }

        IdentifierToken initToken = (IdentifierToken) nxtToken;

        while(lookAhead == SymbolToken.DOT || lookAhead == SymbolToken.OPEN_BRACKET){
            //System.out.println(nxtToken);
            ASTNodes.RefToValue node = null;
            if(lookAhead == SymbolToken.DOT){
                node = new ASTNodes.ObjectAccessFromRef();
                ((ASTNodes.ObjectAccessFromRef) node).object = null;
                readSymbol(); readSymbol();
                ((ASTNodes.ObjectAccessFromRef) node).accessIdentifier = ((IdentifierToken) nxtToken).label;

            }
            else{
                node = new ASTNodes.ArrayAccessFromRef();
                if( !(nxtToken instanceof IdentifierToken)){
                    throw new ParserException("Expected identifier before array access but got " + nxtToken);
                }
                ((ASTNodes.ArrayAccessFromRef) node).ref =  null;
                readSymbol();
                readSymbol();
                ((ASTNodes.ArrayAccessFromRef) node).arrayIndex = parseExpression();
            }
            q.add(node);
        }
        readSymbol(); // Consume last symbol;

        ASTNodes.RefToValue source ;
        if(q.get(0) instanceof ASTNodes.ObjectAccessFromRef){
            source = new ASTNodes.ObjectAccessFromId();
            ((ASTNodes.ObjectAccessFromId) source).identifier = initToken.label;
            ((ASTNodes.ObjectAccessFromId) source).accessIdentifier = ((ASTNodes.ObjectAccessFromRef) q.get(0)).accessIdentifier;
        }
        else {
            source = new ASTNodes.ArrayAccessFromId();
            ((ASTNodes.ArrayAccessFromId) source).arrayId = initToken.label;
            ((ASTNodes.ArrayAccessFromId) source).arrayIndex = ((ASTNodes.ArrayAccessFromRef) q.get(0)).arrayIndex;

        }
        q.set(0, source);

        for(int i = 1; i < q.size(); i++){
            ASTNodes.RefToValue r2v = q.get(i);
            if(r2v instanceof ASTNodes.ObjectAccessFromRef){
                ((ASTNodes.ObjectAccessFromRef) r2v).object = q.get(i-1);
            }
            if(r2v instanceof ASTNodes.ArrayAccessFromRef){
                ((ASTNodes.ArrayAccessFromRef) r2v).ref = q.get(i-1);
            }
        }
        return q.get(q.size()-1);
    }

    public ASTNodes.Expression parseExpression() throws ParserException {
        System.out.println("Parsing Expression " + nxtToken);

        ASTNodes.Expression curr = parseComp();

        while(true){
            ASTNodes.Expression nxt;
            if(nxtToken == KeywordToken.AND){
                readSymbol();
                nxt = new ASTNodes.AndComp();
                ((ASTNodes.AndComp) nxt).expr1 = curr;
                ((ASTNodes.AndComp) nxt).expr2 = parseComp();
            } else if(nxtToken == KeywordToken.OR){
                readSymbol();
                nxt = new ASTNodes.OrComp();
                ((ASTNodes.OrComp) nxt).expr1 = curr;
                ((ASTNodes.OrComp) nxt).expr2 = parseComp();
            } else {
                return curr;
            }
            curr = nxt;
        }
    }

    public ASTNodes.Expression parseMultMod() throws ParserException {
        System.out.println("Parsing MultMod " + nxtToken);
        // Parsing function for  highest precedence math operators : * / %
        ASTNodes.Expression curr = parseTerm();
        while(true){
            ASTNodes.Expression nxt;
            if(nxtToken == OperatorToken.TIMES){
                readSymbol();
                nxt = new ASTNodes.MultExpr();
                ((ASTNodes.MultExpr) nxt).expr1 = curr;
                ((ASTNodes.MultExpr) nxt).expr2 = parseTerm();
            } else if(nxtToken == OperatorToken.DIVIDE){
                readSymbol();
                nxt = new ASTNodes.DivExpr();
                ((ASTNodes.DivExpr) nxt).expr1 = curr;
                ((ASTNodes.DivExpr) nxt).expr2 = parseTerm();
            } else if (nxtToken == OperatorToken.MODULUS) {
                readSymbol();
                nxt = new ASTNodes.ModExpr();
                ((ASTNodes.ModExpr) nxt).expr1 = curr;
                ((ASTNodes.ModExpr) nxt).expr2 = parseTerm();
            } else {
                return curr;
            }
            curr = nxt;
        }
    }

    public ASTNodes.Expression parseAddSub() throws ParserException {
        System.out.println("Parsing AddSub " + nxtToken);
        ASTNodes.Expression curr = parseMultMod();

        while(true){
            ASTNodes.Expression nxt;
            if(nxtToken == OperatorToken.PLUS){
                readSymbol();
                nxt = new ASTNodes.AddExpr();
                ((ASTNodes.AddExpr) nxt).expr1 = curr;
                ((ASTNodes.AddExpr) nxt).expr2 = parseMultMod();
            } else if(nxtToken == OperatorToken.MINUS){
                readSymbol();
                nxt = new ASTNodes.SubExpr();
                ((ASTNodes.SubExpr) nxt).expr1 = curr;
                ((ASTNodes.SubExpr) nxt).expr2 = parseMultMod();
            } else {
                return curr;
            }
            curr = nxt;
        }
    }

    public ASTNodes.Expression parseComp() throws ParserException{
        System.out.println("Parsing Comp " + nxtToken);
        ASTNodes.Expression curr = parseAddSub();

        while(true){
            ASTNodes.Expression nxt;
            if(nxtToken == OperatorToken.EQUALS){
                readSymbol();
                nxt = new ASTNodes.EqComp();
                ((ASTNodes.EqComp) nxt).expr1 = curr;
                ((ASTNodes.EqComp) nxt).expr2 = parseAddSub();
            } else if(nxtToken == OperatorToken.GRTR_OR_EQUAL){
                readSymbol();
                nxt = new ASTNodes.GrEqComp();
                ((ASTNodes.GrEqComp) nxt).expr1 = curr;
                ((ASTNodes.GrEqComp) nxt).expr2 = parseAddSub();
            } else if(nxtToken == OperatorToken.SMLR_OR_EQUAL){
                readSymbol();
                nxt = new ASTNodes.SmEqComp();
                ((ASTNodes.SmEqComp) nxt).expr1 = curr;
                ((ASTNodes.SmEqComp) nxt).expr2 = parseAddSub();
            }else if(nxtToken == OperatorToken.GREATER){
                readSymbol();
                nxt = new ASTNodes.GrComp();
                ((ASTNodes.GrComp) nxt).expr1 = curr;
                ((ASTNodes.GrComp) nxt).expr2 = parseAddSub();
            }else if(nxtToken == OperatorToken.SMALLER){
                readSymbol();
                nxt = new ASTNodes.SmComp();
                ((ASTNodes.SmComp) nxt).expr1 = curr;
                ((ASTNodes.SmComp) nxt).expr2 = parseAddSub();
            }else if(nxtToken == OperatorToken.DIFFERENT){
                readSymbol();
                nxt = new ASTNodes.NotEqComp();
                ((ASTNodes.NotEqComp) nxt).expr1 = curr;
                ((ASTNodes.NotEqComp) nxt).expr2 = parseAddSub();
            } else {
                return curr;
            }
            curr = nxt;
        }
    }

    public ASTNodes.Expression parseTerm() throws ParserException {
        System.out.println("Parsing Term " + nxtToken);
        //
        if(nxtToken instanceof IdentifierToken){
            if(lookAhead == SymbolToken.DOT || lookAhead == SymbolToken.OPEN_BRACKET){
                return parseRefToValue();
            } else if (lookAhead == SymbolToken.OPEN_PARENTHESIS) {
                return parseFunctionCall();
            } else {
                ASTNodes.Identifier node = new ASTNodes.Identifier();
                node.id = ((IdentifierToken) nxtToken).label;
                readSymbol();
                return node;
            }
        } else if(nxtToken instanceof ValueToken){
            ASTNodes.DirectValue node = new ASTNodes.DirectValue();
            node.value = ((ValueToken) nxtToken).value;
            node.type = new ASTNodes.Type();
            node.type.isArray = false;
            switch (((ValueToken) nxtToken).valType) {
                case INT:
                    node.type.type = "int";
                    break;
                case BOOL:
                    node.type.type = "bool";
                    break;
                case REAL:
                    node.type.type = "real";
                    break;
                case STRING:
                    node.type.type = "string";
                    break;
            }
            readSymbol();
            return node;
        } else if (nxtToken instanceof TypeToken) {
            // Array Creation
            return parseArrayCreation();

        } else if (nxtToken == OperatorToken.MINUS) {
            ASTNodes.NegateExpr node = new ASTNodes.NegateExpr();
            readSymbol();
            node.expr = parseTerm();
            return node;
        } else if (nxtToken == SymbolToken.OPEN_PARENTHESIS){
            readSymbol();
            ASTNodes.Expression expr = parseExpression();
            if(nxtToken != SymbolToken.CLOSE_PARENTHESIS){
                throw new ParserException("Missing closing parenthesis in expression");
            }
            readSymbol();
            return expr;
        }
        throw new ParserException("Couldn't parse expression with term starting with " + nxtToken);
    }

    private ASTNodes.Expression parseArrayCreation() throws ParserException {
        // Should have TypeToken in nxtSymbol
        ASTNodes.ArrayCreation node = new ASTNodes.ArrayCreation();
        node.typeIdentifier = ((TypeToken) nxtToken).label;
        readSymbol();
        if(nxtToken != SymbolToken.OPEN_BRACKET){
            throw new ParserException("Expected [ after type for array creation in expression but got " + nxtToken);
        }
        readSymbol();
        if(nxtToken != SymbolToken.CLOSE_BRACKET){
            throw new ParserException("Expected ] after type for array creation in expression but got " + nxtToken);
        }
        readSymbol();
        if(nxtToken != SymbolToken.OPEN_PARENTHESIS){
            throw new ParserException("Expected ( after type[] for array creation in expression but got " + nxtToken);
        }
        readSymbol();
        node.arraySize = parseExpression();

        if(nxtToken != SymbolToken.CLOSE_PARENTHESIS){
            throw new ParserException("Expected ) after size for array creation in expression but got " + nxtToken);
        }
        readSymbol();
        return node;
    }


}
