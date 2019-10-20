package maxflow14.graph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/** This class models a graph
 *  it implementents the interface Serializable to store and load an graph*/
public class Graph implements Serializable {
	/** SerialVersionID to store the graph*/
	private final static long serialVersionUID=-6351573772353994112L;
	/**  _VertexList stores the vertices in a HashMap*/
	private Map<Integer, Vertex> _VertexList = new LinkedHashMap<Integer, Vertex>();
	
	// Die IDs der Knoten S und T werden vorgehalten (siehe addVertex) damit sie nicht gesucht werden müssen.
	// Wenn noch kein Knoten mit dem Label "s"/"S" bzw. "t"/"T" hinzugefügt
	// wurde, kommt "null" statt eines Knotens zurück.
	/**  The ID of the nodes s and t are stored explicitly, so you don't have to search for them in the _vertexList
	 * Whenever a node with a label "s" or "t" is created, this will show you the ID*/
	private Integer tId, sId = -1;
	/**  _AdjacencyMap stores the Edges of the gragh*/
	private Map<Integer, Map<Integer, Edge>> _AdjacencyMap = new LinkedHashMap<Integer, Map<Integer, Edge>>();
	/** Constructor: Creates a graph with zero nodes and an empty _AdjacencyMap  
	 * */
	public Graph() {
		_VertexList = new LinkedHashMap<Integer, Vertex>();
		_AdjacencyMap = new LinkedHashMap<Integer, Map<Integer, Edge>>();
		this.tId = -1;
		this.sId = -1;
	}
	/** Constructor: Creates a graph with all the nodes of g_in
	 * no edge will be taken over
	 * @param g_in all the nodes of g_in will be taken over
	 * */
	public Graph(Graph g_in) {
		// Ãœbernimmt die Knotenliste eines gegebenen Graphen (NICHT die Kanten)
		_VertexList = g_in.getVertexList();
		tId = g_in.tId;
		sId = g_in.sId;
		_AdjacencyMap = new LinkedHashMap<Integer, Map<Integer, Edge>>();
		for (int v : _VertexList.keySet()) {
			_AdjacencyMap.put(v, new HashMap<Integer, Edge>());
		}
		this.removeZeroEdges();
	}

	
	/** Adds a node to the graph
	 * @param v the new vertex
	 * */
	public void addVertex(Vertex v) {
		if (!_VertexList.containsKey(v.getId())) {
			this._VertexList.put(v.getId(), v);
			_AdjacencyMap.put(v.getId(), new LinkedHashMap<Integer, Edge>());
			if ((this.tId == -1)
					&& (v.getLabel().equals("t") | v.getLabel().equals("T"))) {
				this.tId = v.getId();
			} else if ((this.sId == -1)
					&& (v.getLabel().equals("s") | v.getLabel().equals("S"))) {
				this.sId = v.getId();
			}
		} else {
			JFrame frame = new JFrame();
			JOptionPane.showMessageDialog(frame,
					"Konnte Knoten nicht hinzufügen. Ein Knoten mit der ID "
							+ v.getId() + " ist bereits vorhanden.");
		}
	}
	/** Adds a node to the graph
	 * @param vid the id of the new vertex
	 * @param label the label of the new vertex
	 * */
	public void addVertex(int vid, String label) {
		addVertex(new Vertex(vid, label));
	}
	/** Adds a node to the graph, without a label
	 * @param vid the ID of the new vertex
	 * */
	public void addVertex(int vid) {
		addVertex(vid, ((Integer) vid).toString());
	}
	/** Returns the vertex corresponding to s
	 * @return the vertex "s"
	 * */
	public Vertex getS() {
		if (_VertexList.containsKey(tId)) 
			return _VertexList.get(sId);
		return null;
	}
	/** Returns the vertex corresponding to t
	 * @return the vertex "t"
	 * */
	public Vertex getT() {
		if (_VertexList.containsKey(tId)) 
			return _VertexList.get(tId);
		return null;
	}
	/** Is there an edge (u,v) in the graph?
	 * @param u the start of the edge to be checked
	 * @param u the start of the edge to be checked 
	 * @return the result of the check
	 * */
	
	public Boolean hasEdge(int u, int v) {
		// Methode prüft für Eingabe (u,v), ob eine Kante (u,v,*) von u nach v
		// bereits existiert
		if (this._AdjacencyMap.containsKey(u)) {
			if (this._AdjacencyMap.get(u).containsKey(v)
					&& this._AdjacencyMap.get(u).get(v).getWeight() > 0.0) {
				return true;
			} else
				return false;
		}
		return false;
	}
	/** Is there the node in the graph?
	 * @param v the ID of the node to be checked
	 * 
	 * @return the result of the check
	 * */
	public Boolean hasVertex(int v) {
		if (_VertexList.containsKey(v))
			return true;
		else
			return false;
	}
	/** Is there the node in the graph?
	 * @param v the node to be checked
	 * 
	 * @return the result of the check
	 * */
	public Boolean hasVertex(Vertex v) {
		if (_VertexList.containsKey(v.getId()))
			return true;
		else
			return false;
	}
	/** Removes the node with the ID vID 
	 * @param vId the ID of the vertex
	 * */
	public void removeVertex(int vId) {
		if (_VertexList.containsKey(vId)) {
			_VertexList.remove(vId);
			Set<Edge> toRemove = new HashSet<Edge>();
			for (int u:_AdjacencyMap.keySet()) {
				for (int v:_AdjacencyMap.get(u).keySet()) {
					if (u==vId||v==vId) {
						toRemove.add(_AdjacencyMap.get(u).get(v));
					}
				}
			}
			for (Edge e:toRemove) {
				if (_AdjacencyMap.containsKey(e.getStart())) {
					_AdjacencyMap.get(e.getStart()).remove(e.getEnd());
				}
			
			}
		}
	}
	/** Removes the node with the ID of v, after that v will be added
	 * @param v the new node
	 * */
	public void replaceVertex(Vertex v) {
		if (_VertexList.containsKey(v.getId()))
			_VertexList.remove(v.getId());
		// statt nur "put" dieser Aufruf, damit ggf. s/t aktualisiert wird
		addVertex(v);
	}
	/** Removes all edges with weight zero
	
	 * */
	public void removeZeroEdges() {
		Set<Edge> toRemove = new HashSet<Edge>();
		for (int u : _AdjacencyMap.keySet()) {
			for (int v : _AdjacencyMap.get(u).keySet()) {
				if (_AdjacencyMap.get(u).get(v).getWeight() <= 0.0) {
					toRemove.add(_AdjacencyMap.get(u).get(v));
				}
			}
		}
		for (Edge e : toRemove) {
			_AdjacencyMap.get(e.getStart()).remove(e.getEnd());
		}
	}
	/** Removes all edges
	 * 
	 * */
	public void eraseGraph() {
		// ALLE Kanten löschen
		this._AdjacencyMap.clear();
		this._VertexList.clear();
	}
	/** Is there an edge e in the graph?
	 * @param e the edge to be checked
	 * 
	 * @return the result of the check
	 * */
	public Boolean hasEdge(Edge e) {
		// Methode prüft für Eingabe (u,v,w), ob eine Kante (u,v,*) von u nach v
		// bereits existiert
		if (this._AdjacencyMap.containsKey(e.getStart())) {
			if (this._AdjacencyMap.get(e.getStart()).containsKey(e.getEnd())) {
				return true;
			} else
				return false;
		}
		return false;

	}

	//   !!! s und t sind hier ausgenommen
	/** Returns a random vertex disperate of "s" and "t"
	 * @return the random node
	 * */
	public Vertex getRandomVertex() {
		ArrayList<Vertex> vList = new ArrayList<Vertex>();
		for (int u : _VertexList.keySet()) {


			if ((this.getVertex(u).getId() != this.sId)
					&& (this.getVertex(u).getId() != this.tId)) {
				vList.add(this.getVertex(u));
			}
		}
		int v = (int) Math.round(Math.random() * (vList.size() - 1));
		if (vList.isEmpty()) {
			return null;
		}
		return vList.get(v);
	}
	
	/** Returns a random nodes  disperate of "s" and "t" of the Set toExclude
	 * @param toExclude the Set of nodes
	 * @return the random node
	 * */
	public Vertex getRandomVertex(Set<Vertex> toExclude) {
		Vertex v = getRandomVertex();
		if (toExclude.size() >= _VertexList.size() - 2)
			return null;
		while (toExclude.contains(v))
			v = getRandomVertex();
		return v;
	}
	/** Returns the ID of a random nodes  disperate of "s" and "t" of the Set toExclude
	 * @param toExclude the Set of nodes
	 * @return the ID of the random node
	 * */
	public Integer getRandomVertexId(Set<Integer> toExclude) {
		Vertex v = getRandomVertex();
		if (toExclude.size() >= _VertexList.size() - 2)
			return null;
		while (toExclude.contains(v.getId()))
			v = getRandomVertex();
		return v.getId();
	}
	/** Returns the sum of all edges
	 * 
	 * @return the sum of all edges
	 * */
	public int totalEdgeCount() {
		int ec = 0;
		for (int u : _AdjacencyMap.keySet()) {
			ec+=_AdjacencyMap.get(u).size();
		}
		return ec;
	}
	/** Returns a random edge  
	 * 
	 * @return the random edge
	 * */
	public Edge getRandomEdge() {
		// Hier sind s und t nicht ausgenommen
		ArrayList<Edge> edgeList = new ArrayList<Edge>();
		for (int u : _AdjacencyMap.keySet()) {
			for (int v : _AdjacencyMap.get(u).keySet()) {
				edgeList.add(_AdjacencyMap.get(u).get(v));
			}
		}
		int i = (int) Math.round(Math.random() * (edgeList.size() - 1));
		return edgeList.get(i);
	}
	/** Returns the heighest weight of all edges
	 * 
	 * @return the heighest weight
	 * */
	public double getMaxEdgeWeight() {
		double w = 0.0;
		for (int u : _AdjacencyMap.keySet()) {
			for (int v : _AdjacencyMap.get(u).keySet()) {
				if (_AdjacencyMap.get(u).get(v).getWeight() > w) {
					w = _AdjacencyMap.get(u).get(v).getWeight();

				}
			}
		}
		return w;
	}

	/** Returns the VertexList  
	 * 
	 * @return the list (map) of vertices
	 * */
	public Map<Integer, Vertex> getVertexList() {
		return this._VertexList;
	}
	/** Returns a node  
	 * @param id ID of the requested node
	 * @return the requested node
	 * */
	public Vertex getVertex(int id) {
		return this._VertexList.get(id);
	}
	/** Returns an edge  
	 * @param u the start of the requested edge
	 * @param v the end of the requested edge
	 * @return the requested edge
	 * */
	public Edge getEdge(Vertex u, Vertex v) {
		// Gibt Kante von Knoten u nach Knoten v zurück, falls vorhanden (sonst
		// null).
		if (this.hasEdge(new Edge(u.getId(), v.getId(), 1))) {
			return _AdjacencyMap.get(u.getId()).get(v.getId());
		}
		return null;
	}
	
	/** Returns an edge  
	 * @param u the ID of the start of the requested edge
	 * @param v the ID of the end of the requested edge
	 * @return the requested edge
	 * */
	public Edge getEdge(int u, int v) {
		if (_AdjacencyMap.containsKey(u) && _AdjacencyMap.get(u).containsKey(v)) {
			return _AdjacencyMap.get(u).get(v);
		} else
			return new Edge(0, 0, 0);
	}
	/** Removes an edge
	 * @param u the start of the requested edge
	 * @param v the end of the requested edge
	 
	 * */
	public void removeEdge(Vertex u, Vertex v) {
		// Löscht Kante von u nach v
		if (this._AdjacencyMap.containsKey(u.getId())) {
			if (this._AdjacencyMap.get(u.getId()).containsKey(v.getId())) {
				_AdjacencyMap.get(u.getId()).remove(v.getId());
			}
		}
	}
	/** Removes an edge  
	 * @param u the ID of the start of the requested edge
	 * @param v the ID of the end of the requested edge
	 
	 * */
	public void removeEdge(int u, int v) {
		// Löscht Kante von u nach v
		// System.out.println(u.getLabel());
		if (this._AdjacencyMap.containsKey(u)) {
			if (this._AdjacencyMap.get(u).containsKey(v)) {
				_AdjacencyMap.get(u).remove(v);
			}
		}
	}
	/** Returns a edge  
	 * @param e the edge to be removed
	
	 * */
	public void removeEdge(Edge e) {
		// Löscht Kante von u nach v
		// System.out.println(u.getLabel());
		if (this._AdjacencyMap.containsKey(e.getStart())) {
			if (this._AdjacencyMap.get(e.getStart()).containsKey(e.getEnd())) {
				_AdjacencyMap.get(e.getStart()).remove(e.getEnd());
			}
		}
	}
	/** Returns the edge map  
	
	 * @return the _AdjacencyMap
	 * */
	public Map<Integer, Map<Integer, Edge>> getEdgeList() {
		return this._AdjacencyMap;
	}

	/** The weights of all edges is scaled by d
	 * @param d new weight = old weights * d
	 * */
	public void scaleWeight(double d) {
		for (int u:_AdjacencyMap.keySet()) {
			for (int v:_AdjacencyMap.get(u).keySet()) {
				double w=_AdjacencyMap.get(u).get(v).getWeight();
				_AdjacencyMap.get(u).get(v).setWeight(w*d);
			}
		}
	}
	/** Adds an edge to the graph
	 * @param edge the new edge
	 * */
	public void addEdge(Edge edge) {
		if (!this.hasEdge(edge)) {
			if (!_AdjacencyMap.containsKey(edge.getStart())) _AdjacencyMap.put(edge.getStart(), new HashMap<Integer,Edge>());
			_AdjacencyMap.get(edge.getStart()).put(edge.getEnd(), edge);
		} 
		else {
			// Falls beim Hinzufügen von (u,v,x) eine Kante (u,v,w) schon
			// vorhanden ist, setzen wir nur w = w + x
			if (_AdjacencyMap.containsKey(edge.getStart())) {
				if (_AdjacencyMap.get(edge.getStart()).containsKey(
						edge.getEnd())) {
					double w = _AdjacencyMap.get(edge.getStart())
							.get(edge.getEnd()).getWeight();
					Edge newEdge = new Edge(edge.getStart(), edge.getEnd(), w
							+ edge.getWeight());
					_AdjacencyMap.get(edge.getStart()).put(edge.getEnd(),
							newEdge);
				}
			}
		}
	}
	/** Adds an edge to the graph
	 * @param uId vertex ID for edge start
	 * @param vId vertex ID for edge destination
	 * @param weight the weight of the new edge
	 * */
	public void addEdge(int uId, int vId, double weight) {
		Edge e = new Edge(uId, vId, weight);
		addEdge(e);
	}
	/** Adds an edge to the graph
	 * @param u vertex ID for edge start
	 * @param v vertex ID for edge destination
	 * @param weight the weight of the new edge
	 * */
	public void addEdge(Vertex u, Vertex v, double weight) {
		if (u==null||v==null) return;
		Edge e = new Edge(u.getId(), v.getId(), weight);
		addEdge(e);
	}
	
	
	/** returns the number of edges leading out of a vertex v
	 * */
	public int countOutEdges(Vertex v) {
		if (!_AdjacencyMap.containsKey(v))
			return 0;
		else {
			removeZeroEdges();
			return _AdjacencyMap.get(v.getId()).size();
		}
	}
	/** returns the number of edges leading into a vertex v
	 * */
	public int countIncEdges(Vertex v) {
		int count = 0;
		for (int u : _AdjacencyMap.keySet()) {
			for (int uu : _AdjacencyMap.get(u).keySet()) {
				if (uu == v.getId())
					count++;
			}
		}
		return count;
	}
	
	/** Returns the vertex with the given label
	 * @param label the label of the node to be searched
	 * @return the node with the given label
	 * */
	public Vertex searchVertex(String label) {
		for (int v:_VertexList.keySet()) {
			if (_VertexList.get(v).getLabel().equals(label)) return _VertexList.get(v);
		}
		return null;
	}
}
