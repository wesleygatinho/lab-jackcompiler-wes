package br.ufma.ecp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;



public class CodeGeneration extends TestSupport {
    
    
    @Test
    public void testInt () {
        var input = """
            10
            """;
        
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression();
        String actual = parser.VMOutput();
        String expected = """
                push constant 10       
                    """;
            assertEquals(expected, actual);
    }

    
    @Test
    public void testSimpleExpression () {
        var input = """
            10 + 30 + 40
            """;
        
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression();
        String actual = parser.VMOutput();
        String expected = """
                push constant 10
                push constant 30
                add
                push constant 40
                add       
                    """;
            assertEquals(expected, actual);
    }

    @Test
    public void testLiteralString () {
        var input = """
            "OLA"
            """;
        
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression();
        String actual = parser.VMOutput();
        String expected = """
                push constant 3
                call String.new 1
                push constant 79
                call String.appendChar 2
                push constant 76
                call String.appendChar 2
                push constant 65
                call String.appendChar 2
                    """;
            assertEquals(expected, actual);
    }

    
}