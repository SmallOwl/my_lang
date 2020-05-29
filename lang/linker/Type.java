package lang.linker;

import java.util.HashMap;
import java.util.List;

import lang.exception.LangLinkException;
import lang.token.Token;
import lang.token.TokenType;

public class Type {
    
    private static HashMap<String,Integer> typeMap = new HashMap<String,Integer>();
    private static HashMap<String,String> fieldTypeEqualMap = new HashMap<String,String>();
    private static List<Token> tokens;
    private static int pos;

    private static String name;
    private static int size;
    private static boolean pointer;
    private static String fieldName;
    private static Token fieldTypeToken;
    private static Integer fieldSize;

    public static void getTypes(List<Token> tokens) throws LangLinkException {
        Type.tokens = tokens;
        pointer = false;
        typeMap.put("int", 1);
        typeMap.put("pointer", 1);
        typeMap.put("void", 0);
        typeMap.put("double", 1);
        typeMap.put("string", 1);
        typeMap.put("boolean", 1);
        pos = 0;
        while(!getToken().getType().equals(TokenType.EOF)){
            if(getToken().getType().equals(TokenType.SYS_TYPE)){
                getTypeName();
                getFields();
                typeMap.put(name, size);
            }else{
                nextToken();
            }
        } 
    }

    public static HashMap<String,Integer> getTypeMap(){
        return typeMap;
    }

    public static HashMap<String,String> getFieldTypeEqualMap(){
        return fieldTypeEqualMap;
    }

    private static void getFields() throws LangLinkException {
        size = 0;
        while(!getToken().getType().equals(TokenType.RPARENTHES)){
            if(getToken().getType().equals(TokenType.UPARROW)){
                pointer = true;
                tokens.remove(pos);
            }else{
                pointer = false;
            }
            fieldTypeToken = tokens.remove(pos);
            fieldName = name + "." +tokens.remove(pos).getValue();
            if (typeMap.containsKey(fieldName)){
                throw new LangLinkException("Name: " + name + " is already used.");
            }
            if(pointer){
                fieldSize = 1;
                fieldTypeEqualMap.put(fieldName, "^"+fieldTypeToken.getValue());
                fieldTypeEqualMap.put(fieldName+"^", fieldTypeToken.getValue());
            }else{
                Integer typeTokenSize = typeMap.get(fieldTypeToken.getValue());
                if(typeTokenSize == null){
                    throw new LangLinkException("Type: " + fieldTypeToken.getValue() + " is undefined.");
                }
                fieldSize = typeTokenSize.intValue();
                fieldTypeEqualMap.put(fieldName, fieldTypeToken.getValue());
            }
            typeMap.put(fieldName, size);
            size = size + fieldSize;
            tokens.remove(pos);
        }
        tokens.remove(pos);
    }

    private static void getTypeName() throws LangLinkException {
        tokens.remove(pos);
        name = tokens.remove(pos).getValue();
        if(typeMap.containsKey(name)){
            throw new LangLinkException("Name: " + name + " is already used.");
        }
        tokens.remove(pos);
    }

    private static void nextToken(){
        pos++;
    }

    private static Token getToken() {
        return tokens.get(pos);
    }
}