package com.shimaper.globaldatapacks.mixin;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;

import com.shimaper.globaldatapacks.GlobalDatapacks;
import com.shimaper.globaldatapacks.GlobalDatapacksConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {

    @Shadow @Final
    WorldCreationUiState uiState;

    @Shadow protected abstract Path getOrCreateTempDataPackDir();

    @Inject(method = "getOrCreateTempDataPackDir", at = @At("RETURN"))
    private void afterCreateTempDir(CallbackInfoReturnable<Path> cir) {
        Path tempPath = cir.getReturnValue();
        Minecraft client = Minecraft.getInstance();
        Path minecraftDir = client.gameDirectory.toPath();
        GlobalDatapacks.copyDatapacks(tempPath, minecraftDir);
    }

    @Inject(method = "onCreate", at = @At("HEAD"))
    private void cleanupUnusedDatapacks(CallbackInfo ci) {
        if (GlobalDatapacksConfig.embedInactiveDatapacks) {
            return;
        }

        Path tempPath = this.getOrCreateTempDataPackDir();

        if (tempPath == null || !Files.exists(tempPath)) return;

        Collection<String> enabledPacks = this.uiState.getSettings().dataConfiguration().dataPacks().getEnabled();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempPath)) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();
                String resourcePackId = "file/" + fileName;

                if (!enabledPacks.contains(resourcePackId)) {
                    GlobalDatapacks.LOGGER.info("[Global Datapacks] Removing unapplied datapack: {}", fileName);
                    deleteRecursively(file);
                }
            }
        } catch (IOException e) {
            GlobalDatapacks.LOGGER.error("[Global Datapacks] Failed to cleanup temp datapacks", e);
        }
    }

    @Unique
    private void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public @NotNull FileVisitResult postVisitDirectory(@NotNull Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            Files.deleteIfExists(path);
        }
    }
}