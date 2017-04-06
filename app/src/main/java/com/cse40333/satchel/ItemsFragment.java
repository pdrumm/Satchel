package com.cse40333.satchel;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ItemsFragment extends Fragment {

    // Firebase references
    private FirebaseAuth mAuth;

    public ItemsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get current user
        mAuth = FirebaseAuth.getInstance();

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_items, container, false);

        // Populate the List
        ArrayList<String[]> arrayList = new ArrayList<>();
        String[] temp1 = {"Car", "Patrick"};
        String[] temp2 = {"Basketball", "James"};
        arrayList.add(temp1);
        arrayList.add(temp2);

        // Create an adapter for the list of items and attach it to the ListView
        ListView itemListView = (ListView) rootView.findViewById(R.id.item_list_list_view);
        ListAdapter listAdapter = new ItemListAdapter(getContext(), arrayList);
        itemListView.setAdapter(listAdapter);

        // Handler for New Item FAB
        View.OnClickListener newItemClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), NewItemActivity.class);
                startActivity(intent);
            }
        };
        FloatingActionButton newItemFab = (FloatingActionButton) rootView.findViewById(R.id.add_list_item);
        newItemFab.setOnClickListener(newItemClick);

        // Inflate the layout for this fragment
        return rootView;
    }

}
