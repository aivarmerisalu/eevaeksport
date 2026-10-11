package eevaeksport;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class ScheduleFrame extends JFrame implements ActionListener,
   TableCellRenderer, TableCellEditor {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
public ScheduleFrame() {
    setTitle("Automaatne k�ivitus");
    JPanel buttonPanel = new JPanel();
    //buttonPanel.add(addButton);
    buttonPanel.add(okButton);
    mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    initTable();
    initTaskList();
    //mainPanel.add(table, BorderLayout.CENTER);
    getContentPane().add(mainPanel);
    addButton.addActionListener(this);
    deleteButton.addActionListener(this);
    okButton.addActionListener(this);
    first.addActionListener(this);
    second.addActionListener(this);
    third.addActionListener(this);
    pack();
  }

  private void initTaskList(){
    firstTask = new Task();
    firstTask.set(Calendar.HOUR_OF_DAY, 10);
    firstTask.set(Calendar.MINUTE, 00);
    firstTask.setType(POHJADE_SAATMINE);
    //taskList.add(firstTask);
    secondTask = new Task();
    secondTask.set(Calendar.HOUR_OF_DAY, 15);
    secondTask.set(Calendar.MINUTE, 30);
    secondTask.setType(LENDURI_INFO);
    //taskList.add(secondTask);

    thirdTask = new Task();
    thirdTask.set(Calendar.HOUR_OF_DAY, 9);
    thirdTask.set(Calendar.MINUTE, 55);
    thirdTask.setType(POHJADE_SAATMINE);
    taskList.add(thirdTask);

  }

  private void initTable(){
    table.setDefaultRenderer(JComboBox.class, this);
    table.setDefaultRenderer(JCheckBox.class, this);
    table.setDefaultEditor(JCheckBox.class, this);
    table.setSize(100, 50);
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(table.getTableHeader(), BorderLayout.NORTH);
    panel.add(table, BorderLayout.CENTER);
    JPanel tableButtonPanel = new JPanel();
    addButton.setEnabled(false);
    deleteButton.setEnabled(false);
    tableButtonPanel.add(addButton);
    tableButtonPanel.add(deleteButton);
    addButton.setFont(smallFont);
    deleteButton.setFont(smallFont);
    panel.add(tableButtonPanel, BorderLayout.SOUTH);
    mainPanel.add(panel, BorderLayout.CENTER);

    Vector vec = new Vector();
    vec.add("10.00");
    vec.add("P�hjade saatmine");
    vec.add(first);
    model.addRow(vec);

    Vector second = new Vector();
    second.add("15.30");
    second.add("P�eva info lenduritele");
    second.add(this.second);
    model.addRow(second);

    Vector third = new Vector();
    third.add("9.55");
    third.add("P�hjade saatmine");
    third.add(this.third);
    model.addRow(third);

  }

  public boolean needToExecute(String type){
    boolean toExecute = false;
    GregorianCalendar cal = new GregorianCalendar();
    if ((cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) &&
      (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) ){
        Enumeration enums = taskList.elements();
        while(enums.hasMoreElements()){
          Task scheduleElement = (Task)enums.nextElement();
          String taskType = scheduleElement.getType();
          if (type.equalsIgnoreCase(taskType)){
            if (cal.get(Calendar.HOUR_OF_DAY) == scheduleElement.get(Calendar.HOUR_OF_DAY)){
              //System.out.println(cal.get(Calendar.MINUTE) + " " + scheduleElement.get(Calendar.MINUTE));
              if (cal.get(Calendar.MINUTE) == scheduleElement.get(Calendar.MINUTE)){
                 if((cal.get(Calendar.HOUR_OF_DAY) != last.get(Calendar.HOUR_OF_DAY))
                  || (cal.get(Calendar.MINUTE) != last.get(Calendar.MINUTE)) ){
                  last = cal;
                  toExecute = true;
                }

              }
            }
          }
      }

    }
    return toExecute;
  }

  public void actionPerformed(ActionEvent e) {
    Object obj = e.getSource();
    if (obj == addButton){
      Vector vec = new Vector();
      //vec.add(new JComboBox());
      vec.add("8.10");
      vec.add("P�hjade saatmine");
      vec.add(new JCheckBox("", true));
      model.addRow(vec);
      pack();
    }

    if (obj == deleteButton){
      int index = table.getSelectedRow();
      if (index != -1){
        model.removeRow(index);
        pack();
      }
    }

    if (obj == okButton){
      this.setVisible(false);
    }

    if (obj == first){
      if (first.isSelected()){
        taskList.add(firstTask);
      } else {
        taskList.remove(firstTask);
      }
    }

    if (obj == second){
      if (second.isSelected()){
        taskList.add(secondTask);
      } else {
        taskList.remove(secondTask);
      }
    }

    if (obj == third){
      if (third.isSelected()){
        taskList.add(thirdTask);
      } else {
        taskList.remove(thirdTask);
      }
    }

  }

  public Component getTableCellRendererComponent(JTable table,
     Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component comp = null;
    if (value instanceof JComboBox){
      comp = (JComboBox)value;
    } else if (value instanceof JCheckBox){
      comp = (JCheckBox)value;
    }
    return comp;
  }

  public Component getTableCellEditorComponent(JTable table,
     Object value, boolean isSelected, int row, int column) {
    Component comp = null;
    if (value instanceof JCheckBox){
      comp = (JCheckBox)value;
    }
    return comp;
  }

  public void removeCellEditorListener(CellEditorListener l){
  }

  public void addCellEditorListener(CellEditorListener l){
  }

  public void cancelCellEditing(){
  }

  public boolean stopCellEditing(){
    return true;
  }

  public boolean shouldSelectCell(EventObject e){
    return false;
  }

  public boolean isCellEditable(EventObject e){
    return true;
  }

  public Object getCellEditorValue(){
    return new Boolean(first.isSelected());
  }

  JPanel mainPanel = new JPanel(new BorderLayout());
  JButton okButton = new JButton("Sulge");
  JButton addButton = new JButton("Lisa");
  JButton deleteButton = new JButton("Kustuta");
  String[] header = {"aeg", "toiming", "aktiivne"};
  ScheduleTableModel model = new ScheduleTableModel(header,0);
  JTable table = new JTable(model);
  public static final String SPACES = " ";
  private Vector taskList = new Vector();
  private GregorianCalendar last = new GregorianCalendar();
  public static final int TIME_INDEX = 0;
  public static final int ACTIVE_INDEX = 2;
  Font smallFont = new Font("Arial", Font.PLAIN, 10);
  private JCheckBox first = new JCheckBox("", false);
  private JCheckBox second = new JCheckBox("", false);
  private JCheckBox third = new JCheckBox("", true);
  private Task firstTask = null;
  private Task secondTask = null;
  private Task thirdTask = null;
  public static final String LENDURI_INFO = "lendur";
  public static final String POHJADE_SAATMINE = "pohjad";
}