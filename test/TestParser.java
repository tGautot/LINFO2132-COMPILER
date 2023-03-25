import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import compiler.Lexer.*;
import compiler.parser.ASTNodes;
import compiler.parser.Parser;
import compiler.parser.ParserException;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestParser {


    @Test
    public void testEmptyFunc() {
        String input = "proc myfunc(a int, b real[], c bool){}";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        ASTNodes.StatementList sl;
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }
        System.out.println("RESULT:");
        System.out.println(sl.toString());
        //assertNotNull(nxtSymbol);

    }

    @Test
    public void testRecords(){
        String input = "record Point {\n" +
                "    x real;\n" +
                "    y real;\n" +
                "}\n" +
                "\n" +
                "record Person {\n" +
                "    name string;\n" +
                "    location Point;\n" +
                "    history int[];\n" +
                "}";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        ASTNodes.StatementList sl;
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }
        System.out.println("RESULT:");
        System.out.println(sl.toString());
    }

    @Test
    public void testRefToValue(){
        String input = "zzz.yyy.xxx[3].bbb.ccc";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        ASTNodes.RefToValue sl;
        try {
            sl = parser.parseRefToValue();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }
        System.out.println("RESULT:");
        System.out.println(sl.toString());
    }

    @Test
    public void testVarAssign(){
        String input = "zzz.yyy.xxx[3] = 2; aaa = 16;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        ASTNodes.StatementList sl;
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }
        System.out.println("RESULT:");
        System.out.println(sl.toString());
    }


    @Test
    public void testExpressions(){
        String input =
                "aaa = 16;" +
                "bbb = 17+8;" +
                "ccc = -(18+9)*-5;" +
                "ddd = -arr[sqrt(49)]%(2*-3*--4);" +
                "eee = 6==7 and sqrt(64)<>sqrt(81) or \"Hello\"+3 ;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        ASTNodes.StatementList sl;
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }
        System.out.println("RESULT:");
        System.out.println(sl.toString());
    }


    @Test
    public void testCodeExample() throws IOException {
        Path filePath = Path.of("./test/simple_code.txt");

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
        System.out.println("RESULT:");
        System.out.println(sl.toString());
    }

}
