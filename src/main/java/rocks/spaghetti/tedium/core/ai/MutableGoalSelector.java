package rocks.spaghetti.tedium.core.ai;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import rocks.spaghetti.tedium.core.FakePlayer;

import java.util.EnumSet;

public class MutableGoalSelector extends Goal {
    private final FakePlayer entity;
    private final GoalSelector goalSelector;

    public MutableGoalSelector(FakePlayer entity) {
        this.entity = entity;
        this.goalSelector = new GoalSelector(entity.world.getProfilerSupplier());
        super.setControls(EnumSet.allOf(Goal.Control.class));
    }

    @Override
    public boolean canStart() {
        return true;
    }

    @Override
    public void tick() {

    }
}
