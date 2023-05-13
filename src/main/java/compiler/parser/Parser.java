package compiler.parser;

import compiler.Lexer.*;
import compiler.Logger.Logger;

import java.rmi.UnexpectedException;
import java.util.ArrayList;


/**
 * Parser class
 *
 * Constructor takes Lexer as input, which will feed the tokens to the parser
 *
 * To get a full AST (represented by a StatementList object), simply call the parseCode function
 *
 * All parsing functions are made public to facilitate testing and potentially create special behaviour but none other
 * than the parseCode should be needed to parse a source file.
 *
 * As a rule of thumb, when reading this code, keep in mind that all the parsing functions expect that when they are
 * called, nxtToken still holds the first token of the pattern they are meant to match
 * For example: parseVarCreation expects nxtToken to hold the token for the keyword VAR
 *              parseForLoop ------------------------------------------------------ FOR
 *              parseArrayCreation --------------------------- token for the Type of the array
 *
 *
 */
public class Parser {
    Symbol nxtToken; Symbol lookAhead;
    Lexer lexer;
    Logger logger;

    public Parser(Lexer lexer){
        this.lexer = lexer;
        this.logger = Logger.getInstance();
        readSymbol(); // Place first token in lookahead
        readSymbol(); // Place first token in nxtToken
    }

    /**
     * Consumes next token and keeps lookahead valid
     */
    private void readSymbol(){
        nxtToken = lookAhead;
        lookAhead = lexer.getNextSymbol();
    }

    /**
     * Used to parse list of statements
     * Can also parse simple code blocks, for function defs or loops or if/else
     * @return StatementList object
     * @throws ParserException from parseStatement
     */
    public ASTNodes.StatementList parseCode() throws ParserException {
        logger.log("Parsing code block " + nxtToken, null);
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

    /**
     * Parses Statement, a Statement can be almost anything:
     *  - Function def
     *  - Loop
     *  - Condition (if/else)
     *  - Var assignment/creation
     *  - Any expression
     * What is not a statement:
     *  - Type
     *  - Parameter
     *  - RecordVar
     *
     *  This function ignores (consumes) semicolons, and because other parsing functions don't check for them after being
     *  done, that makes them optional in the language
     *  The nxtToken and lookahead are used to know what kind of statement needs to be parsed
     *  and thus which parsing function to call
     *
     *  If the function cannot match current symbol to any type of statement it simply throws an exception with the token it got
     * @return
     * @throws ParserException
     */
    public ASTNodes.Statement parseStatement() throws ParserException {
        logger.log("Parsing statement " + nxtToken, null);

        while (nxtToken == SymbolToken.SEMICOLON){
            readSymbol();
        }
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
        } else if(nxtToken == SymbolToken.CLOSE_CB || nxtToken == SymbolToken.END_OF_FILE) {
            return null;
        }

        throw new ParserException("Cannot begin statement with " + nxtToken.toString());
    }

    /**
     * Parses statement of the form: delete RefToValue
     * expects nxtToken to hold token for keyword delete when called
     * @return DeleteStt object
     * @throws ParserException in case cannot parse RefToValue
     */
    public ASTNodes.DeleteStt parseDelete() throws ParserException {
        logger.log("Parsing delete " + nxtToken, null);


        readSymbol();
        ASTNodes.DeleteStt node = new ASTNodes.DeleteStt();
        node.toDelete = parseRefToValue();
        return node;
    }

    /**
     * Parses statements of the form: const Identifier Type = Expression
     * expects nxtToken to hold the token keyword const when called
     * @return ConstCreation obj
     * @throws ParserException by itself or from parseExpression
     */
    public ASTNodes.ConstCreation parseConstCreation() throws ParserException {
        logger.log("Parsing const creation " + nxtToken, null);

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

    /**
     * Parses statements of the form: val Identifier Type | val Identifier Type = Expression
     * expects nxtToken to hold the token keyword val when called
     * Because this is the exact same behaviour as the parseVarCreation function, simply calls it and uses its return object
     * @return ValCreation object
     * @throws ParserException
     */
    public ASTNodes.ValCreation parseValCreation() throws ParserException {
        logger.log("Parsing val creation " + nxtToken, null);

        // ValCreation and VarCreation have the exact same parsing mechanism, so just re-use it
        ASTNodes.ValCreation node = new ASTNodes.ValCreation();
        ASTNodes.VarCreation sub = parseVarCreation();
        node.identifier = sub.identifier;
        node.type = sub.type;
        node.valExpr = sub.varExpr;
        return node;
    }

    /**
     * Parses statements of the form: var Identifier Type | var Identifier Type = Expression
     * The pattern matched from the two above is selected based on the presence (or not) of the equal sign
     * expects nxtToken to hold the token keyword var when called
     * @return VarCreation object
     * @throws ParserException
     */
    public ASTNodes.VarCreation parseVarCreation() throws ParserException {
        logger.log("Parsing var creation " + nxtToken, null);

        // When in here, var token is already in nxtToken
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

    /**
     * The type grammar is as such
     *
     * BaseType = int | real | string | bool | identifier
     * ArrayType = BaseType [ ]
     *
     * To know whether we have a BaseType or an ArrayType, this function uses the lookahead character
     *
     * @return Type object
     * @throws ParserException
     */
    public ASTNodes.Type parseType() throws ParserException {
        logger.log("Parsing type " + nxtToken, null);

        if(!(nxtToken instanceof TypeToken || nxtToken instanceof IdentifierToken)){
            throw new ParserException("Expected type, but got " + nxtToken.toString());
        }
        ASTNodes.Type type = new ASTNodes.Type();


        if(nxtToken instanceof TypeToken) {
            type.type = ((TypeToken) nxtToken).label;

        }
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

    /**
     * Parses function call: indentifier ( ParamVals )
     *
     * Note: this is the exact same grammar for ObjectCreation, this function is also used for this, in fact the parser
     * makes no difference between function call and object creation. This will be done in later stages of the compiler
     * when we have to see what the identifier represents
     *
     * @return FunctionCall object
     * @throws ParserException
     */
    public ASTNodes.FunctionCall parseFunctionCall() throws ParserException {
        logger.log("Parsing function call " + nxtToken, null);

        // Should currently have function identifier in nxtToken
        ASTNodes.FunctionCall node = new ASTNodes.FunctionCall();
        node.identifier = ( (IdentifierToken) nxtToken).label;
        readSymbol(); readSymbol();
        node.paramVals = parseParamVals();
        return node;
    }

    /**
     * Parses list of values given to functions, since the syntax is : func(a+b, 55, "hello")
     * This function expects to hold the first token following the open parenthesis token (in the example, the identifier a)
     * This function will consume all the tokens up to and including the closing parenthesis
     *
     * @return List of expressions used to calculate the values to give to the function
     * @throws ParserException
     */
    public ArrayList<ASTNodes.Expression> parseParamVals() throws ParserException {
        logger.log("Parsing param vals " + nxtToken, null);

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

    /**
     * Parses function definition: proc identifier ( Params ) ReturnType { CodeBlock }
     * expects nxtToken to be the keyword proc
     *
     * @return FunctionDef object
     * @throws ParserException in case of missing symbol or wrong token type
     */
    public ASTNodes.FunctionDef parseFunctionDef() throws ParserException {
        logger.log("Parsing function def " + nxtToken, null);

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

    /**
     * Parse paramList, the params of a function definition
     *
     * proc func(a int, b real, c bool[]){}
     *           ^----------------------^
     * This function expects nxtToken to be the first token after the open parenthesis in the syntax and will consume
     * all the tokens until the closing parenthesis (or until an error arises)
     *
     * @return List of Param objects
     * @throws ParserException
     */
    public ArrayList<ASTNodes.Param> parseParamList() throws ParserException {
        logger.log("Parsing param list " + nxtToken, null);


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

    /**
     * Parses while loops: while Expression { CodeBlock }
     * Expects nxtToken to hold keyword while
     * @return WhileLoop object
     * @throws ParserException
     */
    public ASTNodes.WhileLoop parseWhileLoop() throws ParserException {
        logger.log("Parsing while loop " + nxtToken, null);

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
        readSymbol();
        return node;
    }

    /**
     * Parses for loop: for RefToValue = Expression by Expression to Expression { CodeBlock }
     * Expects nxtToken to hold keyword for
     *
     * @return ForLoop object
     * @throws ParserException
     */
    public ASTNodes.ForLoop parseForLoop() throws ParserException {
        logger.log("Parsing for loop " + nxtToken, null);

        // nxt symbol should be `for`
        ASTNodes.ForLoop node = new ASTNodes.ForLoop();
        readSymbol();

        node.loopVal = parseRefToValue();
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

    /**
     * Parses record
     * Expects nxtToken to hold keyword record
     *
     * @return Record object
     * @throws ParserException if no record name, or missing symbol
     */
    public ASTNodes.Record parseRecord() throws ParserException {
        logger.log("Parsing record " + nxtToken, null);

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

    /**
     * Parses recordVars, which is simply a list of the form: identifier Type identifier Type identifier Type...
     *
     * Two pair (identifier Type) can be separated by any number of semicolon:
     * The following is thus valid:
     *
     * record Rec{
     *     a int
     *     b int;;;;
     *     c int;
     *     d int
     * }
     *
     * @return ArrayList of recordVar
     * @throws ParserException from parsing type
     */
    public ArrayList<ASTNodes.RecordVar> parseRecordVars() throws ParserException {
        logger.log("Parsing record vars " + nxtToken, null);

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


    /**
     * Parses return statement: return Expression
     * @return ReturnExpr
     * @throws ParserException from parsing expression
     */
    public ASTNodes.ReturnExpr parseReturn() throws ParserException {
        logger.log("Parsing return " + nxtToken, null);

        readSymbol();
        ASTNodes.ReturnExpr node = new ASTNodes.ReturnExpr();
        node.expr = parseExpression();
        /*if(nxtToken != SymbolToken.SEMICOLON){
            throw new ParserException("Expected `;` after return statement but got " + nxtToken);
        }*/
        return node;
    }

    /**
     * Parses if condition: if Expression { CodeBlock } | if Expression { CodeBlock } else { CodeBlock }
     *
     * Will first parse as if no else was there. Once it is done parsing the if, its expression and codeblock, it will check
     * whether the lookahead token is the keyword else. If it is, then it will parse it aswell
     *
     * If there is no else, the associated members of the IfCond object will simply be null
     *
     * @return IfCond
     * @throws ParserException in case of missing/wrong symbol of while parsing expression/codeblock
     */
    public ASTNodes.IfCond parseIfCond() throws ParserException {
        logger.log("Parsing if cond " + nxtToken, null);

        // If in nxtToken
        readSymbol();
        /*if(nxtToken != SymbolToken.OPEN_PARENTHESIS){
            throw new ParserException("Expected `(` after `if` keyword but got " + nxtToken);
        }
        readSymbol();*/
        ASTNodes.IfCond node = new ASTNodes.IfCond();
        node.condition = parseExpression();
        /*if(nxtToken != SymbolToken.CLOSE_PARENTHESIS){
            throw new ParserException("Expected `)` after end of condition in `if` but got " + nxtToken);
        }

        readSymbol();*/
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
        readSymbol();
        return node;
    }

    /**
     * parses var assignment: RefToValue = Expression
     *
     * The source code might try to have expression as statements. These will usually be caught in the parseStatement,
     * but if it starts with an identifier, then this function will catch it and throw an error saying it couldn't parse
     * the var assignment.
     *
     * @return VarAssign object
     * @throws ParserException if there are no equal sign of if problem when parsing expression
     */
    public ASTNodes.VarAssign parseVarAssign() throws ParserException {
        logger.log("Parsing var assign " + nxtToken, null);

        ASTNodes.VarAssign node = new ASTNodes.VarAssign();

        node.ref = parseRefToValue();

        if(nxtToken != OperatorToken.ASSIGN){
            throw new ParserException("Expected '=' for var assignment but got " + nxtToken);
        }

        readSymbol();
        node.value = parseExpression();


        /*if(nxtToken != SymbolToken.SEMICOLON){
            throw new ParserException("Expected semicolon after var assign but got " + nxtToken);
        }
        readSymbol();*/


        return node;
    }

    /**
     * Parses ref to value. Might be as simple as a variable name, or could be nested array and object access:
     * zzz.yyy.xxx[3].bbb.ccc <- This is valid
     *
     * Since the composite RefToValue objects (ArrayAccess and ObjectAccess) have a reference to their source Object
     * (also a RefToValue) we can simply parse left to right and use the current result as source for the next object
     * This is thanks to the left associativity of these operators:
     *
     * zzz.yyy.xxx[3].bbb.ccc -> ((((((zzz).yyy).xxx)[3]).bbb).ccc)
     *
     * Note that, although the language isn't supposed to support it, this function perfectly allows for accessing
     * arrays of more than 1D (but they can't yet be created...)
     *
     * @return RefToValue
     * @throws ParserException
     */
    public ASTNodes.RefToValue parseRefToValue() throws ParserException {
        logger.log("Parsing ref to value " + nxtToken, null);

        ArrayList<ASTNodes.RefToValue> q = new ArrayList<>();

        if( !(nxtToken instanceof IdentifierToken)){
            throw new ParserException("Expected identifier but got " + nxtToken);
        }

        IdentifierToken initToken = (IdentifierToken) nxtToken;
        q.add(new ASTNodes.Identifier(initToken.label));

        while(lookAhead == SymbolToken.DOT || lookAhead == SymbolToken.OPEN_BRACKET){

            ASTNodes.RefToValue node = null;
            if(lookAhead == SymbolToken.DOT){
                node = new ASTNodes.ObjectAccess();
                ((ASTNodes.ObjectAccess) node).object =  q.get(q.size()-1);
                readSymbol(); readSymbol();
                ((ASTNodes.ObjectAccess) node).accessIdentifier = ((IdentifierToken) nxtToken).label;

            }
            else{
                node = new ASTNodes.ArrayAccess();
                /*if( !(nxtToken instanceof IdentifierToken)){
                    throw new ParserException("Expected identifier before array access but got " + nxtToken);
                }*/
                ((ASTNodes.ArrayAccess) node).ref =  q.get(q.size()-1);
                readSymbol(); readSymbol();
                ((ASTNodes.ArrayAccess) node).arrayIndex = parseExpression();
            }
            q.add(node);
        }
        readSymbol(); // Consume last symbol;

        return q.get(q.size()-1);
    }


    /**
     *
     * @return
     * @throws ParserException
     */
    public ASTNodes.Expression parseArrayCreation() throws ParserException {
        logger.log("Parsing array creation " + nxtToken, null);

        // Should have TypeToken in nxtToken
        ASTNodes.ArrayCreation node = new ASTNodes.ArrayCreation();
        node.type = new ASTNodes.Type(); node.type.type = ((TypeToken) nxtToken).label;
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






    /** --------------- EXPRESSION PARSING ------------------------
     * To have an easier time understanding the following function, here is the part of the grammar it is supposed to handle
     * written in a non-left recursive way and with operator precedence taken into account
     *
     * Highest precedence operators are lower
     *
     * Expression -> CompExp and CompExp | CompExp or CompExp
     * CompExp -> AddExp == AddExp | AddExp <> AddExp | ... (>= <= < >)
     * AddExp -> MultExp + MultExp | MultExp - MultExp
     * MultExp -> Term * Term | Term / Term | Term % Term
     * Term -> Value | RefToValue | FunctionCall | ArrayCreation | - Term | ( Expression )
     *
     * While this grammar isn't extensive so you can't see that it is in fact non-left recursive, it is.
     * Each of the following function takes care of parsing one of those levels
     *
     * parseExpression() -> parses Expression
     * parseComp() -> parses CompExp
     * parseAddSub() -> parses AddExp
     * parseMultMod() -> parses MultExp
     * parseTerm() -> parse Term
     *
     * Each function, apart from parseTerm will thus simple match their keywords/symbol and call the function
     * handling the level below
     */

    /**
     * Parses Expression
     * matches keyword and | or and calls parseComp for what is around
     *
     * @return Expression
     * @throws ParserException from parseComp
     */
    public ASTNodes.Expression parseExpression() throws ParserException {
        logger.log("Parsing expression " + nxtToken, null);

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

    /**
     * Parses CompExp
     * matches symbols == | <> | >= | <= | < | > and calls parseAddSub for what is around
     *
     * @return Expression
     * @throws ParserException from parseAddSub
     */
    public ASTNodes.Expression parseComp() throws ParserException{
        logger.log("Parsing comparison " + nxtToken, null);

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

    /**
     * Parses AddExp
     * matches symbols + | - and calls parseMultMod for what is around
     *
     * @return Expression
     * @throws ParserException from parseMultMod
     */
    public ASTNodes.Expression parseAddSub() throws ParserException {
        logger.log("Parsing add sub " + nxtToken, null);

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

    /**
     * Parses MultExp
     * matches symbols * | / | % and calls parseTerm for what is around
     *
     * @return Expression
     * @throws ParserException from parseTerm
     */
    public ASTNodes.Expression parseMultMod() throws ParserException {
        logger.log("Parsing mult mod " + nxtToken, null);

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

    /**
     * Parses Term
     * will try to match, from the current nxtToken, any of the following
     *
     * RefToValue
     * FunctionCall
     * DirectValue
     * ArrayCreation
     * - Term
     * ( Expression )
     *
     * All these together form an LL(1) grammar so we are good
     *
     * @return Expression
     * @throws ParserException if couldn't match to any of the above nodes
     */
    public ASTNodes.Expression parseTerm() throws ParserException {
        logger.log("Parsing term " + nxtToken, null);

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



}
