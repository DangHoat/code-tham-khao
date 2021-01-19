package com.project.gpstracking;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends BaseAdapter{

    private ArrayList<String> _Items;
    private ArrayList<CheckBox> checkBoxes;
    private Context _CurrentContext;
    private static LayoutInflater inflater=null;

    private List<Boolean> list_check;

    public ListAdapter(Context context){

        this._CurrentContext = context;
        this._Items = new ArrayList<>();
        list_check = new ArrayList();
        checkBoxes = new ArrayList<>();

        inflater = ( LayoutInflater )_CurrentContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addChild(String item){
        this._Items.add(item);
        this.list_check.add(false);
    }

    public int getCheckedItem() {
        for(int i = 0; i < list_check.size(); i++) {
            if(list_check.get(i))
                return i;
        }
        return -1;
    }

    @Override
    public int getCount() {
        return this._Items.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return this._Items.get(position);
    }

    public class Holder
    {
        TextView tbChildName;
        CheckBox cbxChoose;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Holder holder = new Holder();
        View rowView = inflater.inflate(R.layout.list_item_1, null);

        try {
            holder.tbChildName = (TextView) rowView.findViewById(R.id.tbChildName);
            holder.cbxChoose = (CheckBox) rowView.findViewById(R.id.cbxChoose);

            if(position < this.checkBoxes.size()) {
                if(this.checkBoxes.get(position) != null) {
                    this.checkBoxes.remove(position);
                    this.checkBoxes.add(position, holder.cbxChoose);
                }
            }
            else {
                this.checkBoxes.add(position, holder.cbxChoose);
            }

            holder.tbChildName.setText(this._Items.get(position));
            holder.cbxChoose.setChecked(this.list_check.get(position));

            holder.cbxChoose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    cbxOnCheckedChanged(isChecked, position);
                }
            });
        }
        catch(Exception ex){
            Log.d("Error", ex.getMessage());
            return null;
        }
        return rowView;
    }

    private void cbxOnCheckedChanged(boolean checked, int position){
        /* uncheck all */
        if(checked) {
            for (int i = 0; i < list_check.size(); i++) {
                list_check.set(i, false);
                if(checkBoxes.get(i) != null)
                    checkBoxes.get(i).setChecked(false);
            }
            list_check.set(position, true);
            if(checkBoxes.get(position) != null)
                checkBoxes.get(position).setChecked(true);
        }
        else {
            list_check.set(position, false);
            if(checkBoxes.get(position) != null)
                checkBoxes.get(position).setChecked(false);
        }
    }
}
