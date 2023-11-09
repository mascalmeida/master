package org.fog.test.my;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.gui.core.Graph;
import org.fog.gui.example.FogGui;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.test.my.application.ApplicationBuilder;
import org.fog.test.my.application.VSOTApplication;
import org.fog.test.my.application.VRGameApplication;
import org.fog.test.my.topology.*;
import org.fog.utils.Logger;
import org.fog.utils.TimeKeeper;
import org.fog.utils.estatisticas.*;

import javax.swing.*;
import java.util.*;

public class Experimento {
    protected static String args[];

    protected static int SEED;
    protected static int placementStrategyConfig;
    protected static int nCloudlests;
    protected static String TOPOLOGY_TYPE;


    private MakeFog makeFog;
    private ModuleMapping moduleMapping;
    private Topology topology;
    private Graph graph;
    private String logsPrefix;

    public static Map getDistArgs(String args[], int app, int salt){

        System.out.println(">>>>> app = " + app);
        System.out.println(">>>>> salt = " + salt);
        System.out.print(">>>>> args[] = ");
        for(int i=0; i<args.length; i++){
            System.out.print(args[i] + "-");
        }
        System.out.println();

        List<Integer> config = new ArrayList<>();
        for(int i=4+app,j=0; i<args.length;i+=salt,j++){
            config.add(j);
            config.add(Integer.valueOf(args[i]));
            System.out.print(" i=" + args[i]);
            System.out.print(" j=" + j);
        }
        System.out.println();
        return dist(config.stream()
                .mapToInt(Integer::intValue)
                .toArray());
    }

    public static Map<Integer, Integer> dist(int... configs) {
        Map<Integer, Integer> distConfig = new HashMap<>();
        for (int i = 0; i < configs.length; i += 2) {
            distConfig.put(configs[i], configs[i + 1]);
        }
        return distConfig;
    }

    public void configureSimulator() {
        int num_user = 1;  //number of cloud users
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false; // trace events?
        CloudSim.init(num_user, calendar, trace_flag);
        ApplicationBuilder.setSeed(Experimento.SEED);
    }

    public void configureEnviroment() {
        try {
            Log.disable();
            moduleMapping = ModuleMapping.createModuleMapping();

            List<ApplicationBuilder> appsBuilds = configurerApplications(topology);

            makeFog = new MakeFog(topology, moduleMapping);
            graph = makeFog.makeNetwork();

            configureController(appsBuilds);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void showTopology() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                FogGui sdn = new ShowFog(graph);
                sdn.setVisible(true);
            }
        });
    }

    public void configureController(List<ApplicationBuilder> appsBuilds) {
        Controller controller = new Controller("master-controller",
                makeFog.getFogDevices(),
                makeFog.getSensors(),
                makeFog.getActuators(),
                moduleMapping, false);

        Logger.ENABLED = false;

        appsBuilds.forEach(app -> controller.submitApplication(app.getApplication(), app.getDelay()));

        List<StatisticsCollector> statisticsCollectors = Arrays.asList(
                new ModulesRuningInDevice(controller, logsPrefix),
                new AverageLoopDelay(controller, logsPrefix),
                new CPUUsage(controller, logsPrefix),
                new Network(controller, logsPrefix),
                new TupleIdDelay(controller, logsPrefix),
                new TupleIdExecutionTime(controller, logsPrefix)
        );

        controller.setStatisticsCollectors(statisticsCollectors);
    }

    public void startSimulation() {
        TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
        CloudSim.startSimulation();
        Log.printLine("Teste");
        CloudSim.stopSimulation();
        Log.printLine("Simulation finished!!");
    }

    public Experimento configure() {
        configureSimulator();
        configureEnviroment();
        return this;
    }

    public Experimento setLogsPrefix(String logsPrefix) {
        this.logsPrefix = logsPrefix;
        return this;
    }

    public void setTopology(Topology topology) {
        this.topology = topology;
    }

    public List<ApplicationBuilder> configurerApplications(Topology topology) throws Exception {
        List<ApplicationBuilder> applicationBuilders = new ArrayList<>();
        applicationBuilders.add(
                new VSOTApplication(topology,
                        getDistArgs(args,0,nCloudlests),
                        VSOTApplication.configureMappingStrategy(placementStrategyConfig),
                        VSOTApplication.configureMappingDelay(placementStrategyConfig))
        );

//        applicationBuilders.add(
//                new LeakSWMApplication(topology,
//                        getDistArgs(args,2,nCloudlests),
//                        LeakSWMApplication.configureMappingStrategy(placementStrategyConfig),
//                        LeakSWMApplication.configureMappingDelay(placementStrategyConfig))
//        );

        Map<Integer, Integer> map = getDistArgs(args, 1,nCloudlests);
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            Integer idx = entry.getKey();
            Integer nMobiles = entry.getValue();
            //nMobiles = 0;
            if (nMobiles > 0) {
                applicationBuilders.add(
                        new VRGameApplication(topology,
                                dist(idx, nMobiles),
                                VRGameApplication.configureMappingStrategy(placementStrategyConfig),
                                String.valueOf(idx),
                                VRGameApplication.configureMappingDelay(placementStrategyConfig))
                );
            }
        }

        return applicationBuilders;
    }

    // <SEED> <ALG> <CLOUDLETS> ... ... ...
    public static void main(String args[]) throws Exception {
        long startTime = System.nanoTime();

        Experimento.SEED = Integer.parseInt(args[0]);
        Experimento.TOPOLOGY_TYPE = args[1];
        Experimento.placementStrategyConfig = Integer.parseInt(args[2]);
        Experimento.nCloudlests = Integer.parseInt(args[3]);

        Experimento.args = args;

        Topology topology = null;
        switch (TOPOLOGY_TYPE){
            case "A":
                topology = new TopologyA();
                break;
            case "B":
                topology = new TopologyB();
                break;
            case "C":
                topology = new TopologyC();
                break;
            case "D":
                topology = new TopologyD();
                break;
            case "A2":
                topology = new TopologyA2();
                break;
            case "D2":
                topology = new TopologyD2();
                break;
        }

        new Experimento(topology)
                .setLogsPrefix(TOPOLOGY_TYPE+"/"+TOPOLOGY_TYPE+"_"+Experimento.SEED+"_"+Runner.placementStrategyConfig+"_"+args[8])
                .configure()
                .startSimulation();

    }

    public Experimento(Topology topology) {
        this.topology = topology;
    }
}
