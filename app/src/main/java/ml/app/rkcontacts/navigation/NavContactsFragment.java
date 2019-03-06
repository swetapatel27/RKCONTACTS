package ml.app.rkcontacts.navigation;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ml.app.rkcontacts.ListViewAdapter;
import ml.app.rkcontacts.Model;
import ml.app.rkcontacts.R;

import static android.content.Context.MODE_PRIVATE;

public class NavContactsFragment extends Fragment {
    ListView listView;
    ListViewAdapter adapter;

    String jsondata;

    ArrayList<Model> arrayList = new ArrayList<Model>();

    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.nav_fragment_contacts, container, false);


        SharedPreferences prefsjsn = getActivity().getSharedPreferences("data", MODE_PRIVATE);
        jsondata = prefsjsn.getString("bulk", "");
        UpdateData();
        getActivity().setTitle("Contacts");


        listView = view.findViewById(R.id.listView);
        if (arrayList != null) {
            arrayList.clear();
        } else {
            arrayList = new ArrayList<>();
        }


        try {
            JSONObject ob = new JSONObject(jsondata);
            JSONArray jsonArray = ob.getJSONArray("faculty");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("fullname");
                String email = jsonObject.getString("email");
                String mobile = jsonObject.getString("mobile");
                String profile = jsonObject.getString("profile");
                String ext = jsonObject.getString("ext");
                Model model = new Model(name, email, profile);
                //bind all strings in an array
                arrayList.add(model);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        //pass results to listViewAdapter class
        adapter = new ListViewAdapter(getContext(), arrayList);

        //bind the adapter to the listview
        listView.setAdapter(adapter);


        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);

        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (TextUtils.isEmpty(s)) {
                    adapter.filter("");
                    listView.clearTextFilter();
                } else {
                    adapter.filter(s);
                }
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.update_contact_data) {
            UpdateData();
            RefreshFragment();
        }
        return super.onOptionsItemSelected(item);
    }

    private void UpdateData() {
        String JSON_URL = "http://rkuinfo.ml/getbulk.php?data=hkpanchani";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, JSON_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        SaveData("bulk", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), "Getting error Please Check Network Connection", Toast.LENGTH_SHORT).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(stringRequest);
    }

    private void RefreshFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

    private boolean SaveData(String type, String response) {
        if (!response.equals("")) {
            SharedPreferences.Editor editor = getContext().getApplicationContext().getSharedPreferences("data", MODE_PRIVATE).edit();
            editor.putString(type, response);
            editor.apply();
            editor.commit();
            return true;
        }
        return false;
    }


}