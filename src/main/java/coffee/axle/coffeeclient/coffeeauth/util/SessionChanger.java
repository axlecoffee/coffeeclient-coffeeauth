package coffee.axle.coffeeclient.coffeeauth.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Swaps the active {@link Session} on the running Minecraft instance.
 * <p>
 * Java 12+ removed access to {@code Field.modifiers}, so we use
 * {@link sun.misc.Unsafe#putObject} to bypass finality.
 */
public class SessionChanger {

    private static final Unsafe UNSAFE;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain Unsafe instance", e);
        }
    }

    /**
     * Replaces the current Minecraft session with the given one.
     * Uses {@code Unsafe.putObject} to write the (potentially final) field.
     */
    public static void setSession(Session session) {
        try {
            Field sessionField = ReflectionHelper.findField(Minecraft.class, "session", "field_71449_j");
            long offset = UNSAFE.objectFieldOffset(sessionField);
            UNSAFE.putObject(Minecraft.getMinecraft(), offset, session);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set session", e);
        }
    }
}
