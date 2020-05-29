package lang.linker.rpn;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import lang.token.Token;
import lang.token.TokenType;

public class RPN {

    private static List<Token> tokens;
    private static List<Token> tokenList = new ArrayList<>();
    private static Stack<Token> opStack = new Stack<>();
    private static boolean plusMinusPrefixFlag;
    private static int pos;

    public static void getRPN(List<Token> tokens){
        RPN.tokens = tokens;
        tokenList.clear();
        opStack.clear();
        pos = 0;
        plusMinusPrefixFlag = false;
        while(tokens.size() > pos){
            if(getToken().getType().equals(TokenType.NORPN)){
                nextToken();
                while(!getToken().getType().equals(TokenType.NORPN)){
                    tokenList.add(tokens.remove(pos));
                }
            }else if(getPriorityAndType(getToken()) == -1){
                operandFind();
                plusMinusPrefixFlag = false;
                
            }else{
                operationFind();
                if(!getToken().getType().equals(TokenType.UPARROW)){
                    plusMinusPrefixFlag = true;
                }
            }
            nextToken();
        }
        pushFromStack();
    }

    public static List<Token> getTokenList() {
        return tokenList;
    }

    private static void operandFind() {
        tokenList.add(getToken());
    }

    private static void operationFind(){
        if(getPriorityAndType(getToken()) == 7){
            if(getToken().getType().equals(TokenType.PLUS)){
                getToken().setType(TokenType.UNARY_PLUS);
            }else if(getToken().getType().equals(TokenType.MINUS)){
                getToken().setType(TokenType.UNARY_MINUS);
            }
            plusMinusPrefixFlag = false;
        }
        if(getToken().getType().equals(TokenType.RBRACKET)){
            pushFromStack();
        }else if(opStack.empty() || getToken().getType().equals(TokenType.LBRACKET) || (getPriorityAndType(getToken()) > getPriorityAndType(opStack.peek()))){
            opStack.push(getToken());
        }else{
            do{
                if(!opStack.peek().getType().equals(TokenType.SEMICOLON)){
                    tokenList.add(opStack.pop());
                }else{
                    opStack.pop();
                }
            }while(!opStack.empty() && (getPriorityAndType(getToken()) <= getPriorityAndType(opStack.peek())));
            if(!getToken().getType().equals(TokenType.SEMICOLON)){
                opStack.push(getToken());
            }
        }
    }

    private static void pushFromStack() {
        while(!opStack.empty() && !opStack.peek().getType().equals(TokenType.LBRACKET)){
            if(!opStack.peek().getType().equals(TokenType.SEMICOLON)){
                tokenList.add(opStack.pop());
            }else{
                opStack.pop();
            }
        }
        if(!opStack.empty()){
            opStack.pop();
            if(!opStack.empty() && (opStack.peek().getType().equals(TokenType.SYS_IF)||opStack.peek().getType().equals(TokenType.SYS_WHILE))){
                tokenList.add(opStack.pop());
            }
        }
    }

    private static int getPriorityAndType(Token value) {
        switch(value.getType()){
            case LBRACKET, SYS_IF, SYS_WHILE, SEMICOLON, SYS_PRINTLN: return 0;
            case RBRACKET, ASSIGNMENT, SYS_ELSE: return 1;
            case SYS_OR: return 2;
            case SYS_AND: return 3;
            case LE,LT,GE,GT,EQUAL,NOTEQUAL: return 4;
            case PLUS, MINUS: if(plusMinusPrefixFlag){return 7;}else{return 5;}
            case STAR, SLASH: return 6;
            case SYS_NOT, UNARY_MINUS, UNARY_PLUS: return 7;
            case DOG: return 8;
            case UPARROW, DOT: return 9;
            default: return -1;
        }
    }

    private static void nextToken(){
        pos++;
    }

    private static Token getToken() {
        return tokens.get(pos);
    }
}