import java.util.concurrent.ConcurrentHashMap;

class TokenBucket {

    private int tokens;
    private long lastRefillTime;
    private int maxTokens;
    private double refillRate;

    public TokenBucket(int maxTokens, double refillRate) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.tokens = maxTokens;
        this.lastRefillTime = System.currentTimeMillis();
    }

    public synchronized boolean allowRequest() {
        refill();

        if (tokens > 0) {
            tokens--;
            return true;
        }
        return false;
    }

    private void refill() {

        long now = System.currentTimeMillis();

        double secondsPassed = (now - lastRefillTime) / 1000.0;

        int tokensToAdd = (int) (secondsPassed * refillRate);

        if (tokensToAdd > 0) {

            tokens = Math.min(maxTokens, tokens + tokensToAdd);

            lastRefillTime = now;
        }
    }

    public int getRemainingTokens() {
        refill();
        return tokens;
    }

    public int getUsedTokens() {
        return maxTokens - tokens;
    }

    public int getLimit() {
        return maxTokens;
    }

    public long getResetTime() {

        long now = System.currentTimeMillis();

        long secondsToFull =
                (long) ((maxTokens - tokens) / refillRate);

        return now + (secondsToFull * 1000);
    }
}

class RateLimiter {

    private ConcurrentHashMap<String, TokenBucket> buckets
            = new ConcurrentHashMap<>();

    private final int MAX_TOKENS = 1000;

    private final double REFILL_RATE = 1000.0 / 3600.0;

    public String checkRateLimit(String clientId) {

        buckets.putIfAbsent(clientId,
                new TokenBucket(MAX_TOKENS, REFILL_RATE));

        TokenBucket bucket = buckets.get(clientId);

        boolean allowed = bucket.allowRequest();

        if (allowed) {

            return "Allowed (" +
                    bucket.getRemainingTokens()
                    + " requests remaining)";
        }

        long retrySeconds =
                (bucket.getResetTime() - System.currentTimeMillis()) / 1000;

        return "Denied (0 requests remaining, retry after "
                + retrySeconds + "s)";
    }

    public String getRateLimitStatus(String clientId) {

        buckets.putIfAbsent(clientId,
                new TokenBucket(MAX_TOKENS, REFILL_RATE));

        TokenBucket bucket = buckets.get(clientId);

        return "{used: "
                + bucket.getUsedTokens()
                + ", limit: "
                + bucket.getLimit()
                + ", reset: "
                + bucket.getResetTime()
                + "}";
    }
}

public class DistributedRateLimiterApp {

    public static void main(String[] args) {

        RateLimiter limiter = new RateLimiter();

        String clientId = "abc123";

        for (int i = 0; i < 5; i++) {

            System.out.println(
                    limiter.checkRateLimit(clientId));
        }

        System.out.println(
                limiter.getRateLimitStatus(clientId));
    }
}