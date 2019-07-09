/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package funnyai;

import com.funnyai.data.C_K_Str;
import com.funnyai.string.Old.S_Strings;
import static java.lang.System.out;
import java.util.List;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;

/**
 *
 * @author admin
 */
public class Tools {


    
    /**
     *
     * @param bFirstLine
     * @param expression
     * @param pGroup
     * @param pItems
     * @return
     */
    public static String expression_calculate_function(boolean bFirstLine, Expression expression, C_Group pGroup, List<String> pItems) {
        double dbValue=0;
        switch (expression.getClass().getName()) {
            
            case "net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd":
            case "net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr":
            case "net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor":
            case "net.sf.jsqlparser.expression.operators.arithmetic.Concat":
            case "net.sf.jsqlparser.expression.operators.arithmetic.Modulo":
            case "net.sf.jsqlparser.expression.operators.arithmetic.Subtraction":
            case "net.sf.jsqlparser.expression.operators.arithmetic.Multiplication":
            case "net.sf.jsqlparser.expression.operators.arithmetic.Division":
            case "net.sf.jsqlparser.expression.operators.arithmetic.Addition":
                BinaryExpression pA = (BinaryExpression) expression;
                String strA=expression_calculate_function(bFirstLine, pA.getLeftExpression(), pGroup, pItems);
                String strB=expression_calculate_function(bFirstLine, pA.getRightExpression(), pGroup, pItems);
                double a = 0;
                if (S_Strings.isNumeric(strA)) a=Double.parseDouble(strA);
                double b = 0;
                if (S_Strings.isNumeric(strA)) b=Double.parseDouble(strB);
                switch (expression.getClass().getName()) {
                    case "net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd":
                        dbValue= Integer.parseInt(a+"") & Integer.parseInt(b+"");
                        break;
                    case "net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr":
                        dbValue= Integer.parseInt(a+"") | Integer.parseInt(b+"");
                        break;
                    case "net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor":
                        dbValue= Integer.parseInt(a+"") ^ Integer.parseInt(b+"");
                        break;
                    case "net.sf.jsqlparser.expression.operators.arithmetic.Concat":
                        return (a+"") + (b+"");
                    case "net.sf.jsqlparser.expression.operators.arithmetic.Modulo":
                        dbValue= a % b;
                        break;
                    case "net.sf.jsqlparser.expression.operators.arithmetic.Subtraction":
                        dbValue= a - b;
                        break;
                    case "net.sf.jsqlparser.expression.operators.arithmetic.Multiplication":
                        dbValue= a * b;
                        break;
                    case "net.sf.jsqlparser.expression.operators.arithmetic.Division":
                        dbValue= a / b;
                        break;
                    case "net.sf.jsqlparser.expression.operators.arithmetic.Addition":
                        dbValue= a + b;
                        break;
                }
                break;
            case "net.sf.jsqlparser.expression.operators.arithmetic.Column":
                Column pC = (Column) expression;
                out.println(pC.getColumnName());
                return pC.getColumnName();
            case "net.sf.jsqlparser.expression.Function":
                Function pFunction = (Function) expression;
                String strFunction = pFunction.toString();
                C_Function pFun = null;
                if (pGroup != null) {
                    pFun = pGroup.pTreap_Function.find(new C_K_Str(strFunction));
                    if (pFun == null) {
                        pFun = new C_Function();
                        pFun.Name = pFunction.getName();
                        pGroup.pTreap_Function.insert(new C_K_Str(strFunction), pFun);
                    }else{
                        if (pFun.bCaculate){
                            return pFun.dbValue+"";
                        }
                    }
                } else {
                    pFun = new C_Function();
                    pFun.Name = pFunction.getName();
                }
                ExpressionList pE = pFunction.getParameters();
                List<Expression> p3 = pE.getExpressions();
                String[] strParam = new String[p3.size()];
                for (int k = 0; k < p3.size(); k++) {
                    strParam[k] = expression_calculate_function(bFirstLine,p3.get(k), pGroup, pItems);
                }
                pFun.bCaculate=true;//代表计算过了
                String strName="";
                if (p3.get(0).getClass().getName().equals("net.sf.jsqlparser.schema.Column")){
                    Column p=(Column)p3.get(0);
                    strName=p.getColumnName();
                }
                return pFun.Funcion_Calculate(bFirstLine,strName,strParam);
            case "net.sf.jsqlparser.schema.Column":
                Column pItem = (Column) expression;
                String column_name = pItem.getColumnName();
                if (column_name.toLowerCase().startsWith("c")) {
                    int index2 = Integer.parseInt(column_name.substring(1)) - 1;
                    if (index2 < pItems.size()) {
                        String strValue = (String) pItems.get(index2);
                        return strValue;
                    } else {
                        return "0";
                    }
                } else {
                    return pItem.getColumnName();
                }
            case "net.sf.jsqlparser.expression.LongValue":
                LongValue pLongValue = (LongValue) expression;
                return pLongValue.getStringValue();
            case "net.sf.jsqlparser.expression.StringValue":
                StringValue pStringValue = (StringValue) expression;
                return pStringValue.getValue();
        }
        return dbValue+"";
    }
    
    /**
     *
     * @param bFirstLine
     * @param expression
     * @param pGroup
     * @param pItems
     * @return
     */
    public static String expression_calculate(boolean bFirstLine, Expression expression, C_Group pGroup, List<String> pItems) {
        double dbValue=0;
        String strName=expression.getClass().getName();
        switch (strName) {
            case "net.sf.jsqlparser.expression.Parenthesis":
                Parenthesis pParenthesis=(Parenthesis) expression;
                String strDB=expression_calculate(bFirstLine,pParenthesis.getExpression(), pGroup, pItems);
                dbValue=Double.parseDouble(strDB);
                break;
            case "net.sf.jsqlparser.expression.SignedExpression":
                SignedExpression pSignedExpression=(SignedExpression) expression;
                String strDB2=expression_calculate(bFirstLine,pSignedExpression.getExpression(), pGroup, pItems);
                if (pSignedExpression.getSign()=='-'){
                    if (strDB2.equals("")){
                        dbValue=0;
                    }else{
                        dbValue=Double.parseDouble(strDB2)*(-1);
                    }
                }else{
                    dbValue=Double.parseDouble(strDB2);
                }
                break;
            case "net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd":
            case "net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr":
            case "net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor":
            case "net.sf.jsqlparser.expression.operators.arithmetic.Concat":
            case "net.sf.jsqlparser.expression.operators.arithmetic.Modulo":
            case "net.sf.jsqlparser.expression.operators.arithmetic.Subtraction":
            case "net.sf.jsqlparser.expression.operators.arithmetic.Multiplication":
            case "net.sf.jsqlparser.expression.operators.arithmetic.Division":
            case "net.sf.jsqlparser.expression.operators.arithmetic.Addition":
                BinaryExpression pA = (BinaryExpression) expression;
                String strA=expression_calculate(bFirstLine, pA.getLeftExpression(), pGroup, pItems);
                String strB=expression_calculate(bFirstLine, pA.getRightExpression(), pGroup, pItems);
                double a = 0;
                if (!strA.equals("")) a= Double.parseDouble(strA);
                double b = 0;
                if (!strB.equals("")) b=Double.parseDouble(strB);
                
                switch (expression.getClass().getName()) {
                    case "net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd":
                        dbValue= Integer.parseInt(a+"") & Integer.parseInt(b+"");
                        break;
                    case "net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr":
                        dbValue= Integer.parseInt(a+"") | Integer.parseInt(b+"");
                        break;
                    case "net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor":
                        dbValue= Integer.parseInt(a+"") ^ Integer.parseInt(b+"");
                        break;
                    case "net.sf.jsqlparser.expression.operators.arithmetic.Concat":
                        return (a+"") + (b+"");
                    case "net.sf.jsqlparser.expression.operators.arithmetic.Modulo":
                        dbValue= a % b;
                        break;
                    case "net.sf.jsqlparser.expression.operators.arithmetic.Subtraction":
                        dbValue= a - b;
                        break;
                    case "net.sf.jsqlparser.expression.operators.arithmetic.Multiplication":
                        dbValue= a * b;
                        break;
                    case "net.sf.jsqlparser.expression.operators.arithmetic.Division":
                        dbValue= a / b;
                        break;
                    case "net.sf.jsqlparser.expression.operators.arithmetic.Addition":
                        dbValue= a + b;
                        break;
                }
                break;
            case "net.sf.jsqlparser.expression.operators.arithmetic.Column":
                Column pC = (Column) expression;
//                out.println(pC.getColumnName());
                return "test";
            case "net.sf.jsqlparser.expression.Function":
                Function pFunction = (Function) expression;
                String strFunction = pFunction.toString();
//                out.println(strFunction);
                C_Function pFun = null;
                if (pGroup != null) {
                    pFun = pGroup.pTreap_Function.find(new C_K_Str(strFunction));
                    if (pFun != null) {
                        return pFun.dbValue+"";
                    }else {
                        return "0";
                    }
                } else {
                    C_Function pFun2 = new C_Function();
                    pFun2.Name = pFunction.getName();
                    ExpressionList pE = pFunction.getParameters();
                    List<Expression> p3 = pE.getExpressions();
                    String[] strParam = new String[p3.size()];
                    for (int k = 0; k < p3.size(); k++) {
                        strParam[k] = expression_calculate_function(bFirstLine,p3.get(k), pGroup, pItems);
                    }
                    pFun2.bCaculate=true;//代表计算过了
                    String strName2="";
                    if (p3.get(0).getClass().getName().equals("net.sf.jsqlparser.schema.Column")){
                        Column p=(Column)p3.get(0);
                        strName2=p.getColumnName();
                    }
                    return pFun2.Funcion_Calculate(bFirstLine,strName2,strParam);
                }
            case "net.sf.jsqlparser.schema.Column":
                Column pItem = (Column) expression;
                String column_name = pItem.getColumnName();
                if (column_name.toLowerCase().startsWith("c")) {
                    int index2 = Integer.parseInt(column_name.substring(1)) - 1;
                    if (pItems!=null && index2 < pItems.size()) {
                        String strValue = (String) pItems.get(index2);
                        return strValue;
                    } else {
                        return "";
                    }
                } else {
                    return pItem.getColumnName();
                }
            case "net.sf.jsqlparser.expression.LongValue":
                LongValue pLongValue = (LongValue) expression;
                return pLongValue.getStringValue();
            case "net.sf.jsqlparser.expression.StringValue":
                StringValue pStringValue = (StringValue) expression;
                return pStringValue.getValue();
            case "net.sf.jsqlparser.expression.DoubleValue":
                DoubleValue pDoubleValue = (DoubleValue) expression;
                return pDoubleValue.toString();
            default:
                out.println(strName);
        }
        return dbValue+"";
    }
    
    /**
     * 
     * @param pExpression
     * @param pItems
     * @return 
     */
    public static boolean where_expression_calculate(
            Expression pExpression,List<String> pItems){
        switch(pExpression.getClass().getName()){
            case "net.sf.jsqlparser.expression.Parenthesis":
                Parenthesis pParenthesis=(Parenthesis)pExpression;
                if (pParenthesis.isNot()){
                    return !where_expression_calculate(pParenthesis.getExpression(),pItems);
                }else{
                    return where_expression_calculate(pParenthesis.getExpression(),pItems);
                }
            case "net.sf.jsqlparser.expression.operators.conditional.AndExpression":
                AndExpression pAND=(AndExpression)pExpression;
                Expression pExpression1=pAND.getLeftExpression();
                Expression pExpression2=pAND.getRightExpression();
                if (Tools.where_expression_calculate(pExpression1, pItems) && Tools.where_expression_calculate(pExpression2, pItems)){
                    return true;
                }
                break;
            case "net.sf.jsqlparser.expression.operators.conditional.OrExpression":
                OrExpression pOR=(OrExpression)pExpression;
                Expression pExpression_OR1=pOR.getLeftExpression();
                Expression pExpression_OR2=pOR.getRightExpression();
                if (Tools.where_expression_calculate(pExpression_OR1, pItems) || Tools.where_expression_calculate(pExpression_OR2, pItems)){
                    return true;
                }
                break;
            case "net.sf.jsqlparser.expression.operators.relational.NotEqualsTo":
            case "net.sf.jsqlparser.expression.operators.relational.EqualsTo":
            case "net.sf.jsqlparser.expression.operators.relational.GreaterThan":
            case "net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals":
            case "net.sf.jsqlparser.expression.operators.relational.MinorThan":
            case "net.sf.jsqlparser.expression.operators.relational.MinorThanEquals":
                BinaryExpression pExpressionBinary=(BinaryExpression)pExpression;
                String strA=expression_calculate(false, pExpressionBinary.getLeftExpression(), null, pItems);
                String strB=expression_calculate(false, pExpressionBinary.getRightExpression(), null, pItems);

                switch(pExpression.getClass().getName()){
                case "net.sf.jsqlparser.expression.operators.conditional.AndExpression":
                    break;
                case "net.sf.jsqlparser.expression.operators.conditional.OrExpression":
                    break;
                case "net.sf.jsqlparser.expression.operators.relational.NotEqualsTo":
                    if (S_Strings.isNumeric(strA) && S_Strings.isNumeric(strB)){
                        double a = Double.parseDouble(strA);
                        double b = Double.parseDouble(strB);
                        if (a!=b){
                            return pExpressionBinary.isNot()==false;
                        }else{
                            return pExpressionBinary.isNot();
                        }
                    }else{
                        if (strA.equals(strB)==false){
                            return pExpressionBinary.isNot()==false;
                        }else{
                            return pExpressionBinary.isNot();
                        }
                    }
                case "net.sf.jsqlparser.expression.operators.relational.GreaterThan":
                    {
                        double a = 0;
                        if (S_Strings.isNumeric(strA)) a=Double.parseDouble(strA);
                        double b = 0;
                        if (S_Strings.isNumeric(strB)) b=Double.parseDouble(strB);
                        if (a>b){
                            return pExpressionBinary.isNot()==false;
                        }else{
                            return pExpressionBinary.isNot();
                        }
                    }
                case "net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals":
                    {
                        double a = 0;
                        if (S_Strings.isNumeric(strA)) a=Double.parseDouble(strA);
                        double b = 0;
                        if (S_Strings.isNumeric(strB)) b=Double.parseDouble(strB);
                        if (a>=b){
                            return !pExpressionBinary.isNot();
                        }else{
                            return pExpressionBinary.isNot();
                        }
                    }
                case "net.sf.jsqlparser.expression.operators.relational.MinorThan":
                    {
                        double a = 0;
                        if (S_Strings.isNumeric(strA)) a=Double.parseDouble(strA);
                        double b = 0;
                        if (S_Strings.isNumeric(strB)) b=Double.parseDouble(strB);
                        if (a<b){
                            return pExpressionBinary.isNot()==false;
                        }else{
                            return pExpressionBinary.isNot();
                        }
                    }
                case "net.sf.jsqlparser.expression.operators.relational.MinorThanEquals":
                    {
                        double a = 0;
                        if (S_Strings.isNumeric(strA)) a=Double.parseDouble(strA);
                        double b = 0;
                        if (S_Strings.isNumeric(strB)) b=Double.parseDouble(strB);
                        if (a<=b){
                            return pExpressionBinary.isNot()==false;
                        }else{
                            return pExpressionBinary.isNot();
                        }
                    }
                case "net.sf.jsqlparser.expression.operators.relational.EqualsTo":
                    if (S_Strings.isNumeric(strA) && S_Strings.isNumeric(strB)){
                        double a = Double.parseDouble(strA);
                        double b = Double.parseDouble(strB);
                        if (a==b){
                            return pExpressionBinary.isNot()==false;
                        }else{
                            return pExpressionBinary.isNot();
                        }
                    }else{
                        if (strA.equals(strB)){
                            return pExpressionBinary.isNot()==false;
                        }else{
                            return pExpressionBinary.isNot();
                        }
                    }
                }
                break;
        }
        return false;
    }
    
}
