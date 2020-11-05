package com.example.funun.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.funun.Model.Helper;
import com.example.funun.Model.Notes;
import com.example.funun.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**This Class Adapter - NotesAdapterRV - provide a binding between the data,holder and the recycler view.
 *  set the views that are displayed within a RecyclerView.
 *  used also as a controller for the elements in background. */
public class NotesAdapterRV extends RecyclerView.Adapter<NotesAdapterRV.NotesHolder> {

    List<Notes> notesList;
    Context context;
    Fragment one;
    NotesHolder ret;

    //init shared pref files.
    SharedPreferences sharedPref;
    SharedPreferences sharedPref2;
    String eventName;
    String myUsername;
    boolean eventHost;

    //init firebase
    private DatabaseReference eventRef;
    private DatabaseReference notesRef;

    //view elements
    TextView tv3;
    ImageView tv2;

    /**This Constructor NotesAdapterRV - updates and initialize the params from the NotesFragment.
     * initialize view element from frag
     * initialize pointer to shared pref files.
     * @param tv2 - part of view that is visible/gone according to the size of the list.
     * @param tv3 - part of view that is visible/gone according to the size of the list.
     * @param context - the relevant context of app.
     * @param notesLists the notes list from notes fragment.
     * @param frag - the fragment NotesFragment.*/
    public NotesAdapterRV(Context context, List<Notes> notesLists, Fragment frag, ImageView tv2,TextView tv3) {
        this.notesList = notesLists;
        this.context = context;
        one = frag;
        this.tv2=tv2;
        this.tv3=tv3;
        sharedPref= one.getActivity().getSharedPreferences("Event",Context.MODE_PRIVATE);
        sharedPref2= one.getActivity().getSharedPreferences("Username",Context.MODE_PRIVATE);
        eventName=sharedPref.getString("eventName","");
        eventRef= FirebaseDatabase.getInstance().getReference("events").child(eventName);
        notesRef = FirebaseDatabase.getInstance().getReference().child("notes");
        showAllNotes();
        if(notesLists.size()==0) {
            tv2.setVisibility(View.VISIBLE);
            tv3.setVisibility(View.VISIBLE);
        }
        else{
            tv2.setVisibility(View.GONE);
            tv3.setVisibility(View.GONE);
        }

    }

    /**This Method - onCreateViewHolder - Called when RecyclerView needs a new RecyclerView.ViewHolder from the given type to represent an item.*/
    @Override
    public NotesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get LayoutInflater object.


        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        // Inflate the RecyclerView item layout xml.
        View noteItemView = layoutInflater.inflate(R.layout.view_item_note_card, parent, false);
        // Get note title text view object.
        // Create and return our custom note Recycler View Item Holder object.
        ret = new NotesHolder(noteItemView);
        return ret;
    }

    /**This Method -onBindViewHolder - Called by RecyclerView to display the data at the specified position.
     *this method update the content of the RecyclerView.ViewHolder.itemView to reflect the item at the given position.
     *this method initialize the shared pref files.
     * checks if the current user is a host in the event.
     * sets dialog(if needed, and listeners to the notes items.
     * @param position - the position of the item in the list (screen)
     * @param holder  - the holder that holds the view elements. */
    @Override
    public void onBindViewHolder(final NotesHolder holder, final int position) {
        final Notes noteItem = notesList.get(position);
        eventName=sharedPref.getString("eventName","");
        myUsername=sharedPref2.getString("MyUsername","");
        if(sharedPref.contains("eventHost")) eventHost=true;
        else eventHost=false;
        if (noteItem != null) {

            //bind the holder and the data.
            holder.itemView.setTag(noteItem);
            holder.noteTitle.setText(noteItem.getNoteTitle());
            holder.noteUserUpdate.setText(noteItem.getNoteUserUpdate());
            holder.imageEdit.setImageResource(R.drawable.ic_edit_button);
            holder.imageRemove.setImageResource(R.drawable.ic_remove_button);

            //set listeners.
            holder.noteTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buildNoteBuilder(v,position);
                }
            });
            holder.imageRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(eventHost) {
                        removeNotesFirebase(noteItem.getNoteTitle());
                        notesList.remove(notesList.get(position));
                        if(notesList.size()==0) {
                            tv2.setVisibility(View.VISIBLE);
                            tv3.setVisibility(View.VISIBLE);
                        }
                        else{
                            tv2.setVisibility(View.GONE);
                            tv3.setVisibility(View.GONE);
                        }
                        notifyDataSetChanged();
                    }
                    else
                      Helper.makeABuilder("אורחים לא יכולים להסיר רשימות",one.getActivity());
                }
            });
            holder.imageEdit.setOnClickListener(new View.OnClickListener() {
                //if note item was pressed, open dialog and show details.
                @Override
                public void onClick(View v) {
                    // Get note title text.
                    buildNoteBuilder(v, position);
                }
            });
        }
    }

    /**This Method - buildNoteBuilder - Creates a dialog for a specific note and view/change his info.
 * @param position - the position of the item in list (on screen)
 * @param v - the view .*/
    public void buildNoteBuilder(View v, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customLayout = inflater.inflate(R.layout.alert_box_edit_note, null);
        builder.setView(customLayout);

        //init view elements of the custom view.
        final TextView title = (TextView) customLayout.findViewById(R.id.tv_edit_note_title);
        final EditText desc = (EditText) customLayout.findViewById(R.id.et_edit_note_desc);
        Button btn_note_cancel = (Button) customLayout.findViewById(R.id.btn_edit_note_cancel);
        Button btn_note_save = (Button) customLayout.findViewById(R.id.btn_edit_note_save);
        final AlertDialog dialog = builder.create();
        title.setText(notesList.get(position).getNoteTitle());
        desc.setText(notesList.get(position).getNoteDescription());

        //define listeners.
        btn_note_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newDesc = desc.getText().toString();
                Notes newItem = new Notes(notesList.get(position).getNoteTitle(), newDesc, myUsername);
                //change the note details in firebase.
                updateNoteinFirebase(newItem,notesList.get(position),position);
                notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        btn_note_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // create and show the alert dialog
        dialog.show();
    }

    /**This Method - updateNoteUpdatesInFirebase - updates the existing item in firebase database.
     * @param newItem - the item to update that holds the new and updated data.  */
    private void updateNoteinFirebase(final Notes newItem, final Notes oldNote,final int position) {

        notesRef.child(eventName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int exist = 1;
                for(DataSnapshot ds: snapshot.getChildren()){
                    if(ds.getKey().equals(newItem.getNoteTitle())) {
                        if (ds.child("noteDescription").getValue().toString().equals(oldNote.getNoteDescription())) {
                            exist=0;
                        }
                    }
                }
                if(exist==0){
                    Map<String, Object> hash = new HashMap<>();
                    hash.put("noteTitle", newItem.getNoteTitle());
                    hash.put("noteUserUpdate", myUsername);
                    hash.put("noteDescription", newItem.getNoteDescription());
                    //change the details in local list.
                    notesRef.child(eventName).child(newItem.getNoteTitle()).updateChildren(hash)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, "עודכן בהצלחה ", Toast.LENGTH_LONG).show();
                                    notesList.set(position, newItem);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "שגיאה במערכת ", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else Helper.makeABuilder("The event notes was changed/deleted.\n please re-enter the event from all events menu button",one.getActivity());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**This Method - removeNotesFirebase - Removes the values and the product in firebase database.
     * @param noteTitle - the key (note) to remove from database.  */
    private void removeNotesFirebase(final String noteTitle) {
        notesRef.child(eventName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int existNote=0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name =ds.getKey();
                    if (name.equals(noteTitle)) {
                        existNote = 1;
                    }
                }
                if(existNote==1){
                    DatabaseReference delete = notesRef.child(eventName).child(noteTitle);
                    delete.removeValue();
                    removeNoteCountFromEvent();
                }
                else Helper.makeABuilder("Please Refresh the event and try again. \nSome one else has deleteed/changed the event.",one.getActivity());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**This Method - removeNoteCountFromEvent - Update the amount of notes in event firebase database. */
    private void removeNoteCountFromEvent() {
        eventRef.child("notes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int amountNotes = Integer.parseInt(snapshot.getValue().toString());
                amountNotes=amountNotes-1;
                eventRef.child("notes").setValue(amountNotes);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public int getItemCount() {
        int ret = 0;
        if (notesList != null) {
            ret = notesList.size();
        }
        return ret;
    }

    public class NotesHolder extends RecyclerView.ViewHolder {

        private TextView noteTitle;
        private EditText noteUserUpdate;
        private ImageView imageRemove;
        private ImageView imageEdit;


        public NotesHolder(View itemView) {
            super(itemView);
            noteTitle = (TextView) itemView.findViewById(R.id.tv_note_item_title_card);
            noteUserUpdate = (EditText) itemView.findViewById(R.id.tv_item_updated__username);
            imageEdit = (ImageView) itemView.findViewById(R.id.iv_note_edit);
            imageRemove = (ImageView) itemView.findViewById(R.id.iv_note_remove);
        }
    }

    /**This Method - showAllNotes - Creates a local list of all notes from the firebase database*/
    public void showAllNotes() {

        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final int i = Integer.valueOf(String.valueOf(snapshot.child("notes").getValue().toString()));
                //check only if notes exists in event..
                if (i > 0) {
                    notesRef.child(eventName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot snapshot2) {
                            for (final DataSnapshot ds : snapshot2.getChildren()) {
                                    final String name = ds.getKey();
                                    notesList.add(ds.getValue(Notes.class));
                                }
                            if(notesList.size()==0) {
                                tv2.setVisibility(View.VISIBLE);
                                tv3.setVisibility(View.VISIBLE);
                            }
                            else{
                                tv2.setVisibility(View.GONE);
                                tv3.setVisibility(View.GONE);
                            }
                            notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "שגיאה בבסיס הנתונים", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
