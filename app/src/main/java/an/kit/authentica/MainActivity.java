package an.kit.authentica;

/*
MIT License

Copyright (c) 2021 Ankit Das

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

// import android libraries
import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

// import zxing barcode scanner library
import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;

// import java arraylist
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ActionMode.Callback {
    private static final String TAG = "themeActivity"  ;
    private ArrayList<Entry> entries;       // variable to store all entries
    private EntriesAdapter adapter;         // adapter to arrange, rearrange entries
    private FloatingActionButton fab;       // fab '+' to create new entry

    private Handler handler;                
    private Runnable handlerTask;

    private static final int PERMISSIONS_REQUEST_CAMERA = 42;   // define permission code for camera

    /*
       function to scan QR Code
    */
    private void doScanQRCode(){            // function to scan QR code using zxing
        new IntentIntegrator(MainActivity.this)
                .setCaptureActivity(CaptureActivityAnyOrientation.class)
                .setOrientationLocked(false)
                .initiateScan();
    }

    /*
       function to check permission and scan QR Code
    */
    private void scanQRCode(){
        // check permission, if granted, scan QR code
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            doScanQRCode();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
       if(requestCode == PERMISSIONS_REQUEST_CAMERA) {
           if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               // permission was granted
               doScanQRCode();
           } else {
               Snackbar.make(fab, R.string.msg_camera_permission, Snackbar.LENGTH_LONG).addCallback(new Snackbar.Callback() {
                   @Override
                   public void onDismissed(Snackbar snackbar, int event) {
                       super.onDismissed(snackbar, event);

                       // if no entry is present, showNoAccount() is called
                       if (entries.isEmpty()) {
                           showNoAccount();
                       }
                   }
               }).show();       // show the created snackbar 
           }
       }
       else {
           super.onRequestPermissionsResult(requestCode, permissions, grantResults);
       }
    }

    private Entry nextSelection = null;
    private void showNoAccount(){
        Snackbar noAccountSnackbar = Snackbar.make(fab, R.string.no_accounts, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.button_add, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        scanQRCode();
                    }
                });
        noAccountSnackbar.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);

	    // Prevents Screenshots to be taken
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.action_scan);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanQRCode();
            }
        });

        final ListView listView = (ListView) findViewById(R.id.listView);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        
        // Load previously stores entries
        entries = SettingsHelper.load(this);

        adapter = new EntriesAdapter();
        adapter.setEntries(entries);

        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                nextSelection = entries.get(i);
                startActionMode(MainActivity.this);

                return true;
            }
        });

        if(entries.isEmpty()){
            showNoAccount();
        }

        handler = new Handler();
        handlerTask = new Runnable()
        {
            @Override
            public void run() {
                int progress =  (int) (System.currentTimeMillis() / 1000) % 30 ;
                progressBar.setProgress(progress*100);

                ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", (progress+1)*100);
                animation.setDuration(1000);
                animation.setInterpolator(new LinearInterpolator());
                animation.start();

                for(int i =0;i < adapter.getCount(); i++){
                    adapter.getItem(i).setCurrentOTP(TOTPHelper.generate(adapter.getItem(i).getSecret()));
                }
                adapter.notifyDataSetChanged();

                handler.postDelayed(this, 1000);
            }
        };
    }


    @Override
    public void onResume() {
        super.onResume();

        handler.post(handlerTask);
    }

    @Override
    public void onPause() {
        super.onPause();

        handler.removeCallbacks(handlerTask);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                Entry e = new Entry(intent.getStringExtra(Intents.Scan.RESULT));
                e.setCurrentOTP(TOTPHelper.generate(e.getSecret()));
                entries.add(e);
                SettingsHelper.store(this, entries);

                adapter.notifyDataSetChanged();

                Snackbar.make(fab, R.string.msg_account_added, Snackbar.LENGTH_LONG).show();
            } catch (Exception e) {
                Snackbar.make(fab, R.string.msg_invalid_qr_code, Snackbar.LENGTH_LONG).addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);

                        if(entries.isEmpty()){
                            showNoAccount();
                        }
                    }
                }).show();

                return;
            }
        }

        if(entries.isEmpty()){
            showNoAccount();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void setTheme(){
        setContentView(R.layout.activity_theme);
        MaterialButtonToggleGroup themeButtons = (MaterialButtonToggleGroup) findViewById(R.id.themebuttons);
        themeButtons.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if(isChecked){
                int theme = 0;
                switch (checkedId){
                    case R.id.button_default:
                        theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                        break;

                    case R.id.button_dark:
                        theme = AppCompatDelegate.MODE_NIGHT_YES;
                        break;

                    case R.id.button_light:
                        theme = AppCompatDelegate.MODE_NIGHT_NO;
                        break;
                }
                AppCompatDelegate.setDefaultNightMode(theme);
                setContentView(R.layout.activity_main);
                recreate();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_about){
            WebView view = (WebView) LayoutInflater.from(this).inflate(R.layout.dialog_about, null);
            view.loadUrl("file:///android_res/raw/about.html");
            new AlertDialog.Builder(this).setView(view).show();

            return true;
        }

        if(id == R.id.action_theme){
            setTheme();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.menu_edit, menu);

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        adapter.setCurrentSelection(nextSelection);
        adapter.notifyDataSetChanged();
        actionMode.setTitle(adapter.getCurrentSelection().getLabel());

        return true;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.action_delete) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.alert_remove) + adapter.getCurrentSelection().getLabel() + "?");
            alert.setMessage(R.string.msg_confirm_delete);

            alert.setPositiveButton(R.string.button_remove, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    entries.remove(adapter.getCurrentSelection());

                    Snackbar.make(fab, R.string.msg_account_removed, Snackbar.LENGTH_LONG).addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            super.onDismissed(snackbar, event);

                            if (entries.isEmpty()) {
                                showNoAccount();
                            }
                        }
                    }).show();

                    actionMode.finish();
                }
            });

            alert.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                    actionMode.finish();
                }
            });

            alert.show();

            return true;
        }
        else if (id == R.id.action_edit) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.alert_rename);

            final EditText input = new EditText(this);
            input.setText(adapter.getCurrentSelection().getLabel());
            alert.setView(input);

            alert.setPositiveButton(R.string.button_save, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    adapter.getCurrentSelection().setLabel(input.getEditableText().toString());
                    actionMode.finish();
                }
            });

            alert.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                    actionMode.finish();
                }
            });

            alert.show();

            return true;
        }

        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        adapter.setCurrentSelection(null);
        adapter.notifyDataSetChanged();

        SettingsHelper.store(this, entries);
    }
}
