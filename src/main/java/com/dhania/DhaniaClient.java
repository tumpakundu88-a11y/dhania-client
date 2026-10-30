package com.dhania;

import com.dhania.config.Config;
import com.dhania.module.ClientModule;
import com.dhania.module.ModuleManager;
import com.dhania.ui.HudEditorScreen;
import com.dhania.ui.MenuScreen;
import com.dhania.ui.WaypointScreen;
import com.dhania.waypoint.WaypointManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

public class DhaniaClient implements ClientModInitializer {
    public static final String NAME = "Dhania Client";
    private static KeyBinding menuKey, waypointKey;

    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dhania.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.dhania"));
        waypointKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dhania.waypoints", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "category.dhania"));
        ModuleManager.ZOOM_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dhania.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.dhania"));

        ModuleManager.init();
        Config.load();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (menuKey.wasPressed()) mc.setScreen(new MenuScreen());
            while (waypointKey.wasPressed()) mc.setScreen(new WaypointScreen());
            ModuleManager.tick(mc);
        });

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden) return;
            ModuleManager.pollClicks(mc);
            if (mc.currentScreen instanceof HudEditorScreen) return; // editor draws them itself
            WaypointManager.render(ctx, mc);
            for (ClientModule m : ModuleManager.ALL)
                if (m.hud && m.enabled) m.render(ctx, mc);
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient) ModuleManager.onAttack(player, entity, hit);
            return ActionResult.PASS;
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> Config.save());
    }
}
