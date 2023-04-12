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
        String input = "const a int = 3;\n";
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
        //
        input += "record Point {\n" +
                "    x int;\n" +
                "    y int;\n" +
                "}" +
                "const test Point = Point(3,1)";
        reader = new StringReader(input);
        lexer = new Lexer(reader);
        parser = new Parser(lexer);
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }

        analyzer = new SemanticAnalyzer(sl);
        SemanticAnalyzer finalAnalyzer = analyzer;
        ASTNodes.StatementList finalSl = sl;
        assertThrows(SemanticAnalyzerException.class,
                ()-> finalAnalyzer.analyze(finalSl, finalAnalyzer.symbolTable));

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

        // type void in a val
        input = "val test void = 3";
        reader = new StringReader(input);
        lexer = new Lexer(reader);
        parser = new Parser(lexer);
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }

        analyzer = new SemanticAnalyzer(sl);
        SemanticAnalyzer finalAnalyzer = analyzer;
        ASTNodes.StatementList finalSl = sl;
        assertThrows(SemanticAnalyzerException.class,
                ()-> finalAnalyzer.analyze(finalSl, finalAnalyzer.symbolTable));

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
    public void testForLoop() throws SemanticAnalyzerException {
        // correct input
        String input = "var i int = 0;\n" +
                "var j int = 0;\n" +
                "var k int = 0;\n" +
                "\n" +
                "for i=0+1+len(\"hello\") to 10/2 by 1 {\n" +
                "    for j=0 to 20 by 2*floor(3.2) {\n" +
                "        for k=3 to 30 by 1 {\n" +
                "            val a string = \"hello\";\n" +
                "        }\n" +
                "    }\n" +
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

        SemanticAnalyzer analyzer = new SemanticAnalyzer(sl);
        analyzer.analyze(sl,analyzer.symbolTable);

        assertTrue(true);

        // input with error in 3rd loop
        input = "var i int = 0;\n" +
                "var j int = 0;\n" +
                "var k int = 0;\n" +
                "\n" +
                "for i=0+1+len(\"hello\") to 10/2 by 1 {\n" +
                "    for j=0 to 20 by 2*floor(3.2) {\n" +
                "        for k=3 to 30 by 7.3*8.8 {\n" +
                "            val a string = \"hello\";\n" +
                "        }\n" +
                "    }\n" +
                "}";
        reader = new StringReader(input);
        lexer = new Lexer(reader);
        parser = new Parser(lexer);
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }

        analyzer = new SemanticAnalyzer(sl);
        SemanticAnalyzer finalAnalyzer = analyzer;
        ASTNodes.StatementList finalSl = sl;
        assertThrows(SemanticAnalyzerException.class,
                ()-> finalAnalyzer.analyze(finalSl, finalAnalyzer.symbolTable));
        //analyzer.analyze(sl,analyzer.symbolTable);

    }

    @Test
    public void testWhileLoop() throws SemanticAnalyzerException {
        String input = "while not(3<>4 and \"hello\"==\"hola\" or 3>7%4) {\n" +
                "    var a int = 35\n" +
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

        SemanticAnalyzer analyzer = new SemanticAnalyzer(sl);
        analyzer.analyze(sl,analyzer.symbolTable);

        assertTrue(true);

        // incorrect condition in while
        input = "while 3<>4 and \"hello\"+\"hola\" or 3>7%4 {\n" +
                "    var a int = 35\n" +
                "}";
        reader = new StringReader(input);
        lexer = new Lexer(reader);
        parser = new Parser(lexer);
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }

        analyzer = new SemanticAnalyzer(sl);
        SemanticAnalyzer finalAnalyzer = analyzer;
        ASTNodes.StatementList finalSl = sl;
        assertThrows(SemanticAnalyzerException.class,
                ()-> finalAnalyzer.analyze(finalSl, finalAnalyzer.symbolTable));

    }

    @Test
    public void testFunctionCall() throws SemanticAnalyzerException {
        String input = "proc copyPoints(p Point,a int) Point {\n" +
                "     return Point(square(p.x)/a, square(p.y)+a*a)\n" +
                "}\n" +
                "\n" +
                "record Point {\n" +
                "    x int;\n" +
                "    y int;\n" +
                "}\n" +
                "\n" +
                "proc square(v int) int {\n" +
                "    return v*v;\n" +
                "}\n" +
                "\n" +
                "var p Point = copyPoints(Point(10%3,chr(\"hello\")),3); ";
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

        // incorrect type for arg "a" in copyPoints
        input = "proc copyPoints(p Point,a int) Point {\n" +
                "     return Point(square(p.x)/a, square(p.y)+a*a)\n" +
                "}\n" +
                "\n" +
                "record Point {\n" +
                "    x int;\n" +
                "    y int;\n" +
                "}\n" +
                "\n" +
                "proc square(v int) int {\n" +
                "    return v*v;\n" +
                "}\n" +
                "\n" +
                "var p Point = copyPoints(Point(10%3,chr(\"hello\")),not(3.4<>1.2)); ";
        reader = new StringReader(input);
        lexer = new Lexer(reader);
        parser = new Parser(lexer);
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }

        analyzer = new SemanticAnalyzer(sl);
        SemanticAnalyzer finalAnalyzer = analyzer;
        ASTNodes.StatementList finalSl = sl;
        assertThrows(SemanticAnalyzerException.class,
                ()-> finalAnalyzer.analyze(finalSl, finalAnalyzer.symbolTable));
    }

    @Test
    public void testVarAssign() throws SemanticAnalyzerException {
        String input = "record Point {\n" +
                "    x int;\n" +
                "    y int;\n" +
                "}\n" +
                "\n" +
                "record Person {\n" +
                "    name string;\n" +
                "    location Point;\n" +
                "    history int[];\n" +
                "    ref A;\n" +
                "}\n" +
                "\n" +
                "record A {\n" +
                "    name string;\n" +
                "    ref B;\n" +
                "}\n" +
                "\n" +
                "record B {\n" +
                "    ref C;\n" +
                "}\n" +
                "\n" +
                "record C {\n" +
                "    tab int[];\n" +
                "    p Point;\n" +
                "}\n" +
                "\n" +
                "var p Person = Person( \"me\",Point(1,2),int[](5),A(\"hola\",B(C(int[](10),Point(10,10)))) )\n" +
                "p.ref.ref.ref.tab[0] = -10;\n" +
                "p.ref.ref.ref.p.x = 34;";
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

        // incorrect assign : real instead of int
        input += "\n" +
                "p.ref.ref.ref.tab[0] = -10.0;";
        reader = new StringReader(input);
        lexer = new Lexer(reader);
        parser = new Parser(lexer);
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }

        analyzer = new SemanticAnalyzer(sl);
        SemanticAnalyzer finalAnalyzer = analyzer;
        ASTNodes.StatementList finalSl = sl;

        assertThrows(SemanticAnalyzerException.class,
                ()-> finalAnalyzer.analyze(finalSl, finalAnalyzer.symbolTable));

    }

    @Test
    public void testFunctionDef() throws SemanticAnalyzerException {
        String input = "proc myfunc(a int, b real[], c bool) void {" +
                "var aaa int[] = int[](7)" +
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

        SemanticAnalyzer analyzer = new SemanticAnalyzer(sl);
        analyzer.analyze(sl,analyzer.symbolTable);

        assertTrue(true);

        // correct function
        input = "proc myfunc(a int, b real[], c bool) real[] {" +
                "var aaa int[] = int[](7)" +
                "b[0]=1.1" +
                "return b" +
                "}";
        reader = new StringReader(input);
        lexer = new Lexer(reader);
        parser = new Parser(lexer);
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }

        analyzer = new SemanticAnalyzer(sl);
        analyzer.analyze(sl,analyzer.symbolTable);
        assertTrue(true);


        // void function returns something but should not
        input = "proc myfunc(a int, b real[], c bool) void {" +
                "var aaa int[] = int[](7)" +
                "return a" +
                "}";
        reader = new StringReader(input);
        lexer = new Lexer(reader);
        parser = new Parser(lexer);
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }

        analyzer = new SemanticAnalyzer(sl);

        SemanticAnalyzer finalAnalyzer = analyzer;
        ASTNodes.StatementList finalSl = sl;
        assertThrows(SemanticAnalyzerException.class,
                ()-> finalAnalyzer.analyze(finalSl, finalAnalyzer.symbolTable));

        // returns nothing but it should return
        input = "proc myfunc(a int, b real[], c bool) int {" +
                "var aaa int[] = int[](7)" +
                "}";
        reader = new StringReader(input);
        lexer = new Lexer(reader);
        parser = new Parser(lexer);
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }

        analyzer = new SemanticAnalyzer(sl);

        SemanticAnalyzer finalAnalyzer1 = analyzer;
        ASTNodes.StatementList finalSl1 = sl;
        assertThrows(SemanticAnalyzerException.class,
                ()-> finalAnalyzer1.analyze(finalSl1, finalAnalyzer1.symbolTable));

        // returns the wrong type
        input = "proc myfunc(a int, b real[], c bool) int {" +
                "var aaa int[] = int[](7)" +
                "return c" +
                "}";
        reader = new StringReader(input);
        lexer = new Lexer(reader);
        parser = new Parser(lexer);
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }

        analyzer = new SemanticAnalyzer(sl);
        SemanticAnalyzer finalAnalyzer2 = analyzer;
        ASTNodes.StatementList finalSl2 = sl;
        assertThrows(SemanticAnalyzerException.class,
                ()-> finalAnalyzer2.analyze(finalSl2, finalAnalyzer2.symbolTable));

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
