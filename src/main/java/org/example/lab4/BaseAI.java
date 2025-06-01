package org.example.lab4;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseAI implements Runnable {
    protected Vector<Record> records;
    protected Thread aiThread;
    protected AtomicBoolean isRunning;
    protected AtomicBoolean isPaused;
    protected final Object pauseLock = new Object();
    protected int priority = Thread.NORM_PRIORITY;
    
    public BaseAI(Vector<Record> records) {
        this.records = records;
        this.isRunning = new AtomicBoolean(false);
        this.isPaused = new AtomicBoolean(false);
    }
    
    public void start() {
        if (aiThread == null || !aiThread.isAlive()) {
            isRunning.set(true);
            isPaused.set(false);
            aiThread = new Thread(this);
            aiThread.setPriority(priority);
            aiThread.start();
        }
    }
    
    public void stop() {
        isRunning.set(false);
        resume();
        if (aiThread != null) {
            try {
                aiThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public void pause() {
        isPaused.set(true);
    }
    
    public void resume() {
        if (isPaused.get()) {
            synchronized (pauseLock) {
                isPaused.set(false);
                pauseLock.notify();
            }
        }
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
        if (aiThread != null && aiThread.isAlive()) {
            aiThread.setPriority(priority);
        }
    }
    
    public boolean isRunning() {
        return isRunning.get();
    }
    
    public boolean isPaused() {
        return isPaused.get();
    }
    
    @Override
    public void run() {
        while (isRunning.get()) {
            synchronized (pauseLock) {
                while (isPaused.get()) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            
            processMovement();
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
    
    protected abstract void processMovement();
} 