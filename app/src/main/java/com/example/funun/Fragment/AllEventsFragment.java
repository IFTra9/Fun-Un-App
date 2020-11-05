package com.example.funun.Fragment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.funun.Adapter.EventAdapterRV;
import com.example.funun.Model.EventModel;
import com.example.funun.Model.Helper;
import com.example.funun.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;


/**This Class AllEventsFragment -  initialize the view of events list.
 * 1. initialize recycler view of events.
 * 2. notify the adapter when an event was added */
public class AllEventsFragment extends Fragment {

    //view items.
    EditText et_eventName, et_Eventlocation, et_eventDate, et_ev_time;
    ImageView iv_pic_change_event_pic;
    View view1;
    EventAdapterRV eventDataAdapter;
    RecyclerView eventRecyclerView;
    GridLayoutManager gridLayoutManager;
    ProgressDialog progressDialog;
    Button btn_add_event_save;
    Button btn_add_event_cancel;
    ImageView tv3;
    TextView tv2;
    List<EventModel> eventsList;
    private FloatingActionButton btn_floatAddNewEvent;

    private Bitmap bitmap;
    EventModel putEvent;

    Uri image_uri;
    boolean flagExist;

    //path to firebase auth, database and storage.
    StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    DatabaseReference eventsRef;
    DatabaseReference userRef;
    FirebaseDatabase database;

    public AllEventsFragment() {
        // Required empty public constructor
    }

    /**This Method - onCreate - Called to do initial creation of a fragment.
     * initialize shared pref and firebase objects.
     * ask storage permissions from the device. */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //ask from the device permission to use the storage phone
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        //define refs to database.
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        progressDialog = new ProgressDialog(getActivity());
        database = FirebaseDatabase.getInstance();
        eventsRef = database.getReference("events");
        userRef = database.getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference();
        Helper.printallEvents();
    }

    /**This Method - onCreateView - creates and returns the view associated with the fragment.
     * @return Return the View for the fragment's UI, or null.
     * @param inflater - The LayoutInflater object inflate the view in the fragment
     * @param container - the parent view that the fragment's UI should be attached to.
     *@param savedInstanceState - If non-null, this fragment is being re-constructed from a previous saved state as given here.*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
            view1=inflater.inflate(R.layout.fragment_all_events, container, false);
        return view1;
    }

/**This Method onViewCreated - initialize the view to all events view.
 * attach listener to add event button.
 * Called immediately after onCreateView
 * check if your a host or a guest in the specific event
 * attach adapter to this fragment.
 * @return Return the View for the fragment's UI, or null.
 * @param view the view that was created for the fragment.
 *@param savedInstanceState - If non-null, this fragment is being re-constructed from a previous saved state as given here.*/
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btn_floatAddNewEvent = view.findViewById(R.id.btn_floatAddNewEvent);
        tv2=view.findViewById(R.id.tvempty21);
        tv3=view.findViewById(R.id.tvempty31);
        tv2.setVisibility(View.GONE);
        tv3.setVisibility(View.GONE);
        eventsList=new ArrayList<>();
            eventRecyclerView = view.findViewById(R.id.card_view_recycler_list);
            // Create the grid layout manager with 2 columns.
            eventRecyclerView.setHasFixedSize(true);
            gridLayoutManager = new GridLayoutManager(getActivity(), 2);
            // Set layout manager.
            eventRecyclerView.setLayoutManager(gridLayoutManager);

            // Create event recycler view data adapter with event item list.
            eventDataAdapter = new EventAdapterRV(getActivity(), eventsList, this,tv3,tv2);
            // Set data adapter.
            eventRecyclerView.setAdapter(eventDataAdapter);

            btn_floatAddNewEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create an alert builder
                    openEventBuilder();
                }
            });
    }

    /**This method onActivityResult - called after picking an image
     * 1. update bitmap of a picture/image. */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            image_uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image_uri);
                iv_pic_change_event_pic.setImageBitmap(bitmap);
                setBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity(), "please check your phone permission and try again", Toast.LENGTH_SHORT).show();
        }
    }


    /**This Method notifyDataChanged - notify the event adapter about changes that accrued in the event list */
    public void notifyDataChanged() {
        eventDataAdapter.notifyDataSetChanged();
    }


    private void setBitmap(Bitmap bitmap3) {
        this.bitmap = bitmap3;
    }

/**This Method - selectImage - opens a new intent - choose from storage phone. */
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Please Select Image"), ProfileFragment.IMAGE_PICK_GALLERY_REQUEST_CODE);
    }


    /**This Method openEventBuilder - Creates an AddEvent dialog for adding a new event to database.
     * initialize the new view of the dialog.
     * creates save and cancel buttons
     * attach listeners to the buttons. */
    public void openEventBuilder() {

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.alert_box_add_event, null);
        builder.setView(customLayout);
        final AlertDialog dialog = builder.create();
        et_eventDate = (EditText) customLayout.findViewById(R.id.et_eventDate);
        et_eventDate.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Helper.setDateDialog(et_eventDate,getActivity());
            }
        });
        //open time dialog to set the time
        et_ev_time = customLayout.findViewById(R.id.et_ev_time);
        et_ev_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.setTimeDialog(et_ev_time,getActivity());
            }
        });

        iv_pic_change_event_pic = customLayout.findViewById(R.id.iv_pic_change_event_pic);
        iv_pic_change_event_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        /*handle event name and location*/
        et_eventName = customLayout.findViewById(R.id.et_eventName);
        et_Eventlocation = customLayout.findViewById(R.id.et_Eventlocation);
        btn_add_event_save = customLayout.findViewById(R.id.btn_add_event_save);
        btn_add_event_cancel = customLayout.findViewById(R.id.btn_add_event_cancel);
        // if user clicked ok in alert box - check input
        btn_add_event_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newEventName = et_eventName.getText().toString();
                final String newLocationEvent = et_Eventlocation.getText().toString();
                final String newDateEvent = et_eventDate.getText().toString();
                final String newTimeEvent = et_ev_time.getText().toString();
                if (TextUtils.isEmpty(newTimeEvent) || TextUtils.isEmpty(newDateEvent) || TextUtils.isEmpty(newLocationEvent) || TextUtils.isEmpty(newEventName) ||
                        newEventName.length() < 2 || newLocationEvent.length() < 2) {
                    Helper.makeABuilder("כללים: \n" + " הוספת אירוע\n"+ "אורך שם האירוע גדול מ-1. \n" + "2. אורך שם המיקום צריך להיות גדול מ-1. \n" +
                            "3. יש לבחור את השעה והתאריך בלחיצה במקום המתאים. \n" + "4. חובה להוסיף תמונה לאירוע. \n" + "",getActivity());
                }
                else if(!TextUtils.isEmpty(newEventName)) {
                    boolean check = Helper.checkLetters(newEventName, getActivity());
                    boolean check2 = Helper.checkLetters(newLocationEvent,getActivity());
                    if (!check&&!check2) {
                        updateAllData(dialog, newEventName, newLocationEvent, newDateEvent, newTimeEvent);
                    }
                }
            }
        });
        btn_add_event_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        //create and show the alert dialog
        dialog.show();
    }


    /**This Method updateAllData - Updates Firebase Storage & Database.
     *  1.if a photo has been acquired from the AlertDialog then compress the image.
     *  2. check that the event name does not exist in the firebase database.
     *  3. create a new event in the local list and notify adapter about the changes.
     * @param newTimeEvent the event time
     * @param newDateEvent the event date
     * @param location the event location.
     * @param eventName the event name
     * @param dialog the alert dialog that we want to dismiss after the event has been created. */
    private void updateAllData(final AlertDialog dialog, final String eventName, final String location, final String newDateEvent, final String newTimeEvent) {

        //set the image to the storage.
        progressDialog.show();
        final StorageReference filepath2 = storageReference.child("eventImages").child("" + eventName + location + ".jpg");
        final DatabaseReference eventRef = eventsRef.child(eventName);
        byte[] data = getBytesFromImage();
        final UploadTask uploadTaskNew = filepath2.putBytes(data);

        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapsnap) {
                flagExist=false;
                for(DataSnapshot dsnew :snapsnap.getChildren()){
                    if(eventName.equals(dsnew.getKey())) flagExist=true;
                }
                if(flagExist){
                    progressDialog.dismiss();
                    Helper.makeABuilder("The Event Name Exist In Funun. Create a Unique Event Name And Try Again",getActivity());
                    btn_add_event_save.setFocusable(false);
                }
                else{
                    uploadTaskNew.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            //if image successfully uploaded, save to database of event specific id name+location.
                            if (task.isSuccessful()) {
                                final UploadTask.TaskSnapshot taskResult = task.getResult();
                                Task<Uri> getDownloadUrl = taskResult.getStorage().getDownloadUrl();
                                while (!getDownloadUrl.isSuccessful()) ;
                                final Uri downloadUri = getDownloadUrl.getResult();
                                //check if image uploaded or not and url is received
                                if (getDownloadUrl.isSuccessful()) {
                                    //image uploaded+added now update uri to event database
                                    HashMap<String, Object> results = new HashMap<>();
                                    results.put("eventPhoto", downloadUri.toString());
                                    eventRef.updateChildren(results)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //url in database of event is updated successfully
                                                    // send data from the AlertDialog to the Activity
                                                    userRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            createAModel(snapshot, eventName, newDateEvent, newTimeEvent, location, downloadUri);
                                                        }
                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                        }
                                                    });
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getActivity(), "problem with database", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(getActivity(), "cant reach database", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(getActivity(), " cant reach storage", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    dialog.dismiss();
                    progressDialog.dismiss();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**This Method getBytesFromImage - compress the image to small size.
     *  must be a small picture because of firebase limits
     * @return - return the byte data array that represent the image*/
    private byte[] getBytesFromImage() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (bitmap != null)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();
        return data;
    }

    /**This Method createAModel - Creates a new EventModel.
     * pushes the model into the Firebase Database and Storage
     * @param snapshot the snap from the firebase database that was taken.
     * @param eventName  the event name
     * @param location the event location
     * @param newDateEvent the event date.
     * @param newTimeEvent the event time.
     * @param downloadUri the link to the url where the photo is saved*/
    public void createAModel(@NonNull DataSnapshot snapshot, String eventName, String newDateEvent, String newTimeEvent, String location, Uri downloadUri) {
        String username = Objects.requireNonNull(snapshot.child("username").getValue()).toString();
        int products = 0;
        int notes = 0;
        HashMap<String, Object> guests = new HashMap<>();
        HashMap<String, Object> hosts = new HashMap<>();
        guests.put(username, username);
        hosts.put(username, username);
        //create a model for recycler view and add to list. notify.
        putEvent = new EventModel(eventName, newDateEvent, newTimeEvent, location, downloadUri.toString(), guests, hosts, products, notes);
        eventsList.add(putEvent);
        HashMap<String, Object> hashMap1 = putEvent.toMapEvent();
        //update in event guests and hosts the created user
        userRef.child(user.getUid()).child("events").child("guests").child(putEvent.getEventName()).setValue(putEvent.getEventName());
        userRef.child(user.getUid()).child("events").child("hosts").child(putEvent.getEventName()).setValue(putEvent.getEventName());
        eventsRef.child(eventName).setValue(hashMap1);

        if (eventsList.size() == 0) {
            tv2.setVisibility(View.VISIBLE);
            tv3.setVisibility(View.VISIBLE);
        } else {
            tv2.setVisibility(View.GONE);
            tv3.setVisibility(View.GONE);
        }
        notifyDataChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(eventsList.size()!=0) {
                tv2.setVisibility(View.GONE);
                tv3.setVisibility(View.GONE);
            }
    }
}