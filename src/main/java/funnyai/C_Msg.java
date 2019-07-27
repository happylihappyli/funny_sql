/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package funnyai;

/**
 *
 * @author happyli
 */
public class C_Msg {
    public String From="";
    public String Msg="";
    public String To="";

    C_Msg(String strFrom, String strTo,String strMsg) {
        this.From=strFrom;
        this.To=strTo;
        this.Msg=strMsg;
    }
    
}
