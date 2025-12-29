package com.shimaper.globaldatapacks.mixin;

import java.nio.file.Path;

import com.shimaper.globaldatapacks.GlobalDatapacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;



@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {

    @Inject(method = "getOrCreateTempDataPackDir", at = @At("RETURN"))
    private void afterCreateTempDir(CallbackInfoReturnable<Path> cir) {
        Path tempPath = cir.getReturnValue();

        Minecraft client = Minecraft.getInstance();
        Path minecraftDir = client.gameDirectory.toPath();

        GlobalDatapacks.copyDatapacks(tempPath, minecraftDir);
    }

}