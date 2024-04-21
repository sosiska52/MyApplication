package com.example.testdrive4;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
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
    private int position =0;
    private Drive mDriveService;
    private final ArrayList<ArrayList<String>> historyItems = new ArrayList<>();
    private HistoryAdapter adapter;
    private int countErrors =0;

    private SharedPreferences sharedHistoryInfo;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        sharedHistoryInfo = getSharedPreferences("HistoryInfo", Context.MODE_PRIVATE);

        TextView textViewDataSavedNumber = findViewById(R.id.textViewDataSavedNumber);
        TextView textViewDataSentNumber = findViewById(R.id.textViewDataSentNumber);

        textViewDataSavedNumber.setText(Integer.toString(sharedHistoryInfo.getInt("Saved",0)));
        textViewDataSentNumber.setText(Integer.toString(sharedHistoryInfo.getInt("Sent",0)));

        makeListView();
        adapter = new HistoryAdapter(this, historyItems);
        ListView listView = findViewById(R.id.listViewHistory);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            // Получаем текст элемента, на который кликнули
            //String selectedItem = (String) parent.getItemAtPosition(position);

            // Выводим сообщение с текстом выбранного элемента
            //Toast.makeText(HistoryActivity.this, selectedItem, Toast.LENGTH_SHORT).show();
        });




        findViewById(R.id.imageButtonAddDataINHistory).setOnClickListener(view -> startDataActivity());
        findViewById(R.id.imageButtonRegistrationINHistory).setOnClickListener(view -> startMainActivity());
        findViewById(R.id.buttonHistorySendData).setOnClickListener(view -> uploadImages());
        findViewById(R.id.buttonClearAllHistory).setOnClickListener(view -> clearShared());
        requestSignIn();
        SharedPreferences sharedPreferences = getSharedPreferences("Saves", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            //Toast.makeText(HistoryActivity.this, entry.getKey() +" "+entry.getValue().toString(), Toast.LENGTH_SHORT).show();
        }
    }
    private void makeListView(){
        SharedPreferences sharedPreferences = getSharedPreferences("Saves", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Object value = entry.getValue();
            //Toast.makeText(HistoryActivity.this, entry.getKey().toString(), Toast.LENGTH_SHORT).show();
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
                String[] data = line.split(","); // Предположим, что данные в CSV разделены запятыми
                if (data.length == 12) { // Проверяем, что строка содержит все поля
                    ArrayList<String> historyItem1 = new ArrayList<>();
                    historyItem1.add(data[8]); // Тип транспорта
                    historyItem1.add(data[7]); // Номер транспорта
                    Date date = new Date(Long.parseLong(data[0])); // Создаем объект Date из миллисекунд
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String formattedTime = sdf.format(date);
                    historyItem1.add(formattedTime); // Время
                    historyItem1.add(data[9]); // Заполненость
                    historyItem1.add(data[10]); // Сколько вошло
                    historyItem1.add(data[11]); // Сколько вышло
                    historyItem1.add(data[2]); // Название остановки
                    historyItem1.add(data[6]); // Заполненость остановки
                    historyItems.add(historyItem1); // Добавляем в список
                }  // Логика обработки неправильной строки CSV (например, пропустить или обработать ошибку)

            //}
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
        // Очистить папку с изображениями
        if (mediaStorageDir.exists() && mediaStorageDir.isDirectory()) {
            java.io.File[] imageFiles = mediaStorageDir.listFiles();
            if (imageFiles != null) {
                for (java.io.File imageFile : imageFiles) {
                    imageFile.delete();
                }
            }
        }

// Очистить папку с CSV файлами
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
    }
    private void requestSignIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // Если учетные данные есть, используем их для аутентификации
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
        SharedPreferences sharedPreferences = getSharedPreferences("Saves", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Object value = entry.getValue();
            position=Integer.parseInt(entry.getKey());
            uploadImageToDrive(value.toString());
        }
        if(countErrors==0) {
            Toast.makeText(HistoryActivity.this, "Files uploaded successfully", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(HistoryActivity.this, countErrors+" Files faild", Toast.LENGTH_SHORT).show();
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

            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            ByteArrayContent imageContent = new ByteArrayContent("image/jpeg", outputStream.toByteArray());

            // Загружаем CSV файл
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

            // Выполняем загрузку изображения и CSV файлаe
            if (csvContent != null) {
                new HistoryActivity.UploadImageTask(fileNames).execute(imageContent, csvContent);
                SharedPreferences sharedPreferences = getSharedPreferences("Saves", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(String.valueOf(position));
                editor.apply();
                imageFile.delete();
                csvFile.delete();
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
                File imageFile = mDriveService.files().create(fileMetadata, params[0])
                        .setFields("id")
                        .execute();

                fileMetadata.setName(fileNames+".csv");
                File csvFile = mDriveService.files().create(fileMetadata, params[1])
                        .setFields("id")
                        .execute();
                return imageFile.getId() + "," + csvFile.getId();
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
                //Toast.makeText(HistoryActivity.this, "Files uploaded successfully", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = sharedHistoryInfo.edit();
                int sent = sharedHistoryInfo.getInt("Sent",0)+1;
                editor.putInt("Sent",sent);
                editor.apply();
            } else {
                //Toast.makeText(HistoryActivity.this, "Failed to upload files", Toast.LENGTH_SHORT).show();
                countErrors++;
            }
        }
    }
}