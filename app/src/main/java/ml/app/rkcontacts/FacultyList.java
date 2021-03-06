package ml.app.rkcontacts;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ml.app.rkcontacts.helpers.ListViewAdapter;
import ml.app.rkcontacts.helpers.Model;

import static android.content.Context.MODE_PRIVATE;

public class FacultyList extends Fragment {
    String branch, school;
    ListView listView;
    ListViewAdapter adapter;
    String jsondata;

    ArrayList<Model> arrayList = new ArrayList<>();

    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.faculty_list, container, false);
        branch = getArguments().getString("branch");
        school = getArguments().getString("school");

        SharedPreferences prefsjsn = getActivity().getSharedPreferences("data", MODE_PRIVATE);
        jsondata = prefsjsn.getString("bulk", "");
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

//            Toast.makeText(getContext(), branch, Toast.LENGTH_SHORT).show();
            if (branch.equals("ALL")) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("school").equals(school)) {
                        String name = jsonObject.getString("fullname");
                        String email = jsonObject.getString("email");
                        String mobile = jsonObject.getString("mobile");
                        String profile = jsonObject.getString("profile");
                        String ext = jsonObject.getString("ext");
                        String gender = jsonObject.getString("gender");
                        String school = jsonObject.getString("school");
                        String branch = jsonObject.getString("branch");
                        String role = jsonObject.getString("role");
                        Model model = new Model(name, email, profile, mobile, ext, gender, school, branch, role);
                        //bind all strings in an array
                        arrayList.add(model);
                    }
                }
            } else {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
//                Toast.makeText(getContext(), school+branch, Toast.LENGTH_SHORT).show();
                    if (jsonObject.getString("branch").equals(branch) && jsonObject.getString("school").equals(school)) {
                        String name = jsonObject.getString("fullname");
                        String email = jsonObject.getString("email");
                        String mobile = jsonObject.getString("mobile");
                        String profile = jsonObject.getString("profile");
                        String ext = jsonObject.getString("ext");
                        String gender = jsonObject.getString("gender");
                        String school = jsonObject.getString("school");
                        String branch = jsonObject.getString("branch");
                        String role = jsonObject.getString("role");
                        Model model = new Model(name, email, profile, mobile, ext, gender, school, branch, role);
                        //bind all strings in an array
                        arrayList.add(model);
                    }


                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


        //pass results to listViewAdapter class
        adapter = new ListViewAdapter(getContext(), arrayList);

        //bind the adapter to the listview
        listView.setAdapter(adapter);
        getActivity().setTitle("Faculties of " + school + " " + branch);
        return view;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search, menu);

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
}