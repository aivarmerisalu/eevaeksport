/*

 * Created on 19.11.2008
 */
package ftpsync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;

public class FTPSSync3 {

	public FTPSSync3(){
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

				SftpClient client = connect();

				if (remDirResults != null) {
					client.cd(remDirResults);
				}
              
				List<SftpFile> aa = client.ls();
				for (SftpFile sf : aa) {
					if (sf.isFile()) {
						System.out.println(sf.getFilename());
						FileOutputStream oos = new FileOutputStream(
								locDirResults + sf.getFilename());
						client.get(sf.getFilename(), oos);
						oos.close();
						client.rename(sf.getFilename(),
								remDirRename + sf.getFilename());
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
					.getProperty("sync.remote.dir.upload.rename");

			SftpClient client = connect();

			if (remDirResults != null) {
				client.cd(remDirResults);
			}
			File list = new File(locDirResults);
			for (String sf : list.list()) {
				File f = new File(locDirResults + sf);
				if (f.isFile()) {
					FileInputStream iis = new FileInputStream(locDirResults
							+ sf);
					client.put(iis, sf);
					iis.close();
					client.rename(sf, remDirRename + sf);

					if (f.exists()) {
						f.renameTo(new File(locDirRename + sf));
					}
					if (f.exists()) {
						f.delete();
					}

				}

			}
			client.quit();
		} catch (Exception ee) {
			ee.printStackTrace();
		}

	}
	
	private SftpClient connect() throws IOException{
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
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		FTPSSync3 sync = new FTPSSync3();		
		
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
