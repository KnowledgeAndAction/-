package cn.hicc.information.sensorsignin.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hicc.information.sensorsignin.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.hicc.information.sensorsignin.activity.HistoryDetailActivity;
import cn.hicc.information.sensorsignin.model.HistoryActivity;
import cn.hicc.information.sensorsignin.model.IsInternet;
import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.SpUtil;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import okhttp3.Call;


/**
 * 历史界面——崔国钊
 */

public class HistoryFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener  {

    private List<HistoryActivity> mActiveList = new ArrayList<>();
    private ListView listView;
    private boolean respond;
    private SwipeRefreshLayout mSwipeLayout;
    private static final int REFRESH_COMPLETE = 0X110;
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         view = inflater.inflate(R.layout.activity_bottom_history, container, false);
        initDate(view);
        getActive();
        IsInternet isInternet=new IsInternet();
        //isInternet.isNetworkAvalible(getContext());
        respond=isInternet.isNetworkAvalible(getContext());
        //isInternet.checkNetwork(getActivity());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HistoryActivity historyActivity = mActiveList.get(position);
                Intent intent = new Intent(getContext(), HistoryDetailActivity.class);
                intent.putExtra("ActivityId", historyActivity);
                startActivity(intent);
            }
        });
        return view;
    }

    private void getActive() {

            OkHttpUtils
                    .get()
                    .url(Constant.API_URL + "api/TSign/GetSign")
                    .addParams("userId", SpUtil.getString(Constant.ACCOUNT, ""))
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int i) {
                            mSwipeLayout.setRefreshing(false);
                            if (respond){
                                ToastUtil.show("历史记录响应失败");
                            }else {
                                ToastUtil.show("当前网络不可用，请检查网络连接");
                            }
                        }

                        @Override
                        public void onResponse(String s, int i) {
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                boolean sucessed = jsonObject.getBoolean("sucessed");
                                if (sucessed) {
                                    mActiveList.clear();
                                    JSONArray data = jsonObject.getJSONArray("data");
                                    if (data.length() == 0) {
                                        ToastUtil.show("没有记录");
                                    } else {
                                        for (int j = 0; j < data.length(); j++) {
                                            JSONObject hActivity = data.getJSONObject(j);
                                            String hActivityId = hActivity.getString("ActivityId");
                                            String hStudnetNum = hActivity.getString("StudnetNum");
                                            String hInTime = hActivity.getString("InTime");
                                            String hOutTime = hActivity.getString("OutTime");
                                            String hActivityDescription = hActivity.getString("ActivityDescription");
                                            String hTime = hActivity.getString("Time");
                                            String hLocation = hActivity.getString("Location");
                                            String hActivityName = hActivity.getString("ActivityName");

                                            HistoryActivity historyActivity = new HistoryActivity();
                                            historyActivity.sethActivityId(hActivityId);
                                            historyActivity.sethStudnetNum(hStudnetNum);
                                            historyActivity.sethInTime(hInTime);
                                            historyActivity.sethOutTime(hOutTime);
                                            historyActivity.setActivityDescription(hActivityDescription);
                                            historyActivity.sethActivityName(hActivityName);
                                            historyActivity.sethLocation(hLocation);
                                            historyActivity.sethTime(hTime);
                                            mActiveList.add(historyActivity);
                                        }
                                    }
                                    mSwipeLayout.setRefreshing(false);
                                    HistoryFragment.MyAdapter adapter = new HistoryFragment.MyAdapter();
                                    listView.setAdapter(adapter);
                                } else {
                                    ToastUtil.show("没有记录");
                                    mSwipeLayout.setRefreshing(false);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                mSwipeLayout.setRefreshing(false);
                            }
                        }
                    });

    }

    @Override
    public void fetchData() {
    }


    private void initDate(View view) {
        listView = (ListView) view.findViewById(R.id.lv_history);
        mSwipeLayout= (SwipeRefreshLayout) view.findViewById(R.id.id_swipe_ly);
        mSwipeLayout.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {

        getActive();
    }


    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mActiveList.size();
        }

        @Override
        public HistoryActivity getItem(int position) {
            return mActiveList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_activity_list, parent, false);
                viewHolder = new HistoryFragment.ViewHolder();
                viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.tv_location = (TextView) convertView.findViewById(R.id.tv_location);
                viewHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (HistoryFragment.ViewHolder) convertView.getTag();
            }

            viewHolder.tv_name.setText(getItem(position).gethActivityName());
            viewHolder.tv_location.setText(getItem(position).getLocation().replace("T", " "));
            viewHolder.tv_time.setText(getItem(position).gethTime().replace("T", " ").substring(0, 16));

            return convertView;
        }
    }

    static class ViewHolder {
        TextView tv_name;
        TextView tv_location;
        TextView tv_time;
    }
}
