package com.example.covid_app.fragments;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid_app.R;
import com.example.covid_app.adapters.PreventionAdapter;
import com.example.covid_app.adapters.SymptomAdapter;
import com.example.covid_app.models.Prevention;
import com.example.covid_app.models.SensibilisationMessage;
import com.example.covid_app.models.Symptom;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class HomeFragment extends Fragment {

    Context context;
    AppCompatActivity activity;

    private FirebaseFirestore db;

    RecyclerView preventionRecyclerView;
    RecyclerView symptomRecyclerView;
    RecyclerView sensibilisationRecyclerView;
    PreventionAdapter preventionAdapter;
    SymptomAdapter symptomAdapter;

    RelativeLayout callButtonClick;
    RelativeLayout smsButtonClick;

    ConstraintLayout anotherTestConstraint;
    ConstraintLayout testConstraint;

    private RelativeLayout testButtonClick;
    private RelativeLayout anotherTestButtonClick;

    AppCompatTextView message;

    AppCompatTextView newsSensibilisations;

    ArrayList<Prevention> preventionList = new ArrayList<>();
    ArrayList<Symptom> symptomList = new ArrayList<>();

    ScrollView scrollView;

    RelativeLayout bottomRelative;

    Dialog smsDialog;
    Dialog callDialog;
    private int ORANGE_NUMBER = 690000000;
    private int MTN_NUMBER = 670000000;

    private int CALL_PERMISSION_CODE = 2;
    private int SMS_PERMISSION_CODE = 1;

    Thread getThread;
    boolean canRunThread = false;
    private SharedPreferences sharedPref;
    String USER_PREFS = "user_prefs";
    String TEST_RESULT_DATA = "test_result_data";
    String MESSAGE = "message_data";
    int testResult = -1;
    private String oldMessage = "";
    private String messageResult = "";
    private String CHANNEL_ID = "covid_app_message_00";
    private NotificationManagerCompat mNotificationManager;

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        canRunThread = true;
        // Inflate the layout for this fragment
        View result =  inflater.inflate(R.layout.fragment_home, container, false);

        context = result.getContext();
        activity = (AppCompatActivity) context;

        initView(result);

        return result;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bottomRelative.startAnimation(AnimationUtils.loadAnimation(context, R.anim.alpha_0));
        symptomRecyclerView.setNestedScrollingEnabled(false);
        sensibilisationRecyclerView.setNestedScrollingEnabled(false);
        preventionRecyclerView.setNestedScrollingEnabled(false);
        OverScrollDecoratorHelper.setUpOverScroll(scrollView);
        getThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (canRunThread){
                    sharedPref = context.getSharedPreferences(
                            USER_PREFS, Context.MODE_PRIVATE);
                    getSavedData();
                    if (testResult >= 0){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                anotherTestConstraint.setVisibility(View.VISIBLE);
                                anotherTestConstraint.setAlpha(1);
                                testConstraint.setAlpha(0);
                                testConstraint.setVisibility(View.GONE);
                            }
                        });
                    }else {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                testConstraint.setVisibility(View.VISIBLE);
                                testConstraint.setAlpha(1);
                                anotherTestConstraint.setAlpha(0);
                                anotherTestConstraint.setVisibility(View.GONE);
                            }
                        });
                    }
                    showCurrentMessage();
                    getPreventionList();
                    getSymptomList();
                    getSensibilisationList();
                    bottomRelative.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fall_down));
                    checkInteractions();
                    setupAdapter();
                }
            }
        });
        getThread.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE){
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(context, "Permissions d'envoie des sms acceptees", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context, "Permissions d'envoie des sms refusees", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == CALL_PERMISSION_CODE){
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(context, "Permissions d'appel acceptees", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context, "Permissions d'appel refusees", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkInteractions() {
        testButtonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });
        anotherTestButtonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });
        callButtonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_CODE);
                }else {
                    showCallDialog(true);
                }
            }
        });

        smsButtonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
                }else {
                    showSmsDialog(true);
                }
            }
        });
    }

    private void saveTestResult(int percentage){
        sharedPref = context.getSharedPreferences(
                USER_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(TEST_RESULT_DATA, percentage);
        editor.apply();
    }

    private void getSavedData(){
        testResult = sharedPref.getInt(TEST_RESULT_DATA, -1);
        messageResult = sharedPref.getString(MESSAGE, "");
    }

    private void sendSms(String destinationNumber, String tempSms) {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }else {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(destinationNumber, null, tempSms, null, null);
            Toast.makeText(context, "Sms envoye !", Toast.LENGTH_SHORT).show();
        }
    }

    private void call(String destinationNumber){
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_CODE);
        }else {
            hideCallDialog();
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:"+destinationNumber));
            startActivity(intent);
        }
    }

    private void showSmsDialog(boolean b) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (smsDialog == null){
                    smsDialog = new Dialog(activity);
                    smsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    smsDialog.setCancelable(false);
                    smsDialog.setContentView(R.layout.custom_dialog_sms);
                    AppCompatEditText send_sms_destination_edit = smsDialog.findViewById(R.id.send_sms_destination_edit);
                    String tempDefaultNumber = ORANGE_NUMBER + "";
                    send_sms_destination_edit.setText(tempDefaultNumber);
                    AppCompatEditText send_sms_edit = smsDialog.findViewById(R.id.send_sms_edit);
                    AppCompatTextView send_sms_error = smsDialog.findViewById(R.id.send_sms_error);
                    RelativeLayout send_sms_button_click = smsDialog.findViewById(R.id.send_sms_button_click);
                    RelativeLayout send_sms_cancel_button_click = smsDialog.findViewById(R.id.send_sms_cancel_button_click);
                    send_sms_button_click.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String tempNumber = send_sms_destination_edit.getText().toString();
                            if (tempNumber.isEmpty()) {
                                send_sms_error.setText("Numero invalide !!!");
                                send_sms_error.setAlpha(1);
                            } else {
                                send_sms_error.setAlpha(0);
                                String tempSms = send_sms_edit.getText().toString();
                                if (tempSms.isEmpty()) {
                                    send_sms_error.setText("Message vide !!!");
                                    send_sms_error.setAlpha(1);
                                } else {
                                    send_sms_error.setAlpha(0);
                                    hideSmsDialog();
                                    sendSms(tempNumber, tempSms);
                                }
                            }
                        }
                    });
                    send_sms_cancel_button_click.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hideSmsDialog();
                        }
                    });
                    smsDialog.show();
                }
            }
        });
    }

    private void showCallDialog(boolean b) {
        if (callDialog == null) {
            callDialog = new Dialog(activity);
            callDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            callDialog.setCancelable(false);
            callDialog.setContentView(R.layout.custom_dialog_call);
            AppCompatEditText call_edit = callDialog.findViewById(R.id.call_edit);
            String tempDefaultNumber2 = ORANGE_NUMBER + "";
            call_edit.setText(tempDefaultNumber2);
            AppCompatTextView call_error = callDialog.findViewById(R.id.call_error);
            RelativeLayout call_button_click = callDialog.findViewById(R.id.call_button_click);
            RelativeLayout call_cancel_button_click = callDialog.findViewById(R.id.call_cancel_button_click);
            call_button_click.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tempNumber = call_edit.getText().toString();
                    if (tempNumber.isEmpty()) {
                        call_error.setText("Numero invalide !!!");
                        call_error.setAlpha(1);
                    } else {
                        call_error.setAlpha(0);
                        hideCallDialog();
                        call(tempNumber);
                    }
                }
            });
            call_cancel_button_click.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideCallDialog();
                }
            });
            callDialog.show();
        }
    }

    private void hideSmsDialog() {
        if (smsDialog != null){
            smsDialog.dismiss();
        }
        smsDialog = null;
    }

    private void hideCallDialog() {
        if (callDialog != null){
            callDialog.dismiss();
        }
        callDialog = null;
    }

    void initView(View view){
        preventionRecyclerView = view.findViewById(R.id.preventionRecyclerView);
        symptomRecyclerView = view.findViewById(R.id.symptomRecyclerView);
        sensibilisationRecyclerView = view.findViewById(R.id.sensibilisationRecyclerView);
        scrollView = view.findViewById(R.id.scrollView);
        callButtonClick = view.findViewById(R.id.callButtonClick);
        smsButtonClick = view.findViewById(R.id.smsButtonClick);
        bottomRelative = view.findViewById(R.id.bottomRelative);

        anotherTestConstraint = view.findViewById(R.id.anotherTestConstraint);
        testConstraint = view.findViewById(R.id.testConstraint);


        anotherTestButtonClick = view.findViewById(R.id.anotherTestButtonClick);
        testButtonClick = view.findViewById(R.id.testButtonClick);

        newsSensibilisations = view.findViewById(R.id.newsSensibilisations);
        message = view.findViewById(R.id.message);
    }

    void setupAdapter(){
        FlexboxLayoutManager preventionLayoutManager = new FlexboxLayoutManager(context);
        preventionLayoutManager.setFlexDirection(FlexDirection.ROW);
        preventionLayoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                preventionRecyclerView.setLayoutManager(preventionLayoutManager);
                preventionAdapter = new PreventionAdapter(context, preventionList);
                preventionRecyclerView.setAdapter(preventionAdapter);
            }
        });

        FlexboxLayoutManager symptomLayoutManager = new FlexboxLayoutManager(context);
        symptomLayoutManager.setFlexDirection(FlexDirection.ROW);
        symptomLayoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                symptomRecyclerView.setLayoutManager(symptomLayoutManager);
                symptomAdapter = new SymptomAdapter(context, symptomList);
                symptomRecyclerView.setAdapter(symptomAdapter);
            }
        });

//        FlexboxLayoutManager sensibilisationLayoutManager = new FlexboxLayoutManager(context);
//        sensibilisationLayoutManager.setFlexDirection(FlexDirection.ROW);
//        sensibilisationLayoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                sensibilisationRecyclerView.setLayoutManager(sensibilisationLayoutManager);
//                SensibilisationAdapter sensibilisationAdapter = new SensibilisationAdapter(context, sensibilisationList);
//                sensibilisationRecyclerView.setAdapter(sensibilisationAdapter);
//            }
//        });
    }

    private void getSensibilisationList() {
        db.collection("sensibilisation").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                for (DocumentSnapshot doc : value) {
                    SensibilisationMessage sensibilisationMessage = doc.toObject(SensibilisationMessage.class);
                    if (sensibilisationMessage != null){
                        if (sensibilisationMessage.getMessage() != null){
                            if (!sensibilisationMessage.getMessage().isEmpty()){
                                String currentMessage = "";
                                currentMessage = sensibilisationMessage.getMessage();
                                if (!currentMessage.equals(messageResult)){
                                    oldMessage = messageResult;
                                    messageResult = currentMessage;
                                    saveCurrentMessage();
                                    showCurrentMessage();
                                    showNotification(currentMessage);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void saveCurrentMessage() {
        sharedPref = context.getSharedPreferences(
                USER_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(MESSAGE, messageResult);
        editor.apply();
    }

    private void showCurrentMessage(){
        if (!Objects.equals(messageResult, oldMessage)){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (messageResult.isEmpty()){
                        message.setText("Aucun conseil pour l'instant !!!");
                    }else {
                        message.setText(messageResult);
                    }
                }
            });
        }
    }

    private void showNotification(String currentMessage) {
        mNotificationManager = NotificationManagerCompat.from(activity);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity)
                .setContentTitle("Nouveau message de sensibilisation")
                .setContentText(currentMessage)
                .setSmallIcon(R.drawable.ic_coronavirus_icon_app)
                .setChannelId(CHANNEL_ID)
                .setAutoCancel(true);
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT > 26) {
            NotificationChannel channel = null;
            channel = new NotificationChannel(CHANNEL_ID, "covid_app", NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }

        manager.notify(0, builder.build());
    }

    void getPreventionList(){
        Prevention prevention = new Prevention();
        prevention.setTitle("Portez un masque");
        prevention.setImage(null);
        preventionList.add(prevention);

        prevention = new Prevention();
        prevention.setTitle("Desinfectez frequemment les mains");
        prevention.setImage(null);
        preventionList.add(prevention);

        prevention = new Prevention();
        prevention.setTitle("Toussez sous le pli du coude");
        prevention.setImage(null);
        preventionList.add(prevention);

        prevention = new Prevention();
        prevention.setTitle("Restez chez vous");
        prevention.setImage(null);
        preventionList.add(prevention);

        prevention = new Prevention();
        prevention.setTitle("Distanciation d'au moins 1 metres");
        prevention.setImage(null);
        preventionList.add(prevention);
    }

    void getSymptomList(){
        Symptom symptom = new Symptom();
        symptom.setTitle("Frequent : fievre");
        symptom.setImage(null);
        symptomList.add(symptom);

//        symptom = new Symptom();
//        symptom.setTitle("Frequent : toux seche");
//        symptom.setImage(null);
//        symptomList.add(symptom);

        symptom = new Symptom();
        symptom.setTitle("Frequent : fatigue");
        symptom.setImage(null);
        symptomList.add(symptom);

//        symptom = new Symptom();
//        symptom.setTitle("Moins frequent : courbatures");
//        symptom.setImage(null);
//        symptomList.add(symptom);

        symptom = new Symptom();
        symptom.setTitle("Moins frequent : maux de gorge");
        symptom.setImage(null);
        symptomList.add(symptom);

        symptom = new Symptom();
        symptom.setTitle("Moins frequent : maux de tete");
        symptom.setImage(null);
        symptomList.add(symptom);

//        symptom = new Symptom();
//        symptom.setTitle("Moins frequent : perte d'odorat ou du gout");
//        symptom.setImage(null);
//        symptomList.add(symptom);

        symptom = new Symptom();
        symptom.setTitle("Grave : difficultes a respirer");
        symptom.setImage(null);
        symptomList.add(symptom);
//
//        symptom = new Symptom();
//        symptom.setTitle("Grave : douleurs au niveau de la poitrine");
//        symptom.setImage(null);
//        symptomList.add(symptom);
//
//        symptom = new Symptom();
//        symptom.setTitle("Grave : perte de motricite");
//        symptom.setImage(null);
//        symptomList.add(symptom);
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
}