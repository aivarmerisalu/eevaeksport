package eevaeksport;

import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ee.mobilejava.util.ListFilter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ClientList extends JDialog{

	

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
public ClientList(JFrame frame, EksportFrame parent, TreeMap custs){
    super(frame, true);
    this.custs = custs;
    this.parent = parent;
    init();
  }

  private void init(){
    setTitle(parent.getString("addpartners"));
    JPanel panel = new JPanel(new BorderLayout());
    JPanel buttonPanel = new JPanel();

    JButton okBtn = new JButton("OK");
    okBtn.setMnemonic('k');
    //final EDoccersSet edoc = parent;
    okBtn.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e){
            addCustomers();
            dispose();
          }
         }
        );
    buttonPanel.add(okBtn);

    JButton cancelBtn = new JButton(("back"));
    cancelBtn.setMnemonic('t');
    cancelBtn.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e){
            dispose();
          }
         }
        );
    buttonPanel.add(cancelBtn);

    panel.add(buttonPanel, BorderLayout.SOUTH);

    this.text = new JTextField();


      list = new JList(new MapListModel(custs));

    List wholeList = new ArrayList();
    for (int i = 0 ; i < list.getModel().getSize(); i++){
      wholeList.add(list.getModel().getElementAt(i));
    }
    listfilter = new ListFilter(wholeList);


    list.setToolTipText("<HTML>"+("clickok")+"<BR>" +
                        /* FIXME Utilities.getString("multihold")+ */ "</HTML>");
    list.addMouseListener(new MouseAdapter(){
                 public void mouseClicked(MouseEvent e){
                   testMouse(e);
                 }
                 public void mousePressed(MouseEvent e){
                   testMouse(e);
                 }
                 private void testMouse(MouseEvent e){
                   if (e.getClickCount() == 2){
                     addCustomers();
                     dispose();
                   }
                 }
                }
                );

    JScrollPane sp = new JScrollPane(list);
    JTabbedPane pane = new JTabbedPane();
    pane.addTab(("allpartners"), sp);
    panel.add(pane, BorderLayout.CENTER);

    pane.addTab(("find"), generateSearchPanel());
    final JTabbedPane fPane = pane;
    pane.addChangeListener(new ChangeListener(){
        public void stateChanged(ChangeEvent e){
          Object obj = e.getSource();
          if (obj == fPane){
            getText().requestFocus();
          }
        }
      }
      );
    getContentPane().add(panel);

    text.getDocument().addDocumentListener(new DocumentListener(){
          public void changedUpdate(DocumentEvent e){
            filterSearch();
          }
          public void removeUpdate(DocumentEvent e){
            filterSearch();
          }
          public void insertUpdate(DocumentEvent e){
            filterSearch();
          }
        }
        );
  }

  private JComponent generateSearchPanel(){
    JScrollPane sp = new JScrollPane(searchList);
    getSearchPanel().add(getText(), BorderLayout.NORTH);
    getSearchPanel().add(sp, BorderLayout.CENTER);

    searchList.setToolTipText("<HTML>"+("clickok")+"<BR>" +
                        /* FIXME Utilities.getString("multihold")+ */"</HTML>");
    searchList.addMouseListener(new MouseAdapter(){
                 public void mouseClicked(MouseEvent e){
                   testMouse(e);
                 }
                 public void mousePressed(MouseEvent e){
                   testMouse(e);
                 }
                 private void testMouse(MouseEvent e){
                   if (e.getClickCount() == 2){
                     addCustomers();
                     dispose();
                   }
                 }
                }
                );
    return getSearchPanel();
  }

  private void addCustomers(){
    addValues(list.getSelectedValues());
    addValues(searchList.getSelectedValues());
  }

  private void addValues(Object[] values){
    for (int i = 0 ; i < values.length ; i++){
    //  parent.addCustomerToMJ((ListItem)values[i]);
    }
  }

  private void filterSearch(){
    searchList.setListData(listfilter.getMatch(text.getText().trim()).toArray());
  }

  /**
   *
   * @return the JTextField value of text.
   */
  private JTextField getText(){
    return text;
  }

  /**
   *
   * @param aText - the new value for text
   */

  /**
   *
   * @return the JPanel value of searchPanel.
   */
  private JPanel getSearchPanel(){
    return searchPanel;
  }

  /**
   *
   * @param aSearchPanel - the new value for searchPanel
   */
 
  private TreeMap custs = null;
  private JList list = null;
  private JTextField text = null;
  private EksportFrame parent = null;
  private JPanel searchPanel = new JPanel(new BorderLayout());
  private ListFilter listfilter = null;
  private JList searchList = new JList();
}
