package br.ufma.ecp;

import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;

import static br.ufma.ecp.token.TokenType.*;

import javax.swing.text.Segment;

public class Parser {

    private static class ParseError extends RuntimeException {}
 
     private Scanner scan;
     private Token currentToken;
     private Token peekToken;
     private StringBuilder xmlOutput = new StringBuilder();
 
     public Parser(byte[] input) {
         scan = new Scanner(input);
         nextToken();
     }
 
     private void nextToken() {
         currentToken = peekToken;
         peekToken = scan.nextToken();
     }
 
 
     public void parse () {
         
     }

     void parseSubroutineDec() {
        printNonTerminal("subroutineDec");
        expectPeek(CONSTRUCTOR);
        while (peekTokenIs(IDENT)){
            expectPeek(IDENT);
        }
        
        expectPeek(LPAREN);
        parseParameterList();
        expectPeek(RPAREN);
        parseSubroutineBody();
        
        printNonTerminal("/subroutineDec");
     }

     

     void  parseParameterList() {
        printNonTerminal("parameterList");
        
        while (peekTokenIs(INT)) {
            expectPeek(INT);
            expectPeek(IDENT);
            
            if (peekTokenIs(COMMA)) {
                expectPeek(COMMA);
            }

        }            
        
        printNonTerminal("/parameterList");
    }

    void parseSubroutineBody() {
        printNonTerminal("subroutineBody");
        if (peekTokenIs(LBRACE)) {
            expectPeek(LBRACE);
            parseStatements();
            expectPeek(RBRACE);
        } 
        
        printNonTerminal("/subroutineBody");
    }

    void parseStatements() {
        printNonTerminal("statements");
        while (peekTokenIs(LET)) {
            parseLet();
        }
        parseDo();
        parseReturnStatement();
        printNonTerminal("/statements");
    }

    void parseReturnStatement() {
        printNonTerminal("returnstatement");
        expectPeek(RETURN);
        if (!peekTokenIs(SEMICOLON)) {
            parseExpression();
            expectPeek(SEMICOLON);
        }
        
        printNonTerminal("/returnstatement");
    }   
    
    
    
    void parseClassVarDec() {
        printNonTerminal("classVarDec");
        expectPeek(FIELD);
        expectPeek(IDENT);
        expectPeek(IDENT);
        expectPeek(SEMICOLON);
        printNonTerminal("/classVarDec");
     }

     
     
     
     
     
     void parseIf() {
        printNonTerminal("ifStatement");
        expectPeek(IF);
        expectPeek(LPAREN);
        parseExpression();
        expectPeek(RPAREN);
        expectPeek(LBRACE);
        printNonTerminal("statements");
        parseDo(); 
        printNonTerminal("/statements");
        expectPeek(RBRACE);
        printNonTerminal("/ifStatement");
    }


     void parseDo() {
        printNonTerminal("doStatement");
        expectPeek(DO);
        expectPeek(IDENT);
        parseSubroutineCall();
        expectPeek(SEMICOLON);
        printNonTerminal("/doStatement");
     }

     void parseSubroutineCall() {
        if (peekTokenIs(LPAREN)) {
            expectPeek(LPAREN);
            expectPeek(RPAREN);
            expectPeek(SEMICOLON); // muito estranho
            parseExpressionList();
            
            
        } else {
            expectPeek(DOT);
            expectPeek(IDENT);
            expectPeek(LPAREN);
            parseExpressionList();
            expectPeek(RPAREN);
        }
     }

     void parseExpressionList() {
        printNonTerminal("expressionList");
        parseExpression();

        /*if (!peekTokenIs(RPAREN)) {
            parseExpression();
        }*/ //devemos continuar analisando as expressões na lista, mesmo que o próximo token não seja um parêntese de fechamento RPAREN

        while (peekTokenIs(COMMA)) {
            expectPeek(COMMA);
            parseExpression();
        }
        printNonTerminal("/expressionList"); 
     }

     void parseLet() {
        printNonTerminal("letStatement");
        expectPeek(LET);
        expectPeek(IDENT);
    
        if (peekTokenIs(LBRACKET)) {
            expectPeek(LBRACKET);
            parseExpression();
            expectPeek(RBRACKET);
        }
    
        expectPeek(EQ);
        parseExpression();
        expectPeek(SEMICOLON);
        printNonTerminal("/letStatement");
    }

    

     void parseExpression() {
        printNonTerminal("expression");
        parseTerm ();
        while (isOperator(peekToken.lexeme)) {
            expectPeek(peekToken.type);
            parseTerm();
        }
        printNonTerminal("/expression");
  }


  void parseTerm() {
    printNonTerminal("term");

    
    if (peekTokenIs(IDENT)) {  
        expectPeek(IDENT);  
              
        if (peekTokenIs(DOT)){
            expectPeek(DOT);
            expectPeek(IDENT);
        }
        
        
        /*if (peekTokenIs(LPAREN)) {
            expectPeek(LPAREN);
            parseExpressionList();
            expectPeek(RPAREN);
        }*/
    } else {
        switch (peekToken.type) {
            case NUMBER:
                expectPeek(TokenType.NUMBER);
                break;
            case STRING:
                expectPeek(TokenType.STRING);
                break;
            case FALSE:
            case NULL:
            case TRUE:
                expectPeek(TokenType.FALSE, TokenType.NULL, TokenType.TRUE);
                break;
            case THIS:
                expectPeek(THIS);
                break;
            case IDENT:
                expectPeek(IDENT);
                break;
            case RETURN:
                parseReturnStatement();
                break;
            case LPAREN:
                expectPeek(LPAREN);
                parseExpression();
                expectPeek(RPAREN);
                break;
            default:
                throw error(peekToken, "term expected");
        }
    }

    printNonTerminal("/term");
}
 
     // funções auxiliares

     static public boolean isOperator(String op) {
        return op!= "" && "+-*/<>=~&|".contains(op);
   }


     public String XMLOutput() {
         return xmlOutput.toString();
     }
 
     private void printNonTerminal(String nterminal) {
         xmlOutput.append(String.format("<%s>\r\n", nterminal));
     }
 
 
     boolean peekTokenIs(TokenType type) {
         return peekToken.type == type;
     }
 
     boolean currentTokenIs(TokenType type) {
         return currentToken.type == type;
     }
 
     private void expectPeek(TokenType... types) {
         for (TokenType type : types) {
             if (peekToken.type == type) {
                 expectPeek(type);
                 return;
             }
         }
 
        throw error(peekToken, "Expected a statement");
 
     }
 
     private void expectPeek(TokenType type) {
         if (peekToken.type == type) {
             nextToken();
             xmlOutput.append(String.format("%s\r\n", currentToken.toString()));
         } else {
             throw error(peekToken, "Expected "+type.name());
         }
     }
 
 
     private static void report(int line, String where,
         String message) {
             System.err.println(
             "[line " + line + "] Error" + where + ": " + message);
     }
 
 
     private ParseError error(Token token, String message) {
         if (token.type == TokenType.EOF) {
             report(token.line, " at end", message);
         } else {
             report(token.line, " at '" + token.lexeme + "'", message);
         }
         return new ParseError();
     }
 
 
 }