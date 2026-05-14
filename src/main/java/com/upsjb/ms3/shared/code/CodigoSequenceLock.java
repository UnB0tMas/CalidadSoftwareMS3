package com.upsjb.ms3.shared.code;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CodigoSequenceLock {

    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public LockHandle lock(String sequenceName) {
        String key = normalize(sequenceName);
        ReentrantLock lock = locks.computeIfAbsent(key, ignored -> new ReentrantLock());
        lock.lock();
        return new LockHandle(key, lock);
    }

    private String normalize(String sequenceName) {
        if (!StringUtils.hasText(sequenceName)) {
            throw new IllegalArgumentException("El nombre de la secuencia es obligatorio.");
        }

        return sequenceName.trim().toUpperCase(Locale.ROOT);
    }

    public static final class LockHandle implements AutoCloseable {

        private final String key;
        private final ReentrantLock lock;
        private boolean released;

        private LockHandle(String key, ReentrantLock lock) {
            this.key = key;
            this.lock = lock;
        }

        public String key() {
            return key;
        }

        public void release() {
            if (!released) {
                released = true;
                lock.unlock();
            }
        }

        @Override
        public void close() {
            release();
        }
    }
}