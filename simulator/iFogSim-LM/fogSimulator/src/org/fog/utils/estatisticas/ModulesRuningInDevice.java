package org.fog.utils.estatisticas;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.entities.FogDevice;
import org.fog.placement.Controller;
import org.fog.utils.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModulesRuningInDevice extends StatisticsCollector<ModulesRuningInDevice.ProcessoXDispostivo> {

    private List<ProcessoXDispostivo> processoXDispostivos = new ArrayList<>();

    public ModulesRuningInDevice(Controller controller, String filePrefix) {
        super(controller,filePrefix);
    }

    @Override
    public void computeStatistics(){
        double time = CloudSim.clock();
        if(onlyFinal && ! (time== Config.MAX_SIMULATION_TIME-1)) return;
        for(FogDevice fogDevice: controller.getFogDevices()){
            for(Map.Entry<String, Map<String, Integer>> appModuleCount : fogDevice.getModuleInstanceCount().entrySet()){
                for(Map.Entry<String, Integer> processoXConunt : appModuleCount.getValue().entrySet()){
                    ProcessoXDispostivo row = new ProcessoXDispostivo();
                    row.time = time;
                    row.count = processoXConunt.getValue();
                    row.module = processoXConunt.getKey();
                    row.dispostivo = fogDevice.getName();
                    row.application = appModuleCount.getKey();
                    row.level = fogDevice.getLevel();
                    this.processoXDispostivos.add(row);
                }
            }
        }
    }

    public void printMyStatistics(){
        this.printMyStatistics(processoXDispostivos.toArray(new ProcessoXDispostivo[0]),"processoXDispostivo.csv");
    }

    public class ProcessoXDispostivo {
        double time;
        String application;
        String module;
        String dispostivo;
        int count;
        int level;

        public double getTime() {
            return time;
        }

        public String getModule() {
            return module;
        }

        public String getDispostivo() {
            return dispostivo;
        }

        public int getCount() {
            return count;
        }

        public String getApplication() {
            return application;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }
    }

}
