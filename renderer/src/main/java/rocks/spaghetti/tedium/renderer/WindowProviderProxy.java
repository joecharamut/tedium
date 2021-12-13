package rocks.spaghetti.tedium.renderer;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.WindowProvider;
import org.jetbrains.annotations.Nullable;
import org.objenesis.ObjenesisStd;

public class WindowProviderProxy {
    private WindowProviderProxy() {

    }

    public static WindowProvider createProxy() {
        WindowProviderProxy instance = new WindowProviderProxy();
        try {
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(WindowProvider.class);
            factory.setFilter(m -> true);

            MethodHandler handler = (self, thisMethod, proceed, args) -> WindowProviderProxy.class.getDeclaredMethod(thisMethod.getName(), thisMethod.getParameterTypes()).invoke(instance, args);

            Object obj = new ObjenesisStd().getInstantiatorOf(factory.createClass()).newInstance();
            ((Proxy) obj).setHandler(handler);
            return (WindowProvider) obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Window createWindow(WindowSettings settings, @Nullable String videoMode, String title) {
        return WindowProxy.createProxy();
    }

    public void close() {
        Log.info("WindowProvider::close() stub");
    }
}
