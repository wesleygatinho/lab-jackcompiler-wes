package br.ufma.ecp;

import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;

import static br.ufma.ecp.token.TokenType.*;

import br.ufma.ecp.VMWriter.Segment;

public class Parser {

    private static class ParseError extends RuntimeException {}
 
     private Scanner scan;
     private Token currentToken;
     private Token peekToken;
     private StringBuilder xmlOutput = new StringBuilder();
     private VMWriter vmWriter = new VMWriter();
 
     public Parser(byte[] input) {
         scan = new Scanner(input);
         nextToken();
     }
 
     private void nextToken() {
         currentToken = peekToken;
         peekToken = scan.nextToken();
     }
 
 
     public void parse () {
        printNonTerminal("class");
        expectPeek(CLASS);
        expectPeek(IDENT); 
        expectPeek(LBRACE);
        while (peekTokenIs(FIELD)) {
            parseClassVarDec();
        }
        parseSubroutineDec();
        expectPeek(RBRACE);
            
        printNonTerminal("/class");
     }
     

     void parseSubroutineDec() {
        
        while (peekTokenIs(CONSTRUCTOR)) {
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
        while (peekTokenIs(METHOD)) {
            printNonTerminal("subroutineDec");
            expectPeek(METHOD);
            expectPeek(VOID);
            while (peekTokenIs(IDENT)){
                expectPeek(IDENT);
            }
            
            expectPeek(LPAREN);
            parseParameterList();
            expectPeek(RPAREN);
            parseSubroutineBody();
            printNonTerminal("/subroutineDec");
        }
        
        
        
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
            if (peekTokenIs(VAR)) {
                parseVarDec();
            }
            parseStatements();
            expectPeek(RBRACE);
        }
        
        
        printNonTerminal("/subroutineBody");
    }

    void parsewhileStatement() {
        printNonTerminal("whileStatement");
        expectPeek(WHILE);
        expectPeek(LPAREN);
        parseExpression();
        expectPeek(RPAREN);
        expectPeek(LBRACE);
        parseStatements();
        expectPeek(RBRACE);
        printNonTerminal("/whileStatement");

    }

    void parseVarDec() {
        
        while (peekTokenIs(VAR)) {
            printNonTerminal("varDec");
            expectPeek(VAR);
            if (peekTokenIs(CHAR)) {
                expectPeek(CHAR);
                expectPeek(IDENT);
                expectPeek(SEMICOLON);
            } else if (peekTokenIs(BOOLEAN)) {
                expectPeek(BOOLEAN);
                expectPeek(IDENT);
                expectPeek(SEMICOLON);
            } else if (peekTokenIs(INT)) {
                expectPeek(INT);
                expectPeek(IDENT);
                expectPeek(SEMICOLON);
            }
            printNonTerminal("/varDec");
        }
        
    }

    void parseStatements() {
        printNonTerminal("statements");
    
        while (true) {
            if (peekTokenIs(LET)) {
                parseLet();
            } else if (peekTokenIs(DO)) {
                parseDo();
            } else if (peekTokenIs(RETURN)) {
                parseReturnStatement();
            } else if (peekTokenIs(IF)) {
                parseIf();
            } else if (peekTokenIs(WHILE)) {
                parsewhileStatement();
            } else {
                break; // Saia do loop se não houver mais tokens relevantes
            }
        }
    
        printNonTerminal("/statements");
    }
    

    void parseReturnStatement() {
        printNonTerminal("returnStatement");
        expectPeek(RETURN);
        if (!peekTokenIs(SEMICOLON)) {
            parseExpression();
            expectPeek(SEMICOLON);
        } else{
            expectPeek(SEMICOLON);
        }
        
        printNonTerminal("/returnStatement");
    }   
    
    
    
    void parseClassVarDec() {
        printNonTerminal("classVarDec");
        expectPeek(FIELD);
        if(peekTokenIs(IDENT)){
            expectPeek(IDENT);
            expectPeek(IDENT);
            expectPeek(SEMICOLON);
        } else if (peekTokenIs(INT)){
                expectPeek(INT);
                while (peekTokenIs(IDENT)) {
                    expectPeek(IDENT);
                    if (peekTokenIs(COMMA)) {
                        expectPeek(COMMA);
                    }
                }
            
            expectPeek(SEMICOLON);
        }
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
        while (true) {
            if (peekTokenIs(DO)) {
                parseDo();
            } else if (peekTokenIs(LET)) {
                parseLet();
            } else {
                break;
            }
        } 
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
        if (peekTokenIs(IDENT)){
            expectPeek(IDENT);
            expectPeek(LPAREN);
            expectPeek(RPAREN);
        } else if (peekTokenIs(LPAREN)) {
            expectPeek(LPAREN);
            parseExpressionList();
            expectPeek(RPAREN);
            
            
            
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
        

        if (!peekTokenIs(RPAREN)) {
            parseExpression();
        } //devemos continuar analisando as expressões na lista, mesmo que o próximo token não seja um parêntese de fechamento RPAREN

        while (peekTokenIs(COMMA)) {
            expectPeek(COMMA);
            parseExpression();
        }
        printNonTerminal("/expressionList"); 
     }

     void parseLet() {
        printNonTerminal("letStatement");
        if (peekTokenIs(LET)) {
            expectPeek(LET);
            expectPeek(IDENT);
        
            if (peekTokenIs(LBRACE)) {
                expectPeek(LBRACE);
                parseExpression();
                expectPeek(RBRACE);
            }
        
            expectPeek(EQ);
            parseExpression();
            expectPeek(SEMICOLON);
        }
  
        printNonTerminal("/letStatement");
    }

    

     void parseExpression() {
        printNonTerminal("expression");
        parseTerm ();

        while (isOperator(peekToken.lexeme)) {
            expectPeek(peekToken.type);
            parseTerm();
        }
        if (peekTokenIs(EQ)){
            expectPeek(EQ);
            parseTerm();
        }
        printNonTerminal("/expression");
  }


  void parseTerm() {
    printNonTerminal("term");
    
        switch (peekToken.type) {
            case NUMBER:
                expectPeek(TokenType.NUMBER);
                vmWriter.writePush(Segment.CONST, Integer.parseInt(currentToken.lexeme));
                break;
            case STRING:
                expectPeek(TokenType.STRING);
                break;
            case FALSE:
            case NULL:
            case TRUE:
                expectPeek(FALSE, NULL, TRUE);
                break;
            case THIS:
                expectPeek(THIS);
                break;
            case IDENT:
                expectPeek(IDENT);
                if (peekTokenIs(DOT)) {
                    expectPeek(DOT);
                    expectPeek(IDENT);
                }
                if (peekTokenIs(LPAREN)) {
                    expectPeek(LPAREN);
                    parseExpressionList();
                    expectPeek(RPAREN);
                }
                break;
            case RETURN:
                expectPeek(RETURN);
                parseReturnStatement();
                break;
            case LPAREN:
                expectPeek(LPAREN);
                parseExpression();
                expectPeek(RPAREN);
                break;
            case CLASS:
                expectPeek(CLASS);
                break;
            case FIELD:
                expectPeek(FIELD);
                break;
            case  METHOD:
                expectPeek(METHOD);
                break;
            case VOID:
                expectPeek(VOID);
                break;
            case VAR:
                expectPeek(VAR);
                break;
            case CHAR:
                expectPeek(CHAR);
                break;
            case BOOLEAN:
                expectPeek(BOOLEAN);
                break;
            case WHILE:
                expectPeek(WHILE);
                break;
            case NOT:
                expectPeek(TokenType.NOT);
                parseTerm();                
                break;
            default:
                throw error(peekToken, "term expected");
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

     public String VMOutput() {
        return vmWriter.vmOutput();
    }
 
 
 }