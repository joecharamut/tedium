package rocks.spaghetti.tedium.script;

import net.minecraft.util.math.BlockPos;
import org.mozilla.javascript.*;
import rocks.spaghetti.tedium.Log;
import rocks.spaghetti.tedium.Util;
import rocks.spaghetti.tedium.core.FakePlayer;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class ScriptEnvironment {
    public void eval(String code) {
        try (Scope scope = new Scope()) {
            Log.info("eval> {}", code);
            Object result = scope.execScript(scope.compile(code, "<eval>"));
            Log.info("eval> {}", scope.toString(result));
        } catch (RhinoException e) {
            Log.error("Script Error: {}", e.getMessage());
        } catch (Exception e) {
            Log.error("Unhandled Exception: {}", e.getClass());
            Log.catching(e);
        }
    }

    public void execResource(String resourceLocation) {
        try (Scope scope = new Scope()) {
            String code = Util.getResourceAsString(resourceLocation);
            if (code.isEmpty()) {
                Log.warn("Failed to load resource!");
                return;
            }
            Script script = scope.compile(code, resourceLocation);
            Thread codeThread = new Thread(() -> scope.execScript(script));
            codeThread.setName("Code Executor Thread");
            codeThread.start();
        } catch (RhinoException e) {
            Log.error("Script Error: {}", e.getMessage());
        } catch (Exception e) {
            Log.error("Unhandled Exception: {}", e.getClass());
            Log.catching(e);
        }
    }

    private static class Scope implements Closeable {
        private final Context context;
        private final Scriptable scriptScope;

        public Scope() {
            context = Context.enter();
            scriptScope = context.initStandardObjects();
            Object sysWrapper = Context.javaToJS(new GlobalMethods(), scriptScope);
            ScriptableObject.putProperty(scriptScope, "sys", sysWrapper);
            Object apiWrapper = Context.javaToJS(new MinecraftApi(), scriptScope);
            ScriptableObject.putProperty(scriptScope, "minecraft", apiWrapper);
        }

        public Script compile(String source, String sourceName) throws IOException {
            return compile(new StringReader(source), sourceName);
        }

        public Script compile(Reader reader, String sourceName) throws IOException {
            return context.compileReader(reader, sourceName, 0, null);
        }

        public Object execScript(Script script) {
            return script.exec(context, scriptScope);
        }

        @Override
        public void close() {
            Context.exit();
        }

        public String toString(Object result) {
            return Context.toString(result);
        }

        @SuppressWarnings({"unused", "RedundantSuppression"})
        public static class GlobalMethods {
            public void println(String msg) {
                Log.info("[Script] {}", msg);
            }
        }

        @SuppressWarnings({"unused", "RedundantSuppression"})
        public static class MinecraftApi {
            public void goToBlock(int x, int y, int z) {
                FakePlayer player = FakePlayer.get();
                if (player == null) return;
                player.setPositionTarget(new BlockPos(x, y, z), 1);
            }
        }
    }
}
