package lang.linker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lang.exception.LangLinkException;
import lang.token.Token;
import lang.token.TokenType;

public class Var {
    
    private static HashMap<String, Integer> typeMap;
    private static HashMap<String,Integer> varMap = new HashMap<String,Integer>();
    private static HashMap<String,List<String>> funcParamMap = new HashMap<String,List<String>>();
    private static HashMap<String,String> fieldTypeEqualMap;
    private static List<Token> tokens;
    private static int pos;
    private static boolean pointer;
    private static String funcName;
    private static String varName;
    private static String typeName;
    private static int emptyAddress;

    public static void getVars(List<Token> tokens, HashMap<String, Integer> typeMap, HashMap<String, String> fieldTypeEqualMap) throws LangLinkException {
        Var.tokens = tokens;
        Var.typeMap = typeMap;
        Var.fieldTypeEqualMap = fieldTypeEqualMap;
        pointer = false;
        funcName = "main";
        varName = "";
        typeName = "";
        pos = 0;
        emptyAddress = 3;
        while(!getToken().getType().equals(TokenType.EOF)){
            if(getToken().getType().equals(TokenType.SYS_VAR)){
                getVar();
                tokens.remove(pos);
            }else if(getToken().getType().equals(TokenType.SYS_FUNCTION)){
                funcName = "main";        
                getVar();
                funcName = varName;
                getFuncParams();
                tokens.add(pos, new Token(TokenType.LABEL, funcName));
                while(!getToken().getType().equals(TokenType.RPARENTHES)){
                    if(getToken().getType().equals(TokenType.SYS_VAR)){
                        getVar();
                        tokens.remove(pos);
                    }else{
                        nextToken();
                    }
                }
                funcName = "main";
            }else{
                nextToken();
            }
        }
        varMap.put("main.SP", 2);
        fieldTypeEqualMap.put("main.SP","pointer"); 
    }

    public static HashMap<String,Integer> getVarMap(){
        return varMap;
    }

    public static HashMap<String,List<String>> getFuncParamMap(){
        return funcParamMap;
    }

    public static void getVar() throws LangLinkException {
        tokens.remove(pos);
        if(getToken().getType().equals(TokenType.UPARROW)){
            pointer = true;
            tokens.remove(pos);
        }else{
            pointer = false;
        }
        typeName = tokens.remove(pos).getValue();
        checkType();
        varName = tokens.remove(pos).getValue();
        checkVarName();
        varMap.put(funcName+"."+varName, emptyAddress);
        if(pointer){
            fieldTypeEqualMap.put(funcName+"."+varName,"^"+typeName);
            fieldTypeEqualMap.put(funcName+"."+varName+"^",typeName);
            emptyAddress = emptyAddress + 1;
        }else{
            fieldTypeEqualMap.put(funcName+"."+varName,typeName);
            emptyAddress = emptyAddress + typeMap.get(typeName);
        }
    }

    public static int getEmptyAddress(){
        return emptyAddress;
    }

    private static void getFuncParams() throws LangLinkException {
        List<String> varListName = new ArrayList<>();
        tokens.remove(pos);
        while(!getToken().getType().equals(TokenType.RBRACKET)){
            getVar();
            varListName.add(varName);
            if(getToken().getType().equals(TokenType.COMMA)){
                tokens.remove(pos);
            }
        }
        tokens.remove(pos);
        funcParamMap.put(funcName, varListName);
    }

    private static void checkVarName() throws LangLinkException {
        if(typeMap.containsKey(varName)||varMap.containsKey(funcName+"."+varName)){
            throw new LangLinkException("Name: " + varName + " is already use.");
        }
    }

    private static void checkType() throws LangLinkException {
        if(!typeMap.containsKey(typeName)){
            throw new LangLinkException("Name: " + typeName + " is undefined.");
        }
    }

    private static void nextToken(){
        pos++;
    }

    private static Token getToken() {
        return tokens.get(pos);
    }

}