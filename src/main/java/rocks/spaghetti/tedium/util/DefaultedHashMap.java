package rocks.spaghetti.tedium.util;

import java.util.HashMap;

public class DefaultedHashMap<K, V> extends HashMap<K, V> {
    private V defaultValue;

    public DefaultedHashMap(V defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public V get(Object key) {
        return super.getOrDefault(key, defaultValue);
    }
}
