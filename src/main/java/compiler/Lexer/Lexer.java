package compiler.Lexer;
import java.io.IOException;
import java.io.Reader;

import static java.lang.Integer.MAX_VALUE;

public class Lexer {

    private final Reader input;

    public Lexer(Reader input) {
        if(!input.markSupported()){
            throw new RuntimeException("Can only Lex markable input");
        }
        this.input = input;
    }
    
    public Symbol getNextSymbol() {

        StringBuilder nxt;

        while(true){
            try {
                this.input.mark(MAX_VALUE);
                int readInt = this.input.read();
                if(readInt == -1) return SymbolToken.END_OF_FILE;
                nxt = new StringBuilder("" + (char) readInt);
                if(Charsets.whitespaces.contains(nxt.toString())) continue;

                if(Charsets.symbols.contains(nxt.toString())){
                    if(nxt.toString().equals("+")) return OperatorToken.PLUS;
                    if(nxt.toString().equals("-")) return OperatorToken.MINUS;
                    if(nxt.toString().equals("*")) return OperatorToken.TIMES;
                    if(nxt.toString().equals("%")) return OperatorToken.MODULUS;
                    if(nxt.toString().equals(",")) return SymbolToken.COMMA;
                    if(nxt.toString().equals(";")) return SymbolToken.SEMICOLON;
                    if(nxt.toString().equals(".")) return SymbolToken.DOT;
                    if(nxt.toString().equals("(")) return SymbolToken.OPEN_PARENTHESIS;
                    if(nxt.toString().equals(")")) return SymbolToken.CLOSE_PARENTHESIS;
                    if(nxt.toString().equals("[")) return SymbolToken.OPEN_BRACKET;
                    if(nxt.toString().equals("]")) return SymbolToken.CLOSE_BRACKET;
                    if(nxt.toString().equals("{")) return SymbolToken.OPEN_CB;
                    if(nxt.toString().equals("}")) return SymbolToken.CLOSE_CB;

                    if(nxt.toString().equals("\"")){ // Is a string value
                        char nxtChar;
                        while(true){
                            this.input.mark(MAX_VALUE); // Might need to reset if we read a char we shouldn't
                            nxtChar = (char) this.input.read();
                            if(nxtChar == '"') {
                                return new ValueToken(ValueToken.ValueType.STRING, nxt.substring(1));
                            }
                            if(nxtChar == '\n'){
                                throw new InvalidTokenException("Reached end of line before end of string");
                            }
                            if(nxtChar == '\\'){
                                char bsChar = (char) this.input.read();
                                if(bsChar == 'n') nxtChar = '\n';
                                else if(bsChar == 't') nxtChar = '\t';
                                else if(bsChar == '"') nxtChar = '\"';
                                else if(bsChar != '\\') throw new InvalidTokenException("Unrecognized char in string: \\" + bsChar);
                            }
                            nxt.append(nxtChar);
                        }
                    }

                    // If not any of the above symbol, might be a 2-char symbol
                    nxt.append((char) this.input.read());
                    if(nxt.toString().equals(">=")) return OperatorToken.GRTR_OR_EQUAL;
                    if(nxt.toString().equals("<=")) return OperatorToken.SMLR_OR_EQUAL;
                    if(nxt.toString().equals("==")) return OperatorToken.EQUALS;
                    if(nxt.toString().equals("<>")) return OperatorToken.DIFFERENT;

                    if(nxt.toString().equals("//")){ // Comment, read until newline
                        //String comment = ""; could be useful for a CommentToken
                        char newChar = ' ';
                        while(newChar != '\n'){
                            newChar = (char) this.input.read();
                        }
                        continue;
                    }

                    // All 2-chars symbols have been checked, was a one char symbol
                    this.input.reset();
                    nxt = new StringBuilder("" +  (char) this.input.read());
                    if(nxt.toString().equals("/")) return OperatorToken.DIVIDE;
                    if(nxt.toString().equals(">")) return OperatorToken.GREATER;
                    if(nxt.toString().equals("<")) return OperatorToken.SMALLER;
                    if(nxt.toString().equals("=")) return OperatorToken.ASSIGN;

                    // Couldn't resolve to any valid symbols
                    throw new InvalidTokenException("Couldn't resolve symbols with " + nxt);
                }

                if(Charsets.number.contains(nxt.toString())){
                    char nxtChar;
                    while(true){
                        this.input.mark(MAX_VALUE); // Might need to reset if we read a char we shouldn't
                        nxtChar = (char) this.input.read();
                        if(! (Charsets.number + ".").contains("" + nxtChar) ){
                            // nxtChar not for decimal notation, end of ValueToken
                            this.input.reset(); // what is nxtChar may be important for next token, need to reset
                            long dotCount = nxt.chars().filter(ch -> ch == '.').count();
                            if(dotCount > 1) throw new InvalidTokenException("Too many dots in decimal expression: " + nxt);

                            // Build and return token
                            ValueToken.ValueType type = dotCount == 0 ? ValueToken.ValueType.INT : ValueToken.ValueType.REAL;
                            return new ValueToken(type, nxt.toString());
                        }
                        nxt.append(nxtChar);
                    }
                }

                if(Charsets.identifiers.contains(nxt.toString())){
                    char nxtChar;
                    while(true){
                        this.input.mark(MAX_VALUE); // Might need to reset if we read a char we shouldn't
                        nxtChar = (char) this.input.read();
                        if(! Charsets.identifiers.contains("" + nxtChar) ){
                            this.input.reset();
                            String idString = nxt.toString();
                            if(idString.equals("true") || idString.equals("false")){
                                return new ValueToken(ValueToken.ValueType.BOOL, idString);
                            }
                            if(idString.equals("for")) return KeywordToken.FOR_LOOP;
                            if(idString.equals("by")) return KeywordToken.BY;
                            if(idString.equals("and")) return KeywordToken.AND;
                            if(idString.equals("or")) return KeywordToken.OR;
                            if(idString.equals("while")) return KeywordToken.WHILE;
                            if(idString.equals("if")) return KeywordToken.IF;
                            if(idString.equals("else")) return KeywordToken.ELSE;
                            if(idString.equals("const")) return KeywordToken.CONST;
                            if(idString.equals("proc")) return KeywordToken.FUNCTION;
                            if(idString.equals("record")) return KeywordToken.RECORD;
                            if(idString.equals("return")) return KeywordToken.RETURN;
                            if(idString.equals("to")) return KeywordToken.TO;
                            if(idString.equals("delete")) return KeywordToken.DELETE;
                            if(idString.equals("val")) return KeywordToken.VALUE;
                            if(idString.equals("var")) return KeywordToken.VARIABLE;

                            if(idString.equals("int")) return TypeToken.INT;
                            if(idString.equals("real")) return TypeToken.DOUBLE;
                            if(idString.equals("bool")) return TypeToken.BOOL;
                            if(idString.equals("string")) return TypeToken.STRING;

                            return new IdentifierToken(nxt.toString());
                        }
                        nxt.append(nxtChar);
                    }
                }

                throw new InvalidTokenException("No token starting with char " + nxt);

            } catch (IOException | InvalidTokenException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
