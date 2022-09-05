package com.example.android_machinelearningkit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class ScannerActivity extends AppCompatActivity {

    private ImageView imgCapture;
    private TextView tvResult;
    private Button btnSnap, btnDetect;
    private Bitmap bmImage;
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        imgCapture = findViewById(R.id.imgCaptureImage);
        tvResult = findViewById(R.id.tvDetectedText);
        btnSnap = findViewById(R.id.btnTextSnap);
        btnDetect = findViewById(R.id.btnDetectText);

        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetectText();
            }
        });

        btnSnap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckPermission()){
                    CaptureImage();
                }else {
                    RequestPermission();
                }
            }
        });
    }

    private void CaptureImage() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0){
            boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (cameraPermission){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                CaptureImage();
            }else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle bundle = data.getExtras();
            bmImage = (Bitmap) bundle.get("data");
            imgCapture.setImageBitmap(bmImage);
        }
    }

    private void RequestPermission() {
        int PERMISSION_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CODE);
    }

    private boolean CheckPermission() {
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA_SERVICE);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * If the text recognition operation succeeds, a Text object is passed to the success listener.
     * A Text object contains the full text recognized in the image and zero or more TextBlock objects.
     * Each TextBlock represents a rectangular block of text, which contains zero or more Line objects.
     * Each Line object contains zero or more Element objects, which represent words and word-like entities such as dates and numbers.
     * For each TextBlock, Line, and Element object, you can get the text recognized in the region and the bounding coordinates of the region.
     */
    private void DetectText() {
        // Create an InputImage object from either a Bitmap, media.Image, ByteBuffer, byte array, or a file on the device.
        // Then, pass the InputImage object to the TextRecognizer's processImage method
        InputImage image = InputImage.fromBitmap(bmImage, 0);

        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result =
                recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                StringBuilder resultAll = new StringBuilder();
                String result = text.getText();
                for (Text.TextBlock block : text.getTextBlocks()){
                    String blockText = block.getText();
                    Point[] blockCornerPoint = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();
                    Log.d("MLKIT","BlockText: " + blockText);
                    resultAll.append("\n");

                    for (Text.Line line : block.getLines()){
                        String lineText = line.getText();
                        Point[] lineCornerPoint = line.getCornerPoints();
                        Rect lineRect = line.getBoundingBox();
                        Log.d("MLKIT","LineText: " + lineText);

                        for (Text.Element element : line.getElements()){
                            String elementText = element.getText();
//                            result.append(elementText);
                            Log.d("MLKIT", "ElementText: " + elementText);
                            resultAll.append(elementText);
                            resultAll.append(" ");
                        }
                    }
                }
                tvResult.setText(resultAll.toString());
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ScannerActivity.this, "Failed to detect from Image", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}