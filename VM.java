import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import lang.token.Token;
import lang.token.TokenType;

public class VM {
    
    private static LinkedHashMap<Integer,Token> programMemory;
    private static int PC;
    private static int MAR;
    private static Stack<Token> dataStack;
    private static Stack<Token> returnStack;

    private static String value_first;
    private static String value_second;
    private static String value_result;

    public static void run(){
        while(!getToken(programMemory,PC).getType().equals(TokenType.EOF)){
            switch(getToken(programMemory, PC).getType()){
                case PLUS:
                    value_first = dataStack.pop().getValue();
                    value_second = dataStack.pop().getValue();
                    try{
                        value_result = Integer.toString(Integer.valueOf(value_first) + Integer.valueOf(value_second));
                    }catch(NumberFormatException not_int){
                        value_result = Double.toString(Double.valueOf(value_first) + Double.valueOf(value_second));
                    }
                    dataStack.push(new Token(TokenType.NUMERIC, value_result));
                    PC++;
                    break;
                case MINUS:
                    value_first = dataStack.pop().getValue();
                    value_second = dataStack.pop().getValue();
                    try{
                        value_result = Integer.toString(Integer.valueOf(value_second) - Integer.valueOf(value_first));
                    }catch(NumberFormatException not_int){
                        value_result = Double.toString(Double.valueOf(value_second) - Double.valueOf(value_first));
                    }
                    dataStack.push(new Token(TokenType.NUMERIC, value_result));
                    PC++;
                    break;
                case STAR:
                    value_first = dataStack.pop().getValue();
                    value_second = dataStack.pop().getValue();
                    try{
                        value_result = Integer.toString(Integer.valueOf(value_second) * Integer.valueOf(value_first));
                    }catch(NumberFormatException not_int){
                        value_result = Double.toString(Double.valueOf(value_second) * Double.valueOf(value_first));
                    }
                    dataStack.push(new Token(TokenType.NUMERIC, value_result));
                    PC++;
                    break;
                case SLASH:
                    value_first = dataStack.pop().getValue();
                    value_second = dataStack.pop().getValue();
                    try{
                        value_result = Integer.toString(Integer.valueOf(value_second) / Integer.valueOf(value_first));
                    }catch(NumberFormatException not_int){
                        value_result = Double.toString(Double.valueOf(value_second) / Double.valueOf(value_first));
                    }
                    dataStack.push(new Token(TokenType.NUMERIC, value_result));
                    PC++;
                    break;
                case SYS_CALL:
                    PC++;
                    returnStack.push(new Token(TokenType.ADDRESS,Integer.toString(PC+1)));
                    PC = Integer.valueOf(getToken(programMemory, PC).getValue());
                    break;
                case SYS_EXIT:
                    PC = Integer.valueOf(returnStack.pop().getValue());
                    break;
                case SYS_IF:
                    PC++;
                    if(Integer.valueOf(dataStack.pop().getValue()) == 0){
                        PC = Integer.valueOf(getToken(programMemory, PC).getValue());
                    }else{
                        PC++;
                    }
                    break;
                case SYS_AND:
                    value_first = dataStack.pop().getValue();
                    value_second = dataStack.pop().getValue();
                    if(Integer.valueOf(value_first) > 0 && Integer.valueOf(value_second) > 0){
                        value_result = "1";
                    }else{
                        value_result = "0";
                    }
                    dataStack.push(new Token(TokenType.NUMERIC, value_result));
                    PC++;
                    break;
                case SYS_OR:
                    value_first = dataStack.pop().getValue();
                    value_second = dataStack.pop().getValue();
                    if(Integer.valueOf(value_first) > 0 || Integer.valueOf(value_second) > 0){
                        value_result = "1";
                    }else{
                        value_result = "0";
                    }
                    dataStack.push(new Token(TokenType.NUMERIC, value_result));
                    PC++;
                    break;
                case SYS_NOT:
                    value_first = dataStack.pop().getValue();
                    if(Integer.valueOf(value_first) == 0){
                        value_result = "1";
                    }else{
                        value_result = "0";
                    }
                    dataStack.push(new Token(TokenType.NUMERIC, value_result));
                    PC++;
                    break;
                case DOT:
                    value_first = dataStack.pop().getValue();
                    value_second = dataStack.pop().getValue();
                    value_result = Integer.toString(Integer.valueOf(value_first) + Integer.valueOf(value_second));
                    dataStack.push(new Token(TokenType.NUMERIC, value_result));
                    PC++;
                    break;
                case UPARROW:
                    value_first = dataStack.pop().getValue();
                    MAR = Integer.valueOf(value_first);
                    try{
                        value_result = getToken(programMemory, MAR).getValue();
                    }catch(NullPointerException not_init){
                        value_result = "0";
                    }
                    dataStack.push(new Token(TokenType.NUMERIC, value_result));
                    PC++;
                    break;
                case ASSIGNMENT:
                    value_first = dataStack.pop().getValue();
                    value_second = dataStack.pop().getValue();
                    MAR = Integer.valueOf(value_second);
                    try{
                        getToken(programMemory, MAR).setValue(value_first);
                    }catch(NullPointerException new_var){
                        programMemory.put(MAR, new Token(TokenType.EMPTY,value_first));
                    }
                    PC++;
                    break;
                case GE:
                    value_first = dataStack.pop().getValue();
                    value_second = dataStack.pop().getValue();
                    if(Integer.valueOf(value_second) >= Integer.valueOf(value_first)){
                        value_result = "1";
                    }else{
                        value_result = "0";
                    }
                    dataStack.push(new Token(TokenType.NUMERIC, value_result));
                    PC++;
                    break;
                case GT: 
                    value_first = dataStack.pop().getValue();
                    value_second = dataStack.pop().getValue();
                    if(Integer.valueOf(value_second) > Integer.valueOf(value_first)){
                        value_result = "1";
                    }else{
                        value_result = "0";
                    }
                    dataStack.push(new Token(TokenType.NUMERIC, value_result));
                    PC++;
                    break;
                case LE:
                    value_first = dataStack.pop().getValue();
                    value_second = dataStack.pop().getValue();
                    if(Integer.valueOf(value_second) <= Integer.valueOf(value_first)){
                        value_result = "1";
                    }else{
                        value_result = "0";
                    }
                    dataStack.push(new Token(TokenType.NUMERIC, value_result));
                    PC++;
                    break;
                case LT:
                    value_first = dataStack.pop().getValue();
                    value_second = dataStack.pop().getValue();
                    if(Integer.valueOf(value_second) < Integer.valueOf(value_first)){
                        value_result = "1";
                    }else{
                        value_result = "0";
                    }
                    dataStack.push(new Token(TokenType.NUMERIC, value_result));
                    PC++;
                    break;
                case EQUAL:
                    value_first = dataStack.pop().getValue();
                    value_second = dataStack.pop().getValue();
                    if(value_second.equals(value_first)){
                        value_result = "1";
                    }else{
                        value_result = "0";
                    }
                    dataStack.push(new Token(TokenType.NUMERIC, value_result));
                    PC++;
                    break;
                case NOTEQUAL:
                    value_first = dataStack.pop().getValue();
                    value_second = dataStack.pop().getValue();
                    if(!value_second.equals(value_first)){
                        value_result = "1";
                    }else{
                        value_result = "0";
                    }
                    dataStack.push(new Token(TokenType.NUMERIC, value_result));
                    PC++;
                    break;
                case UNARY_MINUS:
                    value_first = dataStack.pop().getValue();
                    value_result = "-"+value_first;
                    dataStack.push(new Token(TokenType.NUMERIC, value_result));
                    PC++;
                    break;
                case UNARY_PLUS:
                    PC++;
                    break;
                case POPFROMRETURNSTACK:
                    dataStack.push(returnStack.pop());
                    PC++;
                    break;
                case PUSHTORETURNSTACK:
                    returnStack.push(dataStack.pop());
                    PC++;
                    break;
                case DROP:
                    dataStack.pop();
                    PC++;
                    break;
                case SYS_PRINTLN:
                    System.out.println(dataStack.pop().getValue());
                    PC++;
                    break;
                default:
                    dataStack.push(getToken(programMemory, PC));
                    PC++;
                    break;
            }
        }
        System.out.println("Program end succesfuly.");
    }

    public static void loadProgram(List<Token> tokens){
        int i = 0;
        for (Token token : tokens) {
            programMemory.put(i, token);
            i++;
        }
    }

    public static void init(){
        programMemory = new LinkedHashMap<Integer,Token>();
        PC = 0;
        MAR = 0;
        dataStack = new Stack<>();
        returnStack = new Stack<>();
    }

    private static Token getToken(Map<Integer,Token> tokens, int pos){
        return tokens.get(pos);
    }
}