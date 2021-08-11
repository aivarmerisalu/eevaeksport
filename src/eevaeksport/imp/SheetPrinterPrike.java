package eevaeksport.imp;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.Vector;



public class SheetPrinterPrike implements Printable {


	    private String firma = null; 
	    private String nimi = null;
	    private String aadress = null;
	    private String ring = null;
	    private String number = null; 
	    private String cases = null;  
	    private String kuupaev = null; 
	    private String agendinimi = null;
	    private String tarneHalve = null;
	    public String getTarneHalve() {
			return tarneHalve;
		}
		public void setTarneHalve(String tarneHalve) {
			this.tarneHalve = tarneHalve;
		}
		public String getAgendinimi() {
			return agendinimi;
		}
		public void setAgendinimi(String agendinimi) {
			this.agendinimi = agendinimi;
		}
		public String getKuupaev() {
			return kuupaev;
		}
		public void setKuupaev(String kuupaev) {
			this.kuupaev = kuupaev;
		}
		private String tarnepaev = null; 
		private String markused = null; 
	    public String getMarkused() {
			return markused;
		}
		public void setMarkused(String markused) {
			this.markused = markused;
		}
		public String getTarnepaev() {
			return tarnepaev;
		}
		public void setTarnepaev(String tarnepaev) {
			this.tarnepaev = tarnepaev;
		}
		private String product = null;
	    private String packID = null;
	    private Vector lines = null;
	    public Vector getLines() {
			return lines;
		}
		public void setLines(Vector line) {
			 
			this.lines = line;
		}
		public String getPackID() {
			return packID;
		}
		public void setPackID(String packID) {
			this.packID = packID;
		}
		private int addtop = 0;
	    
	    public int getAddtop() {
			return addtop;
		}
		public void setAddtop(int addtop) {
			this.addtop = addtop;
		}
		public String getProduct() {
			return product;
		}
		public void setProduct(String product) {
			this.product = product;
		}
		private int add = 0;

	    
	    public int getAddInt() {
			return add;
		}
		public void setAddInt(int add) {
			this.add = add;
		}
	
		private String getStamp(){
		    StringBuffer sb = new StringBuffer();
		    Calendar cal= Calendar.getInstance();
		    String hour =   cal.get(Calendar.HOUR_OF_DAY) + "";
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}

			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}

			sb.append("" + cal.get(Calendar.YEAR));
			sb.append("." + month  + ".");
			sb.append(day +" ");
		    
		    String min = String.valueOf(cal.get(Calendar.MINUTE));
		    String sec = String.valueOf(cal.get(Calendar.SECOND));
		     if (hour.length() == 1){
		      sb.append("0");
		    }
		     sb.append(hour);
		     sb.append("_");
		    if (min.length() == 1){
		     sb.append("0");
		   }
		     sb.append(min);
		     sb.append("_");
		     if (sec.length() == 1){
		        sb.append("0");
		      }
		    sb.append(sec);
		    return sb.toString();
		}
		
	
	    private int pages = 0;
	    private int linesLeft = 0;
	   private int currentPage = 0;
	  
	   private int getPages(){
	    	int pag = 1;
	    	int sizr = lines.size();
	    	//  System.out.println("Lehti kokku siz " + sizr);
		      if(sizr > 9 ){
		    	  sizr = sizr -9;
		    	  while (sizr > 0){
		    		 // System.out.println("Lehti kokku si2 " + sizr);
		    		  if(sizr < 15){
			    		  pag = pag + 1;
			    		  sizr = 0;
			    	  }else{
			    		  sizr = sizr -15;
			    		  pag = pag + 1;
			    	  }	  
		    	  }
		      }
		   
		   
	     return pag;
	    }
	      
		
		
		public int print(Graphics g, PageFormat pf, int page) throws
	    PrinterException {
		pages = getPages();
			
			if (page >= pages) { 
				  System.out.println("Lendasin tagasi " + pages + " trykin lehte " + page);
	           return NO_SUCH_PAGE;         
	         }
			
			  System.out.println("Lehti kokku " + pages + " trykin lehte " + page);
	      nf.setMinimumFractionDigits(0);
	      
	      Paper pp = pf.getPaper();
	      pp.setSize(594.992125984252, 841.8897637795276);
	    //  pp.setImageableArea(36.0, 36.0, 522.99212598425197, 769.8897637795276);
	      pp.setImageableArea(36.0, 36.0, 522.99212598425197, 800.8897637795276);

	      pf.setPaper(pp);
	
	      /* User (0,0) is typically outside the imageable area, so we must
	       * translate by the X and Y values in the PageFormat to avoid clipping
	       */
	      
	   
	       
	      Graphics2D g2d = (Graphics2D)g;
	      g2d.translate(pf.getImageableX(), pf.getImageableY());

	      /* Now we perform our rendering */
	      //http://www.java2s.com/Code/Java/2D-Graphics-GUI/PrintinJavaMultipage.htm
	  
	       double x, y;
	       FontMetrics fm;
	       fm = g.getFontMetrics();
	       g.setColor(Color.black);
	       x = pf.getImageableX();
	       y = addtop + pf.getImageableY() + fm.getMaxAscent(); // laseme trykkida 20 punkti allpoolt
	    
	      if(page == 0 ){
	    	  currentPage++;
	    	  linesLeft = lines.size();
	    	  System.out.println("left! " + linesLeft);

	       // fill in geometric and rendering guts here
	   
	      
	       
	       //Trykime Esimese rea kus on Timestamp
	       g.setFont(SMALL_FONT);         
           g.drawString("Tr�kitud:   " + getStamp(),(int)x, (int)y);
           g.drawString("Lk: Page " + (page+1) + " of " + (pages),(int)x + 250, (int)y);
           if(this.getTarneHalve() != null){
        	   g.setFont(NORMAL_FONT);      
        	   g.drawString("H�LVE: " + getTarneHalve(), (int)x + 150 , (int)y); 
           }
          
           y += fm.getHeight() -2;
           
           if (number != null && number.indexOf("-") != -1) {
	           g.setFont(new Font("Free 3 of 9",0,30));  
	               g.drawString("*" + number.substring(0,number.indexOf("-")).trim() + "*",(int)x  + 300, (int)y);
	               System.out.println("*" + number.substring(0,number.indexOf("-")).trim() + "*");
	               y += fm.getHeight()-3;
	       }
          if (ring != null) {
	    	   
	           g.setFont(COURIER_FONT);         
	               g.drawString("Veoring: " + ring,(int)x, (int)y);
	               fm = g.getFontMetrics();
	       }
           
           if (kuupaev != null) {
	           g.setFont(COURIER_FONT);         
	               g.drawString("Kuup�ev :     " +  kuupaev,(int)x+ 300, (int)y);
	               y += 12;
	       }
       
	       
        
	       
	       // Trykime kliendi nime
	       if (nimi != null) {
	           g.setFont(NORMAL_FONT);        
	           fm = g.getFontMetrics();
	           g.drawString("Tellimus:    " + number,(int)x + 300, (int)y);
	           g.drawString("Tarneaadress: ",(int)x, (int)y);
	           g.drawString(nimi,(int)x + 70, (int)y);
	               y += fm.getHeight() ;
	       }
	       // Trykime Kliendi aadressi
	       if (aadress != null) {
	           g.setFont(NORMAL_FONT);   
	           StringTokenizer st = new StringTokenizer(aadress,"|");
	           while(st.hasMoreTokens()){
	        	       g.drawString(st.nextToken(),(int)x + 70, (int)y);
		               y += fm.getHeight() ; 
	           }
	         
	       }
	       // Trykime Tellimuse numbri
	      
	    // Trykime Tooteinfo
	       if (markused != null) {
	           g.setFont(COURIER_FONT);         
	               g.drawString("M�rkused:",(int)x, (int)y);
	           
	       }
	       if (tarnepaev != null) {
	           g.setFont(COURIER_FONT);         
	               g.drawString("Tarneaeg:   " + tarnepaev,(int)x + 300, (int)y);
	               y += fm.getHeight();
	       }
	       if (markused != null) {
	           g.setFont(COURIER_FONT);         
	               g.drawString( markused,(int)x+10, (int)y);
	           
	       }
	       if (agendinimi != null) {
	           g.setFont(COURIER_FONT);         
	           g.drawString("M��ja:         " + agendinimi,(int)x + 300, (int)y);
	               y += 5;
	       }

	    
	       
	      
	       g.setFont(BOLD_FONT);         
           g.drawString("_________________________________________________________________________________________________",(int)x, (int)y);
           y += fm.getHeight();
           g.drawString("POS      KOOD                   NIMETUS                                PARTII      KOGUS   �HIK/ParimEnne",(int)x, (int)y);
           y += 3;
           g.drawString("_________________________________________________________________________________________________",(int)x, (int)y);
           y += fm.getHeight() + 5;
           int si = lines.size();
	    	  if(si > 9){
	    		  si = 9;
	    	  }
	    	  for(int w= 0;w < si ;w++){
	    		  linesLeft--;
		    	  OrderLine ol = (OrderLine)lines.elementAt(w);
		    	  String yhik = ol.getUnit();
		    	  if(ol.getUnit().indexOf("|") != -1){
		    		  g.setFont(BOLD_FONT);    
		    		  g.drawString("Riiulikoht:     " + ol.getUnit().substring(ol.getUnit().indexOf("|")+1) ,(int)x , (int)y);
		    		  g.setFont(SMALL_FONT); 
		    		  g.drawString( ol.getEAN() ,(int)x + 300, (int)y);
		    		  y += fm.getHeight();	
		    		  yhik = ol.getUnit().substring(0,ol.getUnit().indexOf("|"));
		    	  }else{
		    		  g.setFont(SMALL_FONT); 
		    		  g.drawString( ol.getEAN() ,(int)x + 300, (int)y);
		    		  y += fm.getHeight();
		    	  }
		    	  
		    	  g.setFont(NORMAL_FONT); 
		    	  String nimi1 = null;
		    	  String nimi2 = "";
		    	  String parimenne = "";
		    	  String parim = "";
		    	  StringTokenizer st = new StringTokenizer(ol.getTooteNimi(),"|");
		           while(st.hasMoreTokens()){
		        	   nimi1 = st.nextToken();
		        	   if(st.hasMoreTokens()){
		        		   nimi2 = st.nextToken();
		        	   }
		        	   if(st.hasMoreTokens()){
		        		   parimenne = st.nextToken();
		        		   if(parimenne.trim().startsWith("9999")){
		        			   parimenne = "";
		        		   }
		        	   }
		        	   if(st.hasMoreTokens()){
		        		   parim = st.nextToken();
		        		   if(parim.trim().startsWith("1900")){
		        			   parim = "";
		        		   }
		        	   }
		           }
		    	System.out.println(nimi1 + "'" + nimi2 + "'" + parimenne + "'");
		           g.drawString(ol.getToode() ,(int)x, (int)y);
		    	   g.drawString(nimi1 ,(int)x + 120, (int)y);
		    	   g.drawString(ol.getPartii()  ,(int)x + 255, (int)y);
		    	   g.setFont(BOLD_FONT); 
		    	   g.drawString((int)ol.getKogus()+ "" ,(int)x + 370, (int)y);
		    	   g.setFont(NORMAL_FONT); 
		    	   g.drawString( yhik  ,(int)x + 395, (int)y);
		    	   g.drawString( "....." ,(int)x + 420, (int)y);
		    	   y += fm.getHeight();
		    	   g.drawString(nimi2 ,(int)x + 120, (int)y);
		    	   g.drawString(parimenne.trim() ,(int)x + 313, (int)y);
		    	   g.setFont(SMALL_FONT); 
		    	   g.setColor(Color.red);
		    	   g.drawString(parim ,(int)x + 395, (int)y);
		    	   y += fm.getHeight()+ add;
		    	   g.setFont(NORMAL_FONT); 
		    	   g.setColor(Color.black);
		    	    
	      }
	    	  
//	        Trykime alumise otsa esilehele
		       y = 642;
		       g.drawString("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -",(int)x, (int)y);
		      
		       
		       g.drawRect((int)x + 266,(int) y , 80, 15);
		       g.drawRect((int)x + 390,(int) y , 60, 35);
		       g.drawRect((int)x + 390,(int) y+35 , 60, 35);
		       g.setFont(COURIER_FONT); 
		       g.drawString("KASTE:",(int)x+ 340, (int)y+30);
		       g.drawString("LAVASID:",(int)x+340, (int)y+65);
		       y += 10;
		      
	    	  
	    	  if (ring != null) {
		    	   
		           g.setFont(BOLD_FONT);         
		               g.drawString("Veoring: " + ring,(int)x, (int)y);
		               fm = g.getFontMetrics();
		       }
	           
	           if (kuupaev != null) {
	        	   g.setFont(BOLD_FONT);       
		        
		               g.drawString("Kuup�ev:",(int)x+ 100, (int)y);
		               g.drawString("Tellimus:" ,(int)x + 220, (int)y);
		               g.setFont(COURIER_FONT);         
		               g.drawString( kuupaev,(int)x+ 150, (int)y);
		               g.drawString( number,(int)x + 270, (int)y);
		               y += 12;
		       }
	       
		       
	        
		       
		       // Trykime kliendi nime
		       if (nimi != null) {
		           g.setFont(NORMAL_FONT);        
		           fm = g.getFontMetrics();
		          
		           g.drawString("Tarneaadress: ",(int)x, (int)y);
		           g.drawString(nimi,(int)x + 70, (int)y);
		               y += fm.getHeight() ;
		       }
		       // Trykime Kliendi aadressi
		       if (aadress != null) {
		           g.setFont(NORMAL_FONT);   
		           StringTokenizer st = new StringTokenizer(aadress,"|");
		           while(st.hasMoreTokens()){
		        	       g.drawString(st.nextToken(),(int)x + 70, (int)y);
			               y += fm.getHeight() ; 
		           }
		         
		       }
		
		       
		       if (markused != null) {
		           g.setFont(BOLD_FONT);         
		               g.drawString("M�rkused:",(int)x, (int)y);
		           
		       }
		       if (tarnepaev != null) {
		           g.setFont(BOLD_FONT);         
		               g.drawString("Tarneaeg:   ",(int)x + 220, (int)y);
		               g.setFont(COURIER_FONT);    
		               g.drawString( tarnepaev,(int)x + 270, (int)y);
		               y += fm.getHeight();
		       }
		       if (markused != null) {
		           g.setFont(COURIER_FONT);         
		               g.drawString( markused,(int)x+10, (int)y);
		           
		       }
		       if (agendinimi != null) {
		           g.setFont(BOLD_FONT);         
		           g.drawString("M��ja:         ",(int)x + 220, (int)y);
		           g.setFont(COURIER_FONT);
		           g.drawString( agendinimi,(int)x + 280, (int)y);
		               y += 5;
		       }

		  
	    	  
	    	  
	      }
	    
	       if (page != 0 ){ //&& page <=pages
	    	   System.out.println("page " + page + " " + pages);
	    	   if(linesLeft >0){
	    		   g.setFont(SMALL_FONT);         
		           g.drawString("Tr�kitud:   " + getStamp(),(int)x, (int)y);
		           g.drawString("Lk: Page " + (page+1) + " of " + (pages),(int)x + 300, (int)y);
		           g.drawString("Tellimus: " + number,(int)x + 170, (int)y);
		           fm = g.getFontMetrics();
		           y += fm.getHeight()+5 ;   
	    	   }
	    	  
	           
	           
	      
	      try{
		    	 int start = 9 +((page-1)*15) +1;
		    	 System.out.println("j�rgi ridu  " + linesLeft + " lehel " + page + " start " + start);
		    	  for(int w = start; w < start+15  ;w++){
		    		  
		    		  if(linesLeft >= 0){
		    			System.out.println("w" + w + " " +linesLeft);
			    	  OrderLine ol = (OrderLine)lines.elementAt(w-1);
			    	  System.out.println("ol " + ol.getToode() + " " +linesLeft);
			    	  String yhik = ol.getUnit();
			    	  if(ol.getUnit().indexOf("|") != -1){
			    		  g.setFont(BOLD_FONT);    
			    		  g.drawString("Riiulikoht:     " + ol.getUnit().substring(ol.getUnit().indexOf("|")+1) ,(int)x , (int)y);
			    		  g.setFont(SMALL_FONT); 
			    		  g.drawString( ol.getEAN() ,(int)x + 300, (int)y);
			    		  y += fm.getHeight() + 2;	
			    		  yhik = ol.getUnit().substring(0,ol.getUnit().indexOf("|"));
			    	  }else{
			    		  g.setFont(SMALL_FONT); 
			    		  g.drawString( ol.getEAN() ,(int)x + 300, (int)y);
			    		  y += fm.getHeight() + 2;
			    	  }
			    	  
			    	  g.setFont(NORMAL_FONT); 
			    	  String nimi1 = null;
			    	  String nimi2 = "";
			    	  String parimenne = "";
			    	  String parim = "";
			    	  StringTokenizer st = new StringTokenizer(ol.getTooteNimi(),"|");
			    	  while(st.hasMoreTokens()){
			        	   nimi1 = st.nextToken();
			        	   if(st.hasMoreTokens()){
			        		   nimi2 = st.nextToken();
			        	   }
			        	   if(st.hasMoreTokens()){
			        		   parimenne = st.nextToken();
			        		   if(parimenne.trim().startsWith("9999")){
			        			   parimenne = "";
			        		   }
			        	   }
			        	   if(st.hasMoreTokens()){
			        		   parim = st.nextToken();
			        		   if(parim.trim().startsWith("1900")){
			        			   parim = "";
			        		   }
			        	   }
			           }
			    	   g.drawString(ol.getToode() ,(int)x, (int)y);
			    	   g.drawString(nimi1 ,(int)x + 120, (int)y);
			    	   g.drawString(ol.getPartii()  ,(int)x + 255, (int)y);
			    	   g.setFont(BOLD_FONT); 
			    	   g.drawString((int)ol.getKogus()+ "" ,(int)x + 370, (int)y);
			    	   g.setFont(NORMAL_FONT); 
			    	   g.drawString( yhik  ,(int)x + 395, (int)y);
			    	   g.drawString( "......" ,(int)x + 420, (int)y);
			    	   y += fm.getHeight();
			    	   g.drawString(nimi2 ,(int)x + 120, (int)y);
			    	   g.drawString(parimenne ,(int)x + 313, (int)y);
			    	   g.setFont(SMALL_FONT); 
			    	   g.setColor(Color.red);
			    	   g.drawString(parim ,(int)x + 395, (int)y);
			    	   y += fm.getHeight()+ add + 3;
			    	   g.setFont(NORMAL_FONT); 
			    	   g.setColor(Color.black);
			    	    
		      }
		    		  
	    		  }
		      
	      }catch (Exception ss){
	    	//  ss.printStackTrace();
	      }
	    		  
	 
	           
	      }
	    
	       
	      /* tell the caller that this page is part of the printed document */
	      return PAGE_EXISTS;
	    }
		/**
		 * @return the text
		 */
		public String getFirma() {
			return firma;
		}
		/**
		 * @param text the text to set
		 */
		public void setFirma(String text) {
			this.firma = text;
		}
		/**
		 * @return the nimi
		 */
		public String getNimi() {
			return nimi;
		}
		/**
		 * @param nimi the nimi to set
		 */
		public void setNimi(String nimi) {
			this.nimi = nimi;
		}
		/**
		 * @return the aadress
		 */
		public String getAadress() {
			return aadress;
		}
		/**
		 * @param aadress the aadress to set
		 */
		public void setAadress(String aadress) {
			this.aadress = aadress;
		}
		/**
		 * @return the ring
		 */
		public String getRing() {
			return ring;
		}
		/**
		 * @param ring the ring to set
		 */
		public void setRing(String ring) {
			this.ring = ring;
		}
		/**
		 * @return the number
		 */
		public String getNumber() {
			return number;
		}
		/**
		 * @param number the number to set
		 */
		public void setNumber(String number) {
			this.number = number;
		}
		/**
		 * @return the cases
		 */
		public String getCases() {
			return cases;
		}
		/**
		 * @param cases the cases to set
		 */
		public void setCases(String cases) {
			this.cases = cases;
		}
		
		private NumberFormat nf = NumberFormat.getInstance();
		
		public static final Font SMALL_FONT = new Font("Arial",Font.ITALIC, 7);
		public static final Font MEDIUM_FONT = new Font("Arial", 1, 10);
		public static final Font COURIER_FONT = new Font("Arial", 0, 10);
		public static final Font COURIER_SMALL_FONT = new Font("Courier", 0, 10);
		public static final Font NORMAL_FONT = new Font("Arial", 0, 10);
		public static final Font BOLD_FONT = new Font("Arial", 1, 10);
		public static final Font BIG_FONT = new Font("Courier", 1, 14);
		public static final Font BIG16_FONT = new Font("Arial", 1, 16);
		public static final Font BIG20_FONT = new Font("Arial", 1, 20);
		public static final Font BIG14_FONT = new Font("Arial", 1, 14);
		public static final Font ULTRA_BIG_FONT = new Font("Arial", 1, 30);
	  
}
