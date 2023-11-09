package org.fog.test.my.application;

import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.FogBroker;
import org.fog.entities.Tuple;
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

public class LeakSWMApplication extends ApplicationBuilder {

    static double LSWM_TRANSMISSION_TIME = 6;
    static double SD_LSWM_TRANSMISSION_TIME = 3;

    private Map<Integer, Integer> dist;

    public LeakSWMApplication(Topology topology, Map<Integer, Integer> dist, String placementStrategy, int delay) throws Exception {
        super("LSWM", topology, placementStrategy, delay);

        this.fogBroker = new FogBroker("broker"+appId);
        this.dist = dist;
        this.application = this.createApplication();
        this.application.setPlacementStrategy(this.placementStrategy);


        this.createEdgeDevices();
        this.mapModules();
    }

    public Application createApplication() {
        Application application = Application.createApplication(appId, this.fogBroker.getId());

        application.addAppModule("data_aggregator", 10, 110);
        application.addAppModule("leak_detector", 10, 300);
        application.addAppModule("actuator_controller", 10, 300);

        application.addAppEdge("PRESSURE", "data_aggregator", new UniformDistribution(100*0.9, 100*1.1, SEED),
                100, "PRESSURE", Tuple.UP, AppEdge.SENSOR);

        application.addAppEdge("data_aggregator", "leak_detector", new UniformDistribution(500*0.9, 500*1.1, SEED),
                1000, "PRESSURE_AGREGGATED", Tuple.UP, AppEdge.MODULE);

        application.addAppEdge("leak_detector", "actuator_controller", new UniformDistribution(1000*0.9, 1000*1.1, SEED),
                100, "LEAK_DETECTED", Tuple.UP, AppEdge.MODULE);

        application.addAppEdge("actuator_controller", "TURN_OFF", new UniformDistribution(50*0.9, 50*1.1, SEED),
                100, "TURN_OFF", Tuple.DOWN, AppEdge.ACTUATOR);


        application.addTupleMapping("data_aggregator", "PRESSURE",
                "PRESSURE_AGREGGATED", new FractionalSelectivity(1,SEED));

        application.addTupleMapping("leak_detector", "PRESSURE_AGREGGATED",
                "LEAK_DETECTED", new FractionalSelectivity(1,SEED));

        application.addTupleMapping("actuator_controller", "LEAK_DETECTED",
                "TURN_OFF", new FractionalSelectivity(1,SEED));

        final AppLoop loop1 = new AppLoop(new ArrayList<String>() {{
            add("data_aggregator");
            add("leak_detector");
            add("actuator_controller");
        }});

        List<AppLoop> loops = new ArrayList<AppLoop>() {{
            add(loop1);
        }};

        application.setLoops(loops);

        return application;
    }

    protected void createEdgeDevices(){
        this.topology.getBotton().addEdge(
                new Level(this.dist, new HostConfig(200, 10000, 10000, 270, 0), "m-LWSM",topology.getEdgeLatency())
                        .addAppModulesUp("leak_detector",-1)
                        .addAppModulesUp("actuator_controller",-1)
                        .addAppModules("data_aggregator",1)
                        .addGroup(
                                new SensorLevel(1,"s-m-LWSM", "PRESSURE", new NormalDistribution(LSWM_TRANSMISSION_TIME, SD_LSWM_TRANSMISSION_TIME, SEED),1, fogBroker.getId(), appId),
                                new ActuatorLevel(1,"valve-m-LWSM", "TURN_OFF",1, fogBroker.getId(), appId)
                        )
        );
    }

    private void mapModules(){
//        this.topology.getTop().addAppModules("user_interface", 1);
    }

    public static String configureMappingStrategy(int config) {
        return new HashMap<Integer, String>(){
            {
                put(1, "Mapping");
                put(2, "Mapping");
                put(3, "Edgewards");
                put(4, "Mapping");
                put(5, "EdgewardsIndividually");
                put(7, "EdgewardsWithCommunicationAnalysis");
                put(8, "Novo");
                put(9, "Novo2");
                put(10, "Novo3");
                put(11, "Novo4");
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
            }
        }.get(config);
    }

}
