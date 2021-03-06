package ml.app.rkcontacts;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import ml.app.rkcontacts.helpers.GlobalFunctions;
import ml.app.rkcontacts.helpers.InitialPhotoUpdate;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    String temp = "hpanchani813@rku.ac.in";
    //    String temp = "dbusa429@rku.ac.in";
    ImageView login;
    private SpotsDialog progressDialog;
    private GoogleApiClient googleApiClient;
    private static final int REQ_CODE = 9001;
    GlobalFunctions gf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gf = new GlobalFunctions(MainActivity.this);

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions).build();
        login = findViewById(R.id.login);

        progressDialog = new SpotsDialog(this, R.style.Custom);
        progressDialog.setCancelable(false);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                signin();
            }
        });

        if (LoginStatus() == TRUE) {
            Intent i = new Intent(getApplicationContext(), Home.class);
            startActivity(i);
            finish();
        }


//        Intent i=new Intent(this,Home.class);
//        startActivity(i);
//        finish();
    }

    private void signin() {
        progressDialog.show();
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
//        progressDialog.dismiss();
        startActivityForResult(intent, REQ_CODE);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
    }

    private void handleResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            final String email = account.getEmail();
            String profile = "";
            if (!(account.getPhotoUrl() == null)) {
                profile = account.getPhotoUrl().toString();
            }


            if (!email.matches("^[a-zA-Z]+.[a-zA-Z]+@rku.ac.in$") && !email.equals(temp)) {
                Logout();
                progressDialog.dismiss();
                gf.AlertMessage(MainActivity.this, "Please Login with RKU Email ID");
            } else {
                String JSON_URL = getText(R.string.url) + "/getbulk.php";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, JSON_URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (SaveData("bulk", response)) {
                                    if (SaveLogin(email)) {
                                        progressDialog.dismiss();
                                        Intent i = new Intent(getApplicationContext(), Home.class);
                                        startActivity(i);
                                        finish();
                                    }
                                } else {
                                    Logout();
                                    gf.AlertMessage(MainActivity.this, "Error connecting to server. Please try again.");
                                }

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.dismiss();
//                                Logout();
                                gf.AlertMessage(MainActivity.this, "Error connecting to server. Please try again.");
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("data", "hkpanchani");
                        return params;
                    }
                };
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(stringRequest);

                if (!profile.equals("")) {
                    String method = "update";
                    InitialPhotoUpdate updatedp = new InitialPhotoUpdate(this);
                    updatedp.execute(method, profile, email);
                }
            }
        } else {
            progressDialog.dismiss();
            gf.AlertMessage(MainActivity.this, "Google Sign in error.Please use RKU Email ID");
        }
    }

    private void Logout() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                SharedPreferences.Editor logineditor = getSharedPreferences("login", MODE_PRIVATE).edit();
                logineditor.clear().commit();
                logineditor.apply();

                SharedPreferences.Editor dataeditor = getSharedPreferences("data", MODE_PRIVATE).edit();
                dataeditor.clear().commit();
                dataeditor.apply();

            }
        });
    }

    private boolean SaveData(String type, String response) {

        try {
            JSONObject ob = new JSONObject(response);
            JSONArray jsonArray = ob.getJSONArray("faculty");
            if (jsonArray.length() != 0) {
                SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("data", MODE_PRIVATE).edit();
                editor.putString(type, response);
                editor.apply();
                editor.commit();
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean LoginStatus() {
        SharedPreferences prefs = getSharedPreferences("login", Activity.MODE_PRIVATE);
        String username = prefs.getString("username", "");
        if (!username.matches("^[a-zA-Z]+.[a-zA-Z]+@rku.ac.in$") && !username.equals(temp)) {
            return FALSE;
        }
        return TRUE;
    }

    public boolean SaveLogin(String email) {
        if (!email.equals("")) {
            SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("login", MODE_PRIVATE).edit();
            editor.putString("username", email);
            editor.apply();
            return true;
        }
        return false;
    }
}