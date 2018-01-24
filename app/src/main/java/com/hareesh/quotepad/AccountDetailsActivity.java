package com.hareesh.quotepad;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.CardProvider;
import com.dexafree.materialList.card.provider.ListCardProvider;
import com.dexafree.materialList.view.MaterialListView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hareesh.quotepad.R;

import org.w3c.dom.Text;

/**
 * Created by Hareesh on 9/4/2016.
 */
public class AccountDetailsActivity extends AppCompatActivity {

    MaterialListView mListView;
    String currentuser;
    String email;
    Context mContext;
    private static final String TAG = "AccountDetailsActivity";
    FirebaseUser user;
    AuthCredential emailCredentials;
    AuthCredential googleCredentials;
    FirebaseDatabase database;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountdetails);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mContext = this;

        mListView = (MaterialListView) findViewById(R.id.material_listview);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentuser = user.getDisplayName();
            email = user.getEmail();

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            adapter.add("");
            adapter.add(currentuser);
            adapter.add(email);
            adapter.add("Detail 3");
            adapter.add("Detail 4");
            adapter.add("Detail 5");

            final CardProvider provider = new Card.Builder(this)
                    .withProvider(new ListCardProvider())
                    .setLayout(R.layout.material_card_list_layout)
                    .setTitle("Current User: ")
                    .setBackgroundColor(getResources().getColor(R.color.theme_primary))
                    .setTitleColor(Color.parseColor("#FFFFFF"))
                    .setAdapter(adapter);

            mListView.getAdapter().add(provider.endConfig().build());
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_delete_account);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(mContext)
                        .backgroundColor(getResources().getColor(R.color.fab_delete))
                        .titleColor(getResources().getColor(R.color.theme_primary_text_inverted))
                        .negativeColor(getResources().getColor(R.color.theme_primary_text_inverted))
                        .positiveColor(getResources().getColor(R.color.theme_primary_text_inverted))
                        .title("Delete account and all saved data?")
                        .positiveText("Yes")
                        .negativeText("No")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                new MaterialDialog.Builder(mContext)
                                        .backgroundColor(getResources().getColor(R.color.fab_delete))
                                        .titleColor(getResources().getColor(R.color.theme_primary_text_inverted))
                                        .negativeColor(getResources().getColor(R.color.theme_primary_text_inverted))
                                        .positiveColor(getResources().getColor(R.color.theme_primary_text_inverted))
                                        .title("This cannot be undone.")
                                        .positiveText("Cancel")
                                        .negativeText("Delete")
                                        .buttonsGravity(GravityEnum.CENTER)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                dialog.dismiss();

                                                emailCredentials = EmailAuthProvider.getCredential("user@example.com", "password1234");
                                                googleCredentials = GoogleAuthProvider.getCredential("user@example.com", "password1234");

                                                database = FirebaseDatabase.getInstance();
                                                myRef = database.getReference(user.getUid());
                                                myRef.removeValue();

                                                user.reauthenticate(emailCredentials)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()) {
                                                                    Log.d(TAG, "User re-authenticated.");
                                                                    user.delete()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        Log.d(TAG, "Email account deleted.");
                                                                                        Intent i = getBaseContext().getPackageManager()
                                                                                                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                                                                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                        startActivity(i);
                                                                                    } else {
                                                                                        Log.d(TAG, "unsuccessful.");
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                                else{
                                                                    user.reauthenticate(googleCredentials)
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    Log.d(TAG, "User re-authenticated.");
                                                                                    user.delete()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        Log.d(TAG, "Google account deleted.");
                                                                                                        Intent i = getBaseContext().getPackageManager()
                                                                                                                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                                                                                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                                        startActivity(i);
                                                                                                    }
                                                                                                    else{
                                                                                                        Log.d(TAG, "unsuccessful.");
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }).show();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
    }
}
