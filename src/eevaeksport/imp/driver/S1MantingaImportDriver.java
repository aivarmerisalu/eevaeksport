package eevaeksport.imp.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

/**
 * Title: Eeva eksport Description: Copyright: Copyright (c) 2002 Company:
 * 
 * @author Tarmo Tubro
 * @version 1.0
 */

public class S1MantingaImportDriver extends AbstractImportDriver {

	static Category log = Category.getInstance(IceAxaImportDriver.class.getName());
	boolean testing = false;

	public S1MantingaImportDriver(ImportPanel parent) {
		this.parent = parent;
		PropertyConfigurator.configure("log4j.properties");
		if ("true".equals(parent.getProps().getProperty("testing"))) {
			testing = true;
		}
		 loadSignatures();
	}

	private TreeMap names = null;
	private TreeMap custwares = null;

	
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


	
	
	private Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(url);
	}

	private TreeMap custNames() {
		TreeMap res = new TreeMap();
		String sql = "  SELECT k.klientas  AS nimi,k.klient_id AS kood , sandelis as ware " + "  FROM klientai k  ";

		if (custwares == null) {
			custwares = new TreeMap();
		}
		System.out.println(sql);
		try {
			Connection con = getNewConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String name = rs.getString("nimi").trim();
				String id = rs.getString("kood").trim();
				String ware = rs.getString("ware");
				res.put(id, name);

				if (ware != null && ware.trim().length() > 0) {
					custwares.put(id, ware.trim());
				}

			}
			rs.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("customerNames Size " + res.size());

		return res;
	}

	public String getImportFile() {
		return parent.getProps().getProperty("imp.read");
	}

	public String identifyCustomer(String id, String regnr) {

		String acc = null;

		try {
			String sql2 = " select k.[No_] AS klient " + " FROM [Viï¿½iunai Baltic$Customer] k  where k.[No_] = '"
					+ id.trim() + "'";
			System.out.println(sql2);

			Statement st2 = con.createStatement();
			ResultSet rs2 = st2.executeQuery(sql2);

			while (rs2.next()) {
				acc = rs2.getString("klient").trim();

			}
			rs2.close();
			st2.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (acc == null) {
			acc = id;
		}

		System.out.println("Klient: " + acc);
		return acc;

	}

	public String identifyProduct(String ean, String prodID) throws SQLException {

		String sql1 = "SELECT [No_] AS toode  FROM [Viï¿½iunai Baltic$Item] LEFT JOIN [Viï¿½iunai Baltic$Item Unit of Measure]"
				+ " vat ON (vat.[Item No_]=[No_] and vat.[Code]='Dï¿½ï¿½.' ) " + "  WHERE [Brï¿½kï¿½ninis kodas] ='"
				+ ean + "'  order by [Blocked] "; // and [Blocked]=0

		String sql2 = " SELECT [No_] AS toode FROM [Viï¿½iunai Baltic$Item] " + "  WHERE [No_] = '" + prodID + "' "; // and
																														// [Blocked]=0

		String prod = null;

		Statement st1 = con.createStatement();
		ResultSet rs1 = st1.executeQuery(sql1);

		System.out.println(sql1);
		if (rs1.next()) {
			if (ean.trim().length() > 0) {
				prod = rs1.getString("toode").trim();
			}
		}
		System.out.println("peale esimest " + prod);

		if (prod == null || prod.trim().length() == 0) {
			System.out.println(sql2);
			rs1 = st1.executeQuery(sql2);
			while (rs1.next()) {

				if (prodID != null && prodID.trim().length() > 0) {
					prod = rs1.getString("toode").trim();
				}
			}
			System.out.println("peale teist " + prod);
		}

		rs1.close();
		st1.close();

		System.out.println("Tagastan toote:" + prod + " prodID:" + prodID + " ean:" + ean);
		return prod;

	}

	private String getProdNimi(String toode) {

		String sql = " SELECT pr.descr  as pavad FROM SC157 pr where pr.code =  '" + toode + "'";
		System.out.println(sql);
		String res = "<No Name>";
		try {
			Connection con = getNewConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next()) {
				res = rs.getString("pavad").trim();
			}

			rs.close();
			stmt.close();
			con.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
	

	private String getProdEan(String toode) {

		//String sql = " SELECT pr.descr  as pavad FROM SC157 pr where pr.code =  '" + toode + "'";
		  String sql = "  SELECT abi.sp96 AS EAN FROM SC157 pr  left join sc99 abi on rtrim(ltrim(abi.id))=rtrim(ltrim(pr.SP147)) left JOIN SC160 yh ON yh.id=abi.SP93 \r\n" + 
	        		"				 where  parentid in (select id from SC157 where ltrim(rtrim(parentid)) = '1' ) and pr.code =  '" + toode + "'";
		System.out.println(sql);
		String res = null;
		try {
			Connection con = getNewConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next()) {
				res = rs.getString("EAN").trim();
			}

			rs.close();
			stmt.close();
			con.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public void makeOrder(Order order) throws SQLException {
		if (signatures == null){
			   signatures = new Vector();
		}
		
		
		  String signature = order.getSignature();
		  if(signatures.contains(signature)){
			  this.trouble = "REPREATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
			  System.out.println("Korduv tellimus ");  		  
		  }else{
			  makeOutput(order); 
			  signatures.add(signature);
			  this.trouble = null;
		  }
	 
	   try {
			saveObject(signatures, "signatures.vec");
		} catch (IOException e) {
			e.printStackTrace();
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
	

	public static String escapeXML(String s) {
		if (s == null)
			return s;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '&') {
				sb.append("&amp;");
			} else if (c == '<') {
				sb.append("&lt;");
			} else if (c == '>') {
				sb.append("&gt;");
			} else if (c == '\n') {
				sb.append(" ");
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	private void makeOutput(Order order) {

		if (names == null) {
			// names = this.custNames();
		}

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();
			Element root = doc.createElement("Document-Order");
			doc.appendChild(root);

			// String filiaalStr = null;
			// String tarnijaKoodStr = null;

			Node docheader = doc.createElement("Document-Header");
			root.appendChild(docheader);

			Node un2 = doc.createElement("DocumentType");
			Text unVal2 = doc.createTextNode("MeriSoft");
			un2.appendChild(unVal2);
			docheader.appendChild(un2);

			Node un3 = doc.createElement("DocumentSource");
			Text unVal3 = doc.createTextNode("S");
			un3.appendChild(unVal3);
			docheader.appendChild(un3);
			try {

				long ll = Long.parseLong(order.getAgentCode());
				Node un4 = doc.createElement("DateCreated");

				Calendar cal = Calendar.getInstance();
				cal.setTime(new java.util.Date(ll));

				String month = (cal.get(Calendar.MONTH) + 1) + "";
				if (month.length() == 1)
					month = "0" + month;
				String day = cal.get(Calendar.DAY_OF_MONTH) + "";
				if (day.length() == 1)
					day = "0" + day;

				String hour = cal.get(Calendar.HOUR_OF_DAY) + "";
				if (hour.length() == 1)
					hour = "0" + hour;
				String minute = cal.get(Calendar.MINUTE) + "";
				if (minute.length() == 1)
					minute = "0" + minute;

				String second = cal.get(Calendar.SECOND) + "";
				if (second.length() == 1)
					second = "0" + second;

				// [ HH:MM:SS]

				String dateString = cal.get(Calendar.YEAR) + "-" + month + "-" + day + "[" + hour + ":" + minute + ":"
						+ second + "]";

				Text unVal4 = doc.createTextNode(dateString);
				un4.appendChild(unVal4);
				docheader.appendChild(un4);

			} catch (Exception e) {

			}

			String number = order.getDokNr();
			if (number == null || number.length() == 0) {
				number = (Calendar.getInstance().getTimeInMillis() + "").substring(5);
			}

			long uni = Calendar.getInstance().getTimeInMillis();
			Node header = doc.createElement("Order-Header");

			Node nr = doc.createElement("OrderNumber");
			Text nrVal = doc.createTextNode("MS-" + number + "");
			nr.appendChild(nrVal);
			header.appendChild(nr);

			Node age = doc.createElement("SalesAgent");
			Text ageVal = doc.createTextNode(order.getAgent());
			age.appendChild(ageVal);
			header.appendChild(age);

			String comm = "";
			if (order.getInfo() != null && order.getInfo().trim().length() > 0) {

				Node com = doc.createElement("Remarks");
				Text comVal = doc.createTextNode(order.getInfo());
				com.appendChild(comVal);
				header.appendChild(com);
				comm = order.getInfo();

				Node com2 = doc.createElement("comment");
				Text comVal2 = doc.createTextNode(order.getInfo());
				com2.appendChild(comVal2);
				header.appendChild(com2);

			}
			System.out.println("Remarks " + comm);

			Node kuupaev = doc.createElement("OrderDate");
			Calendar cal = Calendar.getInstance();

			String month = (cal.get(Calendar.MONTH) + 1) + "";
			if (month.length() == 1)
				month = "0" + month;
			String day = cal.get(Calendar.DAY_OF_MONTH) + "";
			if (day.length() == 1)
				day = "0" + day;
			String year = cal.get(Calendar.YEAR) + "";
			if (year.length() == 2) {
				year = "20" + year;
			}
			String dateString = year + "-" + month + "-" + day;
			Text kuupaevVal = doc.createTextNode(dateString);
			kuupaev.appendChild(kuupaevVal);
			header.appendChild(kuupaev);

			Node curr = doc.createElement("OrderCurrency");
			Text currVal = doc.createTextNode("EUR");
			curr.appendChild(currVal);
			header.appendChild(curr);

			Node delkuupaev = doc.createElement("ExpectedDeliveryDate");
			Calendar delcal = Calendar.getInstance();
			delcal.setTime(order.getShipCal().getTime());

			month = (delcal.get(Calendar.MONTH) + 1) + "";
			if (month.length() == 1)
				month = "0" + month;
			day = delcal.get(Calendar.DAY_OF_MONTH) + "";
			if (day.length() == 1)
				day = "0" + day;
			year = delcal.get(Calendar.YEAR) + "";
			if (year.length() == 2) {
				year = "20" + year;
			}
			dateString = year + "-" + month + "-" + day;
			Text delkuupaevVal = doc.createTextNode(dateString);
			delkuupaev.appendChild(delkuupaevVal);
			header.appendChild(delkuupaev);

			Node expectedDeliveryTime = doc.createElement("ExpectedDeliveryTime");
			Text expectedDeliveryTimeVal = doc.createTextNode("00:00");
			expectedDeliveryTime.appendChild(expectedDeliveryTimeVal);
			header.appendChild(expectedDeliveryTime);

			root.appendChild(header);

			Node parties = doc.createElement("Document-Parties");
			Node seller = doc.createElement("DeliveryPoint");

			Node ise = doc.createElement("CodeBySupplier");
			Text nimiVal = doc.createTextNode(order.getCompany());
			ise.appendChild(nimiVal);
			Node ise2 = doc.createElement("ILN");
			Text nimiVal2 = doc.createTextNode(order.getCompany());
			ise2.appendChild(nimiVal2);

			seller.appendChild(ise);
			seller.appendChild(ise2);

			parties.appendChild(seller);

			Node receiver = doc.createElement("Receiver");

			Node iseR = doc.createElement("CodeBySupplier");
			Text nimiRVal = doc.createTextNode(order.getCompany());
			iseR.appendChild(nimiRVal);
			Node ise2R = doc.createElement("ILN");
			Text nimiVal2R = doc.createTextNode(order.getCompany());
			ise2R.appendChild(nimiVal2R);

			receiver.appendChild(iseR);
			receiver.appendChild(ise2R);

			parties.appendChild(receiver);

			Node buyer = doc.createElement("Buyer");

			Node iseX2 = doc.createElement("ILN");
			Text nimiXVal2 = doc.createTextNode(order.getCompany());
			iseX2.appendChild(nimiXVal2);

			Node iseX = doc.createElement("CodeByBuyer");
			Text nimiXVal = doc.createTextNode(order.getCompany());
			iseX.appendChild(nimiXVal);

			buyer.appendChild(iseX2);
			buyer.appendChild(iseX);

			parties.appendChild(seller);

			root.appendChild(parties);

			// RIDADE LISAMINE
			Node read = doc.createElement("Order-Lines");

			int coun = 0;
			Iterator iter = order.getOrderLines().iterator();
			while (iter.hasNext()) {
				OrderLine line = (OrderLine) iter.next();
				String toode = line.getToode();
				if (toode != null) {
					toode = toode.trim();
				} else {
					toode = "";
				}
				if (line.getKogus() != 0 && !toode.equals("")) {

					coun++;
					addLine(line, read, doc, coun, order);
				}
			}

			root.appendChild(read);

			String fileName = parent.getProps().getProperty("conv.dir") + order.getDokNr().trim() + (uni + "").trim()
					+ ".xml";
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

	private void addLine(OrderLine line, Node read, Document doc, int counter, Order order) throws SQLException {

		Node rida = doc.createElement("Line");
		read.appendChild(rida);

		Node ridaitem = doc.createElement("Line-Item");

		Node linenum = doc.createElement("LineNumber");
		Text linnum = doc.createTextNode(counter + "");
		linenum.appendChild(linnum);
		ridaitem.appendChild(linenum);

		/*
		 * Node toote_ean = doc.createElement("EAN"); Text toote_eanVal =
		 * doc.createTextNode(line.getEAN());
		 * toote_ean.appendChild(toote_eanVal); ridaitem.appendChild(toote_ean);
		 */

		String toode = line.getToode();

		Node btoote_kood = doc.createElement("BuyerItemCode");
		Text btoote_koodVal = doc.createTextNode(toode);
		btoote_kood.appendChild(btoote_koodVal);
		ridaitem.appendChild(btoote_kood);

		Node toote_kood = doc.createElement("SupplierItemCode");
		Text toote_koodVal = doc.createTextNode(toode);
		toote_kood.appendChild(toote_koodVal);
		ridaitem.appendChild(toote_kood);
		String ean = this.getProdEan(toode);
		if(ean != null) {
			Node ean_kood = doc.createElement("EAN");
			Text ean_koodVal = doc.createTextNode(ean);
			ean_kood.appendChild(ean_koodVal);
			ridaitem.appendChild(ean_kood);		
		}
	

		String nimi = this.getProdNimi(toode);
		if (nimi != null) {
			Text nimetusVal = doc.createTextNode(nimi.replace('`', ' ').replace('´', ' '));
			Node nimetus = doc.createElement("ItemDescription");
			nimetus.appendChild(nimetusVal);
			ridaitem.appendChild(nimetus);
		}

		double kogus = line.getKogus();
		double price = line.getPrice();
		double disc = line.getDiscount();
		String unit = line.getUnit();

		Node koGus = doc.createElement("OrderedQuantity");

		Text kogusVal = doc.createTextNode(getNF().format(kogus));
		koGus.appendChild(kogusVal);
		ridaitem.appendChild(koGus);

		if (disc > 0) {
			// price = price * (100-disc)/100;
			Node yhiku_hind = doc.createElement("OrderedUnitNetPrice");
			Text yhiku_hindVal = doc.createTextNode(this.nf.format(price));
			yhiku_hind.appendChild(yhiku_hindVal);
			ridaitem.appendChild(yhiku_hind);
		}

		Node yhiku_va = doc.createElement("TaxRate");
		Text yhiku_vaVla = doc.createTextNode("21");
		yhiku_va.appendChild(yhiku_vaVla);
		ridaitem.appendChild(yhiku_va);

		Node yhik = doc.createElement("UnitOfMeasure");
		Text yhikVal = doc.createTextNode(unit);
		yhik.appendChild(yhikVal);
		ridaitem.appendChild(yhik);

		rida.appendChild(ridaitem);
		addNumLine();
	}

	public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}

	private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

	public String getDriverVersion() {
		return "AxaAmberLAT";
	}

	public boolean isAmount() {
		return isAmount;
	}

	/**
	 *
	 * @param aUseEAN
	 *            - the new value for useEAN
	 */
	public void setAmount(boolean aAmount) {
		isAmount = aAmount;
	}

	private String url = "jdbc:odbc:mantinga";
	private Connection con;

	private ImportPanel parent;
	private boolean isAmount = false;
	private StringBuffer troubles = new StringBuffer();

	

}
