package rocks.spaghetti.tedium.core.compat;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.Memory;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.Task;
import rocks.spaghetti.tedium.Log;
import rocks.spaghetti.tedium.core.FakePlayer;
import rocks.spaghetti.tedium.mixin.BrainMixin;

import java.util.*;

public class ClientBrain {
    private final Map<SensorType<? extends Sensor<? super FakePlayer>>, Sensor<? super FakePlayer>> sensors;
    private final Map<MemoryModuleType<?>, Optional<? extends Memory<?>>> memories;
    private final Map<Integer, Map<Activity, Set<Task<? super FakePlayer>>>> tasks;

    private final Brain<FakePlayer> brain;

    @SuppressWarnings("unchecked")
    public ClientBrain(Brain<FakePlayer> brain) {
        this.brain = brain;

        this.sensors = ((BrainMixin<FakePlayer>) brain).getSensors();
        this.memories = ((BrainMixin<FakePlayer>) brain).getMemories();
        this.tasks = ((BrainMixin<FakePlayer>) brain).getTasks();
    }

    @SuppressWarnings("unchecked")
    private Set<Activity> getPossibleActivities() {
        return ((BrainMixin<FakePlayer>) brain).getPossibleActivities();
    }

    public void tick(ClientWorld world, FakePlayer entity) {
        this.tickMemories();
        this.tickSensors(world, entity);
        this.startTasks(world, entity);
        this.updateTasks(world, entity);
    }

    protected void tickMemories() {
        for (Map.Entry<MemoryModuleType<?>, Optional<? extends Memory<?>>> entry : this.memories.entrySet()) {
            Optional<? extends Memory<?>> value = entry.getValue();
            if (value.isPresent()) {
                Memory<?> memory = value.get();
                memory.tick();
                if (memory.isExpired()) {
                    brain.forget(entry.getKey());
                }
            }
        }
    }

    private void tickSensors(ClientWorld world, FakePlayer entity) {
        for (Sensor<? super FakePlayer> sensor : this.sensors.values()) {
//            sensor.tick(world, entity);
            Log.info("tickSensors() -> for ({})", sensor);
        }
    }

    private void updateTasks(ClientWorld world, FakePlayer entity) {
        long time = world.getTime();

        for (Task<? super FakePlayer> task : brain.getRunningTasks()) {
            task.tick(FakeServerWorld.create(world), entity, time);
        }
    }

    private void startTasks(ClientWorld world, FakePlayer entity) {
        long time = world.getTime();

        label34:
        for(Map<Activity, Set<Task<? super FakePlayer>>> activityToTasks : this.tasks.values()) {
            Iterator<Map.Entry<Activity, Set<Task<? super FakePlayer>>>> taskIter = activityToTasks.entrySet().iterator();

            while(true) {
                Map.Entry<Activity, Set<Task<? super FakePlayer>>> entry;
                Activity activity;
                do {
                    if (!taskIter.hasNext()) {
                        continue label34;
                    }

                    entry = taskIter.next();
                    activity = entry.getKey();
                } while(!this.getPossibleActivities().contains(activity));

                for (Task<? super FakePlayer> task : entry.getValue()) {
                    if (task.getStatus() == Task.Status.STOPPED) {
                        task.tryStarting(FakeServerWorld.create(world), entity, time);
                    }
                }
            }

        }
    }
}
