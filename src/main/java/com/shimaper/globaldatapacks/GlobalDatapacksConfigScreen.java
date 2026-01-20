package com.shimaper.globaldatapacks;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GlobalDatapacksConfigScreen extends Screen {
    private final Screen parent;
    private boolean localEmbedValue;

    public GlobalDatapacksConfigScreen(Screen parent) {
        super(Component.translatable("global-datapacks.config.title"));
        this.parent = parent;
        this.localEmbedValue = GlobalDatapacksConfig.embedInactiveDatapacks;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addRenderableWidget(new StringWidget(
                centerX - 100, centerY - 45, 200, 20,
                Component.translatable("global-datapacks.config.title"),
                this.font
        ));

        this.addRenderableWidget(Button.builder(getButtonText(), (button) -> {
            this.localEmbedValue = !this.localEmbedValue;
            button.setMessage(getButtonText());
        }).bounds(centerX - 100, centerY - 10, 200, 20).build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            GlobalDatapacksConfig.embedInactiveDatapacks = this.localEmbedValue;
            GlobalDatapacksConfig.save();
            this.minecraft.setScreen(this.parent);
        }).bounds(centerX - 100, centerY + 40, 98, 20).build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
            this.minecraft.setScreen(this.parent);
        }).bounds(centerX + 2, centerY + 40, 98, 20).build());
    }

    private Component getButtonText() {
        Component stateText = Component.translatable(localEmbedValue ? "global-datapacks.config.true" : "global-datapacks.config.false")
                .withStyle(localEmbedValue ? ChatFormatting.GREEN : ChatFormatting.RED);

        return Component.translatable("global-datapacks.config.embed_inactive")
                .append(": ")
                .append(stateText);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
    }
}