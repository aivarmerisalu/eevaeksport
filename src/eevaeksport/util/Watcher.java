package eevaeksport.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Properties;

import eevaeksport.EksportFrame;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyListener;



public class Watcher {
	
	private static Properties props = null;

	public String param1 = null;
	public String param2 = null;
	public String param3 = null;
	public String param4 = null;
	public String param5 = null;
	public String param6 = null;
	public String printport = null;
	
	  public void watch() throws Exception {
		   props = new Properties();
		    props.load(new FileInputStream(EksportFrame.INI_FILE));
		    String file = props.getProperty("watcher");
		    printport = props.getProperty("printport");
		    param1 = props.getProperty("printer1");
		    param2 = props.getProperty("printer2");
		    param3 = props.getProperty("printer3");
		    param4 = props.getProperty("printer4");
		    param5 = props.getProperty("printer5");
		    param6 = props.getProperty("printer6");
		 	  System.out.println("Watcher t��tab");
		
		  System.out.println("Watching for " + file);
		  
		  File f = new File(file);
		  if(f.isDirectory()){
			  exit=false;
			  String[] ai = f.list();
			  for(int i = 0;i<ai.length;i++){
				 
				  String name = ai[i];
				  System.out.println(name);
				  if(name.indexOf("PRINT1") != -1 || name.indexOf("PRINT2") != -1 
						  || name.indexOf("PRINT3") != -1 || name.indexOf("PRINT4") != -1 
						  || name.indexOf("PRINT5") != -1 || name.indexOf("PRINT6") != -1 ){
					  callPrinting(file + name);
				  }
				  if(name.indexOf("BLOCK") != -1  ){
					  callBlocking(file + name);
				  }
				  
			  }
			  exit=true;
		  }
		  // watch mask, specify events you care about,
		  // or JNotify.FILE_ANY for all events.
		  int mask = JNotify.FILE_CREATED ;//  |
		//  JNotify.FILE_DELETED  |   
		//  JNotify.FILE_MODIFIED |     
		// JNotify.FILE_RENAMED;
		  // watch subtree?
		  boolean watchSubtree = false;    
		  // add actual watch    
		  int watchID = JNotify.addWatch(file, mask, watchSubtree, new Listener());
		  // sleep a little, the application will exit if you 
		  // don't (watching is asynchronous), depending on your 
		  
		  // application, this may not be required  
		  
		  //Thread.sleep(57000); 
		
		  // to remove watch the watch
		  tryQuit();
	  
	  }
	  
	  private boolean exit = true;
	  
	  private void  tryQuit(){
		  while (exit){
			  try {
				Thread.sleep(500);
				  System.exit(0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		
		  }
	  }
	  private void sentToPrinter( String param, String fileName) throws Exception {
			
			try{
				System.out.println("kustutame olemas oleva mapingu" + printport );
				
				  String[] cmd1 = {"cmd", "/C", "NET USE " + printport + ": /DELETE"  };	  
				  Runtime rt1 = Runtime.getRuntime();
				  Process p2 = rt1.exec(cmd1);
				    BufferedReader stdInput = new BufferedReader(new
				    		InputStreamReader(p2.getInputStream())); 
				    BufferedReader stdError = new BufferedReader(new
				    		InputStreamReader(p2.getErrorStream()));  
				    // read the output from the command  
				    String s = null;     
				    System.out.println("Here is the standard output of the command:\n");
				    while ((s = stdInput.readLine()) != null) { 
				    	System.out.println(s);          }          
				    // read any errors from the attempted command 
				    System.out.println("Here is the standard error of the command (if any):\n");
				    while ((s = stdError.readLine()) != null) { 
				    	System.out.println(s);          }
				    
				    
				  p2.waitFor();
				  while ( p2.exitValue() != 2 &&  p2.exitValue() != 0){
					 // System.out.println("asi" + p2.exitValue());
				  }
			}catch(Exception e){
				e.printStackTrace();
			}
		  try{
				System.out.println("param " + param + " filename "+ fileName);
			
				  String[] cmd1 = {"cmd", "/C", "NET USE " + printport + ": " + param  };	  
				  Runtime rt1 = Runtime.getRuntime();
				  Process p2 = rt1.exec(cmd1);
				    BufferedReader stdInput = new BufferedReader(new
				    		InputStreamReader(p2.getInputStream())); 
				    BufferedReader stdError = new BufferedReader(new
				    		InputStreamReader(p2.getErrorStream()));  
				    // read the output from the command  
				    String s = null;     
				    System.out.println("Here is the standard output of the command:\n");
				    while ((s = stdInput.readLine()) != null) { 
				    	System.out.println(s);          }          
				    // read any errors from the attempted command 
				    System.out.println("Here is the standard error of the command (if any):\n");
				    while ((s = stdError.readLine()) != null) { 
				    	System.out.println(s);          }
				    
				    
				  p2.waitFor();
				  while ( p2.exitValue() != 2 &&  p2.exitValue() != 0){
					 // System.out.println("asi" + p2.exitValue());
				  }
			}catch(Exception e){
				e.printStackTrace();
			}
			
		
			
				  System.out.println("l�heme printima "  + fileName);
				  String[] cmd = {"cmd", "/C", "copy " +fileName +" " + printport   };	  
				  Runtime rt = Runtime.getRuntime();
				  Process p = rt.exec(cmd);
				    BufferedReader stdInput = new BufferedReader(new
				    		InputStreamReader(p.getInputStream())); 
				    BufferedReader stdError = new BufferedReader(new
				    		InputStreamReader(p.getErrorStream()));  
				    // read the output from the command  
				    String s = null;     
				    System.out.println("Here is the standard output of the command:\n");
				    while ((s = stdInput.readLine()) != null) { 
				    	System.out.println(s);          }          
				    // read any errors from the attempted command 
				    System.out.println("Here is the standard error of the command (if any):\n");
				    while ((s = stdError.readLine()) != null) { 
				    	System.out.println(s);          }
				    
				    
				  p.waitFor();
				  while (p.exitValue() != 0 ){
					//  System.out.println("asi" + p.exitValue());
				  }
				  File f = new File(fileName);
				  if(f.exists()){
					  File uus = new File(props.getProperty("watcher.rename") + File.separator  + f.getName());
					  
					  if(uus.exists()){
						  uus.delete();
					  }
					  f.renameTo(uus);
					//  f.delete();
					  System.out.println("Renamed " + uus.getAbsolutePath());
				  }
				  
	  }
	  
	  
		public void writeConsloStr(String path,String e) {
			try {
				PrintWriter os = new PrintWriter(new FileWriter(
						path, true));
				os.write(e);
				os.close();
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}

	}
	  
	  private void callBlocking(String fileName) throws Exception{
		  File uus = new File(fileName);
		  
		  String input = "";

		  
		   FileReader fr = new FileReader(fileName);
		    BufferedReader reader = new BufferedReader(fr);
		    String line;
		    
		    while((line = reader.readLine()) != null){
		    	input = line;
		    }
		    reader.close();
		    fr.close();
		
		    File old = new File(props.getProperty("watcher.block"));
		    long nyyd = Calendar.getInstance().getTimeInMillis() - 25200000;//7 tundi vana siis kustutame
		    if(old.exists() && old.lastModified() < nyyd){
		    	old.delete();
		    }
		    
			  writeConsloStr(props.getProperty("watcher.block"), input + "\n" );
	  
		  
		  File f = new File(fileName);
		  if(f.exists()){
			  File uus2 = new File(props.getProperty("watcher.rename") + File.separator  + f.getName());
			
			  if(uus2.exists()){
				  uus2.delete();
			  }
			  f.renameTo(uus2);
			
			  System.out.println("Renamed " + uus.getAbsolutePath());
		  }
		  
}
	  
	  
	  private void callPrinting(String fileName) throws Exception{
		 
		  if(fileName.indexOf("PRINT1") != -1){
	          sentToPrinter(param1,fileName);
		  } else if(fileName.indexOf("PRINT2") != -1){
			  sentToPrinter(param2,fileName);  
		  } else if(fileName.indexOf("PRINT3") != -1){
			  sentToPrinter(param3,fileName);  
		  } else if(fileName.indexOf("PRINT4") != -1){
			  sentToPrinter(param4,fileName);  
		  } else if(fileName.indexOf("PRINT5") != -1){
			  sentToPrinter(param5,fileName);  
		  } else if(fileName.indexOf("PRINT6") != -1){
			  sentToPrinter(param6,fileName);  
		  }
		  
		  try {
			  File uus = new File(props.getProperty("watcher.rename"));
			  if(uus.isDirectory()){
				  long tana = Calendar.getInstance().getTimeInMillis() - 259200000 ;
				  File[] files = uus.listFiles();
				  for(int i = 0 ; i < files.length; i++){
					  File f = files[i];
					  if (f.lastModified() < tana){
						  f.delete();
					  }
				  }
			  }
		  }catch(Exception ee ){
			  
		  }
		  
	  }
	
	  class Listener implements JNotifyListener {   
		  public void fileRenamed(int wd, String rootPath, String oldName,
				  String newName) {      print("renamed " + rootPath + " : " + oldName + " -> " + newName);    }  
		  public void fileModified(int wd, String rootPath, String name) {  
			  print("modified " + rootPath + " : " + name);    }
		  public void fileDeleted(int wd, String rootPath, String name) {
			  print("deleted " + rootPath + " : " + name);    }
		  public void fileCreated(int wd, String rootPath, String name) {
			  print("created " + rootPath + " : " + name); 
			  exit=false;
			  try{
				  callPrinting(rootPath + name); 
			  }catch(Exception e){
				  e.printStackTrace();
			  }
			  try{
			  if(name.indexOf("BLOCK") != -1  ){
				  callBlocking(rootPath + name);
			  }
		  }catch(Exception e){
			  e.printStackTrace();
		  }
		  exit=true;
		  }
		  void print(String msg) {  
			  System.err.println(msg); } 
		  
		  private void callPrinting(String fileName) throws Exception{
			  Thread.sleep(2000);
			  if(fileName.indexOf("PRINT1") != -1){
		          sentToPrinter(param1,fileName);
			  } else if(fileName.indexOf("PRINT2") != -1){
				  sentToPrinter(param2,fileName);  
			  } else if(fileName.indexOf("PRINT3") != -1){
				  sentToPrinter(param3,fileName);  
			  } else if(fileName.indexOf("PRINT4") != -1){
				  sentToPrinter(param4,fileName);  
			  } else if(fileName.indexOf("PRINT5") != -1){
				  sentToPrinter(param5,fileName);  
			  } else if(fileName.indexOf("PRINT6") != -1){
				  sentToPrinter(param6,fileName);  
			  }
			  
			  try {
				  File uus = new File(props.getProperty("watcher.rename"));
				  if(uus.isDirectory()){
					  long tana = Calendar.getInstance().getTimeInMillis() - 259200000 ;
					  File[] files = uus.listFiles();
					  for(int i = 0 ; i < files.length; i++){
						  File f = files[i];
						  if (f.lastModified() < tana){
							  f.delete();
						  }
					  }
				  }
			  }catch(Exception ee ){
				  
			  }
		  }
		  
		  }
		
     }
