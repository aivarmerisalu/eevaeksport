package eevaeksport;

import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * <p>Title: EevaEksport</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Tarmo Tubro
 * @version 1.0
 */

public class ScheduleTableModel extends DefaultTableModel {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public ScheduleTableModel(Object[] header, int numRows){
    super(header, numRows);
  }

  public Class getColumnClass (int columnIndex){
    Class cls = null;
    if (columnIndex == ScheduleFrame.ACTIVE_INDEX){
      cls = JCheckBox.class;
      //cls = Boolean.class;
    } else {
      cls = JTextField.class;
    }
  return cls;
  }

  public boolean isCellEditable(int r, int c){
    return c == ScheduleFrame.ACTIVE_INDEX;
  }

}