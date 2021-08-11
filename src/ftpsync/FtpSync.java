/*
 * Created on 19.11.2008
 */
package ftpsync;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;

import org.finj.FTPClient;

public class FtpSync {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		props = loadProps(INI_FILE); 
		 user = props.getProperty("sync.ftp.user");
		 pass = props.getProperty("sync.ftp.pass");
		 if(props.getProperty("sync.ftp.port") != null){
			 port = Integer.parseInt(props.getProperty("sync.ftp.port")); 
		 }
		
		
		
		
	if(props.getProperty("max.worktime")!= null){
		try{

			minutes = Integer.parseInt(props.getProperty("max.worktime"));	
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	     startEndThread();
		 
		 if(args[0].equals("DownAndDelete")){
			String urls = props.getProperty("sync.machines");
			StringTokenizer st = new StringTokenizer(urls,";");
			
			String pcDirResults = props.getProperty("sync.pc.dir.results");
			

			try{
				 if("true".equals(props.getProperty("loadingfile"))){
						FileOutputStream fos = new FileOutputStream(pcDirResults + "loading.txt");
						fos.close();
						System.out.println("Loadingfile = true");
					   }
			}catch(Exception e){
				
			}
			
			
			while (st.hasMoreElements()){
				counter++;
				startDownDeleteThread(st.nextToken());
			}
			
			  if("true".equals(props.getProperty("loadingfile"))){
		    	   File f = new File (pcDirResults + "loading.txt");
		    		if(f.exists()){
		    			f.delete();
		    		}
		    	   }
			
			
			//System.exit(0);
			}
			
			
			if(args[0].equals("GetRequestfile")){
				
				
				String urls = props.getProperty("sync.machines");
				StringTokenizer st = new StringTokenizer(urls,";");
				
				String pdaDirData = props.getProperty("sync.pda.dir.data");			
				String pcDirRoot = props.getProperty("sync.pc.dir.data");
				String pdaRequestFile = props.getProperty("sync.pda.request.file");
				
				System.out.println(pdaDirData);
				System.out.println(pcDirRoot);
				System.out.println(pdaRequestFile);
				
				while (st.hasMoreElements()){
					String ip = st.nextToken();
					
					try {
					FTPClient ftp = new FTPClient(ip,port);
					if(user != null && pass != null ){
						ftp.login(user, pass.toCharArray());
					}else{
						ftp.login("anonymous", "0000".toCharArray());
					}
					
					
					
				if(pdaDirData != null){
					ftp.setWorkingDirectory(pdaDirData);
				}
				 String[] aa =  ftp.getFileNames(pdaRequestFile);
				 if(aa != null && aa.length>0){
					 FileOutputStream oos = new FileOutputStream(pcDirRoot + pdaRequestFile);
						ftp.getFile(oos, pdaRequestFile);
						oos.close();
						ftp.deleteFile(pdaRequestFile);			
				        ftp.close();
				 }
					}catch(Exception ee){
						ee.printStackTrace();
					}	
				}
				System.exit(0);
				}
					
			
			
			
			if(args[0].equals("UpData")){
				
				
				String urls = props.getProperty("sync.machines");
				StringTokenizer st = new StringTokenizer(urls,";");
				
				while (st.hasMoreElements()){
					 counter++;
					 startUpDataThread(st.nextToken());
				}
				}
			
			
			if(args[0].equals("UploadDeleteZip")){
				
				
				String urls = props.getProperty("sync.machines");
				StringTokenizer st = new StringTokenizer(urls,";");
				String pcDirData = props.getProperty("sync.pc.dir.data");
				String pcDataFile = props.getProperty("sync.pc.datazip.file");
				
				while (st.hasMoreElements()){
					counter++;
					startUploadZIpThread(st.nextToken());	
				}			
				File f = new File (pcDirData + pcDataFile);
				startFileDeleteThread(f);			
				}
			

			startExitThread();
		
		
		
		// TODO Auto-generated method stub

	}

	
	private static void startUploadZIpThread(final String ips) {
		Runnable rf = new Runnable() {
			public void run() {
				
					System.out.println("IP startUploadZIpThread:" +  ips);
					try{
						FTPClient ftp = new FTPClient(ips,port);
						if(user != null && pass != null ){
							ftp.login(user, pass.toCharArray());
						}else{
							ftp.login("anonymous", "0000".toCharArray());
						}
						String pdaDirData = props.getProperty("sync.pda.dir.data");
						String pcDirData = props.getProperty("sync.pc.dir.data");
						String pcDataFile = props.getProperty("sync.pc.datazip.file");
					if(pdaDirData != null){
						ftp.setWorkingDirectory(pdaDirData);
					}
				
						FileInputStream iis = new FileInputStream(pcDirData + pcDataFile );
						ftp.putFile(iis, pcDataFile,true);
						iis.close();
				       ftp.close();
				       
				   	

						counter = counter -1;
					}catch(Exception ee){
						counter = counter -1;
						ee.printStackTrace();
					}
					
					
			}
		};
	 Thread thr = new Thread(rf);
		thr.start();
	}
	
	
	
	
	private static void startUpDataThread(final String ips) {
		Runnable rf = new Runnable() {
			public void run() {
				
					System.out.println("IP UpData:" +  ips);
					try{

						String pdaDirData = props.getProperty("sync.pda.dir.data");
						String pcDirData = props.getProperty("sync.pc.dir.data");
						String pcDataFile = props.getProperty("sync.pc.data.file");
						
						FTPClient ftp = new FTPClient(ips);
						if(user != null && pass != null ){
							ftp.login(user, pass.toCharArray());
						}else{
							
							ftp.login("anonymous", "0000".toCharArray());
						}
						
					if(pdaDirData != null){
						ftp.setWorkingDirectory(pdaDirData);
					}
				
						FileInputStream iis = new FileInputStream(pcDirData + pcDataFile );
						ftp.putFile(iis, pcDataFile);
						iis.close();				
				       ftp.close();
				       counter = counter - 1;
					}catch(Exception ee){
					       counter = counter - 1;
						ee.printStackTrace();
					}
					
			}
		};
	 Thread thr = new Thread(rf);
		thr.start();
	}
	
	
	
	
	
	private static void startEndThread() {
		Runnable rf = new Runnable() {
			public void run() {
				try {
					
					while(true){

						Thread.sleep(minutes*60000);
						System.out.println("end Plingib " + counter);
				       System.exit(0);
					
					}	
					
				}catch (InterruptedException e) {}					
			}
		};
	 Thread thr = new Thread(rf);
		thr.start();
	}
	
	
	
	
	private static void startDownDeleteThread(final String ips) {
		
		Runnable rf = new Runnable() {
			public void run() {
				
				try {
					System.out.println("IP DownDelete:" +  ips + " " + port);
						FTPClient ftp = new FTPClient(ips,port);
					
						String pdaDirResults = props.getProperty("sync.pda.dir.results");
						String pdaDirResults2 = props.getProperty("sync.pda.dir.results2");
						String pcDirResults = props.getProperty("sync.pc.dir.results");
						String pdaDirRename = props.getProperty("sync.pda.dir.rename");
						

						if(user != null && pass != null ){
							ftp.login(user, pass.toCharArray());
						}else{
							ftp.login("anonymous", "0000".toCharArray());
						}
						
						System.out.println("IP DownDelete:" +  ips + " " + port);
					if(pdaDirResults != null){
						ftp.setWorkingDirectory(pdaDirResults);
						//ftp.cd("My Documents/Mobile/history");
					}
				
					System.out.println("eeee " + ftp.getWorkingDirectory());
					String [] aa = ftp.getFileNames(true);
					for(int i =0; aa.length >i ;i++){
						System.out.println( i + " " + aa[i]);
					
						try{
							FileOutputStream oos = new FileOutputStream(pcDirResults + aa[i]);
						ftp.getFile(oos, aa[i],true);
						oos.close();
						
							if(pdaDirRename != null){
								System.out.println(aa[i]);
								ftp.renameFile(aa[i],pdaDirRename  + aa[i]);	
							}
							
						}catch(Exception e){
							e.printStackTrace();
						}
						
						try{
						ftp.deleteFile(aa[i]);
					}catch(Exception e){
						e.printStackTrace();
					}
					}
					
					
					if(pdaDirResults2 != null){
						ftp.setWorkingDirectoryUp();
						ftp.setWorkingDirectoryUp();
						ftp.setWorkingDirectoryUp();
						ftp.setWorkingDirectory(pdaDirResults2); //teine kataloog et saaks mitmest kokku t�mmata asju
						//ftp.cd("My Documents/Mobile/history");
					}
					
					
					 aa = ftp.getFileNames(true);
					for(int i =0; aa.length >i ;i++){
						System.out.println( i + " " + aa[i]);
						FileOutputStream oos = new FileOutputStream(pcDirResults + aa[i]);
						ftp.getFile(oos, aa[i],true);
						oos.close();
						//ftp.renameFile(aa[i],pdaDirRename  + aa[i]);
						ftp.deleteFile(aa[i]);
					}
					
					
				       ftp.close();
				       counter = counter-1;
					}catch(Exception ee){
						 counter = counter-1;
						ee.printStackTrace();
					}		
			}
		};
	 Thread thr = new Thread(rf);
		thr.start();
	}
	
	
	
	
	
	private static  void startFileDeleteThread(final File f) {
		Runnable rf = new Runnable() {
			public void run() {
				try {
					
					while(true){

						Thread.sleep(500);
						System.out.println("DELETE Plingib " + counter);
					if (counter == 0) {
				       System.out.println("Counter" + counter);
				       if(f.exists()){
				    	   f.delete();
				       }
					}
					
					}	
					
				}catch (InterruptedException e) {
					e.printStackTrace();
				}					
			}
		};
	 Thread thr = new Thread(rf);
		thr.start();
	}
	
	
	private static void startExitThread() {
		Runnable rf = new Runnable() {
			public void run() {
				try {
					int aa = 0;
					
					String max = props.getProperty("sync.max.pling.time");
				     int ss = 0;
				     try {
				    	 if(max != null)
				    	 ss = Integer.parseInt(max);
				     }catch(Exception e){
				    	 
				     }
					while(true){
                     
						Thread.sleep(1500);
						System.out.println("EXIT Plingib " + counter);
					if (counter == 0) {
				       System.out.println("Counter" + counter);
				       System.exit(0);
					}
					
					if (counter != 1){
						aa = 0;
					}else{
						aa++;
					}
					
					if(max != null && aa == ss){
						 System.out.println("Hopeless case " + counter);
					       System.exit(0);
					}
					
					}	
					
				}catch (InterruptedException e) {}					
			}
		};
	 Thread thr = new Thread(rf);
		thr.start();
	}
	
	
	  public static Properties loadProps(String fileName){
		    Properties props = new Properties();
		    try {
		      FileInputStream in = new FileInputStream(fileName);
		      props.load(in);
		      in.close();
		    } catch (IOException e){
		        e.printStackTrace();
		      }
		    return props;
		  }
	
	  public static final String INI_FILE = "eksport.ini";
	  private static	Properties props = null;
	  private static String user = null;
	  private static String pass = null;
	  private static int port = 21;
	  private static int minutes = 5;
	  public static int counter = 0;
	  
}
