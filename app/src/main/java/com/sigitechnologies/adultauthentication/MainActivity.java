package com.sigitechnologies.adultauthentication;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import dmax.dialog.SpotsDialog;
import edmt.dev.edmtdevcognitivevision.Contract.AnalysisResult;
import edmt.dev.edmtdevcognitivevision.Rest.VisionServiceException;
import edmt.dev.edmtdevcognitivevision.VisionServiceClient;
import edmt.dev.edmtdevcognitivevision.VisionServiceRestClient;

public class MainActivity extends AppCompatActivity {


    private final String API_KEY = "Paste your API_KEY here";
    private final String API_LINK = "API_LINK here";


    VisionServiceClient visionServiceClient = new VisionServiceRestClient(API_KEY, API_LINK);

    ByteArrayInputStream inputStream;

    private Button btnGoodImage, btnRacyImage, btnAdultImage;
    private Bitmap bitmap;
    private ImageView imageView;
    private Button btnPrcss;
    private RadioButton isGoodBtn, isRacyBtn, isAdultBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.img);

        btnPrcss = (Button) findViewById(R.id.prcss_btn);
        btnGoodImage = (Button) findViewById(R.id.good_image);
        btnRacyImage = (Button) findViewById(R.id.racy_image);
        btnAdultImage = (Button) findViewById(R.id.adult_image);

        isGoodBtn = (RadioButton) findViewById(R.id.is_good);
        isRacyBtn = (RadioButton) findViewById(R.id.is_racy);
        isAdultBtn = (RadioButton) findViewById(R.id.is_adult);


        btnAdultImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.adult);
                imageView.setImageBitmap(bitmap);
                //convert bitmap to stream
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            }
        });

        btnGoodImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.good);
                imageView.setImageBitmap(bitmap);
                //convert bitmap to stream
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            }
        });

        btnRacyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.racy);
                imageView.setImageBitmap(bitmap);
                //convert bitmap to stream
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            }
        });

        btnPrcss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set and define Asynctask
                AsyncTask<InputStream, String, String> adultDetecttask = new AsyncTask<InputStream, String, String>() {

                    AlertDialog dialog;

                    @Override
                    protected void onPreExecute() {
                        dialog = new SpotsDialog.Builder()
                                .setContext(MainActivity.this)
                                .setCancelable(false)
                                .build();
                        dialog.show();

                    }

                    @Override
                    protected void onProgressUpdate(String... values) {

                        dialog.setMessage(values[0]);
                    }

                    @Override
                    protected String doInBackground(InputStream... inputStreams) {

                        try {
                            publishProgress("Detecting....");
                            String[] features = {"Adult"};
                            String[] details = {};
//                            AnalysisResult result = visionServiceClient.analyzeImage(inputStreams[0], features, details);
                            AnalysisResult result = visionServiceClient.analyzeImage(inputStream, features, details);
                            String jsonResult = new Gson().toJson(result);
                            return jsonResult;

                        } catch (IOException e) {
                            return "[IO]" + e.getMessage();
                        } catch (VisionServiceException e) {
                            System.out.println("[API ERROR]" + e.getMessage());
                            return "[ERROR]" + e.getMessage();
                        }
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        if (dialog != null && dialog.isShowing())
                            dialog.dismiss();
                        if (s.contains("[IO]") || s.contains("[ERROR]"))
                            Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
                        else {
                            AnalysisResult result = new Gson().fromJson(s, AnalysisResult.class);
                            if (result.adult.isAdultContent) {
                                isGoodBtn.setChecked(true);
                                isRacyBtn.setChecked(false);
                                isAdultBtn.setChecked(false);
                            } else if (result.adult.isRacyContent) {
                                isGoodBtn.setChecked(false);
                                isRacyBtn.setChecked(true);
                                isAdultBtn.setChecked(false);
                            }else{
                                isGoodBtn.setChecked(false);
                                isRacyBtn.setChecked(false);
                                isAdultBtn.setChecked(true);
                            }
                        }
                    }
                };

                // run task
                adultDetecttask.execute();
            }
        });
    }
}
