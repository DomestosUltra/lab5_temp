package org.example.lab4;

import java.util.Random;
import java.util.Vector;

public class LegalEntityAI extends BaseAI {
    private final double canvasWidth;
    private final double canvasHeight;
    private final double moveSpeed;
    private final Random random = new Random();
    
    public LegalEntityAI(Vector<Record> records, double canvasWidth, double canvasHeight, double moveSpeed) {
        super(records);
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.moveSpeed = moveSpeed;
    }
    
    @Override
    protected void processMovement() {
        for (Record record : records) {
            if (record instanceof LegalEntityRecord) {
                LegalEntityRecord legalEntity = (LegalEntityRecord) record;
                synchronized (legalEntity) {
                    if (!legalEntity.hasReachedDestination()) {
                        if (!legalEntity.hasDestination()) {
                            double targetX = legalEntity.getX();
                            double targetY = legalEntity.getY();
                            double halfWidth = canvasWidth / 2;
                            double halfHeight = canvasHeight / 2;
                            
                            if (targetX < halfWidth && targetY < halfHeight) {
                                legalEntity.setDestination(targetX, targetY);
                                legalEntity.setReachedDestination(true);
                                continue;
                            }
                            
                            double destX = random.nextDouble() * (halfWidth - legalEntity.getImageWidth());
                            double destY = random.nextDouble() * (halfHeight - legalEntity.getImageHeight());
                            legalEntity.setDestination(destX, destY);
                        }
                        
                        double deltaX = legalEntity.getDestX() - legalEntity.getX();
                        double deltaY = legalEntity.getDestY() - legalEntity.getY();
                        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                        
                        if (distance <= moveSpeed) {
                            legalEntity.setPosition(legalEntity.getDestX(), legalEntity.getDestY());
                            legalEntity.setReachedDestination(true);
                        } else {
                            double moveX = deltaX / distance * moveSpeed;
                            double moveY = deltaY / distance * moveSpeed;
                            legalEntity.setPosition(legalEntity.getX() + moveX, legalEntity.getY() + moveY);
                        }
                    }
                }
            }
        }
    }
} 