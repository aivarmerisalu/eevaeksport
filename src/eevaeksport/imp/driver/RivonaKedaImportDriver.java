
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.Email;
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

public class RivonaKedaImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public RivonaKedaImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "rivona00_centras");

    }
    catch (Exception e) {
      parent.showError(e);
    }
  }
  private Vector splitOrder (Order order){
	  Vector result = new Vector();
	 String doknr = getNextDoknr();
	 if(doknr == null){
		 doknr = getNextDoknr();
	 }
	 if(doknr == null){
		 doknr = getNextDoknr();
	 }
	 if(doknr == null){
		 doknr = getNextDoknr();
	 }
	 
	 if(doknr.length() == 2){
		 doknr = "0000" + doknr;
	 }
	 if(doknr.length() == 3){
		 doknr = "000" + doknr;
	 }
	 if(doknr.length() == 4){
		 doknr = "00" + doknr;
	 }
	 if(doknr.length() == 5){
		 doknr = "0" + doknr;
	 }
	  Order ord01 = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ord05 = new Order(order.getCompany(),order.getAgentFirstName());	
		 
	  
	  ord01.setAgentCode(order.getAgentCode());
	  ord01.setDokNr("UZSVY1" +doknr);
	  ord01.setInfo(order.getInfo());
	  ord01.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord01.setStatus(order.getStatus());
	  ord01.setPaymTerm(order.getPaymTerm());
	  ord01.setChanged(true); // et teaksime et on tegemita tubakaga T tähe lisamiseks
	  ord01.setWarehouse("VY000001");
	  ord01.setTeostaja(order.getDokNr());
	  
	  ord05.setAgentCode(order.getAgentCode());
	  ord05.setDokNr("UZSND1" +doknr);
	  ord05.setInfo(order.getInfo());
	  ord05.setPaymTerm(order.getPaymTerm());
	  ord05.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord05.setStatus(order.getStatus());
	  ord05.setWarehouse("ND000001");
	  ord05.setTeostaja(order.getDokNr());
		  
	  	  Vector lines = order.getOrderLines();
	  Enumeration enumr = lines.elements();
	  while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
	
		     String itemid = lin.getToode();
		     
		    String ware =  identifyWare(itemid,lin.getKogus());
		    
		    System.out.println("Splitter " + ware );
		    
		     if(ware.equals("VY000001") ){ 		     	    
		    	 	  ord01.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);			 
		     } else if (ware.equals("ND000001")){
		    		 ord05.add(lin.getToode().substring(3), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		    				 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		      }
			     			
			     }
               





	  if(ord01.getOrderLines() != null && ord01.getOrderLines().size()>0){
		  result.add(ord01);  
	  }

	  if(ord05.getOrderLines() != null && ord05.getOrderLines().size()>0){
		  result.add(ord05);  
	  }
	
	 
	 System.out.println("Result =" +order.getDokNr()
			 + " "+ result.size());
	 //System.exit(0);
	  return result ;
  }

  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	
  	    sql1 = "SELECT  trim(N08_KODAS_KS) AS kood1, (case when (N33_kodas_ak) is null then '1' else trim(str(N33_kodas_ak)) end) AS kood " +
	  " FROM n08_klij  JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS " +
	  " where N08_KODAS_LS_4 = '" + id + "'";	
  	  
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood1").trim() + "-" +rs1.getString("kood").trim() ;
    }
    rs1.close();
    st1.close();

   // System.out.println("Klient: " + acc);
    return acc;

  }

  public String identifyWare(String id , double kogus)  {
	  	 String res = "VY000001";

		if(id.startsWith("AL-")){
			res = "ND000001";

		}
	   // System.out.println("Klient: " + acc);
	    return res;

	  }
  
  private String  getNextDoknr(){
	  String res = null;
	  	String   sql1 = "select order_nr as numbe from dbo.MR1_DOK_NR where dev_id = 'ANDROID'";	
  	  
    System.out.println(sql1);
  
	try {
		Connection  con = DriverManager.getConnection(url + "rivona00_centras");
		Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	res = rs1.getString("numbe");
	    }
	    rs1.close();
	    sql1 = " update  dbo.MR1_DOK_NR set order_nr = order_nr +1,last_update = getdate() where dev_id = 'ANDROID' ";	
	    st1.executeUpdate(sql1);

	   
	    st1.close();
	    con.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  return res ;
  }
  
  public String identifyCustomerADDR(String id) throws SQLException {
  	 String	sql1 = "SELECT N33_ADRESAS AS aadress FROM n08_klij JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
  	 		"  trim(N08_KODAS_KS)||'-'|| N33_kodas_ak = '" + id +"'"; 
  	
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("aadress").trim();

    }
    rs1.close();
    st1.close();

    return acc;

  }
  


 
  public String identifyProduct(String ean, String prodID) throws SQLException{
  	
    
    String sql1 = " SELECT N17_KODAS_PS AS toode ,N17_KOD_T AS ean FROM n17_prod a   " +
   	"  WHERE  N17_KODAS_PS = '" + ean.trim().substring(0,ean.trim().length()-1) + "'";

    String sql2 = " SELECT N17_KODAS_PS AS toode ,N17_KOD_T AS ean " +
    " FROM n17_prod a  ,  n27_pak " +
   	"  WHERE  n27_kodas_ps=n17_kodas_ps  and  N17_KODAS_PS = '" + prodID.trim() + "'";

    
    String sql3 = " SELECT N17_KODAS_PS AS toode ,N17_KOD_T AS ean FROM n17_prod a   " +
   	"  WHERE  N17_KODAS_PS = '" + ean + "'";

    String prod = null;

    try {
    	
    System.out.println(sql1);
    Statement st1 = con.createStatement();  
    ResultSet rs1 = st1.executeQuery(sql1);
  
    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }
   System.out.println(ean + " prod0 " + prod);
   

    if(prod == null){

    	   Statement st2 = con.createStatement();
    	   ResultSet rs2 = st2.executeQuery(sql2);
      while (rs2.next()) {
        prod = rs2.getString("toode").trim();
      }
      rs2.close();
      st2.close();
      
    }
    
    
    if(prod == null){

        Statement st3 = con.createStatement();
        ResultSet rs3 = st3.executeQuery(sql3);
        while (rs3.next()) {
          prod = rs3.getString("toode").trim();
        }
        rs3.close();
        st3.close();
    }

    System.out.println(prodID + " prod1 " + prod);
    rs1.close();
    st1.close();
    }catch (Exception e){
    	e.printStackTrace();
    }

    return prod;
  }
  private HashMap bcodes =  null;

  
  public TreeMap getAgents() {
      TreeMap agents = new TreeMap();
      
      String  cmd = "SELECT N35_KODAS_LS AS kood ,N35_PAV AS nimi from  N35_LGST " +
           "  WHERE N35_TEVAS = 'K AGENTAI'  "+
			 "    ORDER by N35_PAV  ";
  

try{
	   Statement st1 = con.createStatement();
	   ResultSet rs = st1.executeQuery(cmd);
          while (rs.next()) {
          	String id = rs.getString("kood").trim();
            Agent agent = new Agent(id);
            String name = rs.getString("nimi").trim();
            agent.setName(name);
            agents.put(agent.getID(), agent);
           
          }
          rs.close();
          st1.close();
      } catch (Exception e) {
  e.printStackTrace();
      }
     
      return agents;
  
  }
  
  
  
  public void makeOrder(Order order) throws SQLException{
	   
    makeOutput(order);
    String agname = order.getAgent();
	TreeMap ags =   getAgents();
	if(ags.get(order.getAgent()) != null){
		agname = ((Agent)ags.get(order.getAgent())).getName();
	}
    sendKinnitusEmail(order.getCompany(),getNimi(order.getCompany()),order.getOrderLines(), order.getAgent() ,
			  order.getInfo(), getAddress(order.getCompany()),agname);
    
  }

  
  
  public String getNimi(String id) throws SQLException {
	  String cmd = "  SELECT    ltrim(N08_KODAS_KS) AS kood1, (case when (N33_kodas_ak) is null then '1' else ltrim(str(N33_kodas_ak)) end) AS kood,  " +    		
      		"rtrim(N08_PAV) + ' ' + rtrim(N33_kodas_ak) AS nimetus, " +
				" rtrim(N33_ADRESAS)  AS aadress, N33_KODAS_LS_3 AS visit ,N08_KODAS_KS AS payer ,N08_IM_KODAS as regnum, " +
				"  (CASE WHEN LENGTH(LTRIM(N33_KODAS_LS_4))=0 or N33_KODAS_LS_4 IS NULL THEN  N08_KODAS_LS_4 ELSE  N33_KODAS_LS_4 END) AS agent , " +
				" '' as paymterm , N08_PVM_KODAS as vatno , n08_credit_lim as credit " +
				"  FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS  where N08_KODAS_KS ='" + id.substring(0,id.indexOf("-"))+ "'  and   " +
				"  ((CASE WHEN LENGTH(LTRIM(N33_KODAS_LS_4))=0 or N33_KODAS_LS_4 IS NULL THEN  N08_KODAS_LS_4 ELSE  N33_KODAS_LS_4 END) != '') " ;
				 
	   System.out.println(cmd);
	   String agent = null;

	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(cmd);

	   while (rs1.next()) {
	   	agent = rs1.getString("nimetus").trim();

	   }
	   rs1.close();
	   st1.close();
  
	   return agent;

	 }
  
  public String getAddress(String id) throws SQLException {
	  String cmd = "  SELECT    ltrim(N08_KODAS_KS) AS kood1, (case when (N33_kodas_ak) is null then '1' else ltrim(str(N33_kodas_ak)) end) AS kood,  " +    		
	      		"rtrim(N08_PAV) + ' ' + rtrim(N33_kodas_ak) AS nimetus, " +
					" rtrim(N33_ADRESAS)  AS aadress, N33_KODAS_LS_3 AS visit ,N08_KODAS_KS AS payer ,N08_IM_KODAS as regnum, " +
					"  (CASE WHEN LENGTH(LTRIM(N33_KODAS_LS_4))=0 or N33_KODAS_LS_4 IS NULL THEN  N08_KODAS_LS_4 ELSE  N33_KODAS_LS_4 END) AS agent , " +
					" '' as paymterm , N08_PVM_KODAS as vatno , n08_credit_lim as credit " +
					"  FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS  where N08_KODAS_KS ='" + id.substring(0,id.indexOf("-")) + "'  and   " +
					"  ((CASE WHEN LENGTH(LTRIM(N33_KODAS_LS_4))=0 or N33_KODAS_LS_4 IS NULL THEN  N08_KODAS_LS_4 ELSE  N33_KODAS_LS_4 END) != '') " ;
					 
	   System.out.println(cmd);
	   String agent = null;

	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(cmd);

	   while (rs1.next()) {
	   	agent = rs1.getString("aadress").trim();

	   }
	   rs1.close();
	   st1.close();
  
	   return agent;

	 }
  

  public String getProdNimi(String id) throws SQLException {
	 
	  String cmd = "SELECT N17_KODAS_PS AS toode,trim(N17_PAV) AS nimetus,N17_KODAS_US as yhik,''  AS tykke,  " +
			  "   n17_kodas_ls_1 AS grupp ,N17_MEN_PAV AS minkogus , " + 
			  "  N17_KODAS_LS_4 AS status  FROM n17_prod a where N17_KODAS_GS = 'MA002.07.01'   " +
			  " and  N17_KODAS_PS = '" + id + "' "; 
	  
	   System.out.println(cmd);
	   String agent = null;

	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(cmd);

	   while (rs1.next()) {
	   	agent = rs1.getString("nimetus").trim();

	   }
	   rs1.close();
	   st1.close();
  
	   return agent;

	 }
  
  
  private void sendKinnitusEmail(String custid, String nimi ,Vector lines ,String agent, String comment,  String aadress ,String agname){
  	try{
  		
  		if(lines.size() > 0){
  			
  			String text = "";
  			for (int i = 0; i < lines.size(); i++) {
  		     	OrderLine line = (OrderLine)lines.elementAt(i);
  		     	text = text + "<br><b><font size=\"5\"> [" + (int)(line.getKogus()) + "] </b></font>  " 
  		     	+ line.getToode() + " " + this.getProdNimi(line.getToode()) + " " + line.getUnit();
  		    }
  			Email mail = new Email(null);
  			    mail.setEncoding("iso-8859-1");
  			   
	  		    mail.setSMTP(parent.getProps().getProperty("smtphost"));
	  	  		mail.setFrom(parent.getProps().getProperty("from"));
	  	  		mail.setType("text/html");
	  	  		mail.setSubjectFilesTo("Gautas uzsakymas  "  + custid + " " +  Converter.cleanLitLatEst(nimi));
	  	  		if(comment == null){
	  	  			comment = "nera";
	  	  		}
	  	  		mail.setBody("<h><br><font size=\"6\">Gautas uzsakymas </font></b>&nbsp;&nbsp<br><br><b><font size=\"5\">" + custid +  " " +  Converter.cleanLitLatEst(nimi) 
	  	  				+ "  " + Converter.cleanLitLatEst(aadress) + " </font></b><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +"<br><hr>" + Converter.cleanLitLatEst(text)
	  	  				+ " <br><br>Komentaras : " + comment + "<br>"  + 
	  	  			      " <br><br>Manager : " + agent  + " " + Converter.cleanLitLatEst(agname) + "<br><br><br><br>"  +"</h1>" );
              
	  	  		mail.setSendNotice(false);
	  	  		System.out.println("Email kuhu saadame teate " + parent.getProps().getProperty("emailto"));
	  	  	    mail.setFilesTo(parent.getProps().getProperty("emailto"));
	  	  	    mail.send();

  		}
	  	
	  			}catch(Exception e){
	  			e.printStackTrace();	
	  			}
	  	
  }  
  
 private void makeOutput(Order order){
 	StringBuffer sb = new StringBuffer();
    
 	if(order.getWarehouse().equals("ND000001")){
 		
 	}
nf4.setMaximumFractionDigits(3);
nf4.setGroupingUsed(false);
 	
 	String ls = System.getProperty("line.separator");
 	try {
 	 
      sb.append("       <I06>" + ls);

      int status =  order.getStatus();
    String type = "55";
   
      if(status == 0){
    	  sb.append("          <I06_OP_TIP>" + type + "</I06_OP_TIP>" + ls); 
      }else{
    	  sb.append("          <I06_OP_TIP>"  + type + "</I06_OP_TIP>" + ls);  
      }
      String cust = order.getCompany();
      cust = cust.replaceAll("11119xx", "Š");
      sb.append("          <I06_DOK_NR>" + escape(order.getDokNr())  + "</I06_DOK_NR>" + ls);
      sb.append("          <I06_KODAS_PO>" + order.getDokNr() + "</I06_KODAS_PO>" + ls);
      sb.append("          <I06_DOK_DATA>" +  getDateString(order.getShipCal()) + "</I06_DOK_DATA>" + ls);
      sb.append("          <I06_OP_DATA>" +  getDateString(Calendar.getInstance()) + "</I06_OP_DATA>" + ls);
      sb.append("          <I06_KODAS_LS_1>" + escape(order.getAgent()) + "</I06_KODAS_LS_1>" + ls);
     // sb.append("          <I06_KODAS_MS>" + escape(order.getAgent()) + "</I06_KODAS_MS>" + ls);
      sb.append("          <I06_KODAS_KS>" + escape(cust.substring(0,cust.lastIndexOf("-"))) + "</I06_KODAS_KS>" + ls);
      sb.append("          <I06_KODAS_AK>" + escape(cust.substring(cust.lastIndexOf("-")+1)) + "</I06_KODAS_AK>" + ls);
      sb.append("          <i06_DOK_REG>" + escape(order.getDokNr()) + "</i06_DOK_REG>" + ls);

      String lisa = null;
      if(order.getInfo() != null ){
  		 lisa = order.getInfo(); 
  	  }
      if(order.getTeostaja() != null){
    	  if(lisa == null){
    		  lisa = "Ref:"+order.getTeostaja();
    	  }else{
    		  lisa = lisa + " Ref:"+order.getTeostaja();
    	  }
      }
	System.out.println("lisa " + lisa);
	  if(lisa != null){
    	
          sb.append("          <I06_PASTABOS>" + escape(lisa) + "</I06_PASTABOS>" + ls);
          
      }
      
 
     
      String addr =  null;
  	try {
  		addr = identifyCustomerADDR(cust);
  	} catch (SQLException e1) {
  		e1.printStackTrace();
  	}
       sb.append("          <I06_ADR2>" + escape(addr) + "</I06_ADR2>" + ls);
           
     
    Vector lines = order.getOrderLines();
      for (int i = 0; i < lines.size(); i++) {
         sb.append("          <I07>" + ls);
          
      	OrderLine line = (OrderLine)lines.elementAt(i);
          String toode = line.getToode();
          
          double ale = line.getDiscount();
          if (ale == -1 ){
             ale = 0 ;
              }
          
          String kog  = escape(nf4.format(line.getKogus()));
          double kogus = line.getKogus();
          boolean inte = true;
          if((int)kogus - kogus != 0){
        	  kog = escape(nf4.format(line.getKogus()));
        	  inte = false;
          }
          
          sb.append("               <I07_KODAS_PO>" + order.getDokNr() + "</I07_KODAS_PO>" + ls); 
          sb.append("               <I07_EIL_NR>" + (i+1) + "</I07_EIL_NR>" + ls);
     //     sb.append("               <I07_KODAS>" +  escape(toode) + "</I07_KODAS>" + ls);
          sb.append("               <DI07_BAR_KODAS>" +  escape(toode) + "</DI07_BAR_KODAS>" + ls);
          if(inte){
        	//  sb.append("               <I07_KIEKIS>" + kog + "</I07_KIEKIS>" + ls);  
          }else{
        	  //sb.append("               <T_KIEKIS>" + kog + "</T_KIEKIS>" + ls);
          }
         
          sb.append("               <KIEKIS>" + kog + "</KIEKIS>" + ls);
        //  sb.append("               <I07_ALT_KIEKIS>" + kog + "</I07_ALT_KIEKIS>" + ls);
          sb.append("               <I07_KODAS_IS>00000017</I07_KODAS_IS>" + ls);    
          sb.append("               <I07_TIPAS>1</I07_TIPAS>" + ls);
       if (ale != 0 ){
          sb.append("               <I07_NUOLAIDA>" + escape(nf.format(ale)) + "</I07_NUOLAIDA>" + ls);
          }     
       
       
         // sb.append("               <I07_MOKESTIS_P>" + prod.getVat() + "</I07_MOKESTIS_P>" + ls);
         // sb.append("               <I07_SUMA>" + escape(getConc().getNF().format((line.getAmount()* price)*(100-ale)/100)
          //		)+ "</I07_SUMA>" + ls);
          
  /*<I07>

  <I07_KAINA_SU>1.0900</I07_KAINA_SU>
  <I07_NUOLAIDA>0.00</I07_NUOLAIDA>
*/
  

  addNumLine();
          sb.append("          </I07>" + ls);
      }

      sb.append("          </I06>" + ls);
 
 	String num =  "KAI" + getNextDoknr();
 	
 	String pdanum = order.getTeostaja();
 	if(pdanum == null || pdanum.length() == 0){
 		pdanum = "";	
 	}
 	
 	 String	sql1 = "   insert into dbo.B00_IMP( B00_KODAS_IM,B00_PAV,B00_MODUL,B00_DATE,B00_OPERACIJA,B00_LOGAS,B00_PERKELTA,B00_ADDUSR,B00_USERIS,B00_R_DATE)" +
 	 		" values( '"+num+"','Importas is Android " + pdanum + " ','RO',current date,'" + sb.toString()+ "'," +
 	 				"'',1,'MASTER','MASTER',DATEFORMAT(getdate(*),'yyyy-mm-dd hh:nn:ss.000'))"; 

System.out.println(sql1);
	  con = DriverManager.getConnection(url + "rivona00_centras");
Statement st1 = con.createStatement();
  st1.executeUpdate(sql1);
  st1.close();

      
   
      
      
      String fileName = escape(parent.getProps().getProperty("conv.dir") + num +"-"+ order.getAgent() + "-" +  order.getDokNr() + "-" + order.getAgent())  + ( Calendar.getInstance().getTimeInMillis()+"").substring(5,13)   +  ".EIP";
      System.out.println(fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(sb.toString().getBytes());
        fos.close();

	} catch (Exception e) {
		e.printStackTrace();
	}
 }
  
 public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
 
  public String getDriverVersion(){
    return "RivonaKedaImport";
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

  
  private String getLineNr2(int i){
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
  private NumberFormat nf4 = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private ImportPanel parent;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
