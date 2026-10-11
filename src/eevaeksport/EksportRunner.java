package eevaeksport;

import javax.swing.JFrame;
import javax.swing.JLabel;
/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class EksportRunner {

  public EksportRunner() {
  }

  public static void main(String[] args){

    JFrame status = new JFrame();
    status.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JLabel statusLabel = new JLabel("Loen andmeid...");
    status.getContentPane().add(statusLabel);
    status.pack();
  
    EksportFrame frame = new EksportFrame(args);
  
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(900,600);
    status.dispose();
    frame.setVisible(true);

  }
}