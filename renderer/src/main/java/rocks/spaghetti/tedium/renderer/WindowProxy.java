package rocks.spaghetti.tedium.renderer;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import net.minecraft.client.util.Window;
import org.objenesis.ObjenesisStd;

import java.io.InputStream;

@SuppressWarnings({"unused", "RedundantSuppression"})
public class WindowProxy {
    private WindowProxy() {

    }

    public static Window createProxy() {
        WindowProxy instance = new WindowProxy();
        try {
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(Window.class);
            factory.setFilter(m -> true);

            MethodHandler handler = (self, thisMethod, proceed, args) -> WindowProxy.class.getDeclaredMethod(thisMethod.getName(), thisMethod.getParameterTypes()).invoke(instance, args);

            Object obj = new ObjenesisStd().getInstantiatorOf(factory.createClass()).newInstance();
            ((Proxy) obj).setHandler(handler);
            return (Window) obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setIcon(InputStream icon16, InputStream icon32) {
        Log.info("Window::setIcon() stub");
    }

    public void setFramerateLimit(int framerateLimit) {
        Log.info("Window::setFramerateLimit() stub");
    }

    public long getHandle() {
        Log.info("Window::getHandle() stub");
        return 0;
    }
}
