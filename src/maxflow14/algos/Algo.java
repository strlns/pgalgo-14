package maxflow14.algos;

import java.io.Serializable;

import maxflow14.graph.Flow;

/**
 * The interface Algo extends the generic interface java.lang.Iterable<Object> for type maxflow14.graph.Flow 
 *  @see maxflow14.algos.Algo
 *  @see java.lang.Iterable
 */

public interface Algo extends Iterable<Flow>, Serializable {
	public Flow next();

	public Flow repeat();

	public int getStepCount();

	public void reset();

	public boolean hasNext();

	public double getFlowValue();
}
