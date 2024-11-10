package org.fog.utils;

import java.util.Objects;

/**
 * Pair 類代表一對鍵值對，類型為 K 和 V。
 */
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

    // 修改 getter 方法名稱
    public K getFirst() {
        return key;
    }

    public V getSecond() {
        return value;
    }

    // 覆寫 equals 和 hashCode 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(getFirst(), pair.getFirst()) &&
               Objects.equals(getSecond(), pair.getSecond());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirst(), getSecond());
    }

    // 覆寫 toString 方法
    @Override
    public String toString() {
        return "Pair{" +
               "first=" + key +
               ", second=" + value +
               '}';
    }
}
