import java.util.*;

class TrieNode {

    HashMap<Character, TrieNode> children;
    List<String> queries;

    public TrieNode() {
        children = new HashMap<>();
        queries = new ArrayList<>();
    }
}

class AutocompleteSystem {

    private TrieNode root;

    private HashMap<String, Integer> frequencyMap;

    public AutocompleteSystem() {

        root = new TrieNode();

        frequencyMap = new HashMap<>();
    }

    // Insert query into Trie
    public void insertQuery(String query) {

        TrieNode node = root;

        for (char c : query.toCharArray()) {

            node.children.putIfAbsent(c, new TrieNode());

            node = node.children.get(c);

            node.queries.add(query);
        }

        frequencyMap.put(query,
                frequencyMap.getOrDefault(query, 0) + 1);
    }

    // Search autocomplete suggestions
    public List<String> search(String prefix) {

        TrieNode node = root;

        for (char c : prefix.toCharArray()) {

            if (!node.children.containsKey(c)) {
                return new ArrayList<>();
            }

            node = node.children.get(c);
        }

        List<String> results = node.queries;

        PriorityQueue<String> pq =
                new PriorityQueue<>((a, b) ->
                        frequencyMap.get(a) - frequencyMap.get(b));

        for (String query : results) {

            pq.offer(query);

            if (pq.size() > 10) {
                pq.poll();
            }
        }

        List<String> suggestions = new ArrayList<>();

        while (!pq.isEmpty()) {
            suggestions.add(pq.poll());
        }

        Collections.reverse(suggestions);

        return suggestions;
    }

    // Update frequency after search
    public void updateFrequency(String query) {

        frequencyMap.put(query,
                frequencyMap.getOrDefault(query, 0) + 1);
    }
}

public class AutocompleteSystemApp {

    public static void main(String[] args) {

        AutocompleteSystem system = new AutocompleteSystem();

        // Insert sample queries
        system.insertQuery("java tutorial");
        system.insertQuery("javascript");
        system.insertQuery("java download");
        system.insertQuery("java 21 features");
        system.insertQuery("java tutorial");
        system.insertQuery("java tutorial");

        // Search prefix
        List<String> results = system.search("jav");

        System.out.println("Suggestions:");

        int rank = 1;

        for (String r : results) {

            System.out.println(rank + ". " + r);

            rank++;
        }

        // Update frequency
        system.updateFrequency("java 21 features");

        System.out.println("\nFrequency Updated for 'java 21 features'");
    }
}
