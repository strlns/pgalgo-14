package maxflow14.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import maxflow14.algos.Algo;
import maxflow14.algos.EdmondsKarp;
import maxflow14.algos.PushRelabel;
import maxflow14.graph.Edge;
import maxflow14.graph.Flow;
import maxflow14.graph.Graph;
import maxflow14.graph.Vertex;
import maxflow14.util.FileIO;
import maxflow14.util.RandomGraph;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraphView;
//Die Klasse zeichnet und steuert alle sichtbaren Komponenten
//und ist gleichzeitig "Controller" f�r den Algorithmus und alles andere

/**
 * MainWindow is the main GUI, as well as the "controller" for all operations in the algorithms. 
 * @see maxflow14.gui.GraphPanel
 */
public class MainWindow extends JFrame implements ActionListener {
	private static final long serialVersionUID = (long) Math.random()
			* Integer.MAX_VALUE;
	private Graph g;
	public Algo algo;
	private GraphPanel panel1, panel2, panel3;
	private ControlPanel controlPanel;
	private Dimension graphPanelSize;
	ArrayList<Integer> userCreatedVertices = new ArrayList<Integer>();

	public MainWindow(Graph g) {
		this.g = g;
		init();
	}

	public void init() {
		//	Initialisierung der Gr��en von Fenster und Graphen
		Dimension d = new Dimension(
				Toolkit.getDefaultToolkit().getScreenSize().width,
				Toolkit.getDefaultToolkit().getScreenSize().height - 100);

		graphPanelSize = new Dimension((int)Math.round(d.getWidth()/2 - 230), (int) Math.round(d.getHeight()-70));
		//		Hier wird der Algorithmus initialisiert
		//		Objekt "algo" bleibt immer bestehen und wird �berschrieben
		//		(Interface Algo)
		
//		Diese scheinbar sinnlose Zuweisung sorgt durch Initialisierung des EK daf�r,
//		dass nicht eine lange Wartezeit nach dem Wechsel von PR zu EK entsteht
		algo = new EdmondsKarp(g);
		algo = new PushRelabel(g);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setTitle("Maximum Flow");
		FlowLayout layout=new FlowLayout();
		this.setLayout(layout);
		layout.setAlignment(FlowLayout.LEADING);
		layout.setAlignOnBaseline(true);
		//		Listener f�r Tasten 
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new MyDispatcher());
		//		Listener f�r Fenstergr��e
		this.addComponentListener(windowResizeListener);
		this.addWindowStateListener(windowStateListener);
		//		ControlPanel ist eine subklasse von MainWindow die die Steuerelemente rechts
		//		enth�lt 
		controlPanel = new ControlPanel();
		panel1 = new GraphPanel(graphPanelSize);
		panel1.setZoom(0.5);
		//		graphlistener dienen erstmal dazu, alle Änderungen zwischen den Graphen
		//		zu �bernehmen, die Synchronierung der Knotenpositionen zwischen
		//		panel1 und panel2 bei EK
		//		ist aber im mouseListener("selectionListener") implementiert (sonst R�ckkopplung)
		panel1.addChangeListener(graphListener1);
		panel1.getGraphComponent().getGraphControl().addMouseListener(selectionListener1);
		panel1.setAlignmentY(TOP_ALIGNMENT);
		panel2 = new GraphPanel(graphPanelSize);
		panel2.setPreferredSize(graphPanelSize);
		panel2.addChangeListener(graphListener2);
		panel2.setZoom(0.5);
		panel2.setAlignmentY(TOP_ALIGNMENT);
		panel2.getGraphComponent().getGraphControl().addMouseListener(selectionListener2);
		panel3 = new GraphPanel(new Dimension(graphPanelSize.width * 2, graphPanelSize.height));
		panel3.drawFlowPushRelabel((PushRelabel)algo);
		panel3.setPreferredSize(new Dimension(graphPanelSize.width * 2, graphPanelSize.height));
		panel3.setAlignmentY(TOP_ALIGNMENT);
		panel3.setZoom(0.5);
		panel3.getGraphComponent().getGraphControl().addMouseListener(selectionListener3);                
		this.setSize(d);
		controlPanel.setPreferredSize(new Dimension(220, d.height));
		controlPanel.setAlignmentY(TOP_ALIGNMENT);
		controlPanel.setAlignmentX(RIGHT_ALIGNMENT);
		controlPanel.algoMenu.setSelectedIndex(1);
		controlPanel.setVisible(true);
		this.add(panel1);
		this.add(panel2);
		this.add(panel3);
		this.add(controlPanel);
		this.setVisible(true);
		controlPanel.sizeInput.setText("7");
		pack();
	}
	//  �bertr�gt die Positionen der Knoten von einem GraphPanel auf ein 
	//	anderes
	public void updatePositions(GraphPanel source, GraphPanel target) {
		target.setVertexPositions(source.getVertexPositions());
	}
	private RandomGraph rg = new RandomGraph();
	//  Erstellt einen neuen "Zufallsgraphen", initialisiert aktuellen Algorithmus
	//	und zeichnet
	public void createNewRandom() {
		if (panel1.getGraphComponent().isEditing()||
				panel2.getGraphComponent().isEditing()||
				panel3.getGraphComponent().isEditing()) return;
		double c=controlPanel.parseCap();
		System.out.println(c);
		if (c-Math.round(c)==0) {
			g = rg.randomFlowGraph(controlPanel.parseSizeInt(), (int)c,
					controlPanel.parseTAdjInt(), true);
		}
		else {
			g = rg.randomFlowGraph(controlPanel.parseSizeInt(), c,
					controlPanel.parseTAdjInt(), false);
		}
		if (controlPanel.algoMenu.getSelectedIndex() == 0) {
			resetEK();
			panel1.drawGraph(g);
			panel1.scrollToS();
			drawRightEK("Flow");
			panel1.scrollToS();
			panel1.makeEdgesMovable();
			panel2.scrollToT();
			panel2.makeEdgesMovable();
		}
		if (controlPanel.algoMenu.getSelectedIndex() == 1) {
			resetPR();
			panel3.makeEdgesMovable();
			panel3.scrollToS();
		}

	}
	//	Entertaste dr�cken in den Eingabeparametern f�r Zufallsgraph
	private KeyListener randomEnterListener = new KeyListener() {
		@Override
		public void keyPressed(KeyEvent ke) {    
		}
		@Override
		public void keyReleased(KeyEvent ke) {
			if (ke.getKeyCode()==KeyEvent.VK_ENTER) {
				ke.consume();
				controlPanel.newRandomButton.doClick();
			}
		}
		@Override
		public void keyTyped(KeyEvent ke) {
		}
	};

	//	Tasten f�r Graphinteraktion
	private class MyDispatcher implements KeyEventDispatcher {
		@Override
		public boolean dispatchKeyEvent(KeyEvent ke) {
			if (ke.getID()==KeyEvent.KEY_RELEASED) {
				if (ke.getKeyCode()==KeyEvent.VK_V) {
					controlPanel.createVertexButton.grabFocus();
					ke.consume();
					controlPanel.createVertexButton.doClick();
				}
				else if (ke.getKeyCode()==KeyEvent.VK_E) 
				{
					ke.consume();
					controlPanel.createEdgeButton.doClick();
					controlPanel.createEdgeButton.grabFocus();
				}
				else if (ke.getKeyCode()==KeyEvent.VK_DELETE) 
				{
					controlPanel.stepButton.grabFocus();
					ke.consume();
					if (controlPanel.removeElementButton.isEnabled())
						controlPanel.removeElementButton.doClick();
				}
				else if (ke.getKeyCode()==KeyEvent.VK_S) 
				{
					controlPanel.stepButton.grabFocus();
					ke.consume();
					if (controlPanel.stepButton.isEnabled())
						controlPanel.stepButton.doClick();
				}
				else if (ke.getKeyCode()==KeyEvent.VK_F) 
				{
					controlPanel.finishButton.grabFocus();
					ke.consume();
					if (controlPanel.finishButton.isEnabled())
						controlPanel.finishButton.doClick();
				}
				else if (ke.getKeyCode()==KeyEvent.VK_R) 
				{
					controlPanel.resetButton.grabFocus();
					ke.consume();
					controlPanel.resetButton.doClick();
				}
			}        	
			return false;
		}
	} 

	private void createVertex(){
		Map<Integer,double[]> vPos = new HashMap<Integer,double[]>();
		Map<Integer,Map<Integer,LinkedList<mxPoint>>> edgePoints = new HashMap<Integer,Map<Integer,LinkedList<mxPoint>>>();
		if (controlPanel.algoMenu.getSelectedIndex()==0) {
			panel1.refreshPositions();
			vPos.putAll(panel1.getVertexPositions());
			edgePoints.putAll(panel1.getAllEdgePoints());
		}
		else if (controlPanel.algoMenu.getSelectedIndex()==1) {
			panel3.refreshPositions();
			vPos.putAll(panel3.getVertexPositions());
			edgePoints.putAll(panel3.getAllEdgePoints());
		}
		int vid = 0;
		if (controlPanel.toggleVertexDetails.isSelected()) {
			CreateVertexDialog d = new CreateVertexDialog(this, g);
			d.setVisible(true);
			vid = d.getLastCreatedId();
		} else {
			for (int v : g.getVertexList().keySet()) {
				if (v > vid)
					vid = v;
			}
			vid++;
			g.addVertex(vid, "v" + vid);
		}
		userCreatedVertices.add(vid);
		refresh();
		_reset();
		double[] n = {0,0};
		double[] m = {400,600};
		if (controlPanel.algoMenu.getSelectedIndex()==0) {
			algo.reset();
			if (g.getS()!=null)
				panel1.moveVertex(g.getS().getId(), n);
			if (g.getT()!=null)
				panel1.moveVertex(g.getT().getId(), m);

			if (userCreatedVertices.size() > 0) {
				for (int i = 0; i < userCreatedVertices.size(); i++) {
					double[] position = { 10 + ((i%2)*2-1)*50, 150+ 150 * i };
					panel1.moveVertex(userCreatedVertices.get(i), position);
				}
			}
			panel1.setVertexPositions(vPos);
			panel1.setEdgePoints(edgePoints);
			panel1.getGraph().refresh();
		}
		if (controlPanel.algoMenu.getSelectedIndex()==1) {
			if (g.getS()!=null)
				panel3.moveVertex(g.getS().getId(), n);
			if (g.getT()!=null)
				panel3.moveVertex(g.getT().getId(), m);

			if (userCreatedVertices.size() > 0) {
				for (int i = 0; i < userCreatedVertices.size(); i++) {
					double[] position = { 10 + ((i%2)*2-1)*50, 150+ 150 * i };
					panel3.moveVertex(userCreatedVertices.get(i), position);
				}
			}
			panel3.layoutGraph();
			panel3.setVertexPositions(vPos);
			panel3.setEdgePoints(edgePoints);
			panel3.getGraph().refresh();
		}
	}
	private void removeElement() {
		if (controlPanel.algoMenu.getSelectedIndex()==0) {
			mxCell selection = (mxCell) panel1.getGraph().getSelectionCell();
			if (selection==null) {
				controlPanel.selectText.setText("selection: \n");
				return;
			}
			if (selection.isVertex()) {
				Vertex v= g.searchVertex(selection.getId());
				if (v.getId()!=g.getS().getId()&&v.getId()!=g.getT().getId()) {
					panel1.removeVertex(v.getId());
					g.removeVertex(v.getId());
					algo=new EdmondsKarp(g);
					controlPanel.unfinish();
					controlPanel.flowValueText.setText("Value: 0");
					controlPanel.selectText.setText("selection: \n");
					controlPanel.show();
					controlPanel.removeElementButton.setEnabled(false);
					drawRightEK((String) controlPanel.rightViewMenu.getSelectedItem());
				}
				else {
					JFrame frame = new JFrame();
					JOptionPane.showMessageDialog(frame,
							"s und t k�nnen nicht entfernt werden.");
				}
			}
			else if (selection.isEdge()) {
				int[] uv = panel1.getEdgeUV(selection);
				panel1.removeEdge(uv[0], uv[1]);
				panel2.eraseGraph();
				g.removeEdge(g.getEdge(uv[0], uv[1]));
				algo=new EdmondsKarp(g);
				controlPanel.selectText.setText("selection: \n");
				controlPanel.unfinish();
				controlPanel.show();
				controlPanel.removeElementButton.setEnabled(false);
				controlPanel.flowValueText.setText("Value: 0");
				drawRightEK((String) controlPanel.rightViewMenu.getSelectedItem());
			}
		}
		else if (controlPanel.algoMenu.getSelectedIndex()==1) {
			mxCell selection = (mxCell) panel3.getGraph().getSelectionCell();
			if (selection==null) {
				return;
			}
			if (selection.isVertex()) {
				Vertex v= g.searchVertex(selection.getId());
				if (v.getId()!=g.getS().getId()&&v.getId()!=g.getT().getId()) {
					g.removeVertex(v.getId());
					algo=new PushRelabel(g);
					panel3.removeVertex(v.getId());
					Map<Integer,double[]> pos = new HashMap<Integer,double[]>();
					Map<Integer,Map<Integer,LinkedList<mxPoint>>> ep = new HashMap<Integer,Map<Integer,LinkedList<mxPoint>>>();
					pos.putAll(panel3.getVertexPositions());
					ep.putAll(panel3.getAllEdgePoints());
					panel3.drawFlowPushRelabel((PushRelabel)algo);
					panel3.makeEdgesMovable();
					panel3.setVertexPositions(pos);
					panel3.setEdgePoints(ep);
					panel3.getGraph().refresh();
					controlPanel.selectText.setText("selection: \n");
					controlPanel.unfinish();
					controlPanel.show();
					controlPanel.flowValueText.setText("Value: 0");
					controlPanel.removeElementButton.setEnabled(false);


				}
				else {
					JFrame frame = new JFrame();
					JOptionPane.showMessageDialog(frame,
							"s und t k�nnen nicht entfernt werden.");
				}
			}
			else if (selection.isEdge()) {
				int[] uv = panel3.getEdgeUV(selection);
				panel3.removeEdge(uv[0], uv[1]);

				g.removeEdge(g.getEdge(uv[0], uv[1]));
				algo=new PushRelabel(g);

				Map<Integer,double[]> pos = new HashMap<Integer,double[]>();
				Map<Integer,Map<Integer,LinkedList<mxPoint>>> ep = new HashMap<Integer,Map<Integer,LinkedList<mxPoint>>>();
				pos.putAll(panel3.getVertexPositions());
				ep.putAll(panel3.getAllEdgePoints());
				panel3.drawFlowPushRelabel((PushRelabel)algo);
				panel3.makeEdgesMovable();
				panel3.setVertexPositions(pos);
				panel3.setEdgePoints(ep);
				panel3.getGraph().refresh();
				controlPanel.unfinish();
				controlPanel.flowValueText.setText("Value: 0");
				controlPanel.selectText.setText("selection: \n");
				controlPanel.show();
				controlPanel.removeElementButton.setEnabled(false);
			}
		}
	}
	private void createEdge() {
		if (controlPanel.algoMenu.getSelectedIndex()==0) {
			algo.reset();
			panel1.refreshPositions();
			Map<Integer, double[]> vPos = new HashMap<Integer,double[]>();
			vPos.putAll(panel1.getVertexPositions());
			CreateEdgeDialog d = new CreateEdgeDialog(this, g);
			d.setVisible(true);
			refresh();
			_reset();
			panel1.setVertexPositions(vPos);
			panel1.makeEdgesMovable();
		}
		else if (controlPanel.algoMenu.getSelectedIndex()==1) {
			panel3.refreshPositions();
			Map<Integer, double[]> vPos = new HashMap<Integer,double[]>();
			vPos.putAll(panel3.getVertexPositions());
			CreateEdgeDialog d = new CreateEdgeDialog(this, g);
			d.setVisible(true);
			refresh();
			_reset();
			panel3.layoutGraph();
			panel3.setVertexPositions(vPos);
			panel3.makeEdgesMovable();
		}
	}
	// Reset Algorithmus+Zeichnen
	public void _reset() {
		controlPanel.setStepText(new LinkedList<JLabel>());
		controlPanel.flowValueText.setText("Value: 0");
		if (controlPanel.algoMenu.getSelectedIndex() == 0)
			resetEK();
		else if (controlPanel.algoMenu.getSelectedIndex() == 1)
			resetPR();
	}

	private void reset() {
		if (controlPanel.algoMenu.getSelectedIndex()==0) {
			panel1.refreshPositions();
			Map<Integer,double[]> vPos = new HashMap<Integer,double[]>();
			vPos.putAll(panel1.getVertexPositions());
			_reset();
			panel1.setVertexPositions(vPos);
		}
		else if (controlPanel.algoMenu.getSelectedIndex()==1) {
			panel3.refreshPositions();
			Map<Integer,double[]> vPos = new HashMap<Integer,double[]>();
			vPos.putAll(panel3.getVertexPositions());
			_reset();
			panel3.setVertexPositions(vPos);
		}
	}
	private void resetLayout() {
		if (controlPanel.algoMenu.getSelectedIndex()==0) {
			panel1.layoutGraph(false);
			updatePositions(panel1, panel2);
			panel1.makeEdgesMovable();
			panel2.makeEdgesMovable();
                        double[] xMinMax = new double[2];
                        double[] yMinMax = new double[2];
                        for (int i: new int[] {0,1}){
                            xMinMax[i] = 0;
                            yMinMax[i] = 0;
                        }
                        for (double[] i:panel1.getVertexPositions().values()) {
                            xMinMax[0] = Math.min(i[0], xMinMax[0]);
                            xMinMax[1] = Math.max(i[0], xMinMax[1]);
                            yMinMax[0] = Math.min(i[1], xMinMax[0]);
                            yMinMax[1] = Math.max(i[1], xMinMax[1]);
                        }
                        panel1.getGraph().getView().setGraphBounds(new mxRectangle(xMinMax[0], yMinMax[1], xMinMax[1], yMinMax[0]));
			controlPanel.selectText.setText("selection: \n");
		}
		else if (controlPanel.algoMenu.getSelectedIndex()==1) {
			panel3.layoutGraph(false);
			panel3.makeEdgesMovable();
			controlPanel.selectText.setText("selection: \n");
		}
	}
	private void finish() {
		if (!algo.hasNext())
			controlPanel.finished();
		if (controlPanel.algoMenu.getSelectedItem().equals("Edmonds-Karp")) {
			while (algo.hasNext()) {
				algo.next();
			}
			drawRightEK(controlPanel.rightViewMenu.getSelectedObjects()[0]
					.toString());
			controlPanel.finished();
		}
		else if (controlPanel.algoMenu.getSelectedItem().equals("Push-Relabel")) {
			panel3.refreshPositions();
			Map<Integer,double[]> vPos=new HashMap<Integer,double[]>();
			vPos.putAll(panel3.getVertexPositions());

			Map<Integer,Map<Integer,LinkedList<mxPoint>>> edgePoints = panel3.getAllEdgePoints();
			
			while (algo.hasNext()) {
				algo.next();
			}
			panel3.drawFlowPushRelabel((PushRelabel)algo);
			controlPanel.setStepCount(algo.getStepCount());
			controlPanel.finished();
			panel3.makeEdgesMovable();
			panel3.setEdgePoints(edgePoints);
			panel3.setVertexPositions(vPos);
		}
	}
	private void loadFlow(){

		FileIO fio = new FileIO();
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String filepath = fc.getSelectedFile().getAbsolutePath();
			Flow f = fio.readFlow(filepath);
			Map<Integer,double[]> vPos = fio.readPositions(filepath);
			Map<Integer,Map<Integer, LinkedList<mxPoint>>> edgePoints = fio.readEdgePoints(filepath);
			if (f.getBaseNetwork().getVertexList().size() == 0) {
				JFrame frame = new JFrame();
				JOptionPane.showMessageDialog(frame,
						"Diese Datei war nicht lesbar");
				return;
			}
			userCreatedVertices.clear();
			panel1.eraseGraph();
			panel2.eraseGraph();
			this.g = f.getBaseNetwork();
			Algo a = fio.readState(filepath);
			if (a.getClass().getSimpleName().equals("EdmondsKarp")) {
				controlPanel.algoMenu.setSelectedIndex(0);
				algo = (EdmondsKarp) a;
			}
			else if  (a.getClass().getSimpleName().equals("PushRelabel")) {
				controlPanel.algoMenu.setSelectedIndex(1);
				algo = (PushRelabel) a;
			}
			controlPanel.show();
			if (algo.hasNext()) controlPanel.unfinish();
			if (algo.getClass().getSimpleName().equals("EdmondsKarp")) {
				panel1.setVisible(true);
				panel2.setVisible(true);
				panel3.setVisible(false);
				panel1.drawGraph(g);
				drawRightEK(controlPanel.rightViewMenu.getSelectedObjects()[0].toString());
				panel1.setVertexPositions(vPos);
				updatePositions(panel1, panel2);
				panel1.makeEdgesMovable();
				panel2.makeEdgesMovable();
				panel1.setEdgePoints(edgePoints);
				LinkedList<JLabel> stepExplainText = new LinkedList<JLabel>();
				JLabel stepLabel;
				if (algo.getStepCount() > 0)
					for (String str : ((EdmondsKarp) algo).currentPath()
							.toText()) {
						str.replaceAll(((Integer)algo.repeat().getBaseNetwork().getT().getId()).toString(), "t");
						str.replaceAll(((Integer)algo.repeat().getBaseNetwork().getS().getId()).toString(), "s");
						stepLabel = new JLabel(str);
						stepLabel.setVisible(true);
						stepExplainText.add(stepLabel);
					}
				controlPanel.setStepText(stepExplainText);
			}
			else if (algo.getClass().getSimpleName().equals("PushRelabel")) {
				controlPanel.algoMenu.setSelectedIndex(1);
				panel1.setVisible(false);
				panel2.setVisible(false);
				panel3.setVisible(true);
				panel3.drawFlowPushRelabel((PushRelabel)algo);
				panel3.setVertexPositions(vPos);
				panel3.setEdgePoints(edgePoints);
				panel3.getGraph().refresh();
			}
			controlPanel.show();
			((JLabel) controlPanel.controls.get("stepCountLabel"))
			.setText("Step " + algo.getStepCount());
			if (!algo.hasNext())
				controlPanel.finished();
		}
	}
	private void saveFlow() {
		FileIO fio = new FileIO();
		JFileChooser fc = new JFileChooser();
		if (controlPanel.algoMenu.getSelectedItem().equals("Push-Relabel")) {
			int returnVal = fc.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String fp = fc.getSelectedFile().getAbsolutePath();
				fio.saveFlow(algo.repeat(), algo, panel3.getVertexPositions(), panel3.getAllEdgePoints(), fp);
				System.out.println("Datei " + fp + " geschrieben. ");
			}
		}
		else if (controlPanel.algoMenu.getSelectedItem().equals("Edmonds-Karp")) {
			int returnVal = fc.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String fp = fc.getSelectedFile().getAbsolutePath();
				fio.saveFlow(algo.repeat(), algo, panel1.getVertexPositions(), panel1.getAllEdgePoints(),fp);
				System.out.println("Datei " + fp + "geschrieben. ");
			}
		}
	}
	private void erase() {
		userCreatedVertices.clear();
		g = new Graph();
		g.addVertex(0, "s");
		g.addVertex(1, "t");
		if (controlPanel.algoMenu.getSelectedItem().equals("Edmonds-Karp")) {
			algo = new EdmondsKarp(g);
		} else if (controlPanel.algoMenu.getSelectedItem().equals(
				"Push Relabel")) {
			algo = new PushRelabel(g);
		}
		_reset();
		refresh();
		double[] n = {0,0};
		double[] m = {400,600};
		if (controlPanel.algoMenu.getSelectedIndex()==0) {
			panel1.moveVertex(g.getS().getId(), n);
			panel1.moveVertex(g.getT().getId(), m);
		}
		else if (controlPanel.algoMenu.getSelectedIndex()==1) {
			panel3.moveVertex(g.getS().getId(), n);
			panel3.moveVertex(g.getT().getId(), m);
		}
		controlPanel.selectText.setText("selection: \n");
	}
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == controlPanel.stepButton) {
			showNext();
		} 
		else if (ae.getSource() == controlPanel.newRandomButton) {
			userCreatedVertices.clear();
			createNewRandom();
		} 
		else if (ae.getSource() == controlPanel.rightViewMenu) {
			drawRightEK((String) controlPanel.rightViewMenu.getSelectedItem());
		} 
		else if (ae.getSource() == controlPanel.createVertexButton) {
			createVertex();
		} 
		else if (ae.getSource() == controlPanel.createEdgeButton) {
			createEdge();
		} 
		else if (ae.getSource() == controlPanel.removeElementButton) {
			removeElement();
		}
		else if (ae.getSource() == controlPanel.eraseButton) {
			erase();
		} 
		else if (ae.getSource() == controlPanel.resetButton) {
			reset();
		} 
		else if (ae.getSource()==controlPanel.resetLayoutButton) {
			resetLayout();
		}
		else if (ae.getSource() == controlPanel.finishButton) {
			finish();
		}
		else if (ae.getSource() == controlPanel.helpButton) {
			HelpWindow h = new HelpWindow(this, algo.repeat());
			h.setVisible(true);
		}
		else if (ae.getSource() == controlPanel.loadFlowButton) {
			loadFlow();
		}
		else if (ae.getSource() == controlPanel.saveFlowButton) {
			saveFlow();
		}
	}
	// Wenn der Benutzer den Algorithmus im Dropdownmen� wechselt, wird der ItemListener aktiv
	private ItemListener algoItemListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent event) {
			algo=null;
			if (event.getStateChange() == ItemEvent.SELECTED) {
				Object item = event.getItem();
				if (((String) item).equals("Edmonds-Karp")) {
					panel3.refreshPositions();
					Map<Integer,double[]> vPos = new HashMap<Integer,double[]>();
					vPos.putAll(panel3.getVertexPositions());
					Map<Integer,Map<Integer,LinkedList<mxPoint>>> edgePoints = panel3.getAllEdgePoints();

					panel1.setVisible(true);
					panel2.setVisible(true);
					panel3.setVisible(false);
					resetEK();
					algo=(EdmondsKarp)algo;
					panel1.drawGraph(g);
					panel2.drawRightEK_Flow((EdmondsKarp)algo);
					panel1.setVertexPositions(vPos);
					updatePositions(panel1, panel2);
					panel1.scrollToS();
					panel2.scrollToT();
					panel1.makeEdgesMovable();
					panel1.setEdgePoints(edgePoints);
					controlPanel.unfinish();
					pack();
				} else if (((String) item).equals("Push-Relabel")) {
					panel1.refreshPositions();
					Map<Integer,double[]> vPos = new HashMap<Integer,double[]>();
					vPos.putAll(panel1.getVertexPositions());
					Map<Integer,Map<Integer,LinkedList<mxPoint>>> edgePoints = panel1.getAllEdgePoints();
					panel1.setVisible(false);
					panel2.setVisible(false);
					panel3.setVisible(true);
					resetPR();
					algo = (PushRelabel) algo;
					panel3.setVertexPositions(vPos);
					panel3.makeEdgesMovable();
					panel3.setEdgePoints(edgePoints);
					panel3.scrollToS();
					controlPanel.unfinish();
				}
			}
		}
	};

	//	Listener f�r Zoomregler
	private ChangeListener sliderListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			if (e.getSource() == controlPanel.zoomSlider) {
				double zoom = (double) controlPanel.zoomSlider.getValue() / 100;
				panel1.setZoom(zoom);
				panel2.setZoom(zoom);
				panel3.setZoom(zoom);
			}
		}
	};
	
	//	Dieser Listener erkennt das Bewegen der Knoten 
	private mxIEventListener graphListener1 = new mxIEventListener() {
		public void invoke(Object sender, mxEventObject evt) {
			panel1.refreshPositions();
			updatePositions(panel1, panel2);
			//			panel1.setLocation(0, 75);
			//			panel2.setLocation(d.width / 2 - 110, 75);
			//			panel3.setLocation(0, 75);
		}
	};
	
	//	Dieser Listener erkennt das Markieren von Knoten und Kanten
	private MouseListener selectionListener1 = new MouseListener() {
		@Override
		public void mouseClicked(MouseEvent e) {
		}
		@Override
		public void mouseEntered(MouseEvent e) {
		}
		@Override
		public void mouseExited(MouseEvent e) {
		}
		@Override
		public void mousePressed(MouseEvent e) {
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			mxCell selection = (mxCell) panel1.getGraph().getSelectionCell();
			if (selection==null) {
				controlPanel.selectText.setText("\n selection: ");
				controlPanel.show();
				controlPanel.removeElementButton.setEnabled(false);
				return;
			}
			if (selection.isVertex()) {
				panel1.makeEdgesMovable();
				panel2.makeEdgesMovable();
				Vertex v = g.searchVertex(selection.getId());
				if (v!=null) {
					controlPanel.selectText.setText("\n selection: \n"+v.getLabel());
					controlPanel.show();
					controlPanel.removeElementButton.setEnabled(true);
				}
				panel2.scrollToVertex(v.getId());
			}
			else if (selection.isEdge()) {
				int[] uv = panel1.getEdgeUV(selection);
				controlPanel.selectText.setText("\n selection: \n"+g.getVertex(uv[0]).getLabel() +" -> "+g.getVertex(uv[1]).getLabel()+ "\n f " +algo.repeat().getFlow(uv[0],uv[1])+"\n / "+algo.repeat().getBaseNetwork().getEdge(uv[0],uv[1]).getWeight());
				controlPanel.show();
				controlPanel.removeElementButton.setEnabled(true);
				panel2.scrollToVertex(uv[0]);
			}
			else controlPanel.removeElementButton.setEnabled(false);
		}
	};
	
	//	Dieser Listener erkennt das Markieren von Knoten und Kanten
	private MouseListener selectionListener2 = new MouseListener() {
		@Override
		public void mouseClicked(MouseEvent e) {
		}
		@Override
		public void mouseEntered(MouseEvent e) {
		}
		@Override
		public void mouseExited(MouseEvent e) {
		}
		@Override
		public void mousePressed(MouseEvent e) {
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			mxCell selection = (mxCell) panel2.getGraph().getSelectionCell();
			if (selection==null) {
				controlPanel.selectText.setText("\n selection: ");
				controlPanel.show();
				controlPanel.removeElementButton.setEnabled(false);
				return;
			}
			if (selection.isVertex()) {
				panel2.refreshPositions();

				updatePositions(panel2,panel1);
				Vertex v = g.searchVertex(selection.getId());
				if (v!=null) {
					controlPanel.selectText.setText("\n selection: \n"+v.getLabel());
				}
				panel1.getGraph().setSelectionCell(panel1.getGraphVertices().get(v.getId()));
				panel2.makeEdgesMovable();
				panel1.makeEdgesMovable();
				panel1.scrollToVertex(v.getId());
				controlPanel.removeElementButton.setEnabled(true);
			}

			else if (selection.isEdge()) {
				int[] uv = panel2.getEdgeUV(selection);
				if (controlPanel.rightViewMenu.getSelectedIndex()==1) {
					HashSet<Edge> resEdges1 = new HashSet<Edge>();
					HashSet<Edge> resEdges2 = new HashSet<Edge>();
					resEdges1.addAll(algo.repeat().getResidualEdges()[0]);
					resEdges2.addAll(algo.repeat().getResidualEdges()[1]);
					for (Edge f: resEdges2) {
						if (f.getStart()==uv[0]&&f.getEnd()==uv[1]) {
							controlPanel.selectText.setText("selection: \n"+g.getVertex(uv[0]).getLabel() +" -> "+g.getVertex(uv[1]).getLabel() +"\n f " + algo.repeat().getFlow(uv[1], uv[0]) +"\n (backw. edge)");
							controlPanel.show();
							controlPanel.removeElementButton.setEnabled(false);
						}
					}
					for (Edge f: resEdges1) {
						if (f.getStart()==uv[0]&&f.getEnd()==uv[1]) {
							controlPanel.selectText.setText("selection: \n"+g.getVertex(uv[0]).getLabel() +" -> "+g.getVertex(uv[1]).getLabel() +"\n f " +algo.repeat().getFlow(uv[0],uv[1])+"\n / "+algo.repeat().getBaseNetwork().getEdge(uv[0],uv[1]).getWeight()+")");
							controlPanel.show();
							panel1.getGraph().setSelectionCell(panel1.getGraphEdges().get(uv[0]).get(uv[1]));
							controlPanel.removeElementButton.setEnabled(true);
							
						}
					}
					panel1.scrollToVertex(uv[0]);
				}
				else {
					controlPanel.selectText.setText("selection: \n"+g.getVertex(uv[0]).getLabel() +" -> "+g.getVertex(uv[1]).getLabel() +"\n f "+algo.repeat().getFlow(uv[0],uv[1])+"\n / "+algo.repeat().getBaseNetwork().getEdge(uv[0],uv[1]).getWeight());
					panel1.getGraph().getModel().beginUpdate();
					panel1.getGraph().setSelectionCell(panel1.getGraphEdges().get(uv[0]).get(uv[1]));
					panel1.getGraph().getModel().endUpdate();
					controlPanel.show();
					controlPanel.removeElementButton.setEnabled(true);
				}
			}
			else {
				controlPanel.selectText.setText("selection: \n");
				controlPanel.show();
				controlPanel.removeElementButton.setEnabled(false);
			}
		}
	};
	
	//	Dieser Listener erkennt das Markieren von Knoten und Kanten
	private MouseListener selectionListener3 = new MouseListener() {
		@Override
		public void mouseClicked(MouseEvent e) {
		}
		@Override
		public void mouseEntered(MouseEvent e) {
		}
		@Override
		public void mouseExited(MouseEvent e) {
		}
		@Override
		public void mousePressed(MouseEvent e) {
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			mxCell selection = (mxCell) panel3.getGraph().getSelectionCell();
			if (selection==null) {
				controlPanel.selectText.setText("\n selection: ");
				controlPanel.show();
				controlPanel.removeElementButton.setEnabled(false);
				return;
			}

			if (selection.isVertex()) {
				panel3.refreshPositions();
				panel3.makeEdgesMovable();
				Vertex v = g.searchVertex(selection.getId());
				if (v!=null) {
					double exc = algo.repeat().getIncomingFlow(v.getId())-algo.repeat().getOutgoingFlow(v.getId());
					int height=((PushRelabel)algo).getVertexHeight().get(v.getId());
					controlPanel.selectText.setText("selection: \n"+v.getLabel() + "\n excess: "+ exc + "\n "+ "height: "+height);
					controlPanel.show();
					controlPanel.removeElementButton.setEnabled(true);
				}
			}
			else if (selection.isEdge()) {
				int[] uv = panel3.getEdgeUV(selection);
				controlPanel.selectText.setText("selection: \n"+g.getVertex(uv[0]).getLabel() +" -> "+g.getVertex(uv[1]).getLabel() + " \n f "+algo.repeat().getFlow(uv[0],uv[1])+"\n / "+algo.repeat().getBaseNetwork().getEdge(uv[0],uv[1]).getWeight());
				controlPanel.show();
				controlPanel.removeElementButton.setEnabled(true);
			}
			else controlPanel.removeElementButton.setEnabled(false);
		}
	};
	
	//	Dieser Listener erkennt das Bewegen der Knoten
	public mxIEventListener graphListener2 = new mxIEventListener() {
		public void invoke(Object sender, mxEventObject evt) {
			panel2.refreshPositions();

		}
	};
	//	Dieser Listener erkennt das Ändern der Fenstergr��e
	private ComponentListener windowResizeListener = new ComponentAdapter() {
		@Override
		public void componentResized(ComponentEvent arg0) {
			Dimension d = ((JFrame) arg0.getSource()).getSize();
			if (d.width<400) d.setSize(400, d.height);
			if (d.height<400) d.setSize(d.width, 400);
			MainWindow.this.setPreferredSize(d);
			panel1.setPreferredSize(new Dimension(Math.round(d.width / 2 - 135),
					d.height- 70));
			panel2.setPreferredSize(new Dimension(Math.round(d.width / 2 - 135),
					d.height- 70));
			panel3.setPreferredSize(new Dimension(Math.round(d.width - 270),
					d.height- 70));
			panel1.resize(new Dimension(Math.round(d.width / 2 - 135),
					d.height - 70));
			panel2.resize(new Dimension(Math.round(d.width / 2 - 135),
					d.height - 70));
			panel3.resize(new Dimension(Math.round(d.width - 270),
					d.height - 70));
			controlPanel.setVisible(true);

			if (d.height<controlPanel.getSize().height) {
				controlPanel.setPreferredSize(new Dimension (controlPanel.getSize().width, d.height));
			}
			else {
				controlPanel.setPreferredSize(new Dimension (controlPanel.getSize().width, d.height));
			}
			panel1.setLocation(0, 75);
			panel2.setLocation(d.width / 2 - 135, 75);
			panel3.setLocation(0, 75);
			repaint();
			pack();
		}

	};
	
	//	Dieser Listener erkennt Maximieren des Fensters etc.
	private WindowStateListener windowStateListener = new WindowStateListener() {
		@Override
		public void windowStateChanged(WindowEvent e) {
			Dimension d;
			if (e.getNewState()==WindowEvent.WINDOW_DEICONIFIED) {
				d=Toolkit.getDefaultToolkit().getScreenSize();
				panel1.setPreferredSize(new Dimension(Math.round(d.width / 2 - 135),
						d.height));
				panel2.setPreferredSize(new Dimension(Math.round(d.width / 2 - 135),
						d.height));
				panel3.setPreferredSize(new Dimension(Math.round(d.width - 270),
						d.height));
				panel1.resize(new Dimension(Math.round(d.width / 2 - 135),
						d.height));
				panel2.resize(new Dimension(Math.round(d.width / 2 - 135),
						d.height));
				panel3.resize(new Dimension(Math.round(d.width - 270), d.height));
			}
			else {
				d = ((JFrame) e.getSource()).getSize();
				panel1.setPreferredSize(new Dimension(Math.round(d.width / 2 - 135),
						d.height));
				panel2.setPreferredSize(new Dimension(Math.round(d.width / 2 - 135),
						d.height));
				panel3.setPreferredSize(new Dimension(Math.round(d.width - 270),
						d.height));
				panel1.resize(new Dimension(Math.round(d.width / 2 - 135),
						d.height));
				panel2.resize(new Dimension(Math.round(d.width / 2 - 135),
						d.height));
				panel3.resize(new Dimension(Math.round(d.width - 270),d.height));
				MainWindow.this.setPreferredSize(d);
			}
			controlPanel.setVisible(true);
			panel1.setLocation(0, 75);
			panel2.setLocation(d.width / 2 - 135, 75);
			panel3.setLocation(0, 75);
			pack();
		}
	};
	
	//	Diese Methode f�hrt einen Schritt im Algorithmus durch und zeichnet ihn
	//	(ausgelagert in 2 einzelmethoden)
	public void showNext() {
		if (controlPanel.algoMenu.getSelectedIndex() == 0)
			showNextEK();
		else if (controlPanel.algoMenu.getSelectedIndex() == 1)
			showNextPR();
	}
	// Graph neu zeichnen
	public void refresh() {
		Map<Integer,double[]> vPos = new HashMap<Integer, double[]>();
		Map<Integer,Map<Integer,LinkedList<mxPoint>>> ep = new HashMap<Integer, Map<Integer,LinkedList<mxPoint>>>();
		if (controlPanel.algoMenu.getSelectedIndex() == 0) {
			vPos.putAll(panel1.getVertexPositions());
			ep.putAll(panel1.getAllEdgePoints());
			panel1.eraseGraph();
			panel1.drawGraph(g, vPos);
			panel1.setEdgePoints(ep);
			panel1.setVisible(true);
			if (controlPanel.rightViewMenu.getSelectedIndex() == 0) {
				panel2.drawRightEK_Flow((EdmondsKarp)algo);
				updatePositions(panel1, panel2);
				panel2.setVisible(true);
			} else if (controlPanel.rightViewMenu.getSelectedIndex() == 1) {
				panel2.drawRightEK_Residual((EdmondsKarp)algo);
				updatePositions(panel1, panel2);
				panel2.setVisible(true);
			}
		} else if (controlPanel.algoMenu.getSelectedIndex() == 1) {
			vPos.putAll(panel3.getVertexPositions());
			ep.putAll(panel3.getAllEdgePoints());
			panel3.drawFlowPushRelabel((PushRelabel)algo);
			panel3.setVertexPositions(vPos);
			panel3.setEdgePoints(ep);
			panel3.setVisible(true);
		}

	}
	// siehe reset()
	public void resetPR() {
		Map<Integer,Map<Integer,LinkedList<mxPoint>>> edgePoints = panel3.getAllEdgePoints();
		Map<Integer,double[]> vPos=panel3.getVertexPositions();
		panel3.getGraph().setSelectionCell(null);
		
//		Diese scheinbar sinnlose Zuweisung sorgt durch Initialisierung des EK daf�r,
//		dass nicht eine lange Wartezeit nach dem Wechsel von PR zu EK entsteht
		algo = new EdmondsKarp(g);
//		
		
		algo = new PushRelabel(g);
		algo.reset();
		if (algo.repeat().getFlowMap().getVertexList().size() == 0) {
			return;
		}
		((JLabel) controlPanel.controls.get("stepCountLabel")).setText("Step "
				+ ((Integer) algo.getStepCount()).toString());
		panel3.drawFlowPushRelabel((PushRelabel)algo);
		panel3.layoutGraph();

		mxCell selection = (mxCell) panel3.getGraph().getSelectionCell();
		if (selection==null) {
			controlPanel.selectText.setText(" ");
			controlPanel.removeElementButton.setEnabled(false);
		}
		else if (selection.isVertex()) {
			Vertex v = g.searchVertex(selection.getId()); 
			double exc = algo.repeat().getIncomingFlow(v.getId())-algo.repeat().getOutgoingFlow(v.getId());
			controlPanel.selectText.setText(v.getLabel() + "\n excess: "+exc);
		}
		panel3.makeEdgesMovable();
		panel3.setEdgePoints(edgePoints);
		panel3.setVertexPositions(vPos);
		panel3.scrollToS();
		controlPanel.show();
		controlPanel.flowValueText.setVisible(false);
	}
	
	public void showNextPR() {
		
		//		Die Knotenpositionen und Kanteneckpunkte m�ssen immer gespeichert und nachher
		//		neu gesetzt werden, weil der Graph jedes Mal neu gezeichnet wird
		//		und sonst das Layout st�ndig wechselt
		panel3.refreshPositions();
		Map<Integer,double[]> vPos=new HashMap<Integer,double[]>();
		vPos.putAll(panel3.getVertexPositions());
		Map<Integer,Map<Integer,LinkedList<mxPoint>>> edgePoints = panel3.getAllEdgePoints();
		
		if (algo.hasNext()) {
			algo.next();
			panel3.drawFlowPushRelabel((PushRelabel)algo);
			if (!algo.hasNext())
				controlPanel.finished();
		} else controlPanel.finished();
		((JLabel) controlPanel.controls.get("stepCountLabel")).setText("Step "
				+ ((Integer) algo.getStepCount()).toString());
		mxCell selection = (mxCell) panel3.getGraph().getSelectionCell();
		if (selection==null) {
			controlPanel.selectText.setText(" ");
			controlPanel.removeElementButton.setEnabled(false);
		}
		else if (selection.isVertex()) {
			Vertex v = g.searchVertex(selection.getId()); 
			double exc = algo.repeat().getIncomingFlow(v.getId())-algo.repeat().getOutgoingFlow(v.getId());
			controlPanel.selectText.setText(v.getLabel() + "\n excess: "+exc);
		}
		controlPanel.show();
		if (algo.hasNext()) controlPanel.flowValueText.setVisible(false);
		panel3.setVertexPositions(vPos);
		panel3.makeEdgesMovable();
		panel3.setEdgePoints(edgePoints);
	}
	
	public void resetEK() {
		
		algo = (EdmondsKarp) new EdmondsKarp(g);
		algo.reset();
		panel1.getGraph().setSelectionCell(null);
		panel2.getGraph().setSelectionCell(null);
		drawRightEK("Flow");
		controlPanel.unfinish();
		panel1.scrollToS();
		panel2.scrollToT();
		controlPanel.controls.put("flowValueText", controlPanel.flowValueText);
		controlPanel.show();
	}

	public void showNextEK() {

		//		Die Knotenpositionen und Kanteneckpunkte m�ssen immer gespeichert und nachher
		//		neu gesetzt werden, weil der Graph jedes Mal neu gezeichnet wird
		//		und sonst das Layout st�ndig wechselt
		//		Bei EK werden einfach die Positionen aus der linken Ansicht �bernommen,
		//		die immer gleich bleibt
		
		if (algo.hasNext()) {
			algo.next();
			drawRightEK(controlPanel.rightViewMenu.getSelectedObjects()[0]
					.toString());
			((JLabel) controlPanel.controls.get("stepCountLabel"))
			.setText("Step "
					+ ((Integer) algo.getStepCount()).toString());
			controlPanel.flowValueText.setText("Value: "+ algo.getFlowValue());
			LinkedList<JLabel> stepExplainText = new LinkedList<JLabel>();
			JLabel stepLabel;
			JLabel stepLabel0 = new JLabel("path: ");
			stepExplainText.add(stepLabel0);
			for (String str : ((EdmondsKarp) algo).currentPath().toText()) {

				stepLabel = new JLabel(str);
				stepLabel.setVisible(true);

				stepExplainText.add(stepLabel);
			}
			controlPanel.setStepText(stepExplainText);
			controlPanel.show();
			if (!algo.hasNext()) {
				controlPanel.finished();
				panel2.scrollToT();
			}
			else {
				panel2.scrollToVertex(((EdmondsKarp)algo).currentPath().getLast().getEnd());
			}
		} 
		else controlPanel.finished();
	}

	// Zeichnen der rechten Ansicht bei Edmonds-Karp
	//	(command= restnetzwerk oder flusswerte)
	public void drawRightEK(String command) {
		if (command.equals("Flow")) {
			panel2.eraseGraph();
			panel2.drawRightEK_Flow((EdmondsKarp)algo);
			updatePositions(panel1, panel2);

			panel2.makeEdgesMovable();
		} else if (command.equals("Residual graph")) {
			panel2.eraseGraph();
			panel2.drawRightEK_Residual((EdmondsKarp)algo);
			updatePositions(panel1, panel2);
			panel2.makeEdgesMovable();
		}
	}

	@SuppressWarnings("serial")
	public class ControlPanel extends JPanel {
		private JPanel stepExplainContainer = new JPanel();
		private int randSize = 4;
		private int randTAdj = 2;
		public int parseTAdjInt() {
			try {
				randTAdj = Integer.parseInt(tAdjInput.getText());
				if ((randTAdj > randSize-2) || (randTAdj <= 0)) {
					randTAdj = Math.round(randSize - 2);
					tAdjInput.setText( ( (Integer) (randSize - 2)).toString());
				}
				else if (randTAdj<2&&randSize>20) randTAdj=2; {
				tAdjInput.setText(((Integer) randTAdj).toString());
				return randTAdj;
				}

			} catch (NumberFormatException e) {
				randTAdj = Math.round(2);
				tAdjInput.setText(((Integer) randTAdj).toString());
				return randTAdj;
			}
		}
		public int parseSizeInt() {
			try {
				randSize = Integer.parseInt(sizeInput.getText());
				if (randSize > 1000) {
					sizeInput.setText("1000");
					randSize = 1000;
				}
				;
				if (randSize < 3) {
					sizeInput.setText("3");
					randSize = 3;
				}
				return randSize;
			} catch (NumberFormatException e) {
				sizeInput.setText("7");
				return 7;
			}
		}
		public double parseCap() {
			try {
				String pCap = capInput.getText();
				pCap = pCap.replace(",", ".");
				double maxCap = Double.parseDouble(pCap);
				if (maxCap < 0.0) {
					maxCap = -maxCap;
					capInput.setText(((Double)(maxCap)).toString());
					}
				else if (maxCap==0.0) {
					maxCap=(Double)1.0;
					capInput.setText("1.0");
				}
				if (maxCap-(double)Math.round(maxCap)>0) {	
					capInput.setText(((Double)maxCap).toString());
					return (Double) maxCap;
				}
				else {
					return (int) maxCap;
				}

			} catch (NumberFormatException e) {
				capInput.setText("10");
				return (Integer) 10;
			}
		}
		public void setStepText(LinkedList<JLabel> text) {
			this.stepExplainText = text;
			stepExplainContainer = new JPanel();
			stepExplainContainer.setPreferredSize(new Dimension(200, 500));
			stepExplainContainer.setLayout(new FlowLayout());
			for (JLabel stepTextLine : stepExplainText) {
				stepExplainContainer.add(stepTextLine);
			}
			controls.put("stepExplainText", stepExplainContainer);
		}
		Map<String, Object> controls = new LinkedHashMap<String, Object>();
		JButton stepButton, helpButton, newRandomButton, resetButton, createVertexButton, 
		createEdgeButton, removeElementButton, finishButton, eraseButton, saveFlowButton, loadFlowButton, resetLayoutButton;
		JTextField sizeInput, tAdjInput, capInput;
		JTextArea selectText;
		JTextPane flowValueText;
		public LinkedList<JLabel> stepExplainText;
		JComboBox<String> rightViewMenu;
		JComboBox<String> algoMenu;
		JCheckBox toggleVertexDetails;
		JSlider zoomSlider;
		JLabel stepCountLabel;
		
		
		public ControlPanel() {
			Dimension controlsSize = new Dimension(215, Toolkit.getDefaultToolkit().getScreenSize().height-100);
			this.setPreferredSize(controlsSize);
			this.setSize(controlsSize);
			this.setLayout(new FlowLayout());

			algoMenu = new JComboBox<String>();
			algoMenu.setPreferredSize(new Dimension(controlsSize.width-25, 25));
			algoMenu.addItem("Edmonds-Karp");
			algoMenu.addItem("Push-Relabel");
			algoMenu.addItemListener(algoItemListener);
			stepButton = new JButton("(S)tep");
			stepButton.setMultiClickThreshhold(20);
			stepButton.addActionListener(MainWindow.this);
			stepButton.setPreferredSize(new Dimension(controlsSize.width-25,25));
			stepCountLabel = new JLabel("Step ");
			Font labelFont = stepCountLabel.getFont();
			stepCountLabel.setFont(new Font(labelFont.getName(), Font.PLAIN, 20));
			JLabel rightViewMenuLabel = new JLabel("view: ");
			rightViewMenu = new JComboBox<String>();
			rightViewMenu.addItem("Flow");
			rightViewMenu.addItem("Residual graph");
			rightViewMenu.setSelectedIndex(0);
			rightViewMenu.setFocusable(false);
			rightViewMenu.addActionListener(MainWindow.this);
			rightViewMenu.setPreferredSize(new Dimension(140, 25));
			zoomSlider = new JSlider(JSlider.HORIZONTAL, 10, 100, 50);
			zoomSlider.setMajorTickSpacing(20);
			zoomSlider.setMinorTickSpacing(5);
			zoomSlider.setPaintTicks(true);
			zoomSlider.setPaintLabels(false);
			zoomSlider.addChangeListener(sliderListener);
			zoomSlider.setPreferredSize(new Dimension(170, 30));
			newRandomButton = new JButton("Generate");
			newRandomButton.setPreferredSize(new Dimension(controlsSize.width/2-5, 20));
			newRandomButton.addActionListener(MainWindow.this);
			newRandomButton.setMultiClickThreshhold(500);
			JLabel sizeInputLabel = new JLabel("graph size");
			sizeInputLabel.setPreferredSize(new Dimension(controlsSize.width/2-15, 25));
			sizeInput = new JTextField();
			sizeInput.setPreferredSize(new Dimension(controlsSize.width/2-15, 25));
			sizeInput.addKeyListener(randomEnterListener);
			JLabel tAdjInputLabel = new JLabel("t-connected");
			tAdjInputLabel.setPreferredSize(new Dimension(controlsSize.width/2-15, 25));
			tAdjInput = new JTextField("3");
			tAdjInput.setPreferredSize(new Dimension(controlsSize.width/2-15, 25));
			tAdjInput.addKeyListener(randomEnterListener);
			capInput = new JTextField("10");
			capInput.setPreferredSize(new Dimension(controlsSize.width/2-15, 25));
			capInput.addKeyListener(randomEnterListener);
			JLabel capInputLabel=new JLabel("max. cap.");
			capInputLabel.setPreferredSize(new Dimension(controlsSize.width/2-15, 25));
			createVertexButton = new JButton("add (V)ertex");
			JLabel tVDL = new JLabel("ID");
			tVDL.setPreferredSize(new Dimension(15,20));
			createVertexButton.setPreferredSize(new Dimension(controlsSize.width-75, 20));
			createVertexButton.addActionListener(MainWindow.this);
			toggleVertexDetails = new JCheckBox();
			toggleVertexDetails.setPreferredSize(new Dimension(20,20));
			toggleVertexDetails.setSelected(false);
			createEdgeButton = new JButton("add (E)dge");
			createEdgeButton.setPreferredSize(new Dimension(controlsSize.width-25, 20));
			createEdgeButton.addActionListener(MainWindow.this);
			removeElementButton = new JButton("Remove element (del)");
			removeElementButton.setPreferredSize(new Dimension(controlsSize.width-25, 20));
			removeElementButton.addActionListener(MainWindow.this);
			removeElementButton.setEnabled(false);
			flowValueText=new JTextPane();
			JPanel graphEditControls = new JPanel();
			eraseButton = new JButton("Erase");
			eraseButton.addActionListener(MainWindow.this);
			eraseButton.setPreferredSize(new Dimension(controlsSize.width-25,25));
			graphEditControls.setLayout(new FlowLayout());
			graphEditControls.setPreferredSize(new Dimension(190, 140));
			graphEditControls.add(tVDL);
			graphEditControls.add(toggleVertexDetails);
			graphEditControls.add(createVertexButton);
			graphEditControls.add(createEdgeButton);
			graphEditControls.add(removeElementButton);
			graphEditControls.add(eraseButton);
			loadFlowButton = new JButton("Load");
			saveFlowButton = new JButton("Save");
			saveFlowButton.setPreferredSize(new Dimension(controlsSize.width/2-5,20));
			loadFlowButton.setPreferredSize(new Dimension(controlsSize.width/2-5,20));
			loadFlowButton.addActionListener(MainWindow.this);
			saveFlowButton.addActionListener(MainWindow.this); 
			flowValueText.setText("Value: 0");
			flowValueText.setPreferredSize(new Dimension(controlsSize.width, 50));
			flowValueText.setOpaque(false);
			Font flowValueFont = flowValueText.getFont();
			flowValueText.setFont(new Font(flowValueFont.getName(), Font.PLAIN, 15));
			resetButton = new JButton("(R)eset");
			resetButton.addActionListener(MainWindow.this);
			resetButton.setPreferredSize(new Dimension(controlsSize.width-24,25));
			finishButton = new JButton("(F)inish");
			finishButton.addActionListener(MainWindow.this);
			finishButton.setPreferredSize(new Dimension(controlsSize.width-25,25));
			stepExplainText = new LinkedList<JLabel>();
			resetLayoutButton=new JButton("Reset Layout");
			resetLayoutButton.setPreferredSize(new Dimension(controlsSize.width-5,25));
			resetLayoutButton.addActionListener(MainWindow.this);
			helpButton=new JButton(" ? ");
			helpButton.addActionListener(MainWindow.this);
			helpButton.setPreferredSize(new Dimension(controlsSize.width/2-5,20));
			controls.put("algoMenu", algoMenu);
			controls.put("rightViewMenuLabel", rightViewMenuLabel);
			controls.put("rightViewMenu", rightViewMenu);
			JPanel zoom = new JPanel();
			JLabel minus = new JLabel("-");
			JLabel plus = new JLabel("+");
			plus.setVisible(true);
			minus.setVisible(true);
			zoom.add(minus);
			zoom.add(zoomSlider);
			zoom.add(plus);
			selectText = new JTextArea(" ");
			selectText.setOpaque(false);
			controls.put("zoomSlider", zoom);
			controls.put("stepButton", stepButton);
			controls.put("finishButton", finishButton);
			controls.put("resetButton", resetButton);
			controls.put("stepCountLabel", stepCountLabel);
			controls.put("graphEditControls", graphEditControls);
			controls.put("sizeInputLabel", sizeInputLabel);
			controls.put("sizeInput", sizeInput);
			controls.put("tAdjInputLabel", tAdjInputLabel);
			controls.put("tAdjInput", tAdjInput);
			controls.put("capInputLabel", capInputLabel);
			controls.put("capInput", capInput);
			controls.put("newRandomButton", newRandomButton);
			controls.put("helpButton", helpButton);
			controls.put("saveFlowButton", saveFlowButton);
			controls.put("loadFlowButton", loadFlowButton);
			controls.put("resetLayoutButton", resetLayoutButton);
			controls.put("flowValueText", flowValueText);
			controls.put("selectText", selectText);
			this.setVisible(true);
			show();
		}
		public void show() {
			stepCountLabel.setText("Step: " + algo.getStepCount());
			for (Object o : this.getComponents()) {
				this.remove((JComponent) o);
			}
			for (String i : controls.keySet()) {
				((JComponent) controls.get(i)).setVisible(true);
				this.add((JComponent) controls.get(i));
			}
			if (algo.hasNext()) {
				((JComponent) controls.get("stepButton")).setEnabled(true);
				((JComponent) controls.get("finishButton")).setEnabled(true);
			}
			// PushRelabel
			if (algoMenu.getSelectedIndex() == 1) {
				((JComponent) controls.get("rightViewMenu")).setVisible(false);
				((JComponent) controls.get("rightViewMenuLabel")).setVisible(false);
			}
			// EdmondsKarp
			else if (algoMenu.getSelectedIndex() == 0) {
				((JComponent) controls.get("rightViewMenu")).setVisible(true);
				((JComponent) controls.get("rightViewMenuLabel")).setVisible(true);
			}
		}
		
		public void finished() {
			double roundedFlow = Math.round(algo.getFlowValue() * 1000);
			roundedFlow = roundedFlow / 1000;
			String text = "Value: " + roundedFlow + "\n Maximum Flow!";
			flowValueText.setText(text);
			flowValueText.setVisible(true);
			stepButton.setEnabled(false);
			finishButton.setEnabled(false);
			panel2.scrollToT();
			setStepText(new LinkedList<JLabel>());
			selectText.setText("");
			SimpleAttributeSet set = new SimpleAttributeSet();
			StyleConstants.setAlignment(set,StyleConstants.ALIGN_CENTER);
			StyleConstants.setBold(set,true);
			flowValueText.setParagraphAttributes(set,true);  
			show();
			controlPanel.helpButton.grabFocus();
		}
		
		public void unfinish() {
			if (algo.getClass().getSimpleName().equals("EdmondsKarp")) {
				algo = (EdmondsKarp) algo;
			}
			else if (algo.getClass().getSimpleName().equals("PushRelabel")) {
				algo = (PushRelabel) algo;
			}
			finishButton.setEnabled(true);
			stepButton.setEnabled(true);
			flowValueText.setText("Value: 0");
			flowValueText.setVisible(true);
			setStepText(new LinkedList<JLabel>());
			selectText.setText("");
			((JLabel) controls.get("stepCountLabel")).setText("Step "
					+ ((Integer) algo.getStepCount()).toString());
			show();
			controlPanel.stepButton.grabFocus();
		}
		
		public void setStepCount(int c) {
			((JLabel)controls.get("stepCountLabel")).setText("Step "+c);
		}
	}
}
