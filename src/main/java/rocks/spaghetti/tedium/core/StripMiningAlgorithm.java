package rocks.spaghetti.tedium.core;

import io.herrmann.generator.Generator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import rocks.spaghetti.tedium.Util;
import rocks.spaghetti.tedium.interaction.ClientInteractionHelper;
import rocks.spaghetti.tedium.interaction.PlayerInventoryHelper;
import rocks.spaghetti.tedium.interaction.action.BlockBreakAction;
import rocks.spaghetti.tedium.interaction.action.CallbackAction;
import rocks.spaghetti.tedium.interaction.action.ClientAction;
import rocks.spaghetti.tedium.interaction.action.MoveAction;

public class StripMiningAlgorithm {
    public void loop(int count, MinecraftClient client, ClientInteractionHelper helper) {
        for (int i = 0; i < count; i++) {
            oneStep(client, helper);
        }
    }

    public void oneStep(MinecraftClient client, ClientInteractionHelper helper) {
        BlockPos playerPos = client.player.getBlockPos();
        Vec3d lookVector = Util.Vec3fToVec3d(client.player.getMovementDirection().getUnitVector());

        helper.enqueue(newActionGroup());
    }

    public static Generator<ClientAction> createActionTree() {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        PlayerInventoryHelper inventoryHelper = new PlayerInventoryHelper();

        return new Generator<ClientAction>() {
            @Override
            protected void run() throws InterruptedException {
                // begin
                switch (player.getMovementDirection()) {
                    case NORTH:
                        player.yaw = 180.0f;
                        break;
                    case EAST:
                        player.yaw = 270.0f;
                        break;
                    case SOUTH:
                        player.yaw = 0.0f;
                        break;
                    case WEST:
                        player.yaw = 90.0f;
                        break;

                    default: break;
                }

                if (inventoryHelper.getEmptySlots().length < 2) {
                    // empty inventory to chests
                }

                this.yield(new BlockBreakAction(1, 1, 0)); // head block
                this.yield(new BlockBreakAction(1, 0, 0)); // foot block
//                this.yield(new MoveAction(Util.Vec3fToVec3d(player.getMovementDirection().getUnitVector())));
            }
        };
    }

    public ClientAction[] newActionGroup() {
        /*
        * begin
        * check inventory
        *   - if < x empty slots: tx some to shulkers/enderchest
        * check block
        *   - gravel, sand => deal with gravity (check above blocks w/ WorldView?)
        *   - water, lava => panic
        *   - monumentally cursed solution: Action subclass repr. action tree
        * dig player hole
        *   - switch to correct tool (impl. in BlockBreakAction?)
        *   - place any blocks over lava/water
        * move forwards
        *   - ensure facing is DIRECTLY straight (no walking into lava here)
        * "xray" any nearby ore (no eyes so its not REALLY xray right)
        *   - if any: continue
        *   - else: loop
        * dig to ore
        *   - similar to dig player hole
        * mine vein
        *   - good luck with this one
        * return to corridor
        * snap back to forward facing
        * loop
        *
        * */
        return new ClientAction[]{
                new CallbackAction(() -> {

                }),
                new BlockBreakAction(1, 0, 0),
                new BlockBreakAction(1, 1, 0),
                new CallbackAction(() -> {
                    // todo: check blocks
                })
        };
    }
}
