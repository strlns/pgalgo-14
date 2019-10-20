package maxflow14.util;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.mxgraph.util.mxPoint;

import maxflow14.algos.Algo;
import maxflow14.algos.EdmondsKarp;
import maxflow14.graph.Flow;
import maxflow14.graph.Graph;

@SuppressWarnings("serial")
public class FileIO implements Serializable {

	public class FlowIO_ implements Serializable {
		private Flow f;
		private Algo a;
		private Map<Integer,double[]> vPos;
		private Map<Integer,Map<Integer,LinkedList<mxPoint>>> edgePoints;
		public FlowIO_(Flow f, Algo a, Map<Integer,double[]> vPos, Map<Integer,Map<Integer,LinkedList<mxPoint>>> edgePoints) {
			this.f = f;
			this.a = a;
			this.vPos = vPos;
			this.edgePoints = edgePoints;
		}

		public Algo returnAlgo() {
			return a;
		}

		public Flow returnFlow() {
			return f;
		}
		public Map<Integer,double[]> returnVertexPositions() {
			return vPos;
		}
		public Map<Integer,Map<Integer,LinkedList<mxPoint>>> returnEdgePoints() {
			return edgePoints;
		}
	}

	public void saveFlow(Flow f, Algo a, Map<Integer,double[]> vPos, Map<Integer,Map<Integer,LinkedList<mxPoint>>> edgePoints, String filePath) {
		FlowIO_ flowObject = new FlowIO_(f, a, vPos, edgePoints);
		try {
			FileOutputStream fout = new FileOutputStream(filePath);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(flowObject);
			oos.close();
			System.out.println("Done");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Flow readFlow(String filename) {
		try {
			InputStream file = new FileInputStream(filename);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);
			FlowIO_ flowObject = (FlowIO_) input.readObject();
			input.close();
			return flowObject.returnFlow();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
			System.out.println("Cannot perform input. Class not found.");
			return new Flow(new Graph());
		} catch (IOException ex) {
			ex.printStackTrace();
			System.out.println("Cannot perform input.");
			return new Flow(new Graph());
		}
	}

	public Algo readState(String filename) {
		try {
			InputStream file = new FileInputStream(filename);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);
			FlowIO_ flowObject = (FlowIO_) input.readObject();
			input.close();
			return flowObject.returnAlgo();
		} catch (ClassNotFoundException ex) {
			System.out.println("Cannot perform input. Class not found.");
			return new EdmondsKarp(new Graph());
		} catch (IOException ex) {
			System.out.println("Cannot perform input.");
			return new EdmondsKarp(new Graph());
		}
	}
	public Map<Integer,double[]> readPositions(String filename) {
		try {
			InputStream file = new FileInputStream(filename);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);
			FlowIO_ flowObject = (FlowIO_) input.readObject();
			input.close();
			return flowObject.returnVertexPositions();
		} catch (ClassNotFoundException ex) {
			System.out.println("Cannot perform input. Class not found.");
			return new HashMap<Integer,double[]>();
		} catch (IOException ex) {
			System.out.println("Cannot perform input.");
			return new HashMap<Integer,double[]>();
		}
	}
	public Map<Integer,Map<Integer,LinkedList<mxPoint>>> readEdgePoints(String filename) {
		try {
			InputStream file = new FileInputStream(filename);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);
			FlowIO_ flowObject = (FlowIO_) input.readObject();
			input.close();
			return flowObject.returnEdgePoints();
		} catch (ClassNotFoundException ex) {
			System.out.println("Cannot perform input. Class not found.");
			return new HashMap<Integer,Map<Integer, LinkedList<mxPoint>>>();
		} catch (IOException ex) {
			System.out.println("Cannot perform input.");
			return new HashMap<Integer,Map<Integer, LinkedList<mxPoint>>>();
		}
	}
}
