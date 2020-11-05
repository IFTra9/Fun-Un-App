package com.example.funun.Adapter;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.funun.Model.Helper;
import com.example.funun.Model.User;
import com.example.funun.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**This Class Adapter - GuestAdapterRV - provide a binding between the data,holder and the recycler view.
 *  set the views that are displayed within a RecyclerView.
 *  used also as a controller for the elements in background. */

public class GuestsAdapter extends RecyclerView.Adapter<GuestsAdapter.UserHolder> {

    List<User> guestsList;
    Context context;
    Fragment userFrag;
    GuestsAdapter.UserHolder ret;

    //
    SharedPreferences sharedPref;
    SharedPreferences sharedPref2;
    String eventName;
    String myUsername;
    boolean eventHost;
    int flagUserHost;

    DatabaseReference userDb;
    DatabaseReference eventDb;

    /**This Constructor NotesAdapterRV - updates and initialize the params from the NotesFragment.
     * initialize view element from frag
     * initialize pointer to shared pref files.
     * @param context - the relevant context of app.
     * @param guestsList the guests list from Guestsfragment.
     * @param userFrag - the fragment GuestsFragments.*/

    public GuestsAdapter(List<User> guestsList, Context context, Fragment userFrag) {
        this.guestsList = guestsList;
        this.context = context;
        this.userFrag = userFrag;
        sharedPref = userFrag.getActivity().getSharedPreferences("Event", Context.MODE_PRIVATE);
        sharedPref2 = userFrag.getActivity().getSharedPreferences("Username", Context.MODE_PRIVATE);
        userDb = FirebaseDatabase.getInstance().getReference("users");
        eventName = sharedPref.getString("eventName", "");
        myUsername = sharedPref2.getString("MyUsername", "");
        eventDb = FirebaseDatabase.getInstance().getReference("events").child(eventName);
        showAllGuests();
    }

    @NonNull
    @Override
    /**This Method - onCreateViewHolder - Called when RecyclerView needs a new RecyclerView.ViewHolder from the given type to represent an item.*/
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        // Inflate the RecyclerView item layout xml.
        View view1 = layoutInflater.inflate(R.layout.view_item_user_card, parent, false);
        // Create and return our custom product Recycler View Item Holder object.
        ret = new GuestsAdapter.UserHolder(view1);
        return ret;
    }

    /**This Method -onBindViewHolder - Called by RecyclerView to display the data at the specified position.
     *this method update the content of the RecyclerView.ViewHolder.itemView to reflect the item at the given position.
     *this method initialize the shared pref files.
     * checks if the current user is a host in the event.
     * sets dialog(if needed, and listeners to the guests items.
     * @param position - the position of the guest item in the list (screen)
     * @param holder  - the holder that holds the view elements. */
    @Override
    public void onBindViewHolder(@NonNull final UserHolder holder, int position) {

        //check if user is a host, and what is his username.
        if(sharedPref.contains("eventHost"))eventHost = sharedPref.getBoolean("eventHost",true);
        else eventHost=false;
        myUsername = sharedPref2.getString("MyUsername", "");
        final User guestItem = guestsList.get(position);

        //bind the view elements with the data of the guest.
        final String friendName = guestsList.get(position).getUsername();
        holder.itemView.setTag(guestItem);
        String email = guestsList.get(position).getUserEmail();
        String photo = guestsList.get(position).getUserImage();
        holder.tv_friend_name.setText(friendName);
        holder.tv_friend_email.setText(email);
        try {
            Picasso.get().load(photo).into(holder.iv_friend_photo);

        } catch (Exception e) {
            holder.iv_friend_photo.setImageResource(R.drawable.person);
        }
        holder.iv_user_delete.setImageResource(R.drawable.remove_icon);

        //set listeners.
        holder.iv_user_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (eventHost) {
                    deleteGuestFromEvent(guestItem.getUsername(),guestItem);
                } else
                    Helper.makeABuilder("רק מנהלי קבוצה יכולים להסיר חברים",userFrag.getActivity());
            }
        });

        holder.tv_add_as_a_host.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUserAsAHost(guestItem);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick (View v){
        }
    });
}
    @Override
    public int getItemCount() {
        return guestsList.size();
    }

    static class UserHolder extends RecyclerView.ViewHolder {

        TextView tv_friend_email;
        TextView tv_friend_name ;
        ImageView iv_friend_photo;
        ImageView iv_user_delete;
        TextView tv_add_as_a_host;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            tv_friend_name = itemView.findViewById(R.id.tv_friend_name);
            tv_friend_email = itemView.findViewById(R.id.tv_friend_email);
            iv_friend_photo = itemView.findViewById(R.id.iv_friend_photo);
            iv_user_delete = itemView.findViewById(R.id.iv_user_delete);
            tv_add_as_a_host = itemView.findViewById(R.id.tv_add_as_a_host);
        }
    }

    /**This Method - showAllGuests - Creates a local list of all Guests from the firebase database*/
    public void showAllGuests(){

        final List<String> userListTemp=new ArrayList<>();
        //get all names guests from event
        eventDb.child("guests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String value = ds.getKey();
                    userListTemp.add(value);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        //go through all users and find the event that they belong.
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                for ( DataSnapshot ds1 : snapshot1.getChildren()) {
                    final String uidnew = ds1.getKey();
                    DatabaseReference userDb2 = userDb.child(uidnew).getRef();
                    userDb2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = snapshot.child("username").getValue().toString();
                        String photo = snapshot.child("userImage").getValue().toString();
                        String name = snapshot.child("username").getValue().toString();
                            if (userListTemp.contains(name)) {
                                guestsList.add(snapshot.getValue(User.class));
                                notifyDataSetChanged();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Helper.makeABuilder("לא ניתן להוסיף. אירעה שגיאה במערכת הנתונים",userFrag.getActivity());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**This Method addUserAsAHost - updates the shared pref if the friend that we want to add is already a host in the event.
     * @param userToUpdate - the specific user to add as a host in firebase database.
     *   */
    public void addUserAsAHost(User userToUpdate) {
        final DatabaseReference findHost = eventDb.child("hosts");
        final String host = userToUpdate.getUsername();
        if (!(host.equals(myUsername))) {//if the friend is not me
            if (eventHost) {//if the curr user is a host, then he can add a host.
                findHost.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override//check if the friend is a host
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        flagUserHost = 1;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            final String name = ds.getKey();
                            if (!(name.equals(myUsername)) && host.equals(ds.getKey())) {
                                flagUserHost = 0;//the friend is a host already
                            }
                        }
                        if (flagUserHost == 0) {
                            Helper.makeABuilder("המשתמש הוא אחד ממנהלי האירוע. לא ניתן להוסיף שוב. ",userFrag.getActivity());
                        } else {
                            //make the user a host
                            updateUserHost(host);
                            Toast.makeText(context, "User " + host + " Is Now A Host", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
            else{
                Helper.makeABuilder("אורחים לא יכולים להוסיף אורחים אחרים. ",userFrag.getActivity());
            }
            //if you pressed yourself
        } else {
            Helper.makeABuilder("את/ה לא יכול/ה להוסיף או להסיר את עצמך ממסך זה. ",userFrag.getActivity());
        }
    }

/**This Method - deleteGuestFromEvent - removes the user from the guests list of the event
 * 1. removes from event hosts and guests
 * 2. removes from the specific event that he was a guest in.
 * 3. removes from the specific event that he was a host in.
 * @param friendName - the friend name that we want to add as a guest.
 * @param guestItem - the item that holds the friend details. */
    private void deleteGuestFromEvent(final String friendName, final User guestItem) {

        if(guestsList.size()==1)
            Helper.makeABuilder("את/ה האורח/ת היחיד/ה של האירוע. ניתן להסיר את האירוע ממסך כל האירועים. ",userFrag.getActivity());
        else {
            if (friendName.equals(myUsername))
                Helper.makeABuilder("לא ניתן לעשות שינויים על עצמך",userFrag.getActivity());
            else {
                DatabaseReference removeGuestFromEvent= eventDb.child("guests").child(friendName).getRef();
                DatabaseReference removeHostsFromEvent= eventDb.child("hosts").child(friendName).getRef();
                removeGuestFromEvent.removeValue();
                removeHostsFromEvent.removeValue();
                userDb.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot snapshot) {
                        for (final DataSnapshot ds : snapshot.getChildren()) {
                            //first find the uid of the user
                            final String uid = ds.getKey();
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (!(ds.getKey().equals(user.getUid()))) {
                                userDb.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                        final String usernameNew = snapshot2.child("username").getValue().toString();
                                        if (friendName.equals(usernameNew)) {
                                            DatabaseReference removeEventFromHost = userDb.child(uid).child("events").child("hosts").child(eventName);
                                            DatabaseReference removeEventFromGuest = userDb.child(uid).child("events").child("guests").child(eventName);
                                            removeEventFromHost.removeValue();
                                            removeEventFromGuest.removeValue();
                                            guestsList.remove(guestItem);
                                            notifyDataSetChanged();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        }
    }

    /**This Method updateUserHost - Updates the following in firebase database
     *@param friendName - the friend that we want to update his status. */
    private void updateUserHost(final String friendName) {
        DatabaseReference eventRef2 = FirebaseDatabase.getInstance().getReference("events").child(eventName);
        final DatabaseReference userRef2 = FirebaseDatabase.getInstance().getReference("users");
        eventRef2.child("hosts").child(friendName).setValue(friendName);
        userRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                for (final DataSnapshot ds : snapshot.getChildren()) {
                    //first find the uid of the user
                    final String uid = ds.getKey();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (!(ds.getKey().equals(user.getUid()))) {
                        //if the userid ==guest username typed, update the correct values in the database.
                        userDb.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                final String usernameNew = snapshot2.child("username").getValue().toString();
                                if (friendName.equals(usernameNew)) {
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put(eventName, eventName);
                                    userRef2.child(uid).child("events").child("hosts").setValue(map);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}


