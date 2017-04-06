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
import android.widget.TextView;

import com.cse40333.satchel.firebaseNodes.Item;
import com.cse40333.satchel.firebaseNodes.UsersItem;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


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

        // Create an adapter for the list of items and attach it to the ListView
        ListView itemListView = (ListView) rootView.findViewById(R.id.item_list_list_view);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mRef = database.getReference("userItems").child(mAuth.getCurrentUser().getUid());
        ListAdapter listAdapter = new FirebaseListAdapter<UsersItem>(getActivity(), UsersItem.class, R.layout.item_list_row, mRef) {
            protected void populateView(View view, UsersItem item, int position) {
                TextView itemName = (TextView) view.findViewById(R.id.item_name);
                TextView itemOwner = (TextView) view.findViewById(R.id.item_owner);
                itemName.setText(item.name);
                itemOwner.setText(item.ownerName);
            }
        };
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
