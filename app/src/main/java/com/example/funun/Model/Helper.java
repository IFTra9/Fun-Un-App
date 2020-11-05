package com.example.funun.Model;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.annotation.RequiresApi;

import com.example.funun.Activities.HomeActivity;
import com.example.funun.Activities.RegisterActivity;
import com.example.funun.Adapter.EventAdapterRV;
import com.example.funun.Adapter.GuestsAdapter;
import com.example.funun.Adapter.NotesAdapterRV;
import com.example.funun.Adapter.ProductAdapter;
import com.example.funun.Fragment.EventDetailsFragment;
import com.example.funun.Fragment.GuestsFragment;
import com.example.funun.Fragment.NotesFragment;
import com.example.funun.Fragment.ShoppingList;

import java.lang.reflect.Method;
import java.util.Calendar;
/**This Class Helper - contains static methods for all classes to use. */
public class Helper {

    /**This Method makeABuilder - makes an alert builder with single button
     * @param value - any string*/
    public static void makeABuilder(String value, Activity activity) {
        new android.app.AlertDialog.Builder(activity)
                .setTitle("פנאן")
                .setMessage(value)
                .setPositiveButton("הבנתי", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                }).show();
    }

    /**This Method checkLetters - check and return true if a String contains a character that is not a letter in hebrew/english.
     * @param activity - the context of the fragment that called this method.
     * @param newTitle - the value to check*/
    public static boolean checkLetters(String newTitle, Activity activity) {
        char[] charArray = newTitle.toCharArray();
        int counter = 0;
        for (char c : charArray) {
            if (!((c <= 0x05ea && c >= 0x05d0) || (c <= 'z' && c >= 'a') || (c <= 'Z' && c >= 'A'))) {
                if(!(c==' ')) {
                    counter = 1;
                    break;
                }
            }
        }
        if(counter==1) {
            makeABuilder("אף שדה לא יכול להכיל תווים שהם לא בעברית/אנגלית."+".-+()=*&^%$#@!~',<>[]{}",activity);
            return true;
        }
        return false;
    }

    /**This Method - setTimeDialog - opens a new Time dialog
     * 1. Pick time from the Clock.
     * 2.sets the time into the et_time EditText Item.*/
    public static void setTimeDialog(final TextView et, Activity activity ) {
        final Calendar cld = Calendar.getInstance();
        int hour = cld.get(Calendar.HOUR_OF_DAY);
        int minute = cld.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {

                        String timeEvent =+hourOfDay+":"+minute;
                        timeEvent=timeEvent.replaceAll("\\s","");
                        et.setText(timeEvent);
                    }
                }, hour, minute, false);
        timePickerDialog.show();
    }



    /**This Method - setDateDialog - opens a new date dialog
     * 1. Pick date from the calendar.
     * 2.sets the date into the et_date EditText Item. */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void setDateDialog(final TextView et, Activity activity ) {
        DatePickerDialog picker1;
        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);

        // date picker dialog
        picker1 = new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String setdate = +dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                et.setText(setdate);
            }
        }, year, month, day);
        picker1.show();
    }

    //test to view all the method of a class
    public static void printallEvents(){
        try {
            Class thisClass = ProductAdapter.class;
            Method[] methods = thisClass.getDeclaredMethods();

            for (int i = 0; i < methods.length; i++) {
                String y = methods[i].getName();
                System.out.println("\n"+y);
            }
        } catch (Throwable e) {
        }
    }
}
