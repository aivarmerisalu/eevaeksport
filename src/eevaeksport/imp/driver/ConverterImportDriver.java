
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;

import java.io.FileOutputStream;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Calendar;
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

public class ConverterImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public ConverterImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    
   String hh =  parent.getProps().getProperty("company");
   
   if(hh != null){
	 company = hh;   
   }
    
   String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();

      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
    
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

private String company = "9998";

  public String identifyCustomer(String id,String regnr) throws SQLException {
  	    return null;

  }
    
 
  
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
    return null;
  }


  
  public void makeOrder(Order order) throws SQLException{

	  counter = 0;

		
	 Order ord =  order;			  
			  java.util.Collections.sort(ord.getOrderLines(), new java.util.Comparator(){
					 
			        public boolean equals(Object obj) {
			          return false;
			        }

			       public int compare(Object o1, Object o2) {
			          OrderLine ol1 = (OrderLine)o1;
			          OrderLine ol2 = (OrderLine)o2;
			          return ol1.getTooteNimi().compareTo(ol2.getTooteNimi());
			        }
			           });
   		           ord.setTeostaja(order.getTeostaja());
     			   makeOutput(ord);
	    	   

      setNumAddedLines(counter);
  }
  
  private String getDateString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    sb.append(cal.get(Calendar.YEAR));
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append(month + "");

	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day);

	    return sb.toString();
	  }

  
  private String getSelver(String asi ){
	  String res = asi;
	  
	  if(asi.equals("741")){
		  res = "F03640"; //T1
	  }
	  if(asi.equals("598")){
		  res = "F036143"; //balti jaam
	  }
	  if(asi.equals("410")){
		  res = "F0361"; //punane
	  }
	  if(asi.equals("589")){
		  res = "F03631"; //aardla
	  }
	  if(asi.equals("775")){
		  res = "F03646"; //aardla
	  }
	  if(asi.equals("596")){
		  res = "F036142";//	t�hesaju SELVER 
	  }
	  if(asi.equals("599")){
		  res = "F03643";//	korde
	  }
	  if(asi.equals("597")){
		  res = "F03636";//	kolde 
	  }
	  if(asi.equals("594")){
		  res = "F03635"; //Arsenali
	  }
	  if(asi.equals("470")){
		  res = "F0361"; //punane
	  }
	  if(asi.equals("272")){
		  res = "F03626"; //punane
	  }
	  if(asi.equals("500")){
		  res = "F03610"; //MAI SELVER PAPINIIDU 42 F03610
	  }
	  if(asi.equals("897")){
		  res = "F03667"; //pärnu delice
	  } 

 if(asi.equals("595")){
		  res = "F03699"; //K�rberi Selver
	  }
	  if(asi.equals("490")){
		  res = "F03611";//	MERIMETSA SELVER PALDISKI MNT 56 F03611
	  }
	  if(asi.equals("510")){
		  res = "F03612";//	RAKVERE KROONI SELVER F.G. ADOFFI 11 F03612
	  }
	  if(asi.equals("505")){
		  res = "F03613";//	M11115xxNNIM11115xxE SELVER RIIA MNT 35 F03613
	  }
	  if(asi.equals("515")){
		  res = "F03614";//	TORUPILLI SELVER VESIV11115xxRAVA 37 F03614
	  }
	
	  if(asi.equals("574")){
		  res = "F036187";//	KERESE SELVER Kerese 3 F036187
	  }
	  if(asi.equals("573")){
		  res = "F036185";//	P11117xxLTSAMAA SELVER Tartu mnt Bussi
	  }
	  if(asi.equals("572")){
		  res = "F036183";//	J11117xxGEVA SELVER Kesk 3A/4 F036183
	  }
	  if(asi.equals("571")){
		  res = "F036182";//	JAAMAM11117xxISA SELVER Jaama 74 F036182
	  }
	  if(asi.equals("535")){
		  res = "F03618" ;//	RINGTEE SELVER Aardla 114 F03618
	  }
	  if(asi.equals("530")){
		  res = "F03617";//	ANNE SELVER KALDA TEE 43 F03617
	  }
	  if(asi.equals("525")){
		  res = "F03616";//	VILJA SELVER VILJA 6 F03616
	  }
	  if(asi.equals("740")){
		  res = "F036144";//	LAAGRI TALLINNA TN 67 F03615
	  }
	  if(asi.equals("520")){
		  res = "F03615";//	SAARE SELVER TALLINNA TN 67 F03615
	  }
	
	  if(asi.equals("5152")){
		  res = "F036141";//	TORUPILLI SELVER II KORRUS VESIV11115xxRAVA 37 F036141
	  }
	  if(asi.equals("575")){
		  res = "F036186";//	KOHTLA-J11115xxRVE SELVER J11111xxrvk11113xxla tee 68 F036186
	  }
	  if(asi.equals("576")){
		  res = "F036188";//	HIIUMAA SELVER Rehem11111xxe,
	  }
	  if(asi.equals("577")){
		  res = "F036189";//	MARIENTHALI SELVER
	  }
	  if(asi.equals("580")){
		  res = "F036192";//	PAIDE SELVER Aiavilja tn 4 F036192

	  }if(asi.equals("579")){
		  res = "F036191";//	11118xxLEJ11117xxE SELVER Tallinna mnt. 93a/Roheline 80 F036191

	  }if(asi.equals("578")){
		  res = "F036190";//	P11117xxLVA SELVER Jaama 12 F036190
	  }
	  
	  if(asi.equals("440")){
			  res = "F0362";//	KADAKA SELVER I KORRUS KADAKA 56A F0362
		  }
	 
	  if(asi.equals("774")){
		  res = "F03638";//	KADAKA SELVER I KORRUS KADAKA 56A F0362
	  }
	  if(asi.equals("550")){
		  res = "F03621";//	VALGA SELVER Raja tn RAJA TN 5 F03621

	  }


	  if(asi.equals("773")){
			  res = "F03637";//	METSAPAJA SELVER  F0362
		  }

	  if(asi.equals("545")){
		  res = "F03620";//	SUUREJ11117xxE SELVER SUUR-J11117xxE 57 F03620
	  }

	  if(asi.equals("584")){
		  res = "F036195";//	RANNAROOTSI SELVER Rannarootsi tee 1, Uuem11114xxisa F036195
	  }

	  if(asi.equals("583")){
		  res = "F036194";//	KAKUM11115xxE SELVER Rannam11114xxisa tee 6b F036194
	  }

	  if(asi.equals("581")){
		  res = "F036193";//	KEILA SELVER Piiri 12 F036193
	  }

	  if(asi.equals("555")){
		  res = "F03622";//	PELGULINNA SELVER S11117xxLE 51 F03622
	  }
	  
	  if(asi.equals("585")){
		  res = "F03624";//	SAKU SELVER 11118xxksnurme tee 2, Saku F03624
	  }
	  
	  
	  if(asi.equals("271")){
		  res = "F03625";//	SELVER GURMEE KAUBAMAJA Gonsori 2 F03625
	  }
	  
	  if(asi.equals("450")){
		  res = "F0364";//	PIRITA SELVER I KORRUS RUMMU TEE 2 F0364

	  }
	  if(asi.equals("460")){
		  res = "F0365";//	J11115xxRVE SELVER I KORRUS P11115xxRNU MNT.238 F0365

	  }
	  if(asi.equals("4602")){
		  res = "F0367";//	J11115xxRVE SELVER II KORRUS P11115xxRNU MNT.238 F0367
	  }
	  if(asi.equals("4402")){
		  res = "F0368";//	KADAKA SELVER II KORRUS KADAKA 56A F0368
	  }
	  if(asi.equals("480")){
		  res = "F0369";//	TONDI SELVER A.H.TAMMSAARE TEE 62 F0369

	  }
	  if(asi.equals("560")){
		  res = "F03691";//	MUSTAKIVI SELVER MUSTAKIVI TEE 3A F03691

	  }
	  if(asi.equals("593")){
		  res = "F03698";//	MUSTAKIVI SELVER MUSTAKIVI TEE 3A F03691

	  }
	  if(asi.equals("4502")){
		  res = "F03641";//	PIRITA SELVER II KORRUS RUMMU TEE 2 F03641

	  }
	  if(asi.equals("742")){
		  res = "F03645";//	Kagukeskuse Selveri KORRUS RUMMU TEE 2 F03641

	  }
	  if(asi.equals("540")){
		  res = "F03692";//	S11117xxBRA SELVER S11117xxBRA 56 F03692

	  }
	  if(asi.equals("565")){
		  res = "F03693";//	VEERIKU SELVER VITAMIINI 1 F03693

	  }
	  if(asi.equals("279")){
		  res = "F03639";//	Ve-Selver

	  }
	  if(asi.equals("570")){
		  res = "F03694";//	J11117xxHVI SELVER NARVA MNT 8 F03694

	  }
	  if(asi.equals("582")){
		  res = "F03695";//	SOLDINO SELVER Tiimani tn 20 F03695
	  }
	  
	  
	  if(asi.equals("772")){
		  res = "F03627";//	P��sk�la SELVER 
	  }
	  if(asi.equals("744")){
		  res = "F03671";//	P��sk�la SELVER 
	  }
  if(asi.equals("743")){
		  
		  res = "F03669";//	priisle SELVER 
	  }
	  
	  if(asi.equals("592")){
		  
		  res = "F03697";//	Astri SELVER 
	  }
	  
	  if(asi.equals("587")){
		  res = "F03628";//	Vahi SELVER 
	  }
	  if(asi.equals("588")){
		  res = "F03630";//	l��nemere SELVER 
	  }
	  
	  if(asi.equals("586")){
		  res = "F03629";//	Rapla SELVER 
	  }
	  

	  if(asi.equals("590")){
		  res = "F03632"; //Centrum
	  } 
	  if(asi.equals("591")){
		  res = "F03633"; //peetri
	  } 
	  if(asi.equals("771")){
		  res = "F03634"; //Balti jaam
	  }

		if (asi.equals("782")) {
			return "F03647";
		}
		if (asi.equals("778")) {
			return "F03648";
		}
		if (asi.equals("784")) {
			return "F03649";
		}
		if (asi.equals("777")) {
			return "F03650";
		}
		if (asi.equals("776")) {
			return "F03651";
		}
		if (asi.equals("779")) {
			return "F03652";
		}
		if (asi.equals("780")) {
			return "F03653";
		}
		if (asi.equals("781")) {
			return "F03654";
		}
		if (asi.equals("783")) {
			return "F03655";
		}
		
		if (asi.equals("785")) {
			return "F03656";
		}
		if (asi.equals("786")) {
			return "F03657";
		}
		if (asi.equals("787")) {
			return "F03658";
		}
		if (asi.equals("788")) {
			return "F03659";
		}
		if (asi.equals("789")) {
			return "F03660";
		}
		if (asi.equals("790")) {
			return "F03661";
		}
		if (asi.equals("791")) {
			return "F03662";
		}
		if (asi.equals("895")) {
			return "F03663";
		}
		if (asi.equals("896")) {
			return "F03664";
		}
		
		if (asi.equals("745")) {
			return "F03674";
		}
	  
	  
	  
		  return res ;
	 	
  }

  
  private String getGLN(String asi ){
	  String res = asi;
	  
	  if(asi.equals("410")){
		  res = "4740581999275"; //punane
	  }
	  
	  if(asi.equals("472")){
		  res = "4740581999695"; //kagukeskus
	  }
	
	  if(asi.equals("598")){
		  res = "4740581999602"; //baltijaam
	  }
	  

	  if(asi.equals("741")){
		  res = "4740581999664"; //T1 selver
	  }
	  
	  
	  if(asi.equals("589")){
		  res = "4740581999510"; //aardla
	  }
	  
	  if(asi.equals("594")){
		  res = "4740581999572"; //arsenali 
	  }
	 
	  if(asi.equals("500")){
		  res = "4740581999169"; //MAI SELVER PAPINIIDU 42 F03610
	  }
	  if(asi.equals("490")){
		  res = "4740581999183";//	MERIMETSA SELVER PALDISKI MNT 56 F03611
	  }
	  if(asi.equals("510")){
		  res = "4740581999152";//	RAKVERE KROONI SELVER F.G. ADOFFI 11 F03612
	  }
	  if(asi.equals("505")){
		  res = "4740581999206";//	M11115xxNNIM11115xxE SELVER RIIA MNT 35 F03613
	  }
	  if(asi.equals("515")){
		  res = "4740581999428";//	TORUPILLI SELVER VESIV11115xxRAVA 37 F03614
	  }
	  
	  if(asi.equals("897")){
		  res = "4740581999732";//	Pärnu Delice
	  }
	
	  if(asi.equals("574")){
		  res = "4740581999138";//	KERESE SELVER Kerese 3 F036187
	  }
	  if(asi.equals("573")){
		  res = "4740581999299";//	P11117xxLTSAMAA SELVER Tartu mnt Bussi
	  }
	  if(asi.equals("572")){
		  res = "4740581999077";//	J11117xxGEVA SELVER Kesk 3A/4 F036183
	  }
	  if(asi.equals("571")){
		  res = "4740581999046";//	JAAMAM11117xxISA SELVER Jaama 74 F036182
	  }
	  if(asi.equals("535")){
		  res = "4740581999343" ;//	RINGTEE SELVER Aardla 114 F03618
	  }
	  if(asi.equals("530")){
		  res = "4740581999015";//	ANNE SELVER KALDA TEE 43 F03617
	  }
	  if(asi.equals("525")){
		  res = "4740581999473";//	VILJA SELVER VILJA 6 F03616
	  }
	  if(asi.equals("520")){
		  res = "4740581999350";//	SAARE SELVER TALLINNA TN 67 F03615
	  }
	  if(asi.equals("740")){
		  res = "4740581999633";//	LAAGRI SELVER TALLINNA TN 67 F03615
	  }
	 
	  if(asi.equals("5152")){
		  res = "4740581999428";//	TORUPILLI SELVER II KORRUS VESIV11115xxRAVA 37 F036141
	  }
	  if(asi.equals("575")){
		  res = "4740581999145";//	KOHTLA-J11115xxRVE SELVER J11111xxrvk11113xxla tee 68 F036186
	  }
	  if(asi.equals("576")){
		  res = "4740581999039";//	HIIUMAA SELVER Rehem11111xxe,
	  }
	  if(asi.equals("577")){
		  res = "4740581999176";//	MARIENTHALI SELVER
	  }
	  if(asi.equals("580")){
		  res = "4740581999213";//	PAIDE SELVER Aiavilja tn 4 F036192

	  }if(asi.equals("579")){
		  res = "4740581999480";//	11118xxLEJ11117xxE SELVER Tallinna mnt. 93a/Roheline 80 F036191

	  }if(asi.equals("578")){
		  res = "4740581999312";//	P11117xxLVA SELVER Jaama 12 F036190
	  }
	  
	  if(asi.equals("440")){
			  res = "4740581999091";//	KADAKA SELVER I KORRUS KADAKA 56A F0362
		  }
	  if(asi.equals("774")){
		  res = "4740581999640";//	KOTKA SELVER F0362
	  }


	  if(asi.equals("773")){
			  res = "4740581999619";//	METSAPAJA SELVER 56A F0362
		  }
	  
	  if(asi.equals("550")){
		  res = "4740581999459";//	VALGA SELVER Raja tn RAJA TN 5 F03621

	  }
	  if(asi.equals("596")){
		  res = "4740581999596";//	T�HESAJU SELVER 

	  }
	  if(asi.equals("599")){
		  res = "4740581999671";//	kolde 

	  }
	

	  if(asi.equals("545")){
		  res = "4740581999398";//	SUUREJ11117xxE SELVER SUUR-J11117xxE 57 F03620
	  }

	  if(asi.equals("584")){
		  res = "4740581999329";//	RANNAROOTSI SELVER Rannarootsi tee 1, Uuem11114xxisa F036195
	  }
	  if(asi.equals("745")){
		  res = "4740581999947";//	KURNA SELVERRannarootsi tee 1, Uuem11114xxisa F036195
	  }

	  if(asi.equals("583")){
		  res = "4740581999497";//	KAKUM11115xxE SELVER Rannam11114xxisa tee 6b F036194
	  }

	  if(asi.equals("581")){
		  res = "4740581999916";//	KEILA SELVER Piiri 12 F036193
	  }

	  if(asi.equals("555")){
		  res = "4740581999220";//	PELGULINNA SELVER S11117xxLE 51 F03622
	  }
	  
	  if(asi.equals("585")){
		  res = "4740581999367";//	SAKU SELVER 11118xxksnurme tee 2, Saku F03624
	  }
	  
	  
	  
	  if(asi.equals("450")){
		  res = "4740581999237";//	PIRITA SELVER I KORRUS RUMMU TEE 2 F0364

	  }
	  if(asi.equals("460")){
		  res = "4740581999053";//	J11115xxRVE SELVER I KORRUS P11115xxRNU MNT.238 F0365

	  }
	  if(asi.equals("4602")){
		  res = "4740581999060";//	J11115xxRVE SELVER II KORRUS P11115xxRNU MNT.238 F0367
	  }
	  if(asi.equals("440")){
		  res = "4740581999107";//	KADAKA SELVER II KORRUS KADAKA 56A F0368
	  }
	  if(asi.equals("480")){
		  res = "4740581999411";//	TONDI SELVER A.H.TAMMSAARE TEE 62 F0369

	  }
	  if(asi.equals("560")){
		  res = "4740581999190";//	MUSTAKIVI SELVER MUSTAKIVI TEE 3A F03691

	  }
	  if(asi.equals("4502")){
		  res = "4740581999244";//	PIRITA SELVER II KORRUS RUMMU TEE 2 F03641

	  }
	  if(asi.equals("540")){
		  res = "4740581999404";//	S11117xxBRA SELVER S11117xxBRA 56 F03692

	  }
	  if(asi.equals("565")){
		  res = "4740581999466";//	VEERIKU SELVER VITAMIINI 1 F03693

	  }
	  if(asi.equals("570")){
		  res = "4740581999084";//	J11117xxHVI SELVER NARVA MNT 8 F03694

	  }
	  if(asi.equals("279")){
		  res = "4740581999688";//	E-Selver

	  }
	  if(asi.equals("582")){
		  res = "K000177";//	SOLDINO SELVER Tiimani tn 20 F03695
	  }
	  
	  
	  if(asi.equals("772")){
		  res = "4740581999282";//	P��sk�la SELVER 
	  }
	  if(asi.equals("744")){
		  res = "4740581999930";//	P��sk�la SELVER 
	  }
	  
	  
	  if(asi.equals("592")){	  
		  res = "4740581999626";//	Astri SELVER 
	  }
	  
	  if(asi.equals("587")){
		  res = "4740581999442";//	Vahi SELVER 
	  }
	  if(asi.equals("588")){
		  res = "4740581999503";//	l��nemere SELVER 
	  }
	  

	  if(asi.equals("597")){
		  res = "4740581999589";//	maardu SELVER 
	  }
	  
	  if(asi.equals("586")){
		  res = "4740581999336";//	Rapla SELVER 
	  }
	  

	  if(asi.equals("590")){
		  res = "4740581999541"; //Centrum
	  } 
	  if(asi.equals("591")){
		  res = "4740581999534"; //peetri
	  } 
	  if(asi.equals("771")){
		  res = "4740581999558"; //Balti jaam
	  }
	  if(asi.equals("595")){
		  res = "4740581999657"; //Balti jaam
	  }
	  
	  
	  if(asi.equals("595")){
		  res = "4740581999657"; //Balti jaam
	  }
	  
	  
		if (asi.equals("782")) {
			return "4740581999800";
		}
		if (asi.equals("778")) {
			return "4740581999763";
		}
		if (asi.equals("784")) {
			return "4740581999824";
		}
		if (asi.equals("777")) {
			return "4740581999756";
		}
		if (asi.equals("776")) {
			return "4740581999749";
		}
		if (asi.equals("779")) {
			return "4740581999770";
		}
		if (asi.equals("780")) {
			return "4740581999787";
		}
		if (asi.equals("781")) {
			return "4740581999794";
		}
		if (asi.equals("783")) {
			return "4740581999817";
		}
		
		if (asi.equals("785")) {
			return "4740581999831";
		}
		if (asi.equals("786")) {
			return "4740581999848";
		}
		if (asi.equals("787")) {
			return "4740581999855";
		}
		if (asi.equals("788")) {
			return "4740581999862";
		}
		if (asi.equals("789")) {
			return "4740581999879";
		}
		if (asi.equals("790")) {
			return "4740581999886";
		}
		if (asi.equals("791")) {
			return "4740581999893";
		}
		if (asi.equals("895")) {
			return "4740581999718";
		}
		if (asi.equals("896")) {
			return "4740581999725";
		}
		
	  
	  
		  return res ;
	 	
  }

  
  
 private void makeOutput(Order order){
 	StringBuffer sb = new StringBuffer();
 	 nf.setGroupingUsed(false);

 	try {
 	  int cou = 0;
		String ref = order.getReference();
		if ( ref == null ){
			ref =  " ";
		}
		String com = order.getEmailBody();
		if ( com == null ){
			com =  " ";
		}		
		
		 String doknr = order.getDokNr();
                 

         String ls = System.getProperty("line.separator");
     
         String custID = getSelver(order.getCompany());
         if(!company.equals("9998")){
        	 custID = getGLN(order.getCompany());
         }
         String ladu = null;
             Calendar  shipDate = order.getShipCal();
             
             String code = order.getWarehouse();        
             if (code != null){
               ladu = code;
             }
           
           if (ladu == null) {
             ladu = "";
           }
  
      

       
           Calendar cal = Calendar.getInstance();
          
           if (shipDate != null) {
                 cal = shipDate;
               }
         
           sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + ls);
           sb.append("<tellimus>" + ls);
           sb.append("   <tellija-kood>" + escape(custID) + "</tellija-kood>" + ls);
           sb.append("   <tarnija-kood>" + company + "</tarnija-kood>" + ls);
          
           	sb.append("   <tellija>" + ls + "       <nimi>" + escape(custID) + "</nimi>" + ls + "  </tellija>" + ls);
             sb.append("   <nr>" + escape(order.getDokNr()) + "</nr>" + ls);
             sb.append("   <regnr>" + escape("12345678") + "</regnr>" + ls);
             sb.append("   <filiaal>" + escape(order.getCompany()) + "</filiaal>" + ls);
             sb.append("   <kuupaev>" + escape(getDateString(Calendar.getInstance())) + "</kuupaev>" + ls);
             sb.append("   <tarne-kuupaev>" + escape(getDateString(cal)) + "</tarne-kuupaev>" + ls);
            
           sb.append("   <koostaja>" + ls);
           sb.append("      <isiku-id> </isiku-id>" + ls);
           sb.append("   </koostaja>" + ls);
           sb.append("   <read>" + ls);
   
           Vector sortedLines = order.getOrderLines();
           for (int i = 0; i < sortedLines.size(); i++) {
               OrderLine line = (OrderLine)sortedLines.elementAt(i);
               counter++;
               String yhik = line.getUnit();
               String ean = line.getEAN();
             
               sb.append("      <rida>" + ls);
               sb.append("         <toote-kood>" + escape(line.getToode()) + "</toote-kood>" + ls);
               if( ean!=null && ean.length()>4 ) {
                 sb.append("         <ean-kood>" + escape(ean) + "</ean-kood>" + ls);
               }
               sb.append("         <nimetus>" + escape(line.getTooteNimi()) +
                         "</nimetus>" + ls);
               sb.append("         <kogus>" + escape(getNF().format(line.getKogus())) + "</kogus>" + ls);
                if(yhik != null && yhik.trim().length() > 0){
                	sb.append("         <yhik>" + escape(yhik.trim()) + "</yhik>" + ls); 	
                }
                sb.append("         <vat>-1</vat>" + ls); 
               sb.append("      </rida>" + ls);
           }

           sb.append("   </read>" + ls);
           sb.append("</tellimus>" + ls);
 
      counter = counter + cou;
    	//
      String fn = doknr;
      if(doknr.trim().length()== 0){
    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
      }
      String fileName = escape(parent.getProps().getProperty("conv.dir") +order.getCompany())  + "." + fn  +  ".xml";
      System.out.println(fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(sb.toString().getBytes());
        fos.close();

	} catch (Exception e) {
		e.printStackTrace();
	}
 }
 
 
	public NumberFormat getNF() {
		return this.nf;
	}
	


 
 
  public String getDriverVersion(){
    return "ConverterImport";
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



  public static final String addSpaces(String s, int totalLen){
    int len = s.length();
    StringBuffer sb = new StringBuffer(s);
    for (int i=len; i < totalLen ; i++){
      sb.append(' ');
    }
    if( len > totalLen){
    	return s.substring(0,totalLen);
    }
    return sb.toString();
  }
  
  public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
  
  private NumberFormat calNF = NumberFormat.getInstance();

  public static final String cleanLitLatEst(String ret){
	    if (ret != null){
	      
	        ret = ret.replaceAll("�","e");
	        ret = ret.replace((char) 0x3f,' ');
	        
	        
	       // ret = ret.replaceAll("?","s");
	       // ret = ret.replaceAll("?","c");
	     // ret = ret.replace('',' ');
	        ret = ret.replaceAll("�","I");
	        ret = ret.replaceAll("�","u");
	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","z");
	        ret = ret.replaceAll("�","s");
	        ret = ret.replaceAll("�","S");
	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","I");
	        ret = ret.replaceAll("�","u");
	        ret = ret.replaceAll("�","i");
	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","Z");
	        ret = ret.replaceAll("�","E");
	        ret = ret.replaceAll("�","U");
	        ret = ret.replaceAll("�","U");
	        ret = ret.replaceAll("�","Pg.");
	        
	        ret = ret.replaceAll("�","Z");
	        ret = ret.replaceAll("�","A");
	        ret = ret.replaceAll("�","a");
	        ret = ret.replaceAll("�","C");

	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","E");
	        ret = ret.replaceAll("�","e");
	        ret = ret.replaceAll("�","G");
	        ret = ret.replaceAll("�","g");
	        ret = ret.replaceAll("�","I");
	        ret = ret.replaceAll("�","i");
	        ret = ret.replaceAll("�","K");
	        ret = ret.replaceAll("�","k");
	        ret = ret.replaceAll("�","L");
	        ret = ret.replaceAll("�","l");
	        ret = ret.replaceAll("�","N");
	        ret = ret.replaceAll("�","n");
	        
	        ret = ret.replaceAll("�","o");
	        ret = ret.replaceAll("�","a");
	        ret = ret.replaceAll("�","o");
	        ret = ret.replaceAll("�","u");
	       
	        ret = ret.replaceAll("�","O");
	        ret = ret.replaceAll("�","A");
	        ret = ret.replaceAll("�","O");
	        ret = ret.replaceAll("�","U");
	    }
	        
	        if(ret == null || ret.trim().length()==0){
	        return " ";
	        }else {
	        	System.out.println(ret);
	        return ret;
	        }        
	      }
  
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private int counter = 0;
  private ImportPanel parent;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
