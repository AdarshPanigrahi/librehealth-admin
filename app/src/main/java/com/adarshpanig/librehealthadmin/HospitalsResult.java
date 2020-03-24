package com.adarshpanig.librehealthadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.net.Uri;
import android.os.Bundle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HospitalsResult extends AppCompatActivity {

    TextView txtProgressPercent;
    ProgressBar progressBar;
    Button btnDownloadFile;

    FirebaseStorage storage;
    FirebaseDatabase database;
    String x;

    DownloadCsvFileTask downloadCsvFileTask;
    String filename="atlanticare";
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101);
        txtProgressPercent = findViewById(R.id.txtProgressPercent);
        progressBar = findViewById(R.id.progressBar);
        btnDownloadFile = findViewById(R.id.button);

        storage=FirebaseStorage.getInstance();
        database=FirebaseDatabase.getInstance();
        btnDownloadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadCsvFile();
            }
        });



    }

    private void downloadCsvFile() {

        RetrofitInterface downloadService = createService(RetrofitInterface.class, "https://www.atlanticare.org/patients-and-visitors/for-patients/billing-and-insurance/hospital-charge-list/");
        Call<ResponseBody> call = downloadService.downloadFileByUrl("/assets/images/services/price-transparency/2019finalpricetransparencyforjan1.csv");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.isSuccessful()) {

                    Toast.makeText(getApplicationContext(), "Downloading CDM...", Toast.LENGTH_SHORT).show();

                    downloadCsvFileTask = new DownloadCsvFileTask();
                    downloadCsvFileTask.execute(response.body());

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public <T> T createService(Class<T> serviceClass, String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(new OkHttpClient.Builder().build())
                .build();
        return retrofit.create(serviceClass);
    }

    private class DownloadCsvFileTask extends AsyncTask<ResponseBody, Pair<Integer, Long>, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(ResponseBody... urls) {
            saveToPhone(urls[0], "atlanticare.csv");
            return null;
        }

        protected void onProgressUpdate(Pair<Integer, Long>... progress) {


            if (progress[0].first == 100)
                Toast.makeText(getApplicationContext(), "CDM downloaded successfully", Toast.LENGTH_SHORT).show();


            if (progress[0].second > 0) {
                int currentProgress = (int) ((double) progress[0].first / (double) progress[0].second * 100);
                progressBar.setProgress(currentProgress);

                txtProgressPercent.setText("Updating " + currentProgress + "%");

            }

            if (progress[0].first == -1) {
                Toast.makeText(getApplicationContext(), "CDM Download failed", Toast.LENGTH_SHORT).show();
            }

        }

        public void doProgress(Pair<Integer, Long> progressDetails) {
            publishProgress(progressDetails);
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    private void saveToPhone(ResponseBody body, String filename) {
        try {

            File destinationFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(destinationFile);
                byte data[] = new byte[4096];
                int count;
                int progress = 0;
                long fileSize = body.contentLength();


                while ((count = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, count);
                    progress += count;
                    Pair<Integer, Long> pairs = new Pair<>(progress, fileSize);
                    downloadCsvFileTask.doProgress(pairs);
                }

                outputStream.flush();

                Pair<Integer, Long> pairs = new Pair<>(100, 100L);
                downloadCsvFileTask.doProgress(pairs);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Pair<Integer, Long> pairs = new Pair<>(-1, Long.valueOf(-1));
                downloadCsvFileTask.doProgress(pairs);
                return;
            } finally {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
                uploadToDatabase();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }


    }


    public void uploadToDatabase(){

        final Uri file = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename+".csv"));
        x= file.toString();
        StorageReference storageReference = storage.getReference();

        storageReference.child("HospitalsCdm/").child(filename).putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

            @Override
            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                String url =taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                final DatabaseReference reference = database.getReference();

                reference.child("HospitalsCdm/").child(filename).setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(HospitalsResult.this, "CDM successfully uploaded", Toast.LENGTH_SHORT).show();
                            txtProgressPercent.setText("Updated ");
                            progressBar.setProgress(100);
                        }
                        else
                            Toast.makeText(HospitalsResult.this,"CDM not Successfully uploaded",Toast.LENGTH_SHORT).show();

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(HospitalsResult.this,"CDM failed to upload",Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

            }
        });

    }


    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(HospitalsResult.this, permission) != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(HospitalsResult.this, permission)) {
                ActivityCompat.requestPermissions(HospitalsResult.this, new String[]{permission}, requestCode);

            } else {
                ActivityCompat.requestPermissions(HospitalsResult.this, new String[]{permission}, requestCode);
            }
        } else if (ContextCompat.checkSelfPermission(HospitalsResult.this, permission) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(getApplicationContext(), "Permission was denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {

            if (requestCode == 101)
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
