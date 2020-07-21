package com.example.accelerometer;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


public class Classifier {


    private final Interpreter.Options options = new Interpreter.Options();
    private final Interpreter mInterpreter;

    private static final String MODEL_FILE = "converted_model2.tflite";


    public Classifier(Activity activity) throws IOException {
        mInterpreter = new Interpreter(loadModelFile(activity), options);
    }
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    public float[][] predictProbabilities(float[] data) {
        float[][] result = new float[1][6];

        float [][][] matrix = new float[1][128][9];
        for (int i = 0; i < 128; i++) {
            for (int j= 0; j<9; j++) {
                matrix[0][i][j] = data[128*j+i];
        }
    }
        mInterpreter.run(matrix, result);
        return result;
    }
}
