package cn.hicc.information.sensorsignin.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hicc.information.sensorsignin.R;

import java.util.List;

import cn.hicc.information.sensorsignin.model.SignInfo;

/**
 * 管理员界面 具体活动签到信息RecyclerView适配器——陈帅
 */

public class SignInfoAdapter extends RecyclerView.Adapter<SignInfoAdapter.ViewHolder> {

    private List<SignInfo> signInfoList;

    static class ViewHolder extends RecyclerView.ViewHolder {

        View view;
        TextView tv_name;
        TextView tv_inTime;
        TextView tv_outTime;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            tv_inTime = (TextView) itemView.findViewById(R.id.tv_inTime);
            tv_outTime = (TextView) itemView.findViewById(R.id.tv_outTime);
        }
    }

    public SignInfoAdapter(List<SignInfo> signInfoList) {
        this.signInfoList = signInfoList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sign_list_admin, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SignInfo signInfo = signInfoList.get(position);
        holder.tv_name.setText(signInfo.getName());
        holder.tv_inTime.setText(signInfo.getInTime().replace("T", " "));
        holder.tv_outTime.setText(signInfo.getOutTime().replace("T", " "));
    }

    @Override
    public int getItemCount() {
        return signInfoList.size();
    }
}
