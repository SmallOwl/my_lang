import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import lang.exception.LangLexException;
import lang.exception.LangLinkException;
import lang.exception.LangParseException;
import lang.lexer.Lexer;
import lang.linker.Linker;
import lang.loader.Loader;
import lang.parser.Parser;
import lang.token.Token;

public class Main {

    public static void main(String[] args) throws LangParseException, LangLexException, IOException, LangLinkException,
            CloneNotSupportedException {
        // System.out.println("Start lang UI");
        // Scanner in = new Scanner(System.in);
        // System.out.print("Input a path to source: ");
        // String program = readProgram(in.nextLine());

        List<Token> tokens = Lexer.tokenize(readProgram("test/test"));
        Parser.parse(tokens);
        tokens = new Loader(tokens, "test/test", true, new ArrayList<>()).Load();
        tokens = Linker.linkAll(tokens);

        VM.init();
        VM.loadProgram(tokens);
        VM.run();
        // in.close();
    }

    private static String readProgram(String path) throws IOException {
        String value = "";
        FileReader fr = new FileReader(path);
        Scanner scan = new Scanner(fr);
        while(scan.hasNextLine()){
            value += scan.nextLine();
        }
        scan.close();
        fr.close();
        return value;
    }
}