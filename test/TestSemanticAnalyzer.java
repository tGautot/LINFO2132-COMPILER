import compiler.Lexer.Lexer;
import compiler.SemanticAnalyzer.SemanticAnalyzer;
import compiler.SemanticAnalyzer.SemanticAnalyzerException;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.parser.ASTNodes;
import compiler.parser.Parser;
import compiler.parser.ParserException;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;



public class TestSemanticAnalyzer {

    @Test
    public void testConstCreation() throws ParserException, SemanticAnalyzerException {
        // correct input
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
        analyzer.analyze(sl,analyzer.symbolTable,true);

        assertTrue(true);


        // incorrect inputs
        String[] inputs = {"const a int = 3;\n" +
                "\n" +
                "record Point {\n" +
                "    x int;\n" +
                "    y int;\n" +
                "}\n" +
                "const test Point = Point(3,4)",
                "const a int = 3;\n" +
                        "a = 10;"};

        for (String str : inputs) {
            reader = new StringReader(str);
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
                    ()-> finalAnalyzer1.analyze(finalSl1, finalAnalyzer1.symbolTable,true));
        }

    }

    @Test
    public void testValCreation() throws SemanticAnalyzerException {
        // correct input
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
        analyzer.analyze(sl,analyzer.symbolTable,true);

        assertTrue(true);


        // incorrect inputs
        String[] inputs = {"val test void = 3",
                            "val a int = 3;\n" +
                                    "a = 10;"};

        for (String str : inputs) {
            reader = new StringReader(str);
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
                    ()-> finalAnalyzer1.analyze(finalSl1, finalAnalyzer1.symbolTable,true));
        }

    }

    @Test
    public void testVarCreation() throws SemanticAnalyzerException {
        // correct input
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
        analyzer.analyze(sl,analyzer.symbolTable,true);

        assertTrue(true);
    }

    @Test
    public void testArrayTypes() throws SemanticAnalyzerException {
        // correct input
        String input = "var a bool[] = bool[](5); var b real = a[3];";
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
        try {
            analyzer.analyze(sl,analyzer.symbolTable,true);
        } catch (SemanticAnalyzerException e) {
            return;
        }

        assertTrue(false);
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
                "            val a string = chr(78);\n" +
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
        analyzer.analyze(sl,analyzer.symbolTable,true);

        assertTrue(true);

        //incorrect input : error in 3rd loop
        input = "var i int = 0;\n" +
                "var j int = 0;\n" +
                "var k int = 0;\n" +
                "\n" +
                "for i=0+1+len(\"hello\") to 10/2 by 1 {\n" +
                "    for j=0 to 20 by 2*floor(3.2) {\n" +
                "        for k=3 to 30 by 7.3*8.8 {\n" +
                "            val a string = chr(78);\n" +
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
                ()-> finalAnalyzer.analyze(finalSl, finalAnalyzer.symbolTable,true));

    }

    @Test
    public void testWhileLoop() throws SemanticAnalyzerException {
        // correct input
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
        analyzer.analyze(sl,analyzer.symbolTable,true);

        assertTrue(true);

        // incorrect input : condition in while
        input = "while 3<>4 and chr(78)+\"hola\" or 3>7%4 {\n" +
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
                ()-> finalAnalyzer.analyze(finalSl, finalAnalyzer.symbolTable,true));

    }

    @Test
    public void testFunctionCall() throws SemanticAnalyzerException {
        // correct input
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
                "var p Point = copyPoints(Point(10%3,len(\"hello\")),3); ";
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
        analyzer.analyze(sl,analyzer.symbolTable,true);

        assertTrue(true);

        // incorrect input : type for arg "a" in copyPoints
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
                "var p Point = copyPoints(Point(10%3,len(\"hello\")),not(3.4<>1.2)); ";
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
                ()-> finalAnalyzer.analyze(finalSl, finalAnalyzer.symbolTable,true));
    }

    @Test
    public void testVarAssign() throws SemanticAnalyzerException {
        // correct input
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
        analyzer.analyze(sl,analyzer.symbolTable,true);

        assertTrue(true);

        // incorrect input : assign real instead of int
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
                ()-> finalAnalyzer.analyze(finalSl, finalAnalyzer.symbolTable,true));

    }

    @Test
    public void testFunctionDef() throws SemanticAnalyzerException {
        // correct input
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
        analyzer.analyze(sl,analyzer.symbolTable,true);

        assertTrue(true);

        // correct input
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
        analyzer.analyze(sl,analyzer.symbolTable,true);
        assertTrue(true);


        // Invalid input : void function has expr after return, parser will parse return (alone) then try to parse
        // the expr as statement, parser will throw error
        input = "proc myfunc(a int, b real[], c bool) void {" +
                "var aaa int[] = int[](7)" +
                "return a " +
                "}";
        reader = new StringReader(input);
        lexer = new Lexer(reader);
        parser = new Parser(lexer);

        boolean expThrown = false;
        try {
            sl = parser.parseCode();
        } catch (ParserException e) {
            expThrown = true;
        }
        assertTrue(expThrown);

        // incorrect input : returns nothing but it should return
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
                ()-> finalAnalyzer1.analyze(finalSl1, finalAnalyzer1.symbolTable,true));

        // incorrect input returns the wrong type
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
                ()-> finalAnalyzer2.analyze(finalSl2, finalAnalyzer2.symbolTable,true));

    }

    @Test
    public void testMathExpr() throws SemanticAnalyzerException {
        // correct input
        String input = "var a int = (1+4+7%6)/(1*2-4--7)+floor(7.5)+-len(\"a\")\n" +
                "var b real = (4.0+-11.2-4.6*7.0)/(47.9--7.1)";
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
        analyzer.analyze(sl,analyzer.symbolTable,true);

        assertTrue(true);

        // incorrect input : wrong math expression
        String[] inputs = {"var a int = (1+4+7%6)/(1*2-4--7)+floor(7.5)*6.5+-len(\"a\")","var b real = (4.0+-11.2-4.6*7.0)/(47.9--7.1)%4.9"};
        for (String str : inputs) {
            reader = new StringReader(str);
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
                    ()-> finalAnalyzer.analyze(finalSl, finalAnalyzer.symbolTable,true));
        }
    }

    @Test
    public void testComparison() throws SemanticAnalyzerException {
        // correct input
        String input = "\n" +
                "proc square(v int) int {\n" +
                "    return v*v;\n" +
                "}\n" +
                "\n" +
                "var a bool = true and false and not(false)\n" +
                "\n" +
                "var b bool = true == a or false <> not(a)\n" +
                "var c bool = 1+len(\"hello\")%78 <= 4*square(floor(4.7)) and chr(45) < \"hola\" or b";
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
        analyzer.analyze(sl,analyzer.symbolTable,true);

        assertTrue(true);

        // incorrect inputs
        String[] inputs = {"proc square(v float) float {\n" +
                "    return v*v;\n" +
                "}\n" +
                "\n" +
                "var a bool = true and false and not(false)\n" +
                "\n" +
                "var b bool = true == a or false <> not(a)\n" +
                "var c bool = 1+len(\"hello\")%78 <= 4.0*square(4.7) and chr(45) < \"hola\" or b",
                "proc square(v int) int {\n" +
                        "    return v*v;\n" +
                        "}\n" +
                        "\n" +
                        "var a bool = true and false and not(false)\n" +
                        "\n" +
                        "var b bool = true == a or false <> not(a)\n" +
                        "var c bool = 1+len(\"hello\")%78 <= 4*square(floor(4.7)) and 4.6/7.2 or b"};
        for (String str : inputs) {
            reader = new StringReader(str);
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
                    ()-> finalAnalyzer.analyze(finalSl, finalAnalyzer.symbolTable,true));
        }
    }

    @Test
    public void testScope() throws SemanticAnalyzerException {
        // correct input
        String input = "var x int = len(\"hello\")\n" +
                "val y int = square(x)\n" +
                "proc square(v int) int {\n" +
                "    return v*v;\n" +
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
        analyzer.analyze(sl,analyzer.symbolTable,true);

        assertTrue(true);

        // incorrect inputs
        String[] inputs = {"proc square(v int) int {\n" +
                "    return v*v;\n" +
                "}\n" +
                "\n" +
                "val y int = square(x)\n" +
                "\n" +
                "var x int = len(\"hello\")",
                "proc square(v int) int {\n" +
                        "    return v*v;\n" +
                        "}\n" +
                        "\n" +
                        "val y int = square(23)\n" +
                        "\n" +
                        "var x int = len(\"hello\")\n" +
                        "var y int = 41",
                "proc square(v int) int {\n" +
                        "    return v*v;\n" +
                        "}\n" +
                        "\n" +
                        "val y int = square(23)\n" +
                        "\n" +
                        "var x int = len(\"hello\")\n" +
                        "var square int = 41",
                "proc square(v int) int {\n" +
                        "    return v*v;\n" +
                        "}\n" +
                        "val squarE int = square(40)\n" +
                        "var x int = len(\"hello\")\n" +
                        "var write string = chr(12)\n"

        };

        for (String str : inputs) {
            reader = new StringReader(str);
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
                    ()-> finalAnalyzer.analyze(finalSl, finalAnalyzer.symbolTable,true));
        }
    }

    @Test public void testSymboleTable() throws IOException, SemanticAnalyzerException {
        // correct input
        String input = "var i int = 0; \n" +
                "var j int = 0;\n" +
                "var k int = 0;\n" +
                "\n" +
                "for i=0+1+len(\"hello\") to 10/2 by 1 {\n" +
                "    val x bool = not(true)\n" +
                "    for j=0 to 20 by 2*floor(3.2) {\n" +
                "        var y int = square(readInt())\n" +
                "        for k=3 to 30 by 1{\n" +
                "            val a string = chr(78);\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "proc square(v int) int {\n" +
                "    return v*v;\n" +
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
        analyzer.analyze(sl,analyzer.symbolTable,true);

        SymbolTable table = new SymbolTable();
        table.add("i",new ASTNodes.Type("int",false));
        table.add("j",new ASTNodes.Type("int",false));
        table.add("k",new ASTNodes.Type("int",false));
        table.add("square",new ASTNodes.Type("int",false));

        analyzer.symbolTable.remove("not");
        analyzer.symbolTable.remove("chr");
        analyzer.symbolTable.remove("floor");
        analyzer.symbolTable.remove("len");
        analyzer.symbolTable.remove("readInt");
        analyzer.symbolTable.remove("readReal");
        analyzer.symbolTable.remove("readString");
        analyzer.symbolTable.remove("writeInt");
        analyzer.symbolTable.remove("writeReal");
        analyzer.symbolTable.remove("write");
        analyzer.symbolTable.remove("writeln");
        assertTrue(table.table.equals(analyzer.symbolTable.table));
    }

    @Test public void testDoubleDefinition() throws IOException,SemanticAnalyzerException {
        // correct input
        String input = "\n" +
                "const a int = square(10)\n" +
                "val b bool = not(true)\n" +
                "var c real = 4.6*7.9+4.1\n" +
                "\n" +
                "proc square(v int) int {\n" +
                "    return v*v;\n" +
                "}\n" +
                "\n" +
                "val squarE int = square(40)\n" +
                "\n" +
                "var x int = len(\"hello\")";

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
        analyzer.analyze(sl,analyzer.symbolTable,true);

        // incorrect inputs
        String[] inputs = {"\n" +
                "const a int = square(10)\n" +
                "val b bool = not(true)\n" +
                "var c real = 4.6*7.9+4.1\n" +
                "\n" +
                "proc square(v int) int {\n" +
                "    return v*v;\n" +
                "}\n" +
                "\n" +
                "val squarE int = square(40)\n" +
                "\n" +
                "var x int = len(\"hello\")\n" +
                "\n" +
                "record square {\n" +
                "    width int\n" +
                "}",
                "proc square(v int) int {\n" +
                        "    return v*v;\n" +
                        "}\n" +
                        "val squarE int = square(40)\n" +
                        "var x int = len(\"hello\")\n" +
                        "proc write (str string) void {\n" +
                        "    //\n" +
                        "}"
        };
        for (String str : inputs) {
            reader = new StringReader(str);
            lexer = new Lexer(reader);
            parser = new Parser(lexer);
            try {
                sl = parser.parseCode();
            } catch (ParserException e) {
                throw new RuntimeException(e);
            }
            ASTNodes.StatementList finalSl = sl;
            assertThrows(SemanticAnalyzerException.class,
                    () -> new SemanticAnalyzer(finalSl));
        }
    }


    /* write the code you want in simple_code_copie.txt and run this test
       and check if the SemanticAnalyzer has the behaviour you expect based
       on your input code
     */
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
        analyzer.analyze(sl,analyzer.symbolTable,true);

        assertTrue(true);
    }


}
