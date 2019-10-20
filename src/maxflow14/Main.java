package maxflow14;

import javax.swing.JFrame;

import maxflow14.graph.*;
import maxflow14.gui.MainWindow;
import maxflow14.util.RandomGraph;


public class Main {
	public static void main(String[] args) {
		Graph g = (new RandomGraph()).randomFlowGraph(10, 20, 4);
		JFrame mainWindow = new MainWindow(g);
		mainWindow.setVisible(true);

	}

}
