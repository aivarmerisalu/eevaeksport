package eevaeksport;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class DistributionInfoPanel extends JPanel {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
public static final int ROWS = 20;
  public static final int COLUMN_TYPECOMBO = 1;
  
  public static final String FILE_NAME = "marketinfo.obj";
  private EksportFrame parent = null;
  private JTable table = null;

  public DistributionInfoPanel(final EksportFrame parent){
    this.parent = parent;
    setLayout(new BorderLayout());
    JPanel centerPanel = new JPanel();
    table = getTable();
    JScrollPane sp = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                     JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    centerPanel.add(sp);

    add(centerPanel, BorderLayout.CENTER);

    JPanel sPanel = new JPanel();
    JButton saveBtn = new JButton(parent.getString("savebutton"));
    saveBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        try {
          saveState(getData(table));
        } catch (IOException ex){
          EksportFrame.showError(ex);
        }
      }
    });
    sPanel.add(saveBtn);
    add(sPanel, BorderLayout.SOUTH);

  }

  protected Vector getData(){
    return getData(table);
  }

  private Vector getData(JTable table){
    String type1 = parent.getString("marketinfo.type1");
    String type2 = parent.getString("marketinfo.type2");
    String type3 = parent.getString("marketinfo.type3");
    String type4 = parent.getString("marketinfo.type4");
    Vector vec = new Vector();
    for (int i = 0; i < table.getModel().getRowCount() ; i++){
      Vector row = new Vector();
      for (int j = 0; j < table.getModel().getColumnCount() ; j++){
        String val = null;
        try {
          val = (String)table.getModel().getValueAt(i, j);
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
        data.addElement(row);
      }

      table = new JTable(data, header);

      for (int i = 0 ; i < table.getRowCount() ; i++){
        String value = (String)table.getValueAt(i, COLUMN_TYPECOMBO);
        if (!"".equals(value) && (value != null)){
          table.setValueAt(parent.getString("marketinfo." + value), i, COLUMN_TYPECOMBO);
        }
      }

      JComboBox types = new JComboBox();
      types.addItem("");
      types.addItem(parent.getString("marketinfo.type1"));
      types.addItem(parent.getString("marketinfo.type2"));
      types.addItem(parent.getString("marketinfo.type3"));
      types.addItem(parent.getString("marketinfo.type4"));

      TableCellEditor comboEditor = new DefaultCellEditor(types);
      table.getColumnModel().getColumn(1).setCellEditor(comboEditor);


    return table;
  }

  private void saveState(Vector table) throws IOException{
    Vector newTable = new Vector();
    for (int i = 0 ; i < table.size() ; i++){
      Vector row = (Vector)table.elementAt(i);
      boolean empty = true;
      for (int j = 0 ; j < row.size() ; j++){
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

}
