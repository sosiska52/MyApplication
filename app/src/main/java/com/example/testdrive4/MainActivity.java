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

import android.content.Intent;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {
    //Поля
    EditText editTextSurname,editTextURL;
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private AutoCompleteTextView autoCompleteTextViewStop, autoCompleteTextViewNextStop;
    private SharedPreferences sharedDataPause;
    private SharedPreferences.Editor editorDataPause;
    private ArrayAdapter<String> adapterStop,adapterNextStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedDataPause = getSharedPreferences("DataPause", Context.MODE_PRIVATE);
        editorDataPause = sharedDataPause.edit();
        //Поля

        editTextSurname=findViewById(R.id.editTextSurname);
        editTextURL=findViewById(R.id.editTextURL);
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

        editTextSurname.setText(sharedPreferences.getString("surname", ""));
        editTextURL.setText(sharedPreferences.getString("URL", ""));
        //Методы
        findViewById(R.id.buttonSaveRegistration).setOnClickListener(view -> saveInfo());
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

//        adapterPath = new ArrayAdapter<String>(DataActivity.this,
//                android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.bus_options));
//        autoCompleteTextViewPathNumber.setAdapter(adapterPath);

        adapterNextStop = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.stop_options));
        autoCompleteTextViewNextStop = (AutoCompleteTextView)
                findViewById(R.id.autoCompleteTextViewNextStop);
        autoCompleteTextViewNextStop.setAdapter(adapterNextStop);

        requestSignIn();
    }
    @Override
    protected void onPause() {
        super.onPause();
        editorDataPause.putString("stop", autoCompleteTextViewStop.getText().toString());
        editorDataPause.putString("nextStop", autoCompleteTextViewNextStop.getText().toString());
        editorDataPause.apply();
    }
    @Override
    protected void onResume() {
        super.onResume();
        String stop = sharedDataPause.getString("stop", "");
        String nextStop = sharedDataPause.getString("nextStop", "");


        if(!stop.isEmpty()){
            autoCompleteTextViewStop.setText(stop);
        }
        if(!nextStop.isEmpty()){
            autoCompleteTextViewNextStop.setText(nextStop);
        }

    }
    public void saveInfo(){
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("surname", editTextSurname.getText().toString());
        editor.putString("URL", editTextURL.getText().toString());
        editor.apply();
        Toast.makeText(MainActivity.this,"Данные успешно сохранены",Toast.LENGTH_LONG).show();
    }
    public void startDataActivity(){
        Intent intent = new Intent(this, DataActivity.class);
        startActivity(intent);
    }
    public void startHistoryActivity(){
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
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
    }

    private void requestSignIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    this, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());
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
        startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

}