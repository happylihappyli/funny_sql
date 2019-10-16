/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package funnyai;

import com.funnyai.tools.C_Msg;
import com.funnyai.net.Old.S_Net;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import tcp.TCP_Client;

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
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(Loop_Send_Msg.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (pList.size()>0){
                C_Msg pMsg=pList.get(pList.size()-1);
                pList=new ArrayList<>();
                //S_Net.SI_Send("sys_event","status",pMsg.From,pMsg.To, pMsg.Msg);
                TCP_Client.client.Send_Msg(0, "", "status",pMsg.From,pMsg.To, pMsg.Msg);
                pList.remove(pMsg);
            }
        }
    }
    
    public void Add_Send_Msg(String strFrom,String strTo,String strMsg){
        C_Msg pMsg=new C_Msg(strFrom,strTo,strMsg);
        pList.add(pMsg);
    }
}
