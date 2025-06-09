package eevaeksport;

import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.driver.AbstractDBDriver;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.driver.AbstractImportDriver;
import eevaeksport.locale.Resource_ee;
import eevaeksport.locale.Resource_en;
import eevaeksport.locale.Resource_fi;
import eevaeksport.locale.Resource_lt;
import eevaeksport.locale.Resource_lv;
import eevaeksport.locale.Resource_ru;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class EksportFrame extends JFrame implements ActionListener{

  /**
	 * files.toUTF=true
	 */
	private static final long serialVersionUID = 8548932712180994137L;
static Category log = Category.getRoot();

  public EksportFrame(String[] args) {

    for (int i = 0 ; i < args.length ; i++){
      if (args[i].trim().equalsIgnoreCase("/auto")){
        this.autoSend = true;
      }
      if (args[i].trim().equalsIgnoreCase("/dashboard")){
          this.setDashboardCreator(true);
        }
      if (args[i].trim().equalsIgnoreCase("/webshopper")){
    	  webShopper = true;
          
        }
      if (args[i].trim().equalsIgnoreCase("/webshopper2")){
    	  setWebShopper2(true);
          
        }

      if (args[i].trim().equalsIgnoreCase("/webreporter2")){
    	  setWebReporter2(true);
          
        }

      if (args[i].trim().equalsIgnoreCase("/fixloader")){
          this.setFixLoader(true);
        }
      if (args[i].trim().equalsIgnoreCase("/fixreporter")){
          this.setFixReporter(true);
        }
      if (args[i].trim().equalsIgnoreCase("/webreporter")){
          this.setWebReporter(true);
        }
      if (args[i].trim().equalsIgnoreCase("/sendstock")){
       this.sendStock = true;
     }
      if (args[i].trim().equalsIgnoreCase("/restall")){
          this.sendRestAll = true;
        }
      if (args[i].trim().equalsIgnoreCase("/restsales")){
          this.sendRestSales = true;
        }
      
      if (args[i].trim().equalsIgnoreCase("/sortexcel")){
          this.sortExcel = true;
        }
      if (args[i].trim().equalsIgnoreCase("/restroutecal")){
          this.sendRoutecal = true;
        }
  	
      
      if (args[i].trim().equalsIgnoreCase("/makeoutput")){
        this.makeOutput = true;
      }
      if (args[i].trim().equalsIgnoreCase("/readstock")){
        this.readStock = true;
      }
      
      if (args[i].trim().equalsIgnoreCase("/sendhalf")){
          this.sendHalf = true;
       }
      
      if (args[i].trim().equalsIgnoreCase("/stockref")){
           this.saveStock = true;
           System.out.println("isSaveStock");
         }
      
      if (args[i].trim().equalsIgnoreCase("/distrep")){
          this.distRep = true;
          System.out.println("isSendDist");
        }
      if (args[i].trim().equalsIgnoreCase("/debtref")){
        this.saveDebt = true;
        System.out.println("isSaveDebt");
      }
      if (args[i].trim().equalsIgnoreCase("/autodebt")){
        this.autoDebt = true;
      }
      if (args[i].trim().equalsIgnoreCase("/onrequest")){
          this.onRequest = true;
        }

      if (args[i].trim().equalsIgnoreCase("/daysales")){
        this.daySales = true;
      }

      if (args[i].trim().equalsIgnoreCase("/autoimport")){
        this.autoImport = true;
      }
      
      if (args[i].trim().equalsIgnoreCase("/onrequestimp")){
          this.autoImportReq = true;
        }
      if (args[i].trim().equalsIgnoreCase("/makepictures")){
          this.makePicture = true;
        }

      if (args[i].trim().equalsIgnoreCase("/makepictures2")){
          this.makePicture2 = true;
        }
      
    }

    setTitle("MobileJava");
    initProps();
    initLocale();
    
  
    PropertyConfigurator.configure("log4j.properties");
    JPanel panel = new JPanel(new GridLayout(0,1));

    JMenu setupMenu = new JMenu(getString("setup"));
    setupMenu.setMnemonic('s');
    JMenu langMenu = new JMenu(getString("lang"));
    setupMenu.add(langMenu);
    menubar.add(setupMenu);

    JMenuItem engItem = new JMenuItem("English");
    engItem.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          changeLanguage(LANG_ENGLISH);
        }
      }
      );
    langMenu.add(engItem);

    JMenuItem estItem = new JMenuItem(LANG_EESTI);
    estItem.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          changeLanguage(LANG_EESTI);
        }
      }
      );

    langMenu.add(estItem);
    JMenuItem latItem = new JMenuItem(LANG_LATVIA);
    latItem.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          changeLanguage(LANG_LATVIA);
        }
      }
      );
          langMenu.add(latItem);

      JMenuItem sweItem = new JMenuItem(LANG_SWEDISH);
      sweItem.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e){
            changeLanguage(LANG_SWEDISH);
          }
        }
        );
           langMenu.add(sweItem);


    JMenuItem lietItem = new JMenuItem("Lietuva");
    lietItem.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          changeLanguage(LANG_LITHUANIA);
        }
      }
      );
    langMenu.add(lietItem);
    JMenuItem finItem = new JMenuItem("Suomi");
    langMenu.add(finItem);
    finItem.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
            changeLanguage(LANG_FINNISH);
          }
        }
        );
    JMenuItem rusItem = new JMenuItem("Russian");
    rusItem.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          changeLanguage(LANG_RUSSIAN);
        }
      }
      );

    langMenu.add(rusItem);

    JMenu menu = new JMenu(getString("help"));
    info = new JMenuItem(getString("info"));
    menu.add(info);
    info.addActionListener(this);
    menu.setMnemonic('a');
    menubar.add(menu);
    setJMenuBar(menubar);

    if ("true".equalsIgnoreCase(getProps().getProperty("distributioninfo"))){
        this.useDistributionInfo = true;
        marketInfoPanel = new MarketInfoPanel(this,null);
      }

    if (getProps().getProperty("savefilesdir") != null && getProps().getProperty("savefilesdir").trim().length()>0){
    	saveFiles = true;
    }

    if ("true".equalsIgnoreCase(getProps().getProperty("supervisorinfo"))){
        this.useSupervisorInfo = true;
        marketInfoPanel = new MarketInfoPanel(this,null);
      }
    if ("true".equalsIgnoreCase(getProps().getProperty("marketinfo"))){
        this.useMarketInfo = true;
      }
    

    System.out.println("ismarket " + useMarketInfo );
 
    JTabbedPane tp = new JTabbedPane();
    

    eksportPanel = new EksportPanel(this);
    
    if(this.useMarketInfo){
    	  marketInfoPanel = new MarketInfoPanel(this,this.getExportDriver(eksportPanel));
    	 // marketInfoPanel.setSize(900, 450);
    }
    tp.addTab(getString("repsdata"), eksportPanel);


    //tp.addTab("Tellimuste lisamine", new ImportPanel(this));

    panel.add(tp);
    try {
      importPanel = new ImportPanel(this);
      System.out.println(this.autoImport);
      if (this.autoImport){
        System.out.println("importPanel.doImport()");
        importPanel.doImport();
      }
      tp.addTab(getString("addorders"), importPanel);
    } catch (Exception e){
      System.out.println(getString("running"));
      //e.printStackTrace();
    }

    if (this.useMarketInfo){
      tp.addTab(getString("marketinfo"), marketInfoPanel);
    }

    if (this.useDistributionInfo){
      tp.addTab(getString("distributioninfo"), distributionInfoPanel);
    }
    
    if (this.useSupervisorInfo){
        tp.addTab(getString("supervisorinfo"), supervisorInfoPanel);
      }

    getContentPane().add(panel);
  }

 /* public void addToImportLog(String s){
    importLogPanel.addText(s);
  }*/

  
  private void changeLanguage(String lang){
    getProps().setProperty(PROPS_LANG, lang);
    saveProps();
    try{
      initResource(lang);
    } catch (IOException e){
      showError(e);
    }
    JOptionPane.showMessageDialog(this, resource.getString("restarttext"));
  }

  private void initLocale(){
    try {
      String lang = getProps().getProperty(PROPS_LANG);
      initResource(lang);
    } catch(IOException e){
      showError(e);
    }
  }

  private void initResource(String lang) throws IOException{
    if (lang != null){
      if (lang.equals(LANG_EESTI)){
        resource = new Resource_ee();
      } else if (lang.equals(LANG_LATVIA)){
        resource = new Resource_lv();
      } else if (lang.equals(LANG_RUSSIAN)){
        resource = new Resource_ru();
      } else if (lang.equals(LANG_LITHUANIA)){
        resource = new Resource_lt();
      } else if (lang.equals(LANG_FINNISH)){
        resource = new Resource_fi();
      } else {
        resource = new Resource_en();
      }
    } else {
      getProps().setProperty(PROPS_LANG, "english");
      resource = new Resource_en();
      saveProps();
    }
  }

  public AbstractDBDriver getExportDriver(EksportPanel exp){
    AbstractDBDriver driver = null;
    String name = props.getProperty("driver");
    if (name == null){
      JOptionPane.showMessageDialog(this, getString("missingexp"), getString("error"), JOptionPane.ERROR_MESSAGE);
      System.exit(0);
    } else {
      try {
        String pkg = "eevaeksport.driver.";
        Class cl = Class.forName(pkg + name + "DBDriver");
        Constructor c = cl.getConstructor(new Class[]{exp.getClass()});

        driver = (AbstractDBDriver)c.newInstance(new Object[]{exp});

      } catch (ClassNotFoundException e){
        JOptionPane.showMessageDialog(this, getString("expdriver") + " " + name + " " + getString("notfound"), getString("error"), JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
        System.exit(0);
      } catch (Throwable e){
        JOptionPane.showMessageDialog(this, getString("expdriver2") + " " + name + " " + getString("initerror"), getString("error"), JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
        System.exit(0);
      }
    }
      return driver;
  }

  public AbstractImportDriver getImportDriver(ImportPanel imp) throws ClassNotFoundException{
    AbstractImportDriver driver = null;
    String name = props.getProperty("driver");
    if (name == null){
      JOptionPane.showMessageDialog(this, getString("missingimp"), getString("error"), JOptionPane.ERROR_MESSAGE);
      System.exit(0);
    } else {
      try {
        String pkg = "eevaeksport.imp.driver.";
        Class cl = Class.forName(pkg + name + "ImportDriver");
        Constructor c = cl.getConstructor(new Class[]{imp.getClass()});
        driver = (AbstractImportDriver)c.newInstance(new Object[]{imp});
      } catch (NoSuchMethodException e){
        JOptionPane.showMessageDialog(this, getString("impdriver") + " " + name + " " + getString("initerror"), getString("error"), JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
        System.exit(1);
      } catch (InvocationTargetException ex){
        JOptionPane.showMessageDialog(this, getString("impdriver") + " " + name + " " + getString("initerror"), getString("error"), JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
        System.exit(1);
      } catch (IllegalAccessException ex){
        JOptionPane.showMessageDialog(this, getString("impdriver") + " " + name + " " + getString("initerror"), getString("error"), JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
        System.exit(1);
      } catch (InstantiationException ex){
        JOptionPane.showMessageDialog(this, getString("impdriver") + " " + name + " " + getString("initerror"), getString("error"), JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
        System.exit(1);
      }
    }
      return driver;
  }

  public void actionPerformed(ActionEvent e){
    Object source = e.getSource();
    if (source == info){
      JOptionPane.showMessageDialog(this,
         "Autor: Aivar Merisalu\nProgramm: Tarmo Tubro\nemail: aivar@mobilejava.ee\nCopyright (c) 2002",
           getString("info"), JOptionPane.INFORMATION_MESSAGE);
    }
  }

  public boolean isDebug(){
    String debug = getProps().getProperty("debug");
    if (debug != null && debug.equalsIgnoreCase("true")) return true;
    return false;
  }

  public static Properties loadProps(String fileName){
    Properties props = new Properties();
    try {
      FileInputStream in = new FileInputStream(fileName);
      props.load(in);
      in.close();
    } catch (IOException e){
       // showError(e);
      }
    return props;
  }

  public void saveProps(){
    saveProps(getProps(), INI_FILE);
  }

  public void savePropsInfo(){
	    saveProps(getPropsInfo(), INFO_FILE);
	  }
  public static void saveProps(Properties props, String fileName){
    try {
      FileOutputStream out =
          new FileOutputStream(fileName);
      props.store(out, "");
      out.close();
    } catch (IOException e) {
      showError(e);
    }
  }

  public static void showError(Exception e){
    log.error(e,e);
    e.printStackTrace();
    showError(e.toString());
  }

  private void initProps(){
    this.props = loadProps(INI_FILE);
    this.propsinfo = loadProps(INFO_FILE);
  }

  private static void showError(String s){
    ErrorFrame error = new ErrorFrame(s);
    error.pack();
    error.setVisible(true);
  }

  public Properties getProps(){
    return this.props;
  }
  public Properties getPropsInfo(){
	    return this.propsinfo;
	  }

  public void log(String s){
    log.debug(s);
  }

  public void logerror(Exception s){
    log.error(s,s);
  }
  
  public boolean agents = false;
  public EksportPanel getEksportPanel(){
    return this.eksportPanel;
  }

/*  public void setAutoSend(boolean value){
    this.autoSend = value;
  }*/

  public static  boolean checkRunningCopy(){
	    
		boolean ok = false;
		try {
			 Socket sock = new Socket("localhost", 4445);
			   sock.close();
			   System.out.println("Tï¿½ï¿½tas paneme kinni");
			} catch (IOException e){
				//e.printStackTrace();
			    ok = true;
			    System.out.println("Paneme tï¿½ï¿½le");
			}
			
			 if ( ok ) {
					Runnable run = new SocketServer();
				   Thread th = new Thread(run);
					th.start();			    
				    System.out.println("Panin tï¿½ï¿½le");
			} 
	    System.out.println("Tagastan " + ok);
	    
		return ok;		
	    }
  
  public boolean isAutoSend(){
    return this.autoSend;
  }
  public boolean isSaveStock(){
    return this.saveStock;
  }
  public boolean isAutoDebt(){
    return this.autoDebt;
  }
  public boolean isSendStock(){
     return this.sendStock;
   }
  public boolean isSendRouteCal(){
	     return this.sendRoutecal;
	   }
  public boolean isDaySales(){
    return this.daySales;
  }

  public boolean isAutoImport(){
    return this.autoImport;
  }

  public String getString(String s){
    return resource.getString(s);
  }

  protected Vector getMarketInfo(){
    return marketInfoPanel.getData();
  }
  
  protected Vector getDistributionInfo(){
    return marketInfoPanel.getData();
  }
  
  protected Vector getSupervisorInfo(){
    return marketInfoPanel.getData();
  }
  public boolean useMarketInfo(){
    return this.useMarketInfo;
  }
  
  public boolean useSupervisorInfo(){
    return this.useMarketInfo;
  }
  
  public boolean useDistributionInfo(){
    return this.useMarketInfo;
  }

  private EksportPanel eksportPanel = null;
  public ImportPanel importPanel = null;
  private JMenuItem info;
  private JMenuBar menubar = new JMenuBar();
  private Properties props;
  private Properties propsinfo;
  public static final String INI_FILE = "eksport.ini";
  public static final String INFO_FILE = "info.properties";
  public static final boolean USE_IMPORT_PANEL = true;
  public final static boolean USE_SCHEDULE_BUTTON = false;
  private boolean autoSend = false;
  private boolean fixLoader = false;
  private boolean dashboardCreator = false;
  private boolean fixReporter = false;
  private boolean webReporter = false;
  private boolean webReporter2 = false;
  private boolean saveStock = false;
  private boolean distRep = false;
  public boolean isDistRep() {
	return distRep;
}

public void setDistRep(boolean distRep) {
	this.distRep = distRep;
}

  private boolean saveDebt = false;
  private boolean autoDebt = false;
  private boolean webShopper = false;
  private boolean webShopper2 = false;

  public boolean isWebShopper() {
	return webShopper;
}

public void setWebShopper(boolean webShopper) {
	this.webShopper = webShopper;
}

private boolean onRequest = false;
  private boolean sendStock = false;
  private boolean sendRoutecal = false;
  private boolean sendRestAll = false;
  private boolean sendRestSales = false;
  private boolean sortExcel = false;
  private boolean makeOutput = false;
  private boolean readStock = false;
  private boolean daySales = false;
  private boolean autoImport = false;
  private boolean sendHalf = false;
  public boolean isSendHalf() {
	return sendHalf;
}

public void setSendHalf(boolean sendHalf) {
	this.sendHalf = sendHalf;
}

private boolean autoImportReq = false;
private boolean makePicture = false;
private boolean makePicture2 = false;

  public boolean isMakePicture() {
	return makePicture;
}

public void setMakePicture(boolean makePicture) {
	this.makePicture = makePicture;
}

//private ImportLogPanel importLogPanel;
  private ResourceBundle resource = null;
  public static final String LANG_EESTI = "Estonian";
  public static final String LANG_LATVIA = "Latvian";
  public static final String LANG_RUSSIAN = "Russian";
  public static final String LANG_ENGLISH = "English";
  public static final String LANG_SWEDISH = "Swedish";
  public static final String LANG_LITHUANIA = "Lithuanian";
  public static final String LANG_FINNISH = "Finnish";

  public static final String PROPS_LANG = "language";
  private MarketInfoPanel marketInfoPanel = null;
  private DistributionInfoPanel distributionInfoPanel = null;
  private SupervisorInfoPanel supervisorInfoPanel = null;
  private boolean useDistributionInfo = false;
  private boolean useSupervisorInfo = false;
  private boolean useMarketInfo = false;
  public boolean saveFiles = false;
/**
 * @return Returns the marketInfoPanel.
 */
public MarketInfoPanel getMarketInfoPanel() {
	return marketInfoPanel;
}
/**
 * @param marketInfoPanel The marketInfoPanel to set.
 */
public void setMarketInfoPanel(MarketInfoPanel marketInfoPanel) {
	this.marketInfoPanel = marketInfoPanel;
}
/**
 * @return Returns the saveDebt.
 */
public boolean isSaveDebt() {
	return saveDebt;
}
/**
 * @param saveDebt The saveDebt to set.
 */
public void setSaveDebt(boolean saveDebt) {
	this.saveDebt = saveDebt;
}
/**
 * @return Returns the makeOutput.
 */
public boolean isMakeOutput() {
	return makeOutput;
}
/**
 * @param makeOutput The makeOutput to set.
 */
public void setMakeOutput(boolean makeOutput) {
	this.makeOutput = makeOutput;
}
/**
 * @return Returns the readStock.
 */
public boolean isReadStock() {
	return readStock;
}
/**
 * @param readStock The readStock to set.
 */
public void setReadStock(boolean readStock) {
	this.readStock = readStock;
}

/**
 * @return the onRequest
 */
public boolean isOnRequest() {
	return onRequest;
}

/**
 * @param onRequest the onRequest to set
 */
public void setOnRequest(boolean onRequest) {
	this.onRequest = onRequest;
}

public void setAutoImportReq(boolean autoImportReq) {
	this.autoImportReq = autoImportReq;
}

public boolean isAutoImportReq() {
	return autoImportReq;
}

public boolean isMakePicture2() {
	return makePicture2;
}

public void setMakePicture2(boolean makePicture2) {
	this.makePicture2 = makePicture2;
}

public boolean isSendRestAll() {
	return sendRestAll;
}

public void setSendRestAll(boolean sendRestAll) {
	this.sendRestAll = sendRestAll;
}

public boolean isSortExcel() {
	return sortExcel;
}

public void setSortExcel(boolean sortExcel) {
	this.sortExcel = sortExcel;
}

public boolean isSendRestSales() {
	return sendRestSales;
}

public void setSendRestSales(boolean sendRestSales) {
	this.sendRestSales = sendRestSales;
}

public boolean isFixLoader() {
	return fixLoader;
}

public void setFixLoader(boolean fixLoader) {
	this.fixLoader = fixLoader;
}
public boolean isFixReporter() {
	return fixReporter;
}

public void setFixReporter(boolean fixReporter) {
	this.fixReporter = fixReporter;
}

public boolean isWebReporter() {
	return webReporter;
}
public boolean isWebReporter2() {
	return webReporter2;
}


public void setWebReporter(boolean webReporter) {
	this.webReporter = webReporter;
}

public void setWebReporter2(boolean webReporter2) {
	this.webReporter2 = webReporter2;
}

public boolean isDashboardCreator() {
	return dashboardCreator;
}

public void setDashboardCreator(boolean dashboardCreator) {
	this.dashboardCreator = dashboardCreator;
}

public boolean isWebShopper2() {
	return webShopper2;
}

public void setWebShopper2(boolean webShopper2) {
	this.webShopper2 = webShopper2;
}
}