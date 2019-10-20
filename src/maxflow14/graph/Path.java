package maxflow14.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;


/**
 * The class Path extends the generic java.lang.LinkedList<Edge> and implements java.lang.Iterable<Edge>
 @see java.util.LinkedList
 @see java.lang.Iterable
 */
public class Path extends LinkedList<Edge> implements Iterable<Edge>,
		Serializable {
	private static final long serialVersionUID = (long) Math.random()
			* Integer.MAX_VALUE;

	public boolean containsEdgeFromTo(Edge e) {
		for (Edge e_ : this) {
			if ((e.getStart() == e_.getStart()) && (e_.getEnd() == e.getEnd()))
				return true;
		}
		return false;
	}

	public Path(PathInt p, Graph g) {
		super();
		this.clear();
		for (int[] e : p) {
			this.add(g.getEdge(e[0], e[1]));
		}
	}

	public Path(ArrayList<Edge> path) {
		super();
		this.clear();
		Set<Edge> edges_ = new HashSet<Edge>();
		edges_.addAll(path);
		Edge currentEdge = null;
		for (Edge e : path) {
			if (e.getStart() == 0) {
				this.add(e);
				currentEdge = e;
				edges_.remove(e);
			}
		}
		while (!edges_.isEmpty() && currentEdge != null) {
			int e_old_Size = edges_.size();
			for (Edge e : edges_) {
				if (e.getStart() == currentEdge.getEnd()) {
					this.add(e);
					currentEdge = e;
					edges_.remove(e);
				}
			}
			if (e_old_Size == edges_.size())
				break;
		}

	}

	public ArrayList<Edge> returnArrayList() {
		ArrayList<Edge> al = new ArrayList<Edge>();
		al.addAll(this);
		return al;
	}

	public Path() {
		super();
		this.clear();
	}

	public ArrayList<String> toText() {
		ArrayList<String> str_ = new ArrayList<String>();
		for (int i = 0; i < this.size(); i++) {
			if (this.get(i).getStart() == 0) {
				str_.add("s ->" + this.get(i).getEnd());
			} else if (this.get(i).getEnd() == 1) {
				str_.add(this.get(i).getStart() + "-> t");
			} else {
				str_.add(this.get(i).getStart() + "->" + this.get(i).getEnd());
			}
		}
		return str_;
	}
}
