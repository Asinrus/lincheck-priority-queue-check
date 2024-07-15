package me.asinrus;

import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Logger;

public class CacheLFUService {

    private final Logger logger = Logger.getLogger(getClass().getName());

    private static class CacheElement {
        private final String key;
        private final Object value;
        private final int hits;

        public static CacheElement createNewElement(String key, Object value) {
            return new CacheElement(key, value, 0);
        }

        public static CacheElement createFromExistingElement(CacheElement element, Object value) {
            return new CacheElement(element.key, value, element.hits + 1);
        }

        public CacheElement(String key, Object value, int hits) {
            this.value = value;
            this.key = key;
            this.hits = hits;
        }

        public String getKey() {
            return key;
        }

        public int getHits() {
            return hits;
        }

        public Object getValue() {
            return value;
        }
    }

    final int cacheMaxSize;
    private final ConcurrentMap<String, CacheElement> caches = new ConcurrentHashMap<>();
    private final PriorityBlockingQueue<CacheElement> hitsToCache;

    public CacheLFUService(int maxSize) {
        cacheMaxSize = maxSize;
        hitsToCache = new PriorityBlockingQueue<>(cacheMaxSize,
                Comparator.comparingInt(CacheElement::getHits));
    }

    public synchronized void put(String key, Object value) {
        if (caches.containsKey(key)) {
            CacheElement cacheElement = caches.get(key);
            CacheElement newCacheElement = CacheElement.createFromExistingElement(cacheElement, value);
            updateStatistic(cacheElement, newCacheElement);
            caches.put(key, newCacheElement);
        } else {
            CacheElement newCacheElement = CacheElement.createNewElement(key, value);
            if (caches.size() >= cacheMaxSize) {
                CacheElement removedCacheElement = hitsToCache.poll();
                if (removedCacheElement != null) {
                    caches.remove(removedCacheElement.getKey());
                    logger.info("Removed " + removedCacheElement.getKey() + " from cache");
                }
            }

            caches.put(key, newCacheElement);
            hitsToCache.add(newCacheElement);
        }
    }

    public Object get(String key) {
        if (caches.containsKey(key)) {
            CacheElement cacheElement = caches.get(key);
            Object value = cacheElement.getValue();
            CacheElement newElement = CacheElement.createFromExistingElement(cacheElement, value);

            updateStatistic(cacheElement, newElement);
            return value;
        } else {
            return null;
        }
    }

    private void updateStatistic(CacheElement cacheElement, CacheElement newCacheElement) {
        hitsToCache.remove(cacheElement);
        hitsToCache.offer(newCacheElement);
    }

}
