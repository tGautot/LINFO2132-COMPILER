import com.google.errorprone.annotations.Var;
import compiler.Lexer.*;
import compiler.parser.ASTNodes;
import compiler.parser.Parser;
import compiler.parser.ParserException;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class TestParser {


    @Test
    public void testBasicFunc() {
        String input = "proc myfunc(a int, b real[], c bool) int {" +
                "return a*b" +
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

        ASTNodes.Type tInt = new ASTNodes.Type("int", false);
        ASTNodes.Type tRealArr = new ASTNodes.Type("real", true);
        ASTNodes.Type tBool = new ASTNodes.Type("bool", false);

        ArrayList<ASTNodes.Statement> expected = new ArrayList<>();




        ASTNodes.StatementList functionSl = new ASTNodes.StatementList();
        ArrayList<ASTNodes.Statement> code = new ArrayList<>();
        code.add(new ASTNodes.ReturnExpr(new ASTNodes.MultExpr(
                new ASTNodes.Identifier("a"),
                new ASTNodes.Identifier("b")
        )));
        functionSl.statements = code;
        ArrayList<ASTNodes.Param> params = new ArrayList<>();
        params.add(new ASTNodes.Param(tInt, "a"));
        params.add(new ASTNodes.Param(tRealArr, "b"));
        params.add(new ASTNodes.Param(tBool, "c"));
        ASTNodes.FunctionDef fDef = new ASTNodes.FunctionDef("myfunc", tInt, params, functionSl);

        expected.add(fDef);

        for(int i = 0; i < expected.size(); i++){
            ASTNodes.Statement expct = expected.get(i);
            ASTNodes.Statement got = sl.statements.get(i);
            assertEquals(expct, got);
        }

    }

    @Test
    public void testNoSemicolon() {
        String input1 = "var a int = 3 var b bool if a == 3 { b = true } else { b = 6*7*8==8*7*6 and sqrt(49) == sqrt(49) a = 555 } printf(b)";
        StringReader reader1 = new StringReader(input1);
        Lexer lexer1 = new Lexer(reader1);
        Parser parser1 = new Parser(lexer1);

        String input2 = "var a int = 3;" +
                "var b bool;" +
                "if a == 3 { " +
                "   b = true; " +
                "} else { " +
                "   b = 6*7*8==8*7*6 and sqrt(49) == sqrt(49);" +
                "   a = 555;" +
                "}" +
                "printf(b);";
        StringReader reader2 = new StringReader(input2);
        Lexer lexer2 = new Lexer(reader2);
        Parser parser2 = new Parser(lexer2);
        ArrayList<ASTNodes.Statement> sl1 = null, sl2 = null;
        try {
            sl1 = parser1.parseCode().statements;
            sl2 = parser2.parseCode().statements;
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }

        assertEquals(sl1.size(), sl2.size());
        for(int i = 0; i < sl1.size(); i++){
            assertEquals(sl1.get(i), sl2.get(i));
        }
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

        ASTNodes.Type tInt = new ASTNodes.Type("int", false);
        ASTNodes.Type tString = new ASTNodes.Type("string", false);
        ASTNodes.Type tPoint = new ASTNodes.Type("Point", false);
        ASTNodes.Type tReal = new ASTNodes.Type("real", false);
        ASTNodes.Type tIntArr = new ASTNodes.Type("int", true);

        ArrayList<ASTNodes.Statement> expected = new ArrayList<>();

        ArrayList<ASTNodes.RecordVar> pointVars = new ArrayList<>();
        pointVars.add(new ASTNodes.RecordVar("x", tReal));
        pointVars.add(new ASTNodes.RecordVar("y", tReal));

        ArrayList<ASTNodes.RecordVar> personVars = new ArrayList<>();
        personVars.add(new ASTNodes.RecordVar("name", tString));
        personVars.add(new ASTNodes.RecordVar("location", tPoint));
        personVars.add(new ASTNodes.RecordVar("history", tIntArr));

        ASTNodes.Record pointDef = new ASTNodes.Record("Point", pointVars);
        ASTNodes.Record personDef = new ASTNodes.Record("Person", personVars);

        expected.add(pointDef);
        expected.add(personDef);

        for(int i = 0; i < expected.size(); i++){
            ASTNodes.Statement expct = expected.get(i);
            ASTNodes.Statement got = sl.statements.get(i);
            assertEquals(expct, got);
        }
    }


    @Test
    public void testVoidType() throws ParserException {


        String input = "proc myfunc(a int, b real[], c bool) void {" +
                "var aaa int[] = int[](7)" +
                "}";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser4 = new Parser(lexer);
        ASTNodes.StatementList sl;
        try {
            sl = parser4.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }

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

        ASTNodes.DirectValue val3 = new ASTNodes.DirectValue("3", new ASTNodes.Type("int", false));
        ASTNodes.RefToValue expected = new ASTNodes.ObjectAccess(
                new ASTNodes.ObjectAccess(
                        new ASTNodes.ArrayAccess(
                                new ASTNodes.ObjectAccess(
                                        new ASTNodes.ObjectAccess(
                                                new ASTNodes.Identifier("zzz"),
                                                "yyy"
                                        ),
                                        "xxx"
                                ), val3
                        ),
                        "bbb"
                ),
                "ccc"
        );

        assertEquals(expected, sl);
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

        ArrayList<ASTNodes.Statement> expected = new ArrayList<>();


        ASTNodes.DirectValue val3 = new ASTNodes.DirectValue("3", new ASTNodes.Type("int", false));
        ASTNodes.DirectValue val2 = new ASTNodes.DirectValue("2", new ASTNodes.Type("int", false));
        ASTNodes.DirectValue val16 = new ASTNodes.DirectValue("16", new ASTNodes.Type("int", false));

        ASTNodes.RefToValue ref1 = new ASTNodes.ArrayAccess(
                new ASTNodes.ObjectAccess(
                        new ASTNodes.ObjectAccess(
                                new ASTNodes.Identifier("zzz"),
                                "yyy"
                        ),
                        "xxx"
                ), val3
        );

        expected.add(new ASTNodes.VarAssign(ref1, val2));
        expected.add(new ASTNodes.VarAssign( new ASTNodes.Identifier("aaa"), val16));

        for(int i = 0; i < expected.size(); i++){
            ASTNodes.Statement expct = expected.get(i);
            ASTNodes.Statement got = sl.statements.get(i);
            assertEquals(expct, got);
        }

    }

    @Test
    public void testPlusAssign(){
        String input = "zzz.yyy.xxx[3] += 2; aaa += 16;";
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


        ASTNodes.DirectValue val3 = new ASTNodes.DirectValue("3", new ASTNodes.Type("int", false));
        ASTNodes.DirectValue val2 = new ASTNodes.DirectValue("2", new ASTNodes.Type("int", false));
        ASTNodes.DirectValue val16 = new ASTNodes.DirectValue("16", new ASTNodes.Type("int", false));

        ASTNodes.RefToValue ref1 = new ASTNodes.ArrayAccess(
                new ASTNodes.ObjectAccess(
                        new ASTNodes.ObjectAccess(
                                new ASTNodes.Identifier("zzz"),
                                "yyy"
                        ),
                        "xxx"
                ), val3
        );

        //expected.add(new ASTNodes.VarAssign(ref1, val2));
        expected.add(new ASTNodes.VarAssign(ref1, new ASTNodes.AddExpr(ref1,val2)));
        //expected.add(new ASTNodes.VarAssign( new ASTNodes.Identifier("aaa"), val16));
        expected.add(new ASTNodes.VarAssign( new ASTNodes.Identifier("aaa"), new ASTNodes.AddExpr(new ASTNodes.Identifier("aaa"),val16)));

        for(int i = 0; i < expected.size(); i++){
            ASTNodes.Statement expct = expected.get(i);
            ASTNodes.Statement got = sl.statements.get(i);
            boolean b = expct.equals(got);
            assertTrue(b);
            //assertEquals(expct, got);
        }

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
        ASTNodes.Type tString= new ASTNodes.Type("string", false);


        // aaa = 16;

        ASTNodes.DirectValue val16 = new ASTNodes.DirectValue("16", tInt);
        ASTNodes.VarAssign a = new ASTNodes.VarAssign(new ASTNodes.Identifier("aaa"), val16);
        expected.add(a);

        // bbb[5] = 17+8;


        ASTNodes.DirectValue val17 = new ASTNodes.DirectValue("17", tInt);
        ASTNodes.DirectValue val8p5 = new ASTNodes.DirectValue("8.5", tReal);
        ASTNodes.AddExpr bExpr = new ASTNodes.AddExpr(val17, val8p5);
        ASTNodes.DirectValue val5 = new ASTNodes.DirectValue("5", tInt);
        ASTNodes.ArrayAccess ref = new ASTNodes.ArrayAccess(new ASTNodes.Identifier("bbb"), val5);
        ASTNodes.VarAssign b = new ASTNodes.VarAssign(ref, bExpr);

        expected.add(b);

        // ccc.xxx = -(18+9)*-5;
        ASTNodes.ObjectAccess refC = new ASTNodes.ObjectAccess(new ASTNodes.Identifier("ccc"), "xxx");
        ASTNodes.NegateExpr neg5 = new ASTNodes.NegateExpr(val5);

        ASTNodes.DirectValue val18 = new ASTNodes.DirectValue("18", tInt);
        ASTNodes.DirectValue val9 = new ASTNodes.DirectValue("9", tInt);
        ASTNodes.AddExpr sum18p9 = new ASTNodes.AddExpr(val18, val9);
        ASTNodes.NegateExpr negSum18p9 = new ASTNodes.NegateExpr(sum18p9);


        ASTNodes.MultExpr topExpr = new ASTNodes.MultExpr(negSum18p9, neg5);

        ASTNodes.VarAssign c = new ASTNodes.VarAssign(refC, topExpr);

        expected.add(c);

        // ddd = -arr[sqrt(49)]%(2*-3*--4);
        ASTNodes.DirectValue val49 = new ASTNodes.DirectValue("49", tInt);
        ArrayList<ASTNodes.Expression> sqrtParams = new ArrayList<>();
        sqrtParams.add(val49);
        ASTNodes.FunctionCall sqrtCall = new ASTNodes.FunctionCall("sqrt", sqrtParams);
        ASTNodes.ArrayAccess arr = new ASTNodes.ArrayAccess(new ASTNodes.Identifier("arr"), sqrtCall);
        ASTNodes.NegateExpr negArr = new ASTNodes.NegateExpr(arr);
        ASTNodes.DirectValue val2 = new ASTNodes.DirectValue("2", tInt);
        ASTNodes.DirectValue val3 = new ASTNodes.DirectValue("3", tInt);
        ASTNodes.DirectValue val4 = new ASTNodes.DirectValue("4", tInt);
        ASTNodes.NegateExpr neg3 = new ASTNodes.NegateExpr(val3);
        ASTNodes.NegateExpr neg4 = new ASTNodes.NegateExpr(val4);
        ASTNodes.NegateExpr negNeg4 = new ASTNodes.NegateExpr(neg4);
        ASTNodes.MultExpr mult1 = new ASTNodes.MultExpr(val2, neg3);
        ASTNodes.MultExpr mult2 = new ASTNodes.MultExpr(mult1, negNeg4);
        ASTNodes.ModExpr topMod = new ASTNodes.ModExpr(negArr, mult2);

        ASTNodes.VarAssign d = new ASTNodes.VarAssign(new ASTNodes.Identifier("ddd"), topMod );

        expected.add(d);

        // eee = 6==7 and sqrt(64)<>sqrt(81) or "Hello"+3 ;
        ASTNodes.DirectValue valHello = new ASTNodes.DirectValue("Hello", tString);
        ASTNodes.DirectValue val81 = new ASTNodes.DirectValue("81", tInt);
        ASTNodes.DirectValue val64 = new ASTNodes.DirectValue("64", tInt);
        ASTNodes.DirectValue val6 = new ASTNodes.DirectValue("6", tInt);
        ASTNodes.DirectValue val7 = new ASTNodes.DirectValue("7", tInt);

        ASTNodes.EqComp sixEqSeven = new ASTNodes.EqComp(val6, val7);

        ArrayList<ASTNodes.Expression> paramSqrt64 = new ArrayList<>();
        paramSqrt64.add(val64);
        ArrayList<ASTNodes.Expression> paramSqrt81 = new ArrayList<>();
        paramSqrt81.add(val81);
        ASTNodes.FunctionCall sqrt64 = new ASTNodes.FunctionCall("sqrt", paramSqrt64);
        ASTNodes.FunctionCall sqrt81 = new ASTNodes.FunctionCall("sqrt", paramSqrt81);
        ASTNodes.NotEqComp sqrtDiffSqrt = new ASTNodes.NotEqComp(sqrt64, sqrt81);

        ASTNodes.AndComp andComp = new ASTNodes.AndComp(sixEqSeven, sqrtDiffSqrt);

        ASTNodes.AddExpr addHello3 = new ASTNodes.AddExpr(valHello, val3);
        ASTNodes.OrComp orComp = new ASTNodes.OrComp(andComp, addHello3);

        ASTNodes.VarAssign e = new ASTNodes.VarAssign(new ASTNodes.Identifier("eee"), orComp    );
        expected.add(e);


        for(int i = 0; i < expected.size(); i++){
            ASTNodes.Statement expct = expected.get(i);
            ASTNodes.Statement got = sl.statements.get(i);
            assertEquals(expct, got);
        }

    }


    @Test
    public void testCodeExample() throws IOException {
        // This test is considered valid if the parser doesn't crash
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
        assertTrue(true);
    }

    @Test
    public void testLoops() {
        String input = "for i=1 to 100 by 2 {\n" +
                "        while value<>3 {\n" +
                "            b = b + 5\n" +
                "        }\n" +
                "    }";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        ASTNodes.StatementList sl;
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }

        ASTNodes.Type tInt = new ASTNodes.Type("int", false);
        ASTNodes.DirectValue val1 = new ASTNodes.DirectValue("1", tInt);
        ASTNodes.DirectValue val2 = new ASTNodes.DirectValue("2", tInt);
        ASTNodes.DirectValue val3 = new ASTNodes.DirectValue("3", tInt);
        ASTNodes.DirectValue val5 = new ASTNodes.DirectValue("5", tInt);
        ASTNodes.DirectValue val100 = new ASTNodes.DirectValue("100", tInt);
        ASTNodes.Identifier bId = new ASTNodes.Identifier("b");
        ASTNodes.Identifier iId = new ASTNodes.Identifier("i");
        ASTNodes.Identifier valueId = new ASTNodes.Identifier("value");

        ArrayList<ASTNodes.Statement> expected = new ArrayList<>();

        ArrayList<ASTNodes.Statement> wlCode = new ArrayList<>();
        wlCode.add(new ASTNodes.VarAssign(bId, new ASTNodes.AddExpr(bId, val5)));
        ASTNodes.StatementList wlSL = new ASTNodes.StatementList();
        wlSL.statements = wlCode;
        ASTNodes.Expression wlCondition = new ASTNodes.NotEqComp(valueId, val3);
        ASTNodes.WhileLoop whileLoop = new ASTNodes.WhileLoop(wlCondition, wlSL);

        ArrayList<ASTNodes.Statement> flCode = new ArrayList<>();
        flCode.add(whileLoop);
        ASTNodes.StatementList flSL = new ASTNodes.StatementList();
        flSL.statements = flCode;

        ASTNodes.ForLoop forLoop = new ASTNodes.ForLoop(iId, val1, val100, val2, flSL);
        expected.add(forLoop);

        for(int i = 0; i < expected.size(); i++){
            ASTNodes.Statement expct = expected.get(i);
            ASTNodes.Statement got = sl.statements.get(i);
            assertEquals(expct, got);
        }

    }

}
