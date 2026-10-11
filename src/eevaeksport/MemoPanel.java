package eevaeksport;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class MemoPanel extends JPanel implements ActionListener{

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
public MemoPanel(EksportPanel parent) {
    super();
    this.parent = parent;
    setLayout(new BorderLayout());
    saveBtn = new JButton(parent.getString("savebutton"));
    JPanel centerPanel = new JPanel();
    JScrollPane sp = new JScrollPane(text, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    centerPanel.add(sp);
    add(centerPanel);
    JPanel btnPanel = new JPanel();
    btnPanel.add(saveBtn);
    add(btnPanel, BorderLayout.SOUTH);
    saveBtn.addActionListener(this);
  }

  public void actionPerformed(ActionEvent e){
    Object src = e.getSource();
    if (src == saveBtn){
            String laks = "promo";
            if (parent.isPersonal){
            laks = parent.getSelectedAgentCode();
            }
            if(parent.isStartText){
            	laks = "starttext";
            }
      parent.saveMemoPanel(laks);

  }}

  public String getMemoText(){
    return text.getText();
  }

  public void setMemoText(String text){
    this.text.setText(text);
  }

  private JTextArea text = new JTextArea(4, 30);
  private JButton saveBtn = null;
  private EksportPanel parent = null;
}
