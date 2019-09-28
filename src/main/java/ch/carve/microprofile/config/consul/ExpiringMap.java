package ch.carve.microprofile.config.consul;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ExpiringMap<K, V> {
    long validity;
    private Map<K, TimedEntry<V>> cache = new ConcurrentHashMap<>();

    public ExpiringMap(long validity) {
        this.validity = validity;
    }

    public V getOrCompute(K propertyName, CheckedFunction<K, V> action, Consumer<K> onException) {
        TimedEntry<V> entry = cache.get(propertyName);
        if (entry == null || entry.isExpired()) {
            try {
                V value = action.apply(propertyName);
                cache.put(propertyName, new TimedEntry<V>(value));
                return value;
            } catch (Exception e) {
                onException.accept(propertyName);
            }
        }
        // if the entry was never cached, then it will be null
        return entry != null ? entry.getValue() : null;
    }

    class TimedEntry<E> {
        private final E value;
        private final long timestamp;

        public TimedEntry(E value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        public E getValue() {
            return value;
        }

        public boolean isExpired() {
            return (timestamp + validity) < System.currentTimeMillis();
        }
    }

    Map<K, TimedEntry<V>> getMap() {
        return cache;
    }

    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }
}
