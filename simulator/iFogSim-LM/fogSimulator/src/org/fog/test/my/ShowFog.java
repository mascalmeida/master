package org.fog.test.my;

import org.fog.gui.core.Graph;
import org.fog.gui.example.FogGui;

public class ShowFog extends FogGui {

    public ShowFog(Graph graph) {
        super();
        super.physicalGraph = graph;
        super.physicalCanvas.setGraph(physicalGraph);
        physicalCanvas.repaint();
    }
}
