package com.hareesh.quotepad;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hareesh.quotepad.explore.ExploreActivity;
import com.hareesh.quotepad.myquotes.MyQuotesActivity;
import com.hareesh.quotepad.popular.PopularActivity;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView welcomeText;
    public static boolean logout;
    FirebaseDatabase database;
    DatabaseReference myRef;
    Firebase detailsFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        logout = false;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Firebase.setAndroidContext(this);

        welcomeText = (TextView) findViewById(R.id.welcomeText);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            welcomeText.setText(getString(R.string.welcomeText, user.getDisplayName()));
            database = FirebaseDatabase.getInstance();
            myRef = database.getReference(user.getUid());
            detailsFirebase = new Firebase(myRef.getRoot().toString().concat("/").concat("Users").concat("/").concat(user.getUid()).concat("/").concat("Info"));
            HashMap<String, Object> userDetails = new HashMap<>();
            userDetails.put("Display Name", user.getDisplayName());
            userDetails.put("Email", user.getEmail());
            detailsFirebase.setValue(userDetails);
        } else {
            Intent intent = new Intent(MainActivity.this, AuthActivity.class);
            startActivity(intent);
        }

    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new MaterialDialog.Builder(this)
                    .backgroundColor(getResources().getColor(R.color.theme_primary))
                    .titleColor(getResources().getColor(R.color.theme_primary_text_inverted))
                    .negativeColor(getResources().getColor(R.color.theme_primary_text_inverted))
                    .positiveColor(getResources().getColor(R.color.theme_primary_text_inverted))
                    .title("Exit?")
                    .positiveText("Yes")
                    .negativeText("No")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.my_quotepad) {
            Intent intent = new Intent(MainActivity.this, MyQuotesActivity.class);
            startActivity(intent);
        } else if (id == R.id.popular_quotes) {
            Intent intent = new Intent(MainActivity.this, PopularActivity.class);
            startActivity(intent);
        } else if (id == R.id.explore) {
            Intent intent = new Intent(MainActivity.this, ExploreActivity.class);
            startActivity(intent);
        } else if (id == R.id.app_settings) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send){

        } else if (id == R.id.account_details) {
            Intent intent = new Intent(MainActivity.this, AccountDetailsActivity.class);
            startActivity(intent);
        } else if (id == R.id.log_out) {
            logout = true;
            Intent intent = new Intent(MainActivity.this, AuthActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
