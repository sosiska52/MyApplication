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
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import android.content.Intent;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {
    //Поля
    EditText editTextName,editTextSurname,editTextPatronymic,editTextURL;
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private Drive mDriveService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Поля
        editTextName=findViewById(R.id.editTextName);
        editTextSurname=findViewById(R.id.editTextSurname);
        editTextPatronymic=findViewById(R.id.editTextPatronymic);
        editTextURL=findViewById(R.id.editTextURL);
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        editTextName.setText(sharedPreferences.getString("name", ""));
        editTextSurname.setText(sharedPreferences.getString("surname", ""));
        editTextPatronymic.setText(sharedPreferences.getString("patronymic", ""));
        editTextURL.setText(sharedPreferences.getString("URL", ""));
        //Методы
        findViewById(R.id.buttonSaveRegistration).setOnClickListener(view -> saveInfo());
        findViewById(R.id.buttonLogIn).setOnClickListener(view -> requestSignInRegister());
        findViewById(R.id.imageButtonHistoryINRegistration).setOnClickListener(view -> startHistoryActivity());
        findViewById(R.id.imageButtonAddDataINRegistration).setOnClickListener(view -> startDataActivity());
        //findViewById(R.id.deleteButton).setOnClickListener(view -> clearShared());

        requestSignIn();
    }

    public void saveInfo(){
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", editTextName.getText().toString());
        editor.putString("surname", editTextSurname.getText().toString());
        editor.putString("patronymic", editTextPatronymic.getText().toString());
        editor.putString("URL", editTextURL.getText().toString());
        editor.apply();
        Toast.makeText(MainActivity.this,"Данные успешно сохранены",Toast.LENGTH_LONG);
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
                        HttpTransport httpTransport = null;
                        httpTransport = new com.google.api.client.http.javanet.NetHttpTransport();
                        mDriveService =
                                new Drive.Builder(
                                        httpTransport ,
                                        new GsonFactory(),
                                        credential)
                                        .setApplicationName("Drive API Migration")
                                        .build();
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
            Toast.makeText(MainActivity.this, "Войдите в аккаунт ", Toast.LENGTH_SHORT).show();
        }
    }
    private void requestSignInRegister() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE))

                        .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);
        startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

}