package lang.parser;

import java.util.List;

import lang.exception.LangParseException;
import lang.token.Token;
import lang.token.TokenType;

public class Parser{

    private static int pos;

    private static List<Token> tokens;

    //program                -> using_description type_description var_description function_description do_block EOF
    //using_description      -> (SYS_USING CHARACTER_STRING SEMICOLON using_description)|empty
    //type_description       -> (SYS_TYPE IDENTIFIER LPARENTHES type_field_description RPARENTHES type_description)|empty
    //type_field_description -> ((UPARROW IDENTIFIER)|IDENTIFIER IDENTIFIER SEMICOLON type_field_description)|empty
    //var_description        -> (SYS_VAR UPARROW{0,1} IDENTIFIER IDENTIFIER SEMICOLON var_description)|empty
    //function_description   -> (SYS_FUNCTION (UPARROW IDENTIFIER)|IDENTIFIER IDENTIFIER params_description 
    //                          do_block function_description)|empty
    //params_description     -> LBRACKET (SYS_VAR UPARROW{0,1} IDENTIFIER IDENTIFIER coma_param_description)|empty RBRACKET
    //coma_param_description -> (COMMA SYS_VAR UPARROW{0,1} IDENTIFIER IDENTIFIER coma_param_description)|empty
    //do_block               -> (LPARENTHES var_description term RPARENTHES)|empty
    //term                   -> (SYS_IF if_description term)|
    //                          (SYS_WHILE while_description term)|
    //                          (IDENTIFIER func_var_use term)|
    //                          (SYS_PRINTLN println_operand term)|
    //                          (SYS_RETURN return_block term)|
    //                          empty
    //if_description         -> LBRACKET expression RBRACKET do_block else_if_description
    //else_if_description    -> (SYS_ELSE (SYS_IF if_description)|(do_block))|empty
    //while_description      -> LBRACKET expression RBRACKET do_block
    //println_operand        -> LBRACKET expression RBRACKET SEMICOLON
    //return_block           -> SEMICOLON|(expression SEMICOLON)
    //func_var_use           -> (LBRACKET RBRACKET|(expression coma_expression RBRACKET) SEMICOLON)|(var_use ASSIGNMENT expression SEMICOLON)
    //var_use                -> (DOT IDENTIFIER var_use)|(UPARROW var_use)|empty
    //expression             -> operand operator_operand
    //operator_operand       -> (operator operand operator_operand)|empty
    //operator               -> SYS_AND|SYS_OR|PLUS|MINUS|STAR|SLASH|EQUAL|GT|GE|LT|LE|NOTEQUAL
    //operand                -> operand_prefix operand_part
    //operand_prefix         -> PLUS|MINUS|SYS_NOT|empty
    //operand_part           -> (NUMERIC double_part)|CHARACTER_STRING|BOOLEAN|
    //                          (DOG IDENTIFIER var_use)|
    //                          (IDENTIFIER (LBRACKET RBRACKET|(expression coma_expression RBRACKET))|var_use)|
    //                          (LBRACKET expression RBRACKET)
    //coma_expression        -> (COMMA expression coma_expression)|empty
    //double_part            -> (DOT NUMERIC)|empty

    public static void parse(List<Token> tokenlist) throws LangParseException {
        tokens = tokenlist;
        pos = 0;
        program();
    }

    private static void program() throws LangParseException{
        using_description();
        type_description();
        var_description();
        function_description();
        do_block();
        matchToken(getToken(), TokenType.EOF);
    }

    private static void using_description() throws LangParseException {
        try{
            matchToken(getToken(), TokenType.SYS_USING);
        }catch(LangParseException not_using){
            return;
        }
        matchToken(getToken(), TokenType.CHARACTER_STRING);
        matchToken(getToken(), TokenType.SEMICOLON);
        using_description();
    }

    private static void type_description() throws LangParseException {
        try{
            matchToken(getToken(), TokenType.SYS_TYPE);
        }catch(LangParseException not_type){
            return;
        }
        matchToken(getToken(), TokenType.IDENTIFIER);
        matchToken(getToken(), TokenType.LPARENTHES);
        type_field_description();
        matchToken(getToken(), TokenType.RPARENTHES);
        type_description();
    }

    private static void type_field_description() throws LangParseException {
        try{
            matchToken(getToken(), TokenType.UPARROW);
        }catch(LangParseException not_uparrow){
            try{
                matchToken(getToken(), TokenType.IDENTIFIER);
            }catch(LangParseException not_identifire){
                return;
            }
            matchToken(getToken(), TokenType.IDENTIFIER);
            matchToken(getToken(), TokenType.SEMICOLON);
            type_field_description();
            return;
        }
        matchToken(getToken(), TokenType.IDENTIFIER);
        matchToken(getToken(), TokenType.IDENTIFIER);
        matchToken(getToken(), TokenType.SEMICOLON);
        type_field_description();
    }

    private static void var_description() throws LangParseException {
        try{
            matchToken(getToken(), TokenType.SYS_VAR);
        }catch(LangParseException not_var){
            return;
        }
        try{
            matchToken(getToken(), TokenType.UPARROW);
        }catch(LangParseException not_uparrow){}
        matchToken(getToken(), TokenType.IDENTIFIER);
        matchToken(getToken(), TokenType.IDENTIFIER);
        matchToken(getToken(), TokenType.SEMICOLON);
        var_description();
    }

    private static void function_description() throws LangParseException {
        try{
            matchToken(getToken(), TokenType.SYS_FUNCTION);
        }catch(LangParseException not_function){
            return;
        }
        try{
            matchToken(getToken(), TokenType.UPARROW);
        }catch(LangParseException not_uparrow){
        }
        matchToken(getToken(), TokenType.IDENTIFIER);
        matchToken(getToken(), TokenType.IDENTIFIER);
        params_description();
        do_block();
        function_description();
    }

    private static void params_description() throws LangParseException {
        matchToken(getToken(), TokenType.LBRACKET);
        try{
            matchToken(getToken(), TokenType.SYS_VAR);
        }catch(LangParseException no_param){
            matchToken(getToken(), TokenType.RBRACKET);
            return;
        }
        try{
            matchToken(getToken(), TokenType.UPARROW);
        }catch(LangParseException not_uparrow){}
        matchToken(getToken(), TokenType.IDENTIFIER);
        matchToken(getToken(), TokenType.IDENTIFIER);
        coma_param_description();
        matchToken(getToken(), TokenType.RBRACKET);
    }

    private static void coma_param_description() throws LangParseException {
        try{
            matchToken(getToken(), TokenType.COMMA);
        }catch(LangParseException not_coma){
            return;
        }
        matchToken(getToken(), TokenType.SYS_VAR);
        try{
            matchToken(getToken(), TokenType.UPARROW);
        }catch(LangParseException not_uparrow){}
        matchToken(getToken(), TokenType.IDENTIFIER);
        matchToken(getToken(), TokenType.IDENTIFIER);
        coma_param_description();
    }

    private static void do_block() throws LangParseException {
        try{
            matchToken(getToken(), TokenType.LPARENTHES);
        }catch(LangParseException not_lparanthes){
            return;
        }
        var_description();
        term();
        matchToken(getToken(), TokenType.RPARENTHES);
    }

    private static void term() throws LangParseException {
        try{
            matchToken(getToken(), TokenType.SYS_IF);
        }catch(LangParseException not_if){
            try{
                matchToken(getToken(), TokenType.SYS_WHILE);
            }catch(LangParseException not_while){
                try{
                    matchToken(getToken(), TokenType.IDENTIFIER);
                }catch(LangParseException not_identifier){
                    try{
                        matchToken(getToken(), TokenType.SYS_PRINTLN);
                    }catch(LangParseException not_println){
                        try{
                            matchToken(getToken(), TokenType.SYS_RETURN);
                        }catch(LangParseException not_return){
                            return;                       
                        }
                        return_block();
                        term();
                        return;
                    }
                    println_operand();
                    term();
                    return;
                }
                func_var_use();
                term();
                return;
            }
            while_description();
            term();
            return;
        }
        if_description();
        term();
        return;
    }

    private static void if_description() throws LangParseException {
        matchToken(getToken(), TokenType.LBRACKET);
        expression();
        matchToken(getToken(), TokenType.RBRACKET);
        do_block();
        else_if_description();
    }

    private static void else_if_description() throws LangParseException {
        try{
            matchToken(getToken(), TokenType.SYS_ELSE);
        }catch(LangParseException not_else){
            return;
        }
        try{
            matchToken(getToken(), TokenType.SYS_IF);
        }catch(LangParseException not_if){
            do_block();
            return;
        }
        if_description();
    }

    private static void while_description() throws LangParseException {
        matchToken(getToken(), TokenType.LBRACKET);
        expression();
        matchToken(getToken(), TokenType.RBRACKET);
        do_block();
    }

    private static void println_operand() throws LangParseException {
        matchToken(getToken(), TokenType.LBRACKET);
        expression();
        matchToken(getToken(), TokenType.RBRACKET);
        matchToken(getToken(), TokenType.SEMICOLON);
    }

    private static void return_block() throws LangParseException {
        try{
            matchToken(getToken(), TokenType.SEMICOLON);
        }catch(LangParseException not_semicolom){
            expression();
            matchToken(getToken(), TokenType.SEMICOLON);
        }
    }

    private static void func_var_use() throws LangParseException {
        try{
            matchToken(getToken(), TokenType.LBRACKET);
        }catch(LangParseException not_lbracket){
            var_use();
            matchToken(getToken(), TokenType.ASSIGNMENT);
            expression();
            matchToken(getToken(), TokenType.SEMICOLON);
            return;
        }
        try{
            matchToken(getToken(), TokenType.RBRACKET);
        }catch(LangParseException not_rbracket){
            expression();
            coma_expression();
            matchToken(getToken(), TokenType.RBRACKET);
        }
        matchToken(getToken(), TokenType.SEMICOLON);
    }

    private static void var_use() throws LangParseException {
        try{
            matchToken(getToken(), TokenType.DOT);
        }catch(LangParseException not_dot){
            try{
                matchToken(getToken(), TokenType.UPARROW);
            }catch(LangParseException not_uparrow){
                return;
            }
            var_use();
            return;
        }
        matchToken(getToken(), TokenType.IDENTIFIER);
        var_use();
    }

    private static void expression() throws LangParseException {
        operand();
        operator_operand();
    }

    private static void operator_operand() throws LangParseException {
        try{
            operator();
        }catch(LangParseException not_operator){
            return;
        }
        operand();
        operator_operand();
    }

    private static void operator() throws LangParseException {
        try{
            matchToken(getToken(), TokenType.SYS_AND);
        }catch(LangParseException not_and){
            try{
                matchToken(getToken(), TokenType.SYS_OR);
            }catch(LangParseException not_or){
                try{
                    matchToken(getToken(), TokenType.PLUS);
                }catch(LangParseException not_plus){
                    try{
                        matchToken(getToken(), TokenType.MINUS);
                    }catch(LangParseException not_minus){
                        try{
                            matchToken(getToken(), TokenType.STAR);
                        }catch(LangParseException not_star){
                            try{
                                matchToken(getToken(), TokenType.SLASH);
                            }catch(LangParseException not_slash){
                                try{
                                    matchToken(getToken(), TokenType.EQUAL);
                                }catch(LangParseException not_equal){
                                    try{
                                        matchToken(getToken(), TokenType.GT);
                                    }catch(LangParseException not_gt){
                                        try{
                                            matchToken(getToken(), TokenType.GE);
                                        }catch(LangParseException not_ge){
                                            try{
                                                matchToken(getToken(), TokenType.LT);
                                            }catch(LangParseException not_lt){
                                                try{
                                                    matchToken(getToken(), TokenType.LE);
                                                }catch(LangParseException not_le){
                                                    matchToken(getToken(), TokenType.NOTEQUAL);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void operand() throws LangParseException {
        operand_prefix();
        operand_part();
    }

    private static void operand_prefix(){
        try{
            matchToken(getToken(), TokenType.PLUS);
        }catch(LangParseException not_plus){
            try{
                matchToken(getToken(), TokenType.MINUS);
            }catch(LangParseException not_minus){
                try{
                    matchToken(getToken(), TokenType.SYS_NOT);
                }catch(LangParseException not_not){
                    return;
                }
            }
        }
    }

    private static void operand_part() throws LangParseException {
        try{
            matchToken(getToken(), TokenType.NUMERIC);
        }catch(LangParseException not_numeric){
            try{
                matchToken(getToken(), TokenType.CHARACTER_STRING);
            }catch(LangParseException not_character_string){
                try{
                    matchToken(getToken(), TokenType.BOOLEAN);
                }catch(LangParseException not_boolean){
                    try{
                        matchToken(getToken(), TokenType.DOG);
                    }catch(LangParseException not_dog){
                        try{
                            matchToken(getToken(), TokenType.IDENTIFIER);
                        }catch(LangParseException not_identifier){
                            
                                matchToken(getToken(), TokenType.LBRACKET);
                                expression();
                                matchToken(getToken(), TokenType.RBRACKET);
                                return;
                           
                        }
                        try{
                            matchToken(getToken(), TokenType.LBRACKET);
                        }catch(LangParseException not_lbracket){
                            var_use();
                            return;
                        }
                        try{
                            matchToken(getToken(), TokenType.RBRACKET);
                        }catch(LangParseException not_rbracket){
                            expression();
                            coma_expression();
                            matchToken(getToken(), TokenType.RBRACKET);
                            return;
                        }
                        return;
                    }
                    matchToken(getToken(), TokenType.IDENTIFIER);
                    var_use();
                    return;
                }
                return;
            }
            return;
        }
        double_part();
    }

    private static void coma_expression() throws LangParseException {
        try{
            matchToken(getToken(), TokenType.COMMA);
        }catch(LangParseException not_comma){
            return;
        }
        expression();
        coma_expression();
    }

    private static void double_part() throws LangParseException {
        try{
            matchToken(getToken(), TokenType.DOT);
        }catch(LangParseException not_dot){
            return;
        }
        matchToken(getToken(), TokenType.NUMERIC);
    }

    private static void matchToken(Token token, TokenType type) throws LangParseException {
        if (!token.getType().equals(type)) {
            throw new LangParseException(type
                    + " expected but "
                    + token.getType().name() + ": " + token.getValue()
                    + " found");
        }
        nextToken();
    }

    private static Token getToken(){
        return tokens.get(pos); 
    }

    private static void nextToken(){
        pos++;
    }
}