package maxflow14.graph;

import java.util.LinkedList;


/**
 * The class Path extends the generic java.lang.LinkedList<int[]> and implements java.lang.Iterable<int[]>
 @see java.util.LinkedList
 @see java.lang.Iterable
 */
public class PathInt extends LinkedList<int[]> implements Iterable<int[]> {
	private static final long serialVersionUID = (long) Math.random()
			* Integer.MAX_VALUE;

	public PathInt() {
		super();
	}

	public void addAll(PathInt p) {
		for (int[] e : p) {
			this.add(e);
		}
	}
}
