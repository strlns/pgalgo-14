package maxflow14.algos;


import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import maxflow14.graph.Edge;
import maxflow14.graph.Flow;
import maxflow14.graph.Graph;


/**
 * This class models the Push Relabel algorithm.
 * It implements the interface Algo.java
 * @see maxflow14.algos.Algo
 */

public class PushRelabel implements Algo {
	 /** SerialVersionID identifies this class for storage
	  * @see maxflow14.util.FileIO */
	private final static long serialVersionUID = -5948406248704789477L;
	/** The algorithm is executed on this graph*/
	private Graph g;
	/** The current flow network*/
	private Flow f;
	/** vertexHeight stores to each node its height in a hash map*/
	private Map<Integer, Integer> vertexHeight;			
	/** vertexExcess stores to each node its excess in a hash map*/
	private Map<Integer, Double> vertexExcess;			
	/** reverseEdge is an internal field that contains a set of nodes with an edge to it for each node in the graph.
	 *  Using reverseEdge, the algorithm creates a "local" residual network for each nodes neighbors. */
	private Map<Integer, Set<Integer>> reverseEdge; 	
	/** vertexActive is a stack of active vertices*/
	private Stack<Integer> vertexActive;		
	
	private boolean fertig = false;			
	
	private int stepCount = 0;
	
	/** Constructor: Initializes the algorithm. For a given graph g,
	 * a flow with value zero on all edges is set, and the internal 
	 * data structures (height, excess etc) are being prepared.
	 * @param g Graph (flow network) to run the algorithm on.
	 * */
	public PushRelabel(Graph g) {
		//Initialisieren der Datenstrukturen
		this.g = g;
		this.f = new Flow(g);
		this.vertexHeight = new LinkedHashMap<Integer, Integer>();
		this.vertexExcess = new LinkedHashMap<Integer, Double>();
		this.vertexActive = new Stack<Integer>();
		this.reverseEdge = new LinkedHashMap<Integer,Set<Integer>>();
		for( int u: g.getVertexList().keySet()){
			reverseEdge.put(u, new HashSet<Integer>());
		}
		for (int u: g.getEdgeList().keySet()){
			for(int v : g.getEdgeList().get(u).keySet()){
				reverseEdge.get(v).add(u);
			}
		}
		reset();
	}
   
	public Map<Integer, Integer> getVertexHeight() {
		return vertexHeight;
	}

	public Map<Integer, Double> getVertexExcess() {
		return vertexExcess;
	}

	public Stack<Integer> getVertexActive() {
		return vertexActive;
	}
	/** Resets the algorithm to the initial state */
	public void reset() {
		stepCount = 0;
		vertexHeight.clear();
		vertexExcess.clear();
		vertexActive.clear();
		reverseEdge.clear();
		
		for( int u: g.getVertexList().keySet()){
			reverseEdge.put(u, new HashSet<Integer>());
		}
		for (int u: g.getEdgeList().keySet()){
			for(int v : g.getEdgeList().get(u).keySet()){
				reverseEdge.get(v).add(u);
			}
		}
	
		// Setzte Höhe von allen Knoten auf 0, ausser s auf |V| 
		// setze Excess auf 0  für  alle Knoten
		for (int u : g.getVertexList().keySet()) {

			if (u == g.getS().getId()) {
				vertexHeight.put(u, g.getVertexList().size());
			} 
			else {
				vertexHeight.put(u, 0);
			}
			
			vertexExcess.put(u, 0.0);
		}
		
		// Starte mit 0-Fluss
		f.setZeroFlow();
	}
	/** Implements Algo.hasNext()
	 * @see Algo
	 * @return boolean that tells if another step is possible
	 * */
	public boolean hasNext() {
		return !this.fertig;
	}
	
	/** Sets flow values on outgoing edges of s to the capacity of the edges.
	 * @return Flow after step was carried out*/
	private Flow firstStep(){
		
		
		int sID = g.getS().getId();
		for (Edge e : g.getEdgeList().get(sID).values()) {
			f.increaseFlow(sID, e.getEnd(), e.getWeight());
			vertexExcess.put(e.getEnd(), e.getWeight());
			if (e.getEnd() != g.getT().getId())
				//wenn nicht direkt nach t gepusht wird, so wird der Knoten Aktiv
				vertexActive.push(e.getEnd());
		}
		stepCount++;
		return f;
	}
	/** Determines a valid edge from the vertex with the given id, or indicates that there is no such edge
	 * @param avID ID of an active node
	 * @return 2-digit array with destination node of the allowed edge and the value of the edge, error code -1: No valid edge available
	 * */
	private double[] validEdge(int avID){
		double[] erge = new double[2];  //erge[0] zielknoten
										//erge[1] pushvalue
										//bei erge[0] == -1 wurde keine erlaubte kante gefunden
		int avHeight = vertexHeight.get(avID);
		
		//suche nach Kanten die im Graph drin sind (vorwärtskanten)
		for (int u : g.getEdgeList().get(avID).keySet()) {
			if ( ( f.edgeInRest(avID, u)   )&& (vertexHeight.get(u) == avHeight - 1)) {
				// erlaubte Kante zwischen av und u
				erge[0] = u;
				erge[1] = f.edgeInRestValue(avID, u);
				return erge;
			}
		}
		
		//suche nach Rückwärtskanten (im Restgraph)
		for (int v:reverseEdge.get(avID)){
			if ((f.isFlow(v, avID)) && ( vertexHeight.get(v) == avHeight - 1) ){
				erge[0] = v;
				erge[1] = f.getFlow(v, avID);
				return erge;
			}
		}
		erge[0] = -1;
		return erge;
	}
	/** Increases the active vertex to a minimal value, so that afterwards there is a valid edge
	 * @param avID ID of an active vertex
	 * */
	private void relabel(int avID){
		int avHeight = vertexHeight.get(avID);   		//Höhe von avID
		int min = g.getVertexList().size() * 2 + 1;		//setze min auf hinreichend groÃŸ ( höher als Max höhe, also 2*|V|)
		//gehe alle benachbarten Knoten durch
		//vorwärtskanten
		for (int u : g.getEdgeList().get(avID).keySet()) {
			if ( ( f.edgeInRest(avID, u)) && (vertexHeight.get(u) <= min) && (vertexHeight.get(u) >= avHeight)) {
				min = vertexHeight.get(u);
			}
		}
		//rückwärtskanten
		for (int v:reverseEdge.get(avID)){
			if ( (f.isFlow(v, avID)) && (vertexHeight.get(v) <= min) && (vertexHeight.get(v) >= avHeight)) {
				min = vertexHeight.get(v);
			}
		}
		//setze Höhe von avID auf min+1
		min++;
		vertexHeight.put(avID, min);
	}
	/** Changes the flow in the network on edge (avID, zielknoten) by the given value. If only the inverted edge (zielknoten, avID) 
	 * exists in the original flow network, the flow is decreased, else increased.   
	 * @param avID ID of an active vertex
	 * @param zielknoten ID of the destination node of a valid edge
	 * @param value Flow is increased by value*/
	public void push(int avID, int zielknoten, double value) {
		// pushe, wenn wir im Restnetzwerk von avid nach zielknoten pushen
		// wollen
		if ((f.getBaseNetwork().getEdgeList().get(avID) != null)
				&& (f.getBaseNetwork().getEdgeList().get(avID).get(zielknoten) != null)) {
			if ((f.getBaseNetwork().getEdgeList().get(avID).get(zielknoten)
					.getWeight() - f.getFlow(avID,zielknoten)) >= value) {
				f.increaseFlow(avID, zielknoten, value);
				return;
			}
		}
		f.decreaseFlow(zielknoten, avID, value);
		return;

	}
	/** Implements the next() function of the interface Algo
	 *  A step of the Algorithm is performed, i.e
	 *  -get an active Vertex of the Stack
	 *  -determine a valid edge
	 *  - PUSH if there is such a valid edge or RELABEL if not
	 *  @return Flow after this step*/
	public Flow next() {
		//Erster Schritt (pushe alle ausgehenden Kanten von s maximal, Excess und Active anpassung an den entsprechenden Knoten
		if (stepCount == 0) return firstStep();
			
		//Nach erstem Schritt
		stepCount++;
	
		
		// bestimme einen aktiven Knoten
		if (vertexActive.empty()) {
			// es gibt keinen aktiven Knoten -> fertig
			this.fertig = true;
			return f;
		}
		int avID = vertexActive.peek(); 			// av activeVertex
		
		// gucke, ob es erlaubte Kante gibt im residualgraph gibt
		
		double[] hilf = new double[2];
		hilf = validEdge(avID); //bestimmt erlaubte kante mit erge[0] = zielknoten und erge[1] = pushvalue
								//falls erge[0] = -1, dann gibts keine erlaubte kante
		int zielknoten = (int) hilf[0];
		double pushvalue = hilf[1];
		

		if (zielknoten == -1) {
		// gibt keine erlaubte Kante
			relabel(avID);// Knoten muss erhöht werden
			return f;			
		}

		else {
		// erlaubte Kante von avID zu zielknoten im Restnatzwerk
		// bestimme, wie viel gepusht wird  = min(kantenrestkap, excess)
			if (pushvalue >= vertexExcess.get(avID))
				pushvalue = vertexExcess.get(avID);

			push(avID, zielknoten, pushvalue);  //pushe 

			// aktualisiere Excess und Active:
			double alt = vertexExcess.get(avID);
			vertexExcess.put(avID, alt - pushvalue);
			
			alt = vertexExcess.get(zielknoten);
			vertexExcess.put(zielknoten, alt + pushvalue);
			// Fertigen Knoten (Excess=0) vom Stack "active" nehmen
			if (vertexExcess.get(avID) == 0) {
				vertexActive.pop();
			}
			
			//Zielknoten wird event. aktiv
			if (  (!vertexActive.contains(zielknoten)) &&		//wenn er nicht schon aktive war
			      (zielknoten != g.getT().getId())     && 		//und wenn er nicht t
			      (zielknoten != g.getS().getId()) )			//und nicht s ist
					
				vertexActive.push(zielknoten);				//dann wird zielknoten aktiv
		}
		return f;
	}
	
	
	
	public Iterator<Flow> iterator() {
		return new Iterator<Flow>() {
			public boolean hasNext() {
				return this.hasNext();
			}

			public Flow next() {
				return this.next();
			}

			public void remove() {
				this.remove();
			}

			@SuppressWarnings("unused")
			public void reset() {
				this.reset();
			}
		};
	}
	/** @return the current Flow*/
	
	public Flow repeat() {
		return f;
	}
	/** @return the current stepCount*/
	public int getStepCount() {
		return stepCount;
	}
	/** @return the current flow-value*/
	public double getFlowValue() {
		return f.getIncomingFlow(g.getT().getId());
	}

}
