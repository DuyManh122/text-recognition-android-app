package com.example.aiapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class SecondActivity extends AppCompatActivity {

    EditText inputText;
    Button btnRewrite,BtnGetText, BtnBack;
    TextView resultText;

    String apiKey = "AIzaSyAgGxCKG_C0FLyJCb5BXlCO9_oaRLPtz9c";
    private String detectedText = "";
    @SuppressLint("MissingInflatedId")

    public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
            // handle success response
        } else {
            String errorBody = response.body().string();
            runOnUiThread(() -> resultText.setText("API error: " + response.code() + "\n" + errorBody));
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        detectedText = getIntent().getStringExtra("detected_text"); // Get data

        inputText = findViewById(R.id.inputText);
        btnRewrite = findViewById(R.id.btnRewrite);
        BtnBack = findViewById(R.id.BtnBack);
        BtnGetText = findViewById(R.id.BtnGetText);
        resultText = findViewById(R.id.resultText);

        btnRewrite.setOnClickListener(v -> {
            String input = inputText.getText().toString();
            if (!input.isEmpty()) {
                callGemini(input);
            }
        });
        BtnGetText.setOnClickListener( v -> {
            if (detectedText != null && !detectedText.isEmpty()) {
                inputText.setText(detectedText);
            } else {
                inputText.setText("No text received");
            }
        });
        BtnBack.setOnClickListener(v -> {
            finish();
        });
    }

    void callGemini(String originalText) {
        OkHttpClient client = new OkHttpClient();

        String prompt = "Rewrite the following paragraph with different wording but keep the same meaning:\n" + originalText;

        JSONObject json = new JSONObject();
        try {
            JSONObject content = new JSONObject();
            content.put("parts", new JSONArray().put(new JSONObject().put("text", prompt)));
            json.put("contents", new JSONArray().put(content));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash-lite:generateContent?key=" + apiKey)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> resultText.setText("Lỗi mạng hoặc kết nối API"));
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String res = response.body().string();
                        JSONObject resJson = new JSONObject(res);
                        String rewritten = resJson
                                .getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");

                        runOnUiThread(() -> resultText.setText(rewritten.trim()));
                    } catch (Exception e) {
                        runOnUiThread(() -> resultText.setText("Lỗi xử lý dữ liệu"));
                    }
                } else {
                    runOnUiThread(() -> resultText.setText("API lỗi: " + response.code()));
                }
            }
        });
    }
}