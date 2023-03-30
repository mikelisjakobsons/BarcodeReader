package michael.example.com.barcodereader;

import android.os.Build;
import android.text.TextUtils;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Michael on 2016-08-10.
 */
public class JdbcBarcodeObj {
    private String barcodeValue = null;
    private String userId = null;
    private String userName = null;
    private String dateStamp = null;

    public JdbcBarcodeObj(String barcodeStr) {
        barcodeValue = barcodeStr;
    }

    public String getBarcodeVal() {
        return barcodeValue == null ? "" : barcodeValue;
    }

    public String getUserId() {
        return userId == null || userId.equals("") ? getDeviceName() : userId;
    }

    public String getUserName() {
        return userName == null ? "" : userName;
    }

    public String getDateStamp() {
        return dateStamp == null ? "" : dateStamp;
    }


    public boolean verifyDb(String barcodeVal, String dateStamp) {
        if( barcodeValue == null || barcodeValue.equals(""))
            return false;

        userId = getDeviceName();
        // TODO add call to JDBC to verify whether barcodeVal is in MySQL database
        // String sql = "SELECT Username FROM Device WHERE barcodeStr = " + barcodeVal;

        // TODO get username from result of MySQL query from table Device, replacing dummy 'Michael'.
        userName = "Michael";

        return true;
    }

    public void insertScanRecord(String barcodeVal, String dateStamp) {
        // TODO add or adjust call to SQL INSERT statement for adding a record to Sodipet database.
        // Currently, temporarily using my localhost MySQL database, SQL connection

    }


    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String id = Build.ID;
        String serial = Build.SERIAL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + "\nMODEL: " + model + "\nID: "+ id + "\nSERIAL: " + serial;
        //return Build.MODEL;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        // String phrase = "";
        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                // phrase += Character.toUpperCase(c);
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            // phrase += c;
            phrase.append(c);
        }

        return phrase.toString();
    }
}
