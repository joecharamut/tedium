package rocks.spaghetti.tedium;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

@SuppressWarnings("unchecked")
public final class UnsafeHelper {
    private UnsafeHelper() { throw new IllegalStateException("Utility Class"); }

    private static final Unsafe theUnsafe;
    static {
        Unsafe temp;

        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            temp = (Unsafe) unsafeField.get(null);
        } catch (NoSuchFieldException|IllegalAccessException e) {
            Log.catching(e);
            temp = null;
        }

        theUnsafe = temp;
    }

    public static <T> T newInstance(Class<T> clazz) {
        try {
            return (T) theUnsafe.allocateInstance(clazz);
        } catch (InstantiationException e) {
            Log.catching(e);
            return null;
        }
    }
}
