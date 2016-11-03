package com.example.marco.talkmap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by marco on 16/10/28.
 */
public class Adapter_msg extends BaseAdapter {

    LayoutInflater inflater;
    Context mycontext;
    ArrayList<Obj_Msg> data_msg = new ArrayList<Obj_Msg>();

    public Adapter_msg(Context context) {
        this.mycontext = context;
        inflater = LayoutInflater.from(mycontext);

    }

    public void change(ArrayList<Obj_Msg> data_msg) {
        this.data_msg = data_msg;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data_msg.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = inflater.inflate(R.layout.adapter_msg_item, null);

        TextView msg_item_name = (TextView) convertView.findViewById(R.id.msg_item_name);
        TextView msg_item_msg = (TextView) convertView.findViewById(R.id.msg_item_msg);
        msg_item_name.setText(data_msg.get(position).getName().toString());
        msg_item_msg.setText(data_msg.get(position).getMsg().toString());


        return convertView;
    }
}
