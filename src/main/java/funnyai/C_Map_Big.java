/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package funnyai;

import com.funnyai.data.C_K_Str;
import com.funnyai.data.Treap;
import com.funnyai.io.C_File;
import com.funnyai.io.Old.S_File;
import static java.lang.System.out;

/**
 *
 * @author happyli
 */
public class C_Map_Big {
    public Treap<String> pTreap=new Treap<>();
    C_Map_Big(String strFile) {
        C_File pFile=S_File.Read_Begin(strFile, "utf-8");
        String strLine=S_File.read_line(pFile);
        while(strLine!=null){
            strLine=strLine.replace("\t", ",");
            String[] strSplit=strLine.split(",");
            if (strSplit.length>2){
                pTreap.insert(new C_K_Str(strSplit[0]+":"+strSplit[1]), strSplit[2]);
            }
            strLine=S_File.read_line(pFile);
        }
        pFile.Close();
    }
    
    public String find(String field_Name,String strValue){
        if (strValue.endsWith(".0")){
            strValue=strValue.substring(0,strValue.length()-2);
        }
        String strReturn=pTreap.find(new C_K_Str(field_Name+":"+strValue));
        if (strReturn==null){
            out.println("None:"+strValue);
            return "0";
        }else{
            return strReturn;
        }
    }
}
