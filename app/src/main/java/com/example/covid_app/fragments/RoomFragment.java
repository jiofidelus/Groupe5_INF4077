package com.example.covid_app.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.covid_app.R;
import com.example.covid_app.adapters.SmsAdapter;
import com.example.covid_app.models.SmsModel;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;


public class RoomFragment extends Fragment {

    Context context;
    AppCompatActivity activity;
    SharedPreferences sharedPref;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    String userName = null;
    private String TAG = "covid_app";

    String USER_PREFS = "user_prefs";
    String USER_NAME_PREFS = "user_name_prefs";
    private Dialog dialog;

    boolean isRecording;

    AppCompatTextView room_user_name;
    RelativeLayout room_send_voice_button_click;
    AppCompatTextView textTitle;
    AppCompatTextView room_recorded_time;
    AppCompatImageView room_record_image;
    RecyclerView room_recycler_view_sms;
    RelativeLayout room_cancel_voice_button_click;
    EditText room_edit_text;
    private RelativeLayout room_send_message_button_click;
    ScrollView room_scroll_view;

    ArrayList<SmsModel> smsList = new ArrayList<>();
    SmsAdapter smsAdapter;
    private boolean logged;
    private String path = null;
    private MediaRecorder mediaRecorder;
    private Handler handlerRecordVoice = new Handler();
    private Runnable runnableRecordVoice;
    private int maxDuration = 0;
    private int voiceDuration = 0;
    private int RECORD_AUDIO_PERMISSION_CODE = 1;

    private FirebaseFirestore db;
    FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    String userUID = "";
    private int oldPosition = -1;
    private int currentPosition = -1;
    private boolean playing = false;
    private Thread threadGetSmsList;
    private boolean canRunThread;
    private Thread threadCheckInteractions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_room, container, false);

        context = result.getContext();
        activity = (AppCompatActivity) context;

        sharedPref = context.getSharedPreferences(
                USER_PREFS, Context.MODE_PRIVATE);

        initView(result);
        getUid();
        checkInteractions();
        setupAdapter();
        canRunThread = true;
        getSmsList();
        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
        canRunThread = true;
        firebaseAuthentification();
    }

    @Override
    public void onResume() {
        super.onResume();
        canRunThread = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        canRunThread = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        canRunThread = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        canRunThread = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        canRunThread = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        canRunThread = false;
    }

    private void getUid(){
        try {
            userUID = Settings.Secure.getString(
                    activity.getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkInteractions() {
        threadCheckInteractions = new Thread(new Runnable() {
            @Override
            public void run() {
                if (canRunThread){
                    room_send_voice_button_click.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkRecordAudioPermissions();
                        }
                    });
                    room_cancel_voice_button_click.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkIfIsRecording();
                            cancelRecordVoice();
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Audio annule !!!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    room_send_message_button_click.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (logged) {
                                String tempSms = room_edit_text.getText().toString();
                                if (!tempSms.isEmpty()) {
                                    room_edit_text.setText(null);
                                    sendChannelMessage(tempSms);
                                }
                            }else {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "Non envoye !!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
        threadCheckInteractions.start();
    }

    private void uploadVoiceToFirebaseStorage(SmsModel smsModel){
        final String randomKey = UUID.randomUUID().toString();
        StorageReference voiceRef = storageReference.child("voices/" + randomKey);
        Uri voiceUri = Uri.fromFile(new File(path));
        StorageTask<UploadTask.TaskSnapshot> uploadTask = voiceRef.putFile(voiceUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        voiceRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                String voiceImageUrl = Objects.requireNonNull(task.getResult()).toString();
                                smsModel.setVoiceUrl(voiceImageUrl);
                                uploadMessage(smsModel);
                            }
                        });
                        deleteVoice();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(activity, "Audio non uploadee", Toast.LENGTH_SHORT).show();
                        deleteVoice();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        //
                    }
                });

    }

    private void deleteVoice(){
        File fileToDelete = new File(path);
        if (fileToDelete.exists()) {
            if (fileToDelete.delete()) {
                Log.w(TAG, "file Deleted");
            } else {
                Log.w(TAG, "file not Deleted");
            }
        }
    }

    private void uploadMessageDataToFireStore(SmsModel smsModel) {
        if (smsModel.isSMS()) {
            uploadMessage(smsModel);
        }else {
            uploadVoiceToFirebaseStorage(smsModel);
        }
    }

    private void uploadMessage(SmsModel smsModel){
        db.collection("messages").add(smsModel)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        hideLoadingDialog();
                        Toast.makeText(activity, "Envoye avec succes", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideLoadingDialog();
                        Toast.makeText(activity, "Echec d'envoie", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    void getSmsList(){
        threadGetSmsList = new Thread(new Runnable() {
            @Override
            public void run() {
                if (canRunThread){
                    db.collection("messages").addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                return;
                            }
                            for (DocumentSnapshot doc : Objects.requireNonNull(value)) {
                                SmsModel smsModel = doc.toObject(SmsModel.class);
                                if (smsModel != null){
                                    tryToAddSms(smsModel);
                                }
                            }
                        }
                    });
                }
            }
        });
        threadGetSmsList.start();
    }

    private void tryToAddSms(SmsModel smsModel) {
        for (SmsModel smsModel1 : smsList){
            if (smsModel1 != null){
                if (smsModel.getUserUid().equals(smsModel1.getUserUid())){
                    if (smsModel.getMessage().equals(smsModel1.getMessage())){
                        if (smsModel.getSendDate().equals(smsModel1.getSendDate())){
                            if (smsModel.getUserName().equals(smsModel1.getUserName())){
                                if (smsModel.getVoiceDuration() == (smsModel1.getVoiceDuration())){
                                    if (smsModel.getVoiceUrl().equals(smsModel1.getVoiceUrl())){
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        smsList.add(smsModel);
        Collections.sort(smsList, new Comparator<SmsModel>() {
            @Override
            public int compare(SmsModel o1, SmsModel o2) {
                return o2.getTimeInMilli().compareTo(o1.getTimeInMilli());
            }
        });
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                smsAdapter.notifyItemChanged(0);
                smsAdapter.notifyItemInserted(smsList.size()-1);
            }
        });
    }

    private void stopAndSendVoice() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            isRecording = false;
            Toast.makeText(context, "Enregistrement termine", Toast.LENGTH_SHORT).show();
            sendChannelVoice();
        }
        maxDuration = 0;
        mediaRecorder = null;
        updateRecordingDuration();
    }

    private void cancelRecordVoice() {
        if (mediaRecorder != null){
            mediaRecorder.stop();
            mediaRecorder.release();
            isRecording = false;
            maxDuration = 0;
            voiceDuration = 0;
            mediaRecorder = null;
            updateRecordingDuration();
        }
    }


    private void recordAudio() {
        voiceDuration = 0;
        String tempFileName = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            android.icu.util.Calendar calendar = android.icu.util.Calendar.getInstance();
            tempFileName += calendar.get(android.icu.util.Calendar.YEAR);
            tempFileName += calendar.get(android.icu.util.Calendar.MONTH);
            tempFileName += calendar.get(android.icu.util.Calendar.DAY_OF_MONTH);
            tempFileName += calendar.get(android.icu.util.Calendar.HOUR);
            tempFileName += calendar.get(android.icu.util.Calendar.MINUTE);
            tempFileName += calendar.get(android.icu.util.Calendar.SECOND);
            tempFileName += calendar.get(android.icu.util.Calendar.MILLISECOND);
        }else {
            java.util.Calendar calendar = null;
            calendar = java.util.Calendar.getInstance();
            tempFileName += calendar.get(java.util.Calendar.YEAR);
            tempFileName += calendar.get(java.util.Calendar.MONTH);
            tempFileName += calendar.get(java.util.Calendar.DAY_OF_MONTH);
            tempFileName += calendar.get(java.util.Calendar.HOUR);
            tempFileName += calendar.get(java.util.Calendar.MINUTE);
            tempFileName += calendar.get(java.util.Calendar.SECOND);
            tempFileName += calendar.get(java.util.Calendar.MILLISECOND);
        }
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString() + tempFileName + ".3gp";
        if (mediaRecorder != null){
            stopAndSendVoice();
        }
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(path);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            runnableRecordVoice = new Runnable() {
                @Override
                public void run() {
                    if (isRecording){
                        maxDuration += 1;
                        voiceDuration = maxDuration;
                        updateRecordingDuration();
                        handlerRecordVoice.postDelayed(this, 1000);
                    }
                }
            };
            //Start
            handlerRecordVoice.postDelayed(runnableRecordVoice, 1000);
        } catch (IOException e) {
            e.printStackTrace();
            mediaRecorder = null;
            checkIfIsRecording();
            Toast.makeText(context, "Erreur d'enregistrement", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRecordingDuration() {
        String tempDuration = "";
        if (isRecording){
            tempDuration = maxDuration+" Sec";
        }
        String finalTempDuration = tempDuration;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                room_recorded_time.setText(finalTempDuration);
            }
        });
    }

    private void checkIfIsRecording() {
        isRecording = !isRecording;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    room_send_message_button_click.setAlpha(0);
                    room_send_message_button_click.setVisibility(View.GONE);
                    room_edit_text.setAlpha(0);
                    room_edit_text.setVisibility(View.GONE);
                    room_recorded_time.setVisibility(View.VISIBLE);
                    room_recorded_time.setAlpha(1);
                    room_record_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_stop_24));
                    room_record_image.setColorFilter(getResources().getColor(R.color.red));
                    room_cancel_voice_button_click.setVisibility(View.VISIBLE);
                    room_cancel_voice_button_click.setAlpha(1);
                }else {
                    room_send_message_button_click.setVisibility(View.VISIBLE);
                    room_send_message_button_click.setAlpha(1);
                    room_edit_text.setVisibility(View.VISIBLE);
                    room_edit_text.setAlpha(1);
                    room_recorded_time.setAlpha(0);
                    room_recorded_time.setVisibility(View.GONE);
                    room_record_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_keyboard_voice_24));
                    room_record_image.setColorFilter(getResources().getColor(R.color.white));
                    room_cancel_voice_button_click.setAlpha(0);
                    room_cancel_voice_button_click.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupAdapter(){
        FlexboxLayoutManager smsLayoutManager = new FlexboxLayoutManager(context);
        smsLayoutManager.setFlexDirection(FlexDirection.ROW);
        smsLayoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                room_recycler_view_sms.setLayoutManager(smsLayoutManager);
                room_recycler_view_sms.setNestedScrollingEnabled(false);
                room_recycler_view_sms.clearOnScrollListeners();
                room_recycler_view_sms.setOverScrollMode(View.OVER_SCROLL_NEVER);
                smsAdapter = new SmsAdapter(context, smsList, userUID, new SmsAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        if (currentPosition >= 0){
                            if (currentPosition != position){
                                if (position >= 0){
                                    smsAdapter.notifyItemChanged(currentPosition);
                                }
                            }
                        }
                        currentPosition = position;
                    }

                    @Override
                    public boolean currentPlaying(int position) {
                        return position == currentPosition;
                    }
                });
                room_recycler_view_sms.setAdapter(smsAdapter);
            }
        });
    }

    private void checkRecordAudioPermissions(){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    RECORD_AUDIO_PERMISSION_CODE
            );
        }else {
            checkIfIsRecording();
            if (isRecording){
                recordAudio();
            }else {
                stopAndSendVoice();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(context, "Permissions acceptees", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(context, "Permissions refusees", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void sendChannelMessage(String msg) {
        showLoadingDialog();
        SmsModel smsModel = new SmsModel();
        smsModel.setUserName(userName);
        smsModel.setMessage(msg);
        smsModel.setSMS(true);
        if (userUID == null){
            userUID = "";
        }
        String time= String.valueOf(System.currentTimeMillis());
        smsModel.setTimeInMilli(time);
        smsModel.setUserUid(userUID);
        smsModel.setVoiceDuration(voiceDuration);
        smsModel.setVoiceUrl("");
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        month = month+1;
        String date = day+"/"+month+"/"+year;
        smsModel.setSendDate(date);
        uploadMessageDataToFireStore(smsModel);
    }

    private void sendChannelVoice(){
        showLoadingDialog();
        SmsModel smsModel = new SmsModel();
        smsModel.setUserName(userName);
        smsModel.setMessage("");
        smsModel.setSMS(false);
        smsModel.setUserUid(userUID);
        String time= String.valueOf(System.currentTimeMillis());
        smsModel.setTimeInMilli(time);
        smsModel.setVoiceDuration(voiceDuration);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        month = month+1;
        String date = day+"/"+month+"/"+year;
        smsModel.setSendDate(date);
        uploadMessageDataToFireStore(smsModel);
    }

    private void logOutRoom() {
        //
    }

    private void initView(View view) {
        textTitle = (AppCompatTextView) view.findViewById(R.id.textTitle);
        room_user_name = (AppCompatTextView) view.findViewById(R.id.room_user_name);
        room_recycler_view_sms = (RecyclerView) view.findViewById(R.id.room_recycler_view_sms);
        room_edit_text = (EditText) view.findViewById(R.id.room_edit_text);
        room_send_message_button_click = (RelativeLayout) view.findViewById(R.id.room_send_message_button_click);
        room_scroll_view = (ScrollView) view.findViewById(R.id.room_scroll_view);
        room_send_voice_button_click = (RelativeLayout) view.findViewById(R.id.room_send_voice_button_click);
        room_recorded_time = (AppCompatTextView) view.findViewById(R.id.room_recorded_time);
        room_record_image = (AppCompatImageView) view.findViewById(R.id.room_record_image);
        room_cancel_voice_button_click = (RelativeLayout) view.findViewById(R.id.room_cancel_voice_button_click);

        OverScrollDecoratorHelper.setUpOverScroll(room_scroll_view);
    }

    void firebaseAuthentification(){
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            if (!currentUser.isAnonymous()) {
                userName = currentUser.getEmail();
            }
            logged = true;
            updateUI();
        }else {
            signInAnonymously();
        }
    }

    private void signInAnonymously() {
        firebaseAuth.signInAnonymously()
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            currentUser = firebaseAuth.getCurrentUser();
                            logged = true;
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showMessages(null);
                                    }
                                });
                                }
                            });
                        }
                    }
                });
    }

    private void updateUI() {
        if (currentUser != null){
            if (currentUser.isAnonymous()){
                getSavedUserName();
                if (userName == null){
                    showNewUserNameDialog();
                }
                if (userName != null){
                    showMessages(userName);
                }
            }else {
                showMessages(userName);
            }
        }
    }

    private void showMessages(String name) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (name != null){
                    room_user_name.setText(name);
                }else {
                    room_user_name.setText("-- Vous etes hors line --");
                }
            }
        });
    }

    void showLoadingDialog(){
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_loading_dialog);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LottieAnimationView loadingAnimation = dialog.findViewById(R.id.loadingAnimation);
                loadingAnimation.playAnimation();
            }
        });
        dialog.show();
    }

    void hideLoadingDialog(){
        if (dialog != null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LottieAnimationView loadingAnimation = dialog.findViewById(R.id.loadingAnimation);
                    loadingAnimation.pauseAnimation();
                }
            });
            dialog.dismiss();
        }
    }

    private void saveNewUserName() {
        if (currentUser.isAnonymous()) {
            sharedPref = context.getSharedPreferences(
                    USER_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(USER_NAME_PREFS, userName);
            editor.apply();
        }
    }

    private void getSavedUserName() {
        if (currentUser.isAnonymous()) {
            userName = sharedPref.getString(USER_NAME_PREFS, null);
        }
    }

    void showNewUserNameDialog(){
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog_user_name);
        final RelativeLayout[] user_name_edit_save_button_click = new RelativeLayout[1];
        final RelativeLayout[] user_name_edit_cancel_button_click = new RelativeLayout[1];
        final EditText[] userNameEditText = new EditText[1];
        final TextView[] user_name_error = new TextView[1];
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userNameEditText[0] = dialog.findViewById(R.id.user_name_edit);
                user_name_error[0] = dialog.findViewById(R.id.user_name_error);
                user_name_error[0].setAlpha(0);
                user_name_edit_save_button_click[0] = dialog.findViewById(R.id.user_name_edit_save_button_click);
                user_name_edit_cancel_button_click[0] = dialog.findViewById(R.id.user_name_edit_cancel_button_click);
            }
        });
        user_name_edit_save_button_click[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showLoadingDialog();
                String tempUsername = userNameEditText[0].getText().toString();
                if(!tempUsername.isEmpty()){
                    if (tempUsername.length() > 3){
                        userName = tempUsername;
                        saveNewUserName();
                    }else {
                        user_name_error[0].setText("Votre nom doit contenir au moins 3 caracteres !!!");
                        user_name_error[0].setAlpha(1);
                    }
                }else {
                    user_name_error[0].setText("Votre nom doit contenir au moins 3 caracteres !!!");
                    user_name_error[0].setAlpha(1);
                }
            }
        });
        user_name_edit_cancel_button_click[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(context, "Aucun nom fourni", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }
}