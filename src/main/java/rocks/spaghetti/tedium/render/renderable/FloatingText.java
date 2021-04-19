package rocks.spaghetti.tedium.render.renderable;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class FloatingText implements Renderable {
    private final Text text;
    private final BlockPos pos;

    public FloatingText(Text text, BlockPos pos) {
        this.text = text;
        this.pos = pos;
    }

    @Override
    public void render(WorldRenderContext context) {
        renderTextLabel(context, text, pos.getX(), pos.getY(), pos.getZ());
    }

    private static void renderTextLabel(WorldRenderContext context, Text text, double x, double y, double z) {
        MatrixStack matrices = context.matrixStack();
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        matrices.push();

        Vec3d cameraPos = camera.getPos();
        // to origin
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        // to block pos
        matrices.translate(x, y, z);
        // center in block
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(camera.getRotation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        float halfWidth = -textRenderer.getWidth(text) / 2.0F;
        textRenderer.draw(text, halfWidth, 0, 0xffffffff, false, matrices.peek().getModel(), context.consumers(), false, 0x3f000000, 0xf00010);
        matrices.pop();
    }
}
