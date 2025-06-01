package org.example.lab4;

import javafx.scene.image.Image;

public class PersonRecord extends Record {
    public PersonRecord(double x, double y, Image image, long birthTime, long lifetime, int id) {
        super(x, y, image, birthTime, lifetime, id);
    }

    @Override
    public synchronized void update() {
    }
}