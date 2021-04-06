package rocks.spaghetti.tedium.ai;

import net.minecraft.entity.ai.goal.Goal;
import rocks.spaghetti.tedium.core.FakePlayer;

public class CraftItemGoal extends Goal {
    private final FakePlayer entity;

    public CraftItemGoal(FakePlayer entity) {
        this.entity = entity;
    }

    @Override
    public boolean canStart() {
        return false;
    }
}
