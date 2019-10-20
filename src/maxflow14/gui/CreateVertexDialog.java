package maxflow14.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import maxflow14.graph.Graph;


@SuppressWarnings("serial")
public class CreateVertexDialog extends JDialog implements ActionListener {
	private JTextField textfield = new JTextField(10);
	private Graph g = new Graph();
	private JButton confirm = new JButton("Erstellen");
	private JButton abort = new JButton("Abbrechen");

	public int parseVid() {
		int vid = -42;
		try {
			vid = Integer.parseInt(textfield.getText());
			if (!g.hasVertex(vid)) {
				return vid;
			} else {
				JFrame frame = new JFrame();
				JOptionPane.showMessageDialog(frame,
						"Konnte Knoten nicht hinzufügen. Ein Knoten mit der ID "
								+ vid + " ist bereits vorhanden.");
				textfield.setText("");
				return -42;
			}
		} catch (NumberFormatException e) {
			textfield.setText("");
			JFrame frame = new JFrame();
			JOptionPane.showMessageDialog(frame, "Keine gültige ID");
			return -42;
		}
	}

	public CreateVertexDialog(JFrame frame, Graph g) {
		super(frame, "Knoten erstellen", true);
		this.g = g;
		JPanel panel = new JPanel();
		panel.add(new JLabel("ID eingeben:"));
		panel.add(textfield);
		panel.add(confirm);
		panel.add(abort);
		add(panel);
		pack();
		setLocationRelativeTo(frame);
		textfield.addKeyListener(enterListener);
		confirm.addActionListener(this);
		abort.addActionListener(this);
		this.addComponentListener(new java.awt.event.ComponentAdapter() {
		     public void componentShown(java.awt.event.ComponentEvent e) {
		          textfield.requestFocus();
		     }
		});
	}

	private int vid = -1;

	public int getLastCreatedId() {
		return vid;
	}
	private void confirm() {
		int v = parseVid();
		if (v != -42) {
			g.addVertex(v, "v" + v);
			vid = v;
			dispose();
		}
		
	}
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == confirm) {
			confirm();
			}
	else if (e.getSource() == abort) {
			
			dispose();
		}
	}
	private KeyListener enterListener = new KeyListener() {

		@Override
		public void keyPressed(KeyEvent ke) {    
        	if (ke.getKeyCode()==KeyEvent.VK_ENTER) {
        		ke.consume();
        		confirm();
        	}
		}
		@Override
		public void keyReleased(KeyEvent ke) {
		}

		@Override
		public void keyTyped(KeyEvent ke) {
		}
		
	};
}
