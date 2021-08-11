package eevaeksport.imp.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.imp.POP3Reader;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class VSuusImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public VSuusImportDriver(ImportPanel parent) {
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "eeva");

      POP3Reader pop3 = new POP3Reader(parent.getEksportFrame().getString("moborder"));
      pop3.setPop3host(parent.getProps().getProperty("pop3.host"));
      pop3.setUser(parent.getProps().getProperty("pop3.user"));
      pop3.setPassword(parent.getProps().getProperty("pop3.password"));
      pop3.setSaveDir(getImportFile());
      pop3.readFiles(true);
    } catch (Exception e){
       parent.showError(e);
    }
    loadSignatures();
    }
    private Vector signatures = new Vector();

    private void loadSignatures(){
		
  	  try{		
  	  		File f = new File("signatures.vec");
  	  		Calendar ss = Calendar.getInstance();
  	  		
  	  		if (f.exists() && f.lastModified() < ss.getTimeInMillis() - 3600000) {// kui on yle tunni vana siis maha 
  	  			f.delete();
  	  			signatures = new Vector();
  	  		} else {
  	  			signatures = (Vector) readObject("signatures.vec");
  	  		}

  	  		
  	  	} catch (Exception e) {
  	  		signatures = new Vector();
  	  	}

  	  	}
  public String identifyCustomer(String id,String regnr){
    return null;
  }
  
	 public static Object readObject(String s) throws IOException,
ClassNotFoundException, IOException {
Object obj = null;
File f = new File(s);
if (f.exists()){
ObjectInputStream ois = new ObjectInputStream(new FileInputStream(s));
obj = ois.readObject();
ois.close();
}
return obj;
}

  
  public String identifyProduct(String ean, String prodID){
    return null;
  }

  public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
  
  public void makeOrder(Order order) throws SQLException{
    setOrder(order);
    
    String signature = order.getSignature();
    if(signatures == null){
    	signatures = new Vector();
    }
	  if(!signatures.contains(signature)){
		    int invoiceID = getNextID();
		    addInvoiceHeader(invoiceID);
		    addInvoiceLines(invoiceID); 
		    signatures.add(signature);
	  }
	  try {
			saveObject(signatures, "signatures.vec");
		} catch (IOException e) {
			e.printStackTrace();
		}
	  
   }

  
  
  
  private int getNextID() throws SQLException{
    String sql = "select seeralgus from alusdok where alusdok='LG'";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);
    int intNr = -1;
    
    while(rs.next()){
      intNr = rs.getInt("seeralgus");
      intNr++;
    }
    
    String sql2 = "update alusdok set seeralgus=" + intNr + "where alusdok='LG'";
    Statement st1 = con.createStatement();
    st1.executeUpdate(sql2);
    st1.close();
    st.close();


    return intNr;
  }

  
  
  private void addInvoiceLines(int invID) throws SQLException{
    String sql1 = "INSERT INTO rid VALUES('G',?, '', ?, ?, ?,'','K','',0,'','','','',?," +
        "0,?,?,0,'','','','','','',0,0,'','','',0,0,.F.,.F.,0,'',.F.,0,.F.)";
    String sql2 = "Select liik,myygihind from toode where toode=? ";
    String sql3 = "select liik from klient where kood=? ";
    String sql4 = "select m.hind,m.hinnak,t.myygihind from hinnamtr m left join klient k  on k.kood=m.klient " +
            " left join toode t on t.toode=m.toode " +
            " where (m.algus<=date() or date()<m.lopp) and (m.toode=? or m.artliik=?) and (m.klient=? or m.klliik=? or trim(m.klient)='')";

        PreparedStatement st = con.prepareStatement(sql1);
        PreparedStatement st2 = con.prepareStatement(sql2);
        PreparedStatement st3 = con.prepareStatement(sql3);
        PreparedStatement st4 = con.prepareStatement(sql4);

        double price = 0;
        double h1 = 0;
        double ale = 0;
        double listprice = 0;

    String tooteliik = null;
    String kliendiliik = null;
    Enumeration enumr = order.getOrderLines().elements();

    while(enumr.hasMoreElements()){
      OrderLine line = (OrderLine)enumr.nextElement();


      st2.setString(1, line.getToode());
      ResultSet rs = st2.executeQuery();
       while (rs.next()) {
         tooteliik = rs.getString("liik");
       }
       st3.setString(1,order.getCompany());
       ResultSet rs3 = st3.executeQuery();

       while (rs3.next()) {
         kliendiliik = rs3.getString("liik");
       }

       st4.setString(1,line.getToode());
       st4.setString(2,tooteliik);
       st4.setString(3,order.getCompany());
       st4.setString(4,kliendiliik);
       ResultSet rs4 = st4.executeQuery();

      while (rs4.next()) {
         h1 = rs4.getDouble("hind");
         ale = rs4.getDouble("hinnak");
         listprice = rs4.getDouble("myygihind");
      }

      if(listprice !=0 ){
        if (h1 != 0){
        price = h1;
        } else if (ale != 0){
        price = listprice*((100-ale)/100);
        }
      }

      st.setInt(1, invID);
      st.setDouble(2, price);
      st.setDouble(3, line.getKogus());
      st.setString(4, line.getToode());
      st.setDate(5,new java.sql.Date(order.getShipCal().getTime().getTime()));
      double disc = line.getDiscount();
      if (disc == -1) disc = 0;
      st.setDouble(6, disc);
       double bonus = 0 ;
      if (line.getBonus() != -1){
          bonus = line.getBonus() ;
              }
      st.setDouble(7, bonus);

      st.executeUpdate();
      setNumAddedLines(getNumAddedLines() + 1);
    }
    st.close();
  }

  private void addInvoiceHeader(int invID) throws SQLException{
    String sql = "INSERT INTO dok VALUES "+
       " ('G', ?, ?, 'ARV',?,1, 'EUR',?," +
                   "'62EUR', date(), ?, '1', ?, ?,0, .F., 'mj'," +
                   "'0000', '112', '1', 1, date(), 0," +
                   "?,'', .T., '', '', '','', '', '','',''," +
                   "'A', '', 0, .F., 0, '', '', .T., date()," +
                   " date(),.F., 0,'00',0,'', '', '', '', '')";
    log.info("addInvoiceHeader2: " + sql);
    PreparedStatement stmt = con.prepareStatement(sql);
    String body  = order.getEmailBody();
    if(body == null){
      body = " ";
    }

    stmt.setInt(1, invID);
    stmt.setDate(2,new java.sql.Date(order.getShipCal().getTime().getTime()));
    stmt.setString(3, order.getCompany());
    stmt.setString(4, String.valueOf(invID));
    stmt.setString(5, order.getDealer());
    stmt.setString(6, order.getAgent());
    stmt.setString(7, order.getCompany());
    stmt.setString(8, body);
    stmt.executeUpdate();
    stmt.close();
  }

  public String getDriverVersion(){
    return "VS";
  }

  private void setOrder(Order order){
    this.order = order;
  }


  private String url = "jdbc:odbc:";
  private Connection con;
  private Order order = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
