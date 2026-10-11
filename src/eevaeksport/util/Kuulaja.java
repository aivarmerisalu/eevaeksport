package eevaeksport.util;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Kuulaja {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		if(checkRunningCopy()){
			Watcher ww = new Watcher();
			try {
				
				ww.watch();
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			System.out.println("Another instance of FileSystem listener is Active ...");
	  System.exit(0);
		}
		
	

	}
	
	
    
    public static  boolean checkRunningCopy(){
    
	boolean ok = false;
	try {
		 Socket sock = new Socket("localhost", 4443);
		   sock.close();
		} catch (IOException e){
			//e.printStackTrace();
		    ok = true;
		}
		
		 if ( ok ) {
				Runnable run = new SocketServer();
			   Thread th = new Thread(run);
				th.start();			    
		} 
   
	return ok;		
    }

}

 final class SocketServer implements Runnable {

	
	  public void run(){
		  ServerSocket server = null;
		  try {
			  server = new ServerSocket(4443);
			  while(!Thread.interrupted()){
				  server.accept();
				 
				  }
			  } catch (IOException io){
				  io.printStackTrace(); 
				  } 
			  }  

	
}