package rocks.spaghetti.tedium.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterestType;
import rocks.spaghetti.tedium.Constants;
import rocks.spaghetti.tedium.mixin.ActivityInvoker;

import java.util.Optional;

public class Activities {
    private Activities() { throw new IllegalStateException("Utility Class"); }

    private static Activity register(String id) {
        return ActivityInvoker.invokeRegister(Constants.MOD_ID + "_" + id);
    }

    public static final Activity CORE = register("core");
    public static ImmutableList<Pair<Integer, Task<? super FakePlayer>>> createCoreTasks() {
        return ImmutableList.of(
                Pair.of(0, new StayAboveWaterTask(0.8F)),
                Pair.of(0, new OpenDoorsTask()),
                Pair.of(10, new GoHomeTask())
        );
    }
    private static class GoHomeTask extends Task<FakePlayer> {
        public GoHomeTask() {
            super(ImmutableMap.of(MemoryModuleType.HOME, MemoryModuleState.VALUE_PRESENT));
        }

        @Override
        protected boolean shouldRun(ServerWorld world, FakePlayer entity) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return false;

            Optional<GlobalPos> homeOptional = entity.getBrain().getOptionalMemory(MemoryModuleType.HOME);
            if (!homeOptional.isPresent()) return false;

            GlobalPos home = homeOptional.get();
            if (home.getDimension() != client.player.world.getRegistryKey()) return false;

            Vec3d homePos = new Vec3d(home.getPos().getX(), home.getPos().getY(), home.getPos().getZ());
            if (client.player.squaredDistanceTo(homePos) < 8) return false;

            return true;
        }

        @Override
        protected void run(ServerWorld world, FakePlayer entity, long time) {
//            entity.getNavigation()
        }
    }
}
