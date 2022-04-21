package br.ufma.ecp;

import java.nio.charset.StandardCharsets;

public class Scanner {

    private byte[] input;
    private int current;
    private int start;
    
    public Scanner (byte[] input) {
        this.input = input;
        current = 0;
        start = 0;
    }

    public String nextToken () {
        start = current;
        char ch = peek();

        if (Character.isDigit(ch)) {
            return number();
        }

        switch (ch) {
            case '+':
                advance();
                return "+";
            default:
                break;
        }

        return null;
    }

    private String number() {
        while (Character.isDigit(peek())) {
            advance();
        }
        
            String n = new String(input, start, current-start, StandardCharsets.UTF_8)  ;
            return n;
    }

    private void advance()  {
        char ch = peek();
        if (ch != 0) {
            current++;
        }
    }
    

    private char peek () {
        if (current < input.length)
           return (char)input[current];
       return 0;
    }

    private void match (char c) {
        if (c == peek()) {
            current++;
        } else {
            throw new Error("syntax error");
        }
    }
    
}
