package com.example.covid_app.models;

public class SmsModel {


    String userUid;
    String userName;
    String message;
    String sendDate;
    boolean isSMS;
    String voiceUrl;
    int voiceDuration;
    String timeInMilli;

    public SmsModel() {
    }

    public String getTimeInMilli() {
        return timeInMilli;
    }

    public void setTimeInMilli(String timeInMilli) {
        this.timeInMilli = timeInMilli;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public int getVoiceDuration() {
        return voiceDuration;
    }

    public void setVoiceDuration(int voiceDuration) {
        this.voiceDuration = voiceDuration;
    }

    public boolean isSMS() {
        return isSMS;
    }

    public void setSMS(boolean SMS) {
        isSMS = SMS;
    }

    public String getVoiceUrl() {
        return voiceUrl;
    }

    public void setVoiceUrl(String voiceUrl) {
        this.voiceUrl = voiceUrl;
    }

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
