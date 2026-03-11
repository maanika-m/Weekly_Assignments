import java.util.*;

class VideoData {

    String videoId;
    String content;

    public VideoData(String id, String content) {
        this.videoId = id;
        this.content = content;
    }
}

class LRUCache<K,V> extends LinkedHashMap<K,V> {

    private int capacity;

    public LRUCache(int capacity) {
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > capacity;
    }
}

class MultiLevelCache {

    private LRUCache<String, VideoData> L1;
    private LRUCache<String, VideoData> L2;

    private HashMap<String, VideoData> L3Database;

    private HashMap<String, Integer> accessCount;

    private int l1Hits = 0;
    private int l2Hits = 0;
    private int l3Hits = 0;

    public MultiLevelCache() {

        L1 = new LRUCache<>(10000);

        L2 = new LRUCache<>(100000);

        L3Database = new HashMap<>();

        accessCount = new HashMap<>();
    }

    // Add video to database
    public void addVideoToDatabase(String id, String content) {
        L3Database.put(id, new VideoData(id, content));
    }

    public VideoData getVideo(String videoId) {

        long start = System.currentTimeMillis();

        // L1 Cache
        if (L1.containsKey(videoId)) {

            l1Hits++;

            System.out.println("L1 Cache HIT (0.5ms)");

            return L1.get(videoId);
        }

        System.out.println("L1 Cache MISS");

        // L2 Cache
        if (L2.containsKey(videoId)) {

            l2Hits++;

            VideoData video = L2.get(videoId);

            System.out.println("L2 Cache HIT (5ms)");

            // Promote to L1
            L1.put(videoId, video);

            System.out.println("Promoted to L1");

            return video;
        }

        System.out.println("L2 Cache MISS");

        // L3 Database
        VideoData video = L3Database.get(videoId);

        if (video != null) {

            l3Hits++;

            System.out.println("L3 Database HIT (150ms)");

            L2.put(videoId, video);

            accessCount.put(videoId,
                    accessCount.getOrDefault(videoId, 0) + 1);

            System.out.println("Added to L2");

            return video;
        }

        System.out.println("Video not found");

        return null;
    }

    // Cache invalidation
    public void invalidate(String videoId) {

        L1.remove(videoId);
        L2.remove(videoId);

        System.out.println("Cache invalidated for " + videoId);
    }

    public void getStatistics() {

        int total = l1Hits + l2Hits + l3Hits;

        double l1Rate = (l1Hits * 100.0) / total;
        double l2Rate = (l2Hits * 100.0) / total;
        double l3Rate = (l3Hits * 100.0) / total;

        System.out.println("\nCache Statistics");

        System.out.println("L1 Hit Rate: " + String.format("%.2f", l1Rate) + "%");
        System.out.println("L2 Hit Rate: " + String.format("%.2f", l2Rate) + "%");
        System.out.println("L3 Hit Rate: " + String.format("%.2f", l3Rate) + "%");

        double overall = ((l1Hits + l2Hits) * 100.0) / total;

        System.out.println("Overall Cache Hit Rate: " + String.format("%.2f", overall) + "%");
    }
}

public class MultiLevelCacheSystemApp {

    public static void main(String[] args) {

        MultiLevelCache cache = new MultiLevelCache();

        cache.addVideoToDatabase("video_123", "Movie A");
        cache.addVideoToDatabase("video_456", "Movie B");
        cache.addVideoToDatabase("video_999", "Movie C");

        System.out.println("\nRequest 1:");
        cache.getVideo("video_123");

        System.out.println("\nRequest 2:");
        cache.getVideo("video_123");

        System.out.println("\nRequest 3:");
        cache.getVideo("video_999");

        cache.getStatistics();
    }
}