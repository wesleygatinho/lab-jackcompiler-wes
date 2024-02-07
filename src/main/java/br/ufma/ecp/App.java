package br.ufma.ecp;

import static br.ufma.ecp.token.TokenType.*;



import br.ufma.ecp.token.Token; 

public class App 
{

    
    public static void main( String[] args )
    {

    
        String input = """
            // Define uma função que retorna o n-ésimo número de Fibonacci.
            function int fibonacci(int n) {
            var int a, b, temp, i;
            let a = 0;
            let b = 1;
            if (n <= 0) {
                return 0;
            } else if (n == 1) {
                return 1;
            } else {
                for (let i = 2; i <= n; i = i + 1) {
                    let temp = b;
                    let b = a + b;
                    let a = temp;
                }
                return b;
            }
}

// Função principal que imprime os primeiros 10 números de Fibonacci.
function void main() {
    var int i;
    for (let i = 0; i < 10; i = i + 1) {
        do Output.printInt(fibonacci(i));
    }
}
                
                """;
        Scanner scan = new Scanner (input.getBytes());
        for (Token tk = scan.nextToken(); tk.type != EOF; tk = scan.nextToken()) {
            System.out.println(tk);
        }

        /*
        Parser p = new Parser (input.getBytes());
        p.parse();
        */


        //Parser p = new Parser (fromFile().getBytes());
        //p.parse();

        /*
        String input = "489-85+69";
        Scanner scan = new Scanner (input.getBytes());
        System.out.println(scan.nextToken());
        System.out.println(scan.nextToken());
        System.out.println(scan.nextToken());
        System.out.println(scan.nextToken());
        System.out.println(scan.nextToken());
        Token tk = new Token(NUMBER, "42");
        System.out.println(tk);
        */
    }
}
