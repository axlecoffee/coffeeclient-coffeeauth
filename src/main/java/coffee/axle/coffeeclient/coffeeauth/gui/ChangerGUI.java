package coffee.axle.coffeeclient.coffeeauth.gui;

import coffee.axle.coffeeclient.coffeeauth.CoffeeAuth;
import coffee.axle.coffeeclient.coffeeauth.util.APIUtils;
import coffee.axle.coffeeclient.coffeeauth.util.SessionChanger;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.Session;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * GUI for changing name and skin on a non-original (alt) session.
 */
public class ChangerGUI extends GuiScreen {

    private final GuiScreen previousScreen;
    private String status = "";
    private GuiTextField nameField;
    private GuiTextField skinField;
    private ScaledResolution sr;
    private final List<GuiTextField> textFields = new ArrayList<>();

    public ChangerGUI(GuiScreen previousScreen) {
        this.previousScreen = previousScreen;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        sr = new ScaledResolution(mc);

        nameField = new GuiTextField(1, mc.fontRendererObj,
                sr.getScaledWidth() / 2 - 100, sr.getScaledHeight() / 2, 97, 20);
        nameField.setMaxStringLength(16);
        nameField.setFocused(true);

        skinField = new GuiTextField(2, mc.fontRendererObj,
                sr.getScaledWidth() / 2 + 3, sr.getScaledHeight() / 2, 97, 20);
        skinField.setMaxStringLength(32767);

        textFields.clear();
        textFields.add(nameField);
        textFields.add(skinField);

        buttonList.add(new GuiButton(3100, sr.getScaledWidth() / 2 - 100,
                sr.getScaledHeight() / 2 + 25, 97, 20, "Change Name"));
        buttonList.add(new GuiButton(3200, sr.getScaledWidth() / 2 + 3,
                sr.getScaledHeight() / 2 + 25, 97, 20, "Change Skin"));
        buttonList.add(new GuiButton(3300, sr.getScaledWidth() / 2 - 100,
                sr.getScaledHeight() / 2 + 50, 200, 20, "Back"));

        super.initGui();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        mc.fontRendererObj.drawString(status,
                sr.getScaledWidth() / 2 - mc.fontRendererObj.getStringWidth(status) / 2,
                sr.getScaledHeight() / 2 - 40, Color.WHITE.getRGB());
        mc.fontRendererObj.drawString("Name:",
                sr.getScaledWidth() / 2 - 99, sr.getScaledHeight() / 2 - 15, Color.WHITE.getRGB());
        mc.fontRendererObj.drawString("Skin:",
                sr.getScaledWidth() / 2 + 4, sr.getScaledHeight() / 2 - 15, Color.WHITE.getRGB());
        nameField.drawTextBox();
        skinField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 3100) {
            handleChangeName();
        }
        if (button.id == 3200) {
            handleChangeSkin();
        }
        if (button.id == 3300) {
            mc.displayGuiScreen(previousScreen);
        }
        super.actionPerformed(button);
    }

    private void handleChangeName() {
        String newName = nameField.getText();
        if (Objects.equals(CoffeeAuth.originalSession.getToken(), mc.getSession().getToken())) {
            status = "\u00A74Prevented you from changing the name of your main account!";
            return;
        }
        new Thread(() -> {
            try {
                int code = APIUtils.changeName(newName, mc.getSession().getToken());
                switch (code) {
                    case 200:
                        status = "\u00A72Successfully changed name!";
                        SessionChanger.setSession(new Session(newName,
                                mc.getSession().getPlayerID(), mc.getSession().getToken(), "mojang"));
                        break;
                    case 400:
                        status = "\u00A74Error: Invalid name!";
                        break;
                    case 401:
                        status = "\u00A74Error: Invalid token!";
                        break;
                    case 403:
                        status = "\u00A74Error: Name unavailable / changed within last 35 days";
                        break;
                    case 429:
                        status = "\u00A74Error: Too many requests!";
                        break;
                    default:
                        status = "\u00A74An unknown error occurred!";
                        break;
                }
            } catch (Exception e) {
                status = "\u00A74An unknown error occurred!";
                e.printStackTrace();
            }
        }, "CoffeeAuth-ChangeName").start();
    }

    private void handleChangeSkin() {
        String skinUrl = skinField.getText();
        new Thread(() -> {
            try {
                int code = APIUtils.changeSkin(skinUrl, mc.getSession().getToken());
                switch (code) {
                    case 200:
                        status = "\u00A72Successfully changed skin!";
                        break;
                    case 401:
                        status = "\u00A74Error: Invalid token!";
                        break;
                    case 429:
                        status = "\u00A74Error: Too many requests!";
                        break;
                    default:
                        status = "\u00A74Error: Invalid Skin";
                        break;
                }
            } catch (Exception e) {
                status = "\u00A74An unknown error occurred!";
                e.printStackTrace();
            }
        }, "CoffeeAuth-ChangeSkin").start();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        nameField.textboxKeyTyped(typedChar, keyCode);
        skinField.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(previousScreen);
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for (GuiTextField text : textFields) {
            text.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }
}
