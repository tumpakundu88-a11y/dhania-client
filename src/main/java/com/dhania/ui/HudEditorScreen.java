package com.dhania.ui;

import com.dhania.config.Config;
import com.dhania.module.ClientModule;
import com.dhania.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class HudEditorScreen extends Screen {
    private ClientModule dragging;
    private int offX, offY;

    public HudEditorScreen() { super(Text.literal("HUD Editor")); }

    @Override public boolean shouldPause() { return false; }

    @Override public void renderBackground(DrawContext c, int mx, int my, float d) {
        c.fill(0, 0, width, height, 0x60000000);
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        renderBackground(c, mx, my, delta);
        String hint = "HUD Editor - drag modules to reposition. ESC to save & exit.";
        c.drawText(textRenderer, hint, (width - textRenderer.getWidth(hint)) / 2, 8, Theme.ACCENT, true);
        for (ClientModule m : ModuleManager.ALL) {
            if (!m.hud || !m.enabled) continue;
            m.render(c, client);
            c.drawBorder(m.x - 1, m.y - 1, m.w + 2, m.h + 2, m == dragging ? Theme.ACCENT : Theme.ACCENT_DARK);
            c.drawText(textRenderer, m.name, m.x, m.y - 10, Theme.MUTED, false);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            for (int i = ModuleManager.ALL.size() - 1; i >= 0; i--) {
                ClientModule m = ModuleManager.ALL.get(i);
                if (m.hud && m.enabled && Theme.inside(mx, my, m.x, m.y, m.w, m.h)) {
                    dragging = m;
                    offX = (int) mx - m.x;
                    offY = (int) my - m.y;
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null) {
            dragging.x = Math.max(0, Math.min(width - dragging.w, (int) mx - offX));
            dragging.y = Math.max(0, Math.min(height - dragging.h, (int) my - offY));
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (dragging != null) { dragging = null; Config.save(); return true; }
        return super.mouseReleased(mx, my, button);
    }

    @Override public void close() { Config.save(); client.setScreen(new MenuScreen()); }
}
