package com.example.parkgo;

public class Parking {
    private String id;
    private String userId;
    private String name;
    private String address;
    private int spacesAvailable;
    private String phone;
    private String schedule;
    private double costPerHour;
    private String restrictions;
    private double latitude;
    private double longitude;

    // Constructor vacío requerido para Firestore
    public Parking() {
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getSpacesAvailable() { return spacesAvailable; }
    public void setSpacesAvailable(int spacesAvailable) { this.spacesAvailable = spacesAvailable; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    public double getCostPerHour() { return costPerHour; }
    public void setCostPerHour(double costPerHour) { this.costPerHour = costPerHour; }

    public String getRestrictions() { return restrictions; }
    public void setRestrictions(String restrictions) { this.restrictions = restrictions; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
