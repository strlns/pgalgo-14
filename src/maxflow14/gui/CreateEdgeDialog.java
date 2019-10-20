package maxflow14.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import maxflow14.graph.Graph;


@SuppressWarnings("serial")
public class CreateEdgeDialog extends JDialog implements ActionListener {
	private JComboBox<String> uChoice = new JComboBox<String>();
	private JComboBox<String> vChoice = new JComboBox<String>();
	private Graph g = new Graph();
	private JButton confirm = new JButton("Erstellen");
	private JButton abort = new JButton("Abbrechen");
	private JTextField textfield = new JTextField(10);

	public double parseWeight() {
		double weight = -1;
		try {
			weight = Double.parseDouble(textfield.getText());
			if (weight > 0) {
				return weight;
			} else {
				JFrame frame = new JFrame();
				JOptionPane.showMessageDialog(frame,
						"Negative Gewichte sind hier nicht erlaubt.");
				textfield.setText("");
				return -1;
			}
		} catch (NumberFormatException e) {
			textfield.setText("");
			JFrame frame = new JFrame();
			JOptionPane.showMessageDialog(frame, "Keine Zahl");
			return -1;
		}
	}

	public CreateEdgeDialog(JFrame frame, Graph g) {
		super(frame, "Kante erstellen", true);
		this.g = g;
		this.setPreferredSize(new Dimension(400,250));
		
		JPanel panel = new JPanel();
		initU();
		initV();
		textfield.setText(((Integer)(int)Math.round(Math.random()*100)).toString());
		panel.add(uChoice);
		panel.add(vChoice);
		uChoice.setSelectedIndex((int)Math.round(Math.random()*(uChoice.getItemCount()-1)));
		refreshV();
		vChoice.setSelectedIndex((int)Math.round(Math.random()*(vChoice.getItemCount()-1)));
		refreshU();
		textfield.addKeyListener(enterListener);
		this.addComponentListener(new java.awt.event.ComponentAdapter() {
		     public void componentShown(java.awt.event.ComponentEvent e) {
		          textfield.requestFocus();
		     }
		});
		panel.add(new JLabel("cap: "));
		panel.add(textfield);
		panel.add(confirm);
		panel.add(abort);
		add(panel);
		pack();
        setLocationRelativeTo(frame);
		confirm.addActionListener(this);
		abort.addActionListener(this);
		uChoice.addItemListener(uChoiceListener);
		vChoice.addItemListener(vChoiceListener);
		
	}

	public void initU() {
		uChoice.removeAllItems();
		for (int u : g.getVertexList().keySet()) {
			if (u != g.getT().getId()) {
				uChoice.addItem(u + " | " + g.getVertex(u).getLabel());
			}
		}
	}

	public void initV() {
		vChoice.removeAllItems();
		for (int v : g.getVertexList().keySet()) {
			if (v != g.getS().getId()) {
				vChoice.addItem(v + " | " + g.getVertex(v).getLabel());
			}
		}
	}
	
	public void refreshU() {

		Object selectedItem = uChoice.getSelectedItem();
		initU();
		uChoice.removeItem(vChoice.getSelectedItem());
		uChoice.setSelectedItem(selectedItem);
	}

	public void refreshV() {
		Object selectedItem = vChoice.getSelectedItem();
		initV();
		vChoice.removeItem(uChoice.getSelectedItem());
		vChoice.setSelectedItem(selectedItem);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == confirm) {
			confirm();
		} 
		else if (e.getSource() == abort) {
			dispose();
		}
	}
	private void confirm() {
		String u = ((String) uChoice.getSelectedItem());
		int uid = Integer.parseInt(u.substring(0, u.indexOf("|") - 1));
		String v = ((String) vChoice.getSelectedItem());
		int vid = Integer.parseInt(v.substring(0, v.indexOf("|") - 1));
		double weight = parseWeight();
		if (weight == -1)
			return;
		g.addEdge(uid, vid, weight);
		dispose();
	}
	private ItemListener uChoiceListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent event) {
			if (event.getStateChange() == ItemEvent.SELECTED) {
				vChoice.removeItemListener(vChoiceListener);
				refreshV();
				vChoice.addItemListener(vChoiceListener);
			}
		}
	};
	private ItemListener vChoiceListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent event) {
			if (event.getStateChange() == ItemEvent.SELECTED) {
				uChoice.removeItemListener(uChoiceListener);
				refreshU();
				uChoice.addItemListener(uChoiceListener);
			}
		}
	};
	private KeyListener enterListener = new KeyListener() {

		@Override
		public void keyPressed(KeyEvent ke) {    
        	if (ke.getKeyCode()==KeyEvent.VK_ENTER) 
        		confirm.doClick();
		}
		@Override
		public void keyReleased(KeyEvent ke) {
		}

		@Override
		public void keyTyped(KeyEvent ke) {
		}
		
	};

}
