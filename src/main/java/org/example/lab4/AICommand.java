package org.example.lab4;

import java.io.Serializable;

public class AICommand implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum CommandType {
        PAUSE, RESUME, SET_PRIORITY, STOP
    }
    
    private final CommandType type;
    private final int priority;
    
    public AICommand(CommandType type) {
        this.type = type;
        this.priority = Thread.NORM_PRIORITY;
    }
    
    public AICommand(CommandType type, int priority) {
        this.type = type;
        this.priority = priority;
    }
    
    public CommandType getType() {
        return type;
    }
    
    public int getPriority() {
        return priority;
    }
}