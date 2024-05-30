package com.example.testdrive4;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class DataActivity extends AppCompatActivity implements LocationListener {

    private long timeStamp;
    private TextView editTextPassengersIn,editTextPassengersOut, textViewProgress ;
    private double latitude, longitude, altitude;
    private Spinner spinnerTransport,spinnerTransportFullness;
    private ImageView imageView;
    private ArrayAdapter<CharSequence> adapterTransportFullness,adapterTransport;
    private ArrayAdapter<String> adapterPath;
    private SeekBar seekBarStopFullness;
    private AutoCompleteTextView autoCompleteTextViewPathNumber;
    TextView textFixation,textViewChooseTransport,textViewChooseTransportFullness;
    private SharedPreferences sharedStops;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        sharedStops = getSharedPreferences("Stops",MODE_PRIVATE);
        SharedPreferences sharedDataPause = getSharedPreferences("DataPause", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorDataPause = sharedDataPause.edit();
        //Поля
        textFixation = findViewById(R.id.textFixation);
        textFixation.setText(sharedStops.getString("stop","Остановка"));
        textViewChooseTransport = findViewById(R.id.textViewChooseTransport);
        textViewChooseTransportFullness = findViewById(R.id.textViewChooseTransportFullness);
        editTextPassengersIn = findViewById(R.id.editTextPassengersIn);
        editTextPassengersOut = findViewById(R.id.editTextPassengersOut);
        imageView = findViewById(R.id.imageViewPhoto);
        spinnerTransport = findViewById(R.id.spinIdTransport);
        spinnerTransportFullness = findViewById(R.id.spinIdTransportFullness);
        autoCompleteTextViewPathNumber = findViewById(R.id.autoCompleteTextViewPathNumber);
        seekBarStopFullness = findViewById(R.id.seekBarStopFullness);
        textViewProgress = findViewById(R.id.textViewStopFullnessNum);
        //Методы
        findViewById(R.id.imageButtonRegistrationINAddData).setOnClickListener(view -> startMainActivity());
        findViewById(R.id.imageButtonHistoryINAddData).setOnClickListener(view -> startHistoryActivity());
        findViewById(R.id.buttonSaveData).setOnClickListener(view -> createCSVFile());
        findViewById(R.id.buttonClearData).setOnClickListener(view -> clearFields());
        imageView.setOnClickListener(view -> makePhoto());

        //заполненость остановки
        seekBarStopFullness.setMax(45);
        seekBarStopFullness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Обновление текстового поля при изменении положения ползунка
                textViewProgress.setText("Человек: " + progress);
                editorDataPause.putInt("StopFullness",progress);
                editorDataPause.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Вызывается при начале перемещения ползунка
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Вызывается при завершении перемещения ползунка
            }
        });

        //заполненость траспорта
        adapterTransportFullness = ArrayAdapter.createFromResource(DataActivity.this,
                R.array.transport_fullness, android.R.layout.simple_spinner_item);
        adapterTransportFullness.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTransportFullness.setAdapter(adapterTransportFullness);
        spinnerTransportFullness.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerTransport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                // Обработка выбора элемента
                Resources res = getResources();
                if (selectedItem.equals(res.getStringArray(R.array.transport_options)[1]) ||
                        selectedItem.equals(res.getStringArray(R.array.transport_options)[2]) ||
                        selectedItem.equals(res.getStringArray(R.array.transport_options)[3])) {
                    adapterPath = new ArrayAdapter<>(DataActivity.this,
                            android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.bus_options));
                } else if (selectedItem.equals(res.getStringArray(R.array.transport_options)[4])) {
                    adapterPath = new ArrayAdapter<>(DataActivity.this,
                            android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.trolleybus_options));
                } else if (selectedItem.equals(res.getStringArray(R.array.transport_options)[5])) {
                    adapterPath = new ArrayAdapter<>(DataActivity.this,
                            android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.minibus_options));
                } else {
                    adapterPath = new ArrayAdapter<>(DataActivity.this,
                            android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.error_optins));
                }
                //adapterPath.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                autoCompleteTextViewPathNumber.setAdapter(adapterPath);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Обработка, если ни один элемент не выбран
            }
        });

        adapterPath = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.error_optins));
        autoCompleteTextViewPathNumber = findViewById(R.id.autoCompleteTextViewPathNumber);
        autoCompleteTextViewPathNumber.setAdapter(adapterPath);
        autoCompleteTextViewPathNumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(autoCompleteTextViewPathNumber.getWindowToken(), 0);
            }
        });
        adapterTransport = ArrayAdapter.createFromResource(this,
                R.array.transport_options, android.R.layout.simple_spinner_item);
        adapterTransport.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTransport.setAdapter(adapterTransport);

        // Геолокация
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            return;
        }

//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000*10, 10, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                1000*10, 10, this);
    }
    //Геолокация
    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        altitude = location.getAltitude();
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getSharedPreferences("DataPause", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("stopFullness", textViewProgress.getText().toString());
        editor.putString("spinnerPath", autoCompleteTextViewPathNumber.getText().toString());
        editor.putString("spinnerTransport", spinnerTransport.getSelectedItem().toString());
        editor.putString("spinnerTransportFullness", spinnerTransportFullness.getSelectedItem().toString());
        editor.putString("editTextPassengersOut", editTextPassengersOut.getText().toString());
        editor.putString("editTextPassengersIn", editTextPassengersIn.getText().toString());
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("DataPause", Context.MODE_PRIVATE);
        SharedPreferences sharedPhoto = getSharedPreferences("Photo", Context.MODE_PRIVATE);

        // Получаем значения из SharedPreferences
        String passengersOut = sharedPreferences.getString("editTextPassengersOut", "");
        String passengersIn = sharedPreferences.getString("editTextPassengersIn", "");
        String selectedStopFullness = sharedPreferences.getString("stopFullness", "Человек: 0");
        String selectedPath = sharedPreferences.getString("spinnerPath", "");
        String selectedTransport = sharedPreferences.getString("spinnerTransport", "");
        String selectedTransportFullness = sharedPreferences.getString("spinnerTransportFullness", "");
        String imagePath = sharedPhoto.getString("Photo","");

        //
        editTextPassengersOut.setText(passengersOut);
        editTextPassengersIn.setText(passengersIn);
        textViewProgress.setText(selectedStopFullness);
        seekBarStopFullness.setProgress(Integer.parseInt(selectedStopFullness.split(" ")[1]));
        int stopFullnessIndex = adapterTransportFullness.getPosition(selectedTransportFullness);
        if (stopFullnessIndex != -1) {
                spinnerTransportFullness.setSelection(stopFullnessIndex);
        }

        if(!selectedTransport.equals("")) {
            int transportIndex = adapterTransport.getPosition(selectedTransport);
            if (transportIndex != -1) {
                spinnerTransport.setSelection(transportIndex);
                spinnerTransport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedItem = (String) parent.getItemAtPosition(position);
                        // Обработка выбора элемента
                        Resources res = getResources();
                        if (selectedItem.equals(res.getStringArray(R.array.transport_options)[1]) ||
                                selectedItem.equals(res.getStringArray(R.array.transport_options)[2]) ||
                                selectedItem.equals(res.getStringArray(R.array.transport_options)[3])) {
                            adapterPath = new ArrayAdapter<>(DataActivity.this,
                                    android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.bus_options));
                        } else if (selectedItem.equals(res.getStringArray(R.array.transport_options)[4])) {
                            adapterPath = new ArrayAdapter<>(DataActivity.this,
                                    android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.trolleybus_options));
                        } else if (selectedItem.equals(res.getStringArray(R.array.transport_options)[5])) {
                            adapterPath = new ArrayAdapter<>(DataActivity.this,
                                    android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.minibus_options));
                        } else {
                            adapterPath = new ArrayAdapter<>(DataActivity.this,
                                    android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.error_optins));
                        }
                        autoCompleteTextViewPathNumber.setAdapter(adapterPath);
                        autoCompleteTextViewPathNumber.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                String enteredText = s.toString();
                                boolean isTextInList = adapterPath.getPosition(enteredText) > -1;

                                if (isTextInList) {
                                    // Если введенный текст есть в списке, устанавливаем цвет текста по умолчанию
                                    autoCompleteTextViewPathNumber.setTextColor(getResources().getColor(R.color.purple));

                                } else {
                                    // Если введенного текста нет в списке, устанавливаем красный цвет текста
                                    autoCompleteTextViewPathNumber.setTextColor(getResources().getColor(R.color.red));
                                }

                            }
                        });

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Обработка, если ни один элемент не выбран
                    }
                });
            }
        }
        autoCompleteTextViewPathNumber.setText(selectedPath);

        if (!imagePath.isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(image);
            File pictureFile = getOutputMediaFile();
            if (pictureFile != null) {
                try (FileOutputStream fos = new FileOutputStream(pictureFile)) {
                    image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        SharedPreferences sharedPreferencesID = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        timeStamp = System.currentTimeMillis();
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                sharedPreferencesID.getString("surname","")+"_"+timeStamp + ".jpg");

        SharedPreferences sharedPhoto = getSharedPreferences("Photo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPhoto.edit();
        editor.putString("Photo", mediaFile.getAbsolutePath());
        editor.apply();
        return mediaFile;
    }
    public void startHistoryActivity(){
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
    public void startMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    public void makePhoto(){
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, 1);
        } else {
            //Request camera permission if we don't have it.
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        }
    }
    private void clearFields(){
        SharedPreferences DataPause = getSharedPreferences("DataPause", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorPause = DataPause.edit();
        editorPause.clear();
        editorPause.apply();
        SharedPreferences sharedPhoto = getSharedPreferences("Photo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorPhoto = sharedPhoto.edit();
        editorPhoto.clear();
        editorPhoto.apply();

        autoCompleteTextViewPathNumber.setText("");
        spinnerTransport.setSelection(0);
        autoCompleteTextViewPathNumber.setText("");
        spinnerTransportFullness.setSelection(0);
        editTextPassengersIn.setText("");
        editTextPassengersOut.setText("");
        imageView.setImageResource(android.R.drawable.ic_menu_gallery);
    }
    private boolean checkFields(){
        Resources res = getResources();
        boolean cheak = true;
        // название отсановки, количество чел на отсановки, номер маршрута
        if(textFixation.getText().toString().equals("")) {
            textFixation.setTextColor(getResources().getColor(R.color.red));
            cheak= false;
        }else {
            textFixation.setTextColor(getResources().getColor(R.color.purple));
        }
        if(textViewProgress.getText().equals("Человек: 0")) {
            textViewProgress.setTextColor(getResources().getColor(R.color.red));
            cheak= false;
        }else {
            textViewProgress.setTextColor(getResources().getColor(R.color.purple));
        }
        if(autoCompleteTextViewPathNumber.getText().toString().equals("")) {
            autoCompleteTextViewPathNumber.setHintTextColor(getResources().getColor(R.color.red));
            cheak= false;
        }else {
            autoCompleteTextViewPathNumber.setHintTextColor(getResources().getColor(R.color.purple));
        }
        if(spinnerTransport.getSelectedItem().toString().equals("")) {
            textViewChooseTransport.setTextColor(getResources().getColor(R.color.red));
            cheak= false;
        }else {
            textViewChooseTransport.setTextColor(getResources().getColor(R.color.purple));
        }
        if(spinnerTransportFullness.getSelectedItem().toString().equals(res.getStringArray(R.array.transport_fullness)[0])) {
            textViewChooseTransportFullness.setTextColor(getResources().getColor(R.color.red));
            cheak= false;
        }else {
            textViewChooseTransportFullness.setTextColor(getResources().getColor(R.color.purple));
        }
        if(editTextPassengersOut.getText().toString().equals("")) {
            editTextPassengersOut.setHintTextColor(getResources().getColor(R.color.red));
            cheak= false;
        }else {
            editTextPassengersOut.setHintTextColor(getResources().getColor(R.color.purple));
        }
        if(editTextPassengersIn.getText().toString().equals("")) {
            editTextPassengersIn.setHintTextColor(getResources().getColor(R.color.red));
            cheak= false;
        }else {
            editTextPassengersIn.setHintTextColor(getResources().getColor(R.color.purple));
        }
        return cheak;
    }
    private void createCSVFile() {
        if(checkFields()) {
            try {
                if(timeStamp==0)
                    timeStamp = System.currentTimeMillis();
                SharedPreferences sharedPreferences = getSharedPreferences("Saves", Context.MODE_PRIVATE);
                SharedPreferences sharedPreferencesID = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                Map<String, ?> allEntries = sharedPreferences.getAll();
                int sizeKeys = allEntries.size();
                editor.putString(String.valueOf(sizeKeys), sharedPreferencesID.getString("surname", "") + "_" + timeStamp);
                editor.apply();

                java.io.File csvFileDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS), "MyAppFolder");
                if (!csvFileDir.exists()) {
                    if (!csvFileDir.mkdirs()) {
                        Toast.makeText(DataActivity.this, "Не удалось создать", Toast.LENGTH_SHORT).show();
                    }
                }
                java.io.File csvFile = new java.io.File(csvFileDir.getPath() + java.io.File.separator +
                        sharedPreferencesID.getString("surname", "") + "_" + timeStamp + ".csv");
                if (!csvFile.exists()) {
                    csvFile.createNewFile();
                    FileWriter writer = new FileWriter(csvFile);
                    writer.append("Время" + "," + "Индетификатор" + "," + "Название остановки" + "," +"Название следующей остановки "+","+ "GPS-широта" + "," + "GPS-долгота" + "," +
                            "Высота места над уровнем моря" + "," + "Число пассажиров на остановке" + "," + "Номер маршрута транспортного средства" +
                            "," + "Тип транспорта" + "," + "Степень заполненности транспортного средства" + "," + "Число вошедших пассажиров" + "," + "Число вышедших пассажиров" + "\n");
                    writer.append(String.valueOf(timeStamp)).append(",").append(sharedPreferencesID.getString("surname", "")).append(",").
                            append(sharedStops.getString("stop","")).append(",").append(sharedStops.getString("nextStop","")).
                            append(",").append(String.valueOf(latitude)).append(",").append(String.valueOf(longitude)).
                            append(",").append(String.valueOf(altitude)).append(",").append(textViewProgress.getText().toString().split(" ")[1]).
                            append(",").append(autoCompleteTextViewPathNumber.getText().toString()).append(",").append(spinnerTransport.getSelectedItem().toString()).append(",").
                            append(spinnerTransportFullness.getSelectedItem().toString()).append(",").append(editTextPassengersOut.getText().toString()).
                            append(",").append(editTextPassengersIn.getText().toString());
                    writer.close();
                    SharedPreferences sharedHistoryInfo = getSharedPreferences("HistoryInfo",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor2 = sharedHistoryInfo.edit();
                    int saved = sharedHistoryInfo.getInt("Saved",0)+1;
                    editor2.putInt("Saved",saved);
                    editor2.apply();
                    Toast.makeText(DataActivity.this, "Фиксация сохранена", Toast.LENGTH_SHORT).show();
                    clearFields();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(DataActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
        }
    }
}