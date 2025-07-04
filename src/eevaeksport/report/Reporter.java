
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.report;


import java.io.BufferedReader;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.mail.MessagingException;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.MarketInfoWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.EksportFrame;
import eevaeksport.Product;
import eevaeksport.driver.AbstractDBDriver;
import eevaeksport.driver.AlitaLTDBDriver;
import eevaeksport.driver.AltiaLATDBDriver;
import eevaeksport.driver.AtriaDBDriver;
import eevaeksport.driver.AxaBuraLatDBDriver;
import eevaeksport.driver.AxaDunkerDBDriver;
import eevaeksport.driver.AxaZpienasDBDriver;
import eevaeksport.driver.BSMSDBDriver;
import eevaeksport.driver.CrmData;
import eevaeksport.driver.ErplyLttDBDriver;
import eevaeksport.driver.GuttaDBDriver;

import eevaeksport.driver.KalevDBDriver;
import eevaeksport.driver.KodupaberDBDriver;
import eevaeksport.driver.LeiburDBDriver;
import eevaeksport.driver.LeiburLITDBDriver;
import eevaeksport.driver.MiesnieksDBDriver;
import eevaeksport.driver.MovexDBDriver;
import eevaeksport.driver.MovexLatDBDriver;
import eevaeksport.driver.NaviKPDBDriver;
import eevaeksport.driver.NaviSaarioinenDBDriver;
import eevaeksport.driver.NaviVilandraDBDriver;
import eevaeksport.driver.OERPRubinetaDBDriver;
import eevaeksport.driver.OskarLTDBDriver;
import eevaeksport.driver.RakvereDBDriver;
import eevaeksport.driver.RautaDBDriver;
import eevaeksport.driver.RivileAmberDBDriver;
import eevaeksport.driver.SAFKohviDBDriver;
import eevaeksport.driver.SAFMaagDBDriver;
import eevaeksport.driver.SAFSaaremaaDBDriver;
import eevaeksport.driver.SAFSireDBDriver;
import eevaeksport.driver.SAFSireLitDBDriver;
import eevaeksport.driver.SAFWiruDBDriver;
import eevaeksport.driver.Scala5iLATDBDriver;
import eevaeksport.driver.ScalaLFDBDriver;
import eevaeksport.driver.ScalaPrikeEstDBDriver;
import eevaeksport.driver.ScalaSaleDBDriver;
import eevaeksport.driver.ScalaTobaDBDriver;
import eevaeksport.driver.SkaiteAGDBDriver;
import eevaeksport.driver.SkaiteKLPDBDriver;
import eevaeksport.driver.SolcraftJunDBDriver;
import eevaeksport.driver.VSuusDBDriver;
import eevaeksport.driver.ValgaLKDBDriver;
import eevaeksport.driver.ValioAxaDBDriver;
import eevaeksport.driver.VichiESTSQLDBDriver;
import eevaeksport.driver.VichiLITMercDBDriver;
import eevaeksport.driver.VichiLITSQLDBDriver;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.POP3Reader;
import eevaeksport.util.Converter;


/**
 * <p>Title: </p>
 * <p>Description: </p>fdayreport
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */
public class Reporter {
    private Connection		con = null;
    private static final String SQL_TYPE_TIMESTAMP = "timestamp";
    private String		sqlType = null;
    Properties			props =
	EksportFrame.loadProps(EksportFrame.INI_FILE);
    static Category log = Category.getRoot();

    /**
     * Constructor declaration
     *
     *
     * @see
     */
    public Reporter() throws Exception {
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
	String xxx = props.getProperty("report.dbDname");
	if(xxx != null){
		databaseDriverName = xxx;
	}
	try {
		Class.forName(databaseDriverName).newInstance();
	}catch(Exception e) {
		
	}
	

	// System.out.println("dsn.user=" + props.getProperty("dsn.user"));
	// System.out.println("dsn.pass=" + props.getProperty("dsn.pass"));
	url = props.getProperty("report.dburl");

    PropertyConfigurator.configure("log4j.properties");


    log.error("reporter runned " + (new Date()).toString());
	sqlType = props.getProperty("report.datetype");
    try{
	    phgr = loadPH();
     }catch (Exception e){
	   e.printStackTrace();
     }
	
	if (url == null) {
	    System.out.println("report.dburl not defined in properties (jdbc:odbc:report ?)");
	}
	try{
		 con = DriverManager.getConnection(url, props.getProperty("dsn.user"),  props.getProperty("dsn.pass"));
	}catch(Exception e){
		try{
			con = DriverManager.getConnection(url, props.getProperty("dsna.user"),  props.getProperty("dsna.pass"));			
		}catch(Exception r){
			
		}
	}
	
	     fr = new EksportFrame(new String[]{});
	     driver = fr.getEksportPanel().getDriver();
         fr.agents = true;	
	
    }
    
    private TreeMap phgr = null;
    String url = null;
    
    public static boolean toDB = true;
    public static boolean makeall = true;
    /**
     * Method declaration
     *
     *
     * @param args
     *
     * @throws Exception
     *
     * @see
     */
    public static void main(String[] args) throws Exception {
	Reporter reporter = new Reporter();

	Properties props = EksportFrame.loadProps(EksportFrame.INI_FILE);
	   if("true".equals(props.getProperty("report.nodb"))){
		   toDB = false;
	   }
	   if("true".equals(props.getProperty("report.base"))){
		   makeall = false;
	   }
	   
	if(toDB)
	reporter.insertSuperVisorData();
	System.out.println("Defective entering");
	if(toDB && makeall)
	reporter.insertDefectiveData();
	//System.exit(0);
	reporter.execute();
//	System.out.println("Market entering");
//	reporter.insertMarketInfo();
	System.out.println("Supervisor entering");
	if(toDB)
	reporter.insertSuperVisorData();
	System.out.println("Defective entering");
	if(toDB && makeall)
	reporter.insertDefectiveData();
	//System.out.println("Distribution entering");
	//reporter.insertDistributionData();


	System.out.println("CLOSE ALL");

	System.exit(0);
    }

    /**
     * Method declaration
     *
     *
     * @throws IOException
     * @throws MessagingException
     * @throws SQLException
     *
     * @see
     */
    private void execute()
	    throws IOException, MessagingException, SQLException,
      ClassNotFoundException, IOException {

	// POP3Reader pop3 = new POP3Reader(concept.SUBJECT_DAYREPORT);
    	TreeMap propspict = new TreeMap() ;
    	
    	try {
    		InputStream is1 = new FileInputStream("forward.properties");
			BufferedReader br = new BufferedReader(new InputStreamReader(is1,
					"iso-8859-1"));
			String line;
			
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, "=");
				if (st.hasMoreTokens()) {
				String groupID = st.nextToken();
					if (st.hasMoreTokens()) {
						String emails = st.nextToken();
						propspict.put(groupID, emails);
					}
					
				}
			}
    	}catch(Exception e){
    		
    	}

    	
    	POP3Reader pop3 = null;			
    	ArrayList files = null;  
    	if( makeall){
    	
    		try {
    			
    		
    	
    	 pop3 = getPop3("defective",0);
    	if("true".equals(props.getProperty("report.delete"))){
   		 pop3.deleteFiles();
   	     }
    	 files = pop3.readFiles(true);
    	
    	}catch (Exception r) {
			
		}
    	try {
			
		
    	  pop3 = getPop3("supervisor",0);
          files = pop3.readFiles(true);
    	  }catch (Exception r) {
  			
  		}
      	if(toDB)
          insertSuperVisorData();
      	
      	try {
      	  pop3.deleteFiles();
          System.out.println("Supervisor");
  }catch (Exception r) {
  			
  		}
      
      	
          String ass = "MobileJava DayReport";
      	 if(props.getProperty("report.subject") != null){
      	 	ass = props.getProperty("report.subject");
      	 }
      	try {
      	 pop3 = getPop3(ass,0);
      	 boolean save = true;
      	 if(props.getProperty("report.nodayfile") != null && props.getProperty("report.nodayfile").equals("true")){
      		 save = false;
      	 }
      	 pop3.setSaveAttach(save);
  	     files = pop3.readFiles(false);
  	     System.out.println("sub " + ass + " size" + files.size());
    	  }catch (Exception r) {
    			
    		}
  	     try {
  	    	  parseFiles(files);
  	    	 pop3.deleteFiles();
  	     }catch(Exception e) {
  	    	 e.printStackTrace();
  	     }
  	   
          try {
          
    	pop3 = getPop3("PICTURES",1);
    	if(propspict != null && propspict.size() > 0 ){
    		pop3.setSmtpfrom(props.getProperty("from"));
    		pop3.setSmtphost(props.getProperty("smtphost"));
    		pop3.setSmtpcheck(propspict);
    		pop3.setForward(true);
    		if(props.getProperty("picture.link") != null){
    			pop3.setLinkText(props.getProperty("picture.link"));
    		}
    		if(props.getProperty("picture.text") != null){
    			pop3.setSendText(props.getProperty("picture.text"));
    		}
    		System.out.println("PICTURES settin texti " + pop3.isForward());

    	}
    	
    	files = pop3.readFiles(true);
    	System.out.println("PICTURES");

    	
    	if(toDB && makeall)
        insertDefectiveData();
        if("true".equals(props.getProperty("picture.distribution"))){
        	pictureDistribution();	
        }
        
        if("true".equals(props.getProperty("picture.folderdelivery"))){
        	 pictureFolderDelivery();	
       }
    	 }catch (Exception r) {
   			
   		}
    	
        if( makeall){
        
    		
    	try {
    	pop3 = getPop3("EVENTINFO",0);
    	files = pop3.readFiles(true);
    	
 }catch (Exception r) {
  			
  		}
    	if(toDB) {
    		
    		insertEventInfo(files,false);
    		File dir = new File(props.getProperty("report.dir"));
    	
    		File [] filesx = dir.listFiles(new FilenameFilter() {
    		    @Override
    		    public boolean accept(File dir, String name) {
    		        return name.contains("eventinfo");
    		    }
    		});
            
    		files = new ArrayList();
    		for (File eventfile : filesx) {
    		    files.add(props.getProperty("report.dir") + eventfile.getName());
    		}
    		
    		insertEventInfo(files,true);
    		
        	System.out.println("EVENTINFO");
    	}
    	
      try {  
        pop3 = getPop3("EMAILLOG",0);
	    files = pop3.readFiles(true);
        updateEmailLog(files);
	   System.out.println("EmailLogi");


        pop3 = getPop3("MESSAGES",0);
        files = pop3.readFiles(true);
    	if(toDB)
    	insertMessageData(files);
        System.out.println("MESSAGES 2");
       
        pop3 = getPop3("TASKINFO",0);
        files = pop3.readFiles(true);
        if(toDB)
            insertTASKData(files);
            System.out.println("TASKINFO 2");
 }catch (Exception r) {
  			
  		}
    	

    	
        }
        if("true".equals(props.getProperty("loadbl"))){
        	pop3 = getPop3("LAOSEIS",0);
        	try {
        	files = pop3.readFiles(true);
        	if(toDB)
        	updateClientStock(files);
        	System.out.println("Ladu");
        	}catch(Exception e) {e.printStackTrace();}
        }
    	
        System.out.println("Qualityinfo");


	       // pop3 = getPop3(ass,0);
	        
	        
    System.out.println("report");
    if("true".equals(props.getProperty("loadbl"))){
    try {
	pop3 = getPop3("BLOKAAD",0);
	files = pop3.readFiles(true);
	if(toDB)
	updateClientBlock(files);
	System.out.println("Blok");
    }catch(Exception e) {e.printStackTrace();}
    }}


        
    }
    
    

    

    private void insertMessageData(ArrayList files)
    throws IOException, SQLException {
    	System.out.println("files MESSAGE " + files);
if (files.size() > 0) {

	  String tableName = "mj_message";
      if(props.getProperty("mess.tablename") != null && !props.getProperty("mess.tablename").trim().equals("")){
      	tableName = props.getProperty("mess.tablename");
      }
	
    System.out.println("MESSAGEDATA - ACTION");

	for (int i = 0; i < files.size(); i++) {

	    String	   fileName = (String) files.get(i);
	    BufferedReader br =
	    	new BufferedReader(
	    			   new InputStreamReader(
	    	                      new FileInputStream(fileName), "UTF8"));
	    String	   line = null;
	    String	   type = null;
	    String	   agentID = null;
	    String	   agentName = null;
	    String     custID  = null;
	    String     custName  = null;
	    String     tekst = null;
	    long     sent  = 0;
	
	    java.sql.Date     date =  null;
	    while ((line = br.readLine()) != null) {
	    	System.out.println(line);
		StringTokenizer st = new StringTokenizer(line, ";");
		while (st.hasMoreTokens()) {
			
			try {
				
			
			type = st.nextToken();
		    agentID = st.nextToken();
		    agentName = st.nextToken();
		    custID = st.nextToken();
		    custName = st.nextToken();
		   	tekst = st.nextToken();
		  
		   	try {
		   		if(st.hasMoreTokens()){
			   		sent = Long.parseLong(st.nextToken());	
			   	}	
			}catch(Exception k) {
				
			}
		   	
			date = new java.sql.Date(new Date().getTime());
		    
			/*agentid    VARCHAR2(20)  NULL,  (Here I want saleman code who sends message i.e. C13)
  clid       VARCHAR2(20)  NULL, (Here I want customer code i.e. 7006879)
  mstype     VARCHAR2(50)  NULL, (Here I want message type (Birojam, Tehni�im, Vad�t�jam, Lo�istikai)
  sentdate   TIMESTAMP(6)  NULL, (Here I want date and time when message is sent  - same format as in MJtask table 10.09.13 16:34:44.569)
  origtext   VARCHAR2(500) NULL, (Here I want the message text  what salesman has entered in message field i.e. 16.12.14. aizmirsu piesaukt atlaidi no 20 iep. D�onam Real men kola. Izlabo l�dzu. Tel.29336901) 
  mestext    VARCHAR2(500) NULL, (here I want one more time same text as in field origtext)
  status     NUMBER        NULL, (here I want just 0 for your inserts)
  chtime     TIMESTAMP(6)  NULL, (here I want just NULL for your inserts)
  userid     VARCHAR2(50)  NULL (Here I want just NULL tor your inserts)

*/
		    String sql = "INSERT into " + tableName + " values (?,?,?,?,?,?,0,null,'')";
		    
		    try{
				 con = DriverManager.getConnection(url, props.getProperty("dsn.user"),  props.getProperty("dsn.pass"));
			}catch(Exception e){
				try{
					con = DriverManager.getConnection(url, props.getProperty("dsna.user"),  props.getProperty("dsna.pass"));			
				}catch(Exception r){
					
				}
			}
		    PreparedStatement stw = con.prepareStatement(sql);
              stw.setString(1,agentID );
		      stw.setString(2, custID);
		      stw.setString(3, type);
		      stw.setTimestamp(4, new java.sql.Timestamp(sent));
			  	
		      stw.setString(6, tekst);
		      stw.setString(5, tekst);
		      
				      try {

					      stw.executeUpdate();
					      con.close();  
				      }catch(Exception e) {
				    	  con.close();
				      }
    		
		}catch(Exception k) {
			
		}
		}
	    }
	    br.close();
    }
    }
}
    

    
    
    
    private void insertTASKData(ArrayList files)
    throws IOException, SQLException {
    	System.out.println(" Taskinfo files " + files);
if (files.size() > 0) {

	  String tableName = "mj_task";
      if(props.getProperty("task.tablename") != null && !props.getProperty("task.tablename").trim().equals("")){
      	tableName = props.getProperty("task.tablename");
      }
	
    System.out.println("TASKDATA - ACTION");

	for (int i = 0; i < files.size(); i++) {

	    String	   fileName = (String) files.get(i);
	    if("true".equals(props.getProperty("testingtask"))){
	    	fileName = "testingtask.txt";
	    }
	    BufferedReader br =
                new BufferedReader(new FileReader(fileName));
	    String	   line = null;
	    String	   agentID = null;
	    String	   agentName = null;
	    String     message = null;
	    String     header = null;
	    String     status = null;
	    String     custID  = null;
	    String     reminder = null;
	    String     custName  = null;
	    String     lineid  = "0";
	    String     origin  = "0";
	    String     datetext = "";
	    String     llong = "0";
	    String photo = null;
	    String done = null;
	   
	
	 
	    while ((line = br.readLine()) != null) {
	    	System.out.println(line);
	    	br.mark(1000);
	    	String line2 = br.readLine();
	    	String line3 = br.readLine();
	    	br.reset();
	    	if(line2 != null && line2.length() < 1000){
	    		line = line + line2;
	    	}
	    	if(line3 != null && line3.length() < 1000){
	    		line = line + line3;
	    	}
	    	
	    	if(line.startsWith(";")){
	    		line = " " + line;
	    	}
	    	line = line.replaceAll(";;", "; ;");
	    	
		StringTokenizer st = new StringTokenizer(line, ";");
		try{
			
		
		while (st.hasMoreTokens()) {
			origin = st.nextToken();
			lineid = st.nextToken();
		    agentID = st.nextToken();
		    agentName = st.nextToken();
		    custID = st.nextToken();
		    custName = st.nextToken();
		   	datetext = st.nextToken();
		    llong = st.nextToken();
		    reminder = st.nextToken();
		    photo = st.nextToken();
		    status = st.nextToken();
		    done = st.nextToken();
		    header = st.nextToken();
		    message = st.nextToken();
		    long remi = 0;
		    try{
		    	remi = Long.parseLong(reminder);
		    }catch(Exception e){
		    	
		    }
		    
		   int donei = Integer.parseInt(done);
		   if(donei == 1){
			   String ss = "UPDATE " + tableName + " set status = 1 , res1 ='" + llong + "' where lineid='" + origin + "'";
			   try{
					 con = DriverManager.getConnection(url, props.getProperty("dsn.user"),  props.getProperty("dsn.pass"));
				}catch(Exception e){
					try{
						con = DriverManager.getConnection(url, props.getProperty("dsna.user"),  props.getProperty("dsna.pass"));			
					}catch(Exception r){
						
					}
				}
			   Statement stw = con.createStatement();
			   System.out.println("updating " + ss);
			      try {

				      stw.executeUpdate(ss);
				      stw.close();
					   con.close(); 
			      }catch(Exception e) {
			    	  con.close();
			      }
			  
			   
		   }

		   
		   if(remi > 10000){
			   String ss = "UPDATE " + tableName + " set reminder = ? where lineid='" + origin + "'";
			   try{
					 con = DriverManager.getConnection(url, props.getProperty("dsn.user"),  props.getProperty("dsn.pass"));
				}catch(Exception e){
					try{
						con = DriverManager.getConnection(url, props.getProperty("dsna.user"),  props.getProperty("dsna.pass"));			
					}catch(Exception r){
						
					}
				}
			
			   PreparedStatement stw = con.prepareStatement(ss);
			   stw.setTimestamp(1, new java.sql.Timestamp(Long.parseLong(reminder)));
			   System.out.println("updating reminder " + ss + " " + reminder);
			   try {
				   stw.executeUpdate();
				   stw.close();
				   con.close();
					   
			   }catch(Exception e) {
				   
				   con.close();
					
			   }
			   
			   	   
					   
		   }
		   
		    String sql = "INSERT into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,'','',0,0)";

					   try{
							 con = DriverManager.getConnection(url, props.getProperty("dsn.user"),  props.getProperty("dsn.pass"));
						}catch(Exception e){
							try{
								con = DriverManager.getConnection(url, props.getProperty("dsna.user"),  props.getProperty("dsna.pass"));			
							}catch(Exception r){
								
							}
						}
					
		    PreparedStatement stw = con.prepareStatement(sql);

		      stw.setString(1, lineid );
		      stw.setString(2, origin);
		      stw.setString(3, agentID );
		      stw.setString(4, Converter.deCode(agentName));
		      stw.setString(5, Converter.deCode(custID));
		      stw.setString(6, Converter.deCode(custName));
		     // if (SQL_TYPE_TIMESTAMP.equals(sqlType)) {
		    	    stw.setTimestamp(7, new java.sql.Timestamp(Long.parseLong(llong)));
		  	  //  } else {
		  	    	//stw.setDate(5, date);
		  	   // }
		      stw.setString(8, datetext.substring(datetext.length() - 8));
		      stw.setTimestamp(9, new java.sql.Timestamp(Long.parseLong(reminder)));
		      stw.setString(10, photo);
		      stw.setInt(11, donei);
		      stw.setString(12, Converter.deCode(header.replace( '|', '\n').replace( '|', '\n').replace( '|', '\n').replace( '|', '\n').replace( '|', '\n')));
		      stw.setString(13, Converter.deCode(message.replace( '|', '\n').replace( '|', '\n').replace( '|', '\n').replace( '|', '\n').replace( '|', '\n')));
		      stw.executeUpdate();
		      System.out.println("inserting line " + sql);
			  con.close();
    		}
		}catch(Exception s){
			if(con != null)
			con.close();
    		s.printStackTrace();	
    		}
	    }
	    br.close();
    }
    }
}
    
    

    private void insertSalesPlanData(ArrayList files)
    throws IOException, SQLException {
if (files.size() > 0) {
   
    System.out.println("SALESPALN - ACTION");

    if (driver instanceof CrmData) {
	for (int i = 0; i < files.size(); i++) {

	    String	   fileName = (String) files.get(i);
	    BufferedReader br =
                new BufferedReader(new FileReader(fileName));
	    String	   line = null;
	    String	   agentID = null;
	    String     agentName = null;
	    String     custID  = null;
	    String     custName = null;
	    String tekst = null;
	
	    java.sql.Date     date =  null;
	    while ((line = br.readLine()) != null) {
	    	System.out.println(line);
		StringTokenizer st = new StringTokenizer(line, ";");
		while (st.hasMoreTokens()) {
		    agentID = st.nextToken();
		   	agentName  = st.nextToken();
		    custID = st.nextToken();
		    custName = st.nextToken();
		    tekst = st.nextToken();
		    date = toDate(st.nextToken());

		    ((CrmData) driver).setCrmData(agentID,agentName,custID,custName,tekst, date);
	        System.out.println("CRM: " + agentID  + " " + agentName  + " " + custID + " " + custName  + " " + tekst + " " + date);

		}
	    }

	    br.close();


    }
    }
}
}
   private java.sql.Date toDate(String dd){
   	java.sql.Date result = new java.sql.Date(new Date().getTime());

   	return result;
   }

    private void insertSuperVisorData() throws IOException, ClassNotFoundException,
      IOException {
    	System.out.println("fffh");
      File   dir = new File(props.getProperty("report.dir"));
      File[] files = dir.listFiles();
            if (files != null){
              for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (f.getName().toLowerCase().endsWith(".sup")) {
                
                  ObjectInputStream ois = null; 
                
                	  try{
                	      ois = new ObjectInputStream(new FileInputStream(f));
                		  SupervisorInfoWrapperMeri visor = new SupervisorInfoWrapperMeri(ois.readObject());
                   	   ois.close();
                   	   Date dd  = new Date(f.lastModified());
                          insertSuperVisorSQLMeri(visor,dd);	
                	}catch(Exception ee){
                		ee.printStackTrace();
                	}
                	
                  
                 
               
                  File done = new File(props.getProperty("report.dir") + System.getProperty("file.separator") + "done");
                  if(!done.exists()){
                	  done.mkdir();
                  }
                  File fff = new File (props.getProperty("report.dir") + System.getProperty("file.separator") + "done"+   System.getProperty("file.separator") +f.getName());
                  System.out.println("fff " + fff.getAbsolutePath());
                  f.renameTo(fff);
                 if(f.exists())
                  f.delete();
                
                }
              }
            }
         /*   if("true".equals(props.getProperty("report.updatecalls"))){
            	try{
               	 EksportFrame     fr = new EksportFrame(new String[]{});
               	 AbstractDBDriver driver = fr.getEksportPanel().getDriver();
               	if (driver instanceof SAFKohviDBDriver){
                       ((SAFKohviDBDriver)driver).updateCalls();
                     }
                   	
               }catch(Exception e){
               	
               }
               	
            }*/
            
            
          }





    private void insertDefectiveData() throws IOException, ClassNotFoundException,
      EOFException {
      File   dir = new File(props.getProperty("report.dir"));
      File[] files = dir.listFiles();
            if (files != null){
              for (int i = 0; i < files.length; i++) {
                File f = files[i];

                if (f.getName().toLowerCase().endsWith(".obj") && f.length() != 0) {
                  ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
              
                  try{
                	     DefectiveWrapper defective = new DefectiveWrapper(ois.readObject());
                	   ois.close();
                	   insertDefectiveSQL(defective);
                  }catch (Exception e){
                	  ois.close();
                	  ois = new ObjectInputStream(new FileInputStream(f));
                	try{
                		DefectiveWrapperMeri visor = new DefectiveWrapperMeri(ois.readObject());
                   	   ois.close();
                   	   
                   	   insertDefectiveSQLMeri(visor);	
                	}catch(Exception ee){
                		ee.printStackTrace();
                	}
                	
                  }
                  
                  
                  
                  
              
                  
                  
                  
                  
                  File done = new File(props.getProperty("report.dir") + System.getProperty("file.separator") + "done");
                  if(!done.exists()){
                	  done.mkdir();
                  }
                  File fff = new File (props.getProperty("report.dir") + System.getProperty("file.separator") + "done"+   System.getProperty("file.separator") +f.getName());
                  System.out.println("fff " + fff.getAbsolutePath());
                  f.renameTo(fff);
                 if(f.exists())
                  f.delete();
                }
                 if (f.getName().toLowerCase().endsWith(".obj")  && f.exists()){
                     f.delete(); 
                 }
              }
            }
          }
    
	public void writeConsloStr(String path,String e) {
		
		try {
			
			File f = new File(path);
			if(!f.exists()){
				  try{
				      FileOutputStream fos = new FileOutputStream(path);
				        fos.write("".getBytes());
				        fos.close();
				
			} catch (Exception uu) {
				uu.printStackTrace();
			}
			}
			
			PrintWriter os = new PrintWriter(new FileWriter(
					path, true));
			os.write(e);
			os.close();
		

	  } catch (Exception es) { 
          es.printStackTrace(); 
      }
		/*
		BufferedReader reader = null; 
        BufferedWriter writer = null; 
        ArrayList list = new ArrayList(); 

        try { 
            reader = new BufferedReader(new FileReader(path)); 
            String tmp; 
            while ((tmp = reader.readLine()) != null){
            	   list.add(tmp); 
            }
             
           reader.close();
 
            list.add(0, e); 
           // list.add("End Text"); 
 
            writer = new BufferedWriter(new FileWriter(path)); 
            for (int i = 0; i < list.size(); i++){
            	  writer.write(list.get(i) + "\r\n");	
            }
               
            writer.close();
        } catch (Exception es) { 
            es.printStackTrace(); 
        }
*/
}
	 private String getYYYYpMMpDDpTimeNiceString(long ll){
		    StringBuffer sb = new StringBuffer();
		    Calendar cal= Calendar.getInstance();
		    cal.setTimeInMillis(ll);
		   
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
    
	 
	 
	    private void pictureFolderDelivery() throws IOException, ClassNotFoundException,
	    EOFException {
	    File   dir = new File(props.getProperty("report.dir"));
	    if(props.getProperty("picture.dir") != null){
	    	dir = new File(props.getProperty("picture.dir"));
	    }
	    File[] files = dir.listFiles();
	          if (files != null){
	            for (int i = 0; i < files.length; i++) {
	              File f = files[i];

	              if (f.getName().toLowerCase().endsWith(".jpg") && f.length() != 0) {
	            	  String name = f.getName();
	                 String custid = null;
	                 String phgroup = null;
	                 String agent = null;
	                 String country = "";
	                 String phchoice = null;
	                 long made = f.lastModified();
	                 
	                 if(name.indexOf("{") != -1){
	                	 agent = name.substring(0,name.indexOf("-"));
	                	  custid = name.substring(name.indexOf("{")+1,name.indexOf("}"));
	                	  if(name.indexOf("[") != -1){
	                		  phgroup =  name.substring(name.indexOf("[")+1,name.indexOf("]"));  
	                	  }
	                	 country = name.substring(name.indexOf("]")+1 , name.indexOf("]") + 3);
	                	try{
	                		 phchoice = name.substring(name.indexOf(".")+1,name.lastIndexOf("."));
	                	 }catch(Exception e){
	                	 }
	                 }
	               
	                if(props.getProperty("picture.country") != null){
	                	country = props.getProperty("picture.country") ;
	                }
	                File done = new File(dir + System.getProperty("file.separator") + phgroup);
	                if(!done.exists()){
	                	done.mkdir();
	                }
	                
	                
	                //teeme alam kataloogi ka ja kui on olemas siis sinna ka salvestame 
	                if(phgr != null && phchoice != null && phgr.get(phchoice) != null){
	                	phchoice = phgr.get(phchoice) + "";
	                	 File done2 = new File(dir + System.getProperty("file.separator") + phgroup +
	 	                		System.getProperty("file.separator") + phchoice );
	 	                if(!done2.exists()){
	 	                	done2.mkdir();
	 	                }
	 	                done = done2;
	                }
	                
	               
	               
	                
	    			    String fname = f.getName();
	    			    File fff = new File (done.getAbsolutePath() + System.getProperty("file.separator") +fname);
	                       f.renameTo(fff);
	               if(f.exists())
	                    f.delete();
	                  }
	               if (f.getName().toLowerCase().endsWith(".jpg")  && f.exists()){
	                   f.delete(); 
	               }
	            }
	          }
	        }

	 
	 
	 
    private void pictureDistribution() throws IOException, ClassNotFoundException,
    EOFException {
    File   dir = new File(props.getProperty("report.dir"));
    if(props.getProperty("picture.dir") != null){
    	dir = new File(props.getProperty("picture.dir"));
    }
    File[] files = dir.listFiles();
          if (files != null){
            for (int i = 0; i < files.length; i++) {
              File f = files[i];

              if (f.getName().toLowerCase().endsWith(".jpg") && f.length() != 0) {
            	  String name = f.getName();
                 String custid = null;
                 String phgroup = null;
                 String agent = null;
                 String country = "";
                 String phchoice = null;
                 long made = f.lastModified();
                 
                 if(name.indexOf("{") != -1){
                	 if(name.startsWith("m-")){
                		 agent = name.substring(2);
                		 agent = agent.substring(0,agent.indexOf("-"));
                	 }else{
                		 agent = name.substring(0,name.indexOf("-"));  
                	 }
                	
                	  custid = name.substring(name.indexOf("{")+1,name.indexOf("}"));
                	  if(name.indexOf("[") != -1){
                		  phgroup =  name.substring(name.indexOf("[")+1,name.indexOf("]"));  
                	  }
                	 country = name.substring(name.indexOf("]")+1 , name.indexOf("]") + 3);
                	try{
                		
                		 phchoice = name.substring(name.indexOf(".")+1,name.lastIndexOf("."));
                	 }catch(Exception e){
                		 
                	 }
                	 
             
                 }
                Customer cc = getCustomer(custid);
                String custname = "bad name";
                if(cc != null){
                	custname = cc.getName();
                }
                System.out.println("ag " + agent +" " + phgroup +  " " + custid + " " +custname+ " " + country + " ch" + phchoice + " "+f.getName() + " " +  (name.length() - name.replaceAll("\\.", "").length()) );
                
                String chain  = "";
                String region = "";
                String aadr = "";
                String agentname = "";
               
                if(props.getProperty("picture.country") != null){
                	country = props.getProperty("picture.country") ;
                }
                if(cc != null){
                	custname = cc.getName();
                	chain = cc.getJuridics();
                    region = cc.getGroup();
                    aadr = cc.getAddress();
                    country = cc.getCity();
                }
                File done = new File(dir + System.getProperty("file.separator") + phgroup);
                if(!done.exists()){
                	done.mkdir();
                }
               String madetime  = getYYYYpMMpDDpTimeNiceString(made);
                
             	  String lineSep = System.getProperty("line.separator");
    			    writeConsloStr(done.getAbsolutePath() +  System.getProperty("file.separator") + "photolog.txt","\r\nTimestamp : " + madetime + "\r\nagent : " + agent + "\r\nphgroup : " + phgroup + "\r\ncustid : " + custid + 
    			    		"\r\ncustname : " + custname + "\r\naddress : " +  aadr + "\r\nchain : " + chain + "\r\nregion : " + region + "\r\npicturepath : "  + done.getAbsolutePath() + System.getProperty("file.separator") + f.getName() +lineSep);
    			 
    			    writeConsloStr(dir +  System.getProperty("file.separator") + "photolog.txt","\r\nTimestamp : " + madetime + "\r\nagent : " + agent + "\r\nphgroup : " + phgroup + "\r\ncustid : " + custid + 
    			    		"\r\ncustname : " + custname + "\r\naddress : " +  aadr + "\r\nchain : " + chain + "\r\nregion : " + region + "\r\npicturepath : "  + done.getAbsolutePath() + System.getProperty("file.separator") + f.getName() +lineSep);
    			
    			    String fname = f.getName();
                  if(cc != null){
                	  fname = f.getName().replace(custid, cc.getID());
                  }
    			 insertToImageBase(made,agent,agentname ,custid ,custname ,
                   		   aadr ,  chain, region , fname   , done.getAbsolutePath()  , "" , "" , 0 , phgroup,country,phchoice);
                File fff = new File (done.getAbsolutePath() + System.getProperty("file.separator") +fname);
                f.renameTo(fff);
               if(f.exists())
                f.delete();
              }
               if (f.getName().toLowerCase().endsWith(".jpg")  && f.exists()){
                   f.delete(); 
               }
            }
          }
        }

    

    private Customer getCustomer(String custid ){
    	Customer cc = null;
    	   if (driver instanceof SkaiteAGDBDriver){
    	       cc =  ((SkaiteAGDBDriver)driver).getCustomerSingle(custid);
    	       }
    	return cc;
    }
    




   
    
    private void insertDefectiveSQLMeri(DefectiveWrapperMeri def){
        if (driver instanceof MovexDBDriver){
          ((MovexDBDriver)driver).insertDefectiveMeri(def);
        }
        if (driver instanceof KalevDBDriver){
            ((KalevDBDriver)driver).insertDefectiveMeri(def);
          }
        if (driver instanceof AltiaLATDBDriver){
            ((AltiaLATDBDriver)driver).insertDefectiveMeri(def);
          }
        if (driver instanceof MovexLatDBDriver){
         ((MovexLatDBDriver)driver).insertDefectiveMeri(def);
       }   if (driver instanceof ValgaLKDBDriver){
          ((ValgaLKDBDriver)driver).insertDefectiveMeri(def);
       }
        
        if (driver instanceof NaviSaarioinenDBDriver){
          ((NaviSaarioinenDBDriver)driver).insertDefectiveMeri(def);
        }
        if (driver instanceof SAFWiruDBDriver){
          ((SAFWiruDBDriver)driver).insertDefectiveMeri(def);
        }

        if (driver instanceof MiesnieksDBDriver){
          ((MiesnieksDBDriver)driver).insertDefectiveMeri(def);
        }
      }

    
    private void insertDefectiveSQL(DefectiveWrapper def){
      if (driver instanceof MovexDBDriver){
        ((MovexDBDriver)driver).insertDefective(def);
      }
      if (driver instanceof KalevDBDriver){
          ((KalevDBDriver)driver).insertDefective(def);
        }
      if (driver instanceof AltiaLATDBDriver){
          ((AltiaLATDBDriver)driver).insertDefective(def);
        }
      if (driver instanceof MovexLatDBDriver){
       ((MovexLatDBDriver)driver).insertDefective(def);
     }   if (driver instanceof ValgaLKDBDriver){
        ((ValgaLKDBDriver)driver).insertDefective(def);
     }
      if (driver instanceof NaviSaarioinenDBDriver){
        ((NaviSaarioinenDBDriver)driver).insertDefective(def);
      }
      if (driver instanceof SAFWiruDBDriver){
        ((SAFWiruDBDriver)driver).insertDefective(def);
      }


      if (driver instanceof MiesnieksDBDriver){
    	  ((MiesnieksDBDriver)driver).insertDefective(def);
      }
      
    }


    private void insertToImageBase(long madedate,String agent,String agentname ,String custid ,String custname ,
    		   String custaddr , String chain, String region ,String  filename  ,String filepath ,String res1 , String res2 ,double res3,String imgroup , String country
    		   ,String imgchoice){
        if (driver instanceof SkaiteAGDBDriver ){
          ((SkaiteAGDBDriver)driver).insertToImageBase( madedate,agent,agentname ,custid ,custname ,
       		   custaddr ,  chain, region , filename  , filepath , res1 , res2 , res3,imgroup ,country,imgchoice);
        }
      
      }


    
    

    private void insertSuperVisorSQL(SupervisorInfoWrapper vis,Date dd){
   

    }

    
    
    private void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri vis,Date dd){
             	System.out.println("ggg meri1");
               if (driver instanceof ScalaSaleDBDriver){
                   ((ScalaSaleDBDriver)driver).insertSuperVisorSQLMeri(vis);
                }
               if (driver instanceof NaviKPDBDriver){
                   ((NaviKPDBDriver)driver).insertSuperVisorSQLMeri(vis);
                }
               
               if (driver instanceof OskarLTDBDriver){
                   ((OskarLTDBDriver)driver).insertSuperVisorSQLMeri(vis);
                }
               if (driver instanceof OERPRubinetaDBDriver){
                   ((OERPRubinetaDBDriver)driver).insertSuperVisorSQLMeri(vis);
                }
              
               
               if (driver instanceof ScalaPrikeEstDBDriver){
                   ((ScalaPrikeEstDBDriver)driver).insertSuperVisorSQLMeri(vis);
                }
               
               if (driver instanceof ErplyLttDBDriver ){
                   ((ErplyLttDBDriver)driver).insertSuperVisorSQLMeri(vis);
                 }
               
               if (driver instanceof AxaZpienasDBDriver){
                   ((AxaZpienasDBDriver)driver).insertSuperVisorSQLMeri(vis);
                }
               if (driver instanceof SkaiteAGDBDriver){
                   ((SkaiteAGDBDriver)driver).insertSuperVisorSQLMeri(vis);
                }
               
               if (driver instanceof SkaiteKLPDBDriver){
                   ((SkaiteKLPDBDriver)driver).insertSuperVisorSQLMeri(vis);
                }
               
               
               if (driver instanceof RautaDBDriver){
                   ((RautaDBDriver)driver).insertSuperVisorSQLMeri(vis);
                }
               
               if (driver instanceof SAFSireDBDriver){
                   ((SAFSireDBDriver)driver).insertSuperVisorSQLMeri(vis);
                }
               
               if (driver instanceof SAFSireLitDBDriver){
                   ((SAFSireLitDBDriver)driver).insertSuperVisorSQLMeri(vis);
                }
               
               
               if (driver instanceof VichiLITSQLDBDriver){
                   ((VichiLITSQLDBDriver)driver).insertSuperVisorSQLMeri(vis);
                }
               if (driver instanceof RakvereDBDriver){
                   ((RakvereDBDriver)driver).insertSuperVisorSQLMeri(vis);
                }             
               if (driver instanceof MovexLatDBDriver ){
                   ((MovexLatDBDriver)driver).insertSuperVisorSQLMeri(vis);
                 }
               
                 if (driver instanceof LeiburDBDriver ){
                     ((LeiburDBDriver)driver).insertSuperVisorSQLMeri(vis);
                   }
                 if (driver instanceof LeiburLITDBDriver ){
                     ((LeiburLITDBDriver)driver).insertSuperVisorSQLMeri(vis);
                   }
                 if (driver instanceof NaviSaarioinenDBDriver ){
                   ((NaviSaarioinenDBDriver)driver).insertSuperVisorSQLMeri(vis);
                 }
                 if (driver instanceof ValioAxaDBDriver ){
                     ((ValioAxaDBDriver)driver).insertSuperVisorSQLMeri(vis);
                   }
                 if (driver instanceof RivileAmberDBDriver ){
                     ((RivileAmberDBDriver)driver).insertSuperVisorSQLMeri(vis);
                   }  
                 if (driver instanceof SAFWiruDBDriver ){
                     ((SAFWiruDBDriver)driver).insertSuperVisorSQLMeri(vis);
                   }
                 if (driver instanceof SAFSaaremaaDBDriver ){
                   ((SAFSaaremaaDBDriver)driver).insertSuperVisorSQLMeri(vis);
                 }
             
                 if (driver instanceof VichiESTSQLDBDriver ){
                     ((VichiESTSQLDBDriver)driver).insertSuperVisorSQLMeri(vis);
                   }
                 
              
                 if (driver instanceof SAFKohviDBDriver ){
                   ((SAFKohviDBDriver)driver).insertSuperVisorSQLMeri(vis);
                 }
                 if (driver instanceof GuttaDBDriver ){
                   ((GuttaDBDriver)driver).insertSuperVisorSQLMeri(vis);
                 }
            
                //}
                 if (driver instanceof MovexDBDriver ){
                   ((MovexDBDriver)driver).insertSuperVisorSQLMeri(vis);
                 }
                 if (driver instanceof VSuusDBDriver ){
                   ((VSuusDBDriver)driver).insertSuperVisorSQLMeri(vis);
                  }
                 if (driver instanceof AxaDunkerDBDriver ){
                     ((AxaDunkerDBDriver)driver).insertSuperVisorSQLMeri(vis);
                    }

               	System.out.println("ggg meri2");

                 if (driver instanceof Scala5iLATDBDriver ){
                 //	System.out.println("ttt");
                  // ((Scala5iLATDBDriver)driver).insertSuperVisorSQL(vis);
                  }


                 if (driver instanceof SolcraftJunDBDriver ){
                   ((SolcraftJunDBDriver)driver).insertSuperVisorSQLMeri(vis);
                  }


                 if (driver instanceof KodupaberDBDriver ){
                   ((KodupaberDBDriver)driver).insertSuperVisorSQLMeri(vis);
                  }

                 if (driver instanceof MiesnieksDBDriver){
                   ((MiesnieksDBDriver)driver).insertSuperVisorSQLMeri(vis);
                 }
                 
                 if (driver instanceof ScalaLFDBDriver){
                     ((ScalaLFDBDriver)driver).insertSuperVisorSQLMeri(vis);
                   }
                 if (driver instanceof NaviVilandraDBDriver){
                     ((NaviVilandraDBDriver)driver).insertSuperVisorSQLMeri(vis);
                   }
                 if (driver instanceof AxaBuraLatDBDriver){
                     ((AxaBuraLatDBDriver)driver).insertSuperVisorSQLMeri(vis);
                   }
               
      }

    
    private void insertMISQL(MarketInfoWrapper mi) {}



    /**
     * Method declaration
     *
     *
     * @param f
     *
     * @return
     *
     * @see
     */
    private Vector readMarketInfo(File f) {
	try {
	    ObjectInputStream ois =
		new ObjectInputStream(new FileInputStream(f));
	    Vector	      result = (Vector) ois.readObject();

	    ois.close();

	    return result;
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return null;
    }

    /**
     * Method declaration
     *
     *
     * @param files
     *
     * @throws IOException
     * @throws SQLException
     *
     * @see
     */
    private void updateClientBlock(ArrayList files)
	    throws IOException, SQLException {
	if (files.size() > 0) {
	 
	    System.out.println("BLOKAAD");

	    if (driver instanceof ClientBlock) {
		for (int i = 0; i < files.size(); i++) {
		    HashMap	   hm = new HashMap();
		    String	   fileName = (String) files.get(i);
		    BufferedReader br =
                    new BufferedReader(new FileReader(fileName));
		    String	   line = null;
		    String	   dealerID = null;
		    ArrayList      custCodes = new ArrayList();
		    while ((line = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line, ";");
			while (st.hasMoreTokens()) {
			    dealerID = st.nextToken();
			    while (st.hasMoreTokens()) {
				custCodes.add(st.nextToken());
				  if(dealerID.length() < 3 && dealerID.trim().length() >0) {
					    hm.put(dealerID, custCodes);
					    }
			    }
			}
		    }

		    br.close();
		  
		    if (hm.size() > 0) {
		    	try{
		    		((ClientBlock) driver).blockClients(hm);
                    System.out.println("Blocks: " + dealerID);
		    	}catch (Exception e){
		    		e.printStackTrace();
		    	}

		    }

        }
	    }
	}
    }

  
    private void insertEventInfo(ArrayList files, boolean delete)
    throws IOException, SQLException {
if (files != null && files.size() > 0) {
  
    System.out.println("EventInfo entering");

    if (driver instanceof EventInfo) {
	for (int i = 0; i < files.size(); i++) {
	    Vector	   hm = new Vector();
	    String	   fileName = (String) files.get(i);
	    BufferedReader br =
                new BufferedReader(new FileReader(fileName));
	    String	   line = null;
	    while ((line = br.readLine()) != null) {
	    	 hm.add(line);
          }
	    br.close();
	   
	    if (hm.size() > 0) {
	    	try{
	    		((EventInfo) driver).eventInfo(hm);
                System.out.println("Eventinfo lines: " + hm.size());
	    	}catch (Exception e){
	    		e.printStackTrace();
	    	}

	    }
        if(delete) {
        	new File(fileName).delete();
        }
    }
    }
}
}




    private void updateClientSales(ArrayList files)
            throws IOException, SQLException {
        if (files.size() > 0) {
         
            System.out.println("MyygiAruanne");

            if (driver instanceof ClientBlock) {
                for (int i = 0; i < files.size(); i++) {
                    HashMap hm = new HashMap();

                    System.out.println("MYYGIARUANDEID  "
                                       + files.size() + " " + i);

                    String	   fileName = (String) files.get(i);

                    BufferedReader br =
                        new BufferedReader(new FileReader(fileName));
                    String	   line = null;
                    String	   dealerID = null;
                    ArrayList      custprods = new ArrayList();

                    while ((line = br.readLine()) != null) {
                        StringTokenizer st = new StringTokenizer(line, ";");

                        if (st.hasMoreTokens() && line.trim().length()>0) {
                            dealerID = st.nextToken();

                            if (st.hasMoreTokens()) {
                              String custID = st.nextToken();

                              if (st.hasMoreTokens()) {
                                String prodID = st.nextToken();
                                String prodID2 = null;
                                if ("Russia".equals(props.getProperty("language"))) {
                                  prodID2 = new String(prodID.getBytes(), "Cp1251");
                                  System.out.println(prodID + prodID2);
                                }
                                double price = 0;
                                double stock = 0;
                                java.sql.Date reportmonth = null;
                                if (st.hasMoreTokens()) {
                                  stock = (Double.parseDouble(st.nextToken()));
                                if (st.hasMoreTokens()) {
                                    price = (Double.parseDouble(st.nextToken()));
                                }
                                if (st.hasMoreTokens()) {
                                    reportmonth = new java.sql.Date(Long.parseLong(st.nextToken()));
                                }
                                }
                                CustProd cp = new CustProd(custID,prodID,0,stock,0," ",price,0,0," ");
                              if(reportmonth != null){
                                cp.setReportmonth(reportmonth);
                              }
                                custprods.add(cp);
                               // System.out.println(dealerID+custID+prodID+stock+price);
                              }
                            }
                        }
                    }
                    System.out.println(dealerID);
                    br.close();
                    hm.put(dealerID, custprods);

                    if (hm.size() > 0) {
                        ((SalesSetter) driver).setSales(hm);
                    }
                }
            }
        }
    }

    /**
     * Method declaration
     *
     *
     * @param files
     *
     * @throws IOException
     * @throws SQLException
     *
     * @see
     */
    
    
    private EksportFrame     fr = null;
    private    AbstractDBDriver driver = null;
    
    private void updateClientStock(ArrayList files)
	    throws IOException, SQLException {
	if (files.size() > 0) {
	   

	    System.out.println("LAOSEIS");

	    if (driver instanceof ClientBlock) {
		for (int i = 0; i < files.size(); i++) {
		    HashMap hm = new HashMap();

		    System.out.println("client stock file size "
				       + files.size() + " " + i);

		    String	   fileName = (String) files.get(i);

		    BufferedReader br =
			new BufferedReader(new FileReader(fileName));
		    String	   line = null;
		    String	   dealerID = null;
		    ArrayList      prods = new ArrayList();

		    while ((line = br.readLine()) != null) {
		    	 System.out.println("line " +  line);
			StringTokenizer st = new StringTokenizer(line, ";");

			if (st.hasMoreTokens() && line.trim().length()>0) {
			    dealerID = st.nextToken();



			    if (st.hasMoreTokens()) {
                              String prodID = st.nextToken();
                              String prodID2 = null;
                              if("Russia".equals(props.getProperty("language"))){
                                prodID2 = new String(prodID.getBytes(), "Cp1251");
                              System.out.println(prodID+prodID2);
                              }
				Product p = new Product(prodID);



				if (st.hasMoreTokens()) {
				    p.setStock(Double.parseDouble(st.nextToken()));
				}

				prods.add(p);
				 if(dealerID.length() < 3 && dealerID.trim().length() >0 && prods.size()>1) {
				       hm.put(dealerID, prods);
				    }
			    }
			   
			}
		    }
                    System.out.println("DEaler " + dealerID);
		    br.close();
		   
		    
		    System.out.println("hm " + hm.toString());
		    
		    if (hm.size() > 0) {
			((StockSetter) driver).setStocks(hm);
		    }
		}
	    }
	}
    }

    
    private void updateEmailLog(ArrayList files)
    throws IOException, SQLException {
if (files.size() > 0) {
   
    System.out.println("EMAILLOG");

    if (driver instanceof EmailLogger) {
	for (int i = 0; i < files.size(); i++) {
	    System.out.println("email file size "
			       + files.size() + " " + i);

	    String	   fileName = (String) files.get(i);
// 	getConc().sendEmailLogMess(cal.getTime().getTime() + ";" + getConc().getAgent() + ";" + ordnr + ";" +
		//	(String)cust.getID() + ";" + lines);
	    BufferedReader br =
		new BufferedReader(new FileReader(fileName));
	    String  line = null;
	    while ((line = br.readLine()) != null) {
			((EmailLogger) driver).setEmailLog(line);

	    }

	    }
	}
    }
}

    
    private TreeMap loadPH() {
		TreeMap res = new TreeMap();
		
		try {
			FileReader reader = new FileReader("photogroups.txt");
			BufferedReader br = new BufferedReader(reader);
			String line;
		
			while ( (line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, ";");
				if (st.hasMoreTokens()) {
					String modeID = st.nextToken();
					if (st.hasMoreTokens()) {
						String modeName = st.nextToken();
						
						if (st.hasMoreTokens()) {
							String type = st.nextToken();
							res.put(modeID, modeName);
						}
					}
					
				}
			}
			br.close();
			reader.close();
		}
		catch (Exception e) {
		}
		
		
		
		try {
			FileReader reader = new FileReader("photogroups2.txt");
			BufferedReader br = new BufferedReader(reader);
			String line;
		
			while ( (line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, ";");
				if (st.hasMoreTokens()) {
					String modeID = st.nextToken();
					if (st.hasMoreTokens()) {
						String modeName = st.nextToken();
						
						if (st.hasMoreTokens()) {
							String type = st.nextToken();
							res.put(modeID, modeName);
						}
					}
					
				}
			}
			br.close();
			reader.close();
		}
		catch (Exception e) {
		}
		
		return res;
	}
	
    
    /**
     * Method declaration
     *
     *
     * @param subjectMatch
     *
     * @return
     *
     * @throws MessagingException
     *
     * @see
     */
    
    
    
    
    
    private POP3Reader getPop3(String subjectMatch,int otherDir)
	    throws MessagingException {
	POP3Reader pop3 = new POP3Reader(subjectMatch);
	if("true".equals(props.getProperty("report.usemax"))){
		pop3.useMax =  true;
	}
	
	Properties props = EksportFrame.loadProps(EksportFrame.INI_FILE);


	pop3.setPop3host(props.getProperty("report.host"));
	pop3.setUser(props.getProperty("report.user"));
	pop3.setPassword(props.getProperty("report.password"));
	pop3.setSaveDir(props.getProperty("report.dir"));
	if(otherDir != 0){
		if(otherDir == 1 && props.getProperty("picture.dir") != null){
			pop3.setSaveDir(props.getProperty("picture.dir"));
			pop3.setSaveDir2(props.getProperty("report.dir"));
			System.out.println("Panin kataloogid paika ");
		}
	}
	if(props.getProperty("mail.protocol") != null){

	    pop3.setPop3proto(props.getProperty("mail.protocol"));	
	}
        if(props.getProperty("readin") != null){
          pop3.setDataDir(props.getProperty("readin"));
        }


	return pop3;
    }

    /**
     * Method declaration
     *
     *
     * @param files
     *
     * @throws IOException
     * @throws SQLException
     *
     * @see
     */
    private void parseFiles(ArrayList files)
	    throws IOException, SQLException {
	Iterator iter = files.iterator();

	while (iter.hasNext()) {
	    String filename = (String) iter.next();
	    File   f = new File(filename);
	    char[] content = new char[(int) f.length()];
	    Reader reader = new FileReader(f);
	    int    offset = 0;

	    while (offset != content.length) {
		offset += reader.read(content, offset, content.length);
	    }

	    reader.close();

	    StringTokenizer lineTokens =
		new StringTokenizer(new String(content), "\n");
	    String	    line;

	    while (lineTokens.hasMoreTokens()) {
		line = lineTokens.nextToken();
              System.out.println(line);
              line = line.replaceAll(";;", "; ;");
		StringTokenizer st = new StringTokenizer(line, ";");
        System.out.println(line);
		if (st.hasMoreTokens()) {
		    ReportLine rline = new ReportLine();

		    rline.setAgentCode(st.nextToken());
		    if(!rline.getAgentCode().equals("xxxx") && !rline.getAgentCode().equals("yyyy") && !rline.getAgentCode().equals("zzzz")){
		    

		    if (st.hasMoreTokens()) {
		    	String token = st.nextToken();
		    	if(token.trim().length()>0){
		    		try{
		    			rline.setTimeSent(new java.util.Date(Long.parseLong(token)));
		    		

			if (st.hasMoreTokens()) {
			    rline.setClientName(st.nextToken());

			    if (st.hasMoreTokens()) {
				rline.setClientCode(st.nextToken());

				if (st.hasMoreTokens()) {
				    rline.setEndTime(new java.util.Date(Long.parseLong(st.nextToken())));

				    if (st.hasMoreTokens()) {
					rline.setStartTime(new java.util.Date(Long.parseLong(st.nextToken())));

					if (st.hasMoreTokens()) {
						try{
							String aaa = st.nextToken();
							//System.out.println(aaa);
							aaa= aaa.replaceAll(",",".");
							aaa= aaa.replaceAll(" ","");
							if(aaa.indexOf(".")>-1){
							//	aaa = aaa.substring(0,aaa.indexOf("."));
							}
						//	System.out.println(aaa);
							rline.setLineSum(Double.parseDouble(aaa));

						}catch(Exception e){
							e.printStackTrace();
							rline.setLineSum(0);

						}
		   if (st.hasMoreTokens()) {
						rline.setLineCount(Integer.parseInt(st.nextToken()));

						if (st.hasMoreTokens()) {
						    rline.setText(st.nextToken());
		
						    if (st.hasMoreTokens()) {
							rline.setAgentName(st.nextToken());

							if (st.hasMoreTokens()) {
							    String val =
								st.nextToken();

							    if (val.equals("1")) {
								rline.setPlannedVisit(true);
							    } else {
								rline.setPlannedVisit(false);
							    }


								if (st.hasMoreTokens()) {
								rline.setNewProdSold(Integer.parseInt(st.nextToken()));
								}

								if (st.hasMoreTokens()) {
									rline.setDaystart(Long.parseLong(st.nextToken()));
								}

								if (st.hasMoreTokens()) {
									rline.setDayend(Long.parseLong(st.nextToken()));
								}

								if (st.hasMoreTokens()) {
									rline.setSpidostart(Integer.parseInt(st.nextToken()));
								}
								if (st.hasMoreTokens()) {
									rline.setSpidoend(Integer.parseInt(st.nextToken()));
								}
                                 if (st.hasMoreTokens()) {
                                                  String red = st.nextToken();
                                                  if(red.equals("{}")){
                                                  	red  = "0";
                                                  }
									rline.setRedlines(Integer.parseInt(red));
								}
                                 if(st.hasMoreTokens()){
                     		    	 token = st.nextToken().trim();
                     		    	System.out.println("token longitude" + token);
                     		    	if(token.trim().length()>0){
                     		    		rline.setLongitude(Double.parseDouble(token));
                     		    	  }	
                     		    	}
                     			if(st.hasMoreTokens()){
                     		    	 token = st.nextToken().trim();
                     		    //	System.out.println("token lattitude" + token);
                     		    	if(token.trim().length()>0){
                     		    		rline.setLatitude(Double.parseDouble(token));
                     		    	  }	
                     		    	}
                     			if(st.hasMoreTokens()){
                    		    	 token = st.nextToken().trim();
                    		    //	System.out.println("Visittype" + token);
                    		    	if(token.trim().length()>0){
                    		    		rline.setContactType(token);
                    		    	  }	
                    		    	}
							}
						    }
						}
					    }
					}
				    }
				}
			    }
			}

		 System.out.println("XXXXXXX2");
			if(toDB)
			insert2DB(rline);

			if( "true".equals(props.getProperty("report.alternative"))){
				if(toDB)
				insert2ALTDB(null,rline);
			//	 System.out.println("XXXXXXX1");
			}
			
}catch(Exception e){
		    		e.printStackTrace();
		    		log.error(e);
		    		}
			
		    }
		    }
		} else if(rline.getAgentCode().equals("xxxx") ){
			ReportVisitLine rl = new ReportVisitLine();
			if(st.hasMoreTokens()){

		    	String token = st.nextToken();
		    	if(token.trim().length()>0){
		    		rl.setAgentCode(token);
		    	}
		    	}
			
			
			
			if(st.hasMoreTokens()){
		    	String token = st.nextToken();
		    	if(token.trim().length()>0){
		    		rl.setAgentName(token);
		    	  }
		    	}
			
			if(st.hasMoreTokens()){
		    	String token = st.nextToken();
		    	if(token.trim().length()>0){
		    	rl.setClientCode(token);	
		    	  }
		    	}
			
			if(st.hasMoreTokens()){
		    	String token = st.nextToken();
		    	if(token.trim().length()>0){
		    		rl.setClientName(token);
		    	  }	
		    	}
			if(st.hasMoreTokens()){
		    	String token = st.nextToken();
		    	if(token.trim().length()>0){
		    			rl.setStatus(token.trim());
				    }	
		    	}
			
			if(st.hasMoreTokens()){
		    	String token = st.nextToken();
		    	if(token.trim().length()>0){
		    		rl.setDaylong(Long.parseLong(token));
		    	  }	
		    	}	
			try{
				if(st.hasMoreTokens()){
			    	String token = st.nextToken();
			    	if(token.trim().length()>0){
			    		rl.setPlannedVisits(Integer.parseInt(token));
			    	  }	
			    	}
			
			if(st.hasMoreTokens()){
		    	String token = st.nextToken();
		    	if(token.trim().length()>0){
		    		rl.setDoneVisits(Integer.parseInt(token));
		    	  }	
		    	}
			
			
			if(st.hasMoreTokens()){
		    	String token = st.nextToken();
		    	if(token.trim().length()>0){
		    		rl.setExtraVisits(Integer.parseInt(token.trim()));
		    	  }	
		    	}
			if(toDB)
			insertVisitLines(rl);
			
				
			}catch(Exception e){
				
			}
			
			/*"xxxx" + sep);
		    reportData.append(conc.getAgentName()+ sep +conc.agent + sep + cust.getID() + sep 
		    		+ cust.getName()+ sep + kaidud + sep + lastCalen.getTime().getTime() + sep 
					+visitplanned.size() + sep+ visitedCustomers.size()+ sep + todaysExtra.size() + lineSep*/
		} else if(rline.getAgentCode().equals("yyyy") ){
			ReportWorkDayLine lin = new ReportWorkDayLine();
			
			if(st.hasMoreTokens()){

		    	String token = st.nextToken();
		    	if(token.trim().length()>0){
		    		lin.setAgentCode(token);
		    	}
		    	}
			
			
			
			if(st.hasMoreTokens()){
		    	String token = st.nextToken();
		    	if(token.trim().length()>0){
		    		lin.setAgentName(token);
		    	  }
		    	}
			
			if(st.hasMoreTokens()){
		    	String token = st.nextToken();
		    	if(token.trim().length()>0){
		    		lin.setDaylong(getLong(token));
		    		lin.setDaystarttime(getTime(lin.getDaylong()+""));
		    	  }	
		    	}	
			
			if(st.hasMoreTokens()){
		    	String token = st.nextToken();
		    	if(token.trim().length()>0){ 		
		    		lin.setDayendtime(getTime(token));
		    	  }	
		    	}	
			
			
		
			if(toDB)
			insertWorkDayLines(lin);
		} else if(rline.getAgentCode().equals("zzzz") ){
			ReportVisitDurationLine lin = new ReportVisitDurationLine();
			
			if(st.hasMoreTokens()){

		    	String token = st.nextToken();
		    	if(token.trim().length()>0){
		    		lin.setAgentName(token);
		    	}
		    	}
			/*(conc.getAgentName() + sep + conc.agent + sep + cust.getID() + sep 
                               		+ cust.getName()+ sep + visittype + sep +  startDate.getTime() + lineSep);*/
			
			
			if(st.hasMoreTokens()){
		    	String token = st.nextToken();
		    	if(token.trim().length()>0){
		    		lin.setAgentCode(token);
		    	  }
		    	}
			
			

			if(st.hasMoreTokens()){
		    	String token = st.nextToken();
		    	if(token.trim().length()>0){
		    		lin.setCustid(token);
		    	  }
		    	}

			if(st.hasMoreTokens()){
		    	String token = st.nextToken();
		    	if(token.trim().length()>0){
		    		lin.setCustname(token);
		    	  }
		    	}
			
			if(st.hasMoreTokens()){
		    	String token = st.nextToken();
		    	if(token.trim().length()>0){
		    		lin.setVisitType(token);
		    	  }
		    	}
			if(st.hasMoreTokens()){
		    	String token = st.nextToken();
		    	if(token.trim().length()>0){
		    		try{
		    			lin.setDuration(Long.parseLong(token));
		    		}catch(Exception e){
		    			
		    		}
		    		
		    		
		    	  }	
		    	}	
			
			if(st.hasMoreTokens()){
		    	String token = st.nextToken().trim();
		    	System.out.println("token" + token);
		    	if(token.trim().length()>0){
		    		try{
		    		lin.setMadeatday(Long.parseLong(token));
}catch(Exception e){
		    			
		    		}
		    	  }	
		    	}
			
			if(st.hasMoreTokens()){
		    	String token = st.nextToken().trim();
		    	System.out.println("token longitude" + token);
		    	if(token.trim().length()>0){
		    		try{
		    		
		    		
		    		lin.setLongitude(Double.parseDouble(token));}catch(Exception e){
		    			
		    		}
		    	  }	
		    	}
			if(st.hasMoreTokens()){
		    	String token = st.nextToken().trim();
		    	System.out.println("token lattitude" + token);
		    	if(token.trim().length()>0){
		    		try{
		    		lin.setLattitude(Double.parseDouble(token));
		    		}catch(Exception e){
		    			
		    		}
		    	  }	
		    	}
			try{
				if(toDB)
				insertVisitDurationLines(lin);	
			}catch(Exception e){
				
			}
		
		}
	    }
		
	    }
	}
    }
    
    
    private String  getTime(String time){
       	String res = "";
       	if(time.trim().length()<=14){
       		Calendar dd = Calendar.getInstance();
       		dd.setTimeInMillis(Long.parseLong(time.trim()));
       		String min = dd.get(Calendar.MINUTE)+ "";
     		String hou = dd.get(Calendar.HOUR_OF_DAY)+ "";
     		String sec = dd.get(Calendar.SECOND)+ "";
     		if(hou.length()==1){
     			hou = "0" + hou;
     		}if(min.length()==1){
     			min = "0" + min;
     		}if(sec.length()==1){
     			sec = "0" + sec;
     		}
     		res = hou + ":"+ min +":" + sec;
       	}else{
       		//Thu Nov 29 15:52:21 GMT+02:00 2007
       		try{
       			res = time.substring(11,20).trim();		
       		}catch(Exception e){
       			
       		}
       	
       	}
       	
       	return res;
       }
    private long  getLong(String time){
       	long res = 0;
       	if(time.length()==13){
       		
     		res = Long.parseLong(time);
       	}else{
       		Calendar dd = Calendar.getInstance();
       		try{
       	        int year = Integer.parseInt((time.substring(time.length()-5,time.length()).trim()));
         	    dd.set(Calendar.YEAR,year);
         	    dd.set(Calendar.DAY_OF_MONTH,Integer.parseInt(time.substring(8,10).trim()));

         	    dd.set(Calendar.MONTH,getMon(time.substring(4,7).trim()));
           		res = dd.getTime().getTime();
       		}catch(Exception e){
       			
       		}
   
       	}
       	
       	return res;
       }
        
    
   private int  getMon(String time){
   	int res = 0;
   	if(time.equals("Feb")){
        res = 1;
   	}
   	if(time.equals("Mar")){
        res = 2;
   	}
   	if(time.equals("Apr")){
        res = 3;
   	}
   	if(time.equals("May")){
        res = 4;
   	}
   	if(time.equals("Jun")){
        res = 5;
   	}
   	if(time.equals("Jul")){
        res = 6;
   	}
   	if(time.equals("Aug")){
        res = 7;
   	}
   	if(time.equals("Sep")){
        res = 8;
   	}
   	if(time.equals("Oct")){
        res = 9;
   	}
   	if(time.equals("Nov")){
        res = 10;
   	}
   	if(time.equals("Dec")){
        res = 11;
   	}
   	return res;
   }
    
    private void insertVisitLines(ReportVisitLine line)throws SQLException{
        String tableName = "visitlines";
        if(props.getProperty("visitlines.tablename") != null && !props.getProperty("visitlines.tablename").trim().equals("")){
        tableName = props.getProperty("visitlines.tablename");
        }

        

        boolean country = false;
        if(props.getProperty("rep.country") != null){
        	country = true;
        }
        
        
	String		  sql = "INSERT INTO " + tableName + " VALUES (?,?,?,?,'','',?,? "
				+ " ,?,?,?,0,?) ";
	if(country){
		  sql = "INSERT INTO " + tableName + " VALUES (?,?,?,?,'','',?,? "
			+ " ,?,?,?,0,?,'" + props.getProperty("rep.country") + "') ";
	}
	
	//	agentid    agentname  clid  clname prodid prodname   sentdate   todayok done  extra planned    res1  res2 
	Connection conn =  null;
	try{
		conn = DriverManager.getConnection(props.getProperty("report.dburl"), props.getProperty("dsn.user"),
				  props.getProperty("dsn.pass"));
	}catch(Exception e){
		conn = DriverManager.getConnection(props.getProperty("report.dburl"), props.getProperty("dsna.user"),
				  props.getProperty("dsna.pass"));
	}

	PreparedStatement st = conn.prepareStatement(sql);
   System.out.println(sql);
//   log.error("sql " + sql);
	st.setString(1, line.getAgentCode());
	st.setString(2, line.getAgentName());
	st.setString(3, line.getClientCode());
	st.setString(4, line.getClientName());

	if (sqlType.equals(SQL_TYPE_TIMESTAMP)) {
	    st.setTimestamp(5,new java.sql.Timestamp(line.getDaylong()));
	} else {
	    st.setDate(5, new java.sql.Date(line.getDaylong()));
	}
	if (line.isTodayOK()) {
	    st.setInt(6, 1);
	} else {
	    st.setInt(6, 0);
	}
	
	st.setDouble(7,line.getDoneVisits());
	st.setDouble(8,line.getExtraVisits());
	st.setDouble(9,line.getPlannedVisits());
	st.setString(10, line.getStatus());
	
	log.info("Aruande VISITLINE Rida Baasi " + line.getAgentName() );
	try{
		st.executeUpdate();
		st.close();
		conn.close();
	}catch(Exception e){
		log.error(e);
		e.printStackTrace();
		conn.close();
	}
	
	
    }
    

    
    private String getTimeString(long mils){
   
    int ee = 0;
    	Date dd = new Date(mils);
    	if(dd.getTimezoneOffset() != 0){
        	ee = dd.getHours() + dd.getTimezoneOffset()/60;

    	}
        String hour = ee + "";
        String minute= dd.getMinutes() + "";
        String sec= dd.getSeconds() + "";
    if(hour.length()==1){
    	hour= "0" + hour;
    }
    
    if(minute.length()==1){
    	minute= "0" + minute;
    }
    if(sec.length()==1){
    	sec= "0" + sec;
    }
        
        
    	return hour + ":" +minute + ":" + sec ;
    }
    
    private void insertVisitDurationLines(ReportVisitDurationLine line)throws SQLException{
        String tableName = "visitdura";
        if(props.getProperty("visitduration.tablename") != null && !props.getProperty("visitduration.tablename").trim().equals("")){
        tableName = props.getProperty("visitduration.tablename");
        }
        
        boolean location = false;
      
        
        if(props.getProperty("visitduration.location") != null){
        	location = true;
        }
      
        boolean country = false;
        if(props.getProperty("rep.country") != null){
        	country = true;
        }
        
	String		  sql = "INSERT INTO " + tableName + " VALUES (?,?,?,?,?,?,?,?) ";
	 if(country){
		 sql = "INSERT INTO " + tableName + " VALUES (?,?,?,?,?,?,?,?) "; 
	 }
	if(location){
		     sql = "INSERT INTO " + tableName + " VALUES (?,?,?,?,?,?,?,?,?,?) ";
		 if(country){
			 sql = "INSERT INTO " + tableName + " VALUES (?,?,?,?,?,?,?,?,?,?,?) "; 
		 }
	}
	//	agentid    agentname  clid  clname prodid prodname   sentdate   todayok done  extra planned    res1  res2 
	Connection conn =  null;
	try{
		conn = DriverManager.getConnection(props.getProperty("report.dburl"), props.getProperty("dsn.user"),
				  props.getProperty("dsn.pass"));
	}catch(Exception e){
		conn = DriverManager.getConnection(props.getProperty("report.dburl"), props.getProperty("dsna.user"),
				  props.getProperty("dsna.pass"));
	}

	PreparedStatement st = conn.prepareStatement(sql);
   System.out.println(sql);
//   log.error("sql " + sql);
	st.setString(1, line.getAgentCode());
	st.setString(2, line.getAgentName());
	st.setString(3, line.getCustid());
	st.setString(4, line.getCustname());

	
	    st.setString(5, getTimeString(line.getDuration()));
	
	if (sqlType.equals(SQL_TYPE_TIMESTAMP)) {
	    st.setTimestamp(6,new java.sql.Timestamp(line.getMadeatday()));
	} else {
	    st.setDate(6, new java.sql.Date(line.getMadeatday()));
	}
	st.setString(7, line.getVisitType());
  
	st.setString(8, getTime(line.getMadeatday()+""));
	if(location){
		st.setDouble(9,line.getLongitude());
		st.setDouble(10, line.getLattitude());
		
			if(country){
				st.setString(11, props.getProperty("rep.country"));
			}
		
	}else if(country){
		st.setString(9, props.getProperty("rep.country"));
	}
	
	log.info("Aruande VISITDURATION Rida Baasi " + line.getAgentName() );
	
	try{
		st.executeUpdate();
		st.close();
		conn.close();
	}catch(Exception e){
		e.printStackTrace();
		log.error(e);
conn.close();
	}
	
    }
    
    
    
    private void insertWorkDayLines(ReportWorkDayLine line)throws SQLException{
        String tableName = "mjworktime";
        if(props.getProperty("worktime.tablename") != null && !props.getProperty("worktime.tablename").trim().equals("")){
        tableName = props.getProperty("worktime.tablename");
        }
        
   
        boolean country = false;
        if(props.getProperty("rep.country") != null){
        	country = true;
        }

	String		  sql = "INSERT INTO " + tableName + " VALUES (?,?,?,?,?,0,0,'')";
	if(country){
		 sql = "INSERT INTO " + tableName + " VALUES (?,?,?,?,?,0,0,'','" +props.getProperty("rep.country") + "')";
	}
	//	agentid    agentname  clid  clname prodid prodname   sentdate   todayok done  extra planned    res1  res2 
	Connection conn =  null;
	try{
		conn = DriverManager.getConnection(props.getProperty("report.dburl"), props.getProperty("dsn.user"),
				  props.getProperty("dsn.pass"));
	}catch(Exception e){
		try {
			conn = DriverManager.getConnection(props.getProperty("report.dburl"), props.getProperty("dsna.user"),
					  props.getProperty("dsna.pass"));		
			
		}catch(Exception r) {
		}
	
	}

	if(conn !=null) {
	
	PreparedStatement st = conn.prepareStatement(sql);
   System.out.println(sql);
//   log.error("sql " + sql);
	st.setString(1, line.getAgentCode());
	st.setString(2, line.getAgentName());

	if (sqlType.equals(SQL_TYPE_TIMESTAMP)) {
	    st.setTimestamp(3,new java.sql.Timestamp(line.getDaylong()));
	} else {
	    st.setDate(3, new java.sql.Date(line.getDaylong()));
	}
	st.setString(4, line.getDaystarttime());
	st.setString(5, line.getDayendtime());


	
	
	log.info("Aruande MJworktime Rida Baasi " + line.getAgentName()  +  " " + line.getDaylong());
	
	try{
		st.executeUpdate();
		st.close();
		conn.close();
	}catch(Exception e){
		e.printStackTrace();
		log.error(e);
		conn.close();
	}
	}
    }
    
    
    
    private void insert2ALTDB(ReportVisitLine rl,ReportLine rline) throws SQLException {
       System.out.println("Alternative sees ");
       
        if (driver instanceof OERPRubinetaDBDriver){
            ((OERPRubinetaDBDriver)driver).setAlternativeReport(rl,rline);
            System.out.println("Alternative sees  ");
    	     
        }
        
    	  	  
    	
    	    }


    /**
     * Method declaration
     *
     *
     * @param line
     *
     * @throws SQLException
     *
     * @see
     */
    private void insert2DB(ReportLine line) throws SQLException {

        String tableName = "dayreport";
       
        if(props.getProperty("report.tablename") != null && !props.getProperty("report.tablename").trim().equals("")){
        tableName = props.getProperty("report.tablename");
        }
        
  boolean location = false;
        
        if(props.getProperty("visitduration.location") != null){
        	location = true;
        }
        if("false".equals(props.getProperty("dayreport.location"))){
        	location = false;
        }
        
        
        boolean country = false;
        if(props.getProperty("rep.country") != null){
        	country = true;
        }
	String		  sql = "INSERT INTO " + tableName + " VALUES (?,?,?,?,?,? "
				+ " ,?,?,?,?,?,?,?,?,?,?,?) ";
	 if(country){
		         sql = "INSERT INTO " + tableName + " VALUES (?,?,?,?,?,? "
			    + " ,?,?,?,?,?,?,?,?,?,?,?,?) ";
       }
	if(location){
		         sql = "INSERT INTO " + tableName + " VALUES (?,?,?,?,?,? "
			    + " ,?,?,?,?, ?,?,?,?,? ,?,?,?,?) ";
		         
		         if(country){
		        	    sql = "INSERT INTO " + tableName + " VALUES (?,?,?,?,?,? "
					    + " ,?,?,?,? ,?,?,?,?,?, ?,?,?,?,?) ";
			      }
	}
	
	 if(props.getProperty("report.convtodate") != null && !props.getProperty("report.convtodate").trim().equals("")){
		  sql = "INSERT INTO " + tableName + " VALUES (?,cast(? as date),?,?,cast(? as date),cast(? as date) "
			+ " ,?,?,?,?,?,?,?,?,?,?,?) ";	 
	 }
	
	Connection conn =  null;
	try{
		conn = DriverManager.getConnection(props.getProperty("report.dburl"), props.getProperty("dsn.user"),
				  props.getProperty("dsn.pass"));
	}catch(Exception e){
		log.error(e);
		conn = DriverManager.getConnection(props.getProperty("report.dburl"), props.getProperty("dsna.user"),
				  props.getProperty("dsna.pass"));
	}
	
   String nvar = "";
   if("true".equals(props.getProperty("report.usen"))){
       nvar = "N";
       }
   
	PreparedStatement st = conn.prepareStatement(sql);
   System.out.println(sql);
    log.error("sql " + sql);
	st.setString(1, line.getAgentCode());

	if (sqlType.equals(SQL_TYPE_TIMESTAMP)) {
	    st.setTimestamp(2,
			    new java.sql.Timestamp(line.getTimeSent().getTime()));
	    st.setTimestamp(5,
			    new java.sql.Timestamp(line.getEndTime().getTime()));
	    st.setTimestamp(6,
			    new java.sql.Timestamp(line.getStartTime().getTime()));
	} else {
	    st.setDate(2, new java.sql.Date(line.getTimeSent().getTime()));
	    st.setDate(5, new java.sql.Date(line.getEndTime().getTime()));
	    st.setDate(6, new java.sql.Date(line.getStartTime().getTime()));
	}

	st.setString(3, nvar +line.getClientCode());
	st.setString(4, nvar +Converter.deCode(line.getClientName()));
	st.setDouble(7, line.getLineSum());
	st.setInt(8, line.getLineCount());
	String text = line.getText();
	if(text.length()>200){
		text = text.substring(0,199);
	}
	st.setString(9,nvar + Converter.deCode(text));
	String agname = line.getAgentName();
	if(agname.length()>50){
		agname = agname.substring(0,49);
	}
	st.setString(10, nvar +Converter.deCode(agname));
	String dealer = line.getDealerCode();
	
	if (dealer == null){
		dealer = " ";
	}
	
	st.setString(11,dealer );

	Date daystart = new Date(line.getDaystart());
	Date dayend = line.getTimeSent();
	st.setString(12, ensLen(daystart.getHours()) + ":" + ensLen(daystart.getMinutes()) + ":" + ensLen(daystart.getSeconds()));
	st.setString(13, ensLen(dayend.getHours()) + ":" + ensLen(dayend.getMinutes()) + ":" + ensLen(dayend.getSeconds()));
	
	String timeStr =  ensLen(line.getStartTime().getHours()) + ":" + ensLen(line.getStartTime().getMinutes()) + ":" + ensLen(line.getStartTime().getSeconds());
	st.setString(14, timeStr);

	if (line.isPlannedVisit()) {
	    st.setInt(15, 1);
	} else {
	    st.setInt(15, 0);
	}

	st.setInt(16, line.getNewProdSold());
	String red = line.getRedlines() + "";
	st.setString(17 , red);
	if(location){
		st.setDouble(18,line.getLongitude());
		st.setDouble(19, line.getLatitude());
		System.out.println("Location xxxx");
		if(country){
			st.setString(20, props.getProperty("rep.country"));
			System.out.println("Country xxxx");
		}
	}else if(country){
		st.setString(18, props.getProperty("rep.country"));
		System.out.println("Country yyyy");
	}
	

	try {

		st.executeUpdate();
		st.close();
		conn.close();
	}catch(Exception e) {

		st.close();
		conn.close();
	}	

    }




    private String ensLen(int s){
	  String tt = s + "";
	    if((s+"").length()== 1){
		    tt = "0" + s;
	      }
	  return tt;
   }

}




/*--- formatting done in "Sun Java Convention" style on 11-13-2003 ---*/

