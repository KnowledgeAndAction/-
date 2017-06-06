package com.example.administrator.my;

import java.util.concurrent.CopyOnWriteArrayList;

import com.sensoro.beacon.kit.Beacon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BeaconsFragment extends Fragment implements OnItemClickListener {

    ListView beaconsListView;
    BeaconAdaper adapter;
    CopyOnWriteArrayList<Beacon> beacons;

    EditText searchEditText;

    Bitmap b0Bitmap;
    Bitmap a0Bitmap;
    Bitmap c0Bitmap;

    HActivity activity;

    static final String GRID_KEY_IMG = "img";
    static final String GRID_KEY_ID = "id";
    static final String GRID_KEY_SN = "sn";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_beacons, container, false);
    }

    private void initCtrl() {
        activity = (HActivity) getActivity();
        beaconsListView = (ListView) activity.findViewById(R.id.fragment_beacons_lv_beacons);
        beaconsListView.setOnItemClickListener(this);
        adapter = new BeaconAdaper(activity);
        beaconsListView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        initActionBar();
        super.onResume();
    }

    private void initActionBar() {
        activity.actionBar.setHomeButtonEnabled(false);
        activity.actionBar.setDisplayHomeAsUpEnabled(false);
        activity.actionBar.setDisplayShowHomeEnabled(false);
        activity.actionBar.setDisplayShowCustomEnabled(true);

        activity.actionBar.setCustomView(activity.actionBarMainLayout);
    }

    private void initDrawable() {
        a0Bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.yunzi_a0);
        b0Bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.yunzi_b0);
        c0Bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.yunzi_4aa);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        initCtrl();
        initDrawable();
        super.onActivityCreated(savedInstanceState);
    }

    public void notifyFresh() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    class BeaconAdaper extends BaseAdapter {

        LayoutInflater layoutInflater;
        HActivity activity;

        BeaconAdaper(HActivity activity) {
            this.activity = activity;
            layoutInflater = LayoutInflater.from(activity);
        }

        @Override
        public int getCount() {
            activity = (HActivity) activity;
            beacons = activity.beacons;
            if (beacons == null) {
                return 0;
            }
            return beacons.size();
        }

        @Override
        public Object getItem(int position) {
            if (beacons == null) {
                return null;
            }

            return beacons.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.fragment_beacons_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.idTextView = (TextView) convertView.findViewById(R.id.fragment_beacons_grid_item_tv_id);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Beacon beacon = beacons.get(position);
            if (beacon == null) {
                return null;
            }
            /*
			 * set model name
			 */
            String model = beacon.getHardwareModelName();
            if (model.equalsIgnoreCase(activity.getString(R.string.a0))) {
                viewHolder.imageView.setImageBitmap(a0Bitmap);
            } else if (model.equalsIgnoreCase(activity.getString(R.string.b0))) {
                viewHolder.imageView.setImageBitmap(b0Bitmap);
            } else if (model.equalsIgnoreCase(activity.getString(R.string.c0))) {
                viewHolder.imageView.setImageBitmap(c0Bitmap);
            }
			/*
			 * set id
			 */
            String id = String.format("ID:%04x-%04x", beacon.getMajor(), beacon.getMinor());
            viewHolder.idTextView.setText(id);
			/*
			 * set sn
			 */
            String sn = String.format("SN:%s", beacon.getSerialNumber());
            viewHolder.snTextView.setText(sn);

            return convertView;
        }
    }

    class ViewHolder {
        ImageView imageView;
        TextView idTextView;
        TextView snTextView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

}
