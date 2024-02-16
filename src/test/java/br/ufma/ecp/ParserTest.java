package br.ufma.ecp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;



public class ParserTest extends TestSupport {


    @Test
    public void testSimple () {
        String input = "45  + if + \"ola mundo\" - 876";
        Scanner scan = new Scanner (input.getBytes());
        for (Token tk = scan.nextToken(); tk.type != TokenType.EOF; tk = scan.nextToken()) {
            System.out.println(tk);
        }
    }
    
    @Test
    public void testScannerWithSquareGame() throws IOException {
        var input = fromFile("Square/SquareGame.jack");
        var expectedResult =  fromFile("Square/SquareGameT.xml");

        var scanner = new Scanner(input.getBytes(StandardCharsets.UTF_8));
        var result = new StringBuilder();
        
        result.append("<tokens>\r\n");

        for (Token tk = scanner.nextToken(); tk.type !=TokenType.EOF; tk = scanner.nextToken()) {
            result.append(String.format("%s\r\n",tk.toString()));
        }

        result.append("</tokens>\r\n");
        
        assertEquals(expectedResult, result.toString());
    }


    @Test
    public void testScannerWithSquare() throws IOException {
        var input = fromFile("Square/Square.jack");
        var expectedResult =  fromFile("Square/SquareT.xml");

        var scanner = new Scanner(input.getBytes(StandardCharsets.UTF_8));
        var result = new StringBuilder();
        
        result.append("<tokens>\r\n");

        for (Token tk = scanner.nextToken(); tk.type !=TokenType.EOF; tk = scanner.nextToken()) {
            result.append(String.format("%s\r\n",tk.toString()));
        }
        
        result.append("</tokens>\r\n");
        System.out.println(result.toString());
        assertEquals(expectedResult, result.toString());
    }

    @Test
    public void testParseExpressionSimple() {
        var input = "10+20";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression();
        
        var expectedResult =  """
          <expression>
          <term>
          <integerConstant> 10 </integerConstant>
          </term>
          <symbol> + </symbol>
          <term>
          <integerConstant> 20 </integerConstant>
          </term>
          </expression>
          """;
              
          var result = parser.XMLOutput();
          result = result.replaceAll("\r", ""); 
          expectedResult = expectedResult.replaceAll("  ", "");
          assertEquals(expectedResult, result);    

    }

    @Test
    public void testParseLetSimple() {
        var input = "let var1 = 10+20;";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseLet();
				var expectedResult =  """
	     <letStatement>
        <keyword> let </keyword>
        <identifier> var1 </identifier>
        <symbol> = </symbol>
        <expression>
          <term>
          <integerConstant> 10 </integerConstant>
          </term>
          <symbol> + </symbol>
          <term>
          <integerConstant> 20 </integerConstant>
          </term>
          </expression>
        <symbol> ; </symbol>
      </letStatement> 
				""";
        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);
    }

    @Test
    public void testParseSubroutineCall() {
        var input = "hello()";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseSubroutineCall();
        
        var expectedResult =  """
          <identifier> hello </identifier>
          <symbol> ( </symbol>
          <symbol> ) </symbol>
          """;
              
          var result = parser.XMLOutput();
          result = result.replaceAll("\r", ""); 
          expectedResult = expectedResult.replaceAll("  ", "");
          assertEquals(expectedResult, result);    

    }

    @Test
    public void testParseDoSimples() {
        var input = "do hello();";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseDo();

        var expectedResult = """
            <doStatement>
            <keyword> do </keyword>
            <identifier> hello </identifier>
            <symbol> ( </symbol>
            <symbol> ) </symbol>
            <symbol> ; </symbol>
          </doStatement>
                """;
        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);
    }

    @Test
    public void testParseDo() {
        var input = "do Sys.wait(5);";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseDo();

        var expectedResult = """
            <doStatement>
            <keyword> do </keyword>
            <identifier> Sys </identifier>
            <symbol> . </symbol>
            <identifier> wait </identifier>
            <symbol> ( </symbol>
            <expressionList>
              <expression>
                <term>
                  <integerConstant> 5 </integerConstant>
                </term>
              </expression>
            </expressionList>
            <symbol> ) </symbol>
            <symbol> ; </symbol>
          </doStatement>
                """;
        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);
    }


    @Test
    public void testParseIf() {
        var input = "if (direction = 1) { do square.moveUp(); }";
        var expectedResult = """
            <ifStatement>
            <keyword> if </keyword>
            <symbol> ( </symbol>
            <expression>
              <term>
                <identifier> direction </identifier>
              </term>
              <symbol> = </symbol>
              <term>
                <integerConstant> 1 </integerConstant>
              </term>
            </expression>
            <symbol> ) </symbol>
            <symbol> { </symbol>
            <statements>
              <doStatement>
                <keyword> do </keyword>
                <identifier> square </identifier>
                <symbol> . </symbol>
                <identifier> moveUp </identifier>
                <symbol> ( </symbol>
                <expressionList>
                </expressionList>
                <symbol> ) </symbol>
                <symbol> ; </symbol>
              </doStatement>
            </statements>
            <symbol> } </symbol>
          </ifStatement>
                """;

        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseIf();
        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);
    }


    @Test
    public void testParseLet() {
        var input = "let square = Square.new(0, 0, 30);";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseLet();
        var expectedResult =  """
        <letStatement>
        <keyword> let </keyword>
        <identifier> square </identifier>
        <symbol> = </symbol>
        <expression>
          <term>
            <identifier> Square </identifier>
            <symbol> . </symbol>
            <identifier> new </identifier>
            <symbol> ( </symbol>
            <expressionList>
              <expression>
                <term>
                  <integerConstant> 0 </integerConstant>
                </term>
              </expression>
              <symbol> , </symbol>
              <expression>
                <term>
                  <integerConstant> 0 </integerConstant>
                </term>
              </expression>
              <symbol> , </symbol>
              <expression>
                <term>
                  <integerConstant> 30 </integerConstant>
                </term>
              </expression>
            </expressionList>
            <symbol> ) </symbol>
          </term>
        </expression>
        <symbol> ; </symbol>
      </letStatement>
      """;
        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);
    }


    
    
}
