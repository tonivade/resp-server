/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ThreadSafeCache<K, V> {

    private static Logger log = Logger.getLogger(ThreadSafeCache.class.getName());

    private Map<K, FutureTask<V>> values = new HashMap<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public V get(K key, Object ... params) {
        try {
            FutureTask<V> task;
            synchronized (values) {
                task = values.get(key);
                if (task == null) {
                    task = new FutureTask<>(new Callable<V>() {
                        @Override
                        public V call() throws Exception {
                            log.log(Level.INFO, "creating object from cache `{0}`", key);
                            return createValue(key, params);
                        }
                    });
                    executor.execute(task);
                    values.put(key, task);
                }
            }
            log.log(Level.FINE, "getting object from cache `{0}`", key);
            return task.get();
        } catch (InterruptedException e) {
            log.log(Level.WARNING, "interrupted", e);
        } catch (ExecutionException e) {
            log.log(Level.SEVERE, "error", e);
        }
        return null;
    }

    protected abstract V createValue(K key, Object ... params);

    public V remove(K key) {
        try {
            FutureTask<V> task;
            synchronized (values) {
                task = values.remove(key);
            }
            return task != null ? task.get() : null;
        } catch (InterruptedException e) {
            log.log(Level.WARNING, "interrupted", e);
        } catch (ExecutionException e) {
            log.log(Level.SEVERE, "error", e);
        }
        return null;
    }

    public int size() {
        return values.size();
    }

    public void clear() {
        synchronized (values) {
            values.clear();
        }
    }

    public void destroy() {
        executor.shutdown();
    }
}
