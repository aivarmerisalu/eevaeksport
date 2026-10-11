
package eevaeksport.imp;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.log4j.Category;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.pop3.POP3Store;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.MailSSLSocketFactory;

import eevaeksport.Customer;
import eevaeksport.EksportFrame;
import eevaeksport.Email;
import eevaeksport.driver.AbstractDBDriver;
import eevaeksport.driver.AlitaLTDBDriver;
import eevaeksport.driver.SkaiteAGDBDriver;
import eevaeksport.util.Converter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class POP3Reader {

  public POP3Reader(String subject) throws MessagingException{
    this.subject = subject;
    
    try{
    	this.phSel = this.loadPH();
    }catch(Exception e){
    	e.printStackTrace();
    }
         fr = new EksportFrame(new String[]{});
	 driver = fr.getEksportPanel().getDriver();
  }
  
  static Category log = Category.getRoot();
  
  public ArrayList readAttFiles() throws MessagingException, IOException{
    ArrayList bodyFiles = new ArrayList();
    Properties props = new Properties();
    props.put("mail.host", this.pop3host);
    props.put("mail.protocol", this.pop3proto);
    
    
    
    urlName = new URLName(this.pop3proto, this.pop3host, PORT,
            "INBOX", user, password);
    
    
    String proto = props.getProperty("mail.protocol");
    if (proto != null){
      PROTOCOL = proto.trim().toLowerCase();
      if (proto.equals("pop3s")){
        PORT = 995;
      }
    }
    Session ses = Session.getInstance(props);
    Store store = null;
    
    if(PROTOCOL.equals("pop3s")){


    	MailSSLSocketFactory sf = null;
    	try {
    		sf = new MailSSLSocketFactory();
    		sf.setTrustAllHosts(true);
    	} catch (GeneralSecurityException e1) {
    		// TODO Auto-generated catch block
    	
    		e1.printStackTrace();
    	}
        	
                   Properties prop = System.getProperties();
                  // prop.put("mail.pop3.ssl.socketFactory", sf);
                   prop.setProperty( "mail.pop3s.socketFactory.class", "eevaeksport.mailagent.AcceptAllCerts");//javax.net.ssl.SSLSocketFactory
                   prop.setProperty( "mail.pop3s.socketFactory.fallback", "false");
                   prop.setProperty("mail.pop3s.port", PORT+"");
                   prop.setProperty("mail.pop3s.socketFactory.port", PORT+"");
                   Session session = Session.getInstance(prop);
                   store = session.getStore("pop3s");
            
               
               // session.setDebug(true);
               // Get a Store object
               // Connect
               
               store.connect(urlName.getHost(), urlName.getUsername(), urlName.getPassword());
               
        	
    }else if (PROTOCOL.equals("imap")){
    	store = new IMAPStore(ses,  new URLName("imap", props.getProperty("mail.host"), 143,
 	           "INBOX", this.user, this.password));
    	 store.connect();
    }else if (PROTOCOL.equals("imaps")){
    		MailSSLSocketFactory socketFactory = null;
    	try {
    		socketFactory = new MailSSLSocketFactory();
    	} catch (GeneralSecurityException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
        socketFactory.setTrustAllHosts(true);
       
    	
        Properties prop = System.getProperties();
        prop.setProperty( "mail.imap.socketFactory.class", "eevaeksport.mailagent.AcceptAllCerts");//javax.net.ssl.SSLSocketFactory
        prop.setProperty( "mail.imap.socketFactory.fallback", "false");
        Session session = Session.getInstance(prop);
        store = session.getStore("imaps");
    store.connect(urlName.getHost(), urlName.getUsername(), urlName.getPassword());

    }else if (PROTOCOL.equals("imapssl")){
    		MailSSLSocketFactory socketFactory = null;
    	try {
    		socketFactory = new MailSSLSocketFactory();
    	} catch (GeneralSecurityException e) {
    		e.printStackTrace();
    	}
        socketFactory.setTrustAllHosts(true);
         Properties prop = System.getProperties();
        prop.setProperty( "mail.imaps.socketFactory.class", "eevaeksport.mailagent.AcceptAllCerts");//javax.net.ssl.SSLSocketFactory
        prop.setProperty( "mail.imaps.socketFactory.fallback", "false");
        prop.setProperty( "mail.imap.socketFactory.class", "eevaeksport.mailagent.AcceptAllCerts");//javax.net.ssl.SSLSocketFactory
        prop.setProperty( "mail.imap.socketFactory.fallback", "false");
       Session session = Session.getInstance(prop);
        store = session.getStore("imaps");
    store.connect(urlName.getHost(), urlName.getUsername(), urlName.getPassword());

    } else {
    	
        store = new POP3Store(ses, new URLName("pop3", props.getProperty("mail.host"), PORT,
           "INBOX", this.user, this.password));
        store.connect();
   		
    }
    
    
    
	   Folder folder;
	    if (PROTOCOL.equals("imap") || PROTOCOL.equals("imaps") || PROTOCOL.equals("imapssl")){
	      folder = (IMAPFolder)store.getFolder("INBOX");
	    }	else {
	      folder = (POP3Folder)store.getFolder("INBOX");
	    }
	    	
	    
    folder.open(Folder.READ_WRITE);
    Message[] msgs = folder.getMessages();
    
    int max = msgs.length;
    if(this.useMax) {
    	max = 200;
    }
    
    if(msgs.length < 200) {
    	max = msgs.length;
    }
    
    for (int i = 0 ; i < max ; i++){
      Message msg = msgs[i];
      if (msg.getSubject() != null && msg.getSubject().trim().indexOf(this.subject) != -1){
        Object o = msg.getContent();
        System.out.println("class instance" + o.getClass().toString());
        if (o instanceof MimeMultipart){
          MimeMultipart mp = (MimeMultipart)o;
          String fileName = System.currentTimeMillis() + ".tmp";
          for (int j = mp.getCount(); j > 0 ; j--){
            MimeBodyPart mbp = (MimeBodyPart)mp.getBodyPart(j-1);
            boolean isBody = false;
            if (mbp.getFileName() != null){
              isBody = false;
              fileName = addTimeStamp(mbp.getFileName());
            } else {
              isBody = true;
            }

            InputStream is = mbp.getInputStream();

            if (!isBody){
              File bodyDir = new File(saveDir + ImportPanel.DIR_BODY + File.separator);
                          if (!bodyDir.exists()){
                            bodyDir.mkdir();
                          }
                          writeToFile(is, saveDir + ImportPanel.DIR_BODY + File.separator + fileName);
                          bodyFiles.add(saveDir + ImportPanel.DIR_BODY + File.separator + fileName);

            }
            is.close();

          }
        } else {
          String fn = System.currentTimeMillis() + ".tmp";
          writeToFile((String)o, saveDir + ImportPanel.DIR_BODY + File.separator + fn);
          bodyFiles.add(saveDir + ImportPanel.DIR_BODY + File.separator + fn);
        }
        msg.setFlag(Flags.Flag.DELETED, true);
      }
    }
    folder.close(true);
    store.close();
    return bodyFiles;
  }

  private TreeMap phSel = new TreeMap();

  public ArrayList readFiles(boolean delete) throws MessagingException, IOException{
    ArrayList bodyFiles = new ArrayList();
    Properties props = new Properties();
    props.put("mail.host", this.pop3host);
    props.put("mail.protocol", this.pop3proto);
    
    
    urlName = new URLName(this.pop3proto, this.pop3host, PORT,
            "INBOX", user, password);
    
    
    String proto = props.getProperty("mail.protocol");
    if (proto != null){
      PROTOCOL = proto.trim().toLowerCase();
      if (proto.equals("pop3s")){
        PORT = 995;
      }
    }
    Session ses = Session.getInstance(props);
    Store store = null;
    
    if(PROTOCOL.equals("pop3s")){


	MailSSLSocketFactory sf = null;
	try {
		sf = new MailSSLSocketFactory();
		sf.setTrustAllHosts(true);
	} catch (GeneralSecurityException e1) {
		// TODO Auto-generated catch block
	
		e1.printStackTrace();
	}
    	
               Properties prop = System.getProperties();
              // prop.put("mail.pop3.ssl.socketFactory", sf);
               prop.setProperty( "mail.pop3s.socketFactory.class", "eevaeksport.mailagent.AcceptAllCerts");//javax.net.ssl.SSLSocketFactory
               prop.setProperty( "mail.pop3s.socketFactory.fallback", "false");
               prop.setProperty("mail.pop3s.port", PORT+"");
               prop.setProperty("mail.pop3s.socketFactory.port", PORT+"");
               Session session = Session.getInstance(prop);
               store = session.getStore("pop3s");
        
           
           // session.setDebug(true);
           // Get a Store object
           // Connect
           
           store.connect(urlName.getHost(), urlName.getUsername(), urlName.getPassword());
           
    	
   	
   	
   	
    }else if (PROTOCOL.equals("imaps")  ){
   	
   	
   	MailSSLSocketFactory socketFactory = null;
	try {
		socketFactory = new MailSSLSocketFactory();
	} catch (GeneralSecurityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    socketFactory.setTrustAllHosts(true);
   
	
    Properties prop = System.getProperties();
  //  prop.put("mail.imaps.ssl.socketFactory", socketFactory);
    prop.setProperty( "mail.imap.socketFactory.class", "eevaeksport.mailagent.AcceptAllCerts");//javax.net.ssl.SSLSocketFactory
    prop.setProperty( "mail.imap.socketFactory.fallback", "false");
    Session session = Session.getInstance(prop);
    store = session.getStore("imaps");
store.connect(urlName.getHost(), urlName.getUsername(), urlName.getPassword());

   	
    }else if (PROTOCOL.equals("imapssl")  ){
       	
     
    
    MailSSLSocketFactory socketFactory = null;
	try {
		socketFactory = new MailSSLSocketFactory();
	} catch (GeneralSecurityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    socketFactory.setTrustAllHosts(true);
   
	
    Properties prop = System.getProperties();
     prop.setProperty( "mail.imaps.socketFactory.class", "eevaeksport.mailagent.AcceptAllCerts");//javax.net.ssl.SSLSocketFactory
     prop.setProperty( "mail.imaps.socketFactory.fallback", "false");
     prop.setProperty( "mail.imap.socketFactory.class", "eevaeksport.mailagent.AcceptAllCerts");//javax.net.ssl.SSLSocketFactory
     prop.setProperty( "mail.imap.socketFactory.fallback", "false");
  //  prop.put("mail.imaps.ssl.checkserveridentity", "false");
  //  prop.put("mail.imap.ssl.trust", "*");
   // prop.put("mail.imaps.ssl.trust", "*");
    
    Session session = Session.getInstance(prop);
     
    store = session.getStore("imaps");
    store.connect(urlName.getHost(), urlName.getUsername(), urlName.getPassword());

    
    
    
    
    
    
        }else if (PROTOCOL.equals("imap")  ){
    	store = new IMAPStore(ses,  new URLName("imap", props.getProperty("mail.host"), 143,
  	           "INBOX", this.user, this.password));
    	 store.connect();
     }else {
    	
        store = new POP3Store(ses, new URLName("pop3", props.getProperty("mail.host"), PORT,
           "INBOX", this.user, this.password));
        store.connect();
   		
    }
    	   Folder folder;
    if (PROTOCOL.equals("imap") || PROTOCOL.equals("imaps") || PROTOCOL.equals("imapssl")){
      folder = (IMAPFolder)store.getFolder("INBOX");
    }	else {
      folder = (POP3Folder)store.getFolder("INBOX");
    }
    	
    folder.open(Folder.READ_WRITE);
    Message[] msgs = folder.getMessages();
    System.out.println( "Messages len " + msgs.length);
    
    int max = msgs.length;
    if(this.useMax) {
    	max = 200;
    }
    
    if(msgs.length < 200) {
    	max = msgs.length;
    }
    for (int i = 0 ; i < max ; i++){
      Message msg = msgs[i];
      try{
    	
    	

    	

    	
    	         // String sub = MimeUtility.decodeText(MimeUtility.unfold( msg.getHeader("Subject")));
    	
    	  
    	  
     System.out.println(msg.getSubject() + " "  + " " + (this.subject) + " " + msg.getHeader("Subject"));
      if (msg.getSubject() != null && msg.getSubject().indexOf(this.subject) != -1){
        Object o = msg.getContent();
        if (o instanceof MimeMultipart){
          MimeMultipart mp = (MimeMultipart)o;
          String fileName = System.currentTimeMillis() + ".tmp";
          try{
        	
          for (int j = mp.getCount(); j > 0 ; j--){
            MimeBodyPart mbp = (MimeBodyPart)mp.getBodyPart(j-1);
            boolean isBody = false;
            if (mbp.getFileName() != null){
              isBody = false;
              fileName = addTimeStamp(mbp.getFileName());
            } else {
              isBody = true;
            }

            InputStream is = mbp.getInputStream();

            if (!isBody){
            	if(isSaveAttach()){
            		try{
            			 writeToFile(is, saveDir + fileName);	
            		}catch(Exception e){
            			e.printStackTrace();
            			try{
                			log.info("Faili salvestamise viga Baasi 1 " + saveDir + fileName + " " + e);

                			if(saveDir2 != null){
                				 writeToFile(is, saveDir2 + fileName);
                			}            				
                			
            			}catch(Exception k){
            				log.info("Faili salvestamise viga Baasi 2 " + saveDir2 + fileName + " " + k);

            			}

            		
            		}
            			
            	}
             
              is.close();
              
            //  System.out.println("On Forward " + isForward() + " sub:" + subject);
              if(this.isForward()){
            	  File f = new File(saveDir + fileName);
            	  if(!f.exists()){
            		  f = new File(saveDir2 + fileName);
            	  }
            	  System.out.println("On olemas " + f.exists() + " " + f.getAbsolutePath());
                  
            	  //  siia teeme emaili sisu kirjutamise 
            	  String name = f.getName();
                  String custid = null;
                  String phgroup = null;
                  String agent = null;
                  String country = "";
                  String phchoice = null;
                  
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
                 Customer cc = getCustomer(custid);
                 String custname = "bad name";
                 if(cc != null){
                	 custname = cc.getName();
                	   
                 }
                 System.out.println("ag " + agent +" " + phgroup +  " " + custid + " " + custname+ " " + country + " ch" + phchoice + " "+f.getName() + " " +  (name.length() - name.replaceAll("\\.", "").length()) );
                 
                
                 String text = "Manager : " + agent + "\n";
                 text = text + "Customer Name: " + custname + "\n";
                 text = text + "Photogroup : " + phgroup + "\n";
                 if(phchoice != null && !phchoice.equals("0") && phSel.get(phchoice) != null){
                 text = text + "Selection : " + phSel.get(phchoice) + "\n";
                 }
                 if(linkText != null){
                	 text = text + "Picture Link : " + linkText + phgroup + 
                	 "/" + name + "\n";
                 }
                 
                 if(sendText != null){
                	 if(sendText.equals(phchoice)){
                		 this.setPictEmailBody(text);
                		 if(!forwardEmail( saveDir + fileName)){
                			 forwardEmail( saveDir2 + fileName);
                		 }
                		
                	 }
                	
                 }else{
                	 this.setPictEmailBody(text); 
                	 if(!forwardEmail( saveDir + fileName)){
            			 forwardEmail( saveDir2 + fileName);
            		 }
                 }
                 
                
             
                 
            	  
            	  
            	  
            	
              }
            } else {
              File bodyDir = new File(saveDir + ImportPanel.DIR_BODY + File.separator);
              if (!bodyDir.exists()){
                bodyDir.mkdir();
              }
              writeToFile(is, saveDir + ImportPanel.DIR_BODY + File.separator + fileName);
              bodyFiles.add(saveDir + ImportPanel.DIR_BODY + File.separator + fileName);
            }
            is.close();
            //System.out.println();
          }
          
          }catch(Exception e){
        	  e.printStackTrace();
          }
        } else {
          String fn = System.currentTimeMillis() + ".tmp";
          String filename = saveDir + ImportPanel.DIR_BODY + File.separator + fn;
          
          if(o instanceof BASE64DecoderStream){
        	  BASE64DecoderStream newObj = (BASE64DecoderStream) o;
             
              FileOutputStream fos = new FileOutputStream(filename);
              byte[] buffer = new byte[1024];

              while (0 < newObj.read(buffer)) {
                  fos.write(buffer);
              }

              newObj.close();
              fos.close();


          }else{
        	   writeToFile((String)o, filename); 
          }
              
              
       
          bodyFiles.add(filename);
        }
        if(delete){
        	  msg.setFlag(Flags.Flag.DELETED, true);  	
        }
        
      }

      }catch(Exception k){
    	  k.printStackTrace();
    	  folder.open(Folder.READ_WRITE);
      }
      }
      
    folder.close(true);
    store.close();
    return bodyFiles;
  }
  
  private String linkText = null;
  private String sendText = null;
  
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
		
		return res;
	}
	
	private  EksportFrame     fr = null;
	  AbstractDBDriver driver = null ;
  
	  private Customer getCustomer(String custid ){
  	Customer cc = null;
  	  
  	   if (driver instanceof SkaiteAGDBDriver){
  	       cc =  ((SkaiteAGDBDriver)driver).getCustomerSingle(custid);
  	       }
  	   
  	 if (driver instanceof AlitaLTDBDriver){
	       cc =  ((AlitaLTDBDriver)driver).getCustomerSingle(custid);
	       }
  	return cc;
  }
  
  
  private boolean forward = false;
  private String pictEmailBody = null;


  public boolean isForward() {
	return forward;
}
public void setForward(boolean forward) {
	this.forward = forward;
}
public void deleteFiles() throws MessagingException, IOException{
  	Properties props = new Properties();
    props.put("mail.host", this.pop3host);
    props.put("mail.protocol", this.pop3proto);
    
    
    urlName = new URLName(this.pop3proto, this.pop3host, PORT,
            "INBOX", user, password);
    
    
    String proto = props.getProperty("mail.protocol");
    if (proto != null){
      PROTOCOL = proto.trim().toLowerCase();
      if (proto.equals("pop3s")){
        PORT = 995;
      }
    }
    Session ses = Session.getInstance(props);
    Store store = null;
    
    if(PROTOCOL.equals("pop3s")){

    	MailSSLSocketFactory sf = null;
    	try {
    		sf = new MailSSLSocketFactory();
    		sf.setTrustAllHosts(true);
    	} catch (GeneralSecurityException e1) {
    		// TODO Auto-generated catch block
    	
    		e1.printStackTrace();
    	}
        	
                   Properties prop = System.getProperties();
                  // prop.put("mail.pop3.ssl.socketFactory", sf);
                   prop.setProperty( "mail.pop3s.socketFactory.class", "eevaeksport.mailagent.AcceptAllCerts");//javax.net.ssl.SSLSocketFactory
                   prop.setProperty( "mail.pop3s.socketFactory.fallback", "false");
                   prop.setProperty("mail.pop3s.port", PORT+"");
                   prop.setProperty("mail.pop3s.socketFactory.port", PORT+"");
                   Session session = Session.getInstance(prop);
                   store = session.getStore("pop3s");
            
               
               // session.setDebug(true);
               // Get a Store object
               // Connect
               
               store.connect(urlName.getHost(), urlName.getUsername(), urlName.getPassword());
               
        	
    }else if (PROTOCOL.equals("imap")){
    	store = new IMAPStore(ses,  new URLName("imap", props.getProperty("mail.host"), 143,
   	           "INBOX", this.user, this.password));
     	 store.connect();
    }else if (PROTOCOL.equals("imapssl")){
    		MailSSLSocketFactory socketFactory = null;
    	try {
    		socketFactory = new MailSSLSocketFactory();
    	} catch (GeneralSecurityException e) {
    		e.printStackTrace();
    	}
        socketFactory.setTrustAllHosts(true);
         Properties prop = System.getProperties();
        prop.setProperty( "mail.imaps.socketFactory.class", "eevaeksport.mailagent.AcceptAllCerts");//javax.net.ssl.SSLSocketFactory
        prop.setProperty( "mail.imaps.socketFactory.fallback", "false");
        prop.setProperty( "mail.imap.socketFactory.class", "eevaeksport.mailagent.AcceptAllCerts");//javax.net.ssl.SSLSocketFactory
        prop.setProperty( "mail.imap.socketFactory.fallback", "false");
       Session session = Session.getInstance(prop);
        store = session.getStore("imaps");
    store.connect(urlName.getHost(), urlName.getUsername(), urlName.getPassword());

    }else if (PROTOCOL.equals("imaps")){
    	
    	MailSSLSocketFactory socketFactory = null;
    	try {
    		socketFactory = new MailSSLSocketFactory();
    	} catch (GeneralSecurityException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
        socketFactory.setTrustAllHosts(true);
       
    	
        Properties prop = System.getProperties();
      //  prop.put("mail.imaps.ssl.socketFactory", socketFactory);
        prop.setProperty( "mail.imap.socketFactory.class", "eevaeksport.mailagent.AcceptAllCerts");//javax.net.ssl.SSLSocketFactory
        prop.setProperty( "mail.imap.socketFactory.fallback", "false");
        Session session = Session.getInstance(prop);
        store = session.getStore("imaps");


    // session.setDebug(true);
    // Get a Store object
    // Connect

    store.connect(urlName.getHost(), urlName.getUsername(), urlName.getPassword());
     }else {
    	
        store = new POP3Store(ses, new URLName("pop3", props.getProperty("mail.host"), PORT,
           "INBOX", this.user, this.password));
        store.connect();
   		
    }
	   Folder folder;
	    if (PROTOCOL.equals("imap") || PROTOCOL.equals("imaps")  || PROTOCOL.equals("imapssl")){
	      folder = (IMAPFolder)store.getFolder("INBOX");
	    }	else {
	      folder = (POP3Folder)store.getFolder("INBOX");
	    }
    folder.open(Folder.READ_WRITE);
    Message[] msgs = folder.getMessages();
    
    int max = msgs.length;
    if(this.useMax) {
    	max = 200;
    }
    
    if(msgs.length < 200) {
    	max = msgs.length;
    }
    
    for (int i = 0 ; i < max ; i++){
      Message msg = msgs[i];
      if (msg.getSubject() != null && 
    		  this.subject != null &&
    		  msg.getSubject().trim().indexOf(this.subject) != -1){
      	System.out.println("Deleting  " + msg.getSubject() );
        	  msg.setFlag(Flags.Flag.DELETED, true);  	
        
      }
    }
    folder.close(true);
    store.close();
  }

private boolean  forwardEmail( String fileName){
	File f = new File(fileName);
  boolean res = false;
  System.out.println("Forward Email file exists " + f.exists() + " " + fileName);
	if(f.exists() ){
		res = true;
		try {
			Iterator enume = smtpcheck.keySet().iterator();
			String to = null ;
			while (enume.hasNext()){
				String pro = (String)enume.next();
				System.out.println("ASI " + fileName + " pro:" + pro + " ind:" + fileName.indexOf("[" +pro + "]"));
				if(fileName.indexOf("[" +pro + "]") != -1){
					
					to = smtpcheck.get(pro) + "";
					break;
				}
			}
			
		if(to != null){
			Email mail = new Email(null);
			mail.setSMTP(smtphost);
			mail.setFrom(smtpfrom);
			mail.setSubjectFilesTo("Picture");
			//mail.addAttach(f.getAbsolutePath());
			if(pictEmailBody != null){
				mail.setBodyHtml("Picture\n" + Converter.deCode(pictEmailBody),f.getAbsolutePath() );	
			}else{
				mail.setBodyHtml("Picture\n" ,f.getAbsolutePath());
			}
		    
			mail.setSendNotice(false);
			
			
			StringTokenizer st = new StringTokenizer(to,",");
			while(st.hasMoreTokens()){
				String mai = st.nextToken();
				mail.setFilesTo(mai);
				mail.send();
			}	
		
		}
	    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	return res ;
}


  public ArrayList readBodyFiles() throws MessagingException, IOException{
    ArrayList bodyFiles = new ArrayList();
    Properties props = new Properties();
    props.put("mail.host", this.pop3host);
    props.put("mail.protocol", this.pop3proto);
    
    
    urlName = new URLName(this.pop3proto, this.pop3host, PORT,
            "INBOX", user, password);
    
    
    String proto = props.getProperty("mail.protocol");
    if (proto != null){
      PROTOCOL = proto.trim().toLowerCase();
      if (proto.equals("pop3s")){
        PORT = 995;
      }
    }
    Session ses = Session.getInstance(props);
    Store store = null;
  
    if(PROTOCOL.equals("pop3s")){

    	MailSSLSocketFactory sf = null;
    	try {
    		sf = new MailSSLSocketFactory();
    		sf.setTrustAllHosts(true);
    	} catch (GeneralSecurityException e1) {
    		// TODO Auto-generated catch block
    	
    		e1.printStackTrace();
    	}
        	
                   Properties prop = System.getProperties();
                  // prop.put("mail.pop3.ssl.socketFactory", sf);
                   prop.setProperty( "mail.pop3s.socketFactory.class", "eevaeksport.mailagent.AcceptAllCerts");//javax.net.ssl.SSLSocketFactory
                   prop.setProperty( "mail.pop3s.socketFactory.fallback", "false");
                   prop.setProperty("mail.pop3s.port", PORT+"");
                   prop.setProperty("mail.pop3s.socketFactory.port", PORT+"");
                   Session session = Session.getInstance(prop);
                   store = session.getStore("pop3s");
            
               
               // session.setDebug(true);
               // Get a Store object
               // Connect
               
               store.connect(urlName.getHost(), urlName.getUsername(), urlName.getPassword());
               
        	
    }else if (PROTOCOL.equals("imaps")  ){
       	MailSSLSocketFactory socketFactory = null;
    	try {
    		socketFactory = new MailSSLSocketFactory();
    	} catch (GeneralSecurityException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
        socketFactory.setTrustAllHosts(true);
       
    	
        Properties prop = System.getProperties();
      //  prop.put("mail.imaps.ssl.socketFactory", socketFactory);
        prop.setProperty( "mail.imap.socketFactory.class", "eevaeksport.mailagent.AcceptAllCerts");//javax.net.ssl.SSLSocketFactory
        prop.setProperty( "mail.imap.socketFactory.fallback", "false");
        Session session = Session.getInstance(prop);
        store = session.getStore("imaps");
    store.connect(urlName.getHost(), urlName.getUsername(), urlName.getPassword());

    }else if (PROTOCOL.equals("imapssl")){
    		MailSSLSocketFactory socketFactory = null;
    	try {
    		socketFactory = new MailSSLSocketFactory();
    	} catch (GeneralSecurityException e) {
    		e.printStackTrace();
    	}
        socketFactory.setTrustAllHosts(true);
         Properties prop = System.getProperties();
        prop.setProperty( "mail.imaps.socketFactory.class", "eevaeksport.mailagent.AcceptAllCerts");//javax.net.ssl.SSLSocketFactory
        prop.setProperty( "mail.imaps.socketFactory.fallback", "false");
        prop.setProperty( "mail.imap.socketFactory.class", "eevaeksport.mailagent.AcceptAllCerts");//javax.net.ssl.SSLSocketFactory
        prop.setProperty( "mail.imap.socketFactory.fallback", "false");
       Session session = Session.getInstance(prop);
        store = session.getStore("imaps");
    store.connect(urlName.getHost(), urlName.getUsername(), urlName.getPassword());

    }else if (PROTOCOL.equals("imap")){
    	
    	store = new IMAPStore(ses,  new URLName("imap", props.getProperty("mail.host"), 143,
  	           "INBOX", this.user, this.password));
    	 store.connect();
     }else  {
    	
        store = new POP3Store(ses, new URLName("pop3", props.getProperty("mail.host"), PORT,
           "INBOX", this.user, this.password));
        store.connect();
   		
    }
	   Folder folder;
	    if (PROTOCOL.equals("imap") || PROTOCOL.equals("imaps") || PROTOCOL.equals("imapssl")){
	      folder = (IMAPFolder)store.getFolder("INBOX");
	    }	else {
	      folder = (POP3Folder)store.getFolder("INBOX");
	    }
	    	
    folder.open(Folder.READ_WRITE);
    Message[] msgs = folder.getMessages();
    int max = msgs.length;
    if(this.useMax) {
    	max = 200;
    }
    
    if(msgs.length < 200) {
    	max = msgs.length;
    }
    
    for (int i = 0 ; i < max ; i++){
      Message msg = msgs[i];
      if (msg.getSubject() != null && msg.getSubject().trim().indexOf(this.subject) != -1){
      //	System.out.println("msg.getContent() " +((MimeMultipart)msg.getContent()).toString());
        InputStream is = ((MimeMultipart)msg.getContent()).getBodyPart(0).getInputStream (  ) ; 
        BufferedReader reader = 
                        new BufferedReader ( new InputStreamReader ( is )  ) ; 
        String line = reader.readLine (  ) ; 
        String s = "";
        while ( line!=null )  {  
          s = s + line; 
          line = reader.readLine (  ) ; 
         } 
    //	System.out.println("msg.getContent() " +((MimeMultipart)msg.getContent()).toString() + s);
       // String s = (String)msg.getContent();
        String fn = parseFileFromSubject(msg.getSubject());
        writeToFile((String)s, dataDir  + File.separator + fn);
        bodyFiles.add(dataDir +  File.separator + fn);

        msg.setFlag(Flags.Flag.DELETED, true);
      }
    }
    folder.close(true);
    store.close();
    return bodyFiles;
  }

  private String parseFileFromSubject(String sub){
    StringTokenizer st = new StringTokenizer(sub);
    st.nextToken();
    return st.nextToken();
  }

  private void writeToFile(InputStream is, String fileName) throws IOException{
	  try{
		  FileOutputStream fos = new FileOutputStream(fileName);
		
		    byte[] bytes = new byte[1024];
		    int read = 0;
		    while ((read = is.read(bytes)) != -1) {
				fos.write(bytes, 0, read);
			}
		    fos.close();
		    
	  }catch(Exception e){
		  e.printStackTrace();
	  }
  
  }

  private void writeToFile(String mes, String fileName) throws IOException{
    FileOutputStream fos = new FileOutputStream(fileName);
    fos.write(mes.getBytes());
    fos.close();
  }

  public static String addTimeStamp(String s){
    String result = null;
    if(s.contains("-TASK")){
    	return s;
    }
    int index = s.indexOf(".");
    if (index == -1){
      result = s + "(" + System.currentTimeMillis() + ")";
    } else {
      result = s.substring(0, index) + "(" + System.currentTimeMillis() + ")" + s.substring(index, s.length());
    }
    return result;
  }

  /**
   *
   * @param aPop3host - the new value for pop3host
   */
  public void setPop3host(String aPop3host){
    pop3host = aPop3host;
  }

  /**
  *
  * @param aUser - the new value for user
  */
  public void setUser(String aUser){
          user = aUser;
  }

  /**
  *
  * @param aPassword - the new value for password
  */
  public void setPassword(String aPassword){
          password = aPassword;
  }

  /**
  *
  * @param aSaveDir - the new value for saveDir
  */
  public void setSaveDir(String aSaveDir){
          saveDir = aSaveDir;
  }


  public void setDataDir(String aSaveDir){
           dataDir = aSaveDir;
   }

  private  int PORT = 110;
  private String pop3host = null;
  private String PROTOCOL = "pop3";
  private String user = null;
  private String password = null;
  private String smtphost = null;
  private String smtpfrom = null;
  private String smtpto = null;
  public String getSmtpto() {
	return smtpto;
}
public void setSmtpto(String smtpto) {
	this.smtpto = smtpto;
}


private TreeMap smtpcheck = null; 
  public String getSmtpfrom() {
	return smtpfrom;
}
public void setSmtpfrom(String smtpfrom) {
	this.smtpfrom = smtpfrom;
}
public TreeMap getSmtpcheck() {
	return smtpcheck;
}
public void setSmtpcheck(TreeMap smtpcheck) {
	this.smtpcheck = smtpcheck;
}
public String getSmtphost() {
	return smtphost;
}
public void setSmtphost(String smtphost) {
	this.smtphost = smtphost;
}


private boolean saveAttach = true;
  public boolean isSaveAttach() {
	return saveAttach;
}
public void setSaveAttach(boolean saveAttach) {
	this.saveAttach = saveAttach;
}

private URLName urlName = null;
  private String pop3proto = "pop3";
  private String saveDir = null;
  private String saveDir2 = null;
  public String getSaveDir2() {
	return saveDir2;
}
public void setSaveDir2(String saveDir2) {
	this.saveDir2 = saveDir2;
}

public boolean useMax = false;

private String dataDir = null;
  private String subject = null;
/**
 * @return Returns the pop3proto.
 */
public String getPop3proto() {
	return pop3proto;
}
/**
 * @param pop3proto The pop3proto to set.
 */
public void setPop3proto(String pop3proto) {
	this.pop3proto = pop3proto;
}
public String getPictEmailBody() {
	return pictEmailBody;
}
public void setPictEmailBody(String pictEmailBody) {
	this.pictEmailBody = pictEmailBody;
}
public String getLinkText() {
	return linkText;
}
public void setLinkText(String linkText) {
	this.linkText = linkText;
}
public String getSendText() {
	return sendText;
}
public void setSendText(String sendText) {
	this.sendText = sendText;
}
}