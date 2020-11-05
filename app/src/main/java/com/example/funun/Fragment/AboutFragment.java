package com.example.funun.Fragment;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
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
import androidx.fragment.app.Fragment;

import com.example.funun.Model.EventModel;
import com.example.funun.Model.Helper;
import com.example.funun.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;

/**This Class AboutFragment - Initialize the fragment with the chosen event details.*/
public class AboutFragment extends Fragment {

    //variables containers for event location
    String eventName;
    String eventDate;
    String eventTime;
    String eventLocation;

    //firebase variables
    FirebaseUser user;
    DatabaseReference eventRef;

    //view items
    EditText et_about_location, et_about_time, et_about_date;
    TextView et_about_title;
    ImageView iv_about_image;
    Button btn_about_update_events;
    Button btn_about_set_calender;
    TextView are_you_a_guest;
    View view ;

    SharedPreferences sharedPref;

    private ProgressDialog progressDialog;
    private boolean eventHost;
    private boolean no_info;


    public AboutFragment() {
        // Required empty public constructor
    }

    /**This Method - onCreate - Called to do initial creation of a fragment.
     * initialize shared pref and firebase objects.*/
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(getActivity());
        sharedPref = Objects.requireNonNull(getActivity()).getSharedPreferences("Event", MODE_PRIVATE);
        eventName = sharedPref.getString("eventName", "");
        eventRef = FirebaseDatabase.getInstance().getReference("events");
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    /**This Method - onCreateView - creates and returns the view associated with the fragment.
     this method makes sure that if the shared pref file is empty,  the view will not be created.
     * @return Return the View for the fragment's UI, or null.
     * @param inflater - The LayoutInflater object that can be used to inflate views in the fragment
     * @param container - the parent view that the fragment's UI should be attached to.
     *@param savedInstanceState - If non-null, this fragment is being re-constructed from a previous saved state as given here.*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        sharedPref = getActivity().getSharedPreferences("Event", MODE_PRIVATE);
        if(sharedPref.contains("eventHost"))eventHost=true;
        else eventHost=false;
        if (sharedPref.getAll().size() == 0) {
            view = inflater.inflate(R.layout.no_info, container, false);
            no_info = true;
        }
        else {
            view = inflater.inflate(R.layout.fragment_about, container, false);
            no_info=false;
        }
        return view;
    }

    /**This Method - onViewCreated - initialize the view elements and attach listeners with values.
     * Called immediately after onCreateView
     * 1. gets from shared pref the event details.
     * 2. set the text and photo details on screen.
     * 3. check if your a host or a guest in the specific event
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(!no_info) {
            sharedPref = getActivity().getSharedPreferences("Event", MODE_PRIVATE);
            eventName = sharedPref.getString("eventName", "");
            eventLocation = sharedPref.getString("eventLocation", "");
            eventDate = sharedPref.getString("eventDate", "");
            eventTime = sharedPref.getString("eventTime", "");
            if(sharedPref.contains("eventHost")) eventHost=sharedPref.getBoolean("eventHost",true);
            else eventHost = false;
            eventRef = FirebaseDatabase.getInstance().getReference("events").child(eventName);
            //identify the view item
            et_about_title = view.findViewById(R.id.et_about_title);
            et_about_location = view.findViewById(R.id.et_about_location);
            et_about_time = view.findViewById(R.id.et_about_time);
            et_about_date = view.findViewById(R.id.et_about_date);
            iv_about_image = view.findViewById(R.id.iv_about_image);
            are_you_a_guest= view.findViewById(R.id.are_you_a_guest_about);
            btn_about_update_events = view.findViewById(R.id.btn_about_update_event);
            btn_about_set_calender = view.findViewById(R.id.btn_about_set_calender);

            //set the content of event
            iv_about_image=setPhotoFromFirebase(iv_about_image);
            et_about_title.setText(eventName);
            et_about_date.setText(eventDate);
            et_about_time.setText(eventTime);
            et_about_location.setText(eventLocation);

           //disable input for both date and time (only Tap Twice action will triger them.
            disableInput(et_about_date);
            disableInput(et_about_time);

            //if the user is a guest in the event - disable keys. set text guest, disable editing location.
            if (!eventHost) {
                disableInput(et_about_location);
                are_you_a_guest.setText(R.string.Guest);
                btn_about_update_events.setVisibility(View.GONE);
            } else {
                are_you_a_guest.setText(R.string.Host);
                et_about_title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      Helper.makeABuilder("לא ניתן לשנות את שם האירוע.",getActivity());
                    }
                });

                et_about_date.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View v) {
                        Helper.setDateDialog(et_about_date,getActivity());
                    }
                });

                et_about_time.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Helper.setTimeDialog(et_about_time,getActivity());
                    }
                });

                btn_about_update_events.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //check if some of the field is empty
                        progressDialog.show();
                        if ((TextUtils.isEmpty(et_about_location.getText().toString())) || TextUtils.isEmpty(et_about_date.getText().toString())
                                || TextUtils.isEmpty(et_about_time.getText().toString())) {
                            Helper.makeABuilder("שגיאה!."+"\n"+ "חלק מהשדות ריקים. "+"\n"+" אנא עדכן את פרטי האירוע. לא ניתן לשנות שם אירוע או תמונה.",getActivity());
                        }
                        else if(!TextUtils.isEmpty(et_about_location.getText().toString())) {
                            //check if the input in location in according to the rules.
                            boolean check = Helper.checkLetters(et_about_location.getText().toString(), getActivity());
                            if (!check) {
                                String location = et_about_location.getText().toString();
                                String time = et_about_time.getText().toString();
                                String date = et_about_date.getText().toString();
                                updateAllEventDetails(location, time, date, progressDialog);
                                eventDate = date;
                                eventTime = time;
                            }
                            else{
                                progressDialog.dismiss();
                            }
                        }
                    }
                });
            }
            btn_about_set_calender.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                @Override
                public void onClick(View v) {
                    setEventCalendar();
                }
            });
        }
    }

/**This Method setEventCalendar -
 * 1. davide the Date and Time strings into int
 * 2. creates a Calendar Object and update to correct time and date received from main.
 * 3. calls Calendar Intent and Opens the phone Calender
 * */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void setEventCalendar()  {
        String[] str1 = et_about_date.getText().toString().split("/");
        int day = Integer.parseInt(str1[0]);
        int month = Integer.parseInt(str1[1])-1;
        int year = Integer.parseInt(str1[2]);
        String[] str2 = et_about_time.getText().toString().split(":");
        int hours = Integer.parseInt(str2[0]);
        int minutes = Integer.parseInt(str2[1]);
        Calendar cal = Calendar.getInstance();
        cal.set(year,month,day,hours,minutes);
        cal.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        Intent callCalender = new Intent(Intent.ACTION_INSERT);
        callCalender.setData(CalendarContract.Events.CONTENT_URI);
        callCalender.putExtra(CalendarContract.Events.TITLE,eventName);
        callCalender.putExtra(CalendarContract.Events.EVENT_LOCATION,eventLocation);
        callCalender.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY,false);
        callCalender.putExtra(CalendarContract.Events.EVENT_TIMEZONE,cal.getTimeZone());
        callCalender.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,cal.getTimeInMillis());
        callCalender.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,cal.getTimeInMillis()+1000*60*60*2);
        callCalender.putExtra(CalendarContract.Events.RRULE,  "FREQ=DAILY;INTERVAL=6;COUNT=4");
        callCalender.putExtra(CalendarContract.Reminders.MINUTES, 60);
        callCalender.putExtra(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        callCalender.putExtra(CalendarContract.Reminders.EVENT_ID, eventName);
        startActivity(callCalender);
    }

/**This Method - setPhotoFromFirebase - Sets the picture from Firebase database to profile image
 * @param iv the ImageView object on the view. */
    public ImageView setPhotoFromFirebase(ImageView iv) {
        String eventPhotoFirebase =sharedPref.getString("eventPhoto","");
        try {
            Picasso.get().load(eventPhotoFirebase).into(iv);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.person).into(iv);
        }
        return iv;
    }

    /**This Method - updateAllEventDetails - Updates the event details received from the change ant put into firebase database.
     * @param dialog the dialog that we want to close after the update accrued.
     * @param location the event location
     * @param date the event date
     * @param time the event time*/
    private void updateAllEventDetails(String location, String time, String date, final ProgressDialog dialog) {

        dialog.show();
        final HashMap<String, Object> result = new HashMap<>();
        result.put("eventLocation", location);
        result.put("eventTime", time);
        result.put("eventDate", date);
        DatabaseReference data = FirebaseDatabase.getInstance().getReference("events");
        data.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(eventName).exists()){
                    if(snapshot.child(eventName).child("eventLocation").getValue().toString().equals(eventLocation)&&
                            snapshot.child(eventName).child("eventTime").getValue().toString().equals(eventTime)&&
                            snapshot.child(eventName).child("eventDate").getValue().toString().equals(eventDate)) {
                        eventRef.updateChildren(result)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getActivity(), "האירוע עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity(), "לא ניתן לעדכן את האירוע בבסיס הנתונים", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                    else {
                        Helper.makeABuilder("The event was changed because another host has changed the event.\n" +
                                "you can choose another event or can add a new event," +
                                "\n you can also sign out and login to refresh the all events tab.", getActivity());
                        dialog.dismiss();
                    }
                }
                else Helper.makeABuilder("The event was deleted because another host has deleted the event.\n" +
                        "you can add a new event, or choose another event." +
                        "\n you can also sign out and login to refresh the all events tab.",getActivity());
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    /**This Method - disableInput - stop the EditText containers from being editable.
     * @param editText the EditText object that we want to disable. */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    void disableInput(EditText editText) {
        editText.setInputType(InputType.TYPE_NULL);
        editText.setTextIsSelectable(false);
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return true;  // Blocks input from hardware keyboards.
            }
        });
    }
}