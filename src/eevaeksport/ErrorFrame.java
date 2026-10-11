package eevaeksport;

import java.awt.Frame;
import java.awt.Label;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class ErrorFrame extends Frame implements WindowListener{

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public ErrorFrame(String msg) {
    text.setText(msg);
    add(text);
    //setTitle("Error");
    addWindowListener(this);
  }

  public void windowDeactivated(WindowEvent e){
  }

  public void windowActivated(WindowEvent e){
  }

  public void windowDeiconified(WindowEvent e){
  }

  public void windowIconified(WindowEvent e){
  }

  public void windowClosed(WindowEvent e){
  }

  public void windowClosing(WindowEvent e){
    dispose();
  }

  public void windowOpened(WindowEvent e){
  }

  private Label text = new Label("                                                 ");
}
