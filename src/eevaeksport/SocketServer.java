package eevaeksport;

import java.io.IOException;
import java.net.ServerSocket;


/*
 * Created on 30.11.2008
 */

public class SocketServer implements Runnable {

	
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
