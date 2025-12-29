package com.shimaper.globaldatapacks.mixin;

import com.shimaper.globaldatapacks.GlobalDatapacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.nio.file.Path;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {

    @Inject(method = "openDataPackSelectionScreen", at = @At("HEAD"))
    private void onOpenPackScreen(CallbackInfo ci) {
        GlobalDatapacks.LOGGER.info("User opened datapack selection screen");

        try {
            Path tempDir = Path.of(System.getProperty("java.io.tmpdir"));
            Minecraft client = Minecraft.getInstance();
            Path minecraftDir = client.gameDirectory.toPath();

            File[] tempWorldDirs = tempDir.toFile().listFiles((dir, name) ->
                    name.startsWith("mcworld-") && new File(dir, name).isDirectory()
            );

            if (tempWorldDirs != null && tempWorldDirs.length > 0) {
                Path tempWorldPath = tempWorldDirs[0].toPath();
                GlobalDatapacks.LOGGER.info("Found temporary world folder: {}", tempWorldPath);

                boolean copied = GlobalDatapacks.copyDatapacks(tempWorldPath, minecraftDir);

                if (copied) {
                    GlobalDatapacks.LOGGER.info("Datapacks are ready to use!");
                }
            } else {
                GlobalDatapacks.LOGGER.warn("Temporary world folder not found in {}", tempDir);
                GlobalDatapacks.LOGGER.warn("Datapacks will be copied on next screen open");
            }

        } catch (Exception e) {
            GlobalDatapacks.LOGGER.error("Error while trying to copy datapacks", e);
        }
    }

    @Inject(method = "onClose", at = @At("HEAD"))
    private void onScreenClosed(CallbackInfo ci) {
        GlobalDatapacks.resetCopyFlag();
        GlobalDatapacks.LOGGER.debug("Create world screen closed");
    }
}