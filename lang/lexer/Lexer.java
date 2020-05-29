package lang.lexer;

import java.util.ArrayList;
import java.util.List;

import lang.exception.LangLexException;
import lang.token.Token;
import lang.token.TokenType;



public class Lexer {
    
    private static String input;
    private static int length;
    
    private static List<Token> tokens;
    
    private static int pos;
    private static TokenType type;
    private static String value;
    
    public static List<Token> tokenize(String input_string) throws LangLexException {
        input = input_string;
        pos = 0;
        length = input.length();
        tokens = new ArrayList<>();
        while (pos < length) {
            searchforToken();
            if (type == null) {
                throw new LangLexException("Unknow token found: " + peek());
            }
            searchfullToken();
            if (!type.equals(TokenType.SPACE)){
                addToken(type, value);
            }
            pos++;
        }
        addToken(TokenType.EOF, "\0");
        return tokens;
    }
    
    private static void searchforToken() {
        value = peek();
        type = TokenType.searchType(value);
        while (type == null && pos < length-1){
            pos++;
            value += peek();
            type = TokenType.searchType(value);
        }
    }

    private static void searchfullToken() {
        if(pos < length-1){
            pos++;
            TokenType checktype = TokenType.searchType(value + peek());
            while (checktype != null && pos < length-1){
                value += peek();
                pos++;
                checktype = TokenType.searchType(value + peek());
            }
            if (checktype != null) {
                value += peek();
                type = checktype;
            } else {
                type = TokenType.searchType(value);
                pos--;
            } 
        }
    }

    private static String peek() {
        return Character.toString(input.charAt(pos));
    }
    
    private static void addToken(TokenType type, String text) {
        tokens.add(new Token(type, text));
    }
}
