package org.fog.utils;

import java.util.Objects;

public class Pair<K, V> {
    private final K key;
    private final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    // Getter 方法
    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    // 覆寫 equals 和 hashCode 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(getKey(), pair.getKey()) &&
               Objects.equals(getValue(), pair.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getValue());
    }

    // 覆寫 toString 方法
    @Override
    public String toString() {
        return "Pair{" +
               "key=" + key +
               ", value=" + value +
               '}';
    }
}
