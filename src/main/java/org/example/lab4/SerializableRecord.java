package org.example.lab4;

import java.io.Serializable;

public class SerializableRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private double x, y;
    private double destX, destY;
    private boolean hasDestination;
    private boolean reachedDestination;
    private long birthTime;
    private long lifetime;
    private int id;
    private String recordType; // "PERSON" или "LEGAL_ENTITY"
    
    public SerializableRecord(Record record) {
        this.x = record.getX();
        this.y = record.getY();
        this.destX = record.getDestX();
        this.destY = record.getDestY();
        this.hasDestination = record.hasDestination();
        this.reachedDestination = record.hasReachedDestination();
        this.birthTime = record.getBirthTime();
        this.lifetime = record.getLifetime();
        this.id = record.getId();
        this.recordType = (record instanceof PersonRecord) ? "PERSON" : "LEGAL_ENTITY";
    }
    
    // Геттеры
    public double getX() { return x; }
    public double getY() { return y; }
    public double getDestX() { return destX; }
    public double getDestY() { return destY; }
    public boolean hasDestination() { return hasDestination; }
    public boolean hasReachedDestination() { return reachedDestination; }
    public long getBirthTime() { return birthTime; }
    public long getLifetime() { return lifetime; }
    public int getId() { return id; }
    public String getRecordType() { return recordType; }
    
    // Сеттеры для корректировки времени
    public void setBirthTime(long birthTime) { this.birthTime = birthTime; }
}