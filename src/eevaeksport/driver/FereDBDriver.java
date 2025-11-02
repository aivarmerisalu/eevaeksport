/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class FereDBDriver extends AbstractDBDriver implements  BulkUpdater ,Discounter,CrmData{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
	
    public FereDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
        confirmemail = parent.getProps().getProperty("confirmemail");
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

        try {
            Class.forName(databaseDriverName).newInstance();
            con3 = DriverManager.getConnection(url3);

        } catch (Exception e) {
            parent.showError(e);
        }
    }

   public void setCrmData(String agentID,String agentName,String custID,String custName,String tekst,java.sql.Date date) throws SQLException {
  }
   
   
   private void makePictureCat(){
	   String cmd = " SELECT a.siffer AS toode " +
  " FROM osmy/lm_mat a INNER JOIN osmy/lm_kla k ON a.grupp = k.klkood LEFT JOIN ladu/ll010000 ll " +
  " ON ll.siffer=a.siffer WHERE (a.uhik not in ('------') OR (right(ltrim(str(tarnaeg)),2)=right(str(year(date())),2) AND " +
   " left(ltrim(str(tarnaeg)),2)>iif(len(ltrim(str(month(date()))))=1,'0'+ltrim(str(month(date()))),ltrim(str(month(date()))))))" +
   " ORDER BY a.siffer " ; 
	   	
	     	String picts = parent.getProps().getProperty("pict.dir");
	 	    String pictspda = parent.getProps().getProperty("pictpda.dir");
	 	  boolean sub = true;
	 	  if("true".equals(parent.getProps().getProperty("nosub"))){
	 		sub = false;  
	 	  }

	   	try {
	   		connectionUp();
	   		Statement stmt = con.createStatement();
	   	    ResultSet	      rs = stmt.executeQuery(cmd);


	        StringBuffer buf = new StringBuffer();
	        StringBuffer buf0 = new StringBuffer();
	        int a = 0;
	        int b = 0;
	        int c = 0;
	        
	        // leiame kataloogid ja peame meeles 
	        File d  = new File(picts);
	        File[] dd = d.listFiles();

	        Vector cats = new Vector();
	        
	        for(int i = 0; i < dd.length ; i++){
	        	File t = dd[i];
	        	if(t.isDirectory() && sub){
	        		
	        		cats.add(t.getName());
	        	}
	        }
	        
	        
	        
	       // Kopeerime pildid laiali koodide lõikes neil on alakriipsuga failinimesmitu toodet 
	        //T1050_T1052_T1054_T1055_T1056_T1059
	        
	        
	        for(int i = 0; i < dd.length ; i++){
	        	File t = dd[i];
	        	if(!t.isDirectory() && t.getName().indexOf("_") != -1){
	        		String name = t.getName().substring(0,t.getName().lastIndexOf("."));
	        		System.out.println(name);
	        		StringTokenizer st = new StringTokenizer(name,"_");
	        		while (st.hasMoreTokens()){
	        			File r = new File(picts + st.nextToken().trim()+ ".jpg");
	        			copyFile(t,r);
	        			System.out.println(r.getName());
	    	        	
	        		}
	        	}
	        }
	        
	        
	        System.out.println("Piltide leidmine - katalooge sees " + cats.size() + " " + cats.toString());
	   	    while (rs.next()) {
	   	    	a++;
	   	    	c++;
	   	    	
	   	     String toode = rs.getString("toode").trim();  
	         toode = toode.replace('/', '_');
	         toode = toode.replace('/', '_');
	   	    	if(c == 100){
	   	    		  c = 0;
	   	    		  System.out.println("Piltide leidmine "  + a);	
	   	    		  
	   	    	}
	   	     
	              
	                 File f = new File(picts + toode+".jpg");
	                 String len = "0";
	                 if(f.exists()){
	                   len =	f.length() + "";
	                 } else if(cats.size() > 0 ){
	                	 for(int w = 0 ;w < cats.size();w++){
	                		f = new File(picts + cats.elementAt(w) + "/" + toode+".jpg");
	                		
	                		if(f.exists()){
	                			System.out.println("Kataloogis leitud " + picts + cats.elementAt(w) + "/" + toode+".jpg");	
	                		     len =	f.length() + "";	
	                		}                     
	                	 }
	                 }
	                 
	                 if(len.equals("0")){
	                 	//System.out.println(toode+".jpg;"  + len);
	                 	buf0.append(toode+".jpg\n");
	                 }else{
	                 	buf.append(toode+".jpg;"  + len + "\n");
	           	    	b++;
	                 }
	                 }
	   	    rs.close();
	   	    stmt.close();
	   	    con.close();
	   	
	   	    //oleasoleva kataloogi kustutamine
	   	  System.out.println("Kataloogi kustutamine "  + a);	    
	               File f = new File("list.txt");
	               if (f.exists())f.delete();
	               
	                 try {
	           		PrintWriter os = new PrintWriter(new FileWriter(
	           				  "list.txt", true));
	           		os.write(buf.toString());
	           		os.close();
	           	
	           	  }catch (Exception ex) {
	           		ex.printStackTrace();
	           	  }
	           	//  System.out.println("buf 0 " + a  + " " +  b + " "+ buf0.toString());
	           	

	  
	          	 
	        	 
	          System.out.println("Piltide kopeerimine ");	
	          //Failide kopeerimine  
	            String[] gg = new File(picts).list();
	         	  //System.out.println("gg " + gg[0] + " " + gg.length);
	            
	             for(int i = 0;gg.length>i;i++){          	
	             	File in = new File ( picts + gg[i]); 
	             	if(in.isFile()){            	
	             	File inpda = new File ( pictspda + toUpper(gg[i]).trim()); 
	             	if(!inpda.exists() && !in.getName().contains("(") && !in.getName().contains("_") && in.getName().length() > 6) {
	             	   	Converter.shrinkAndCopyFile(in,inpda);
		            	}
	                }
	             }
	             
	             //piltide kopeerimine alam kataloogist ..
	             
	             if(cats.size() >0){
	            	 for(int h = 0;h < cats.size(); h++){
	            		
	              String[]  ff = new File(picts + cats.elementAt(h)+ "/").list();
	               	   for(int i = 0;i < ff.length  ;i++){          	
	                   	File in = new File ( picts + cats.elementAt(h)+ "/" + ff[i].trim()); 
	                   	if(in.isFile() && !in.getName().contains("(") && !in.getName().contains("_") && in.getName().length() > 6){   
	                   		
	                   	File inpda = new File ( pictspda + toUpper(ff[i])); 
	                         	copyFile(in,inpda);
	                   	 }
	                   }
	                   
	            	 }
	             }
	             
	             File fff =  new File (pictspda +  "list.txt");
	       	  copyFile(f,fff);  	
	       	   	
	   	} catch (Exception e) {
	   	    getParent().showError(e);
	   	}
	   
	}
	 
	   private String toUpper(String asi ){
		   
		   asi = asi.substring(0,asi.length()-4).toUpperCase() + asi.substring(asi.lastIndexOf("."));
		   System.out.println(asi);
		   return asi ;
	   }
	   
	 public void copyFile(File in, File out) throws Exception {
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


   
   
    
    public String getProductSortingData(){
       	return null;
       }
    public String getAssortmentStr(Agent agent){
    	return null;
    }
            public String getDealerCustomerCSV(String agent) {
		return null;
	}
            public String getSaveDebtInfo() {
            	makePictureCat();
            	System.exit(0);
               	return null;
               }

            public Vector getFixedAssortmentData() {           
            Vector lines = new Vector();
           
            try {
           	  String cmd = "SELECT te.saaja  AS klient,pr.siffer AS toode FROM osmy/os_toode pr " +
           " INNER JOIN osmy/os_tell te ON te.nr=pr.recnr INNER JOIN osmy/lm_mat a ON a.siffer =pr.siffer  WHERE  " +
           " olek in (924) AND  a.uhik not in ('------')  " +
           " GROUP BY te.saaja,pr.siffer";
           	  
           	     System.out.println(cmd);

           		connectionUp();
           	 
          	       Statement stmt = con.createStatement();
          	        ResultSet rs = stmt.executeQuery(cmd);
          	        while(rs.next()){
                             String custID = " ";
                             String prodID = " ";
                             MatrixLine mline = new MatrixLine();

                             custID = rs.getString("klient");
                             prodID = rs.getString("toode");
                             
                             if (custID != null && custID.trim().length() > 0) {
                               mline.setCliGroup(custID.trim());
                             }

                             if (prodID != null && prodID.trim().length() > 0) {
                               mline.setProdID(prodID.trim());
                             }
                             
                               lines.add(mline);
                           }

          	       rs.close();
          	       con.close();
                          } catch (Exception e) {
                              getParent().showError(e);
                          }
            System.out.println("Fixed lines " + lines.size());
            return lines;
    }
        	public Vector getLastThreeData() {
        		// TODO Auto-generated method stub
        		return null;
        	}     

	public String getCustProdDiscounts(Agent agent) {
		return null;
	}
	
	private void  getEriHinnad(Vector lines){

        String sql = "select siffer as toode , i1+i2+i3  as asi from osmy/hinnad where tunnus = 'T'   order by siffer";
      HashSet olemas = new HashSet();
      TreeMap aaa = new TreeMap();
        try{
            stmt = con.prepareStatement(sql);
            ResultSet	      rs = stmt.executeQuery();
       
       
            while (rs.next()) {
            	  String kood = rs.getString("toode").trim();
                  String nimetus = rs.getString("asi").trim();
                  if(aaa.get(kood) != null) {
                	  nimetus = aaa.get(kood) + nimetus;
                  }
                aaa.put(kood, nimetus);	
            }
            rs.close();           
            stmt.close();     	
        }catch(Exception e){
        	e.printStackTrace();
        }
   
      //Tegima mapi kus liitsime kõik hinna kirjed kokku ja nyyd pakime lahti...  
        
       Iterator itera =  aaa.keySet().iterator();
        
        while(itera.hasNext()) {
        	
        	String kood = itera.next()+"";
        String nimetus = aaa.get(kood)+"";
        
        StringTokenizer st = new StringTokenizer(nimetus,"|");
        
        

        
        while (st.hasMoreTokens()){
      	  String aa = st.nextToken();
      	 

      	  if(aa.indexOf("000000") == -1 ){
      		  StringTokenizer ss = new StringTokenizer(aa,"Ų");
      		  while(ss.hasMoreTokens()){
      			  try{
      				  
      			  
      			  String saaja = ss.nextToken().trim();
      			  String kogus = ss.nextToken();
      			  String ale = ss.nextToken();
      			  String hind = ss.nextToken();
      			 
      			  MatrixLine mline = new MatrixLine();
      			  mline.setCustID(saaja);
      			  mline.setProdID(kood);
      			  try{
      				  mline.setPrice(Double.parseDouble(hind));
      			  }catch(Exception e ){
      			  }
      			
      			  try{
      				  double al = Double.parseDouble(ale);
      				  if(al > 0 ){
      					  mline.setPrice(Double.parseDouble(tooteHind.get(kood) + "") * (100-al)/100);
      				  }
      				//  mline.setDiscount(al);
      			  }catch(Exception e ){
      			  }
      			  
      			  
      			  if((mline.getPrice() > 0 || mline.getDiscount() > 0) && !olemas.contains(saaja+kood)){
      				  lines.add(mline);
      				 // System.out.println("aa " + saaja + " " + kogus + " "+ ale+ " "+ hind);
                		olemas.add(saaja+kood);
      			  }
      			
      			  }catch(Exception e ){
      			  }
      			  
      		  }
      			  
      		
          }
      	     }
      	    }
        
        
        
   
	    System.out.println("LinesEri size " + lines.size());
	}
	
	private TreeMap tooteHind = null;
	
	private void  getHinnad(Vector lines){
		
		
		String sql = "select siffer as toode , i1+i2+i3  as asi from osmy/hinnad where tunnus = 'M' and siffer not like '%*%' order by siffer";
		TreeMap res = new TreeMap();
		TreeMap vahe = new TreeMap();
	        try{
	            stmt = con.prepareStatement(sql);
	            ResultSet	      rs = stmt.executeQuery();
	       
	            while (rs.next()) {
	            	  String kood = rs.getString("toode").trim();
	                  String nimetus = rs.getString("asi").trim();
	                if(vahe.get(kood) != null){
	                	String aa = vahe.get(kood) + "";
	                	vahe.put(kood, aa + nimetus);
	                }else{
	                	vahe.put(kood, nimetus);
	                }
	            }
	            
	            rs.close();
	            stmt.close();     	
	        }catch(Exception e){
	        	e.printStackTrace();
	        }
	   
     
	    Iterator itera =    vahe.keySet().iterator();
	    
	    while (itera.hasNext()){
	    	String toode = itera.next() + "";
	    	String nimetus = vahe.get(toode) + "";
	    	StringTokenizer st = new StringTokenizer(nimetus,"|");
	    	//System.out.println("tood " + toode + " " + nimetus);
	    	int cou = 1;
            while (st.hasMoreTokens()){
          	  double aa = Double.parseDouble(st.nextToken());
          	  MatrixLine mline = new MatrixLine();
          		mline.setCliGroup(cou+ "");
				mline.setProdID(toode);
				if (aa != 0) {
					mline.setPrice(aa);
					lines.add(mline);
				}
			  	cou++;
            }
        }
	    System.out.println("Lines size " + lines.size());
	}

	public Vector getPriceMatrixData() {
		Vector lines = new Vector();
         getHinnad(lines);
         getEriHinnad(lines);
         
         
         String sql = " SELECT kood AS custid,'' AS clgroup,'' AS prodid,grupp AS alegroup,alla AS discount,0 " +
                      " AS price FROM osmy/allahind  GROUP BY custid,grupp  ORDER BY custid,grupp ";

		Statement st = null;
		try {
			connectionUp();
			st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {

				String custID = rs.getString("custid");
				String clGroup = rs.getString("clgroup");
				String prodID = rs.getString("prodid");
				String discGroup = rs.getString("alegroup");
				double discount = rs.getDouble("discount");
				double price = rs.getDouble("price");
				MatrixLine mline = new MatrixLine();

				if (custID != null && custID.trim().length() > 0) {
					mline.setCustID(custID.trim());
				}
				if (mline.getCustID() == null && clGroup != null
						&& clGroup.trim().length() > 0) {
					mline.setCliGroup(clGroup.trim());
				}

				if (prodID != null && prodID.trim().length() > 0) {
					mline.setProdID(prodID.trim());
				}
				if (mline.getProdID() == null && discGroup != null
						&& discGroup.trim().length() > 0) {
					mline.setDiscGroup(discGroup);
				}
				mline.setDiscount(discount);

				if (price != 0) {
					mline.setPrice(price);
				}
				lines.add(mline);
				//System.out.println(discGroup);
			}

			rs.close();
			st.close();
			con.close();
		} catch (SQLException ex) {
		}

		String sql2 = " SELECT kood AS custid,'' AS clgroup,'' AS prodid,grupp AS alegroup,alla AS discount,0 " +
        " AS price FROM osmy/allahind  GROUP BY custid,grupp  ORDER BY custid,grupp";

 st = null;
try {
connectionUp();
st = con.createStatement();
ResultSet rs = st.executeQuery(sql);

while (rs.next()) {

	String custID = rs.getString("custid");
	String clGroup = rs.getString("clgroup");
	String prodID = rs.getString("prodid");
	String discGroup = rs.getString("alegroup");
	double discount = rs.getDouble("discount");
	double price = rs.getDouble("price");
	MatrixLine mline = new MatrixLine();

	if (custID != null && custID.trim().length() > 0) {
		mline.setCustID(custID.trim());
	}
	if (mline.getCustID() == null && clGroup != null
			&& clGroup.trim().length() > 0) {
		mline.setCliGroup(clGroup.trim());
	}

	if (prodID != null && prodID.trim().length() > 0) {
		mline.setProdID(prodID.trim());
	}
	if (mline.getProdID() == null && discGroup != null
			&& discGroup.trim().length() > 0) {
		mline.setDiscGroup(discGroup);
	}
	mline.setDiscount(discount);

	if (price != 0) {
		mline.setPrice(price);
	}
	lines.add(mline);
	//System.out.println(discGroup);
}

rs.close();
st.close();
con.close();
} catch (SQLException ex) {
}

		
		
		
		
		
		
		
		
		
		
		return lines;
	}



	

    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = "SELECT d.firma as payer,d.saaja AS klient,aa.nr AS number ,aa.vkuup AS maksepaev , " + 
         " sum(aa.summa-aa.makstud) AS tasumata ,kuup AS kuupaev, vkuup-date() AS yle from osmy/my_arve aa " +
		 "  INNER JOIN osmy/my_tell d ON d.arvenr=aa.nr  where  " +
         " aa.summa-aa.makstud !=0 and aa.vkuup <= date()  "+
         " and aa.liik not like '%S%'  group by aa.nr,d.saaja";

      connectionUp();
      PreparedStatement st = con.prepareStatement(sql);
      //st.setString(1, agentCode);
      ResultSet rs = st.executeQuery();
      while(rs.next()){
        Debt debt = new Debt();
        debt.setPayerID(rs.getString("payer").trim());
        debt.setCustID(rs.getString("klient").trim());
        debt.setDoknr(rs.getString("number").trim());
        debt.setDuedate(rs.getDate("maksepaev"));

        debt.setSum(rs.getDouble("tasumata"));
        debt.setDate(rs.getDate("kuupaev"));
        debt.setDue(rs.getString("yle"));
        list.addElement(debt);
      }
      st.close();
      return list;
    }


    /**
	 *
	 */
    public void connectionUp(){
      connectionUp(this.url);
    }



    public String getSaveStockInfo(){
    	
  	  StringBuffer products = new StringBuffer();


  	   String sql = " SELECT a.siffer AS toode,  (kak+tulek-minek) AS kogus " +
  	   " FROM osmy/lm_mat a INNER JOIN osmy/lm_kla k ON a.grupp = k.klkood LEFT JOIN ladu/ll010000 ll " +
  	   " ON ll.siffer=a.siffer WHERE (a.uhik not in ('------') OR (right(ltrim(str(tarnaeg)),2)=right(str(year(date())),2) AND " +
  	    " left(ltrim(str(tarnaeg)),2)>iif(len(ltrim(str(month(date()))))=1,'0'+ltrim(str(month(date()))),ltrim(str(month(date()))))))" +
  	    " ORDER BY a.siffer " ;
  	   
       if (isDebug()) {
           System.out.println(sql);
       }

       try {

      	 Connection con = DriverManager.getConnection(url);
      	 PreparedStatement pstm = con.prepareStatement(sql);
      	 

         ResultSet rs = pstm.executeQuery();
        
        while (rs.next()) {
      	  String  toode =  rs.getString("toode").trim();
      	  double seis = rs.getDouble("kogus");

      	  if(seis > 0){
      		  products.append(toode + ";");
         		  products.append(" ;");
         		  products.append(getParent().getNF().format(seis) + "; "
         				+ "\n");

               // System.out.println("IA laoseisud " + seis + " " + toode);
      	  }
      	
        }
        rs.close();
        pstm.close();
         con.close();
          } catch (Exception e) {
              getParent().showError(e);
          }
          
       //  System.out.println("K�ik laoseisud tehtud " + products.toString());
          
          return products.toString();
      }




        public String getDistribution() {
          return null;
        }


    private void connectionUp(String url){
      try {
        if (con != null){
          con.close();
        }

           con = DriverManager.getConnection(url);
      } catch (Exception e) {
            getParent().showError(e);
      }

    }

    public TreeMap getProdGroups() throws SQLException {

      connectionUp(url2);

      TreeMap tm = new TreeMap();

            String cmd = " select left(trim(kood),1) AS kood ,lisa as nimi from osmy/klassif  " +
           " where grupp='GT' and lisa !='' AND lisa not like '%*%' ORDER BY lisa ";
       try {
        stmt = con.prepareStatement(cmd);
        ResultSet rs = stmt.executeQuery();
       setTreeGroup(true);
        while (rs.next()) {
          String liik = rs.getString("kood").trim();
          String nimetus = rs.getString("nimi").trim();
          if (liik != "") {
            tm.put(liik, Converter.enCodeEst(nimetus.replaceAll("'", "ä").replaceAll("", "Ä").replaceAll("å", "Õ").replaceAll("", "Ö")));
          }
        }
      }
      catch (Exception e) {
        getParent().showError(e);
      }
      connectionDown();
      return tm;
    }


    public TreeMap getProdGroupsSecond() throws SQLException {

       connectionUp(url2);

       TreeMap tm = new TreeMap();

       String cmd = "SELECT  left(kood,2) AS kood,(left(trim(kood),1)+ ' ' + right(lisa,59)) AS nimi " +
            " FROM osmy/klassif WHERE grupp ='GT' and lisa like '%*%'  ORDER BY nimi ";
       try {
         stmt = con.prepareStatement(cmd);
         ResultSet rs = stmt.executeQuery();

         while (rs.next()) {
           String liik = rs.getString("kood").trim();
           String nimetus = rs.getString("nimi").trim();
           if (liik != "") {
             tm.put(liik, Converter.enCodeEst(nimetus));
           }
         }
       }
       catch (Exception e) {
         getParent().showError(e);
       }
       connectionDown();
       return tm;
     }

    /**
     *
     */
    public void connectionDown(){
        try {
           con.close();
           con = null;
        } catch (Exception e) {
            getParent().showError(e);
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
    public TreeMap getAgents() {
        connectionUp(url2);
        
       
        String  cmd = "SELECT kood AS yksus,trim(enimi)+' '+ trim(pnimi)" +
                       " as nimetus, '1' AS objekt FROM osmy/isik  ORDER BY enimi";
        TreeMap agents = new TreeMap();

        try {
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();
         
            while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              agent.setObject(rs.getString("objekt").trim());
              String name = rs.getString("nimetus").trim();
              agent.setName(name);
              agents.put(name, agent);
            }
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        return agents;
    }

    public TreeMap getCustomers(Agent agent){
        connectionUp(url2);
        String cmd = "SELECT  f.kood, left(f.kood,5)+ ' ' + f.nimi AS nimetus,(trim(aadr1)+' '+trim(aadr2))" +
           " AS aadress, '0' AS ale , kleht AS agent2 ,tt.firma AS payer ,f.agent AS agent ,hgrupp as hinnak FROM osmy/firma f " +
        "LEFT JOIN osmy/my_tell tt ON f.kood=tt.saaja  " +
        "WHERE   (f.agent)!=0  GROUP BY kood ";
        log(cmd);
        TreeMap customers = new TreeMap();
        try {
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();


            while (rs.next()) {
              String name = rs.getString("nimetus").trim();
              String accountnum = rs.getString("kood").trim();
              Customer cust = new Customer(accountnum);
              cust.setName(Converter.enCodeEst(name.replaceAll("'", "õ").replaceAll("", "Ä").replaceAll("å", "Õ").replaceAll("", "Ö")));
              cust.setAddress(Converter.adressLink(Converter.enCodeEst(rs.getString("aadress").trim().replaceAll("'", "õ").replaceAll("", "Ä").replaceAll("å", "Õ").replaceAll("", "Ö")),"Estonia"));
              //cust.setVisit();
              if(confirmemail != null && confirmemail.trim().length()>0){
              cust.setConfirmEmail(confirmemail);
              }
              String payer =rs.getString("payer");
              if(payer != null){
                cust.setPayer(payer.trim());
                cust.setGroup(payer.trim());
              }
              String ag = rs.getString("agent2");
              if (ag != null && ag.trim().length()>0){
              //	cust.addAgent(new Agent(ag.trim()));
              }
              String discStr = rs.getString("ale");
              if (discStr != null){
                cust.setDiscount(Double.parseDouble(discStr));
              }
              String agenta = rs.getString("agent").trim(); 
            
              if( agenta.length()< 3 ){
              	cust.addAgent(new Agent(agenta));
              	if(agenta.equals("8")){
              		cust.addAgent(new Agent("44"));
              	}
              }else {
              	cust.addAgent(new Agent(agenta.substring(0,2)));
              	cust.addAgent(new Agent(agenta.substring(2,agenta.length())));
              }
              
              cust.addFixSortCode(accountnum);
              

              String hinnak = rs.getString("hinnak".trim());
              if (hinnak != null && !hinnak.equals("0")){
                cust.setCustType(hinnak);
              }
              
              
              String sql =
                  "SELECT firma AS klient,sum(aa.summa-aa.makstud) AS summa from osmy/my_arve aa where " +
                  "aa.summa-aa.makstud !=0  " +
                  " and liik not like '%S%' and vkuup<= date() and firma =? group by firma ";

            PreparedStatement  stm = con.prepareStatement(sql);
               stm.setString(1, cust.getPayer());
               //stmt.setDate(2, getStartingDate());
               ResultSet	      rs1 = stm.executeQuery();

              while (rs1.next()) {
                double debt = Double.parseDouble(rs1.getString("summa").trim());
                  cust.setDebt(cust.getDebt() + debt );
              }
              rs1.close();
              stm.close();

              customers.put(name, cust);
             // System.out.println("agenta " + cust.getAgents().toString() + "'");
             // custMap.put(cust.getPayer(), cust);
            }

            String sql2 = "select clid AS kood,agentnam AS agent ,sentdate as kuup , mestext " +
            		"AS mess from mj_crm where sentdate > date()-60 order by clid,sentdate";
            Statement stt = con3.createStatement();
            ResultSet rr = stt.executeQuery(sql2);
            StringBuffer sb = new StringBuffer ();
            while (rr.next()){
            	sb.append(rr.getString("kood").trim() + ";" + rr.getString("kuup").trim() + " " + rr.getString("agent").trim()
            			+ "|" + Converter.enCodeEst(rr.getString("mess").trim()) + "\n");
            }
            
            con.close();
            
            if(sb.length() > 2){
                File f = new File(getDir() + "customercrm.txt");
                if (f.exists())f.delete();
                  try {
            		PrintWriter os = new PrintWriter(new FileWriter(
            				getDir() + "customercrm.txt", true));
            		os.write(sb.toString());
            		os.close();
            	  }catch (IOException ex) {
            		ex.printStackTrace();
            	  }
             }
            

          }
          catch (Exception e) {
            getParent().showError(e);
          }

          return customers;
    }

    private String getDir(){
        String dir = getParent().getProps().getProperty("readin");
        if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
        return dir;
      }

    
    public TreeMap getProducts(Agent agent){
        connectionUp(url2);
  /*      String cmd1 =   "SELECT  a.siffer AS toode,trim(a.siffer) + ' ' +  a.nimi AS nimetus, right(a.uhik,5) AS tykke, a.mghind AS myygihind,  " +
    "(kak+tulek-minek) AS kogus, left (allgrupp,1) AS grupp,left(allgrupp,2) AS grupp2  " +
  " FROM osmy/lm_mat a INNER JOIN osmy/lm_kla k ON a.grupp = k.klkood  LEFT JOIN ladu/ll010000 ll  " +
  " ON ll.siffer=a.siffer  WHERE a.uhik  not in ('------') OR (right(ltrim(str(tarnaeg)),2)=right(str(year(date())),2) AND  " +
  " left(ltrim(str(tarnaeg)),2)>iif(len(ltrim(str(month(date()))))=1,'0'+ltrim(str(month(date()))),ltrim(str(month(date()))))) ORDER BY a.siffer ";
*/
        tooteHind = new TreeMap();
        
        
        
        
   String cmd = " SELECT a.siffer AS toode,trim(a.siffer) + ' ' + iif(right(a.uhik,5)='1---T','+++','') + a.nimi AS nimetus," +
   		"  right(left(a.uhik,4),3) AS tykke, a.mghind AS myygihind, " +
   " (kak+tulek-minek) AS kogus, left (allgrupp,1) AS grupp,left(allgrupp,2) AS grupp2 , grupp AS alegrupp,a.kassak as ean " +
   " FROM osmy/lm_mat a INNER JOIN osmy/lm_kla k ON a.grupp = k.klkood LEFT JOIN ladu/ll010000 ll " +
   " ON ll.siffer=a.siffer WHERE (a.uhik not in ('------') OR (right(ltrim(str(tarnaeg)),2)=right(str(year(date())),2) AND " +
    " left(ltrim(str(tarnaeg)),2)>iif(len(ltrim(str(month(date()))))=1,'0'+ltrim(str(month(date()))),ltrim(str(month(date()))))))" +
    " ORDER BY a.siffer " ;

        if (isDebug()) {
            System.out.println(cmd);
        }

        TreeMap products = new TreeMap();
        TreeMap prods = new TreeMap();

        try {
            stmt = con.prepareStatement(cmd);
            	   ResultSet   rs = stmt.executeQuery();
       
            while (rs.next()) {
              String kood = rs.getString("toode").trim();
              Product prod = new Product(kood);
              String nimetus = rs.getString("nimetus").trim();
              prod.setName(Converter.enCodeEst(nimetus.replaceAll("'", "ä").replaceAll("", "Ä").replaceAll("å", "Õ").replaceAll("", "Ö").replaceAll("", "\u2300")));
              try{
            	  String asi = rs.getString("tykke").replaceAll("-", " ").trim();
              prod.setInCases(Double.parseDouble(asi));
            } catch (Exception e){}
              prod.setPrice(rs.getDouble("myygihind"));              
              tooteHind.put(prod.getID(), prod.getPrice());
              
              String laoseis = rs.getString("kogus");
              if (laoseis != null){
                prod.setStock(Double.parseDouble(laoseis));
              }
              String grupp = rs.getString("grupp");
             if (grupp != null && grupp .trim().length() != 0){
               prod.setGroup(grupp.trim());
             }
             String grupp2 = rs.getString("grupp2");
                    if (grupp2 != null && grupp2.trim().length() != 0){
                      prod.setGroupSecond(grupp2.trim());
                    }

                    String grupp3 = rs.getString("alegrupp");
                           if (grupp3 != null){
                             prod.setAlegrupp(grupp3.trim());
                           }
                           String ean = rs.getString("ean");
                           if (ean != null && ean.trim().length() >0){
                             prod.setEAN(ean.trim());
                          //   prod.setName(prod.getName() + " " + ean.substring(ean.trim().length()-4) );
                           }          
                           
                       prod.setVat("24");   
                           
                products.put(nimetus, prod);
                prods.put(prod.getID(), prod);

            }
   
        
        } catch (Exception e) {
            getParent().showError(e);
        }

        
        
        String sql = "select siffer as toode , i1+i2+i3  as asi from osmy/hinnad where tunnus = 'T'  and i1 like '%00000%' order by siffer";
      
        try{
            stmt = con.prepareStatement(sql);
            ResultSet	      rs = stmt.executeQuery();
       
            while (rs.next()) {
            	  String kood = rs.getString("toode").trim();
                  String nimetus = rs.getString("asi").trim();
                  StringTokenizer st = new StringTokenizer(nimetus,"|");
                  while (st.hasMoreTokens()){
                	  String aa = st.nextToken();
                	  if(aa.indexOf("000000") != -1 ){
                		  StringTokenizer ss = new StringTokenizer(aa,"Ų");
                		  while(ss.hasMoreTokens()){
                			  String saaja = ss.nextToken();
                			  String kogus = ss.nextToken();
                			  String ale = ss.nextToken();
                			 
                			  
                			  String hind = ss.nextToken();
                			  
                				  try{
                					  double sss = Double.parseDouble(hind);
                					  
                					  if(sss == 0){
                						//  System.out.println("aadd " + ale+ " "+ hind);
                              			  double hhh = Double.parseDouble(tooteHind.get(kood)+"");
                    					  double al = Double.parseDouble(ale);
                        				  hind = " " + parent.getNF2().format((hhh *(100 - al)/100 )) + "€";  
                        				  //System.out.println("aacc " + ale+ " "+ hind);
                                			
                					  }else{
                						  hind = hind + "€";
                					  }
                				  }catch (Exception g ){
                					  g.printStackTrace();
                					  hind = "";
                				  }
                				
                			  
                			  if(ale.indexOf("0.00") != -1){
                				  ale = "";
                			  }else{
                				  ale = ale + "%";
                			  }
                			  
                			//  System.out.println("aa " + saaja + " " + kogus + " "+ ale+ " "+ hind);
                			  
                			  
                			  if(prods.get(kood) != null){
                				  Product prod = (Product)prods.get(kood);
                				  
                				  if(prod.getPartii() != null && prod.getPartii().trim().length() > 0){
                					  prod.setPartii(prod.getPartii() + " , " +kogus + "->" + ale + hind+ "");
                                   	    
                				  }else{
                					  prod.setPartii(kogus + "->" + ale + hind+ "");
                                   	  
                				  }
                               	   prod.setStatusDiscount("1");
                                  
                			  }
                		  }
                     
                	     }
                	    }
                	
            }
            rs.close();
            
            stmt.close();     	
        }catch(Exception e){
        	e.printStackTrace();
        }
   
   
        
        
        
        
        return products;
    }

    /**
     * Method declaration
     *
     *
     * @param agentCode
     *
     * @return
     *
     * @see
     */
    public Vector getCustomerProductsList(String agentCode) {
        connectionUp(url2);
        String cmd =
            "SELECT te.saaja  AS klient,pr.siffer AS toode,pr.kogus AS kogus,te.regaeg AS kuupaev FROM osmy/my_toode pr " +
            "INNER JOIN osmy/my_tell te ON te.nr=pr.recnr INNER JOIN osmy/lm_mat a ON a.siffer =pr.siffer  WHERE " +
            "regaeg >? AND olek in (904,905) AND  a.uhik not in ('------') order by te.saaja ,pr.siffer, te.regaeg desc " ;
           // "GROUP BY te.saaja,pr.siffer ";
        String cmd2 =
            "SELECT te.saaja  AS klient,pr.siffer AS toode,pr.kogus AS puudu ,te.regaeg AS kuupaev FROM osmy/my_toode pr " +
            "INNER JOIN osmy/my_tell te ON te.nr=pr.recnr INNER JOIN osmy/lm_mat a ON a.siffer =pr.siffer   WHERE " +
            "regaeg >date()-300 AND recnr>5000000 AND  a.uhik not in ('------') " +
            "GROUP BY te.saaja,pr.siffer ";
        if (isDebug()) {
           System.out.println(cmd);
       }

       Vector custProds = new Vector();
       HashMap kliendid = new HashMap();
       HashMap ei = null;

       try {
          stmt = con.prepareStatement(cmd);
          stmt.setDate(1, getStartingDate());
           ResultSet	      rs = stmt.executeQuery();
          
            FileReader fr = new FileReader("eisaada.txt");
            if(fr != null){
            ei = new HashMap();
            BufferedReader reader = new BufferedReader(fr);
             String line = null;
              while((line = reader.readLine()) != null){
          	    StringTokenizer st = new StringTokenizer(line, ";");
                if (st.hasMoreTokens()){
            	 ei.put(st.nextToken(),st.nextToken());
                }
              }    	
            }
          
           HashSet done = new HashSet();
           
           while (rs.next()) {
             String kliendikood = rs.getString("klient").trim();
             String tootekood = rs.getString("toode").trim();
             double kogus = Double.parseDouble(rs.getString("kogus").trim());
             String kuup = rs.getString("kuupaev");
             Customer cust = (Customer) kliendid.get(kliendikood);

             if (cust == null) {
               cust = new Customer(kliendikood);
             }

             Product prod = new Product(tootekood);
             if(!done.contains(kliendikood + "|" + tootekood )) {
            
             
             if(ei == null){
             
             cust.addProduct(prod,
                             new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                          kuup.substring(5,kuup.length()), 0, 0,0," "),false);
             kliendid.put(kliendikood, cust);
             custProds.add(new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                          kuup.substring(5,kuup.length()), 0, 0,0," "));
         	
             }else if (getEi(kliendikood,tootekood,ei)){
                cust.addProduct(prod,
                        new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                     kuup.substring(5,kuup.length()), 0, 0,0," "),false);
        kliendid.put(kliendikood, cust);
        custProds.add(new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                     kuup.substring(5,kuup.length()), 0, 0,0," "));
             }
        	 done.add(kliendikood + "|" + tootekood);
             }
           }
           rs.close();
           stmt = con.prepareStatement(cmd2);
          
           ResultSet rs2 = stmt.executeQuery();
           while (rs2.next()) {
             String toode = rs2.getString("toode").trim();
             String klient = rs2.getString("klient").trim();
             double openAmount = rs2.getDouble("puudu");
             String kuupaev = rs2.getString("kuupaev");
             Customer c = (Customer) kliendid.get(klient);
             if (c != null) {
               ArrayList al = (ArrayList) c.getProducts().get(toode);
               if (al != null) {
                 CustProd cp = (CustProd) al.get(1);
                 if (cp != null) {
                 if(cp.getLastAmountDate().compareTo(kuupaev)<0){
                     cp.setOpenAmount(openAmount);
                   custProds.add(cp);}
                 }
               }else {
                 CustProd cpnew = new CustProd(c.getID(), toode, 0, 0, 0,
                                          kuupaev.substring(5,kuupaev.length()), 0, 0,0," ");
                  cpnew.setOpenAmount(openAmount);
                 custProds.add(cpnew);
               }
             }
           }
       } catch (Exception e) {
           getParent().showError(e);
       }
       connectionDown();
       return custProds;
    }

    private boolean getEi(String klient,String toode,HashMap ei){
    	if(ei.containsKey(klient)){
    		String eiList = (String)ei.get(klient);
    		if(eiList.indexOf(toode)>0){
    			return false;
    		}
    	}
    	return true;
    }
    
    public String getPilotDayData(Agent agent){
      return null;
    }


    /**
     * Method declaration
     *
     *
     * @param agentCode
     *
     * @return
     *
     * @see
     */
    public double getSalary(String agentCode) {
      connectionUp(url2);
        double sales = 0;
        String cmd = "SELECT SUM(summa)  AS salary " +
                     "FROM osmy/my_tell te  WHERE te.osmy = ? " +
                     "AND month(regaeg)=month(date()) AND year(regaeg)=year(date())";
        log(cmd);

        try {
            stmt = con.prepareStatement(cmd);
            stmt.setInt(1, Integer.parseInt(agentCode));
            ResultSet	      rs = stmt.executeQuery();

            if(rs.next()) {
              sales = rs.getDouble("salary");
            }
        } catch (Exception e) {
            getParent().showError(e);
        }

        setSales(sales);
        return 0;
    }




    public void setBulkSales(Map agents){
        boolean ok = true;
		if (ok) return;
       String cmd = "SELECT SUM(summa)  AS sales , te.osmy AS agent " +
                     "FROM osmy/my_tell te  WHERE " +
                     "AND month(regaeg)=month(date()) AND year(regaeg)=year(date())" +
                     " GROUP BY te.osmy ";

       log(cmd);

         try {
           stmt = con.prepareStatement(cmd);

             Calendar cal = Calendar.getInstance();
             cal.set(Calendar.DAY_OF_MONTH, 1);

             ResultSet	      rs = stmt.executeQuery(cmd);
             while(rs.next()) {
               String agentID = rs.getString("agent").trim();
               Agent agent = (Agent)agents.get(agentID);
               if (agent != null){
                 agent.setMonthSales((int)rs.getDouble("sales"));
               }
             }
             rs.close();
             stmt.close();
             con.close();
         } catch (Exception e) {
             getParent().showError(e);
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
    public String getStartingTime() {
        Calendar cal = Calendar.getInstance();

        // cal.add(Calendar.MONTH, -6);
        cal.add(Calendar.MONTH,
                -Integer.parseInt(getParent().getProps().getProperty("months")));

        int month = cal.get(Calendar.MONTH) + 1;
        String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        String sqlDate = "{^" + day + '-' + month + '-'
                         + cal.get(Calendar.YEAR) + '}';

        return sqlDate;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getDriverVersion() {
        return DRIVER_VERSION;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getSubjectFilesTo() {
        return this.subjectFilesTo;
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
        getParent().log(s);
    }

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "Fere";
    private String             url = "jdbc:odbc:kiire";
    private String	       url2 = "jdbc:odbc:baas";
    private String	       url3 = "jdbc:odbc:report";
    private Connection	       con = null;
    private Connection	       con3 = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
    private String             confirmemail = null;
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

