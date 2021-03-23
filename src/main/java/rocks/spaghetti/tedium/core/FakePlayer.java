package rocks.spaghetti.tedium.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.control.JumpControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.MobVisibilityCache;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import rocks.spaghetti.tedium.Log;
import rocks.spaghetti.tedium.core.compat.ClientBrain;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

// FakePlayer -> PathAwareEntity -> MobEntity -> LivingEntity -> Entity
public class FakePlayer extends PathAwareEntity {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static AttributeContainer attributes;
    private static FakePlayer imposter = null;

    private final ClientPlayerEntity realPlayer;
    private final MobVisibilityCache visibilityCache;
    private ClientBrain clientBrain;
    private Brain<FakePlayer> myBrain;

    public FakePlayer(ClientPlayerEntity realPlayer) {
        super(EntityType.ZOMBIE, realPlayer.world);
        this.realPlayer = realPlayer;
        this.setAiDisabled(true);

        this.lookControl = new FakeLookControl(this);
        this.jumpControl = new FakeJumpControl(this);

        this.visibilityCache = new MobVisibilityCache(this);
    }

    public static FakePlayer create(ClientPlayerEntity realPlayer) {
        attributes = new AttributeContainer(LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.10000000149011612D)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED)
                .add(EntityAttributes.GENERIC_LUCK)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0D)
                .add(EntityAttributes.GENERIC_ARMOR, 2.0D)
        .build());


        Map<Method, Method> playerMethods = new HashMap<>();
        for (Method method : realPlayer.getClass().getMethods()) {
            playerMethods.put(method, method);
        }

        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(FakePlayer.class);
        factory.setFilter(playerMethods::containsKey);

        MethodHandler handler = (self, thisMethod, proceed, args) -> {
            Method redirect = playerMethods.getOrDefault(thisMethod, null);
            if (redirect != null) {
                return redirect.invoke(realPlayer, args);
            }

            Log.error("Handler: Could not invoke {}({})", thisMethod.getName(), Arrays.toString(thisMethod.getParameters()));
            return null;
        };

        try {
            imposter = (FakePlayer) factory.create(new Class<?>[]{ClientPlayerEntity.class}, new Object[]{realPlayer}, handler);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            Log.catching(e);
        }
        return imposter;
    }

    public static FakePlayer get() {
        return imposter;
    }

    @Override
    public void setAiDisabled(boolean disabled) {
        if (disabled) {
            realPlayer.input = new KeyboardInput(client.options);
        } else {
            realPlayer.input = new FakeInput();
        }

        super.setAiDisabled(disabled);
    }

    @Override
    public void initGoals() {
        Log.info("initGoals()");
        this.goalSelector.add(0, new SwimGoal(this));
        Log.info("end initGoals()");
    }

    public List<PrioritizedGoal> getGoals() {
        return this.goalSelector.getRunningGoals().collect(Collectors.toList());
    }

    @Override
    public Brain<?> getBrain() {
        return this.myBrain;
    }

    public void initBrain() {
        Log.info("initBrain()");
        List<MemoryModuleType<?>> memories = Arrays.asList(
                MemoryModuleType.HOME,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE
        );
        List<SensorType<? extends Sensor<? super FakePlayer>>> sensors = Arrays.asList(

        );

        myBrain = new Brain<>(memories, sensors, ImmutableList.of(), () -> Brain.createBrainCodec(memories, sensors));
        this.brain = myBrain;

        if (true) return;

        myBrain.remember(MemoryModuleType.HOME, GlobalPos.create(World.OVERWORLD, new BlockPos(217, 66, -123)));

        myBrain.setTaskList(Activities.CORE, Activities.createCoreTasks());
        brain.setCoreActivities(ImmutableSet.of(Activities.CORE));
        brain.setDefaultActivity(Activities.CORE);
        brain.doExclusively(Activities.CORE);

        this.clientBrain = new ClientBrain(myBrain);
        Log.info("end initBrain()");
    }

    // ========================= \/ Compatibility Layers \/ =========================

    private class FakeInput extends KeyboardInput {
        public FakeInput() {
            super(null);
        }

        @Override
        public void tick(boolean slowDown) {
            this.pressingForward = forwardSpeed > 0;
            this.pressingBack = forwardSpeed < 0;
            this.pressingRight = sidewaysSpeed > 0;
            this.pressingLeft = sidewaysSpeed < 0;

            if (this.pressingForward == this.pressingBack) {
                this.movementForward = 0.0F;
            } else if (this.pressingForward) {
                this.movementForward = 1.0F;
            } else {
                this.movementForward = -1.0F;
            }

            if (this.pressingLeft == this.pressingRight) {
                this.movementSideways = 0.0F;
            } else if (this.pressingLeft) {
                this.movementSideways = 1.0F;
            } else {
                this.movementSideways = -1.0F;
            }

//            this.sneaking = sneaking;
            if (slowDown) {
                this.movementSideways = (float)((double)this.movementSideways * 0.3D);
                this.movementForward = (float)((double)this.movementForward * 0.3D);
            }
        }
    }

    private class FakeLookControl extends LookControl {
        public FakeLookControl(MobEntity entity) {
            super(entity);
        }

        @Override
        public void tick() {
            if (this.shouldStayHorizontal()) {
                realPlayer.pitch = 0.0F;
            }

            if (this.active) {
                this.active = false;
                realPlayer.yaw = this.changeAngle(realPlayer.yaw, this.getTargetYaw(), this.yawSpeed);
                realPlayer.pitch = this.changeAngle(realPlayer.pitch, this.getTargetPitch(), this.pitchSpeed);
            } else {
                realPlayer.yaw = this.changeAngle(realPlayer.yaw, realPlayer.bodyYaw, 10.0F);
            }

            if (!getNavigation().isIdle()) {
                realPlayer.yaw = MathHelper.stepAngleTowards(realPlayer.yaw, realPlayer.bodyYaw, 1.0f);
            }

        }
    }

    private class FakeJumpControl extends JumpControl {
        public FakeJumpControl(MobEntity entity) {
            super(entity);
        }

        @Override
        public void tick() {
            realPlayer.input.jumping = this.active;
            this.active = false;
        }
    }

    private class FakeBodyControl extends BodyControl {
        public FakeBodyControl(MobEntity entity) {
            super(entity);
            assert realPlayer != null;
        }

        @Override
        public void tick() {
            Log.error("STUB: FakeBodyControl.tick()");
            super.tick();
        }
    }

    @Override
    protected BodyControl createBodyControl() {
        return new FakeBodyControl(this);
    }

    @Override
    public int getLookPitchSpeed() {
        return 40;
    }

    @Override
    public int getBodyYawSpeed() {
        return 75;
    }

    @Override
    public int getLookYawSpeed() {
        return 10;
    }

    @Override
    public void tickNewAi() {
        if (!this.isAiDisabled()) {
//            this.clientBrain.tick(client.world, this);
            this.visibilityCache.clear();
            this.targetSelector.tick();
            this.goalSelector.tick();
            this.navigation.tick();
            this.mobTick();
            this.moveControl.tick();
            this.lookControl.tick();
            this.jumpControl.tick();
        }
    }

    @Override
    public void lookAtEntity(Entity targetEntity, float maxYawChange, float maxPitchChange) {
        Log.error("STUB: lookAtEntity({}, {}, {})", targetEntity, maxYawChange, maxPitchChange);
    }

    @Override
    public void setBoundingBox(Box boundingBox) {
        if (realPlayer == null) {
            Log.warn("Can't set bounding box, realPlayer == null: {}", boundingBox);
            return;
        }
        realPlayer.setBoundingBox(boundingBox);
    }

    @Override
    public AttributeContainer getAttributes() {
        if (realPlayer != null) {
            return realPlayer.getAttributes();
        }
        return attributes;
    }

    @Override
    public double getAttributeValue(EntityAttribute attribute) {
        if (realPlayer != null && realPlayer.getAttributes().hasAttribute(attribute)) {
            return realPlayer.getAttributeValue(attribute);
        }
        return attributes.getValue(attribute);
    }

    @Override
    public double getAttributeBaseValue(EntityAttribute attribute) {
        if (realPlayer != null && realPlayer.getAttributes().hasAttribute(attribute)) {
            return realPlayer.getAttributeBaseValue(attribute);
        }
        return attributes.getBaseValue(attribute);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FakePlayer that = (FakePlayer) o;
        return realPlayer.equals(that.realPlayer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), realPlayer);
    }
}
