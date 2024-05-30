package inc.vareli.crusman.UI;

import javax.swing.*;  
import java.awt.event.*;  
public class TextAreaExample {  
	TextAreaExample() {

		///////////////////////
		JFrame f = new JFrame("label");
		JLabel l1;
		l1 = new JLabel("bruh label");
		l1.setBounds(20, 20, 80, 10);
		f.add(l1);
		f.setSize(200,200);
		f.setLayout(null);
		f.setVisible(true);
		////////////////////////////

		JTextField tf = new JTextField();
		tf.setBounds(40, 40, 140, 10);
		JButton b = new JButton("Find crewmate");
		/*
		   b.setbounds950(50, 100, 95, 25);
		   b.addActionListener(this);
		   add(b);
		   setSize(300, 300);
		   setLayout(null);
		   setVisible(true);*/

		//////////////////////////////

		JTextArea area=new JTextArea("Text area");  
		area.setBounds(10,30, 200,200);  
		f.add(area);  
		f.setSize(300,300);  
		f.setLayout(null);  
		f.setVisible(true);  
	}  

	/** 
	  public void actionPerformed (ActionEvent e) {
	  try {

	  String host = tf.getText();

	  }
	  }

*/
	public static void main(String args[])  {  
		new TextAreaExample();  
	}
}  
