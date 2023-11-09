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

public class VRGameApplication extends ApplicationBuilder {

    private final String appNameSifx;
    private Map<Integer, Integer> dist;

    static double EEG_TRANSMISSION_TIME = 15;
    static double SD_EEG_TRANSMISSION_TIME = 3.5;

    public VRGameApplication(Topology topology, Map<Integer, Integer> dist, String placementStrategy, String appNameSifx, int delay) throws Exception {
        super("VRGame",topology, placementStrategy, delay);

        this.appNameSifx = appNameSifx;
        this.fogBroker = new FogBroker("broker" + appId + this.appNameSifx);
        this.dist = dist;

        this.application = this.createApplication();
        this.application.setPlacementStrategy(this.placementStrategy);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>> [VRGame] The placementStrategy is: " + this.placementStrategy);
        this.createEdgeDevices();
        this.mapModules();
    }

    public Application createApplication() {
        String i = this.appNameSifx;

        Application application = Application.createApplication(this.appId + this.appNameSifx, fogBroker.getId());
        application.addAppModule("client_" + this.appNameSifx, 10, 200);
        application.addAppModule("concentration_calculator_" + this.appNameSifx, 10, 350);
        application.addAppModule("connector_" + this.appNameSifx, 10, 100);

        application.addAppEdge("EEG_" + this.appNameSifx, "client_" + this.appNameSifx, new UniformDistribution(3000*0.9, 3000*1.1, SEED), 500,
                    "EEG_" + this.appNameSifx, Tuple.UP, AppEdge.SENSOR);

        application.addAppEdge("client_" + this.appNameSifx, "concentration_calculator_" + this.appNameSifx, new UniformDistribution(3500*0.9, 3500*1.1, SEED),
                500, "_SENSOR_" + this.appNameSifx, Tuple.UP,
                AppEdge.MODULE);
        application.addAppEdge("concentration_calculator_" + this.appNameSifx, "connector_" + this.appNameSifx, 100,
                new UniformDistribution(1000*0.9, 1000*1.1, SEED), 1000, "PLAYER_GAME_STATE_" + this.appNameSifx,
                Tuple.UP, AppEdge.MODULE);
        application.addAppEdge("concentration_calculator_" + this.appNameSifx, "client_" + this.appNameSifx, new UniformDistribution(14*0.9, 141.1, SEED),
                500, "CONCENTRATION_" + this.appNameSifx, Tuple.DOWN,
                AppEdge.MODULE);
        // adding periodic edge (period=1000ms) from Connector to Client
        // module carrying tuples of type GLOBAL_GAME_STATE
        application.addAppEdge("connector_" + this.appNameSifx, "client_" + this.appNameSifx, 100, new UniformDistribution(28*0.9, 28*1.1, SEED),
                1000, "GLOBAL_GAME_STATE_" + this.appNameSifx, Tuple.DOWN,
                AppEdge.MODULE);
        // adding edge from Client module to Display (actuator) carrying tuples of type SELF_STATE_UPDATE
        application.addAppEdge("client_" + this.appNameSifx, "DISPLAY_" + this.appNameSifx, new UniformDistribution(1000*0.9, 1000*1.1, SEED), 500,
                "SELF_STATE_UPDATE_" + this.appNameSifx, Tuple.DOWN, AppEdge.ACTUATOR);
        // adding edge from Client module to Display (actuator) carrying  tuples of type GLOBAL_STATE_UPDATE
        application.addAppEdge("client_" + this.appNameSifx, "DISPLAY_" + this.appNameSifx, new UniformDistribution(1000*0.9, 1000*1.1, SEED), 500,
                "GLOBAL_STATE_UPDATE_" + this.appNameSifx, Tuple.DOWN, AppEdge.ACTUATOR);

        // Defining the input-output relationships (represented by selectivity) of the application modules.

        // 0.9 tuples of type _SENSOR are emitted by Client module per incoming tuple of type EEG
        application.addTupleMapping("client_" + this.appNameSifx, "EEG_" + this.appNameSifx, "_SENSOR_" + this.appNameSifx,
                new FractionalSelectivity(0.9, SEED));
        // 1.0 tuples of type SELF_STATE_UPDATE are emitted by Client module per incoming tuple of type
        // CONCENTRATION
        application.addTupleMapping("client_" + this.appNameSifx, "CONCENTRATION_" + this.appNameSifx,
                "SELF_STATE_UPDATE_" + this.appNameSifx, new FractionalSelectivity(1.0, SEED));
        // 1.0 tuples of type CONCENTRATION are emitted by Concentration Calculator module
        // per incoming tuple of type _SENSOR
        application.addTupleMapping("concentration_calculator_" + this.appNameSifx, "_SENSOR_" + this.appNameSifx,
                "CONCENTRATION_" + this.appNameSifx, new FractionalSelectivity(1.0, SEED));
        // 1.0 tuples of type GLOBAL_STATE_UPDATE are emitted by Client module
        // per incoming tuple of type GLOBAL_GAME_STATE
        application.addTupleMapping("client_" + this.appNameSifx, "GLOBAL_GAME_STATE_" + this.appNameSifx,
                "GLOBAL_STATE_UPDATE_" + this.appNameSifx, new FractionalSelectivity(1.0, SEED));

        // Defining application loops to monitor the latency of.
        // Here, we add only one loop for monitoring :
        // EEG(sensor) -> Client -> Concentration Calculator -> Client -> DISPLAY (actuator)
        final AppLoop loop1 = new AppLoop(new ArrayList<String>() {{
            add("EEG_" + appNameSifx);
            add("client_" + appNameSifx);
            add("concentration_calculator_" + appNameSifx);
            add("client_" + appNameSifx);
            add("DISPLAY_" + appNameSifx);
        }});
        List<AppLoop> loops = new ArrayList<AppLoop>() {{
            add(loop1);
        }};
        application.setLoops(loops);

        return application;
    }

    protected void createEdgeDevices() {
        this.topology.getBotton().addEdge(
                new Level(dist, new HostConfig(1000, 10000, 10000, 270, 0), "m-VRGame",topology.getEdgeLatency())
                        .addAppModulesUp("connector_"+this.appNameSifx,1)
                        .addAppModulesUp("concentration_calculator_"+this.appNameSifx,1)
                        .addAppModules("client_"+this.appNameSifx,1)
                .addGroup(
                        new SensorLevel(1,"s-m-VRGame", "EEG_"+this.appNameSifx, new NormalDistribution(EEG_TRANSMISSION_TIME, SD_EEG_TRANSMISSION_TIME, SEED),6, fogBroker.getId(), this.appId + this.appNameSifx),
                        new ActuatorLevel(1,"d-m-VRGame", "DISPLAY_"+this.appNameSifx,1, fogBroker.getId(), this.appId + this.appNameSifx)
                )
        );
    }

    private void mapModules() {
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
                put(2, 10);
                put(3, 0);
                put(4, 10);
                put(5, 0);
                put(7, 0);
                put(8, 0);
                put(9, 0);
                put(10, 0);
                put(11, 0);
                put(12, 0);
            }
        }.get(config);
    }
}
