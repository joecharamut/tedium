package rocks.spaghetti.tedium.core.ai;

import net.minecraft.entity.ai.goal.Goal;
import rocks.spaghetti.tedium.core.FakePlayer;

import java.util.EnumSet;

public class TaskRunner extends Goal {
    private final FakePlayer entity;

    public TaskRunner(FakePlayer entity) {
        this.entity = entity;
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
