/*
 * Created on 8.08.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package eevaeksport;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;


public class MIListDialog extends JDialog implements ItemListener, ActionListener, KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MIListDialog(EksportFrame parent,TreeMap custs,TreeMap agents,String kyssa,TreeMap olemas){
		super(parent, true);
		this.custs = custs;
		this.agents = agents;
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				  selection  = new TreeMap();
					 Vector vec = new Vector();
					 for(int a = 0 ; selAgentList.getModel().getSize()>a;a++){
							vec.addElement(selAgentList.getModel().getElementAt(a));
						} 
					 
					 selection.put("customer", vec);
					 setVisible(false);
			}
		});
		JPanel mainPanel = new JPanel(new BorderLayout());
		getContentPane().add(mainPanel);
	
		
		this.setTitle(kyssa);
		
		JPanel sPanel = new JPanel();
		 okBtn = new JButton("OK");
		 canBtn = new JButton("Cancel");
		
		okBtn.addActionListener(this);
		canBtn.addActionListener(this);
		
		JPanel selBtnPanel = new JPanel(new GridLayout(2,1,1,1));
		JPanel aa = new JPanel();
		addBtn = new JButton("->");
		addBtn.addActionListener(this);
		aa.add(addBtn);
		JPanel bb = new JPanel();
		 remBtn = new JButton("<-");
		 remBtn.addActionListener(this);
		bb.add(remBtn);
		selBtnPanel.add(aa);
		selBtnPanel.add(bb);
		
		DefaultListModel model = new DefaultListModel();
		Vector vec = new Vector();
	
		for (Iterator iter = custs.keySet().iterator(); iter.hasNext();){
			String key = (String)iter.next();
			Customer cust = (Customer)custs.get(key);
			model.addElement(cust.getID() + " || " + cust.getName());
			vec.addElement(cust.getID() + " || " + cust.getName());
		}
		

     
         selClientList = new JList();
         selClientList.setDragEnabled(true);
         selClientList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

 		selClientList.setListData(vec);

         List wholeList = new ArrayList();
         for (int i = 0 ; i < selClientList.getModel().getSize(); i++){
           wholeList.add(selClientList.getModel().getElementAt(i));
         }
         
         JScrollPane	 scrollPane = new JScrollPane(selClientList);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		DefaultListModel model2 = new DefaultListModel();
		Vector vv = new Vector();
		if(olemas != null){
			 vv = (Vector)olemas.get("customer");
			// System.out.println(vv + "" + olemas);
	if(vv != null){
		 for(int ii = 0 ;vv.size()>ii;ii++){
			 model2.addElement(vv.elementAt(ii));		 
		  }	
	     }
		}
         selAgentList= new JList(model2);
         selAgentList.setPreferredSize(new Dimension(400,200));
         selAgentList.setDragEnabled(true);
         selAgentList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         
         
		sPanel.add(okBtn);
		//sPanel.add(canBtn);
		mainPanel.add(sPanel, BorderLayout.SOUTH);
	  
       JPanel dPanel = new JPanel(new BorderLayout());
       

		dPanel.add(scrollPane,BorderLayout.WEST);
		dPanel.add(selBtnPanel,BorderLayout.CENTER);
		dPanel.add(selAgentList,BorderLayout.EAST);

		//dPanel.add(new JButton("hhh"),BorderLayout.CENTER);
		mainPanel.add(dPanel,BorderLayout.CENTER);
		
		
		
		//JComboBox list = new JComboBox(vec);

		Vector vec2 = new Vector();
		vec2.add("Select Agent");
		for (Iterator iter = agents.keySet().iterator(); iter.hasNext();){
			String key = (String)iter.next();
			Agent ag = (Agent)agents.get(key);
			vec2.add(ag.getName());
		}
		
		agCH = new JComboBox(vec2);
		
		agCH.addItemListener(this);
		JPanel cPanel = new JPanel();
		mainPanel.add(cPanel,BorderLayout.NORTH);
		cPanel.add(new JLabel("filter:"));
		cPanel.add(textF);
		cPanel.add(agCH);
		textF.addKeyListener(this);
		
	}
	
	private JList selClientList = new JList();

	private JList selAgentList = new JList();
	private JTextField textF = new JTextField(10);
	private JComboBox agCH = new JComboBox();
	private TreeMap selection = new TreeMap();
	private TreeMap custs = new TreeMap();
	private TreeMap agents = new TreeMap();
	private JButton okBtn = null;
	private JButton canBtn = null;
	private JButton remBtn = null;
	private JButton addBtn = null;
	
	/**
	 * @return the selection
	 */
	public TreeMap getSelection() {
		return selection;
	}
	/**
	 * @param selection the selection to set
	 */
	public void setSelection(TreeMap selection) {
		this.selection = selection;
	}
	public void itemStateChanged(ItemEvent e) {
		String code = null;
		DefaultListModel model = new DefaultListModel();
		
		if(agCH.getSelectedIndex() != 0){
		
			
		Agent aa = (Agent)agents.get(agCH.getSelectedItem());
		code = aa.getID();
		selClientList.removeAll();
		for (Iterator iter = this.custs.keySet().iterator(); iter.hasNext();){
			String key = (String)iter.next();
		//	System.out.println(key);
			Customer cust = (Customer)custs.get(key);
			if(cust.hasAgent(code)){
				model.addElement(cust.getID() + " || " +cust.getName());
			}
				
		}
		
		}else{
			selClientList.removeAll();
			for (Iterator iter = this.custs.keySet().iterator(); iter.hasNext();){
				String key = (String)iter.next();
				Customer cust = (Customer)custs.get(key);
					model.addElement(cust.getID() + " || " +cust.getName());
			}
		}
		
		selClientList.setModel(model);
		
	}
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(this.okBtn)){
			  selection  = new TreeMap();
			 Vector vec = new Vector();
			 for(int a = 0 ; selAgentList.getModel().getSize()>a;a++){
					vec.addElement(selAgentList.getModel().getElementAt(a));
				} 
			 
			 selection.put("customer", vec);
			 this.setVisible(false);
		}
      
		if(e.getSource().equals(this.canBtn)){
	       setVisible(false);
		}
		
		if(e.getSource().equals(this.remBtn)){
			DefaultListModel model = new DefaultListModel();
			for(int a = 0 ; selAgentList.getModel().getSize()>a;a++){
				model.addElement(selAgentList.getModel().getElementAt(a));
			}
			Object[] key = selAgentList.getSelectedValues();
			for (int i = 0; key.length>i;i++){	
				model.removeElement((String)key [i]);
			}
		   this.selAgentList.setModel(model);
		}
		
		
		if(e.getSource().equals(this.addBtn)){
			DefaultListModel model = new DefaultListModel();
			for(int a = 0 ; selAgentList.getModel().getSize()>a;a++){
				model.addElement(selAgentList.getModel().getElementAt(a));
			}
			
			
			Object[] key = selClientList.getSelectedValues();
			for (int i = 0; key.length>i;i++){	
				Enumeration en = model.elements();
				boolean xxx = true;
				while (en.hasMoreElements()){
					String aa = (String )en.nextElement();
					if(aa.equals((String)key [i])) xxx = false;
				}
				if(xxx)
				model.addElement((String)key [i]);
				//System.out.println("ccc " + (String)key [i]);
			}
			//System.out.println("ccc " + (String)key [0]);
			//selAgentList.removeAll();
		   this.selAgentList.setModel(model);
		   this.selClientList.clearSelection();
		}
		
	}
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	public void keyReleased(KeyEvent e) {
		if(textF.getText().length() >1){
			   filterList();
			}
				
			if(textF.getText().length() == 0){
				   resetList();
				}
		
	}
	
	private void filterList(){
		DefaultListModel model = new DefaultListModel();

	for (int i = 0;	selClientList.getModel().getSize()>i;i++){
		String item = (String)selClientList.getModel().getElementAt(i);
		//System.out.println(item);
		if(item.toLowerCase().indexOf(textF.getText().toLowerCase()) != -1){
			model.addElement(item);
		}
	}
	selClientList.removeAll();
	selClientList.setModel(model);
	
		
	}
	
	
	
	
	private void resetList(){
		DefaultListModel model = new DefaultListModel();
	
		for (Iterator iter = custs.keySet().iterator(); iter.hasNext();){
			String key = (String)iter.next();
			Customer cust = (Customer)custs.get(key);
			model.addElement(cust.getID() + " || " +cust.getName());
		}
		this.agCH.setSelectedIndex(0);
	    selClientList.removeAll();
	    selClientList.setModel(model);	
	}
	
	public void keyTyped(KeyEvent e) {
	
	}
	
	
}
