/*
 * Copyright (C) 2021 xuexiangjys(xuexiangjys@163.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package cn.dreamn.qianji_auto.ui.fragment.data.regulars;

import static cn.dreamn.qianji_auto.ui.utils.HandlerUtil.HANDLE_ERR;
import static cn.dreamn.qianji_auto.ui.utils.HandlerUtil.HANDLE_OK;
import static cn.dreamn.qianji_auto.ui.utils.HandlerUtil.HANDLE_REFRESH;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.hjq.toast.ToastUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.shehuan.statusview.StatusView;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.enums.CoreAnim;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;
import com.yanzhenjie.recyclerview.touch.OnItemMoveListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import cn.dreamn.qianji_auto.R;
import cn.dreamn.qianji_auto.database.Helper.identifyRegulars;
import cn.dreamn.qianji_auto.ui.adapter.CateItemListAdapter;
import cn.dreamn.qianji_auto.ui.base.BaseFragment;
import cn.dreamn.qianji_auto.ui.components.Loading.LVCircularRing;
import cn.dreamn.qianji_auto.ui.fragment.web.WebViewFragment;
import cn.dreamn.qianji_auto.ui.utils.BottomArea;
import cn.dreamn.qianji_auto.ui.utils.HandlerUtil;
import cn.dreamn.qianji_auto.utils.files.RegularManager;
import cn.dreamn.qianji_auto.utils.runUtils.Log;
import cn.dreamn.qianji_auto.utils.runUtils.Task;
import cn.dreamn.qianji_auto.utils.runUtils.Tool;


@Page(name = "本地识别规则", anim = CoreAnim.slide)
public class localFragment extends BaseFragment {
    private final String type;
    @BindView(R.id.status)
    StatusView statusView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.recycler_view)
    SwipeRecyclerView recyclerView;
    @BindView(R.id.multiple_actions_down)
    FloatingActionsMenu floatingActionButton;
    @BindView(R.id.action_cate)
    FloatingActionButton action_cate;
    @BindView(R.id.action_import)
    FloatingActionButton action_import;
    @BindView(R.id.action_export)
    FloatingActionButton action_export;
    @BindView(R.id.action_delAll)
    FloatingActionButton action_delAll;
    private CateItemListAdapter mAdapter;
    private List<Bundle> list;
    Handler mHandler;

    public localFragment(String type) {
        this.type = type;
    }

    private String getName() {
        switch (type) {
            case "sms":
                return getString(R.string.sms);
            case "notice":
                return getString(R.string.notice);
            case "app":
                return getString(R.string.app);
        }
        return "";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main_data_sort_manager;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFromData();
    }

    @Override
    protected void initViews() {
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HANDLE_ERR:
                        if (statusView != null) statusView.showEmptyView();
                        break;
                    case HANDLE_OK:
                        mAdapter.refresh(list);
                        if (statusView != null) statusView.showContentView();
                        break;
                    case HANDLE_REFRESH:
                        loadFromData();
                        break;

                }
                String d = (String) msg.obj;
                if ((d != null && !d.equals("")))
                    ToastUtils.show(d);
            }
        };
        statusView.setEmptyView(R.layout.fragment_empty_view);
        statusView.setLoadingView(R.layout.fragment_loading_view);

        statusView.setOnEmptyViewConvertListener(viewHolder -> {
            viewHolder.setText(R.id.empty_info, String.format(getString(R.string.no_regular), getName()));
        });
        statusView.setOnLoadingViewConvertListener(viewHolder -> {
            LVCircularRing lv_circularring = viewHolder.getView(R.id.lv_circularring);
            lv_circularring.startAnim();
            viewHolder.setText(R.id.loading_text, getString(R.string.main_loading));
        });
        initLayout();
    }

    @SuppressLint("CheckResult")
    @Override
    protected void initListeners() {

        refreshLayout.setOnRefreshListener(refreshlayout -> {
            loadFromData();
            refreshlayout.finishRefresh(0);//传入false表示刷新失败
        });
        action_cate.setOnClickListener(v -> {

            WebViewFragment.openUrl(this, "file:///android_asset/html/Regulars/index.min.html?type=" + this.type);

        });
        action_import.setOnClickListener(v -> {

            RegularManager.importReg(getContext(), getName(), getType(), new RegularManager.End() {
                @Override
                public void onFinish(int code) {

                }
            });

        });
        action_export.setOnClickListener(v -> {
            RegularManager.output(getContext(), getName(), getType());
        });
        action_delAll.setOnClickListener(v -> {
            BottomArea.msg(getContext(), getString(R.string.log_clean_title), String.format(getString(R.string.reg_clean_body), getName()), getString(R.string.set_sure), getString(R.string.set_cancle), new BottomArea.MsgCallback() {
                @Override
                public void cancel() {

                }

                @Override
                public void sure() {
                    identifyRegulars.clear(getType(), str -> {
                        HandlerUtil.send(mHandler, getString(R.string.log_clean_success), HANDLE_REFRESH);
                    });
                }
            });

        });
    }

    private String getType() {
        return this.type;
    }

    private void initLayout() {
        mAdapter = new CateItemListAdapter(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this::OnItemClickListen);
        mAdapter.setOnMoreClick(item -> {
            BottomArea.msg(getContext(), item.getString("name"), item.getString("des"));

        });
        recyclerView.setLongPressDragEnabled(true);
        recyclerView.setOnItemMoveListener(new OnItemMoveListener() {
            @Override
            public boolean onItemMove(RecyclerView.ViewHolder srcHolder, RecyclerView.ViewHolder targetHolder) {
                // 此方法在Item拖拽交换位置时被调用。
                // 第一个参数是要交换为之的Item，第二个是目标位置的Item。

                // 交换数据，并更新adapter。
                int fromPosition = srcHolder.getAdapterPosition();
                int toPosition = targetHolder.getAdapterPosition();
                Collections.swap(list, fromPosition, toPosition);
                mAdapter.notifyItemMoved(fromPosition, toPosition);
                identifyRegulars.setSort(list.get(fromPosition).getInt("id"), fromPosition);
                identifyRegulars.setSort(list.get(toPosition).getInt("id"), toPosition);

                // 返回true，表示数据交换成功，ItemView可以交换位置。
                return true;
            }

            @Override
            public void onItemDismiss(RecyclerView.ViewHolder viewHolder) {

            }

        });// 监听拖拽，更新UI。
        refreshLayout.setEnableRefresh(true);

    }

    @SuppressLint("CheckResult")
    private void OnItemClickListen(View view, int position) {
        if (list == null || position >= list.size()) return;

        Bundle cate = list.get(position);


        String disable = getString(R.string.disable);
        if (cate.getInt("use") != 1) {
            disable = getString(R.string.enable);
        }
        BaseFragment baseFragment = this;
        String finalDisable = disable;
        BottomArea.list(getContext(), String.format(getString(R.string.assert_change), cate.getString("name")), Arrays.asList(getString(R.string.del), getString(R.string.edit_default), getString(R.string.upload_cloud), getString(R.string.share_share), disable), new BottomArea.ListCallback() {
            @Override
            public void onSelect(int position) {
                switch (position) {
                    case 0:
                        identifyRegulars.del(cate.getInt("id"), () -> {
                            HandlerUtil.send(mHandler, getString(R.string.del_success), HANDLE_REFRESH);
                        });
                        break;
                    case 1:
                        Log.d("test", cate.toString());
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("name", cate.getString("name"));
                        jsonObject.put("text", cate.getString("text"));
                        jsonObject.put("regular", cate.getString("regular"));
                        jsonObject.put("fromApp", cate.getString("fromApp"));

                        jsonObject.put("des", cate.getString("des"));
                        jsonObject.put("identify", cate.getString("identify"));
                        jsonObject.put("tableList", JSONObject.parseObject(cate.getString("tableList")));

                        //包括微信个人收款与群收款
                        WebViewFragment.openUrl(baseFragment, "file:///android_asset/html/Regulars/index.min.html?id=" + cate.getInt("id") + "&type=" + getType() + "&data=" + Uri.encode(jsonObject.toJSONString()));
                        break;

                    case 2:

                        BottomArea.msg(getContext(), getString(R.string.could_title), getString(R.string.could_body), getString(R.string.regular_upload), getString(R.string.regular_know), new BottomArea.MsgCallback() {
                            @Override
                            public void cancel() {

                            }

                            @Override
                            public void sure() {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("name", cate.getString("name"));
                                jsonObject.put("text", cate.getString("text"));
                                jsonObject.put("regular", cate.getString("regular"));
                                jsonObject.put("fromApp", cate.getString("fromApp"));

                                jsonObject.put("des", cate.getString("des"));
                                jsonObject.put("identify", cate.getString("identify"));
                                jsonObject.put("tableList", JSONObject.parseObject(cate.getString("tableList")));
                                RegularManager.outputRegOne(getContext(), getName(), getType(), jsonObject);
                                Tool.goUrl(getContext(), getString(R.string.github_issue_regular));

                            }
                        });


                        break;
                    case 3:
                        JSONObject jsonObject2 = new JSONObject();
                        jsonObject2.put("name", cate.getString("name"));
                        jsonObject2.put("regular", cate.getString("regular"));
                        jsonObject2.put("tableList", cate.getString("tableList"));
                        jsonObject2.put("des", cate.getString("des"));
                        RegularManager.outputRegOne(getContext(), getString(R.string.auto), getType(), jsonObject2, true);
                        break;
                    case 4:
                        if (finalDisable.equals(getString(R.string.disable))) {
                            identifyRegulars.deny(cate.getInt("id"), () -> {
                                HandlerUtil.send(mHandler, getString(R.string.deny_success), HANDLE_REFRESH);
                            });
                        } else {
                            identifyRegulars.enable(cate.getInt("id"), () -> {
                                HandlerUtil.send(mHandler, getString(R.string.enable_success), HANDLE_REFRESH);
                            });
                        }
                }
            }
        });


    }


    public void loadFromData() {
        if (statusView != null) statusView.showLoadingView();
        Task.onThread(() -> {
            identifyRegulars.getAll(type, null, regulars -> {
                if (regulars == null || regulars.length == 0) {
                    HandlerUtil.send(mHandler, HANDLE_ERR);
                } else {
                    list = Arrays.asList(regulars);
                    HandlerUtil.send(mHandler, HANDLE_OK);
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacksAndMessages(null);
    }


}
