package maxflow14.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;
import maxflow14.algos.GraphSearch;
import maxflow14.graph.Edge;
import maxflow14.graph.Graph;
import maxflow14.graph.Path;
import maxflow14.graph.Vertex;


public class RandomGraph {
	private Graph g = new Graph();
	private ArrayList<LinkedList<Vertex>> branches = new ArrayList<LinkedList<Vertex>>();
	private Stack<Vertex> tAdjStack = new Stack<Vertex>();
	public Graph randomFlowGraph(int size, int maxCap, int numOfT_Adj) {
		return randomFlowGraph(size,maxCap,numOfT_Adj,true);
	}
	public Graph randomFlowGraph(int size, double maxCap, int numOfT_Adj, boolean roundWeights) {
		// Erstellt einen Flussgraphen mit size Knoten und numOfT_Adj vielen
		// direkt mit t verbundenen Knoten
		if (size < 5)
			size = 5;
		int avBranchSize = (int) Math.min(20,Math.round(size / numOfT_Adj));
		g.eraseGraph();
		branches.clear();
		tAdjStack.clear();
		g.addVertex(0, "s");
		g.addVertex(1, "t");
		int currentSize = 2;
		int currentBranch = 0;
		int vid = 2;
		while (currentSize < size) {
			int branchSize = (int) Math.round(Math.random() * avBranchSize);
			if (currentSize + branchSize > size)
				branchSize = size - currentSize;
			branches.add(new LinkedList<Vertex>());
			for (int i = 0; i < branchSize; i++) {
				Vertex v = new Vertex(vid, "v" + vid);
				vid++;
				g.addVertex(v);
				if (i == 0) {
					g.addEdge(g.getS(), v,
							(Math.random()+1) * maxCap);
				} else {
					if (Math.random() > 0.5) {
						g.addEdge(branches.get(currentBranch).getLast(), v,
								Math.random() * maxCap);
					} else {
						Set<Vertex> exclude = new HashSet<Vertex>();
						exclude.add(v);
						exclude.addAll(branches.get(currentBranch));
						if (exclude.size() >= g.getVertexList().size() - 2) {
							g.addEdge(branches.get(currentBranch).getLast(), v,
									Math.random() * maxCap);
						} else {
							Vertex z = g.getRandomVertex(exclude);
							g.addEdge(branches.get(currentBranch).getLast(), z,
									Math.random() * maxCap);
							g.addEdge(z, v,
									Math.random() * maxCap);
						}
					}
				}
				branches.get(currentBranch).add(v);
			}
			currentBranch++;
			currentSize = currentSize + branchSize;
		}

		Set<Vertex> tAdj = new HashSet<Vertex>();
		if (numOfT_Adj > g.getVertexList().size() - 2)
			numOfT_Adj = g.getVertexList().size() - 2;
		for (int i = 0; i < numOfT_Adj; i++) {
			Vertex v = g.getRandomVertex();
			while (tAdj.contains(v))
				v = g.getRandomVertex();
			tAdj.add(v);
			g.addEdge(v, g.getT(), (Math.random()+1.0) * maxCap);
			tAdjStack.add(v);
		}
		Set<Vertex> toRepeat = new HashSet<Vertex>();
		for (int u_id : g.getVertexList().keySet()) {
			int loopCount = 0;
			if (!((u_id == 0) | (u_id == 1))) {
				Path p = GraphSearch.BFS(g, g.getVertex(u_id), g.getT());
				while (p.size() == 0) {
					loopCount++;
					Set<Vertex> exclude = new HashSet<Vertex>();
					exclude.add(g.getVertex(u_id));
					Vertex v = g.getRandomVertex(exclude);
					while (g.countOutEdges(v) > numOfT_Adj) {
						v = g.getRandomVertex(exclude);
					}
					exclude.add(v);
					if (Math.random() > 0.7) {
						g.addEdge(g.getVertex(u_id), v,
								Math.random() * maxCap);
					} else if ((Math.random() > 0.3)
							&& (g.countOutEdges(g.getVertex(u_id)) < numOfT_Adj)) {
						g.addEdge(v, g.getVertex(u_id),
								Math.random() * maxCap);
					} else {
						Vertex w = g.getRandomVertex();
						while (w.getId() == v.getId() | g.countIncEdges(w) > 4) {
							w = g.getRandomVertex();
						}
						g.addEdge(v, w, Math.random() * maxCap);
					}
					if (loopCount > Math.round(size)) {
						g.addEdge(g.getVertex(u_id), g.getT(),
								Math.random() * maxCap);
						toRepeat.add(g.getVertex(tAdjStack.peek().getId()));
						g.removeEdge(tAdjStack.pop().getId(), g.getT().getId());
						tAdjStack.add(g.getVertex(u_id));
						p = GraphSearch.BFS(g, g.getVertex(u_id), g.getT());
						break;
					}
					p = GraphSearch.BFS(g, g.getVertex(u_id), g.getT());
				}

			}

		}

		for (Vertex u : toRepeat) {
			int u_id = u.getId();
			Set<Vertex> exclude = new HashSet<Vertex>();
			exclude.addAll(tAdjStack);
			int loopCount = 0;
			while (GraphSearch.BFS(g, g.getVertex(u_id), g.getT()).size() == 0) {
				loopCount++;
				if (loopCount > size / numOfT_Adj) {
					break;
				}
				if (!(exclude.size() >= g.getVertexList().size() - 2)) {
					Vertex v = g.getRandomVertex(exclude);
					exclude.add(v);

					Vertex w = g.getRandomVertex(exclude);
					exclude.add(w);
					g.addEdge(v, w, Math.random() * maxCap);
				}
			}
		}

		Graph g_temp=new Graph(g);
		for (int u:g.getEdgeList().keySet()) {
			for (int v:g.getEdgeList().get(u).keySet()) {
				if (u==g.getS().getId()) {
					g_temp.addEdge(u,v,g.getEdge(u, v).getWeight()
							+maxCap*g.countOutEdges(g.getVertex(v))
							);
				}
				else 
					g_temp.addEdge(u,v,g.getEdge(u, v).getWeight()
							+g.countIncEdges(g.getVertex(u))
							);
			}
		}


		g_temp.scaleWeight((double)((double)maxCap/g_temp.getMaxEdgeWeight()));
		if (roundWeights) {
		for (int u:g_temp.getVertexList().keySet()) {
			for (int v:g_temp.getEdgeList().get(u).keySet()) { 
				Edge e = g_temp.getEdge(u, v);
				e.setWeight((int)Math.round(e.getWeight()));
			}
		}}
		g=g_temp;
		return g;

	}

	public Graph randomCompleteFlow(int size, double maxCap) {
		if (size > 200)
			size = 200;
		if (size < 3)
			size = 3;
		Graph g = new Graph();
		g.addVertex(0, "s");
		g.addVertex(size - 1, "t");
		for (int i = 1; i < size - 1; i++) {
			g.addVertex(i, "v" + i);
		}
		for (int u : g.getVertexList().keySet()) {
			for (int v : g.getVertexList().keySet()) {
				if ((v != 0) && (u != size - 1) && !(u == 0 && v == size - 1)
						&& !(u == v)) {
					g.addEdge(u, v, Math.random() * maxCap);
				}

			}
		}
		return g;
	}
}
