package com.example.covid_app.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.sql.Date;

public class HasScreened {

    Citizen citizen_who_has_been_screened;
    String status;
    String screening_date;
    String type_screening;
    String region;
    String department;
    String quarter;
    String city;

    public HasScreened() {
    }

    public Citizen getCitizen_who_has_been_screened() {
        return citizen_who_has_been_screened;
    }

    public void setCitizen_who_has_been_screened(Citizen citizen_who_has_been_screened) {
        this.citizen_who_has_been_screened = citizen_who_has_been_screened;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getScreening_date() {
        return screening_date;
    }

    public void setScreening_date(String screening_date) {
        this.screening_date = screening_date;
    }

    public String getType_screening() {
        return type_screening;
    }

    public void setType_screening(String type_screening) {
        this.type_screening = type_screening;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
