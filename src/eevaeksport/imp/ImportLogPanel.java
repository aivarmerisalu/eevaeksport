package eevaeksport.imp;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class ImportLogPanel extends JPanel {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
public ImportLogPanel(ImportPanel parent) {
    this.parent = parent;
    textarea.setEditable(false);
    JLabel fileLabel = new JLabel(parent.getEksportFrame().getString("order3") + ":");
    JLabel infoLabel = new JLabel(parent.getEksportFrame().getString("info") + ":");
    renewFileArea();
    filePanel.add(fileLabel, BorderLayout.NORTH);
    filePanel.add(filesp, BorderLayout.CENTER);
    add(filePanel);
    JPanel infoPanel = new JPanel(new BorderLayout());
    infoPanel.add(infoLabel, BorderLayout.NORTH);
    sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    infoPanel.add(sp, BorderLayout.CENTER);
    add(infoPanel);
  }

  public void addText(String text){
    textarea.append(text);
  }

  public void addFile(String text){
    fileArea.append(text + "\n");
  }

  public void renewFileArea(){
    if (filesp != null){
      filePanel.remove(filesp);
    }
    fileArea = new JTextArea(8,10);
    //fileArea.setEditable(false);
    filesp = new JScrollPane(fileArea);
    filePanel.add(filesp);
    filePanel.doLayout();
  }

  JTextArea textarea = new JTextArea(8,25);
  JTextArea fileArea = null;
  JPanel filePanel = new JPanel(new BorderLayout());
  JScrollPane sp = new JScrollPane(textarea);
  JScrollPane filesp = null;
  ImportPanel parent = null;
}