package com.example.testdrive4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.DriveScopes;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Collections;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedStops = getSharedPreferences("Stops", Context.MODE_PRIVATE);
        editorStops = sharedStops.edit();
        //Поля
        textViewGmail = findViewById(R.id.textViewGmail);
        editTextSurname=findViewById(R.id.editTextSurname);
        editTextURL=findViewById(R.id.editTextURL);
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

        editTextSurname.setText(sharedPreferences.getString("surname", ""));
        editTextURL.setText(sharedPreferences.getString("URL", ""));
        //Методы
        findViewById(R.id.buttonSaveRegistration).setOnClickListener(view -> saveInfo(true));
        findViewById(R.id.buttonLogIn).setOnClickListener(view -> requestSignInRegister());
        findViewById(R.id.imageButtonHistoryINRegistration).setOnClickListener(view -> startHistoryActivity());
        findViewById(R.id.imageButtonAddDataINRegistration).setOnClickListener(view -> startDataActivity());
        //findViewById(R.id.deleteButton).setOnClickListener(view -> clearShared());
        /////////

        adapterStop = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.stop_options));
        autoCompleteTextViewStop = (AutoCompleteTextView)
                findViewById(R.id.autoCompleteTextViewStop);
        autoCompleteTextViewStop.setAdapter(adapterStop);

        adapterNextStop = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.stop_options));
        autoCompleteTextViewNextStop = (AutoCompleteTextView)
                findViewById(R.id.autoCompleteTextViewNextStop);
        autoCompleteTextViewNextStop.setAdapter(adapterNextStop);
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
                } else {
                    autoCompleteTextViewNextStop.setTextColor(getResources().getColor(R.color.red));
                }
            }
        });
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
                } else {
                    autoCompleteTextViewStop.setTextColor(getResources().getColor(R.color.red));
                }
            }
        });
        requestSignIn();
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

        // Предварительно выйти из текущего аккаунта, чтобы пользователь мог выбрать новый
        client.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Открываем окно выбора аккаунта
                startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
            }
        });
    }

}