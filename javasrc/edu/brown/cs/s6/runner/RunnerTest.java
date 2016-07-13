package edu.brown.cs.s6.runner;



import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;



public class RunnerTest {



/********************************************************************************/
/*										*/
/*	UI Class								*/
/*										*/
/********************************************************************************/

public static class S6_UI_CLASS {
   public static java.awt.Component S6_UI(){
      try {
	 AddPlayerFrame temp=new AddPlayerFrame();
	 return temp;
       }
      catch (	 java.lang.Throwable tex) {
	 throw new java.lang.Error(tex);
       }
    }
}




/********************************************************************************/
/*										*/
/*	Result class								*/
/*										*/
/********************************************************************************/

public static class AddPlayerFrame extends JDialog {
  private static final long serialVersionUID=5363319517090557033L;
  public static final String DIALOG_TITLE="Add Player";
  private JCheckBox aliasCheckbox;
  private JTextField aliasField;
  private JTextField userField;
  private JCheckBox passCheckbox;
  private JPasswordField passField;
  /**
 * Constructor
 * @param owner Owner
 * @param p Player to edit
 */
  public AddPlayerFrame(){
    super((javax.swing.JDialog)null,DIALOG_TITLE,true);
    buildUI();
    userField.selectAll();
    setLocationRelativeTo((javax.swing.JDialog)null);
  }
  /**
 * Build the frame UI
 */
  public void buildUI(){
    setLayout(new BorderLayout(0,0));
    setSize(350,getHeight());
    setResizable(false);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    JPanel buttonsPanel=new JPanel();
    buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT,3,3));
    JButton addBtn=new JButton("Add");
    JButton cancelBtn=new JButton("Cancel");
    addBtn.setPreferredSize(new Dimension(70,addBtn.getPreferredSize().height));
    cancelBtn.setPreferredSize(new Dimension(70,cancelBtn.getPreferredSize().height));
    buttonsPanel.add(addBtn);
    buttonsPanel.add(cancelBtn);
    final AddPlayerFrame instance=this;
    addBtn.addActionListener(new ActionListener(){
      @Override public void actionPerformed(	  ActionEvent e){
	if (!addPlayer())	  return;
	instance.dispose();
      }
    }
);
    cancelBtn.addActionListener(new ActionListener(){
      @Override public void actionPerformed(	  ActionEvent e){
	instance.dispose();
      }
    }
);
    JPanel mainPnl=new JPanel();
    mainPnl.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
    mainPnl.setLayout(new BoxLayout(mainPnl,BoxLayout.Y_AXIS));
    mainPnl.add(getAccountPanel());
    mainPnl.add(getAliasPanel());
    mainPnl.add(buttonsPanel,BorderLayout.SOUTH);
    mainPnl.setPreferredSize(new Dimension(getWidth(),mainPnl.getPreferredSize().height));
    add(mainPnl);
    pack();
  }
  public JPanel getAccountPanel(){
    JPanel panel=new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("Account"));
    GridBagConstraints fieldC=new GridBagConstraints();
    fieldC.fill=GridBagConstraints.HORIZONTAL;
    fieldC.weightx=1.0;
    fieldC.gridwidth=GridBagConstraints.REMAINDER;
    fieldC.insets=new Insets(2,1,2,1);
    GridBagConstraints labelC=(GridBagConstraints)fieldC.clone();
    labelC.weightx=0.0;
    labelC.gridwidth=1;
    labelC.insets=new Insets(1,1,1,10);
    GridBagConstraints checkboxC=(GridBagConstraints)fieldC.clone();
    checkboxC.insets=new Insets(5,2,1,2);
    GridBagLayout layout=new GridBagLayout();
    panel.setLayout(layout);
    JLabel userLbl=new JLabel("Username:",SwingConstants.LEFT);
    JLabel passLbl=new JLabel("Password:",SwingConstants.LEFT);
    userField=new JTextField();
    passCheckbox=new JCheckBox("Remember password");
    passField=new JPasswordField();
    passCheckbox.setSelected(true);
    passField.setEnabled(true);
    passCheckbox.addActionListener(new ActionListener(){
      @Override public void actionPerformed(	  ActionEvent e){
	JCheckBox checkbox=(JCheckBox)e.getSource();
	passField.setEnabled(checkbox.isSelected());
      }
    }
);
    userLbl.setLabelFor(userField);
    passLbl.setLabelFor(passField);
    layout.setConstraints(userField,fieldC);
    layout.setConstraints(passField,fieldC);
    panel.add(userLbl,labelC);
    panel.add(userField,fieldC);
    panel.add(passCheckbox,checkboxC);
    panel.add(passLbl,labelC);
    panel.add(passField,fieldC);
    return panel;
  }
  public JPanel getAliasPanel(){
    JPanel panel=new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("Alias"));
    GridBagConstraints fieldC=new GridBagConstraints();
    fieldC.fill=GridBagConstraints.HORIZONTAL;
    fieldC.weightx=1.0;
    fieldC.gridwidth=GridBagConstraints.REMAINDER;
    fieldC.insets=new Insets(2,1,2,1);
    GridBagConstraints labelC=(GridBagConstraints)fieldC.clone();
    labelC.weightx=0.0;
    labelC.gridwidth=1;
    labelC.insets=new Insets(1,1,1,10);
    GridBagConstraints checkboxC=(GridBagConstraints)fieldC.clone();
    checkboxC.insets=new Insets(5,2,1,2);
    GridBagLayout layout=new GridBagLayout();
    panel.setLayout(layout);
    JLabel aliasLbl=new JLabel("Alias:",SwingConstants.LEFT);
    aliasCheckbox=new JCheckBox("Use custom alias");
    aliasField=new JTextField();
    aliasCheckbox.setSelected(false);
    aliasField.setEnabled(false);
    aliasCheckbox.addActionListener(new ActionListener(){
      @Override public void actionPerformed(	  ActionEvent e){
	JCheckBox checkbox=(JCheckBox)e.getSource();
	aliasField.setEnabled(checkbox.isSelected());
      }
    }
);
    aliasLbl.setLabelFor(aliasField);
    layout.setConstraints(aliasField,fieldC);
    panel.add(aliasCheckbox,checkboxC);
    panel.add(aliasLbl,labelC);
    panel.add(aliasField,fieldC);
    return panel;
  }
  /**
 * Add a player
 * @return True if succeed
 */
  public boolean addPlayer(){
    final String login=userField.getText().trim();
    if (login.equals("")) {
      return false;
    }
    return true;
  }
}




/********************************************************************************/
/*										*/
/*	Test program								*/
/*										*/
/********************************************************************************/

private static RunnerS6HierData [] s6_hier_data = new RunnerS6HierData[] {
   new RunnerS6HierData("C1",-1,-1,-1,-1,"javax.swing.JComponent,",null,null,null,null,null,6),
      new RunnerS6HierData("C2",-1,-1,-1,-1,"javax.swing.JLabel,","C1",null,"C1",null,"Sign in name",0),
      new RunnerS6HierData("C3",-1,-1,-1,-1,"javax.swing.text.JTextComponent,",null,null,"C2",null,null,0),
      new RunnerS6HierData("C4",-1,-1,-1,-1,"javax.swing.JLabel,","C2",null,"C1",null,"Password",0),
      new RunnerS6HierData("C5",-1,-1,-1,-1,"javax.swing.JPasswordField,",null,null,"C4",null,null,0),
      new RunnerS6HierData("C6",-1,-1,-1,-1,"javax.swing.AbstractButton,","C5",null,null,null,"Next time auto sign in",0),
      new RunnerS6HierData("C7",-1,-1,-1,-1,"javax.swing.AbstractButton,",null,"C1",null,null,"Sign In",0),
};


private static RunnerS6HierData [] s6_hier_data1 = new RunnerS6HierData[] {
   new RunnerS6HierData("C1",-1,-1,-1,-1,"javax.swing.JComponent,",null,null,null,null,null,7),
      new RunnerS6HierData("C2",-1,-1,-1,-1,"javax.swing.JLabel,","C1",null,"C1",null,"Sign in name",0),
      new RunnerS6HierData("C3",-1,-1,-1,-1,"javax.swing.text.JTextComponent,",null,null,"C2",null,null,0),
      new RunnerS6HierData("C4",-1,-1,-1,-1,"javax.swing.JLabel,","C2",null,"C1",null,"Password",0),
      new RunnerS6HierData("C5",-1,-1,-1,-1,"javax.swing.JPasswordField,",null,null,"C4",null,null,0),
      new RunnerS6HierData("C6",-1,-1,-1,-1,"javax.swing.AbstractButton,","C5",null,null,null,"Next time auto sign in",0),
      new RunnerS6HierData("C7",-1,-1,-1,-1,"javax.swing.AbstractButton,",null,"C1",null,null,"Sign In",0),
      new RunnerS6HierData("C8",-1,-1,-1,-1,"javax.swing.JTree",null,"C1",null,null,null,0),
};




public static void main(String [] args)
{
   java.awt.Component y;
   y = S6_UI_CLASS.S6_UI();
   RunnerSwingMatcher sm = new RunnerSwingMatcher(s6_hier_data,y);
   double v = sm.computeMatch();
   System.err.println("MATCH 0 SCORE = " + v);

   RunnerSwingMatcher sm1 = new RunnerSwingMatcher(s6_hier_data1,y);
   double v1 = sm1.computeMatch();
   System.err.println("MATCH 1 SCORE = " + v1);

   RunnerTestCase.assertMatchHierarchy((y),s6_hier_data);
   RunnerSwingShow rss = new RunnerSwingShow("S6TestCase Interactor",y);
   rss.process();
}




}	// end of RunnerTest.java
