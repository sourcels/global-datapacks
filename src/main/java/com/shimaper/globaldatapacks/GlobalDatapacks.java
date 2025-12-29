package com.shimaper.globaldatapacks;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class GlobalDatapacks implements ModInitializer {
    public static final String MOD_ID = "global-datapacks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static boolean datapacksCopiedForCurrentSession = false;

    @Override
    public void onInitialize() {
        LOGGER.info("Global Datapacks initialized for Minecraft 1.21.11");
    }

    public static boolean copyDatapacks(Path tempWorldPath, Path minecraftDir) {
        if (datapacksCopiedForCurrentSession) {
            LOGGER.debug("Datapacks already copied for this world creation session");
            return false;
        }

        Path globalDatapacksDir = minecraftDir.resolve("datapacks");
        Path worldDatapacksDir = tempWorldPath.resolve("datapacks");

        if (!Files.exists(globalDatapacksDir)) {
            LOGGER.info("Global datapacks folder not found: {}", globalDatapacksDir);
            LOGGER.info("Create .minecraft/datapacks folder and place your datapacks there");
            return false;
        }

        try {
            if (!Files.exists(worldDatapacksDir)) {
                Files.createDirectories(worldDatapacksDir);
                LOGGER.info("Created datapacks folder: {}", worldDatapacksDir);
            }

            int copiedCount = 0;

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(globalDatapacksDir)) {
                for (Path sourceDatapack : stream) {
                    String datapackName = sourceDatapack.getFileName().toString();
                    Path targetDatapack = worldDatapacksDir.resolve(datapackName);

                    if (Files.isDirectory(sourceDatapack)) {
                        copyDirectory(sourceDatapack, targetDatapack);
                        LOGGER.info("Copied datapack folder: {}", datapackName);
                        copiedCount++;
                    } else if (datapackName.endsWith(".zip")) {
                        Files.copy(sourceDatapack, targetDatapack, StandardCopyOption.REPLACE_EXISTING);
                        LOGGER.info("Copied datapack zip: {}", datapackName);
                        copiedCount++;
                    }
                }
            }

            if (copiedCount > 0) {
                LOGGER.info("Successfully copied {} datapack(s) from {} to {}",
                        copiedCount, globalDatapacksDir, worldDatapacksDir);
                datapacksCopiedForCurrentSession = true;
                return true;
            } else {
                LOGGER.info("No datapacks found in {}", globalDatapacksDir);
                return false;
            }

        } catch (IOException e) {
            LOGGER.error("Error copying datapacks", e);
            return false;
        }
    }

    public static void resetCopyFlag() {
        datapacksCopiedForCurrentSession = false;
        LOGGER.debug("Datapack copy flag reset");
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Path targetFile = target.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}