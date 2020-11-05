package com.example.funun.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.funun.Fragment.EventDetailsFragment;
import com.example.funun.Model.EventModel;
import com.example.funun.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


/**This Class Adapter - EventAdapterRV - provide a binding between the data,holder and the recycler view.
 *  set the views that are displayed within a RecyclerView. */
public class EventAdapterRV extends RecyclerView.Adapter<EventAdapterRV.EventRecyclerViewItemHolder>{

    public static List<EventModel> eventsList;
    ProgressDialog progressDialog;
    Context context;
    Fragment one;

    //shared pref elements
    private SharedPreferences sharedPref2;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    boolean eventHost;
    String myUsername;
    String eventPhotoFrom;

    private TextView tv3;
    private ImageView tv2;

    //initialize firebase objects.
    DatabaseReference eventsRef;
    FirebaseUser user;
    DatabaseReference userRef;


    /**This Constructor EventAdapterRV - updates and initialize the params from the alleventfragment.
     * 1. gets an empty list from all events,, and the original fragment that holds the recyclerview
     * @param tv2 - part of view that is visible/gone accoridng to the size of the list.
     * @param tv3 - part of view that is visible/gone accoridng to the size of the list.
     * @param context - the relevant context of app.
     * @param eventsLists the event list from all events.
     * @param frag - the fragment AllEventsFragment. */
    public EventAdapterRV(Context context, List<EventModel> eventsLists, Fragment frag,ImageView tv2,TextView tv3)
    {
        eventsList = eventsLists;
        this.context = context;
        one = frag;
        this.tv2=tv2;
        this.tv3=tv3;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Uploading....");
        user=FirebaseAuth.getInstance().getCurrentUser();
        eventsRef=FirebaseDatabase.getInstance().getReference("events");
        userRef=FirebaseDatabase.getInstance().getReference("users");
        sharedPref = one.getActivity().getSharedPreferences("Event", MODE_PRIVATE);
        sharedPref2 = one.getActivity().getSharedPreferences("Username", MODE_PRIVATE);
        editor = sharedPref.edit();
        getAllEvents();

    }


    /**This Method - onCreateViewHolder - Called when RecyclerView needs a new RecyclerView.ViewHolder from the given type to represent an item.*/
    @Override
    public EventRecyclerViewItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get LayoutInflater object.

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        // Inflate the RecyclerView item layout xml.
        View eventItemView = layoutInflater.inflate(R.layout.view_item_event_card, parent, false);
        // Create and return our custom event Recycler View Item Holder object.
        EventRecyclerViewItemHolder ret = new EventRecyclerViewItemHolder(eventItemView);
        return ret;
    }

    /**This Method -onBindViewHolder - Called by RecyclerView to display the data at the specified position.
     *this method update the content of the RecyclerView.ViewHolder.itemView to reflect the item at the given position.
     *this method initialize the shared pref files.
     * sets dialog(if needed, and listeners to the event items.  */
    @Override
    public void onBindViewHolder(final EventRecyclerViewItemHolder holder, final int position) {
        // Get event item dto in list.

        sharedPref = one.getActivity().getSharedPreferences("Event", MODE_PRIVATE);
        sharedPref2 = one.getActivity().getSharedPreferences("Username", MODE_PRIVATE);
        final EventModel eventItem = eventsList.get(position);
        // Set event item title.
        holder.itemView.setTag(eventItem);
        holder.eventDate.setText(eventItem.getEventDate());
        holder.eventName.setText(eventItem.getEventName());
        eventsRef.child(eventItem.getEventName()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventPhotoFrom = eventItem.getEventPhoto();
                // Set event image resource id.
                try {
                    Picasso.get().load(eventPhotoFrom).into(holder.eventPhoto);
                    progressDialog.dismiss();
                } catch (Exception e) {
                    holder.eventPhoto.setImageResource(R.drawable.person);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });
        progressDialog.show();
        holder.iv_all_events_remove.setImageResource(R.drawable.ic_remove_button);
        holder.iv_all_events_edit.setImageResource(R.drawable.ic_edit_button);
        holder.iv_all_events_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add here info to pass to other fragment
                moveToDeatilsFrag(eventItem);
            }
        });
        holder.iv_all_events_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFromFirebaseDatabase(eventItem.getEventName());
                eventsList.remove(eventsList.get(position));
                if(eventsList.size()==0) {
                    tv2.setVisibility(View.VISIBLE);
                    tv3.setVisibility(View.VISIBLE);
                }
                else{
                    tv2.setVisibility(View.GONE);
                    tv3.setVisibility(View.GONE);
                }
                notifyDataSetChanged();
            }
        });
        holder.eventPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToDeatilsFrag(eventItem);
            }
        });
    }
    /**This Method moveToDeatilsFrag - Changes the view to EventDetails Fragment with the event details inside
     * @param eventItem - passed for future handeling ( the event that was pressed on the list */
    public void moveToDeatilsFrag(EventModel eventItem) {
        sharedPref = one.getActivity().getSharedPreferences("Event", MODE_PRIVATE);
        FragmentManager fragmentManager = one.getActivity().getSupportFragmentManager();
        EventDetailsFragment eventDetailsFragment = new EventDetailsFragment();
        setPref(eventItem);
        Bundle bundle = new Bundle();
        bundle.putSerializable("eventItem", eventItem);
        eventDetailsFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.home_frame, eventDetailsFragment, "");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        ((BottomNavigationView) one.getActivity().findViewById(R.id.bottom_navigation)).setSelectedItemId(R.id.action_event_details);
    }

    @Override
    public int getItemCount() {
        int ret = 0;
        if (eventsList != null) {
            ret = eventsList.size();
        }
        return ret;
    }


    public class EventRecyclerViewItemHolder extends RecyclerView.ViewHolder {

        private TextView eventName;
        private ImageView eventPhoto;
        private TextView eventDate;
        private ImageView iv_all_events_remove;
        private ImageView iv_all_events_edit;

        public EventRecyclerViewItemHolder(View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.card_view_image_title);
            eventPhoto = itemView.findViewById(R.id.card_view_image);
            eventDate = itemView.findViewById(R.id.tv_date);
            iv_all_events_remove = itemView.findViewById(R.id.iv_all_events_remove);
            iv_all_events_edit = itemView.findViewById(R.id.iv_all_events_edit);
        }
    }

    /**This Method - getAllEvents - updates the UI Events List on screen
     * 1.search events and find the events that the user exists in them.
     * 2. add event info to every event found from (1). */
    private void getAllEvents(){

        //take a list of all user events from the users list firebase.
        final List<String> listOfStrings=new ArrayList<>();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("events").child("guests");
        //put all events that the user knows in one list
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String eventName = ds.getKey();
                    listOfStrings.add(eventName);
                }
                //search if event exist at database, put event info from firebase and add to local list for view updates.
                eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                        for (DataSnapshot ds1 : snapshot1.getChildren()) {
                            if (listOfStrings.contains(ds1.getKey())) {
                                eventsList.add(ds1.getValue(EventModel.class));
                            }
                        }
                        if(eventsList.size()==0) {
                            tv2.setVisibility(View.VISIBLE);
                            tv3.setVisibility(View.VISIBLE);
                        }
                        else{
                            tv2.setVisibility(View.GONE);
                            tv3.setVisibility(View.GONE);
                        }
                        notifyDataSetChanged();

                        progressDialog.dismiss();
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


    /**This Method - deleteFromFirebaseStorage - Delete's The Picture Of The Event From Firebase Storage By The URL From eventPhoto*/
    private void deleteFromFirebaseStorage(String url){

        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });
    }

    /**This Method deleteFromFirebaseDatabase - Deletes Event From All Users Associated To The Event
     * @param eventNameRemove  - the event name that was pressed by the user. */
    private void deleteFromFirebaseDatabase(final String eventNameRemove){
        //remove first from all users that are hosts or guests.
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for( DataSnapshot ds:snapshot.getChildren()){
                    String uid = ds.getKey();
                    if((ds.child("events").child("guests").child(eventNameRemove).getKey()).equals(eventNameRemove))
                        userRef.child(uid).child("events").child("guests").child(eventNameRemove).removeValue();
                    if((ds.child("events").child("hosts").child(eventNameRemove).getKey()).equals(eventNameRemove))
                        userRef.child(uid).child("events").child("hosts").child(eventNameRemove).removeValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //remove the picture from storage and remove the event from event database.
        DatabaseReference eventToremove=eventsRef.child(eventNameRemove);
        eventToremove.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("eventPhoto").getValue().toString();
                deleteFromFirebaseStorage(name);
                eventsRef.child(eventNameRemove).removeValue();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    /**This Method - setPref - update the shared pref file with the event details.
     *@param eventItem - the event that was pressed on the screen */
    public void setPref(final EventModel eventItem) {
        sharedPref = one.getActivity().getSharedPreferences("Event", MODE_PRIVATE);
        editor = sharedPref.edit();
        sharedPref2=one.getActivity().getSharedPreferences("Username",MODE_PRIVATE);
        myUsername=sharedPref2.getString("MyUsername","");
        if(eventItem.getHosts().containsKey(myUsername)) {
            eventHost=true;
            sharedPref.edit().putBoolean("eventHost",true).commit();
        }
        editor.putString("eventName", eventItem.getEventName());
        editor.putString("eventLocation", eventItem.getEventLocation());
        editor.putString("eventTime", eventItem.getEventTime());
        editor.putString("eventDate", eventItem.getEventDate());
        editor.putString("eventPhoto", eventItem.getEventPhoto());
        editor.commit();
    }
}