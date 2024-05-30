package com.example.testdrive4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.DriveScopes;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    //Поля
    EditText editTextSurname,editTextURL;
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private AutoCompleteTextView autoCompleteTextViewStop, autoCompleteTextViewNextStop;
    private SharedPreferences sharedStops;
    private SharedPreferences.Editor editorStops;
    private ArrayAdapter<String> adapterStop,adapterNextStop;
    private TextView textViewGmail;
    private List<Stop> stopList;
    private class Stop{
        public String name;
        public String moveto;
        public double x;
        public double y;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedStops = getSharedPreferences("Stops", Context.MODE_PRIVATE);
        editorStops = sharedStops.edit();
        SharedPreferences sharedUserInfo = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        //Поля
        textViewGmail = findViewById(R.id.textViewGmail);
        editTextSurname=findViewById(R.id.editTextSurname);
        editTextURL=findViewById(R.id.editTextURL);
        editTextSurname.setText(sharedUserInfo.getString("surname", ""));
        editTextURL.setText(sharedUserInfo.getString("URL", ""));
        //Методы
        findViewById(R.id.buttonSaveRegistration).setOnClickListener(view -> saveInfo(true));
        findViewById(R.id.buttonLogIn).setOnClickListener(view -> requestSignInRegister());
        findViewById(R.id.imageButtonHistoryINRegistration).setOnClickListener(view -> startHistoryActivity());
        findViewById(R.id.imageButtonAddDataINRegistration).setOnClickListener(view -> startDataActivity());

        //autoCompleteTextView
        String jsonString = loadJSONFromAsset();
        stopList = parseJSONWithGson(jsonString);

        List<String> stopNames = new ArrayList<>();
        for (int i =1;i<stopList.size();i++)
        {
            if(!stopList.get(i).name.equals(stopList.get(i-1).name)){
                stopNames.add(stopList.get(i).name);
            }
        }

        adapterStop = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_dropdown_item_1line, stopNames );
        autoCompleteTextViewStop = findViewById(R.id.autoCompleteTextViewStop);
        autoCompleteTextViewStop.setAdapter(adapterStop);

        adapterNextStop = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_dropdown_item_1line, stopNames);
        autoCompleteTextViewNextStop = findViewById(R.id.autoCompleteTextViewNextStop);
        autoCompleteTextViewNextStop.setAdapter(adapterNextStop);
        autoCompleteTextViewStop.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String enteredText = s.toString();
                boolean isTextInList = adapterStop.getPosition(enteredText) > -1;

                if (isTextInList) {
                    autoCompleteTextViewStop.setTextColor(getResources().getColor(R.color.purple));
                    makeNewAdapter();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(autoCompleteTextViewStop.getWindowToken(), 0);
                } else {
                    autoCompleteTextViewStop.setTextColor(getResources().getColor(R.color.red));
                }
            }
        });
        autoCompleteTextViewNextStop.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String enteredText = s.toString();
                boolean isTextInList = adapterNextStop.getPosition(enteredText) > -1;

                if (isTextInList) {
                    autoCompleteTextViewNextStop.setTextColor(getResources().getColor(R.color.purple));
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(autoCompleteTextViewNextStop.getWindowToken(), 0);
                } else {
                    autoCompleteTextViewNextStop.setTextColor(getResources().getColor(R.color.red));
                }
            }
        });

        requestSignIn();
    }
    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("astops_with_next.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
    private List<Stop> parseJSONWithGson(String jsonString) {
        Gson gson = new Gson();
        Type stopListType = new TypeToken<ArrayList<Stop>>() {}.getType();
        return gson.fromJson(jsonString, stopListType);
    }
    private void makeNewAdapter(){
        String str = autoCompleteTextViewStop.getText().toString();
        List<String> nextStopNames = new ArrayList<>();
        for (int i =1;i<stopList.size();i++)
        {
            if(stopList.get(i).name.equals(str)){
                nextStopNames.add(stopList.get(i).moveto);
            }
        }
        adapterNextStop = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_dropdown_item_1line, nextStopNames);
        autoCompleteTextViewNextStop.setAdapter(adapterNextStop);
    }
    @Override
    protected void onPause() {
        super.onPause();
        editorStops.putString("stop", autoCompleteTextViewStop.getText().toString());
        editorStops.putString("nextStop", autoCompleteTextViewNextStop.getText().toString());
        editorStops.apply();
    }
    @Override
    protected void onResume() {
        super.onResume();
        String stop = sharedStops.getString("stop", "");
        String nextStop = sharedStops.getString("nextStop", "");

        if(!stop.isEmpty()){
            autoCompleteTextViewStop.setText(stop);
        }
        if(!nextStop.isEmpty()){
            autoCompleteTextViewNextStop.setText(nextStop);
        }

    }
    private boolean cheakFields(){
        boolean cheak = true;
        if(editTextSurname.getText().toString().isEmpty())
        {
            cheak=false;
            editTextSurname.setHintTextColor(getResources().getColor(R.color.red));
        }else {
            editTextSurname.setHintTextColor(getResources().getColor(R.color.purple));
        }
        if(editTextURL.getText().toString().isEmpty())
        {
            cheak=false;
            editTextURL.setHintTextColor(getResources().getColor(R.color.red));
        }else {
            editTextURL.setHintTextColor(getResources().getColor(R.color.purple));
        }
        if(autoCompleteTextViewStop.getText().toString().isEmpty())
        {
            cheak=false;
            autoCompleteTextViewStop.setHintTextColor(getResources().getColor(R.color.red));
        }else {
            autoCompleteTextViewStop.setHintTextColor(getResources().getColor(R.color.purple));
        }
        if(autoCompleteTextViewNextStop.getText().toString().isEmpty())
        {
            cheak=false;
            autoCompleteTextViewNextStop.setHintTextColor(getResources().getColor(R.color.red));
        }else {
            autoCompleteTextViewNextStop.setHintTextColor(getResources().getColor(R.color.purple));
        }
        return cheak;
    }
    public void saveInfo(boolean cheak){

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("surname", editTextSurname.getText().toString());
        editor.putString("URL", editTextURL.getText().toString());
        editor.apply();
        if(cheak) {
            Toast.makeText(MainActivity.this,"Данные успешно сохранены",Toast.LENGTH_LONG).show();
        }
    }
    public void startDataActivity(){
        if(cheakFields()) {
            saveInfo(false);
            Intent intent = new Intent(this, DataActivity.class);
            startActivity(intent);
        }
    }
    public void startHistoryActivity(){
        if(cheakFields()) {
            saveInfo(false);
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN && resultCode == RESULT_OK) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener(googleAccount -> {
                        GoogleAccountCredential credential =
                                GoogleAccountCredential.usingOAuth2(
                                        this, Collections.singleton(DriveScopes.DRIVE_FILE));
                        credential.setSelectedAccount(googleAccount.getAccount());
                    })
                    .addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));

        }
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            textViewGmail.setText(account.getEmail());
            textViewGmail.setTextColor(getResources().getColor(R.color.blue));
            Toast.makeText(MainActivity.this, "Вы успешно вошли в аккаунт", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestSignIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    this, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());
            textViewGmail.setText(account.getEmail());
            textViewGmail.setTextColor(getResources().getColor(R.color.blue));
        } else {
            Toast.makeText(MainActivity.this, "Войдите в аккаунт ", Toast.LENGTH_SHORT).show();
        }
    }
    private void requestSignInRegister() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE))
                        .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        client.signOut().addOnCompleteListener(this, task -> {
            startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
        });
    }

}