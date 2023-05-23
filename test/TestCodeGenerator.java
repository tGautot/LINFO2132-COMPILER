import compiler.CodeGenerator.CodeGenerator;
import compiler.Lexer.*;
import compiler.SemanticAnalyzer.SemanticAnalyzer;
import compiler.SemanticAnalyzer.SemanticAnalyzerException;
import compiler.parser.ASTNodes;
import compiler.parser.Parser;
import compiler.parser.ParserException;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TestCodeGenerator {

    @Test
    public void testInt() throws IOException, SemanticAnalyzerException, ClassNotFoundException {
        Path filePath = Path.of("./test/test1CodeGenerator.txt");

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
        CodeGenerator generator = new CodeGenerator(sl);
        generator.generateCode("Main");

        List<Integer> expected = new ArrayList<>();
        expected.add(5);
        expected.add(5);
        expected.add(36);
        expected.add(-1);
        expected.add(1);
        expected.add(104);
        expected.add(-10);
        expected.add(8);

        String[] cmd = new String[] {"java", "Main"};
        Process proc = new ProcessBuilder(cmd).start();

        BufferedReader bf =
                new BufferedReader(new InputStreamReader(proc.getInputStream()));

        String line = "";
        int i = 0;
        while((line = bf.readLine()) != null) {
            System.out.print(line + "\n");
            assertTrue((Integer.parseInt(line)) == expected.get(i));
            i++;
        }

        assertEquals(i, expected.size());
    }

    @Test
    public void testString() throws IOException, SemanticAnalyzerException, ClassNotFoundException {
        Path filePath = Path.of("./test/test2CodeGenerator.txt");

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
        CodeGenerator generator = new CodeGenerator(sl);
        generator.generateCode("Main");

        List<String> expected = new ArrayList<>();
        expected.add("Test");
        expected.add("Squared");
        expected.add("NegPoint");
        expected.add("A");
        expected.add("else");
        expected.add("else");

        String[] cmd = new String[] {"java", "Main"};
        Process proc = new ProcessBuilder(cmd).start();

        BufferedReader bf =
                new BufferedReader(new InputStreamReader(proc.getInputStream()));

        String line = "";
        int i = 0;
        while((line = bf.readLine()) != null) {
            System.out.print(line + "\n");
            assertTrue(line.equals(expected.get(i)));
            i++;
        }

        assertEquals(i, expected.size());
    }


    @Test
    public void testnDimArray() throws IOException, SemanticAnalyzerException, ClassNotFoundException {
        Path filePath = Path.of("./test/nDimArray.txt");

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
        CodeGenerator generator = new CodeGenerator(sl);
        generator.generateCode("Main");

        List<String> expected = new ArrayList<>();
        for(int i = 0; i < 5 ; i++){
            for(int j = 0; j < 6; j++){
                expected.add(String.valueOf(i));
                expected.add(String.valueOf(j));
                expected.add(String.valueOf(i*6+j));
            }
        }

        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                for(int k = 0; k < 3; k++) {
                    expected.add(String.valueOf(i-1));
                    expected.add(String.valueOf(j-1));
                    expected.add(String.valueOf(k-1));
                }
            }
        }

        String[] cmd = new String[] {"java", "Main"};
        Process proc = new ProcessBuilder(cmd).start();

        BufferedReader bf =
                new BufferedReader(new InputStreamReader(proc.getInputStream()));

        String line = "";
        int i = 0;
        while((line = bf.readLine()) != null) {
            System.out.print(line + "\n");
            assertTrue(line.equals(expected.get(i)));
            i++;
        }
        System.out.println(i);
        assertEquals(i, expected.size());
    }


}
