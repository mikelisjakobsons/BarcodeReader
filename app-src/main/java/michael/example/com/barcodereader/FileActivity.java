package michael.example.com.barcodereader;

import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class FileActivity extends AppCompatActivity {
    private final String FILENAME = "SodipetScans.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        String contents = "";
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            contents = "Contents of file:\n  Date               Time             Scan Result\n";
            // get the contents by reading in from file FILENAME
            File root = Environment.getExternalStorageDirectory();
            File file = new File(root, FILENAME);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                contents += line + "\n";
            }
            br.close();
        }
        catch (Exception e) {
            contents += "ERROR: " + e.getMessage();
        }

        // Create the text view
        TextView textView = new TextView(this);
        textView.setTextSize(16);
        textView.setTextColor(Color.parseColor("#000000"));
        textView.setText(contents);

        // Set the text view as the activity layout
        setContentView(textView);

    }


    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

}
