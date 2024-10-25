package eevaeksport;

import java.awt.BorderLayout;




import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import eevaeksport.driver.WebShopper;
import eevaeksport.driver.WebShopper2;
import eevaeksport.driver.AbstractDBDriver;
import eevaeksport.driver.BulkUpdater;
import eevaeksport.driver.Discounter;
import eevaeksport.driver.Manager;
import eevaeksport.driver.MustDister;
import eevaeksport.driver.WebReporter;
import eevaeksport.driver.Outputter;
import eevaeksport.driver.PictureMaker;
import eevaeksport.driver.FixLoader;
import eevaeksport.driver.FixReporter;
import eevaeksport.driver.DashboardCreator;
import eevaeksport.driver.PictureMaker2;
import eevaeksport.driver.Promo;
import eevaeksport.driver.RestSalesSender;
import eevaeksport.driver.Router;
import eevaeksport.driver.Toode;
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;

/**fset
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo TubrogetObjektmain()
 * @version 1.0
 */
public class EksportPanel extends JPanel implements ActionListener,
        ItemListener {

	
	
	private static final long serialVersionUID = 6581927816158731079L;

	/**
     * Constructor declaration
     *
     *
     * @param parentfcustprod
     *
     * @see
     */
	
	public TreeMap custCodes = null;
	public TreeMap agentsBuffer = null;
	
    public EksportPanel(EksportFrame parent) {
    	
    	//GoogleReader gr = new GoogleReader("0Au88zOrKfAcfdFRleEpjZG5rTC0tTDdmdmxOQ1pTbEE","select%20*");
    	
    	// System.out.println( gr.getData() );
        this.parent = parent;
        this.mp = new MemoPanel(this);
        getNF().setGroupingUsed(false);
        getNF().setMaximumFractionDigits(4);
        getNF2().setGroupingUsed(false);
        getNF2().setMaximumFractionDigits(4);
        getNF2().setMinimumFractionDigits(0);
        //System.out.println(parent.isDaySales());
        initDriver();
        genButton = new JButton(getString("senddata"));
        muuda = new JButton(getString("changemail"));
        sendToAll = new JCheckBox(getString("sendall"));
        startButton = new JButton(getString("starttext"));
        promoButton = new JButton(getString("monthpromo"));
        promoPersButton = new JButton(getString("perspromo"));
         closeBtn = new JButton(parent.getString("closebutton"));
       
        /*
         * String title = props.getProperty("title");
         * if ((title != null) && (!title.equals(""))){
         * setTitle(title);
         * } else {
         * setTitle(driver.getDriverVersion());
         * }
         */
        initAgents();
        setLayout(new BorderLayout());
        //setBackground(BACKGROUND_COLOR);

        centerPanel = new JPanel(new BorderLayout());
        JPanel innerNorth = new JPanel();
        centerPanel.add(innerNorth, BorderLayout.NORTH);
        //centerPanel.setBackground(BACKGROUND_COLOR);
        innerNorth.add(agentBox);
        innerNorth.add(muuda);

        JPanel innerSouth = new JPanel();
        centerPanel.add(innerSouth, BorderLayout.SOUTH);
        innerSouth.add(startButton);
        innerSouth.add(promoButton);
        innerSouth.add(promoPersButton);
        muuda.setFont(new Font(muuda.getFont().getFontName(),
                               muuda.getFont().getStyle(), 10));

        
        southPanel.add(closeBtn);
        southPanel.add(genButton);
        closeBtn.addActionListener(this);
        //southPanel.setBackground(BACKGROUND_COLOR);
        southPanel.add(sendToAll);
        southPanel.add(progressBar);
        add(centerPanel, BorderLayout.CENTER);

        JPanel northPanel = new JPanel(new BorderLayout());
         //northPanel.setBackground(BACKGROUND_COLOR);

        // northPanel.add(new JLabel("P?hjade saatmine"), BorderLayout.NORTH);
        northPanel.add(status, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);
        add(southPanel, BorderLayout.SOUTH);
        genButton.addActionListener(this);
        //sendToAll.addActionListener(this);
        genButton.setMnemonic('g');
        muuda.addActionListener(this);
        muuda.setMnemonic('m');
        promoButton.addActionListener(this);
        startButton.addActionListener(this);
        promoPersButton.addActionListener(this);

        timer = new Timer(200, new ActionListener() {

            /**
             * Method declaration
             *
             *
             * @param evt
             *
             * @see
             */
            public void actionPerformed(ActionEvent evt) {
                progressBar.setValue(getCurrentProgress());

                if (getCurrentProgress() == 100) {
                    timer.stop();
                    genButton.setEnabled(true);

                    task1 = false;
                    task2 = false;
                    task3 = false;
                    task4 = false;
                }
            }

        });

        if (parent.isAutoSend()){
          autoSend();
        }
        
        if (parent.isDaySales()){
          generateDay2Pilots();
          while(!sent){
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e){}
          }
          sent = false;
          System.exit(0);
        }
        
        if (parent.isSendHalf()){
            autoSend();
          }
        
        if (parent.isMakePicture()){
        	makePictures();
          }

        
        if(parent.isFixLoader()) {
         uploadFixsortiments();	
        }
        if(parent.isDashboardCreator()) {
        	createDashboard();	
           }
        
        if(parent.isWebShopper()) {
        	createWebShopper();
           }
        if(parent.isWebShopper2()) {
        	createWebShopper2();
           }
        
        if(parent.isFixReporter()) {
            createFixreport();	
           }
        if(parent.isWebReporter()) {
            createWebreport();	
           }
        
        if (parent.isMakePicture2()){
        	makePictures2();
          }
        
        if (parent.isAutoDebt()){
          autoSend();
        }
        
        if (parent.isOnRequest()){
        	File f = new File ("makedata.txt");
        	if(f.exists()){
        		requestData = null;
        		try{
        			FileReader fr = new FileReader("makedata.txt");
        	        BufferedReader reader = new BufferedReader(fr);
        	        String line = " ";
        	  
        	      while( ((line = reader.readLine())!= null   && line.trim().length() > 0 )){
        	    	  requestData = line;  
        	      }
        	      fr.close();
        		}catch (Exception dd){
        			dd.printStackTrace();
        		}
        		   f.delete();
        		 //  System.out.println("andmed tellitud LISA INFO:'" + requestData + "'");
                autoSend();	
             
        	}
        	System.exit(0);
          }

       
        
        if (parent.isSendStock()) {
          autoSendStock();
        }
        if (parent.isSendRouteCal()) {
            autoSendRouteCal();
          }
       
        if (parent.isSendRestAll()) {
            autoSendRestAll();
          }
        
        if (parent.isSendRestSales()) {
            autoSendRestSales();
          }
        

        if (parent.isSortExcel()) {
        	sortExcel();
          }
        
        if (parent.isMakeOutput()) {
            try{
            	saveOutputInventoryFiles();
              } catch (IOException e){
                e.printStackTrace();
              }
          }
        
        
        if (parent.isReadStock()) {
            try{
            	readStockToDb();
              } catch (IOException e){
                e.printStackTrace();
              }
          }
        

        if (parent.isDistRep()) {
          try{
            sendDistributionFile();
          } catch (IOException e){
            e.printStackTrace();
          }
        }
        
        if (parent.isSaveStock()) {
          try{
            saveProductFile();
          } catch (IOException e){
            e.printStackTrace();
          }
        }
        if (parent.isSaveDebt()) {
            try{
              saveDebtFile();
            } catch (IOException e){
              e.printStackTrace();
            }
          }

        //initSchedule();

    }


    private void  saveOutputInventoryFiles() throws IOException {
    if(driver.getDriverVersion().equals("EevaInvent")){
    
    	HashMap hh = ((Outputter)driver).getOutPutHash();
        if(hh != null){
        
        	Iterator iter = hh.keySet().iterator();
        	while (iter.hasNext()){
        	String fileName = (String)iter.next();
        	
        String prodstring = (String)hh.get(fileName);
					        if(prodstring != null){
					          String fpath = getProps().getProperty("outputdir");	      
								          if (fpath != null) {
								            fileName = fpath + fileName;
								          }
					          File prodFile = new File(fileName);
					          java.io.Writer wr = new FileWriter(prodFile);
					          wr.write(prodstring);
					          wr.close();
					          System.exit(0);
					              }	
        	                     }  	
                        }else{
                        	 System.exit(0);	
                        }
            }
      }

    private void  readStockToDb() throws IOException {
        if(driver.getDriverVersion().equals("EevaInvent")){      
        	((Outputter)driver).readStockToDb();
            }
          }
    
    private void makePictures(){
    	((PictureMaker)driver).PictureMaker();
    }
    
    private void uploadFixsortiments(){
    	((FixLoader)driver).FixLoader();
    }
    
    private void createFixreport(){
    	((FixReporter)driver).FixReporter();
    }
    
    private void createWebShopper(){
    	((WebShopper)driver).WebShopper();
    }
    private void createWebShopper2(){
    	((WebShopper2)driver).WebShopper2();
    }
    private void createDashboard(){
    	((DashboardCreator)driver).createDashboard();
    }
    
    private void createWebreport(){
    	((WebReporter)driver).WebReporter();
    }
    
    private void makePictures2(){
    	((PictureMaker2)driver).PictureMaker2();
    }
    
    public String make(String aa){
    	if(aa != null && aa.trim().length()>0){
    		aa = aa.replaceAll(";", " ").trim();
    	}else{
    		aa = " ";
    	}
    	return aa;
    }
    
    private void autoSend(){
      generateFilesAndEmailsToAll(null);
      while(!sent){
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e){}
      }
      sent = false;
      
     /* if(getProps().getProperty("aws.direxp") != null) {
    	  FileSender ff = new FileSender();
          ff.sendMaster = true;
          try {
    		ff.start(null);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}  
      }*/
      
      System.exit(0);
    }
    
    
    
    
    
    private void autoSendRouteCal(){
        sendRoutesOnly();
          System.exit(0);
        }

    private void autoSendRestAll(){
    	sendRestAll();
          System.exit(0);
        }
    
    private void autoSendRestSales(){
    	sendRestSales();
          System.exit(0);
        }

    
    private void sortExcel(){
    	makeSortExcel();
          System.exit(0);
        }
    
	private void autoSendStock(){
       generateFilesAndEmailsToAll(null);
         while(!sent){
           try {
             Thread.sleep(1000);
           } catch (InterruptedException e){}
         }
         sent = false;
         System.exit(0);
       }


    /**
     * Method declaration
     *
     *
     * @param e
     *
     * @see
     */
    public void itemStateChanged(ItemEvent e) {
        Object obj = e.getSource();

        if (obj == agentBox) {
            status.setText(" ");
            progressBar.setStringPainted(false);
            progressBar.setValue(0);
        }
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    private int getCurrentProgress() {
        if (!task1) {
            return 0;
        } else if (!task2) {
            return 25;
        } else if (!task3) {
            return 50;
        } else if (!task4) {
            return 75;
        } else {
            return 100;
        }
    }

    /**
     * Method declaration
     *
     *
     * @see
     */
    private void initDriver() {
        driver = parent.getExportDriver(this);

        if (driver != null) {
            if (isDebug()) {
                driver.setDebug(true);
            } else {
                driver.setDebug(false);
            }
        }
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public boolean isDebug() {
        return parent.isDebug();
    }

    /**
     * Method declaration
     *
     *
     * @param e
     *
     * @see
     */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == genButton) {
          if (sendToAll.isSelected()){
            generateFilesAndEmailsToAll(null);
          } else {
        	  if (driver instanceof BulkUpdater){
        		  generateFilesAndEmailsToAll(this.getSelectedAgentCode());
        	  }else{
                  generateFilesAndEmails(getSelectedAgentVector());  
        	  }
          }
        }
        
        if (source == closeBtn) {
            System.exit(0);
        }

        if (source == muuda) {
            changeEmail();
        }


        if (source == promoButton){
          doPromo();
        }
        if (source == startButton){
            doPromoStart();
          }
        if (source == promoPersButton){
          doPersonalPromo();
        }
    }

    private void doPromo(){
      Properties props = parent.getPropsInfo();
      isPersonal = false;
      isStartText = false;
      String promo = props.getProperty("promo");
      if (promo == null){
        promo = "";
      }
      mp.setMemoText(promo);
      centerPanel.add(mp, BorderLayout.CENTER);
      parent.pack();
    }

    private void doPromoStart(){
        Properties props = parent.getPropsInfo();
        isPersonal = false;
        isStartText = true;
        String promo = props.getProperty("starttext");
        if (promo == null){
          promo = "";
        }
        mp.setMemoText(promo);
        centerPanel.add(mp, BorderLayout.CENTER);
        parent.pack();
      }

    
    
    
    public boolean isPersonal = false;
    public boolean isStartText = false;

    private void doPersonalPromo(){
        Properties props = parent.getPropsInfo();
        String agent = this.getSelectedAgentCode();
        String promo = props.getProperty(agent);
        if (promo == null){
          promo = "";
        }
        mp.setMemoText(promo);
        isPersonal = true;
        isStartText = false;
        centerPanel.add(mp, BorderLayout.CENTER);
        parent.pack();
      }




    protected void saveMemoPanel(String property){
      parent.getPropsInfo().setProperty(property, mp.getMemoText());
      parent.savePropsInfo();
      centerPanel.remove(mp);
      parent.pack();
    }


    /**
     *
     */
     private Vector getSelectedAgentVector(){
      Vector vec = new Vector(1);
      vec.add(getSelectedAgentCode());
      return vec;
     }

    /**
     * Method declaration
     *
     *
     * @see
     */
    private void changeEmail() {
        String      emails = getEmail(getSelectedAgentCode());
        ChangeEmail email = new ChangeEmail(parseFirstEmail(emails),
                                            parseSecondEmail(emails), this);

        email.pack();
        email.setVisible(true);
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    

	public void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
	
    
    
    public Properties getEmailProps() {
        return EksportFrame.loadProps(getProps().getProperty("emailfile"));
    }

    public static double round(double value, double comma){
        double a = Math.pow(10, comma);
        return Math.round(value * a)/a;
      }

    
    /**
     * Method declaration
     *
     *
     * @param email
     * @param email2
     *
     * @see
     */
    public void setEmailForCurrentAgent(String email, String email2) {
        setEmail(getSelectedAgentCode(), email, email2);
        refreshAgents();
    }

    /**
     * Method declaration
     *
     *
     * @param agentCode
     *
     * @return
     *
     * 
     * @see
     */
    public String getEmail(String agentCode) {
        Properties props = getEmailProps();
        String     email = props.getProperty(agentCode);
        if (email == null) {
            email = "";
        }

        return email;
    }



    /**
     * Method declaration
     *
     *
     * @param agentCode
     * @param email
     * @param email2
     *
     * @see
     */
    private void setEmail(String agentCode, String email, String email2) {
        Properties emailProps =
            EksportFrame.loadProps(getProps().getProperty("emailfile"));

        emailProps.setProperty(agentCode, email + EMAILS_SEPARATOR + email2);

        try {
            FileOutputStream out =
                new FileOutputStream(getProps().getProperty("emailfile"));

            emailProps.store(out, "Agendi kood vastavuses emailiga");
            out.close();
        } catch (IOException e) {
            EksportFrame.showError(e);
        }
    }




    private void generateFilesAndEmailsToAll(String  agcode){
      Iterator iter = agents.values().iterator();
      final Vector agentsq = new Vector();
      TreeMap terr = new TreeMap();
      if(agcode == null ){
    	
      while (iter.hasNext()){
        Agent agent = (Agent)iter.next();
        String agentCode = agent.getID();
        if (!parseFirstEmail(getEmail(agentCode)).equals("")){
          agentsq.addElement(agentCode);
          terr.put(agentCode, agent);
       }
      }
      
      } else {
    	  agentsq.addElement(agcode);
      }

    Vector res   = sortAgs(agentsq,terr);
    System.out.println("res " + res.toString());
      
      if (driver instanceof BulkUpdater){
        generateForBulk(res);
      } else{
        generateFilesAndEmails(res);
      }
    }

    public static Vector upsideOrderLines(Vector inVec){
        Vector sorted = new Vector(inVec.size());
        while(inVec.size() > 0){
        	sorted.addElement(inVec.elementAt(inVec.size()-1));
        	inVec.removeElementAt(inVec.size()-1);
        }
        return sorted;
      }
    
    public static Vector sortAgs(Vector inVec, TreeMap ags){
        Vector vec = new Vector();
        for (int i = 0 ; i < inVec.size() ; i++){
          vec.addElement(inVec.elementAt(i));
        }
        Vector sorted = new Vector(vec.size());
        while(vec.size() > 0){
          String smallest = null;
          Agent smProd = null;
          for (Enumeration enumer = vec.elements(); enumer.hasMoreElements() ; ){
            String ol = (String)enumer.nextElement();
            Agent ag = (Agent)ags.get(ol);
           
            	
            if ( (smallest == null) || (smProd!= null && ag != null && ag.getObject() != null &&
            		smProd.getObject() != null &&	ag.getObject().compareTo(smProd.getObject()) < 0 )) {
              smallest = ol;
              smProd = (Agent)ags.get(smallest);
            }
          
          }
          vec.removeElement(smallest);
          sorted.addElement(smallest);
          //System.out.println(smallest);
        }
        return upsideOrderLines(sorted);
      }
    
    
    private void generateForBulk(final Vector theAgents){
      genButton.setEnabled(false);
      progressBar.setStringPainted(true);
     
      final SwingWorker worker = new SwingWorker() {

          public Object construct() {

        	  System.out.println("starting");
        	  
            TreeMap customers = driver.getCustomers(new Agent("xxxx"));
            Vector fixed = null;
            System.out.println("parent. FIXED");
            
            try {
                fixed = ( (Discounter) driver).getFixedAssortmentData();
              } catch (Exception e){
              //  showError(e);
              } 
              System.out.println("parent. FIXED done" );
              
              HashMap custHash = new HashMap();
		      HashMap custPayHash = new HashMap();
            for (Iterator iter = customers.values().iterator() ; iter.hasNext() ;){
              Customer cust = (Customer)iter.next();
              custHash.put(cust.getID(), cust);
                
              if(custPayHash.containsKey(cust.getPayer())){
	              	TreeMap ags = (TreeMap)custPayHash.get(cust.getPayer()); 
	              	TreeMap agg = (TreeMap)ags.clone();
	              	
	              	Iterator itera = cust.getAgents().keySet().iterator();
	              	while(itera.hasNext()){
	              		String a = (String)itera.next();
	              		Agent aa = new Agent(a);
	              		agg.put(a,aa);	
	              	}
              	      custPayHash.put(cust.getPayer(),agg);
                  }else{
              	      custPayHash.put(cust.getPayer(),cust.getAgents());
                
                   }
              
              
            
              
              }
            
            
            Vector custDistProds = null;
            try{
           	  custDistProds =  ((MustDister) driver).getCustomerProductsDistList();
            }catch(Exception e){
           	// e.printStackTrace();
            }
            System.out.println("parent. products");
            TreeMap products = driver.getProducts(null);
            System.out.println("parent. assortment");
            String assortment = driver.getAssortmentStr(null);
            
            System.out.println("parent. dealerproductlist");
            DealerProduct stocks = driver.getDealerProductsList();
            if(!parent.isSendHalf()){
            	 System.out.println("parent. bulksales");
            	((BulkUpdater)driver).setBulkSales(getAgentsHash());

            }
            Vector custProds = null;
            if(parent.isSendHalf()){
            	custProds = null;
            }else{
            	 System.out.println("parent. custprods");
            	custProds = driver.getCustomerProductsList(null);
            }
            
         
          
            System.out.println("parent. costructfiles");
            Vector lastThree = null;
            if(driver.getDriverVersion().equals("LeiburLat") || driver.getDriverVersion().equals("Leibur") || driver.getDriverVersion().equals("SAFSaaremaa") 
            		|| driver.getDriverVersion().equals("LeiburLIT") 	
            		|| driver.getDriverVersion().equals("AxaFazerLV") 
            		|| driver.getDriverVersion().equals("AxaFazerLT") 
            		|| driver.getDriverVersion().equals("AxaFazerEE") 
            		|| driver.getDriverVersion().equals("AxaNoo") 
            		|| driver.getDriverVersion().equals("NaviOrkla")
            		|| driver.getDriverVersion().equals("HorizonMuiz") 
            		|| driver.getDriverVersion().equals("NaviLivikoEST")
            		|| driver.getDriverVersion().equals("NaviLivikoLAT")
            		|| driver.getDriverVersion().equals("EevaArke")
            		|| driver.getDriverVersion().equals("HorizonKonig")
            		|| driver.getDriverVersion().equals("AxaJNGLAT")
            		|| driver.getDriverVersion().equals("SmartenR")
            		|| driver.getDriverVersion().equals("Kalev")
            		
            		|| driver.getDriverVersion().equals("NaviLivikoLIT")){
            	lastThree = ((Discounter) driver).getLastThreeData();
            }
            Vector debts = null;
          
            System.out.println("parent. costructfiles debt  ");
            
            try {
              debts = driver.getDebt(null);
            } catch (SQLException e){
              showError(e);
            }
         
            System.out.println("parent. costructfiles debt done ");
              
            Vector matrix = null ;

            System.out.println("parent. costructfiles matrix start ");
            try {
                matrix = ( (Discounter) driver).getPriceMatrixData();
              } catch (Exception e){
                e.printStackTrace();
              }
         
            System.out.println("parent. costructfiles matrix done "  + theAgents.toString());
            
            Enumeration enume = theAgents.elements();
            while (enume.hasMoreElements()){
            
                
              timer.stop();
              task1 = false;
              task2 = false;
              task3 = false;
              task4 = false;
              timer.start();

              String agentCode = (String)enume.nextElement();
          	System.out.println("parent. costructfiles elements sisse "+ agentCode);
              Agent agent = (Agent)getAgentsHash().get(agentCode);
              email = new Email(getString("datasent"));
              email.setEncoding("UTF-8");
            //  email.setNoticeTo(parseSecondEmail(getEmail(agentCode)));
              email.setFrom(getProps().getProperty("from"));
              email.setSMTP(getProps().getProperty("smtphost"));
              email.setUser(getProps().getProperty("smtpuser"));
              email.setPass(getProps().getProperty("smtppass"));
              email.setPort(getProps().getProperty("smtpport"));
              email.setSubjectFilesTo(driver.getSubjectFilesTo());
              try {
            	  if("true".equals(getProps().getProperty("nosub"))){
            		  
            	  }else{
            			email.setBody("MerisoftDataFile");
            	  }
			
			} catch (MessagingException e1) {
				e1.printStackTrace();
			}
              if(getProps().getProperty("language").equals("Russian")){
                email.setEncoding("KOI8-R");
              }

              zip = new ZipHandlerPass(EksportPanel.this);

              try {
                status.setText(getString("readcust"));
                System.out.println("parent. costructfiles  start ");
                
                if(parent.isSendStock()){
                  ProductFile(agentCode, products,stocks);
                  CustomerFile(agentCode, customers);
                  try {
                 DealerFiles(customers, agentCode);
                
               } catch (SQLException e){
                 showError(e);
               } 
               }else{
            	   System.out.println("parent. costructfiles prgroup start ");
                   
                  
               	ProdGroupfile(agent);
               	if(assortment != null){
               	  AssortmentFile(agent,assortment);
               	}
                System.out.println("parent. costructfiles cust start ");
                
                CustomerFile(agentCode, customers);
            
                  HashSet agprods =  ProductFile(agentCode, products, stocks);

                  	System.out.println("Siin on asi 2");
                  	
                //}
                    if(!parent.isSendHalf()){
                    	  CustProdFile(agentCode, custProds, custHash,agprods);
                    }
              
                    if(custDistProds != null) {
                    	CustProdMustDistrFile(agentCode, custDistProds, custHash,agprods);
                    }
                
                if(debts != null){
                	DebtFile(agentCode, debts, custHash,custPayHash);             	
                }
                
                try {
                	System.out.println("enne " );
                  DealerFiles(customers, agentCode);
                  System.out.println("p�rast " );
                
                  
                  
                  if(driver.getDriverVersion().equals("ArkoAXALat") || driver.getDriverVersion().equals("Trid") || driver.getDriverVersion().equals("Vipex")
                  	 	|| driver.getDriverVersion().equals("Fere")
                                || driver.getDriverVersion().equals("Motoral")
                                || driver.getDriverVersion().equals("Tallegg")
                                || driver.getDriverVersion().equals("HansaLumene")
                                 || driver.getDriverVersion().equals("HansaPomm")
                                  || driver.getDriverVersion().equals("Scala5iESTVIA")
                                  	|| driver.getDriverVersion().equals("SolcraftJun")
                                    || driver.getDriverVersion().equals("SolMan")
                                || driver.getDriverVersion().equals("VPGFin")
                                || driver.getDriverVersion().equals("EevaDtl")
                                
                  		|| driver.getDriverVersion().equals("VipexKTK")
						|| driver.getDriverVersion().equals("NaviAtria")
                        || driver.getDriverVersion().equals("SAFArtik")
						|| driver.getDriverVersion().equals("Scala5iLATJUN")
						|| driver.getDriverVersion().equals("EevaDVDLAT")
						|| driver.getDriverVersion().equals("Ogmios")
						|| driver.getDriverVersion().equals("Amer")
						|| driver.getDriverVersion().equals("EevaMG")
						|| driver.getDriverVersion().equals("ScalaPrikeLat")
						|| driver.getDriverVersion().equals("ScalaPrikeLatDOM")
						|| driver.getDriverVersion().equals("ScalaPrikeEst")
						|| driver.getDriverVersion().equals("ScalaPrikeLit")
						|| driver.getDriverVersion().equals("AxaPere")
						|| driver.getDriverVersion().equals("NaviTobis")
						|| driver.getDriverVersion().equals("NaviTobisDirecto")
						|| driver.getDriverVersion().equals("NaviTobisKAL")					
						|| driver.getDriverVersion().equals("NaviTobisEST")
						|| driver.getDriverVersion().equals("NaviTobisBY")
						|| driver.getDriverVersion().equals("Leibur")
						|| driver.getDriverVersion().equals("LeiburLIT")
						|| driver.getDriverVersion().equals("LeiburLat")
						|| driver.getDriverVersion().equals("Kalev")
						|| driver.getDriverVersion().equals("KalevBakery")
						|| driver.getDriverVersion().equals("ScalaJaun")
						|| driver.getDriverVersion().equals("Scala5iLATDOM")
						|| driver.getDriverVersion().equals("Rakvere")
                        || driver.getDriverVersion().equals("SAFNurmiAG")
                	    || driver.getDriverVersion().equals("ScalaToba")
                	    || driver.getDriverVersion().equals("ValioAxa")
                	    || driver.getDriverVersion().equals("SkaiteCC")
                	    || driver.getDriverVersion().equals("ScalaSale")
                	    || driver.getDriverVersion().equals("Altia")
						|| driver.getDriverVersion().equals("AltiaLAT")
                	    || driver.getDriverVersion().equals("AxaDunker")
						|| driver.getDriverVersion().equals("NaviPremiaEST")
						|| driver.getDriverVersion().equals("SAFMaag")
						|| driver.getDriverVersion().equals("SAFSire")
						|| driver.getDriverVersion().equals("SAFMaagGrupp")
						|| driver.getDriverVersion().equals("AxaKaupmees")				
                	  || driver.getDriverVersion().equals("AxaBuraLat")
                	  || driver.getDriverVersion().equals("Rivona")
                	  || driver.getDriverVersion().equals("RivonaKeda")
                	  
                	  || driver.getDriverVersion().equals("Biovela")
                	  || driver.getDriverVersion().equals("RivileCeryx")
                	  || driver.getDriverVersion().equals("RivileKuryba") 
                	   || driver.getDriverVersion().equals("RivileLedna") 
                	   || driver.getDriverVersion().equals("RivilePoliartis") 
                	    || driver.getDriverVersion().equals("RivileHonestas")   
                	     	    || driver.getDriverVersion().equals("RivileLobis") 
                	  || driver.getDriverVersion().equals("VichiLITMerc")  
                	    || driver.getDriverVersion().equals("VichiLIT") 
                	    || driver.getDriverVersion().equals("VichiLATSQL") 
                	    || driver.getDriverVersion().equals("VichiESTSQL") 
                	      || driver.getDriverVersion().equals("AxaZpienas")
                	  || driver.getDriverVersion().equals("AxaZpienasMana")
                	    || driver.getDriverVersion().equals("AxaZpienasMilkMana")
                	  	|| driver.getDriverVersion().equals("NaviVilandra")
                	  		|| driver.getDriverVersion().equals("SkaiteKLP")
						|| driver.getDriverVersion().equals("NaviVilandraMerc")
						|| driver.getDriverVersion().equals("NaviVilandraSV")
						|| driver.getDriverVersion().equals("BuitNowacoLT")
						|| driver.getDriverVersion().equals("BuitNowacoLV")
						|| driver.getDriverVersion().equals("Movex")
						|| driver.getDriverVersion().equals("MovexLAT")
						|| driver.getDriverVersion().equals("RivileSalcio")
						|| driver.getDriverVersion().equals("BSMS")
						|| driver.getDriverVersion().equals("DunkerLAT")
						|| driver.getDriverVersion().equals("VichiLT") 
						|| driver.getDriverVersion().equals("NaviOskar") 
								|| driver.getDriverVersion().equals("Rauta")
						|| driver.getDriverVersion().equals("HansaSatcom") 
						|| driver.getDriverVersion().equals("Interaltus")  
						|| driver.getDriverVersion().equals("LabbisSkonis") 
					    || driver.getDriverVersion().equals("AxaZpienasMerc")
					    || driver.getDriverVersion().equals("RivileSes")
					     || driver.getDriverVersion().equals("ViaLaima")
					    || driver.getDriverVersion().equals("BiovelaUus")
					    || driver.getDriverVersion().equals("ErplyLtt")
					    || driver.getDriverVersion().equals("SkaiteArvol")
					    || driver.getDriverVersion().equals("SkaiteMesine")
					    || driver.getDriverVersion().equals("SkaiteVigesta")
					    || driver.getDriverVersion().equals("AxaOrklaLV")
					    || driver.getDriverVersion().equals("AxaAbestock")
					    || driver.getDriverVersion().equals("SolcraftAuto")
					    || driver.getDriverVersion().equals("SolcraftFlower")
					    || driver.getDriverVersion().equals("NaviPernod")
					    || driver.getDriverVersion().equals("IceAxa")
					    || driver.getDriverVersion().equals("IceAxaUus")
					    || driver.getDriverVersion().equals("GlobusTrade")
					    || driver.getDriverVersion().equals("GlobalMar")
					    || driver.getDriverVersion().equals("NaviIceco")
					    || driver.getDriverVersion().equals("DirectoLinpet")
					    || driver.getDriverVersion().equals("DirectoAgapics")
					    
				
					    || driver.getDriverVersion().equals("HorizonMuiz")
					    || driver.getDriverVersion().equals("HorizonKonig")
					    || driver.getDriverVersion().equals("NaviNematekas")
					    || driver.getDriverVersion().equals("NaviOrkla")
					    || driver.getDriverVersion().equals("AxaAmber")
					    || driver.getDriverVersion().equals("NaviKAL")
					    || driver.getDriverVersion().equals("AxaNoo")
					    || driver.getDriverVersion().equals("NaviTobisBY")
					    || driver.getDriverVersion().equals("KMAHorti")
					    || driver.getDriverVersion().equals("RVEktoni")
					    || driver.getDriverVersion().equals("AxaAmberIB")
					    || driver.getDriverVersion().equals("AxaAmberUK")
					    || driver.getDriverVersion().equals("PalettiLit")
					    || driver.getDriverVersion().equals("Vikstal")
					    || driver.getDriverVersion().equals("RivileRiestlita")
					    || driver.getDriverVersion().equals("RivileArktis")
					    || driver.getDriverVersion().equals("Boom5")
					    || driver.getDriverVersion().equals("HorizonTri")
					    || driver.getDriverVersion().equals("Avallone")
					    || driver.getDriverVersion().equals("ViciCZSQL")
					    || driver.getDriverVersion().equals("SAFWiru")
					    || driver.getDriverVersion().equals("EevaMantinga")
					    || driver.getDriverVersion().equals("NaviLivikoEST")
					    || driver.getDriverVersion().equals("NaviLivikoLIT")
					    || driver.getDriverVersion().equals("NaviLivikoLAT")
					    || driver.getDriverVersion().equals("AxaFazerLV")
					    || driver.getDriverVersion().equals("AxaFazerLT")
					    || driver.getDriverVersion().equals("AxaFazerEE")
					    || driver.getDriverVersion().equals("EevaArke")
					    || driver.getDriverVersion().equals("ViciPOLSQL")
					    || driver.getDriverVersion().equals("RivileBalmerk")
					    || driver.getDriverVersion().equals("RivileAmber")
					    || driver.getDriverVersion().equals("SkaiteKM")
					    || driver.getDriverVersion().equals("SolcraftShell")
					    || driver.getDriverVersion().equals("SmartenR")
					    || driver.getDriverVersion().equals("AnkvarsFlora")
					    || driver.getDriverVersion().equals("RivileKepe")
					    || driver.getDriverVersion().equals("AxaJNGLAT")
					    || driver.getDriverVersion().equals("ODOOApi")
					    || driver.getDriverVersion().equals("AxaAmberAT")
					    || driver.getDriverVersion().equals("NaviValdo")
					    || driver.getDriverVersion().equals("SamsonasCSV")
					    || driver.getDriverVersion().equals("RivileTridens")
					    || driver.getDriverVersion().equals("S1XMLRadex")
					    || driver.getDriverVersion().equals("KulcsSoft")
					    || driver.getDriverVersion().equals("Rinku")
					    || driver.getDriverVersion().equals("RivileLedinta")
					    || driver.getDriverVersion().equals("RivileVasca")

					    || driver.getDriverVersion().equals("NaviTobisDirecto")
	                	 )
                  
                  {
                	  if(matrix != null){
                		  PriceMatrixFile(agentCode, customers,matrix,agprods);
                	  }
                   
                  }
                  
                  if(driver.getDriverVersion().equals("VichiLT")  || driver.getDriverVersion().equals("VichiESTSQL") ||
                		  driver.getDriverVersion().equals("Cido") ||  driver.getDriverVersion().equals("VichiLITMerc") ||
                  		driver.getDriverVersion().equals("VichiSQLLAT") ||	driver.getDriverVersion().equals("ArcoTXT")
						|| driver.getDriverVersion().equals("Miesnieks")
						|| driver.getDriverVersion().equals("Kruzas")
						|| driver.getDriverVersion().equals("VichiLATSQL")
						|| driver.getDriverVersion().equals("KruzasDea")
						|| driver.getDriverVersion().equals("Rakvere")
						|| driver.getDriverVersion().equals("SkaiteEug")						
						|| driver.getDriverVersion().equals("SkaiteCC")
						|| driver.getDriverVersion().equals("NaviAtria")
	                    || driver.getDriverVersion().equals("Ogmios")
						|| driver.getDriverVersion().equals("SAFSaaremaa")
						|| driver.getDriverVersion().equals("SAFNurmiAG")
						|| driver.getDriverVersion().equals("Coverland")
						|| driver.getDriverVersion().equals("Amer")
						|| driver.getDriverVersion().equals("SAFSire")
						|| driver.getDriverVersion().equals("Movex")
						|| driver.getDriverVersion().equals("Leibur")
						|| driver.getDriverVersion().equals("LeiburLat")
							|| driver.getDriverVersion().equals("LeiburLIT")
							|| driver.getDriverVersion().equals("Fere")
							|| driver.getDriverVersion().equals("SolcraftJun")
							|| driver.getDriverVersion().equals("RivileAmber")
							|| driver.getDriverVersion().equals("MovexLIT")
							|| driver.getDriverVersion().equals("Festival")
								|| driver.getDriverVersion().equals("Kalev")
								|| driver.getDriverVersion().equals("OcraCharlot")
								|| driver.getDriverVersion().equals("EevaPal")
									|| driver.getDriverVersion().equals("KMPSQL")
									|| driver.getDriverVersion().equals("ValioAxa")
									|| driver.getDriverVersion().equals("ScalaPrikeLatVia")
									|| driver.getDriverVersion().equals("ScalaPrikeLatDOM")
									|| driver.getDriverVersion().equals("ScalaPrikeEst")
									|| driver.getDriverVersion().equals("ScalaPrikeLit")
									|| driver.getDriverVersion().equals("ScalaSale")
										|| driver.getDriverVersion().equals("Smarten")
										|| driver.getDriverVersion().equals("AxaDunker")
										|| driver.getDriverVersion().equals("NaviPremiaEST")
										|| driver.getDriverVersion().equals("NaviVilandra")
										|| driver.getDriverVersion().equals("NaviVilandraMerc")
										|| driver.getDriverVersion().equals("NaviVilandraSV")
										|| driver.getDriverVersion().equals("AxaKaupmees")
										|| driver.getDriverVersion().equals("AxaBuraLat")
										|| driver.getDriverVersion().equals("Rivona")
										|| driver.getDriverVersion().equals("SAFMaag")
										|| driver.getDriverVersion().equals("AltiaLT") 
										|| driver.getDriverVersion().equals("BSMS") 
										|| driver.getDriverVersion().equals("NaviOskar") 
										|| driver.getDriverVersion().equals("Biovela")
										|| driver.getDriverVersion().equals("AltiaLAT")
										   || driver.getDriverVersion().equals("AxaZpienas")
										      || driver.getDriverVersion().equals("AxaZpienasAudit")
                	  || driver.getDriverVersion().equals("AxaZpienasMana")
                	  || driver.getDriverVersion().equals("AxaZpienasMerc")
                      || driver.getDriverVersion().equals("KGGrupp")
                        || driver.getDriverVersion().equals("ErplyLtt")
                        || driver.getDriverVersion().equals("DunkerLAT")
                        || driver.getDriverVersion().equals("KalevMerc")
                           || driver.getDriverVersion().equals("Rauta")
                           || driver.getDriverVersion().equals("RivileArktis")
                             || driver.getDriverVersion().equals("ScalaToba")
                            || driver.getDriverVersion().equals("BiovelaUus")
                             || driver.getDriverVersion().equals("EevaPalLat")
                             || driver.getDriverVersion().equals("RivonaKeda")
     						|| driver.getDriverVersion().equals("BuitNowacoLV")
                             || driver.getDriverVersion().equals("VS")
                             || driver.getDriverVersion().equals("DirectoLinpet")
                             || driver.getDriverVersion().equals("DirectoAgapics")
                             
                            || driver.getDriverVersion().equals("ViaLaima")
                            || driver.getDriverVersion().equals("AxaOrklaLV")
                	  || driver.getDriverVersion().equals("IceAxa")
                	  || driver.getDriverVersion().equals("IceAxaUus")
                	  || driver.getDriverVersion().equals("Ozols")
                	  || driver.getDriverVersion().equals("GlobalMar")
                	  || driver.getDriverVersion().equals("Movex")
                	  || driver.getDriverVersion().equals("Interaltus")
                	  || driver.getDriverVersion().equals("SkaiteVigesta")
					    || driver.getDriverVersion().equals("SkaiteMesine")
					    || driver.getDriverVersion().equals("SkaiteMesine")
					    || driver.getDriverVersion().equals("AxaAmber")
					    || driver.getDriverVersion().equals("KMAHorti")
					    || driver.getDriverVersion().equals("AxaAmberLATCall")
					    || driver.getDriverVersion().equals("AxaAmberIB")
					    || driver.getDriverVersion().equals("RivileRiestlita")
					    || driver.getDriverVersion().equals("RivileLedinta")
					    || driver.getDriverVersion().equals("NaviLivikoEST")
					    || driver.getDriverVersion().equals("NaviLivikoLIT")
					    || driver.getDriverVersion().equals("NaviLivikoLAT")
					    || driver.getDriverVersion().equals("ViciCZSQL")
					    || driver.getDriverVersion().equals("AxaFazerLT")
					    || driver.getDriverVersion().equals("AxaFazerLV")
					    || driver.getDriverVersion().equals("SkaiteKM")
					    || driver.getDriverVersion().equals("SamsonasCSV")
					    || driver.getDriverVersion().equals("MovexLAT")
					    || driver.getDriverVersion().equals("NaviTobisDirecto")
					  ){
                	 
                	  System.out.println("Teen Fixi " + jsonDate(Calendar.getInstance().getTime()) );
                   FixedAssortmentFile(agentCode, customers,fixed,agprods);
                   System.out.println("Teen Fixi lõpp " + jsonDate(Calendar.getInstance().getTime()) );
                 }
                  if(driver.getDriverVersion().equals("LeiburLat") || driver.getDriverVersion().equals("LeiburLIT") 
                		  || driver.getDriverVersion().equals("Leibur") || driver.getDriverVersion().equals("SAFSaaremaa")
                		  || driver.getDriverVersion().equals("NaviOrkla")
                		  || driver.getDriverVersion().equals("HorizonMuiz")
                		  || driver.getDriverVersion().equals("AxaFazerLV")
                		  || driver.getDriverVersion().equals("AxaNoo")
                		  || driver.getDriverVersion().equals("NaviLivikoEST")
                		  || driver.getDriverVersion().equals("NaviLivikoLAT")
                		  || driver.getDriverVersion().equals("NaviLivikoLIT")
                		  || driver.getDriverVersion().equals("EevaArke")
                		  || driver.getDriverVersion().equals("HorizonKonig")
                		  || driver.getDriverVersion().equals("AxaFazerLT")
                		  || driver.getDriverVersion().equals("AxaJNGLAT")
                		  || driver.getDriverVersion().equals("Kalev")
                		  || driver.getDriverVersion().equals("SmartenR")
                		  || driver.getDriverVersion().equals("AxaFazerEE")){
                     LastThreeFile(agentCode, customers,  lastThree);
                   }
                 
                
                  

                } catch (SQLException e){
                  showError(e);
                }

                }

                String doneOrders = driver.getNonMobileOrders((Agent)getAgentsHash().get(agentCode));

                if (doneOrders != null){
                  zip.addFile("phoneorders.txt", doneOrders);
                }
                String prodDiscounts = driver.getCustProdDiscounts((Agent)getAgentsHash().get(agentCode));
                                                if (prodDiscounts != null){
                                                  zip.addFile("proddiscounts.txt", prodDiscounts);
                                                }

                Properties values = new Properties();
                int sales = agent.getMonthSales();
                int foodsales = agent.getMonthFoodSales();
                int alcosales = agent.getMonthAlkoSales();
                int horeca = agent.getMonthHorecaSales();
                int offtrade = agent.getMonthOFFSales();
                int salesvol = agent.getMonthSalesVol();
                int salesvol3 = agent.getMonthSales3();
                int saleskg = agent.getKgSales();
                int salesbonus = agent.getMonthBonusSales();
                int budget = agent.getMonthBudget();
                int profit = agent.getMonthProfit();
                int budget1 = agent.getMonthBudget1();
                int budget2 = agent.getMonthBudget1();
                String startinff = agent.getStart();
                String techdata = agent.techdata;
               
                
                if (startinff.length() != 0){
                    values.setProperty("info2", startinff);
                  }
                if (techdata.length() != 0){
                    values.setProperty("info3", techdata);
                  }
               
                
                if (budget1 != 0){
                    values.setProperty("budget1", String.valueOf(budget1));
                  }
                
                if (budget2 != 0){
                    values.setProperty("budget2", String.valueOf(budget2));
                  }
                
                if (profit != 0){
                    values.setProperty("profit", String.valueOf(profit));
                  }
                if (saleskg != 0){
                    values.setProperty("saleskg", String.valueOf(saleskg));
                  }
                if (salesbonus != 0){
                    values.setProperty("salesbonus", String.valueOf(sales));
                  }
                if (sales != 0){
                  values.setProperty("sales", String.valueOf(sales));
                  driver.setSales(sales);
                }
                if (salesvol3 != 0){
                    values.setProperty("sales3", String.valueOf(salesvol3));
                  }
                
                if (budget != 0){
                  values.setProperty("budget", String.valueOf(budget));
                  driver.setBudget(budget);
                }
                
                double percent = driver.getPercent();
                if (percent != 0){
                  values.setProperty("percent", String.valueOf(percent));
                }
                
                
                double percent1 = driver.getPercent1();
                if (percent1 != 0){
                  values.setProperty("percent1", String.valueOf(percent1));
                }
                
                double percent2 = driver.getPercent2();
                if (percent2 != 0){
                  values.setProperty("percent2", String.valueOf(percent2));
                }
                double sales3 = driver.getSales3();
                if (sales3 != 0) {
                  values.setProperty("sales3", String.valueOf( (int) sales3));
                }

                if (salesvol != 0) {
                  values.setProperty("volume", String.valueOf( (int) salesvol));
                }

                if ( (foodsales != -1) && (foodsales != 0)) {
                  values.setProperty("toit", String.valueOf(foodsales));
                }

                if ( (alcosales != -1) && (alcosales != 0)) {
                  values.setProperty("alko", String.valueOf(alcosales));
                }

                if ( (offtrade != -1) && (offtrade != 0)) {
                  values.setProperty("pood", String.valueOf(offtrade));
                }

                if ( (horeca != -1) && (horeca != 0)) {
                  values.setProperty("baar", String.valueOf(horeca));
                }



                String promo = parent.getPropsInfo().getProperty("promo");
                if (promo != null){
                  values.setProperty("promo", String.valueOf(promo));
                }


                String startText = parent.getPropsInfo().getProperty("starttext");
                if (startText != null){
                  values.setProperty("starttext", String.valueOf(startText));
                }else{
                	   values.setProperty("starttext", "");
                }
                String startText2 = parent.getPropsInfo().getProperty("starttext2");
                if (startText2 != null){
                  values.setProperty("starttext2", String.valueOf(startText2));
                }else{
                	 values.setProperty("starttext2", "");
                }
                
                startText2 = agent.getStartText();
                if(startText2 != null){
                	  values.setProperty("starttext2", String.valueOf(startText2));
                }
                
                String promoPers = parent.getPropsInfo().getProperty(agentCode);
                String promoAll = values.getProperty("promo");
                if (promoAll!= null && promoPers != null){
                  values.setProperty("promo", String.valueOf(promoAll)+ "\n" + String.valueOf(promoPers));
                } else if(promoPers != null){
                    values.setProperty("promo",String.valueOf(promoPers));
                }


                String listgrupp = parent.getProps().getProperty("listing.grupp");
                if(listgrupp != null){
                	  values.setProperty("listing.grupp",String.valueOf(listgrupp));
                }
                setMarketInfo(values,custHash);

                EksportFrame.saveProps(values, "values.txt");

              } catch (Exception e){
                e.printStackTrace();
                JOptionPane.showMessageDialog(parent, e.toString(), "SQL " + getString("error"), JOptionPane.ERROR_MESSAGE);
              }

                zip.addFile("values.txt");
                File tr = new File("tararelation.txt");
                if(tr.exists()){
                    zip.addFile("tararelation.txt");
                }
               
                File trx = new File("minussales.txt");
                if(trx.exists()){
                    zip.addFile("minussales.txt");
                }
                

                File j1 = new File("json/routes.json");
                if(j1.exists()){
                    zip.addFile("json/routes.json");
                }
                File j2 = new File("json/pods.json");
                if(j2.exists()){
                 //   zip.addFile("json/pods.json");
                }
                File j3 = new File("json/docsshipit.json");
                if(j3.exists()){
                    zip.addFile("json/docsshipit.json");
                }
                
                
                File sv22 = new File("json/podsall.txt");
                if(sv22.exists()){
                    zip.addFile("json/podsall.txt");
                }
                
                File sv = new File("supervisor.txt");
                if(sv.exists()){
                    zip.addFile("supervisor.txt");
                }
                File sva = new File("visitreason.txt");
                if(sva.exists()){
                    zip.addFile("visitreason.txt");
                }
                
                File svdd = new File("ownlogo.jpg");
                if(svdd.exists()){
                    zip.addFile("ownlogo.jpg");                	
                }
                
                File svddw = new File("ownlogo.png");
                if(svddw.exists()){
                    zip.addFile("ownlogo.png");                	
                }
                
                File svddw2 = new File("ownlogo2.png");
                if(svddw2.exists()){
                    zip.addFile("ownlogo2.png");                	
                }
                
                
                File see = new File("inventory.txt");
                if(see.exists()){
                    zip.addFile("inventory.txt");
                }
                
               
                File uv = new File("alterperson.txt");
                if(uv.exists()){
                    zip.addFile("alterperson.txt");
                }
                
               
                File sm = new File("prodgroup.txt");
                if(sm.exists()){
                    zip.addFile("prodgroup.txt");
                }
                
                File svx = new File("questions.txt");
                
                if(svx.exists()){
                    zip.addFile("questions.txt");           	
                }
                
                File ssvd = new File("custcontacts.txt");
                if(ssvd.exists()){
                    zip.addFile("custcontacts.txt");               	
                }
                File ssv = new File("update.pro");
                if(ssv.exists()){
                    zip.addFile("update.pro");               	
                }
                File ssvrr = new File("settings/update.pro");
                if(ssvrr.exists()){
                    zip.addFile("settings/update.pro");               	
                }
                
                
                File ssve = new File("inv.properties");
                if(ssve.exists()){
                    zip.addFile("inv.properties");               	
                }
                File asve = new File("settings/inv.properties");
                if(asve.exists()){
                    zip.addFile("settings/inv.properties");               	
                }
                

                File ssves = new File("offer.properties");
                if(ssves.exists()){
                    zip.addFile("offer.properties");               	
                }
                
                File asvex = new File("settings/offer.properties");
                if(asvex.exists()){
                    zip.addFile("settings/offer.properties");               	
                }
                
                File sdve = new File("ordertype.txt");
                if(sdve.exists()){
                    zip.addFile("ordertype.txt");               	
                }
                
                
                File sdv = new File("managerdata.txt");
                if(sdv.exists()){
                    zip.addFile("managerdata.txt");               	
                }

                File ssd = new File(agentCode + ".pro");
                if(ssd.exists()){
                    zip.addFile(agentCode + ".pro");               	
                }
                File sad = new File("settings/" + agentCode + ".pro");
                if(sad.exists()){
                    zip.addFile("settings/" + agentCode + ".pro");               	
                }

                File sadd = new File("settings/m-" + agentCode + ".pro");
                if(sadd.exists()){
                    zip.addFile("settings/m-" + agentCode + ".pro");               	
                }
                
                File svw = new File("asksup2.txt");
                if(svw.exists()){
                    zip.addFile("asksup2.txt");
                	
                }
                File svm = new File("photogroups.txt");
                
                if(svm.exists()){
                    zip.addFile("photogroups.txt");                	
                }
                
                File svcl = new File("clientagent.txt");
                if(svcl.exists()){
                    zip.addFile("clientagent.txt");
                	
                }
                
                File svclss = new File("optamounts.txt");
                if(svclss.exists()){
                    zip.addFile("optamounts.txt");
                	
                }
                
                
                File lt = new File("lastthree.txt");
                if(lt.exists()){
                   // zip.addFile("lastthree.txt");
                	
                }
                File com = new File("company.txt");
                if(com.exists()){
                    zip.addFile("company.txt");
                	
                }
                sv = new File("supervisor2.txt");
                if(sv.exists()){
                    zip.addFile("supervisor2.txt");
                	
                }
                
                File mdf = new File("maxdiscounts.txt");
                if(mdf.exists()){
                    zip.addFile("maxdiscounts.txt");
                }
               
                File mdfs = new File("deposit.txt");
                if(mdfs.exists()){
                    zip.addFile("deposit.txt");
                }
               
                
                File mdfj = new File("customercrm.txt");
                if(mdfj.exists()){
                    zip.addFile("customercrm.txt");
                	
                }
                
                File sl = new File("stocklocations.txt");
                if(sl.exists()){
                    zip.addFile("stocklocations.txt");
                	
                }
                File s2 = new File("warestatic.txt");
                if(s2.exists()){
                    zip.addFile("warestatic.txt");
                	
                }
                
                File pa = new File("prodactions.txt");
                if(pa.exists()){
                    zip.addFile("prodactions.txt");
                	
                }
                File wa = new File("tootedRev.has");
                if(wa.exists()){
                    zip.addFile("tootedRev.has");
                }
                File wx = new File("tootedRev.txt");
                if(wx.exists()){
                    zip.addFile("tootedRev.txt");
                }
                File wq = new File("agent.txt");
                if(wq.exists()){
                    zip.addFile("agent.txt");
                }
                
                
                File wbb = new File("tood.txt");
                if(wbb.exists()){
                    zip.addFile("tood.txt");
                }
                File sk = new File("prodsort.txt");
                if(sk.exists()){
                    zip.addFile("prodsort.txt");
                	
                }
                File wh5 = new File("salescontracts.txt");
                if(wh5.exists()){
                    zip.addFile("salescontracts.txt");
                }
                File wh53 = new File("salescompa.txt");
                if(wh53.exists()){
                    zip.addFile("salescompa.txt");
                }
                
               
                File wh = new File("warehouse.txt");
                if(wh.exists()){
                    zip.addFile("warehouse.txt");
                }
                File wh2 = new File("warehouse2.txt");
                if(wh2.exists()){
                	if(getProps().getProperty("send.ware2") != null && getProps().getProperty("send.ware2").contains(agent.getID()))
                    zip.addFile("warehouse2.txt");
                }
                File wh33 = new File("kegoverdue.txt");
                if(wh33.exists()){
                    zip.addFile("kegoverdue.txt");
                }
                File whdd = new File("fixsortnew.txt");
                if(whdd.exists()){
                    zip.addFile("fixsortnew.txt");
                }
                File whq = new File("assortment.txt");
                if(whq.exists()){
                    zip.addFile("assortment.txt");
                }
                
                File who = new File("payments.txt");
                if(who.exists()){
                    zip.addFile("payments.txt");
                }
                
                File wh22 = new File("taraoptimum.txt");
                if(wh22.exists()){
                    zip.addFile("taraoptimum.txt");
                }
                
                File wh3 = new File("contactpersons.txt");
                if(wh3.exists()){
                    zip.addFile("contactpersons.txt");
                }
                wh = new File("prodanalogs.txt");
                if(wh.exists()){
                    zip.addFile("prodanalogs.txt");
                }
                File wh1 = new File("outcome.txt");
                if(wh1.exists()){
                    zip.addFile("outcome.txt");
                }
                
                File imor = new File("aorder");
                if(imor.exists()){
                zip.addFolder("aorder");
                }
                
                File wqs = new File("allowed.txt");
                if(wqs.exists()){
                    zip.addFile("allowed.txt");
                }
                
                File whqe = new File("nonevisited.txt");
                if(whqe.exists()){
                    zip.addFile("nonevisited.txt");
                }
                 
                
                File whe = new File("displaypoints.txt");
                if(whe.exists()){
                    zip.addFile("displaypoints.txt");
                }
                
                File cl = new File("carlist.txt");
                if(cl.exists()){
                    zip.addFile("carlist.txt");
                }
                File whw = new File("commenttext.txt");
                if(whw.exists()){
                    zip.addFile("commenttext.txt");
                }
             
                File whqq = new File("inventprods.txt");
                if(whqq.exists()){
                    zip.addFile("inventprods.txt");
                }
             
                File whqa = new File("xcust.txt");
                if(whqa.exists()){
                    zip.addFile("xcust.txt");
                }
                
                File whdr = new File("texts.txt");
                if(whdr.exists()){
                    zip.addFile("texts.txt");
                	
                }
                
                File whd = new File("inv.proprties");
                if(whd.exists()){
                    zip.addFile("inv.proprties");
                	
                }
                
                File ak = new File("akcija.xls");
                if(ak.exists()){
                    zip.addFile("akcija.xls");           	
                }
                
                File aks = new File("form.xls");
                if(aks.exists()){
                    zip.addFile("form.xls");           	
                }
                
                
                File sh = new File("shipmode.txt");
                if(sh.exists()){
                    zip.addFile("shipmode.txt");              	
                }
                
                File sf = new File("paymterm.txt");
                if(sf.exists()){
                    zip.addFile("paymterm.txt");              	
                }
                
                
                File shh = new File("shipcond.txt");
                if(shh.exists()){
                    zip.addFile("shipcond.txt");
                	
                }
                
                File shhd = new File("xcusts.txt");
                if(shhd.exists()){
                    zip.addFile("xcusts.txt");
                	
                }
                
                File shi = new File("returnreason.txt");
                if(shi.exists()){
                    zip.addFile("returnreason.txt");
                }
                
                File shg = new File("custdetch.txt");
                if(shg.exists()){
                    zip.addFile("custdetch.txt");               	
                }
                File shgr = new File("questnames.txt");
                if(shgr.exists()){
                    zip.addFile("questnames.txt");               	
                }
                
                
                File shgw = new File("custdetch1.txt");
                if(shgw.exists()){
                    zip.addFile("custdetch1.txt");               	
                }

                File shgws = new File("custdetch2.txt");
                if(shgws.exists()){
                    zip.addFile("custdetch2.txt");               	
                }

                File shgws1 = new File("custdetchhrc.txt");
                if(shgws1.exists()){
                    zip.addFile("custdetchhrc.txt");               	
                }
                File shgws12 = new File("custdetchfnf.txt");
                if(shgws12.exists()){
                    zip.addFile("custdetchfnf.txt");               	
                }
               
                
                File shjy = new File("custgroups.txt");
                if(shjy.exists()){
                    zip.addFile("custgroups.txt");
                	
                }
                
                File shjya = new File("forms.txt");
                if(shjya.exists()){
                    zip.addFile("forms.txt");
                }
                
                File shjyas = new File("formsdata.txt");
                if(shjyas.exists()){
                    zip.addFile("formsdata.txt");
                }
                
                File shj = new File("eventprods.txt");
                if(shj.exists()){
                    zip.addFile("eventprods.txt");
                	
                }
                
                File of = new File("offer.txt");
                if(of.exists()){
                    zip.addFile("offer.txt");
                }
                task1 = true;

                status.setText(getString("readprod"));
                //ProductFile(agentCode);

                task2 = true;

                status.setText(getString("readshit"));
                //CustProdFile(agentCode);

                task3 = true;

                status.setText(getString("sending"));

                try {
                	
                	System.out.println("res 2 " + agentCode );
                    zip.write(agentCode,agent.zipPass);
                    String zipName = agentCode + ".zip";

                    email.addAttach(zipName);
                    email.setBody("");
                    
                    if(parent.saveFiles){
                    	File fildest = new File(getProps().getProperty("savefilesdir") + zipName);
                    	File filz = new File(zipName);
                    
                    	
                    	  String ext = 	getProps().getProperty("extension");
                        //	System.out.println("ext " + ext);
                        	if(ext != null){
                        		fildest = new File(getProps().getProperty("savefilesdir") + agentCode + "." + ext);
                        		
                        	}
                        	if(fildest.exists()){
                        		fildest.delete();
                        	}
                    	if(filz.exists()){
                    	filz.renameTo(fildest);
                    	}
                    	
                       }else{
                    	   if(!parseFirstEmail(getEmail(agentCode)).equals("aa")){
                    		   try{
                    			   String saata = parseFirstEmail(getEmail(agentCode));
                    			   StringTokenizer st = new StringTokenizer(saata,",");
                    			   while (st.hasMoreTokens()){
                    				   email.setFilesTo(st.nextToken());
                            		   email.send();  
                    			   }
                    			   
                        	   }catch (Exception e ){
                        		try{
                        			  String saata = parseFirstEmail(getEmail(agentCode));
                       			   StringTokenizer st = new StringTokenizer(saata,",");
                       			   while (st.hasMoreTokens()){
                       				   email.setFilesTo(st.nextToken());
                               		   email.send();  
                       			   }  	
                        		}catch(Exception ee){
                        			ee.printStackTrace();
                        			System.out.println(" ei suuda ka teist korda saata " + parseFirstEmail(getEmail(agentCode)) + " " + agentCode);
                        		}
                        		
                        	   } 
                    		   if(getProps().getProperty("savefilesafteremail") != null){
                    			
                    		   File fildest = new File(getProps().getProperty("savefilesafteremail") + zipName);
                           	File filz = new File(zipName);
                           
                           	
                           	  String ext = 	getProps().getProperty("extension");
                               //	System.out.println("ext " + ext);
                               	if(ext != null){
                               		fildest = new File(getProps().getProperty("savefilesdir") + agentCode + "." + ext);
                               		
                               	}
                               	if(fildest.exists()){
                               		fildest.delete();
                               	}
                           	if(filz.exists()){
                           	filz.renameTo(fildest);
                           	}
                            
                    		   }
                    	   } else if(getProps().getProperty("data.arhiiv") != null ){
                           	File fildest = new File(getProps().getProperty("data.arhiiv")  + Calendar.getInstance().getTimeInMillis()+ zipName);
                        	File filz = new File(zipName);
                        
                        	if(filz.exists()){
                        		copyFile(filz,fildest);
                        	}
                    	   }
                    	
                    	 	
                       }
                   
                    status.setText(getString("sent"));

                    task4 = true;
                } catch (Exception e) {
                    EksportFrame.showError(e);
                }
            }
            sent = true;
                return null;
          }
        };

      worker.start();
    }

    public static void copyFile(File in, File out) throws Exception {
        FileInputStream fis  = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        byte[] buf = new byte[1024];
        int i = 0;
        while((i=fis.read(buf))!=-1) {
          fos.write(buf, 0, i);
          }
        fis.close();
        fos.close();
        }
    
    private void setMarketInfo(Properties props ,HashMap custs){
 if (parent.useMarketInfo()){
 	MarketInfoPanel pan = new MarketInfoPanel(parent,this.driver);
          Vector data = pan.getData();
          long date = new Date().getTime();
          int counter = 0;
          for (int i = 0 ; i < data.size() ; i++){
        	  try{
        		
            Vector row = (Vector)data.get(i);
            String question = (String)row.get(0);

            if (question.trim().length() > 1){

              Date start = getDate((String)row.get(2));
              Date end = getDate((String)row.get(3));
              String clients = null;
           //   System.out.println(" row 4 " + row.get(4).toString());
           
            	   TreeMap tr = (TreeMap)row.get(4);
                   
                   Vector cliIDs =(Vector) tr.get("customer");
                   
                   if(cliIDs != null && cliIDs.size() > 0){
                   	Enumeration enume = cliIDs.elements();
                   	while (enume.hasMoreElements()){
                   		String id = ((String)enume.nextElement());
                   		id = id.substring(0,id.indexOf("||"));
                   		Customer cc = (Customer) custs.get(id.trim());
                   		//System.out.println("kliendid " + cc + id );
                   		if(cc != null){            		
                   		if(clients == null){
                       		clients = cc.getID();
                   		}else {
                       		clients = clients + ";" + cc.getID();
                   		}	
                   		}
                   	}
                   } 	 
         
           
              
              if ((start != null) || (end != null)){


                if ((date < end.getTime() ) && (date >= start.getTime())){
                String xxx = "false";
                 if(((String)row.get(5)).length()>0){
                 	xxx = (String)row.get(5);
                 }
                  counter++;
                  props.setProperty("marketinfo.question." + counter, (String)row.get(0));
                  props.setProperty("marketinfo.type." + counter, (String)row.get(1));
                  props.setProperty("marketinfo.after." + counter, xxx);
                  
                  if(clients != null){
                 	  props.setProperty("marketinfo.clients." + counter, clients); 	
                 }
                  
                }
              }
            }
            
        	  }catch (Exception ee){
        		ee.printStackTrace();  
        	  }
          }
    }
    }

    private Date getDate(String ddmmyyyy){
      Date date = null;
      if (ddmmyyyy.length() < 10) return date;
      try {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(ddmmyyyy.substring(0, 2)));
        cal.set(Calendar.MONTH, Integer.parseInt(ddmmyyyy.substring(3, 5))-1);
        cal.set(Calendar.YEAR, Integer.parseInt(ddmmyyyy.substring(6, 10)));
        //System.out.println(cal.getTime());
        date = cal.getTime();
      } catch (Exception e){
        EksportFrame.showError(e);
      }
      return date;
    }

    public static String jsonDate(Date dd){
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    	format.setTimeZone(TimeZone.getTimeZone("UTC"));
    	return   format.format(dd) ;	
    }
    
    private void generateDay2Pilots(){
      Iterator iter = agents.values().iterator();
      final Hashtable pilots = new Hashtable();
      while (iter.hasNext()){
        Agent agent = (Agent)iter.next();
        String agentCode = agent.getID();
        String objekt = agent.getObject();
        if (objekt == null){
        	objekt = "";
        }
        if((agent.isIspilot()) && (!parseFirstEmail(getEmail(agentCode)).equals(""))){
          pilots.put(agentCode, objekt);
        }
      }
     // System.out.println("pilots " + pilots);
      if (pilots.size() != 0){
        generateToPilotList(pilots,false);
      }
    }




    private void generateToPilotList(Hashtable pilots,boolean daysales){
        genButton.setEnabled(false);
        progressBar.setStringPainted(true);
 
        final Hashtable pilotObjekts = pilots;

        final SwingWorker worker = new SwingWorker() {

            public Object construct() {

              Enumeration enume = pilotObjekts.keys();
              while (enume.hasMoreElements()){

               timer.stop();
                task1 = false;
                task2 = false;
                task3 = false;
                task4 = false;
               timer.start();

                String agentCode = (String)enume.nextElement();
                Agent agent = (Agent)getAgentsHash().get(agentCode);
                email = new Email(getString("datasent"));

                email.setEncoding("UTF-8");
           //     email.setNoticeTo(parseSecondEmail(getEmail(agentCode)));
                email.setFrom(getProps().getProperty("from"));
                email.setSMTP(getProps().getProperty("smtphost"));

                email.setUser(getProps().getProperty("smtpuser"));
                email.setPass(getProps().getProperty("smtppass"));
                email.setPort(getProps().getProperty("smtpport"));
                email.setSubjectFilesTo(getString("salesreport"));

                
                
                
                  Calendar cal = Calendar.getInstance();
                  String datestr = cal.get(Calendar.DAY_OF_MONTH) + "-" +
                   ( cal.get(Calendar.MONTH ) + 1) + "-" + cal.get(Calendar.YEAR);
                  String filename = agentCode + "-" + datestr + ".txt";
                 
                 String asi =   PilotDayFile(agent, filename);
                   
               
                  task1 = true;
                  task2 = true;
                  task3 = true;

                  status.setText(getString("sending"));

                  try {
                    File file = new File(filename);
                    if (file.exists()){
                     email.addAttach(filename);
                     email.setBody(asi);
                     String addr = parseFirstEmail(getEmail(agentCode));
                     if(addr.equals("aa")){
                    	 addr = parseSecondEmail(getEmail(agentCode));
                     }
                     StringTokenizer st = new StringTokenizer(addr,",");
                   
                     while(st.hasMoreTokens()){
                    	  email.setFilesTo(st.nextToken());
                          email.send();	 
                     }
                   
                      status.setText(getString("sending"));
                 
                      file.delete();
                      }
                    
                 

                      task4 = true;
                  } catch (Exception e) {
                      EksportFrame.showError(e);
                      setError(true);
                      try {
                      File file = new File(filename);
                      if (file.exists()){
                    	  file.delete();
                      }
                      } catch (Exception d) {
                    	  
                      }
                  }

              }
              
        
        	if(!isError()){
        		parent.getPropsInfo().setProperty("lastreport", System.currentTimeMillis()+"");
        	    parent.savePropsInfo();  
        	}
        	
        	
        	
              sent = true;
                  return null;
            }
          };

       worker.start();

    }

   private  boolean error = false;
    public String getString(String s){
      return parent.getString(s);
    }

    private String  PilotDayFile(Agent agent, String filename){
    	
    	String res = "";
    	if (agent != null && driver != null){
    		
    	try {
    			agent.lastreport = Long.parseLong(parent.getPropsInfo().getProperty("lastreport"));
    		}catch (Exception w) {
    		 System.out.println("no last report saved jet");
    			//w.printStackTrace();
    		}
    	
      String data = driver.getPilotDayData(agent);
     if(data != null && data.trim().length() > 0){
    	 res = data.toString();
      try{
    	  
    	  Converter.saveFileInUTF8(filename, data);
    	
      } catch (Exception e){
    	  e.printStackTrace();
        showError(e);
         }	
       }
     }
    	return res ;
    }


    

    /**
     * Method declaration
     *
     *
     * @see
     */
    private void generateFilesAndEmails(Vector theAgents) {

        genButton.setEnabled(false);
        progressBar.setStringPainted(true);

        final Vector agentCodes = theAgents;
        System.out.println("res 12 " + theAgents.toString());

        final SwingWorker worker = new SwingWorker() {

            /**
             * Method declaration
             *
             *
             * @return
             *
             * @see
             */
            public Object construct() {

              Enumeration enume = agentCodes.elements();
              while (enume.hasMoreElements()){

                timer.stop();
                task1 = false;
                task2 = false;
                task3 = false;
                task4 = false;
                timer.start();

                String agentCode = (String)enume.nextElement();
                Agent agent = (Agent)getAgentsHash().get(agentCode);

                email = new Email(getString("datasent"));
                email.setEncoding("UTF-8");
                email.setFilesTo(parseFirstEmail(getEmail(agentCode)));
                //email.setNoticeTo(parseSecondEmail(getEmail(agentCode)));
                email.setFrom(getProps().getProperty("from"));
                email.setSMTP(getProps().getProperty("smtphost"));

                email.setUser(getProps().getProperty("smtpuser"));
                email.setPass(getProps().getProperty("smtppass"));
                email.setPort(getProps().getProperty("smtpport"));
                email.setSubjectFilesTo(driver.getSubjectFilesTo());
                if(getProps().getProperty("language").equals("Russian")){
              email.setEncoding("KOI8-R");
             }


                zip = new ZipHandlerPass(EksportPanel.this);

                try {
                  status.setText(getString("readcust"));

                  CustomerFile(agentCode,false);

                  if (parent.isAutoDebt()){
                    DebtFile(agentCode);
                  }
                  String doneOrders = driver.getNonMobileOrders((Agent)getAgentsHash().get(agentCode));

                  if (doneOrders != null){
                    zip.addFile("phoneorders.txt", doneOrders);
                  }

                  String prodDiscounts = driver.getCustProdDiscounts((Agent)getAgentsHash().get(agentCode));
                                 if (prodDiscounts != null){
                                   zip.addFile("proddiscounts.txt", prodDiscounts);
                                 }


                  Properties values = new Properties();
                  double salary = driver.getSalary(agentCode);
                  if (salary != 0){
                    values.setProperty("salary", getNF().format(salary));
                  }

                  double budget = driver.getBudget();
                  if (budget != 0){
                    values.setProperty("budget", String.valueOf(budget));
                  }

                  double percent = driver.getPercent();
                  if (percent != 0){
                    values.setProperty("percent", String.valueOf(percent));
                  }

                  double sales = driver.getSales();
                  if (sales != 0){
                    values.setProperty("sales", String.valueOf((int)sales));
                  }

                  double sales3 = driver.getSales3();
                  if (sales3 != 0){
                    values.setProperty("sales3", String.valueOf((int)sales3));
                  }
                  double sales4 = driver.getSales4();
                  if (sales4 != 0){
                    values.setProperty("sales4", String.valueOf((int)sales4));
                  }


                  double toit = driver.getFoodSales(agentCode);
                  if ((toit != -1) && (toit != 0)){
                    values.setProperty("toit", String.valueOf(toit));
                  }

                  double alko = driver.getAlcoSales(agentCode);
                  if ((alko != -1) && (alko != 0)){
                    values.setProperty("alko", String.valueOf(alko));
                  }

                  if (driver instanceof Promo){
                    double promo1 = ((Promo)driver).getPromoSales(agentCode, 1);
                    if ((promo1 != -1) && (promo1 != 0)){
                      values.setProperty("promo1", getNF().format(promo1));
                    }
                    double promo2 = ((Promo)driver).getPromoSales(agentCode, 2);
                    if ((promo2 != -1) && (promo2 != 0)){
                      values.setProperty("promo2", getNF().format(promo2));
                    }
                    double promo3 = ((Promo)driver).getPromoSales(agentCode, 3);
                    if ((promo3 != -1) && (promo3 != 0)){
                      values.setProperty("promo3", getNF().format(promo3));
                    }
                  }

                  String promo = parent.getPropsInfo().getProperty("promo");
                  if (promo != null){
                    values.setProperty("promo", String.valueOf(promo));
                  }

                  String startText = parent.getPropsInfo().getProperty("starttext");
                  if (startText != null){
                    values.setProperty("starttext", String.valueOf(startText));
                  }
                  
                  String startText2 = parent.getPropsInfo().getProperty("starttext2");
                  if (startText2 != null){
                    values.setProperty("starttext2", String.valueOf(startText2));
                  }
                  
                  setMarketInfo(values,null);

                  EksportFrame.saveProps(values, "values.txt");
                  zip.addFile("values.txt");

                  File mdfj = new File("customercrm.txt");
                  if(mdfj.exists()){
                      zip.addFile("customercrm.txt");
                  	
                  }
                  File mddd = new File("supervisor.txt");
                  if(mddd.exists()){
                      zip.addFile("supervisor.txt");
                  }
                  File sva = new File("visitreason.txt");
                  if(sva.exists()){
                      zip.addFile("visitreason.txt");
                  }

                  File svdd = new File("ownlogo.jpg");
                  if(svdd.exists()){
                      zip.addFile("ownlogo.jpg");                	
                  }
                  
                  File svddw = new File("ownlogo.png");
                  if(svddw.exists()){
                      zip.addFile("ownlogo.png");                	
                  }
                  File svddw2 = new File("ownlogo2.png");
                  if(svddw2.exists()){
                      zip.addFile("ownlogo2.png");                	
                  }
                  
                  
                  File see = new File("inventory.txt");
                  if(see.exists()){
                      zip.addFile("inventory.txt");
                  	
                  }
                  File uv = new File("alterperson.txt");
                  if(uv.exists()){
                      zip.addFile("alterperson.txt");
                  	
                  }
                 
                  
                  File svx = new File("questions.txt");
                  
                  if(svx.exists()){
                      zip.addFile("questions.txt");           	
                  }
                  
                  File sm = new File("prodgroup.txt");
                  if(sm.exists()){
                      zip.addFile("prodgroup.txt");
                  	
                  }
                  File ssvd = new File("custcontacts.txt");
                  if(ssvd.exists()){
                      zip.addFile("custcontacts.txt");               	
                  }
                  
                  File ssv = new File("update.pro");
                  if(ssv.exists()){
                      zip.addFile("update.pro");
                  }
                  
                  File ssvrr = new File("settings/update.pro");
                  if(ssvrr.exists()){
                      zip.addFile("settings/update.pro");               	
                  }
                  
                  
                  File ssve = new File("inv.properties");
                  if(ssve.exists()){
                      zip.addFile("inv.properties");               	
                  }
                  
                  File sdv = new File("managerdata.txt");
                  if(sdv.exists()){
                      zip.addFile("managerdata.txt");               	
                  }
                  
                  File sdve = new File("ordertype.txt");
                  if(sdve.exists()){
                      zip.addFile("ordertype.txt");               	
                  }
                  
                  
                  File ssd = new File(agentCode + ".pro");
                  if(ssd.exists()){
                      zip.addFile(agentCode + ".pro");               	
                  }
                  
                  
                  File svw = new File("asksup2.txt");
                  if(svw.exists()){
                      zip.addFile("asksup2.txt");
                  }
                  
                  File svm = new File("photogroups.txt");
                  if(svm.exists()){
                      zip.addFile("photogroups.txt");                	
                  }
                  
                  File svcl = new File("clientagent.txt");
                  if(svcl.exists()){
                      zip.addFile("clientagent.txt");
                  }
                  
                  
                  File svclss = new File("optamounts.txt");
                  if(svclss.exists()){
                      zip.addFile("optamounts.txt");
                  }
                  
                  File lt = new File("lastthree.txt");
                  if(lt.exists()){
                    //  zip.addFile("lastthree.txt");
                  	
                  }
                  File com = new File("company.txt");
                  if(com.exists()){
                      zip.addFile("company.txt");
                  }
                  File shgr = new File("questnames.txt");
                  if(shgr.exists()){
                      zip.addFile("questnames.txt");               	
                  }
                  File mccc = new File("custdetch.txt");
                  if(mccc.exists()){
                      zip.addFile("custdetch.txt");
                  }
               
                  File shgw = new File("custdetch1.txt");
                  if(shgw.exists()){
                      zip.addFile("custdetch1.txt");               	
                  }

                  File shgww = new File("custdetch2.txt");
                  if(shgww.exists()){
                      zip.addFile("custdetch2.txt");               	
                  }
                  File shj = new File("eventprods.txt");
                  if(shj.exists()){
                      zip.addFile("eventprods.txt");
                  	
                  }
                  File shjy = new File("custgroups.txt");
                  if(shjy.exists()){
                      zip.addFile("custgroups.txt");
                  }
                  
                  File shjya = new File("forms.txt");
                  if(shjya.exists()){
                      zip.addFile("forms.txt");
                  }
                  
                  File shjyas = new File("formsdata.txt");
                  if(shjyas.exists()){
                      zip.addFile("formsdata.txt");
                  }
                  
           //       System.out.println("Salary Tehtud " + agentCode + " " + salary );
                  ProdGroupfile(agent);
                  String assortment = driver.getAssortmentStr(null);
                  if(assortment != null)
                  AssortmentFile(agent,assortment);

       //           System.out.println("Sortiment Tehtud " );
                } catch (SQLException e){
                  e.printStackTrace();
                  JOptionPane.showMessageDialog(parent, e.toString(), "SQL " + getString("error"), JOptionPane.ERROR_MESSAGE);
                }

                  task1 = true;

                  status.setText(getString("readprod"));
                  ProductFile(agentCode);
           //       System.out.println("Tooted  Tehtud " );

                  task2 = true;

                  status.setText(getString("readshit"));
                  CustProdFile(agentCode);
              //    System.out.println("sortimendid  Tehtud " );
                  task3 = true;

                  status.setText(getString("sending"));

                  try {
                      zip.write(agentCode, agent.zipPass);
                      String zipName = agentCode + ".zip";

                      email.addAttach(zipName);


                      if(parent.saveFiles){
                      	File fildest = new File(getProps().getProperty("savefilesdir") + zipName);
                      	File filz = new File(zipName);
                      	if(fildest.exists()){
                      		fildest.delete();
                      	}
                      	
                      String ext = 	getProps().getProperty("extension");
                    //  	System.out.println("ext " + ext);
                      	if(ext != null){
                      		fildest = new File(getProps().getProperty("savefilesdir") + agentCode + "." + ext);
                      		
                      	}
                      	
                      	if(fildest.exists()){
                      		fildest.delete();
                      	}
                      	
                      	if(filz.exists()){
                      	filz.renameTo(fildest);
                      	}
                      	
                         }else{
                        	 if(!parseFirstEmail(getEmail(agentCode)).equals("aa")){
                        		 email.send(); 	 
                        	 }
                      		
                         }
                      
                      status.setText(getString("sent"));

                      task4 = true;
                  } catch (Exception e) {
                      EksportFrame.showError(e);
                  }
              }
              sent = true;
                  return null;
            }
          };

        worker.start();
    }

    public void doManagerTask(){
    	
    	email = new Email(getString("datasent"));
        email.setSendNotice(false);
            	
    	Enumeration enume = agentsHash.elements();
        Vector agents = new Vector();
        while (enume.hasMoreElements()){
        	Agent ag = (Agent)enume.nextElement();
            if(ag.isManager()){
            	agents.addElement(ag);
            }
        }

    	String managerEmail = getProps().getProperty("managerdataemail");
      if(agents.size()==0){    
     
    	if (managerEmail == null){
        managerEmail = JOptionPane.showInputDialog(this, getString("fileemail"));
        }
        
    	   if (managerEmail == null) System.exit(0);
      }
      email.setEncoding("UTF-8");
      email.setFrom(getProps().getProperty("from"));
      email.setSMTP(getProps().getProperty("smtphost"));
      email.setUser(getProps().getProperty("smtpuser"));
      email.setPass(getProps().getProperty("smtppass"));
      email.setPort(getProps().getProperty("smtpport"));
      email.setSubjectFilesTo(driver.getSubjectFilesTo());

      zip = new ZipHandlerPass(EksportPanel.this);

      this.customers2 = driver.getCustomers(null);
       customers = new TreeMap();
     // System.out.println("custsize " + customers2.size() + "   " + agents.size() );
     
     if(agents.size()>0){

        Vector       custProds = driver.getCustomerProductsList(null);

     //   System.out.println("custprodsize  " + custProds.size() + "   " + agents.size() );
     	enume = agents.elements();
     	while(enume.hasMoreElements()){
            zip = new ZipHandlerPass(EksportPanel.this);
         	email = new Email(getString("datasent"));
         	email.setEncoding("UTF-8");
            email.setSendNotice(false);
            email.setFrom(getProps().getProperty("from"));
            email.setSMTP(getProps().getProperty("smtphost"));
            email.setUser(getProps().getProperty("smtpuser"));
            email.setPass(getProps().getProperty("smtppass"));
            email.setPort(getProps().getProperty("smtpport"));
            email.setSubjectFilesTo(driver.getSubjectFilesTo());

     		customers.clear();
     		Agent age = (Agent)enume.nextElement();
     		//System.out.println("Agendinimi" + age.getName()+ customers2.size());
     		Iterator iter = customers2.values().iterator();
     		while(iter.hasNext()){
     		Customer cust = (Customer)iter.next();
     		Iterator agiter = ((TreeMap)cust.getAgents()).values().iterator();

     		while (agiter.hasNext()){
     			String inside =((Agent)agiter.next()).getID();
     			Enumeration en = age.getDubbler().elements();
     			while (en.hasMoreElements()){
     				if (inside.equals((String)en.nextElement())){
     				customers.put(cust.getName(),cust);
     			  }
     		  	}
     		  }
     		}
           
     		CustomerFile(null,true);       
  
     	try{
          ManagerData md = ((Manager)driver).getManagerData();
          zip.addFile("managerdata.txt", md.getClientVisitInfo());
          String cai = md.getClientAgentInfo();
          if (cai != null){
            zip.addFile("clientagent.txt", cai);
          }
        } catch (SQLException e){
          e.printStackTrace();
          showError(e);
        }
        ProductFile(null);
        CustProdFile(custProds,customers);
        File sv = new File("supervisor.txt");
        if(sv.exists()){
            zip.addFile("supervisor.txt");
        	
        }
        

        File svdd = new File("ownlogo.jpg");
        if(svdd.exists()){
            zip.addFile("ownlogo.jpg");                	
        }
        
        File svddw = new File("ownlogo.png");
        if(svddw.exists()){
            zip.addFile("ownlogo.png");                	
        }
        File svddw2 = new File("ownlogo2.png");
        if(svddw2.exists()){
            zip.addFile("ownlogo2.png");                	
        }
        
        
        File svx = new File("questions.txt");
        
        if(svx.exists()){
            zip.addFile("questions.txt");           	
        }
        
        File ssv = new File("update.pro");
        if(ssv.exists()){
            zip.addFile("update.pro");
        	
        }
        File ssvrr = new File("settings/update.pro");
        if(ssvrr.exists()){
            zip.addFile("settings/update.pro");               	
        }
        
        
        File sdv = new File("managerdata.txt");
        if(sdv.exists()){
            zip.addFile("managerdata.txt");               	
        }
        File sdve = new File("ordertype.txt");
        if(sdve.exists()){
            zip.addFile("ordertype.txt");               	
        }
        
        File svw = new File("asksup2.txt");
        if(svw.exists()){
            zip.addFile("asksup2.txt");
        	
        }
        File svcl = new File("clientagent.txt");
        if(svcl.exists()){
            zip.addFile("clientagent.txt");
        	
        }
        File lt = new File("lastthree.txt");
        if(lt.exists()){
          //  zip.addFile("lastthree.txt");
        	
        }
        File of = new File("offer.txt");
        if(of.exists()){
            zip.addFile("offer.txt");
        }
        File mdfj = new File("customercrm.txt");
        if(mdfj.exists()){
            zip.addFile("customercrm.txt");
        	
        }
        
        File vall = new File("values.txt");
        if(vall.exists()){
            zip.addFile("values.txt");
        	
        }

        String assortment = driver.getAssortmentStr(null);
       try{
         ProdGroupfile(null);
         if(assortment != null){
        	 AssortmentFile(null,assortment);
             	 
         }
         
        if(!driver.getDriverVersion().equals("Amer")){
        	 FixedAssortmentFile(null, customers,
                     ( (Discounter) driver).getFixedAssortmentData(),null);
           		
        }

        
       } catch (SQLException e){
       e.printStackTrace();	
       }

        try {
        
        	zip.write("manager",null);
            String zipName = "manager.zip";

            this.email.addAttach(zipName);

            email.setFilesTo(parseFirstEmail(getEmail(age.getID())));

            if(parent.saveFiles){
            	File fildest = new File(getProps().getProperty("savefilesdir") + zipName);
            	File filz = new File(zipName);
            	if(fildest.exists()){
            		fildest.delete();
            	}
            	
            	  String ext = 	getProps().getProperty("extension");
                	//System.out.println("ext " + ext);
                	if(ext != null){
                		fildest = new File(getProps().getProperty("savefilesdir") +   "manager." + ext);
                		
                	}
            	
                	if(fildest.exists()){
                		fildest.delete();
                	}
                	
            	if(filz.exists()){
            	filz.renameTo(fildest);
            	}
            	
               }else{
            	   if(!parseFirstEmail(getEmail("manager")).equals("aa")){
            		   email.send(); 	   
            	   }
            	
               }

        } catch (Exception e) {
            EksportFrame.showError(e);
        }
    
     	}
        
     }else{
     	customers = customers2;

      //  System.out.println("custsize" + customers2.size() + "   " + agents.size() );
        CustomerFile(null,true);    

        try{
        	  System.out.println("manafile starting" );
          ManagerData md = ((Manager)driver).getManagerData();
          System.out.println("manafile done" );
          zip.addFile("managerdata.txt", md.getClientVisitInfo());
          String cai = md.getClientAgentInfo();
          if (cai != null){
            zip.addFile("clientagent.txt", cai);
          }

          System.out.println("clientfile done" );
        } catch (SQLException e){
          e.printStackTrace();
          showError(e);
        }
  
        
        ProductFile(null);
        System.out.println("manaprods done");
        CustProdFile(null);
        System.out.println("manacustprods done");
        String assortment = driver.getAssortmentStr(null);
        File sv = new File("supervisor.txt");
        if(sv.exists()){
            zip.addFile("supervisor.txt");
        	
        }
        File svx = new File("questions.txt");
        
        if(svx.exists()){
            zip.addFile("questions.txt");           	
        }
        
    
        File svdd = new File("ownlogo.jpg");
        if(svdd.exists()){
            zip.addFile("ownlogo.jpg");                	
        }
        
        File svddw = new File("ownlogo.png");
        if(svddw.exists()){
            zip.addFile("ownlogo.png");                	
        }
        
        File svddw2 = new File("ownlogo2.png");
        if(svddw2.exists()){
            zip.addFile("ownlogo2.png");                	
        }
        
        
        File ssv = new File("update.pro");
        if(ssv.exists()){
            zip.addFile("update.pro");
        	
        }
        File ssvrr = new File("settings/update.pro");
        if(ssvrr.exists()){
            zip.addFile("settings/update.pro");               	
        }
        
        File sdv = new File("managerdata.txt");
        if(sdv.exists()){
            zip.addFile("managerdata.txt");               	
        }
        File svw = new File("asksup2.txt");
        if(svw.exists()){
            zip.addFile("asksup2.txt");
        	
        }
        File sdve = new File("ordertype.txt");
        if(sdve.exists()){
            zip.addFile("ordertype.txt");               	
        }
        
        
        File svcl = new File("clientagent.txt");
        if(svcl.exists()){
            zip.addFile("clientagent.txt");
        	
        }
        File lt = new File("lastthree.txt");
        if(lt.exists()){
           // zip.addFile("lastthree.txt");
        	
        }
        File mdfj = new File("customercrm.txt");
        if(mdfj.exists()){
            zip.addFile("customercrm.txt");
        	
        }
        
        File of = new File("offer.txt");
        if(of.exists()){
            zip.addFile("offer.txt");
        }
        File vall = new File("values.txt");
        if(vall.exists()){
            zip.addFile("values.txt");
        	
        }
       try{
         ProdGroupfile(null);
         System.out.println("manaprodgroups done");

         if(!driver.getDriverVersion().equals("Amer")){
        	 FixedAssortmentFile(null, customers,
                     ( (Discounter) driver).getFixedAssortmentData(),null);
              System.out.println("fixedassortment done");
              	 
         }
         if(assortment != null
        )
         AssortmentFile(null,assortment);
       } catch (SQLException e){e.printStackTrace();}

        try {
            zip.write("manager",null);
            String zipName = "manager.zip";

            this.email.addAttach(zipName);

            if(parent.saveFiles){
            	File fildest = new File(getProps().getProperty("savefilesdir") + zipName);
            	File filz = new File(zipName);
            	if(fildest.exists()){
            		fildest.delete();
            	}
            	  String ext = 	getProps().getProperty("extension");
                	//System.out.println("ext " + ext);
                	if(ext != null){
                		fildest = new File(getProps().getProperty("savefilesdir") + "manager." + ext);
                		
                	}
                	if(fildest.exists()){
                		fildest.delete();
                	}
                	
            	if(filz.exists()){
            	filz.renameTo(fildest);
            	}
            	
               }else{
                if(managerEmail != null){
                StringTokenizer st =  new StringTokenizer(managerEmail,";");
                while(st.hasMoreTokens()){
                  email.setFilesTo(st.nextToken());
	              email.send();
                  }
                }

               	
               }

        } catch (Exception e) {
            EksportFrame.showError(e);
        }     	
     }
        
    }

    
    
    protected String getSelectedAgentCode() {
        return (String) agentCodes.elementAt(agentBox.getSelectedIndex());
    }
    
    
    private void PriceMatrixFile(String agentCode, TreeMap customers,Vector pricematrix,HashSet agprods) {

        String fileName = "pricematrix.txt";
        String nomx = null;
      
        System.out.println("PMFILE date " + agentCode + " " +  getProps().getProperty("no.matrixagent"));
System.out.println("Matrix Size " + pricematrix.size());
boolean prods = false;
if(agprods != null && agprods.size() >0){
	prods = true;
	System.out.println("Size agprods " + agprods.size());
}

if (getProps().getProperty("no.matrixagent") != null) {
	  nomx = getProps().getProperty("no.matrixagent") ;
 }


  boolean dostuff = true;

  if(agentCode != null && nomx != null && agentCode.equals(nomx)) {
	  dostuff = false;
  }
          StringBuffer buf = new StringBuffer();
          int count = 0;

          
         if(dostuff) { 
          HashSet done = new HashSet();
       
          Iterator codes = customers.values().iterator();
          while(codes.hasNext()){
            Customer cust = (Customer)codes.next();
            if (cust.hasAgent(agentCode)){
             Enumeration enume= pricematrix.elements();
             while (enume.hasMoreElements()) {
               MatrixLine mline = (MatrixLine) enume.nextElement();
               if (count==0){
                 count++;
               // System.out.println("system linesxxx" + mline.getCustID() + cust.getID() + cust.getPayer());
               }
               boolean prodok = true;
               if(mline.getProdID() != null && mline.getProdID().length()>0 && prods && !agprods.contains(mline.getProdID())){
            	   prodok = false; 
               }
               if(mline.getCustID()!= null && (cust.getID().equals(mline.getCustID()) || cust.getPayer().equals(mline.getCustID()))){
            	 //  System.out.println("system linesxxx" + mline.getCustID() + " "  + cust.getID() +" "+  cust.getPayer()+ " " 
            		//	   + done.contains(cust.getID()) + " " +  done.contains(cust.getPayer()));
              boolean payer = cust.getPayer().equals(mline.getCustID());
            	   if(!done.contains(cust.getID())  && !payer ){
            		if(prodok){
            			buf.append(mline.getString() + lineSeparator);   
            		   }
               	    }
            	   
             	if(payer && !done.contains(cust.getPayer())){
             		if(prodok){
            			buf.append(mline.getString() + lineSeparator);   
            		   }
               	     }
               	   }   		
                }
             
             done.add(cust.getID());
             if(!cust.getPayer().equals(cust.getID())){
             	done.add(cust.getPayer());
             }
             
             if(cust.getGroup()!= null && cust.getGroup().trim().length()>0 && !done.contains(cust.getGroup())){
            	 done.add(cust.getGroup());
            	 StringTokenizer st = new StringTokenizer(cust.getGroup(),"|");
            	 while (st.hasMoreTokens()){
            		 String token = st.nextToken();
            		 if(!done.contains(token)){
            			 done.add(token);	 
            		 }          		
            	 }
             }
             
             if(cust.getCustType()!= null && cust.getCustType().trim().length()>0 && !done.contains(cust.getCustType())){
             	done.add(cust.getCustType());
             	 StringTokenizer st = new StringTokenizer(cust.getCustType(),"|");
            	 while (st.hasMoreTokens()){
            		 String token = st.nextToken();
            		 if(!done.contains(token)){
            			 done.add(token);	 
            		 }          		
            	   }
                 }          
               }
             }
          
          // kui on ainult toodete case
          
          Enumeration enume = pricematrix.elements();
          while (enume.hasMoreElements()) {
            MatrixLine mline = (MatrixLine) enume.nextElement();
            boolean prodok = true;
            if(mline.getProdID() != null && mline.getProdID().length()>0 && prods && !agprods.contains(mline.getProdID())){
         	   prodok = false; 
            }
            
          if((mline.getCustID()== null || mline.getCustID().trim().length() == 0) ){
        	 if(mline.getCliGroup() == null || mline.getCliGroup().trim().length() == 0 ){
        		if(mline.getDiscGroup() == null || mline.getDiscGroup().trim().length() == 0 ){
                    //   System.out.println("system lines" + mline.getCustID() + cust.getID() + "'");
                      // System.out.println("system tooted '" + mline.getCliGroup() + "'");
              		if(prodok){
              			buf.append(mline.getString() + lineSeparator);	
              		}
        	   	}
        	  }
        	}
          }
          
          
          //kliendi gruppide oma
          Enumeration enum2 = pricematrix.elements();
          while (enum2.hasMoreElements()) {
          	
            MatrixLine mline = (MatrixLine) enum2.nextElement();
            boolean prodok = true;
            if(mline.getProdID() != null && mline.getProdID().length()>0 && prods && !agprods.contains(mline.getProdID())){
         	   prodok = false; 
            }
           if((mline.getCustID()== null || mline.getCustID().trim().length() == 0) ){
        	if(mline.getCliGroup() != null && mline.getCliGroup().trim().length() >= 0  
        			){
              //   System.out.println("system lines" + mline.getCustID() + cust.getID() + "'");
                //System.out.println("system lin '" + mline.getCliGroup() + "'");
        		if(done.contains(mline.getCliGroup().trim()) && !done.contains(mline.getString())){
        		//	 System.out.println("system lin2 '" + mline.getCliGroup() + "'" + prodok);
        			if(prodok){
              			buf.append(mline.getString() + lineSeparator);	
              		}
        			done.add(mline.getString());
        		}
            }
           }
          }
        //Dicount gruppide oma   
             enum2 = pricematrix.elements();
          while (enum2.hasMoreElements()) {
          	
            MatrixLine mline = (MatrixLine) enum2.nextElement();
            boolean prodok = true;
            if(mline.getProdID() != null && mline.getProdID().length()>0 && prods && !agprods.contains(mline.getProdID())){
         	   prodok = false; 
            }
          
           if((mline.getCustID()== null || mline.getCustID().trim().length() == 0) ){
        	if(mline.getDiscGroup() != null && mline.getDiscGroup().trim().length() >= 0  
        			){
                // System.out.println("system lines" + mline.getCustID() + cust.getID() + "'");
               // System.out.println("system lin" + mline.getCliGroup() + "'");
        		if(done.contains(mline.getDiscGroup().trim()) && !done.contains(mline.getString())){
        			if(prodok){
              			buf.append(mline.getString() + lineSeparator);	
              		}
        			done.add(mline.getString());
        		}
            }
           }
          }
          
           }
          try {
        	  
              zip.addFile(fileName, buf.toString());
          } catch (Exception e) {
              EksportFrame.showError(e);
          }
      }
//ClientInfoFile
    private void StaticInfoFile(String agentCode) {

        String fileName = "clientstatic.txt";
          StringBuffer buf = new StringBuffer();
          if(agents.get(agentCode)!= null && ((Agent)agents.get(agentCode)).getCustStaticInfo() != null ){
          
          //blabla siis lisada        
               }
             
             try {
             	if(buf.length()>3){
             		zip.addFile(fileName, buf.toString());
                 }
             	} catch (Exception e) {
              EksportFrame.showError(e);
          }
        }

    private void ClientInfoFile(String agentCode, TreeMap customers) {

        String fileName = "clientinfo.txt";
          StringBuffer buf = new StringBuffer();
          Iterator codes = customers.values().iterator();
          while(codes.hasNext()){
            Customer cust = (Customer)codes.next();
            if (cust.hasAgent(agentCode) && cust.getCustinfo() != null){
               CustomerInfo cline = cust.getCustinfo();            
               if(cline.getId()!= null && cline.getId().equals(cust.getID())){
               	buf.append(cline.getStringForCustomerInfoFile() + lineSeparator);   
                 }
               }
             }
             try {
             	if(buf.length()>3){
             		zip.addFile(fileName, buf.toString());
                 }
             	} catch (Exception e) {
              EksportFrame.showError(e);
          }
        }



    
    
    
    private void FixedAssortmentFile(String agentCode, TreeMap customers,Vector pricematrix,HashSet agprods) {
    	Agent agent = (Agent)getAgentsHash().get(agentCode);
    	
    	 boolean prods = false;
         if(agprods != null && agprods.size() >0){
         	prods = true;
         	System.out.println("Size  agprods " + agprods.size());
         }
         
   if( pricematrix != null ){
	   
	   System.out.println("Fixassort size " +  pricematrix.size());
	   String fileName = "fixassort.txt";
       StringBuffer buf = new StringBuffer();
       Iterator codes = customers.values().iterator();
       HashSet done = new HashSet();
       
       Vector asi = (Vector)pricematrix.clone();
     
       
       
       //teeme HasMapi valmis koodidega 
       
       HashMap sordid = new HashMap();
       System.out.println("Sordid alustame ");
      for(int e = 0 ; e < asi.size(); e++){
    	  MatrixLine mline = (MatrixLine) asi.elementAt(e);
    	  String key = mline.getCliGroup();
    	  if(key != null) {
    	  Vector vv = new Vector();
    	  if(sordid.containsKey(key)) {
    		vv = (Vector)sordid.get(key) ;
    	  }
    	  vv.add(mline);
    	  sordid.put(key, vv);
    	  
    	  }
        }
       
       System.out.println("Sordid suurus " + sordid.size());
       
       
       
       
       while(codes.hasNext()){
         Customer cust = (Customer)codes.next();
         String group = cust.getGroup();
         if(group == null){
         	group = " ";
         }
         String type =  cust.getCustType();
         if(type == null){
         	type = " ";
         }
         String sortCode =  cust.getFixSortCode();
         if(sortCode == null){
         	sortCode = " ";
         }
          
         
      if (((cust.getAgents()!= null && cust.hasAgent(agentCode)) || agentCode == null) && cust.fixSortCode.size() > 0){
   		 
    	  
    	  
    	  
    	  // teeme key seti ja käime läbi - kui sobib siis lisame juurde.
    	  
    	  Iterator iterr = sordid.keySet().iterator();
    	  
    	  
    	  while(iterr.hasNext()) {
    		  String sCode = (String)iterr.next();
    		//  System.out.println("sortCode "+ sCode + " '" + sortCode);
    		   if(  sortCode.indexOf(sCode) != -1 ){ 
    	  
    	  
          Iterator enume = ((Vector) sordid.get(sCode)).iterator();
          while (enume.hasNext()) {
            MatrixLine mline = (MatrixLine) enume.next();
           
         
            boolean goes = true;
           
         	  if(agent.getCustsort() != null && agent.getCustsort().get(cust.getID()) != null
         			  && mline.getProdGroup()!= null && mline.getProdGroup().trim().length() > 0){
         		 goes = false;
             	  Vector grps  =  (Vector)agent.getCustsort().get(cust.getID());
             	 
               String prodGroup = null;
      		for(int k = 0;k < grps.size();k++){
      			prodGroup = (String)grps.elementAt(k);
      			if(prodGroup.equals(mline.getProdGroup())){
      				goes = true;
      			}
      		    }	        
         	  } 
         	  
               if(mline.getProdID() != null && mline.getProdID().length()>0 && prods && !agprods.contains(mline.getProdID())){
            	   goes = false;
               }
             
               if(goes) {
            	   String info = mline.getStringForFixedAssortment();
              
               if( !done.contains(info)) {
            	     buf.append(info + lineSeparator);
                     done.add(info);
               }
             }
             
           }
         }
    	   }
          
	  }
    		   
   	 // if(toRemove.size() > 0) {
   		
   	 //  	System.out.println("asi size " + enume.size());
   	  //}
   	 
       }
    //   System.out.println("buffer " + buf.toString());
          try {
          	if(buf.length()>3){
          		zip.addFile(fileName, buf.toString());
              }
          	} catch (Exception e) {
           EksportFrame.showError(e);
       }   
   }
    
      }


    private void LastThreeFile(String agentCode, TreeMap customers,Vector pricematrix) {
    	   if(pricematrix != null){
    		   
    		  System.out.println("LastThree " +  pricematrix.size());
    		   String fileName = "lastthree.txt";
    	       StringBuffer buf = new StringBuffer();
    	       Iterator codes = customers.values().iterator();
    	       Vector done = new Vector();
    	       while(codes.hasNext()){
    	         Customer cust = (Customer)codes.next();
    	         if(cust.hasAgent(agentCode))
    	      
    	         if ((cust.getAgents()!= null && cust.hasAgent(agentCode)) || agentCode == null){
    	          Enumeration enume = pricematrix.elements();
    	          while (enume.hasMoreElements()) {
    	            MatrixLine mline = (MatrixLine) enume.nextElement();
    	              if(mline.getCustID()!= null && mline.getCustID().equals(cust.getID())){
    	            	  
    	            	  if(agentCode.equals("130") && !cust.getCity().equals("99")){
    	            		  buf.append(mline.getStringForLastThree() + lineSeparator);
    	    	               done.add(mline.getStringForLastThree()); 
    	            	  }else if(!agentCode.equals("130")){
    	            		  buf.append(mline.getStringForLastThree() + lineSeparator);
    	    	               done.add(mline.getStringForLastThree()); 
    	            	  }
    	            	
    	              }
    	          }
    	         }
    	       }
    	     //  System.out.println("buffer " + buf.toString());
    	          try {
    	          	if(buf.length()>3){
    	          		zip.addFile(fileName, buf.toString());
    	          	  }
    	          	} catch (Exception e) {
    	           EksportFrame.showError(e);
    	       }   
    	   }
    	    
    	      }


 TreeMap sortimendid = null;

    

    private void AssortmentFile(Agent agent ,String buf) {
 System.out.println("Assortment starting " + new Date().toLocaleString() );
        String fileName = "assortment.txt";
        
        if (driver.getDriverVersion().equals("Movex") || driver.getDriverVersion().equals("AxaBuraLat") 
        		|| driver.getDriverVersion().equals("KMAHorti")  || driver.getDriverVersion().equals("DirectoAgapics")){
        	
           System.out.println("Bufri0 suurus " + buf.length());
           
        	StringBuffer res = new StringBuffer();
        	int cou = 0;
        	int iii = 0;
        	int cou2 = 0;
        	StringTokenizer st = new StringTokenizer(buf,"\n");
            while (st.hasMoreElements()){
            	String line = st.nextToken();
            	cou2++;
        		if(cou2 == 1000){
        			System.out.println(  " " + cou + " " + line);
        		}
            	if(line.indexOf("," + agent.getID() + ",") != -1){
            		 res.append(line.substring(0,line.indexOf(",")) + " \n");
            		cou++;
            		if(cou == 5000){
            			iii++;
            			System.out.println( iii + " " + cou);
            			cou = 0;
            		}
            	}
            }
        	
        	
            System.out.println("Bufri suurus " + res.length());
              try {
                      if(res != null && res.length()>0){
                  zip.addFile(fileName, res.toString());
                      }
              } catch (Exception e) {
                  EksportFrame.showError(e);
              }
        	
        	
        	
        }else{
        	StringBuffer sb = new StringBuffer();
            sb.append(buf);
            
            String asi = sb.toString();
            StringTokenizer st = new StringTokenizer(asi,"\n");
            
           
            
              try {
                      if(buf != null && buf.toString().trim().length()>0){
                  zip.addFile(fileName, buf);
                      }
              } catch (Exception e) {
                  EksportFrame.showError(e);
              }	
        }
        System.out.println("Assortment ending " + new Date().toLocaleString() );  
        
      }



    private void CustomerFile(String agentCode, TreeMap customers) {

      String  fileName = agentCode.toLowerCase() + ".mte";
 
        StringBuffer buf = new StringBuffer("%2" + lineSeparator);
        Iterator     codes = customers.values().iterator();
        ArrayList agCusts = new ArrayList();
        while(codes.hasNext()){
          Customer cust = (Customer)codes.next();
          
          if (cust.hasAgent(agentCode)){
            Agent age = (Agent) cust.getAgents().get(agentCode);
            //	System.out.println(cust.getName() +" agent " + age.getID() + " visit " + age.getVisit()); 
            if( age!=null && age.getVisit() != null && age.getVisit().trim().length()>0){
              cust.setVisitabi(clearVis(age.getVisit()));
            }
            
            if( age!=null && age.getCall() != null && age.getCall().trim().length()>0){
                cust.setCallabi(clearVis(age.getCall()));
            //    System.out.println("klientOK:" + cust.getName() + " " + age.getCall() );
            }
          
            agCusts.add(cust);
          }
        }
        String missingprods = "missing.txt";
        String custdata = "custdata.txt";
        String payments = "payments.txt";
        StringBuffer buf2 = new StringBuffer();
        StringBuffer buf3 = new StringBuffer();
        StringBuffer buf4 = new StringBuffer();
        appendCustMissingInfo(buf2, agCusts.iterator(),agentCode);
        appendCustDataInfo(buf3, agCusts.iterator(),agentCode);
        appendCustPaymentsInfo(buf4, agCusts.iterator(),agentCode);
        appendCustInfo(buf, agCusts.iterator(),agentCode);
        try {            
       

        	zip.addFile(fileName, buf.toString());
            if(buf2.length()>0){
            zip.addFile(missingprods, buf2.toString());
            }
            if(buf3.length()>0){
                zip.addFile(custdata, buf3.toString());
                }
            if(buf4.length()>0){
                zip.addFile(payments, buf4.toString());
                }
        } catch (Exception e) {
            EksportFrame.showError(e);
        }


    }
  private String clearVis(String s){
  	s = s.replace(';',' ');
 	s = s.replace(';',' ');
 	s = s.replace('.',' ');
 	s = s.replace('.',' ');
 	s = s.replace(',',' ');
 	s = s.replace(',',' ');
 	if(s.trim().length()>0){
 		s = s.replaceAll(" ","");	
 	}
 	return s;
  }
    /**
     * Method declaration
     *
     *
     * @param agentCode
     *
     * @see
     */
    private void CustomerFile(String agentCode,boolean isManager) {
        /*String       fileName = getSelectedAgentFirstName().toLowerCase()
                                + ".mte";
      */
      String fileName = null;
   
      String missingprods = "missing.txt";
      StringBuffer buf2 = new StringBuffer();
      String custdata = "custdata.txt";
      StringBuffer buf3 = new StringBuffer();
     // customers = driver.getCustomers(null);
        if (!isManager){
          fileName = agentCode.toLowerCase() + ".mte";
          customers = driver.getCustomers(new Agent(agentCode));
        } else {
          fileName = "manager.mte";
        }
 // System.out.println("filename" + fileName + "  " + customers.size());
        StringBuffer buf = new StringBuffer("%2" + lineSeparator);
        Iterator     codes = customers.values().iterator();
       
        if(isManager && agentCode != null){
        	appendCustInfo(buf, codes,agentCode);	
            appendCustMissingInfo(buf2, codes,agentCode);
            appendCustDataInfo(buf3, codes,agentCode);
        
        }else {
        	appendCustInfo(buf, codes,agentCode);
            appendCustMissingInfo(buf2, codes,agentCode);
            appendCustDataInfo(buf3, codes,agentCode);
        }
        
        
        try {
        	
            zip.addFile(fileName, buf.toString());
            if(buf2.length()>0){
                zip.addFile(missingprods, buf2.toString());
            }
            if(buf3.length()>0){
                zip.addFile(custdata, buf3.toString());
            }
        } catch (Exception e) {
            EksportFrame.showError(e);
        }

 
        try {
          DealerFiles(customers, agentCode);
       
        } catch (SQLException e){
          showError(e);
        }
    }
    
    private void appendCustMissingInfo(StringBuffer buf2, Iterator codes, String agent){
    	//System.out.println("eeee");
    	while (codes.hasNext()) {
    	 Customer cust = (Customer)codes.next();
      
             if(cust.getLastCheckMissingProds().size()>0){
            // 	System.out.println(cust.getLastCheckMissingProds());
                   for(int i = 0;i<cust.getLastCheckMissingProds().size();i++){
			        	
			            buf2.append(cust.getID());
			            buf2.append(EksportPanel.separator);
			
			            buf2.append(cust.getLastCheckDate());
			            buf2.append(EksportPanel.separator);
			
			            buf2.append(cust.getLastCheckMissingProds().elementAt(i));

			            buf2.append(lineSeparator);
                  }
              }
    	 }
    }
    
    private void appendCustDataInfo(StringBuffer buf3, Iterator codes, String agent){
    	
    	while (codes.hasNext()) {
    	 Customer cust = (Customer)codes.next();
      
             if(cust.getCustDataVec().size()>0){
             
                   for(int i = 0;i<cust.getCustDataVec().size();i++){
                	   String data =  Converter.enCodeEst(cust.getCustDataVec().elementAt(i) +"");
                	   if(buf3.indexOf(data) == -1){
                		     buf3.append(data);
                             buf3.append(lineSeparator);   
                	   }			       
                   }
              }
    	 }
    }
  private void appendCustPaymentsInfo(StringBuffer buf4, Iterator codes, String agent){
    	
    	while (codes.hasNext()) {
    	 Customer cust = (Customer)codes.next();
             if(cust.payments != null){
                 	   if(buf4.indexOf(cust.getPayer() + ";" + cust.payments) == -1){
                		     buf4.append(cust.getPayer() + ";" + cust.payments);
                             buf4.append(lineSeparator);   
              }
    	 }
    }}
    
    
    private void appendCustInfo(StringBuffer buf, Iterator codes, String agent){
    String infoflow = "";
      while (codes.hasNext()) {
          Customer cust = (Customer)codes.next();

          buf.append(cust.getID());
          buf.append(EksportPanel.separator);

          String name = cust.getName().replaceAll("\\r\\n|\\r|\\n", " ");
          name = name.replaceAll("Casino", "Cas.");
          name = name.replaceAll("CASINO", "Cas.");
          name = name.replaceAll("casino", "cas.");
          name = name.replaceAll("\"", " ");
          name = name.replaceAll("   ", " ");
          name = name.replaceAll("  ", " ");
	      name = name.replace('*', ' ');
	      name = name.replace('*', ' ');
	      name = name.replace('*', ' ');
	      name = name.replaceAll("  ", " ");
          
          if (name.equals("")) name = " ";
          buf.append(name.replace(';', ':'));

          buf.append(EksportPanel.separator);
          double debt = cust.getDebt();
          if(debt == -3){
        	  debt = 0;
          }
          buf.append(getNF().format(debt).replace(',', '.'));

          buf.append(EksportPanel.separator);
          buf.append(replaceAll(cust.getAddress(), '\n', ' ').replaceAll("\\r\\n|\\r|\\n", " ").replace(';', ':').replaceAll("\"", " "));

          buf.append(EksportPanel.separator);
          buf.append(cust.getDiscount());

          buf.append(EksportPanel.separator);
          
          if(cust.getVisitabi().trim().length() > 0){
          	 buf.append(testSpace(clearVis(cust.getVisitabi())));
          	 cust.setVisitabi(" ");
          }else{
          	buf.append(testSpace(clearVis(cust.getVisit())));
	
          }
          
          
          buf.append(EksportPanel.separator);
          buf.append(testSpace(cust.getGroup()));

          buf.append(EksportPanel.separator);
          buf.append(testSpace(cust.getBlockingDealer()));

          buf.append(EksportPanel.separator);
          buf.append(testSpace(cust.getPrintean()));

          buf.append(EksportPanel.separator);
          buf.append(testSpace(replaceAll(cust.getInfo(), '\n', ' ').replaceAll("\\r\\n|\\r|\\n", " ").replace(';', ':')));
          if(cust.getInfo().startsWith("Degu:")){
        	  infoflow = infoflow + "| |" + cust.getName() + "|" + cust.getInfo().replaceAll("\\r\\n|\\r|\\n", " ");
          }
          buf.append(EksportPanel.separator);
          buf.append((testSpace(cust.getPayer())));

          buf.append(EksportPanel.separator);
          buf.append(cust.getFixDisc());

          buf.append(EksportPanel.separator);
          buf.append(cust.getConfirmEmail().replaceAll("\\r\\n|\\r|\\n", " "));

          buf.append(EksportPanel.separator);
          buf.append(cust.getLogistics());

          buf.append(EksportPanel.separator);
          String block = "0";
          if (cust.isSalesBlock()){
          block = "1";
          }
          if (cust.isSalesBlockT()){
              block = "2";
              }
          if (cust.isSalesBlockPrepay()){
              block = "3";
              }
          if ((cust.isSalesBlockT() || cust.isSalesBlockPrepay()) && agent.contains("HOUSE")){
              block = "1";
              }
          buf.append(block);

          buf.append(EksportPanel.separator);
          buf.append(cust.getCredit());

          buf.append(EksportPanel.separator);
           String show = "1";
           String show2 = "0";

           if (agent != null){
             Agent agent1 = (Agent)getAgentsHash().get(agent);
             if (agent1.isShowmarketinfoall()) {  show = "0";
             }  else if (cust.isShowmarketinfo()) { show = "0";} }

           if(cust.isShowFullPricelist()){ show2 = "1"; }



           buf.append(show);

           buf.append(EksportPanel.separator);
           buf.append(cust.getDistUpTo());

           buf.append(EksportPanel.separator);
           buf.append(show2);

           buf.append(EksportPanel.separator);
           buf.append(cust.getPaymTerm());

           buf.append(EksportPanel.separator);
           buf.append(cust.getUseSecondPricelist());

           buf.append(EksportPanel.separator);
           buf.append(cust.getCustType().replace(';', ':').replaceAll("\\r\\n|\\r|\\n", " "));

           buf.append(EksportPanel.separator);
           buf.append(round( cust.getSold(), 2));

           buf.append(EksportPanel.separator);
           buf.append(round(cust.getBudget(),2));

           buf.append(EksportPanel.separator);
           buf.append(cust.getPersonalData().replace(';', ':').replaceAll("\\r\\n|\\r|\\n", " "));

           buf.append(EksportPanel.separator);
           buf.append(round(cust.getUsedCredit(),2));

           buf.append(EksportPanel.separator);
           buf.append(cust.getLicence());

           buf.append(EksportPanel.separator);
           buf.append(cust.getPrimDeal());


           if (agent != null){
            Agent agent1 = (Agent)getAgentsHash().get(agent);
            if(agent1.isIspilot()){
            	buf.append(EksportPanel.separator);
                buf.append(cust.getJuridics().replaceAll("\\r\\n|\\r|\\n", " "));
                	
            }else{
            	buf.append(EksportPanel.separator);
                buf.append(" ");
                	
            }
            
           }else{
           	buf.append(EksportPanel.separator);
            buf.append(" ");
            	
           }

           buf.append(EksportPanel.separator);
           buf.append(cust.getContractOver());

           buf.append(EksportPanel.separator);
           String today = "0";
           if (cust.isVisitToday()){
           	today = "1"; 
           	}
           
           
           if (agent != null){
               Agent agent1 = (Agent)getAgentsHash().get(agent);
               if(agent1.getTodayCusts() != null){
            	   if( agent1.getTodayCusts().contains(cust.getID()) && cust.isVisitToday()){
            		   today = "1"; 
            		  // System.out.println("T�na " + cust.getName());
            	   }else{
            		   today = "0";
            	   }
            	   
               }
               
              }
           
           
           buf.append(today);

           buf.append(EksportPanel.separator);
           buf.append(cust.getZipcode());

           buf.append(EksportPanel.separator);
           String cc = cust.getCity().replaceAll("\\r\\n|\\r|\\n", " ").replaceAll(";", ":");
           if(cc.trim().startsWith("|")) {
        	   cc = cc.substring(cc.indexOf("|"));
           }
           buf.append(cc);

           buf.append(EksportPanel.separator);
           
           if(cust.getCallabi().trim().length() > 0){
        	 //  System.out.println("Call Customer " + cust.getID() +" "+ cust.getCallabi());
            	 buf.append(testSpace(clearVis(cust.getCallabi())));
            	 cust.setCallabi(" ");
            	
               }else{
            	buf.append(testSpace(clearVis(cust.getCallDay())));
  	        }
           
          
           buf.append(EksportPanel.separator);
           buf.append(cust.getWare()); 
           buf.append(EksportPanel.separator);
           buf.append(cust.getWeekVisit()); 
           buf.append(EksportPanel.separator);
           buf.append(cust.getAverage());//See on keskmise myygi n�itamiseks 
           buf.append(EksportPanel.separator);
           String dd = "0";
           if(cust.isSupervisorFilled()){
           	dd = "1";
           }
           buf.append(dd);//See on lipp kas SV info on see kuu olemas v�i mitte 
         
           
           
           buf.append(EksportPanel.separator);
           String action = "0";
           if (cust.isAdAction()){
           	action = "1"; 
           	}
           buf.append(action);
           buf.append(EksportPanel.separator);
           
           buf.append(cust.getLastBillInfo());//lisatud et Viimati tehtud arve olemust edastada
           buf.append(EksportPanel.separator);
           
           
           buf.append(cust.getRegnr().replaceAll("\\r\\n|\\r|\\n", " "));
           buf.append(EksportPanel.separator);

           buf.append(cust.getVatnr().replaceAll("\\r\\n|\\r|\\n", " "));
           buf.append(EksportPanel.separator);
           

           buf.append(cust.getMaxdisc());
           buf.append(EksportPanel.separator);

           buf.append(cust.getMinsum());
           buf.append(EksportPanel.separator);

           
           String actio = "0";
           if (cust.isSupervisor2Filled()){
           	actio = "1"; 
           	}
           buf.append(actio);
           buf.append(EksportPanel.separator);
           
           
           String actio2 = "0";
           if (cust.isSupervisormustfill()){
           	actio2 = "1"; 
           	}
           buf.append(actio2);
           buf.append(EksportPanel.separator);
           
           //buf.append(cust.getSortCode());
           //buf.append(EksportPanel.separator);
           
           buf.append(cust.getReturnEmail());
           buf.append(EksportPanel.separator);
           

           buf.append(cust.getMaxOrderSum());
           buf.append(EksportPanel.separator);

           buf.append(cust.getFixSortCode());
           buf.append(EksportPanel.separator);
           
           buf.append(cust.getQueryIDs());
           buf.append(EksportPanel.separator);
           
           buf.append(cust.getMainAgent());
           buf.append(EksportPanel.separator);

           
           String maker = "0";
           if (cust.isMakeAmountRounding()){
           	maker = "1"; 
           	}
           buf.append(maker);
           buf.append(EksportPanel.separator);
           
           buf.append(cust.getInfoEmail().replaceAll("\\r\\n|\\r|\\n", " "));
           buf.append(EksportPanel.separator);
           
           buf.append(cust.getVAT());
           buf.append(EksportPanel.separator);
           
           buf.append(cust.getPictEmail());
           buf.append(EksportPanel.separator);
           
           buf.append(cust.getDelDays());
           buf.append(EksportPanel.separator);
           
           buf.append(cust.getChain());
           buf.append(EksportPanel.separator);

           buf.append(cust.getDelExtraDays()+ "");
           buf.append(EksportPanel.separator);

           
           String isPay = "0";
           if (cust.isPayer()){
        	   isPay = "1"; 
           	}
           buf.append(isPay + "");
           buf.append(EksportPanel.separator);

           
           
           String ask = "0";
           if(cust.isAskContactData()){
           	ask = "1";
           }
           buf.append(ask); 
           buf.append(EksportPanel.separator);
           String diff = "0";
           if(cust.diffBack){
           	diff = "1";
           }
           buf.append(diff); 
           buf.append(EksportPanel.separator);
           String gift = "0";
           if(cust.allowGift){
           	gift = "1";
           }
           buf.append(gift); 
           buf.append(EksportPanel.separator);
           
           buf.append(cust.suggAssortCode);
            
           buf.append(EksportPanel.separator);
           String noord = "0";
           if(cust.noOrder){
           	noord = "1";
           }
           
           if ((cust.isSalesBlockT() || cust.isSalesBlockPrepay()) && agent.contains("HOUSE")){
        		noord = "0";
           }
           buf.append(noord); 
           
           buf.append(EksportPanel.separator);
           if(cust.category != null){
        	   buf.append(cust.category); 
           }else{
        	   buf.append(" ");
           }
           
           buf.append(EksportPanel.separator);
           if(cust.level != null){
        	   buf.append(cust.level); 
           }else{
        	   buf.append(" ");
           }
           
           buf.append(EksportPanel.separator);
           if(cust.nfctag != null){
        	   buf.append(cust.nfctag); 
           }else{
        	   buf.append(" ");
           }

           buf.append(EksportPanel.separator);
           if(cust.currency != null){
        	   buf.append(cust.currency); 
           }else{
        	   buf.append(" ");
           }
           
           buf.append(EksportPanel.separator);
           String diff2 = "0";
           if(cust.diffBack2){
           	diff2 = "1";
           }
           buf.append(diff2); 
           
           buf.append(EksportPanel.separator);
           String sms = " ";
           if(cust.setSMS != null){
        	   sms = cust.setSMS;
           }
           buf.append(sms); 
          
           buf.append(EksportPanel.separator);
           String tdeal = " ";
           if(cust.taraDeal != null){
        	   tdeal = cust.taraDeal;
           }
           buf.append(tdeal); 
           
           buf.append(EksportPanel.separator);
           buf.append(cust.bonusAvailable); 
           
           buf.append(EksportPanel.separator);
           String diff5 = "0";
           if(cust.noCashSales){
           	diff5 = "1";
           }
           buf.append(diff5); 
         
           
           buf.append(EksportPanel.separator);
           String diff3 = "0";
           if(cust.needsToBeVisited){
           	diff3 = "1";
           }
           buf.append(diff3); 
           
           buf.append(EksportPanel.separator);
           String bbb = "0";
           if(cust.criticalDebt > 0){
        	   bbb = cust.criticalDebt + "";
           }
           buf.append(bbb); 
           buf.append(EksportPanel.separator);
           String bb = "1";
           if(!cust.topstatus){
        	   bb =  "0";
           }
           buf.append(bb); 
           
           buf.append(EksportPanel.separator);
           bb = "0";
           if(cust.investstatus){
        	   bb =  "1";
           }
           buf.append(bb); 
           
           
           buf.append(EksportPanel.separator);
           buf.append(cust.salescompa); 
           
           buf.append(EksportPanel.separator);
           bb = "0";
           if(cust.statusHoreca){
        	   bb =  "1";
           }
           buf.append(bb); 
           
           buf.append(lineSeparator);
      }

      buf.append("%1" + lineSeparator);
      try{
    	  if(!infoflow.equals("")){
    	    	 parent.getPropsInfo().setProperty("starttext2",  infoflow );
    	    	 parent.savePropsInfo();  
    	  }
      }catch(Exception s){
    	  
      }
    }

    private void ProdGroupfile(Agent agent) throws SQLException{
      TreeMap allProdGrps = driver.getProdGroups();
      TreeMap prodGrps = null;
      TreeMap secondGroups = null;
      TreeMap alternGroups = null;

   //   if(agent != null){
      boolean ok = false;
		if (ok) {
        if (agent.hasProdGroups() && !driver.getDriverVersion().equals("AxaAbestock") && !driver.getDriverVersion().equals("AxaOrklaLV") ) {

          prodGrps = new TreeMap();
          for (Iterator iter = allProdGrps.keySet().iterator(); iter.hasNext(); ) {
            String key = (String) iter.next();
            if (agent.hasProdGroupList(key)) {
              prodGrps.put(key, allProdGrps.get(key));
            }
          }
        }else {
          prodGrps = allProdGrps;
        }
      }else{
        prodGrps = allProdGrps;
      }
       // System.out.println("Teen gruppe" + prodGrps.toString());
      TreeMap sortedMap = new TreeMap();
        for (Iterator iter = prodGrps.keySet().iterator(); iter.hasNext(); ) {
          Object key = iter.next();
          Object val = prodGrps.get(key);
          if(val != null && key != null){
        	  sortedMap.put(val, key);   
          }
         // siin tekib sortedMap kus on key ehk siis value j?rgi sorteeritud
        }

      if (prodGrps.size() > 0){
        StringBuffer sb = new StringBuffer();
         for (Iterator keys = sortedMap.keySet().iterator(); keys.hasNext(); ){

           while (keys.hasNext()) {
             String key = (String) keys.next();
             String val = key;
             if(driver.getDriverVersion().equals("Kurzeme")){
            	val = val.substring(2,key.length());	
            }else if(driver.getDriverVersion().equals("Miesnieks") || driver.getDriverVersion().equals("RivileKepe")){
            	val = val.substring(3,key.length());	
            }else if(driver.getDriverVersion().equals("SkaiteVigesta")){
            	val = val.substring(2,key.length());	
            }else if(driver.getDriverVersion().equals("RivileTridens")){
            	val = val.substring(2,key.length());	
            }
             
             
             if (driver.isTreeGroup()) {
               sb.append( sortedMap.get(key) + ";" + val + ";1;1\n");
             }
             else {
               sb.append(sortedMap.get(key) + ";" + val + ";1\n");
             }
           }
         }
        secondGroups = driver.getProdGroupsSecond();
         if (secondGroups!= null && secondGroups.size() > 0){
           TreeMap sortedMap2 = new TreeMap();
           for (Iterator iter = secondGroups.keySet().iterator(); iter.hasNext(); ) {
             Object key = iter.next();
             Object val = secondGroups.get(key);
             sortedMap2.put(val, key); // siin tekib sortedMap kus on key ehk siis value j?rgi sorteeritud
           }

           for (Iterator ke = sortedMap2.keySet().iterator(); ke.hasNext(); ) {
             while (ke.hasNext()) {
               String ke1 = (String) ke.next();
               sb.append(sortedMap2.get(ke1) + ";" + ke1 + ";2\n");
             }

           }
         }

         if (driver.getDriverVersion().equals("Movex")){
        	
         alternGroups = driver.getProdGroupsAlternative();
         if (alternGroups!= null && alternGroups.size() > 0){
           TreeMap sortedMap2 = new TreeMap();
           for (Iterator iter = alternGroups.keySet().iterator(); iter.hasNext(); ) {
             Object key = iter.next();
             Object val = alternGroups.get(key);
             sortedMap2.put(val, key); // siin tekib sortedMap kus on key ehk siis value j?rgi sorteeritud
           }

           for (Iterator ke = sortedMap2.keySet().iterator(); ke.hasNext(); ) {
             while (ke.hasNext()) {
               String ke1 = (String) ke.next();
               sb.append(sortedMap2.get(ke1) + ";" + ke1 + ";3\n");
             }

           }
         }
         
         
         }
        zip.addFile(FILE_PRODUCT_GROUP, sb.toString());
      }
    }

    
    
    private void agentFile(TreeMap ags) throws SQLException{
       StringBuffer sb = new StringBuffer();
       Iterator enume  = ags.keySet().iterator();
       while(enume.hasNext()){
       	Agent aa = (Agent)ags.get((String)enume.next());
       	
       	String pass = aa.getPassWord();
       	if(pass == null){
       	    pass = " "	;
       	}else {
       		pass = ";" + pass;
        	}
       	String lang = aa.getLanguage();
       	if(lang == null ){
       		lang = " ";
       	}
       	
       	sb.append(aa.getID() + ";" + aa.getName() + ";" + pass + ";" + lang + "\n");
       }
  
       if(sb.length() > 0){
        zip.addFile(FILE_AGENT, sb.toString());
       }
        
      }

    
    
    
    

    private void DealerFiles(TreeMap customers, String agentCode) throws SQLException{



      Iterator custs = customers.values().iterator();
      StringBuffer sb = new StringBuffer();
      TreeMap dealers = driver.getDealers();
      System.out.println("dealers" + dealers);
      if (dealers != null){
        while(custs.hasNext()){
          Customer cust = (Customer)custs.next();

          if (cust.getDealer() != null){
           // System.out.println("klient" + cust.getName());
            String dealer = cust.getDealer().getID();
            if (dealer != null && cust.hasAgent(agentCode)){
              sb.append(cust.getID() + EksportPanel.separator + dealer + lineSeparator);
            }
          }
        }
        zip.addFile(FILE_CUST_DEALER, sb.toString());
     //  System.out.println("dealer" + sb.toString());
        if (dealers.size() > 0){
          Iterator it = dealers.values().iterator();
          StringBuffer sbf = new StringBuffer();
          while(it.hasNext()){
            Dealer dlr = (Dealer)it.next();
            sbf.append(dlr.getID() + EksportPanel.separator + dlr.getName() + lineSeparator);
          }
          if (sbf.length() > 0){
            zip.addFile(FILE_DEALER, sbf.toString());
          }

          DealerProduct dr = driver.getDealerProductsList();
          System.out.println("driver.getDealerProductsList() " + dr);
          
          zip.addFile(FILE_DEALER_PRODUCTS, dr.getCSVData());

          if(driver.getDealerCustomerCSV(agentCode) != null){
            zip.addFile(FILE_DEALER_CUST, driver.getDealerCustomerCSV(agentCode));
          }


          if (driver.getDriverVersion().equals("VS")){
          zip.addFile(FILE_PLAN_BONUS, driver.getPlansBonuses(agentCode).getCSVData());
         }
        }
      }
    }

    private String testSpace(String s){
      if (s != null && s.trim().equals("")){
        return " ";
      } else {
        return s;
      }
    }



    private void DebtFile(String agentCode) throws SQLException{

        String       fileName = "debt.txt";

        // showStatus();
        Vector      debts = driver.getDebt(agentCode);

        StringBuffer buf = new StringBuffer();
       if(debts != null){
         Enumeration enume = debts.elements();
         DateFormat df = DateFormat.getDateInstance();
         while (enume.hasMoreElements()) {
           Debt debt = (Debt) enume.nextElement();
           if (debt.getDuedate() != null) {
             buf.append(debt.getCustID() + separator);
             buf.append(debt.getDoknr() + separator);
             buf.append(df.format(debt.getDuedate()) + separator);
             int i = 0;
             try{
             	i = (int) Double.parseDouble(debt.getDue());
             
             }catch (Exception e){
             	
             }
             buf.append(getNF().format(debt.getSum()) + ";" + i + ";"  +getNF().format(debt.getTara()) +"\n");
           }

         }
       }

        // writeToFile(fileName, buf.toString());
        try {
            //email.addAttach(fileName, buf.toString());
            zip.addFile(fileName, buf.toString());
        } catch (Exception e) {
            EksportFrame.showError(e);
        }
    }

    private void DebtFile(String agentCode, Vector debts, HashMap custHash,HashMap custPayer) throws SQLException{
        String       fileName = "debt.txt";
        System.out.println("Debt Start " + agentCode);
  

        StringBuffer buf = new StringBuffer();
        Enumeration enume = debts.elements();
        //DateFormat df = DateFormat.getDateInstance();
        
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

        while(enume.hasMoreElements()){
          Debt debt = (Debt)enume.nextElement();
          Customer cust = (Customer)custHash.get(debt.getCustID());
           String date = " ";
           if(debt.getDate() != null){
        	   date = df.format(debt.getDate());
           }
           
       //    df.format(debt.getDate());
           
          TreeMap cust2 = (TreeMap)custPayer.get(debt.getCustID());
          TreeMap cust3 = null ;

          String payer = debt.getPayerID();
          if(payer != null){
        	  cust3 =  (TreeMap)custPayer.get(debt.getPayerID()); 	        	  
          }else{
        	  payer = " ";
          }
        //  System.out.println("Debyt " + ((cust != null) && (cust.hasAgent(agentCode))) 
        	//	  +" " +  ((cust2 != null) && (cust2.keySet().contains(agentCode)))
        		//  +" " +  ((cust3 != null) && (cust3.keySet().contains(agentCode)))
        		 // +" " +   (cust != null && cust.getPayer() !=null && payer.trim().length() > 0 && cust.getPayer().equals(payer)) );
          
          if (       (cust != null && cust.hasAgent(agentCode)) 
        		  || (cust2 != null && cust2.keySet().contains(agentCode))
        		  || (cust3 != null && cust3.keySet().contains(agentCode))
        		  ){
        	//  ||  (cust != null  && cust.getPayer() !=null && payer.trim().length() > 0 && cust.getPayer().equals(payer))
           
        	  buf.append(debt.getCustID() + separator);
            buf.append(debt.getDoknr() + separator);
            buf.append(df.format(debt.getDuedate()) + separator);  
            int i = (int)Double.parseDouble(debt.getDue());
            String extra = "";
            if(debt.extraInfo != null) {
            	extra = debt.extraInfo;
            }
            buf.append(getNF().format(debt.getSum()) + ";" + i + ";"  +getNF().format(debt.getTara()) + ";" + date + ";" + payer + ";"  + extra +"\n");
          }
        }

        try {
            if (debts.size() > 0){
              zip.addFile(fileName, buf.toString().replaceAll(";;", "; ;"));
            }
        } catch (Exception e) {
            EksportFrame.showError(e);
        }
        System.out.println("Debt end");
    }



    public final static String replaceAll(String source, char old, char newStr){
      while(source.indexOf(String.valueOf(old)) != -1){
        source = source.replace(old, newStr);
      }
      return source;
    }

    private void ProdStrings(StringBuffer buf,StringBuffer buf2, Iterator codes){
      this.products = new Hashtable();
      
      NN3.setGroupingUsed(false);
      NN3.setMaximumFractionDigits(4);
      NN = getNF();
      NN.setMaximumFractionDigits(3);
      if(this.getDriver().getDriverVersion().equals("MovexLAT")){
      getNF().setMaximumFractionDigits(4);
      }
      while (codes.hasNext()) {
        Product prod = (Product)codes.next();
        
          if(prod.getBarcodes().size()>0){
          	buf2.append(prod.getID());
          	Enumeration enume = prod.getBarcodes().elements();
          	while(enume.hasMoreElements()){
          		buf2.append(EksportPanel.separator);
          		buf2.append((String)enume.nextElement());
          	}
            buf2.append(lineSeparator);
          }
          
        buf.append(prod.getID());
        buf.append(EksportPanel.separator);
       
     //jj   String name3 =  ;
//     /   System.out.println("driver Version " + driver.getDriverVersion() );
        /*}else  if(driver.getDriverVersion().equals("NaviVilandra")){
        	buf.append((prod.getName().replace(';', ':')).substring(5,prod.getName().length()));
        }else  if(driver.getDriverVersion().equals("NaviVilandraSV")){
        	buf.append((prod.getName().replace(';', ':')).substring(5,prod.getName().length()));
        }else  if(driver.getDriverVersion().equals("NaviVilandraMerc")){
        	buf.append((prod.getName().replace(';', ':')).substring(5,prod.getName().length()));*/
        
        if(driver.getDriverVersion().equals("AltiaLAT")){
        	buf.append((prod.getName().replace(';', '-').replace(':', '-')).substring(2,prod.getName().length()));
            	
        }else if(driver.getDriverVersion().equals("Miesnieks")){
        	buf.append((prod.getName().replace(';', '-').replace(':', '-')).substring(5,prod.getName().length()));
        }else if(driver.getDriverVersion().equals("Dansg")){
        	buf.append((prod.getName().replace(';', '-').replace(':', '-')).substring(2,prod.getName().length()));
        }else if(driver.getDriverVersion().equals("AxaJNGLATww")){
        	buf.append(  (prod.getName().replace(';', '-').replace(':', '-')).substring(3,prod.getName().length()));
        }else if(driver.getDriverVersion().equals("SAFSaaremaa")){
        	buf.append((prod.getName().replace(';', '-').replace(':', '-')).substring(3,prod.getName().length()).replaceAll("\\r\\n|\\r|\\n", " "));
        }else if(driver.getDriverVersion().equals("Rakvere")){
        	String add = "";
        	if(prod.getStatusTop().equals("1") && prod.getMustBe().equals("1")){
        		add = "N+T ";
        	}
        	
        	if(prod.getStatusTop().equals("1")){
        		add = "TOP ";        	}
        	if(prod.getMustBe().equals("1")){
        		add = "NT ";
        	} 	

           // System.out.println((add + "'"+  (prod.getName().replace(';', ':'))).trim());
        	buf.append((add + (prod.getName().replace(';', '-').replace(':', '-').replaceAll("\\r\\n|\\r|\\n", " "))).trim());	
       
        }else if(driver.getDriverVersion().equals("ScalaPrikeEst")){
        	String add = "  ";
        	if(prod.getOldName() != null &&prod.getOldName().trim().length() != 0){
        		add = prod.getOldName() +" ";
        	}
        
          	buf.append("   " + add +(prod.getName().trim().replace(';', '-').replace(':', '-').replaceAll("\\r\\n|\\r|\\n", " ")).trim());	
        }else if(driver.getDriverVersion().equals("ScalaPrikeEstCC")){
        	String add = "  ";
        	if(prod.getOldName() != null &&prod.getOldName().trim().length() != 0){
        		add = prod.getOldName() +" ";
        		// System.out.println((add + "'"+  (prod.getName().replace(';', ':'))).trim());
        	}
        
          	buf.append("   " + add +(prod.getName().trim().replace(';', '-').replace(':', '-').replaceAll("\\r\\n|\\r|\\n", " ")).trim());	
        }else if(driver.getDriverVersion().equals("VichiLATSQL")){
        	buf.append( prod.getID() + " " +(prod.getName().trim().replace(';', '-').replace(':', '-').replaceAll("\\r\\n|\\r|\\n", " ")));	
        }else{
        	buf.append(prod.getName().replace(';', '-').replace(':', '-').replaceAll("\\r\\n|\\r|\\n", " ")
        			.replace('²', '2').replace('¹', '1').replace('¼', ' ').replace('¾', ' ').replace('~', ' ').replace('¤', ' '));
        	
        }
        
        
        if (prod.getMainWarehouseStock() != null){
            buf.append( " " + prod.getMainWarehouseStock());
           }
        
        if (prod.getInCases() != 0){
        	String optam = "";
        	if(prod.getOptAmountInUnits() > 0 ){
        		optam = ":" + getNF().format(prod.getOptAmountInUnits());
        	}
        	String minam = "";
        	if(prod.getMinAmountInUnits() > 0 ){
        		minam = "-" + getNF().format(prod.getMinAmountInUnits());
        	}
            if(driver.getDriverVersion().equals("PM")){
            }else{
            	  buf.append(" (" + getNF().format(prod.getInCases()) + optam + minam +")");              	
            }
        }
        if (prod.getStock() != -1){
        	String amountSTR = prod.getStock()+"";
			if(amountSTR.indexOf(".") != -1){
				amountSTR = amountSTR.substring(0,amountSTR.indexOf("."));
			}
          buf.append(" [" + amountSTR + "]");
        }
        if (prod.getOnPallet()  != 0){
        	String lay = "";
        	if(prod.layer > 0) {
        		lay = getNF().format(prod.layer) + ":";
        	}
        buf.append(" <" + lay + getNF().format(prod.getOnPallet()) + ">");
        }

        buf.append(EksportPanel.separator);
        buf.append(NN3.format(prod.getPrice()));

        buf.append(EksportPanel.separator);
       
        buf.append(NN3.format(prod.getInCases()));

        buf.append(EksportPanel.separator);
        buf.append(getNF().format(prod.getStock()));

        buf.append(EksportPanel.separator);
        buf.append(prod.getGroup());


        buf.append(EksportPanel.separator);
        buf.append(prod.getMeasure());

        buf.append(EksportPanel.separator);
        buf.append(prod.getMaxDiscount());

        buf.append(EksportPanel.separator);
        buf.append(prod.getEAN().replaceAll("\r", " ").replaceAll("\n", " ").replace(';', ','));

        buf.append(EksportPanel.separator);
        buf.append((int)prod.getOnPallet());

       buf.append(EksportPanel.separator);
       buf.append(prod.getUnit());

       buf.append(EksportPanel.separator);
       buf.append(prod.getSorting());

       buf.append(EksportPanel.separator);
       if(driver.getDriverVersion().equals("Rakvere")){
       	buf.append("0");
       }else {
        buf.append(prod.getMustBe());       	
       }

       buf.append(EksportPanel.separator);
       buf.append(prod.getVat());

       buf.append(EksportPanel.separator);
       buf.append(prod.getSendSeparate());

       buf.append(EksportPanel.separator);
       buf.append(prod.getGroupSecond());

       buf.append(EksportPanel.separator);
       buf.append(prod.getMargin());

       buf.append(EksportPanel.separator);
       buf.append(prod.getSendExtra());

       buf.append(EksportPanel.separator);
       String diff = "0";
       if(prod.isDiffColor()){
          diff = "1";
       }
       buf.append(diff);

       buf.append(EksportPanel.separator);
       buf.append(prod.getPalSpace());

       buf.append(EksportPanel.separator);
       buf.append(NN3.format(prod.getPriceSecond()));
     
       buf.append(EksportPanel.separator);
       buf.append(prod.getStatusNew());

       buf.append(EksportPanel.separator);
       buf.append(prod.getStatusSellout());

       buf.append(EksportPanel.separator);
       buf.append(prod.getStatusTop());
       
       
       buf.append(EksportPanel.separator);
       buf.append(prod.getStatusDiscount());


       buf.append(EksportPanel.separator);
       buf.append(prod.getPartii().replaceAll("\r", " ").replaceAll("\n", " "));

       buf.append(EksportPanel.separator);
       buf.append(prod.getAlegrupp().replaceAll("\r", " ").replaceAll("\n", " "));


       buf.append(EksportPanel.separator);
       buf.append(prod.getInUnit()+"");
         

       buf.append(EksportPanel.separator);     
       buf.append(prod.getDefectiveList());
    
       buf.append(EksportPanel.separator);
       buf.append(prod.getOptAmountInUnits()+"");
       
       buf.append(EksportPanel.separator);
       buf.append(prod.getPandPakendiKood());
      
       buf.append(EksportPanel.separator);
       buf.append(prod.getOldName()); //namesecond pihukas
       
       buf.append(EksportPanel.separator);
       if(prod.isShowOnSeparateOrderList()){
           buf.append("1");   
       }else{
           buf.append("0");
    	   
       }
       
       buf.append(EksportPanel.separator);
       buf.append(prod.getType());
       buf.append(EksportPanel.separator);
       buf.append(prod.getMinAmountInUnits());
       buf.append(EksportPanel.separator);
       buf.append(prod.getType2());
       buf.append(EksportPanel.separator);
       buf.append(NN3.format(prod.getMinPrice()));
       buf.append(EksportPanel.separator);
       buf.append(prod.getBoxbrutto());
       buf.append(EksportPanel.separator);
       buf.append(prod.getBoxnetto());
       buf.append(EksportPanel.separator);
       buf.append(prod.getPhotoid());
       buf.append(EksportPanel.separator);
       buf.append(prod.getTypeSimi());
       buf.append(EksportPanel.separator);
       
       if(prod.isAllowGift()){
           buf.append("1");   
         //  System.out.println("lubatud " + prod.getID());
       }else{
           buf.append("0");
       }
       buf.append(EksportPanel.separator);

       if(prod.isLabel()){
           buf.append("1");   
       }else{
           buf.append("0");
       }
       buf.append(EksportPanel.separator);

       if(prod.isNoCommas()){
           buf.append("1");   
       }else{
           buf.append("0");
       }
       buf.append(EksportPanel.separator);
       buf.append(prod.getTimeToSell());

       buf.append(EksportPanel.separator);
       if(prod.weblink == null){
    	   buf.append(" ");
       }else{
    	   buf.append(prod.weblink);
       }
       buf.append(EksportPanel.separator);
       buf.append(prod.innerCaseContent);
       
       buf.append(EksportPanel.separator);
       buf.append(prod.getStatusGiftPack());
       
       buf.append(EksportPanel.separator);
       if(prod.licence == null){
    	   buf.append(" ");
       }else{
    	   buf.append(prod.licence);
       }
       buf.append(EksportPanel.separator);
       if(prod.sumNoCount){
           buf.append("1");   
       }else{
           buf.append("0");
       }
       buf.append(EksportPanel.separator);
       buf.append(prod.greendisc);
       
       buf.append(EksportPanel.separator);
       String lay = "0";
      	if(prod.layer > 0) {
   		lay = getNF().format(prod.layer) ;
   	   }
       buf.append(lay);
       
       
       buf.append(EksportPanel.separator);
       buf.append(prod.points);
       
       
       buf.append(EksportPanel.separator);
       if(prod.noOrder){
           buf.append("1");   
       }else{
           buf.append("0");
       }
      

       buf.append(EksportPanel.separator);
       if(prod.suggAle == null) {
    	   buf.append(" ");
        } else {
    	   buf.append(prod.suggAle);
        }
       
       buf.append(EksportPanel.separator);
       buf.append(prod.statusSmallPack);
   
       buf.append(EksportPanel.separator);
       buf.append(prod.getArrivesToStock());
       

       buf.append(EksportPanel.separator);
       buf.append(prod.TAARA_VAT);

       buf.append(EksportPanel.separator);
       buf.append(prod.DEPO_WO_VAT);

       
       buf.append(lineSeparator);
       
       
       this.products.put(prod.getID(), prod);
      }
      
    }
    /**
     * Method declaration
     *
     *
     * @param agentCode
     *
     * @see
     */

    public static String getDateAAAADDMMStr(Calendar cal,int add){
		StringBuffer sb = new StringBuffer();
		cal.add(Calendar.DAY_OF_YEAR,add);
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}

		String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		if (month.length() == 1){month = ("0") + month;}

		sb.append("" + cal.get(Calendar.YEAR));
		sb.append("" + month);
		sb.append(day +"");
		return sb.toString();
	}
    

    public  String getTimeHHpMMpSSStr(long mills){
		StringBuffer sb = new StringBuffer();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(mills);
		String hour = String.valueOf(cal.get(Calendar.HOUR));
		if (hour.length() == 1) {hour = "0" + hour;}

		String minute = String.valueOf(cal.get(Calendar.MINUTE ) + 1);
		if (minute.length() == 1){minute = ("0") + minute;}

		String second = String.valueOf(cal.get(Calendar.SECOND ) + 1);
		if (second.length() == 1){second = ("0") + second;}

		sb.append(hour+ "." +  minute + "." + second);
		return sb.toString();
	}
   
   
    public static String getDateMMpDDpAAAAStr(long ca,int add){
		StringBuffer sb = new StringBuffer();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(ca);
		cal.add(Calendar.DAY_OF_YEAR,add);
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}

		String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		if (month.length() == 1){month = ("0") + month;}
		sb.append(day +".");
		sb.append("" + month);
		sb.append("." + cal.get(Calendar.YEAR));
		
		
		return sb.toString();
	}
   
    
   
    
    private void  sendDistributionFile() throws IOException {

    
          System.exit(0);
        
      }

    
    
    
    
    private void sendRoutesOnly() {
    ((Router) driver).renewRoutingCals();
        System.exit(0);
    }

    private void sendRestAll() {
    	 String user =  getProps().getProperty("rest.user");
    	 String   pass = getProps().getProperty("rest.pass");
    	 String   rcompany = getProps().getProperty("rest.companyx");
    	 boolean duplicates = true;
    	 boolean sendpartner = false;
    	 try{
    		 if("false".equals((getProps().getProperty("rest.duplicates")))){
    	 		 duplicates = false;
    	 	 }
    	 }catch(Exception e){
    		 e.printStackTrace();
    	 }
    	 try{
    		 if("true".equals((getProps().getProperty("rest.loadvisitlines")))){
    			    Converter.loadVisitLines("plan", rcompany, user, pass);
    	    	    Converter.loadVisitLines("done", rcompany, user, pass);
    	    	    Converter.loadVisitLines("extra", rcompany, user, pass);
    	 	 }
    	 }catch(Exception e){
    		 e.printStackTrace();
    	 }
    	  
    	 
    	 try{
    		 if("true".equals((getProps().getProperty("rest.sendpartner")))){
    			 sendpartner = true;
    	 	 }
    	 }catch(Exception e){
    		 e.printStackTrace();
    	 }
    	 String what =  getProps().getProperty("rest.what");
    	 System.out.println("restalla duplicates " + duplicates);
    	 if(rcompany != null){
    		if(what == null){
    			 Converter.sendAgentJson(driver.getAgents(), rcompany, false);
        		 Converter.sendProductJson(driver.getProducts(null), rcompany);
        		 Converter.sendCustomerJson(driver.getCustomers(null), rcompany, false,user,pass,duplicates,sendpartner );
        			
    		}else{
    			if(what.equals("prod")){
    				 Converter.sendProductJson(driver.getProducts(null), rcompany);
    		    }
    			if(what.equals("cust")){
    				 Converter.sendCustomerJson(driver.getCustomers(null), rcompany, false,user,pass,duplicates,sendpartner);
    	        			
    			}
    			if(what.equals("custroute")){
   				 Converter.sendCustomerJson(driver.getCustomers(null), rcompany, true,user,pass,duplicates,sendpartner);
   	        			
   			}
        			
    		}
    		
    	 
    	 }
    	    System.exit(0);
        } 
    private void sendRestSales() {
   	 String user =  getProps().getProperty("rest.user");
   	 String   pass = getProps().getProperty("rest.pass");
   	 String   rcompany = getProps().getProperty("rest.companyx");
   	 boolean duplicates = true;
   	 try{
   		 if("false".equals((getProps().getProperty("rest.duplicates")))){
   	 		 duplicates = false;
   	 	 }
   	 }catch(Exception e){
   		 e.printStackTrace();
   	 }
   	 
   	 
   	 if(rcompany != null){
   		if (driver instanceof RestSalesSender){
   		 Converter.sendSalesJson(((RestSalesSender) driver).getSalesData(),rcompany, user, pass);
  	     }
   		}
   	
   	    System.exit(0);
     
    } 
   
    

    private void makeSortExcel() {
    
    	 try{
    		    
    	TreeMap prg1 =	driver.getProdGroups();
    TreeMap prg2 =	driver.getProdGroupsSecond();
   TreeMap prods = driver.getProducts(null);
   TreeMap prodsx = new TreeMap();
	 // teeme productsist uue asje inn et koodi j�rgi oleks k�tte saadav
	Iterator iterr =  prods.keySet().iterator();
	while(iterr.hasNext()){
		String kk = (String)iterr.next();
		Product key = (Product) prods.get(kk);
		if(key.getGroup() != null && key.getGroup().length() > 0 && prg1.get(key.getGroup()) != null ){
			key.setGroupname(""+prg1.get(key.getGroup()));
		}
		if(key.getGroupSecond() != null && key.getGroupSecond().length() > 0 && 
				prg2 != null && 
				prg2.get(key.getGroupSecond()) != null ){
			key.setGroupSecname(""+prg2.get(key.getGroupSecond()));
		}
		prodsx.put(key.getID() , key );
	}
	 
	// System.out.println("prodx size " + prodsx.size());
	
  
   TreeMap custs = driver.getCustomers(null);

   TreeMap custsSort = new TreeMap(); //siis siis need kliendie kellel on sortimendid olemas 
   Vector fixed = null;
   
   Vector usedProds = new Vector(); // mis tooted yldes on olemas sortimentides 
   TreeMap fixedT = new TreeMap(); // fixed klGrupp ja vectorina tooted sees 
   try {
       fixed = ( (Discounter) driver).getFixedAssortmentData();
     } catch (Exception e){
     //  showError(e);
     } 
     System.out.println("parent. FIXED done");
   			
     if(fixed != null){
    
    	 
    	 //teeme fixedist treemapi koodi j�rgi etsaaks hilem korraga kylge panna 
    Enumeration enume = fixed.elements();
    while(enume.hasMoreElements()){
    	MatrixLine ml = (MatrixLine)enume.nextElement();
    	String clGroup = ml.getCliGroup();
    	String prodid = ml.getProdID();
    	if(!usedProds.contains(prodid)){
    		usedProds.add(prodid);
    	}
    
    	Vector mlines = (Vector)fixedT.get(clGroup);
    	if(mlines == null){
    		mlines = new Vector();
    	}
    	
    	mlines.addElement(ml);
    	fixedT.put(clGroup, mlines);
    }
    
    System.out.println(". FIXED done " + fixedT.size() +" " + fixed.size());
   	
    // k�ime kliendid l�bi ja paneme igale kliendile kylge  temale kuuluvad lined
     
    Iterator itera = custs.keySet().iterator();
    while (itera.hasNext()){
    	String cname = (String)itera.next();
    	Customer cust = (Customer)custs.get(cname);
    	//kui tal on fixed yldse kyljes
    	cust.fixedLines = null;
    	if(cust.fixSortCode.size() > 0){
    	Enumeration enu = cust.fixSortCode.elements();
    	while (enu.hasMoreElements()){
    		String code = (String)enu.nextElement();
    		if(fixedT.get(code) != null){
    			 System.out.println(". FIXED donex " + ((Vector)fixedT.get(code)).size() + " " + code);
    			   
    			if(cust.fixedLines != null){
    				cust.fixedLines.addAll((Vector)fixedT.get(code));
    			}else{
    				cust.fixedLines = (Vector)fixedT.get(code);	
    			}
    		
    			custsSort.put(cname, cust);
    		}
         }
    		
    	}
    }
    	 
     }
       		
     System.out.println("Vahe tulemus kliente kelle sortiment: " + custsSort.size() );
       	
   //  V�tame Exceli  template ja loeme sinna tulemused sisse 
     
    	    String fname = driver.getExcelDir() + "sortimentsBase.xlsx";
    	 	
    	     FileInputStream fIPS= new FileInputStream(fname); //Read the spreadsheet that needs to be updated
    	     XSSFWorkbook  wb = new XSSFWorkbook(fIPS);
    	     XSSFSheet worksheet = wb.getSheetAt(0);
    	
    	    // Customer Code	Customer Name	CustomerAddress	CustomerChain	Customer Level	CustomerManagers	Product Code	EAN Code	Description	Group	Group 2	Type	Type2	Unit

    	     
    	     XSSFRow row10 = worksheet.getRow(3);  //0 = row number
    	     Cell ce = row10.getCell(8);
    	     CellStyle my90 = ce.getCellStyle();
    	     
    	    	//my90.setRotation((short)90);
    	 	TreeMap prodasub = new TreeMap();
    	     
    	      //Paneme toote info tabelisse  
    	     
    	 
    	 	for(int i = 0; i < usedProds.size() ; i++ ){
    	    	 Product pro = (Product)prodsx.get(usedProds.elementAt(i));
    	    	 if(pro != null){
    	    		
    	    		 prodasub.put(pro.getID(), 5+i );
    	    	 
    	    		 XSSFRow row1 = worksheet.getRow(5+i);  //0 = row number
    	    	     Cell c = row1.getCell(0);
    	    	    
    	    	          c.setCellValue(pro.getID());
    	    	         
    	    	          String ean = pro.getEAN();
    	    	          if(ean != null){
    	    	        	  c = row1.getCell(1);
    	    	        	  try{
    	    	        		  c.setCellValue(ean);  
    	    	        	  }catch(Exception e){
    	    	        		  
    	    	        	  }
    	    	        	 
    	    	          }
    	    	          
    	    	          c = row1.getCell(2);
    	    	          c.setCellValue(Converter.deCode(pro.getName()));
    	    	         
    	    	          String gr1 = pro.getGroupname();
    	    	          if(gr1 != null){
    	    	        	  c = row1.getCell(3);
        	    	          c.setCellValue(Converter.deCode(gr1));  
    	    	          }
    	    	         
    	    	          String gr2 = pro.getGroupSecname();
    	    	          if(gr2 != null){
    	    	        	  c = row1.getCell(4);
        	    	          c.setCellValue(Converter.deCode(gr2));  
    	    	          }
    	    	         
    	    	          String type = pro.getType();
    	    	          if(type != null){
    	    	        	  c = row1.getCell(5);
    	    	        	  c.setCellValue(Converter.deCode(type));
    	    	          }
    	    	          
    	    	          String type2 = pro.getType2();
    	    	          if(type2 != null){
    	    	        	  c = row1.getCell(6);
    	    	        	  c.setCellValue(Converter.deCode(type2));
    	    	          }
    	    	          
    	    	         String unit = pro.getUnit();
    	    	          if(unit != null){
    	    	        	  c = row1.getCell(7);
    	    	        	  c.setCellValue(unit);
    	    	          }
    	    	 }
    	    	 
    	     }
    	     
    	 	  if(false){
    	 		
    Iterator itt = custsSort.keySet().iterator();
    	     int i = 1;
  	         
    while (itt.hasNext()){
    	
    	String klnimi = (String)itt.next();
    	Customer cc = (Customer)custsSort.get(klnimi);
    	
    	
    	
    	XSSFRow rowo = worksheet.getRow(2);
    	try{
    	Cell c = rowo.getCell(7 + i);
    	c.setCellStyle(my90);
    	try{
    		if(cc.getChain() != null){
        		c.setCellValue(Converter.deCode(cc.getChain()));
        	}	
    	}catch(Exception p){
    		//p.printStackTrace();
    	}
    	
    	
    	XSSFRow row1 = worksheet.getRow(3);  //0 = row number
 	     c = row1.getCell(7 + i);
 	    c.setCellStyle(my90);
  	    
  	try{
  		if(cc.category != null && cc.category.trim().length() > 0){
  			klnimi = klnimi + " " + cc.category;
  			//System.out.println("nimi " +  klnimi);
  		}
  			c.setCellValue(Converter.deCode(klnimi));
   }catch(Exception p){
  		//p.printStackTrace();
  	}
  	    
  	    
    	Iterator itra = cc.fixedLines.iterator();
    	while (itra.hasNext()){
    		MatrixLine ml = (MatrixLine)itra.next();
    		String go = ml.getLineinfo();
    		try{
    			int u = Integer.parseInt(prodasub.get(ml.getProdID())+"");
       		 row1 = worksheet.getRow(u);  //0 = row number
           	 c = row1.getCell(7 + i);
           	 c.setCellValue("1");
           	 if(go.equals("CENTRAL")){
           		 c.setCellValue("2");
   				}
   			 if(go.equals("TOP")){
   				 c.setCellValue("1");	
   				}	
   			// System.out.println("u " + c.getStringCellValue());
    		}catch(Exception t){
    			
    			//t.printStackTrace();
    		}
    	}
    	
    	}catch(Exception p){
    		//p.printStackTrace();
    	}
        	i++;
        }
   	  }
    
    // Teeme Pivot lehekylje 
    
    
    Iterator itt = custsSort.keySet().iterator();
   int  i = 1;
     int y = 0;
        
     
		System.out.println("hakkame tegema pivoti ridu  " + custsSort.keySet().size());
		XSSFSheet worksheet1 = wb.getSheet("Pivot");

		int h = 0;
		while (itt.hasNext()){

String klnimi = (String)itt.next();
Customer cc = (Customer)custsSort.get(klnimi);
y++;

Iterator itra = cc.fixedLines.iterator();
if(y == 50){
	System.out.println("cust lines " + i + " " + cc.fixedLines.size() + " " + cc.getName() + " ");
  y = 0;
  h++;
}


if(h == 20){
	 break;
}
if( cc.fixedLines.size() > 700){
	System.out.println("cust lines over 700 " + i + " " + cc.fixedLines.size() + " " + cc.getName() + " ");
}


if( cc.fixedLines.size() < 700){

while (itra.hasNext()){
	
	MatrixLine ml = (MatrixLine)itra.next();
	worksheet1.createRow(i+1);
	XSSFRow rowo = worksheet1.getRow(i+1);

	rowo.createCell(0);
	rowo.createCell(1);
	rowo.createCell(2);
	rowo.createCell(3);
	rowo.createCell(4);
	rowo.createCell(5);
	rowo.createCell(6);
	rowo.createCell(7);
	rowo.createCell(8);
	rowo.createCell(9);
	rowo.createCell(10);
	rowo.createCell(11);
	rowo.createCell(12);
	rowo.createCell(13);
	rowo.createCell(14);
	
		String agent = "";
		if(cc.getAgents() != null && cc.getAgents().size() > 0){
			Iterator itrax = cc.getAgents().keySet().iterator();
			while(itrax.hasNext()){
				String aa = itrax.next() +"";
				if(aa.indexOf("HOUSE") == -1){
					agent = agent + aa + " " ;	
				}
				
			}
		}
	
	try{
		String go = ml.getLineinfo();
		//XSSFRow rowo = worksheet1.getRow(i+1);
		int u = Integer.parseInt(prodasub.get(ml.getProdID())+"");
	
		Cell c = rowo.getCell(14);
			 c.setCellValue("1");
			 if(go != null){
			 if(go.equals("CENTRAL")){
		  		 c.setCellValue("2");
					}
				 if(go.equals("TOP")){
					 c.setCellValue("1");	
					}	
			 }
		
		 c = rowo.getCell(0);
		 c.setCellValue(Converter.deCode(cc.getID()));
			
	
	     c = rowo.getCell(1);
		 c.setCellValue(Converter.deCode(cc.getName()));
		 c = rowo.getCell(2);
		 c.setCellValue(Converter.deCode(cc.getAddress()));
		 c = rowo.getCell(3);
			 try{
					if(cc.getChain() != null){
						c.setCellValue(Converter.deCode(cc.getChain()));
					}	
				}catch(Exception p){
					//p.printStackTrace();
				}
				
		 c = rowo.getCell(4);
			 try{
					if(cc.category != null && cc.category.trim().length() > 0){
						c.setCellValue(Converter.deCode(cc.category));
					}
						
				}catch(Exception p){
					//p.printStackTrace();
				}
			 c = rowo.getCell(5);
			 
			 
			 try{
				 
				 c.setCellValue(Converter.deCode(agent));
	        	  }catch(Exception e){
	        		  
	        	  }
			
				
			
			 c = rowo.getCell(6);
			 c.setCellValue(Converter.deCode( ml.getProdID()));
			
			 Product pro = (Product)prodsx.get(ml.getProdID());
			
			 if(pro != null){

   	         
   	          String ean = pro.getEAN();
   	          if(ean != null){
   	        	  c = rowo.getCell(7);
   	        	  try{
   	        		  c.setCellValue(ean);  
   	        	  }catch(Exception e){
   	        		  
   	        	  }
   	        	 
   	          }
   	          
   	          c = rowo.getCell(8);
   	          c.setCellValue(Converter.deCode(pro.getName()));
   	         
   	          String gr1 = pro.getGroupname();
   	          if(gr1 != null){
   	        	  c = rowo.getCell(9);
	    	          c.setCellValue(Converter.deCode(gr1));  
   	          }
   	         
   	          String gr2 = pro.getGroupSecname();
   	          if(gr2 != null){
   	        	  c = rowo.getCell(10);
	    	          c.setCellValue(Converter.deCode(gr2));  
   	          }
   	         
   	          String type = pro.getType();
   	          if(type != null){
   	        	  c = rowo.getCell(11);
   	        	 try{
   	     		  c.setCellValue(Converter.deCode(type));  
   	     	  }catch(Exception e){
   	     		 e.printStackTrace(); 
   	     	  }
   	     	
   	        	  
   	          }
   	          
   	          String type2 = pro.getType2();
   	          if(type2 != null){
   	        	  c = rowo.getCell(12);
   	        	 try{
      	     		  c.setCellValue(Converter.deCode(type2));  
      	     	  }catch(Exception e){
      	     		  
      	     	  }  }
   	          
   	       c = rowo.getCell(13);
   	      
   	       String unit = pro.getUnit();

   	    try{
     		  c.setCellValue(unit);  
     	  }catch(Exception e){
     		e.printStackTrace();  
     	  }
     	
	
	
}
			 
			 i++;
}catch(Exception e){
	//System.out.println("uii " + i );
	e.printStackTrace();
	i++;
}
}

}
}
    
    
    
    
    
    
    
    			    fIPS.close(); //Close the InputStream
			        FileOutputStream output_file = 
			        		new FileOutputStream(driver.getExcelDir() + "sortiments/fullSortiment-" + Converter.getDDpMMpYYYYpString(Calendar.getInstance()) + ".xlsx");
			        wb.write(output_file); //write changes
			        output_file.close();  //close the stream
				
			        
    	          
    	        	 
     }catch(Exception r){
    	 r.printStackTrace();
     }
 
   	 
   	    System.exit(0);
       } 
   
    
    private void  saveProductFile() throws IOException {

    	
        String prodstring = driver.getSaveStockInfo();
        
       // System.out.println("prodstring " +prodstring);
        if(prodstring != null && prodstring .length() > 0){
          String fpath = getProps().getProperty("stockrefdir");
          String fileName = "stockref.txt";
          String fileName2 = null;
         //"laoseis" + dateStr +  viacode +".csv"
          
  		if(getProps().getProperty("grupp") != null && !driver.getDriverVersion().equals("SolcraftJun")){
  			fileName = "laoseis" + getDateAAAADDMMStr(Calendar.getInstance() , 0) +  "_" + getProps().getProperty("grupp")+ ".csv";
  			fileName2 = "stockref.txt";
  		}
        
          if (fpath != null) {
            fileName = fpath + fileName;
          }
          File prodFile = new File(fileName);
          if(prodFile.exists() && prodstring.length()> 10){
          	prodFile.delete();
          }
         
         
          
          FileOutputStream fos = new FileOutputStream(prodFile);
          fos.write(prodstring.getBytes());
          fos.close();
          
          	 
          
          
          
          
          if(fileName2 != null){
          	  if (fpath != null) {
                    fileName2 = fpath + fileName2;
                  }
                  prodFile = new File(fileName2);
                  if(prodFile.exists()){
                  	prodFile.delete();
                  }

                  fos = new FileOutputStream(prodFile);
                  fos.write(prodstring.getBytes());
                  fos.close();
                  }
          
          
          File fildest = new File(fpath + "stockref.zip");
          if(fildest.exists()){
       		fildest.delete();
       	}
          if(prodFile.exists()){
          	System.out.println("pikkus " + prodFile.length() );
        	  if(prodFile.length() > 100000){
        		  ZipHandlerPass  zip2 = new ZipHandlerPass(EksportPanel.this);
        		  zip2.addFile("stockref.txt", prodstring);
        		  zip2.write("stockref",null);
        		  fildest = new File(fpath + "stockref.zip");
               	File filz = new File("stockref.zip");                       	
                   	if(fildest.exists()){
                   		fildest.delete();
                   	}
               	if(filz.exists()){
               	filz.renameTo(fildest);
               	}
        		 }  
          }     
        }
        System.exit(0);
      }

    private void  saveDebtFile() throws IOException {
  
        String debtstring = driver.getSaveDebtInfo();
        if(debtstring != null){
          String fpath = getProps().getProperty("debtrefdir");
          String fileName = "debtref.txt";
          if (fpath != null) {
            fileName = fpath + fileName;
          }
          File debtFile = new File(fileName);
          java.io.Writer wr = new FileWriter(debtFile);
          wr.write(debtstring);
          wr.close();
        }
        System.exit(0);
      }
    private TreeMap groupProds = new TreeMap();
    
    private HashSet ProductFile(String agentCode, TreeMap prodsy , DealerProduct stocks){
    	HashSet result = new HashSet();
      String       fileName = "tooted.txt";
      String       fileName2 = "prodanalogs.txt";
      Agent agent = (Agent)getAgentsHash().get(agentCode);
 //     System.out.println("agendi grupid 26 " + agent.getProdGroupList().toString());
  	if(agent.getID().equals("KURZEME1")){
		System.out.println("agendi grupid 2 " + agent.getProdGroupList().toString());
	}
      
      ArrayList agProds = new ArrayList();
      TreeMap vahe = null;
      System.out.println("toodete tegemine");
      TreeMap prods = (TreeMap) prodsy.clone();
      
      Iterator er = prods.values().iterator();
      while (er.hasNext()){
    	  Product prod = (Product)er.next();
    	  String gr = prod.getGroup();
    	  String id = prod.getID();
    	  Vector vvv = new Vector();
    	  if(groupProds.get(gr) != null){
    		  vvv = (Vector)groupProds.get(gr);
    		  if(!vvv.contains(id)){
    			  vvv.add(id);
    		  }
    		  groupProds.put(gr, vvv);
    	  }else{
    		  vvv.add(id);
    		  groupProds.put(gr, vvv);
    	  }
    	  
      }
      if (agent != null){

        boolean hasGroups = agent.hasProdGroups();
        boolean hasDisProds = agent.getDiscountProds() != null;
       System.out.println("agent" + agent.getName() + " " + agent.getObject() +  agent.getObjektmain() 
       + agent.isIspilot() + agent.getProdGroupList().size() + " " + hasGroups);
      //System.out.println("stocks != null : " + (stocks.toString()));
       if(hasDisProds){
    	     for (Iterator iter = prods.values().iterator(); iter.hasNext(); ) {
 	            Product prod = (Product) iter.next();   
 	            //prod.setStatusDiscount("0");
 	            prod.setMaxDiscount(0);
 	             prod.setOldName(" ");
 	            
 	            Enumeration enume = agent.getDiscountProds().elements();
 	            while (enume.hasMoreElements()){
 	            	Toode tt = (Toode)enume.nextElement();
 	            	if(tt.getID().equals(prod.getID())){
 	            	
 	            		prod.setOldName("  %% ");
 	            		prod.setMaxDiscount(tt.getKogus());  //kasutame et max discounti kaasa anda Alecoqi case
 	            	//	System.out.println(prod.getName() + " soodukas !!!!");
 	            	}
 	            }	            
    	    }
        }
       
   //   System.out.println("stocks != null : " + (stocks != null) + stocks.getDealers().contains(agent.getObject()) + " "+ agent.getObject() + " " 
    //	  + stocks.list2.size());
        if (stocks != null && stocks.getDealers().contains(agent.getObject()) ){
        	
          for (Iterator iter = prods.values().iterator(); iter.hasNext(); ) {
            Product prod = (Product) iter.next();
            prod.setStock(-1);
            prod.setMainWarehouseStock(null);
            double stock = stocks.getDealerProductStock(agent.getObject(),prod.getID());
            //System.out.println(agent.getObject() + " Laoseis "+ prod.getName() +" " + stock);
            double mainstock = stocks.getDealerProductStock(agent.getObjektmain(),prod.getID());
             String partii = stocks.getDealerProductLOT(agent.getObject(), prod.getID());
           
           // System.out.println(agent.getObject() + " Laoseismain "+ prod.getName() +" " + mainstock);
            if(mainstock != -1 && mainstock !=0 && agent.isIspilot()){
              int main = Integer.parseInt((mainstock+"").substring(0,(mainstock+"").indexOf(".")));
            prod.setMainWarehouseStock("|" + main + "|");
            }

            if(stock != -1){
                 // System.out.println("prod " + prod.getName() + " " + prod.getStock());
                prod.setStock(stock);
                if(partii != null && partii.trim().length() > 0){
               	 prod.setPartii(partii);
                }
                //System.out.println("prod " + prod.getName() + " " + prod.getStock());
                if(vahe == null){
                vahe = new TreeMap();
                }
                vahe.put(prod.getID(), prod);
              }else{
                if(vahe == null){
                vahe = new TreeMap();
                }

                vahe.put(prod.getID(), prod);
              }
          }
        }

     //   System.out.println("vahe : " + (vahe != null) + " " + vahe);

        if (vahe != null) {
          for (Iterator iter = vahe.values().iterator(); iter.hasNext(); ) {
            Product prod = (Product) iter.next();
           
            if (hasGroups) {
            	
            	
            
                  System.out.println(prod.getAgentGroup() + " " + agent.hasProdGroupList(prod.getAgentGroup()));
              if (agent.hasProdGroupList(prod.getGroup()) || agent.hasProdGroupList(prod.getGroupSecond())
            		  || ( agent.hasProdGroupList(prod.getAgentGroup()) || prod.getAgentGroup().equals("all"))
            		  ||   agent.hasProdGroupList(prod.getID()) 
            		  ) {

               // System.out.println(prod.getName());
                agProds.add(prod);
                result.add(prod.getID());
              }
            }
            else {
              agProds.add(prod);
              result.add(prod.getID());
            }
          }
          
        } else {
          
        	for (Iterator iter = prods.values().iterator(); iter.hasNext(); ) {
            Product prod = (Product) iter.next();
            if (hasGroups) {
            	
            //	if(!agent.hasProdGroupList(prod.getAgentGroup()))
            	//    System.out.println( agent.getID() + " " + prod.getID() + " " + hasGroups
            	  //  		+ " " + prod.getAgentGroup() + " " + agent.hasProdGroupList(prod.getAgentGroup()) + " " + agent.prodGroupList);
                
            	if (agent.hasProdGroupList(prod.getGroup()) || agent.hasProdGroupList(prod.getGroupSecond()) || 
            		 ( agent.hasProdGroupList(prod.getAgentGroup()) || prod.getAgentGroup().equals("all")) ||   agent.hasProdGroupList(prod.getID())  ) {
                agProds.add(prod);
                result.add(prod.getID());
              }
            }  else {
              agProds.add(prod);
              result.add(prod.getID());
            }
          }
        }
 // vaat siin on agProds mis on vaja sorterida prod.getname j?rgi
        System.out.println(" agProds hashxx " + result.size());
        if(!"true".equals(getProps().getProperty("use.prodsorting"))) {
        
           java.util.Collections.sort(agProds, new java.util.Comparator(){
	 

        public boolean equals(Object obj) {
          return false;
        }

     
        public int compare(Object o1, Object o2) {
          Product prod1 = (Product)o1;
          Product prod2 = (Product)o2;
          return prod1.getNameSorting().compareTo(prod2.getNameSorting());
        }
      });
        }
 
 
        StringBuffer buf = new StringBuffer();
        StringBuffer buf2 = new StringBuffer();
        Iterator     codes = agProds.iterator();
        ProdStrings(buf,buf2, codes);
        try {
            zip.addFile(fileName, buf.toString().replaceAll(";;", "; ;").replaceAll("²", "2"));
            if(buf2.length()>0){
            	zip.addFile(fileName2, buf2.toString().replaceAll(";;", "; ;").replaceAll("²", "2"));	
            }
            
        } catch (Exception e) {
            EksportFrame.showError(e);
        }

      }
     
      System.out.println("toodete tegemine l�ppes");
      return result;
    }


    private void ProductFile(String agentCode) {
        String       fileName = "tooted.txt";
        // showStatus();
        TreeMap      products = null;
        if (agentCode == null){
          products = driver.getProducts(null);
        } else {
          products = driver.getProducts((Agent)agentsHash.get(agentCode));
        }
        // LTT
        //TreeMap      products = driver.getProducts(agentCode);
//System.out.println("1" + products.size());
        StringBuffer buf = new StringBuffer();
        StringBuffer buf2 = new StringBuffer();
        Iterator     codes = products.values().iterator();
        ProdStrings(buf,buf2, codes);

        try {

            zip.addFile(fileName, buf.toString().replaceAll(";;", "; ;").replaceAll("²", "2"));

        } catch (Exception e) {
            EksportFrame.showError(e);
        }

    }

    private void CustProdFile(String agentCode, Vector custProds, HashMap custKeyList, HashSet agprods) {
      Agent agent = (Agent)getAgentsHash().get(agentCode);
    
      boolean prods = false;
      if(agprods != null && agprods.size() >0){
      	prods = true;
      	System.out.println("Size  agprods " + agprods.size());
      }
        String       fileName = "klienditoode.txt";
        StringBuffer buf = new StringBuffer();
        Enumeration  custCodes = custProds.elements();
        TreeMap customers = new TreeMap();
        System.out.println("cpsize " + custProds.size() + " " + agentCode);
        while (custCodes.hasMoreElements()) {
            CustProd cp = (CustProd)custCodes.nextElement();
            String custID = cp.getCustID();
//             System.out.println("custKeyList size :" + custKeyList.size());


            Customer cust = (Customer)custKeyList.get(custID);

            boolean goes = true;
            
       	  if(agent != null && cust != null && agent.getCustsort() != null && agent.getCustsort().get(cust.getID()) != null){
       		 goes = false;
           	  Vector grps  =  (Vector)agent.getCustsort().get(cust.getID());
           	 
             String prodGroup = null;
    		for(int k = 0;k < grps.size();k++){
    			prodGroup = (String)grps.elementAt(k);
    			if(prodGroup.equals(cp.getGroup())){
    				goes = true;
    			}
    		    }	        
       	  } 
      // 	if(cust != null && cust.getID().indexOf("1001") != -1) {
       	//	System.out.println("cust.kood: '"+cust.getID()+"'" + cust.getAgents() + " " + agentCode);
       //	}
            
            
            if (cust != null && goes){
              if (cust.hasAgent(agentCode)){
            
                customers.put(custID, cust);
                String prodID = cp.getProdID();
                
                if( prods && !agprods.contains(prodID)){
             	   goes = false; 
                }
                if(goes){
                Product prod = (Product)products.get(prodID);
                if (prod == null){
                  prod = new Product(prodID);
                  prod.setName("<tundmatu toode: " + prodID + ">");
                }
              
                if (agent.hasProdGroups()){
                //	System.out.println(agent.getName() + agent.getProdGroupList().toString()+ "' nnn '" +prod.getGroup()+ "'");
                  if (agent.hasProdGroupList(prod.getGroup()) || agent.hasProdGroupList(prod.getGroupSecond())
                		  ||  ( agent.hasProdGroupList(prod.getAgentGroup()) || prod.getAgentGroup().equals("all"))
                		  ||   agent.hasProdGroupList(prod.getID())){

                //	System.out.println(agent.getName() + prodID);
                		   products.put(prodID, prod);
                           cust.addProduct(prod, cp,isWare());	  
                  }
                } else {
                	 
                //		 System.out.println("lisanjuurde "  +cust.getID() + " " + prodID);
                		 products.put(prodID, prod);
                         cust.addProduct(prod, cp,isWare());	 
                	 
                 
               //   System.out.println("lisanjuurde "  +cust.getID() + " " + prodID);
                }
                }

              }
            }

        }

        Iterator iter = customers.values().iterator();
        while (iter.hasNext()){
          Customer sortedCust = (Customer)iter.next();
          
          Iterator sortedProds = sortedCust.getSortedProducts(isWare(),isAbiKood()).values().iterator();
          while (sortedProds.hasNext()){
            ArrayList al = (ArrayList)sortedProds.next();
            Product sortedProd = (Product)al.get(0);
            CustProd cp = (CustProd)al.get(1);
            buf.append(sortedCust.getID());
            buf.append(EksportPanel.separator);
            buf.append(sortedProd.getID());
            buf.append(EksportPanel.separator);
            buf.append(getNF2().format(cp.getLastAmount()));
            buf.append(EksportPanel.separator);
            buf.append(getNF().format(cp.getMovedStockPluss()));
            buf.append(EksportPanel.separator);
            buf.append(getNF().format(cp.getMovedStockMinus()));
            buf.append(EksportPanel.separator);
            buf.append(cp.getLastAmountDate());
            buf.append(EksportPanel.separator);
            buf.append(getNF().format(cp.getLastPrice()));
            buf.append(EksportPanel.separator);
            buf.append(getNF().format(cp.getOpenAmount()));
            buf.append(EksportPanel.separator);
            buf.append(getNF().format(cp.getFixPrice()));
            buf.append(EksportPanel.separator);
            buf.append(getSoldString(cp));
            buf.append(EksportPanel.separator);
            buf.append(cp.getRedline());
            buf.append(EksportPanel.separator);
            buf.append(cp.getAbiKood());
            buf.append(EksportPanel.separator);
            buf.append(cp.ale);
            buf.append(EksportPanel.separator);
            buf.append(cp.returnData);
            buf.append(EksportPanel.lineSeparator);

         }

        }


        // writeToFile(fileName, buf.toString());
        try {
            //email.addAttach(fileName, buf.toString());
        	if(buf.length() > 10)
            zip.addFile(fileName, buf.toString());
        } catch (Exception e) {
            EksportFrame.showError(e);
        }
System.out.println("cp ending");
        // hideStatus();
    }

    public String getSoldString(CustProd cp) {
  	  if(cp.soldString != null && cp.soldString.trim().length() >0){
  		  return cp.soldString;
  	  }
      if(cp.s1.equals("=") && cp.s2.equals("=") && cp.s3.equals("=") && cp.s4.equals("=") && cp.s5.equals("=")  ){
      return " ";
      }
      return cp.s1 + "." + cp.s2 + "." + cp.s3 + "." + cp.s4 + "." + cp.s5 + ".";
    }    
    
    
    private void CustProdMustDistrFile(String agentCode, Vector custProds, HashMap custKeyList, HashSet agprods) {
        Agent agent = (Agent)getAgentsHash().get(agentCode);
      
        boolean prods = false;
        if(agprods != null && agprods.size() >0){
        	prods = true;
        	System.out.println("Size  agprods " + agprods.size());
        }
          String       fileName = "klienditoodedist.txt";
          StringBuffer buf = new StringBuffer();
          Enumeration  custCodes = custProds.elements();
          TreeMap customers = new TreeMap();
          System.out.println("cpsizedist " + custProds.size() + " " + agentCode);
          while (custCodes.hasMoreElements()) {
              CustProd cp = (CustProd)custCodes.nextElement();
              String custID = cp.getCustID();
//               System.out.println("custKeyList size :" + custKeyList.size());


              Customer cust = (Customer)custKeyList.get(custID);

              boolean goes = true;
              
         	  if(agent != null && cust != null && agent.getCustsort() != null && agent.getCustsort().get(cust.getID()) != null){
         		 goes = false;
             	  Vector grps  =  (Vector)agent.getCustsort().get(cust.getID());
             	 
               String prodGroup = null;
      		for(int k = 0;k < grps.size();k++){
      			prodGroup = (String)grps.elementAt(k);
      			if(prodGroup.equals(cp.getGroup())){
      				goes = true;
      			}
      		    }	        
         	  } 
         	
              
            //  System.out.println("cust.kood: '"+cust.getID()+"'");
              if (cust != null){
                if (cust.hasAgent(agentCode)){
              
                  customers.put(custID, cust);
                  String prodID = cp.getProdID();
                  
                  if( prods && !agprods.contains(prodID)){
               	   goes = false; 
                  }
                  Product prod = (Product)products.get(prodID);
                  if (prod == null){
                    prod = new Product(prodID);
                    prod.setName("<tundmatu toode: " + prodID + ">");
                  }

                  if (agent.hasProdGroups()){
                  //	System.out.println(agent.getName() + agent.getProdGroupList().toString()+ "' nnn '" +prod.getGroup()+ "'");
                    if (agent.hasProdGroupList(prod.getGroup()) || agent.hasProdGroupList(prod.getGroupSecond())
                  		  ||  ( agent.hasProdGroupList(prod.getAgentGroup()) || prod.getAgentGroup().equals("all"))
                  		 ||   agent.hasProdGroupList(prod.getID())){

                  //	System.out.println(agent.getName() + prodID);
                  	  if(goes){
                  		   products.put(prodID, prod);
                             cust.addProductDist(prod, cp,isWare());	  
                  	  }
                   
                    }
                  } else {
                  	 if(goes){
                  //		 System.out.println("lisanjuurde "  +cust.getID() + " " + prodID);
                  		 products.put(prodID, prod);
                           cust.addProductDist(prod, cp,isWare());	 
                  	 }
                   
                 //   System.out.println("lisanjuurde "  +cust.getID() + " " + prodID);
                  }

                }
              }

          }

          Iterator iter = customers.values().iterator();
          while (iter.hasNext()){
            Customer sortedCust = (Customer)iter.next();
            
            Iterator sortedProds = sortedCust.getSortedDistProducts(isWare()).values().iterator();
            while (sortedProds.hasNext()){
              ArrayList al = (ArrayList)sortedProds.next();
              Product sortedProd = (Product)al.get(0);
              CustProd cp = (CustProd)al.get(1);
              buf.append(sortedCust.getID());
              buf.append(EksportPanel.separator);
              buf.append(sortedProd.getID());
              buf.append(EksportPanel.separator);
              buf.append(getNF2().format(cp.getLastAmount()));
              buf.append(EksportPanel.separator);
              buf.append(getNF().format(cp.getMovedStockPluss()));
              buf.append(EksportPanel.separator);
              buf.append(getNF().format(cp.getMovedStockMinus()));
              buf.append(EksportPanel.separator);
              buf.append(cp.getLastAmountDate());
              buf.append(EksportPanel.separator);
              buf.append(getNF().format(cp.getLastPrice()));
              buf.append(EksportPanel.separator);
              buf.append(getNF().format(cp.getOpenAmount()));
              buf.append(EksportPanel.separator);
              buf.append(getNF().format(cp.getFixPrice()));
              buf.append(EksportPanel.separator);
              buf.append(getSoldString(cp));
              buf.append(EksportPanel.separator);
              buf.append(cp.getRedline());
              buf.append(EksportPanel.separator);
              buf.append(cp.getAbiKood());
              buf.append(EksportPanel.lineSeparator);

           }

          }


          // writeToFile(fileName, buf.toString());
          try {
              //email.addAttach(fileName, buf.toString());
              zip.addFile(fileName, buf.toString());
          } catch (Exception e) {
              EksportFrame.showError(e);
          }
  System.out.println("cp ending");
          // hideStatus();
      }

    private void CustProdFile(Vector custProds, TreeMap custList) {
     
      HashMap custKeyList = new HashMap();
      
      for (Iterator iter = custList.values().iterator() ; iter.hasNext() ;){
        Customer cust = (Customer)iter.next();
        custKeyList.put(cust.getID(), cust);
       }
      
        String       fileName = "klienditoode.txt";
        StringBuffer buf = new StringBuffer();
        Enumeration  custCodes = custProds.elements();
        TreeMap custome = new TreeMap();
        System.out.println("cpsize" + custProds.size());
        while (custCodes.hasMoreElements()) {
            CustProd cp = (CustProd)custCodes.nextElement();
            String custID = cp.getCustID();
            Customer cust = (Customer)custKeyList.get(custID);

            if (cust != null){
            	custome.put(custID, cust);
                String prodID = cp.getProdID();
                Product prod = (Product)products.get(prodID);
                if (prod == null){
                  prod = new Product(prodID);
                  prod.setName("<tundmatu toode: " + prodID + ">");
                  products.put(prodID, prod);
                }
                  cust.addProduct(prod, cp,isWare());
              }
            }

        

        Iterator iter = custome.values().iterator();
        while (iter.hasNext()){
          Customer sortedCust = (Customer)iter.next();
          Iterator sortedProds = sortedCust.getSortedProducts(isWare(),isAbiKood()).values().iterator();
          while (sortedProds.hasNext()){
            ArrayList al = (ArrayList)sortedProds.next();
            Product sortedProd = (Product)al.get(0);
            CustProd cp = (CustProd)al.get(1);
            buf.append(sortedCust.getID());
            buf.append(EksportPanel.separator);
            buf.append(sortedProd.getID());
            buf.append(EksportPanel.separator);
            buf.append(getNF().format(cp.getLastAmount()));
            buf.append(EksportPanel.separator);
            buf.append(getNF().format(cp.getMovedStockPluss()));
            buf.append(EksportPanel.separator);
            buf.append(getNF().format(cp.getMovedStockMinus()));
            buf.append(EksportPanel.separator);
            buf.append(cp.getLastAmountDate());
            buf.append(EksportPanel.separator);
            buf.append(getNF().format(cp.getLastPrice()));
            buf.append(EksportPanel.separator);
            buf.append(getNF().format(cp.getOpenAmount()));
            buf.append(EksportPanel.separator);
            buf.append(getNF().format(cp.getFixPrice()));
            buf.append(EksportPanel.separator);
            buf.append(getSoldString(cp));
            buf.append(EksportPanel.separator);
            buf.append(cp.getRedline());
            buf.append(EksportPanel.separator);
            buf.append(cp.getAbiKood());
            buf.append(EksportPanel.lineSeparator);

         }

        }


        // writeToFile(fileName, buf.toString());
        try {
            //email.addAttach(fileName, buf.toString());
            zip.addFile(fileName, buf.toString());
        } catch (Exception e) {
            EksportFrame.showError(e);
        }

        // hideStatus();
    }

    /**
     * Method declaration
     *
     *
     * @param agentCode
     *
     * @see
     */
    private void CustProdFile(String agentCode) {
        String       fileName = "klienditoode.txt";

        // showStatus();
        Vector       custProds = driver.getCustomerProductsList(agentCode);
        StringBuffer buf = new StringBuffer();
        Enumeration  custCodes = custProds.elements();
        TreeMap customers = new TreeMap();

        while (custCodes.hasMoreElements()) {
            CustProd cp = (CustProd)custCodes.nextElement();
            String custID = cp.getCustID();

            Customer cust = (Customer)customers.get(custID);
            if (cust == null){
              cust = new Customer(custID);
              customers.put(custID, cust);
            }

            String prodID = cp.getProdID();
            Product prod = (Product)products.get(prodID);
            if (prod == null){
              prod = new Product(prodID);
              prod.setName("<tundmatu toode: " + prodID + ">");
              products.put(prodID, prod);
            }
            cust.addProduct(prod,cp,isWare());
        }

        Iterator iter = customers.values().iterator();
        while (iter.hasNext()){
          Customer sortedCust = (Customer)iter.next();
          Iterator sortedProds = sortedCust.getSortedProducts(isWare(),isAbiKood()).values().iterator();
          while (sortedProds.hasNext()){
            ArrayList al = (ArrayList)sortedProds.next();
            Product sortedProd = (Product)al.get(0);
            CustProd cp = (CustProd)al.get(1);
            buf.append(sortedCust.getID());
            buf.append(EksportPanel.separator);
            buf.append(sortedProd.getID());
            buf.append(EksportPanel.separator);
            buf.append(getNF().format(cp.getLastAmount()));
            buf.append(EksportPanel.separator);
            buf.append(getNF().format(cp.getMovedStockPluss()));
            buf.append(EksportPanel.separator);
            buf.append(getNF().format(cp.getMovedStockMinus()));
            buf.append(EksportPanel.separator);
            buf.append(cp.getLastAmountDate());
            buf.append(EksportPanel.separator);
            buf.append(getNF().format(cp.getLastPrice()));
            buf.append(EksportPanel.separator);
            buf.append(getNF().format(cp.getOpenAmount()));
            buf.append(EksportPanel.separator);
            buf.append(getNF().format(cp.getFixPrice()));
            buf.append(EksportPanel.separator);
            buf.append(getSoldString(cp));
            buf.append(EksportPanel.lineSeparator);

         }

        }


        // writeToFile(fileName, buf.toString());
        try {
            //email.addAttach(fileName, buf.toString());
            zip.addFile(fileName, buf.toString());
        } catch (Exception e) {
            EksportFrame.showError(e);
        }

        // hideStatus();
    }

    /**
     * Method declaration
     *
     *
     * @param fileName
     * @param data
     *
     * @see
     */

    /**
     * Method declaration
     *
     *
     * @see
     */
    private void initAgents() {
        agents = driver.getAgents();

        agentsHash = new Hashtable();
        Iterator iter = agents.values().iterator();
        while (iter.hasNext()){
          Agent ag = (Agent)iter.next();
          agentsHash.put(ag.getID(), ag);
        }

        refreshAgents();
        agentBox.addItemListener(this);

    }

    /**
     * Method declaration
     *
     *
     * @see
     */
    private void refreshAgents() {
        int selected = agentBox.getSelectedIndex();

        agentBox.removeAllItems();


        agentCodes = new Vector();
        agentNames = new Vector();

        Iterator codes = agents.values().iterator();
      
        while (codes.hasNext()) {
            Agent agent = (Agent)codes.next();
            String agentCode = agent.getID();
            String agentName = agent.getName();

            agentCodes.addElement(agentCode);
            agentNames.addElement(agentName);

            String boxLine = (String) agentName + " " + agentCode;
            String email = parseFirstEmail(getEmail((String) agentCode));

            if (!email.equals("")) {
                boxLine = boxLine + " <" + email + ">";
            }

            agentBox.addItem(boxLine);
        }
      //  System.out.println("res 122 " + agentCodes.toString());
        if (selected != -1) {
            agentBox.setSelectedIndex(selected);
        }

        parent.pack();
    }

    /**
     * Method declaration
     *
     *
     * @param emails
     *
     * @return
     *
     * @see
     */
    public String parseFirstEmail(String emails) {
        String		email = "";
        StringTokenizer st =
            new StringTokenizer(emails, String.valueOf(EMAILS_SEPARATOR));

        if (st.hasMoreTokens()) {
            email = st.nextToken();
        }

        return email;
    }

    /**
     * Method declaration
     *
     *
     * @param emails
     *
     * @return
     *
     * @see
     */
    public String parseSecondEmail(String emails) {
        String		email = "";
        StringTokenizer st = new StringTokenizer(emails, ";");

        if (st.hasMoreTokens()) {
            st.nextToken();
        }

        if (st.hasMoreTokens()) {
            email = st.nextToken();
        }

        return email;
    }

    /*public int getAgentObjekt(String agentCode){
      return ((Integer)agentObjekts.get(agentCode)).intValue();
    }*/

  /*  public String getAgentObjekt(String agentCode){
      return (String)agentObjekts.get(agentCode);
    }*/

  /*  public String getAgentAreaDays(String agentCode){
      return (String)agentAreaDays.get(agentCode);
    }*/

    /**
     * Method declaration
     *
     *
     * @param e
     *
     * @see
     */
    public void showError(Exception e) {
        EksportFrame.showError(e);
    }

    public Hashtable getAgentsHash(){
      return this.agentsHash;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public Properties getProps() {
        return parent.getProps();
    }

    /**
     *
     * @return the NumberFormat value of NF.
     */
    public NumberFormat getNF(){
      return NF;
    }

    public NumberFormat getNF2(){
        return NF2;
      }

    /**
     *
     * @param aNF - the new value for NF
     */
    public void setNF(NumberFormat aNF){
      NF = aNF;
    }

    /**
     * Method declaration
     *
     *
     * @param s
     *
     * @see
     */
    public void log(String s) {
        parent.log(s);
    }
    
    public void logerror(Exception s) {
        parent.logerror(s);
    }

    /**
     * Method declaration
     *
     *
     * @param salary
     *
     * @see
     */
 

   public EksportFrame getParentFrame(){
     return this.parent;
   }

   public AbstractDBDriver getDriver(){
     return this.driver;
   }

    private JComboBox	       agentBox = new JComboBox();
    private TreeMap	       agents;
    private Hashtable agentsHash = null;
    private Vector	       agentCodes;
    private Vector	       agentNames;
    private AbstractDBDriver   driver;
    private JButton	       genButton = null;
    private JButton	       muuda = null;
    public String    requestData= null;
    private JLabel	       status = new JLabel(" ");
    public static final char   separator = ';';
    public static final String lineSeparator =
        System.getProperty("line.separator");
    private JProgressBar       progressBar =
        new JProgressBar(SwingConstants.HORIZONTAL, 0, 100);
    public static final char   EMAILS_SEPARATOR = ';';
    public static final String EXPORT_DIR = "eksport";
    private Email	       email;
    private boolean	       task1, task2, task3, task4 = false;
    private Timer	       timer;
    private JPanel	       southPanel = new JPanel();
    private EksportFrame       parent;
    public static final Color  BACKGROUND_COLOR = new Color(160, 204, 207);
    private ZipHandlerPass zip = null;
 

    private JCheckBox sendToAll = null;
    private boolean sent = false;
    private JButton promoPersButton = null;
    private JButton promoButton = null;
    private JButton startButton = null;
    private JButton closeBtn = null;
    private JPanel centerPanel = null;
    private MemoPanel mp = null;
    private Hashtable products = null;
    private  TreeMap customers2 = null;
    private  TreeMap customers = null;

    /**
     *
     * @return the Hashtable value of products.
     */
    public Hashtable getProducts(){
      return products;
    }

    public static final String FILE_CUST_DEALER = "kliendidiiler.txt";
    public static final String FILE_DEALER = "diiler.txt";
    public static final String FILE_DEALER_CUST = "diileriklient.txt";
    public static final String FILE_DEALER_PRODUCTS = "diileritoode.txt";
    public static final String FILE_PRODUCT_GROUP = "prodgroup.txt";
    public static final String FILE_PLAN_BONUS = "planbonus.txt";
    public static final String FILE_AGENT = "agent.txt";
    private boolean ware = false;
    public boolean isWare() {
		return ware;
	}

	public void setWare(boolean abiKood) {
		this.ware = abiKood;
	}
    
    private boolean abiKood = false;
  
    public boolean isAbiKood() {
		return abiKood;
	}
    
  
	public void setAbiKood(boolean abiKood) {
		this.abiKood = abiKood;
	}



	   private NumberFormat NN3 = NumberFormat.getInstance(Locale.ENGLISH);
	private NumberFormat NF = NumberFormat.getInstance(Locale.ENGLISH);
	private NumberFormat NF2 = NumberFormat.getInstance(Locale.ENGLISH);
    private NumberFormat NN = NumberFormat.getInstance(Locale.ENGLISH);
/**
 * @return Returns the error.
 */
public boolean isError() {
	return error;
}
/**
 * @param error The error to set.
 */
public void setError(boolean error) {
	this.error = error;
}
}



/*--- formatting done in "Sun Java Convention" style on 04-24-2002 ---*/

