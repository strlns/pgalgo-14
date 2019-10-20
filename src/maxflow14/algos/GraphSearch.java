package maxflow14.algos;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import maxflow14.graph.Edge;
import maxflow14.graph.Graph;
import maxflow14.graph.Path;
import maxflow14.graph.PathInt;
import maxflow14.graph.Vertex;

/**
 * The class GraphSearch contains the BFS algorithm used by EdmondsKarp
 @see maxflow14.algos.EdmondsKarp
 */
public class GraphSearch {

	public static Path BFS(Graph g, Vertex startNode, Vertex goalNode) {
		// Breitensuche (gibt einen kürzesten Pfad zurück)
		// kein Pfad gefunden -> leeres Path-Objekt wird zurückgegeben

		if (!g.hasVertex(goalNode))
			return new Path();
		if (!g.hasVertex(startNode))
			return new Path();
		Queue<Integer> activeNodes = new LinkedList<Integer>();
		HashSet<Integer> visitedNodes = new HashSet<Integer>();
		// Hashmap <key,<ArrayList<DWE>>> enthält Weg von s zu Knoten mit ID
		// <key>
		HashMap<Integer, PathInt> pathTree = new HashMap<Integer, PathInt>();
		Set<Integer> keys = g.getVertexList().keySet();
		for (int key : keys) {
			pathTree.put(key, new PathInt());
		}
		// StartNode an den Anfang der Warteschlange einfügen
		activeNodes.add(startNode.getId());
		visitedNodes.add(startNode.getId());
		PathInt p = BFS_rec(g, activeNodes, visitedNodes, goalNode, pathTree);
		Path P = new Path(p, g);
		Collections.reverse(P);
		return P;
	}

	private static PathInt BFS_rec(Graph g, Queue<Integer> activeNodes,
			HashSet<Integer> visitedNodes, Vertex goalNode,
			HashMap<Integer, PathInt> pathTree) {
		if (activeNodes.isEmpty())
			return new PathInt();
		int pos = activeNodes.poll();
		if (pos == goalNode.getId()) {
			return pathTree.get(goalNode.getId());
		} else {
			for (int i : g.getEdgeList().get(pos).keySet()) {
				if (!visitedNodes.contains(g.getEdgeList().get(pos).get(i)
						.getEnd())) {
					if (g.getEdgeList().get(pos).get(i).getWeight() > 0.0) {
						int v = g.getEdgeList().get(pos).get(i).getEnd();
						Edge e = g.getEdge(pos, v);
						int[] e_int = { e.getStart(), e.getEnd() };
						pathTree.get(v).add(e_int);
						if (!pathTree.get(e.getStart()).isEmpty()) {
							pathTree.get(v).addAll(pathTree.get(e.getStart()));
						}
						activeNodes.add(v);
						visitedNodes.add(v);
					}
				}
			}
		}
		if (activeNodes.isEmpty()) {
			return new PathInt();
		} else
			return BFS_rec(g, activeNodes, visitedNodes, goalNode, pathTree);
	}
}
