import java.util.*;
import java.util.concurrent.*;

class PageViewEvent {
    String url;
    String userId;
    String source;

    PageViewEvent(String url, String userId, String source) {
        this.url = url;
        this.userId = userId;
        this.source = source;
    }
}

public class TimeAnalytics {
    private Map<String, Integer> pageViews;
    private Map<String, Set<String>> uniqueVisitors;
    private Map<String, Integer> trafficSources;

    public TimeAnalytics() {
        pageViews = new ConcurrentHashMap<>();
        uniqueVisitors = new ConcurrentHashMap<>();
        trafficSources = new ConcurrentHashMap<>();

        // Dashboard updater every 5 seconds
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            System.out.println(getDashboard());
        }, 5, 5, TimeUnit.SECONDS);
    }

    // Process incoming event
    public void processEvent(PageViewEvent event) {
        // Update page views
        pageViews.merge(event.url, 1, Integer::sum);

        // Track unique visitors
        uniqueVisitors.computeIfAbsent(event.url, k -> ConcurrentHashMap.newKeySet()).add(event.userId);

        // Track traffic source
        trafficSources.merge(event.source.toLowerCase(), 1, Integer::sum);
    }

    // Get dashboard snapshot
    public String getDashboard() {
        StringBuilder sb = new StringBuilder();
        sb.append("Top Pages:\n");

        // Sort by views
        pageViews.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .forEach(entry -> {
                    String url = entry.getKey();
                    int views = entry.getValue();
                    int uniques = uniqueVisitors.getOrDefault(url, Collections.emptySet()).size();
                    sb.append(url).append(" - ").append(views)
                            .append(" views (").append(uniques).append(" unique)\n");
                });

        sb.append("\nTraffic Sources:\n");
        int total = trafficSources.values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<String, Integer> entry : trafficSources.entrySet()) {
            double percent = total == 0 ? 0 : (entry.getValue() * 100.0 / total);
            sb.append(entry.getKey()).append(": ").append(String.format("%.1f", percent)).append("%\n");
        }

        return sb.toString();
    }

    // Demo
    public static void main(String[] args) throws InterruptedException {
        TimeAnalytics analytics = new TimeAnalytics();

        analytics.processEvent(new PageViewEvent("/article/breaking-news", "user_123", "google"));
        analytics.processEvent(new PageViewEvent("/article/breaking-news", "user_456", "facebook"));
        analytics.processEvent(new PageViewEvent("/sports/championship", "user_789", "direct"));

        Thread.sleep(15000); // Let dashboard print updates
    }
}
