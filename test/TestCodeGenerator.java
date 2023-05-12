import compiler.CodeGenerator.CodeGenerator;
import compiler.Lexer.*;
import compiler.SemanticAnalyzer.SemanticAnalyzer;
import compiler.SemanticAnalyzer.SemanticAnalyzerException;
import compiler.parser.ASTNodes;
import compiler.parser.Parser;
import compiler.parser.ParserException;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class TestCodeGenerator {

    @Test
    public void test() throws IOException, SemanticAnalyzerException, ClassNotFoundException {
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
        //SemanticAnalyzer analyzer = new SemanticAnalyzer(sl);
        //analyzer.analyze(sl,analyzer.symbolTable);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(sl);
        analyzer.analyze(sl,analyzer.symbolTable,true);
        CodeGenerator generator = new CodeGenerator(sl);

        assertTrue(true);
    }


}
