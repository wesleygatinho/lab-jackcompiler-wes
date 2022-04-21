package br.ufma.ecp;

import java.security.DigestInputStream;

public class Parser {
    private byte[] input;
    private int current;
    
    public Parser (byte[] input) {
        this.input = input;
    }

    public void parse () {
        expr();
    }

    void expr() {
        digit();
        oper();
    }

    void oper () {
        if (peek() == '+') {
            match('+');
            digit();
            System.out.println("add");
            oper();
        } else if (peek() == '-') {
            match('-');
            digit();
            System.out.println("sub");
            oper();
        } else if (peek() == 0) {
            // vazio
        } else {
            throw new Error("syntax error");
        }
    }

    void digit () {
        if (Character.isDigit(peek())) {
            System.out.println(peek());
            match(peek());
        } else {
           throw new Error("syntax error");
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
