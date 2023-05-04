// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   KeyGenRunner.java

package eevaeksport.keygenjava;

import java.awt.*;

import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import eevaeksport.mailagent.FileSender;

// Referenced classes of package ee.mobilejava.keygen:
//            KeyGen

//töötav kataloog/log
//kell-kuupaev; süsteemi kasutaja; pin küsimine ;ja saamine
//vajutus:logifaili rida juurde
//vajutus: wd/toweb/keygen/pincode-timestamp.txt
public class KeyGenRunner extends Frame implements TextListener
{
	
    public KeyGenRunner()
    {
    	
    	String currentWorkingDirectory = Paths.get(".").toAbsolutePath().normalize().toString();
	    System.out.println("Current working directory: " + currentWorkingDirectory);
//loome /log kausta
	    File logDir = new File(currentWorkingDirectory + "/log");
        if (!logDir.exists()) {
            if (logDir.mkdir()) {
                System.out.println("The log directory has been created.");
            } else {
                System.out.println("Failed to create the log directory.");
            }
        }
        
//loome /toweb/keygen kausta
	    File logSingleEntryDir1 = new File(currentWorkingDirectory + "/toweb");
        if (!logSingleEntryDir1.exists()) {
            if (logSingleEntryDir1.mkdir()) {
                System.out.println("The toweb directory has been created.");
            } else {
                System.out.println("Failed to create toweb directory.");
            }
        }
	    File logSingleEntryDir = new File(currentWorkingDirectory + "/toweb/keygen");
        if (!logSingleEntryDir.exists()) {
            if (logSingleEntryDir.mkdir()) {
                System.out.println("The toweb/keygen directory has been created.");
            } else {
                System.out.println("Failed to create toweb/keygen directory.");
            }
        }
        
//loome /log/keygenlog.txt 
        File keygenLogFile = new File(currentWorkingDirectory + "/log/" + "keygenlog.txt");
        if (!keygenLogFile.exists()) {
            try {
                if (keygenLogFile.createNewFile()) {
                    System.out.println("New keygenlog.txt file has been created: " + currentWorkingDirectory + "/log/" + "keygenlog.txt");
                } else {
                    System.out.println("Failed to create the keygenlog.txt file.");
                }
            } catch (IOException e) {
                System.out.println("An error occurred while creating the keygenlog.txt file.");
                e.printStackTrace();
            }
        }
        
        
        
	    
    	day  = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) +"";
    	
        keyLabel = new TextField(10);
        keyLabel.setFont(new Font("Courier", 1, 22));
        preLabel = new Label("Pre-String:");
        preLabel.setFont(new Font("Courier", 1, 16));
        loginLabel = new Label("Login:");
        loginLabel.setFont(new Font("Courier", 1, 16));
        if(customer) {
        	  agentLabel = new Label("CustomerCode:");
        }else {
        	  agentLabel = new Label("Pass-ManagerCode:");
        }
      
        agentLabel.setFont(new Font("Courier", 1, 16));
        loginField = new TextField(12);
        loginField.setFont(new Font("Courier", 1, 16));
        preField = new TextField(12);
        preField.setFont(new Font("Courier", 1, 16));
        codeField = new TextField(12);
        codeField.setFont(new Font("Courier", 1, 16));
        if(customer) {
        	   setTitle("Customer Deblocking Merisoft");
        }else{
        	   setTitle("Install PIN for Merisoft");	
        }
     
        loginField.addTextListener(this);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }

        }
);
        setSize(280, 200);
        setLayout(new BorderLayout());
        Panel nPanel = new Panel(new GridLayout(2, 2));
        nPanel.add(loginLabel);
        nPanel.add(loginField);
       // nPanel.add(preLabel);
       // nPanel.add(preField);
        nPanel.add(agentLabel);
        nPanel.add(codeField);
        agentLabel.setEnabled(false);
        codeField.setEnabled(false);
        add(nPanel, "North");
        Panel cPanel = new Panel();
        cPanel.add(keyLabel);
        add(cPanel, "Center");
        setBackground(Color.green);
        Panel sPanel = new Panel();
        genBtn = new Button("Get PIN CODE");
        genBtn.setFont(new Font("Courier", 0, 16));
        genBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                generate();
            }

        }
);
        sPanel.add(genBtn);
        add(sPanel, "South");
    }

    private static boolean customer = false;
    private  Button genBtn = null;
    
    private void generate()
    {
    	String key = KeyGen.getKey(codeField.getText(), Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"");
    	if(key.length() > 4){
    		key = key.substring(key.length()-4);
    	}
        keyLabel.setText(key);
        keyLabel.setSize(keyLabel.getPreferredSize());
        keyLabel.setBackground(Color.YELLOW);
        

        
        
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
        String formattedDate = dateFormat.format(date);
        
        String currentWorkingDirectory = Paths.get(".").toAbsolutePath().normalize().toString();
        String logName = codeField.getText() + "_pincode.txt";
        String userName = System.getProperty("user.name");
        String newData = formattedDate + ";" + userName + ";" + codeField.getText() + ";" + key;
        
        
        
//uus rida faili log/keygenlog.txt
        try {
            // Create a FileWriter object with the "keygenlog.txt" file in append mode
            FileWriter fileWriter = new FileWriter(currentWorkingDirectory + "/log/keygenlog.txt", true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.newLine();
            bufferedWriter.write(newData);
            bufferedWriter.close();
            System.out.println("Data appended to the keygenlog.txt file.");
        } catch (IOException e) {
            System.out.println("Error appending data to the keygenlog.txt file.");
            e.printStackTrace();
        }
        
//uus keygen log fail ühe reaga agentcode_pincode.txt
        File keygenSingleLogLine = new File(currentWorkingDirectory + "/toweb/keygen/" + logName);
        try {
            if (keygenSingleLogLine.createNewFile()) {
                System.out.println("New keygenlog.txt file has been created: " + currentWorkingDirectory + "/toweb/keygen/" + logName);
            } else {
                System.out.println("Failed to create  " + currentWorkingDirectory + "/toweb/keygen/" + logName);
            }
            FileWriter fileWriter = new FileWriter(currentWorkingDirectory + "/toweb/keygen/" + logName);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(newData);
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred while creating the toweb/keygen log file.");
            e.printStackTrace();
        }
        
        
        FileSender ff = new FileSender();
        try {
        	ff.uploadKeygen();
        	genBtn.setBackground(Color.GREEN);
        }catch (Exception e) {
        	e.printStackTrace();
        }
        
        
        
        
        
    }

    public static void main(String args[])
    {
    	
    	 for (int i = 0 ; i < args.length ; i++){
        	 if (args[i].trim().equalsIgnoreCase("customer")){
                 customer = true;
               }
        }
    	 
        KeyGenRunner keygen = new KeyGenRunner();
        keygen.show();
       
           
            
    }

    private TextField keyLabel;
    private Label preLabel;
    private Label loginLabel;
    private Label agentLabel;
    private TextField preField;
    private TextField loginField;
    private TextField codeField;
    private String day = "";
	@Override
	public void textValueChanged(TextEvent arg0) {
		System.out.println("ahaa" + loginField.getText() + " " + day);
		if(loginField.getText().equals("Merisoft" + day)){
			agentLabel.setEnabled(true);
	        codeField.setEnabled(true);
	        loginField.setBackground(Color.BLACK);
	        codeField.requestFocus();
		}
		
	}

}
