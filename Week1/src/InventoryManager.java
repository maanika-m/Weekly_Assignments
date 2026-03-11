import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class InventoryManager {
    // Product stock levels
    private ConcurrentHashMap<String, AtomicInteger> stockMap;
    // Waiting list per product
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Integer>> waitingList;

    public InventoryManager() {
        stockMap = new ConcurrentHashMap<>();
        waitingList = new ConcurrentHashMap<>();
    }

    // Initialize product with stock
    public void addProduct(String productId, int stockCount) {
        stockMap.put(productId, new AtomicInteger(stockCount));
        waitingList.put(productId, new ConcurrentLinkedQueue<>());
    }

    // Check stock availability
    public String checkStock(String productId) {
        AtomicInteger stock = stockMap.get(productId);
        if (stock == null) return "Product not found";
        return stock.get() + " units available";
    }

    // Process purchase request
    public String purchaseItem(String productId, int userId) {
        AtomicInteger stock = stockMap.get(productId);
        if (stock == null) return "Product not found";

        // Atomic decrement
        int currentStock;
        do {
            currentStock = stock.get();
            if (currentStock == 0) {
                waitingList.get(productId).add(userId);
                int position = waitingList.get(productId).size();
                return "Added to waiting list, position #" + position;
            }
        } while (!stock.compareAndSet(currentStock, currentStock - 1));

        return "Success, " + (currentStock - 1) + " units remaining";
    }

    // Get waiting list for a product
    public List<Integer> getWaitingList(String productId) {
        return new ArrayList<>(waitingList.get(productId));
    }

    // Demo
    public static void main(String[] args) {
        InventoryManager im = new InventoryManager();
        im.addProduct("IPHONE15_256GB", 100);

        System.out.println(im.checkStock("IPHONE15_256GB")); // 100 units available
        System.out.println(im.purchaseItem("IPHONE15_256GB", 12345)); // Success, 99 units remaining
        System.out.println(im.purchaseItem("IPHONE15_256GB", 67890)); // Success, 98 units remaining

        // Simulate overselling prevention
        for (int i = 0; i < 100; i++) {
            im.purchaseItem("IPHONE15_256GB", i);
        }
        System.out.println(im.purchaseItem("IPHONE15_256GB", 99999)); // Added to waiting list, position #1
    }
}
