import compiler.Lexer.Lexer;
import compiler.SemanticAnalyzer.SemanticAnalyzer;
import compiler.SemanticAnalyzer.SemanticAnalyzerException;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.parser.ASTNodes;
import compiler.parser.Parser;
import compiler.parser.ParserException;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;



public class TestSemanticAnalyzer {

    @Test
    public void testConstCreation() throws ParserException, SemanticAnalyzerException {
        String input = "const a int = 3;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        ASTNodes.StatementList sl;
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }

        SemanticAnalyzer analyzer = new SemanticAnalyzer(sl);
        analyzer.analyze(sl,analyzer.symbolTable);

        assertTrue(true);
    }

    @Test
    public void testValCreation() throws SemanticAnalyzerException {
        String input = "val a int = 3;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        ASTNodes.StatementList sl;
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }

        SemanticAnalyzer analyzer = new SemanticAnalyzer(sl);
        analyzer.analyze(sl,analyzer.symbolTable);

        assertTrue(true);
    }

    @Test
    public void testVarCreation() throws SemanticAnalyzerException {
        String input = "var a int = 3;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        ASTNodes.StatementList sl;
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }

        SemanticAnalyzer analyzer = new SemanticAnalyzer(sl);
        analyzer.analyze(sl,analyzer.symbolTable);

        assertTrue(true);
    }

    @Test
    public void testCodeExample() throws IOException, SemanticAnalyzerException {
        Path filePath = Path.of("./test/simple_code_copie.txt");

        String input = Files.readString(filePath);
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        ASTNodes.StatementList sl;
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }
        SemanticAnalyzer analyzer = new SemanticAnalyzer(sl);
        analyzer.analyze(sl,analyzer.symbolTable);

        assertTrue(true);


    }
}
