package cn.hicc.information.sensorsignin.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hicc.information.sensorsignin.R;

import java.util.List;

import cn.hicc.information.sensorsignin.model.FeedBack;

/**
 * 管理员界面 具体活动签到信息RecyclerView适配器——陈帅
 */

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.ViewHolder> {

    private List<FeedBack> feedbackInfoList;

    static class ViewHolder extends RecyclerView.ViewHolder {

        View view;
        TextView tv_account;
        TextView tv_msg;
        TextView tv_phone;
        TextView tv_version;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            tv_account = (TextView) itemView.findViewById(R.id.tv_account);
            tv_msg = (TextView) itemView.findViewById(R.id.tv_msg);
            tv_phone = (TextView) itemView.findViewById(R.id.tv_phone);
            tv_version = (TextView) itemView.findViewById(R.id.tv_version);
        }
    }

    public FeedbackAdapter(List<FeedBack> feedbackInfoList) {
        this.feedbackInfoList = feedbackInfoList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feedback_list, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FeedBack feedBack = feedbackInfoList.get(position);
        holder.tv_account.setText(feedBack.getAccount());
        holder.tv_msg.setText(feedBack.getMsg());
        holder.tv_phone.setText(feedBack.getPhoneBrand() + " " + feedBack.getPhoneBrandType());
        holder.tv_version.setText(feedBack.getAndroidVersion());
    }

    @Override
    public int getItemCount() {
        return feedbackInfoList.size();
    }
}
