package org.fog.utils.estatisticas;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppModule;
import org.fog.entities.FogDevice;
import org.fog.placement.Controller;
import org.fog.utils.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CPUUsage extends StatisticsCollector<CPUUsage.CPUUsageRow> {
    List<CPUUsageRow> rows = new ArrayList<>();
    public CPUUsage(Controller controller,String filePrefix) {
        super(controller,filePrefix);
    }

    @Override
    public void computeStatistics() {
        double time = CloudSim.clock();

//        if(onlyFinal && ! (time== Config.MAX_SIMULATION_TIME-1)) return;

        for(FogDevice fogDevice: controller.getFogDevices()){


            fogDevice.getHost()
                    .getVmList().stream()
                    .map(vm -> (AppModule)vm)
                    .forEach(vm -> {

                        if(vm.getCloudletScheduler()!=null
                                && vm.getCloudletScheduler().getCurrentMipsShare()!=null
                                && vm.getCloudletScheduler().getCurrentMipsShare().size()>0) {
                            CPUUsageRow row = new CPUUsageRow();

                            row.time = time;
                            row.device = fogDevice.getName();
                            row.cpuTotal = fogDevice.getHost().getTotalMips();
                            row.vm = vm.getName();
                            row.instance = vm.getUserId();
                            row.cpuUsage = vm.getCloudletScheduler().getCurrentMipsShare().get(0);

                            rows.add(row);
                        }

                    });
        }
    }

    public void printMyStatistics() {
        this.printMyStatistics(rows.toArray(new CPUUsageRow[0]), "cpuUsage.csv");
    }

    public class CPUUsageRow {
        double time;
        double cpuUsage;
        double cpuTotal;
        String device;
        String vm;
        int instance;

        public String getVm() {
            return vm;
        }

        public int getInstance() {
            return instance;
        }

        public double getTime() {
            return time;
        }

        public double getCpuUsage() {
            return cpuUsage;
        }

        public String getDevice() {
            return device;
        }

        public double getCpuTotal() {
            return cpuTotal;
        }
    }
}
