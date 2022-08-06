package eevaeksport.mailagent;

import java.io.BufferedReader;



import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import com.github.scribejava.apis.LinkedInApi20;
import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.core.builder.ScopeBuilder;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth.OAuthService;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.ITokenCacheAccessAspect;
import com.microsoft.aad.msal4j.ITokenCacheAccessContext;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.MsalException;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.SilentParameters;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.pop3.POP3Store;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.MailSSLSocketFactory;

import eevaeksport.EksportFrame;
import eevaeksport.Email;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class MailAgent  {

  private Properties props = null;
  private Properties emailProps = null;
  private static int PORT = 110;
  private String PROTOCOL = "pop3";
  private URLName urlName = null;
  static Category log = Category.getRoot();
  private boolean logi = false;
  
  private boolean sendmessages = false;
  private boolean secure = false;
  private boolean filecomparator = false;
  private Date  datee = new Date();
  
  
  private String time = "";
  
  
 
  private static IAuthenticationResult acquireTokenInteractive(String applicationId, String authEndpoint, String redirectURI, Set<String> SCOPE) throws Exception {

      // Load token cache from file and initialize token cache aspect. The token cache will have
      // dummy data, so the acquireTokenSilently call will fail.
      TokenCacheAspect tokenCacheAspect = new TokenCacheAspect("sample_cache.json");

      PublicClientApplication pca = PublicClientApplication.builder(applicationId)
              .authority(authEndpoint)
              .setTokenCacheAccessAspect((ITokenCacheAccessAspect) tokenCacheAspect)
              .build();

      Set<IAccount> accountsInCache = pca.getAccounts().join();
      // Take first account in the cache. In a production application, you would filter
      // accountsInCache to get the right account for the user authenticating.
      IAccount account = accountsInCache.iterator().next();

      IAuthenticationResult result;
      try {
          SilentParameters silentParameters =
                  SilentParameters
                          .builder(SCOPE, account)
                          .build();

          // try to acquire token silently. This call will fail since the token cache
          // does not have any data for the user you are trying to acquire a token for
          result = pca.acquireTokenSilently(silentParameters).join();
      } catch (Exception ex) {
          if (ex.getCause() instanceof MsalException) {

              InteractiveRequestParameters parameters = InteractiveRequestParameters
                      .builder(new URI(redirectURI))
                      .scopes(SCOPE)
                      .build();

              // Try to acquire a token interactively with system browser. If successful, you should see
              // the token and account information printed out to console
              result = pca.acquireToken(parameters).join();
          } else {
              // Handle other exceptions accordingly
              throw ex;
          }
      }
      return result;
  }

  
  
 private String  getMSToken() {
	 String res = null;
	 
	 String secret = "vPcy.o_8iA-VSCst-g16fOAB8UGI527EC~"; 
	 String appID = "4ff5c689-a2ee-427a-a78b-6246855d0b39";
	 String scope = "https://outlook.office.com/IMAP.AccessAsUser.All";
	 
  props.put("appid", appID);
  props.put("scope", scope);
 
//  public static final Set<String> SCOPE = Set.of("Mail.ReadAll","Mail.SendAll");
  
/*  IAuthenticationResult authresult = null;
  try {
      
    
    
      String scopes[]=scope.split(",");
      Set<String> SCOPE = new HashSet<String>();
      SCOPE.add("https://outlook.office.com/IMAP.AccessAsUser.All");
     
      String authEndpoint="https://login.microsoftonline.com/" + secret + "/oauth2/v2.0/authorize";
      String redirectURI=  "http://localhost" ; 
    		  
    		  //prop.getProperty("redirectURI");
      
      //applicationId=xxxxyyy
    		 // authEndpoint=
    		  //redirectURI=http://localhost
    		  //scopes=https://outlook.office365.com/SMTP.Send,https://outlook.office365.com/Mail.Read,https://outlook.office365.com/Mail.Send


      authresult = acquireTokenInteractive(appID,authEndpoint,redirectURI, SCOPE);
    System.out.println("uhuu " + authresult.accessToken());
    
  
            
  } catch (Exception e) {
      e.printStackTrace();
  }
  System.exit(0); */
  
  ScopeBuilder sp = new ScopeBuilder();
  sp.withScope("offline_access");
  sp.withScope("IMAP.AccessAsUser.All");
  sp.withScope("SMTP.Send");
	 try {
		//  final OAuthRequest request = new OAuthRequest(Verb.GET, "https://graph.microsoft.com/v1.0/me");
		  final OAuth20Service service = new ServiceBuilder(appID)
	                .apiSecret(secret)
	                .defaultScope(sp)
	               // .defaultScope("SMTP.Send")
	              
	               // .callback("https://outlook.office.com/IMAP.AccessAsUser.All")
	                .build(MicrosoftAzureActiveDirectory20Api.instance());
		 
		  final String authorizationUrl = service.getAuthorizationUrl();
	      
	        System.out.println(authorizationUrl);
	        String url = authorizationUrl;
			URL urlst = null;
			try {
				urlst = new URL(url);
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
		
			
			HttpsURLConnection	 https = (HttpsURLConnection) urlst.openConnection();
			
            String code0 = "";
			
			InputStream is = https.getInputStream();
			BufferedReader r = new BufferedReader(new InputStreamReader(is));
			try {
			    while (r.readLine() != null) {
			        code0 = code0 + (r.readLine());
			    }  
			 } catch (IOException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			 }
	        
			System.out.println("code0 " + code0);
			System.out.println(authorizationUrl);
			
	        System.out.println("And paste the authorization code here");
	        System.out.print(">>");
	        final Scanner in = new Scanner(System.in, "UTF-8");
	        System.out.println("=== "  + "'s OAuth Workflow ===");
	        System.out.println();
	        // Obtain the Authorization URL
	        System.out.println("Fetching the Authorization URL...");
	        final String code = in.nextLine();
		 
		 System.out.println("Trading the Authorization Code for an Access Token...");
	        final OAuth2AccessToken accessToken = service.getAccessToken(code);
	        System.out.println("Got the Access Token!");
	        System.out.println("(The raw response looks like this: " + accessToken.getAccessToken() + "')");
		res =   accessToken.getAccessToken();
		
		  
		//  service.execute(request);
		 
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	 
	 return res;
 }
   
   
   
  public MailAgent() throws IOException{
	  PropertyConfigurator.configure("log4j.properties");
    props = new Properties();
    datee.setTime(Calendar.getInstance().getTimeInMillis());
    time = datee.toString();
    props.load(new FileInputStream(EksportFrame.INI_FILE));
    
    
    
    emailProps = new Properties();
   try{
   	emailProps.load(new FileInputStream(props.getProperty("emailfile")));
     }catch (Exception e){
     }
    
    String proto = props.getProperty("mail.protocol");
    if (proto != null){
      PROTOCOL = proto.trim().toLowerCase();
      if (proto.equals("imap")){
        PORT = 143;
      }
      if (proto.equals("pop3s")){
        PORT = 995;
      }
      if (proto.equals("imaps")){
          PORT = 993;
        }
    }
    
    String user = props.getProperty("pop3.user");
    urlName = new URLName(PROTOCOL, props.getProperty("pop3.host"), PORT,
        "INBOX", user, props.getProperty("pop3.password"));

  

    
  }

  private void execute() throws Exception{
  if("true".equals(props.getProperty("logi"))){
   logi =  true;
  }

  if("true".equals(props.getProperty("loadingfile"))){
	FileOutputStream fos = new FileOutputStream(props.getProperty("pop3.dir") + "loading.txt");
	fos.close();
	System.out.println("Loadingfile = true");
   }
  
  System.out.println("logging = " + logi);
  
  if(props.getProperty("securemail") != null){
    secure =  true;
   }
  
  if(props.getProperty("failcomparator.mail") != null){
    this.filecomparator =  true;
   }
  
  
  if("true".equals(props.getProperty("sendmessages"))){
    sendmessages =  true;
   }
  
  
  if(sendmessages){
    String dir = props.getProperty("readin");
    if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
    File f = new File(dir + "agent.txt");

    if(f.exists()){
    FileReader fr = new FileReader(dir + "agent.txt");
    BufferedReader reader = new BufferedReader(fr);
    String line = null;

  while((line = reader.readLine()) != null){

    StringTokenizer st = new StringTokenizer(line, ";");
    String agent = st.nextToken();
    String client = st.nextToken();
    String trouble = st.nextToken();
    if(emailProps.getProperty(agent)!= null && this.parseSecondEmail(emailProps.getProperty(agent)).length()>2){
    
    Email mail = new Email(null);
	mail.setSMTP(props.getProperty("smtphost"));
	mail.setFilesTo(this.parseSecondEmail(emailProps.getProperty(agent)));
	mail.setFrom(props.getProperty("from"));
	mail.setSubjectFilesTo("Problems From Warehouse");
	//mail.addAttach(f.getName());
	System.out.println(client + "\n" + trouble + agent + this.parseSecondEmail(emailProps.getProperty(agent)));
	mail.setBody(client + "\n" + trouble  );
	mail.setSendNotice(false);
	mail.send();
	
    }
  }
  
  fr.close();
  reader.close();
  	//f.delete();
  }
  
  }
  
  
  
  System.out.println("works");
  
  

	
	
	
	
  
    Calendar cal = Calendar.getInstance();
    Session ses = Session.getInstance(props);
    Store store;
    System.setProperty("mail.mime.base64.ignoreerrors", "true");
    
    if (PROTOCOL.equals("imap")){
    	
    	Properties prop = System.getProperties();
          prop.setProperty( "mail.imap.partialfetch", "false");
          Session session = Session.getInstance(prop);
          store = new IMAPStore(session, urlName);
          store.connect();
          
    }else if (PROTOCOL.equals("imaps")){
    	
    	MailSSLSocketFactory socketFactory= new MailSSLSocketFactory();
        socketFactory.setTrustAllHosts(true);
       
    	
        Properties prop = System.getProperties();
       prop.setProperty( "mail.imap.socketFactory.class", "eevaeksport.mailagent.AcceptAllCerts");//javax.net.ssl.SSLSocketFactory
       // prop.setProperty( "mail.imap.socketFactory.class", "eevaeksport..AcceptAllCerts");//javax.net.ssl.SSLSocketFactory
        
        prop.setProperty( "mail.imap.socketFactory.fallback", "false");
        Session session = Session.getInstance(prop);
        store = session.getStore("imaps");
 
    
    // session.setDebug(true);
    // Get a Store object
    // Connect
    
    store.connect(props.getProperty("pop3.host"), props.getProperty("pop3.user"), props.getProperty("pop3.password"));
    
	
} else if (PROTOCOL.equals("imapoath")){
	
	MailSSLSocketFactory socketFactory= new MailSSLSocketFactory();
    socketFactory.setTrustAllHosts(true);
     
     Properties prop = new Properties();
   //  prop.put("mail.imap.ssl.enable", "true"); // required for Gmail
  //   prop.put("mail.imap.sasl.enable", "true");
   //  prop.put("mail.imap.sasl.mechanisms", "XOAUTH2");
    // prop.put("mail.imap.auth.login.disable", "true");
    // prop.put("mail.imap.auth.plain.disable", "true");
     
     
     
  // props.put("mail.imap.ssl.enable", "true"); // required for Gmail
   /*  prop.put("mail.smtp.auth.xoauth2.disable","false");
     prop.put("mail.smtp.sasl.enable", "true");
     prop.put("mail.imap.auth.mechanisms", "XOAUTH2");
     prop.put("mail.smtp.auth.mechanisms","XOAUTH2");
     prop.put("mail.smtp.port", "587");
     prop.put("mail.smtp.starttls.enable", "true");*/
     
     prop.put("mail.smtp.auth", "true");
     prop.put("mail.transport.protocol", "smtp");
     prop.put("mail.smtp.starttls.enable", "true");
     prop.put("mail.smtp.auth.mechanisms", "XOAUTH2");
     prop.put("mail.smtp.auth.login.disable","true");
     prop.put("mail.smtp.auth.plain.disable","true");
     prop.put("mail.debug.auth", "true");
     
     //prop.put("mail.transport.protocol","smtp");
    // prop.put("mail.smtp.host","smtp.office365.com"); 
    
     prop.put("mail.debug",true);
     
     

   //  props.put("mail.imap.ssl.enable", "true"); // required for Gmail
   //  prop.put("mail.imap.sasl.enable", "true");
   //  prop.put("mail.imap.sasl.mechanisms", "XOAUTH2");
    // prop.put("mail.imap.auth.login.disable", "true");
     // prop.put("mail.imap.auth.plain.disable", "true");
   
    
     String token = props.getProperty("oauth.accesstoken");
     
    if("outlook.office365.com".equals( props.getProperty("pop3.host"))) {
    	token = getMSToken();
    }
     
    System.out.println("token" + token);
    
    Session session = Session.getInstance(prop);
     store = session.getStore("imaps");
     store.connect(props.getProperty("pop3.host"), props.getProperty("pop3.user"), token);
     
     
} else if (PROTOCOL.equals("imapssl")){
	
	MailSSLSocketFactory socketFactory= new MailSSLSocketFactory();
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

   store.connect(props.getProperty("pop3.host"), props.getProperty("pop3.user"), props.getProperty("pop3.password"));


}else if (PROTOCOL.equals("pop3s")){
    	

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
               if("true".equals(props.getProperty("pop3.tls2"))){
            	     prop.setProperty("mail.pop3s.ssl.protocols", "TLSv1.2");
                }
               
               Session session = Session.getInstance(prop);
               store = session.getStore("pop3s");
        
           
           // session.setDebug(true);
           // Get a Store object
           // Connect
           
           store.connect(props.getProperty("pop3.host"),PORT, props.getProperty("pop3.user"), props.getProperty("pop3.password"));
           
    	
    }else if (PROTOCOL.equals("pop3tls")){
    	
    	Properties propse = new Properties();

    	propse.setProperty("mail.pop3.ssl.socketFactory.class", "eevaeksport.mailagent.AcceptAllCerts");
    	propse.setProperty("mail.pop3.ssl.socketFactory.port", PORT + "");
    	propse.setProperty("mail.pop3.starttls.enable", "true");
    	  if("true".equals(props.getProperty("pop3.tls2"))){
    			propse.setProperty("mail.pop3.ssl.protocols", "TLSv1.2");
    	  }
    

    	Session session = Session.getInstance(propse, null);
    	 store = session.getStore("pop3");
    	 store.connect(props.getProperty("pop3.host"), props.getProperty("pop3.user"), props.getProperty("pop3.password"));
    
}else {
        store = new POP3Store(ses, urlName);
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
     if(logi){
    log(time + " Number of messages in EMAIL Server : " + msgs.length);
     }
    for (int i = 0 ; i < msgs.length ; i++){
      Message msg = msgs[i];
      String subject = null;
      try{
    	  subject = msg.getSubject();//siis tee subjekti  
    	  System.out.println("msg. subject " + msg.getSubject() + " " + msg.getDescription() + " "+msg.getContentType());
      }catch (Exception eee ){
    	  eee.printStackTrace();
    	  log(eee.toString());
    	  
      } 
     
      File f = null;
      log(time + " Subject : " + subject);
      
      if("true".equals(props.getProperty("pop3.nosubject"))){
    	  if(subject == null || subject.equals("null")){
    		  subject = "Mob"; 
    	  }
      }
      
      if("true".equals(props.getProperty("pop3.nosubjectatall"))){
    		  subject = "Mob"; 
         }
    
      
      if(subject != null && subject.length()>30 && subject.indexOf("@") == -1 && subject.indexOf("|") == -1){
    	  subject = subject.substring(0,29);
      }
    
      if(subject != null){
    	  subject = subject.toLowerCase().trim();
      }
      
      
   if(props.getProperty("pop3.csvdir") != null && props.getProperty("pop3.makedir") != null && props.getProperty("pop3.makedir").equals("true")){
     f = new File(props.getProperty("pop3.csvdir") + ("(" + cal.get(Calendar.DAY_OF_MONTH)) + testLength(cal.get(Calendar.MONTH) + 1) + ")" +
                           testLength(cal.get(Calendar.HOUR_OF_DAY)) + "." + testLength(cal.get(Calendar.MINUTE))
                          + "." + testLength(cal.get(Calendar.SECOND))+ "." + testLength(cal.get(Calendar.MILLISECOND)));
     
     }else if(props.getProperty("pop3.csvdir") != null){
     f = new File(props.getProperty("pop3.csvdir"));
   }
    
     
   boolean pdf = false;
   if(props.getProperty("pop3.pdfdir") != null){  
	   pdf = true;
   }
     
    if (msg != null && subject != null &&
    		subject.trim().length()>0 && (subject.trim().toLowerCase().indexOf(props.getProperty("pop3.subjectcheck").toLowerCase()) != -1 ||
    				subject.trim().toLowerCase().indexOf(props.getProperty("pop3.subjectcheck2").toLowerCase()) != -1 
    				|| subject.trim().toLowerCase().startsWith(props.getProperty("pop3.subjectcheck3").toLowerCase()) || pdf )){
     
    	Object o = null;
    	try{
    		o = msg.getContent();
    	}catch(Exception k){
    		
    	}
    	if(o != null){
    		
    	
        
        System.out.println("class instance" + o.getClass().toString());
        if(logi){
              log(time + " class instance" + o.getClass().toString());
             }
        
        if(o instanceof BASE64DecoderStream){
       
                  BASE64DecoderStream newObj = (BASE64DecoderStream) o;
                  String filename = MimeUtility.decodeText(msg.getFileName());
                  
                
                  
System.out.println(filename + " | " +msg.getFileName());

if("true".equals( props.getProperty("addtime"))){
    String add =Calendar.getInstance().getTimeInMillis() + "" ;
    System.out.println(add);
    System.out.println(add.substring(5,12));
    filename = add + filename;
  }
                  File attacheFile = new File(props.getProperty("pop3.dir") + filename.replace('/', '-'));

                  FileOutputStream fos = new FileOutputStream(attacheFile);
                  byte[] buffer = new byte[1024];

                  while (0 < newObj.read(buffer)) {
                      fos.write(buffer);
                  }

                  newObj.close();
                  fos.close();

                  System.out.println("Filename : " + filename);

              } 

        	  
       boolean errormail = false;
        
        
        if (o instanceof MimeMultipart){  
          MimeMultipart mp = (MimeMultipart)o;
          String fileName = null;

          try{
          if(logi){
          	Address yyy = msg.getFrom()[0];
          	int tykke = 0;
          	try{
          		tykke = mp.getCount();
          	}catch(Exception e){
          		errormail = true;
          	}
            log(time + " Message NR. " + (i+1) +" from: " + yyy.toString() + " subject: "  + subject + 
            	System.getProperty("line.separator")+ "\t\t\t\t number of files attached to current message :" + tykke);
           
          }
          	try{
          		
          if(!errormail){
        	  
          
          for (int j = mp.getCount(); j > 0 ; j--){
            MimeBodyPart mbp = (MimeBodyPart)mp.getBodyPart(j-1);
            
            if (mbp.getFileName() != null){
              fileName = mbp.getFileName();
              
             try{
            	 fileName = MimeUtility.decodeText(mbp.getFileName());
             }catch(Exception e){
            	 
             }
               int mailFileSize = mbp.getSize() ;
              if(logi){
                log(time + " "
                     + fileName);
              }
              System.out.println(fileName);
              InputStream is = mbp.getInputStream();
              if (subject.indexOf("CREDIT") != -1 ){
                if(props.getProperty("pop3.creditdir") != null){
                  writeToFile(is,props.getProperty("pop3.creditdir") + fileName);
                  if(logi){
                    log(time + " Creditdirectory " + props.getProperty("pop3.creditdir") + fileName);
                  }

                }
              } else if(msg != null && subject != null && subject.indexOf("data") != -1 ) {
                if(props.getProperty("pop3.datadir") != null){
                    writeToFile(is,props.getProperty("pop3.datadir") + fileName);
                    File ff = new File(props.getProperty("pop3.datadir") + fileName);
                    if(logi){
                   log(time + " DATAdirectory " + props.getProperty("pop3.datadir") + fileName + " size:" + ff.length() + "bytes. Size on server:" + mailFileSize  +"bytes");
                 }
                  }
              } else if(msg != null && subject != null && (subject.indexOf("e-arve") != -1 || subject.indexOf("pas") != -1 || subject.indexOf("mob") != -1 ) &&
                     (  fileName.startsWith("t-")|| fileName.startsWith("a-")|| fileName.startsWith("p-")|| fileName.startsWith("A-")|| fileName.startsWith("L-")
                    			|| fileName.startsWith("Cash")	|| fileName.startsWith("l-")) && fileName.endsWith(".csv") && props.getProperty("pop3.csvdir") != null &&
              props.getProperty("pop3.csvdir").trim().length()>0){
               if (!f.exists()) {
                 f.mkdir();
               }
               
             
               File ff = new File(props.getProperty("pop3.csvdir") + fileName);
               if(props.getProperty("pop3.confirmdir") != null && props.getProperty("pop3.confirminfo") != null &&
            		subject.indexOf(props.getProperty("pop3.confirminfo")) > -1){
            	writeToFile(is, props.getProperty("pop3.confirmdir") + fileName);
            }else if(props.getProperty("pop3.confirmdir2") != null && props.getProperty("pop3.confirminfo2") != null &&
            		subject.indexOf(props.getProperty("pop3.confirminfo2")) > -1){
            	writeToFile(is, props.getProperty("pop3.confirmdir2") + fileName);
            }else if(props.getProperty("pop3.confirmdir3") != null && props.getProperty("pop3.confirminfo3") != null &&
            		subject.indexOf(props.getProperty("pop3.confirminfo3")) > -1){
            	writeToFile(is, props.getProperty("pop3.confirmdir3") + fileName);
            }else if(props.getProperty("pop3.confirmdir4") != null && props.getProperty("pop3.confirminfo4") != null &&
            		subject.indexOf(props.getProperty("pop3.confirminfo4")) > -1){
            	
            	writeToFile(is, props.getProperty("pop3.confirmdir4") + fileName);
            }else if(props.getProperty("pop3.confirmdir5") != null && props.getProperty("pop3.confirminfo5") != null &&
            		subject.indexOf(props.getProperty("pop3.confirminfo5")) > -1){
            	writeToFile(is, props.getProperty("pop3.confirmdir5") + fileName);
            }else if(props.getProperty("pop3.confirmdir6") != null && props.getProperty("pop3.confirminfo6") != null &&
            		subject.indexOf(props.getProperty("pop3.confirminfo6")) > -1){
            	writeToFile(is, props.getProperty("pop3.confirmdir6") + fileName);
            }else if(props.getProperty("pop3.confirmdir7") != null && props.getProperty("pop3.confirminfo7") != null &&
            		subject.indexOf(props.getProperty("pop3.confirminfo7")) > -1){
            	writeToFile(is, props.getProperty("pop3.confirmdir7") + fileName);
            }else if(props.getProperty("pop3.confirmdir8") != null && props.getProperty("pop3.confirminfo8") != null &&
            		subject.indexOf(props.getProperty("pop3.confirminfo8")) > -1){
            	writeToFile(is, props.getProperty("pop3.confirmdir8") + fileName);
            }else{
            	 	
            	  try{
            		  writeToFile(is,  props.getProperty("pop3.csvdir") + fileName);
            		  }catch(Exception fh){
                 	log.error(fh);
                 	System.exit(0);
             	}
            }
             
               if(logi){
                   log(time + " CSVdirectory koond-file " + props.getProperty("pop3.csvdir") + fileName + " size:" + ff.length() + "bytes. Size on server:" + mailFileSize  +"bytes");
                 }


             } else if(msg != null && subject != null && subject.indexOf("e-arve") != -1 && props.getProperty("pop3.csvdir")!=null &&
                     ((fileName.startsWith("sub-") && fileName.endsWith(".csv")) || (fileName.startsWith("t-") && fileName.endsWith(".csv")))) {
             if("true".equals( props.getProperty("addtime"))){
               String add =Calendar.getInstance().getTimeInMillis() + "" ;
               System.out.println(add);
               System.out.println(add.substring(5,12));
                   fileName = add + fileName;
             }
                writeToFile(is, props.getProperty("pop3.csvdir") + fileName);
                if(logi){

                    File ff = new File(props.getProperty("pop3.csvdir") + fileName);
                   log(time + " CSVdirectory ALAM-file ja KOOND-file " + props.getProperty("pop3.csvdir")+  fileName + " size:" + ff.length() + "bytes. Size on server:" + mailFileSize  +"bytes");
                 }

            } else if(msg != null && subject != null && ( subject.indexOf("e-arve") != -1 || subject.indexOf("mob") != -1 ) && fileName.endsWith(".xml") && props.getProperty("pop3.xmldir") != null ) {
              String asi = i + "";
            	
             if( "true".equals(props.getProperty("noindex"))){
            	  asi = "";
              }
             if("true".equals(props.getProperty("addtimestamp"))){
            	 asi = getStamp();
             }
            	writeToFile(is, props.getProperty("pop3.xmldir") + asi + fileName + ".tmp");
            	File ffx = new File(props.getProperty("pop3.xmldir") + asi + fileName + ".tmp");
            	ffx.renameTo(new File(props.getProperty("pop3.xmldir") + asi + fileName));
              System.out.println("xml");
              if(logi){

                File ff = new File(props.getProperty("pop3.xmldir") + fileName);
                   log(time + " XMLdirectory xml-file " + props.getProperty("pop3.xmldir") + fileName + " size:" + ff.length() + "bytes. Size on server:" + mailFileSize  +"bytes");
                 }

            }else if(msg != null && subject != null && fileName.endsWith(".pdf") && props.getProperty("pop3.pdfdir") != null ) {
            	 if("true".equals( props.getProperty("addtime"))){
                     String add =Calendar.getInstance().getTimeInMillis() + "" ;
                     System.out.println(add);
                     System.out.println(add.substring(5,12));
                         fileName = add + fileName;
                   }
                writeToFile(is, props.getProperty("pop3.pdfdir") + i + fileName);
                System.out.println("PDfFILE");
                if(logi){

                  File ff = new File(props.getProperty("pop3.pdfdir") + fileName);
                     log(time + " PDFdirectory pdf-file " + props.getProperty("pop3.pdfdir") + fileName + " size:" + ff.length() + "bytes. Size on server:" + mailFileSize  +"bytes");
                   }

              }
           else if(msg != null && subject != null && subject.indexOf("E-ARVE") != -1 && (fileName.endsWith(".TXT") ||
           		fileName.endsWith(".txt")) && props.getProperty("pop3.txtdir") != null ) {
             String add =Calendar.getInstance().getTimeInMillis() + "" ;
                    fileName = add + fileName;
             writeToFile(is, props.getProperty("pop3.txtdir") + fileName);
                       System.out.println("txt");
                       if(logi){
                        File ff = new File(props.getProperty("pop3.txtdir") + fileName);
                            log(time + " TXTdirectory txt-file " + props.getProperty("pop3.txtdir") + fileName + " size:" + ff.length() + "bytes. Size on server:" + mailFileSize  +"bytes");
                          }
          } else if (msg != null && subject != null && (subject.toLowerCase().trim().indexOf(props.getProperty("pop3.subjectcheck").toLowerCase())!= -1 ||
                     		subject.toLowerCase().trim().indexOf(props.getProperty("pop3.subjectcheck2").toLowerCase())!= -1
                     		|| subject.toLowerCase().trim().startsWith(props.getProperty("pop3.subjectcheck3").toLowerCase()))|| pdf) {
               
                if("true".equals( props.getProperty("addtime"))){
                    String add =Calendar.getInstance().getTimeInMillis() + "" ;
                    System.out.println(add);
                    System.out.println(add.substring(5,12));
                        fileName = add + fileName;
                  }
                
                
                System.out.println(props.getProperty("pop3.confirmdir") + props.getProperty("pop3.confirminfo")+ "\n" +subject );
                if(props.getProperty("pop3.confirmdir") != null && props.getProperty("pop3.confirminfo") != null &&
                		subject.indexOf(props.getProperty("pop3.confirminfo")) != -1){
                	writeToFile(is, props.getProperty("pop3.confirmdir") + fileName);
                	if(logi){
                        File ff = new File(props.getProperty("pop3.confirmdir") + fileName);
                       log(time + " pop3.confirmdir " + props.getProperty("pop3.confirmdir") + fileName + " size:" + ff.length() + "bytes. Size on server:" + mailFileSize  +"bytes");
                     }
                } else if(props.getProperty("pop3.confirmdir2") != null && props.getProperty("pop3.confirminfo2") != null &&
                		subject.indexOf(props.getProperty("pop3.confirminfo2")) != -1){
                	writeToFile(is, props.getProperty("pop3.confirmdir2") + fileName);
                	if(logi){
                        File ff = new File(props.getProperty("pop3.confirmdir2") + fileName);
                       log(time + " pop3.confirmdir2 " + props.getProperty("pop3.confirmdir2") + fileName + " size:" + ff.length() + "bytes. Size on server:" + mailFileSize  +"bytes");
                     }
                } else if(props.getProperty("pop3.confirmdir3") != null && props.getProperty("pop3.confirminfo3") != null &&
                		subject.indexOf(props.getProperty("pop3.confirminfo3")) != -1){
                	writeToFile(is, props.getProperty("pop3.confirmdir3") + fileName);
                	if(logi){
                        File ff = new File(props.getProperty("pop3.confirmdir3") + fileName);
                       log(time + " pop3.confirmdir3 " + props.getProperty("pop3.confirmdir3") + fileName + " size:" + ff.length() + "bytes. Size on server:" + mailFileSize  +"bytes");
                     }
                }else if(props.getProperty("pop3.confirmdir4") != null && props.getProperty("pop3.confirminfo4") != null &&
                		subject.indexOf(props.getProperty("pop3.confirminfo4")) != -1){
                	writeToFile(is, props.getProperty("pop3.confirmdir4") + fileName);
                	if(logi){
                        File ff = new File(props.getProperty("pop3.confirmdir4") + fileName);
                       log(time + " pop3.confirmdir4 " + props.getProperty("pop3.confirmdir4") + fileName + " size:" + ff.length() + "bytes. Size on server:" + mailFileSize  +"bytes");
                     }
                } else if(props.getProperty("pop3.confirmdir5") != null && props.getProperty("pop3.confirminfo5") != null &&
                		subject.indexOf(props.getProperty("pop3.confirminfo5")) != -1){
                	writeToFile(is, props.getProperty("pop3.confirmdir5") + fileName);
                	if(logi){
                        File ff = new File(props.getProperty("pop3.confirmdir5") + fileName);
                       log(time + " pop3.confirmdir5 " + props.getProperty("pop3.confirmdir5") + fileName + " size:" + ff.length() + "bytes. Size on server:" + mailFileSize  +"bytes");
                     }
                } else if(props.getProperty("pop3.confirmdir6") != null && props.getProperty("pop3.confirminfo6") != null &&
                		subject.indexOf(props.getProperty("pop3.confirminfo6")) != -1){
                	writeToFile(is, props.getProperty("pop3.confirmdir6") + fileName);
                	if(logi){
                        File ff = new File(props.getProperty("pop3.confirmdir6") + fileName);
                       log(time + " pop3.confirmdir6 " + props.getProperty("pop3.confirmdir6") + fileName + " size:" + ff.length() + "bytes. Size on server:" + mailFileSize  +"bytes");
                     }
                } else if(props.getProperty("pop3.confirmdir7") != null && props.getProperty("pop3.confirminfo7") != null &&
                		subject.indexOf(props.getProperty("pop3.confirminfo7")) != -1){
                	writeToFile(is, props.getProperty("pop3.confirmdir7") + fileName);
                	if(logi){
                        File ff = new File(props.getProperty("pop3.confirmdir7") + fileName);
                       log(time + " pop3.confirmdir7 " + props.getProperty("pop3.confirmdir7") + fileName + " size:" + ff.length() + "bytes. Size on server:" + mailFileSize  +"bytes");
                     }
                } else if(props.getProperty("pop3.confirmdir8") != null && props.getProperty("pop3.confirminfo8") != null &&
                		subject.indexOf(props.getProperty("pop3.confirminfo8")) != -1){
                	writeToFile(is, props.getProperty("pop3.confirmdir8") + fileName);
                	if(logi){
                        File ff = new File(props.getProperty("pop3.confirmdir8") + fileName);
                       log(time + " pop3.confirmdir8 " + props.getProperty("pop3.confirmdir8") + fileName + " size:" + ff.length() + "bytes. Size on server:" + mailFileSize  +"bytes");
                     }
                }else{
                	if("true".equals(props.getProperty("pop3.changename"))){
                		
                		fileName =clearFileName(fileName);
                			//props.getProperty("pop3.newname")+ (Calendar.getInstance().getTimeInMillis() + "").substring(6) + props.getProperty("pop3.newext");
                	}
                	
                	if(fileName.endsWith("pgp")){
                		fileName = new Date().getTime() + fileName;
                	}
                	 File fg = new File(fileName);
                	 if(fg.exists() && fileName.endsWith("order.txt") ){
               		  Calendar cc = Calendar.getInstance();
               		  fileName = fileName.substring(0,fileName.length()-4) + "." + cc.getTimeInMillis() + fileName.substring(fileName.length()-4) ;
               	  }
                	 try{
                		 writeToFile(is, props.getProperty("pop3.dir") + fileName);
                	 }catch(Exception e){
                		  Calendar cc = Calendar.getInstance();
                		  fileName = cc.getTimeInMillis() + ".xml"  ;
                		 try{
                    		 writeToFile(is, props.getProperty("pop3.dir") + fileName);
                        }catch(Exception fh){
                        	log.error(fh);
                        	System.exit(0);
                    	}
                	 }
                	
                	if(logi){
                        File ff = new File(props.getProperty("pop3.dir") + fileName);
                       log(time + " pop3.directory all whats good " + props.getProperty("pop3.dir") + fileName + " size:" + ff.length() + "bytes. Size on server:" + mailFileSize  +"bytes");
                    if(ff.length()!= mailFileSize){
                    	try{
                    	 writeToFile(mbp.getInputStream(), props.getProperty("pop3.dir") + fileName);
                    }catch(Exception e ){
                    	log.error(e);
                    	try{
                    	 	 writeToFile(mbp.getInputStream(), props.getProperty("pop3.dir") + fileName);
                        }catch(Exception fh){
                        	log.error(fh);
                        	System.exit(0);
                    	}
                    }
                   	
                    }
                	}
                }
                
                

              }else{
            	     log(time + " pop3.directory  download skipped " + props.getProperty("pop3.dir") + fileName );
              }
              is.close();
            }
          }
          }
      	}catch(MessagingException aa){
      		aa.printStackTrace();
      		log.error(aa);
      	}
          }catch (Exception e){
          	e.printStackTrace();
          	log.error(e);
          }  
        }
      }
    }
      msg.setFlag(Flags.Flag.DELETED, true);
      //folder.expunge();

      String aaa = "";
      try{
    	  Address yyy = msg.getFrom()[0];  
    	  aaa = yyy.toString();
      }catch (Exception e){
    	  
      }
      log(time + " deleted message NR." + (i + 1)+" from: " + aaa + " subject: "  + subject);
      log(" ");

    }
    try{
    	   folder.close(true);
    	    store.close();	
    }catch(Exception d){
    	//d.printStackTrace();
    	 store.close();
    }
 
   
    //osa mis saadab  Turva maili selleks et kontrollida kaugel töötava asja toimimist
    if(secure){
    	File f = new File(props.getProperty("securefail"));
        FileOutputStream out = new FileOutputStream(f);
        out.write(' ');
        out.close();

  Email mail = new Email(null);
		mail.setSMTP(props.getProperty("smtphost"));
		mail.setFilesTo(props.getProperty("securemail"));
		mail.setFrom(props.getProperty("from"));
		mail.setSubjectFilesTo("Secure");
		mail.addAttach(f.getName());
		mail.setSendNotice(false);
		mail.send();
	
    }

        
    //osa mis võimaldab kontrollida faili date modyfied ja vastavalt seadt ajale saata maillistile teated
    if(filecomparator){
    	File f = new File(props.getProperty("failcomparator.filename"));
    	int a = Integer.parseInt(props.getProperty("failcomparator.dueminutes"));
    	long b = new Date().getTime()- (a * 60000);
    	//long b = new Date().getTime()- ( 6000);
    	//System.out.println(b +" " +f.lastModified());
        if (f.exists() && f.lastModified()< b){
      //  System.out.println("wwww");

  Email mail = new Email(null);
  String emailadd = props.getProperty("failcomparator.mail");
  StringTokenizer st =
    new StringTokenizer(emailadd,";");
  while (st.hasMoreTokens()) {
  	String addr = st.nextToken();
  	 System.out.println(addr);
  	 
		mail.setSMTP(props.getProperty("smtphost"));
		mail.setFilesTo(addr);
		mail.setFrom(props.getProperty("from"));
		mail.setSubjectFilesTo(props.getProperty("failcomparator.message"));
		//mail.addAttach(f.getName());
		mail.setSendNotice(false);
		mail.send();
		 log(time + " ALARM SENT " + addr);
	      log(" ");
         }	
      }
    }
    if("true".equals(props.getProperty("loadingfile"))){
    	   File f = new File (props.getProperty("pop3.dir") + "loading.txt");
    		if(f.exists()){
    			f.delete();
    		}
    	   }
    }    
 
  void send(String cmdline,String host) {
	//debug(">> " + cmdline);
	//LastCmd = cmdline; // Save command for error msg

	try {
		// Write string as a set of bytes
		(new DataOutputStream((new Socket(host, 110)).getOutputStream())).writeBytes(cmdline + "\r\n");
		//_StatusOK = true;
	} catch (IOException i) {
		System.err.println("Caught exception while sending command to server");
		//_StatusOK = false;
	} catch (Exception e) {
		System.err.println("Send: Unexpected exception: " + e.toString());
		//_StatusOK = false;
	}
}

  
  
  public static String getStamp(){
	    StringBuffer sb = new StringBuffer();
	    Calendar cal= Calendar.getInstance();
	    
	    String hour =   cal.get(Calendar.HOUR_OF_DAY) + "";
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}

		String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		if (month.length() == 1){month = ("0") + month;}

		sb.append("" + cal.get(Calendar.YEAR));
		sb.append("" + month  + "");
		sb.append(day +"_");
	
	    	  String min = String.valueOf(cal.get(Calendar.MINUTE));
			    String sec = String.valueOf(cal.get(Calendar.SECOND));
			     if (hour.length() == 1){
			      sb.append("0");
			    }
			     sb.append(hour);
			   
			    if (min.length() == 1){
			     sb.append("0");
			   }
			     sb.append(min);
			   
			     if (sec.length() == 1){
			        sb.append("0");
			      }
			    sb.append(sec);	
			    sb.append("-");	
	    
	  
	    return sb.toString().trim();
	  }



  
  public String login(String username, String timestamp, String secret)
  throws IOException, NoSuchAlgorithmException
  {
      int i;
      byte[] digest;
      StringBuffer buffer, digestBuffer;
      MessageDigest md5;


      md5 = MessageDigest.getInstance("MD5");
      timestamp += secret;
      digest = md5.digest(timestamp.getBytes());
      digestBuffer = new StringBuffer(128);

      for (i = 0; i < digest.length; i++)
          digestBuffer.append(Integer.toHexString(digest[i] & 0xff));

      buffer = new StringBuffer(256);
      buffer.append(username);
      buffer.append(' ');
      buffer.append(digestBuffer.toString());

      return( buffer.toString()) ;
     //   return true;  


  }

  
/**
 * Method declaration
 *
 *
 * @param emails
 *
 * @return
 *
 * @see
 */
private String parseSecondEmail(String emails) {
    String		email = "";
    StringTokenizer st = new StringTokenizer(emails, ";");

    if (st.hasMoreTokens()) {
        st.nextToken();
    }

    if (st.hasMoreTokens()) {
        email = st.nextToken();
    }

    return email;
}


  private void writeToFile(InputStream is, String fileName) throws IOException{
	  File fg = new File(fileName);
	  if(fg.exists() && fileName.endsWith("order.txt") ){
		//  Calendar cc = Calendar.getInstance();
	//	  fileName = fileName.substring(0,fileName.length()-4) + "." + cc.getTimeInMillis() + fileName.substring(fileName.length()-4) ;
	  }
	  
    FileOutputStream fos = new FileOutputStream(fileName);
    int k = -1;
    while ((k = is.read()) != -1){
      fos.write(k);
    }
    fos.close();
  }
  private String testLength(int i){
     String s = String.valueOf(i);
     if (s.length() == 1) return "0" + s;
     if (s.length() == 4) return s.substring(2, 4);
     return s;
   }


   public void log(String s) {
     log.error(s);
   }


  public static void main(String[] args) throws Exception{
    MailAgent agent = new MailAgent();
    agent.execute();
  }
  
 private String clearFileName(String st ){
	  
		//=?Windows-1257?q?U=FEsakymas=5FEugesta_UAB=2Exls?
		//=?iso-8859-2?Q?1077_U=BEsakymas=5FEugesta_UAB.xls?=
		//=?iso-8859-2?Q?1206_U=BEsakymas=5FEugesta_UAB.xls?=
		// =?iso-8859-1?Q?U=FEsakymas=5FEugesta_UAB.xls?=
	// (Calendar.getInstance().getTimeInMillis() + "").substring(6)
		String aaa = st;
		int ab = aaa.toLowerCase().indexOf("?q?");
		aaa = aaa.substring(ab+3);
		aaa = aaa.replaceAll("=FE", "z");
		aaa = aaa.replaceAll("=BE", "z");
		aaa = aaa.replaceAll("=5F", " ");
		aaa = aaa.replaceAll("=2E", ".");
		aaa = aaa.replaceAll("=", "");
if(aaa.endsWith("?")){
	aaa = aaa.substring(0,aaa.length()-1);	
}
		System.out.println("Uuring " + " " + aaa);
		return  (Calendar.getInstance().getTimeInMillis() + "").substring(6)  +  " " + aaa;
 }


  class MailAuthenticator extends Authenticator {
	  private String user;
	  private String pass;
	  
  	  public MailAuthenticator(String user,String pass) {
  	     this.user = user;
  	     this.pass = pass;
  	  }

	protected PasswordAuthentication getPasswordAuthentication() {
		// TODO Auto-generated method stub
		return new PasswordAuthentication(user, pass);
	}
  	  

  }



  


  
}



