package com.alloget.shendul.allowanceapp;

import android.widget.ArrayAdapter;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shendul on 4/10/2018.
 */

public class TwoItemListAdapter extends ArrayAdapter<TwoLineListItem> {

        private Context mContext;
        private List<TwoLineListItem> twoLineList = new ArrayList<>();

        public TwoItemListAdapter(@NonNull Context context, ArrayList<TwoLineListItem> list) {
            super(context, 0 , list);
            mContext = context;
            twoLineList = list;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if(listItem == null)
                listItem = LayoutInflater.from(mContext).inflate(R.layout.two_item_listview, parent,false);

            TwoLineListItem currentAllow = twoLineList.get(position);

            TextView name = (TextView) listItem.findViewById(R.id.name);
            name.setText(currentAllow.getLeftLine());

            TextView release = (TextView) listItem.findViewById(R.id.amount);
            release.setText(currentAllow.getRightLine());

            return listItem;
        }
}
