package com.example.funun.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.funun.Adapter.GuestsAdapter;
import com.example.funun.Model.EventModel;
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
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


/**This Fragment - GuestsFragment - initialize the view of guests list.
 * 1. initialize recycler view of guests.
 * 2. notify the adapter when a guest is added */
public class GuestsFragment extends Fragment {

    //view elements + layout & adapter.
    GuestsAdapter friendDataAdapter;
    RecyclerView friendRecyclerView;
    List<User> guestsList ;
    GridLayoutManager gridLayoutManager;
    EditText searchView;
    ImageView iv_search_friend;
    View view;

    EventModel model;
    TextView tv_guests_view;
    TextView tv_search_guest_text;

    //shared pref items.
    SharedPreferences sharedPref;
    SharedPreferences sharedPref2;
    boolean eventHost;
    boolean no_info;
    String eventName;
    String myUserName;

    //firebase references
    DatabaseReference usersRef;
    DatabaseReference eventsRef;

    public GuestsFragment() {
        // Required empty public constructor
    }

    /**This Method - onCreate - Called to do initial creation of a fragment.
     * initialize shared pref and firebase objects.*/
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getActivity().getSharedPreferences("Event", Context.MODE_PRIVATE);
        sharedPref2=getActivity().getSharedPreferences("Username",Context.MODE_PRIVATE);
        if(sharedPref.contains("eventHost")) eventHost=true;
        else eventHost=false;
        usersRef=FirebaseDatabase.getInstance().getReference("users");
        eventsRef=FirebaseDatabase.getInstance().getReference("events");
    }

    /**This Method - onCreateView - creates and returns the view associated with the fragment.
     this method makes sure that if the shared pref file is empty,  the view will not be created.
     * @return Return the View for the fragment's UI, or null.
     *  @param inflater - The LayoutInflater object that can be used to inflate views in the fragment
     * @param container - the parent view that the fragment's UI should be attached to.
     *@param savedInstanceState - If non-null, this fragment is being re-constructed from a previous saved state as given here.*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        sharedPref = getActivity().getSharedPreferences("Event", Context.MODE_PRIVATE);
        sharedPref2=getActivity().getSharedPreferences("Username",Context.MODE_PRIVATE);
        if (sharedPref.getAll().size() ==0) {
            view = inflater.inflate(R.layout.no_info, container, false);
            no_info = true;
        } else {
            no_info = false;
            //init list of guests.
            guestsList = new ArrayList<>();
            eventHost = sharedPref.getBoolean("eventHost",true);
            eventName = sharedPref.getString("eventName","");
            myUserName= sharedPref.getString("MyUsername","");
            view = inflater.inflate(R.layout.fragment_guests, container, false);
        }

        // Create event recycler view data adapter with event item list.
        return view;
    }

    /**This Method - onViewCreated - initialize the view elements and attach listeners with values.
     * Called immediately after onCreateView
     * check if your a host or a guest in the specific event
     * attach adapter to this fragment.
     */

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //get your user name and if youre a host.
        if (!no_info) {

            sharedPref2=getActivity().getSharedPreferences("Username",Context.MODE_PRIVATE);
            sharedPref = getActivity().getSharedPreferences("Event", Context.MODE_PRIVATE);
            Bundle bundle=getParentFragment().getArguments();
            model=(EventModel) bundle.getSerializable("eventItem");

            if(sharedPref.contains("isFriendAdded")) sharedPref.edit().remove("isFriendAdded").commit();
            myUserName=sharedPref2.getString("MyUsername","");
            eventName = sharedPref.getString("eventName","");
            if(sharedPref.contains("eventHost")) eventHost=true;
            else eventHost=false;

            tv_guests_view=view.findViewById(R.id.tv_guests_view);
            tv_search_guest_text=view.findViewById(R.id.tv_search_guest_text);
            searchView = view.findViewById(R.id.search_view);
            iv_search_friend = view.findViewById(R.id.iv_search_friend);
            friendRecyclerView = (RecyclerView) view.findViewById(R.id.card_view_guests_list);
            gridLayoutManager = new GridLayoutManager(getActivity(), 1);
            friendRecyclerView.setLayoutManager(gridLayoutManager);
            friendRecyclerView.setHasFixedSize(true);
            friendDataAdapter = new GuestsAdapter(guestsList, getActivity(), this);
            friendRecyclerView.setAdapter(friendDataAdapter);
            iv_search_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = searchView.getText().toString();
                if(eventHost) {
                    if (TextUtils.isEmpty(username))
                        Helper.makeABuilder("אנא כתבו שם משתמש של חבר קיים ב FUNUN",getActivity());
                    else if (username.equals(myUserName)) {
                        Helper.makeABuilder("אתה לא יכול להוסיף את עצמך",getActivity());
                    } else {
                        findIfGuestExistInFunun(username);
                    }
                }
                //if user is not a host of the event then he cant add friends. he can see the list.
                else Helper.makeABuilder("את/ה אורח! רק למנהלי אירוע מותר להוסיף אורחים.",getActivity());
            }
            });
        }
    }

    public void notifyDataChanged() {
        friendDataAdapter.notifyDataSetChanged();
    }


    /**This Method - findIfGuestExistInFunun - searches in the firebase database if the guest exist.
     * @param newGuestToFind  - the name of the guest */
    private void findIfGuestExistInFunun(final String newGuestToFind){

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int flagExist = 0;
                for(DataSnapshot dsds:snapshot.getChildren()) {
                    if ((dsds.child("username").getValue().toString()).equals(newGuestToFind)) {
                        flagExist = 1;
                    }
                }
                if (flagExist==1){
                    addGuest(newGuestToFind);
                }
                else{
                    Helper.makeABuilder("שם המשתמש לא קיים באפליקציית פנאן." +"\n"+
                            " אנא הזמן את החבר להוריד את האפליקציה",getActivity());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**This Method - addGuest - Creates a new guest in the event
     *updates the firebase database in the guestRef and eventRef
     * @param friendToAdd - the name of the guest that we want to add */
    private void addGuest(final String friendToAdd) {
        final DatabaseReference eventDb = eventsRef.child(eventName);
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        int flagOfDup = 0;

        //check if user exist in guests list
        for (User ur : guestsList) {
            if (ur.getUsername().equals(friendToAdd)) {
                Helper.makeABuilder("האורח קיים כבר באירוע. ",getActivity());
                flagOfDup = 1;
            }
        }
        //if the user does not exists in local database, check if he exists in users.
        if (flagOfDup != 1) {
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot snapshot) {

                    for (DataSnapshot ds : snapshot.getChildren()) {
                        //first find the uid of the user
                        final String uidnew = ds.getKey();
                        //if the user is not my user.
                        if (!(uidnew.equals(user.getUid()))) {
                            usersRef.child(uidnew).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                    final String usernameNew = snapshot2.child("username").getValue().toString();
                                    final String email = snapshot2.child("userEmail").getValue().toString();
                                    final String photo = snapshot2.child("userImage").getValue().toString();
                                    //take ds user name new and compare to the username we want to add
                                    if (friendToAdd.equals(usernameNew)) {
                                        User friendToRecycler = new User(friendToAdd, email, photo);
                                        guestsList.add(friendToRecycler);
                                        //add event to guest. add guest to event
                                        usersRef.child(uidnew).child("events").child("guests").child(eventName).setValue(eventName);
                                        eventDb.child("guests").child(friendToAdd).setValue(friendToAdd);
                                        Toast.makeText(getActivity(), "החבר " + friendToAdd + "הצטרף לאירוע ", Toast.LENGTH_SHORT).show();
                                        notifyDataChanged();
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