package maxflow14.gui;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import maxflow14.graph.Flow;
import maxflow14.graph.Graph;


@SuppressWarnings("serial")
public class HelpWindow extends JDialog implements ActionListener {
	private JButton confirm = new JButton("OK");


		JTextPane helpText, flowText;
	public HelpWindow(JFrame frame, Flow f) {
		super(frame, " ? ", true);
		JPanel panel = new JPanel();
		Dimension d=new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width-100, Toolkit.getDefaultToolkit().getScreenSize().height-100);
		if (d.width>1024) d.setSize(1024, d.height);
		this.setPreferredSize(d);
		this.setAlwaysOnTop(true);
		this.setLayout(new FlowLayout());
//		this.setResizable(false);
		flowText = new JTextPane();
		helpText = new JTextPane();
		StringBuffer flowtext = new StringBuffer();
		flowtext.append("*** Graph ***  \n");
		Graph g = f.getBaseNetwork();
		flowtext.append("\n\n Vertices: \n ");
		for (int u:g.getVertexList().keySet()) {
			flowtext.append(g.getVertexList().get(u).getLabel() + " ("+g.getVertexList().get(u).getId()+")   ");
		}
		flowtext.append("\n \n Edges: \n ");
		for (int u:g.getEdgeList().keySet()) {
			for (int v:g.getEdgeList().get(u).keySet()) {
				flowtext.append(g.getEdgeList().get(u).get(v).toString() + ";  ");
			}
			if (g.getEdgeList().get(u).size()>0) flowtext.append("\n");
		}
		
		flowtext.append("\n \n *** Flow: *** \n ");
		flowtext.append("Value: "+((Double)f.getIncomingFlow(f.getBaseNetwork().getT().getId())).toString()+" \n");
		for (int u:f.getFlowMap().getEdgeList().keySet()) {
			for (int v:f.getFlowMap().getEdgeList().get(u).keySet()) {
				if (f.getFlow(u, v)>0) {
					String texxt=f.getFlowMap().getEdgeList().get(u).get(v).toString().replace("c:" , "");
					flowtext.append(texxt + "\n");
				}
			}
		}
		String text = flowtext.toString();
		String helptext="Help: \n \n " +
				"\n EdmondsKarp: \n " +
				"\n [DE] " +
				"\n Die linke Ansicht zeigt den Graphen, in dem der maximale Fluss von s nach t gesucht ist. Die rechte Ansicht zeigt die Flusswerte oder das Restnetzwerk. Der Algorithmus erhöht die Flusswerte entlang Pfaden von s nach t im Restnetzwerk. Das Restnetzwerk enthält die ursprünglichen Kanten, mit um die gesetzten Flusswerte verringter Kapazität, sowie \"Rückwärtskanten\" in Gegenrichtung, mit den Flusswerten als Kapazität." +"\n" +
				"\n [EN] \n" +
				"The left panel shows the original graph that is being worked on. " +
				"The right panel shows either current flow values or a so-called Residual network. This allows the algorithm to see if there is a possible \"augmenting path\" left to increase flow on. It also allows to increase flow on \"backwards edges\", which are created correspondingly for all currently set flow values (flow is decreased when those are part of the augmenting path)." +
				"\n" +
				"\n" +
				"\n" +"*** \n" +
				"\n PushRelabel \n" +
				"\n [DE]" +
				"\n Der pink gezeichnete Knoten wird gerade besucht, d.h. seine Höhe oder die ausgehenden Flusswerte ändern sich." +
				"\n Im Knoten ist der Überschuss (Excess) und die Höhe (Height) angegeben. \"Aktive\" Knoten mit excess>0 (außer s und t) werden rot angezeigt." +
				"\n \n [EN] \n " +

				"The pink node is currently being visited, that means a change of the \"height\" value itself, or a change of the flow values on adjacent edges. While the algorithm works, it sets flow values so that the vertices might have \"excess\", meaning that there is more incoming than outgoing flow. Vertices with excess>0 are drawn red, except for s and t. After increasing flow values, the algorithm subsequently corrects flow values to reestablish zero excess in all nodes." +
				"\n" +
				"\n" +"*** \n [DE] \n " +
				"In beiden Algorithmen wird beim Klick auf Kanten/Knoten unten rechts die aktuelle" +
				" Auswahl angezeigt. Sie kann durch den Button \" Delete \" aus dem Graphen entfernt werden," +
				"sofern es sich nicht um s, t oder eine Rückkante im Restnetzwerk handelt." + 
				" Danach wird der Algorithmus zurückgesetzt, ebenfalls nach dem Erstellen von Kanten und Kanten." + 
				"\n Die Kanten können über den grünen Punkt in der Mitte verbogen und die Knoten verschoben werden. Manchmal sind Doppelkanten nicht gut als solche zu erkennen, dann " +
				"hilft das Verschieben des Knotens. Die verbogenen Kanten werden beim Verschieben von Knoten zurückgesetzt." +
				"Durch \"Save/Load\" werden der Graph, die Position im Algorithmus und die Positionen der Knoten mit gespeichert bzw. geladen."
				
				+ "\n \n " +"[EN] \n" +
				"In all views, information about the selection is shown right on the bottom of the window after clicking on vertices or edges.\n "
				+"If the selection is not s (source), or  t (target), or a \"backward\" edge in the residual network, it can be removed from the graph by clicking on the delete button. "
				+"After removing (or adding) any vertex or edge from the graph, the algorithm will reset. \n"
				+"Using the save and load buttons, it is possible write the graph, algorithm state and vertex positions and to restore it from a file. \n"
				+"Edges can be bent using the green dot displayed in the middle when selected. Moving any vertex causes all bent edges to reset. In some cases, the automatic bending of \"double\" edges fails. If you notice such a pair of overlapping edges, try moving one of the vertices. You may also \"bend\" the edges apart manually.";
		flowText.setText(text);
		flowText.setEditable(false);
		helpText.setEditable(false);
		helpText.setText(helptext);
		helpText.setAlignmentY(TOP_ALIGNMENT);
		helpText.setPreferredSize(new Dimension(d.width/2 -80,d.height-150));
		helpText.setOpaque(false);
		flowText.setAlignmentY(TOP_ALIGNMENT);
		flowText.setPreferredSize(new Dimension(d.width/2 -80,d.height-150));
		JScrollPane scrollPane1 = new JScrollPane();
		JScrollPane scrollPane2 = new JScrollPane();
		scrollPane1.setViewportView(flowText);
		scrollPane2.setViewportView(helpText);
		
		scrollPane1.setAlignmentY(TOP_ALIGNMENT);
		scrollPane2.setAlignmentY(TOP_ALIGNMENT);
		scrollPane1.setPreferredSize(new Dimension(d.width/2-100,d.height-150));
		scrollPane2.setPreferredSize(new Dimension(d.width/2-100,d.height-150));
		scrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panel.setLayout(new FlowLayout());
		panel.add(scrollPane1);
		panel.add(scrollPane2);
		panel.add(confirm);
		
		add(panel);
		setLocationRelativeTo(frame);
		confirm.addActionListener(this);
		confirm.setEnabled(true);
		confirm.setVisible(true);
		confirm.addKeyListener(enterListener);
		this.addComponentListener(new java.awt.event.ComponentAdapter() {
		     public void componentShown(java.awt.event.ComponentEvent e) {
		          confirm.requestFocus();
		     }
		});
		pack();
		this.setLocation(10, 25);
		scrollPane1.setLocation(0, 0);
		scrollPane2.setLocation(0+d.width/2-50, 0);
		panel.setLocation(0,0);
	}

	private void confirm() {
			dispose();
		}
		
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == confirm) {
			confirm();
			}
	}
	private KeyListener enterListener = new KeyListener() {

		@Override
		public void keyPressed(KeyEvent ke) {    
		}
		@Override
		public void keyReleased(KeyEvent ke) {
        	if (ke.getKeyCode()==KeyEvent.VK_ENTER) {
        		ke.consume();
        		confirm();
        	}
		}

		@Override
		public void keyTyped(KeyEvent ke) {
		}
		
	};
}
