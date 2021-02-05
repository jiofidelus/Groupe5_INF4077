package com.example.covid_app.fragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.covid_app.R;
import com.example.covid_app.adapters.CitizenAdapter;
import com.example.covid_app.adapters.CustomCitizenSpinnerAdapter;
import com.example.covid_app.adapters.HasScreenedAdapter;
import com.example.covid_app.models.Citizen;
import com.example.covid_app.models.HasScreened;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class
ConnectedFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private ScrollView connectedScrollView;
    private AppCompatActivity activity;
    private Context context;

    TextView accountMail;
    SwitchCompat listCitizenSwitch;

    RelativeLayout newCitizenButtonClick;
    RelativeLayout newHasScreenedButtonClick;
    RelativeLayout sensibilisationButtonClick;

    RelativeLayout citizenRelativeList;
    RelativeLayout hasScreenedRelativeList;

    ArrayList<HasScreened> hasScreenedList;
    RecyclerView hasScreenedRecyclerView;
    HasScreenedAdapter hasScreenedAdapter;
    ArrayList<Citizen> citizenList;
    RecyclerView citizenRecyclerView;
    CitizenAdapter citizenAdapter;

    private Dialog newCitizenDialog;
    private Dialog newHasScreenedDDialog;
    private BottomSheetDialog bottomSheetDialog;
    private DatePickerDialog datePickerDialog;
    private DatePickerDialog.OnDateSetListener onDateSetListener;
    private Dialog dialog;
    private LottieAnimationView loadingAnimation;
    private int CAMERA_PERMISSION_CODE = 100;

    Citizen citizen = new Citizen();
    String citizenProfilePicturePath = null;
    String citizenProfilePictureDownloadUrl = null;
    private SimpleDraweeView registerCitizenPicture;
    private HasScreened hasScreened;
    private int LOCATION_PERMISSION_CODE = 200;
    private LottieAnimationView topLoadingAnimation;


    CardView logOutButton;
    RelativeLayout logOutButtonClick;
    private boolean canRunThread = true;
    private String sensibilisationId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_connected, container, false);

        context = result.getContext();
        activity = (AppCompatActivity) getContext();

        initViews(result);
        return result;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        canRunThread = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (canRunThread){
                    hideLogOutButton();
                    hideLoadingDialog();
                    OverScrollDecoratorHelper.setUpOverScroll(connectedScrollView);
                    firebaseAuthentification();
                    checkInteractions();
                    setupAdapter();
                    getCitizenList();
                    getHasScreenedList();
                }
            }
        }).start();
    }

    void initViews(View view){
        connectedScrollView = view.findViewById(R.id.connectedScrollView);

        listCitizenSwitch = view.findViewById(R.id.listCitizenSwitch);

        newCitizenButtonClick = view.findViewById(R.id.newCitizenButtonClick);
        newHasScreenedButtonClick = view.findViewById(R.id.newHasScreenedButtonClick);
        logOutButtonClick = view.findViewById(R.id.logOutButtonClick);
        logOutButton = view.findViewById(R.id.logOutButton);
        sensibilisationButtonClick = view.findViewById(R.id.sensibilisationButtonClick);

        citizenRecyclerView = view.findViewById(R.id.citizenRecyclerView);
        hasScreenedRecyclerView = view.findViewById(R.id.hasScreenedRecyclerView);

        accountMail = view.findViewById(R.id.accountMail);

        citizenRelativeList = view.findViewById(R.id.citizenRelativeList);
        hasScreenedRelativeList = view.findViewById(R.id.hasScreenedRelativeList);
        topLoadingAnimation = view.findViewById(R.id.topLoadingAnimation);
    }

    private void uploadImageToFirebaseStorage(){
        final String randomKey = UUID.randomUUID().toString();
        StorageReference profileRef = storageReference.child("images/" + randomKey);
        Uri imageUri = Uri.fromFile(new File(citizenProfilePicturePath));
        StorageTask<UploadTask.TaskSnapshot> uploadTask = profileRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        profileRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                String profileImageUrl = Objects.requireNonNull(task.getResult()).toString();
                                citizen.setUrlPicture(profileImageUrl);
                                uploadCitizenDataToFireStore();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(activity, "Image non uploadee", Toast.LENGTH_SHORT).show();
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

    private void uploadCitizenDataToFireStore() {
        db.collection("citizens").add(citizen)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        hideLoadingDialog();
                        Toast.makeText(activity, "Enregistre avec succes", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideLoadingDialog();
                        Toast.makeText(activity, "Echec d'enregistrement", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadHasScreenedDataToFireStore() {
        db.collection("hasscreeneds").add(hasScreened)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        hideLoadingDialog();
                        Toast.makeText(activity, "Enregistre avec succes", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideLoadingDialog();
                        Toast.makeText(activity, "Echec d'enregistrement", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkInteractions() {
        listCitizenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            citizenRelativeList.setVisibility(View.INVISIBLE);
                            listCitizenSwitch.setText("Depistes");
                            hasScreenedRelativeList.setVisibility(View.VISIBLE);
                        }
                    });
                }else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hasScreenedRelativeList.setVisibility(View.INVISIBLE);
                            listCitizenSwitch.setText("Citoyens");
                            citizenRelativeList.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
        newCitizenButtonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewCitizenDialog();
            }
        });
        newHasScreenedButtonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewHasScreenedDialog();
            }
        });
        logOutButtonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOutFirebase();
            }
        });
        sensibilisationButtonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensibilisation();
            }
        });
    }

    private void sensibilisation() {
        if (dialog == null){
            dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.custom_dialog_sensibilise);
            RelativeLayout send_sms_button_click = dialog.findViewById(R.id.send_sms_button_click);
            RelativeLayout send_sms_cancel_button_click = dialog.findViewById(R.id.send_sms_cancel_button_click);
            AppCompatTextView send_sms_error = dialog.findViewById(R.id.send_sms_error);
            AppCompatEditText send_sms_edit = dialog.findViewById(R.id.send_sms_edit);
            send_sms_button_click.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tempSms = send_sms_edit.getText().toString();
                    if (tempSms.isEmpty()) {
                        send_sms_error.setText("Message invalide !!!");
                        send_sms_error.setAlpha(1);
                    } else {
                        send_sms_error.setAlpha(0);
                        dialog.dismiss();
                        dialog = null;
                        updateSensibilisationMessage(tempSms);
                    }
                }
            });
            send_sms_cancel_button_click.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    dialog = null;
                }
            });
            dialog.show();
        }
    }

    private void updateSensibilisationMessage(String message) {
        showTopLoadingDialog();
        db.collection("sensibilisation")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())){
                                sensibilisationId = documentSnapshot.getId();
                            }
                            if (sensibilisationId != null){
                                db.collection("sensibilisation")
                                        .document(sensibilisationId)
                                        .update("message", message)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                hideTopLoadingDialog();
                                                Toast.makeText(activity, "Envoye avec succes", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }else {
                                hideTopLoadingDialog();
                                Toast.makeText(activity, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void logOutFirebase() {
        if (dialog == null){
            dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.custom_log_out_alert);
            RelativeLayout log_out_button_click = dialog.findViewById(R.id.log_out_button_click);
            RelativeLayout log_out_cancel_button_click = dialog.findViewById(R.id.log_out_cancel_button_click);
            log_out_button_click.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    dialog = null;
                    FirebaseAuth.getInstance()
                            .signOut();
                    Toast.makeText(activity, "Deconnecte", Toast.LENGTH_SHORT).show();
                    FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                    ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
                    ft.replace(R.id.fragment, new ConnectedFragment());
                    ft.commit();
                }
            });
            log_out_cancel_button_click.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    dialog = null;
                }
            });
            dialog.show();
        }
    }

    void showMap(){
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        ft.add(R.id.fragmentActivity, new MapFragment()).addToBackStack(null);
        ft.commit();
    }

    void firebaseAuthentification(){
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            accountMail.setText(currentUser.getEmail());
            showLogOutButton();
        }
    }

    void setupAdapter(){
        citizenList = new ArrayList<>();
        hasScreenedList = new ArrayList<>();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FlexboxLayoutManager citizenLayoutManager = new FlexboxLayoutManager(context);
                citizenLayoutManager.setFlexDirection(FlexDirection.COLUMN);
                citizenLayoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
                citizenRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                citizenRecyclerView.setLayoutManager(citizenLayoutManager);
                citizenAdapter = new CitizenAdapter(activity, context, citizenList);
                citizenRecyclerView.setAdapter(citizenAdapter);

                FlexboxLayoutManager hasScreenedLayoutManager = new FlexboxLayoutManager(context);
                hasScreenedLayoutManager.setFlexDirection(FlexDirection.COLUMN);
                hasScreenedLayoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
                hasScreenedRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                hasScreenedRecyclerView.setLayoutManager(hasScreenedLayoutManager);
                hasScreenedAdapter = new HasScreenedAdapter(context, hasScreenedList);
                hasScreenedRecyclerView.setAdapter(hasScreenedAdapter);
            }
        });
    }

    void getCitizenList(){
        db.collection("citizens").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                showTopLoadingDialog();
                citizenList.clear();
                for (DocumentSnapshot doc : value) {
                    Citizen citizen = doc.toObject(Citizen.class);
                    tryToAddCitizen(citizen);
                }
                hideTopLoadingDialog();
            }
        });
    }

    void getHasScreenedList(){
        db.collection("hasscreeneds").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                showTopLoadingDialog();
                hasScreenedList.clear();
                for (DocumentSnapshot doc : value) {
                    HasScreened hasScreened = doc.toObject(HasScreened.class);
                    tryToAddHasScreened(hasScreened);
                }
                hideTopLoadingDialog();
            }
        });
    }


    private void tryToAddHasScreened(HasScreened hasScreened) {
        for (HasScreened hasScreened1 : hasScreenedList){
            if (hasScreened1 != null){
                if (hasScreened.getStatus().equals(hasScreened1.getStatus())){
                    if (hasScreened.getRegion().equals(hasScreened1.getRegion())){
                        if (hasScreened.getCity().equals(hasScreened1.getCity())){
                            if (hasScreened.getDepartment().equals(hasScreened1.getDepartment())){
                                if (hasScreened.getQuarter().equals(hasScreened1.getQuarter())){
                                    if (hasScreened.getScreening_date().equals(hasScreened1.getScreening_date())){
                                        if (hasScreened.getType_screening().equals(hasScreened1.getType_screening())){
                                           if (hasScreened.getCitizen_who_has_been_screened() != null && hasScreened1.getCitizen_who_has_been_screened() != null){
                                               if (hasScreened.getCitizen_who_has_been_screened().equals(hasScreened1.getCitizen_who_has_been_screened())){
                                                   return;
                                               }
                                           }else if(hasScreened.getCitizen_who_has_been_screened() == null && hasScreened1.getCitizen_who_has_been_screened() == null){
                                               return;
                                           }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        hasScreenedList.add(hasScreened);
        hasScreenedAdapter.notifyItemInserted(hasScreenedList.size()-1);
    }

    private void tryToAddCitizen(Citizen citizen) {
        for (Citizen citizen1 : citizenList){
            if (citizen1 != null){
                if (citizen.getSecondName().equals(citizen1.getSecondName())){
                    if (citizen.getBirthday().equals(citizen1.getBirthday())){
                        if (citizen.getFirstName().equals(citizen1.getFirstName())){
                            if (citizen.getGender().equals(citizen1.getGender())){
                                if (citizen.getIdentityCard().equals(citizen1.getIdentityCard())){
                                    if (citizen.getMobilePhone() == citizen1.getMobilePhone()){
                                        if (citizen.getNationality().equals(citizen1.getNationality())){
                                            if (citizen.getRegisterDate().equals(citizen1.getRegisterDate())){
                                                if (citizen.getUrlPicture().equals(citizen1.getUrlPicture())){
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        citizenList.add(citizen);
        citizenAdapter.notifyItemInserted(citizenList.size()-1);
    }

    void showLoadingDialog(){
        if (dialog == null){
            dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.custom_loading_dialog);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LottieAnimationView loadingAnimation = dialog.findViewById(R.id.loadingAnimation);
                    loadingAnimation.playAnimation();
                    RelativeLayout cancelButtonClick = dialog.findViewById(R.id.cancelButtonClick);
                    cancelButtonClick.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hideLoadingDialog();
                        }
                    });
                }
            });
            dialog.show();
        }
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
            dialog = null;
        }
    }

    void showTopLoadingDialog(){
        topLoadingAnimation.playAnimation();
        topLoadingAnimation.setVisibility(View.VISIBLE);
    }

    void hideTopLoadingDialog(){
        topLoadingAnimation.pauseAnimation();
        topLoadingAnimation.setVisibility(View.INVISIBLE);
    }


    void showNewHasScreenedDialog() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (newHasScreenedDDialog == null) {
                    newHasScreenedDDialog = new Dialog(activity);
                    newHasScreenedDDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    newHasScreenedDDialog.setCancelable(true);
                    newHasScreenedDDialog.setContentView(R.layout.custom_dialog_add_hasscreened);

                    ScrollView registerHasScreenedScrollView = newHasScreenedDDialog.findViewById(R.id.registerHasScreenedScrollView);

                    Spinner registerHasScreenedCitizen = newHasScreenedDDialog.findViewById(R.id.registerHasScreenedCitizen);
                    Spinner registerHasScreenedStatus = newHasScreenedDDialog.findViewById(R.id.registerHasScreenedStatus);
                    Spinner registerHasScreenedType = newHasScreenedDDialog.findViewById(R.id.registerHasScreenedType);
                    Spinner registerHasScreenedRegion = newHasScreenedDDialog.findViewById(R.id.registerHasScreenedRegion);

                    TextView registerHasScreenedDepartment = newHasScreenedDDialog.findViewById(R.id.registerHasScreenedDepartment);
                    TextView registerHasScreenedQuarter = newHasScreenedDDialog.findViewById(R.id.registerHasScreenedQuarter);
                    TextView registerHasScreenedCity = newHasScreenedDDialog.findViewById(R.id.registerHasScreenedCity);
                    TextView registerHasScreenedError = newHasScreenedDDialog.findViewById(R.id.registerHasScreenedError);

                    RelativeLayout registerHasScreenedButtonClick = newHasScreenedDDialog.findViewById(R.id.registerHasScreenedButtonClick);
                    RelativeLayout registerHasScreenedButtonCancelClick = newHasScreenedDDialog.findViewById(R.id.registerHasScreenedButtonCancelClick);

                    registerHasScreenedScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                    OverScrollDecoratorHelper.setUpOverScroll(registerHasScreenedScrollView);

                    ArrayAdapter<CharSequence> hasScreenedStatusSpinnerAdapter = ArrayAdapter.createFromResource(context, R.array.status, android.R.layout.simple_spinner_dropdown_item);
                    hasScreenedStatusSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    registerHasScreenedStatus.setAdapter(hasScreenedStatusSpinnerAdapter);

                    ArrayAdapter<CharSequence> hasScreenedTypeSpinnerAdapter = ArrayAdapter.createFromResource(context, R.array.type, android.R.layout.simple_spinner_dropdown_item);
                    hasScreenedTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    registerHasScreenedType.setAdapter(hasScreenedTypeSpinnerAdapter);

                    CustomCitizenSpinnerAdapter hasScreenedCitizenSpinnerAdapter = new CustomCitizenSpinnerAdapter(context, 0, citizenList);
                    registerHasScreenedCitizen.setAdapter(hasScreenedCitizenSpinnerAdapter);

                    ArrayAdapter<CharSequence> hasScreenedRegionSpinnerAdapter = ArrayAdapter.createFromResource(context, R.array.region, android.R.layout.simple_spinner_dropdown_item);
                    hasScreenedRegionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    registerHasScreenedRegion.setAdapter(hasScreenedRegionSpinnerAdapter);

                    registerHasScreenedButtonCancelClick.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            newHasScreenedDDialog.dismiss();
                            newHasScreenedDDialog = null;
                        }
                    });
                    registerHasScreenedButtonClick.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (registerHasScreenedCitizen.getSelectedItem() != null) {
                                if (registerHasScreenedStatus.getSelectedItem().toString().length() > 0) {
                                    if (registerHasScreenedType.getSelectedItem().toString().length() > 0) {
                                        if (registerHasScreenedRegion.getSelectedItem().toString().length() > 0) {
                                            if (registerHasScreenedDepartment.getText().toString().length() > 0) {
                                                if (registerHasScreenedQuarter.getText().toString().length() > 0) {
                                                    if (registerHasScreenedCity.getText().toString().length() > 0) {
                                                        registerHasScreenedError.setVisibility(View.INVISIBLE);
                                                        hasScreened = new HasScreened();
                                                        hasScreened.setCitizen_who_has_been_screened(null);
                                                        hasScreened.setStatus(registerHasScreenedStatus.getSelectedItem().toString());
                                                        hasScreened.setType_screening(registerHasScreenedType.getSelectedItem().toString());
                                                        hasScreened.setRegion(registerHasScreenedRegion.getSelectedItem().toString());
                                                        hasScreened.setDepartment(registerHasScreenedDepartment.getText().toString());
                                                        hasScreened.setQuarter(registerHasScreenedQuarter.getText().toString());
                                                        hasScreened.setCity(registerHasScreenedCity.getText().toString());
                                                        hasScreened.setCitizen_who_has_been_screened((Citizen) registerHasScreenedCitizen.getSelectedItem());
                                                        Calendar calendar = Calendar.getInstance();
                                                        int year = calendar.get(Calendar.YEAR);
                                                        int month = calendar.get(Calendar.MONTH);
                                                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                                                        month = month + 1;
                                                        String date = day + "/" + month + "/" + year;
                                                        hasScreened.setScreening_date(date);
                                                        newHasScreenedDDialog.dismiss();
                                                        newHasScreenedDDialog = null;
                                                        showLoadingDialog();
                                                        uploadHasScreenedDataToFireStore();
                                                    } else {
                                                        registerHasScreenedError.setText("Aucune ville selectione");
                                                        registerHasScreenedError.setVisibility(View.VISIBLE);
                                                    }
                                                } else {
                                                    registerHasScreenedError.setText("Aucun quartier selectione");
                                                    registerHasScreenedError.setVisibility(View.VISIBLE);
                                                }
                                            } else {
                                                registerHasScreenedError.setText("Aucune departement selectione");
                                                registerHasScreenedError.setVisibility(View.VISIBLE);
                                            }
                                        } else {
                                            registerHasScreenedError.setText("Aucune region selectione");
                                            registerHasScreenedError.setVisibility(View.VISIBLE);
                                        }
                                    } else {
                                        registerHasScreenedError.setText("Aucun type selectione");
                                        registerHasScreenedError.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    registerHasScreenedError.setText("Aucun statut selectione");
                                    registerHasScreenedError.setVisibility(View.VISIBLE);
                                }
                            } else {
                                registerHasScreenedError.setText("Aucun citoyen selectione");
                                registerHasScreenedError.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    newHasScreenedDDialog.show();
                }
            }
        });
    }

    void showNewCitizenDialog(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (newCitizenDialog == null) {
                    newCitizenDialog = new Dialog(activity);
                    newCitizenDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    newCitizenDialog.setCancelable(true);
                    newCitizenDialog.setContentView(R.layout.custom_dialog_add_citizen);
                    newCitizenDialog.show();

                    ScrollView registerCitizenScrollView = newCitizenDialog.findViewById(R.id.registerCitizenScrollView);

                    TextView registerCitizenFirstName = newCitizenDialog.findViewById(R.id.registerCitizenFirstName);
                    TextView registerCitizenSecondName = newCitizenDialog.findViewById(R.id.registerCitizenSecondName);
                    TextView registerCitizenBornDate = newCitizenDialog.findViewById(R.id.registerCitizenBornDate);
                    TextView registerCitizenCNI = newCitizenDialog.findViewById(R.id.registerCitizenCNI);
                    TextView registerCitizenSecondPhone = newCitizenDialog.findViewById(R.id.registerCitizenSecondPhone);
                    TextView registerCitizenError = newCitizenDialog.findViewById(R.id.registerCitizenError);
                    Spinner registerCitizenGender = newCitizenDialog.findViewById(R.id.registerCitizenGender);
                    Spinner registerCitizenNationality = newCitizenDialog.findViewById(R.id.registerCitizenNationality);
                    registerCitizenPicture = (SimpleDraweeView) newCitizenDialog.findViewById(R.id.registerCitizenPicture);

                    ImageView registerCitizenPictureClick = newCitizenDialog.findViewById(R.id.registerCitizenPictureClick);

                    RelativeLayout registerCitizenBornDateRelativeClick = newCitizenDialog.findViewById(R.id.registerCitizenBornDateRelativeClick);
                    RelativeLayout registerCitizenButtonClick = newCitizenDialog.findViewById(R.id.registerCitizenButtonClick);
                    RelativeLayout registerCitizenButtonCancelClick = newCitizenDialog.findViewById(R.id.registerCitizenButtonCancelClick);

                    citizenProfilePicturePath = null;
                    registerCitizenScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                    OverScrollDecoratorHelper.setUpOverScroll(registerCitizenScrollView);
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int finalMonth = month;
                    month = month+1;
                    String date = day+"/"+month+"/"+year;
                    citizen.setRegisterDate(date);
                    registerCitizenBornDate.setText(date);
                    registerCitizenBornDateRelativeClick.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            datePickerDialog = new DatePickerDialog(
                                    context,
                                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                                    onDateSetListener, year, finalMonth, day
                            );
                            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            datePickerDialog.show();
                        }
                    });
                    onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            month = month+1;
                            String date = dayOfMonth+"/"+month+"/"+year;
                            registerCitizenBornDate.setText(date);
                        }
                    };
                    registerCitizenPictureClick.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            citizenProfilePicturePath = null;
                            checkCameraPermissions();
                        }
                    });
                    ArrayAdapter<CharSequence> citizenGenderSpinnerAdapter = ArrayAdapter.createFromResource(context, R.array.genders, android.R.layout.simple_spinner_dropdown_item);
                    citizenGenderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    registerCitizenGender.setAdapter(citizenGenderSpinnerAdapter);

                    ArrayAdapter<CharSequence> citizenNationalitySpinnerAdapter = ArrayAdapter.createFromResource(context, R.array.nationality, android.R.layout.simple_spinner_dropdown_item);
                    citizenNationalitySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    registerCitizenNationality.setAdapter(citizenNationalitySpinnerAdapter);

                    registerCitizenButtonCancelClick.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            newCitizenDialog.dismiss();
                            newCitizenDialog = null;
                        }
                    });
                    registerCitizenButtonClick.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (registerCitizenFirstName.getText().toString().length() > 0){
                                if (registerCitizenSecondName.getText().toString().length() > 0){
                                    if (registerCitizenBornDate.getText().toString().length() > 0){
                                        if (registerCitizenGender.getSelectedItem().toString().length() > 0){
                                            if (registerCitizenCNI.getText().toString().length() > 0){
                                                if (registerCitizenSecondPhone.getText().toString().length() > 0){
                                                    if (registerCitizenNationality.getSelectedItem().toString().length() > 0){
                                                        citizen.setUrlPicture("");
                                                        citizen.setFirstName(registerCitizenFirstName.getText().toString());
                                                        citizen.setSecondName(registerCitizenSecondName.getText().toString());
                                                        citizen.setBirthday(registerCitizenBornDate.getText().toString());
                                                        citizen.setGender(registerCitizenGender.getSelectedItem().toString());
                                                        citizen.setIdentityCard(registerCitizenCNI.getText().toString());
                                                        citizen.setMobilePhone(Integer.parseInt(registerCitizenSecondPhone.getText().toString()));
                                                        citizen.setNationality(registerCitizenNationality.getSelectedItem().toString());
                                                        newCitizenDialog.dismiss();
                                                        newCitizenDialog = null;
                                                        showLoadingDialog();
                                                        if (citizenProfilePicturePath != null){
                                                            uploadImageToFirebaseStorage();
                                                        }else{
                                                            uploadCitizenDataToFireStore();
                                                        }
                                                    }else {
                                                        registerCitizenError.setText("Nationalite invalide");
                                                        registerCitizenError.setVisibility(View.VISIBLE);
                                                    }
                                                }else {
                                                    registerCitizenError.setText("Telephone invalide");
                                                    registerCitizenError.setVisibility(View.VISIBLE);
                                                }
                                            }else {
                                                registerCitizenError.setText("CNI invalide");
                                                registerCitizenError.setVisibility(View.VISIBLE);
                                            }
                                        }else {
                                            registerCitizenError.setText("Genre invalide");
                                            registerCitizenError.setVisibility(View.VISIBLE);
                                        }
                                    }else {
                                        registerCitizenError.setText("Date de naissance invalide");
                                        registerCitizenError.setVisibility(View.VISIBLE);
                                    }
                                }else {
                                    registerCitizenError.setText("Prenom invalide");
                                    registerCitizenError.setVisibility(View.VISIBLE);
                                }
                            }else {
                                registerCitizenError.setText("Nom invalide");
                                registerCitizenError.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }

    private void getPicture(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_PERMISSION_CODE);
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(activity.getApplicationContext());
        // path to /data/data/covid_app/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profil.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Objects.requireNonNull(fos).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath()+"/profil.jpg";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_PERMISSION_CODE){
            Bitmap citizenPicture = null;
            try {
                citizenPicture = (Bitmap) Objects.requireNonNull(data).getExtras().get("data");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (citizenPicture != null){
                registerCitizenPicture.setImageBitmap(citizenPicture);
                citizenProfilePicturePath = saveToInternalStorage(citizenPicture);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE){
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(context, "Permissions acceptees", Toast.LENGTH_SHORT).show();
                checkCameraPermissions();
            }else{
                Toast.makeText(context, "Permissions refusees", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == CAMERA_PERMISSION_CODE){
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(context, "Permissions acceptees", Toast.LENGTH_SHORT).show();
                checkCameraPermissions();
            }else{
                Toast.makeText(context, "Permissions refusees", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkCameraPermissions(){
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }else{
            getPicture();
        }
    }

    private void showLogOutButton(){activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
            logOutButton.setVisibility(View.VISIBLE);
            logOutButton.setAlpha(1);
        }
    });
    }

    private void hideLogOutButton(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logOutButton.setAlpha(0);
                logOutButton.setVisibility(View.INVISIBLE);
            }
        });
    }
}