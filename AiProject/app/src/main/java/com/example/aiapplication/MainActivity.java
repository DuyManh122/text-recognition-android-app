package com.example.aiapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.yalantis.ucrop.UCrop;


import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_PICK = 100;
    private static final int REQUEST_IMAGE_CROP = 200;
    private Uri imageUri;

    private TextView resultTextView;
    private ImageView inputImageView;
    Button buttonImage, buttonCropImage, buttonProcessText, ButtonRewrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        resultTextView = findViewById(R.id.resultTextView);
        inputImageView = findViewById(R.id.inputImageView);
        buttonImage  = findViewById(R.id.ButtonImage);
        buttonCropImage = findViewById(R.id.ButtonCropImage);
        buttonProcessText = findViewById(R.id.ButtonProcessText);
        ButtonRewrite = findViewById(R.id.ButtonRewrite);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        buttonImage.setOnClickListener(view -> openGallery());
        buttonCropImage.setOnClickListener(view -> {
            if (imageUri != null) {
                cropImage(imageUri);
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        });

        buttonProcessText.setOnClickListener(view -> {
            if (imageUri != null) {
                processImage(imageUri);
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        });
        ButtonRewrite.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            intent.putExtra("detected_text", resultTextView.getText().toString());
            startActivity(intent);

        });
    }

    private void processImage(Uri uri) {
        try {
            InputImage image = InputImage.fromFilePath(this, uri);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String extractedText = visionText.getText();
                        resultTextView.setText(extractedText);
                    })
                    .addOnFailureListener(e -> {
                        resultTextView.setText("Failed to recognize text");
                        e.printStackTrace();
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void cropImage(Uri uri) {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped_image.jpg"));

        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(100);
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setToolbarTitle("Crop Image");
        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(true); // allow free resizing

        int outputWidth = 1024;
        int outputHeight = 1024;

        UCrop.of(uri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(outputWidth, outputHeight)
                .withOptions(options)
                .start(this);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                imageUri = data.getData();
                inputImageView.setImageURI(imageUri);
            } else if (requestCode == UCrop.REQUEST_CROP && data != null) {
                final Uri resultUri = UCrop.getOutput(data);
                if (resultUri != null) {
                    imageUri = resultUri;
                    inputImageView.setImageURI(imageUri);
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR && data != null) {
            final Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "Crop error: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}