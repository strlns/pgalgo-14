package maxflow14.graph;

import java.io.Serializable;

/** This class models an edge of a graph
 *  it implements the interface Serializable to store and load a graph*/
public class Edge implements Serializable {
	 /** SerialVersionID to store the edge*/
	private final static long serialVersionUID = 9166677945556489777L;
	 /** The ID of the start vertex*/
	private int startVertex;
	/** The ID of the end vertex*/
	private int endVertex;
	/** The Weight of the edge*/
	private double edgeWeight;
	
	/** Constructor: Initialises the edge 
	 * @param start ID of the start vertex
	 * @param end ID of the end vertex
	 * @param weight Weight of the edge*/
	public Edge(int start, int end, double weight) {
		super();
		this.startVertex = start;
		this.endVertex = end;
		this.edgeWeight = weight;
	}
	/** @return The start of the edge*/
	public int getStart() {
		return startVertex;
	}
	/** @return The end of the edge*/
	public int getEnd() {
		return endVertex;
	}
	/** @return The weight of the edge*/
	public double getWeight() {
		return edgeWeight;
	}
	
	public void setWeight(double w) {
		this.edgeWeight = w;
	}
	@Override
	public String toString() {
		return startVertex + "->" + endVertex + "  (c:"+ edgeWeight+")";
	}
}
