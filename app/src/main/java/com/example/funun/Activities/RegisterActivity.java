package com.example.funun.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.funun.Model.User;
import com.example.funun.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**This Class RegisterActivity - Handles a new user registration.
 *  get from user - username, password, and mail.
 * The request connection is transferred to firebase that creates a new user auth and database reference to the user id.
 */

public class RegisterActivity extends AppCompatActivity {

    //view items
    EditText et_username,et_password, et_email;
    Button btn_signUp;
    ProgressDialog progressDialog;
    String username;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;
    private int flagExist;

    //initialize
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        flagExist=0;

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        et_password = findViewById(R.id.et_password1);
        et_username = findViewById(R.id.et_username);
        et_email = findViewById(R.id.et_email);
        btn_signUp = findViewById(R.id.btn_signUp);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("registering user..");

        //initialize the FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();


        //check conditions ( not legit email or password length is shorter then 6 chars)
        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = et_email.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                username = et_username.getText().toString();
                //valid email and password
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //set error and focus on the email.
                    et_email.setError("Invalid email ");
                    et_email.setFocusable(true);
                }
                else if (password.length()<6){
                    //set error and focus on the password.
                    et_password.setError("Password length at least 6 characters");
                    et_password.setFocusable(true);
                }

                else {
                    //if username exists/!exists
                    if(flagExist==0) {
                        registerUser(btn_signUp, username, email, password);
                    }
                }
            }
        });
    }



    /**This Method - registerUser - Creates a new User in firebase authentication and database
     * creates a user only if the username is unique. else sets error.
     * @param btn_signUp - the button sign up on screen. (if he was tapped but the username exist, set error).
     * @param username2  - the username that was typed by the user.
     * @param password  - the  user password.
     * @param email - the user email address.*/
    public void registerUser(final Button btn_signUp,final String username2, final String email, final String password){
        //email and password were valid, show progress dialog and start registering user

        progressDialog.show();

        final DatabaseReference userCheck = FirebaseDatabase.getInstance().getReference("users");
        userCheck.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String key = ds.getKey();
                    if ((snapshot.child(key).child("username").getValue().toString()).equals(username2)) flagExist=1;
                }
                if(flagExist==1){
                    btn_signUp.setError("The username Exist In Funun");
                    et_username.setError("The username Exist In Funun");
                    et_username.setFocusable(true);
                    btn_signUp.setFocusable(true);
                    progressDialog.dismiss();
                    flagExist=0;
                }
                else if(flagExist==0){
                    btn_signUp.setFocusable(false);
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success,dismiss dialog and start register activity
                                        progressDialog.dismiss();
                                        final FirebaseUser user = mAuth.getCurrentUser();
                                        String uemail = user.getEmail();
                                        User user1= new User(username2,uemail," ");
                                        final HashMap<String,Object> hashMap= user1.toMapFirst();
                                        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                                        userRef.setValue(hashMap);

                                        Toast.makeText(RegisterActivity.this, "Registered \n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this,HomeActivity.class));
                                        finish();


                                    } else {
                                        // If sign in fails, display a message to the user.
                                        progressDialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //error. dismiss progress dialog and get and show error message
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

/**This Method sets the back button to the MainActivity*/
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RegisterActivity.this,MainActivity.class));
        finish();
    }

}