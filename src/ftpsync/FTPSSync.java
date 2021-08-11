/*
 * Created on 19.11.2008
 */
package ftpsync;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;


public class FTPSSync {

	public FTPSSync(){
		props = loadProps(INI_FILE);
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

				FTPSClient client = connect();

				if (remDirResults != null) {
					client.cwd(remDirResults);
				}
              
				FTPFile[] aa = client.listFiles();
				for (FTPFile sf : aa) {
					if (sf.isFile()) {
						System.out.println(sf.getName());
						FileOutputStream oos = new FileOutputStream(
								locDirResults + sf.getName());
						client.retrieveFile(sf.getName(), oos);
						oos.close();
						try{
							client.rename(sf.getName(),
									remDirRename + sf.getName());
						}catch(Exception o ){
							client.deleteFile(sf.getName());
							o.printStackTrace();
						}
						
					
						// client.rm(sf.getFilename());
					}

				}

				client.quit();
			} catch (Exception ee) {
				ee.printStackTrace();
			}
		
	}
	
	public void upload(){
		try {
			String remDirResults = props
					.getProperty("sync.remote.dir.upload");
			String locDirResults = props
					.getProperty("sync.local.dir.upload");
			String locDirRename = props
					.getProperty("sync.local.dir.upload.rename");
			String remDirRename = props
					.getProperty("sync.remote.dir.upload.copy");


			File list = new File(locDirResults);
			for (String sf : list.list()) {
				File f = new File(locDirResults + sf);
				if (f.isFile()) {
					FTPSClient client = connect();
					client.enterLocalPassiveMode();
					if (remDirResults != null) {
						client.cwd(remDirResults);
					}
					 System.out.println("Kataloog " + client.printWorkingDirectory() + "\n" +locDirResults
								+ sf);
					 
					FileInputStream iis = new FileInputStream(f);
					 client.setFileType(FTP.BINARY_FILE_TYPE);
					 client.execPROT("P");
					if(!client.storeFile(sf,iis)){
						System.out.println(" 1 stooring false " + client.getReplyCode() + " " + client.getReplyString() + ""
								+ "\n " + f.exists());
						System.out.println("Ei ole  done " + sf);
						iis.close();
					}else{
						iis.close();
						if (f.exists()) {
							f.renameTo(new File(locDirRename + sf));
						}
						if (f.exists()) {
							f.delete();
						}
						
						System.out.println("Olemas done " + sf);
					
					}
					
                    client.quit();

				}

			}
			
		} catch (Exception ee) {
			ee.printStackTrace();
		}

	}
	
	private FTPSClient connect() throws IOException{
		String url = props.getProperty("sync.machines");
		System.out.println("sync.machines=" + url);
		FTPSClient ftp = new FTPSClient(true);
		 ftp.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
		int port = 22;
		if(props.getProperty("ftps.port") != null){
			port = Integer.parseInt(props.getProperty("ftps.port"));
		}
		
		ftp.connect(url);
        ftp.enterLocalPassiveMode();
       
        ftp.login(user, pass);
        System.out.println("sync.machines222=" + url);  
		return ftp;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		FTPSSync sync = new FTPSSync();		
		
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
	private String pass;
	private String passx;
}
