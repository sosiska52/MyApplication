package com.example.testdrive4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity {
    private static final String TAG = "Historyctivity";
    private static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private Drive mDriveService;
    private ArrayList<ArrayList<String>> historyItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        makeListView();
        HistoryAdapter adapter = new HistoryAdapter(this, historyItems);
        ListView listView = findViewById(R.id.listViewHistory);
        listView.setAdapter(adapter);


        // Устанавливаем слушателя событий на ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                // Получаем текст элемента, на который кликнули
                String selectedItem = (String) parent.getItemAtPosition(position);

                // Выводим сообщение с текстом выбранного элемента
                Toast.makeText(HistoryActivity.this, selectedItem, Toast.LENGTH_SHORT).show();
            }
        });




        findViewById(R.id.imageButtonAddDataINHistory).setOnClickListener(view -> startDataActivity());
        findViewById(R.id.imageButtonRegistrationINHistory).setOnClickListener(view -> startMainActivity());
        findViewById(R.id.buttonHistorySendData).setOnClickListener(view -> uploadImages());
        findViewById(R.id.deleteButton).setOnClickListener(view -> clearShared());
        requestSignIn();
    }
    private void makeListView(){
        SharedPreferences sharedPreferences = getSharedPreferences("Saves", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        int sizeKeys = allEntries.size();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Object value = entry.getValue();
            Toast.makeText(HistoryActivity.this, entry.getKey().toString(), Toast.LENGTH_SHORT).show();
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
                } else {
                    // Логика обработки неправильной строки CSV (например, пропустить или обработать ошибку)
                }
            //}
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    private void requestSignIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // Если учетные данные есть, используем их для аутентификации
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    this, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());
            HttpTransport httpTransport = null;
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
        int sizeKeys = allEntries.size();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Object value = entry.getValue();
            uploadImageToDrive(value.toString());
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

            // Выполняем загрузку изображения и CSV файла
            if (imageContent != null && csvContent != null) {
                new HistoryActivity.UploadImageTask(fileNames).execute(imageContent, csvContent);
            } else {
                Toast.makeText(HistoryActivity.this, "Failed to load image or CSV file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(HistoryActivity.this, "Sign in to your account", Toast.LENGTH_SHORT).show();
        }
    }
    private class UploadImageTask extends AsyncTask<ByteArrayContent, Void, String> {
        private String fileNames;
        private SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        //private String folderId = "1OEon_6lH94B6TupvVeicEwf8cc1vEIfL";
        private String folderId = sharedPreferences.getString("URL", "");

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
                Toast.makeText(HistoryActivity.this, "Files uploaded successfully", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(HistoryActivity.this, "Failed to upload files", Toast.LENGTH_SHORT).show();
            }
        }
    }
}