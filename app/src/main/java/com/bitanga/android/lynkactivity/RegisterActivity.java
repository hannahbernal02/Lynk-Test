//RegisterActivity
//This class takes user data and creates an account in Firebase
//Last edit made on 11/4/2018 by Jared Bitanga
package com.bitanga.android.lynkactivity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    EditText userLoginName;
    EditText userPassword;
    EditText userEmail;
    Button registerButton;
    private FirebaseAuth mAuth;
    ProgressBar mProgressBar;
    String emailRegex = "^[A-Za-z0-9.]+@\\bcalbaptist.edu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Declare instance of firebase
        mAuth = FirebaseAuth.getInstance();

        userLoginName = (EditText) findViewById(R.id.register_username);
        userPassword = (EditText) findViewById(R.id.register_password);
        userEmail = (EditText) findViewById(R.id.register_mail);
        mProgressBar = (ProgressBar) findViewById((R.id.progress_bar));
        findViewById(R.id.create_button).setOnClickListener(this);

    }

    //If create button is pressed, registerUser() is run and text fields sent to Firebase
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_button:
                try {
                    registerUser();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }

    }

    public boolean searchBadWords(String test) {
        String bad = "(.*shit.*|shit|.*ass.*|ass|.*fuck.*|fuck|" +
                ".*piss.*|piss|.*bitch.*|bitch|.*whore.*|whore|cunt|" +
                ".*cunt.*|.*damn.*|damn)";
        if(test.matches(bad)) {
            System.out.print(test);
            return true;
        }
//        File file = new File("/Users/v3l0z/Desktop/lynk/app/src/main/assets/BadWords.txt");
//        LineIterator line = FileUtils.lineIterator(file, "UTF-8");
//        while(line.hasNext()) {
//            String temp = line.nextLine();
//            if(temp.contains((CharSequence) test));
//                System.out.print(temp);
//                return true;
//        }
        return false;
    }

    //This class takes the edittext fields and stores them into Firebase
    private void registerUser() throws IOException {
        String username = userLoginName.getText().toString().trim();
        String pass = userPassword.getText().toString().trim();
        String email = userEmail.getText().toString().trim();

        //If the email is empty - displays toasts that email field is null
        if (email.isEmpty()) {
            userEmail.setError("Email is required");
            userEmail.requestFocus();
            return;
        }

        //If email field does not have a domain - toast error is displayed
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            userEmail.setError("Email is required");
            userEmail.requestFocus();
            return;
        }

        //If username field empty - display toast error
        if (username.isEmpty()) {
            userLoginName.setError("User name is required");
            userLoginName.requestFocus();
            return;
        }

        //If username field empty - display toast error
        if (searchBadWords(username)) {
            userLoginName.setError("Profanity not allowed");
            userLoginName.requestFocus();
            return;
        }


        //If password field is empty - display toast error
        if (pass.isEmpty()) {
            userPassword.setError("Password is required");
            userPassword.requestFocus();
            return;
        }

        if (searchBadWords(pass)) {
            userLoginName.setError("Profanity not allowed");
            userLoginName.requestFocus();
            return;
        }

        //If password length is less than 6 chars - display toast error
        if (userPassword.length() < 6) {
            userPassword.setError("Must be longer than 6 characters");
            userPassword.requestFocus();
            return;
        }

        //Set progress bar visible
        mProgressBar.setVisibility(View.VISIBLE);

        //Send data to Firebase and create a new account
        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //Displays progress bar
                mProgressBar.setVisibility(View.GONE);
                //If database successfully registers data - display toast
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(),
                            "Signup successful!", Toast.LENGTH_SHORT).show();
                }

                //If user is already in database - display toast
                else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException){
                        Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }


}