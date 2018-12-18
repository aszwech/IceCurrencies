package pl.aszwech.currencies;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Button mCalcButton;
    private TextView mConvertedTextView;
    private EditText mAmountEditText;
    private Spinner mForSpinner, mHomSpinner;
    private String[] mCurrencies;

    public static final String FOR = "FOR_CURRENCY";
    public static final String HOM = "HOM_CURRENCY";

    private String mKey;
    public static final String RATES = "rates";
    public static final String URL_BASE =
            "https://openexchangerates.org/api/latest.json?app_id=";
    private static final DecimalFormat DECIMAL_FORMAT = new
            DecimalFormat("#,##0.00000");

    private CurrencyTaskCallback mCurrencyTaskCallback;
    private RelativeLayout layout;

    public static interface CurrencyTaskCallback {
        void executionDone();
    }

    public void setCurrencyTaskCallback(CurrencyTaskCallback currencyTaskCallback) {
        this.mCurrencyTaskCallback = currencyTaskCallback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);

        ArrayList<String> arrayList = ((ArrayList<String>)
                getIntent().getSerializableExtra(SplashActivity.KEY_ARRAYLIST));
        Collections.sort(arrayList);
        mCurrencies = arrayList.toArray(new String[arrayList.size()]);

        layout = findViewById(R.id.relativeLayout);
        mConvertedTextView = (TextView) findViewById(R.id.txt_converted);
        mAmountEditText = (EditText) findViewById(R.id.edt_amount);
        mCalcButton = (Button) findViewById(R.id.btn_calc);
        mForSpinner = (Spinner) findViewById(R.id.spn_for);
        mHomSpinner = (Spinner) findViewById(R.id.spn_hom);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                R.layout.spinner_closed,
                mCurrencies
        );

        arrayAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        mHomSpinner.setAdapter(arrayAdapter);
        mForSpinner.setAdapter(arrayAdapter);

        mHomSpinner.setOnItemSelectedListener(this);
        mForSpinner.setOnItemSelectedListener(this);

        if (savedInstanceState == null
                && (PrefsMgr.getString(this, FOR) == null &&
                PrefsMgr.getString(this, HOM) == null)) {
            mForSpinner.setSelection(findPositionGivenCode("CNY", mCurrencies));
            mHomSpinner.setSelection(findPositionGivenCode("USD", mCurrencies));

            PrefsMgr.setString(this, FOR, "CNY");
            PrefsMgr.setString(this, HOM, "USD");
        } else {
            mForSpinner.setSelection(findPositionGivenCode(PrefsMgr.getString(this,
                    FOR), mCurrencies));
            mHomSpinner.setSelection(findPositionGivenCode(PrefsMgr.getString(this,
                    HOM), mCurrencies));
        }
        mCalcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNumeric(String.valueOf(mAmountEditText.getText()))) {
                    new CurrencyConverterTask().execute(URL_BASE + mKey);
                } else {
                    Toast.makeText(MainActivity.this, "Not a numeric value, try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
        mKey = getKey("open_key");
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    private void launchBrowser(String strUri) {
        if (isOnline()) {
            Uri uri = Uri.parse(strUri);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    private void invertCurrencies() {
        int nFor = mForSpinner.getSelectedItemPosition();
        int nHom = mHomSpinner.getSelectedItemPosition();
        mForSpinner.setSelection(nHom);
        mHomSpinner.setSelection(nFor);
        mConvertedTextView.setText("");
        PrefsMgr.setString(this, FOR, extractCodeFromCurrency((String)
                mForSpinner.getSelectedItem()));
        PrefsMgr.setString(this, HOM, extractCodeFromCurrency((String)
                mHomSpinner.getSelectedItem()));
    }

    private int findPositionGivenCode(String code, String[] currencies) {
        for (int i = 0; i < currencies.length; i++) {
            if (extractCodeFromCurrency(currencies[i]).equalsIgnoreCase(code)) {
                return i;
            }
        }
        return 0;
    }

    private String extractCodeFromCurrency(String currency) {
        return (currency).substring(0, 3);
    }

    private String getKey(String keyName) {
        AssetManager assetManager = this.getResources().getAssets();
        Properties properties = new Properties();
        try {
            InputStream inputStream = assetManager.open("keys.properties");
            properties.load(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(keyName);

    }

    public static boolean isNumeric(String str) {
        try {
            double dub = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.mnu_invert:
                invertCurrencies();
                break;
            case R.id.mnu_codes:
                launchBrowser(SplashActivity.URL_CODES);
                break;
            case R.id.mnu_exit:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()) {
            case R.id.spn_for:
                PrefsMgr.setString(this, FOR,
                        extractCodeFromCurrency((String) mForSpinner.getSelectedItem()));
                break;

            case R.id.spn_hom:
                PrefsMgr.setString(this, HOM,
                        extractCodeFromCurrency((String) mHomSpinner.getSelectedItem()));
                break;

            default:
                break;
        }
        mConvertedTextView.setText("");

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class CurrencyConverterTask extends AsyncTask<String, Void, JSONObject> {
        ProgressBar progressBar;


        @Override
        protected void onPreExecute() {
            progressBar = new ProgressBar(MainActivity.this, null, android.R.attr.progressBarStyleLarge);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            layout.addView(progressBar, params);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            return new JSONParser().getJSONFromUrl(params[0]);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            double dCalculated = 0.0;
            String strForCode =
                    extractCodeFromCurrency(mCurrencies[mForSpinner.getSelectedItemPosition()]);
            String strHomCode = extractCodeFromCurrency(mCurrencies[mHomSpinner.
                    getSelectedItemPosition()]);
            String strAmount = mAmountEditText.getText().toString();
            try {
                JSONObject jsonRates = jsonObject.getJSONObject(RATES);
                if (strHomCode.equalsIgnoreCase("USD")) {
                    dCalculated = Double.parseDouble(strAmount) / jsonRates.getDouble(strForCode);
                } else if (strForCode.equalsIgnoreCase("USD")) {
                    dCalculated = Double.parseDouble(strAmount) * jsonRates.getDouble(strHomCode);
                } else {
                    dCalculated = Double.parseDouble(strAmount) * jsonRates.getDouble(strHomCode)
                            / jsonRates.getDouble(strForCode);
                }
            } catch (JSONException e) {
                Toast.makeText(
                        MainActivity.this,
                        "There's been a JSON exception: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
                mConvertedTextView.setText("");
                e.printStackTrace();
            }
            mConvertedTextView.setText(DECIMAL_FORMAT.format(dCalculated) + " " + strHomCode);
            progressBar.setVisibility(View.GONE);
            if (mCurrencyTaskCallback != null) {
                mCurrencyTaskCallback.executionDone();
            }
        }
    }
}
