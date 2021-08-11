
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */


public class RivonaImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public RivonaImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "rivona00");
      con2 = DriverManager.getConnection(url + "rivona00_centras");

    }
    catch (Exception e) {
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


  
  private Vector splitOrder (Order order){
	  Vector result = new Vector();
	// String doknr = getNextDoknr();
	 String doknr = order.getDokNr();
	  Order ord01 = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ord02 = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ord03 = new Order(order.getCompany(),order.getAgentFirstName());	 
	  Order ord05 = new Order(order.getCompany(),order.getAgentFirstName());	
	  Order ord04 = new Order(order.getCompany(),order.getAgentFirstName());	 
	  Order ord06 = new Order(order.getCompany(),order.getAgentFirstName());	 
	  Order ord07 = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ord08 = new Order(order.getCompany(),order.getAgentFirstName());
		 
	  
	  ord07.setAgentCode(order.getAgentCode());
	  ord07.setDokNr("K80" +doknr);
	  ord07.setInfo(order.getInfo());
	  ord07.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord07.setStatus(order.getStatus());
	  ord07.setPaymTerm(order.getPaymTerm());
	  ord07.setTeostaja(order.getTeostaja());
	  ord07.setChanged(true); // et teaksime et on tegemita tubakaga T tähe lisamiseks
	  ord07.setWarehouse("KS000080");
	  ord07.setShipDateUsed(order.isShipDateUsed());
	  
	  
	  ord01.setAgentCode(order.getAgentCode());
	  ord01.setDokNr("K01" +doknr);
	  ord01.setInfo(order.getInfo());
	  ord01.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord01.setStatus(order.getStatus());
	  ord01.setPaymTerm(order.getPaymTerm());
	  ord01.setTeostaja(order.getTeostaja());
	  ord01.setChanged(true); // et teaksime et on tegemita tubakaga T tähe lisamiseks
	  ord01.setWarehouse("KS000001");
	  ord01.setShipDateUsed(order.isShipDateUsed());
		
	  
	  ord05.setAgentCode(order.getAgentCode());
	  ord05.setDokNr("K27" +doknr);
	  ord05.setInfo(order.getInfo());
	  ord05.setPaymTerm(order.getPaymTerm());
	  ord05.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord05.setStatus(order.getStatus());
	  ord05.setShipDateUsed(order.isShipDateUsed());
	  ord05.setTeostaja(order.getTeostaja());
	  ord05.setWarehouse("KS000080");
	  
	  
	  ord02.setAgentCode(order.getAgentCode());
	  ord02.setDokNr("K02" +doknr);
	  ord02.setInfo(order.getInfo());
	  ord02.setTeostaja(order.getTeostaja());
	  ord02.setPaymTerm(order.getPaymTerm());
	  ord02.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord02.setStatus(order.getStatus());
	  ord02.setWarehouse("KS000002");
	  ord02.setShipDateUsed(order.isShipDateUsed());
		
	  
	  ord03.setAgentCode(order.getAgentCode());
	  ord03.setDokNr("K03" +doknr);
	  ord03.setInfo(order.getInfo());
	  ord03.setTeostaja(order.getTeostaja());
	  ord03.setPaymTerm(order.getPaymTerm());
	  ord03.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord03.setStatus(order.getStatus());
	  ord03.setWarehouse("KS000003");
	  ord03.setShipDateUsed(order.isShipDateUsed());
		
	  
	  ord04.setAgentCode(order.getAgentCode());
	  ord04.setDokNr("015" +doknr);
	  ord04.setInfo(order.getInfo());
	  ord04.setTeostaja(order.getTeostaja());
	  ord04.setPaymTerm(order.getPaymTerm());
	  ord04.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord04.setStatus(order.getStatus());
	  ord04.setWarehouse("00000015");
	  ord04.setShipDateUsed(order.isShipDateUsed());
		
	  
	  ord06.setAgentCode(order.getAgentCode());
	  ord06.setDokNr("030" +doknr);
	  ord06.setInfo(order.getInfo());
	  ord06.setTeostaja(order.getTeostaja());
	  ord06.setPaymTerm(order.getPaymTerm());
	  ord06.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord06.setStatus(order.getStatus());
	  ord06.setWarehouse("00000030");
	  ord06.setShipDateUsed(order.isShipDateUsed());
		
	  ord08.setAgentCode(order.getAgentCode());
	  ord08.setDokNr("090" +doknr);
	  ord08.setInfo(order.getInfo());
	  ord08.setTeostaja(order.getTeostaja());
	  ord08.setPaymTerm(order.getPaymTerm());
	  ord08.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord08.setStatus(order.getStatus());
	  ord08.setWarehouse("KS000003");
	  ord08.setShipDateUsed(order.isShipDateUsed());
	  
	  
	  Vector lines = order.getOrderLines();
	  Enumeration enumr = lines.elements();
	  while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
	
		     String itemid = lin.getToode();
		     
		    String ware =  identifyWare(itemid,lin.getKogus());
		    
		    System.out.println("Splitter " + ware );
		    
		     if(ware.equals("KS000001") ){ 		     	    
		    	 	  ord01.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);			 
		     } else if(ware.equals("KS000003")){
		    	     ord03.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			 } else if (ware.equals("KS000002")){
		    		 ord02.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		    				 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		     } else if (ware.equals("KS000080")){
		    		 ord05.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		    				 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		      }else if (ware.equals("00000030")){
		    		 ord06.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		    				 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		      }else if (ware.equals("00000015")){
		    		 ord04.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		    				 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		      }else if (ware.equals("KS000090")){
		    		 ord08.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		    				 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		      }
			     			
			     }
               





	  if(ord01.getOrderLines() != null && ord01.getOrderLines().size()>0){
		  result.add(ord01);  
	  }

	  if(ord05.getOrderLines() != null && ord05.getOrderLines().size()>0){
		  result.add(ord05);  
	  }
	  
	  if(ord02.getOrderLines() != null && ord02.getOrderLines().size()>0){
		  result.add(ord02);  
	  }

	  if(ord03.getOrderLines() != null && ord03.getOrderLines().size()>0){
		  result.add(ord03);  
	  }

	  if(ord04.getOrderLines() != null && ord04.getOrderLines().size()>0){
		  result.add(ord04);  
	  }

	  if(ord06.getOrderLines() != null && ord06.getOrderLines().size()>0){
		  result.add(ord06);  
	  }
	  
	  if(ord08.getOrderLines() != null && ord08.getOrderLines().size()>0){
		  result.add(ord08);  
	  }
	  
	 System.out.println("Result =" +order.getDokNr()
			 + " "+ result.size());
	 //System.exit(0);
	  return result ;
  }

  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	
  	    sql1 = "SELECT  trim(N08_KODAS_KS) AS kood1, (case when (N33_kodas_ak) is null then '1' else trim(str(N33_kodas_ak)) end) AS kood " +
	  " FROM n08_klij  JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS " +
	  " where N08_KODAS_KS = '" + id + "'";	
  	  
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood1").trim() + "-" +rs1.getString("kood").trim() ;
    }
    rs1.close();
    st1.close();

   // System.out.println("Klient: " + acc);
    return acc;

  }
  
  HashSet milks = new HashSet();
  HashSet eighties = new HashSet();
  
  
  
  

  public String identifyWare(String id , double kogus)  {
	  
	  String res = "KS000001";
	  
	  System.out.println("Milks sisaldab toodet " + milks.contains(id) );
	  
	  if(milks.contains(id)){
		  
		  String sql1  = "SELECT  (I17_KIEKIS) AS STOCK, I17_KODAS_IS as ladu from i17_vpro inner join n17_prod on  I17_KODAS_PS = N17_KODAS_PS  " +
			        " where I17_KODAS_IS in ('00000030','00000015') AND ((I17_KIEKIS) >0 OR (I17_P_PAR_K)>0) and n17_kodas_ls_1<>''  and  N17_POZ_DATE =0 " +
					"	 and I17_KODAS_PS='" + id + "' ORDER BY I17_KODAS_PS ";	
				  	  
				    System.out.println(sql1);
				    res = "00000030";

					try {
						Statement st1 = con2.createStatement();
					    ResultSet rs1 = st1.executeQuery(sql1);
			    
					    
					    while (rs1.next()) {
					    		res = rs1.getString("ladu").trim()  ;
					    }
					    rs1.close();
					    st1.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					  
		  
	  }else{
		  String sql1  = "SELECT  (I17_KIEKIS-  I17_REZERVAS - i17_ATIDUOTA)  AS STOCK, I17_KODAS_IS as ladu from i17_vpro inner join n17_prod on  I17_KODAS_PS = N17_KODAS_PS  " +
			        " where I17_KODAS_IS in ('KS000090') AND ((I17_KIEKIS-  I17_REZERVAS - i17_ATIDUOTA)  >0 OR (I17_P_PAR_K)>0) and n17_kodas_ls_1<>''  and  N17_POZ_DATE =0 " +
					"	 and I17_KODAS_PS='" + id + "' ORDER BY I17_KODAS_PS, I17_KODAS_IS desc";	
				  	  
	 
	  String sql2  = "SELECT  (I17_KIEKIS-  I17_REZERVAS - i17_ATIDUOTA)  AS STOCK, I17_KODAS_IS as ladu from i17_vpro inner join n17_prod on  I17_KODAS_PS = N17_KODAS_PS  " +
        " where I17_KODAS_IS in ('KS000001','KS000002','KS000003') AND ((I17_KIEKIS-  I17_REZERVAS - i17_ATIDUOTA)  >0 OR (I17_P_PAR_K)>0) and n17_kodas_ls_1<>''  and  N17_POZ_DATE =0 " +
		"	 and I17_KODAS_PS='" + id + "' and (I17_KIEKIS-  I17_REZERVAS - i17_ATIDUOTA) >= " + kogus + " ORDER BY I17_KODAS_PS, I17_KODAS_IS ";	// desc
	  	  
	    System.out.println(sql1);
	    

		try {
			Statement st1 = con.createStatement();
			
			 ResultSet rs1 = st1.executeQuery(sql1);
			    
			    
			    while (rs1.next()) {
			    	double kog = rs1.getDouble("STOCK");
			    	if(kogus<=kog){
			    		res = rs1.getString("ladu").trim()  ;
			    	}
			    }
			    rs1.close();
		if(!res.equals("KS000090"))	{
          rs1 = st1.executeQuery(sql2);
    
		    
		    while (rs1.next()) {
		    //	double kog = rs1.getDouble("STOCK");
		    	//if(kogus<=kog){
		    		res = rs1.getString("ladu").trim()  ;
		    	//}
		    		
		    		System.out.println("Res: " + res);
		    }
		    rs1.close();
		}
		     
		    st1.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
	  }

	  if(eighties.contains(id)) {
		 return "KS000080"; 
	  }
	  
	    System.out.println("Ladu: " + res);
	    return res;

	  }
  
  private String  getNextDoknr(){
	  String res = null;
	  	String   sql1 = "select order_nr as number from dbo.MR1_DOK_NR where dev_id = 'ANDROID'";	
  	  
    System.out.println(sql1);
  
	try {
		Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	res = rs1.getString("number");
	    }
	    rs1.close();
	    sql1 = " update  dbo.MR1_DOK_NR set order_nr = order_nr +1,last_update = getdate() where dev_id = 'ANDROID' ";	
	    st1.executeUpdate(sql1);

	   
	    st1.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  return res ;
  }
  
  public String identifyCustomerADDR(String id ) throws SQLException {
  	 String	sql1 = "SELECT N33_ADRESAS AS aadress FROM n08_klij JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
  	 		"  trim(N08_KODAS_KS)||'-'|| N33_kodas_ak = '" + id +"'"; 
  	
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("aadress").trim();

    }
    rs1.close();
    st1.close();
    
    
	 	sql1 = "SELECT N33_ADRESAS AS aadress FROM n08_klij JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
		"  trim(N08_KODAS_KS) = '" + id +"'"; 

System.out.println(sql1);


 st1 = con.createStatement();
 rs1 = st1.executeQuery(sql1);

if (rs1.next()) {
acc = rs1.getString("aadress").trim();

}
rs1.close();
st1.close();

    return acc;

  }
  
  
  
  
  
  public boolean isExportCust(String cust)  {
	  boolean res = false;
	  
	 	 String	sql1 = "SELECT    ltrim(N08_KODAS_KS) AS kood1  FROM n08_klij " +
				"  where trim(N08_KODAS_KS) = '" + cust +"' and N08_KODAS_XS_P in ('00010','00002')";
	 	
	 	 //  and if value are  equal to 00010 or 00002 

	   System.out.println(sql1);
	   
try{
	 Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(sql1);

	   if (rs1.next()) {
	   res = true;

	   }
	   rs1.close();
	   st1.close();

}catch(Exception e){
 e.printStackTrace();	
}
	  
	   return res;

	 }
  
  public String getContract(Order ord) throws SQLException {
	 	 String	sql1 = "SELECT N51_KODAS_KT AS contract FROM N51_SUTH  WHERE " +
	 	 		" trim(N51_KODAS_KS) ='" + ord.getCompany() +"' and now(*) between n51_beg_date and n51_end_date "; 
	

	   System.out.println(sql1);
	   String acc = null;

	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(sql1);

	   if (rs1.next()) {
	     acc = rs1.getString("contract").trim();

	   }
	   rs1.close();
	   st1.close();

	   return acc;

	 }
  private boolean isKeda(Order order){
	OrderLine ol = (OrderLine)  order.getOrderLines().get(0);
	 String	sql1 = "select N17_kodas_ps, N17_pav from n17_prod where n17_kodas_Ls_1 in (SELECT N35_kodas_ls FROM dbo.N35_LGST " +
 " where n35_pav_k3 = 'TECHNOLOGAI')  and N17_KODAS_PS = '" + ol.getToode() +"'"; 
	 	
	   System.out.println(sql1);
boolean res = false ;
try{
	  Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(sql1);

	   if (rs1.next()) {
	    res = true;
	   }
	   rs1.close();
	   st1.close();
}catch(Exception g){
	g.printStackTrace();
}
	  
	  return res ;
  }
  public String identifyCustomerAGENT(String id) throws SQLException {
 	 String	sql1 = "SELECT N33_KODAS_LS_1 AS aadress FROM n08_klij  JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE " +
 	 		" trim(N08_KODAS_KS)||'-'|| N33_kodas_ak ='" + id +"'"; 
 	
   System.out.println(sql1);
   String acc = null;

   Statement st1 = con.createStatement();
   ResultSet rs1 = st1.executeQuery(sql1);

   while (rs1.next()) {
     acc = rs1.getString("aadress").trim();

   }
   rs1.close();
   st1.close();

   return acc;

 }
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
  	
    
    String sql1 = " SELECT N17_KODAS_PS AS toode ,N17_KOD_T AS ean FROM n17_prod a   " +
   	"  WHERE  N17_KODAS_PS = '" + ean.trim().substring(0,ean.trim().length()-1) + "'";

    String sql2 = " SELECT N17_KODAS_PS AS toode ,N17_KOD_T AS ean " +
    " FROM n17_prod a  ,  n27_pak " +
   	"  WHERE  n27_kodas_ps=n17_kodas_ps  and  N17_KODAS_PS = '" + prodID.trim() + "'";

    
    String sql3 = " SELECT N17_KODAS_PS AS toode ,N17_KOD_T AS ean FROM n17_prod a   " +
   	"  WHERE  N17_KODAS_PS = '" + ean + "'";

    String prod = null;

    try {
    	
    System.out.println(sql1);
    Statement st1 = con.createStatement();  
    ResultSet rs1 = st1.executeQuery(sql1);
  
    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }
   System.out.println(ean + " prod0 " + prod);
   

    if(prod == null){

    	   Statement st2 = con.createStatement();
    	   ResultSet rs2 = st2.executeQuery(sql2);
      while (rs2.next()) {
        prod = rs2.getString("toode").trim();
      }
      rs2.close();
      st2.close();
      
    }
    
    
    if(prod == null){

        Statement st3 = con.createStatement();
        ResultSet rs3 = st3.executeQuery(sql3);
        while (rs3.next()) {
          prod = rs3.getString("toode").trim();
        }
        rs3.close();
        st3.close();
    }

    System.out.println(prodID + " prod1 " + prod);
    rs1.close();
    st1.close();
    }catch (Exception e){
    	e.printStackTrace();
    }

    return prod;
  }
  private HashMap bcodes =  null;

  public void makeOrder(Order order) throws SQLException{
	    String bc = "select N37_KODAS_PS as prod ,N37_BAR_KODAS as ean  from n37_pmat where N37_BAR_KODAS != N37_KODAS_PS " +
		"and N37_BAR_KODAS!='000000000000' and '0'+ltrim(N37_KODAS_PS)!=N37_BAR_KODAS and '00'+ltrim(N37_KODAS_PS)!=N37_BAR_KODAS";
	    bcodes =  new HashMap();
try{
    Statement stm = con.createStatement();
    ResultSet rst = stm.executeQuery(bc);

     while (rst.next()) {
       String toode = rst.getString("prod").trim();
       String bar = rst.getString("ean").trim();        
       bcodes.put(toode,bar);	
   }
   rst.close();
   stm.close();
}catch (Exception r ){
	System.exit(0);
}

if (signatures == null){
	   signatures = new Vector();
}

String contract = getContract(order);
 

String sql1 = "SELECT distinct N17_KODAS_US as unit ,N17_KODAS_PS AS toode,trim(N17_PAV) AS nimetus,N27_KIEKIS_PAK  AS tykke, " + 
		 "    n17_kodas_ls_1 AS grupp ,N17_MEN_PAV AS minkogus ,  N17_KODAS_LS_4 AS status , N17_MAX_NUOL as maxa ,( select top 1 N27_KIEKIS_PAK from N27_PAK a where left(a.N27_KODAS_US_PAK,7)='SANDPAL' and " +
		 " a.N27_KODAS_PS = N17_KODAS_PS) as alusel,( select top 1 N27_KIEKIS_PAK from N27_PAK a where left(a.N27_KODAS_US_PAK,8)='SANDPICK' and " +
		 " a.N27_KODAS_PS = N17_KODAS_PS)  as optimum  " +
		 "  FROM n17_prod a   left join N27_PAK on  trim(N17_KODAS_PS)=trim(N27_KODAS_PS) and left(N27_KODAS_US_PAK,7)='SANDPAK' " +
		 "  left join N32_PABC on N17_KODAS_PS = n32_kodas_ps " +
		 " where  n17_kodas_ls_1<>'' and n17_kodas_ls_1 !='GAMYBA'  and  N17_POZ_DATE =0  and n32_tipas = 'E'   " +
		 "   and N17_kodas_ls_1 in (SELECT  N35_KODAS_LS  FROM N35_LGST where n35_pav_k3 = 'ALYTAUS PIENINE' or n35_pav_k3='ALYTAUS KONSERVAI') " +
		 "  order by N17_KODAS_PS ";	
	  	  
	    System.out.println(sql1);
	
		try {
			Statement st1 = con2.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql1);
       
		    while (rs1.next()) {
		    		milks.add(rs1.getString("toode").trim());
		    }
		    rs1.close();
		    st1.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		sql1 = "SELECT  n17_KODAS_PS AS toode from  n17_prod n  inner join N35_LGST on n17_kodas_ls_1 = N35_KODAS_LS \r\n" + 
				"			    where  n35_tevas='MOBILINK' and n35_pav_k2 = 'KS000080'";
		System.out.println(sql1);
		
		try {
			Statement st1 = con.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql1);
       
		    while (rs1.next()) {
		    		eighties.add(rs1.getString("toode").trim());
		    }
		    rs1.close();
		    st1.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	  
	  Vector splitted =  splitOrder(order);

	  Enumeration en = splitted.elements();
	  while(en.hasMoreElements()){
		  Order ord = (Order)en.nextElement();
	    	  String signature = ord.getSignature();
		  if(signatures.contains(signature)){
			  this.trouble = "REPREATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
			  System.out.println("Korduv tellimus ");  		  
		  }else{
			  makeOutput(ord ,contract );  
			  signatures.add(signature);
			  this.trouble = null;
		  }
	 
	   try {
			saveObject(signatures, "signatures.vec");
		} catch (IOException e) {
			e.printStackTrace();
		}
	 }
  }


private String trouble = null;
public String getTrouble() {
		// TODO Auto-generated method stub
		return trouble;
	}



public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
	




 private void makeOutput(Order order , String contract){
 	StringBuffer sb = new StringBuffer();
	String num =  "KS" + getNextDoknr();   
 	  String cust = (order.getCompany());
 	 cust = cust.replaceAll("11119xx", "Š");
      String kk = cust;
	  String kka = null;
	  if(cust.indexOf("-") > -1){
		  kk = cust.substring(0,cust.lastIndexOf("-"));
		  kka = cust.substring(cust.lastIndexOf("-")+1);
	  }
 	
 	
	  boolean todb = true;
	     if( order.getWarehouse().equals("00000030") || order.getWarehouse().equals("00000015")){
	    	 todb = false;
	     }
	  
 	boolean exportCust = isExportCust(kk);
 	
nf4.setMaximumFractionDigits(3);
nf4.setGroupingUsed(false);
 	
 	String ls = System.getProperty("line.separator");
 	try {
 	 
      sb.append("       <I06>" + ls);

      int status =  order.getStatus();
    String type = "54";
      if (order.getWarehouse().equals("KS000001") || order.getWarehouse().equals("KS000005") ||
    		  order.getWarehouse().equals("KS000003") ||
    		  order.getWarehouse().equals("KS000002")){
    	  type = "54";
      }
      if(!todb){
    	  type = "55";
      }
       
      
      if(order.getDokNr().startsWith("090")) {
    	  type = "55";  
      }
   
      if(status == 0){
    	  sb.append("          <I06_OP_TIP>" + type + "</I06_OP_TIP>" + ls); 
      }else{
    	  sb.append("          <I06_OP_TIP>54</I06_OP_TIP>" + ls);  
      }
      
      
      
      String stat = "0";
      String val = "";
      if(exportCust){
    	  stat = "1";
    	  val = "EUR";
      }
      String prf = "K0";
      if(order.getAgent().startsWith("KAI") || isKeda(order)){
    	  prf = "P0";
      }
      
      String ordnum = prf+ escape(order.getDokNr());
      
      String ref = ordnum;
      if(order.getTeostaja() != null && order.getTeostaja().trim().length() > 0 ){
    	  ref = order.getTeostaja();
      }
      System.out.println("ref " + ref);
      String shipd = getDateString(order.getShipCal());
      
     if( todb && !order.isShipDateUsed()){
    	  shipd = getDateString(Calendar.getInstance()); 
     }
      System.out.println("shipper " + todb + " " +  order.isShipDateUsed() + " " + getDateString(order.getShipCal()));
      
      sb.append("          <I06_DOK_NR>" + ordnum + "</I06_DOK_NR>" + ls);
      sb.append("          <I06_KODAS_PO>"+ ordnum+ "</I06_KODAS_PO>" + ls);
      if( todb ){
    	  sb.append("          <I06_DOK_DATA>" +  shipd + "</I06_DOK_DATA>" + ls);
          sb.append("          <I06_OP_DATA>" +  getDateString(Calendar.getInstance()) + "</I06_OP_DATA>" + ls);
        	  
      }else{
    	  shipd = getDateString(Calendar.getInstance()) ;
          
    	     if( order.isShipDateUsed()){
    	    	  shipd = getDateString(order.getShipCal());; 
    	     }
    	  sb.append("          <I06_DOK_DATA>" +  shipd + "</I06_DOK_DATA>" + ls);
          sb.append("          <I06_OP_DATA>" +   shipd + "</I06_OP_DATA>" + ls);
          
      }
      sb.append("          <I06_INTRASTAT>" +  stat + "</I06_INTRASTAT>" + ls);
      sb.append("          <I06_VAL_POZ>" +  stat + "</I06_VAL_POZ>" + ls);
      sb.append("          <I06_KODAS_VL>" +  val + "</I06_KODAS_VL>" + ls);
          
      
      sb.append("          <I06_KODAS_KS>" + escape(kk) + "</I06_KODAS_KS>" + ls);
     
      if(kka != null){
    	  sb.append("          <I06_KODAS_AK>" + escape(kka) + "</I06_KODAS_AK>" + ls); 
      }
      
      sb.append("          <i06_DOK_REG>" + ref + "</i06_DOK_REG>" + ls);
      if(order.getStatus() == 55 || kka == null){
    	  //I06_VAL_POZ  must be 1
    	  sb.append("          <I06_VAL_POZ>1</I06_VAL_POZ>" + ls);  
    	  sb.append("          <I06_KODAS_LS_1>SVS_ORINIS</I06_KODAS_LS_1>" + ls);
    	  sb.append("          <I06_KODAS_VL>EUR</I06_KODAS_VL>" + ls);  
    	  sb.append("          <I06_INTRASTAT>1</I06_INTRASTAT>" + ls);  
    	  
      }else {
    	  sb.append("          <I06_KODAS_LS_3>" + escape(order.getAgent()) + "</I06_KODAS_LS_3>" + ls);
    	   sb.append("          <I06_KODAS_MS>" + escape(order.getAgent()) + "</I06_KODAS_MS>" + ls);
      }
      
      
      if(order.getDokNr().startsWith("090")) {
      	   sb.append("               <I06_ATSTOVAS>akcizas</I06_ATSTOVAS>" + ls);
              
         }
      
      if(contract != null){
    	  sb.append("           <I07_KODAS_KT>" + contract.trim() + "</I07_KODAS_KT>" + ls);  
      }

      
      
      String addr =  null;
  	try {
  		addr = identifyCustomerADDR(cust);
  	} catch (SQLException e1) {
  		e1.printStackTrace();
  	}
       sb.append("          <I06_ADR2>" + escape(addr) + "</I06_ADR2>" + ls);

      
     // sb.append("          <I06_PVM_TIP>0</I06_PVM_TIP>" + ls);

   //   String agent = identifyCustomerAGENT(cust);
      
      /*  
   <I06_PAV>D.BU</I06_PAV>
      <I06_PAV2>D.BUL </I06_PAV2>
      <I06_ADR2>         </I06_ADR2>
      <I06_KODAS_VS>025010006   </I06_KODAS_VS>
      <I06_SUMA_PVM>971.53</I06_SUMA_PVM>
*/

 
     
      
     
    Vector lines = order.getOrderLines();
      for (int i = 0; i < lines.size(); i++) {
         sb.append("          <I07>" + ls);
          
      	OrderLine line = (OrderLine)lines.elementAt(i);
          String toode = line.getToode();
          if(this.bcodes.get(toode) != null ){
        	  toode = (String)bcodes.get(toode) ;
          }
          
          double ale = line.getDiscount();
          if (ale == -1 ){
             ale = 0 ;
              }
          String kog  = escape(nf4.format(line.getKogus()));
          double kogus = line.getKogus();
          boolean inte = true;
          if((int)kogus - kogus != 0){
        	  kog = escape(nf4.format(line.getKogus()));
        	  inte = false;
          }
          
          sb.append("               <I07_KODAS_PO>" + ordnum + "</I07_KODAS_PO>" + ls); 
          sb.append("               <I07_EIL_NR>" + i + "</I07_EIL_NR>" + ls);
     //     sb.append("               <I07_KODAS>" +  escape(toode) + "</I07_KODAS>" + ls);
          sb.append("               <DI07_BAR_KODAS>" +  escape(toode) + "</DI07_BAR_KODAS>" + ls);
          if(inte){
        	//  sb.append("               <I07_KIEKIS>" + kog + "</I07_KIEKIS>" + ls);  
          }else{
        	  //sb.append("               <T_KIEKIS>" + kog + "</T_KIEKIS>" + ls);
          }
         
          sb.append("               <KIEKIS>" + kog + "</KIEKIS>" + ls);
        //  sb.append("               <I07_ALT_KIEKIS>" + kog + "</I07_ALT_KIEKIS>" + ls);
          sb.append("               <I07_KODAS_IS>" + order.getWarehouse() + "</I07_KODAS_IS>" + ls);    
          sb.append("               <I07_TIPAS>1</I07_TIPAS>" + ls);
       if (ale != 0 ){
          sb.append("               <I07_NUOLAIDA>" + escape(nf.format(ale)) + "</I07_NUOLAIDA>" + ls);
          }     
       
       
       
       if(line.getPrice() > 0 && order.getStatus() == 55){
    	   sb.append("               <I07_KAINA_BE>" + escape(nf.format(line.getPrice())) + "</I07_KAINA_BE>" + ls);
           sb.append("               <I07_SUMA>" + escape(nf.format(line.getPrice()*line.getKogus())) + "</I07_SUMA>" + ls);
		   sb.append("               <I07_PVM>0</I07_PVM>" + ls);
       }
         // sb.append("               <I07_MOKESTIS_P>" + prod.getVat() + "</I07_MOKESTIS_P>" + ls);
         // sb.append("               <I07_SUMA>" + escape(getConc().getNF().format((line.getAmount()* price)*(100-ale)/100)
          //		)+ "</I07_SUMA>" + ls);
          
  /*<I07>

  <I07_KAINA_SU>1.0900</I07_KAINA_SU>
  <I07_NUOLAIDA>0.00</I07_NUOLAIDA>
*/
  

  addNumLine();
          sb.append("          </I07>" + ls);
      }

      sb.append("          </I06>" + ls);
 
    
 	
 	String pdanum = order.getTeostaja();
 	if(pdanum == null || pdanum.length() == 0){
 		pdanum = "";	
 	}
 	if(todb){
 	 	 String	sql1 = "   insert into dbo.B00_IMP( B00_KODAS_IM,B00_PAV,B00_MODUL,B00_DATE,B00_OPERACIJA,B00_LOGAS,B00_PERKELTA,B00_ADDUSR,B00_USERIS,B00_R_DATE)" +
 	 	 		" values( '"+num+"','Importas is Android " + pdanum + " ','RO',current date,'" + sb.toString()+ "'," +
 	 	 				"'',1,'MASTER','MASTER',DATEFORMAT(getdate(*),'yyyy-mm-dd hh:nn:ss.000'))"; 

 	System.out.println(sql1);

 	Statement st1 = con.createStatement();
 	  st1.executeUpdate(sql1);
 	  st1.close();
 	     		
 	}

 
      
   if(todb){
	   String fileName = escape(parent.getProps().getProperty("conv.dir") + num + ordnum + "-" + order.getAgent())  + ( Calendar.getInstance().getTimeInMillis()+"").substring(5,13)   +  ".EIP";
	      System.out.println(fileName);
	        FileOutputStream fos = new FileOutputStream(fileName);
	        fos.write(sb.toString().getBytes());
	        fos.close();
   }else{
	   String fileName = escape(parent.getProps().getProperty("conv.dir2") + num + ordnum + "-" + order.getAgent())  + ( Calendar.getInstance().getTimeInMillis()+"").substring(5,13)   +  ".EIP";
	   System.out.println(fileName);
	   FileOutputStream fos = new FileOutputStream(fileName);
	   fos.write(sb.toString().getBytes());
	   fos.close(); 
   }  
      
      
   

	} catch (Exception e) {
		e.printStackTrace();
	}
 	
 	
 	 addOrderToDB(num, order);
 }
  
 
 private void addOrderToDB(String num , Order ord){
	 try{
		  String cust = (ord.getCompany());
		 	 cust = cust.replaceAll("11119xx", "Š");
		      String kk = cust;
			  String kka = null;
			  if(cust.indexOf("-") > -1){
				  kk = cust.substring(0,cust.lastIndexOf("-"));
				  kka = cust.substring(cust.lastIndexOf("-")+1);
			  }
		 	
		 	boolean exportCust = isExportCust(kk);
		 
		      int status =  ord.getStatus();
		    String type = "54";
		      if (ord.getWarehouse().equals("KS000001") || ord.getWarehouse().equals("KS000005") ||
		    		  ord.getWarehouse().equals("KS000003") ||
		    		  ord.getWarehouse().equals("KS000002")){
		    	  type = "54";
		      }
		    
		      if(status != 0){
		    	 type = "54";
		      }
		      
		      String stat = "0";
		      String val = "";
		      if(exportCust){
		    	  stat = "1";
		    	  val = "EUR";
		      }
		    
		      String prf = "K0";
		      if(ord.getAgent().startsWith("KAI") || this.isKeda(ord)){
		    	  prf = "P0";
		      }
		    String ordnum = prf + ord.getDokNr();
		 
		      String addr =  null;
			  	try {
			  		addr = identifyCustomerADDR(cust);
			  	} catch (SQLException e1) {
			  		e1.printStackTrace();
			  	}
		      
		 	
		 	
	 	 String	sql1 = "   insert into dbo.B06_PARH( "
	 	 		+ "B06_OP_TIP," +
	              "B06_DOK_NR," +
	"B06_KODAS_PO," +
	"B06_DOK_DATA," +
	"B06_OP_DATA," +
	"B06_INTRASTAT," +
	"B06_VAL_POZ," +
	"B06_KODAS_VL," +
	"B06_KODAS_KS," +
	"B06_KODAS_AK," +
	"B06_DOK_REG,"+
	"B06_KODAS_LS_3,"+
	"B06_KODAS_MS," +
	"B06_ADR2)" +
	 	 		" values( '"+type+"',"
	 	 				+ "'" + ordnum + "',"
	 	 				+ "'" + ordnum + "',"
	 	 				+ "'" + getDateString(ord.getShipCal()) + "',"
	 	 				+ "'" + getDateString(Calendar.getInstance()) + "',"
	 	 				+ "'" + stat + "',"
	 	 				+ "'" + stat + "',"
	 	 				+ "'" + val + "',"
	 	 				+ "'" + escape(kk) + "',"
	 	 				+ "'" + escape(kka) + "',"
	 	 				+ "'" + ordnum + "',"
	 	 				+ "'" + escape(ord.getAgent()) + "',"
	 	 				+ "'" + escape(ord.getAgent()) + "',"
	 	 				+ "'" + escape(addr) + "')"; 
	 	
	System.out.println(sql1);

	Statement st1 = con.createStatement();
	  st1.executeUpdate(sql1);
	  st1.close();
	    
	    Vector lines = ord.getOrderLines();
	      for (int i = 0; i < lines.size(); i++) {
	    	  OrderLine line = (OrderLine)lines.elementAt(i);
	          String toode = line.getToode();
	          if(this.bcodes.get(toode) != null ){
	        	  toode = (String)bcodes.get(toode) ;
	          }
	          
	          double ale = line.getDiscount();
	          if (ale == -1 ){
	             ale = 0 ;
	              }
	          String kog  = escape(nf4.format(line.getKogus()));
	          double kogus = line.getKogus();
	          if((int)kogus - kogus != 0){
	        	  kog = escape(nf4.format(line.getKogus()));
	          }  
	    	  
	    	  
	    		sql1 = "   insert into dbo.B07_PARD( "
	    		 	 		+ "B07_KODAS_PO,"
	    		 	 		 +"B07_EIL_NR,"// numeric(6,0) NOT NULL DEFAULT 0,
	    		 	 		 +"DB07_BAR_KODAS," //char(12) NOT NULL DEFAULT ' ',
	    		 	 		 +"KIEKIS," //numeric(14,3) NOT NULL DEFAULT 0,
	    		 	 		 +"B07_KODAS_IS," //char(12) NULL DEFAULT '',
	    		 	 		 +"B07_TIPAS)" //numeric(1,0) NOT NULL DEFAULT 1,

	    		 	 		+ " values( '"+ordnum+"',"
	    		 	 				+ "" + i + ","
	    		 	 				+ "'" + escape(toode) + "',"
	    		 	 				+ "" + kog + ","
	    		 	 				+ "'" + ord.getWarehouse() + "',"
	    		 	 				+ "1)";
	    		st1 = con.createStatement();
	    		  st1.executeUpdate(sql1);
	    		  st1.close();	
	    	  
	    	  
	    	  
	      }
	        


		} catch (Exception e) {
			e.printStackTrace();
		}
	 	
	 	
 }
 
 public String getImportFile(){
	    return parent.getProps().getProperty("imp.dir");
	  }
 
  public String getDriverVersion(){
    return "RivonaImport";
  }


 

  private String escape(String s){
    if (s == null) return s;
    StringBuffer sb = new StringBuffer();
    for (int i = 0 ; i < s.length() ; i++){
      char c = s.charAt(i);
      if (c == '&'){
        sb.append("&amp;");
      } else if (c == '<'){
        sb.append("&lt;");
      } else if (c == '>'){
        sb.append("&gt;");
      } else if (c == '\n'){
        sb.append(" ");
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  
  private String getLineNr2(int i){
  	String res = i + "";
  	if (res.length() == 1){
  		res = "00000" + res;
  	} else if (res.length() == 2){
  		res = "0000" + res;
  	} else if (res.length() == 3){
  		res = "000" + res;
  	} else if (res.length() == 4){
  		res = "00" + res;
  	} else if (res.length() == 5){
  		res = "0" + res;
  	}
  	return res;
  }
  
  private String getDateString(Calendar cal){
    StringBuffer sb = new StringBuffer();
    sb.append(cal.get(Calendar.YEAR) + ".");
    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
    if (month.length() == 1){
      sb.append("0");
    }
    sb.append(month + ".");
//2005.09.05 00:00
    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    if (day.length() == 1){
      sb.append("0");
    }
    sb.append(day );

    return sb.toString();
  }
  

  private String url = "jdbc:odbc:";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private NumberFormat nf4 = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private Connection con2;
  private ImportPanel parent;


}
