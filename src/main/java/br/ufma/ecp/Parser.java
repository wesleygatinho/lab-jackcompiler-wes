package br.ufma.ecp;

import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;

import static br.ufma.ecp.token.TokenType.*;

import br.ufma.ecp.VMWriter.Command;
import br.ufma.ecp.VMWriter.Segment;

import static br.ufma.ecp.VMWriter.*;

public class Parser {

    private static class ParseError extends RuntimeException {}
 
     private Scanner scan;
     private Token currentToken;
     private Token peekToken;
     private StringBuilder xmlOutput = new StringBuilder();
     private VMWriter vmWriter = new VMWriter();

     private int ifLabelNum = 0;
     private int whileLabelNum = 0;
 
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
            ifLabelNum = 0;
            whileLabelNum = 0;
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
            vmWriter.writePush(Segment.CONST, 0);
            expectPeek(SEMICOLON);
        }
        vmWriter.writeReturn();

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

        var labelTrue  = "IF_TRUE" + ifLabelNum;
        var labelFalse = "IF_FALSE" + ifLabelNum;
        var labelEnd = "IF_END" + ifLabelNum;

        ifLabelNum++;


        expectPeek(IF);
        expectPeek(LPAREN);
        parseExpression();
        expectPeek(RPAREN);

        vmWriter.writeIf(labelTrue);
        vmWriter.writeGoto(labelFalse);
        vmWriter.writeLabel(labelTrue);


        expectPeek(LBRACE);
        parseStatements();
        
        while (true) {
            if (peekTokenIs(DO)) {
                parseDo();
            } else if (peekTokenIs(LET)) {
                parseLet();
            } else {
                break;
            }
        } 
        
        expectPeek(RBRACE);

        if (peekTokenIs(ELSE)){
            vmWriter.writeGoto(labelEnd);
        }

        vmWriter.writeLabel(labelFalse);

        if (peekTokenIs(ELSE))
        {
            expectPeek(ELSE);
            expectPeek(LBRACE);
            parseStatements();
            expectPeek(RBRACE);
            vmWriter.writeLabel(labelEnd);
        }

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
            var ope = peekToken.type;
            expectPeek(peekToken.type);
            parseTerm();
            compileOperators(ope);
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
                var strValue = currentToken.lexeme;
                vmWriter.writePush(Segment.CONST, strValue.length());
                vmWriter.writeCall("String.new", 1);
                for (int i = 0; i < strValue.length(); i++) {
                    vmWriter.writePush(Segment.CONST, strValue.charAt(i));
                    vmWriter.writeCall("String.appendChar", 2);
                }
                break;
            case FALSE:
            case NULL:
            case TRUE:
                expectPeek(FALSE, NULL, TRUE);
                vmWriter.writePush(Segment.CONST, 0);
                if (currentToken.type == TRUE)
                    vmWriter.writeArithmetic(Command.NOT);
                break;
            case THIS:
                expectPeek(THIS);
                vmWriter.writePush(Segment.POINTER, 0);
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
            case MINUS:
            case NOT:
                expectPeek(MINUS, NOT);
                var op = currentToken.type;
                parseTerm();               
                if (op == MINUS) 
                    vmWriter.writeArithmetic(Command.NEG);
                else
                    vmWriter.writeArithmetic(Command.NOT);
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

     public void compileOperators(TokenType type) {

        if (type == ASTERISK) {
            vmWriter.writeCall("Math.multiply", 2);
        } else if (type == SLASH) {
            vmWriter.writeCall("Math.divide", 2);
        } else {
            vmWriter.writeArithmetic(typeOperator(type));
        }
    }

    private Command typeOperator(TokenType type) {
        if (type == PLUS)
            return Command.ADD;
        if (type == MINUS)
            return Command.SUB;
        if (type == LT)
            return Command.LT;
        if (type == GT)
            return Command.GT;
        if (type == EQ)
            return Command.EQ;
        if (type == AND)
            return Command.AND;
        if (type == OR)
            return Command.OR;
        return null;
    }

     public String VMOutput() {
        return vmWriter.vmOutput();
    }

    
 
 
 }