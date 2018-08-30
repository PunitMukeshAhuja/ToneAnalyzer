package com.example.punit.toneanalyzer;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.Tone;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneOptions;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneScore;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));
                    final String analyzeSpeech=result.get(0);


                    ToneOptions options = new ToneOptions.Builder()
                            .addTone(Tone.EMOTION)
                            .html(false).build();
                    final ToneAnalyzer toneAnalyzer =
                            new ToneAnalyzer("2017-07-01");
                    JSONObject credentials=null;
                    try {
                        try {
                            credentials = new JSONObject(IOUtils.toString(
                                    getResources().openRawResource(R.raw.credentials), "UTF-8"
                            )); // Convert the file into a JSON object
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

// Extract the two values
                    String username = null;
                    try {
                        username = credentials.getString("username");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String password = null;
                    try {
                        password = credentials.getString("password");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    toneAnalyzer.setUsernameAndPassword(username, password);
                    toneAnalyzer.getTone(analyzeSpeech, options).enqueue(
                            new ServiceCallback<ToneAnalysis>() {
                                @Override
                                public void onResponse(ToneAnalysis response) {
                                    // More code here
                                    List<ToneScore> scores = response.getDocumentTone()
                                            .getTones()
                                            .get(0)
                                            .getTones();
                                    String detectedTones = "";
                                    boolean toneGot=false;
                                    for(ToneScore score:scores) {
                                        if(score.getScore() > 0.5f) {
                                            detectedTones += score.getName() + " ";
                                            toneGot=true;
                                        }
                                    }
                                    if(toneGot==false){
                                        detectedTones="NEUTRAL";
                                    }

                                    final String toastMessage =
                                            "The following emotions were detected:\n\n"
                                                    + detectedTones.toUpperCase();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getBaseContext(),
                                                    toastMessage, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(getBaseContext(),
                                            "Error fetching response", Toast.LENGTH_LONG).show();
                                }
                            });

                }
                break;
            }

        }
    }



    public void analyzeText(View v) throws IOException, JSONException {
        EditText userInput = (EditText)findViewById(R.id.user_input);

        final String textToAnalyze = userInput.getText().toString();
        ToneOptions options = new ToneOptions.Builder()
                .addTone(Tone.EMOTION)
                .html(false).build();
        final ToneAnalyzer toneAnalyzer =
                new ToneAnalyzer("2017-07-01");
        JSONObject credentials=null;
        try {
            credentials = new JSONObject(IOUtils.toString(
                    getResources().openRawResource(R.raw.credentials), "UTF-8"
            )); // Convert the file into a JSON object
        } catch (JSONException e) {
            e.printStackTrace();
        }

// Extract the two values
        String username = credentials.getString("username");
        String password = credentials.getString("password");
        toneAnalyzer.setUsernameAndPassword(username, password);
        toneAnalyzer.getTone(textToAnalyze, options).enqueue(
                new ServiceCallback<ToneAnalysis>() {
                    @Override
                    public void onResponse(ToneAnalysis response) {
                        // More code here
                        List<ToneScore> scores = response.getDocumentTone()
                                .getTones()
                                .get(0)
                                .getTones();
                        String detectedTones = "";
                        boolean toneGot=false;
                        for(ToneScore score:scores) {
                            if(score.getScore() > 0.5f) {
                                detectedTones += score.getName() + " ";
                                toneGot=true;
                            }
                        }
                        if(toneGot==false){
                            detectedTones="NEUTRAL";
                        }

                        final String toastMessage =
                                "The following emotions were detected:\n\n"
                                        + detectedTones.toUpperCase();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getBaseContext(),
                                        toastMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getBaseContext(),
                                "Error fetching response", Toast.LENGTH_LONG).show();
                    }
                });

    }
}
