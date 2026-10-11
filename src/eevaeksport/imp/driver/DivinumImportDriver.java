
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class DivinumImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public DivinumImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    nf.setGroupingUsed(false);
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url);
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  public String getImportFile(){
	   return parent.getProps().getProperty("imp.read");
	 }
  
  private String norm(String id) {
	  id = id.replaceAll("�", "o");
	  id = id.replaceAll("�", "a");
	  id = id.replaceAll("�", "u");
	  id = id.replaceAll("�", "o");
	  return id;
  }

  public String identifyCustomer(String id,String regnr) throws SQLException {
	  
	//  SELECT  f.kood  FROM osmy/firma f  
	  //   where partner='500'  order by f.nimi
	  

	     
	  String sql1 = " SELECT  f.kood  FROM osmy/firma f  where  aadr2= '" + (norm(id)) + "'";
	    System.out.println(sql1);
	    String acc = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      acc = rs1.getString("kood").trim();

	    }
	    rs1.close();
	    st1.close();

	    
	   //if(id.length() > 6){
		//	  id = id.substring(id.length()-6);
		 // }
	    
	  
	    if(acc == null){
	    	sql1 = " SELECT  f.kood  FROM osmy/firma f  where  partner= '" + id + "'";
	    	 st1 = con.createStatement();
	  	   rs1 = st1.executeQuery(sql1);

	  	    while (rs1.next()) {
	  	      acc = rs1.getString("kood").trim();

	  	    }
	  	    rs1.close();
	  	    st1.close();
	    }
	    System.out.println(sql1);
	
	    System.out.println("Klient: " + acc);
	    return acc;


  }

  public String identifyCustomerADDR(String id) throws SQLException {  	
  return id ;

  }
  
  public String identifyCustomerAGENT(String id) throws SQLException {
	  String sql1 = " SELECT  agent as kood  FROM osmy/firma f  where  partner= '" + id + "'";
	    System.out.println(sql1);
	    String acc = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      acc = rs1.getString("kood").trim();

	    }
	    rs1.close();
	    st1.close();

	    System.out.println("Agent: " + acc);
	    return acc;

 }
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
	 
	  String sql1 = "SELECT a.siffer AS toode,  a.nimi AS nimetus,a.kassak as ean FROM osmy/lm_mat a " +
       " WHERE  (  tkood <> 'F' and  a.mghind  != 0 ) and kassak = '" + ean + "' ORDER by a.nimi";

	  String sql3 = "SELECT a.siffer AS toode,  a.nimi AS nimetus,a.kassak as ean FROM osmy/lm_mat a " +
      " WHERE  (  tkood <> 'F' and  a.mghind  != 0 ) and kassak = '0" + ean + "' ORDER by a.nimi";
	    String sql2 = "SELECT a.siffer AS toode,  a.nimi AS nimetus,a.kassak as ean FROM osmy/lm_mat a " +
       " WHERE  (  tkood <> 'F' and  a.mghind  != 0 ) and siffer = '" + prodID + "' ORDER by a.nimi";
	    String sql4 = "SELECT a.siffer AS toode,  a.nimi AS nimetus,a.kassak as ean FROM osmy/lm_mat a " +
	       " WHERE  (  tkood <> 'F' and  a.mghind  != 0 ) and kassak = '" + prodID + "' ORDER by a.nimi";

	    String prod = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      prod = rs1.getString("toode").trim();
	    }
	//System.out.println("prod0" + prod);
	    if(prod == null){

		      rs1 = st1.executeQuery(sql3);

		      while (rs1.next()) {
		        prod = rs1.getString("toode").trim();
		      }
		    }
	    
	    if(prod == null){

	      rs1 = st1.executeQuery(sql2);

	      while (rs1.next()) {
	        prod = rs1.getString("toode").trim();
	      }
	    }
	    
	    if(prod == null){

		      rs1 = st1.executeQuery(sql4);

		      while (rs1.next()) {
		        prod = rs1.getString("toode").trim();
		      }
		    }

	    //System.out.println("prod1" + prod);
	    rs1.close();
	    st1.close();

	    return prod;
	  }


 
  public void makeOrder(Order order) throws SQLException{
 
    makeOutput(order);
  }
 
  public static String deCode(String res ){
	   
	   while(res.indexOf("11111xx") != -1){
	   	int aa = res.indexOf("11111xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   }
	   while(res.indexOf("11112xx") != -1){
	   	
	   	int aa = res.indexOf("11112xx");
	   //	System.out.println(aa + " " +res);
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11113xx") != -1){
	   	int aa = res.indexOf("11113xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11114xx") != -1){
	   	int aa = res.indexOf("11114xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11115xx") != -1){
	   	int aa = res.indexOf("11115xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11116xx") != -1){
	   	int aa = res.indexOf("11116xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11117xx") != -1){
	   	int aa = res.indexOf("11117xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11118xx") != -1){
	   	int aa = res.indexOf("11118xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 

	   
	   while(res.indexOf("11119xx") != -1){
		   	int aa = res.indexOf("11119xx");
		   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
		   } 

	   while(res.indexOf("11120xx") != -1){
		   	int aa = res.indexOf("11120xx");
		   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
		   } 

	   

	   while(res.indexOf("11121xx") != -1){
		   	int aa = res.indexOf("11121xx");
		   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
		   } 	   

	   while(res.indexOf("11122xx") != -1){
		   	int aa = res.indexOf("11122xx");
		   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
		   } 
	
	return res;
}

  private String clear (String ss){
	  //	System.out.println(ss);
	    String ret =   ss.replace(',','.');
	    ret =  ret.replace(',','.');
	    ret =  ret.replace(',','.');
	    ret =  ret.replace(',','.');
	    ret =  ret.replace('"',' ').trim();
	    ret =  ret.replace('"',' ').trim();
	    ret =  ret.replace('"',' ').trim();


	    ret = ret.replaceAll("�","11111xx");

	    ret = ret.replaceAll("�","11112xx");

	    ret = ret.replaceAll("�","11113xx");


	    ret = ret.replaceAll("�","11114xx");

	    ret = ret.replaceAll("�","11115xx");

	    ret = ret.replaceAll("�","11116xx");

	    ret = ret.replaceAll("�","11117xx");

	    ret = ret.replaceAll("�","11118xx");
	    ret = ret.replaceAll("�","11119xx");
	    //�mon�
	    ret = ret.replaceAll("�","11130xx");
	    ret = ret.replaceAll("�","11120xx");
	    ret = ret.replaceAll("�","11121xx");
	    ret = ret.replaceAll("�","11122xx");
	    ret = ret.replaceAll("�","11123xx");
	    ret = ret.replaceAll("�","11124xx");
	//O?
	    ret = ret.replaceAll("�","11224xx");
	    ret = ret.replaceAll("�","11225xx");
	    ret = ret.replaceAll("�","11226xx");
	    ret = ret.replaceAll("�","11227xx");
	    ret = ret.replaceAll("�","11228xx");
	    ret = ret.replaceAll("�","11229xx");
	//�
	    ret = ret.replaceAll("11224xx","�");
	    ret = ret.replaceAll("11225xx","�");
	    ret = ret.replaceAll("11226xx","�");
	    ret = ret.replaceAll("11227xx","�");
	    ret = ret.replaceAll("11228xx","�");
	    ret = ret.replaceAll("11229xx","�");
	    
	    ret = ret.replaceAll("11130xx","�");
	    ret = ret.replaceAll("11119xx","�");
	    ret = ret.replaceAll("11120xx","�");
	    ret = ret.replaceAll("11121xx","�");
	    ret = ret.replaceAll("11122xx","�");
	    ret = ret.replaceAll("11123xx","�");
	    ret = ret.replaceAll("11124xx","�");
	    ret = ret.replaceAll("11118xx","�");
	    ret = ret.replaceAll("11119xx","�");
	    ret = ret.replaceAll("11117xx","�");
	    ret = ret.replaceAll("11116xx","�");
	    ret = ret.replaceAll("11115xx","�");
	    ret = ret.replaceAll("11114xx","�");
	    ret = ret.replaceAll("11113xx","�");
	    ret = ret.replaceAll("11111xx","�");
	    ret = ret.replaceAll("11112xx","�");
	    if(ret == null || ret.trim().length()==0){
	    return " ";
	    }else {
	    return ret;
	    }
	  }

  private void makeOutput(Order order){
	    
	  String company = order.getCompany();
	 	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    
	      try {
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.newDocument();
	        Element root = doc.createElement("tellimus");
	        doc.appendChild(root);
    String comment = " ";
	        
	        if(order.getInfo() != null){
	        	comment = order.getInfo().replaceAll("\n"," ");
	        	comment = order.getInfo().replaceAll(","," ");
	        }
	    if(comment.length()>70){
	    //	comment = comment.substring(0,70);
	    }
	        
	        Node nr = doc.createElement("nr");
	        Calendar calv = Calendar.getInstance();//order.getDokNr()
	        Text nrVal = doc.createTextNode(comment);
	        nr.appendChild(nrVal);
	        root.appendChild(nr);

	        Node kuupaev = doc.createElement("kuupaev");
	        Calendar cal = Calendar.getInstance();
	        String month = (cal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	        String day = cal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;

	        String dateString = cal.get(Calendar.YEAR) +""+ month +""+ day;
	        Text kuupaevVal = doc.createTextNode(dateString);
	        kuupaev.appendChild(kuupaevVal);
	        root.appendChild(kuupaev);

	        /*
	        Node curr = doc.createElement("OrderCurrency");
	        Text currVal = doc.createTextNode("LTL");
	        curr.appendChild(currVal);
	        header.appendChild(curr);*/
	        
	        
	        Node mkuupaev = doc.createElement("tarne-kuupaev");
	        Calendar mcal = Calendar.getInstance();
	        try{
	        	 mcal.setTime(order.getShipCal().getTime());
	        }catch (Exception e){
	        	
	        }
	       

	         month = (mcal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	         day = mcal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;

	         String mdateString = cal.get(Calendar.YEAR)+"" + month +""+ day;
	        Text mkuupaevVal = doc.createTextNode(mdateString);
	        mkuupaev.appendChild(mkuupaevVal);
	        root.appendChild(mkuupaev);
	        
	        
	        
	        
	     
	        
	        String agent = identifyCustomerAGENT(company);
	    
	        
	        Node delivery = doc.createElement("tellija-kood");
	     
	        Text deliveryVal = doc.createTextNode(company);
	        delivery.appendChild(deliveryVal);
	        root.appendChild(delivery);
	        
	        
	   	
	     

  
	        if(agent != null){
	        	   Node koostaja = doc.createElement("koostaja");
	   	        root.appendChild(koostaja);
	   	        Node agentid = doc.createElement("isiku-id");
	   	        Text agetnVal = doc.createTextNode(agent);
	   	        agentid.appendChild(agetnVal);
	     	        koostaja.appendChild(agentid);
	   	        
	   	        	
	        }
	 
	    
	        if(comment.trim().length()>0){
	            Node markused = doc.createElement("markused");
		        Text markusedVal = doc.createTextNode(" ");
		        markused.appendChild(markusedVal);
		        root.appendChild(markused);
		    }
	        

	        
	        //RIDADE LISAMINE
	        Node read = doc.createElement("read");

	        int coun = 0;
	        Iterator iter = order.getOrderLines().iterator();
	        while (iter.hasNext()){
	          OrderLine line = (OrderLine)iter.next();
	          String toode  = line.getToode();
	 	     if(toode!= null){
	 	    	 toode = toode.trim();
	 	     }else{
	 	    	 toode = "";
	 	     }
	          if(line.getKogus()!= 0 && !toode.equals("")){
	          	
	        	  
	        	  coun++;
	          	addLine(line, read, doc, coun , order);
	          }
	          }

	        root.appendChild(read);

	        long uni = Calendar.getInstance().getTimeInMillis();
	 	
	      String fileName = parent.getProps().getProperty("conv.dir") + "t-mj-" + order.getAgent()+ "-"+ order.getCompany() + "-" + uni +  ".xml";
	      System.out.println(fileName);
	      OutputFormat of = new OutputFormat();
	        of.setIndenting(true);
	        of.setLineSeparator(System.getProperty("line.separator"));
	        //<?xml version="1.0" encoding="windows-1257"?>  <?xml version="1.0" encoding="windows-1257" ?>
	        of.setEncoding("windows-1257");
	        XMLSerializer ser = new XMLSerializer(of);
	        FileOutputStream fos = new FileOutputStream(fileName);
	        PrintWriter out = new PrintWriter(fos);
	        ser.setOutputCharStream(out);
	        ser.serialize(doc);
	        out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	 }

 
  public String getDriverVersion(){
    return "DivinumImport";
  }

  private void addLine(OrderLine line, Node read, Document doc,int counter ,Order order) throws SQLException{

	  Node rida = doc.createElement("rida");
	    read.appendChild(rida);


	/*    Node linenum = doc.createElement("LineNumber");
	    Text linnum = doc.createTextNode(counter+"");
	    linenum.appendChild(linnum);
	    ridaitem.appendChild(linenum);
*/

	    Node toote_ean = doc.createElement("ean-kood");
	    Text toote_eanVal = doc.createTextNode(line.getEAN());
	    toote_ean.appendChild(toote_eanVal);
	    rida.appendChild(toote_ean);

	     
	     String toode  = line.getToode();
	     if(toode!= null){
	    	 toode = toode.trim();
	     }else{
	    	 toode = "";
	     }

	    Node toote_kood = doc.createElement("toote-kood");
	    Text toote_koodVal = doc.createTextNode(toode);
	    toote_kood.appendChild(toote_koodVal);
	    rida.appendChild(toote_kood);

   String nimetu = escape(line.getTooteNimi().replace('`',' ').replace('�',' '));

	    Text nimetusVal = doc.createTextNode(nimetu);
	    Node nimetus = doc.createElement("nimetus");
	    nimetus.appendChild(nimetusVal);
	    rida.appendChild(nimetus);

	    
	    double kogus = line.getKogus();
	    Node koGus = doc.createElement("kogus");
	    Text kogusVal = doc.createTextNode(getNF().format(kogus));
	    koGus.appendChild(kogusVal);
	    rida.appendChild(koGus);


	/*    Node yhik = doc.createElement("yhik");
	    Text yhikVal = doc.createTextNode(line.getUnit());
	    yhik.appendChild(yhikVal);
	    rida.appendChild(yhik);
	   
	    Node price = doc.createElement("yhiku-hind");
	    Text priceVal = doc.createTextNode(getNF().format(0));
	    price.appendChild(priceVal);
	    rida.appendChild(price);
	    */ 

	    Node km = doc.createElement("km");
	    Text kmVal = doc.createTextNode(getNF().format(20));
	    km.appendChild(kmVal);
	    rida.appendChild(km);
	     
	     addNumLine(); 
	   }


 

  private String escape(String s){
    if (s == null) return s;
    StringBuffer sb = new StringBuffer();
    for (int i = 0 ; i < s.length() ; i++){
      char c = s.charAt(i);
      if (c == '&'){
        sb.append("&amp;");
      } else if (c == '<'){
        sb.append("&lt;");
      } else if (c == '>'){
        sb.append("&gt;");
      } else if (c == '\n'){
        sb.append(" ");
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

 
  
 
  public NumberFormat getNF() {
		return nf;
	}
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private ImportPanel parent;

  private String             url = "jdbc:odbc:Divinum";
  private Connection con;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
