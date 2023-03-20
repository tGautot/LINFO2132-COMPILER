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
            if(s == null) return sl;
            sl.statements.add(s);
        }
    }

    public ASTNodes.Statement parseStatement() throws ParserException {
        System.out.println("Parsing statement " + nxtToken);
        if(nxtToken == KeywordToken.VARIABLE){
            return parseVarCreation();
        }
        if(nxtToken instanceof IdentifierToken){
            // Either VarAssign or FunctionCall
            if(lookAhead == SymbolToken.OPEN_PARENTHESIS){
                // FunctionCall
                return parseFunctionCall();
            }
            // TODO VarAssign
        }
        if(nxtToken == KeywordToken.FUNCTION){
            // FunctionDef
            return parseFunctionDef();
        }
        if(nxtToken == KeywordToken.FOR_LOOP){
            // For loop
            return parseForLoop();
        }
        if(nxtToken == KeywordToken.WHILE){
            // While loop
        }
        if(nxtToken == KeywordToken.RECORD){
            // Record
            return parseRecord();
        }
        if(nxtToken == KeywordToken.RETURN){

        }
        if(nxtToken == KeywordToken.IF){

        }
        return null;
        //throw new ParserException("Cannot begin statement with " + nxtToken.toString());
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
        readSymbol();

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
        if(nxtToken != SymbolToken.OPEN_CB){
            throw new ParserException("Expected `}` after function prototype but got " + nxtToken.toString());
        }
        readSymbol();
        node.functionCode = parseCode();
        return node;
    }

    public ArrayList<ASTNodes.Param> parseParamList() throws ParserException {
        System.out.println("Parsing ParamList");

        ArrayList<ASTNodes.Param> params = new ArrayList<>();
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
        readSymbol();
        if(nxtToken != SymbolToken.OPEN_CB){
            throw new ParserException("Expected symbol `{` after for loop to start code block but got " + nxtToken);
        }
        readSymbol();
        node.codeBlock = parseCode();
        readSymbol();
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
            if(!(nxtToken instanceof IdentifierToken)){
                break;
            }
            ASTNodes.RecordVar var = new ASTNodes.RecordVar();
            var.identifier = ((IdentifierToken) nxtToken).label;
            readSymbol();
            var.type = parseType();
            if(nxtToken != SymbolToken.SEMICOLON){
                throw new ParserException("Expected `;` after record parameter definition, but got " + nxtToken);
            }
            readSymbol();
            vars.add(var);
        }
        return vars;
    }

    public ASTNodes.Expression parseExpression() throws ParserException {
        System.out.println("Parsing Expression");
        // TODO
        if(!(nxtToken instanceof ValueToken)){
            throw new ParserException("The only allowed expressions at the moment are direct values");
        }
        ASTNodes.DirectValue node = new ASTNodes.DirectValue();
        node.value= ((ValueToken) nxtToken).value;
        node.type = new ASTNodes.Type();
        node.type.isArray = false;
        switch(((ValueToken) nxtToken).valType){
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
    }
}
