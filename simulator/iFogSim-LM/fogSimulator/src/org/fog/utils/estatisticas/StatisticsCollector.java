package org.fog.utils.estatisticas;

import org.fog.placement.Controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;

abstract public class StatisticsCollector<T> {
    public static boolean onlyFinal = true;
    Controller controller;
    String prefix;

    public StatisticsCollector(Controller controller, String prefix) {
        this.controller = controller;
        this.prefix=prefix;
    }

    public void printMyStatistics(T[] rows, String filename){
        try {
            File file = new File("logs/"+ prefix+"_"+filename);

            //Cria pasta de logs caso não exista nos diretorios
            if(!file.getParentFile().exists()) file.getParentFile().mkdirs();

            if(file.exists())
                file.delete();

            Method[] methods = rows.getClass().getComponentType().getDeclaredMethods();
            StringBuilder builder = new StringBuilder();
            BufferedWriter writer = new BufferedWriter(new FileWriter("logs/"+ prefix+"_"+filename));
            for(Method m : methods){
                if(m.getParameterTypes().length==0 )
                {
                    if(m.getName().startsWith("get"))
                    {
                        builder.append(m.getName().substring(3)).append(',');
                    }
                    else if(m.getName().startsWith("is"))
                    {
                        builder.append(m.getName().substring(2)).append(',');
                    }

                }
            }
            builder.deleteCharAt(builder.length()-1);
            builder.append('\n');


            for (Object d : rows) {
                for (Method m : methods) {
                    if (m.getParameterTypes().length == 0) {
                        if (m.getName().startsWith("get") || m.getName().startsWith("is")) {
                            builder.append(m.invoke(d).toString()).append(',');
                        }
                    }
                }
                builder.deleteCharAt(builder.length() - 1);
                builder.append('\n');
            }
            builder.deleteCharAt(builder.length() - 1);

            writer.write(builder.toString());
            writer.close();

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public abstract void computeStatistics();
    public abstract void printMyStatistics();
}
