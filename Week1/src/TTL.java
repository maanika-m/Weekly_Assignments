import java.util.*;
import java.util.concurrent.*;

class DNSEntry {
    String domain;
    String ipAddress;
    long expiryTime;

    DNSEntry(String domain, String ipAddress, int ttlSeconds) {
        this.domain = domain;
        this.ipAddress = ipAddress;
        this.expiryTime = System.currentTimeMillis() + ttlSeconds * 1000;
    }

    boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}

public class TTL {
    private final int MAX_CACHE_SIZE = 1000;
    private LinkedHashMap<String, DNSEntry> cache;
    private int hits = 0, misses = 0;
    private long totalLookupTime = 0;

    public TTL() {
        cache = new LinkedHashMap<>(16, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };

        // Background thread to clean expired entries
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            synchronized (cache) {
                Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();
                while (it.hasNext()) {
                    if (it.next().getValue().isExpired()) {
                        it.remove();
                    }
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    // Resolve domain
    public String resolve(String domain) {
        long start = System.nanoTime();
        DNSEntry entry;

        synchronized (cache) {
            entry = cache.get(domain);
            if (entry != null && !entry.isExpired()) {
                hits++;
                totalLookupTime += (System.nanoTime() - start);
                return "Cache HIT → " + entry.ipAddress;
            }
        }

        // Cache miss → query upstream
        misses++;
        String ip = queryUpstream(domain);
        entry = new DNSEntry(domain, ip, 300); // TTL = 300s
        synchronized (cache) {
            cache.put(domain, entry);
        }
        totalLookupTime += (System.nanoTime() - start);
        return "Cache MISS → " + ip + " (TTL: 300s)";
    }

    // Simulated upstream DNS query
    private String queryUpstream(String domain) {
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        return "172.217." + new Random().nextInt(255) + "." + new Random().nextInt(255);
    }

    // Cache stats
    public String getCacheStats() {
        int total = hits + misses;
        double hitRate = total == 0 ? 0 : (hits * 100.0 / total);
        double avgTime = total == 0 ? 0 : (totalLookupTime / total) / 1_000_000.0;
        return String.format("Hit Rate: %.2f%%, Avg Lookup Time: %.2fms", hitRate, avgTime);
    }

    // Demo
    public static void main(String[] args) throws InterruptedException {
        TTL dns = new TTL();

        System.out.println(dns.resolve("google.com")); // MISS
        System.out.println(dns.resolve("google.com")); // HIT
        Thread.sleep(310_000); // wait for TTL expiry
        System.out.println(dns.resolve("google.com")); // EXPIRED → MISS
        System.out.println(dns.getCacheStats());
    }
}
