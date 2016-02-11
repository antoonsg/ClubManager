package com.antoonsg.clubmanager;

import android.util.Log;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "ClubManager.MainAct";

    private ListView m_lstReminder;
    private String DB_NAME ="La-Gaillarde.db";
    private SQLiteDatabase m_database;
    private SimpleCursorAdapter m_dataAdapter;
    private ExternalDbOpenHelper m_dbOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        m_dbOpenHelper = new ExternalDbOpenHelper(MainActivity.this, DB_NAME);
        m_database = m_dbOpenHelper.openDataBase();

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

        Button btnSendSMS = (Button) findViewById(R.id.btnSendReminderSMS);
        btnSendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PaymentReminder p = new PaymentReminder();
                //TODO: optimise query of data, var allocation and add protections
                //TODO: understand why solde>0 is not working on selection (type of column in INT not correctly converted by db manager ?)
                Cursor c_data = m_database.query("Cotisations",null,"(AmenagementPayement='FAUX') AND (Solde > 0)",null,null,null,null);
                Integer numofsms = 0;
                if (c_data.moveToFirst()) {
                    while(!c_data.isAfterLast()) {
                        String fam = c_data.getString(c_data.getColumnIndex("NomDeFamille"));
                        Integer total = c_data.getInt(c_data.getColumnIndex("CotisationDue"));
                        Integer solde = c_data.getInt(c_data.getColumnIndex("Solde"));
                        String gsm = c_data.getString(c_data.getColumnIndex("GSM"));
                        String cours = c_data.getString(c_data.getColumnIndex("ListeDesCours"));
                        if (solde > 0) {
                            Log.i(TAG, String.format("Sending reminder of %d (total %d) to family %s on phone %s", solde, total, fam, gsm));
                            p.RemindBySMS(MainActivity.this.getApplicationContext(),
                                    gsm,fam,total,solde,cours);
                            numofsms++;
                        }
                        c_data.moveToNext();
                        }
                    }
                c_data = null;
                //p.RemindBySMS(MainActivity.this.getApplicationContext(),"+32 498 61 63 81","Antoons","10","5","ADMIN, F6");
                Log.i(TAG, String.format("%d SMS sent.", numofsms));
            }
        });
        Button btnLoadList = (Button) findViewById(R.id.btnLoadReminder);
        btnLoadList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor c_data = m_database.query("Cotisations",null,"AmenagementPayement='FAUX'",null,null,null,null);
                String[] columns = new String[] {"NomDeFamille","GSM","ListeDesCours","CotisationDue","Solde"};

                // the XML defined views which the data will be bound to
                int[] to = new int[] {
                        R.id.name,
                        R.id.phone,
                        R.id.courses,
                        R.id.cotisation,
                        R.id.reminder
                };

                // create the adapter using the cursor pointing to the desired data
                //as well as the layout information
                m_dataAdapter = new SimpleCursorAdapter(
                        MainActivity.this,
                        R.layout.one_family_info,
                        c_data,
                        columns,
                        to,
                        0);
                m_lstReminder = (ListView) findViewById(R.id.lstReminder);
                // Assign adapter to ListView
                m_lstReminder.setAdapter(m_dataAdapter);
            }
        });

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
