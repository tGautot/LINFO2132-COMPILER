import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import compiler.Lexer.*;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestLexer {

    
    @Test
    public void baseTest() {
        String input = "var x int = 2;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Symbol nxtSymbol = null;
        Symbol[] expectedSymbols = {
                KeywordToken.VARIABLE,
                new IdentifierToken("x"),
                TypeToken.INT,
                OperatorToken.ASSIGN,
                new ValueToken(ValueToken.ValueType.INT, "2"),
                SymbolToken.SEMICOLON,
                SymbolToken.END_OF_FILE
        };
        for (Symbol expectedSymbol : expectedSymbols) {
            nxtSymbol = lexer.getNextSymbol();
            assertEquals(nxtSymbol, expectedSymbol);
        }
        //assertNotNull(nxtSymbol);

    }

    @Test
    public void testTypes() {
        String input = "string bool int real";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Symbol nxtSymbol = null;
        Symbol[] expectedSymbols = {
                TypeToken.STRING,
                TypeToken.BOOL,
                TypeToken.INT,
                TypeToken.DOUBLE
        };
        for (Symbol expectedSymbol : expectedSymbols) {
            nxtSymbol = lexer.getNextSymbol();
            assertEquals(nxtSymbol, expectedSymbol);
        }
        //assertNotNull(nxtSymbol);

    }

    @Test
    public void testProc() {
        String input = "                                                                                              \n" +
                "proc square(v int) int {\n" +
                "    return v*v;\n" +
                "}\n";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Symbol nxtSymbol = null;
        Symbol[] expectedSymbols = {
                KeywordToken.FUNCTION,
                new IdentifierToken("square"),
                SymbolToken.OPEN_PARENTHESIS,
                new IdentifierToken("v"),
                TypeToken.INT,
                SymbolToken.CLOSE_PARENTHESIS,
                TypeToken.INT,
                SymbolToken.OPEN_CB,
                KeywordToken.RETURN,
                new IdentifierToken("v"),
                OperatorToken.TIMES,
                new IdentifierToken("v"),
                SymbolToken.SEMICOLON,
                SymbolToken.CLOSE_CB,
                SymbolToken.END_OF_FILE
        };
        for (Symbol expectedSymbol : expectedSymbols) {
            nxtSymbol = lexer.getNextSymbol();
            assertEquals(nxtSymbol, expectedSymbol);
        }
        //assertNotNull(nxtSymbol);

    }

    @Test
    public void testRecord() {
        String input = "record Person {\n" +
                "    name string;\n" +
                "    location Point;\n" +
                "    history int[];\n" +
                "}";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Symbol nxtSymbol = null;
        Symbol[] expectedSymbols = {
            KeywordToken.RECORD,
            new IdentifierToken("Person"),
            SymbolToken.OPEN_CB,
            new IdentifierToken("name"),
            TypeToken.STRING,
            SymbolToken.SEMICOLON,
            new IdentifierToken("location"),
            new IdentifierToken("Point"),
            SymbolToken.SEMICOLON,
            new IdentifierToken("history"),
            TypeToken.INT,
            SymbolToken.OPEN_BRACKET,
            SymbolToken.CLOSE_BRACKET,
            SymbolToken.SEMICOLON,
            SymbolToken.CLOSE_CB,
            SymbolToken.END_OF_FILE
        };
        for (Symbol expectedSymbol : expectedSymbols) {
            nxtSymbol = lexer.getNextSymbol();
            assertEquals(nxtSymbol, expectedSymbol);
        }
    }

    @Test
    public void testStrings() throws IOException {
        Path filePath = Path.of("./test/testStrings.txt");

        String input = Files.readString(filePath);
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Symbol nxtSymbol = null;
        Symbol[] expectedSymbols = {
                KeywordToken.VARIABLE,
                new IdentifierToken("s1"),
                TypeToken.STRING,
                OperatorToken.ASSIGN,
                new ValueToken(ValueToken.ValueType.STRING, "\\\\\""),
                SymbolToken.SEMICOLON,
                KeywordToken.VARIABLE,
                new IdentifierToken("s2"),
                TypeToken.STRING,
                OperatorToken.ASSIGN,
                new ValueToken(ValueToken.ValueType.STRING, ""),
                new ValueToken(ValueToken.ValueType.STRING, ""),
                SymbolToken.SEMICOLON,
                KeywordToken.VARIABLE,
                new IdentifierToken("s3"),
                TypeToken.STRING,
                OperatorToken.ASSIGN,
                new ValueToken(ValueToken.ValueType.STRING, "\""),
                SymbolToken.SEMICOLON,
                KeywordToken.VARIABLE,
                new IdentifierToken("s4"),
                TypeToken.STRING,
                OperatorToken.ASSIGN,
                new ValueToken(ValueToken.ValueType.STRING, "\\"),
                SymbolToken.SEMICOLON,
                KeywordToken.VARIABLE,
                new IdentifierToken("s5"),
                TypeToken.STRING,
                OperatorToken.ASSIGN,
                new ValueToken(ValueToken.ValueType.STRING, "\\\\\\"),
                new ValueToken(ValueToken.ValueType.STRING, "\\\""),
                SymbolToken.SEMICOLON,
                KeywordToken.VARIABLE,
                new IdentifierToken("s6"),
                TypeToken.STRING,
                OperatorToken.ASSIGN,
                new ValueToken(ValueToken.ValueType.STRING, "\\\"\t\"\n\\\t\"\n"),
                SymbolToken.SEMICOLON,
                KeywordToken.VARIABLE,
                new IdentifierToken("s7"),
                TypeToken.STRING,
                OperatorToken.ASSIGN,
                new ValueToken(ValueToken.ValueType.STRING, "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"),
                SymbolToken.SEMICOLON,
                SymbolToken.END_OF_FILE
        };
        for (Symbol expectedSymbol : expectedSymbols) {
            nxtSymbol = lexer.getNextSymbol();
            assertEquals(nxtSymbol, expectedSymbol);
        }
        assertNotNull(nxtSymbol);

    }


    // TESTS FOR ADDITIONAL FEATURES
    @Test
    public void plusAssignTest() {
        String input = "x += 2;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Symbol nxtSymbol = null;
        Symbol[] expectedSymbols = {
                new IdentifierToken("x"),
                OperatorToken.PLUS_ASSIGN,
                new ValueToken(ValueToken.ValueType.INT, "2"),
                SymbolToken.SEMICOLON,
                SymbolToken.END_OF_FILE
        };
        for (Symbol expectedSymbol : expectedSymbols) {
            nxtSymbol = lexer.getNextSymbol();
            assertEquals(nxtSymbol, expectedSymbol);
        }
        //assertNotNull(nxtSymbol);

    }

    @Test
    public void minusAssignTest() {
        String input = "x -= 2;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Symbol nxtSymbol = null;
        Symbol[] expectedSymbols = {
                new IdentifierToken("x"),
                OperatorToken.MINUS_ASSIGN,
                new ValueToken(ValueToken.ValueType.INT, "2"),
                SymbolToken.SEMICOLON,
                SymbolToken.END_OF_FILE
        };
        for (Symbol expectedSymbol : expectedSymbols) {
            nxtSymbol = lexer.getNextSymbol();
            assertEquals(nxtSymbol, expectedSymbol);
        }
        //assertNotNull(nxtSymbol);

    }

    @Test
    public void timesAssignTest() {
        String input = "x *= 2;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Symbol nxtSymbol = null;
        Symbol[] expectedSymbols = {
                new IdentifierToken("x"),
                OperatorToken.TIMES_ASSIGN,
                new ValueToken(ValueToken.ValueType.INT, "2"),
                SymbolToken.SEMICOLON,
                SymbolToken.END_OF_FILE
        };
        for (Symbol expectedSymbol : expectedSymbols) {
            nxtSymbol = lexer.getNextSymbol();
            assertEquals(nxtSymbol, expectedSymbol);
        }
        //assertNotNull(nxtSymbol);
    }

    @Test
    public void divideAssignTest() {
        String input = "x /= 2;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Symbol nxtSymbol = null;
        Symbol[] expectedSymbols = {
                new IdentifierToken("x"),
                OperatorToken.DIVIDE_ASSIGN,
                new ValueToken(ValueToken.ValueType.INT, "2"),
                SymbolToken.SEMICOLON,
                SymbolToken.END_OF_FILE
        };
        for (Symbol expectedSymbol : expectedSymbols) {
            nxtSymbol = lexer.getNextSymbol();
            assertEquals(nxtSymbol, expectedSymbol);
        }
        //assertNotNull(nxtSymbol);
    }

    @Test
    public void modAssignTest() {
        String input = "x %= 2;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Symbol nxtSymbol = null;
        Symbol[] expectedSymbols = {
                new IdentifierToken("x"),
                OperatorToken.MOD_ASSIGN,
                new ValueToken(ValueToken.ValueType.INT, "2"),
                SymbolToken.SEMICOLON,
                SymbolToken.END_OF_FILE
        };
        for (Symbol expectedSymbol : expectedSymbols) {
            nxtSymbol = lexer.getNextSymbol();
            assertEquals(nxtSymbol, expectedSymbol);
        }
        //assertNotNull(nxtSymbol);

    }

    @Test
    public void bigCommentTest() {
        String input = "var x int = 3;\n" +
                "/*here a comment on 1 line */\n" +
                "var d string = \"hello\" /* a comment\n" +
                "on multiple \n" +
                "lines*/";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Symbol nxtSymbol = null;
        Symbol[] expectedSymbols = {
                KeywordToken.VARIABLE,
                new IdentifierToken("x"),
                TypeToken.INT,
                OperatorToken.ASSIGN,
                new ValueToken(ValueToken.ValueType.INT, "3"),
                SymbolToken.SEMICOLON,
                KeywordToken.VARIABLE,
                new IdentifierToken("d"),
                TypeToken.STRING,
                OperatorToken.ASSIGN,
                new ValueToken(ValueToken.ValueType.STRING,"hello"),
                SymbolToken.END_OF_FILE
        };
        for (Symbol expectedSymbol : expectedSymbols) {
            nxtSymbol = lexer.getNextSymbol();
            assertEquals(nxtSymbol, expectedSymbol);
        }
        //assertNotNull(nxtSymbol);

    }

}
