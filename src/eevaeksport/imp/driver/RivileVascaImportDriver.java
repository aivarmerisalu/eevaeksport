
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class RivileVascaImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public RivileVascaImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
     } catch (Exception e) {
      parent.showError(e);
    }
    
  }
  
  
  private HttpResponse restPostRequest(String data) throws Exception {

		 HttpPost httpPost = new HttpPost("https://api.manorivile.lt/client/v2");
		 if(data != null){
			 httpPost.setEntity(new StringEntity(data,"utf-8"));
			  httpPost.setHeader("ApiKey", parent.getProps().getProperty("api.key") );
		        httpPost.setHeader("Accept", "application/xml");
		        httpPost.setHeader("Content-type", "application/xml;charset=utf-8");
		 }
	        return HttpClients.createDefault().execute(httpPost);

	}

  
  private void connectionUp(){
      try {
    	  
        if (con != null){
          con.close();
        }

           con = DriverManager.getConnection(url + "vasca","sa",  parent.getProps().getProperty("dsn.pass"));//8tL);
      } catch (Exception e) {
            e.printStackTrace();
      }

    }

  private TreeMap getAgentHash() {
	  TreeMap res = new TreeMap();
	  
		String sql1 = null;
		try {
	  	connectionUp();
	  	    sql1 =  "SELECT   (CASE WHEN len(TRIM(N33_SHIP_VIA))=0 or N33_SHIP_VIA IS NULL THEN  N08_KODAS_MS_P ELSE N33_SHIP_VIA END) AS kood , max(N15_PAV) as nimi   \r\n" + 
	        		"        					FROM [VASCA_EUR].dbo.[n08_klij]  left JOIN [VASCA_EUR].dbo.[N33_KBAN] ON N33_KODAS_KS = N08_KODAS_KS left join [VASCA_EUR].dbo.[n15_men] \r\n" + 
	        		"							on N15_KODAS_MS =(CASE WHEN len(TRIM(N33_SHIP_VIA))=0 or N33_SHIP_VIA IS NULL THEN  N08_KODAS_MS_P ELSE N33_SHIP_VIA END)\r\n" + 
	        		"						 group by (CASE WHEN len(TRIM(N33_SHIP_VIA))=0 or N33_SHIP_VIA IS NULL THEN  N08_KODAS_MS_P ELSE N33_SHIP_VIA END)\r\n" + 
	        		"						order by  max(N15_PAV)";	
		
	    System.out.println(sql1);
	    

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      String acc = rs1.getString("kood") ;
	      if(acc != null) {
	      String asi = Converter.cleanLitLatEst(acc.trim());
	      res.put(asi, acc.trim());
	      
	      }
	    }
	    rs1.close();
	    st1.close();
	   con.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println(res.toString());
	  return res;
	  
  }
  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
	 
  
  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	connectionUp();
  	    sql1 = "SELECT  trim(N08_KODAS_KS) AS kood1, (case when (N33_EIL_NR) is null then '1' else trim(str(N33_EIL_NR)) end) AS kood " +
	  " FROM [VASCA_EUR].dbo.[n08_klij]  left JOIN [VASCA_EUR].dbo.[N33_KBAN] ON N33_KODAS_KS = N08_KODAS_KS " +
	  " where right(rtrim(N33_PAV),3) =  '" + id + "'";	
	
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood1").trim() + "-" +rs1.getString("kood").trim() ;

    }
    rs1.close();
    st1.close();
   con.close();
   // System.out.println("Klient: " + acc);
    return acc;

  }

  public String identifyCustomerName(String id) throws SQLException {
	  	 String	sql1 = 
	  	"  SELECT N08_PAV as aaa FROM [VASCA_EUR].dbo.[n08_klij]  WHERE " + 
	 		"  	 		  ltrim(N08_KODAS_KS) = '" + id +"'";
	    System.out.println(sql1);
	    String acc = null;

	    this.connectionUp();
	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      acc = rs1.getString("aaa").replace("\"", "-").trim();

	    }
	    rs1.close();
	    st1.close();
con.close();
	    
	    return acc;

	  }
  
  public String identifyCustomerADDR(String id) throws SQLException {
  	 String	sql1 = "  SELECT N08_ADR as aaa FROM [VASCA_EUR].dbo.[n08_klij]  WHERE " + 
  	 		"  	 		  ltrim(N08_KODAS_KS) = '" + id +"'"; 
    System.out.println(sql1);
    String acc = null;
    this.connectionUp();
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      acc = rs1.getString("aaa").trim();

    }
    rs1.close();
    st1.close();
    con.close();
    return acc;
  }
  
 
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
  	
    
    String sql1 = " SELECT N17_KODAS_PS AS toode ,N17_KOD_T AS ean " +
     " FROM [VASCA_EUR].dbo.[n17_prod] a LEFT JOIN  [VASCA_EUR].dbo.[n27_pak] ON n27_kodas_ps=n17_kodas_ps " +
   	"  WHERE N17_KOD_T = '" + ean.trim() + "'";

    String sql2 = " SELECT N17_KODAS_PS AS toode ,N17_KOD_T AS ean " +
    " FROM [VASCA_EUR].dbo.[n17_prod] a  LEFT JOIN  [VASCA_EUR].dbo.[n27_pak] ON n27_kodas_ps=n17_kodas_ps " +
   	"  WHERE  N17_KODAS_PS = '" + prodID.trim() + "'";

    String prod = null;
 this.connectionUp();
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }
    rs1.close();
   System.out.println(ean + " prod0 " + prod);
    if(prod == null){

      rs1 = st1.executeQuery(sql2);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
      }
    }

    System.out.println(prodID + " prod1 " + prod);
    rs1.close();
    st1.close();

    con.close();
    return prod;
  }

  private void setOrder(Order order){
	     this.order = order;
	   }
  private Order order = null;
  
  private TreeMap agh  = null;
  
  public void makeOrder(Order order) throws SQLException{
 
	  setOrder(order);
	     
	     String salesid = null ;
	  if(agh == null) {   
	   agh = getAgentHash();
	  }
    makeOutput(order,agh);
  }
  
  
 private boolean addTransa(Order ord) {
	 
	 return false;
 }


 private void makeOutput(Order order, TreeMap agh){
 	StringBuffer sb = new StringBuffer();
    boolean addtransa = addTransa(order);
    
 	String ls = System.getProperty("line.separator");
 	try {
 	  int cou = 1;
      ////sb.append(getLineNr(cou));
      sb.append("       <I06>" + ls);
      cou++;

      ////sb.append(getLineNr(cou));
      sb.append("          <I06_OP_TIP>53</I06_OP_TIP>" + ls);
      cou++;

      ////sb.append(getLineNr(cou));
      sb.append("          <I06_PVM_TIP>0</I06_PVM_TIP>" + ls);
      cou++;
      String cust = Converter.deCode(order.getCompany());

      
      String agent1 = order.getAgent();
      
      //sb.append(getLineNr(cou));
      String agent = agent1;
      
      if(agh != null && agh.get(agent1) != null) {
    	  agent = agh.get(agent)+"";
      }
      
      sb.append("          <I06_KODAS_MS>" + escape(agent) + "</I06_KODAS_MS>" + ls);
      cou++;
      String name =  null;
  	try {
  		
  		
  		name = identifyCustomerName(escape(cust.substring(0,cust.lastIndexOf("-"))));
  		if(name == null){
  			name ="nonam";
  		}else{
  			name = name.replaceAll("UAB", " ").trim();
  	  		if(name.length()>5){
  	  			name = name.substring(0,5).trim();
  	  		}	
  		}
  		
  	} catch (SQLException e1) {
  		e1.printStackTrace();
  	}

      //sb.append(getLineNr(cou));
      sb.append("          <I06_DOK_NR>U" +  escape(agent1)+ "-" + name + "-"  +  escape(order.getDokNr())  + "</I06_DOK_NR>" + ls);
      cou++;
      

      //sb.append(getLineNr(cou));
      sb.append("          <I06_OP_DATA>" +  getDateString(Calendar.getInstance()) + "</I06_OP_DATA>" + ls);
      cou++;
      
      
      //sb.append(getLineNr(cou));
      sb.append("          <I06_DOK_DATA>" +  getDateString(Calendar.getInstance()) + "</I06_DOK_DATA>" + ls);
      cou++;
  


	//sb.append(getLineNr(cou));
      sb.append("          <I06_KODAS_KS>" + escape(cust.substring(0,cust.lastIndexOf("-"))) + "</I06_KODAS_KS>" + ls);
      cou++;
      
      String addr =  null;
	try {
		addr = identifyCustomerADDR(escape(cust.substring(0,cust.lastIndexOf("-"))));
	} catch (SQLException e1) {
		e1.printStackTrace();
	}
	//sb.append(getLineNr(cou));
      sb.append("          <I06_ADR2>" + escape(addr) + "</I06_ADR2>" + ls);
      cou++;
      
      String lisa = null;
      if(order.getInfo() != null && order.getInfo().trim().length() > 0) {
    	  lisa = order.getInfo().trim();
      }
      if(lisa != null){
    	  //sb.append(getLineNr(cou));
          sb.append("          <I06_PASTABOS>" + escape(lisa) + "</I06_PASTABOS>" + ls);
          cou++;  
      }
     
    Vector lines = order.getOrderLines();
      for (int i = 0; i < lines.size(); i++) {
      	 //sb.append(getLineNr(cou));
         sb.append("          <I07>" + ls);
         cou++;
        
          
      	OrderLine line = (OrderLine)lines.elementAt(i);
          String toode = line.getToode();
          double ale = line.getDiscount();
          if (ale == -1 ){
          ale = 0 ;
              }
          if(ale == -1 ){
        	  ale = 100;
          }
          
          //sb.append(getLineNr(cou));
          sb.append("               <I07_TIPAS>1</I07_TIPAS>" + ls);
          cou++;
          /*
          Fillial 01   -  agent 007
Fillial 02   -  agent 020
Fillial 03    - agent 016*/
          String ladu = order.getWarehouse();
          
if(ladu.trim().length()!=0){
	
	
          //sb.append(getLineNr(cou));
          sb.append("               <I07_KODAS_IS>" + ladu  + "</I07_KODAS_IS>" + ls);
          cou++;

}  else {
	 //sb.append(getLineNr(cou));
     sb.append("               <I07_KODAS_IS>CENTRINIS</I07_KODAS_IS>" + ls);
     cou++;
}



          //sb.append(getLineNr(cou));
          sb.append("               <I07_KODAS>" +  escape(toode) + "</I07_KODAS>" + ls);
          cou++;
          
          //sb.append(getLineNr(cou));
          sb.append("               <I07_KIEKIS>" + escape(nf.format(line.getKogus())) + "</I07_KIEKIS>" + ls);
          cou++;
         
       //   //sb.append(getLineNr(cou));
       //   sb.append("               <I07_KAINA_BE>" + escape(getConc().getNF().format(price)) + "</I07_KAINA_BE>" + ls);
       //   cou++;
  if (ale > 0 ){
  	
          //sb.append(getLineNr(cou));
          sb.append("               <I07_NUOLAIDA>" + escape(nf.format(ale)) + "</I07_NUOLAIDA>" + ls);
          cou++;

  }     else if(ale == -2){
	  //sb.append(getLineNr(cou));
      sb.append("               <I07_NUOLAIDA>100</I07_NUOLAIDA>" + ls);
      cou++;
  }
         // //sb.append(getLineNr(cou));
         // sb.append("               <I07_MOKESTIS_P>" + prod.getVat() + "</I07_MOKESTIS_P>" + ls);
         // cou++;
          
         // //sb.append(getLineNr(cou));
         // sb.append("               <I07_SUMA>" + escape(getConc().getNF().format((line.getAmount()* price)*(100-ale)/100)
          //		)+ "</I07_SUMA>" + ls);
          //cou++;
          

  addNumLine();

          //sb.append(getLineNr(cou));
          sb.append("          </I07>" + ls);
          cou++;
      }
      


      //sb.append(getLineNr(cou));
      sb.append("          </I06>" + ls);
      cou++;

 	
      String fileName = escape(parent.getProps().getProperty("conv.dir") +"response/"+ order.getAgent()) + "-" + name + "-" + ( Calendar.getInstance().getTimeInMillis()+"").substring(5,13)   +  ".EIP";
      System.out.println(fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(sb.toString().getBytes());
        fos.close();
        
    	try {
        


        String data = "	<body><method>EDIT_I06_FULL</method><data>" + sb.toString() + "</data>" + 
        		"</body>";
        System.out.println(data);
        
        HttpResponse aaa = restPostRequest(data);
       
        String sbb = EntityUtils.toString(aaa.getEntity());
        System.out.println("Response " + sbb);
     //  StringBuffer sbb = postData(data);
     //  System.out.println("Response " + sbb);

        String cat = parent.getProps().getProperty("conv.dir")+"response/";
        
        
        fileName = escape(cat + order.getAgent())  + "-" + ( Calendar.getInstance().getTimeInMillis()+"").substring(5,13)   +  "response.txt";
       System.out.println(fileName);
          fos = new FileOutputStream(fileName);
         fos.write(sbb.toString().getBytes());
         fos.close();
         if(sbb.contains("<h1>Error: Server Error") || sbb.contains("Connection refused") ||  sbb.contains("connect timed out") ||  sbb.contains("errorMessage")) {
            System.exit(0);	
         }
         if(sbb.contains("<errorMessage></errorMessage>") ) {
             System.exit(0);	
          }
       
         
    	} catch (Exception o) {
    		o.printStackTrace();
    		this.log.info(o);
    	
    		System.exit(0);
    	}
	} catch (Exception e) {
		e.printStackTrace();
	}
 }
  
  public String getDriverVersion(){
    return "RivileVascaImport";
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

  
  private String getLineNr(int i){
  	String res = i + "";
  	if (res.length() == 1){
  		res = "00000" + res;
  	} else if (res.length() == 2){
  		res = "0000" + res;
  	} else if (res.length() == 3){
  		res = "000" + res;
  	} else if (res.length() == 4){
  		res = "00" + res;
  	} else if (res.length() == 5){
  		res = "0" + res;
  	}
  	return res;
  }
  
  private String getDateString(Calendar cal){
    StringBuffer sb = new StringBuffer();
    sb.append(cal.get(Calendar.YEAR) + ".");
    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
    if (month.length() == 1){
      sb.append("0");
    }
    sb.append(month + ".");
//2005.09.05 00:00
    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    if (day.length() == 1){
      sb.append("0");
    }
    sb.append(day );

    return sb.toString();
  }

  private String url = "jdbc:odbc:";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private ImportPanel parent;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
