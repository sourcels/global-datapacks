package com.shimaper.globaldatapacks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class GlobalDatapacks implements ModInitializer {
    public static final String MOD_ID = "global-datapacks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        GlobalDatapacksConfig.load();
        createGlobalDatapacksFolder();
    }

    private void createGlobalDatapacksFolder() {
        try {
            Path minecraftDir = FabricLoader.getInstance().getGameDir();
            Path datapacksDir = minecraftDir.resolve("datapacks");

            if (!Files.exists(datapacksDir))
                Files.createDirectories(datapacksDir);

        } catch (IOException e) {
            LOGGER.error("[Global Datapacks] Failed to create global datapacks folder", e);
        }
    }

    public static void copyDatapacks(Path tempWorldPath, Path minecraftDir) {
        Path globalDatapacksDir = minecraftDir.resolve("datapacks");

        if (!Files.exists(globalDatapacksDir)) {
            LOGGER.info("[Global Datapacks] Folder not found: {}", globalDatapacksDir);
            LOGGER.info("[Global Datapacks] Create .minecraft/datapacks folder and place your datapacks there");
            return;
        }

        try {
            int copiedCount = 0;

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(globalDatapacksDir)) {
                for (Path sourceDatapack : stream) {
                    String datapackName = sourceDatapack.getFileName().toString();
                    Path targetDatapack = tempWorldPath.resolve(datapackName);

                    if (Files.isDirectory(sourceDatapack)) {
                        copyDirectory(sourceDatapack, targetDatapack);
                        LOGGER.info("[Global Datapacks] Copied datapack folder: {}", datapackName);
                        copiedCount++;
                    } else if (datapackName.endsWith(".zip")) {
                        Files.copy(sourceDatapack, targetDatapack, StandardCopyOption.REPLACE_EXISTING);
                        LOGGER.info("[Global Datapacks] Copied datapack zip: {}", datapackName);
                        copiedCount++;
                    }
                }
            }

            if (copiedCount > 0) {
                LOGGER.info("[Global Datapacks] Successfully copied {} datapack(s) from {} to {}",
                        copiedCount, globalDatapacksDir, tempWorldPath);
            } else {
                LOGGER.info("[Global Datapacks] No datapacks found in {}", globalDatapacksDir);
            }

        } catch (IOException e) {
            LOGGER.error("[Global Datapacks] Error copying datapacks", e);
        }
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {

            @Override
            public @NotNull FileVisitResult preVisitDirectory(@NotNull Path dir, @NotNull BasicFileAttributes attrs)
                    throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs)
                    throws IOException {
                Path targetFile = target.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}