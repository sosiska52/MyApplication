package com.example.testdrive4;

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.http.ByteArrayContent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import io.opencensus.resource.Resource;

public class DataActivity extends AppCompatActivity implements LocationListener {

    private long timeStamp;
    private TextView textView, editTextPassengersIn,editTextPassengersOut;
    private LocationManager locationManager;
    private double latitude, longitude, altitude;
    private Spinner spinnerStop,spinnerTransport, spinnerPath,spinnerStopFullness,spinnerTransportFullness;
    private ImageView imageView;
    private ArrayAdapter<CharSequence> adapterPath,adapterStop,adapterStopFullness,
            adapterTransportFullness,adapterTransport;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        Intent intent = getIntent();
        timeStamp = intent.getLongExtra("time",0L);
        //
        editTextPassengersIn = findViewById(R.id.editTextPassengersIn);
        editTextPassengersOut = findViewById(R.id.editTextPassengersOut);
        //
        imageView = findViewById(R.id.imageViewPhoto);
        //Спинеры
        spinnerStop = findViewById(R.id.spinIdStop);
        spinnerTransport = findViewById(R.id.spinIdTransport);
        spinnerPath = findViewById(R.id.spinIdPathNumber);
        spinnerStopFullness = findViewById(R.id.spinIdStopFullness);
        spinnerTransportFullness = findViewById(R.id.spinIdTransportFullness);
        //
        adapterStop = ArrayAdapter.createFromResource(this,
                R.array.stop_options, android.R.layout.simple_spinner_item);
        adapterStop.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStop.setAdapter(adapterStop);
        spinnerStop.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                // Обработка выбора элемента
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Обработка, если ни один элемент не выбран
            }
        });
        //
        adapterStopFullness = ArrayAdapter.createFromResource(this,
                R.array.stop_fullness, android.R.layout.simple_spinner_item);
        adapterStop.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStopFullness.setAdapter(adapterStopFullness);
        spinnerStopFullness.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                // Обработка выбора элемента
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Обработка, если ни один элемент не выбран
            }
        });

        //
        adapterTransportFullness = ArrayAdapter.createFromResource(this,
                R.array.transport_fullness, android.R.layout.simple_spinner_item);
        adapterStop.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTransportFullness.setAdapter(adapterTransportFullness);
        spinnerTransportFullness.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                // Обработка выбора элемента
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Обработка, если ни один элемент не выбран
            }
        });
        //
        adapterPath = ArrayAdapter.createFromResource(DataActivity.this,
                R.array.error_optins, android.R.layout.simple_spinner_item);
        adapterPath.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPath.setAdapter(adapterPath);

        adapterTransport = ArrayAdapter.createFromResource(this,
                R.array.transport_options, android.R.layout.simple_spinner_item);
        adapterTransport.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTransport.setAdapter(adapterTransport);
        //можно убрать но я не хочу
//        spinnerTransport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String selectedItem = (String) parent.getItemAtPosition(position);
//                // Обработка выбора элемента
//                Resources res = getResources();
//                if (selectedItem.equals(res.getStringArray(R.array.transport_options)[1]) ||
//                        selectedItem.equals(res.getStringArray(R.array.transport_options)[2]) ||
//                        selectedItem.equals(res.getStringArray(R.array.transport_options)[3])) {
//                    adapterPath = ArrayAdapter.createFromResource(DataActivity.this,
//                            R.array.bus_options, android.R.layout.simple_spinner_item);
//                } else if (selectedItem.equals(res.getStringArray(R.array.transport_options)[4])) {
//                    adapterPath = ArrayAdapter.createFromResource(DataActivity.this,
//                            R.array.trolleybus_options, android.R.layout.simple_spinner_item);
//                } else if (selectedItem.equals(res.getStringArray(R.array.transport_options)[1])) {
//                    adapterPath = ArrayAdapter.createFromResource(DataActivity.this,
//                            R.array.minibus_options, android.R.layout.simple_spinner_item);
//                } else {
//                    adapterPath = ArrayAdapter.createFromResource(DataActivity.this,
//                            R.array.error_optins, android.R.layout.simple_spinner_item);
//                }
//                adapterPath.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                spinnerPath.setAdapter(adapterPath);
//                int pathIndex = adapterPath.getPosition(sharedPreferences.getString("spinnerPath", ""););
//                if (pathIndex != -1) {
//                    spinnerPath.setSelection(pathIndex);
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // Обработка, если ни один элемент не выбран
//            }
//        });


        //Методы
        findViewById(R.id.imageButtonRegistrationINAddData).setOnClickListener(view -> startMainActivity());
        findViewById(R.id.imageButtonTakePhoto).setOnClickListener(view -> startCameraActivity());
        findViewById(R.id.imageButtonHistoryINAddData).setOnClickListener(view -> startHistoryActivity());
        findViewById(R.id.buttonSaveData).setOnClickListener(view -> createCSVFile());
        findViewById(R.id.buttonClearData).setOnClickListener(view -> clearFields());

        ///////
        // Получаем ссылку на LocationManager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Проверяем разрешение на использование геолокации
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // Если разрешение не предоставлено, запрашиваем его
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            return;
        }

        // Запрашиваем обновления местоположения
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0, 0, this);



        textView = findViewById(R.id.textFixation);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getSharedPreferences("DataPause", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("spinnerStop", spinnerStop.getSelectedItem().toString());
        editor.putString("spinnerStopFullness", spinnerStopFullness.getSelectedItem().toString());
        editor.putString("spinnerPath", spinnerPath.getSelectedItem().toString());
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

        // Получаем значения из SharedPreferences
        String passengersOut = sharedPreferences.getString("editTextPassengersOut", "");
        String passengersIn = sharedPreferences.getString("editTextPassengersIn", "");
        String selectedStop = sharedPreferences.getString("spinnerStop", "");
        String selectedStopFullness = sharedPreferences.getString("spinnerStopFullness", "");
        String selectedPath = sharedPreferences.getString("spinnerPath", "");
        String selectedTransport = sharedPreferences.getString("spinnerTransport", "");
        String selectedTransportFullness = sharedPreferences.getString("spinnerTransportFullness", "");

        // Проверяем на пустоту перед установкой значений в Spinner
        if (!passengersOut.isEmpty()) {
            editTextPassengersOut.setText(passengersOut);
        }

        if (!passengersIn.isEmpty()) {
            editTextPassengersIn.setText(passengersIn);
        }

        int stopIndex = adapterStop.getPosition(selectedStop);
        if (stopIndex != -1) {
            spinnerStop.setSelection(stopIndex);
        }

        int stopFullnessIndex = adapterStopFullness.getPosition(selectedStopFullness);
        if (stopFullnessIndex != -1) {
            spinnerStopFullness.setSelection(stopFullnessIndex);
        }
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
                        adapterPath = ArrayAdapter.createFromResource(DataActivity.this,
                                R.array.bus_options, android.R.layout.simple_spinner_item);
                    } else if (selectedItem.equals(res.getStringArray(R.array.transport_options)[4])) {
                        adapterPath = ArrayAdapter.createFromResource(DataActivity.this,
                                R.array.trolleybus_options, android.R.layout.simple_spinner_item);
                    } else if (selectedItem.equals(res.getStringArray(R.array.transport_options)[5])) {
                        adapterPath = ArrayAdapter.createFromResource(DataActivity.this,
                                R.array.minibus_options, android.R.layout.simple_spinner_item);
                    } else {
                        adapterPath = ArrayAdapter.createFromResource(DataActivity.this,
                                R.array.error_optins, android.R.layout.simple_spinner_item);
                    }
                    adapterPath.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerPath.setAdapter(adapterPath);
                    int pathIndex = adapterPath.getPosition(selectedPath);
                    if (pathIndex != -1) {
                        spinnerPath.setSelection(pathIndex);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Обработка, если ни один элемент не выбран
                }
            });
        }

        int transportFullnessIndex = adapterTransportFullness.getPosition(selectedTransportFullness);
        if (transportFullnessIndex != -1) {
            spinnerTransportFullness.setSelection(transportFullnessIndex);
        }


        SharedPreferences sharedPhoto = getSharedPreferences("Photo", Context.MODE_PRIVATE);
        sharedPhoto.getString("Photo","");

        String imagePath = sharedPhoto.getString("Photo","");
        if (imagePath != null && !imagePath.isEmpty()) {
            //Toast.makeText(DataActivity.this, ""+imagePath, Toast.LENGTH_SHORT).show();
            // Загружаем изображение в ImageView
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }

    }






    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            altitude = location.getAltitude();
        }
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show();
    }

    public void startHistoryActivity(){
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
    public void startMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    public void startCameraActivity(){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);

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

        spinnerStop.setSelection(0);
        spinnerTransport.setSelection(0);
        spinnerPath.setSelection(0);
        spinnerTransportFullness.setSelection(0);
        spinnerStopFullness.setSelection(0);

        editTextPassengersIn.setText("");
        editTextPassengersOut.setText("");

        imageView.setImageBitmap(null);
    }
    private boolean checkFields(){
        Resources res = getResources();
        if(spinnerStop.getSelectedItem().toString().equals(res.getStringArray(R.array.stop_options)[0]))
            return false;
        if(spinnerStopFullness.getSelectedItem().toString().equals(res.getStringArray(R.array.stop_fullness)[0]))
            return false;
        if(spinnerPath.getSelectedItem().toString().equals(res.getStringArray(R.array.bus_options)[0]))
            return false;
//        if(spinnerTransport.getSelectedItem().toString().equals(""))
//            return false;
        if(spinnerTransportFullness.getSelectedItem().toString().equals(res.getStringArray(R.array.transport_fullness)[0]))
            return false;
        if(editTextPassengersOut.getText().toString().equals(""))
            return false;
        if(editTextPassengersIn.getText().toString().equals(""))
            return false;
        return true;
    }
    private void createCSVFile() {
        if(checkFields()) {
            try {
                SharedPreferences sharedPreferences = getSharedPreferences("Saves", Context.MODE_PRIVATE);
                SharedPreferences sharedPreferencesID = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                Map<String, ?> allEntries = sharedPreferences.getAll();
                int sizeKeys = allEntries.size();
                //Toast.makeText(DataActivity.this, ""+sizeKeys, Toast.LENGTH_SHORT).show();
                editor.putString("" + (sizeKeys), sharedPreferencesID.getString("surname", "") + " " + timeStamp);
                editor.apply();
                java.io.File csvFileDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS), "MyAppFolder");
                if (!csvFileDir.exists()) {
                    if (!csvFileDir.mkdirs()) {
                        Toast.makeText(DataActivity.this, "Не удалось создать", Toast.LENGTH_SHORT).show();
                    }
                }
                java.io.File csvFile = new java.io.File(csvFileDir.getPath() + java.io.File.separator +
                        sharedPreferencesID.getString("surname", "") + " " + timeStamp + ".csv");
                if (!csvFile.exists()) {
                    csvFile.createNewFile();
                    FileWriter writer = new FileWriter(csvFile);
                    SharedPreferences sharedSavesHistory = getSharedPreferences("SavesHistory", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editorSavesHistore = sharedSavesHistory.edit();

                    writer.append("Время" + "," + "Индетификатор" + "," + "Название остановки" + "," + "GPS-широта" + "," + "GPS-долгота" + "," +
                            "Высота места над уровнем моря" + "," + "Число пассажиров на остановке" + "," + "Номер маршрута транспортного средства" +
                            "," + "Тип транспорта" + "," + "Степень заполненности транспортного средства" + "," + "Число вошедших пассажиров" + "," + "Число вышедших пассажиров" + "\n");
                    writer.append(timeStamp + "," + sharedPreferencesID.getString("surname", "") + "," + spinnerStop.getSelectedItem().toString() +
                            "," + latitude + "," + longitude + "," + altitude +
                            "," + spinnerStopFullness.getSelectedItem().toString() + "," + spinnerPath.getSelectedItem().toString() + ","
                            + spinnerTransport.getSelectedItem().toString() + "," + spinnerTransportFullness.getSelectedItem().toString() +
                            "," + editTextPassengersOut.getText().toString() + "," + editTextPassengersIn.getText().toString());
                    writer.close();
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