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
import java.util.ArrayList;

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
                "bbb[5] = 17+8.5;" +
                "ccc.xxx = -(18+9)*-5;" +
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

        ArrayList<ASTNodes.Statement> expected = new ArrayList<>();

        ASTNodes.Type tInt= new ASTNodes.Type("int", false);
        ASTNodes.Type tReal= new ASTNodes.Type("real", false);


        // aaa = 16;

        ASTNodes.DirectValue val16 = new ASTNodes.DirectValue("16", tInt);
        ASTNodes.DirectVarAssign a = new ASTNodes.DirectVarAssign(val16, "aaa");
        expected.add(a);

        // bbb[5] = 17+8;


        ASTNodes.DirectValue val17 = new ASTNodes.DirectValue("17", tInt);
        ASTNodes.DirectValue val8p5 = new ASTNodes.DirectValue("8.5", tReal);
        ASTNodes.AddExpr bExpr = new ASTNodes.AddExpr(val17, val8p5);
        ASTNodes.DirectValue val5 = new ASTNodes.DirectValue("5", tInt);
        ASTNodes.ArrayAccessFromId ref = new ASTNodes.ArrayAccessFromId("bbb", val5);
        ASTNodes.RefVarAssign b = new ASTNodes.RefVarAssign(bExpr, ref);

        expected.add(b);

        // ccc.xxx = -(18+9)*-5;
        ASTNodes.ObjectAccessFromId refC = new ASTNodes.ObjectAccessFromId("ccc", "xxx");
        ASTNodes.NegateExpr neg5 = new ASTNodes.NegateExpr(val5);

        ASTNodes.DirectValue val18 = new ASTNodes.DirectValue("18", tInt);
        ASTNodes.DirectValue val9 = new ASTNodes.DirectValue("9", tInt);
        ASTNodes.AddExpr sum18p9 = new ASTNodes.AddExpr(val18, val9);
        ASTNodes.NegateExpr negSum18p9 = new ASTNodes.NegateExpr(sum18p9);


        ASTNodes.MultExpr topExpr = new ASTNodes.MultExpr(negSum18p9, neg5);

        ASTNodes.RefVarAssign c = new ASTNodes.RefVarAssign(topExpr, refC);

        expected.add(c);

        for(int i = 0; i < expected.size(); i++){
            ASTNodes.Statement expct = expected.get(i);
            ASTNodes.Statement got = sl.statements.get(i);
            assertEquals(expct, got);
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
