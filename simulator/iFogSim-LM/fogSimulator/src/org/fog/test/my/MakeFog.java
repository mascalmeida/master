package org.fog.test.my;

import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.gui.core.*;
import org.fog.placement.ModuleMapping;
import org.fog.test.my.topology.Topology;
import org.fog.test.my.utils.ActuatorLevel;
import org.fog.test.my.utils.Level;
import org.fog.test.my.utils.SensorLevel;

import java.util.ArrayList;
import java.util.List;


class MakeFog {
    private Topology topology;
    private ModuleMapping moduleMapping;

    private List<FogDevice> fogDevices;
    private List<Sensor> sensors;
    private List<Actuator> actuators;
    private Graph graph;

    public void makeTopology(Level level,
                             Graph graph,
                             String sufix,
                             int levelNumber,
                             Node parent,
                             int parentIndex,
                             FogDevice parentDevice){

        if(level == null) return;

        Integer hostPerParent = level.getHostPerParent();

        if(hostPerParent == null){
            hostPerParent = level.getChildrensDists().getOrDefault(parentIndex,0);
        }

        for (int i = 0; i < hostPerParent; i++) {

            String newSufix = sufix + (levelNumber < 2 ? "" : ("-" + i));
            String finalName = level.getPrefix() + newSufix;

            Node node;

            if(level instanceof SensorLevel) {
                SensorLevel sensorLevel = (SensorLevel) level;
                node = new SensorGui(finalName, sensorLevel.getType(), sensorLevel.getDistribution(), sensorLevel.getUserId(), sensorLevel.getAppId(), parentIndex+1);
            } else if(level instanceof ActuatorLevel){
                ActuatorLevel actuatorLevel = (ActuatorLevel) level;
                node = new ActuatorGui(finalName, actuatorLevel.getType(), actuatorLevel.getAppId(), actuatorLevel.getUserId());
            } else {
                node = new FogDeviceGui(finalName,
                        level.getHostConfig().getMips(),
                        level.getHostConfig().getRam(),
                        level.getHostConfig().getUpBw(),
                        level.getHostConfig().getDownBw(),
                        levelNumber,
                        level.getHostConfig().getRate());
            }


            final FogDevice device = node instanceof FogDeviceGui ? ((FogDeviceGui) node).convertToSimEntity() : null;

            graph.addNode(node);

            if(node instanceof FogDeviceGui){
                fogDevices.add(device);
                FogDeviceGui deviceGui = (FogDeviceGui) node;

                if(parent == null){
                    device.setParentId(-1);
                } else {
                    device.setParentId(parentDevice.getId());
                    device.setUplinkLatency(level.getUpLatency());
                }

                //Adiciona o modulo das aplicações aos dispostivos
                level.getModulesCount()
                        .keySet()
                        .forEach(moduleName -> {
                            moduleMapping.addModuleToDevice(
                                    moduleName,
                                    device.getName(),
                                    level.getModulesCount().get(moduleName)
                            );
                        });

                level.getModulesCountUp()
                        .keySet()
                        .forEach(moduleName -> {
                            int currentCount = 0;
                            if (moduleMapping.getNodeToModuleCountMapping().containsKey(parentDevice.getName()) &&
                                    moduleMapping.getNodeToModuleCountMapping().get(parentDevice.getName()).containsKey(moduleName))
                                currentCount = moduleMapping.getNodeToModuleCountMapping().get(parentDevice.getName()).get(moduleName);

                            int newCount = currentCount + level.getModulesCountUp().get(moduleName);

                            if(newCount > 1){
                                moduleMapping
                                        .getNodeToModuleCountMapping()
                                        .get(parentDevice.getName())
                                        .put(moduleName, newCount);
                            } else {
                                moduleMapping.addModuleToDevice(
                                        moduleName,
                                        parentDevice.getName(),
                                        newCount > 0 ? newCount : 1
                                );
                            }
                        });
            }

            if(node instanceof SensorGui){
                Sensor sensor = ((SensorGui) node).convertToSimEntity();
                sensor.setGatewayDeviceId(parentDevice.getId());
                sensor.setLatency(level.getUpLatency());

                sensors.add(sensor);
            }

            if(node instanceof ActuatorGui){
                Actuator actuator = ((ActuatorGui) node).convertToSimEntity();
                actuator.setGatewayDeviceId(parentDevice.getId());
                actuator.setLatency(level.getUpLatency());

                actuators.add(actuator);
            }

            if (parent != null) {
                Edge edge = new Edge(parent, level.getUpLatency());
                graph.addEdge(node, edge);
            }

            int finalI = i+parentIndex;
            level.getChildrens().forEach(next ->
                    makeTopology(next, graph, newSufix, levelNumber + 1, node, finalI, device)
            );
        }
    }

    public void makeTopology(Level level, Graph graph){
        makeTopology(level, graph, "",0, null,0, null);
    }

    public Graph makeNetwork(){
        this.graph = new Graph();
        this.fogDevices = new ArrayList<>();
        this.sensors = new ArrayList<>();
        this.actuators = new ArrayList<>();

        makeTopology(this.topology.getTop(), graph);
        return graph;
    }

    public MakeFog(Topology topology, ModuleMapping moduleMapping) {
        this.topology = topology;
        this.moduleMapping = moduleMapping;
    }

    public List<FogDevice> getFogDevices() {
        return fogDevices;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public List<Actuator> getActuators() {
        return actuators;
    }

    public Graph getGraph() {
        return graph;
    }
}
