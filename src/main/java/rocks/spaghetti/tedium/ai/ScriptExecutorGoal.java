package rocks.spaghetti.tedium.ai;

import net.minecraft.entity.ai.goal.Goal;

public class ScriptExecutorGoal extends Goal {
    @Override
    public boolean canStart() {
        return true;
    }
}