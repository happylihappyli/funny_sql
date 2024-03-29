/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.funnyai.tools;

import com.funnyai.data.C_K_Str;
import com.funnyai.data.Treap;
import com.funnyai.string.Old.S_Strings;
import static java.lang.System.out;

/**
 *
 * @author happyli
 */
public class C_Function {
    public String Name="";
    public double dbValue=0;
    public boolean bCaculate=false;
    
    public static Treap pTreap=new Treap();
    
    /**
     * 函数计算
     * @param bFirstLine
     * @param strName 字段名
     * @param strParam
     * @return
     */
    public String Funcion_Calculate(boolean bFirstLine,String strName, String[] strParam) {
        switch (this.Name.toLowerCase()) {
            case "zero":
                this.dbValue=0;
                break;
            case "map":
                C_Map pMap=(C_Map) pTreap.find(new C_K_Str(strParam[1]));
                if (pMap==null){
                    pMap=new C_Map(strParam[1]);
                    pTreap.insert(new C_K_Str(strParam[1]), pMap);
                }
                return pMap.find(strName,strParam[0]);
            case "map_big":
                C_Map_Big pMap_Big=(C_Map_Big) pTreap.find(new C_K_Str(strParam[1]));
                if (pMap_Big==null){
                    pMap_Big=new C_Map_Big(strParam[1]);
                    pTreap.insert(new C_K_Str(strParam[1]), pMap_Big);
                }
                return pMap_Big.find(strName,strParam[0]);
            case "substring":
                {
                    String strLine=strParam[0];
                    int a=Integer.parseInt(strParam[1])-1;
                    int b=Integer.parseInt(strParam[2]);
                    return strLine.substring(a,a+b);
                }
            case "isnumeric":
                {
                    String strLine=strParam[0];
                    if (S_Strings.isNumeric(strLine)){
                        return "1";
                    }else{
                        return "0";
                    }
                }
            case "ifzero":
                {
                    String strLine=strParam[0];
                    if ("0".equals(strLine)){
                        return "1";
                    }else{
                        return "0";
                    }
                }
            case "round":
                if (S_Strings.isNumeric(strParam[0])){
                    this.dbValue=Math.round(Double.parseDouble(strParam[0]));
                }else{
                    out.println(strName+":not number:"+strParam[0]);
                    this.dbValue=0;
                }
                break;
            case "sum":
                for (String strParam1 : strParam) {
                    this.dbValue += Double.parseDouble(strParam1);
                }
                break;
            case "count":
                this.dbValue += 1;
                break;
            case "max":
                for (String strParam1 : strParam) {
                    if (Double.parseDouble(strParam1) > this.dbValue || bFirstLine) {
                        this.dbValue = Double.parseDouble(strParam1);
                    }
                }
                break;
            case "min":
                for (String strParam1 : strParam) {
                    if (Double.parseDouble(strParam1) < this.dbValue  || bFirstLine ) {
                        this.dbValue = Double.parseDouble(strParam1);
                    }
                }
                break;
        }
        return this.dbValue+"";
    }
}
