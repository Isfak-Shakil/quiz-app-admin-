package com.example.quizadmin;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
private EditText emailEt,passwordEt;
private Button login;
private FirebaseAuth firebaseAuth;
private Dialog  loadingDialog;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        emailEt=findViewById(R.id.emailEt);
        passwordEt=findViewById(R.id.passwordEt);
        login=findViewById(R.id.button);

        loadingDialog=new Dialog(MainActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        Objects.requireNonNull(loadingDialog.getWindow()).setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        firebaseAuth=FirebaseAuth.getInstance();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailEt.getText().toString().isEmpty()){
                    emailEt.setError("Enter email id");
                }else {
                    emailEt.setError(null);
                }
                if (passwordEt.getText().toString().isEmpty()){
                    passwordEt.setError("Enter Password");
                }else {
                    passwordEt.setError(null);
                }
                firebaseLogin();
            }
        });
        if (firebaseAuth.getCurrentUser()!=null){
            startActivity(new Intent(MainActivity.this,CategoryActivity.class));
            finish();
        }
    }

    private void firebaseLogin() {
        loadingDialog.show();
firebaseAuth.signInWithEmailAndPassword(emailEt.getText().toString(),passwordEt.getText().toString())
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()){
                startActivity(new Intent(MainActivity.this,CategoryActivity.class));
                finish();
            }else {

            }
            loadingDialog.dismiss();
            }
        });
    }
}