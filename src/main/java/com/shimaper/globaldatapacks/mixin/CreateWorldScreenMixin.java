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
        try {
            Path tempDir = Path.of(System.getProperty("java.io.tmpdir"));
            Minecraft client = Minecraft.getInstance();
            Path minecraftDir = client.gameDirectory.toPath();

            File[] tempWorldDirs = tempDir.toFile().listFiles((dir, name) ->
                    name.startsWith("mcworld-") && new File(dir, name).isDirectory()
            );

            if (tempWorldDirs != null && tempWorldDirs.length > 0) {
                File newestTempWorld = null;
                long newestTime = 0;

                GlobalDatapacks.LOGGER.debug("Found {} temporary world folder(s)", tempWorldDirs.length);

                for (File tempWorld : tempWorldDirs) {
                    long lastModified = tempWorld.lastModified();
                    GlobalDatapacks.LOGGER.debug("Checking folder: {} (modified: {})",
                            tempWorld.getName(), lastModified);

                    if (lastModified > newestTime) {
                        newestTime = lastModified;
                        newestTempWorld = tempWorld;
                    }
                }

                if (newestTempWorld != null) {
                    Path tempWorldPath = newestTempWorld.toPath();
                    GlobalDatapacks.LOGGER.info("Selected newest temporary world folder: {} (age: {} ms ago)",
                            tempWorldPath.getFileName(),
                            System.currentTimeMillis() - newestTime);

                    boolean copied = GlobalDatapacks.copyDatapacks(tempWorldPath, minecraftDir);

                    if (copied) {
                        GlobalDatapacks.LOGGER.info("Datapacks are ready to use!");
                    }
                }
            } else {
                GlobalDatapacks.LOGGER.warn("No temporary world folders found in {}", tempDir);
                GlobalDatapacks.LOGGER.warn("This might happen if you opened Data Packs screen before the temp folder was created");
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