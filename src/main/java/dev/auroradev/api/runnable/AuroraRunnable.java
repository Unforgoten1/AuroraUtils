package dev.auroradev.api.runnable;

import dev.auroradev.api.runnable.enums.ExecutionType;
import dev.auroradev.api.runnable.manager.RunnableManager;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class AuroraRunnable {
    private final String name;
    private Plugin plugin;
    private long delay;
    private long period;
    private ExecutionType executionType;
    private Runnable runnable;

    public AuroraRunnable(String name) {
        this.name = name;
        this.delay = 0;
        this.period = -1;
    }

    public AuroraRunnable plugin (Plugin plugin){
        this.plugin = plugin;
        return this;
    }

    public AuroraRunnable delay(long delay, TimeUnit unit){
        this.delay = unit.toSeconds(delay) * 20;
        return this;
    }

    public AuroraRunnable period(long period, TimeUnit unit){
        this.period = unit.toSeconds(period) * 20;
        return this;
    }

    public AuroraRunnable executionType(ExecutionType type){
        this.executionType = type;
        return this;
    }

    public AuroraRunnable execution(Runnable action){
        this.runnable = action;
        return this;
    }

    public void register(){
        if (plugin ==null || executionType == null || runnable == null){
            throw new IllegalStateException("plugin, executiontype, and action must be set");
        }
        RunnableManager.getInstance().registerRunnable(this);
    }

    public String getName(){
        return name;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public long getDelay() {
        return delay;
    }

    public ExecutionType getExecutionType() {
        return executionType;
    }

    public long getPeriod() {
        return period;
    }

    public Runnable getRunnable() {
        return runnable;
    }
}
