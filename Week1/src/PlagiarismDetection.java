import java.util.*;

class PlagiarismDetection {
    private Map<String, Set<String>> ngramIndex;
    private int n; // size of n-gram

    public PlagiarismDetection(int n) {
        this.n = n;
        this.ngramIndex = new HashMap<>();
    }

    // Break text into n-grams
    private List<String> extractNGrams(String text) {
        String[] words = text.split("\\s+");
        List<String> ngrams = new ArrayList<>();
        for (int i = 0; i <= words.length - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                sb.append(words[i + j]).append(" ");
            }
            ngrams.add(sb.toString().trim());
        }
        return ngrams;
    }

    // Index a document
    public void indexDocument(String docId, String text) {
        List<String> ngrams = extractNGrams(text);
        for (String ng : ngrams) {
            ngramIndex.computeIfAbsent(ng, k -> new HashSet<>()).add(docId);
        }
    }

    // Analyze similarity with existing documents
    public Map<String, Double> analyzeDocument(String docId, String text) {
        List<String> ngrams = extractNGrams(text);
        Map<String, Integer> matchCount = new HashMap<>();

        for (String ng : ngrams) {
            if (ngramIndex.containsKey(ng)) {
                for (String existingDoc : ngramIndex.get(ng)) {
                    if (!existingDoc.equals(docId)) {
                        matchCount.put(existingDoc, matchCount.getOrDefault(existingDoc, 0) + 1);
                    }
                }
            }
        }

        Map<String, Double> similarity = new HashMap<>();
        int totalNGrams = ngrams.size();
        for (Map.Entry<String, Integer> entry : matchCount.entrySet()) {
            double percent = (entry.getValue() * 100.0) / totalNGrams;
            similarity.put(entry.getKey(), percent);
        }

        return similarity;
    }

    // Demo
    public static void main(String[] args) {
        PlagiarismDetection detector = new PlagiarismDetection(5);

        // Index existing essays
        detector.indexDocument("essay_089", "This is a sample essay with some unique content.");
        detector.indexDocument("essay_092", "This essay contains a lot of similar words and repeated content.");

        // Analyze new essay
        Map<String, Double> results = detector.analyzeDocument("essay_123",
                "This essay contains a lot of similar words and repeated content.");

        for (Map.Entry<String, Double> entry : results.entrySet()) {
            System.out.println("Found similarity with " + entry.getKey() +
                    " → " + String.format("%.2f", entry.getValue()) + "%");
        }
    }
}
