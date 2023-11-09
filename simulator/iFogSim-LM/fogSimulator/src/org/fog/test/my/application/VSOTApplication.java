package org.fog.test.my.application;

import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.FogBroker;
import org.fog.entities.Tuple;
import org.fog.placement.ModuleMapping;
import org.fog.test.my.topology.Topology;
import org.fog.test.my.utils.ActuatorLevel;
import org.fog.test.my.utils.HostConfig;
import org.fog.test.my.utils.Level;
import org.fog.test.my.utils.SensorLevel;
import org.fog.utils.distribution.NormalDistribution;
import org.fog.utils.distribution.UniformDistribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VSOTApplication extends ApplicationBuilder {

    static double DCNS_TRANSMISSION_TIME = 6;
    static double SD_DCNS_TRANSMISSION_TIME = 1.5;

    private Map<Integer, Integer> dist;

    public VSOTApplication(Topology topology, Map<Integer, Integer> dist, String placementStrategy, int delay) throws Exception {
        super("DCNS", topology, placementStrategy, delay);

        this.fogBroker = new FogBroker("broker"+appId);
        this.dist = dist;
        this.application = this.createApplication();
        this.application.setPlacementStrategy(this.placementStrategy);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>> [VSOT] The placementStrategy is: " + this.placementStrategy);


        this.createEdgeDevices();
        this.mapModules();
    }

    public Application createApplication() {
        int userId = this.fogBroker.getId();

        Application application = Application.createApplication(this.appId, userId);

        // Adding modules (vertices) to the application model (directed graph)
        application.addAppModule("motion_detector", 10, 300);
        application.addAppModule("object_detector", 10, 550);
        application.addAppModule("user_interface", 10, 200);
        application.addAppModule("object_tracker", 10, 300);

        // Connecting the application modules (vertices) in the application model (directed graph) with edges

        // adding edge from CAMERA (sensor) to Motion Detector module carrying tuples of type CAMERA
        application.addAppEdge("CAMERA", "motion_detector", new UniformDistribution(1000*0.9, 1000*1.1, SEED),
                20000, "CAMERA", Tuple.UP, AppEdge.SENSOR);
        // adding edge from Motion Detector to Object Detector module carrying tuples of type MOTION_VIDEO_STREAM
        application.addAppEdge("motion_detector", "object_detector", new UniformDistribution(2000*0.9, 2000*1.1, SEED),
                2000, "MOTION_VIDEO_STREAM", Tuple.UP,
                AppEdge.MODULE);
        // adding edge from Object Detector to User Interface module carrying tuples of type DETECTED_OBJECT
        application.addAppEdge("object_detector", "user_interface", new UniformDistribution(500*0.9, 500*1.1, SEED),
                2000, "DETECTED_OBJECT", Tuple.UP,
                AppEdge.MODULE);
        // adding edge from Object Detector to Object Tracker module carrying  tuples of type OBJECT_LOCATION
        application.addAppEdge("object_detector", "object_tracker", new UniformDistribution(1000*0.9, 1000*1.1, SEED),
                100, "OBJECT_LOCATION", Tuple.UP,
                AppEdge.MODULE);
        // adding edge from Object Tracker to PTZ (pan-tilt-zoom) CONTROL (actuator) carrying tuples of type PTZ_PARAMS
        application.addAppEdge("object_tracker", "PTZ_CONTROL", 100, new UniformDistribution(28*0.9, 28*1.1, SEED),
                100, "PTZ_PARAMS", Tuple.DOWN,
                AppEdge.ACTUATOR);

        // Defining the input-output relationships (represented by selectivity) of the application modules.

        // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type
        // CAMERA
        application.addTupleMapping("motion_detector", "CAMERA",
                "MOTION_VIDEO_STREAM", new FractionalSelectivity(1.0,SEED));
        // 1.0 tuples of type OBJECT_LOCATION are emitted
        // by Object Detector module per incoming tuple of type MOTION_VIDEO_STREAM
        application.addTupleMapping("object_detector", "MOTION_VIDEO_STREAM",
                "OBJECT_LOCATION", new FractionalSelectivity(1.0,SEED));
        // 0.05 tuples of type MOTION_VIDEO_STREAM are
        // emitted by Object Detector module per incoming tuple of type MOTION_VIDEO_STREAM
        application.addTupleMapping("object_detector", "MOTION_VIDEO_STREAM",
                "DETECTED_OBJECT", new FractionalSelectivity(0.05,SEED));

        // Defining application loops (maybe incomplete loops) to monitor the latency of.
        // Here, we add two loops for monitoring :
        // Motion Detector -> Object Detector -> Object Tracker and Object
        // Tracker -> PTZ Control
        final AppLoop loop1 = new AppLoop(new ArrayList<String>() {{
            add("motion_detector");
            add("object_detector");
            add("object_tracker");
        }});
        final AppLoop loop2 = new AppLoop(new ArrayList<String>() {{
            add("object_tracker");
            add("PTZ_CONTROL");
        }});
        List<AppLoop> loops = new ArrayList<AppLoop>() {{
            add(loop1);
            add(loop2);
        }};

        application.setLoops(loops);

        return application;
    }

    protected void createEdgeDevices(){
        this.topology.getBotton().addEdge(
                new Level(this.dist, new HostConfig(1000, 10000, 10000, 270, 0), "m-DCNS",topology.getEdgeLatency())
                        .addAppModulesUp("object_detector",1)
                        .addAppModulesUp("object_tracker",1)
                        .addAppModules("motion_detector",1)
                        .addGroup(
                                new SensorLevel(1,"s-m-DCNS", "CAMERA",
                                        new NormalDistribution(DCNS_TRANSMISSION_TIME, SD_DCNS_TRANSMISSION_TIME, SEED),1, fogBroker.getId(), appId),
                                new ActuatorLevel(1,"ptz-m-DCNS", "PTZ_CONTROL",1, fogBroker.getId(), appId)
                        )
        );
    }

    private void mapModules(){
        this.topology.getTop().addAppModules("user_interface", 1);
    }

    public static String configureMappingStrategy(int config) {
        return new HashMap<Integer, String>(){
            {
                put(1, "Mapping");
                put(2, "Edgewards");
                put(3, "Edgewards");
                put(4, "EdgewardsIndividually");
                put(5, "EdgewardsIndividually");
                put(7, "EdgewardsWithCommunicationAnalysis");
                put(8, "ModulePlacementEdgewardsIndividually2");
                put(9, "ModulePlacementEdgewardsCommunicationAnalysis2");
                put(10, "ModulePlacementEdgewardsCommunicationAnalysis3");
                put(11, "ModulePlacementEdgewardsCommunicationAnalysis4");
                put(12, "ModulePlacementEdgewardsCommunicationAnalysis5");
            }
        }.get(config);
    }

    public static Integer configureMappingDelay(int config) {
        return new HashMap<Integer, Integer>(){
            {
                put(1, 0);
                put(2, 0);
                put(3, 10);
                put(4, 0);
                put(5, 10);
                put(7, 10);
                put(8, 10);
                put(9, 10);
                put(10, 10);
                put(11, 10);
                put(12, 10);
            }
        }.get(config);
    }

}
