package com.example.funun.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.funun.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

/**This Class - MainActivity - The first screen in app.
 * gets email and password from the user.
 * creates a connection (if the user exist) and start the funun app home view.  */
public class MainActivity extends AppCompatActivity {

    //init view elements
    Button btn_login, btn_sign;
    ImageView iv_facebook;
    EditText et_password, et_mail;
    TextView tv_recover;

    //init firebase
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).hide();
        //init view elements
        btn_login = findViewById(R.id.btn_login);
        btn_sign = findViewById(R.id.btn_sign);
        iv_facebook = findViewById(R.id.iv_facebook);
        et_mail = findViewById(R.id.et_mail);
        et_password = findViewById(R.id.et_password);
        tv_recover = findViewById(R.id.tv_recover) ;

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = et_mail.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                //valid email and password
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //set error and focus on the email.
                    et_mail.setError("Invalid email");
                    et_mail.setFocusable(true);
                } else if (password.length() < 6) {
                    //set error and focus on the password.
                    et_password.setError("Password length should be at least 6 characters");
                    et_password.setFocusable(true);
                } else {
                    loginUser(email, password);
                }
            }
        });
        btn_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                finish();
            }
        });

        iv_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "In The Next Funun Generation :) ", Toast.LENGTH_SHORT).show();
            }
        });

        tv_recover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoveryPassDialog();
            }
        });
    }


    /**This Method showRecoverPassDialog - Creates dialog */
    private void showRecoveryPassDialog() {
        //set alert popup
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        //set dialog layout
        LinearLayout linearLayout = new LinearLayout(this);
        //view to set in dialog
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEt.setEms(16);
        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);
        //set buttons actions(Recover,Cancel)
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //get email
                String emailEt2 = emailEt.getText().toString();
                beginRecover(emailEt2);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void beginRecover(String emailEt2) {

        progressDialog.setMessage("Sending Email...");
        progressDialog.show();
        mAuth.sendPasswordResetEmail(emailEt2).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(MainActivity.this, "Failed..", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**This Method = loginUser - creates the connection to firebase authentication.
     * if the user exist, the connection is establish
     * finish and move to home activity. */
    public void loginUser(String email, String password) {

        progressDialog.setMessage("login user..");
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            progressDialog.dismiss();
                            user = mAuth.getCurrentUser();
                            Toast.makeText(MainActivity.this, "logged in \n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this,HomeActivity.class));
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //error. dismiss progress dialog and get and show error message
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**This Method - onStart - Check connection to firebase at start. if the user hadn't logged out, go to profile in home activity.*/
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user2 = mAuth.getCurrentUser();
        if (user2 != null) {
            // User is signed in
            Intent i = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(i);
        }
    }


    @Override
    public void onBackPressed() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Exit Window")
                .setMessage("Are You Sure You Want To Leave?")
                .setIcon(R.drawable.ic_exit_back)
                .setPositiveButton("Stay",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(MainActivity.this, "Lets Plan Some FUNUN Events", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton("\"Leave The App", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        MainActivity.super.onBackPressed();
                    }
                }).create().show();
    }
}