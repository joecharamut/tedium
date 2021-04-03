package rocks.spaghetti.tedium.mixin;

import net.fabricmc.fabric.impl.client.rendering.WorldRenderContextImpl;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.spaghetti.tedium.ClientEntrypoint;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow @Final private BufferBuilderStorage bufferBuilders;
    @Shadow private ClientWorld world;
    @Shadow private ShaderEffect transparencyShader;
    @Unique private final WorldRenderContextImpl context = new WorldRenderContextImpl();

    @Inject(method = "render", at = @At("HEAD"))
    private void beforeRender(MatrixStack matrices,
                              float tickDelta,
                              long limitTime,
                              boolean renderBlockOutline,
                              Camera camera,
                              GameRenderer gameRenderer,
                              LightmapTextureManager lightmapTextureManager,
                              Matrix4f matrix4f,
                              CallbackInfo ci) {
        context.prepare((WorldRenderer) (Object) this, matrices, tickDelta, limitTime, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, matrix4f, bufferBuilders.getEntityVertexConsumers(), world.getProfiler(), transparencyShader != null, world);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getWaterMask()Lnet/minecraft/client/render/RenderLayer;"))
    private void onRender(MatrixStack matrices,
                          float tickDelta,
                          long limitTime,
                          boolean renderBlockOutline,
                          Camera camera,
                          GameRenderer gameRenderer,
                          LightmapTextureManager lightmapTextureManager,
                          Matrix4f matrix4f,
                          CallbackInfo ci) {
        ClientEntrypoint.onRender(context);
    }
}
