package lang.token;

import java.util.regex.Pattern;

public enum TokenType {

    //Список токенов
    //Зарезервированные системой слова
    LABEL("label"),
    SYS_CALL("call"),
    SYS_USING("using"),
    SYS_TYPE("type"),
    SYS_VAR("var"),
    SYS_MAIN("main"),
    SYS_FUNCTION("function"),
    SYS_PRINTLN("println"),
    SYS_RETURN("return"),
    SYS_BREAK("break"),
    SYS_EXIT("exit"),
    SYS_IF("if"),
    SYS_ELSE("else"),
    SYS_WHILE("while"),
    SYS_AND("&&"),
    SYS_OR("||"),
    SYS_NOT("not"),
    //Возможные значения данных
    BOOLEAN("true|false"),
    CHARACTER_STRING("'.*'"),
    NUMERIC("0|([1-9][0-9]*)"),
    //Символы
    LPARENTHES("\\{"),
    RPARENTHES("\\}"),
    LBRACKET("\\("),
    RBRACKET("\\)"),
    DOT("\\."),
    COMMA(","),
    SEMICOLON(";"),
    //Символы операций
    ASSIGNMENT(":="),
    EQUAL("="),
    NOTEQUAL("!="),
    GE(">="),
    GT(">"),
    LE("<="),
    LT("<"),
    MINUS("-"),
    PLUS("\\+"),
    SLASH("/"),
    STAR("\\*"),
    DOG("@"),
    UPARROW("\\^"),
    //Идентификатор
    IDENTIFIER("[A-Za-z_]+"),
    //Пробельные символы, а ткаже символ конца файла
    SPACE("( |\t)+"),
    EOF("\0"),
    //Токены, используемые программой
    UNARY_PLUS("\\+"),
    UNARY_MINUS("-"),
    ADDRESS("0|([1-9][0-9]*)"),
    OFFSET("0|([1-9][0-9]*)"),
    POPFROMRETURNSTACK("R>"),
    PUSHTORETURNSTACK(">R"),
    DROP("DROP"),
    NORPN("//"),
    EMPTY("");


    private Pattern pattern;

    TokenType(String regexp) {
        this.pattern = Pattern.compile(regexp);
    }

    public static TokenType searchType(String value){
        for (TokenType tokenvalue : TokenType.values()) {
            if(tokenvalue.pattern.matcher(value).matches()){
                return tokenvalue;
            }
        }
        return null;
    }
}