/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package funnyai;

import com.funnyai.net.Old.S_Net;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author happyli
 */
public class Loop_Send_Msg extends Thread{
    
    public boolean bSend=true;
    public ArrayList<C_Msg> pList=new ArrayList<>();
    
    @Override
    public void run(){
        while(bSend){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Loop_Send_Msg.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (pList.size()>0){
                C_Msg pMsg=pList.get(pList.size()-1);
                pList=new ArrayList<>();
                S_Net.Send_Msg_To_Socket_IO(pMsg.From,pMsg.Msg, "", "");
            }
        }
    }
    
    public void Add_Send_Msg(String strFrom,String strMsg){
        C_Msg pMsg=new C_Msg(strFrom,strMsg);
        pList.add(pMsg);
    }
}
