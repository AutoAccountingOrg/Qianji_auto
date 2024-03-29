package cn.dreamn.qianji_auto.ui.utils;

import static cn.dreamn.qianji_auto.ui.utils.HandlerUtil.HANDLE_ERR;
import static cn.dreamn.qianji_auto.ui.utils.HandlerUtil.HANDLE_OK;
import static cn.dreamn.qianji_auto.ui.utils.HandlerUtil.HANDLE_REFRESH;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;

import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.dreamn.qianji_auto.data.database.Helper.Categorys;
import cn.dreamn.qianji_auto.ui.adapter.CategoryAdapter;
import cn.dreamn.qianji_auto.utils.runUtils.Log;

public class CategoryUtils {
    SwipeRecyclerView recyclerView;//列表容器
    CategoryAdapter mAdapter;//数据处理工具
    private final List<Bundle> list = new ArrayList<>();//数据列表
    private String book_id;//当前book
    private final String type;//当前类别
    private final Context mContext;//上下文
    private finishRefresh finish;//刷新结束，回调
    private int topInt = -1;//当前展开或点击的pos
    private boolean expand = false;//是否展开
    private Click click;//点击事件
    private final boolean allowChange;//是否允许修改



    //回调接口
    public interface finishRefresh {
        void onFinish(int state);
    }

    public void refreshData(finishRefresh f) {
        clean();
        finish = f;
        Categorys.getParents(book_id, type, obj -> {
            Bundle[] books = (Bundle[]) obj;
            //Log.i("books" + Arrays.toString(books));
            if (books == null || books.length == 0) {
                HandlerUtil.send(mHandler, HANDLE_ERR);
            } else {
                int len = books.length;
                int line = len / 5;//共有几行
                if (len % 5 != 0) line = line + 1;
                int realLen = line * 5 + line;//取5的整数
                for (int i = 0; i < len; i++) {
                    list.add(books[i]);
                    if ((i) % 5 == 4) {
                        Bundle bundle = new Bundle();
                        bundle.putString("name", null);//保留数据
                        bundle.putBoolean("change", false);//保留数据
                        list.add(bundle);
                    }
                }
                // Log.i("还差"+(realLen-list.size())+"条数据");
                int len2 = realLen - list.size() - 1;
                //长度补全
                for (int j = 0; j < len2; j++) {
                    // Log.i("循环次数+"+j);
                    Bundle bundle = new Bundle();
                    bundle.putString("name", null);//保留数据
                    // bundle.putBoolean("change",true);//保留数据
                    list.add(bundle);
                }
                Bundle bundle = new Bundle();
                bundle.putString("name", null);//保留数据
                bundle.putBoolean("change", false);//保留数据
                list.add(bundle);
                //  Log.i("输出" + list.toString());
                HandlerUtil.send(mHandler, list, HANDLE_REFRESH);
            }

        });

    }

    private void refreshSubData(Bundle item, int position, int left) {
        if (item == null) return;
        if (item.getString("name") == null) return;//为null就不响应

        int real = getItemPos(position);//当前的item
        int real2 = getItemPos(topInt);//上一个item

        // Log.i("real " + real + " real2 " + real2);

        if (real2 != real) {//item不同布局则清除上一个
            closeItem(topInt);
        }
        // 当前布局设置选中
        mAdapter.setSelect(position);
        // 清除上一个布局
        mAdapter.notifyItemChanged(topInt);
        //再次点击，如果已展开则收起
        if (topInt == position && isOpenItem()) {
            closeItem(topInt);

            topInt = position;
            return;
        }

        topInt = position;
        //点击默认展开
        openItem(position, left);

    }

    private final MyHandler mHandler = new MyHandler(this);

    private finishRefresh getFinish() {
        return finish;
    }

    private CategoryAdapter getAdapter() {
        return mAdapter;
    }

    public CategoryUtils(SwipeRecyclerView recyclerView, String book_id, String type, Context context, Boolean allowChange) {

        this.book_id = book_id;
        this.recyclerView = recyclerView;
        this.mAdapter = new CategoryAdapter(context, allowChange);
        this.allowChange = allowChange;
        this.type = type;
        mContext = context;
    }

    private void resetRVHeight() {

        ViewGroup.LayoutParams lp = recyclerView.getLayoutParams();
        int height1 = recyclerView.getHeight();
        int height = ScreenUtils.getScreenHeight(mContext);
        Log.d("屏幕高度：" + height);
        Log.d("recyclerView高度：" + height1);
        int height2 = height - ScreenUtils.dip2px(mContext, 200);//减去底部和顶部
        // Log.d("55dip："+ScreenUtils.dip2px(mContext,55));
        Log.d("计算最大限制高度：" + height2);
        if (height1 > height2) {
            Log.d("超出高度：recyclerView高度" + height1 + "_计算:" + height2);
            lp.height = height2;
            recyclerView.setLayoutParams(lp);
        }


    }

    private void openItem(int position, int left) {

        Bundle item = list.get(position);
        //  Log.i("postion:" + position + " data" + item.toString());
        int real = getItemPos(position);

        Handler mmHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                expand = true;
                Bundle[] bundles = (Bundle[]) msg.obj;
                Bundle bundle = new Bundle();
                bundle.putInt("id", -1);
                bundle.putBundle("item", item);
                bundle.putString("name", null);
                bundle.putString("book_id", item.getString("book_id"));
                bundle.putInt("left", left + 13);
                bundle.putInt("leftRaw", left);
                bundle.putSerializable("data", bundles);
                bundle.putBoolean("change", bundles.length != 0);
                list.set(real - 1, bundle);
                mAdapter.replaceNotNotify(real - 1, bundle);
                mAdapter.notifyItemChanged(real - 1);

                mAdapter.notifyItemChanged(position);
                resetRVHeight();
            }
        };

        Categorys.getChildrens(item.getString("self_id"), book_id, item.getString("type"), allowChange, books -> {
            //      Log.i("子类" + Arrays.toString(books));
            HandlerUtil.send(mmHandler, books, HANDLE_REFRESH);
        });
    }



    public void show(){

        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 5);
        layoutManager.setSpanSizeLookup(new SpecialSpanSizeLookup());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter=new CategoryAdapter(mContext,allowChange);

        mAdapter.setOnItemClickListener(this::OnItemClickListen);
        mAdapter.setOnItemLongClickListener(this::OnLongClickListen);
        mAdapter.setOpenAnimationEnable(false);
        recyclerView.setAdapter(mAdapter);


    }

    private void OnLongClickListen(View view, int position) {
        if (position >= list.size()) return;
        Bundle item = list.get(position);
        if(item.getString("name")==null)return;//为null就不响应
        if(click!=null){
            click.onParentLongClick(item, position);
        }
    }


    private void OnItemClickListen(View view, int position) {

        if (position >= list.size()) return;
        Bundle item= list.get(position);
        int left = view.getLeft();
        if(this.click!=null){
            click.onParentClick(item,position);
        }
        refreshSubData(item,position,left);
    }


    public void setOnClick(Click item){
        this.click=item;
        mAdapter.setOnItemListener((bundle, parent, parentPos) -> {
            if(click!=null){
                click.onItemClick(bundle,parent,parentPos);
            }
        });
    }

    public void refreshData(String book_id, int parent, finishRefresh f) {
        Log.i("ref_book_id", book_id);
        Log.i("ref_book_parent", String.valueOf(parent));

        if (parent != -2) {
            Bundle data = list.get(parent);
            Bundle dataItem = data.getBundle("item");
            int index = list.indexOf(dataItem);
            closeItem(index);//清除
            topInt = -1;
            refreshSubData(dataItem, index, data.getInt("leftRaw"));
        } else refreshData(book_id, f);
        resetRVHeight();
    }

    public void clean() {//全部清除
        list.clear();
        topInt = -1;
        if (mAdapter != null) {
            mAdapter.setSelect(-1);
            mAdapter.refresh(null);
        }
    }

    private void closeItem(int position) {
        //  Bundle item=list.get(position);
        expand = false;
        int real = getItemPos(position);
        Bundle bundle1 = new Bundle();
        bundle1.putString("name", null);//保留数据
        bundle1.putBoolean("change", false);//保留数据
        try {
            list.set(real - 1, bundle1);
            mAdapter.replaceNotNotify(real - 1, bundle1);
            mAdapter.notifyItemChanged(real - 1);
        } catch (Exception e) {
            Log.d("发生越界:" + e.toString());
        }
        resetRVHeight();

    }

    public void refreshData(String book_id, finishRefresh f) {
        this.book_id = book_id;
        refreshData(f);
    }

    private boolean isOpenItem() {
        return expand;
    }

    private int getItemPos(int position) {
        int line = position / 6;
        line = line + 1;
        return line * 6;
    }

    class SpecialSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

        @Override
        public int getSpanSize(int i) {
            if (list.size() <= i) return 1;
            Bundle bundle = list.get(i);
            return bundle.containsKey("change") ? 5 : 1;
        }
    }

    private class MyHandler extends Handler {
        private final WeakReference<CategoryUtils> categoryUtilsWeakReference;

        public MyHandler(CategoryUtils categoryUtilsWeakReference1) {
            categoryUtilsWeakReference = new WeakReference<>(categoryUtilsWeakReference1);
        }

        @Override
        public void handleMessage(Message msg) {
            CategoryUtils categoryUtils = categoryUtilsWeakReference.get();
            if (categoryUtils != null) {
                if (msg.what == HANDLE_REFRESH) {
                    List<Bundle> list = (List<Bundle>) msg.obj;
                    categoryUtils.getAdapter().refresh(list);//全部刷新
                }
                if (categoryUtils.getFinish() != null) {
                    int state = (msg.what == HANDLE_REFRESH) ? HANDLE_OK : HANDLE_ERR;
                    categoryUtils.getFinish().onFinish(state);
                }

            }
        }
    }

    public interface Click {
        void onParentClick(Bundle bundle, int position);//父标签点击

        void onItemClick(Bundle bundle, Bundle parent_bundle, int position);//子标签点击

        void onParentLongClick(Bundle bundle, int position);//父标签长按
    }


}
