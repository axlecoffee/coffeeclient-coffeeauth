package coffee.axle.coffeeclient.coffeeauth;

import coffee.axle.coffeeclient.coffeeauth.gui.ChangerGUI;
import coffee.axle.coffeeclient.coffeeauth.gui.SessionGUI;
import coffee.axle.coffeeclient.coffeeauth.util.APIUtils;
import io.github.moulberry.notenoughupdates.coffeeclient.hook.CoffeeMod;
import io.github.moulberry.notenoughupdates.coffeeclient.hook.event.CLInitEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.util.Session;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

/**
 * CoffeeAuth — Session login mod for CoffeeClient.
 * <p>
 * Ported from SchubiAuthV2 to the CoffeeLoader API.
 * Uses zero external HTTP deps (java.net only).
 */
@CoffeeMod(name = "CoffeeAuth", version = CoffeeAuth.VERSION)
public class CoffeeAuth {

    public static final String VERSION = "1.0.0";

    private static final Minecraft mc = Minecraft.getMinecraft();

    /** Captured at class-load so we can always restore the original session. */
    public static Session originalSession = mc.getSession();

    /** Status strings rendered on the multiplayer screen. */
    public static String onlineStatus = "\u00A74\u2573 Offline";
    public static String isSessionValid = "\u00A72\u2714 Valid";

    @CoffeeMod.EventHandler
    public void onInit(CLInitEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    // ── Multiplayer screen hooks ────────────────────────────────────────

    @SubscribeEvent
    public void onInitGuiPost(GuiScreenEvent.InitGuiEvent.Post e) {
        if (!(e.gui instanceof GuiMultiplayer))
            return;

        e.buttonList.add(new GuiButton(2100, e.gui.width - 90, 5, 80, 20, "Login"));
        e.buttonList.add(new GuiButton(2200, e.gui.width - 180, 5, 80, 20, "Changer"));

        new Thread(() -> {
            try {
                isSessionValid = APIUtils.validateSession(mc.getSession().getToken())
                        ? "\u00A72\u2714 Valid"
                        : "\u00A74\u2573 Invalid";
                onlineStatus = APIUtils.checkOnline(mc.getSession().getUsername())
                        ? "\u00A72\u2714 Online"
                        : "\u00A74\u2573 Offline";
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, "CoffeeAuth-StatusCheck").start();
    }

    @SubscribeEvent
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post e) {
        if (!(e.gui instanceof GuiMultiplayer))
            return;

        String info = "\u00A7fUser: " + mc.getSession().getUsername()
                + "  \u00A7f|  " + onlineStatus
                + "  \u00A7f|  " + isSessionValid;
        mc.fontRendererObj.drawString(info, 5, 10, Color.RED.getRGB());
    }

    @SubscribeEvent
    public void onActionPerformedPre(GuiScreenEvent.ActionPerformedEvent.Pre e) {
        if (!(e.gui instanceof GuiMultiplayer))
            return;

        if (e.button.id == 2100) {
            mc.displayGuiScreen(new SessionGUI(e.gui));
        } else if (e.button.id == 2200) {
            mc.displayGuiScreen(new ChangerGUI(e.gui));
        }
    }
}
