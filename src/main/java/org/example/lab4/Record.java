package org.example.lab4;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.Random;

public abstract class Record {
    protected double x, y;
    protected double destX, destY;
    protected boolean hasDestination = false;
    protected boolean reachedDestination = false;
    protected Image image;
    protected Random random = new Random();
    protected double speedX, speedY;
    protected long birthTime;
    protected long lifetime;
    protected int id;

    public Record(double x, double y, Image image, long birthTime, long lifetime, int id) {
        this.x = x;
        this.y = y;
        this.image = image;
        this.speedX = random.nextDouble() * 1;
        this.speedY = random.nextDouble() * 1;
        this.birthTime = birthTime;
        this.lifetime = lifetime;
        this.id = id; // ID теперь передается извне
    }

    public abstract void update();

    public void draw(GraphicsContext gc) {
        gc.drawImage(image, x, y);
    }

    public long getBirthTime() {
        return birthTime;
    }

    public long getLifetime() {
        return lifetime;
    }

    public int getId() {
        return id;
    }

    public boolean isExpired(long currentTime) {
        return (currentTime - birthTime) >= lifetime;
    }
    
    // New methods for AI movement
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double getDestX() {
        return destX;
    }
    
    public double getDestY() {
        return destY;
    }
    
    public void setDestination(double destX, double destY) {
        this.destX = destX;
        this.destY = destY;
        this.hasDestination = true;
    }
    
    public boolean hasDestination() {
        return hasDestination;
    }
    
    public boolean hasReachedDestination() {
        return reachedDestination;
    }
    
    public void setReachedDestination(boolean reachedDestination) {
        this.reachedDestination = reachedDestination;
    }
    
    public double getImageWidth() {
        return image.getWidth();
    }
    
    public double getImageHeight() {
        return image.getHeight();
    }
}