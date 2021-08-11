package eevaeksport;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class ChangeEmail extends JFrame implements ActionListener{

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
public ChangeEmail(String email, String email2, EksportPanel parent) {
    this.parent = parent;
    //if (email.length() < 10) email = email + "                                 ";
    JPanel mainPanel = new JPanel(new BorderLayout());
    JPanel centerPanel = new JPanel();
    JPanel northPanel = new JPanel();
    JPanel southPanel = new JPanel();
    getContentPane().add(mainPanel);
    northPanel.add(new JLabel(getString("fileemail")), BorderLayout.CENTER);
    northPanel.add(text);
    centerPanel.add(new JLabel(getString("noteemail")), BorderLayout.CENTER);
    centerPanel.add(msg);
    mainPanel.add(northPanel, BorderLayout.NORTH);
    mainPanel.add(centerPanel, BorderLayout.CENTER);
    text.setText(email);
    msg.setText(email2);
    ok = new JButton(getString("ok"));
    cancel = new JButton(getString("back"));
    southPanel.add(ok);
    southPanel.add(cancel);
    mainPanel.add(southPanel, BorderLayout.SOUTH);
    ok.addActionListener(this);
    ok.setMnemonic('o');
    cancel.addActionListener(this);
    cancel.setMnemonic('t');
  }

  public void actionPerformed(ActionEvent e){
    Object src = e.getSource();
    if (src == ok){
      String email = text.getText().trim();
      String email2 = msg.getText().trim();
      if ((email.indexOf(EksportPanel.EMAILS_SEPARATOR) != -1) || (email2.indexOf(EksportPanel.EMAILS_SEPARATOR) != -1)){
      } else {
        parent.setEmailForCurrentAgent(email, email2);
        dispose();
        }
    }

    if (src == cancel){
      dispose();
    }
  }

  private String getString(String s){
    return parent.getParentFrame().getString(s);
  }

  private JTextField text = new JTextField(15);
  private JTextField msg = new JTextField(15);
  private JButton ok = null;
  private JButton cancel = null;
  private EksportPanel parent;
}