/*
 * Created on 19.11.2008
 */
package ftpsync;

import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;



public class FTPSSync2 {

	public FTPSSync2(){
		props = loadProps(INI_FILE);
		 url = props.getProperty("sync.machines");
		user = props.getProperty("sync.ftps.user");
		pass = props.getProperty("sync.ftps.pass");
		passx = props.getProperty("sync.ftps.passx");
		if (pass == null) {
			pass = deCode(passx);
		}
		System.out.println("pass " + pass);		
	}

	public void downAndDelete(){
			try {

				String remDirResults = props
						.getProperty("sync.remote.dir.results");
				String locDirResults = props
						.getProperty("sync.local.dir.results");
				String remDirRename = props
						.getProperty("sync.remote.dir.results.rename");
} catch (Exception ee) {
				ee.printStackTrace();
			}
		
	}
	
	public void upload(){
		String remDirResults = props
				.getProperty("sync.remote.dir.upload");
		String locDirResults = props
				.getProperty("sync.local.dir.upload");
		String locDirRename = props
				.getProperty("sync.local.dir.upload.rename");
		String remDirRename = props
				.getProperty("sync.remote.dir.upload.rename");

		StandardFileSystemManager manager = new StandardFileSystemManager();

		try {
			
		 
		 //check if the file exists
		   String filepath = locDirResults;
		   File file = new File(filepath);
		   if (!file.exists())
		    throw new RuntimeException("Error. Local file not found");
		 
		   //Initializes the file manager
		   manager.init();
		    
		   //Setup our SFTP configuration
		   FileSystemOptions opts = new FileSystemOptions();
		   SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(
		     opts, "no");
		   SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
		   SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 10000);
		   
		   File list = new File(locDirResults);
			for (String sf : list.list()) {
				File f = new File(locDirResults + sf);
				if (f.isFile()) {
			
		   //Create the SFTP URI using the host name, userid, password,  remote path and file name
		    
		   // Create local file object
		   FileObject localFile = (FileObject) manager.resolveFile(f.getAbsolutePath());
		 
		   // Create remote file object
	//	FileObject remoteFile = (FileObject) manager.resolveFile(sftpUri, opts);
		   String userInfo = user + ":" + pass;
		   URI sftpUri = new URI("sftp", userInfo, url, 20222, remDirResults + f.getName(), null, null);
		FileObject remoteFile = manager.resolveFile
				(sftpUri.toString(), opts);
        
		   // Copy local file to sftp server
		try{
			   ( remoteFile).copyFrom(localFile, Selectors.SELECT_SELF);
			   System.out.println("File upload successful");
		}catch(Exception k ){
			k.printStackTrace();
		}
		
			}	
			}
		  }
		  catch (Exception ex) {
		   ex.printStackTrace();
		 
		  }
		 
		  manager.close();
			 		
		

	}
	
	 public static String createConnectionString(String hostName, String username, String password,int port , String remoteFilePath) {
	        return "sftp://" + username + ":" + password + "@" + hostName + ":"  + port + "/" + (remoteFilePath);
	    }
/*	private SftpClient connect() throws IOException{
		String url = props.getProperty("sync.machines");
		System.out.println("sync.machines=" + url);
		SshClient ftp = new SshClient();
		int port = 22;
		if(props.getProperty("ftps.port") != null){
			port = Integer.parseInt(props.getProperty("ftps.port"));
		}
		ftp.connect(url, port, new IgnoreHostKeyVerification());

		PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
		pwd.setUsername(user);
		pwd.setPassword(pass);

		int result = ftp.authenticate(pwd);
		if (result == AuthenticationProtocolState.FAILED) {
			System.out.println("The authentication failed");
			System.exit(0);
		}

		if (result == AuthenticationProtocolState.PARTIAL) {
			System.out
					.println("The authentication succeeded but another"
							+ "authentication is required");
		}

		if (result == AuthenticationProtocolState.COMPLETE)
			System.out.println("The authentication is complete");

		return ftp.openSftpClient();
	}*/
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		FTPSSync2 sync = new FTPSSync2();		
		
		if (args[0].equals("DownAndDelete")) {
			sync.downAndDelete();					
		} else if (args[0].equals("UpLoad")){
			sync.upload();
		} else {
			System.out.println("Use arguments 'DownAndDelete' or 'UpLoad'");
		}
		System.exit(0);
	}

	private static String deCode(String asi) {
		return (asi.substring(1)).substring(0, asi.length() - 2);
	}

	public static Properties loadProps(String fileName) {
		Properties props = new Properties();
		try {
			FileInputStream in = new FileInputStream(fileName);
			props.load(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return props;
	}

	public static final String INI_FILE = "eksport.ini";
	private Properties props;
	private String user;
	private String url;
	private String pass;
	private String passx;
}
