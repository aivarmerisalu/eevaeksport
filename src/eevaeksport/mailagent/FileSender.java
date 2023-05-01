package eevaeksport.mailagent;

import java.io.File;


import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import eevaeksport.Email;
import eevaeksport.util.Converter;



public class FileSender {

	/**
	 * @param args
	 */
	
	public static void main(String[] arg) throws Exception {
		
		PropertyConfigurator.configure("log4j.properties");
		 System.out.println("AWS " );
		FileSender fs = new FileSender();
		fs.start(arg);
	}
	
	static Category log = Category.getRoot();
	public boolean sendMaster = false;
	public boolean sendStock = false;
	public boolean getOrders = false;
	public boolean sendPicts = false;
	public boolean sendKeygen = false;
	
	 
	   boolean getAWS ( Properties mjProps) {
		 
		   String bucketName = mjProps.getProperty("aws.bucket");
		  
		   String catalogue = mjProps.getProperty("aws.getfiles");
		   String catalogueLOG = mjProps.getProperty("aws.getfilesLOG");
		   
		   String dirIn = mjProps.getProperty("aws.dirloc");
		   String dirInLOG = mjProps.getProperty("aws.dirlocLOG");
		  
	  	   String dirOutLoc = mjProps.getProperty("aws.direxploc");
	  	   String dirOut = mjProps.getProperty("aws.direxp");
	  	 
	  	   String dirInSup = mjProps.getProperty("aws.dirsup");
		   String dirOutSupLoc = mjProps.getProperty("aws.dirsupout");
	
		   String dirInCash = mjProps.getProperty("aws.dircash");
		   String dirOutCashLoc = mjProps.getProperty("aws.dircashout");
		   String dirOutCashLoc2 = mjProps.getProperty("aws.dircashout2");
	
	  	 
		   String dirPictLoc = mjProps.getProperty("aws.dirpictloc");
		  	 String dirPict = mjProps.getProperty("aws.dirpict");
		   
	       String stringObjKeyName =  catalogue ;
	       String stringObjKeyNameLOG =  catalogueLOG ;
	       
	       System.out.println("AWS " + dirOutLoc + " " + dirOut + " " + getOrders + " " + sendMaster + " " + sendStock);
	      
	       try {
	 
	    	   AWSCredentials credentials = new BasicAWSCredentials(
	    			   mjProps.getProperty("aws.key"), //"AKIARKYW3NNC"
	    			   mjProps.getProperty("aws.secret")//"Oz9mSR0kMQrmMr+H4PV"
	    			 );
	    			
	   		   AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
	    			   .withCredentials(new AWSStaticCredentialsProvider(credentials))
	    			   .withRegion(Regions.EU_NORTH_1)
	    			   .build();
	   		Date datee = new Date();
	   		datee.setTime(Calendar.getInstance().getTimeInMillis());
	   	   String time = datee.toString();
	   	   
	   	   
	   	   
	   	   if(this.sendKeygen) {
	   		   String currentWorkingDirectory = Paths.get(".").toAbsolutePath().normalize().toString();
		        String sourceDirectoryPath = currentWorkingDirectory + "/toweb/keygen";
		        String targetDirectoryPath = currentWorkingDirectory + "/toweb/keygen/done";
		        File sourceDirectory = new File(sourceDirectoryPath);
		        File targetDirectory = new File(targetDirectoryPath);
	   		   
	   		   
		       	
		       	//loome /toweb/done kausta
			    File logDir = new File(currentWorkingDirectory + "/toweb/keygen/done");
		        if (!logDir.exists()) {
		            if (logDir.mkdir()) {
		                System.out.println("The /toweb/keygen/done directory has been created.");
		            } else {
		                System.out.println("Failed to create the /toweb/keygen/done directory.");
		            }
		        }
	   		   
		        

	        	 System.out.println(time + " Sees " + new File(sourceDirectoryPath).listFiles() + " " + sourceDirectoryPath);
	    	        for (File f : new File(sourceDirectoryPath).listFiles())  {
	    	        	 System.out.println(time + " Sees " + f.getName());
	    	        	try {

	    	                // Upload a text string as a new object.
	    	               // s3Client.putObject(bucketName, dirOut, "Uploaded String Object");

	    	                // Upload a file as a new object with ContentType and title specified.
	    	        		if(f.getName().contains("_pincode.txt")) {
	    	        		
	    	                PutObjectRequest request = new PutObjectRequest(bucketName, dirOut + "pincode/" + f.getName(), f);
	    	                ObjectMetadata metadata = new ObjectMetadata();
	    	                metadata.setContentType("plain/text");
	    	                metadata.addUserMetadata("title", "someTitle");
	    	                request.setMetadata(metadata);
	    	                s3Client.putObject(request);
	    	                log(time + " Uploaded file to AWS : " + f.getName());
		    	            System.out.println(time + " Uploading " + f.getName());
		    	        	
	    	        		}
		    	           
	    	            } catch (AmazonServiceException e) {
	    	                // The call was transmitted successfully, but Amazon S3 couldn't process 
	    	                // it, so it returned an error response.
	    	                e.printStackTrace();
	    	            } catch (SdkClientException e) {
	    	                // Amazon S3 couldn't be contacted for a response, or the client
	    	                // couldn't parse the response from Amazon S3.
	    	                e.printStackTrace();
	    	            }
	    	        }
	    	      
		        
		        
		        
		        //failide kopimine

		        File[] files = sourceDirectory.listFiles();
		        for (File file : files) {
		            if (file.isFile() && file.getName().endsWith(".txt")) {
		                try {
		                    Path sourceFilePath = Paths.get(file.getAbsolutePath());
		                    Path targetFilePath = Paths.get(targetDirectoryPath + "/" + file.getName());
		                    Files.move(sourceFilePath, targetFilePath);
		                    System.out.println("Moved file: " + sourceFilePath + " to " + targetFilePath);
		                } catch (IOException e) {
		                    System.out.println("An error occurred while moving the file: " + file.getAbsolutePath());
		                    e.printStackTrace();
		                }
		            }
		        }
	   		   
		        
		        
		        
		        
		        
	   	   }
	   	   
	   	   
	        if(this.getOrders) {
	            ObjectListing objects = s3Client.listObjects(bucketName, stringObjKeyName);
	            log(time + " Starting session download file from AWS" );
	    	        for (S3ObjectSummary objectSummary : objects.getObjectSummaries())  {
	    	        	 if(!objectSummary.getKey().equals(stringObjKeyName)) {
	    	            String keyy = objectSummary.getKey();
	    	        		 String filen = dirIn + keyy.substring(keyy.lastIndexOf("/")+1);
	    	            FileUtils.copyInputStreamToFile(s3Client.getObject(bucketName, keyy)
	    	            		.getObjectContent(), new File(filen));
	    	            
	    	            log(time + " Downloaded file from AWS : " + filen);
	    	            System.out.println(time + " downing " + filen);
	    	            
	    	            s3Client.deleteObject(bucketName, objectSummary.getKey() );
	    	            datee.setTime(Calendar.getInstance().getTimeInMillis());
	    	       	    time = datee.toString();
	    	            log(time + " Deleting file from AWS : " + objectSummary.getKey());
	    	            System.out.println(time + " Deleting " + objectSummary.getKey());
	    	            }	 
		           }  
	    	        
	    	        if(catalogueLOG != null ) {
	    	        	objects = s3Client.listObjects(bucketName, stringObjKeyNameLOG);
	    	        
	    	        	for (S3ObjectSummary objectSummary : objects.getObjectSummaries())  {
		    	        	 if(!objectSummary.getKey().equals(stringObjKeyNameLOG)) {
		    	            String keyy = objectSummary.getKey();
		    	        		 String filen = dirInLOG + keyy.substring(keyy.lastIndexOf("/")+1);
		    	            FileUtils.copyInputStreamToFile(s3Client.getObject(bucketName, keyy)
		    	            		.getObjectContent(), new File(filen));
		    	            
		    	            log(time + " Downloaded LOG file from AWS : " + filen);
		    	            System.out.println(time + " downing " + filen);
		    	            
		    	            s3Client.deleteObject(bucketName, objectSummary.getKey() );
		    	            datee.setTime(Calendar.getInstance().getTimeInMillis());
		    	       	    time = datee.toString();
		    	            log(time + " Deleting LOG file from AWS : " + objectSummary.getKey());
		    	            System.out.println(time + " Deleting " + objectSummary.getKey());
		    	            }	 
	    	        }
	    	        }  
	    	        
	    	      //loeme Supervisorid alla   
	    	        if(dirInSup != null ) {
	    	        	objects = s3Client.listObjects(bucketName, dirInSup);
	    	        
	    	        	for (S3ObjectSummary objectSummary : objects.getObjectSummaries())  {
		    	        	 if(!objectSummary.getKey().equals(dirInSup)) {
		    	            String keyy = objectSummary.getKey();
		    	        		 String filen = dirOutSupLoc + keyy.substring(keyy.lastIndexOf("/")+1);
		    	            FileUtils.copyInputStreamToFile(s3Client.getObject(bucketName, keyy)
		    	            		.getObjectContent(), new File(filen));
		    	            
		    	            log(time + " Downloaded Supervisor file from AWS : " + filen);
		    	            System.out.println(time + " downing " + filen);
		    	            
		    	            s3Client.deleteObject(bucketName, objectSummary.getKey() );
		    	            datee.setTime(Calendar.getInstance().getTimeInMillis());
		    	       	    time = datee.toString();
		    	            log(time + " Deleting Supervisor file from AWS : " + objectSummary.getKey());
		    	            System.out.println(time + " Deleting " + objectSummary.getKey());
		    	            }	 
	    	        }
	    	        }
	    	        
	    	        //loeme sisse sularaha info 
	    	        if(dirInCash != null ) {
	    	        	objects = s3Client.listObjects(bucketName, dirInCash);
	    	        
	    	        	for (S3ObjectSummary objectSummary : objects.getObjectSummaries())  {
		    	        	 if(!objectSummary.getKey().equals(dirInCash)) {
		    	            String keyy = objectSummary.getKey();
		    	        		 String filen = dirOutCashLoc + keyy.substring(keyy.lastIndexOf("/")+1).replaceAll("\n", " " );
		    	            FileUtils.copyInputStreamToFile(s3Client.getObject(bucketName, keyy)
		    	            		.getObjectContent(), new File(filen));
		    	            if(dirOutCashLoc2 != null) {
		    	            	String filen2 = dirOutCashLoc2 + keyy.substring(keyy.lastIndexOf("/")+1).replaceAll("\n", " " );
		    	            	Converter.copyFile(new File(filen), new File(filen2));
		    	            }
		    	            
		    	            log(time + " Downloaded Cash file from AWS : " + filen);
		    	            System.out.println(time + " downing " + filen);
		    	            
		    	            s3Client.deleteObject(bucketName, objectSummary.getKey() );
		    	            datee.setTime(Calendar.getInstance().getTimeInMillis());
		    	       	    time = datee.toString();
		    	            log(time + " Deleting Cash file from AWS : " + objectSummary.getKey());
		    	            System.out.println(time + " Deleting " + objectSummary.getKey());
		    	            }	 
	    	        }
	    	        }
	    	        log(time + " Ending session download file from AWS "  );
	          }
	          
	        
	        if(this.sendMaster) {
	        	 System.out.println(time + " Sees " + new File(dirOutLoc).listFiles() + " " + dirOutLoc);
	    	        for (File f : new File(dirOutLoc).listFiles())  {
	    	        	 System.out.println(time + " Sees " + f.getName());
	    	        	try {

	    	                // Upload a text string as a new object.
	    	               // s3Client.putObject(bucketName, dirOut, "Uploaded String Object");

	    	                // Upload a file as a new object with ContentType and title specified.
	    	        		if(f.getName().contains("zip")) {
	    	        		
	    	                PutObjectRequest request = new PutObjectRequest(bucketName, dirOut + f.getName(), f);
	    	                ObjectMetadata metadata = new ObjectMetadata();
	    	                metadata.setContentType("plain/text");
	    	                metadata.addUserMetadata("title", "someTitle");
	    	                request.setMetadata(metadata);
	    	                s3Client.putObject(request);
	    	                log(time + " Uploaded file to AWS : " + f.getName());
		    	            System.out.println(time + " Uploading " + f.getName());
		    	        	
	    	        		}
		    	           
	    	            } catch (AmazonServiceException e) {
	    	                // The call was transmitted successfully, but Amazon S3 couldn't process 
	    	                // it, so it returned an error response.
	    	                e.printStackTrace();
	    	            } catch (SdkClientException e) {
	    	                // Amazon S3 couldn't be contacted for a response, or the client
	    	                // couldn't parse the response from Amazon S3.
	    	                e.printStackTrace();
	    	            }
	    	        }
	    	     }    
	        if(this.sendStock) {
	        	 System.out.println(time + " Sees Stock " + new File(dirOutLoc).listFiles() + " " + dirOutLoc);
	    	        for (File f : new File(dirOutLoc).listFiles())  {
	    	        	 System.out.println(time + " Sees " + f.getName());
	    	        	try {

	    	                // Upload a text string as a new object.
	    	               // s3Client.putObject(bucketName, dirOut, "Uploaded String Object");

	    	                // Upload a file as a new object with ContentType and title specified.
	    	        		if(f.getName().contains("stockref") || f.getName().contains("customerorders") || f.getName().contains("prodactions")
	    	        				|| f.getName().contains("debtref") ||  f.getName().startsWith("st")) {
	    	        			  PutObjectRequest request = new PutObjectRequest(bucketName, dirOut + f.getName(), f);
	  	    	                ObjectMetadata metadata = new ObjectMetadata();
	  	    	                metadata.setContentType("plain/text");
	  	    	                metadata.addUserMetadata("title", "someTitle");
	  	    	                request.setMetadata(metadata);
	  	    	                s3Client.putObject(request);
	  	    	                log(time + " Uploaded stock file to AWS : " + f.getName());
	  		    	            System.out.println(time + " Uploading " + f.getName());	
	    	        		}
	    	              
		    	           
	    	            } catch (AmazonServiceException e) {
	    	                // The call was transmitted successfully, but Amazon S3 couldn't process 
	    	                // it, so it returned an error response.
	    	                e.printStackTrace();
	    	            } catch (SdkClientException e) {
	    	                // Amazon S3 couldn't be contacted for a response, or the client
	    	                // couldn't parse the response from Amazon S3.
	    	                e.printStackTrace();
	    	            }
	    	        }
	    	     }  
	        
	        
	        if(this.sendPicts) {
	        	 System.out.println(time + " Sees Picts " + new File(dirPictLoc).listFiles() + " " + dirOutLoc);
	        	  TreeMap<String,Long> sizes = new TreeMap<String,Long>();
	        	 try {
	        		if("true".equals(mjProps.getProperty("aws.nopictlist"))) {
	        			
	        		}else {
	        			 ObjectListing   objectListing = s3Client.listObjects(bucketName, dirPict);
		    	          //  System.out.println(time + " Sees " + f.getName() + " " + objectListing.getObjectSummaries().toString());
		    	            for (S3ObjectSummary objectSummary :    objectListing.getObjectSummaries()) {
		    	             // System.out.println( " - " + objectSummary.getKey() + "  " +
		    	              //    "(size = " + objectSummary.getSize() +  ")");
		    	               sizes.put(objectSummary.getKey(), objectSummary.getSize());
		    	            }	
	        		}
	        		
	        	 }catch(Exception e) {
	        		 
	        	 }
	            
	    	            
	    	        for (File f : new File(dirPictLoc).listFiles())  {
	    	        	
	    	        	try {

	    	        			  PutObjectRequest request = new PutObjectRequest(bucketName, dirPict + f.getName(), f);
	  	    	                ObjectMetadata metadata = new ObjectMetadata();
	  	    	                metadata.setContentType("plain/text");
	  	    	                metadata.addUserMetadata("title", "someTitle");
	  	    	                request.setMetadata(metadata);
	  	    	              
	  	    	                
	  	    	         
	  	    	            
	  	    	            //    System.exit(0);
	  	    	                
	  	    	             if ( sizes.get(dirPict + f.getName()) == null || f.getName().endsWith("txt")) {
	  	    	            	 s3Client.putObject(request);
		  	    	                log(time + " Uploaded PIcture file to AWS : " + f.getName());
		  		    	            System.out.println(time + " Uploading " + f.getName());	
	  	    	             } else if(sizes.get( dirPict + f.getName()) != f.length()){
	  	    	            	 s3Client.putObject(request);
	  	    	            	 System.out.println(time + " Pilt on uue suurusega " + f.getName() + " W" + sizes.get( dirPict + f.getName())+ " L" + f.length() );	
	  	    	             } else {
	  	    	            	 System.out.println(time + " Pilt olemas ei lae " + f.getName());	
	  	    	             }
	  	    	               
	    	        		
	    	              
		    	           
	    	            } catch (AmazonServiceException e) {
	    	                // The call was transmitted successfully, but Amazon S3 couldn't process 
	    	                // it, so it returned an error response.
	    	                e.printStackTrace();
	    	            } catch (SdkClientException e) {
	    	                // Amazon S3 couldn't be contacted for a response, or the client
	    	                // couldn't parse the response from Amazon S3.
	    	                e.printStackTrace();
	    	            }
	    	        }
	    	     }
	        
	           return true;
	           
	       } catch (Exception e) {
	           e.printStackTrace();
	           return false;
	       } 
	       
	       
	  } 
	   
	   public void log(String s) {
		     log.error(s);
		   }
	
		public void uploadKeygen() throws Exception{
			sendKeygen = true;
			
			Properties props = new Properties();
			File file = new File("eksport.ini");
			if (file.exists()) {
				FileInputStream in = new FileInputStream(file);
					props = new Properties();
				props.load(in);
				in.close();
			}
			getAWS(props);
			
			
			
			//in.close();
			
			
		}
	   
	public void start(String[] args) throws Exception{
		if(args != null) {
		
		 for (int i = 0 ; i < args.length ; i++){
		      if (args[i].trim().equalsIgnoreCase("/master")){
		        this.sendMaster = true;
		      }
		      if (args[i].trim().equalsIgnoreCase("/stock")){
			        this.sendStock = true;
			      }
		      if (args[i].trim().equalsIgnoreCase("/orders")){
			        this.getOrders = true;
			      }
		      if (args[i].trim().equalsIgnoreCase("/picts")){
			        this.sendPicts = true;
			      }
		      }
		  } 
		
		Properties props = new Properties();
		File file = new File("eksport.ini");
		if (file.exists()) {
			FileInputStream in = new FileInputStream(file);
				props = new Properties();
			
			props.load(in);
			in.close();
		}
		
		if(getOrders || sendMaster || sendStock || sendPicts ) {
			getAWS(props);
	        System.exit(0);
	    }
		
		
		
	  file = new File("filesender.properties");
		if (file.exists()) {
			FileInputStream in = new FileInputStream(file);
				props = new Properties();
			
			props.load(in);
			in.close();
		}
		
		
		String srcDir = props.getProperty("sourcedir");
		String arcdir = props.getProperty("archivedir");
		String sep = System.getProperty("file.separator");
		if (!(arcdir.endsWith("/") || arcdir.endsWith("\\"))){
			arcdir = arcdir + sep;
		}
		String toEmail = props.getProperty("filesemail");
		String fromEmail = props.getProperty("from");
		String smtp = props.getProperty("smtphost");
		String port = props.getProperty("smtpport");
		String user = props.getProperty("smtpuser");
		String pass = props.getProperty("smtppass");
		System.out.println("STARTING ");
		
		File dir = new File(srcDir);
		if (dir.isDirectory()){
			File destDir = new File(arcdir);
			if (!destDir.isDirectory()) throw new Exception(destDir + " is not a directory");
			File[] files = dir.listFiles();
			for (int i = 0 ; i < files.length ; i++){
				File f = files[i];
				if (f.isFile()){
					Email email = new Email(null);
					email.setFrom(fromEmail);
					email.setFilesTo(toEmail);
					email.addAttach(f.getAbsolutePath());
					email.setSubjectFilesTo("Mobilejava Filesender " + f.getName());
					email.setBody(f.getName());
					email.setSMTP(smtp);
					if(port != null) {
						email.setPort(port);
					}
					if(user != null) {
						email.setUser(user);
					}
					if(user != null) {
						email.setPass(pass);
					}
					
					System.out.println("sending " + f.getName());
					try {
						email.send();
						f.renameTo(new File(arcdir + f.getName()));	
						System.out.println("Saadetud ja renametud " + arcdir + f.getName());
						
					} catch (Exception e){
						e.printStackTrace();
					}
				}
			}			
		} else throw new Exception(srcDir + " is not a directory");
		System.out.println("Program Closed ");
	}
	
}
