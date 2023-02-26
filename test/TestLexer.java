import static org.junit.Assert.assertNotNull;

import compiler.Lexer.Symbol;
import org.junit.Test;

import java.io.StringReader;
import compiler.Lexer.Lexer;

public class TestLexer {
    
    @Test
    public void test() {
        String input = "var x int = 2;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Symbol nxtSymbol = lexer.getNextSymbol();
        System.out.println(nxtSymbol.toString());
        assertNotNull(nxtSymbol);

    }

}
