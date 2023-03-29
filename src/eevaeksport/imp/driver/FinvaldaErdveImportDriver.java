package eevaeksport.imp.driver;




import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class FinvaldaErdveImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());
  boolean testing = false;
  
  public FinvaldaErdveImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
  
    if("true".equals( parent.getProps().getProperty("testing"))){
    	testing = true;
    }
  }
 
  
  private TreeMap names = null;

  private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection(url, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	  }
  
 
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

  
  
  public String identifyCustomer(String id,String regnr)  {
    String acc = null;
    return acc;
  }

  
  public String identifyProduct(String ean, String prodID) throws SQLException{
	
    return null;
  
}
  
  private String getProdNimi(String toode){
	  
	  String sql =  " SELECT  pre_pav as pavad FROM prekes t where t.pre_kod =  '" + toode + "'" ;
	  
	  	   System.out.println(sql);
	  	   String res = "<No Name>";
	  	   try {
	  		   Connection con = getNewConnection();
	  	        Statement stmt = con.createStatement();
	  	        ResultSet rs = stmt.executeQuery(sql);
	  	        
	  	        if(rs.next()){
	  	        	res = rs.getString("pavad").trim();
	  	        }
	  	        
	  	        rs.close();
	  	        stmt.close();
	  	        con.close();
	  	        
	  	   }catch(Exception e){
	  		 e.printStackTrace();  
	  	   }
     return res;
  }

  String warelist = "'CHI DIDMEN','CHI STUDI.','107','MP KLAIPĖD','TRISKELL K'";
  
  private String getLadu(String toode , String agent){
	  
	  String  warelist = null;
	  if(parent.getProps().getProperty("KLA.list").contains(agent)) {
		  warelist = "'CHI DIDMEN','CHI STUDI.','107','MP KLAIPĖD','TRISKELL K'";
	  } else {
		  warelist = "'LOMA','LASIO','IMAGE','ERAYBA','ONMACABIM','MP PROFESI','MILANOBRUS','TRISKELL','SKEYNDOR','CENTR.','BIOREPEEL'";
	  }
	  
	  String sql =  " select san_kod as ladu , sum(sp1_ska)  " + 
	  		"    	 as seis from preks1_pkksar   where  pre_kod ='" + toode + "' and san_kod in ( " + warelist +  
	  		")		 group by san_kod having sum(sp1_ska) > 0 order by san_kod " ;
	  
	  	   System.out.println(sql);
	  	   String res = "CENTR.";
	  	   try {
	  		   Connection con = getNewConnection();
	  	        Statement stmt = con.createStatement();
	  	        ResultSet rs = stmt.executeQuery(sql);
	  	        
	  	        if(rs.next()){
	  	        	res = rs.getString("ladu").trim();
	  	        }
	  	        
	  	        rs.close();
	  	        stmt.close();
	  	        con.close();
	  	        
	  	   }catch(Exception e){
	  		 e.printStackTrace();  
	  	   }
     return res;
  }
  
  private double getProdHind(String toode){
	  
	  String sql =  " SELECT  pre_par1 as hind FROM prekes t where t.pre_kod =  '" + toode + "'" ;
	  
	  	   System.out.println(sql);
	  	 double res = 10;
	  	   try {
	  		   Connection con = getNewConnection();
	  	        Statement stmt = con.createStatement();
	  	        ResultSet rs = stmt.executeQuery(sql);
	  	        
	  	        if(rs.next()){
	  	        	res = rs.getDouble("hind");
	  	        }
	  	        
	  	        rs.close();
	  	        stmt.close();
	  	        con.close();
	  	        
	  	   }catch(Exception e){
	  		 e.printStackTrace();  
	  	   }
     return res;
  }
  
  
  private String getAgentName(String kood){
	  
	  String sql =  " SELECT  klg_pav as pavad FROM klgrp  where klg_kod =  '" + kood + "'" ;
	  
	  System.out.println(sql);
	  	   String res = "<No Name>";
	  	   try {
	  		   Connection con = getNewConnection();
	  	        Statement stmt = con.createStatement();
	  	        ResultSet rs = stmt.executeQuery(sql);
	  	        
	  	        if(rs.next()){
	  	        	res = rs.getString("pavad").trim();
	  	        }
	  	        
	  	        rs.close();
	  	        stmt.close();
	  	        con.close();
	  	        
	  	   }catch(Exception e){
	  		 e.printStackTrace();  
	  	   }
     return res;
  }
  
  
  public void makeOrder(Order order) throws SQLException{

     counter = 0;
     makeOutput(order); 	    
    
  }
  

  public static  String escapeXML(String s){
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
  

  public static void saveProps(Properties props, String fileName){
	    try {
	      FileOutputStream out =
	          new FileOutputStream(fileName);
	      props.store(out, "");
	      out.close();
	    } catch (IOException e) {
	     
	    }
	  }

  private long getAndUpdateRecID() {
      long nextRecID = 0;
       Properties props = loadProps("numerator.properties"); 
         if(props == null){
      	   props = new Properties();
         }
       String asi = (String) props.get("nextval");
       if(asi == null){
      	 nextRecID = 1;
       }else{
      	 nextRecID = Long.parseLong(asi) + 1;
      	 
       }
       props.put("nextval", nextRecID  + "");
       saveProps(props,"numerator.properties");
      return nextRecID;
    }
  
  private void makeOutput(Order order){
	  Vector lin = order.getOrderLines();
	  boolean laks = false;
	 	 for (int i = 0; i < lin.size(); i++) {
	 		OrderLine ol = (OrderLine) lin.elementAt(i);
	 		//System.out.println("XXXXX orderStatus " + ord.getStatus());
	 		if(ol.getKogus() < 0 ){//&& ol.getDiscount() > 0 && (order.getStatus() == 5 || order.getStatus() == 4
	 			laks = true;
	 		}
	 		
	 	 }  
	 	 String mana = order.getAgent();
	 	 
	  String comm =  escape(order.getInfo());
      if(comm != null && comm.length()>200){
      	comm =  mana + " " + comm.substring(0,190);
      }else {
    	  comm = mana;
      }
	 	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    
	      try {
	    	
	    	  
    	        String add = getAndUpdateRecID() + "";
    	        if(add.length() == 1){
    	          add = "000000" + add;
    	        }
    	      if(add.length() == 2){
    	          add = "00000" + add;
    	        }
    	    if(add.length() == 3){
    	          add = "0000" + add;
    	        }
  	
    	  if(add.length() == 4){
	          add = "000" + add;
	        }
  		        
    	if(add.length() == 5){
	          add = "00" + add;
	        }
    	if(add.length() == 6){
	          add = "0" + add;
	        }
String number =  add ;
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.newDocument();
	        Element root = doc.createElement("fvsdata");
	        root.setAttribute("xmlns", "http://www.fvs.lt/ns/accounting");
	        doc.appendChild(root);


	        Node pardavimas = doc.createElement("pardavimas");
	        root.appendChild(pardavimas);


	        Node serija = doc.createElement("serija");
	        Text unVal11 = doc.createTextNode("");
	        serija.appendChild(unVal11);
	        pardavimas.appendChild(serija);
	        
	        Node dokumentas = doc.createElement("dokumentas");
	        
	        Text unVal22 = doc.createTextNode(number);
	        dokumentas.appendChild(unVal22);
	        pardavimas.appendChild(dokumentas);
	        String type = "order";
	        if(laks){
	        	type = "return";
	        }
	        
	        Node doktype = doc.createElement("doktype");
	        Text doktype22 = doc.createTextNode(type);
	        doktype.appendChild(doktype22);
	        pardavimas.appendChild(doktype);
	        
	        
	        String klient = Converter.deCode(order.getCompany().substring(0, order.getCompany().lastIndexOf("-")));
	        String aadr = order.getCompany().substring(order.getCompany().lastIndexOf("-") + 1);
	        
	        Node klientas = doc.createElement("klientas");
	        Text nimiVal = doc.createTextNode(klient);
	        klientas.appendChild(nimiVal);
	        pardavimas.appendChild(klientas);
	   
	        if(!aadr.equals("00")) {
	        Node adresas = doc.createElement("adresas");
	        Text adresasVal = doc.createTextNode(aadr);
	        adresas.appendChild(adresasVal);
	        pardavimas.appendChild(adresas);

	        }  
	        
	        
	        if(comm != null){
	        
	        if(comm.length()<50){

		        Node pavadinimas1 = doc.createElement("pavadinimas1");
		        Text pavadinimas1Val = doc.createTextNode(comm);
		        pavadinimas1.appendChild(pavadinimas1Val);
		        pardavimas.appendChild(pavadinimas1);	
		        
	        } else if(comm.length()<100){
	        	
	        	   Node pavadinimas1 = doc.createElement("pavadinimas1");
			        Text pavadinimas1Val = doc.createTextNode( comm.substring(0,50));
			        pavadinimas1.appendChild(pavadinimas1Val);
			        pardavimas.appendChild(pavadinimas1);
			        
			        Node pavadinimas2 = doc.createElement("pavadinimas2");
			        Text pavadinimas2Val = doc.createTextNode(comm.substring(50));
			        pavadinimas2.appendChild(pavadinimas2Val);
			        pardavimas.appendChild(pavadinimas2);
			        
	        } else if(comm.length()<150){
	        	
	        	   Node pavadinimas1 = doc.createElement("pavadinimas1");
			        Text pavadinimas1Val = doc.createTextNode(comm.substring(0,50));
			        pavadinimas1.appendChild(pavadinimas1Val);
			        pardavimas.appendChild(pavadinimas1);
			        
			        Node pavadinimas2 = doc.createElement("pavadinimas2");
			        Text pavadinimas2Val = doc.createTextNode(comm.substring(50,100));
			        pavadinimas2.appendChild(pavadinimas2Val);
			        pardavimas.appendChild(pavadinimas2);
			        Node pavadinimas3 = doc.createElement("pavadinimas3");
			        Text pavadinimas3Val = doc.createTextNode(comm.substring(100));
			        pavadinimas3.appendChild(pavadinimas3Val);
			        pardavimas.appendChild(pavadinimas3);
			        
	        }else if(comm.length()<200){
	        	
	        	   Node pavadinimas1 = doc.createElement("pavadinimas1");
			        Text pavadinimas1Val = doc.createTextNode(comm.substring(0,50));
			        pavadinimas1.appendChild(pavadinimas1Val);
			        pardavimas.appendChild(pavadinimas1);
			        
			        Node pavadinimas2 = doc.createElement("pavadinimas2");
			        Text pavadinimas2Val = doc.createTextNode(comm.substring(50,100));
			        pavadinimas2.appendChild(pavadinimas2Val);
			        pardavimas.appendChild(pavadinimas2);
			        
			        Node pavadinimas3 = doc.createElement("pavadinimas3");
			        Text pavadinimas3Val = doc.createTextNode(comm.substring(100,150));
			        pavadinimas3.appendChild(pavadinimas3Val);
			        pardavimas.appendChild(pavadinimas3);
			        
			        Node pavadinimas4 = doc.createElement("pavadinimas4");
			        Text pavadinimas4Val = doc.createTextNode(comm.substring(150));
			        pavadinimas4.appendChild(pavadinimas4Val);
			        pardavimas.appendChild(pavadinimas4);
			    	
			        
	        }
	        
	        
	        
	        
	        
	        
	        
	        }
	        
	        //Agendi nimi 
	        Node pavadinimas5 = doc.createElement("pavadinimas5");
	        Text pavadinimas5Val = doc.createTextNode(getAgentName(order.getAgent()));
	        pavadinimas5.appendChild(pavadinimas5Val);
	        pardavimas.appendChild(pavadinimas5);
	        
	  
	        
	       
	        Node data = doc.createElement("data");
	        Calendar cal = Calendar.getInstance();
	        if(order.getShipCal() != null){
	        	cal = order.getShipCal();
	        }
	        
	    	        String month = (cal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	        String day = cal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;

	        String hour = cal.get(Calendar.HOUR_OF_DAY) + "";
	        if (hour.length() == 1) hour = "0" + hour;
	        String minute = cal.get(Calendar.MINUTE) + "";
	        if (minute.length() == 1) minute = "0" + minute;
	        
	        String second = cal.get(Calendar.SECOND) + "";
	        if (second.length() == 1) second = "0" + second;
	        
	        //[ HH:MM:SS]
	        
	        String dateString = cal.get(Calendar.YEAR) +"-"+ month +"-"+ day;
	        
	        Text unVal44 = doc.createTextNode(dateString);
	        data.appendChild(unVal44);
	        pardavimas.appendChild(data);
	        
	        if(!aadr.equals("00")) {
	        
	        Node gavejas = doc.createElement("gavejas");
	        Text gavejas55 = doc.createTextNode(aadr);
	        gavejas.appendChild(gavejas55);
	        pardavimas.appendChild(gavejas);
	        }
	        
	        Node valiuta = doc.createElement("valiuta");
	        Text unVal55 = doc.createTextNode("EUR");
	        valiuta.appendChild(unVal55);
	        pardavimas.appendChild(valiuta);
	        
	        Node imp_param = doc.createElement("imp_param");
	        Text unVal66 = doc.createTextNode("dbimp");
	        imp_param.appendChild(unVal66);
	        pardavimas.appendChild(imp_param);

	      	/* 
			String comm = "";
	 		if(order.getInfo() != null && order.getInfo().trim().length() >0){

	 			Node com = doc.createElement("comment");
		        Text comVal = doc.createTextNode(order.getInfo());
		        com.appendChild(comVal);
		        pardavimas.appendChild(com);
		        comm = order.getInfo();
	 		}
	 		System.out.println("Comment " + comm);
	        */
	        
	        
	        
	        Node read = doc.createElement("operacijaDet");
	        pardavimas.appendChild(read);

	        //RIDADE LISAMINE

	        int coun = 0;
	        Iterator iter = order.getOrderLines().iterator();
	        while (iter.hasNext()){
	          OrderLine line = (OrderLine)iter.next();
	          String toode  = line.getToode();
	 	     if(toode!= null){
	 	    	 toode = toode.trim();
	 	     }else{
	 	    	 toode = "";
	 	     }
	          if(line.getKogus()!= 0 && !toode.equals("")){
	            coun++;
	          	addLine(line, read, doc, coun , order, order.getAgent());
	          }
	          }

	        pardavimas.appendChild(read);

  String uni = (Calendar.getInstance().getTimeInMillis()+ "").substring(5);
	 	
	      String fileName = parent.getProps().getProperty("conv.dir") + order.getDokNr().trim() + (uni + "").trim() +  ".xml";
	      System.out.println(fileName);
	      OutputFormat of = new OutputFormat();
	        of.setIndenting(true);
	        of.setLineSeparator(System.getProperty("line.separator"));
	        of.setEncoding("ISO-8859-1");
	        XMLSerializer ser = new XMLSerializer(of);
	        FileOutputStream fos = new FileOutputStream(fileName);
	        PrintWriter out = new PrintWriter(fos);
	        ser.setOutputCharStream(out);
	        ser.serialize(doc);
	        out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
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
   
   
   
   private void addLine(OrderLine line, Node read, Document doc,int counter ,Order order, String agent) throws SQLException{
	   

	
	     Node rida = doc.createElement("eilute");
	     read.appendChild(rida);

	     


	     Node linenum = doc.createElement("tipas");
	     Text linnum = doc.createTextNode("1");
	     linenum.appendChild(linnum);
	     rida.appendChild(linenum);

     
	     String toode  = line.getToode();
	
	     Node toote_kood = doc.createElement("kodas");
	     Text toote_koodVal = doc.createTextNode(toode);
	     toote_kood.appendChild(toote_koodVal);
	     rida.appendChild(toote_kood);
	     
	     String ladu = this.getLadu(toode, agent);
	     Node sandel = doc.createElement("sandelis");
	     Text sandelVal = doc.createTextNode(ladu);
	     sandel.appendChild(sandelVal);
	     rida.appendChild(sandel);
        
	     String nimi = this.getProdNimi(toode);
	     if(nimi != null){
	    	 Text nimetusVal = doc.createTextNode(nimi.replace('`',' ').replace('´',' '));
		     Node nimetus = doc.createElement("pavadinimas");
		     nimetus.appendChild(nimetusVal);
		     rida.appendChild(nimetus);
	     }

	     
	     double hind = this.getProdHind(toode)/1.21;
	     double kogus = line.getKogus();
	     double price = line.getPrice();
	     double disc = line.getDiscount();
	     String unit = line.getUnit();
	    // <kiekis pirmas_mat="true">2.0</kiekis>
	     
	     Element koGus = doc.createElement("kiekis");
	     koGus.setAttribute("pirmas_mat", "true");
	     Text kogusVal = doc.createTextNode(getNF().format(kogus));
	     koGus.appendChild(kogusVal);
	     rida.appendChild(koGus);

	     int status = 1;
		    if(disc > 0 ){
		     status = 0;
		     
		    } 
	     
	     Node siulyti_kainas = doc.createElement("siulyti_kainas");
	      Text siulyti_kainasVla = doc.createTextNode(status+"");
	      siulyti_kainas.appendChild(siulyti_kainasVla);
		  rida.appendChild(siulyti_kainas);
	    
	     if(status == 0){
//kui on ale siis kirjutame sisse hinnad 	    	
    double disco = (100-disc)/100;
	    Node suma_v = doc.createElement("suma_v");
	    Text suma_vVal = doc.createTextNode(this.nf.format(hind* kogus ));
	    suma_v.appendChild(suma_vVal);
	     rida.appendChild(suma_v);
	     
	     Node suma_l = doc.createElement("suma_l");
		    Text suma_lVal = doc.createTextNode(this.nf.format(hind* kogus));
		    suma_l.appendChild(suma_lVal);
		     rida.appendChild(suma_l);
		     /*   >
		     <suma_v>200.00</suma_v>
		     <suma_l>200.00</suma_l>
		     <suma_pvmv>37.80</suma_pvmv>
		     <suma_pvml>37.80</suma_pvml>
		     <suma_nv>20.00</suma_nv>
		     <suma_nl>20.00</suma_nl>
		     <pvmproc>21</pvmproc>
		     </eilute> 
		     */   
		     Node suma_pvmv = doc.createElement("suma_pvmv");
			    Text suma_pvmvVal = doc.createTextNode(this.nf.format(hind* disco * kogus * 0.21));
			    suma_pvmv.appendChild(suma_pvmvVal);
			     rida.appendChild(suma_pvmv);
			     
			     Node suma_pvml = doc.createElement("suma_pvml");
				    Text suma_pvmlVal = doc.createTextNode(this.nf.format(hind* disco * kogus * 0.21));
				    suma_pvml.appendChild(suma_pvmlVal);
				     rida.appendChild(suma_pvml);

				     Node suma_nv = doc.createElement("suma_nv");
					    Text suma_nvVal = doc.createTextNode(this.nf.format((hind -(hind* disco)) * kogus));
					    suma_nv.appendChild(suma_nvVal);
					     rida.appendChild(suma_nv);

					     Node suma_nl = doc.createElement("suma_nl");
						    Text suma_nlVal = doc.createTextNode(this.nf.format((hind -(hind* disco)) * kogus));
						    suma_nl.appendChild(suma_nlVal);
						     rida.appendChild(suma_nl);
				     
				     
	     }else{
	 	    Node suma_v = doc.createElement("suma_v");
		    Text suma_vVal = doc.createTextNode(this.nf.format(0));
		    suma_v.appendChild(suma_vVal);
		     rida.appendChild(suma_v);
		     
		     Node suma_l = doc.createElement("suma_l");
			    Text suma_lVal = doc.createTextNode(this.nf.format(0));
			    suma_l.appendChild(suma_lVal);
			     rida.appendChild(suma_l);

	     }
	     
	     

			     Node yhiku_va = doc.createElement("pvmproc");
			      Text yhiku_vaVla = doc.createTextNode("21");
				     yhiku_va.appendChild(yhiku_vaVla);
				     rida.appendChild(yhiku_va);


	     addNumLine(); 
	   }

   public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}
  


	   private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);


 
  public String getDriverVersion(){
    return "FinvaldaErdveImport";
  }
 
 
 
  public boolean isAmount(){
          return isAmount;
  }

  /**
  *
  * @param aUseEAN - the new value for useEAN
  */
  public void setAmount(boolean aAmount){
           isAmount= aAmount;
  }


  
  private String url = "jdbc:odbc:finvalda";
  private Connection con;
  private int counter = 0;
  private ImportPanel parent;
  private boolean isAmount = false;
  private StringBuffer troubles= new StringBuffer();

  public String getTrouble() {
  	// TODO Auto-generated method stub
  	return troubles.toString();
  }
  
}
