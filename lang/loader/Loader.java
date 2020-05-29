package lang.loader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import lang.exception.LangLexException;
import lang.exception.LangParseException;
import lang.lexer.Lexer;
import lang.parser.Parser;
import lang.token.Token;
import lang.token.TokenType;

public class Loader {

    private int pos;
    private String root_path;
    private boolean main;

    private List<Token> inputTokens;
    private List<Token> childrenTokens = new ArrayList<>();
    private List<Token> resultTokens = new ArrayList<>();
    private List<String> sources = new ArrayList<>();
    private List<String> sourceListForCheck;

    public Loader(List<Token> tokenlist, String root_path, boolean main, List<String> sourceListForCheck) {
        this.inputTokens = tokenlist;
        this.root_path = root_path;
        this.main = main;
        this.sourceListForCheck = sourceListForCheck;
    }

    //Добавить проверку одинаковых ресурсов

    public List<Token> Load() throws LangParseException, LangLexException, IOException {
        while(getToken().getType().equals(TokenType.SYS_USING)){
            nextToken();
            if(!sourceListForCheck.contains(getPath(root_path))){
                sources.add(getPath(root_path));   
                sourceListForCheck.add(getPath(root_path));
            }
            nextToken();
            nextToken();
        }
        while(!getToken().getType().equals(TokenType.EOF)){
            resultTokens.add(getToken());
            nextToken();
        }
        for (String source : sources) {
            childrenTokens = Lexer.tokenize(readProgram(source));
            Parser.parse(childrenTokens);
            childrenTokens = new Loader(childrenTokens, source, false, sourceListForCheck).Load();
            for (Token token : childrenTokens) {
                resultTokens.add(token);
            }
        }
        if(main){
            resultTokens.add(getToken());
        }
        return resultTokens;
    }

    private String readProgram(String path) throws IOException {
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

    private void nextToken(){
        pos++;
    }

    private Token getToken(){
        return inputTokens.get(pos);
    }

    private String getPath(String root_path){
        String path = getToken().getValue();
        path = path.substring(1, path.length()-1);
        return root_path.substring(0, root_path.lastIndexOf("/")+1).concat(path);
    }
}