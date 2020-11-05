package com.example.funun.Fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.funun.Activities.MainActivity;
import com.example.funun.Model.Helper;
import com.example.funun.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Objects;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

/**This Class ProfileFragment - responsible for user profile settings (picture, username and mail).
 * offers buttons to sign out, change username or change photo.
 * */
public class ProfileFragment extends Fragment {

    public static final int STORAGE_REQUEST_CODE=200;
    public static final int IMAGE_PICK_GALLERY_REQUEST_CODE=300;
    private View view;
    ProgressDialog progressDialog;
    private ImageView iv_profile_img;
    private TextView tv_profile_email;
    private TextView tv_profile_username;
    private FirebaseUser user;
    private DatabaseReference userDB;
    StorageReference storageReference;
    SharedPreferences sharedPref;
    Uri image_uri;
    private Bitmap bitmap;
    private SharedPreferences sharedPref2;
    private String myUsername;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**This Method - onCreate - Called to do initial creation of a fragment.
     * initialize shared pref and firebase objects.
     * ask storage permissions from the device. */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_CALENDAR}, 1);
        }
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("update user profile info");
        user=FirebaseAuth.getInstance().getCurrentUser();
        sharedPref = Objects.requireNonNull(getActivity()).getSharedPreferences("Username", MODE_PRIVATE);
        storageReference = FirebaseStorage.getInstance().getReference();
        userDB= FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());

    }

    /**This Method - onCreateView - creates and returns the view associated with the fragment.
     * @return Return the View for the fragment's UI, or null.
     * @param inflater - The LayoutInflater object that can be used to inflate views in the fragment
     * @param container - the parent view that the fragment's UI should be attached to.
     *@param savedInstanceState - If non-null, this fragment is being re-constructed from a previous saved state as given here.*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }

    /**This Method - onViewCreated - initialize the view elements and attach listeners with values.
     * Called immediately after onCreateView
     * check if your a host or a guest in the specific event
     * attach adapter to this fragment.*/
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        iv_profile_img = view.findViewById(R.id.iv_profile_img);
        tv_profile_username = view.findViewById(R.id.tv_profile_username);
        tv_profile_email = view.findViewById(R.id.tv_profile_email);
        Button btn_profile_change_username = view.findViewById(R.id.btn_profile_change_username);
        Button btn_profile_change_pic = view.findViewById(R.id.btn_profile_change_pic);
        Button btn_profile_signout = view.findViewById(R.id.btn_profile_signout);

        btn_profile_signout.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        btn_profile_change_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        btn_profile_change_username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("changing username....");
                setUsername("username");
            }
        });

        progressDialog.setTitle("Update");
        progressDialog.show();
        userDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue().toString();
                String useremail = dataSnapshot.child("userEmail").getValue().toString();
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("MyUsername",username);
                editor.commit();
                tv_profile_username.setText(username);
                tv_profile_email.setText(useremail);
                try {
                    String userimage = dataSnapshot.child("userImage").getValue().toString();
                    Picasso.get().load(userimage).into(iv_profile_img);
                    progressDialog.dismiss();

                } catch (Exception e) {
                    iv_profile_img.setImageResource(R.drawable.person);
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        progressDialog.dismiss();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void signOut() {

                FirebaseAuth.getInstance().signOut();
                // User is signed in
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getActivity().finishAffinity();
                startActivity(intent);
                getActivity().finish();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_GALLERY_REQUEST_CODE && data != null && data.getData() != null) {
            //image picked form gallery
            image_uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image_uri);
                changeProfileImage(bitmap);
            } catch (Exception e) {
            }

        }
    }

    /**--------- This Method "getNameEmailUpdate" - creates a Dialog according to flag.
     * Update The Database Reference According To The Flag(Username/Email)--------*/
    private void setUsername(final String flagForString) {

        AlertDialog.Builder builderKey= new AlertDialog.Builder(getActivity());
        builderKey.setTitle("Update "+ flagForString);
        LinearLayout linearLayout=new LinearLayout(getActivity());
        final EditText getUserName= new EditText(getActivity());
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        getUserName.setGravity(Gravity.CENTER);
        getUserName.setHint("update "+flagForString);
        linearLayout.addView(getUserName);
        builderKey.setView(linearLayout);
        builderKey.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String value = getUserName.getText().toString();
                if(!TextUtils.isEmpty(value)) {
                    progressDialog.show();
                   if(flagForString.equals("username")) {
                       updateUsername(value,"username",tv_profile_username.getText().toString());
                       progressDialog.dismiss();
                   }
                }
                else Helper.makeABuilder("שגיאה. יש שדות ריקים. ",getActivity());
            }})
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
            }
        });
        builderKey.create().show();
    }

    /**This Method updateUsername - Updates The Database Of The User With A New Username if the username does not exist.*/
    private void updateUsername(final String value, String flagForString, final String old) {

        final DatabaseReference userCheck = FirebaseDatabase.getInstance().getReference("users");
        userCheck.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int flagExist = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String key = ds.getKey();
                    //if the user is not me and the new username equals to someone elses name, then exit.
                    if (key!=user.getUid() &&(snapshot.child(key).child("username").getValue().toString()).equals(value))
                        flagExist = 1;
                }
                if (flagExist == 1) {
                    progressDialog.dismiss();
                    Helper.makeABuilder("the username exist in funun.\n plz choose another username and try again. ",getActivity());
                } else if (flagExist == 0) {
                    sharedPref2 = getActivity().getSharedPreferences("Username", MODE_PRIVATE);
                    //update in user db
                    //update in events db
                    final DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("events");
                    eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (final DataSnapshot ds : snapshot.getChildren()) {
                                //for every event name
                                final DatabaseReference db = eventRef.child(ds.getKey());
                                db.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                        //find guest lists //find host lists
                                        final DatabaseReference dbguests = db.child("guests");
                                        final DatabaseReference dbhosts = db.child("hosts");
                                        dbguests.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot3) {
                                                //if the old username is in event, update the username to the new username.
                                                for (DataSnapshot ds2 : snapshot3.getChildren()) {
                                                    String name = ds2.getKey().toString();
                                                    if (name.equals(old)) {
                                                        HashMap<String, Object> map = new HashMap<>();
                                                        map.put(value, value);
                                                        dbguests.child(old).removeValue();
                                                        dbguests.setValue(map);
                                                    }
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                            }
                                        });
                                        dbhosts.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot3) {
                                                for (DataSnapshot ds2 : snapshot3.getChildren()) {
                                                    String name = ds2.getValue().toString();
                                                    if (name.equals(old)) {
                                                        if (name.equals(old)) {
                                                            HashMap<String, Object> map = new HashMap<>();
                                                            map.put(value, value);
                                                            dbhosts.child(old).removeValue();
                                                            dbhosts.setValue(map);
                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                    tv_profile_username.setText(value);
                    myUsername = value;
                    userDB.child("username").setValue(value);
                    sharedPref2.edit().putString("MyUsername", value).commit();
                    Toast.makeText(getActivity(), "שם המשתמש עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**This Method selectImage - Creates A New Intent To Get A New Profile Picture From The Gallery */
    private void selectImage() {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(Intent.createChooser(intent, "Please Select Image"), IMAGE_PICK_GALLERY_REQUEST_CODE);
    }

    /**This Method Update The Profile Image In Firebase Storage & Database
     * @param bitmap2 - contains the bitmap of a specific photo.  */
    private void changeProfileImage(Bitmap bitmap2)  {

        final StorageReference filepath1 = storageReference.child("profile_images").child(user.getUid() + ".jpg");
        byte[] data ;
        Toast.makeText(getActivity(), "התמונה תתעדכן מיד", Toast.LENGTH_SHORT).show();
        //compress image.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap2.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        data = byteArrayOutputStream.toByteArray();

        //upload image
        UploadTask uploadTaskNew = filepath1.putBytes(data);
        uploadTaskNew.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                   final UploadTask.TaskSnapshot taskResult = task.getResult();
                    Task<Uri> getDownloadUrl = taskResult.getStorage().getDownloadUrl();
                    while (!getDownloadUrl.isSuccessful()) ;
                    Uri downloadUri = getDownloadUrl.getResult();
                    //check if image uploaded or not and url is received
                    if (getDownloadUrl.isSuccessful()) {
                        //image uploaded+add update uri to user database
                        HashMap<String, Object> results = new HashMap<>();
                        results.put("userImage", downloadUri.toString());
                        userDB.updateChildren(results)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //url in database of user is updated successfully
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), "שגיאה בבסיס הנתונים. אנא נסה שנית", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getActivity(), "שגיאה בבסיס הנתונים 2. אנא נסה שנית", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getActivity(), "שגיאה בבסיס הנתונים 3. אנא נסה שנית ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**This Method onResume() -
     *  All data from any event should Be deleted if exists.*/
    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPref = getActivity().getSharedPreferences("Event", MODE_PRIVATE);
        if(sharedPref!=null) {
            SharedPreferences.Editor editorNew=sharedPref.edit();
            editorNew.clear().commit();
        }
    }
}
