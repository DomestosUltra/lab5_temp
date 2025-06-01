package org.example.lab4;

import java.util.Random;
import java.util.Vector;

public class PersonAI extends BaseAI {
    private final double canvasWidth;
    private final double canvasHeight;
    private final double moveSpeed;
    private final Random random = new Random();
    
    public PersonAI(Vector<Record> records, double canvasWidth, double canvasHeight, double moveSpeed) {
        super(records);
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.moveSpeed = moveSpeed;
    }
    
    @Override
    protected void processMovement() {
        for (Record record : records) {
            if (record instanceof PersonRecord) {
                PersonRecord person = (PersonRecord) record;
                synchronized (person) {
                    if (!person.hasReachedDestination()) {
                        if (!person.hasDestination()) {

                            double targetX = person.getX();
                            double targetY = person.getY();
                            double halfWidth = canvasWidth / 2;
                            double halfHeight = canvasHeight / 2;
                            
                            if (targetX >= halfWidth && targetY >= halfHeight) {
                                person.setDestination(targetX, targetY);
                                person.setReachedDestination(true);
                                continue;
                            }
                            
                            double destX = halfWidth + random.nextDouble() * (halfWidth - person.getImageWidth());
                            double destY = halfHeight + random.nextDouble() * (halfHeight - person.getImageHeight());
                            person.setDestination(destX, destY);
                        }
                        
                        double deltaX = person.getDestX() - person.getX();
                        double deltaY = person.getDestY() - person.getY();
                        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                        
                        if (distance <= moveSpeed) {
                            person.setPosition(person.getDestX(), person.getDestY());
                            person.setReachedDestination(true);
                        } else {
                            double moveX = deltaX / distance * moveSpeed;
                            double moveY = deltaY / distance * moveSpeed;
                            person.setPosition(person.getX() + moveX, person.getY() + moveY);
                        }
                    }
                }
            }
        }
    }
} 