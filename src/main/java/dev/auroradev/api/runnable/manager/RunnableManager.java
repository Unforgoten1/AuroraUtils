package dev.auroradev.api.runnable.manager;

import dev.auroradev.api.runnable.AuroraRunnable;
import dev.auroradev.api.runnable.enums.ExecutionType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class RunnableManager {

    private static RunnableManager instance;
    private final Map<String , AuroraRunnable> runnableMap = new HashMap<>();

    public static RunnableManager getInstance(){
        if (instance == null){
            instance = new RunnableManager();
        }
        return instance;
    }

    public void registerRunnable(AuroraRunnable runnable){
        runnableMap.put(runnable.getName(), runnable);
        scheduleRunnable(runnable);
    }

    public boolean executeRunnable(String name){
        AuroraRunnable runnable = runnableMap.get(name);

        if (runnable == null){
            return false;
        }
        scheduleRunnable(runnable);
        return true;
    }

    private void scheduleRunnable(AuroraRunnable runnable){
        Plugin plugin = runnable.getPlugin();
        Runnable action = runnable.getRunnable();
        long delay = runnable.getDelay();
        long period = runnable.getPeriod();
        ExecutionType type = runnable.getExecutionType();

        switch (type){
            case SYNC:
                new BukkitRunnable(){
                    @Override
                    public void run(){
                        action.run();
                    }
                }.runTask(plugin);
                break;
            case ASYNC:
                new BukkitRunnable(){
                    @Override
                    public void run(){
                        action.run();
                    }
                }.runTaskAsynchronously(plugin);
                break;
            case TASK_LATER:
                new BukkitRunnable(){
                    @Override
                    public void run(){
                        action.run();
                    }
                }.runTaskLater(plugin, delay);
                break;
            case ASYNC_LATER:
                new BukkitRunnable(){
                    @Override
                    public void run(){
                        action.run();
                    }
                }.runTaskLaterAsynchronously(plugin, delay);
                break;
            case TASK_TIMER:
                if (period <= 0){
                    throw new IllegalStateException("period must be set for TASK_TIMER");
                }
                new BukkitRunnable(){
                    @Override
                    public void run(){
                        action.run();
                    }
                }.runTaskTimer(plugin,delay,period);
                break;
            case ASYNC_TIMER:
                if (period <= 0){
                    throw new IllegalStateException("period must be set for TASK_TIMER");
                }
                new BukkitRunnable(){
                    @Override
                    public void run(){
                        action.run();
                    }
                }.runTaskTimerAsynchronously(plugin,delay,period);
                break;
        }
    }

    public Map<String, AuroraRunnable> getRunnableMap(){
        return new HashMap<>(runnableMap);
    }
}
