package com.example.funun.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.funun.Adapter.NotesAdapterRV;
import com.example.funun.Model.Helper;
import com.example.funun.Model.Notes;
import com.example.funun.R;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**This Fragment - NotesFragment - initialize the view of notes list.
 * 1. initialize recycler view of list of notes.
 * 2. notify the NoteAdapter when a note is added */

public class NotesFragment extends Fragment  {

    //view items.
    public static List<Notes> notesList;
    private FloatingActionButton btn_add_new_notes;
    private String newNoteTitle, newNoteDescription;
    Button btn_save,btn_cancel;
    View view;
    NotesAdapterRV noteDataAdapter;
    RecyclerView noteRecyclerView;
    GridLayoutManager gridLayoutManager;
    private TextView tv3;
    private ImageView tv2;
    TextView tv_notes_list_text;

    //firebase references
    FirebaseUser user;
    DatabaseReference notesRef;
    DatabaseReference eventRef;

    //shared pref items.
    private SharedPreferences shared_pref;
    SharedPreferences sharedPref2;
    private String eventName;
    Boolean eventHost;
    String myUsername;
    private boolean no_info;


    public NotesFragment() {
        // Required empty public constructor
    }

    /**This Method - onCreate - Called to do initial creation of a fragment.
     * initialize shared pref and firebase objects.*/
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shared_pref=getActivity().getSharedPreferences("Event", Context.MODE_PRIVATE);
        sharedPref2=getActivity().getSharedPreferences("Username",Context.MODE_PRIVATE);
        user=FirebaseAuth.getInstance().getCurrentUser();
        if(shared_pref.contains("eventHost"))eventHost = shared_pref.getBoolean("eventHost",true);
        else eventHost=false;
        notesRef = FirebaseDatabase.getInstance().getReference("notes");
        eventRef = FirebaseDatabase.getInstance().getReference("events");
        eventName = shared_pref.getString("eventName","");
        myUsername=sharedPref2.getString("MyUsername","");
    }

    /**This Method - onCreateView - creates and returns the view associated with the fragment.
     this method makes sure that if the shared pref file is empty,  the view will not be created.
     * @return Return the View for the fragment's UI, or null.
     * @param inflater - The LayoutInflater object that can be used to inflate views in the fragment
     * @param container - the parent view that the fragment's UI should be attached to.
     *@param savedInstanceState - If non-null, this fragment is being re-constructed from a previous saved state as given here.*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment

        if (shared_pref.getAll().size() == 0) {
            no_info = true;
        }
        else {
            no_info = false;
            view = inflater.inflate(R.layout.fragment_notes, container, false);

        }
        return view;

    }

    /**This Method - onViewCreated - initialize the view elements and attach listeners with values.
     * Called immediately after onCreateView
     * check if you are a host or a guest in the specific event
     * attach adapter to this fragment.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(!no_info) {
            tv2=view.findViewById(R.id.tvemptynotes3);
            tv3=view.findViewById(R.id.tvemptynotes2);
            tv2.setVisibility(View.GONE);
            tv3.setVisibility(View.GONE);
            tv_notes_list_text= view.findViewById(R.id.tv_notes_list_text);
            notesList=new ArrayList<>();
            // Create event recycler view data adapter with event item list.
            // Create the recyclerview.
            noteRecyclerView = (RecyclerView) view.findViewById(R.id.card_view_notes_list);
            // Create the grid layout manager with 1 columns.
            gridLayoutManager = new GridLayoutManager(getActivity(), 2);
            // Set layout manager.
            noteRecyclerView.setLayoutManager(gridLayoutManager);
            // Create event recycler view data adapter with event item list.
            noteDataAdapter = new NotesAdapterRV(this.getActivity(), notesList, this,tv2,tv3);
            // Set data adapter.
            noteRecyclerView.setAdapter(noteDataAdapter);
            btn_add_new_notes = (FloatingActionButton) view.findViewById(R.id.btn_float_add_new_note);
            btn_add_new_notes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    builderNote();
                }
            });
        }
    }

    public void notifyDataChanged(){
        noteDataAdapter.notifyDataSetChanged();
    }

    /**This Method setNotesInFirebase - updates the new note in notes reference in firebase database
     * @param note - the new note we want to add*/
    public void setNotesInFirebase(Notes note ){
        Map<String,Object> map= note.noteToMap();
        notesRef.child(eventName).child(note.getNoteTitle()).setValue(map);
    }

    /**This Method builderNote - Creates a new dialog for adding a new note.
     * defines a new custom layout for the alert dialog.
     * set cancel and save buttons
     * attach listeners to the save and cancel buttons*/
    public void builderNote() {
        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.alert_box_add_note, null);
        builder.setView(customLayout);
        final EditText et_Note_Title = (EditText) customLayout.findViewById(R.id.et_note_title);
        final EditText et_Note_Description = (EditText) customLayout.findViewById(R.id.et_note_description);
        btn_save = customLayout.findViewById(R.id.btn_add_note_save);
        btn_cancel = customLayout.findViewById(R.id.btn_add_note_cancel);
        final AlertDialog dialog = builder.create();
        // if user clicked save in alert box - check input
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newNoteTitle = et_Note_Title.getText().toString();
                newNoteDescription = et_Note_Description.getText().toString();
                if (TextUtils.isEmpty(newNoteTitle) || TextUtils.isEmpty(newNoteDescription)) {
                    Helper.makeABuilder("שגיאה! חלק מהשדות ריקים",getActivity());
                } else {
                    if(notesList.size()>=0) {
                        int flagOfDup = 0;
                        for (Notes iter : notesList) {
                            if (newNoteTitle.equals(iter.getNoteTitle())) {
                               Helper.makeABuilder( "שגיאה. יש רשימה עם שם זהה במערכת.",getActivity());
                               flagOfDup=1;
                            }
                        }
                        if(!TextUtils.isEmpty(newNoteTitle)) {
                            boolean check = Helper.checkLetters(newNoteTitle, getActivity());
                            if (!check) {
                                if (flagOfDup != 1) {
                                    Notes note = new Notes(newNoteTitle, newNoteDescription, myUsername);
                                    notesList.add(note);
                                    setNotesInFirebase(note);
                                    updateNotesAmountInEvent();
                                    tv2.setVisibility(View.GONE);
                                    tv3.setVisibility(View.GONE);
                                    notifyDataChanged();
                                    dialog.dismiss();
                                    //move fragment
                                }
                            }
                        }
                    }
                }

            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // create and show the alert dialog
        dialog.show();
    }

    /**This Method updateNotesAmountInEvent - updates the count of the notes of the event in firebase database
     * updates in eventsRef. */
    public void updateNotesAmountInEvent(){

        final DatabaseReference eventNoteValue= eventRef.child(eventName).child("notes");
        eventNoteValue.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int amountNotes= Integer.valueOf(String.valueOf(snapshot.getValue().toString().trim()))+1;
                eventNoteValue.setValue(amountNotes);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}