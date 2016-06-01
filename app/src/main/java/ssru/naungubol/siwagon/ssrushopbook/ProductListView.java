package ssru.naungubol.siwagon.ssrushopbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProductListView extends AppCompatActivity {

    //Explicit
    private TextView nameTextView, surnameTextView, moneyTextView;
    private ListView listView;
    private String[] loginStrings, nameStrings, priceStrings,
            coverStrings, eBookStrings;
    private String urlJSON = "http://swiftcodingthai.com/ssru/get_product.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list_view);

        //Bind Widget
        nameTextView = (TextView) findViewById(R.id.textView7);
        surnameTextView = (TextView) findViewById(R.id.textView8);
        moneyTextView = (TextView) findViewById(R.id.textView9);
        listView = (ListView) findViewById(R.id.listView);

        //Receive Value From Intent
        loginStrings = getIntent().getStringArrayExtra("Login");

        //Show View
        nameTextView.setText(loginStrings[1]);
        surnameTextView.setText(loginStrings[2]);
        moneyTextView.setText(loginStrings[5] + " THB.");


        SynchroizeProduct synchroizeProduct = new SynchroizeProduct(this, urlJSON);
        synchroizeProduct.execute();


    }   // Main Method

    private class SynchroizeProduct extends AsyncTask<Void, Void, String> {

        private Context context;
        private String urlstring;
        private ProgressDialog progressDialog;

        public SynchroizeProduct(Context context,
                                 String urlstring) {
            this.context = context;
            this.urlstring = urlstring;
        }   //  Constructor

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(context, "Load Product",
                    "Load Product Process ...");

        }   // onPre

        @Override
        protected String doInBackground(Void... voids) {

            try {

                OkHttpClient okHttpClient = new OkHttpClient();
                Request.Builder builder = new Request.Builder();
                Request request = builder.url(urlstring).build();
                Response response = okHttpClient.newCall(request).execute();
                return response.body().string();

            } catch (Exception e) {
                Log.d("1JuneV1", "doIn e ==> " + e.toString());
                return null;
            }

        }   //doInBack


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.length() !=0) {
                progressDialog.dismiss();
                Log.d("1JuneV1", "s ==> " + s);
            }

            try {

                JSONArray jsonArray = new JSONArray(s);

                nameStrings = new String[jsonArray.length()];
                priceStrings = new String[jsonArray.length()];
                coverStrings = new String[jsonArray.length()];
                eBookStrings = new String[jsonArray.length()];

                for (int i=0;i<jsonArray.length();i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    nameStrings[i] = jsonObject.getString("Name");
                    priceStrings[i] = jsonObject.getString("Price");
                    coverStrings[i] = jsonObject.getString("Cover");
                    eBookStrings[i] = jsonObject.getString("Ebook");

                }   // for

                MyAdapter myAdapter = new MyAdapter(context, nameStrings,
                        priceStrings, coverStrings);
                listView.setAdapter(myAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int i, long id) {

                        if (checkMoney(priceStrings[i])) {

                            confirmDialog(nameStrings[i], priceStrings[i], eBookStrings[i]);

                        } else {

                            MyAlert myAlert = new MyAlert();
                            myAlert.myDialog(context, "เงินไม่พอ", "กรุณาเลือกเล่มใหม่");

                        }

                    }   // onItemClick
                });

            } catch (Exception e) {
                Log.d("1JuneV2", "onPost e ==> " + e.toString());
            }

        }   // onPost

        private boolean checkMoney(String priceString) {

            int intMyMoney = Integer.parseInt(loginStrings[5]);
            int intPrice = Integer.parseInt(priceString);

            if (intMyMoney >= intPrice) {
                return true;
            } else {
                return false;
            }

        }

    }   //Synclass

    private void confirmDialog(final String nameString,
                               final String priceString,
                               final String eBookString) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.icon_myaccount);
        builder.setCancelable(false);
        builder.setTitle("Confirm Order");
        builder.setMessage(nameString + " ราคา " + priceString + " THB." + ""
                + "\n" + "จริงๆ หรือ ?");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("Order", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Intent intent = new Intent(ProductListView.this, ReadPDF.class);
                intent.putExtra("Login", loginStrings);
                intent.putExtra("Namebook", nameString);
                intent.putExtra("PriceBook", priceString);
                intent.putExtra("urlEbook", eBookString);
                startActivity(intent);

                dialogInterface.dismiss();
            }
        });
        builder.show();

    }   // confirm


}   // Main Class

