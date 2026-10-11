/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.ReportLine;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class ScalaTobaCallDBDriver extends AbstractDBDriver implements BulkUpdater,Manager,Promo,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public ScalaTobaCallDBDriver(EksportPanel parent) {
	super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
	// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();

	} catch (Exception e) {
	    parent.showError(e);
	}
    }
    public String getProductSortingData(){
       	return null;
       }
    /**
     *
     */
	
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	} 
    public void connectionUp(){
      connectionUp(this.url);
    }
    public String getSaveDebtInfo() {
       	return null;
       }
       
    public String getAssortmentStr(Agent agent){
    	return null;
    }

            public String getDealerCustomerCSV(String agent){
             return null;
             }
            
            
      	  public static String getYYYYpMMpDDpTimeNiceString(){
  		    StringBuffer sb = new StringBuffer();
		    Calendar cal= Calendar.getInstance();
		    
		    String hour =   cal.get(Calendar.HOUR_OF_DAY) + "";
		    
		    
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}

			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}

			sb.append("" + cal.get(Calendar.YEAR));
			sb.append("." + month  + ".");
			sb.append(day +" ");
		    
		    String min = String.valueOf(cal.get(Calendar.MINUTE));
		    String sec = String.valueOf(cal.get(Calendar.SECOND));
		     if (hour.length() == 1){
		      sb.append("0");
		    }
		     sb.append(hour);
		     sb.append("_");
		    if (min.length() == 1){
		     sb.append("0");
		   }
		     sb.append(min);
		     sb.append("_");
		     if (sec.length() == 1){
		        sb.append("0");
		      }
		    sb.append(sec);
		    return sb.toString();
		  
  		  }
  	  
  	  
  	  


        public String getSaveStockInfo(){
        	  connectionUp(url);//'PR' +
        	  StringBuffer buf = new StringBuffer();
        	  StringBuffer buf2 = new StringBuffer();
        	  StringBuffer buf3 = new StringBuffer();
        	  StringBuffer buf4 = new StringBuffer();
        	  StringBuffer buf5 = new StringBuffer();
        	  StringBuffer buf6 = new StringBuffer();
        	  String aegx = getYYYYpMMpDDpTimeNiceString();
        	  String paevx = aegx.substring(0,10);
        	  String kuux = aegx.substring(0,7);
        	  String tiitel = " Eugesta Eesti AS - Distributsiooni automaataruanne\n\tmoodustatud: " + aegx + "\n\n";
         	 
        	  //teeme plano korrasoleku info
        	 
        	  String plano = "Planogrammi korrasolek kuup�ev:" + paevx+ "\n\n";
        	  String oos  =  "Out of Stock kauplustes kuup�ev:" + paevx+ "\n\n";
        	  String cont =  "Kontrollitud kauplused kuup�ev:" + paevx+ "\n\n";
        	  String planom = "Planogrammi korrasolek jooksev kuu:" + kuux+ "\n\n";
        	  String oosm  =  "Out of Stock kauplustes jooksev kuu:" + kuux+ "\n\n";
        	  String contm =  "Kontrollitud kauplused jooksev kuu:" + kuux+ "\n\n";
         	 
        	  
        	  
        	  String  cmd = "  select convert(date,sentdate) as kuupaev,convert(time,sentdate) as aeg,agentid,agentname, custid,(select SL01002 from SL010100 where SL01001 =custid) as custname,(select  SL01002 from " + 
              " SL010100 where SL01001= (select SL01056 from SL010100 where SL01001 =custid) ) as kett ,substring(prodid,5,len(prodid)) as tkood " +
        	  " ,ltrim(prodname) as toote_nimi,resstr2 as staatus,amoun/100 as kogus  " + 
        	  " , comment from supervisor where convert(date,getdate()-1) < convert (date,sentdate) and  custid != 'fake' and left (prodid,4) = 'plun'";
              

		       if (isDebug()) System.out.println(cmd);
		       StringBuffer products = new StringBuffer();
		       try {

		         Statement st = con.createStatement();
		         ResultSet rs = st.executeQuery(cmd);
		         buf.append("kuup�ev;aeg;agent;agendinimi;saaja;saajanimi;klient;tootekood;tootenimi;staatus;kogus;kommentaar\n");
			        
		         while (rs.next()) {

		           String paev = rs.getString("kuupaev").trim();
		           String aeg = rs.getString("aeg").trim();
		           String agentid = rs.getString("agentid").trim();
		           String agentname = rs.getString("agentname").trim();
		           String custid = rs.getString("custid").trim();    
		           String custname = rs.getString("custname").trim();
		           String kett = rs.getString("kett");
		           if(kett == null){
		        	   kett = "";
		           }else{
		        	   kett = kett.trim();
		           }
		           String tkood = rs.getString("tkood").trim();
		           String tootenimi = rs.getString("toote_nimi").trim();
		           String result = rs.getString("staatus").trim();
		           String kogus = rs.getString("kogus").trim();
		           String komment = rs.getString("comment").trim();
		           buf.append(paev + ";" + aeg + ";" + agentid + ";" + agentname + ";" + custid
		        		   + ";" + custname + ";" + kett + ";" + tkood + ";" + tootenimi + ";" + result + ";" +  kogus + ";" +komment + "\n");
				     
		         }
		           rs.close();

		        	  String  cmd2 = "  select convert(date,sentdate) as kuupaev,convert(time,sentdate) as aeg,agentid,agentname,custid,custname,(select  SL01002 from " +
		             " SL010100 where SL01001= left(custid,8) ) as kett ,substring(prodid,5,len(prodid)) as tkood "+ 
		        	 " ,ltrim(prodname) as toote_nimi, resstr2  as staatus ,amoun/100 as kogus  "+
		        	 " ,  comment from supervisor where  custid != 'fake' and convert(date,getdate()-1) < convert (date,sentdate) and left (prodid,4) = 'prun' ";
		              
			          rs = st.executeQuery(cmd2);
			         buf2.append("kuup�ev;aeg;agent;agendinimi;saaja;saajanimi;klient;tootekood;tootenimi;staatus;kogus;kommentaar\n");
				        
			         while (rs.next()) {

			           String paev = rs.getString("kuupaev").trim();
			           String aeg = rs.getString("aeg").trim();
			           String agentid = rs.getString("agentid").trim();
			           String agentname = rs.getString("agentname").trim();
			           String custid = rs.getString("custid").trim();    
			           String custname = rs.getString("custname").trim();
			           String kett = rs.getString("kett");
			           if(kett == null){
			        	   kett = "";
			           }else{
			        	   kett = kett.trim();
			           }
			           String tkood = rs.getString("tkood").trim();
			           String tootenimi = rs.getString("toote_nimi").trim();
			           String result = rs.getString("staatus").trim();
			           String kogus = rs.getString("kogus").trim();
			           String komment = rs.getString("comment").trim();
			           buf2.append(paev + ";" + aeg + ";" + agentid + ";" + agentname + ";" + custid
			        		   + ";" + custname + ";" + kett + ";" + tkood + ";" + tootenimi + ";" + result + ";" + kogus + ";"  + komment + "\n");
					     
			         }
			           rs.close();
			           
			           
			        	  String  cmd3 = "    select (convert(date,(sentdate))) as kuupaev,agentname,(select SL01002 from SL010100 where SL01001 =custid) " +
					   " as custname,(select  SL01002 from " +
					    "           SL010100 where SL01001= (select SL01056 from SL010100 where SL01001 =custid) ) as kett " +
                         "  from supervisor where convert(date,getdate()-1) < convert (date,sentdate) and left (prodid,4) in " +
						  "  ('prun','plun') and custid != 'fake' group by (convert(date,(sentdate))) " +
                          " ,agentname,custid,custname";
				              
					          rs = st.executeQuery(cmd3);
					         buf3.append("kuup�ev;agendinimi;saajanimi;klient\n");
						        
					         while (rs.next()) {

					           String paev = rs.getString("kuupaev").trim();
					           String agentname = rs.getString("agentname").trim();
					           String custname = rs.getString("custname").trim();
					           String kett = rs.getString("kett");
					           if(kett == null){
					        	   kett = "";
					           }else{
					        	   kett = kett.trim();
					           }
					           buf3.append(paev +  ";" + agentname 
					        		   + ";" + custname + ";" + kett + "\n");
							     
					         }
					           rs.close();

					           
					        	  //jooksva kuu asjad 
					        	   cmd = "  select convert(date,sentdate) as kuupaev,convert(time,sentdate) as aeg,agentid,agentname, custid,(select SL01002 from SL010100 where SL01001 =custid) as custname,(select  SL01002 from " + 
					              " SL010100 where SL01001= (select SL01056 from SL010100 where SL01001 =custid) ) as kett ,substring(prodid,5,len(prodid)) as tkood " +
					        	  " ,ltrim(prodname) as toote_nimi,resstr2 as staatus, amoun/100 as kogus " + 
					        	  " , comment from supervisor where month(sentdate)= month(getdate()) and year(sentdate)= year(getdate()) and left (prodid,4) = 'plun' and custid != 'fake'";
					              

							       if (isDebug()) System.out.println(cmd);
							        products = new StringBuffer();
							      

							         st = con.createStatement();
							         rs = st.executeQuery(cmd);
							         buf4.append("kuup�ev;aeg;agent;agendinimi;saaja;saajanimi;klient;tootekood;tootenimi;staatus;kogus;kommentaar\n");
								        
							         while (rs.next()) {

							           String paev = rs.getString("kuupaev").trim();
							           String aeg = rs.getString("aeg").trim();
							           String agentid = rs.getString("agentid").trim();
							           String agentname = rs.getString("agentname").trim();
							           String custid = rs.getString("custid").trim();    
							           String custname = rs.getString("custname").trim();
							           String kett = rs.getString("kett");
							           if(kett == null){
							        	   kett = "";
							           }else{
							        	   kett = kett.trim();
							           }
							           String tkood = rs.getString("tkood").trim();
							           String tootenimi = rs.getString("toote_nimi").trim();
							           String result = rs.getString("staatus").trim();
							           String kogus = rs.getString("kogus").trim();
							           String komment = rs.getString("comment").trim();
							           buf4.append(paev + ";" + aeg + ";" + agentid + ";" + agentname + ";" + custid
							        		   + ";" + custname + ";" + kett + ";" + tkood + ";" + tootenimi + ";" + result + ";" + kogus + ";"  + komment + "\n");
									     
							         }
				rs.close();
				
				
				   cmd2 = "  select convert(date,sentdate) as kuupaev,convert(time,sentdate) as aeg,agentid,agentname,custid,convert(time,sentdate) as aeg,agentid,agentname, custid,(select SL01002 from SL010100 where SL01001 =custid) as custname,(select  SL01002 from " + 
					              " SL010100 where SL01001= (select SL01056 from SL010100 where SL01001 =custid) ) as kett ,substring(prodid,5,len(prodid)) as tkood "+ 
		        	 " ,ltrim(prodname) as toote_nimi,resstr2  as staatus ,amoun/100 as kogus  "+
		        	 " ,  comment from supervisor where month(sentdate)= month(getdate()) and year(sentdate)= year(getdate()) and left (prodid,4) = 'prun' and custid != 'fake' ";
		              
			          rs = st.executeQuery(cmd2);
			         buf5.append("kuup�ev;aeg;agent;agendinimi;saaja;saajanimi;klient;tootekood;tootenimi;staatus;kogus;kommentaar\n");
				        
			         while (rs.next()) {

			           String paev = rs.getString("kuupaev").trim();
			           String aeg = rs.getString("aeg").trim();
			           String agentid = rs.getString("agentid").trim();
			           String agentname = rs.getString("agentname").trim();
			           String custid = rs.getString("custid").trim();    
			           String custname = rs.getString("custname").trim();
			           String kett = rs.getString("kett");
			           if(kett == null){
			        	   kett = "";
			           }else{
			        	   kett = kett.trim();
			           }
			           String tkood = rs.getString("tkood").trim();
			           String tootenimi = rs.getString("toote_nimi").trim();
			           String result = rs.getString("staatus").trim();
			           String kogus = rs.getString("kogus").trim();
			           String komment = rs.getString("comment").trim();
			           buf5.append(paev + ";" + aeg + ";" + agentid + ";" + agentname + ";" + custid
			        		   + ";" + custname + ";" + kett + ";" + tkood + ";" + tootenimi + ";" + result + ";" + kogus + ";" + komment + "\n");
					     
			             }
			           rs.close();


			        	   cmd3 = "    select (convert(date,(sentdate))) as kuupaev,agentname,(select SL01002 from SL010100 where SL01001 =custid) " +
					   " as custname,(select  SL01002 from " +
					    "           SL010100 where SL01001= (select SL01056 from SL010100 where SL01001 =custid) ) as kett " +
                      "  from supervisor where month(sentdate)= month(getdate()) and year(sentdate)= year(getdate()) and left (prodid,4) in " +
						  "  ('prun','plun') and custid !='fake' group by (convert(date,(sentdate))) " +
                       " ,agentname,custid,custname";
				              
					          rs = st.executeQuery(cmd3);
					         buf6.append("kuup�ev;agendinimi;saajanimi;klient\n");
						        
					         while (rs.next()) {

					           String paev = rs.getString("kuupaev").trim();
					           String agentname = rs.getString("agentname").trim();
					           String custname = rs.getString("custname").trim();
					           String kett = rs.getString("kett");
					           if(kett == null){
					        	   kett = "";
					           }else{
					        	   kett = kett.trim();
					           }
					           buf6.append(paev +  ";" + agentname 
					        		   + ";" + custname + ";" + kett + "\n");
							     
					         }
					           rs.close();

				
				
		       } catch (Exception e) {
		           getParent().showError(e);
		       }
		       

		       File f4 = new File(getDir() + kuux + "planoreport.csv");
               if (f4.exists())f4.delete();
		       if(buf4.length() > 2){
                     try {
               		PrintWriter os = new PrintWriter(new FileWriter(
               				getDir() +  kuux + "planoreport.csv", true));
               		os.write(tiitel + planom + buf4.toString());
               		os.close();
               	  }catch (IOException ex) {
               		ex.printStackTrace();
               	  }
                }
		       
		       
		       
		       File f = new File(getDir() + paevx + "planoreport.csv");
               if (f.exists())f.delete();
		       if(buf.length() > 2){
                     try {
               		PrintWriter os = new PrintWriter(new FileWriter(
               				getDir() +  paevx + "planoreport.csv", true));
               		os.write(tiitel + plano + buf.toString());
               		os.close();
               	  }catch (IOException ex) {
               		ex.printStackTrace();
               	  }
                }
		       

		       File f2 = new File(getDir() + paevx +"oosreport.csv");
		       if (f2.exists())f2.delete();
               
		       if(buf2.length() > 2){
                 
                    try {
               		PrintWriter os = new PrintWriter(new FileWriter(
               				getDir() + paevx + "oosreport.csv", true));
               		os.write(tiitel + oos +buf2.toString());
               		os.close();
               	  }catch (IOException ex) {
               		ex.printStackTrace();
               	  }
                }
		      
		       File f5 = new File(getDir()  + kuux + "oosreport.csv");
		       if (f5.exists())f5.delete();
               
		       if(buf5.length() > 2){
                 
                    try {
               		PrintWriter os = new PrintWriter(new FileWriter(
               				getDir() + kuux + "oosreport.csv", true));
               		os.write(tiitel + oosm +buf5.toString());
               		 os.close();
               	  }catch (IOException ex) {
               		ex.printStackTrace();
               	  }
                }
		      
		       
		       File f3 = new File(getDir() + paevx +  "contactreport.csv");
		       if (f3.exists())f3.delete();
               
		       if(buf3.length() > 2){
                 
                    try {
               		PrintWriter os = new PrintWriter(new FileWriter(
               				getDir() + paevx + "contactreport.csv", true));
               		os.write(tiitel + cont + buf3.toString());
               		os.close();
               	  }catch (IOException ex) {
               		ex.printStackTrace();
               	  }
                }
		       
		       File f6 = new File(getDir() + kuux +  "contactreport.csv");
		       if (f6.exists())f6.delete();
               
		       if(buf6.length() > 2){
                 
                    try {
               		PrintWriter os = new PrintWriter(new FileWriter(
               				getDir() + kuux + "contactreport.csv", true));
               		os.write(tiitel + contm +buf6.toString());
               		os.close();
               	  }catch (IOException ex) {
               		ex.printStackTrace();
               	  }
                }
		      Email email = new Email(parent.getString("datasent"));
		      
	            //  email.setNoticeTo(parent.getProps().getProperty("unilever.noticeemail"));
	              email.setFrom(parent.getProps().getProperty("from"));
	              email.setSMTP(parent.getProps().getProperty("smtphost"));
	              email.setUser(parent.getProps().getProperty("smtpuser"));
	              email.setPass(parent.getProps().getProperty("smtppass"));
	              email.setPort(parent.getProps().getProperty("smtpport"));
	              email.setSubjectFilesTo("Eugesta - Herkuless distributsiooni aruanne (Made by MobileJava Solutions)");
	              try {
	            	  if(f.exists())
					email.addAttach(f.getName());
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	              try {
	            	  if(f2.exists())
					email.addAttach(f2.getName());
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				  try {
	            	  if(f3.exists())
					email.addAttach(f3.getName());
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				  try {
	            	  if(f4.exists())
					email.addAttach(f4.getName());
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				  try {
	            	  if(f5.exists())
					email.addAttach(f5.getName());
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				  try {
	            	  if(f6.exists())
					email.addAttach(f6.getName());
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
	              try {
					email.setBody("Lisatud aruanded t�nane p�ev ja jooksev kuu\n\nPlanogrammi kontroll planoreport.csv" +
							"\nOOS kontroll oosreport.csv\nK�lastuste �levaade contactreport.csv");
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                  try {
			  String aadr = parent.getProps().getProperty("unilever.reportemail");
			  StringTokenizer ste = new StringTokenizer(aadr,";");
			  while(ste.hasMoreTokens()){
			      email.setFilesTo(ste.nextToken());
				  email.send();
			  }
    			} catch (AddressException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (f.exists())f.delete();
				if (f2.exists())f2.delete();
				if (f3.exists())f3.delete();
				if (f4.exists())f4.delete();
				if (f5.exists())f5.delete();
				if (f6.exists())f6.delete();
				
		          System.exit(0);
	              return products.toString();
	              
			   }       
		    
        


                                           public String getCustProdDiscounts(Agent agent){
                                            return null;
                                            }


        public String getDistribution() {
          return null;
        }

        public Vector getFixedAssortmentData() {
          return null;
        }


    private void connectionUp(String url){
      try {
        if (con != null){
          con.close();
        }
           con = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
           getParent().getProps().getProperty("dsn.pass"));
      } catch (Exception e) {
	    getParent().showError(e);
      }

    }

    
    private void connectionUp2(String url){
        try {
          if (con2 != null){
            con2.close();
          }
             con2 = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
             getParent().getProps().getProperty("dsn.pass"));
        } catch (Exception e) {
  	    getParent().showError(e);
        }

      }
    /**
     *
     */
    public void connectionDown(){
        try {
           con.close();
           con = null;
	} catch (Exception e) {
	    getParent().showError(e);
	}
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */

    public TreeMap getProdGroups() throws SQLException {
    	connectionUp(url);
        TreeMap   tm = new TreeMap();
        String    sql = " SELECT SY24002 AS liik ,SY24003 AS nimetus FROM SY240100 WHERE RTRIM(SY24003)<>'' AND SY24001 = 'IB' ORDER BY SY24003";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String kood=  rs.getString("liik").trim();
            tm.put(kood,
                   rs.getString("nimetus").trim() +" " + kood);
        }
        rs.close();
        st.close();
        con.close();
        this.connectionDown();
        return tm;
 }

    public TreeMap getAgents() {
        connectionUp(url);
        String  cmd = "select Code ,Name from PlanEmployee";
	TreeMap agents = new TreeMap();

	try {
            stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();
	

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("Code").trim());
              String nimi = rs.getString("Name").trim();
              agent.setName(nimi);
              agent.setObject("10");
              agent.setIsPilot(true);
              agents.put(nimi, agent);
	    }
	    Agent aaa = new Agent("HOUSE");
	    aaa.setName("HOUSE");
	    aaa.setObject("10");
        aaa.setIsPilot(true);
	    agents.put("HOUSE", aaa);
	
	   // shipmode.txt

	    
	    String sql1 = "SELECT SY24002 AS yksus,SY24003 AS nimi FROM SY240100 WHERE SY24001='BK'";
	    
	    stmt = con.prepareStatement(sql1);
	          rs = stmt.executeQuery();
	          StringBuffer buf = new StringBuffer();

	    while (rs.next()) {
	    	buf.append(rs.getString("yksus") +  ";" + rs.getString("nimi") + System.getProperty("line.separator"));
	    }
	    
	    
	    if(buf.length() > 2){
            File f = new File(getDir() + "shipmode.txt");
            if (f.exists())f.delete();
              try {
        		PrintWriter os = new PrintWriter(new FileWriter(
        				getDir() + "shipmode.txt", true));
        		os.write(buf.toString());
        		os.close();
        	  }catch (IOException ex) {
        		ex.printStackTrace();
        	  }
         }
	    
	    
	    
	} catch (Exception e) {
	    getParent().showError(e);
	}
	
	
	
	
	
        connectionDown();
	return agents;
    }

    private String getDir(){
        String dir = getParent().getProps().getProperty("readin");
        if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
        return dir;
      }

    
    public Vector getPriceMatrixData()  {
        
         return prlines;
       }

    
    private String getTimeStringShort(long mils){
        
        int ee = 0;
        	Date dd = new Date(mils);
        	if(dd.getTimezoneOffset() != 0){
            	ee = dd.getHours() + dd.getTimezoneOffset()/60;
        	}
            String hour = ee + "";
            String minute = dd.getMinutes() + "";

        if(hour.length()==1){
        	hour= "0" + hour;
        }
        
        if(minute.length()==1){
        	minute= "0" + minute;
        }          
            
        	return hour + "" +minute ;
        }
        
    
	public void setAlternativeReport(ReportLine rr) throws SQLException {
	
		//M��rame reportline kuup�eva 
		Calendar cal = Calendar.getInstance();
	      cal.setTime(rr.getEndTime());
		  String kuupaev = getDateAAAADDMMStr(cal, 0);
		
		//Esitekas m��rame kindlaks kas sihuke visit on olemas
		
		String sql = "select VISI_ID   from PDA_VISIT vis INNER JOIN PDA_CLIENT cl on cl.CLIE_ID = vis.CLIE_ID " +
				"where PLANNED_TIME='" + kuupaev + "' and cl.CODE='" + rr.getClientCode() + "'";
			
		String visi_id = null; 
		Statement st = con.createStatement();
        ResultSet rs8 = st.executeQuery(sql);
        if (rs8.next()){
        	visi_id = rs8.getString("visi_id");
        }
        rs8.close();
         st.close();
        System.out.println("Aruanderida ALTERNATIVE " + visi_id );
      
        
        
        //nyyd lisame vajalikud vidinad olemasolevale reale 
      
        if(visi_id != null){
        	 String   sql5 = " UPDATE PDA_VISIT set START_TIME ='"  + getTimeStringShort(rr.getStartTime().getTime())+ "' , END_TIME ='"
        	    + getTimeStringShort(rr.getEndTime().getTime()) + "' , VISIT_DATE ='" + kuupaev+ "' where visi_id =" + visi_id + "";
        	 
   		              PreparedStatement st2 = con.prepareStatement(sql5);
   		              st2.executeUpdate();	              
   		              st2.close();
    			  
        }
    
	}
    
    
	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr ,Date dat){}



    public static String getDateAAAADDMMStr(Calendar cal,int add){
		StringBuffer sb = new StringBuffer();
		cal.add(Calendar.DAY_OF_YEAR,add);
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}

		String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		if (month.length() == 1){month = ("0") + month;}

		sb.append("" + cal.get(Calendar.YEAR));
		sb.append("" + month);
		sb.append(day +"");
		return sb.toString();
	}



    public TreeMap getCustomers(Agent agent){
        
    	//k�lastusp�eva leidmine 
    	  Vector thisweek = new Vector ();
    	  TreeMap custemps = new TreeMap();

    	  TreeMap custsort = new TreeMap();
    	  String weekno = "";
    	  connectionUp(url);
    	 String weeksql =  "select WeekNo from PlanDay where ContactDate='" + getDateAAAADDMMStr(Calendar.getInstance(),0) + "' group by WeekNo " ;
  		  
  			 Statement st = null;
  		   ResultSet	      rs = null;
  			try {   
  				st = con.createStatement();
  			      rs = st.executeQuery(weeksql);
  			    if(rs.next()){
  			    	weekno = rs.getString("weekno").trim();
  			    }
  			    rs.close();
  			    st.close();
  			    
  			  String custsql = "select ScalaCode as klient,  substring(ContactPlan," + weekno + ",1) as ag from PlanClient " +
  			  		"inner join  SL010100 on SL01001 = ScalaCode where   substring(ContactPlan," + weekno + ",1) != '' " ; 
  			  System.out.println(custsql);
  			  
    			try {   
    				st = con.createStatement();
    			      rs = st.executeQuery(custsql);
    			    while(rs.next()){
    			    	String ccc = rs.getString("klient").trim();
    			    	thisweek.addElement(ccc);
    			    
    			    Agent aa = (Agent)getParent().getAgentsHash().get(rs.getString("ag").trim());
    			    if(aa != null){
    			    	aa.addTodayCusts(ccc);
    			    }
    			    }
    			    
    			    rs.close();
    			    st.close();  
    			}catch(Exception e){
    		  		e.printStackTrace();
    	  		}
 	  			
    			
    			
    			  String cuemsql = " select ScalaCode as klient,Caller as agent from PlanClient where len(Caller) >0 order by  ScalaCode,Caller "; 
    			 // where contactplan != '' 
      			try {   
      				st = con.createStatement();
      			      rs = st.executeQuery(cuemsql);
      			    while(rs.next()){
      			    	String cc = rs.getString("klient").trim();
      			    	String em = rs.getString("agent").trim();
      			    	Vector vv = null;
      			    	if(!custemps.containsKey(cc)){
      			    		vv = new Vector();
      			    		vv.addElement(em);
      			    		custemps.put(cc, vv);
      			    	}else{
      			    		Vector va = (Vector)custemps.get(cc);
      			    		va.addElement(em);
      			    		custemps.put(cc, va);
      			    	}
      			    }
      			    rs.close();
      			    st.close();  
      			}catch(Exception e){
      		  		e.printStackTrace();
      	  		}
      			
      			/*
      			 * 
      			 * PAnin selle kinni et eraldi teha helistajatele driver
      		  String cucasql = " select Scalacode as klient,caller as agent from planclient  order by  Scalacode,caller "; 
	  			
    			try {   
    				st = con.createStatement();
    			      rs = st.executeQuery(cucasql);
    			    while(rs.next()){
    			    	String cc = rs.getString("klient").trim();
    			    	String em = rs.getString("agent").trim();
    			    	Vector vv = null;
    			    	if(!custcalls.containsKey(cc)){
    			    		vv = new Vector();
    			    		vv.addElement(em);
    			    		custcalls.put(cc, vv);
    			    	}else{
    			    		Vector va = (Vector)custcalls.get(cc);
    			    		va.addElement(em);
    			    		custcalls.put(cc, va);
    			    	}
    			    }
    			    rs.close();
    			    st.close();  
    			}catch(Exception e){
    		  		e.printStackTrace();
    	  		}
      			*/
      			
      			
      			
      		  String custtm = "select Client,Stop from PlanContact where WeekNo = '" + weekno + "' and ContactType!='C' group by Client,Stop  "; 
	  			
    			try {   
    				st = con.createStatement();
    			      rs = st.executeQuery(custtm);
    			    while(rs.next()){
    			    	String cc = rs.getString("Client").trim();
    			    	String em = rs.getString("Stop").trim();
    			         if(em.length()==1){
    			        	 em = "0" + em;
    			         }
    			    
    			    		custsort.put(cc, em);
    			    	
    			    }
    			    rs.close();
    			    st.close();  
    			}catch(Exception e){
    		  		e.printStackTrace();
    	  		}
      			
    			

    	  		System.out.println("Custsort size " + custsort.size());
  		}catch(Exception e){
  		e.printStackTrace();
  		}
  			
  			
  		System.out.println("Thisweek size " + thisweek.size());
  		
  		//jooksev n�dal tuleb kus tuleb k�ija see nadal
  	//	select client,w9 from plancontactexcel where w9 != ''
  		
  	//	ja vaata ka kas n�dala p�ev on olemas kui jah siis pane 1 
    	
  		
    	
    	
  		
  		
  		
  		
        connectionUp2(url);
        String cmd = "SELECT SL01002 AS nimi,SL01001 AS kood, SL01035 as ring ,(SL01003 + ' (' + SL01004 + ')') AS aadress, SC45004 AS ale,SL01036 AS ale2 , " +
        "   ' ' AS visit, SL22005 as paym , SL01029 as klgrupp, SL01075 as block ,SL01056 as payer, " +
        "  WeekDay AS millal  ,SL01006 + ' ' + SL01011 as info FROM SL010100 left JOIN SC450100 " + 
		"   ON SL01036=SC45003    LEFT OUTER JOIN SL140100 ON SL01001=SL14001 AND SL14002='00'" +
        "  LEFT OUTER JOIN SY240100 ON SL01035=SY24002 AND SY24001='BK'" +
        "  LEFT OUTER JOIN SL220100 ON SL01024=SL22002 AND SL22001='0' " +
		"   inner join PlanClient pl ON pl.ScalaCode=SL01001 " +
        "  WHERE len(SL01001)>3  and len(SL01002) >0 and (rtrim(SL01071) not like'%Z' and  rtrim(SL01071) not like'%X') ORDER BY SL01002 ";
        

        connectionUp(url);
        String cmd3 = "SELECT SL01056 as pea   FROM SL010100 left JOIN SC450100   ON SL01036=SC45003 " +
                  "  WHERE SL01002 != '' AND SL01035 != ''  and SL01056 != '' ORDER BY SL01002";
	
      log(cmd);

	Vector vec = new Vector();

	try {   
		st = con.createStatement();
	      rs = st.executeQuery(cmd3);
	    while(rs.next()){
	    	vec.addElement(rs.getString("pea").trim());
	    }
	    rs.close();
	    st.close();
	}catch (Exception e){
		e.printStackTrace();
	}
	    System.out.println("vecsize " + vec.size());
	// vektoris on klindid kellel on peaklient m�rgitud ja neile ei pane kaasa allahindlust
	
        String    sql = "SELECT SL01008 AS custid,'' AS clgroup,SUBSTRING(SC18002,11,35) AS prodid,'' AS prodgroup,SC18009, " +
        "	(case when SY24003=null then '0' else SY24003 end ) AS discount,SC18008 AS price  " +
        "   FROM SC180100 INNER JOIN SL010100 ON  left(SC18002,4)= SL01001 LEFT JOIN SY240100 on SC18009=SY24002 and SY24001='BG' WHERE " +
         "  SC18001='C' AND CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN  SC18010 AND SC18011   and len(SL01008) >0  " +
         " order by SL01008,SUBSTRING(SC18002,11,35)";

        
          try {
           
            st = con.createStatement();
             rs = st.executeQuery(sql);

            while (rs.next()) {

              String custID = rs.getString("custid").trim();
              String clGroup = rs.getString("clgroup");
              String prodID = rs.getString("prodid").trim();
              String prodGroup = rs.getString("prodgroup");
              String sss = rs.getString("SC18009");
              String discount = rs.getString("discount");
              if(discount == null){
              	discount = sss.trim();
              }else{
              	discount = discount.trim().replaceAll("%"," ").trim();
              }
			   
              double price = rs.getDouble("price");
              MatrixLine mline = new MatrixLine();

              if (custID != null && custID.trim().length() > 0) {
                mline.setCustID(custID.trim());
              }
              if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
                mline.setCliGroup(clGroup.trim());
              }

              if (prodID != null && prodID.trim().length() > 0) {
                mline.setProdID(prodID.trim());
              }
              if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
                mline.setProdGroup(prodGroup.trim());
              }
              if(discount.trim().length()==0){
              	discount = "0";
              }
                mline.setDiscount(Double.parseDouble(discount));

              if (Double.parseDouble(discount) == 0 && price != 0) {
                mline.setPrice(price);

              }
              if(!vec.contains(custID.trim())){
                  prlines.add(mline);   
              }
            }

            // siisn l�hevad ainult need kilendid kellel pole peaklienti
            
            rs.close();
            
            
            String sqq = "       SELECT  LEFT(SC18002,10) AS clgroup,SUBSTRING(SC18002,11,35)  AS prodid, SC18008 AS price  " +
     "   FROM SC180100 WHERE  SC18001='C' AND " +
     "   CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN  SC18010 AND SC18011 " +
     "   and LEFT(SC18002,10) like '%P%'";
            /*      SELECT  LEFT(SC18002,10) AS clgroup,SUBSTRING(SC18002,11,35) + '03' AS prodid, SC18008 AS price 
            FROM SC180100 WHERE  SC18001='C' AND
            CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN  SC18010 AND SC18011
            and LEFT(SC18002,10) like '%P%'*/
            
            rs = st.executeQuery(sqq);

            while (rs.next()) {

                String clGroup = rs.getString("clgroup").trim();
                String prodID = rs.getString("prodid").trim();
  	   
                double price = rs.getDouble("price");
                MatrixLine mline = new MatrixLine();
      
                if (clGroup != null && clGroup.trim().length() > 0) {
                    mline.setDiscGroup(clGroup.trim());
                  }
                
                if (prodID != null && prodID.trim().length() > 0) {
                  mline.setProdID(prodID.trim());
                }

                if (price != 0) {
                    mline.setPrice(price);
                }
               
                
                    prlines.add(mline);   
                
            }
            
            //Siin panime �ra PEAKLIENDI SOODUKAD ja kasutame seal pricergroupi
            
            
            
            String sql2 = "select '' AS custid ,SC39002 AS clgroup , rtrim(ltrim(SC39001))  AS prodid,' ' AS prodgroup, " +
            " case when SC39006 <= getdate() AND (SC39007 >= getdate() OR SC39007 = null) then " +
            " SC39005 else SC39004 end AS price  " +
            " FROM SC390100 WHERE SC39002 != '00' order by SC39002,SC39001 ";
            rs = st.executeQuery(sql2);
/*
select '' AS custid ,SC39002 AS clgroup , rtrim(ltrim(SC39001)) + '03'  AS prodid,' ' AS prodgroup, 
             

case when CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN SC39006 and SC39007 then 
             SC39005 else SC39004 end AS price  
             FROM SC390100 WHERE SC39002 != '00' order by SC39002,SC39001 */
            while (rs.next()) {

              String clGroup = rs.getString("clgroup");
              String prodID = rs.getString("prodid").trim();
              String discount = "0";       
			   
              double price = rs.getDouble("price");
              MatrixLine mline = new MatrixLine();
            
              if (clGroup !=null && clGroup.trim().length() > 0) {
                mline.setCliGroup(clGroup.trim());
              }

              if (prodID != null && prodID.trim().length() > 0) {
                mline.setProdID(prodID.trim());
              }
  
              if (Double.parseDouble(discount) == 0 && price != 0) {
                mline.setPrice(price);

              }
             prlines.add(mline);  // Kommentisin v�lja kuniks uus hinna skeem hakkab toimima
            }

            rs.close();
            st.close();
            
            
            
            
            
            
            
            
            
            
            
            connectionDown();
          }
          catch (SQLException ex) {
            ex.printStackTrace();
          }
   
        
        
        
        
        
	log(cmd);

	TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        connectionUp();
	try {
           PreparedStatement stmt = con.prepareStatement(cmd);
            if (agent != null){
//              stmt.setString(1, agent.getID());
            }
	   	      rs = stmt.executeQuery();

	    while (rs.next()) {
		String nimi = rs.getString("nimi").trim();
		nimi = nimi.substring(0,1) + nimi.substring(1).toLowerCase();
                Customer cust = new Customer(rs.getString("kood").trim());
                
                String ring = rs.getString("ring");
                if(ring != null && ring.trim().length()>0){
                	ring = "r" +ring.trim() + " ";
                }else{
                	ring = "";
                }
                cust.setAddress(rs.getString("aadress").trim());
                  nimi = ring + nimi + " " + cust.getAddress();
                
                cust.setName(nimi);
                
                
                String ale = rs.getString("ale");
                if (ale != null && ale.length() > 0){
                 // cust.setDiscount(Double.parseDouble(ale.trim()));
                }
                String ale2 = rs.getString("ale2");
                if (ale2 != null && ale2.trim().equals("04")){
                	//cust.setDiscount(17);
                	
                }
             //   cust.setVisit(rs.getString("visit").trim());
              //  String agen = rs.getString("agent");
                //if(agen != null){
                    // cust.addAgent(new Agent(agen.trim()));
                  // }
                if(custemps.containsKey(cust.getID())){
                	Vector vvv = (Vector)custemps.get(cust.getID());
                       Enumeration en = vvv.elements();
                       while (en.hasMoreElements()){
                    	   String aa = (String)en.nextElement();
                    	   Agent as = (Agent)parent.getAgentsHash().get(aa);
                    	   if(as != null){
                        	   cust.addAgent(as);   
                    	   }
                        }
                     }
             
                
                String paym = rs.getString("paym");
                if(paym != null && paym.trim().length()>0){
                	cust.setPaymTerm(paym.trim());
                }
              
                
                if (cust.getID().equals("0100000")) {
                    Enumeration enums = getParent().getAgentsHash().elements();
                      while (enums.hasMoreElements()) {
                        Agent a = (Agent) enums.nextElement();
                        cust.addAgent(a);
                        }
                      }
             
                cust.setGroup(rs.getString("klgrupp"));
                
                String block  =  rs.getString("block");
                if(block != null && block.trim().length()>0){
                	if(block.trim().equals("1")){
                		cust.setSalesBlockT(true);
                	}
                	
                }
                String payer = rs.getString("payer");
                if(payer != null && payer.trim().length()>0){
                	cust.setPayer(payer);              	
                }
                String millal = rs.getString("millal");
                if(millal != null && millal.trim().length()>0){
             
                	if(isToday(millal) && thisweek.contains(cust.getID())){
                		cust.setVisitToday(true);
                		//System.out.println("T�na" + cust.getName());
                		 if(custsort.get(cust.getID()) != null){
                         	String sort = (String)custsort.get(cust.getID());
                         	//cust.setSortCode(sort);
                         	cust.setName(sort + " " + "" + cust.getName());
                         }		
                		
                	} else  if (thisweek.contains(cust.getID()) && !isToday(millal)){
                		cust.setVisit(millal);
                	}
                 
                }
                
                if(custsort.get(cust.getID()) != null){
                 	String sort = (String)custsort.get(cust.getID());
                 	cust.setSortCode(sort);
                 //	cust.setName(sort + " " + cust.getName());
                 }
                
                String inff = rs.getString("info");
                if(inff != null && inff.trim().length() >0 ){
                	cust.setPersonalData(inff.trim());
                }
                
                
                //otsime v�lasumma igale kauplusele 
                
                String sql6 = "SELECT SL01001 AS id,SUM(SL03013-SL03053) AS debt FROM SL030100 INNER JOIN SL010100 ON SL03001 = SL01001  WHERE  " +
                " NOT(SL03013=SL03053) AND SL03006<=getdate() and SL01001='" + cust.getPayer()+ "'  GROUP BY SL01001 ";

   Statement stm = con2.createStatement();

   ResultSet rs3 = stm.executeQuery(sql6);
   while(rs3.next()){    
     double debt = Double.parseDouble(rs3.getString("debt").trim());
     
     
       cust.setDebt(cust.getDebt() + debt);
       
       if(cust.getDebt() > -1 && cust.getDebt() < -4){
    	   cust.setDebt(0);
       }
   }
   
   rs3.close();
   stm.close();

                
   if(cust.getDebt()<0.02 && cust.getDebt() > 0 ){
   	   cust.setDebt(0);
     }           
                customers.put(cust.getName(), cust);
                custMap.put(cust.getID(), cust);
	    }
            rs.close();
            stmt.close();



        } catch (Exception e) {
            getParent().showError(e);
        }

        
	return customers;
    }

    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = "SELECT SL01001 AS klient, SL03002 AS number,SL03006 AS kuupaev,SL03013-SL03053 AS summa ,-datediff(day ,getdate(),SL03006) as yle" +
      		" FROM SL030100 INNER JOIN SL010100 ON SL03001 = SL01001  WHERE NOT(SL03013=SL03053) AND " +
                   "LEN(LTRIM(SL03013))>0 AND SL03006<GETDATE()+5 ORDER BY SL01001";

      connectionUp(url);
      stmt = con.prepareStatement(sql);


      ResultSet rs = stmt.executeQuery();

      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klient").trim());
        debt.setDoknr(rs.getString("number").trim());

        debt.setDuedate(rs.getDate("kuupaev"));

        debt.setSum(Double.parseDouble(rs.getString("summa").trim()));
        debt.setDue(rs.getString("yle"));
        if(debt.getSum()>0.04 || debt.getSum()<-0.04){
        	list.addElement(debt);	
        }
        }
      rs.close();
      stmt.close();
      return list;
    }

    public TreeMap getProducts(Agent agent){
        connectionUp(url);

//'PR' +
        String cmd = " SELECT  RTRIM(SC01001) AS toode,SC01015 as sort, SC01037 + ' ' +RTRIM(SC01002) AS nimi,SC39004 AS myygihind ,SC01013 " +
        	  "  AS tykke,SC03003-SC03004 AS stock, '' AS maxale,' ' AS pandikas , " +  
              "  case when  SC01031 = 1 then 1 else 0 end AS out ,SC01037 AS grupp,SC01060 AS ean  ,SC01144 as vat , SC01012 as opt " +
              "  FROM SC010100 left outer JOIN SC030100 on SC03001=SC01001 and SC03002='10' AND SC03003!=0 left outer JOIN SC390100 ON SC01001 = SC39001 and SC39002='00'  " +
              "  WHERE   LEN(LTRIM(SC01037))>0 and rtrim(ltrim(SC01016)) != 'S' and SC01015 != '' ORDER BY RTRIM(SC01037),SC01002";
        //V�tsin maha and SC39004 > 0  kuna yks hinnairir niikui nii
        
	if (isDebug()) {
	    System.out.println(cmd);
	}

	TreeMap products = new TreeMap();

	try {
          Statement st = con.createStatement();
	    ResultSet	      rs = st.executeQuery(cmd);

	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
		String sort = rs.getString("sort");
		String nimetus = rs.getString("nimi").trim();

                Product prod = new Product(kood);
                prod.setName(sort +"      " + nimetus );
                prod.setPrice(parseDouble(rs.getString("myygihind").trim()));

                prod.setInCases(parseDouble(rs.getString("tykke").trim()));
                prod.setStock(rs.getDouble("stock"));
                

                String pant = rs.getString("pandikas");
                if(pant != null && pant.trim().length()>0){
                  prod.setPandPakendiKood(pant.trim());
                }
                String out = rs.getString("out");
                if(out != null && out.trim().equals("1")){
                //  prod.setStatusSellout("1");
                }
                String gr = rs.getString("grupp");
                if(gr != null && gr.trim().length()>0){
                  prod.setGroup(gr.trim());
                }
                 gr = rs.getString("ean");
                if(gr != null && gr.trim().length()>0){
                	if(gr.length()>13){
                		gr = gr.substring(0,13);
                	}
                  prod.setEAN(gr.trim());
                  prod.setName(prod.getName() + "   ." + prod.getEAN() + ".   ");
                }
                String vat = rs.getString("vat");
                if(vat != null && vat.trim().length()>0){
                	if(vat.trim().equals("1")){
                		prod.setVat("20");	
                	}else if(vat.trim().equals("2")) {
                		prod.setVat("9");
                	}             
                  }              
                
                double opt = rs.getDouble("opt");
                if(opt > 1){
                	prod.setOptAmountInUnits(opt);            
                  }   
                
                
                
		products.put(prod.getName(), prod);

	    }
            rs.close();
            st.close();
	} catch (Exception e) {
	    getParent().showError(e);
	}

	return products;
    }

    
    
    private boolean isToday(String aa){
    	String day =getDayFromDate(new Date());
    	if(aa.indexOf(day) != -1){
    		return true;
    	}else {
    		return false;
    	}
    }
    
    
	public static String getDayFromDate(Date dat){
		String day = " ";
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dat);
		 if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
		      day = "E";
		    }
		 if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
		      day = "T";
		    }
		 if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
		      day = "K";
		    }
		 if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
		      day = "N";
		    }
		 if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
		      day = "R";
		    }
		 if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
		      day = "L";
		    }
		 if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
		      day = "P";
		    }
		
		  	return day;
	}
    
    
    /**
     * Method declaration
     *
     *
     * @param agentCode
     *
     * @return
     *
     * @see
     */
    public Vector getCustomerProductsList(String agentCode) {
        connectionUp(url);
	/*
   select "Customer Code" AS klient ,"Stock Code" AS toode,Qty AS kogus from st02 where"Salesman"='45'
AND "Invoice order date">'20021101' AND "Actual Del  Date">'00000000'
 group by "Stock Code","Customer Code",qty  order by "Customer Code","Stock Code","Invoice order date" */

     /*   SELECT  ST02001 AS klient ,ST02017 AS toode ,ST02020 AS Kogus,ST02015
         FROM ST020100 WHERE ST02015>? AND ST02016>0 AND ST02020>0 GROUP BY ST02017,ST02001, ST02020,ST02015 ORDER BY ST02001,ST02017 ,ST02015 asc
*/
//Seelega tuleb teha h�kk

        String sql2 = "SELECT  SL01001 AS klient ,ST03017 AS toode ,ST03020 AS Kogus,ST03015  as lastdate " +
                      "  FROM ST030100 INNER JOIN SL010100 ON ST03001 = SL01001 WHERE ST03015> ? AND ST03016>0 AND ST03020>0 " +
                      " GROUP BY ST03017,SL01001, ST03020,ST03015 ORDER BY SL01001,ST03017 ,ST03015 ASC";

        String sql3 = "select custid,prodid,sentdate from supervisor where comment like '%OUT%' and sentdate >getdate()-180";
     
        
	if (isDebug()) {
	    System.out.println(sql2);
	}

	Vector custProds = new Vector();
      
          try {
        	  

              Connection con2 = getNewConnection2();
              PreparedStatement   stmt2 = con2.prepareStatement(sql3);
        	  
        	  
        	  
            stmt = con.prepareStatement(sql2);
            stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
            
            
            HashMap aeg = new HashMap();
            ResultSet rs2 = stmt2.executeQuery();
          while (rs2.next()) {
            String kliendikood = rs2.getString("custid").trim();
            String tootekood = rs2.getString("prodid").trim();
            long dd = rs2.getDate("sentdate").getTime();
            
            
            
            if(aeg.get(kliendikood+tootekood) == null){
            	aeg.put(kliendikood+tootekood, dd+"");
            }else{          	
            	aeg.put(kliendikood+tootekood, dd+"");
            }
            
  
          }
            
            
            
            
            
            
            ResultSet	      rs = stmt.executeQuery();

            while (rs.next()) {
              String kliendikood = rs.getString("klient").trim();
              String tootekood = rs.getString("toode").trim();
              double kogus = Double.parseDouble(rs.getString("kogus").trim());
              Date date = rs.getDate("lastdate");
              
              boolean ok = true;
              if(aeg.get(kliendikood+tootekood) != null ){
              	
              	String vv = (String)aeg.get(kliendikood+tootekood);
              	if(Long.parseLong(vv) > date.getTime()){
              		ok = false;
              		System.out.println("siin on sortimendi erand " + kliendikood + "  " + tootekood);
              	}
              
              }
              if(ok){

                  CustProd cp = new CustProd(kliendikood, tootekood, kogus,0 ,0,
                  		date.toString().substring(5,10),0,0,0," ");
                    custProds.add(cp);  
              }
            }
            rs.close();
            stmt.close();
          } catch (Exception e) {
            getParent().showError(e);
          }
        
        connectionDown();
        System.out.println("CustProd l�peb");
	return custProds;
    }

    private Connection getNewConnection2() throws SQLException{
        return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsn.user"),
                getParent().getProps().getProperty("dsn.pass"));
      }
    
    
    public String getPilotDayData(Agent agent){
        String sql = " SELECT  pl.Employee AS agent ,  " +
     " Sum(case when SC01037='08' and (SC01002 like '%Leek%' or SC01002 like '%Vermont%' or SC01002 like '%Saint%')   then  ST03020 else 0 end)  AS kohalik ,  " +
 " Sum(case when SC01037='08' and (SC01002  not like '%Leek%' and SC01002  not like '%Vermont%' and  SC01002 not like '%Saint%')   then  ST03020 else 0 end)  AS global  " +
 "  ,sum(ST03021) AS summa   FROM ST030100 INNER JOIN  SC010100 pr on ST03017=SC01001  inner join PlanRegion pl ON pl.code=ST03007  " + 
				  "   WHERE  MONTH(ST03015)=  MONTH(GETDATE())-1 AND YEAR(ST03015)=YEAR(GETDATE()) AND ST03016>0 AND ST03020>0  " +
                       "   GROUP BY pl.Employee ORDER BY pl.Employee ASC  ";

StringBuffer sb = new StringBuffer();
  this.connectionUp();
try {
 stmt = con.prepareStatement(sql);
 //stmt.setString(1, agent.getID());
 String sep= System.getProperty("line.separator");
 ResultSet	      rs = stmt.executeQuery();
 String header =  "Agent  Kohal  Glob  Summa" + sep + "-------------------------------------------" + sep;
 boolean heade = true;
 while(rs.next()){
 	if (heade){
 		sb.append(header);
 		heade = false;
 	}
 	
     String nimi = rs.getString("agent");
     if (nimi == null){
     nimi = " ";
     } else {nimi = nimi.trim();}

 	sb.append(nimi + " ");
    sb.append(getNF().format(Double.parseDouble(rs.getString("kohalik").trim())/5000) + "\t");
    sb.append(getNF().format(Double.parseDouble(rs.getString("global").trim())/5000) + "\t");
   String sum = getNF().format(Double.parseDouble(rs.getString("summa").trim())) ;
   sb.append(sum + "\n" + sep);
 }
} catch (Exception e) {
 getParent().showError(e);
}

connectionDown();

System.out.println(sb.toString());
return sb.toString();

    }


    public String getNonMobileOrders(Agent agent){
    /*  connectionUp(url);

      /*String sql = "SELECT \"Order value\" AS summa,(RTRIM(\"sl01002\")+' '+(\"Customer name\")) AS Nimi," +
                  " i.\"order Number\" AS number FROM st02 i INNER JOIN or20 o ON " +
                   " o.\"Order Number\"=i.\"Order Number\" INNER JOIN sl14 k ON " +
                   " i.\"Customer Code\"=k.\"Customer Code\" INNER JOIN sl01 l ON \"sl01001\"= " +
                   " k.\"Customer Code\" WHERE \"actual del  date\"=? AND  length(\"Shipment Mark\")=0 ";
      
      String sql = "SELECT RTRIM(SL01002) AS nimi, ST02009 AS number, sum(ST02040) AS summa FROM ST020100  " +
        " LEFT JOIN OR200100 ON  OR20001=ST02009 LEFT JOIN SL140100 k ON ST02001=SL14001  " +
        " LEFT JOIN SL010100 l ON SL01001=SL14001 WHERE ST02016>GETDATE()-1 AND ST02007=? " +
        " GROUP BY ST02009,SL01002";

      StringBuffer sb = new StringBuffer();

        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, agent.getID());
            String sep= System.getProperty("line.separator");
            ResultSet	      rs = stmt.executeQuery();
            String header = getParent().getString("name") + "  \t\t" + getParent().getString("number") +
            "\t" + getParent().getString("sum") + sep + "-------------------------------------------" + sep;
            boolean heade = true;
            while(rs.next()){
            	if (heade){
            		sb.append(header);
            		heade = false;
            	}
                String nimi = rs.getString("nimi");
                if (nimi == null){
                nimi = " ";
                } else {nimi = nimi.trim();}

            	sb.append(nimi + "\n");
              sb.append(rs.getString("number").trim() + "\t");
              String sum = getNF().format(Double.parseDouble(rs.getString("summa").trim())) ;
              sb.append(sum + sep +  sep);
            }
        } catch (Exception e) {
            getParent().showError(e);
        }

        connectionDown();*/
        return null;
    }

    /**
     * Method declaration
     *
     *
     * @param agentCode
     *
     * @return
     *
     * @see
     */
    public double getSalary(String agentCode) {
      if (agentCode == null) return 0;
      connectionUp(url);
        String cmd = "SELECT SUM(OR20024) AS sales FROM OR200400 WHERE MONTH(OR20015)=" +
                     "MONTH(GETDATE()) AND YEAR(OR20015)=YEAR(GETDATE()) AND OR20019=?";
        log(cmd);

        try {
            stmt = con.prepareStatement(cmd);
            stmt.setString(1, agentCode);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            ResultSet	      rs = stmt.executeQuery();

            while(rs.next()) {
              setSales(rs.getDouble("sales"));
            }
            rs.close();
            stmt.close();

        } catch (Exception e) {
            getParent().showError(e);
        }

	return 0;
    }


    public void setBulkSales(Map agents) {
       //if (agentCode == null) return 0;
       connectionUp(url);
         String cmd = "SELECT Employee AS agent,SUM(OR20024) AS sales FROM OR200100 inner join PlanRegion pl " +
         		"ON pl.code=OR20019 WHERE MONTH(OR20015)= " +
                   "   MONTH(GETDATE()) AND YEAR(OR20015)=YEAR(GETDATE()) GROUP BY Employee ";
         log(cmd);

         try {
             stmt = con.prepareStatement(cmd);
             //stmt.setString(1, agentCode);

             Calendar cal = Calendar.getInstance();
             cal.set(Calendar.DAY_OF_MONTH, 1);

             ResultSet	      rs = stmt.executeQuery();

             while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                 Agent agent = (Agent)agents.get(agentID);
                 if (agent != null){
                   agent.setMonthSales((int)rs.getDouble("sales"));
                       }
                    }

             rs.close();
             stmt.close();

         } catch (Exception e) {
             getParent().showError(e);
         }

     }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      String sql = "SELECT ST02020 AS  kogus, SC01071 AS sisu FROM ST020400 INNER JOIN " +
                   "SC010100 ON ST02017=SC01001 WHERE ST02007=?" +
                   " AND YEAR(ST02015)=YEAR(GETDATE()) AND MONTH(ST02015)=MONTH(GETDATE()) AND SC01012=?";
      PreparedStatement pst = con.prepareStatement(sql);
      pst.setString(1, agentCode);
      //Calendar cal = Calendar.getInstance();
      //cal.set(Calendar.DAY_OF_MONTH, 1);
      //pst.setTimestamp(2, new Timestamp(cal.getTime().getTime()));
      pst.setString(2, String.valueOf(nr));
      ResultSet rs = pst.executeQuery();
      double total = 0;
      while(rs.next()){
        double kogus = rs.getDouble("kogus");
        String sisu = rs.getString("sisu");
        if (!sisu.equals("")){
          total += kogus*Double.parseDouble(sisu);
        }
      }
      rs.close();
      pst.close();
      //System.out.println("nr:" + nr + ", total:" + total);
      return total;
    }


    public ManagerData getManagerData() throws SQLException{
      String sql = "SELECT clid AS klient,docsum AS summa,linecount AS ridu,text AS tekst," +
                   "sentlong AS kuupaev FROM dayreport WHERE sentdate>(DATE()-30) ORDER BY clid";
      Connection c = DriverManager.getConnection(url2);
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery(sql);


      String sql2 = "SELECT SL01001 AS kood, SL01040 AS aastamyyk,SL01041 AS kuumyyk,SY24003 AS agendinimi, SL01006 AS kontaktisik " +
                    ",SL01011 AS telefon, SL01024 AS maksetin,SL01036 AS ale" +
                    " FROM SL010100 LEFT JOIN SY240100 ON SY24002=SL01035 ORDER BY " +
                    "SL01002";
      connectionUp();
      Statement st2 = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ResultSet rs2 = st2.executeQuery(sql2);

      StringBuffer sb = new StringBuffer();
      String sep = ";";
      while(rs.next()){
        String cliID = rs.getString("klient").trim();
        sb.append(cliID + sep);
        sb.append(rs.getString("kuupaev").trim() + sep);
        sb.append(rs.getDouble("summa") + sep);
        sb.append(rs.getInt("ridu") + sep);
        String tekst = ensureLen(rs.getString("tekst")," ");
        tekst = tekst.replace('\'', ' ').replace('\"', ' ').replace(';', ' ');
        if (tekst == null) tekst = " ";

        sb.append(tekst + sep);

        boolean has = scrollTo(cliID, rs2);
        if (has){
         sb.append(ensureLen(rs2.getString("aastamyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("kuumyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("agendinimi"), " ") + sep);
         sb.append(ensureLen(rs2.getString("kontaktisik"), " ") + sep);
         String tel =  rs2.getString("telefon") ;
         tel.replace('\'', ' ').replace('\"', ' ').replace(';',' ');
         sb.append(ensureLen(tel, " ") + sep);
        // System.out.println(ensureLen(tel, " "));
         sb.append(ensureLen(rs2.getString("maksetin"), " ") + sep);
         sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
        }

        sb.append("\n");
      }

      ManagerData md = new ManagerData();
      md.setClientVisitInfo(sb.toString());

      sql = "SELECT OR20003 AS klient,OR20019 AS agent,OR20017 AS agentname FROM OR200100 " +
          "WHERE OR20015>? GROUP BY OR20003,OR20019,OR20017 ORDER BY OR20003";

      PreparedStatement ps = con.prepareStatement(sql);
      ps.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
      rs = ps.executeQuery();
      StringBuffer bf2 = new StringBuffer();
      while(rs.next()){
        bf2.append(rs.getString("klient").trim() + sep);
        bf2.append(rs.getString("agent").trim() + sep);
        String agentName = rs.getString("agentname");
         if (agentName != null) agentName = agentName.trim();
        if (agentName == null) agentName = " ";
        bf2.append(agentName + "\n");
      }

      md.setClientAgentInfo(bf2.toString());

      return md;
    }

    private boolean scrollTo(String id, ResultSet rs) throws SQLException{
      rs.beforeFirst();
      while(rs.next()){
        if (id.equals(rs.getString("kood").trim())){
          return true;
        }
      }
      return false;
    }

    private String ensureLen(String str, String replace){
      if (str == null) str = "";
      else str = str.trim().replace('\'', ' ').replace('\"', ' ').replace(';',' ');
      if (str.length() == 0){
        return replace;
      } else {
        return str;

      }
    }
    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getStartingTime() {
      return null;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getDriverVersion() {
	return DRIVER_VERSION;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getSubjectFilesTo() {
	return this.subjectFilesTo;
    }

    /**
     * Method declaration
     *
     *
     * @param s
     *
     * @see
     */
    public void log(String s) {
	getParent().log(s);
    }

    private static double parseDouble(String s){
      if (s.length() > 0) return Double.parseDouble(s);
      else return 0;
    }

       public NumberFormat getNF(){
         return NF;
       }

       /**
        *
        * @param aNF - the new value for NF
        */
       public void setNF(NumberFormat aNF){
         NF = aNF;
    }

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "ScalaTobaCall";
    private String             url = "jdbc:odbc:scalaDB";
    private String             url2 = "jdbc:odbc:report";
    private Connection	       con = null;
    private Connection	       con2 = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
    private  Vector prlines = new Vector();

}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

