package org.fog.test.my.topology;

import org.fog.test.my.utils.HostConfig;
import org.fog.test.my.utils.Level;

public class TopologyD2 extends Topology {

    private void configNetwork(){
        Level top = new Level(1, new HostConfig(44800, 40000, 10000, 10000, 0.011), "cloud",0);
        Level botton = top
                .addNext(new Level(1, new HostConfig(1, 4000, 10000, 10000, 0.0), "proxy-server",100))
                .addNext(new Level(3, new HostConfig(2000, 4000, 10000, 10000,0.0), "cloudlet",25))
                .addNext(new Level(1, new HostConfig(1500, 4000, 10000, 10000,0.0), "cloudlet",25))
                .addNext(new Level(1, new HostConfig(1000, 4000, 10000, 10000,0.0), "cloudlet",25));
        setTop(top);
        setBottom(botton);
    }

    public TopologyD2() {
        super("D2",4);
        configNetwork();
    }
}
