/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.injection.forge.mixins.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minusmc.minusbounce.MinusBounce;
import net.minusmc.minusbounce.event.RenderModelEvent;
import net.minusmc.minusbounce.event.RenderNameTagsEvent;
import net.minusmc.minusbounce.features.module.modules.render.*;
import net.minusmc.minusbounce.utils.ClientUtils;
import net.minusmc.minusbounce.utils.EntityUtils;
import net.minusmc.minusbounce.utils.render.GLUtils;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity extends MixinRender {

    @Shadow
    protected ModelBase mainModel;

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("HEAD"), cancellable = true)
    private <T extends EntityLivingBase> void injectChamsPre(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo callbackInfo) {
        final NoRender noRender = MinusBounce.moduleManager.getModule(NoRender.class);

        if (noRender.getState() && noRender.shouldStopRender(entity)) {
            callbackInfo.cancel();
            return;
        }
    }

    @Inject(method = "canRenderName(Lnet/minecraft/entity/EntityLivingBase;)Z", at = @At("HEAD"), cancellable = true)
    private <T extends EntityLivingBase> void canRenderName(T entity, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        final RenderNameTagsEvent event = new RenderNameTagsEvent(entity);
        MinusBounce.eventManager.callEvent(event);

        if (event.isCancelled())
            callbackInfoReturnable.setReturnValue(false);
    }

    /**
     * @author CCBlueX
     * @reason ESP
     */

    @Overwrite
    protected <T extends EntityLivingBase> void renderModel(T entityLivingBaseIn, float x, float y, float z, float yaw, float pitch, float partialTicks) {
        final boolean visible = !entityLivingBaseIn.isInvisible();
        final TrueSight trueSight = MinusBounce.moduleManager.getModule(TrueSight.class);
        final boolean semiVisible = !visible && (!entityLivingBaseIn.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer) || (trueSight.getState() && trueSight.getEntitiesValue().get()));

        if (visible || semiVisible) {
            if (!this.bindEntityTexture(entityLivingBaseIn))
                return;

            if (semiVisible) {
                GlStateManager.pushMatrix();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 0.15F);
                GlStateManager.depthMask(false);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(770, 771);
                GlStateManager.alphaFunc(516, 0.003921569F);
            }

            MinusBounce.eventManager.callEvent(new RenderModelEvent(mainModel, entityLivingBaseIn, x, y, z, yaw, pitch, partialTicks));

            this.mainModel.render(entityLivingBaseIn, x, y, z, yaw, pitch, partialTicks);

            if (semiVisible) {
                GlStateManager.disableBlend();
                GlStateManager.alphaFunc(516, 0.1F);
                GlStateManager.popMatrix();
                GlStateManager.depthMask(true);
            }
        }
    }
}