import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import compiler.Lexer.*;
import compiler.parser.ASTNodes;
import compiler.parser.Parser;
import compiler.parser.ParserException;
import org.junit.Test;

import java.io.StringReader;

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

}
