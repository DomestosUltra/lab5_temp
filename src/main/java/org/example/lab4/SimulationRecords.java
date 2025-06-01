package org.example.lab4;

import java.util.*;
import javafx.scene.image.Image;

public class SimulationRecords {
    private static final SimulationRecords instance = new SimulationRecords();
    private Vector<Record> records;
    private TreeSet<Integer> usedIds;
    private HashMap<Integer, Long> birthTimes;
    private Random random = new Random();

    private SimulationRecords() {
        records = new Vector<>();
        usedIds = new TreeSet<>();
        birthTimes = new HashMap<>();
    }

    public static SimulationRecords getInstance() {
        return instance;
    }

    public Vector<Record> getRecords() {
        return records;
    }

    public HashMap<Integer, Long> getBirthTimes() {
        return birthTimes;
    }

    private int generateUniqueId() {
        int newId;
        do {
            newId = random.nextInt(1000000);
        } while (usedIds.contains(newId));
        return newId;
    }

    public void addPersonRecord(double x, double y, Image individualImage, long birthTime, long lifetime) {
        int id = generateUniqueId();
        Record record = new PersonRecord(x, y, individualImage, birthTime, lifetime, id);
        records.add(record);
        usedIds.add(id);
        birthTimes.put(id, birthTime);
    }

    public void addLegalEntityRecord(double x, double y, Image legalImage, long birthTime, long lifetime) {
        int id = generateUniqueId();
        Record record = new LegalEntityRecord(x, y, legalImage, birthTime, lifetime, id);
        records.add(record);
        usedIds.add(id);
        birthTimes.put(id, birthTime);
    }

    public void removeExpired(long currentTime) {
        Iterator<Record> iterator = records.iterator();
        while (iterator.hasNext()) {
            Record record = iterator.next();
            if (record.isExpired(currentTime)) {
                int id = record.getId();
                iterator.remove();
                usedIds.remove(id);
                birthTimes.remove(id);
            }
        }
    }

    public void clearRecords() {
        records.clear();
        usedIds.clear();
        birthTimes.clear();
    }
}