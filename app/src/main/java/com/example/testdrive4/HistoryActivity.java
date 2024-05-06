package com.example.testdrive4;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;
    private Drive mDriveService;
    private final ArrayList<ArrayList<String>> historyItems = new ArrayList<>();
    private HistoryAdapter adapter;
    private int countErrors =0, countUploaded = 0, countAll = 0;
    private ListView listView;
    private boolean cheakUpload = true;
    private SharedPreferences sharedHistoryInfo;
    private TextView textViewDataSavedNumber,textViewDataSentNumber;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        sharedHistoryInfo = getSharedPreferences("HistoryInfo", Context.MODE_PRIVATE);

        //Поля
        textViewDataSavedNumber = findViewById(R.id.textViewDataSavedNumber);
        textViewDataSentNumber = findViewById(R.id.textViewDataSentNumber);
        textViewDataSavedNumber.setText(Integer.toString(sharedHistoryInfo.getInt("Saved",0)));
        textViewDataSentNumber.setText(Integer.toString(sharedHistoryInfo.getInt("Sent",0)));

        //Методы
        findViewById(R.id.imageButtonAddDataINHistory).setOnClickListener(view -> startDataActivity());
        findViewById(R.id.imageButtonRegistrationINHistory).setOnClickListener(view -> startMainActivity());
        findViewById(R.id.buttonHistorySendData).setOnClickListener(view -> uploadImages());
        findViewById(R.id.buttonClearAllHistory).setOnClickListener(view -> showConfirmationDialog());

        //Список с записями
        makeListView();
        adapter = new HistoryAdapter(this, historyItems);
        listView = findViewById(R.id.listViewHistory);
        listView.setAdapter(adapter);

        requestSignIn();
    }
    private void deleteFileNames(String fileNames){
        SharedPreferences sharedPreferences = getSharedPreferences("Saves", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorSaves = sharedPreferences.edit();
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if(entry.getValue().toString().equals(fileNames)){
                editorSaves.remove(entry.getKey());
            }
        }
        editorSaves.apply();
        java.io.File mediaStorageDir = new java.io.File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        java.io.File imageFile = new java.io.File(mediaStorageDir.getPath() + java.io.File.separator + fileNames + ".jpg");

        java.io.File csvFileDir = new java.io.File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "MyAppFolder");
        java.io.File csvFile = new java.io.File(csvFileDir.getPath() + java.io.File.separator + fileNames + ".csv");

        if (imageFile.exists()) {
            imageFile.delete();
        }

        if (csvFile.exists()) {
            csvFile.delete();
        }
    }
    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Подтверждение");
        builder.setMessage("Вы уверены, что хотите удилить данные?");

        builder.setPositiveButton("Да", (dialog, which) -> {
            clearShared();
            dialog.dismiss();
        });
        builder.setNegativeButton("Нет", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void makeListView(){
        SharedPreferences sharedPreferences = getSharedPreferences("Saves", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Object value = entry.getValue();
            takeInfoFromCSV(value.toString());
        }
    }
    private void takeInfoFromCSV(String fileNames) {
        java.io.File csvFileDir = new java.io.File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "MyAppFolder");
        java.io.File csvFile = new java.io.File(csvFileDir.getPath() + java.io.File.separator + fileNames + ".csv");
        BufferedReader reader = null;
        String line;
        try {
            reader = new BufferedReader(new FileReader(csvFile));
            //while ((line = reader.readLine()) != null) {
            line = reader.readLine();
            line = reader.readLine();
                String[] data = line.split(",");
                if (data.length == 13) {
                    ArrayList<String> historyItem1 = new ArrayList<>();
                    historyItem1.add(data[8]);
                    historyItem1.add(data[7]);
                    Date date = new Date(Long.parseLong(data[0]));
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String formattedTime = sdf.format(date);
                    historyItem1.add(formattedTime); // Время
                    historyItem1.add(data[9]); // Заполненость
                    historyItem1.add(data[10]); // Сколько вошло
                    historyItem1.add(data[11]); // Сколько вышло
                    historyItem1.add(data[2]); // Название остановки
                    historyItem1.add(data[3]);
                    historyItem1.add(data[6]); // Заполненость остановки
                    historyItems.add(historyItem1); // Добавляем в список
                }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void startMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    public void startDataActivity(){
        Intent intent = new Intent(this, DataActivity.class);
        startActivity(intent);
    }
    private void clearShared(){
        SharedPreferences sharedPreferences = getSharedPreferences("Saves", Context.MODE_PRIVATE);
        java.io.File mediaStorageDir = new java.io.File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        java.io.File csvFileDir = new java.io.File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "MyAppFolder");

        if (mediaStorageDir.exists() && mediaStorageDir.isDirectory()) {
            java.io.File[] imageFiles = mediaStorageDir.listFiles();
            if (imageFiles != null) {
                for (java.io.File imageFile : imageFiles) {
                    imageFile.delete();
                }
            }
        }

        if (csvFileDir.exists() && csvFileDir.isDirectory()) {
            java.io.File[] csvFiles = csvFileDir.listFiles();
            if (csvFiles != null) {
                for (java.io.File csvFile : csvFiles) {
                    csvFile.delete();
                }
            }
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        adapter.clear();
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }
    private void requestSignIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    this, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());
            HttpTransport httpTransport;
            httpTransport = new com.google.api.client.http.javanet.NetHttpTransport();
            mDriveService =
                    new Drive.Builder(
                            httpTransport ,
                            new GsonFactory(),
                            credential)
                            .setApplicationName("Drive API Migration")
                            .build();
        } else {
            Toast.makeText(HistoryActivity.this, "Войдите в аккаунт ", Toast.LENGTH_SHORT).show();
        }

    }
    private void uploadImages(){
        if(cheakUpload) {
            cheakUpload=false;
            SharedPreferences sharedPreferences = getSharedPreferences("Saves", Context.MODE_PRIVATE);
            Map<String, ?> allEntries = sharedPreferences.getAll();
            countAll = allEntries.size();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                Object value = entry.getValue();
                uploadImageToDrive(value.toString());
            }
        }else {
            Toast.makeText(HistoryActivity.this, "Отправка уже идёт", Toast.LENGTH_SHORT).show();
        }
    }
    private void uploadImageToDrive(String fileNames) {
        if (mDriveService!=null) {
            java.io.File mediaStorageDir = new java.io.File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "MyCameraApp");
            java.io.File imageFile = new java.io.File(mediaStorageDir.getPath() + java.io.File.separator + fileNames+".jpg");

            java.io.File csvFileDir = new java.io.File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "MyAppFolder");
            java.io.File csvFile = new java.io.File(csvFileDir.getPath() + java.io.File.separator + fileNames+".csv");

            ByteArrayContent imageContent = null;
            if(imageFile.exists())
            {
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                imageContent = new ByteArrayContent("image/jpeg", outputStream.toByteArray());
            }

            ByteArrayContent csvContent = null;
            try {
                FileInputStream fileInputStream = new FileInputStream(csvFile);
                byte[] fileBytes = new byte[(int) csvFile.length()];
                fileInputStream.read(fileBytes);
                fileInputStream.close();
                csvContent = new ByteArrayContent("text/csv", fileBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }


            if (csvContent != null && imageContent !=null) {
                new HistoryActivity.UploadImageTask(fileNames).execute(imageContent, csvContent);
            } else if(csvContent != null) {
                new HistoryActivity.UploadImageTask(fileNames).execute(csvContent);
            } else {
                Toast.makeText(HistoryActivity.this, "Failed to load image or CSV file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(HistoryActivity.this, "Sign in to your account", Toast.LENGTH_SHORT).show();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class UploadImageTask extends AsyncTask<ByteArrayContent, Void, String> {
        private final String fileNames;
        private final SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        //private String folderId = "1OEon_6lH94B6TupvVeicEwf8cc1vEIfL";
        private final String folderId = sharedPreferences.getString("URL", "");

        public UploadImageTask(String fileNames) {
            this.fileNames = fileNames;
        }
        @Override
        protected String doInBackground(ByteArrayContent... params) {
            try {
                File fileMetadata = new File();
                fileMetadata.setName(fileNames+".jpg");
                if (folderId != null && !folderId.isEmpty()) {
                    List<String> parents = Collections.singletonList(folderId);
                    fileMetadata.setParents(parents);
                }
                if(params.length==1) {
                    fileMetadata.setName(fileNames+".csv");
                    File csvFile = mDriveService.files().create(fileMetadata, params[0])
                            .setFields("id")
                            .execute();
                    return csvFile.getId();

                }else {
                    File imageFile = mDriveService.files().create(fileMetadata, params[0])
                            .setFields("id")
                            .execute();
                    fileMetadata.setName(fileNames + ".csv");
                    File csvFile = mDriveService.files().create(fileMetadata, params[1])
                            .setFields("id")
                            .execute();
                    return imageFile.getId() + "," + csvFile.getId();
                }
            } catch (UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String fileId) {
            super.onPostExecute(fileId);
            if (fileId != null) {
                deleteFileNames(fileNames);
                SharedPreferences.Editor editor = sharedHistoryInfo.edit();
                int sent = sharedHistoryInfo.getInt("Sent",0)+1;
                editor.putInt("Sent",sent);
                editor.apply();

                listView.setAdapter(adapter);
                countUploaded++;
            } else {
                countErrors++;
            }
            if(countUploaded+countErrors==countAll)
            {
                Toast.makeText(HistoryActivity.this, "Успешно отправленно файлов : " + countUploaded +"\n Не отправленно файлов : "+countErrors, Toast.LENGTH_SHORT).show();
                countUploaded=0;
                countErrors=0;
                adapter.clear();
                adapter.notifyDataSetChanged();
                makeListView();
                adapter.notifyDataSetChanged();
                listView.setAdapter(adapter);
                cheakUpload=true;
            }
        }
    }

}