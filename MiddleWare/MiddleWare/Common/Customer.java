package MiddleWare.Common;

import java.util.*;

public class Customer {
    private int cid;
    private List<HashMap<Integer, Integer>> flights = new ArrayList<>();
    private List<HashMap<String, Integer>> cars = new ArrayList<>();
    private List<HashMap<String, Integer>> rooms = new ArrayList<>();

    public Customer(int id) {
        cid = id;
        // Initialize with empty HashMaps
        flights.add(new HashMap<>());
        cars.add(new HashMap<>());
        rooms.add(new HashMap<>());
    }

    public boolean addFlight(int flight) {
        if (!flights.isEmpty()) {
            HashMap<Integer, Integer> flightMap = flights.get(0);
            flightMap.put(flight, flightMap.getOrDefault(flight, 0) + 1);
            return true;
        }
        return false;
    }

    public boolean addCar(String car) {
        if (!cars.isEmpty()) {
            HashMap<String, Integer> carMap = cars.get(0);
            carMap.put(car, carMap.getOrDefault(car, 0) + 1);
            return true;
        }
        return false;
    }


    public boolean addRoom(String room) {
        if (!rooms.isEmpty()) {
            HashMap<String, Integer> roomMap = rooms.get(0);
            roomMap.put(room, roomMap.getOrDefault(room, 0) + 1);
            return true;
        }
        return false;
    }

    public int removeFlight(int flight) {
        int totalRemoved = 0;
        for (HashMap<Integer, Integer> flightMap : flights) {
            if (flightMap.containsKey(flight)) {
                totalRemoved += flightMap.get(flight);
                flightMap.remove(flight);
            }
        }
        if (totalRemoved > 0) {
            System.out.println("Removed " + totalRemoved + " flight(s) with number " + flight);
        }
        return totalRemoved;
    }

    public int removeCar(String car) {
        int totalRemoved = 0;
        for (HashMap<String, Integer> carMap : cars) {
            if (carMap.containsKey(car)) {
                totalRemoved += carMap.get(car);
                carMap.remove(car);
            }
        }
        if (totalRemoved > 0) {
            System.out.println("Removed " + totalRemoved + " car(s) at location " + car);
        }
        return totalRemoved;
    }


    public int removeRoom(String room) {
        int totalRemoved = 0;
        for (HashMap<String, Integer> roomMap : rooms) {
            if (roomMap.containsKey(room)) {
                totalRemoved += roomMap.get(room);
                roomMap.remove(room);
            }
        }
        if (totalRemoved > 0) {
            System.out.println("Removed " + totalRemoved + " room(s) at location " + room);
        }
        return totalRemoved;
    }


    public int getFlightCount(int flight) {
        int totalCount = 0;
        for (HashMap<Integer, Integer> flightMap : flights) {
            totalCount += flightMap.getOrDefault(flight, 0);
        }
        return totalCount;
    }

    public int getCarCount(String car) {
        int totalCount = 0;
        for (HashMap<String, Integer> carMap : cars) {
            totalCount += carMap.getOrDefault(car, 0);
        }
        return totalCount;
    }

    public int getRoomCount(String room) {
        int totalCount = 0;
        for (HashMap<String, Integer> roomMap : rooms) {
            totalCount += roomMap.getOrDefault(room, 0);
        }
        return totalCount;
    }


    public int getCustomerId() {
        return cid;
    }

    public Map<Integer, Integer> getFlightReservations() {
        Map<Integer, Integer> allFlights = new HashMap<>();
        for (HashMap<Integer, Integer> flightMap : flights) {
            for (Map.Entry<Integer, Integer> entry : flightMap.entrySet()) {
                allFlights.put(entry.getKey(),
                        allFlights.getOrDefault(entry.getKey(), 0) + entry.getValue());
            }
        }
        return allFlights;
    }

    public Map<String, Integer> getCarReservations() {
        Map<String, Integer> allCars = new HashMap<>();
        for (HashMap<String, Integer> carMap : cars) {
            for (Map.Entry<String, Integer> entry : carMap.entrySet()) {
                allCars.put(entry.getKey(),
                        allCars.getOrDefault(entry.getKey(), 0) + entry.getValue());
            }
        }
        return allCars;
    }

    public Map<String, Integer> getRoomReservations() {
        Map<String, Integer> allRooms = new HashMap<>();
        for (HashMap<String, Integer> roomMap : rooms) {
            for (Map.Entry<String, Integer> entry : roomMap.entrySet()) {
                allRooms.put(entry.getKey(),
                        allRooms.getOrDefault(entry.getKey(), 0) + entry.getValue());
            }
        }
        return allRooms;
    }
}