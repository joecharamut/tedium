package rocks.spaghetti.tedium.interaction.action;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.options.GameOptions;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import rocks.spaghetti.tedium.Util;

public class MoveAction implements ClientAction {
    private final Vec3d endPosition;
    private final MinecraftClient client = MinecraftClient.getInstance();
    private boolean doneMoving = false;

    public MoveAction(Vec3d movement) {
        this.endPosition = Util.applyOffsetWithFacing(client.player.getHorizontalFacing(), client.player.getPos(), movement);
    }

    public MoveAction(double x, double y, double z) {
        this(new Vec3d(x, y, z));
    }

    @Override
    public boolean done() {
        return doneMoving;
    }

    @Override
    public void tick(MinecraftClient client) {
        doneMoving = client.player.getPos().distanceTo(endPosition) < 0.1;

        client.options.keyForward.setPressed(false);
        client.options.keyBack.setPressed(false);
        client.options.keyLeft.setPressed(false);
        client.options.keyRight.setPressed(false);

        if (!doneMoving) {
            client.options.keyForward.setPressed(true);
        }
    }
}
