package funnyai;

import com.funnyai.data.C_K_Double;
import com.funnyai.data.TreapEnumerator;
import com.funnyai.data.C_K_Int;
import com.funnyai.data.C_K_Str;
import com.funnyai.data.Treap;
import com.funnyai.io.C_File;
import com.funnyai.io.Old.S_File;
import com.funnyai.io.S_file;
import com.funnyai.io.S_file_sub;
import com.funnyai.string.Old.S_Strings;
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
    
    public static void main(String[] args){
        try {
            String strFile="";
            String sql="select c1,c3,c2 from test";
            String strSep="";
            String strOutput="";
            if (args.length>5){
                strUser=args[0];
                strFile=args[1];
                JavaMain.max_read=Integer.parseInt(args[2]);
                sql=args[3];
                strSep=args[4];
                if (strSep.equals("t")) strSep="\t";
                if (strSep.equals("v")) strSep="\\|";
                strOutput=args[5];
            }else{
                out.println("5个参数 文件 sql t outputfile user");
                return ;
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
           //List<OrderByElement> pOrders=p.getOrderByElements();
            
            
            Loop_Send_Msg pLoop_Send_Msg=new Loop_Send_Msg();
            pLoop_Send_Msg.start();
            
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
                if (Line_Count % 1000==0){
                    float Percent=Math.round(500*Line_Count/max_read)/10;
                    pLoop_Send_Msg.Add_Send_Msg("progress2",strUser, Percent+"");
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
                    if (Line_Count % 1000==0){
                        float Percent=Math.round(500*Line_Count/Row_Count)/10+50;
                        //S_Net.Send_Msg_To_Socket_IO("progress2", Percent+"", "", "");
                        pLoop_Send_Msg.Add_Send_Msg("progress2",strUser, Percent+"");
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
                    if (Line_Count % 1000==0){
                        float Percent=Math.round(500*Line_Count/Row_Count)/10+50;
                        pLoop_Send_Msg.Add_Send_Msg("progress2", strUser,Percent+"");
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
            pLoop_Send_Msg.Add_Send_Msg("progress2", strUser,"100");
            Table pFrom=(Table) p.getFromItem();
            out.println("db="+pFrom.getName());
            
            pFile2.Close();
            pLoop_Send_Msg.bSend=false;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Logger.getLogger(JavaMain.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (JSQLParserException ex){
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
