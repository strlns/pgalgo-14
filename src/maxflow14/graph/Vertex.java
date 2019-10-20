package maxflow14.graph;

import java.io.Serializable;

@SuppressWarnings("serial")
/** This class models a vertex of a graph
 * It implements the interface Serializable to store and load a graph*/
public class Vertex implements Serializable {
	/** The ID of the Vertex
	 * A vertex is usualy */
	private int id;
	private String label;

	// Konstruktoren für Knoten

	public Vertex(int id, String label) {
		super();
		this.id = id;
		this.label = label;
	}

	public Vertex(int id) {
		super();
		this.id = id;
		this.label = new Integer(id).toString();
	}

	public int getId() {
		return this.id;
	}

	public void setLabel(String l) {
		this.label = l;
	}

	public String getLabel() {
		return this.label;
	}
}