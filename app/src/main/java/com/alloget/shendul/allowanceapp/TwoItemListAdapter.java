package com.alloget.shendul.allowanceapp;

import android.graphics.Color;
import android.util.TypedValue;
import android.widget.ArrayAdapter;
import android.content.Context;
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
        private boolean isAllowance;

        public TwoItemListAdapter(@NonNull Context context, ArrayList<TwoLineListItem> list, boolean flag) {
            super(context, 0 , list);
            mContext = context;
            twoLineList = list;
            isAllowance = flag;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if(listItem == null)
                listItem = LayoutInflater.from(mContext).inflate(R.layout.two_item_listview, parent,false);

            TwoLineListItem currentAllow = twoLineList.get(position);
            TextView nameTV = listItem.findViewById(R.id.name);
            TextView amountTV = listItem.findViewById(R.id.amount);

            if(!isAllowance){
                // change font sizes to transactions specs.
                nameTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                amountTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            } // else don't change sizes, default is set for allowance specs
            // quick formatting and font color change.
            String amount = currentAllow.getRightLine();
            if (amount.charAt(0) == '-'){
                amount = "-$" + amount.substring(1);
                // Change font to red.
                amountTV.setTextColor(Color.parseColor("#f44336"));
            } else {
                amount = "$" + amount;
                amountTV.setTextColor(Color.parseColor("#43a047"));
            }
            nameTV.setText(currentAllow.getLeftLine());
            amountTV.setText(amount);

            return listItem;
        }
}
