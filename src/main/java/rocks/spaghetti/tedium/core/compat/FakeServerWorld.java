package rocks.spaghetti.tedium.core.compat;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.world.ServerWorld;
import rocks.spaghetti.tedium.Log;
import rocks.spaghetti.tedium.UnsafeHelper;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FakeServerWorld {
    private FakeServerWorld() { throw new IllegalStateException("Utility Class"); }
    private static ClientWorld realWorld;
    private static ServerWorld fakeWorld;

    public static ServerWorld create(ClientWorld realWorld) {
        if (fakeWorld != null && FakeServerWorld.realWorld.equals(realWorld)) return fakeWorld;
        FakeServerWorld.realWorld = realWorld;

        Map<Method, Method> realMethods = new HashMap<>();
        for (Method method : realWorld.getClass().getMethods()) {
            realMethods.put(method, method);
        }

        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(ServerWorld.class);
        factory.setFilter(realMethods::containsKey);

        MethodHandler handler = (self, thisMethod, proceed, args) -> {
            Method redirect = realMethods.getOrDefault(thisMethod, null);
            if (redirect != null) {
                return redirect.invoke(realWorld, args);
            }

            Log.error("Handler: Could not invoke {}({})", thisMethod.getName(), Arrays.toString(thisMethod.getParameters()));
            return null;
        };

        try {
            Class<?> proxyClass = factory.createClass();
            Object lies = UnsafeHelper.newInstance(proxyClass);
            ((Proxy) lies).setHandler(handler);
            fakeWorld = ((ServerWorld) lies);
        } catch (Exception e) {
            Log.catching(e);
        }

        return fakeWorld;
    }
}
