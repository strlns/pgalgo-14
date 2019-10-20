package maxflow14.gui;

import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;

import maxflow14.algos.EdmondsKarp;
import maxflow14.algos.PushRelabel;
import maxflow14.graph.Edge;
import maxflow14.graph.Flow;
import maxflow14.graph.Graph;
import maxflow14.graph.Path;
import maxflow14.graph.Vertex;

import java.awt.Dimension;
import java.awt.FlowLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;
import com.mxgraph.view.mxStylesheet;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxPoint;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

/**
 * GraphPanel extends javax.swing.JPanel to contain an instance of JGraphX (canvas and graph). 
 * It allows allows some interaction with the graph and provides drawing functions for the flow graphs.
 * 
  
 @see maxflow14.gui.GraphPanel
 @see javax.swing.JPanel
 
 */
public class GraphPanel extends JPanel {
	private static final long serialVersionUID = (long) Math.random()
			* Integer.MAX_VALUE;
	private Map<Integer, Object> graphVertices;
	private Map<Integer, double[]> vertexPositions;
	private Map<Integer, Map<Integer, Object>> graphEdges;
	private double maxCap = 0.0;
	private Object defaultParent;
	private mxGraphComponent graphComponent;
	private mxGraph graph;
	private int sId, tId = -1;
	private mxGraphView view;

	private mxGraphLayout layout;

	public GraphPanel(Dimension graphSize) {
		graph = new mxGraph() {
			@Override
			public boolean isCellSelectable(Object cell) {
				if (cell != null) {
					if (cell instanceof mxCell) {
						mxCell myCell = (mxCell) cell;
						if (myCell.isVertex()) {
							myCell.setConnectable(false);
							return true;
						}
						if (myCell.isEdge()) {
							return true;
						}
						return false;
					}
				}
				return super.isCellSelectable(cell);
			}
		};

		graphEdges = new HashMap<Integer, Map<Integer, Object>>();
		vertexPositions = new HashMap<Integer, double[]>();
		graphVertices = new LinkedHashMap<Integer, Object>();
		graphComponent = new mxGraphComponent(graph);
		defaultParent = graph.getDefaultParent();
		graphComponent.setPreferredSize(new Dimension((int) graphSize
				.getWidth() - 10, (int) graphSize.getHeight() - 10));
		view = new mxGraphView(graph);
		view.setScale(1.0);
		graph.setView(view);
		this.setPreferredSize(graphSize);
		this.setSize(graphSize);
		this.setLayout(new FlowLayout());
		this.add(graphComponent);
		this.setVisible(true);
		graphComponent.setDragEnabled(false);
		graph.setAutoSizeCells(true);
		graph.setAllowNegativeCoordinates(true);
		graph.setCellsEditable(false);
		// graph.setEdgeLabelsMovable(true);
		graph.setAutoOrigin(true);
		graph.setDropEnabled(false);
		graph.setCellsMovable(true);
		graph.setCellsDisconnectable(false);
		graph.setLabelsVisible(true);
		graph.setCellsResizable(false);
		graph.setCellsBendable(true);
		graph.setCellsCloneable(false);
		graph.setAllowDanglingEdges(false);
	}

	public static int vertexDiameterST = 100;
	public static int vertexDiameter = 100;

	@Override
	public void resize(Dimension d) {
		graphComponent.setPreferredSize(new Dimension(d.width, d.height - 50));
		this.setPreferredSize(new Dimension(d.width, d.height));
	}

	public mxGraphLayout getGraphLayout() {
		return layout;
	}

	public mxGraphComponent getGraphComponent() {
		return graphComponent;
	}

	public void setZoom(double z) {
		view.setScale(z);
		graph.setView(view);
	}

	public void addChangeListener(mxIEventListener l) {
		graph.getModel().addListener(mxEvent.CHANGE, l);
	}

	public void addSelectListener(mxIEventListener l) {
		graph.addListener(mxEvent.SELECT, l);
	}

	public Map<Integer, Object> getGraphVertices() {
		return graphVertices;
	}

	public Map<Integer, Map<Integer, Object>> getGraphEdges() {
		return graphEdges;
	}

	// Zeichenmethode für Knoten (Edmonds-Karp)
	public void drawVertex(Vertex v) {

		graph.getModel().beginUpdate();
		Object v_;
		if (v.getLabel().toLowerCase().equals("s")) {
			v_ = graph.insertVertex(defaultParent, v.getLabel(), v.getLabel(),
					0, 0, vertexDiameterST, vertexDiameterST, "shape=ellipse;");
			graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, "#EFDE00",
					new Object[] { v_ });
			this.sId = v.getId();
		} else if (v.getLabel().toLowerCase().equals("t")) {
			v_ = graph.insertVertex(defaultParent, v.getLabel(), v.getLabel(),
					0, 0, vertexDiameterST, vertexDiameterST, "shape=ellipse;");
			graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, "#00FF00",
					new Object[] { v_ });
			this.tId = v.getId();

		} else {
			v_ = graph.insertVertex(defaultParent, v.getLabel(), v.getLabel(),
					0, 0, vertexDiameter, vertexDiameter, "shape=ellipse;");
			graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, "#FFFFFF",
					new Object[] { v_ });
		}
		mxCell cell = (mxCell) v_;
		cell.setConnectable(false);
		graph.setCellStyles(mxConstants.STYLE_FONTSIZE, "29",
				new Object[] { v_ });
		cell.setConnectable(false);
		graph.setCellStyles(mxConstants.STYLE_FONTSTYLE, ((Integer)mxConstants.FONT_BOLD).toString(),
				new Object[] { v_ });
		graphVertices.put(v.getId(), v_);
		refreshPositions();

		graph.getModel().endUpdate();
		if (v.getLabel().toLowerCase().equals("s"))
			this.sId = v.getId();
		else if (v.getLabel().toLowerCase().equals("t"))
			this.tId = v.getId();
	}

	public void drawEdge(Edge e) {
		drawEdge(e, 0);
	}

	// Zeichenmethode für Kanten (Edmonds-Karp)
	public void drawEdge(Edge e, int type) {

		graph.getModel().beginUpdate();
		String edgeId = e.getStart() + " " + e.getEnd() + " " + e.getWeight();
		edgeId = ((Integer) edgeId.hashCode()).toString();
		Object u = graphVertices.get(e.getStart());
		Object v = graphVertices.get(e.getEnd());
		double w = e.getWeight();
		w = w * 100;
		w = (double) (int) w;
		double truncWeight = w / 100;
		Object e_;

		if (e.getWeight() == (int) e.getWeight()) {
			e_ = graph.insertEdge(defaultParent, edgeId.toString(),
					(int) truncWeight, u, v, "strokeWidth=1;");
		} else {
			e_ = graph.insertEdge(defaultParent, edgeId.toString(),
					truncWeight, u, v, "strokeWidth=1;");
		}

		if (type == 0) {
			graph.setCellStyles(mxConstants.STYLE_FONTSIZE, "27",
					new Object[] { e_ });
			graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, "#383838",
					new Object[] { e_ });
			graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "#000000",
					new Object[] { e_ });
			graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "1",
					new Object[] { e_ });

		} else if (type == 1) {
			graph.setCellStyles(mxConstants.STYLE_FONTSIZE, "29",
					new Object[] { e_ });

			graph.setCellStyles(mxConstants.STYLE_FONTFAMILY, "title",
					new Object[] { e_ });

			graph.setCellStyles(mxConstants.STYLE_FONTSTYLE,
					((Integer) mxConstants.FONT_BOLD).toString(),
					new Object[] { e_ });
			graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, "#00AAFF",
					new Object[] { e_ });
			graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "#0000FF",
					new Object[] { e_ });
			graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH,
					getStrokeWidth(e).toString(), new Object[] { e_ });
		} else if (type == 2) {
			graph.setCellStyles(mxConstants.STYLE_FONTSIZE, "27",
					new Object[] { e_ });

			graph.setCellStyles(mxConstants.STYLE_FONTSTYLE,
					((Integer) mxConstants.FONT_BOLD).toString(),
					new Object[] { e_ });

			graph.setCellStyles(mxConstants.STYLE_FONTFAMILY, "serif",
					new Object[] { e_ });
			graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH,
					getStrokeWidth(e).toString(), new Object[] { e_ });
			graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, "#0000FF",
					new Object[] { e_ });
			graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "#000000",
					new Object[] { e_ });
		} else if (type == 3) {
			graph.setCellStyles(mxConstants.STYLE_FONTSIZE, "27",
					new Object[] { e_ });
			graph.setCellStyles(mxConstants.STYLE_FONTSTYLE,
					((Integer) mxConstants.FONT_BOLD).toString(),
					new Object[] { e_ });
			graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, "#00EEFF",
					new Object[] { e_ });
			graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "#4E4E4E",
					new Object[] { e_ });
		} else if (type == 4) {
			graph.setCellStyles(mxConstants.STYLE_FONTSIZE, "29",
					new Object[] { e_ });
			graph.setCellStyles(mxConstants.STYLE_FONTSTYLE,
					((Integer) mxConstants.FONT_BOLD).toString(),
					new Object[] { e_ });
			graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, "#FF229E",
					new Object[] { e_ });
			graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "#9E00FF",
					new Object[] { e_ });
		}
		if (graphEdges.containsKey(e.getStart()))
			graphEdges.get(e.getStart()).put(e.getEnd(), e_);
		else {
			graphEdges.put(e.getStart(), new HashMap<Integer, Object>());
			graphEdges.get(e.getStart()).put(e.getEnd(), e_);
		}
		graph.getModel().endUpdate();
	}

	public Integer getStrokeWidth(Edge e) {
		Double foo = maxCap;
		if (foo == 0.0)
			foo = 1.0;
		int w = (int) Math.round((e.getWeight() / foo) * 8 + 1);
		return w;
	}

	public mxGraph getGraph() {
		return graph;
	}

	// Zeichenmethode EdmondsKarp - Linke Ansicht (eingegebenes Flussnetzwerk)
	// mit
	// Angabe der Anzahl anzuzeigender Nachkommastellen der Kantengewichte
	public void drawGraph(Graph g, int digits, Map<Integer, double[]> vPositions) {
		graphVertices.clear();
		graphEdges.clear();
		for (int u : g.getVertexList().keySet()) {
			graphEdges.put(u, new HashMap<Integer, Object>());
		}
		graph.getModel().beginUpdate();
		graph.setResetEdgesOnMove(false);
		graphComponent.setWheelScrollingEnabled(true);
		this.eraseGraph();
		// Knoten
		for (int keyV : g.getVertexList().keySet()) {
			drawVertex(g.getVertexList().get(keyV));
		}

		// Kanten
		for (int u : g.getEdgeList().keySet()) {
			for (int v : g.getEdgeList().get(u).keySet()) {
				if (g.getEdgeList().get(u).get(v).getWeight() > 0) {
					drawEdge(g.getEdgeList().get(u).get(v), 0);

				}
			}
		}

		layoutGraph();
		graph.getModel().endUpdate();
		if (vPositions.size() > 0)
			setVertexPositions(vPositions);
		this.sId = g.getS().getId();
		this.tId = g.getT().getId();
	}

	// Zeichenmethode EdmondsKarp - Linke Ansicht (eingegebenes Flussnetzwerk)
	public void drawGraph(Graph g) {
		drawGraph(g, 3, new HashMap<Integer, double[]>());
	}

	// Zeichenmethode EdmondsKarp - Linke Ansicht (eingegebenes Flussnetzwerk)
	// mit
	// Angabe von Knotenpositionen
	public void drawGraph(Graph g, Map<Integer, double[]> vPositions) {
		drawGraph(g, 3, vPositions);
	}

	// Zeichenmethode für EdmondsKarp: Rechte Ansicht: Fluss
	public void drawRightEK_Flow(EdmondsKarp a) {
		Graph g = a.repeat().getFlowMap();
		Path highlightPath = a.currentPath();
		double maxCap = a.repeat().getBaseNetwork().getMaxEdgeWeight();
		graphVertices.clear();
		graphEdges.clear();
		for (int u : g.getVertexList().keySet()) {
			graphEdges.put(u, new HashMap<Integer, Object>());
		}
		graph.getModel().beginUpdate();
		graph.setResetEdgesOnMove(true);
		this.eraseGraph();
		graph.setAllowDanglingEdges(false);
		this.maxCap = maxCap;
		// Knoten
		for (int v : g.getVertexList().keySet()) {
			drawVertex(g.getVertexList().get(v));
		}
		// Kanten
		for (int u : g.getEdgeList().keySet()) {
			for (int v : g.getEdgeList().get(u).keySet()) {
				if (g.getEdgeList().get(u).get(v).getWeight() > 0) {
					if (highlightPath!=null&&highlightPath.containsEdgeFromTo(g.getEdgeList().get(u)
							.get(v))) {
						drawEdge(g.getEdgeList().get(u).get(v), 1);
					} else {
						drawEdge(g.getEdgeList().get(u).get(v), 2);
					}
				}
			}
		}
		graph.getModel().endUpdate();

	}

	// Zeichenmethode für EdmondsKarp: Rechte Ansicht: Restnetzwerk - Knoten
	public void drawRightEK_Residual(EdmondsKarp a) {
		if (a.getStepCount()==0) drawGraph(a.repeat().getBaseNetwork());
		drawRightEK_ResPart1(a.repeat());
		drawRightEK_ResPart2(a.repeat());
	}
	public void drawRightEK_ResPart1(Flow f) {

		this.eraseGraph();
		graphEdges.clear();

		for (int u : f.getBaseNetwork().getVertexList().keySet()) {
			graphEdges.put(u, new HashMap<Integer, Object>());
		}
		graph.getModel().beginUpdate();

		graph.setResetEdgesOnMove(false);

		Graph g = f.getResidualNetwork();

		// Knoten
		for (int keyV : g.getVertexList().keySet()) {
			drawVertex(g.getVertexList().get(keyV));
		}

	}

	// Zeichenmethode für EdmondsKarp: Rechte Ansicht: Restnetzwerk - Kanten
	public void drawRightEK_ResPart2(Flow f) {

		Graph g = f.getResidualNetwork();

		HashSet<Edge> resEdges1 = f.getResidualEdges()[0];
		HashSet<Edge> resEdges2 = f.getResidualEdges()[1];

		// Kanten
		for (int u : g.getEdgeList().keySet()) {
			for (int v : g.getEdgeList().get(u).keySet()) {
				Edge e = g.getEdgeList().get(u).get(v);

				if (e.getWeight() > 0) {
					if (resEdges1.contains(e)) {
						drawEdge(g.getEdgeList().get(u).get(v), 0);
					} else if (resEdges2.contains(e)) {
						drawEdge(g.getEdgeList().get(u).get(v), 4);
					}
				}
			}

		}
		graph.getModel().endUpdate();
		graph.repaint();

	}

	// Zeichenmethode für PushRelabel
	public void drawFlowPushRelabel(PushRelabel a) {
		Map<Integer, Integer> vertexHeight = a.getVertexHeight();
		Map<Integer, Double> vertexExcess = a.getVertexExcess();
		Stack<Integer> vertexActive = a.getVertexActive();
		Flow f = a.repeat();
		eraseGraph();
		for (int u : f.getBaseNetwork().getVertexList().keySet()) {
			graphEdges.put(u, new HashMap<Integer, Object>());
		}
		if (f.getBaseNetwork().getVertexList().size() < 1)
			return;
		this.sId=f.getBaseNetwork().getS().getId();
		this.tId=f.getBaseNetwork().getT().getId();
		graph.getModel().beginUpdate();
		// Knoten
		for (int v : f.getBaseNetwork().getVertexList().keySet()) {
			if (!vertexActive.empty() && vertexActive.peek() == v) {
				drawVertexActive2_GT(f.getBaseNetwork().getVertex(v), f
						.getBaseNetwork().getVertex(v).getLabel()
						+ "\n"
						+ vertexExcess.get(v).toString()
						+ "\n h "
						+ vertexHeight.get(v));
			} else if (vertexActive.contains(v)) {
				drawVertexActive_GT(f.getBaseNetwork().getVertex(v), f
						.getBaseNetwork().getVertex(v).getLabel()
						+ "\n"
						+ vertexExcess.get(v).toString()
						+ "\n h "
						+ vertexHeight.get(v));
			} else if (v == f.getBaseNetwork().getS().getId())
				drawVertexQuelle_GT(f.getBaseNetwork().getVertex(v), f
						.getBaseNetwork().getVertex(v).getLabel()
						+ "\n h " + vertexHeight.get(v));
			else if (v == f.getBaseNetwork().getT().getId())
				drawVertexSenke_GT(f.getBaseNetwork().getVertex(v), f
						.getBaseNetwork().getVertex(v).getLabel()
						+ "\n"
						+ vertexExcess.get(v).toString()
						+ "\n h "
						+ vertexHeight.get(v));
			else {
				if (vertexExcess.containsKey(v) && vertexHeight.containsKey(v))
					drawVertexPassiv_GT(f.getBaseNetwork().getVertex(v), f
							.getBaseNetwork().getVertex(v).getLabel()
							+ "\n ex "
							+ vertexExcess.get(v).toString()
							+ "\n h " + vertexHeight.get(v).toString());
				else if (vertexHeight.containsKey(v))
					drawVertexPassiv_GT(f.getBaseNetwork().getVertex(v), f
							.getBaseNetwork().getVertex(v).getLabel()
							+ "\n ex 0"
							+ "\n h "
							+ vertexHeight.get(v).toString());
				else
					drawVertexPassiv_GT(f.getBaseNetwork().getVertex(v), f
							.getBaseNetwork().getVertex(v).getLabel()
							+ "\n ex 0" + "\n h " + "-");
			}

		}

		// Kanten
		for (int u : f.getBaseNetwork().getEdgeList().keySet()) {
			for (int v : f.getBaseNetwork().getEdgeList().get(u).keySet()) {
				Edge e = f.getBaseNetwork().getEdge(u, v);
				if (f.getFlowMap().hasEdge(e)) {
					drawEdgePushRelabel(e,
							f.getFlowMap().getEdge(e.getStart(), e.getEnd())
									.getWeight());
				} else
					drawEdgePushRelabel(e, 0);
			}
		}
		graph.getModel().endUpdate();
		refreshPositions();
	}

	// Zeichenmethode für PushRelabel: Kante
	public void drawEdgePushRelabel(Edge e, double flow) {
		graphComponent.getGraph().setCellsBendable(true);
		graph.getModel().beginUpdate();
		String edgeId;
		if (Math.round(e.getWeight()) - e.getWeight() > 0) {
			edgeId = e.getStart() + " " + e.getEnd() + " " + e.getWeight();
		} else
			edgeId = e.getStart() + " " + e.getEnd() + " "
					+ ((int) Math.round(e.getWeight()));
		edgeId = ((Integer) edgeId.hashCode()).toString();

		double fd = flow;
		fd = fd * 100;
		fd = (double) (int) fd;
		double truncFlow = fd / 100;
		
		double w = e.getWeight();
		w = w * 100;
		w = (double) (int) w;
		double truncWeight = w / 100;
		Object e_;

		Object u = graphVertices.get(e.getStart());
		Object v = graphVertices.get(e.getEnd());
		if (e.getWeight() == (int) e.getWeight()) {
			e_ = graph.insertEdge(defaultParent, edgeId.toString(),
					(int) truncFlow + " / " + (int) truncWeight, u, v, "strokeWidth=1;");
		} else {
			e_ = graph.insertEdge(defaultParent, edgeId.toString(),
					truncFlow + " / " + truncWeight, u, v, "strokeWidth=1;");
		}
		
		if (flow > 0) {
			graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, "#0000FF",
					new Object[] { e_ });
		}
		graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "3",
				new Object[] { e_ });
		graph.getModel().endUpdate();

		graphEdges.get(e.getStart()).put(e.getEnd(), e_);

	}

	// Zeichenmethode für PushRelabel: Knoten aktiv
	public void drawVertexActive_GT(Vertex v, String label) {
		mxCell v_ = (mxCell) graph.insertVertex(defaultParent, v.getLabel(),
				label, 0, 0, vertexDiameter, vertexDiameter,
				"shape=ellipse;fillColor=#FF2222;");
		v_.setConnectable(false);
		graph.setCellStyles(mxConstants.STYLE_FONTSIZE, "23",
				new Object[] { v_ });
		graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "#FFFFFF",
				new Object[] { v_ });
		graphVertices.put(v.getId(), v_);
		refreshPositions();
	}

	// Zeichenmethode für PushRelabel: Knoten aktiv und "im Fokus" (auf dem
	// Stack ganz oben)
	public void drawVertexActive2_GT(Vertex v, String label) {
		mxCell v_ = (mxCell) graph.insertVertex(defaultParent, v.getLabel(),
				label, 0, 0, vertexDiameter, vertexDiameter,
				"shape=ellipse;fillColor=#FF22FF;");
		v_.setConnectable(false);
		graph.setCellStyles(mxConstants.STYLE_FONTSIZE, "23",
				new Object[] { v_ });
		graph.setCellStyles(mxConstants.STYLE_FONTSTYLE,
				((Integer) mxConstants.FONT_BOLD).toString(),
				new Object[] { v_ });
		graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "#FFFFFF",
				new Object[] { v_ });
		graphVertices.put(v.getId(), v_);
		refreshPositions();
	}

	// Zeichenmethode für PushRelabel: Knoten Quelle
	public void drawVertexQuelle_GT(Vertex v, String label) {
		mxCell v_ = (mxCell) graph.insertVertex(defaultParent, v.getLabel(),
				label, 0, 0, vertexDiameterST, vertexDiameterST,
				"shape=ellipse;fillColor=#EFDE00;");
		v_.setConnectable(false);
		graph.setCellStyles(mxConstants.STYLE_FONTSIZE, "23",
				new Object[] { v_ });
		graphVertices.put(v.getId(), v_);
		refreshPositions();
	}

	// Zeichenmethode für PushRelabel: Knoten Senke
	public void drawVertexSenke_GT(Vertex v, String label) {
		mxCell v_ = (mxCell) graph.insertVertex(defaultParent, v.getLabel(),
				label, 240, 150, vertexDiameterST, vertexDiameterST,
				"shape=ellipse;fillColor=#00FF00;");
		v_.setConnectable(false);
		graph.setCellStyles(mxConstants.STYLE_FONTSIZE, "23",
				new Object[] { v_ });
		graphVertices.put(v.getId(), v_);
		refreshPositions();
	}

	// Zeichenmethode für PushRelabel: Knoten passiv
	public void drawVertexPassiv_GT(Vertex v, String label) {
		mxCell v_ = (mxCell) graph.insertVertex(defaultParent, v.getLabel(),
				label, 240, 150, vertexDiameter, vertexDiameter,
				"shape=ellipse;fillColor=white;");
		v_.setConnectable(false);
		graph.setCellStyles(mxConstants.STYLE_FONTSIZE, "23",
				new Object[] { v_ });
		graphVertices.put(v.getId(), v_);
	}

	public void layoutGraph() {
		layoutGraph(false);
	}

	public void layoutGraph(boolean reLayout) {

		Map<Integer, double[]> vPos = new HashMap<Integer,double[]>();
		if (!reLayout&&graphVertices.size()<500) {
		
		graph.getModel().beginUpdate();
		// Knotenpositionen werden erstmal mit TreeLayout vorlayoutet

		mxGraphLayout layout2 = new mxCompactTreeLayout(graph);
		((mxCompactTreeLayout) layout2).setHorizontal(true);
		((mxCompactTreeLayout) layout2).setEdgeRouting(true);
		((mxCompactTreeLayout) layout2).setLevelDistance(100);
		((mxCompactTreeLayout) layout2).setNodeDistance(150);
		graph.setKeepEdgesInBackground(true);
		layout2.execute(defaultParent);
		graph.getModel().endUpdate();
		refreshPositions();
		vPos.putAll(getVertexPositions());
		}
		graph.getModel().beginUpdate();
		layout = new mxFastOrganicLayout(graph);
		((mxFastOrganicLayout) layout).setForceConstant(600);
		int E = 0;
		for (int foo : graphEdges.keySet()) {
			E += graphEdges.get(foo).size();
		}
		((mxFastOrganicLayout) layout).setMaxIterations(E + 2);
		((mxFastOrganicLayout) layout).getGraph()
				.setKeepEdgesInBackground(true);

		layout.execute(defaultParent);
		if (!reLayout) {
			setVertexPositions(vPos);
		}
		
		for (int v_id : graphVertices.keySet()) {
			mxGeometry v_geometry = ((mxCell) graphVertices.get(v_id))
					.getGeometry();
			double[] v_coordinates = { v_geometry.getX(), v_geometry.getY() };
			vertexPositions.put(v_id, v_coordinates);
		}

		Map<String, Object> edge = new HashMap<String, Object>();
		edge.put(mxConstants.STYLE_ROUNDED, true);
		edge.put(mxConstants.STYLE_FONTSIZE, "25");
		edge.put(mxConstants.STYLE_EDGE, mxEdgeStyle.routePatterns);
		edge.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
		edge.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
		edge.put(mxConstants.STYLE_STROKECOLOR, "#848484");
		mxStylesheet edgeStyle = new mxStylesheet();
		edgeStyle.setDefaultEdgeStyle(edge);
		graph.setStylesheet(edgeStyle);
		graph.getModel().endUpdate();
		graphComponent.refresh();

	}

	// Diese Methode aktualisiert für alle Knoten deren gespeicherte Position
	public void refreshPositions() {
		vertexPositions.clear();
		for (int v_id : graphVertices.keySet()) {
			mxGeometry v_geometry = ((mxCell) graphVertices.get(v_id))
					.getGeometry();
			double[] v_coordinates = { v_geometry.getX(), v_geometry.getY() };
			vertexPositions.put(v_id, v_coordinates);
		}
	}

	// Gibt für eine bestimmte Kante deren Kontrollpunkte zurück
	public LinkedList<mxPoint> getEdgePoints(int u, int v) {
		if (graphEdges.containsKey(u) && graphEdges.get(u).containsKey(v)) {
			mxGeometry mg = (mxGeometry) ((mxCell) graphEdges.get(u).get(v))
					.getGeometry().clone();
			LinkedList<mxPoint> points_ = new LinkedList<mxPoint>();
			if (mg.getPoints() == null)
				return null;
			else {
				points_.addAll(mg.getPoints());
				return points_;
			}
		} else
			return null;
	}

	// Gibt eine zweidimensionale HashMap (StartID+EndID) Kanten-Kontrollpunkten
	// zurück
	public Map<Integer, Map<Integer, LinkedList<mxPoint>>> getAllEdgePoints() {
		Map<Integer, Map<Integer, LinkedList<mxPoint>>> ep = new HashMap<Integer, Map<Integer, LinkedList<mxPoint>>>();
		for (int u : graphEdges.keySet()) {
			ep.put(u, new HashMap<Integer, LinkedList<mxPoint>>());
			for (int v : graphEdges.get(u).keySet()) {
				if (getEdgePoints(u, v) != null)
					ep.get(u).put(v, getEdgePoints(u, v));
			}
		}
		return ep;
	}

	public void makeEdgesMovable() {
		// Kanten bekommen einen Kontrollpunkt zum Bewegen in der Mitte
		// oder werden auseinandergezogen falls symmetrisch (u<=>v)
		graph.getModel().beginUpdate();
		refreshPositions();
		for (int u : graphEdges.keySet()) {
			for (int v : graphEdges.get(u).keySet()) {
				if (vertexPositions.containsKey(u)
						&& vertexPositions.containsKey(v)) {
					Object e_ = graphEdges.get(u).get(v);
					LinkedList<mxPoint> points = new LinkedList<mxPoint>();
					mxGeometry mg = (mxGeometry) graph.getCellGeometry(e_)
							.clone();
					mg.setRelative(false);
					double[][] rot2 = { { 0, -1 }, { 1, 0 } };
					double[] endPoint = {
							vertexPositions.get(v)[0] + vertexDiameter / 2,
							vertexPositions.get(v)[1] + vertexDiameter / 2 };
					double[] startPoint = {
							vertexPositions.get(u)[0] + vertexDiameter / 2,
							vertexPositions.get(u)[1] + vertexDiameter / 2 };
					double[] center = { (endPoint[0] + startPoint[0]) / 2,
							(endPoint[1] + startPoint[1]) / 2 };

					// Doppelkanten auseinanderziehen, damit sie nicht
					// aufeinander liegen
					if (graphEdges.containsKey(v)
							&& graphEdges.get(v).containsKey(u)) {
						double[] vector_edge = { endPoint[0] - startPoint[0],
								endPoint[0] - startPoint[0] };
						double norm_edge = Math.sqrt(Math
								.pow(vector_edge[0], 2)
								+ Math.pow(vector_edge[1], 2));
						double[] vector_orth = { 0, 0 };
						for (int i = 0; i < 2; i++) {
							for (int j = 0; j < 2; j++) {
								vector_orth[i] = vector_orth[i]
										+ vector_edge[i] / norm_edge
										* rot2[i][j] * 100;
							}
						}
						center[0] = center[0] + vector_orth[0];
						center[1] = center[1] + vector_orth[1];
					}
					points.add(new mxPoint(center[0], center[1]));
					mg.setRelative(true);
					mg.setX(0);
					mg.setY(30);
					if (
							!((Double)points.getFirst().getX()).isNaN()&&
							!((Double)points.getFirst().getY()).isNaN()
									)
					mg.setPoints(points);
					mxCell cell = (mxCell) e_;
					cell.setGeometry(mg);}
				
			}
		}
		graph.getModel().endUpdate();
		graph.refresh();
	}

	// Graph löschen
	public void eraseGraph() {
		graph.getModel().beginUpdate();
		graph.removeCells(graph.getChildVertices(graph.getDefaultParent()));
		graph.getModel().endUpdate();
		graphVertices.clear();
		graphEdges.clear();
	}

	// Knoten entfernen
	public void removeVertex(int id) {
		if (!graphVertices.containsKey(id))
			return;
		graph.getModel().beginUpdate();
		graph.removeCells(new Object[] { graphVertices.get(id) });
		graph.getModel().endUpdate();
		graphVertices.remove(id);
		refreshPositions();
	}

	// Kante entfernen
	public void removeEdge(int u, int v) {
		if (!graphEdges.containsKey(u))
			return;
		if (!graphEdges.get(u).containsKey(v))
			return;

		graph.getModel().beginUpdate();
		graph.removeCells(new Object[] { graphEdges.get(u).get(v) });
		graph.getModel().endUpdate();
		graphEdges.get(u).remove(v);
	}

	// Zu einem mxCell-Objekt, falls es eine Kante in graphEdges ist, die IDs
	// von Start- (u) und Zielknoten (v) herausfinden
	public int[] getEdgeUV(mxCell m) {
		for (int u : graphEdges.keySet()) {
			for (int v : graphEdges.get(u).keySet()) {
				if (graphEdges.get(u).get(v).equals(m)) {
					int[] uv = { u, v };
					return uv;
				}
			}
		}
		return null;
	}

	// mxCell zurückgeben - Knoten
	public mxCell returnVertex(int id) {
		return (mxCell) graphVertices.get(id);
	}

	// Map mit Knotenpositionen auslesen
	public Map<Integer, double[]> getVertexPositions() {
		refreshPositions();
		return vertexPositions;
	}

	// Knoten relativ verschieben
	public void moveVertexBy(int id, double[] offset) {
		if (graphVertices.containsKey(id)) {
			Object v_ = graphVertices.get(id);
			mxCell cell = (mxCell) v_;
			graph.moveCells(new Object[] { cell }, offset[0], offset[1]);
		} else
			return;
	}

	// Knoten absolut verschieben
	public void moveVertex(int id, double[] pos) {
		if (graphVertices.containsKey(id)) {
			Object v_ = graphVertices.get(id);
			mxCell cell = (mxCell) v_;
			mxGeometry ge = (mxGeometry) graph.getCellGeometry(cell).clone();
			ge.setX(pos[0]);
			ge.setY(pos[1]);
			graph.getModel().setGeometry(cell, ge);
		} else
			return;
	}

	// Position für mehrere Knoten absolut setzen
	public void setVertexPositions(Map<Integer, double[]> vPos_) {
		graph.getModel().beginUpdate();
		graphComponent
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		graphComponent
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		for (int v_id : vPos_.keySet()) {
			mxCell vCell = (mxCell) graphVertices.get(v_id);
			if (vCell == null)
				break;
			mxGeometry v_g = (mxGeometry) vCell.getGeometry().clone();
			v_g.setX(vPos_.get(v_id)[0]);
			v_g.setY(vPos_.get(v_id)[1]);
			graph.getModel().setGeometry(vCell, v_g);
			vCell.setGeometry(v_g);
			graphVertices.put(v_id, vCell);
		}
		graph.getModel().endUpdate();
		vertexPositions.putAll(vPos_);
	}

	// Ansicht nach S bzw. T scrollen
	public void scrollToS() {
		if (tId != -1)
			graphComponent.scrollCellToVisible(graphVertices.get(sId));
	}

	public void scrollToT() {
		if (tId != -1)
			graphComponent.scrollCellToVisible(graphVertices.get(tId));
	}

	// Ansicht zu einem Knoten scrollen
	public void scrollToVertex(int vid) {
		if (!graphVertices.containsKey(vid))
			return;
		graphComponent.scrollCellToVisible(graphVertices.get(vid));
	}

	// Für mehrere Kanten die Eckpunkte setzen
	// Eingabe: 2-dim. Map (int u, int v) -> List<mxPoint> edgePoints
	public void setEdgePoints(
			Map<Integer, Map<Integer, LinkedList<mxPoint>>> edgePoints) {
		for (int u : edgePoints.keySet()) {
			for (int v : edgePoints.get(u).keySet()) {
				if ((edgePoints.get(u).get(v) != null)
						&& graphEdges.containsKey(u)
						&& graphEdges.get(u).containsKey(v)
						&& ((mxCell) graphEdges.get(u).get(v)).getGeometry() != null
						&& edgePoints.get(u).get(v).getFirst().getX()!=0)
				{
					((mxCell) graphEdges.get(u).get(v)).getGeometry()
							.setPoints(edgePoints.get(u).get(v));
				}	
			}
		}
		graph.refresh();
	}
	
}
