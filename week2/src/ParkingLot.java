import java.util.*;

class ParkingSpot {

    String licensePlate;
    long entryTime;
    String status;

    public ParkingSpot() {
        status = "EMPTY";
    }
}

class ParkingLot {

    private ParkingSpot[] table;

    private int size = 500;

    private int occupiedSpots = 0;

    private int totalProbes = 0;

    private int totalParks = 0;

    public ParkingLot() {

        table = new ParkingSpot[size];

        for (int i = 0; i < size; i++) {
            table[i] = new ParkingSpot();
        }
    }

    // Custom hash function
    private int hash(String plate) {

        int hash = 0;

        for (char c : plate.toCharArray()) {
            hash = (hash * 31 + c) % size;
        }

        return hash;
    }

    // Park vehicle
    public void parkVehicle(String plate) {

        int index = hash(plate);

        int probes = 0;

        while (table[index].status.equals("OCCUPIED")) {

            index = (index + 1) % size;

            probes++;
        }

        table[index].licensePlate = plate;

        table[index].entryTime = System.currentTimeMillis();

        table[index].status = "OCCUPIED";

        occupiedSpots++;

        totalProbes += probes;

        totalParks++;

        System.out.println("Assigned spot #" + index +
                " (" + probes + " probes)");
    }

    // Exit vehicle
    public void exitVehicle(String plate) {

        int index = hash(plate);

        int probes = 0;

        while (!table[index].status.equals("EMPTY")) {

            if (table[index].status.equals("OCCUPIED") &&
                    table[index].licensePlate.equals(plate)) {

                long duration =
                        System.currentTimeMillis() -
                                table[index].entryTime;

                double hours = duration / (1000.0 * 60 * 60);

                double fee = hours * 5.0;

                table[index].status = "DELETED";

                occupiedSpots--;

                System.out.println(
                        "Spot #" + index +
                                " freed, Duration: " +
                                String.format("%.2f", hours) +
                                "h, Fee: $" +
                                String.format("%.2f", fee));

                return;
            }

            index = (index + 1) % size;

            probes++;
        }

        System.out.println("Vehicle not found.");
    }

    // Find nearest available spot
    public int findNearestSpot() {

        for (int i = 0; i < size; i++) {

            if (!table[i].status.equals("OCCUPIED")) {
                return i;
            }
        }

        return -1;
    }

    // Parking statistics
    public void getStatistics() {

        double occupancy =
                (occupiedSpots * 100.0) / size;

        double avgProbes =
                totalParks == 0 ? 0 :
                        (double) totalProbes / totalParks;

        System.out.println("\nParking Statistics");

        System.out.println("Occupancy: "
                + String.format("%.2f", occupancy) + "%");

        System.out.println("Average Probes: "
                + String.format("%.2f", avgProbes));

        System.out.println("Peak Hour: 2-3 PM (simulated)");
    }
}

public class ParkingLotOpenAddressingApp {

    public static void main(String[] args) {

        ParkingLot lot = new ParkingLot();

        lot.parkVehicle("ABC-1234");

        lot.parkVehicle("ABC-1235");

        lot.parkVehicle("XYZ-9999");

        lot.exitVehicle("ABC-1234");

        lot.getStatistics();
    }
}