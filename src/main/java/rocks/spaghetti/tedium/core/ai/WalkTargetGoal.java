package rocks.spaghetti.tedium.core.ai;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.ai.TargetFinder;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import rocks.spaghetti.tedium.Util;

import java.util.EnumSet;

public class WalkTargetGoal extends Goal {
    private final PathAwareEntity entity;
    private double x;
    private double y;
    private double z;

    public WalkTargetGoal(PathAwareEntity entity) {
        this.entity = entity;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (this.entity.isInWalkTargetRange()) {
            return false;
        }

        return calculateNewTarget();
    }

    private boolean calculateNewTarget() {
        Vec3d vec3d = TargetFinder.findTargetTowards(this.entity, 16, 4, Vec3d.ofBottomCenter(this.entity.getPositionTarget()));
        if (vec3d == null) {
            return false;
        } else {
            this.x = vec3d.x;
            this.y = vec3d.y;
            this.z = vec3d.z;
            return true;
        }
    }

    @Override
    public boolean shouldContinue() {
        return !this.entity.getNavigation().isIdle() || !this.entity.isInWalkTargetRange();
    }

    @Override
    public void start() {
        this.entity.getNavigation().startMovingTo(this.x, this.y, this.z, 1.0D);
    }

    @Override
    public void stop() {
        this.entity.setPositionTarget(BlockPos.ORIGIN, -1);
    }

    @Override
    public void tick() {
        if (!this.entity.isInWalkTargetRange()
                && this.entity.squaredDistanceTo(Util.Vec3iToVec3d(this.entity.getNavigation().getTargetPos())) < 8
                && calculateNewTarget()) {
            this.entity.getNavigation().startMovingTo(this.x, this.y, this.z, 1.0D);
        }

        MinecraftClient.getInstance().options.keySprint.setPressed(true);
    }
}
