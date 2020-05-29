package lang.token;

public class Token {

    private TokenType type;

    private String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public void  setType(TokenType type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

}