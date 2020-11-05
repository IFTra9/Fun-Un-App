package com.example.funun.Model;


import java.util.HashMap;
import java.util.Map;

/**This class Notes represent the Notes Model in firebase database. */

public class Notes {

    String noteTitle;
    String noteUserUpdate;
    String noteDescription;

    public Notes(){}
    public Notes(String noteTitle, String noteDescription, String noteUserUpdate) {
        this.noteTitle = noteTitle;
        this.noteUserUpdate = noteUserUpdate;
        this.noteDescription = noteDescription;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getNoteUserUpdate() {
        return noteUserUpdate;
    }

    public void setNoteUserUpdate(String noteUserUpdate) {
        this.noteUserUpdate = noteUserUpdate;
    }

    public String getNoteDescription() {
        return noteDescription;
    }

    public void setNoteDescription(String noteDescription) {
        this.noteDescription = noteDescription;
    }

    public Map<String, Object> noteToMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("noteTitle", this.noteTitle);
        result.put("noteDescription", this.noteDescription);
        result.put("noteUserUpdate", this.noteUserUpdate);
        return result;
    }

}
