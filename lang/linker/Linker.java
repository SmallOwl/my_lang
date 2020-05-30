package lang.linker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lang.exception.LangLinkException;
import lang.linker.rpn.RPN;
import lang.token.Token;
import lang.token.TokenType;

public class Linker {
    
    private static HashMap<String, Integer> typeMap;
    private static HashMap<String, Integer> varMap;
    private static HashMap<String, List<String>> funcParamMap;
    private static HashMap<String, String> fieldTypeEqualMap;
    private static HashMap<String, String> funcAddress = new HashMap<String, String>();

    private static boolean address;
    private static boolean pointer;

    private static int SEMICOLONCounter;
    private static int NORPNCounter;
    private static int NORPNDeleteCounter;
    private static int BRACKETCounter;

    private static String exprType;
    private static String exprTypePrev;

    public static List<Token> linkAll(List<Token> tokens) throws LangLinkException {
        Type.getTypes(tokens);
        typeMap = Type.getTypeMap();
        fieldTypeEqualMap = Type.getFieldTypeEqualMap();
        Var.getVars(tokens, typeMap, fieldTypeEqualMap);
        varMap = Var.getVarMap();
        funcParamMap = Var.getFuncParamMap();
        tokens = getMemoryForVars(tokens);
        tokens = linkAllTypes(tokens);
        tokens = linkProgram(tokens);
        RPN.getRPN(tokens);
        tokens = RPN.getTokenList();
        return tokens;
    }

    private static List<Token> linkProgram(List<Token> tokens) throws LangLinkException {
        int pos = 0;
        SEMICOLONCounter = 0;
        Token callEnd = new Token(TokenType.SYS_CALL,"call");
        Token posEnd = new Token(TokenType.ADDRESS,"");
        while(!getTokenByPos(tokens,pos).getType().equals(TokenType.EOF)){
            switch(getTokenByPos(tokens,pos).getType()){
                case EMPTY:
                    pos++;
                    break;
                case LABEL:
                    String funcName = tokens.remove(pos).getValue();
                    funcAddress.put(funcName, Integer.toString(pos - SEMICOLONCounter - NORPNDeleteCounter-BRACKETCounter));
                    pos = linkDoBlock(tokens, pos, funcName);
                    tokens.add(pos, new Token(TokenType.SYS_EXIT,"exit"));
                    pos++;
                    break;
                case LPARENTHES:
                    getTokenByPos(tokens, 1).setType(TokenType.ADDRESS);
                    getTokenByPos(tokens, 1).setValue(Integer.toString(pos - SEMICOLONCounter - NORPNDeleteCounter-BRACKETCounter));
                    pos = linkDoBlock(tokens, pos, "main");
                    tokens.add(pos, callEnd);
                    pos++;
                    tokens.add(pos, posEnd);
                    pos++;
                    break;
                default:
                    break;
            }
        }
        posEnd.setValue(Integer.toString(pos - SEMICOLONCounter - NORPNDeleteCounter-BRACKETCounter));
        getTokenByPos(tokens, 0).setType(TokenType.SYS_CALL);
        getTokenByPos(tokens, 0).setValue("call");
        if(getTokenByPos(tokens, 1).getType().equals(TokenType.EMPTY)){
            getTokenByPos(tokens, 1).setType(TokenType.ADDRESS);
            getTokenByPos(tokens, 1).setValue(Integer.toString(pos - SEMICOLONCounter - NORPNDeleteCounter-BRACKETCounter));
        }
        getTokenByPos(tokens, 2).setType(TokenType.ADDRESS);
        getTokenByPos(tokens, 2).setValue(Integer.toString(pos - SEMICOLONCounter - NORPNDeleteCounter-BRACKETCounter+1));
        linkAllFuncIdentifier(tokens);
        return tokens;
    }

    private static void linkAllFuncIdentifier(List<Token> tokens) {
        for (Token token : tokens) {
            if(token.getType().equals(TokenType.IDENTIFIER)){
                token.setType(TokenType.ADDRESS);
                token.setValue(funcAddress.get(token.getValue()));
            }
        }
    }

    private static int linkDoBlock(List<Token> tokens, int pos, String funcName) throws LangLinkException {
        tokens.remove(pos);
        pos = linkTerm(tokens, pos, funcName);
        tokens.remove(pos);
        return pos;
    }

    private static int linkTerm(List<Token> tokens, int pos, String funcName) throws LangLinkException {
        switch(getTokenByPos(tokens,pos).getType()){
            case SYS_IF:
                pos = linkIf(tokens,pos, funcName);
                pos = linkTerm(tokens,pos, funcName);
                break;
            case SYS_WHILE:
                pos = linkWhile(tokens,pos, funcName);
                pos = linkTerm(tokens,pos, funcName);
                break;
            case IDENTIFIER:
                pos = linkIdentifierTerm(tokens,pos, funcName);
                pos = linkTerm(tokens,pos, funcName);
                break;
            case SYS_PRINTLN:
                pos = linkPrintln(tokens,pos, funcName);
                pos = linkTerm(tokens,pos, funcName);
                break;
            case SYS_RETURN:
                pos = linkReturn(tokens,pos, funcName);
                pos = linkTerm(tokens,pos, funcName);
                break;
            default:
                break;
        }
        return pos;
    }

    private static int linkReturn(List<Token> tokens, int pos, String funcName) throws LangLinkException {
        tokens.remove(pos);
        if(!fieldTypeEqualMap.get("main."+funcName).equals("void") && !getTokenByPos(tokens, pos).getType().equals(TokenType.SEMICOLON)){
            List<Token> valueSave = new ArrayList<>();
            List<Token> varSave = new ArrayList<>();
            varSave.add(new Token(TokenType.IDENTIFIER,funcName));
            while(!getTokenByPos(tokens, pos).getType().equals(TokenType.SEMICOLON)){
                valueSave.add(tokens.remove(pos));
            }
            SEMICOLONCounter++;
            pos++;
            List<Token> bufferList = assignVar(varSave, valueSave, funcName, funcName);
            tokens.addAll(pos, bufferList);
            pos = pos + bufferList.size();
        }else if(!fieldTypeEqualMap.get("main."+funcName).equals("void") && getTokenByPos(tokens, pos).getType().equals(TokenType.SEMICOLON)){
            throw new LangLinkException("No return parameter in function: " + funcName);
        }else if(fieldTypeEqualMap.get("main."+funcName).equals("void") && !getTokenByPos(tokens, pos).getType().equals(TokenType.SEMICOLON)){
            throw new LangLinkException("Function: " + funcName + " has type void.");
        }else{
            SEMICOLONCounter++;
            pos++;
        }
        
        tokens.add(pos, new Token(TokenType.SYS_EXIT,"exit"));
        return pos+1;
    }

    private static int linkIdentifierTerm(List<Token> tokens, int pos, String funcName) throws LangLinkException {
        if(tokens.size() > pos+1 && getTokenByPos(tokens, pos+1).getType().equals(TokenType.LBRACKET)){
            pos = linkVarFuncUse(tokens,pos,funcName);
            pos--;
            pos--;
            tokens.remove(pos);
            tokens.remove(pos);
            pos++;
            SEMICOLONCounter++;
        }else{
            List<Token> varSave = new ArrayList<>();
            List<Token> valueSave = new ArrayList<>();
            while(!getTokenByPos(tokens, pos).getType().equals(TokenType.ASSIGNMENT)){
                varSave.add(tokens.remove(pos));
            }
            tokens.remove(pos);
            while(!getTokenByPos(tokens, pos).getType().equals(TokenType.SEMICOLON)){
                valueSave.add(tokens.remove(pos));
            }
            tokens.remove(pos);
            List<Token> bufferList = assignVar(varSave, valueSave, funcName, funcName);
            tokens.addAll(pos, bufferList);
            pos = pos + bufferList.size();
        }
        return pos;
    }

    private static int linkPrintln(List<Token> tokens, int pos, String funcName) throws LangLinkException {
        pos++;
        tokens.remove(pos);
        pos = linkExpression(tokens, pos, funcName);
        checkConditionType();
        tokens.remove(pos);
        pos++;
        SEMICOLONCounter++;
        return pos;
    }

    private static int linkWhile(List<Token> tokens, int pos, String funcName) throws LangLinkException {
        tokens.add(pos, new Token(TokenType.NUMERIC, "0"));
        pos++;
        tokens.add(pos, new Token(TokenType.PUSHTORETURNSTACK, ">R"));
        pos++;
        int posCycle = pos - SEMICOLONCounter - NORPNDeleteCounter-BRACKETCounter;
        int posDump;
        getTokenByPos(tokens,pos).setType(TokenType.SYS_IF);
        getTokenByPos(tokens,pos).setValue("if");
        pos++;
        pos++;
        BRACKETCounter++;
        pos = linkExpression(tokens, pos, funcName);
        BRACKETCounter++;
        checkConditionType();
        pos++;
        tokens.add(pos, new Token(TokenType.ADDRESS, ""));
        posDump = pos;
        pos++;
        tokens.add(pos, new Token(TokenType.POPFROMRETURNSTACK,"R>"));
        pos++;
        tokens.add(pos, new Token(TokenType.DROP,"DROP"));
        pos++;
        pos = linkDoBlock(tokens, pos, funcName);
        tokens.add(pos, new Token(TokenType.SYS_CALL,"call"));
        pos++;
        tokens.add(pos, new Token(TokenType.ADDRESS,Integer.toString(posCycle)));
        pos++;
        getTokenByPos(tokens,posDump).setValue(Integer.toString(pos - SEMICOLONCounter - NORPNDeleteCounter-BRACKETCounter));
        //Не точно. Проверить в работе количество заталкиваний и выталкиваний returnStack
        tokens.add(pos, new Token(TokenType.POPFROMRETURNSTACK,"R>"));
        pos++;
        tokens.add(pos, new Token(TokenType.DROP,"DROP"));
        pos++;
        //
        return pos;
    }

    private static void checkConditionType() throws LangLinkException {
        if(!exprType.contains("^") && typeMap.get(exprType)!=1){
            throw new LangLinkException("You can use only base types and pointer in conditions.");
        }
    }

    private static int linkIf(List<Token> tokens, int pos, String funcName) throws LangLinkException {
        int posDump;
        pos++;
        pos++;
        BRACKETCounter++;
        pos = linkExpression(tokens, pos, funcName);
        BRACKETCounter++;
        checkConditionType();
        pos++;
        tokens.add(pos, new Token(TokenType.ADDRESS, ""));
        posDump = pos;
        pos++;
        pos = linkDoBlock(tokens, pos, funcName);
        if(getTokenByPos(tokens,pos).getType().equals(TokenType.SYS_ELSE)){
            Token call = new Token(TokenType.SYS_CALL,"call");
            Token addressOut = new Token(TokenType.ADDRESS,"");
            while(getTokenByPos(tokens, pos).getType().equals(TokenType.SYS_ELSE)){
                tokens.remove(pos);
                tokens.add(pos, call);
                pos++;
                tokens.add(pos, addressOut);
                pos++;
                getTokenByPos(tokens,posDump).setValue(Integer.toString(pos - SEMICOLONCounter - NORPNDeleteCounter-BRACKETCounter));
                if(getTokenByPos(tokens,pos).getType().equals(TokenType.SYS_IF)){
                    pos++;
                    pos++;
                    BRACKETCounter++;
                    pos = linkExpression(tokens, pos, funcName);
                    BRACKETCounter++;
                    checkConditionType();
                    pos++;
                    tokens.add(pos, new Token(TokenType.ADDRESS, ""));
                    posDump = pos;
                    pos++;
                }
                pos = linkDoBlock(tokens,pos, funcName);
            }
            tokens.add(pos, call);
            pos++;
            tokens.add(pos, addressOut);
            pos++;
            addressOut.setValue(Integer.toString(pos - SEMICOLONCounter - NORPNDeleteCounter-BRACKETCounter));
            tokens.add(pos, new Token(TokenType.POPFROMRETURNSTACK,"R>"));
            pos++;
            tokens.add(pos, new Token(TokenType.DROP,"DROP"));
            pos++;
        }else{
            getTokenByPos(tokens,posDump).setValue(Integer.toString(pos - SEMICOLONCounter - NORPNDeleteCounter-BRACKETCounter));
        }
        return pos;
    }

    private static int linkExpression(List<Token> tokens, int pos, String funcName) throws LangLinkException {
        pos = linkOperand(tokens, pos, funcName);
        exprTypePrev = exprType;
        pos = linkOperatorOperand(tokens, pos, funcName);
        return pos;
    }

    private static void checkExprType() throws LangLinkException {
        if(!exprTypePrev.equals("pointer") && !exprType.equals("int") && !exprType.equals(exprTypePrev)){
            throw new LangLinkException("Different operand types in expression");
        }
    }

    //Добавить проверку типов для операций
    private static int linkOperatorOperand(List<Token> tokens, int pos, String funcName) throws LangLinkException {
        if(tokens.size() > pos && (getTokenByPos(tokens,pos).getType().equals(TokenType.SYS_AND)||getTokenByPos(tokens,pos).getType().equals(TokenType.SYS_OR)||getTokenByPos(tokens,pos).getType().equals(TokenType.PLUS)||
        getTokenByPos(tokens,pos).getType().equals(TokenType.MINUS)||getTokenByPos(tokens,pos).getType().equals(TokenType.STAR)||getTokenByPos(tokens,pos).getType().equals(TokenType.SLASH)||
        getTokenByPos(tokens,pos).getType().equals(TokenType.EQUAL)||getTokenByPos(tokens,pos).getType().equals(TokenType.GT)||getTokenByPos(tokens,pos).getType().equals(TokenType.GE)||
        getTokenByPos(tokens,pos).getType().equals(TokenType.LT)||getTokenByPos(tokens,pos).getType().equals(TokenType.LE)||getTokenByPos(tokens,pos).getType().equals(TokenType.NOTEQUAL))){
            pos++;
            pos = linkOperand(tokens, pos, funcName);
            checkExprType();
            pos = linkOperatorOperand(tokens, pos, funcName);
        }
        return pos;
    }

    private static int linkOperand(List<Token> tokens, int pos, String funcName) throws LangLinkException {
        pos = linkOperandPrefix(tokens,pos);
        pos = linkOperandPart(tokens, pos, funcName);
        return pos;
    }

    private static int linkOperandPart(List<Token> tokens, int pos, String funcName) throws LangLinkException {
        switch(getTokenByPos(tokens, pos).getType()){
            case NUMERIC:
                pos = linkNumeric(tokens, pos);
                break;
            case CHARACTER_STRING:
                pos = linkCharacterString(tokens,pos);
                break;
            case BOOLEAN:
                pos = linkBoolean(tokens,pos);
                break;
            case DOG:
                pointer = true;
            case IDENTIFIER:
                pos = linkVarFuncUse(tokens,pos,funcName);
                break;
            case LBRACKET:
                pos++;
                BRACKETCounter++;
                pos = linkExpression(tokens,pos, funcName);
                BRACKETCounter++;
                pos++;
                break;
            default:
                break;
        }
        return pos;
    }

    private static int linkVarFuncUse(List<Token> tokens, int pos, String funcName) throws LangLinkException {
        if(tokens.size() == pos+1 || !getTokenByPos(tokens, pos+1).getType().equals(TokenType.LBRACKET)){
            pos = linkVar(tokens, pos,funcName);
        }else if(tokens.size() >= pos+2 && getTokenByPos(tokens, pos+1).getType().equals(TokenType.LBRACKET)){
            String varFuncName = tokens.remove(pos).getValue();
            int parameterPos = 0;
            int varCounter;
            List<Token> permanentList = new ArrayList<>();
            //Удаляем левую скобку
            tokens.remove(pos);
            //
            NORPNCounter++;
            //Тут вставить сохранение всех переменных вызываемой функции
            varCounter = saveAllFuncVars(permanentList,varFuncName);
            //
            //Тут вставить присвоение всем параметрам. Тут удаляется правая скобка
            parameterPos = assignAllParameters(tokens, pos, permanentList, varFuncName, funcName);
            //
            //Тут вставить вызов функции("call" identifer)
            permanentList.add(new Token(TokenType.SYS_CALL, "call"));
            permanentList.add(new Token(TokenType.IDENTIFIER, varFuncName));
            //
            //Тут вставить ":=" для всем переменных функции
            addAssignForAllFuncVars(permanentList,varCounter);
            //
            //Тут вставить NORPN в начало и конец. Вставить permanentTokens в tokens. Откалибровать pos
            NORPNCounter--;
            if(NORPNCounter == 0){
                permanentList.add(0, new Token(TokenType.NORPN,"//"));
                permanentList.add(new Token(TokenType.NORPN,"//"));
                NORPNDeleteCounter++;
                NORPNDeleteCounter++;
            }
            tokens.addAll(pos, permanentList);
            pos = pos + permanentList.size();
            //
            //Если есть возвращаемое значение. Тут вставить имя функции. Вызвать linkVarFuncUse
            String funcTypeNameDup = fieldTypeEqualMap.get("main."+varFuncName);
            if(funcTypeNameDup != null){
                exprType = funcTypeNameDup;
                tokens.add(pos,new Token(TokenType.IDENTIFIER,varFuncName));
                pos = linkVar(tokens, pos, funcName);
            }else{
                throw new LangLinkException(varFuncName + " is undefined.");
            }
            //
            if(funcParamMap.size() != 0 && funcParamMap.get(varFuncName)!= null && parameterPos < funcParamMap.get(varFuncName).size()){
                throw new LangLinkException(funcParamMap.get(varFuncName).get(parameterPos) + " expected, but not find.");
            }
        }
        return pos;
    }

    private static void addAssignForAllFuncVars(List<Token> permanentList, int varCounter) {
        for(int i = 0; i < varCounter; i++){
            permanentList.add(new Token(TokenType.ASSIGNMENT,":="));
        }
    }

    private static int linkVar(List<Token> tokens, int pos, String funcName) throws LangLinkException {
        if(getTokenByPos(tokens, pos).getType().equals(TokenType.DOG)){
            address = true;
            tokens.remove(pos);
        }
        String varName = getTokenByPos(tokens, pos).getValue();
        getTokenByPos(tokens, pos).setType(TokenType.ADDRESS);
        try{
            getTokenByPos(tokens,pos).setValue(varMap.get(funcName + "." + varName).toString());
        }catch(NullPointerException not_in_func){
            try{
                getTokenByPos(tokens,pos).setValue(varMap.get("main" + "." + varName).toString());
            }catch(NullPointerException not_in_main){
                throw new LangLinkException(varName + " is undefined in " + funcName);
            }
        }
        pos++;
        if((tokens.size() != pos) && tokens.get(pos).getType().equals(TokenType.UPARROW)){
            varName = varName + "^";
            pos++;
        }
        exprType = fieldTypeEqualMap.get(funcName + "." + varName);
        if(exprType == null){
            exprType = fieldTypeEqualMap.get("main" + "." + varName);
        }
        if(exprType == null){
            throw new LangLinkException("Function parameter: " + varName + " is undefined");
        } 
        while(tokens.size() > pos && getTokenByPos(tokens, pos).getType().equals(TokenType.DOT)){
            pos++;
            varName = getTokenByPos(tokens, pos).getValue();
            getTokenByPos(tokens, pos).setType(TokenType.OFFSET);
            try{
                getTokenByPos(tokens,pos).setValue(typeMap.get(exprType + "." + varName).toString());
            }catch(NullPointerException undefined){
                throw new LangLinkException(varName + " is undefined for " +exprType);
            }
            pos++;
            if((tokens.size() != pos) && tokens.get(pos).getType().equals(TokenType.UPARROW)){
                varName = varName + "^";
                pos++;
            }
            exprType = fieldTypeEqualMap.get(exprType + "." + varName);
            if(exprType == null){
                throw new LangLinkException(varName + " is undefined for " +exprType);
            } 
        }
        if(pointer){
            exprType = "^" + exprType;
            pointer = false;
        }
        if(address){
            address = false;
            return pos;
        }else{
            tokens.add(pos, new Token(TokenType.UPARROW,"^"));
            return pos+1;
        }
    }

    private static int assignAllParameters(List<Token> tokens, int pos, List<Token> permanentList, String varFuncName,
            String funcName)
            throws LangLinkException {
        List<String> parameterList = funcParamMap.get(varFuncName);
        if(parameterList != null && parameterList.size() == 0 && getTokenByPos(tokens, pos).getType().equals(TokenType.RBRACKET)){
            tokens.remove(pos);
            return 0;
        }else if(parameterList != null && parameterList.size() == 0 && !getTokenByPos(tokens, pos).getType().equals(TokenType.RBRACKET)){
            throw new LangLinkException(varFuncName + " has no parameters");
        }else if(parameterList != null && parameterList.size() != 0 && getTokenByPos(tokens, pos).getType().equals(TokenType.RBRACKET)){
            throw new LangLinkException(varFuncName + " has parameter(s)");
        }else if(parameterList != null && parameterList.size() != 0 && !getTokenByPos(tokens, pos).getType().equals(TokenType.RBRACKET)){
            int bracketCounter = 1;
            int parameterPos = 0;
            while(bracketCounter != 0){
                //Формируем параметр
                List<Token> paramSave = new ArrayList<>();
                while(!getTokenByPos(tokens, pos).getType().equals(TokenType.COMMA) && bracketCounter != 0){
                    if(getTokenByPos(tokens, pos).getType().equals(TokenType.LBRACKET)){
                        bracketCounter++;
                    }else if(getTokenByPos(tokens, pos).getType().equals(TokenType.RBRACKET)){
                        bracketCounter--;
                    }
                    if(bracketCounter == 0){
                        break;
                    }
                    paramSave.add(tokens.remove(pos));
                }
                tokens.remove(pos);
                //Формируем список принимаемой переменной
                List<Token> paramVar = new ArrayList<>();
                paramVar.add(new Token(TokenType.IDENTIFIER,funcParamMap.get(varFuncName).get(parameterPos)));
                //Присвоение параметру и перевод в обратную польскую запись
                List<Token> secondPermanentList = assignVar(paramVar,paramSave,varFuncName,funcName);
                SEMICOLONCounter = SEMICOLONCounter - semicolonCounter(secondPermanentList);
                NORPNDeleteCounter = NORPNDeleteCounter - norpnCounter(secondPermanentList);
                RPN.getRPN(secondPermanentList);
                permanentList.addAll(RPN.getTokenList());
                parameterPos++;
                //
            }
            return parameterPos;
        }
        return -1;
    }

    private static int semicolonCounter(List<Token> tokens){
        int i = 0;
        for (Token token : tokens) {
            if(token.getType().equals(TokenType.SEMICOLON)){
                i++;
            }
        }
        return i;
    }

    private static int norpnCounter(List<Token> tokens){
        int i = 0;
        for (Token token : tokens) {
            if(token.getType().equals(TokenType.NORPN)){
                i++;
            }
        }
        return i;
    }

    private static List<Token> assignVar(List<Token> to, List<Token> value, String varFuncName, String funcName) throws LangLinkException {
        String exprTypePrevDup = exprTypePrev;
        String exprTypeDup = exprType;    
        List<Token> resultList = new ArrayList<>();
        // to.add(0, new Token(TokenType.DOG, "@"));
        address = true;
        linkOperand(to, 0, varFuncName);
        if(exprType.contains("^") || typeMap.get(exprType) == 1){
            linkExpression(value, 0, funcName);
            // if(!varExprType.equals("pointer") && !exprType.equals("pointer") && !varExprType.equals(exprType)){//Изменил. Смотреть в бекапах
            //     throw new LangLinkException(varExprType + " expected, but " + exprType + " found.");
            // }
            resultList.addAll(to);
            resultList.add(new Token(TokenType.ASSIGNMENT,":="));
            resultList.addAll(value);
            resultList.add(new Token(TokenType.SEMICOLON,";"));
            SEMICOLONCounter++;
        }else if(typeMap.get(exprType) > 1){
            throw new LangLinkException("Now you can't use multi assign for difficult types");
            // String varExprType = exprType;
            // linkVarFuncUse(value, 0, funcName);
            // if(!varExprType.equals(exprType)){//Изменил. Смотреть в бекапах
            //     throw new LangLinkException(varExprType + " expected, but " + exprType + " found.");
            // }
            // List<Token> valueSave = new ArrayList<>();
            // int pos = value.size()-1; 
            // Token search = getTokenByPos(value, pos);
            // while(search.getType().equals(TokenType.ADDRESS) || search.getType().equals(TokenType.DOT) || search.getType().equals(TokenType.UPARROW)){
            //     valueSave.add(0, search);
            //     pos--;
            //     if(pos < 0){
            //         break;
            //     }
            //     search = getTokenByPos(value, pos);
            // }
            // resultList.addAll(to);
            // resultList.add(new Token(TokenType.ASSIGNMENT,":="));
            // resultList.addAll(value);
            // resultList.add(new Token(TokenType.SEMICOLON,";"));
            // SEMICOLONCounter++;
            // for(int i = 1; i < typeMap.get(exprType); i++){     
            //     resultList.addAll(to);
            //     resultList.add(new Token(TokenType.DOT,"."));
            //     resultList.add(new Token(TokenType.OFFSET,Integer.toString(i)));
            //     resultList.add(new Token(TokenType.ASSIGNMENT,":="));
            //     resultList.addAll(valueSave);
            //     resultList.add(resultList.size()-1,new Token(TokenType.DOT,"."));
            //     resultList.add(resultList.size()-1,new Token(TokenType.OFFSET,Integer.toString(i)));
            //     resultList.add(new Token(TokenType.SEMICOLON,";"));
            //     SEMICOLONCounter++;
            // }
        }
        exprTypePrev = exprTypePrevDup;
        exprType = exprTypeDup;
        return resultList;
    }

    private static int saveAllFuncVars(List<Token> tokens, String varFuncName) {
        int varCounter = 0;
        for (Map.Entry<String,Integer> entry : varMap.entrySet()) {
            if(entry.getKey().matches(varFuncName + "\\.[A-Za-z_]+")){
                tokens.add(new Token(TokenType.ADDRESS, entry.getValue().toString()));
                tokens.add(new Token(TokenType.ADDRESS, entry.getValue().toString()));
                tokens.add(new Token(TokenType.UPARROW, "^"));
                varCounter++;
            }
        }
        return varCounter;
    }

    private static int linkCharacterString(List<Token> tokens, int pos) {
        exprType = "string";
        getTokenByPos(tokens,pos).setValue(getTokenByPos(tokens, pos).getValue().substring(1, getTokenByPos(tokens, pos).getValue().length()-1));
        pos++;
        return pos;
    }

    private static int linkBoolean(List<Token> tokens,int pos) {
        exprType = "boolean";
        if(getTokenByPos(tokens, pos).getValue().equals("false")){
            getTokenByPos(tokens, pos).setValue("0");
        }else{
            getTokenByPos(tokens, pos).setValue("1");
        }
        pos++;
        return pos;
    }

    private static int linkNumeric(List<Token> tokens,int pos) {
        pos++;
        if(tokens.size() > pos && getTokenByPos(tokens,pos).getType().equals(TokenType.DOT)){
            tokens.remove(pos);
            getTokenByPos(tokens, pos-1).setValue(getTokenByPos(tokens, pos-1).getValue() + "." + getTokenByPos(tokens, pos).getValue());
            tokens.remove(pos);
            exprType = "double";
        }else{
            exprType = "int";
        }
        return pos;
    }

    private static int linkOperandPrefix(List<Token> tokens, int pos) {
        switch(getTokenByPos(tokens,pos).getType()){
            case PLUS:
                getTokenByPos(tokens,pos).setType(TokenType.UNARY_PLUS);
                pos++;
                break;
            case MINUS:
                getTokenByPos(tokens,pos).setType(TokenType.UNARY_MINUS);
                pos++;
                break;
            case SYS_NOT:
                pos++;
                break;
            default:
                break;
        }
        return pos;
    }

    private static List<Token> getMemoryForVars(List<Token> tokens) {
        int pos = 0;
        for(int i = 0; i < Var.getEmptyAddress(); i++){
            tokens.add(pos, new Token(TokenType.EMPTY,"0"));
        }
        return tokens;
    }

    private static Token getTokenByPos(List<Token> tokens,int pos){
        return tokens.get(pos);
    }

    private static List<Token> linkAllTypes(List<Token> tokens){
        for (Token token : tokens) {
            if(token.getType().equals(TokenType.IDENTIFIER) && typeMap.containsKey(token.getValue())){
                token.setType(TokenType.NUMERIC);
                token.setValue(Integer.toString(typeMap.get(token.getValue())));
            }
        }
        return tokens;
    }
}