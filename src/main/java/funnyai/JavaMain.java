package funnyai;

import com.funnyai.common.Tools_Init;
import com.funnyai.tools.*;
import com.funnyai.data.*;
import com.funnyai.io.C_File;
import com.funnyai.io.Old.C_Property_File;
import com.funnyai.io.Old.S_File;
import com.funnyai.io.S_file;
import com.funnyai.string.Old.S_Strings;
import java.io.FileNotFoundException;
import static java.lang.System.out;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import tcp.TCP_Client;

/**
 *
 * @author happyli
 */
public class JavaMain {
    public static int max_read=30000;
    
    public static boolean check_if_group(Expression pExpression){
        boolean bGroup=false;
        switch (pExpression.getClass().getName()){
            case "net.sf.jsqlparser.expression.operators.arithmetic.Addition":
            case "net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd":
            case "net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr":
            case "net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor":
            case "net.sf.jsqlparser.expression.operators.arithmetic.Concat":
            case "net.sf.jsqlparser.expression.operators.arithmetic.Modulo":
            case "net.sf.jsqlparser.expression.operators.arithmetic.Subtraction":
            case "net.sf.jsqlparser.expression.operators.arithmetic.Multiplication":
            case "net.sf.jsqlparser.expression.operators.arithmetic.Division":
                BinaryExpression pDiv=(BinaryExpression) pExpression;
                bGroup=check_if_group(pDiv.getLeftExpression());
                if (bGroup) return true;
                bGroup=check_if_group(pDiv.getRightExpression());
                break;
            case "net.sf.jsqlparser.expression.Function":
                Function pFun=(Function) pExpression;
                switch(pFun.getName()){
                    case "sum":
                    case "count":
                    case "max":
                    case "min":
                        bGroup=true;
                        break;
                }
        }
        return bGroup;
    }
    
    
    public static String strUser="";
    public static String strOutput="";
    
    public static void main(String[] args){
        
        try {
            String strFile="";
            String sql="select c1,c3,c2 from test";
            String strSep="";
            String File_Init="";
            if (args.length>6){
                File_Init=args[0];
                
                out.println("0="+File_Init);
                strUser=args[1];
                if ("x".equals(strUser)){
                    strUser="*";
                }
                out.println("1="+strUser);
                strFile=args[2];
                out.println("2="+strFile);
                JavaMain.max_read=Integer.parseInt(args[3]);
                out.println("3="+JavaMain.max_read);
                sql=args[4];
                out.println("4="+sql);
                strSep=args[5];
                out.println("5="+strSep);
                if (strSep.equals("t")) strSep="\t";
                if (strSep.equals("v")) strSep="\\|";
                strOutput=args[6];
                out.println("6="+strOutput);
            }else{
                out.println("args.length="+args.length+"7个参数 ini文件 user 文件 max_read sql t outputfile");
                return ;
            }
                
            Tools_Init.Init(File_Init);
            try {
                C_Property_File pFile=new C_Property_File(File_Init);
                
                String tcp_host=pFile.Read("tcp.host");
                int tcp_port=Integer.parseInt(pFile.Read("tcp.port"));

                TCP_Client.client=new TCP_Client(tcp_host,tcp_port);
                TCP_Client.client.start();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(JavaMain.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            sql=SQL_Replace(sql);
            
            out.println("SQL="+sql);
            out.println("out="+strOutput);
            
            Select select = (Select) CCJSqlParserUtil.parse(sql);
            out.println(select.getSelectBody());
            
            PlainSelect p=(PlainSelect) select.getSelectBody();
            long Row_Count=0;
            int iStart=0;
            if (p.getLimit()!=null){
                Row_Count=p.getLimit().getRowCount();
                iStart=(int) p.getLimit().getOffset();
            }
            List<SelectItem> pSelectItems=p.getSelectItems();
            List<Expression> pGroups=p.getGroupByColumnReferences();
            
            ArrayList<C_Line> pData=new ArrayList<>();
            
            //读取数据,用where 过滤掉
            out.println("File="+strFile);
            
            C_File pFile=S_file.main.Read_Begin(strFile, "utf-8");
            String strLine=S_file.main.read_line(pFile);
            int Line_Count=1;
            while(strLine!=null){
                strLine=S_file.main.read_line(pFile);
                Line_Count+=1;
            }
            if (Line_Count<max_read) max_read=Line_Count;
            int Max_Line=Line_Count;
            
            
            pFile=S_file.main.Read_Begin(strFile, "utf-8");
            strLine=S_file.main.read_line(pFile);
            Line_Count=1;
            while(strLine!=null){
                C_Line pLine1=new C_Line();
                String[] strSplit=strLine.split(strSep);
                for (String strValue : strSplit) {
                    if (strValue.equals("")) strValue="0";
                    pLine1.pLines.add(strValue);
                }
                
                Expression pWhere=p.getWhere();
                if (pWhere!=null){
                    if (Tools.where_expression_calculate(pWhere,pLine1.pLines)){
                        pData.add(pLine1);
                    }
                }else{
                    pData.add(pLine1);
                }
                int Step_Count=Math.round(Max_Line/20);
                if (Line_Count % Step_Count==0){
                    float Percent=Math.round(500*Line_Count/max_read)/10;
                    TCP_Client.client.Send_Msg(0, "", "status","progress2",strUser, Percent+"");
                    //pLoop_Send_Msg.Add_Send_Msg("progress2",strUser, Percent+"");
                }
                if (pData.size()>max_read) break;

                strLine=S_file.main.read_line(pFile);
                Line_Count+=1;
            }
            pFile.Close();
            
            //是否有聚合函数
            boolean bGroup=false;
            for (int i=0;i<pSelectItems.size();i++){
                SelectExpressionItem pSelect=(SelectExpressionItem) pSelectItems.get(i);
                bGroup=check_if_group(pSelect.getExpression());
                if (bGroup) break;
            }
            
            C_File pFile2=S_File.Write_Begin(strOutput, false, "utf-8");
            String strLineOutput;
            
            //数据处理
            if (bGroup==true){//处理Group的数据
                Treap<C_Group> pTreapGroups=new Treap<>(); //先分群
                Treap<C_Group> pTreapGroups_Double=new Treap<>(); //先分群
                if (pGroups!=null && pGroups.size()>0){//如果有group语句
                    for(int k=0;k<pData.size();k++){
                        C_Line pLine1=pData.get(k);
                        pLine1.Group=Tools.expression_calculate(k==0,pGroups.get(0),null,pLine1.pLines);
                        C_Group pGroup=pTreapGroups.find(new C_K_Str(pLine1.Group));
                        if (pGroup==null){
                            pGroup=new C_Group();
                            pGroup.GroupName=pLine1.Group;
                            if (S_Strings.isNumeric(pGroup.GroupName)){
                                pTreapGroups_Double.insert(
                                        new C_K_Double(Double.parseDouble(pGroup.GroupName)), 
                                        pGroup);
                            }
                            pTreapGroups.insert(new C_K_Str(pGroup.GroupName), pGroup);
                        }
                        pGroup.pTreap.insert(new C_K_Int(k), pLine1);
                    }
                }else{ //没有group语句，但有sum，count函数
                    C_Group pGroup=new C_Group();
                    pGroup.GroupName="all";
                    pTreapGroups.insert(new C_K_Str(pGroup.GroupName), pGroup);
                    for(int k=0;k<pData.size();k++){
                        C_Line pLine1=pData.get(k);
                        pLine1.Group="";
                        pGroup.pTreap.insert(new C_K_Int(k), pLine1);
                    }
                }
                
                Line_Count=1;
                Row_Count=pTreapGroups.Size();
                //然后对每个Group进行数据统计
                
                
                TreapEnumerator<C_Group> p2;
                if (pTreapGroups_Double.Size()>=pTreapGroups.Size()){
                    //如果是Double类型
                    p2=pTreapGroups_Double.Elements();
                }else{
                    p2=pTreapGroups.Elements();
                }
                while(p2.HasMoreElements()){
                    int Step_Count=Math.round(Max_Line/20);
                    if (Line_Count % Step_Count==0){
                        float Percent=Math.round(500*Line_Count/Row_Count)/10+50;
                        TCP_Client.client.Send_Msg(0, "", "status","progress2",strUser, Percent+"");
                    }
                    
                    C_Group pGroup=p2.NextElement();
                    //===清空 pFun.bCaculate===
                    TreapEnumerator<C_Function> p4=pGroup.pTreap_Function.GetEnumerator();// .pTreap_Function
                    while(p4.HasMoreElements()){
                        C_Function pFun=p4.NextElement();
                        pFun.dbValue=0;//清空前面的
                    }
                    TreapEnumerator<C_Line> p3=pGroup.pTreap.Elements();
                    boolean bFirstLine=true;
                    C_Line pLine1=null;
                    while(p3.HasMoreElements()){
                        pLine1=p3.NextElement();
                        //===清空 pFun.bCaculate===
                        p4=pGroup.pTreap_Function.GetEnumerator();// .pTreap_Function
                        while(p4.HasMoreElements()){
                            C_Function pFun=p4.NextElement();
                            pFun.bCaculate=false;//代表这个函数没有计算过
                        }
                        ////////////////Sum，Count等函数进行累加
                        for (int i=0;i<pSelectItems.size();i++){
                            SelectExpressionItem pSelect=(SelectExpressionItem) pSelectItems.get(i);
                            Tools.expression_calculate_function(bFirstLine,pSelect.getExpression(),pGroup,pLine1.pLines);
                        }
                        bFirstLine=false;
                    }
                    strLineOutput="";
                    ////////////////最后一次运算结果
                    for (int i=0;i<pSelectItems.size();i++){
                        SelectExpressionItem pSelect=(SelectExpressionItem) pSelectItems.get(i);
                        String strValue=Tools.expression_calculate(bFirstLine,pSelect.getExpression(),pGroup,pLine1.pLines);//);
                        strLineOutput+=strValue+",";
                    }
                    if (strLineOutput.endsWith(",")) strLineOutput=strLineOutput.substring(0,strLineOutput.length()-1);
                    S_File.Write_Line(pFile2, strLineOutput);
                }
            }else{
                if (Row_Count==0) Row_Count=pData.size();//Row_Count 要读取的行数
                int iCount=0;
                Line_Count=1;
                for(int k=iStart;k<pData.size();k++){
                    int Step_Count=Math.round(Max_Line/20);
                    if (Line_Count % Step_Count==0){
                        float Percent=Math.round(500*Line_Count/Row_Count)/10+50;
                        TCP_Client.client.Send_Msg(0, "", "status","progress2",strUser, Percent+"");
                    }
                    
                    iCount+=1;
                    if (iCount % 10000 == 0) out.println(iCount);
                    if (iCount>Row_Count) break;
                    strLineOutput="";
                    C_Line pLine1=pData.get(k);
                    for (int i=0;i<pSelectItems.size();i++){
                        SelectExpressionItem pSelect=(SelectExpressionItem) pSelectItems.get(i);
                        String strLine2=Tools.expression_calculate(k==0,pSelect.getExpression(),null,pLine1.pLines);
                        strLineOutput+=strLine2+",";
                    }
                    if (strLineOutput.endsWith(",")) strLineOutput=strLineOutput.substring(0,strLineOutput.length()-1);
                    S_File.Write_Line(pFile2, strLineOutput);
                }
            }
            TCP_Client.client.Send_Msg(0, "", "status","progress2",strUser, "100");
            //pLoop_Send_Msg.Add_Send_Msg("progress2", strUser,"100");
            Table pFrom=(Table) p.getFromItem();
            out.println("db="+pFrom.getName());
            
            pFile2.Close();

            out.println("file_sql to="+strUser+" finished");
            TCP_Client.client.Send_Msg(0, "", "file_sql",strOutput,strUser,"finished");
            
            Thread.sleep(10*1000);
        } catch (JSQLParserException | InterruptedException ex){
            Logger.getLogger(JavaMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }

    private static String SQL_Replace(String sql) {
        
        sql=sql.replace("\r"," ");
        sql=sql.replace("\n"," ");
        sql=sql.replace("{gt}",">");
        sql=sql.replace("{gte}",">=");
        sql=sql.replace("{lt}","<");
        sql=sql.replace("{lte}","<=");
        return sql;
    }

    
}
