/*
 * Created on 04.09.2008
 */
package eevaeksport;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TreeMap;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;
/** * @version 1.0 11/09/98 */
public class ButtonEditor extends DefaultCellEditor {  
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected JButton button; 
	private String    label;
	private MIListDialog dia = null;
	private EksportFrame parent = null;
	private MarketInfoPanel mi = null;
	
	public ButtonEditor(JCheckBox checkBox,EksportFrame parent,TreeMap custs,TreeMap agents,final MarketInfoPanel mi) {
		super(checkBox);  
		button = new JButton("customers");
		button.setOpaque(true);
		this.mi = mi;
		this.parent = parent;
		this.custs = custs;
		this.agents = agents;
		kyssa = ""; 
		
		
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				JTable tt = mi.getRealTAble(); 
				
				TreeMap tr = new TreeMap();
				try{
					tr = (TreeMap)tt.getValueAt(tt.getSelectedRow(), 4);
				}catch (Exception ee){
					
				}
				dia = new MIListDialog(getParent(),getCusts(),getAgents(),(String)tt.getValueAt(tt.getSelectedRow(), 
						0),tr);
				dia.setSize(1000, 500);
				dia.setVisible(true);
				
				setCustSelected(dia.getSelection());
			
				getMi().saveState();
				fireEditingStopped();
				}    });
		} 
	
	public String getKyssa() {
		return kyssa;
	}

	
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		if (isSelected) {
			button.setForeground(table.getSelectionForeground());
			button.setBackground(table.getSelectionBackground());
			} else{
			button.setForeground(table.getForeground());
			button.setBackground(table.getBackground());
			} 
		    label = (value == null) ? "" : value.toString();
		  //  System.out.println("value " + value);
		    
			button.setText( label );
			
			return button;  }  
	
	public Object getCellEditorValue() {

			return this.getCustSelected() ;
			
			}
		
		
		public boolean stopCellEditing() { 
		  
				return super.stopCellEditing() ;
				}
		
		protected void fireEditingStopped() {
	       super.fireEditingStopped();
	  }
		

		  TreeMap custs = new TreeMap();
		  TreeMap agents = new TreeMap();
		  String kyssa = null;
		  TreeMap custSelected = new TreeMap();

		/**
		 * @return the custs
		 */
		public TreeMap getCusts() {
			return custs;
		}

		/**
		 * @param custs the custs to set
		 */
		public void setCusts(TreeMap custs) {
			this.custs = custs;
		}

		/**
		 * @return the agents
		 */
		public TreeMap getAgents() {
			return agents;
		}

		/**
		 * @param agents the agents to set
		 */
		public void setAgents(TreeMap agents) {
			this.agents = agents;
		}

		/**
		 * @return the parent
		 */
		public EksportFrame getParent() {
			return parent;
		}

		/**
		 * @param parent the parent to set
		 */
		public void setParent(EksportFrame parent) {
			this.parent = parent;
		}

		/**
		 * @return the custSelected
		 */
		public TreeMap getCustSelected() {
			return custSelected;
		}

		/**
		 * @param custSelected the custSelected to set
		 */
		public void setCustSelected(TreeMap custSelected) {
			this.custSelected = custSelected;
		}

		/**
		 * @return the kyssa
		 */
	

		/**
		 * @param kyssa the kyssa to set
		 */
		public void setKyssa(String kyssa) {
			this.kyssa = kyssa;
		}

		/**
		 * @return the mi
		 */
		public MarketInfoPanel getMi() {
			return mi;
		}

		/**
		 * @param mi the mi to set
		 */
		public void setMi(MarketInfoPanel mi) {
			this.mi = mi;
		}
		
		}

