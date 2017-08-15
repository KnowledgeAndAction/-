package cn.hicc.information.sensorsignin.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hicc.information.sensorsignin.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.hicc.information.sensorsignin.activity.HistoryActivity;
import cn.hicc.information.sensorsignin.db.MyDatabase;
import cn.hicc.information.sensorsignin.model.AnalyzeSign;
import cn.hicc.information.sensorsignin.model.HistoryActive;
import cn.hicc.information.sensorsignin.utils.Constant;
import cn.hicc.information.sensorsignin.utils.SpUtil;
import cn.hicc.information.sensorsignin.utils.ToastUtil;
import cn.hicc.information.sensorsignin.utils.Utils;
import okhttp3.Call;
import rorbin.q.radarview.RadarData;
import rorbin.q.radarview.RadarView;


/**
 * 历史界面
 */

public class HistoryFragment2 extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private List<HistoryActive> mActiveList = new ArrayList<>();
    private boolean respond;
    private SwipeRefreshLayout mSwipeLayout;
    private View view;
    private RadarView mRadarView;
    private MyDatabase db;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_history2, container, false);

        db = MyDatabase.getInstance();

        // 判断网络是否可用
        respond = Utils.isNetworkAvalible(getContext());

        initView(view);

        // 获取活动信息
        getActive();

        return view;
    }

    private void initData() {
        List<String> vertexText = new ArrayList<>();
        Collections.addAll(vertexText, "力量", "敏捷", "速度", "智力", "精神");
        mRadarView.setVertexText(vertexText);

        List<Float> values = new ArrayList<>();
        Collections.addAll(values, 3f, 6f, 2f, 7f, 5f);
        RadarData data = new RadarData(values);

        mRadarView.addData(data);

        mRadarView.animeValue(2000);
    }

    // 获取历史活动
    private void getActive() {
        db.deleteAnalyzeSign();
        OkHttpUtils
                .get()
                .url(Constant.API_URL + "api/TSign/GetSign")
                .addParams("userId", SpUtil.getString(Constant.ACCOUNT, ""))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        mSwipeLayout.setRefreshing(false);
                        if (respond) {
                            ToastUtil.show("历史记录响应失败:"+e.toString());
                        } else {
                            ToastUtil.show("当前网络不可用，请检查网络连接");
                        }
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            boolean sucessed = jsonObject.getBoolean("sucessed");
                            mActiveList.clear();
                            if (sucessed) {
                                JSONArray data = jsonObject.getJSONArray("data");
                                if (data.length() == 0) {
                                    ToastUtil.show("没有历史记录");
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

                                        // 如果是完成签离的活动才展示
                                        if (!hInTime.equals(hOutTime)) {
                                            HistoryActive historyActive = new HistoryActive();
                                            historyActive.sethActivityId(hActivityId);
                                            historyActive.sethStudnetNum(hStudnetNum);
                                            historyActive.sethInTime(hInTime);
                                            historyActive.sethOutTime(hOutTime);
                                            historyActive.setActivityDescription(hActivityDescription);
                                            historyActive.sethActivityName(hActivityName);
                                            historyActive.sethLocation(hLocation);
                                            historyActive.sethTime(hTime);
                                            historyActive.setEndTime(hActivity.getString("EndTime"));
                                            mActiveList.add(historyActive);

                                            AnalyzeSign analyzeSign = new AnalyzeSign();
                                            analyzeSign.setNumber(hStudnetNum);
                                            analyzeSign.setActiveName(hActivityName);
                                            analyzeSign.setRule(Integer.valueOf(hActivity.getString("Rule")));
                                            analyzeSign.setInTime(hInTime);
                                            analyzeSign.setOutTime(hOutTime);
                                            analyzeSign.setTime(hTime);
                                            analyzeSign.setEndTime(hActivity.getString("EndTime"));
                                            db.saveAnalyzeSign(analyzeSign);
                                        }
                                    }
                                }
                            } else {
                                ToastUtil.show("获取历史记录失败");
                            }
                            mSwipeLayout.setRefreshing(false);
                            initData();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mSwipeLayout.setRefreshing(false);
                            ToastUtil.show("获取历史记录失败："+e.toString());
                        }
                    }
                });
    }

    @Override
    public void fetchData() {
    }

    private void initView(View view) {
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.id_swipe_ly);
        mSwipeLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark);
        mSwipeLayout.setOnRefreshListener(this);

        mRadarView = (RadarView) view.findViewById(R.id.radarView);
        mRadarView.setEmptyHint("无数据");

        // 设置线条颜色
        List<Integer> layerColor = new ArrayList<>();
        Collections.addAll(layerColor, 0x3300bcd4, 0x3303a9f4, 0x335677fc, 0x333f51b5, 0x33673ab7);
        mRadarView.setLayerColor(layerColor);

        TextView tv_detail = (TextView) view.findViewById(R.id.tv_detail);
        tv_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), HistoryActivity.class));
            }
        });
    }

    @Override
    public void onRefresh() {
        getActive();
        initData();
    }
}
