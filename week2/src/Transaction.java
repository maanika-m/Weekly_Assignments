import java.util.*;

class Transaction {

    int id;
    int amount;
    String merchant;
    String account;
    int time; // minutes since start of day

    public Transaction(int id, int amount, String merchant, String account, int time) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        this.time = time;
    }

    public String toString() {
        return "{id:" + id + ", amount:" + amount + ", merchant:" + merchant + "}";
    }
}

class TransactionAnalyzer {

    List<Transaction> transactions;

    public TransactionAnalyzer(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    // Classic Two Sum
    public List<String> findTwoSum(int target) {

        HashMap<Integer, Transaction> map = new HashMap<>();

        List<String> result = new ArrayList<>();

        for (Transaction t : transactions) {

            int complement = target - t.amount;

            if (map.containsKey(complement)) {

                Transaction other = map.get(complement);

                result.add("(" + other.id + "," + t.id + ")");
            }

            map.put(t.amount, t);
        }

        return result;
    }

    // Two sum with 1 hour window
    public List<String> findTwoSumWithTime(int target) {

        HashMap<Integer, Transaction> map = new HashMap<>();

        List<String> result = new ArrayList<>();

        for (Transaction t : transactions) {

            int complement = target - t.amount;

            if (map.containsKey(complement)) {

                Transaction other = map.get(complement);

                if (Math.abs(t.time - other.time) <= 60) {

                    result.add("(" + other.id + "," + t.id + ")");
                }
            }

            map.put(t.amount, t);
        }

        return result;
    }

    // Duplicate detection
    public void detectDuplicates() {

        HashMap<String, List<Transaction>> map = new HashMap<>();

        for (Transaction t : transactions) {

            String key = t.amount + "_" + t.merchant;

            map.putIfAbsent(key, new ArrayList<>());

            map.get(key).add(t);
        }

        for (String key : map.keySet()) {

            List<Transaction> list = map.get(key);

            if (list.size() > 1) {

                System.out.println("Duplicate detected: " + list);
            }
        }
    }

    // K Sum (general case)
    public void findKSum(int k, int target) {

        List<Integer> nums = new ArrayList<>();

        for (Transaction t : transactions) {
            nums.add(t.amount);
        }

        Collections.sort(nums);

        kSum(nums, target, k, 0, new ArrayList<>());
    }

    private void kSum(List<Integer> nums, int target, int k, int start, List<Integer> path) {

        if (k == 2) {

            int left = start;
            int right = nums.size() - 1;

            while (left < right) {

                int sum = nums.get(left) + nums.get(right);

                if (sum == target) {

                    List<Integer> result = new ArrayList<>(path);

                    result.add(nums.get(left));
                    result.add(nums.get(right));

                    System.out.println(result);

                    left++;
                    right--;

                } else if (sum < target) {
                    left++;
                } else {
                    right--;
                }
            }

            return;
        }

        for (int i = start; i < nums.size(); i++) {

            path.add(nums.get(i));

            kSum(nums, target - nums.get(i), k - 1, i + 1, path);

            path.remove(path.size() - 1);
        }
    }
}

public class TransactionAnalyzerApp {

    public static void main(String[] args) {

        List<Transaction> transactions = new ArrayList<>();

        transactions.add(new Transaction(1, 500, "StoreA", "acc1", 600));
        transactions.add(new Transaction(2, 300, "StoreB", "acc2", 615));
        transactions.add(new Transaction(3, 200, "StoreC", "acc3", 630));
        transactions.add(new Transaction(4, 500, "StoreA", "acc4", 700));

        TransactionAnalyzer analyzer = new TransactionAnalyzer(transactions);

        System.out.println("Two Sum:");
        System.out.println(analyzer.findTwoSum(500));

        System.out.println("\nTwo Sum (Time Window):");
        System.out.println(analyzer.findTwoSumWithTime(500));

        System.out.println("\nDuplicate Detection:");
        analyzer.detectDuplicates();

        System.out.println("\nK Sum (k=3 target=1000):");
        analyzer.findKSum(3, 1000);
    }
}