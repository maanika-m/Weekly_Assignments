import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UsernameAvailability {
    // Store registered usernames
    private ConcurrentHashMap<String, Integer> usernameMap;
    // Track attempted usernames
    private ConcurrentHashMap<String, Integer> attemptFrequency;

    public UsernameAvailability() {
        usernameMap = new ConcurrentHashMap<>();
        attemptFrequency = new ConcurrentHashMap<>();
    }

    // Register a username (for simulation)
    public void registerUsername(String username, int userId) {
        usernameMap.put(username.toLowerCase(), userId);
    }

    // Check availability in O(1)
    public boolean checkAvailability(String username) {
        String key = username.toLowerCase();
        attemptFrequency.merge(key, 1, Integer::sum);
        return !usernameMap.containsKey(key);
    }

    // Suggest alternatives if taken
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        String base = username.toLowerCase();

        for (int i = 1; i <= 3; i++) {
            String suggestion = base + i;
            if (!usernameMap.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        // Replace underscore with dot
        String modified = base.replace("_", ".");
        if (!usernameMap.containsKey(modified)) {
            suggestions.add(modified);
        }

        return suggestions;
    }

    // Get most attempted username
    public String getMostAttempted() {
        return attemptFrequency.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey() + " (" + entry.getValue() + " attempts)")
                .orElse("No attempts yet");
    }

    // Demo
    public static void main(String[] args) {
        UsernameAvailability ua = new UsernameAvailability();

        ua.registerUsername("john_doe", 1);
        ua.registerUsername("admin", 2);

        System.out.println(ua.checkAvailability("john_doe")); // false
        System.out.println(ua.checkAvailability("jane_smith")); // true
        System.out.println(ua.suggestAlternatives("john_doe")); // [john_doe1, john_doe2, john.doe]
        System.out.println(ua.getMostAttempted()); // admin (1 attempts)
    }
}
