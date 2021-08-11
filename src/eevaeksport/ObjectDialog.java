/*
 * Created on 13.11.2007
 */
package eevaeksport;


import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Dialog;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;


public class ObjectDialog extends Dialog {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
public ObjectDialog(EksportFrame parent) {
    super(parent, true);
    Panel panel = new Panel(new BorderLayout());
    add(panel);

    Button okBtn = new Button(parent.getString("ok"));
    Panel btnPanel = new Panel();
    btnPanel.add(okBtn);
    panel.add(btnPanel, BorderLayout.SOUTH);

    choice = new Choice();
    choice.add("");
    indexPointer.addElement("");

    panel.add(choice, BorderLayout.NORTH);

    choice.addItemListener(new ItemListener(){
         public void itemStateChanged(ItemEvent e) {
           int index = choice.getSelectedIndex();
           if (index != 0){
      
         }else{
         	dispose();			
         }
         
       }});
    okBtn.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e){
           int index = choice.getSelectedIndex();
           if (index != 0){
        
          setVisible(false);
         }else{
         	dispose();			
         }
         
       }}
      );

  }
  public  Choice choice = null;
  private Vector indexPointer = new Vector();
  private boolean loading = false;
/**
 * @return Returns the loading.
 */
public boolean isLoading() {
	return loading;
}
/**
 * @param loading The loading to set.
 */
public void setLoading(boolean loading) {
	this.loading = loading;
}
/**
 * @return Returns the choice.
 */
public Choice getChoice() {
	return choice;
}
/**
 * @param choice The choice to set.
 */
public void setChoice(Choice choice) {
	this.choice = choice;
}
/**
 * @return Returns the indexPointer.
 */
public Vector getIndexPointer() {
	return indexPointer;
}
/**
 * @param indexPointer The indexPointer to set.
 */
public void setIndexPointer(Vector indexPointer) {
	this.indexPointer = indexPointer;
}
}
