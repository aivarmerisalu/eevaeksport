package eevaeksport;

import java.io.File;


import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import com.sun.mail.util.MailSSLSocketFactory;

import eevaeksport.util.Converter;


/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class Email {

  public Email(String datasent) {
    noticeSubject = datasent;
  }

  public void setFilesTo(String filesTo){
    this.filesTo = filesTo;
  }

  public void setNoticeTo(String noticeTo){
    this.noticeTo = noticeTo;
  }

  public void setFrom(String from){
	  System.out.println("from on xxx " + from);
    this.from = from;
  }
  
  public void setFrom(InternetAddress iafrom){
	  System.out.println("from on xxxi " + from);
	    this.iafrom = iafrom;
	  }

  public void setSMTP(String smtp){
    this.smtp = smtp;
  }

  public void addAttach(String fileName) throws MessagingException{
    MimeBodyPart mbp = new MimeBodyPart();

    FileDataSource fds = new FileDataSource(fileName);
    mbp.setDataHandler(new DataHandler(fds));
    mbp.setFileName(fds.getName());
    //mbp.setText(to);
    //mbp.setText(to, "iso-8859-1");
    //mbp.setFileName(fileName);
    mp.addBodyPart(mbp);
  }

  public void setBody(String txt) throws MessagingException{
    MimeBodyPart mbp = new MimeBodyPart();
    mbp.setText(txt, getEncoding());
   
    if(type != null){
      mbp.setContent(txt,getType() + "; charset=" + getEncoding());	
      //  mbp.setText(txt, getEncoding(), getType());
    }
    mp.addBodyPart(mbp);
  }

  public void setBodyHtml(String txt) throws MessagingException{
 
      MimeBodyPart mbp1 = new MimeBodyPart(); 
      mbp1.setContent("<HTML><BODY>" + txt.replaceAll("\n", "<br>") + "</BODY></HTML>", "text/html; charset=utf-8"); 
      mp.addBodyPart(mbp1);
	 
  }

  public void setBodyHtml(String txt,String image) throws MessagingException{

      MimeBodyPart mbp = new MimeBodyPart(); 
     
      File f = new File(image);
      DataSource source = null;
	  if(f.exists()){
		 source = new FileDataSource(f); 	  
	  }
      
	  mbp.setDataHandler(new DataHandler(source)); 
	  mbp.setFileName(f.getName()); 
      mbp.setDisposition(MimeBodyPart.INLINE); 
      mbp.setHeader("Content-ID","<vogue>"); 
      mp.addBodyPart(mbp);
     
      MimeBodyPart mbp1 = new MimeBodyPart(); 
      String htmlText = "<table border=\"0\"><center><tr><td><br><br><img BORDER=\"0\" width=\"380\" ALIGN=\"Left\" src=\"cid:vogue\"></td></tr></center></table>";
       // htmlText =  htmlText + ("<tr><td><br><br><a  href=\"http://www.merisoft.ee\"><img style=\"border:0;\" src=\"http://www.merisoft.lt/images/logo.png\" width=\"75\" height=\"24\"> </a></td></tr></center></table>" );
  
      mbp1.setContent("<HTML><BODY>" + txt.replaceAll("\n", "<br>") + htmlText + "</BODY></HTML>", "text/html; charset=utf-8"); 
      mp.addBodyPart(mbp1);
	  
    
  }
  
  
  public void setSubjectFilesTo(String subject){
    this.subjectFilesTo = subject;
  }

  public void send() throws AddressException, MessagingException, UnsupportedEncodingException{
    Properties props = System.getProperties();
    props.put("mail.smtp.host", smtp);
    System.out.println("from on " + from);
    
    if (iafrom != null){
    	 props.put("mail.from", iafrom);
      }else{
    	  props.put("mail.from", from);	
      }
   
    
    try {
		Converter.doTrustToCertificates();
	} catch (Exception e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	
	MailSSLSocketFactory sf = null;
	try {
		sf = new MailSSLSocketFactory();
		sf.setTrustAllHosts(true);
	} catch (GeneralSecurityException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
 boolean ssl = false;
	// also use following for additional safety
	//props.put("mail.smtp.ssl.checkserveridentity", "true");
	Session session = Session.getDefaultInstance(props);
    if(port >0){
        System.out.println("port on " + port);
         if(port == 465 ){
         	
        	 if(!smtpplain){
        		 props.put("mail.smtp.socketFactory.port", "465");
          		props.put("mail.smtp.socketFactory.class",
          				"javax.net.ssl.SSLSocketFactory"); 
        	 }
     		
     		props.put("mail.smtp.auth", "true");
     		props.put("mail.smtp.port", "465");
         }else if(port == 587){
        	 ssl= true;
         	props.put("mail.smtp.starttls.enable", "true");
         	props.put("mail.smtp.ssl.trust", smtp);
     		props.put("mail.smtp.port", "587");
     		props.put("mail.smtp.ssl.socketFactory", sf);
     		session.getTransport("smtps");
     	} else {
         	 props.put("mail.smtp.port", port + "");
         	 
         }
     }
     

    
    if(user!=null && user.length()>0 && pass!=null && pass.length()>0){
    	props.put("mail.smtp.auth", "true"); 
    	session = Session.getInstance(props,new PopupAuthenticator(user, pass));
    }
  
    
    
    
    
    
    if(ssl){
   	 sf = null;
	       	try {
	       		sf = new MailSSLSocketFactory();
	       		sf.setTrustAllHosts(true);
	       	} catch (GeneralSecurityException e1) {
	       		 e1.printStackTrace();
	       	}
       props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.port", "587");  
		props.put("mail.smtp.ssl.trust", smtp);
		props.put("mail.smtp.ssl.socketFactory", sf);
		
   }
   
   if(port > 0){
       props.put("mail.smtp.port", port + "");	
   }
   
    session = Session.getDefaultInstance(props);
   
   if(ssl){
   	session.getTransport("smtps");
   }
   
   if(user!=null && user.length()>0 && pass!=null && pass.length()>0){
   	props.put("mail.smtp.auth", "true"); 
   	Authenticator authenticator = new Authenticator()
    {
        @Override
        protected PasswordAuthentication getPasswordAuthentication()
        {
            return new PasswordAuthentication(user, pass);
        }
    };
   	session = Session.getInstance(props,authenticator);
   	//session.setPasswordAuthentication(new URLName(smtp),new PasswordAuthentication(user, pass));
   }
   
    
    
    
  
   
  
		Message msg = new MimeMessage(session);

    if (getImportance().equals(IMPORTANCE_HIGH)){
      msg.addHeader("X-Priority", "1 (Highest)");
      msg.addHeader("X-MSMail-Priority", "High");
      msg.addHeader("Importance", "High");
    }
    msg.setSentDate(new java.util.Date());
   if (iafrom != null){
	 msg.setFrom(iafrom);
   }else{
	 msg.setFrom(new InternetAddress(from));	
   }
   
    msg.setRecipient(
      Message.RecipientType.TO, new InternetAddress(filesTo));
  //  System.out.println(this.subjectFilesTo + getEncoding() );
    msg.setSubject(MimeUtility.encodeText(this.subjectFilesTo, getEncoding(), "B"));
    msg.setContent(mp);
    
    
    
	MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
    mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
    mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
    mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
    mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
    mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
    CommandMap.setDefaultCommandMap(mc);
    
    
    	Transport.send(msg);
	
try{
    if (sendNotice && noticeTo!= null && noticeTo.trim().length()>0){
    	System.out.println("not'"+noticeTo+"'");
        msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(noticeTo));
        msg.setSubject("");

        MimeBodyPart mbp = new MimeBodyPart();
        mbp.setText(this.noticeSubject, getEncoding());
        MimeMultipart mmp = new MimeMultipart();
        mmp.addBodyPart(mbp);
        msg.setContent(mmp);
        Transport.send(msg);
      }	
}catch (Exception e){
	
}


  }

  public void setSendNotice(boolean val){
    this.sendNotice = val;
  }

  /**
   *
   * @return the String value of importance.
   */
  public String getImportance(){
    return importance;
  }

  /**
   *
   * @param aImportance - the new value for importance
   */
  public void setImportance(String aImportance){
    importance = aImportance;
  }

  public void setEncoding(String enc){
    encoding = enc;
  }

  public String getEncoding(){
    return this.encoding;
  }

  private String smtp;
  private String user;
  private String pass;
  public boolean smtpplain = false;
  private int port = 0;
  private String from;
  private InternetAddress iafrom = null;
  private String type = null;
  public String getType() {
	return type;
}

public void setType(String type) {
	this.type = type;
}

private String filesTo;
  private String noticeTo;
  private String subjectFilesTo;
  public String getSubjectFilesTo() {
	return subjectFilesTo;
}

private Multipart mp = new MimeMultipart();
  private String noticeSubject = null;
  private boolean sendNotice = true;
  public static final String IMPORTANCE_HIGH = "high";
  public static final String IMPORTANCE_NORMAL = "normal";
  public static final String IMPORTANCE_LOW = "low";
  private String importance = IMPORTANCE_NORMAL;
  private String encoding = "utf-8";
/**
 * @return Returns the user.
 */
public String getUser() {
	return user;
}
/**
 * 
 * @param user The user to set.
 */
public void setUser(String user) {
	this.user = user;
}
/**
 * @return Returns the pass.
 */
public String getPass() {
	return pass;
}
/**
 * @param pass The pass to set.
 */
public void setPass(String pass) {
	this.pass = pass;
}
class PopupAuthenticator extends Authenticator {
	String username;
	String password;
	public PopupAuthenticator(String username,String password){
	this.username=username;
	this.password=password;
	}
	public PasswordAuthentication getPasswordAuthentication() {
	return new PasswordAuthentication(username,password);
	}
	}
/**
 * @return the port
 */
public int getPort() {
	return port;
}

/**
 * @param port the port to set
 */
public void setPort(String port) {
	if(port != null){
		this.port =Integer.parseInt(port);	
	}
} 
}
