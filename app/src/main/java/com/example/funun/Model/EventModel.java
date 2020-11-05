package com.example.funun.Model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map;
import java.util.Objects;

/**This class EventModel represent the Event Model in firebase database. */
public class EventModel implements Serializable {
    // Save event name.
    private String eventName;
    private String eventDate;
    private String eventTime;
    private String eventLocation;
    private String eventPhoto;
    private HashMap<String,Object> guests;
    private HashMap<String,Object> hosts;
    private int products;
    private int notes;
    private float sum;

    public float getSum() {
        return sum;
    }

    public void setSum(float sum) {
        this.sum = sum;
    }

    public EventModel(String eventName, String eventDate, String eventTime, String eventLocation, String eventPhoto, HashMap<String, Object> guests,
                      HashMap<String, Object> hosts, int products, int notes) {
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.eventLocation = eventLocation;
        this.eventPhoto = eventPhoto;
        this.guests = guests;
        this.hosts = hosts;
        this.sum = 0;
    }

    public EventModel(){}


    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getEventPhoto() {
        return eventPhoto;
    }

    public void setEventPhoto(String eventPhoto) {
        this.eventPhoto = eventPhoto;
    }

    public HashMap<String, Object> toMapNew() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("eventName", this.eventName);
        result.put("eventTime", this.eventTime);
        result.put("eventDate", this.eventDate);
        result.put("eventLocation", this.eventLocation);
        result.put("eventPhoto", this.eventPhoto);
        return result;
    }
    public HashMap<String, Object> toMapEvent() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("eventName", this.eventName);
        result.put("eventTime", this.eventTime);
        result.put("eventDate", this.eventDate);
        result.put("eventLocation", this.eventLocation);
        result.put("eventPhoto", this.eventPhoto);
        result.put("products",this.products);
        result.put("notes",this.notes);
        result.put("guests",this.guests);
        result.put("hosts",this.hosts);
        result.put("sum",this.sum);
        return result;
    }

    public int getProducts() {
        return products;
    }

    public void setProducts(int products) {
        this.products = products;
    }

    public int getNotes() {
        return notes;
    }

    public void setNotes(int notes) {
        this.notes = notes;
    }

    public HashMap<String, Object> getGuests() {
        return guests;
    }

    public void setGuests(HashMap<String, Object> guests) {
        this.guests = guests;
    }

    public HashMap<String, Object> getHosts() {
        return hosts;
    }

    public void setHosts(HashMap<String, Object> hosts) {
        this.hosts = hosts;
    }

}
