package maxflow14.algos;

import java.util.Iterator;

import maxflow14.graph.Edge;
import maxflow14.graph.Flow;
import maxflow14.graph.Graph;
import maxflow14.graph.Path;
import maxflow14.algos.GraphSearch;
/**
 * The class EdmondsKarps represents an instance of the EdmondsKarp algorithm. It implements the interface Algo.java.
 @see maxflow14.algos.Algo
 */
public class EdmondsKarp implements Algo {
	 /** SerialVersionID identifies this class for storage
	  @see maxflow14.util.FileIO */
	final static long serialVersionUID=4930657681384160061L;
	 /** f is an object containing a flow network with flow values
	  @see maxflow14.graph.Flow */
	private Flow f;
	 /** Number of steps that the algorithm has carried out so far 
	  * One step = Search for an augmenting path, increase flow. Step 0 => Zero flow) */
	private int stepCount;
	 /** Current augmenting path 
	  @see maxflow14.graph.Path */
	private Path currentPath;


	
	public EdmondsKarp(Graph g) {
		 /** Initialization of the algorithm, i.e. creating Flow object with a given Graph g*/
		this.f = new Flow(g);
		stepCount = 0;
		// Initialisierung mit 0-Fluss
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
	/** Implements method hasNext() of inherited interface Iterable<Object>
	 @see maxflow14.algos.Algo
	 @see java.lang.Iterable
	 * @return boolean, tells if another step is possible
	 * */
	public boolean hasNext() {
		Path p = augmentingPath();
		if (p.size() == 0)
			return false;
		double c = getMinPathCapacity(p);
		if (c <= 0.0) {
			return false;
		} else {
			return true;
		}
	}

	/** Implements method next() in inherited interface Iterable<Object>
	 @see maxflow14.algos.Algo
	 @see java.lang.Iterable
	 * @return Returns a Flow object containing the network as well as all set flow values, after one step has been carried out in the EdmondsKarp algorithm
	 @see maxflow14.graph.Flow
	 * */
	public Flow next() {
		stepCount += 1;
		Path path = augmentingPath();
		this.currentPath = path;

		for (Edge e : path.returnArrayList()) {
			if (f.getBaseNetwork().hasEdge(e.getStart(), e.getEnd())) {
				f.increaseFlow(e.getStart(), e.getEnd(), e.getWeight());
			} else if (f.getBaseNetwork().hasEdge(e.getEnd(), e.getStart())) {
				f.decreaseFlow(e.getEnd(), e.getStart(), e.getWeight());
			}
		}

		return f;
	}
	/** 
	 * Returns the current Flow object, but does not proceed the algorithm (as next() does)
	 @return Flow object that is being worked with 
	 @see maxflow14.graph.Flow
	 */
	public Flow repeat() {
		return f;
	}

	/** 
	 * Returns the last used / current augmenting path in the residual network, without proceeding.
	 @return Path object that is/was being worked with 
	 @see maxflow14.graph.Path
	 */
	public Path currentPath() {
		return currentPath;
	}

	public void remove() {
		// Methode macht hier keinen Sinn
	}

	/** 
	 * Resets all flow values and step count to zero.
	 */
	public void reset() {
		f.setZeroFlow();
		stepCount = 0;
	}

	/** 
	 * Returns the current step count (0 => zero flow)
	 @return number of steps (search path, increase flow) executed so far
	 */
	public int getStepCount() {
		return stepCount;
	}

	/** 
	 * Returns the current step count (0 => zero flow)
	 @return sum of all flow values on edges to "t", not regarding if the flow is valid
	@see maxflow14.graph.Flow
	 */
	public double getFlowValue() {

		double val = f.getIncomingFlow(f.getBaseNetwork().getT().getId());
		return val;
	}

	/** 
	 * Returns the an augmenting path in the residual network
	 * and sets all edge weights to the minimum path capacity
	 @return Path object
	 @see maxflow14.graph.Path
	 @see maxflow14.algos.EdmondsKarp#getMinPathCapacity(Path)
	 */
	public Path augmentingPath() {
		Graph residualNetwork = f.getResidualNetwork();
		Path path = GraphSearch.BFS(residualNetwork, residualNetwork.getS(),
				residualNetwork.getT());
		double minCap = getMinPathCapacity(path);
		for (Edge e : path) {
			e.setWeight(minCap);
		}
		return path;
	}

	/** 
	 * Returns the minimum capacity of the edges in path p
	 @return minimum capacity among all edges in p
	 @param p the path to look at
	 @see maxflow14.graph.Path
	 @see maxflow14.algos.EdmondsKarp#getMinPathCapacity(Path)
	 */
	public double getMinPathCapacity(Path p) {
		double mc = Double.MAX_VALUE;
		for (Edge e : p) {
			if (e.getWeight() < mc) {
				mc = e.getWeight();
			}
		}
		if (mc == Double.MAX_VALUE) {
			return (Double) 0.0;
		} else {
			return mc;
		}
	}
}
