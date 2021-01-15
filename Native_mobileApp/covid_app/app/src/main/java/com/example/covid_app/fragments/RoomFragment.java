package com.example.covid_app.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

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

import com.airbnb.lottie.LottieAnimationView;
import com.example.covid_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmChannel;
import io.agora.rtm.RtmChannelAttribute;
import io.agora.rtm.RtmChannelListener;
import io.agora.rtm.RtmChannelMember;
import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmClientListener;
import io.agora.rtm.RtmMessage;
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

    RtmChannel mRtmChannel;
    RtmClient mRtmClient;
    String mChannelName = "covid_app_id";
    private String userUid;

    TextView room_user_name;
    RecyclerView room_recycler_view_sms;
    EditText room_edit_text;
    private RelativeLayout room_send_message_button_click;
    ScrollView room_scroll_view;

    boolean logged = false;

    private RtmChannelListener mRtmChannelListener = new RtmChannelListener() {
        @Override
        public void onMemberCountUpdated(int i) {
            //
        }

        @Override
        public void onAttributesUpdated(List<RtmChannelAttribute> list) {
            //
        }

        @Override
        public void onMessageReceived(RtmMessage rtmMessage, RtmChannelMember rtmChannelMember) {
            //
        }

        @Override
        public void onMemberJoined(RtmChannelMember rtmChannelMember) {

        }

        @Override
        public void onMemberLeft(RtmChannelMember rtmChannelMember) {
            //
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_room, container, false);

        context = result.getContext();
        activity = (AppCompatActivity) context;

        initializeAgoraEngine();

        sharedPref = context.getSharedPreferences(
                USER_PREFS, Context.MODE_PRIVATE);

        initView(result);
        checkInteractions();

        return result;
    }

    private void checkInteractions() {
        room_send_message_button_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (logged) {
                    String tempSms = room_edit_text.getText().toString();
                    if (tempSms != null) {
                        if (!tempSms.isEmpty()) {
                            sendChannelMessage(tempSms);
                        }
                    }
                }else {
                    initializeAgoraEngine();
                    showMessages();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuthentification();
    }

    private void initializeAgoraEngine() {
        try {
            mRtmClient = RtmClient.createInstance(context, getString(R.string.agora_app_id),
                    new RtmClientListener() {
                        @Override
                        public void onConnectionStateChanged(int i, int i1) {
                            //
                        }

                        @Override
                        public void onMessageReceived(RtmMessage rtmMessage, String s) {
                            //
                        }

                        @Override
                        public void onTokenExpired() {
                            //
                        }
                    }
            );
        } catch (Exception e) {
            Log.d(TAG, "RTM SDK initialization fatal error!");
            throw new RuntimeException("You need to check the RTM initialization process.");
        }
    }

    private void loginAgora(String userId){
        mRtmClient.login(null, userId, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                initChannel();
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                hideLoadingDialog();
                Log.e(TAG, errorInfo+"");
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Impossible de se connecter au salon", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void initChannel() {
        try {
            mRtmChannel = mRtmClient.createChannel(mChannelName, mRtmChannelListener);
        } catch (RuntimeException e) {
            Log.e(TAG, "Fails to create channel. Maybe the channel ID is invalid," +
                    " or already in use. See the API Reference for more information.");
        }

        mRtmChannel.join(new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                Log.d(TAG, "Successfully joins the channel!");
                logged = true;
                hideLoadingDialog();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Vous etes entre dans le salon", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                Log.d(TAG, "join channel failure! errorCode = "
                        + errorInfo.getErrorCode());
                hideLoadingDialog();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Impossible d'entrer dans le salon", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void sendChannelMessage(String msg) {
        RtmMessage message = mRtmClient.createMessage();
        message.setText(msg);

        mRtmChannel.sendMessage(message, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Message envoye avec succes", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Echec d'envoie du message", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void logOutAgora() {
        mRtmClient.logout(null);
    }
    private void initView(View view) {
        room_user_name = (TextView) view.findViewById(R.id.room_user_name);
        room_recycler_view_sms = (RecyclerView) view.findViewById(R.id.room_recycler_view_sms);
        room_edit_text = (EditText) view.findViewById(R.id.room_edit_text);
        room_send_message_button_click = (RelativeLayout) view.findViewById(R.id.room_send_message_button_click);
        room_scroll_view = (ScrollView) view.findViewById(R.id.room_scroll_view);

        OverScrollDecoratorHelper.setUpOverScroll(room_scroll_view);
    }

    void firebaseAuthentification(){
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {

            if (!currentUser.isAnonymous()) {
                userName = currentUser.getEmail();
            }
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
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "Echec d'envoie du message", Toast.LENGTH_SHORT).show();
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
                    showMessages();
                }
            }else {
                showMessages();
            }
        }
    }

    private void showMessages() {
        room_user_name.setText(userName);
        userUid = currentUser.getUid();
        loginAgora(userUid);
    }

    void showLoadingDialog(){
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_loading_dialog);
        LottieAnimationView loadingAnimation = dialog.findViewById(R.id.loadingAnimation);
        loadingAnimation.playAnimation();
        dialog.show();
    }

    void hideLoadingDialog(){
        if (dialog != null){
            LottieAnimationView loadingAnimation = dialog.findViewById(R.id.loadingAnimation);
            loadingAnimation.pauseAnimation();
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
        EditText userNameEditText = dialog.findViewById(R.id.user_name_edit);
        TextView user_name_error = dialog.findViewById(R.id.user_name_error);
        user_name_error.setAlpha(0);
        RelativeLayout user_name_edit_save_button_click = dialog.findViewById(R.id.user_name_edit_save_button_click);
        RelativeLayout user_name_edit_cancel_button_click = dialog.findViewById(R.id.user_name_edit_cancel_button_click);
        user_name_edit_save_button_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showLoadingDialog();
                String tempUsername = userNameEditText.getText().toString();
                if (tempUsername != null){
                    if(!tempUsername.isEmpty()){
                        if (tempUsername.length() > 3){
                            userName = tempUsername;
                            saveNewUserName();
                        }else {
                            user_name_error.setText("Votre nom doit contenir au moins 3 caracteres !!!");
                            user_name_error.setAlpha(1);
                        }
                    }else {
                        user_name_error.setText("Votre nom doit contenir au moins 3 caracteres !!!");
                        user_name_error.setAlpha(1);
                    }
                }else {
                    user_name_error.setText("Votre nom doit contenir au moins 3 caracteres !!!");
                    user_name_error.setAlpha(1);
                }
            }
        });
        user_name_edit_cancel_button_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(context, "Aucun nom fourni", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

}