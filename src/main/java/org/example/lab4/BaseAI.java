package org.example.lab4;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseAI implements Runnable {
    protected Vector<Record> records;
    protected Thread aiThread;
    protected AtomicBoolean isRunning;
    protected AtomicBoolean isPaused;
    protected final Object pauseLock = new Object();
    protected int priority = Thread.NORM_PRIORITY;
    
    // Каналы для передачи команд
    protected Pipe commandPipe;
    protected WritableByteChannel commandWriter;
    protected ReadableByteChannel commandReader;
    
    public BaseAI(Vector<Record> records) {
        this.records = records;
        this.isRunning = new AtomicBoolean(false);
        this.isPaused = new AtomicBoolean(false);
        
        try {
            // Создаем канал для передачи команд
            commandPipe = Pipe.open();
            commandWriter = commandPipe.sink();
            commandReader = commandPipe.source();
            // Настраиваем неблокирующий режим для чтения
            commandReader.configureBlocking(false);
        } catch (IOException e) {
            System.err.println("Ошибка создания канала команд: " + e.getMessage());
        }
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
        sendCommand(new AICommand(AICommand.CommandType.STOP));
        if (aiThread != null) {
            try {
                aiThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Закрываем каналы
        try {
            if (commandWriter != null) commandWriter.close();
            if (commandReader != null) commandReader.close();
        } catch (IOException e) {
            System.err.println("Ошибка закрытия каналов: " + e.getMessage());
        }
    }
    
    public void pause() {
        isPaused.set(true);
        sendCommand(new AICommand(AICommand.CommandType.PAUSE));
    }
    
    public void resume() {
        if (isPaused.get()) {
            isPaused.set(false);
            sendCommand(new AICommand(AICommand.CommandType.RESUME));
        }
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
        sendCommand(new AICommand(AICommand.CommandType.SET_PRIORITY, priority));
        if (aiThread != null && aiThread.isAlive()) {
            aiThread.setPriority(priority);
        }
    }
    
    private void sendCommand(AICommand command) {
        if (commandWriter == null) return;
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(command);
            oos.close();
            
            byte[] data = baos.toByteArray();
            ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
            buffer.putInt(data.length);
            buffer.put(data);
            buffer.flip();
            
            commandWriter.write(buffer);
        } catch (IOException e) {
            System.err.println("Ошибка отправки команды: " + e.getMessage());
        }
    }
    
    private AICommand readCommand() {
        if (commandReader == null) return null;
        
        try {
            ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
            int bytesRead = commandReader.read(lengthBuffer);
            
            if (bytesRead < 4) return null;
            
            lengthBuffer.flip();
            int dataLength = lengthBuffer.getInt();
            
            ByteBuffer dataBuffer = ByteBuffer.allocate(dataLength);
            bytesRead = 0;
            while (bytesRead < dataLength) {
                int read = commandReader.read(dataBuffer);
                if (read == -1) break;
                bytesRead += read;
            }
            
            if (bytesRead < dataLength) return null;
            
            dataBuffer.flip();
            byte[] data = new byte[dataLength];
            dataBuffer.get(data);
            
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais);
            AICommand command = (AICommand) ois.readObject();
            ois.close();
            
            return command;
        } catch (IOException | ClassNotFoundException e) {
            // Ошибки чтения игнорируем, так как канал неблокирующий
            return null;
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
            // Проверяем команды из канала
            AICommand command = readCommand();
            if (command != null) {
                processCommand(command);
            }
            
            synchronized (pauseLock) {
                while (isPaused.get() && isRunning.get()) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            
            if (isRunning.get()) {
                processMovement();
            }
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
    
    private void processCommand(AICommand command) {
        switch (command.getType()) {
            case PAUSE:
                // Команда уже обработана через isPaused
                break;
            case RESUME:
                synchronized (pauseLock) {
                    pauseLock.notify();
                }
                break;
            case SET_PRIORITY:
                if (aiThread != null) {
                    aiThread.setPriority(command.getPriority());
                }
                break;
            case STOP:
                // Команда уже обработана через isRunning
                break;
        }
    }
    
    protected abstract void processMovement();
}