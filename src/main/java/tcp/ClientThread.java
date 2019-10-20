/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcp;

/**
 *
 * @author happyli
 */
 
import com.funnyai.common.S_Debug;
import com.funnyai.net.Old.S_Net;
import com.funnyai.string.Old.S_Strings;
import funnyai.JavaMain;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import static java.lang.System.out;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
 
/**
 * Hello world!
 * client
 */
 
public class ClientThread extends Thread{// implements Runnable{
    private Socket socket;
    BufferedReader br = null;
    
    public String userName="";
    public String tcp_host="";
    public int tcp_port=0;
    public TCP_Client client=null;
    public int keep_count=0;
 
    public ClientThread(TCP_Client pClient) throws IOException{
        this.client=pClient;
        this.socket = pClient.socket;
        this.tcp_host=pClient.tcp_host;
        this.tcp_port=pClient.tcp_port;
        
        br = new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
    }
 
    @Override
    public void run() {
        try {

            boolean bError=false;
            while(bError==false){
                int len=0;
                byte[] buffer=new byte[4096];
                do{
                    try{
                        len=socket.getInputStream().read(buffer, 0, buffer.length);
                        
                    }catch(SocketException ex){
                        try {
                            socket = new Socket(this.tcp_host,this.tcp_port);
                            ex.printStackTrace();
                        } catch (IOException ex2) {
                            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex2);
                        }
                    }
                }while(len==0);
                if (len>0){
                    byte[] dest=new byte[len];
                    System.arraycopy(buffer,0, dest,0,len);
                    String content=new String(dest,"utf-8");
                    if (content.startsWith("m:")){
                        int index1=content.indexOf(":<s>:");
                        int index2=content.indexOf(":</s>");
                        if (index2>index1 && index1>0){
                            String strJSON=content.substring(index1+5,index2);
                            JSONObject obj = new JSONObject(strJSON);
                            Chat_Event(obj);
                        }
                    }else{
                        String[] strSplit=content.split("\\r\\n");
                        for (String strSplit1 : strSplit) {
                            if ("s:keep".equals(strSplit1)) {
                                keep_count=0;
                            }
                        }
                    }
                    System.out.println(content);
                }
            
            }
        } catch (IOException e) {
            try {
                socket = new Socket(this.tcp_host,this.tcp_port);
                e.printStackTrace();
            } catch (IOException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    /**
     * 回答用户的命令
     * @param args1 
     */
    public void Chat_Event(Object... args1){
        for (Object args11 : args1) {
            JSONObject obj = (JSONObject) args11;

            String id="";
            if (obj.has("id")) id=obj.getString("id");

            String strType="";
            if (obj.has("type")) strType=obj.getString("type");
            
            String token="";
            if (obj.has("token")) token=obj.getString("token");

            String strFrom=obj.getString("from");
            String strTo=obj.getString("to");
            String strMessage=obj.getString("message");

            String url="http://www.funnyai.com/login_check_token.php";
            String[] strSplit=strFrom.split("/");
            //反馈给对方，告诉他，收到了。
            if ("chat_return".equals(strType)){
                return ;
            }
            client.Send_Msg(0,id,"chat_return",userName,strFrom,id); //消息返回
            
            out.println(obj.toString());
        }
    }
}

