package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalLineWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.util.Converter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class KMPDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

  public KMPDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
  }

  public String getPilotDayData(Agent agent){

	  StringBuffer sb = new StringBuffer();
	  String sep= System.getProperty("line.separator");      
	 System.out.println(agent.getName());
	  
	  try {
		  TreeMap groups = this.getProdGroups();
		  File ff = new File(getDir() + DATA_FILE);
	
	  FileReader fr = new FileReader(getDir() + DATA_FILE);
	       BufferedReader reader = new BufferedReader(fr);
	     String line = null;
	     String agTotal = "Total : " +  getBulkSales(agent.getID());
	     String header = eevaeksport.EksportPanel.getDateMMpDDpAAAAStr(ff.lastModified(), 0) + 
	     "\n\n" + agTotal +  "\n\n"+  getParent().getString("name") + "  " + getParent().getString("number") + " kg.+(Lt.)" +
		    sep + "-------------------------------------------" + sep;
		  boolean heade = true;

	     
	     
	     
	     while(!(line = reader.readLine()).startsWith("[Get Grupp Sales]")){
	     }
	     System.out.println(line);
	      while(((line = reader.readLine()).trim().length()) > 0 ){

	           line = line.replaceAll("\",\"","|");
	      	StringTokenizer st = new StringTokenizer(line, SEPARATOR);
  System.out.println(line);
	        if (st.hasMoreTokens()) {
	          String ag = clear(clear(st.nextToken()));
	          //"agent","grupp","subgroup","summa","kaal"
	         // "000","MP0","PPR","207843.48","22519.995"
	          
	          
	          if(ag.equals(agent.getID())){
	  	      	if (heade){
		    		sb.append(header);
		    		heade = false;
		    	} 
		          if (st.hasMoreTokens()) {
		        	  st.nextToken();//laseme peagrupi vahele
		            String gr = clear(st.nextToken());
		            String name = (String)groups.get(gr);
		            sb.append(name + " ") ;
		          }	     
		          int sum = 0;
		          int vol = 0;
		          if (st.hasMoreTokens()) {
		        	  sum = (int) Double.parseDouble(clear(st.nextToken()));
		          }
		          if (st.hasMoreTokens()) {
		        	  vol = (int) Double.parseDouble(clear(st.nextToken()));
		          }
		          sb.append("\n\t" + vol + " kg. ( " + sum + "Lt.)\n" );
	          }	  

	        }
	      

	
}
} catch (Exception e) {
getParent().showError(e);
}


System.out.println(sb.toString());
return sb.toString();

  }

   public Vector getPriceMatrixData() {
   	return null;
   }
   
   private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }

   public String getSaveDebtInfo() {
   	return null;
   }
   
   public void insertDefective(DefectiveWrapper wr){
    String sql = "INSERT INTO dbo.MJ_QUALITY VALUES (?,?,?,?,?,?,?,?,?,?,getdate(),?,' ',' ',0,0)";
    try {
      Connection con = getNewConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, wr.getDefective().getAgentID());
        ps.setString(2, wr.getDefective().getAgentName());

        for (Iterator iter = wr.getDefective().getLines().iterator(); iter.hasNext(); ) {
          DefectiveLineWrapper lw = new DefectiveLineWrapper(iter.next());
          ps.setString(3, lw.getLine().getCustID());
          ps.setString(4, lw.getLine().getCustName());
          ps.setString(5, lw.getLine().getProdID());
          ps.setString(6, lw.getLine().getProdName());

          double kogus = 0;
          try {
            kogus = Double.parseDouble(lw.getLine().getAmount());
          } catch (NumberFormatException nfe){};
          ps.setDouble(7, kogus);
          String bb = lw.getLine().getBestBefore();
          System.out.println(kogus+"bb '"+lw.getLine().getBestBefore()+"'"+bb+"'");
          ps.setString(8, bb);
          ps.setInt(9, lw.getLine().getDefectCode());
          ps.setString(10, lw.getLine().getProdType());
          ps.setString(11, lw.getLine().getDefectText());

          ps.executeUpdate();
        }

        con.commit();
        ps.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }
  }


   public String getProductSortingData(){
   	return null;
   }
   public void insertDistribution(DistEvalListWrapper wr){
     String sql = "INSERT into dbo.mj_distributionest values ("
       + "?,"   //   agent	C(50),
        + "?,"   //   agentname	C(200),
        + "?,"   //   clientid	C(50),
        + "?,"   //   clientname	C(200),
        + "?,"   //	prodid	C(50),
        + "?,"   //   prodname	C(200),
        + "date() ,"   //	sentdate	D,
        + "?,"   //	evalcode	C(50),
        + "?,"   //	bestbefore	C(50),
        + "?,"   //   ownproduct	L,
        + "?,"   //	evalname	C(200),
        + "?,"   //	variation	I,
        + "?,"   //	package1	I,
        + "?,"   //	presence	L,
        + "?,"   //	StockDisp	N,
        + "?,"   //	StockWare	N,
        + "?,"   //	SpacePros	I,
        + "?,"   //	FaceAmBef	I,
        + "?,"   //	FaceAmAft	I,
        + "?,"   //	PlanoEval	I,
        + "?,"   //	PalAmount	I,
        + "?,"   //	action	L,
        + "?,"   //	dominance	I,
        + "?,"   //	FirstInFl	I,
        + "?,"   //	PlacPres	I,
        + "?,"   //	NormalPr	N,
        + "?,"   //	ActionPr	N,
        + "?,"   //	comment	C(200),	reserv1	C(200),	reserv2	C(200),
        + "?," 
        + "?," 
   	 + "?,"   //	resint1	I,resint2	I,
   	 + "?," 
        + "?,"   //	resdou1	N,	resdou2	N,
        + "?," 
        + "?,"   //   a2	I,
        + "?,"   //   a3	I,
        + "?,"   //	a4	I,
        + "?,"   //	vobbler	I,
        + "?,"   //	pricetag	I,
        + "?,"   //	numberof11	I,
        + "?,"   //	numberof12	I,
        + "?,"   //	numberof21	I,
        + "?,"   //	numberof22	I,
        + "?,"   //	planocomm	C(200),
        + "?,"   //	colldtime	C(12),
        + "?)" ; //	isbrand	L  		
    
        try {
        	Connection con = getNewConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            

            for (Iterator iter = wr.getDistEval().getLines().iterator(); iter.hasNext(); ) {
              DistEvalLineWrapper lw = new DistEvalLineWrapper(iter.next());

              ps.setString(1, wr.getDistEval().getAgentID());
              ps.setString(2, wr.getDistEval().getAgentName());
              ps.setString(3, lw.getLine().getCustID());
              ps.setString(4, lw.getLine().getCustName());
              ps.setString(5, lw.getLine().getProdID());
              ps.setString(6, lw.getLine().getProdName());
              ps.setString(7, lw.getLine().getEvalCode());
              ps.setString(8, lw.getLine().getBestBefore());
              ps.setBoolean(9, lw.getLine().isOwnProduct());
              ps.setString(10, lw.getLine().getEvalName());
              ps.setInt(11, lw.getLine().getVariation());
              ps.setInt(12, lw.getLine().getPackage1());
              ps.setBoolean(13, lw.getLine().isPresence());
              ps.setDouble(14, lw.getLine().getStockAtDisplay());//   + "?,"   //	StockDisp	N,
              ps.setDouble(15, lw.getLine().getStockAtWarehouse());// + "?,"   //	StockWare	N,
              ps.setInt(16, lw.getLine().getSpaceProsentage());//  + "?,"   //	SpacePros	I,
              ps.setInt(17, lw.getLine().getFaceAmountBefore());//  + "?,"   //	FaceAmBef	I,
              ps.setInt(18, lw.getLine().getFaceAmountAfter());// + "?,"   //	FaceAmAft	I,
              ps.setInt(19, lw.getLine().getPalnoeval());//  + "?,"   //	PlanoEval	I,
              ps.setInt(20, lw.getLine().getPalAmount());//  + "?,"   //	PalAmount	I,
              ps.setBoolean(21, lw.getLine().isAction());//  + "?,"   //	action	L,
   		      ps.setInt(22, lw.getLine().getDominance());// + "?,"   //	dominance	I,
   		      ps.setInt(23, lw.getLine().getFirstinflow());// + "?,"   //	FirstInFl	I,
   		      ps.setInt(24, lw.getLine().getPlacespresent());// + "?,"   //	PlacPres	I,
   		      ps.setDouble(25, lw.getLine().getNormalPrice());  // + "?,"   //	NormalPr	N,
   		      ps.setDouble(26, lw.getLine().getPromoPrice());//+ "?,"   //	ActionPr	N,
   		      ps.setString(27, lw.getLine().getComment());//   + "?,"   //	comment	C(200),	reserv1	C(200),	reserv2	C(200),
   		      ps.setString(28, lw.getLine().getReserv1());
   		      ps.setString(29, lw.getLine().getReserv2());
   		      ps.setInt(30, lw.getLine().getResint1());// + "?,"   //	resint1	I,resint2	I,
                ps.setInt(31, lw.getLine().getResint2());  
                ps.setDouble(32, lw.getLine().getResdouble1());// + "?,"   //	resdou1	N,	resdou2	N,
                ps.setDouble(33, lw.getLine().getResdouble2());
   		      ps.setInt(34, lw.getLine().getA2());//  + "?,"   //   a2	I,
   		      ps.setInt(35, lw.getLine().getA3());//  + "?,"   //   a3	I,
   		      ps.setInt(36, lw.getLine().getA4());//  + "?,"   //	a4	I,
   		      ps.setInt(37, lw.getLine().getVobbler());// + "?,"   //	vobbler	I,
   		      ps.setInt(38, lw.getLine().getPricetag());// + "?,"   //	pricetag	I,
   		      ps.setInt(39, lw.getLine().getNumberOF11());// + "?,"   //	numberof11	I,
   		      ps.setInt(40, lw.getLine().getNumberOF12());// + "?,"   //	numberof12	I,
   		      ps.setInt(41, lw.getLine().getNumberOF21());// + "?,"   //	numberof21	I,
   		      ps.setInt(42, lw.getLine().getNumberOF22());// + "?,"   //	numberof22	I,
   		      ps.setString(43, lw.getLine().getPlanoComment());//  + "?,"   //	planocomm	C(200),
   		     String time = parent.getTimeHHpMMpSSStr(lw.getLine().getDate().getTime());
   			       ps.setString(44, time);//  + "?,"   //	colldtime	C(12),
                ps.setBoolean(45, lw.getLine().isBrand());// + "?)" ; //	isbrand	L  		
   		  
              ps.executeUpdate();
            }

            //con.commit();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
      }



   public Vector getFixedAssortmentData() {
           Vector lines = new Vector();
           try {

                   FileReader fr = new FileReader(getDir() + DATA_FILE);
                   BufferedReader reader = new BufferedReader(fr);
                 String line = null;

                 while(!(line = reader.readLine()).equals("[Get Pricelists]")){
                 }
                 line = reader.readLine();

                 while((line = reader.readLine()) != null ){
                   line = line.replaceAll("\",\"", "|");
                //  System.out.println(line);
                   StringTokenizer st = new StringTokenizer(line, SEPARATOR);

                   String custID = " ";
                   String clGroup = " ";
                   String prodID = " ";
                   String prodGroup = " ";
                   MatrixLine mline = new MatrixLine();

                   if (st.hasMoreTokens()) {
                   	clGroup = clear(st.nextToken());
                   }
                   if(clGroup.trim().length() != 0 && !clGroup.trim().equals("BAZINIS")){
                	
                   if (st.hasMoreTokens()) {
                     prodID = clear(st.nextToken());
                   }


                   if (custID != null && custID.trim().length() > 0) {
                     mline.setCustID(custID.trim());
                   }
                   if (clGroup != null
                       && clGroup.trim().length() > 0) {
                     mline.setCliGroup(clGroup.trim());
                   }

                   if (prodID != null && prodID.trim().length() > 0) {
                     mline.setProdID(prodID.trim());
                   }
                   if (mline.getProdID() == null && prodGroup != null
                       && prodGroup.trim().length() > 0) {
                     mline.setProdGroup(prodGroup);
                   }

                   lines.add(mline);
                   
                   }
                 }


                         } catch (Exception e) {
                             getParent().showError(e);
                         }


           return lines;
   }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
   public DealerProduct getDealerProductsList() {
     return null;
   }




       public String getSaveStockInfo(){
       return null;
       }



  private Date getDate2(String mmddyy){
          long lon = 0;

          Calendar cal = Calendar.getInstance();
          if(mmddyy.indexOf(".") != -1){
        	    cal.set(Calendar.YEAR, Integer.parseInt("20" + mmddyy.substring(0, 2)));
        	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(3, 5))-1);
        	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(6, 8)));
        	    //System.out.println(cal.getTime());"09.03.07" 10.08.17  
          }else if (mmddyy.indexOf("/") != -1){
        	  cal.set(Calendar.YEAR, Integer.parseInt("20" + mmddyy.substring(6, 8)));
      	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(0, 2))-1);
      	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(3, 5)));
      	    //System.out.println(cal.getTime());"09.03.07" 10.08.17    
          }
          
          
   Date date = new Date(lon);
   date.setTime(cal.getTimeInMillis());
  return date;
  }




   public Vector getDebt(String agentCode){
        return this.debtList;
  }

   public void connectionDown(){
   }
   public void connectionUp(){
   }

           public String getDealerCustomerCSV(String agent){
            return null;
            }

                                               public String  getCustProdDiscounts(Agent agent){
                                                return null;
                                                }

                                               public TreeMap getGroupSorting() {
                                                 TreeMap   tm = new TreeMap();
                                                 setTreeGroup(true);
                                                 try {
                                                    FileReader fr = new FileReader(getDir() + "GroupSorting.txt");
                                                  BufferedReader reader = new BufferedReader(fr);
                                                String line = null;
                                              
                                                while((line = reader.readLine()) != null ){                                                
                                                   StringTokenizer st = new StringTokenizer(line, ";");
                                                    if (st.hasMoreTokens()){
                                                      String id = clear(st.nextToken());
                                                            if (st.hasMoreTokens()){
                                                             String name = clear(st.nextToken());
                                                             tm.put(id, name);
                                                           
                                                        }
                                                      }
                                                    }
                                                  } catch (IOException e){
                                                    e.printStackTrace();
                                                  }
                                                     return tm;
                                               }
                                               
   public TreeMap getProdGroups() throws SQLException {
     TreeMap   tm = new TreeMap();
     setTreeGroup(true);
     try {
        FileReader fr = new FileReader(getDir() + DATA_FILE);
      BufferedReader reader = new BufferedReader(fr);
    String line = null;
    while(!(line = reader.readLine()).equals("[Get Prod Sub Groups]")){
    }
    line=reader.readLine();
    while(((line = reader.readLine()).trim().length()) > 0 ){
       line = line.replaceAll("\",\"","| ");
       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
        if (st.hasMoreTokens()){
        	String add = clear(st.nextToken());
          String id = add + clear(st.nextToken());
                if (st.hasMoreTokens()){
                 String name = Converter.enCodeEst(clear(st.nextToken()));
                 tm.put(id, name);
               
            }
          }
        }
      } catch (IOException e){
        e.printStackTrace();
      }
         return tm;
   }

   public TreeMap getProdGroupsSecond() throws SQLException {
    TreeMap   tm = new TreeMap();
   /* try {
       FileReader fr = new FileReader(getDir() + DATA_FILE);
     BufferedReader reader = new BufferedReader(fr);
   String line = null;

   while(!(line = reader.readLine()).equals("[Get Prod Sub Groups]")){
   }
   line=reader.readLine();
   while(((line = reader.readLine()).trim().length()) > 0 ){
      line = line.replaceAll("\",\"","|");
      StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
         String groupID = clear(st.nextToken());
               if (st.hasMoreTokens()){
                String name = clear(st.nextToken());
                if(groupID.length()>5){
                   tm.put(cleardots(groupID),cleardots(groupID) + " " +name);
                }
           }
         }
       }


     } catch (IOException e){
       e.printStackTrace();
     }*/
        return tm;
  }


   private String cleardots(String idx){
   	while(idx.indexOf(".")>0){
    idx = idx.substring(0,idx.indexOf("."))+idx.substring(idx.indexOf(".")+1);
   	}
	   return idx;
   }

   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();


     try {
       FileReader fr = new FileReader(getDir() + DATA_FILE);
       BufferedReader reader = new BufferedReader(fr);
     String line = null;
     while((line = reader.readLine()).startsWith("[Get Agents]")){
     }
      while(((line = reader.readLine()).trim().length()) > 0 ){

           line = line.replaceAll("\",\"","|");
      	StringTokenizer st = new StringTokenizer(line, SEPARATOR);

        if (st.hasMoreTokens()) {
          String id = clear(clear(st.nextToken()));
          Agent agent = new Agent(id);
          if (st.hasMoreTokens()) {
            String name = clear(st.nextToken());
            if (name.equals("")){ name = "<" + id + ">";}
            agent.setName(name);
            agent.setObject("LOC-2");
            agent.setIsPilot(true);
            
		       	

				String agentAGENTS = getParent().getProps().getProperty("dub." +agent.getID());
				if(agentAGENTS != null){
      	           StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
			    while(stag.hasMoreTokens()){  	
                 agent.addDubbler(stag.nextToken());
			    }

				}
        	agents1.put(agent.getID(), agent);
            tm.put(agent.getName(), agent);
          }
        }
      }


 //Loe sisse kliendi pysi allalhindlused
      FileReader fr1 = new FileReader(getDir() + DATA_FILE);
      BufferedReader reader1 = new BufferedReader(fr1);
    String line1 = null;

    while(!(line1 = reader1.readLine()).equals("[Get Customer Discount]")){
    }
    line1=reader1.readLine();
    while(((line1 = reader1.readLine()).trim().length()) > 0 ){
        line1 = line1.replaceAll("\",\"","| ");
        StringTokenizer st1 = new StringTokenizer(line1, SEPARATOR);
         String id = clear(st1.nextToken());
         String value = clear(st1.nextToken());
         discounts.put(id,value);
    }

 // loe sisse yle makset�htaja summad
    FileReader fr2 = new FileReader(getDir() + DATA_FILE);
    BufferedReader reader2 = new BufferedReader(fr2);
  String line2 = null;

  while(!(line2 = reader2.readLine()).equals("[Get Payer Overdue]")){
  }
  line2=reader2.readLine();
  while(((line2 = reader2.readLine()).trim().length()) > 0 ){
      line2 = line2.replaceAll("\",\"","| ");
      StringTokenizer st2 = new StringTokenizer(line2, SEPARATOR);
     // System.out.println(line2);
       String id = clear(st2.nextToken());
       Debt debt = new Debt ();
       debt.setDoknr(clear(st2.nextToken()));
       if(st2.hasMoreTokens()){
       debt.setDue(clear(st2.nextToken()));
       }
       overdebts.put(id,debt);
  }





     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }




       public String getDistribution() {
         return null;
       }


   public TreeMap getCustomers(Agent agent){
	   String date = "";
    try {
        FileReader fr = new FileReader(getDir() + DATA_FILE);
        BufferedReader reader = new BufferedReader(fr);
      String line = null;
      Date datenow = new Date(Calendar.getInstance().getTimeInMillis());
      this.debtList = new Vector();
      while(!(line = reader.readLine()).equals("[Get Debt Information]")){
     // while((line = reader.readLine()).startsWith("[Get Debt Information]")){
      }
      line = reader.readLine();
   
       while(((line = reader.readLine()).trim().length()) > 0 ){
            line = line.replaceAll("\",\"","| ");
           // System.out.println(line);
       	StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       	//   	"klient","number","kuup�ev","summa"
         if (st.hasMoreTokens()) {
           String custID = clear(st.nextToken()).trim();
           Debt debt = new Debt();
           debt.setCustID(custID);
           if (st.hasMoreTokens()) {
             String bill = clear(st.nextToken().trim());
             debt.setDoknr(bill);
             if (st.hasMoreTokens()) {
               date = clear(st.nextToken().trim());
               Date due = getDate2(date);
               debt.setDuedate(due);
               long days = datenow.getTime()-due.getTime();
               int dueint = Integer.parseInt((days/86400000)+"");
               debt.setDue(dueint + "");
               int old = 0;
               if(debtDays.get(custID) != null){
                old = Integer.parseInt((String)debtDays.get(custID));
               }             
               if(old<dueint){
                  this.debtDays.put(custID,dueint+"") ;
               }
               }
             if (st.hasMoreTokens()) {
                String sum = clear(st.nextToken());
                debt.setSum(Double.parseDouble(sum));
              }
             if (st.hasMoreTokens()) {
               clear(st.nextToken());
              }
            this.debtList.add(debt);
           }
         }
       }
      } catch (Exception e){
        e.printStackTrace();
        System.out.println("aiaiaii" + date);
      }


     TreeMap tm = new TreeMap();
     this.customers = new ArrayList();

     try {
       FileReader fr = new FileReader(getDir() + DATA_FILE);
       BufferedReader reader = new BufferedReader(fr);
     String line = null;

     while(!(line = reader.readLine()).equals("[Get Customers]")){
     }
     line=reader.readLine();
     while(((line = reader.readLine()).trim().length()) > 0 ){
        line = line.replaceAll("\",\"","| ");
        //System.out.println(line);
        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
         String name = clear(st.nextToken());
         if (st.hasMoreTokens()){
           String id = clear(st.nextToken());
           //System.out.println(line);
           Customer customer = new Customer(id);
            String payer = null;
           if (name.equals("")) name = "<" + id + ">";
           customer.setName( Converter.enCodeEst(name));
           if (st.hasMoreTokens()){
           	payer = clear(st.nextToken());
           	if(payer.indexOf("#") != -1 ){
           		payer = payer.substring(0,payer.indexOf("#"));
           	}
             customer.setPayer(payer);
           }

           if (st.hasMoreTokens()){
             String aadress = clear(st.nextToken());
             if( aadress != null){
               customer.setAddress( Converter.adressLink(Converter.enCodeEst(aadress),"lihtuania"));
               customer.setName(customer.getName()+ " " +  Converter.enCodeEst(aadress));
             }
             //System.out.println(customer.getAddress());
           }

           if (st.hasMoreTokens()){
           	String ag = st.nextToken();
             customer.addAgent(new Agent(clear(ag)));
             
             if (ag != null && ag.trim().length()>0){
				Agent agg = ((Agent)this.getAgents1().get(ag.trim()));
				//System.out.println(agg);
				if(agg != null && agg.getDubbler().size()>0){
					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
						customer.addAgent(new Agent((String)agg.getDubbler().elementAt(i)));
						//System.out.println(customer.getName() + customer.getAgents());
					}
				}
			}
             
             //System.out.println(customer.getAgents());
           }

           String paym = null;
           if (st.hasMoreTokens()){
           	paym = clear(st.nextToken());
            paym = paym.replaceAll("DIENOS"," ");
            paym = paym.replaceAll("DIEN"," ");
            paym = paym.trim();
           }

           if (st.hasMoreTokens()){
           	String num = clear(st.nextToken());
           	if(num.trim().length()>0 && discounts.get(num) != null ){
             customer.setDiscount(Double.parseDouble((String)discounts.get(num)));
           	}
           }


           if (st.hasMoreTokens()){
             if(clear(st.nextToken()).equals("1")){
               customer.setSalesBlock(true);
             }
           }
       	String debti = null;
        String used = "0";
       	if (st.hasMoreTokens()){
           	 debti = clear(st.nextToken());
           		if (overdebts.containsKey(id)){
           		debti = ((Debt)overdebts.get(id)).getDoknr();
                           used = ((Debt)overdebts.get(id)).getDue() ;
           		}
           		if (overdebts.containsKey(payer)){
               		debti = ((Debt)overdebts.get(payer)).getDoknr();
                     used = ((Debt)overdebts.get(payer)).getDue() ;
               		}
           }


           if (st.hasMoreTokens()){
            customer.setVisit(clear(st.nextToken()));
          }
        
           if (st.hasMoreTokens()){
            customer.setCustType(clear(st.nextToken()));
          }
           if (st.hasMoreTokens()){
           	String bll = clear(st.nextToken());
           	if (customer.getSalesBlock()){
              	 customer.setSalesBlock(false);
           	int extra = Integer.parseInt(bll);
           		if(debtDays.get(customer.getID())!= null && customer.getPaymTerm().trim().length()>0 &&
           				(extra < Integer.parseInt((String)debtDays.get(customer.getID())) )){

               		//System.out.println(customer.getID()+" "+extra+" "+debtDays.get(customer.getID()));
           	 customer.setSalesBlock(true);
           	}
          }
           }
           if (st.hasMoreTokens()){
            customer.setGroup(clear(st.nextToken()));
          }

           if (st.hasMoreTokens()){
           	String yes = clear(st.nextToken());
           	if (yes != null && !yes.trim().equals("0")){
               // customer.setNodebt(true);
           	}
          }

           if(paym == null || paym.trim().length()==0){
           	paym = " ";
           }
           
           if (customer != null &&!customer.isNodebt()){
        	   if(debti != null && debti.trim().length() > 0){
                   customer.setDebt(Double.parseDouble(debti));   
        	   }
        	   if(paym.trim().length() > 0){
               	customer.setPaymTerm(paym);   
        	   }
        	   if(used != null  && used.trim().length() > 0){
               	customer.setUsedCredit(Double.parseDouble(used.replaceAll(",",".").replaceAll(" ","")));
        	   }                 
           }

           if (st.hasMoreTokens()){
           	String yes = clear(st.nextToken());
           	if (yes != null && yes.trim().length() >0){
           		customer.setGroup(yes.trim());
           	}
          }
           if (st.hasMoreTokens()){
              	 String grupp = clear(st.nextToken());
               	if (grupp != null && grupp.trim().length()>0){
                    customer.setGroup(grupp.trim());
               	}
             }

            tm.put(customer.getName(), customer);
            this.customers.add(customer.getID());
           
         }
       }
     }

     reader.close();
     fr.close();

     } catch (IOException e){
       e.printStackTrace();
     }
     return tm;
   }


   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
   TreeMap lok2 = new TreeMap();
   TreeMap lok4 = new TreeMap();
   TreeMap sorting= getGroupSorting();
   System.out.println("sorting " + sorting.size());
     try {
    	 
    	  try {

              FileReader fr = new FileReader(getDir() + DATA_FILE);
              BufferedReader reader = new BufferedReader(fr);
            String line = null;

            while(!(line = reader.readLine()).equals("[Get stock]")){
            }
            if(line!=null){

            line = reader.readLine();

             while(((line = reader.readLine()).trim().length()) > 0 ){
                  line = line.replaceAll("\",\"","|");
                  //System.out.println(line);
             	StringTokenizer st = new StringTokenizer(line, SEPARATOR);
             	//   	           	"toode","Stock","warehouse"

              String toode = null;
              double seis = 0;;
              String ladu  = null;
             	if (st.hasMoreTokens()) {
               	toode = clear(st.nextToken());
               }
             	if (st.hasMoreTokens()) {
               	seis = Double.parseDouble(clear(st.nextToken()));
               }
             	if (st.hasMoreTokens()) {
               	ladu = clear(st.nextToken());
               }
             	
             	if(seis != 0){
             		if(ladu.equals("XKMP")){
                 		lok2.put(toode, seis + "");
                 	}
                 	if(ladu.equals("LOC 4")){
                 		lok4.put(toode, seis + "");
                 	}
                 		
             	}
                  //System.out.println(ladu+" "+toode+" "+seis+"");
              }}

      } catch (Exception e) {
          getParent().showError(e);
      }

    	 
    	 
    	 
    	 
    	 
    	 
    	 
    	 
    	 
       FileReader fr = new FileReader(getDir() + DATA_FILE);
       BufferedReader reader = new BufferedReader(fr);
     String line = null;

     while(!(line = reader.readLine()).equals("[Get Products]")){
     }
     line=reader.readLine();
     while(((line = reader.readLine()).trim().length()) > 0 ){
        line = line.replaceAll("\",\"","| ");
  String gr = "";
       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
         String id = clear(st.nextToken());
         String weight ="";
         Product prod = new Product(id);
         if (st.hasMoreTokens()){
         	String name = clear(st.nextToken());
         	name = name.substring(0,1) + (name.substring(1,name.length())).toLowerCase();
         	//System.out.println(name);

           if (st.hasMoreTokens()){
           	String pr =st.nextToken().replace(',','.');
            //System.out.println("price" + pr);
             double price = Double.parseDouble(clear(pr));
             if (price > 0){
               prod.setPrice(price);}
               if (st.hasMoreTokens()){
                String add = (cleardots(clear(st.nextToken())));
                if(add.equals("BENZINAS") || add.equals("TARA")){
                	prod.setPrice(0);//siis ei l�he toode kaasa
                }
                if (st.hasMoreTokens()){
                	String gru = cleardots(clear(st.nextToken()));
                    prod.setGroup(add + gru);
                    if(sorting.get(gru) != null){
                    	gr = (String)sorting.get(gru);
                    }
                    if(gru.length()>3){
                    	   gr = gr + gru.substring(0,3);
                    }else{
                    	   gr = gr + gru;
                    }
                 
                    }
                     if (st.hasMoreTokens()){
                       prod.setUnit(clear(st.nextToken()));
                       // System.out.println("ladu"+prod.getStock());
                       if (st.hasMoreTokens()){
                         String ean = clear(st.nextToken());
                         if (ean.trim().length()>0){
                           prod.setEAN(ean);
                         }
                         if (st.hasMoreTokens()){
                             clear(st.nextToken());
                      /*   int xx = Integer.parseInt(xxx);
                         if(xx == 1){
                         	prod.setStatusTop("1");
                         }else if (xx == 2){
                           prod.setMustBe("1");
                         }
                         else if (xx == 3) {
                           prod.setMustBe("1");
                        	prod.setStatusTop("1");
                         }
                         else if (xx == 4) {
                            prod.setStatusNew("1");
                          }else if (xx == 5) {
                            prod.setStatusSellout("1");
                         }*/
                         if (st.hasMoreTokens()){
                         	String discou = clear(st.nextToken());
                         	if(discou != null && discou.trim().length()>0){
                         		//System.out.println(line);
                            prod.setMaxDiscount(Double.parseDouble(discou));
                         	}
                         String cases = clear(st.nextToken());
                            if (st.hasMoreTokens()){
                             	cases = clear(st.nextToken());
                             	if(cases != null && cases.trim().length()>0){
                             		double inCases =Double.parseDouble(cases);
                             		if(inCases != 0){
                                        prod.setInCases(inCases);	
                             		}

                                if (st.hasMoreTokens()){
                                	 weight = clear(st.nextToken());
                                         if(weight != null && weight.trim().length()>0){
                                           weight = weight.trim();
                                   }
                                }
                             }
                        }
                     }
                   }
                 }
               }
             }
           }
           prod.setVat("21");
           prod.setName("    " + gr + " " +   Converter.enCodeEst(name));
           
         }
         
         if(lok2.get(prod.getID()) != null){
        	 prod.setStock(Double.parseDouble((String)lok2.get(prod.getID())));
        	 
         }
         if(lok4.get(prod.getID()) != null){
        	 prod.setPartii("Loc4 = " + ((String)lok4.get(prod.getID())));
         }
         
         if(prod.getPrice() > 0){
             tm.put(prod.getName(), prod);	 
         }
         
        }

     }
     } catch (IOException e){
       e.printStackTrace();
     }
     return tm;
   }

   public String getAssortmentStr(Agent agent){
    StringBuffer sb = new StringBuffer();

    try {
      FileReader fr = new FileReader(getDir() + DATA_FILE);
      BufferedReader reader = new BufferedReader(fr);
    String line = null;
   while(!(line = reader.readLine()).equals("[Get Products]")){
    }
    line=reader.readLine();
    while(((line = reader.readLine()).trim().length()) > 0 ){
       line = line.replaceAll("\",\"","| ");

   // "toode","nimi","myygihind","grupp1",grupp2,"unit","ean","status","maxdiscount","tyypi kuuluvus"

      StringTokenizer st = new StringTokenizer(line, SEPARATOR);
      if (st.hasMoreTokens()){
        String id = clear(st.nextToken());
        if (st.hasMoreTokens()){
           st.nextToken();
          if (st.hasMoreTokens()){
          	  st.nextToken();
              if (st.hasMoreTokens()){
               st.nextToken();
               if (st.hasMoreTokens()){
               	st.nextToken();
                   }
                    if (st.hasMoreTokens()){
                    	st.nextToken();
                    	if (st.hasMoreTokens()){
                    		st.nextToken();
                    			if (st.hasMoreTokens())
                    				{st.nextToken();
                    				if (st.hasMoreTokens()){
                    					st.nextToken();
                    					if (st.hasMoreTokens()){
                    						String type = clear(st.nextToken());
                    						//ABCD
                    						int i = type.trim().length();
                    						if(i>0){
                    						while (i != 0){
                    							sb.append(" ; ;" + type.substring(i-1,i) + ";" + id + "\n");
                    							i = i -1;
                    						}

                    						}

                            }
                    }}
                  }
                }
              }
            }
          }
        }
       }}


     catch (IOException e){
      e.printStackTrace();
    }
    return sb.toString();
  }


   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
     HashMap kliendid = new HashMap();
     try {
       FileReader fr = new FileReader(getDir() + DATA_FILE);
     BufferedReader reader = new BufferedReader(fr);
   String line = null;

   //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"

   while(!(line = reader.readLine()).equals("[Get Customer Products]")){
   }
  line=reader.readLine();
   while(((line = reader.readLine()).trim().length()) > 0 ){
      line = line.replaceAll("\",\"","|");
      if(line.length()< 15){
    	 break;
      }
      StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
         String custID = clear(st.nextToken());
               if (st.hasMoreTokens()){
                 String prodID = clear(st.nextToken());
                 double lastamount = 0;
                 double amount = 0;
                 String lastdate = " ";
                 if (st.hasMoreTokens()){
                    amount = Double.parseDouble(clear(st.nextToken()));
                }
                 if (st.hasMoreTokens()){
                     lastdate = clear(st.nextToken());
                 }

                 if (st.hasMoreTokens()){
                     lastamount = Double.parseDouble(clear(st.nextToken()));
                 }
                
                 
                 double percent = 0;
                 if(amount>0){
                 	percent =  amount / lastamount * 100;
                 }
                 
              
                 
                // teeme 70 % kriteeriumi 
                 
                 
                 double open = 0;
                 if(amount == 0 && lastamount >0){
                 	open = lastamount;
                 	System.out.println("LisID :" + line + percent);
                    
                 }else if (percent < 70){
                 	open = lastamount - amount ;
                 	System.out.println("LisID :" + line + percent);
                 }
                 
                 
                    if(lastdate.length()>4){
                    lastdate = lastdate.substring(0,5);
                    }else{
                    lastdate =" ";
                    }
                    Customer cust = (Customer)kliendid.get(custID);
                    if (cust == null){
                      cust = new Customer(custID);
                    }
                    Product prod = new Product(prodID);

                 CustProd cp = new CustProd(custID, prodID,amount , 0, 0,lastdate,0,open,0," ");

                 cust.addProduct(prod,cp,false);
                 kliendid.put(custID, cust);
                   vec.addElement(cp);

           }
         }
       }



    fr = new FileReader(getDir() + DATA_FILE);
    reader = new BufferedReader(fr);
  line = null;

           int counter = 0;
           int cou = 0;
          String oldklient = null;
          String olddate = " ";
          String oldtoode = null;
          Vector lines = new Vector();
 while(!(line = reader.readLine()).equals("[Get Customer Products]")){
 }
 line=reader.readLine();
 while(((line = reader.readLine()).trim().length()) > 0 ){
	 
	 
    line = line.replaceAll("\",\"","|");
    lines.add(line);
 }
 lines = upsideLines(lines);
 Iterator itera  = lines.iterator();
 while(itera.hasNext()){
	 String line2 = (String)itera.next();
    StringTokenizer st = new StringTokenizer(line2, SEPARATOR);
     if (st.hasMoreTokens()){
       String klient = clear(st.nextToken());
             if (st.hasMoreTokens()){
               String toode = clear(st.nextToken());
               String kogus = "0";
               String date = " ";
               if (st.hasMoreTokens()){
                  kogus = clear(st.nextToken());
                  kogus = kogus.replace('.',',');
				  if(kogus.indexOf(",") != -1){
				  	kogus=kogus.substring(0,kogus.indexOf(",")+2);
				  }
              }
               if (st.hasMoreTokens()){
                   date = clear(st.nextToken());
               }
            
               if(cou == 0){oldklient = klient;olddate = date;}
               if(!oldklient.equals(klient)){
                             counter = 0;
                             oldklient = klient;
                             olddate = date;
                        }
               Customer c = (Customer)kliendid.get(klient);
               if (c != null ){
                 ArrayList al = (ArrayList)c.getProducts().get(toode);
                 if(!olddate.toString().equals(date.toString())){
                 	 counter++;
                 	 }
                 if (al != null){          	
                     
                   CustProd cp = (CustProd)al.get(1);
                   if (cp != null){
                   
                     if (counter == 0){cp.setS1(kogus );}
                     if (counter == 1){cp.setS2(kogus );}
                     if (counter == 2){cp.setS3(kogus );}
                     if (counter == 3){cp.setS4(kogus );}
                     if (counter == 4){cp.setS5(kogus );}
                     if(klient.equals("300004#300004001")){
                      	 //300004#300004001;300302
                        System.out.println(toode+ "Counter: " + counter +" date " + date.toString()+" olddate " + olddate.toString() + " kogus "+ kogus +"oldklient" + oldklient);
                       }

                     vec.add(cp);
                   }
                 }
                 oldklient = klient;
                 olddate = date;
               }
               cou++;
             }}}

     } catch (IOException e){
       e.printStackTrace();
     }
     return vec;
   }


public static Vector upsideLines(Vector inVec){
    Vector sorted = new Vector(inVec.size());
    while(inVec.size() > 0){
    	sorted.addElement(inVec.elementAt(inVec.size()-1));
    	inVec.removeElementAt(inVec.size()-1);
    }
    return sorted;
  }


   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "KMP";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }
  private String clear (String ss){
  //	System.out.println(ss);
    String ret =   ss.replace(',','.');
    ret =  ret.replace(',','.');
    ret =  ret.replace(',','.');
    ret =  ret.replace(',','.');
    ret =  ret.replace('"',' ').trim();
    ret =  ret.replace('"',' ').trim();
    ret =  ret.replace('"',' ').trim();


    ret = ret.replaceAll("�","11111xx");
    ret = ret.replaceAll("�","11112xx");
    ret = ret.replaceAll("�","11113xx");
    ret = ret.replaceAll("�","11114xx");
    ret = ret.replaceAll("�","11115xx");
    ret = ret.replaceAll("�","11116xx");
    ret = ret.replaceAll("�","11117xx");
    ret = ret.replaceAll("�","11118xx");
    ret = ret.replaceAll("�","11119xx");
    ret = ret.replaceAll("�","11130xx");
    ret = ret.replaceAll("�","11120xx");
    ret = ret.replaceAll("�","11121xx");
    ret = ret.replaceAll("�","11122xx");
    ret = ret.replaceAll("�","11123xx");
    ret = ret.replaceAll("�","11124xx");
    ret = ret.replaceAll("�","11224xx");
    ret = ret.replaceAll("�","11225xx");
    ret = ret.replaceAll("�","11226xx");
    ret = ret.replaceAll("�","11227xx");
    ret = ret.replaceAll("�","11228xx");
    ret = ret.replaceAll("�","11229xx");
//�
    ret = ret.replaceAll("11224xx","�");
    ret = ret.replaceAll("11225xx","�");
    ret = ret.replaceAll("11226xx","�");
    ret = ret.replaceAll("11227xx","�");
    ret = ret.replaceAll("11228xx","�");
    ret = ret.replaceAll("11229xx","�");
    
    ret = ret.replaceAll("11130xx","�");
    ret = ret.replaceAll("11119xx","�");
    ret = ret.replaceAll("11120xx","�");
    ret = ret.replaceAll("11121xx","�");
    ret = ret.replaceAll("11122xx","�");
    ret = ret.replaceAll("11123xx","�");
    ret = ret.replaceAll("11124xx","�");
    ret = ret.replaceAll("11118xx","�");
    ret = ret.replaceAll("11119xx","�");
    ret = ret.replaceAll("11117xx","�");
    ret = ret.replaceAll("11116xx","�");
    ret = ret.replaceAll("11115xx","�");
    ret = ret.replaceAll("11114xx","�");
    ret = ret.replaceAll("11113xx","�");
    ret = ret.replaceAll("11111xx","�");
    ret = ret.replaceAll("11112xx","�");
    if(ret == null || ret.trim().length()==0){
    return " ";
    }else {
    return ret;
    }
  }
   public double getSalary(String agentCode){

     return 0;
   }

   private String getDir(){
     String dir = getParent().getProps().getProperty("readin");
     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
     return dir;
   }


   public static final String SEPARATOR = "|";
   public static final String DATA_FILE = "data.txt";
   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private TreeMap overdebts = new TreeMap();
   private TreeMap discounts = new TreeMap();
   private Vector debtList = new Vector();
   private TreeMap agents1 = new TreeMap();
   private String url = "jdbc:odbc:report";
   private TreeMap debtDays = new TreeMap();

  /**
   * setBulkSales
   *
   * @param agents Map
   */

   private String getBulkSales(String agent) {
	   String res = "";
	  	 try {
	        FileReader fr = new FileReader(getDir() + DATA_FILE);
	      BufferedReader reader = new BufferedReader(fr);
	    String line = null;

	    //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"

	    while(!(line = reader.readLine()).equals("[Get Salary]")){
	    }
	    line=reader.readLine();

	    while((line = reader.readLine())!= null &&  line.trim().length() > 0 ){
	       line = line.replaceAll("\",\"","|");
	       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
	        if (st.hasMoreTokens()){
	          double sales = Double.parseDouble(clear(st.nextToken()));
	                if (st.hasMoreTokens()){
	                 String agentID = clear(st.nextToken());
	                 
	                 if (agent.equals(agentID)){
	                    res = ((int)(sales)) + " Lt.";
	                   if(st.hasMoreTokens()){
	                	   double measure = Double.parseDouble(clear(st.nextToken()));
	                	   res = (int)measure + " kg. ";
	                	   res = res + " " +(int)sales + " Lt.";
	                   }                   
	            }
	          }
	        }
	    }
	      } catch (Exception e){
	        e.printStackTrace();
	      }
	      return res;
	      
	  }
   public void setBulkSales(Map agents) {

  	 try {
        FileReader fr = new FileReader(getDir() + DATA_FILE);
      BufferedReader reader = new BufferedReader(fr);
    String line = null;

    //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"

    while(!(line = reader.readLine()).equals("[Get Salary]")){
    }
    line=reader.readLine();
    while((line = reader.readLine())!= null &&  line.trim().length() > 0 ){
       line = line.replaceAll("\",\"","|");
       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
        if (st.hasMoreTokens()){
          double sales = Double.parseDouble(clear(st.nextToken()));
                if (st.hasMoreTokens()){
                 String agentID = clear(st.nextToken());
                 Agent agent = (Agent)agents.get(agentID);
                 if (agent != null){
                   agent.setMonthSales((int)(sales));
                   if(st.hasMoreTokens()){
                	   double measure = Double.parseDouble(clear(st.nextToken()));
                	   agent.setKgSales((int)measure);
                   }                   
            }
          }
        }
    }
      } catch (Exception e){
        e.printStackTrace();
      }
  }
  
  
/**
 * @return Returns the agents1.
 */
public TreeMap getAgents1() {
	return agents1;
}
/**
 * @param agents1 The agents1 to set.
 */
public void setAgents1(TreeMap agents1) {
	this.agents1 = agents1;
}
}
