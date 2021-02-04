package com.example.covid_app.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid_app.R;
import com.example.covid_app.models.Citizen;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class CitizenAdapter extends RecyclerView.Adapter<CitizenHolder> {

    Activity activity;
    Context context;
    ArrayList<Citizen> citizenList;

    public CitizenAdapter(Activity activity, Context context, ArrayList<Citizen> citizenList) {
        this.activity = activity;
        this.context = context;
        this.citizenList = citizenList;
    }

    @NonNull
    @Override
    public CitizenHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        CitizenHolder citizenHolder = new CitizenHolder(LayoutInflater.from(context).inflate(
                R.layout.card_citizen,
                parent,
                false));
        return citizenHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CitizenHolder holder, int position) {
        String tempName = citizenList.get(position).getFirstName() + " " + citizenList.get(position).getSecondName();
        String tempMobile = citizenList.get(position).getMobilePhone() + "";

        holder.citizenName.setText(tempName);
        holder.citizenPhone.setText(tempMobile);

        holder.citizenInfoButtonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogCitizenInfo(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return citizenList.size();
    }

    private void showDialogCitizenInfo(int position){
        Dialog citizenInfoDialog;
        citizenInfoDialog = new Dialog(context);
        citizenInfoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        citizenInfoDialog.setCancelable(true);
        citizenInfoDialog.setContentView(R.layout.custom_dialog_show_citizen);
        citizenInfoDialog.show();

        ScrollView citizenInfoScrollView = citizenInfoDialog.findViewById(R.id.citizenInfoScrollView);

        SimpleDraweeView infoCitizenPicture = citizenInfoDialog.findViewById(R.id.infoCitizenPicture);
        TextView infoCitizenFirstName = citizenInfoDialog.findViewById(R.id.infoCitizenFirstName);
        TextView infoCitizenLastName = citizenInfoDialog.findViewById(R.id.infoCitizenLastName);
        TextView infoCitizenBornDate = citizenInfoDialog.findViewById(R.id.infoCitizenBornDate);
        TextView infoCitizenGenre = citizenInfoDialog.findViewById(R.id.infoCitizenGenre);
        TextView infoCitizenCNI = citizenInfoDialog.findViewById(R.id.infoCitizenCNI);
        TextView infoCitizenPhone = citizenInfoDialog.findViewById(R.id.infoCitizenPhone);
        TextView infoCitizenNationality = citizenInfoDialog.findViewById(R.id.infoCitizenNationality);
        RelativeLayout infoCitizenButtonCloseClick = citizenInfoDialog.findViewById(R.id.infoCitizenButtonCloseClick);

        citizenInfoScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        OverScrollDecoratorHelper.setUpOverScroll(citizenInfoScrollView);

        infoCitizenPicture.setImageURI(citizenList.get(position).getUrlPicture());
        infoCitizenFirstName.setText(citizenList.get(position).getFirstName());
        infoCitizenLastName.setText(citizenList.get(position).getSecondName());
        infoCitizenBornDate.setText(citizenList.get(position).getBirthday());
        infoCitizenGenre.setText(citizenList.get(position).getGender());
        infoCitizenCNI.setText(citizenList.get(position).getIdentityCard());
        String tempPhone = citizenList.get(position).getMobilePhone() +"";
        infoCitizenPhone.setText(tempPhone);
        infoCitizenNationality.setText(citizenList.get(position).getNationality());

        infoCitizenButtonCloseClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                citizenInfoDialog.dismiss();
            }
        });
    }
}
