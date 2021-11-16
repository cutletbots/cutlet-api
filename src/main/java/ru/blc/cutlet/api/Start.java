package ru.blc.cutlet.api;

import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

public class Start {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
        Cutlet c = new Cutlet();
        c.start();
        long time = System.currentTimeMillis() - start;
        c.getLogger().info("Cutlet started at {}s {}ms", time / 1000, time % 1000);
    }
}
