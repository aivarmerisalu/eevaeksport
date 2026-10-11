package eevaeksport;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import eevaeksport.driver.AbstractDBDriver;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class MarketInfoPanel extends JPanel {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
public static final int ROWS = 20;
  public static final int COLUMN_TYPECOMBO = 1;
  
  public static final String FILE_NAME = "marketinfo.obj";
  private EksportFrame parent = null;
  private JTable table = null;
  
  public JTable getRealTAble(){
	  return table;
  }

  public MarketInfoPanel(final EksportFrame parent,AbstractDBDriver driver){
    this.parent = parent;
    setLayout(new BorderLayout());
    JPanel centerPanel = new JPanel(new BorderLayout());
 System.out.println("olen siin marketinfos ");
  if(driver != null){
  	custs = driver.getCustomers(null);
	agents = driver.getAgents();
  }
  System.out.println("olen siin marketinfos teine faas ");
	table = getTable();
	//table.setLayout(new BorderLayout());
	
    JScrollPane sp = new JScrollPane(table);
    centerPanel.add(sp,BorderLayout.CENTER);
  //  mainPanel.add(buttonPanel, BorderLayout.EAST);
  //  getContentPane().add(scrollPane, BorderLayout.CENTER);
   // getContentPane().add(mainPanel, BorderLayout.EAST); 

    add(centerPanel, BorderLayout.CENTER);
   //centerPanel.setSize(1000,500);
    JPanel sPanel = new JPanel();
    JButton saveBtn = new JButton(parent.getString("savebutton"));
    saveBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        try {
          saveState(getData(table));
        } catch (RuntimeException ex){
          EksportFrame.showError(ex);
        } catch (Exception ex){
          EksportFrame.showError(ex);
        }
      }
    });
    JButton closeBtn = new JButton(parent.getString("closebutton"));
    closeBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        try {
          saveState(getData(table));
          
        } catch (RuntimeException ex){
          EksportFrame.showError(ex);
        } catch (Exception ex){
          EksportFrame.showError(ex);
        }
        System.exit(0);
      }
    });
    sPanel.add(saveBtn);
    sPanel.add(closeBtn);
    add(sPanel, BorderLayout.SOUTH);

	
  }

  public void saveState(){
	  try{
		  saveState(getData(table));		    
	  }catch (IOException ex){
		  EksportFrame.showError(ex);		    
	  }
	  }
  protected Vector getData(){
    return getData(table);
  }

  private Vector getData(JTable table){
    String type1 = parent.getString("marketinfo.type1");
    String type2 = parent.getString("marketinfo.type2");
    String type3 = parent.getString("marketinfo.type3");
    String type4 = parent.getString("marketinfo.type4");
    String type5 = parent.getString("marketinfo.type5");
    String type6 = parent.getString("marketinfo.type6");
    Vector vec = new Vector();
    for (int i = 0; i < table.getModel().getRowCount() ; i++){
      Vector row = new Vector();
      for (int j = 0; j < table.getModel().getColumnCount() ; j++){
        Object val = null;
        try {
          Object o = table.getModel().getValueAt(i, j);
          if(o != null){
              if(o instanceof Map){
                  val = (Map)o;
              }else{
                  val = o.toString();  
              }  
          }else {
        	  val = "";
          }
          
         // System.out.println("0 =" + o);
        } catch (RuntimeException e){
          System.out.println("row=" + i + "; col=" + j);
          throw e;
        }
        if ((j == COLUMN_TYPECOMBO) && (val != null)){
          if (val.equals(type1) ){
            row.add("type1");
          } else if (val.equals(type2)){
            row.add("type2");
          } else if (val.equals(type3)){
            row.add("type3");
          } else if (val.equals(type4)){
            row.add("type4");
          } else if (val.equals(type5)){
            row.add("type5");
          } else if (val.equals(type6)){
            row.add("type6");
          }
        } else {
          row.add(val);
        }
      }
      vec.add(row);
    }
    return vec;
  }

  private JTable getTable(){
    JTable table = null;
    Vector header = new Vector();
    header.add(parent.getString("marketinfo.question"));
    header.add(parent.getString("marketinfo.answertype"));
    header.add(parent.getString("marketinfo.startdate"));
    header.add(parent.getString("marketinfo.enddate"));
    header.add(parent.getString("marketinfo.clients"));
    header.add(parent.getString("marketinfo.after"));
    Vector data = null;
    try {
      data = loadState();
    } catch (Exception e){
      data = new Vector();
    }
      for (int i = 0 ; i < ROWS ; i++){
        Vector row = new Vector();

        row.add("");
        row.add("");
        row.add("");
        row.add("");
        row.add("");
        row.add("");
        data.addElement(row);
      }

      table = new JTable(data, header);
    //  table.setColumnModel(columnModel);

      for (int i = 0 ; i < table.getRowCount() ; i++){
        String value = (String)table.getValueAt(i, COLUMN_TYPECOMBO);
        if (!"".equals(value) && (value != null)){
          table.setValueAt(parent.getString("marketinfo." + value), i, COLUMN_TYPECOMBO);        
        }
      }

      TableColumn column = null;
      for (int i = 0; i < 6; i++) {
          column = table.getColumnModel().getColumn(i);
          if (i == 0) {
              column.setPreferredWidth(150); //sport column is bigger
          } else {
              column.setPreferredWidth(100);
          }
      }    
      
      
      JComboBox types = new JComboBox();
      types.addItem("");
      types.addItem(parent.getString("marketinfo.type1"));
      types.addItem(parent.getString("marketinfo.type2"));
      types.addItem(parent.getString("marketinfo.type3"));
      types.addItem(parent.getString("marketinfo.type4"));
      types.addItem(parent.getString("marketinfo.type5"));
      types.addItem(parent.getString("marketinfo.type6"));
      
    final  JCheckBox cb = new JCheckBox();

      TableCellEditor comboEditor = new DefaultCellEditor(types);
      table.getColumnModel().getColumn(1).setCellEditor(comboEditor);

      TableCellEditor cbEditor = new DefaultCellEditor(cb);
      table.getColumnModel().getColumn(5).setCellEditor(cbEditor);
      
      table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
    	  /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table,
    	  Object value, boolean isSelected, boolean hasFocus, int row, int
    	  column) {
    		  if(value != null && value.toString().equals("true")){
    			  cb.setSelected(true);
    		  }else{
    			  cb.setSelected(false);
    		  }
    	  return cb;
    	  }
    	  });

      
      table.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
      ButtonEditor be = new ButtonEditor(new JCheckBox(),parent,custs,agents,this);
      
      table.getColumnModel().getColumn(4).setCellEditor(be);
       
    return table;
  }

  
  
  
  
  public void saveState(Vector table) throws IOException{
    Vector newTable = new Vector();
    for (int i = 0 ; i < table.size() ; i++){
      Vector row = (Vector)table.elementAt(i);
      boolean empty = true;
      for (int j = 0 ; j < row.size() ; j++){
    	 // System.out.println("elem " + j  + " " +row.elementAt(j));
        String val = (String)row.elementAt(j);
        if (val != null){
          if (!"".equals(val.trim())){
            empty = false;
            break;
          }
        }
      }
      if (!empty){
        newTable.add(row);
      }
    }
    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME));
    oos.writeObject(newTable);
    oos.close();
  }

  private Vector loadState() throws IOException, ClassNotFoundException {
    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME));
    Vector table = (Vector)ois.readObject();
    ois.close();
    return table;
  }

  TreeMap custs = new TreeMap();
  TreeMap agents = new TreeMap();
}
