// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   KeyGenRunner.java

package eevaeksport.keygenjava;

import java.awt.*;

import java.awt.event.*;
import java.util.Calendar;

// Referenced classes of package ee.mobilejava.keygen:
//            KeyGen

public class KeyGenRunner extends Frame implements TextListener
{

    public KeyGenRunner()
    {
    	
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
        Button genBtn = new Button("Get PIN CODE");
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
    
    
    private void generate()
    {
    	String key = KeyGen.getKey(codeField.getText(), Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"");
    	if(key.length() > 4){
    		key = key.substring(key.length()-4);
    	}
        keyLabel.setText(key);
        keyLabel.setSize(keyLabel.getPreferredSize());
        keyLabel.setBackground(Color.YELLOW);
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
