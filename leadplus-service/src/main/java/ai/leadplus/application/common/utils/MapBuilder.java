package ai.leadplus.application.common.utils;

import java.util.HashMap;
import java.util.Map;

public class MapBuilder {

    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    public static <K, V> Builder<K, V> from(Map<K, V> starter) {
        return new Builder<>(starter);
    }

    public static class Builder<K, V> {

        private final Map<K, V> map;

        Builder() {
            this.map = new HashMap<>();
        }

        Builder(Map<K, V> starter) {
            this.map = new HashMap<>(starter);
        }

        public Builder<K, V> put(K key, V value) {
            map.put(key, value);
            return this;
        }

        public Builder<K, V> putIfNotNull(K key, V value) {
            if (value != null) {
                map.put(key, value);
            }
            return this;
        }


        public Map<K, V> build() {
            return new HashMap<>(map);
        }
    }
}
