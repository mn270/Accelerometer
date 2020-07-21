package com.example.accelerometer;

public class Memory {
    private float [] x = new float[4];
    private float [] y = new float[4];

    public Memory() {
    for(int i = 0; i<4;i++){
        x[i] = 0f;
        y[i] = 0f;
    }}

   public float getX(int i){
        return x[i];
    }
    public float getY(int i){
        return y[i];
    }
    public void set(int i,float input,float output){
        x[i] = input;
        y[i] = output;
    }
}
