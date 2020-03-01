package com.example.arduino_4wd_car;

public interface ContextListernerInterface {
    void sendMovePosition(float positionX, float positionY);
    void sendTiltingCommand(String cmd, int data1, int data2);
}
