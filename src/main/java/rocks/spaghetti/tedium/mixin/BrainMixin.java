package rocks.spaghetti.tedium.mixin;

import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.Memory;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.task.Task;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.*;

@Mixin(Brain.class)
public interface BrainMixin<E> {
    @Accessor("memories")
    Map<MemoryModuleType<?>, Optional<? extends Memory<?>>> getMemories();

    @Accessor("sensors")
    Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> getSensors();

    @Accessor("tasks")
    Map<Integer, Map<Activity, Set<Task<? super E>>>> getTasks();

    @Accessor("possibleActivities")
    Set<Activity> getPossibleActivities();
}
