/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package funnyai;

import com.funnyai.data.C_K_Str;
import com.funnyai.data.Treap;
import com.funnyai.io.C_File;
import com.funnyai.io.S_file;
import com.funnyai.io.S_file_sub;
import static java.lang.System.out;

/**
 *
 * @author happyli
 */
public class C_Map {
    public String strFile="";
    public Treap<String> pTreap=new Treap<>();
    C_Map(String strFile) {
        this.strFile=strFile;
        C_File pFile=S_file.main.Read_Begin(strFile, "utf-8");
        String strLine=S_file.main.read_line(pFile);
        while(strLine!=null){
            strLine=strLine.replace("\t", ",");
            String[] strSplit=strLine.split(",");
            if (strSplit.length>1){
                String key=strSplit[0];
                if (key.startsWith(">")){
                    pTreap.insert(new C_K_Str(">"), strSplit[1]);
                    key=key.replace(">", "");
                    key=key.replace("=", "");
                }
                pTreap.insert(new C_K_Str(key), strSplit[1]);
            }
            strLine=S_file.main.read_line(pFile);
        }
        pFile.Close();
    }
    
    /**
     * 查找
     * @param field_Name
     * @param strValue
     * @return 
     */
    public String find(String field_Name,String strValue){
        if (strValue.endsWith(".0")){
            strValue=strValue.substring(0,strValue.length()-2);
        }
        String strReturn=pTreap.find(new C_K_Str(strValue));
        if (strReturn==null){
            strReturn=pTreap.find(new C_K_Str(">"));
            if (strReturn==null){
                out.println(this.strFile+":"+field_Name+":None:"+strValue);
                return "0";
            }else{
                return strReturn;
            }
        }else{
            return strReturn;
        }
    }
}
