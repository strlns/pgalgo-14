package maxflow14.graph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;


/**
 * This class models a network with flow values, N:=(V,E,s,t,c,f), and provides methods for interaction
 @see maxflow14.graph.Graph
 */
public class Flow implements Serializable {
	
	
	
	private static final long serialVersionUID = 2093353673145039459L;
	// Ein Objekt "MyFlow" enthält sowohl das
	// zugrundeliegende Flussnetzwerk (baseNetwork), als auch die
	// Flusswerte in einer Map <u<v,f(u,v)>> (flowValues)
	/**  flow values in a 2-dimensional HashMap <<u>v> -> f*/
	private Map<Integer, Map<Integer, Double>> flowValues;
	/**  network N:=(V,E,s,t,c)
	 * that is being worked with
	 @see maxflow14.graph.Graph */
	private Graph baseNetwork;
	/**  "forward" edges in the residual network (if requested)
	 @see maxflow.graph.Flow#getResidualNetwork() */
	private Set<Edge> residualEdges1 = new HashSet<Edge>();
	/**  "backward" edges in the residual network (if requested)
	 @see maxflow.graph.Flow#getResidualNetwork() */
	private Set<Edge> residualEdges2 = new HashSet<Edge>();

	// Konstruktor initialisiert mit Flussnetzwerk und setzt den
	// Fluss an allen Kanten auf 0

	/**  constructor with a given flow graph 
	 @see maxflow14.graph.Graph */
	public Flow(Graph g) {
		this.flowValues = new LinkedHashMap<Integer, Map<Integer, Double>>();
		this.baseNetwork = g;
		setZeroFlow();
	}

	/**  sets all flow values in the network to zero */
	public void setZeroFlow() {
		for (int u : baseNetwork.getEdgeList().keySet()) {
			for (int v : baseNetwork.getEdgeList().get(u).keySet()) {
				if (!flowValues.containsKey(u)) {
					flowValues.put(u, new HashMap<Integer, Double>());
				}
				flowValues.get(u).put(v, ((Double) 0.0));
			}
		}
	}

	/**  increases flow on Edge e by double f*/
	public void increaseFlow(int u, int v, double f) {
		if (baseNetwork.hasEdge(u, v)) {
			// Erhöhen des Flusswertes an einer existierenden Kante
			if (flowValues.containsKey(u)) {
				if (flowValues.get(u).containsKey(v)) {
					flowValues.get(u).put(v, flowValues.get(u).get(v) + f);
				} else {
					flowValues.get(u).put(v, f);
				}
			} else {
				flowValues.put(u, new HashMap<Integer, Double>());
				flowValues.get(u).put(v, f);
			}

		}
	}

	/**  decreases flow on Edge e by double f*/
	public void decreaseFlow(int u, int v, double f) {
		if (baseNetwork.hasEdge(u, v)) {
			// Senken an existierender Kante
			if (flowValues.containsKey(u)) {
				if (flowValues.get(u).containsKey(v)) {
					flowValues.get(u).put(v, flowValues.get(u).get(v) - f);
				} else {
					System.out.println("Wollen Fluss auf Kante "
							+ String.valueOf(u) + " nach " + String.valueOf(v)
							+ "erniedrigen, aber da ist kein Fluss!");
				}
			} else {
				System.out.println("Wollen Fluss auf Kante "
						+ String.valueOf(u) + " nach " + String.valueOf(v)
						+ "erniedrigen, aber da ist keine Kante!");
			}

		}
	}
	/**  @return the sum of all flow values leaving vertex v (given its id)*/
	public double getOutgoingFlow(int vid) {
		double flow = 0.0;
		if (!flowValues.containsKey(vid)) return flow;
		for (int v : flowValues.get(vid).keySet()) {
			flow += flowValues.get(vid).get(v);
		}
		return flow;
	}

	/**  @return tells if the given edge is present in the residual network*/
	public boolean edgeInRest(int u, int v){
		Double flow = (Double) flowValues.get(u).get(v);
		double capa = baseNetwork.getEdge(u, v).getWeight();
		if (flow == capa||flow==null) return false;
		else return true;
	}

	/**  @return returns edges capacities in the residual network for given IDs (u,v) */
	public double edgeInRestValue(int u, int v){
		double flow = flowValues.get(u).get(v);
		double capa = baseNetwork.getEdge(u, v).getWeight();
		return (capa - flow);
	}

	/**  @return If flow on the edge (u,v) is set to a positive value */
	public boolean isFlow(int u, int v){
		
		if (flowValues.get(u).get(v) == null) return false;
		double flow = flowValues.get(u).get(v);
		if (flow > 0) return true;
		else return false;
	}

	/**  @return flow value on edge (u,v)*/
	public double getFlow(int u, int v){
		if (!flowValues.containsKey(u)||!flowValues.get(u).containsKey(v)) return 0.0;
		double flow = flowValues.get(u).get(v);
		return flow;
	}

	/**  @return sum of flow values on edges (*,v) for a given vertex ID v*/
	public double getIncomingFlow(int vid) {
		double flow = 0.0;
		for (int u : flowValues.keySet()) {
			for (int v : flowValues.get(u).keySet()) {
				if (v == vid) {
					flow += flowValues.get(u).get(v);
				}
			}
		}
		return flow;
	}

	

	/**  @return the Residual network for the current graph and flow values*/
	public Graph getResidualNetwork() {
		// Knotenliste initialisieren
		Graph residualNetwork = new Graph(baseNetwork);
		residualEdges1.clear();
		residualEdges2.clear();
		for (int u : residualNetwork.getVertexList().keySet()) {
			for (int v : residualNetwork.getVertexList().keySet()) {
				if (baseNetwork.hasEdge(u, v)) {
					if (flowValues.containsKey(u)&&flowValues.get(u).containsKey(v)) {
					Edge e = new Edge(u, v, baseNetwork.getEdgeList().get(u)
							.get(v).getWeight()
							- flowValues.get(u).get(v));
					residualNetwork.addEdge(e);
					residualEdges1.add(e);}
					else {
						Edge e = new Edge(u, v, baseNetwork.getEdgeList().get(u)
								.get(v).getWeight());
						residualNetwork.addEdge(e);
						residualEdges1.add(e);
					}
				} else if (baseNetwork.hasEdge(v, u)) {
					if (flowValues.containsKey(v)&&flowValues.get(v).containsKey(u)) {
					Edge e = new Edge(u, v, flowValues.get(v).get(u));
					residualNetwork.addEdge(e);
					residualEdges2.add(e); }
				}
			}
		}
		residualNetwork.removeZeroEdges();
		return residualNetwork;

	}


	/**  @return the flow network N:=(V,E,s,t,c) that is being worked on 
	 @see maxflow14.graph.Graph*/
	public Graph getBaseNetwork() {
		return baseNetwork;
	}

	/**  @return a two-dimensional array Set<Path>[] with two sets, first the "forward" edges in the residual network, then the "backwards" edges
	*/
	@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	public HashSet<Edge>[] getResidualEdges() {
		HashSet[] r_e = new HashSet[2];
		for (HashSet hs : r_e) {
			hs = new HashSet<Edge>();
		}
		r_e[0] = (HashSet) residualEdges1;
		r_e[1] = (HashSet) residualEdges2;
		return r_e;
	}/**  @return tells if the flow meets the "flow sustainment" condition, i.e. exc(v)=0 for all v except s,t and exc(t)=-exc(s)
	 @see maxflow14.algos.PushRelabel */
	public boolean isFlowAllowed() {
		for (int v:baseNetwork.getVertexList().keySet()) {
			if (v!=baseNetwork.getT().getId()&&v!=baseNetwork.getS().getId()&&
					getIncomingFlow(v)!=getOutgoingFlow(v)) {
				return false;
			}
		}
		return true;
	}
	/**  @return a graph that contains the original vertex set, but only edges between vertices where positive flow values are set, and with the flow values as capacities. 
	 @see maxflow14.graph.Graph
	 @see maxflow14.gui.GraphPanel#drawRightEK_Flow(EdmondsKarp) */
	public Graph getFlowMap() {
		// Gibt einen Graphen zurÃ¼ck, der nur die Flusswerte als Kanten enthält
		Graph flowMap = new Graph(baseNetwork);

		for (int u : flowValues.keySet()) {
			for (int v : flowValues.get(u).keySet()) {
				flowMap.addEdge(new Edge(u, v, flowValues.get(u).get(v)));
			}
		}
		return flowMap;
	}

}
