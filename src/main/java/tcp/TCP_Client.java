
package tcp;

import com.funnyai.common.AI_Var2;
import com.funnyai.common.S_Debug;
import com.funnyai.net.Old.S_Net;
import com.funnyai.string.Old.S_Strings;
import funnyai.JavaMain;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.System.out;
import java.net.Socket;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author happyli
 */
public class TCP_Client extends Thread {
    public String strServer_IP="";
    public Socket socket=null;
    public String tcp_host="";
    public int tcp_port=0;
    public ClientThread pThread=null;
    public static TCP_Client client=null;
    
    public TCP_Client(String tcp_host,int tcp_port){
        
        this.tcp_host=tcp_host;
        this.tcp_port=tcp_port;
    }
    
    @Override
    public void run() {
        try {
            socket = new Socket(this.tcp_host,this.tcp_port);
            socket.setKeepAlive(true);
            
            pThread=new ClientThread(this);
            pThread.start();
            
        } catch (IOException ex) {
            Logger.getLogger(TCP_Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
//    public String get_token(){
//        String url="http://www.funnyai.com/login_get_token_json.php";
//        String name=JavaMain.account;
//        String md5=JavaMain.md5;
//        String data="email="+S_Strings.URL_Encode(name)
//                +"&password="+S_Strings.URL_Encode(md5);
//        String result=S_Net.http_post(url,data);
//        String token="";
//        if (result.contains("登录成功")){
//            String[] strSplit=result.split("=");
//            token=strSplit[2];
//        }
//        return token;
//    }
    
    public long Msg_ID=0;
    public void Send_Msg(
            long ID,
            String old_ID,
            String type,
            String from,
            String strTo,
            String strMsg){
        
        if (ID==0){
            Msg_ID++;
            ID=Msg_ID;
        }
        //String token=get_token();
        JSONObject obj = new JSONObject();
        obj.put("id", ID+"");
        obj.put("oid", old_ID);
        obj.put("type", type);
        obj.put("from", from);
        obj.put("to", strTo);
        obj.put("message", strMsg);
//        obj.put("token", token);
        
        try {
            byte[] b = ("m:<s>:"+obj.toString()+":</s>\r\n").getBytes("UTF-8");
            socket.getOutputStream().write(b);
        } catch (IOException ex) {
            Logger.getLogger(TCP_Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
