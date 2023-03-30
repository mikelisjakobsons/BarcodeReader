package michael.example.com.barcodereader;

import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    private Button scanBtn, showfileBtn, clearBtn;
    private CheckBox saveChk;
    private TextView formatTxt, contentTxt, messageTxt, useridTxt, usernameTxt, datestampTxt;
    private final String FILENAME = "SodipetScans.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanBtn = (Button)findViewById(R.id.scan_button);
        saveChk = (CheckBox)findViewById(R.id.save_to_file);
        showfileBtn = (Button)findViewById(R.id.show_file_contents);
        clearBtn = (Button)findViewById(R.id.clear_file);
        formatTxt = (TextView)findViewById(R.id.scan_format);
        contentTxt = (TextView)findViewById(R.id.scan_content);
        messageTxt = (TextView)findViewById(R.id.scan_message);
        useridTxt = (TextView)findViewById(R.id.scan_userid);
        usernameTxt = (TextView)findViewById(R.id.scan_username);
        datestampTxt = (TextView)findViewById(R.id.scan_datestamp);

        scanBtn.setOnClickListener(this);
        saveChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // update your model (or other business logic) based on isChecked
                if(isChecked)
                {
                    Toast.makeText(MainActivity.this, "Saving barcode results to a file", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Results are no longer saved to a file", Toast.LENGTH_LONG).show();
                }

            }
        });
        showfileBtn.setOnClickListener(this);
        clearBtn.setOnClickListener(this);
    }

    public void onClick(View v){
        if(v.getId() == R.id.scan_button){
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
        else if(v.getId() == R.id.show_file_contents) {
            Intent intent = new Intent(this, FileActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.clear_file) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to clear scan file?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            clearFile();
                        }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void clearFile() {
        try {
            File root = Environment.getExternalStorageDirectory();
            FileOutputStream fOut = new FileOutputStream(new File(root, FILENAME), false);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            osw.write("");
            osw.close();
            Toast.makeText(MainActivity.this, "Scan file has been emptied.", Toast.LENGTH_LONG).show();
        }
        catch(IOException eio) {
            String scanMessage = "IO ERROR: " + eio.getMessage();
            formatTxt.setText(scanMessage);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        String scanMessage;
        String datetimeStamp = "";
        useridTxt.setText("");
        usernameTxt.setText("");
        datestampTxt.setText("");
        try {
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanningResult != null) {
                String scanContent = scanningResult.getContents();
                String scanFormat = scanningResult.getFormatName();
                if (scanFormat.equals("CODE_39")) {
                    datetimeStamp = getDateStamp(System.currentTimeMillis());
                    datestampTxt.setText("DATE & TIME: " + datetimeStamp);

                    JdbcBarcodeObj jdbcBarcodeObj = new JdbcBarcodeObj(scanContent);
                    if (jdbcBarcodeObj.verifyDb(scanContent, datetimeStamp)) {
                        jdbcBarcodeObj.insertScanRecord(scanContent, datetimeStamp);

                        useridTxt.setText("USER ID: " + jdbcBarcodeObj.getUserId());
                        usernameTxt.setText("USERNAME: " + jdbcBarcodeObj.getUserName());
                        // TODO  message may need to be in French. e.g. "Coupon est accepte."
                        scanMessage = "Coupon is accepted.";
                        if( saveChk.isChecked() ) {
                            // record scanContent and datetimeStamp into save file.
                            try {
                                File root = Environment.getExternalStorageDirectory();
                                FileOutputStream fOut = new FileOutputStream(new File(root, FILENAME), true);
                                OutputStreamWriter osw = new OutputStreamWriter(fOut);
                                BufferedWriter fbw = new BufferedWriter(osw);
                                fbw.write(datetimeStamp + "     " + scanContent);
                                fbw.newLine();
                                fbw.close();
                            }
                            catch(IOException eio) {
                                scanMessage += "\nIO ERROR: " + eio.getMessage();
                            }
                        }
                    }
                    else {
                        scanMessage = "Coupon is invalid or used!";
                        Toast msg = Toast.makeText(getApplicationContext(), scanMessage, Toast.LENGTH_SHORT);
                        msg.show();
                    }
                } else {
                    scanMessage = "Data received is not Code 39!";
                    Toast msg = Toast.makeText(getApplicationContext(), scanMessage, Toast.LENGTH_SHORT);
                    msg.show();
                }
                formatTxt.setText("FORMAT: " + scanFormat);
                contentTxt.setText("CONTENT: " + scanContent);
                messageTxt.setText("*** " + scanMessage);
            } else {
                scanMessage = "*** No scan data received!";
                formatTxt.setText("FORMAT: - - -");
                contentTxt.setText("CONTENT: - - -");
                messageTxt.setText(scanMessage);
                Toast toast = Toast.makeText(getApplicationContext(),
                        scanMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        catch(Exception e) {
            scanMessage = "*** Error scanning data!";
            formatTxt.setText("FORMAT: - - -");
            contentTxt.setText("CONTENT: - - -");
            messageTxt.setText(scanMessage);
            Toast toast = Toast.makeText(getApplicationContext(),
                    scanMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private String getDateStamp(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("yyyy-MM-dd    HH:mm:ss", cal).toString();
        return date;
    }

}
