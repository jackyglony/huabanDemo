package licola.demo.com.huabandemo.Module;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.ProgressBar;

import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;
import licola.demo.com.huabandemo.API.OnPinsFragmentInteractionListener;
import licola.demo.com.huabandemo.R;
import licola.demo.com.huabandemo.Util.Constant;
import licola.demo.com.huabandemo.Util.Logger;
import licola.demo.com.huabandemo.Adapter.RecyclerPinsHeadCardAdapter;
import licola.demo.com.huabandemo.Bean.ListPinsBean;
import licola.demo.com.huabandemo.Bean.PinsAndUserEntity;
import licola.demo.com.huabandemo.Base.BaseFragment;
import licola.demo.com.huabandemo.HttpUtils.RetrofitAvatarRx;
import licola.demo.com.huabandemo.View.LoadingFooter;
import licola.demo.com.huabandemo.View.recyclerview.HeaderAndFooterRecyclerViewAdapter;
import licola.demo.com.huabandemo.View.recyclerview.RecyclerViewUtils;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by LiCola on  2015/11/28  18:00
 * 展示各个模块的Fragment 在Main和Module Activity负责展示UI
 */
public class ModuleFragment extends BaseFragment {
    private final float percentageScroll = 0.8f;//滑动距离的百分比
    private int mMaxId = 0;

    protected static final String TYPE_KEY = "TYPE_KEY";
    protected static final String TYPE_TITLE = "TYPE_TITLE";
    protected String type;
    protected String title;
    private static int limit = Constant.LIMIT;


    @Bind(R.id.recycler_list)
    RecyclerView mRecyclerView;
    @Bind(R.id.swipe_refresh_widget)
    SwipeRefreshLayout mSwipeRefresh;
    @Bind(R.id.progressBar_recycler)
    ProgressBar mProgressBar;

    //    private MainRecyclerViewAdapter mAdapter;
//    private RecyclerPinsCardAdapter mAdapter;
    private RecyclerPinsHeadCardAdapter mAdapter;

    private OnPinsFragmentInteractionListener mListener;

    public static ModuleFragment newInstance(String type, String title) {
        ModuleFragment fragment = new ModuleFragment();
        Bundle args = new Bundle();
        args.putString(TYPE_KEY, type);
        args.putString(TYPE_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_module;
    }

    @Override
    protected String getTAG() {
        return this.toString();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        type = args.getString(TYPE_KEY);
        title = args.getString(TYPE_TITLE);
//        EventBus.getDefault().register(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSwipeRefresh.setColorSchemeResources(R.color.pink_300, R.color.pink_500, R.color.pink_700, R.color.pink_900);
        initRecyclerView();
        initListener();
        getHttpFirstAndRefresh();//默认的联网，区分于滑动的联网加载
    }


    private void initRecyclerView() {
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        //// TODO: 2016/3/17 0017 预留选项 应该在设置中 添加一条单条垂直滚动选项
//        LinearLayoutManager layoutManager=new LinearLayoutManager(HuaBanApplication.getInstance());

//        mAdapter = new MainRecyclerViewAdapter(HuaBanApplication.getInstance());

        mAdapter = new RecyclerPinsHeadCardAdapter(mRecyclerView);//正常adapter的初始化
        //转换成headAdapter
        HeaderAndFooterRecyclerViewAdapter headAdapter = new HeaderAndFooterRecyclerViewAdapter(mAdapter);
        mRecyclerView.setAdapter(headAdapter);
        mRecyclerView.setLayoutManager(layoutManager);
        LoadingFooter loadingFooter = new LoadingFooter(getContext());
        loadingFooter.setState(LoadingFooter.State.Loading);
        RecyclerViewUtils.addFootView(mRecyclerView, loadingFooter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());//设置默认动画

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    //滑动停止
//                    Logger.d("滑动停止 position=" + mAdapter.getAdapterPosition());
                    int size = (int) (mAdapter.getItemCount() * percentageScroll);
                    if (mAdapter.getAdapterPosition() >= --size) {
                        getHttpMaxId(mMaxId);
                    }
                } else if (RecyclerView.SCROLL_STATE_DRAGGING == newState) {
                    //用户正在滑动
//                    Logger.d("用户正在滑动 position=" + mAdapter.getAdapterPosition());
                } else {
                    //惯性滑动
//                    Logger.d("惯性滑动 position=" + mAdapter.getAdapterPosition());
                }
            }
        });

    }

    /**
     * 根据max值联网 在滑动时调用 继续加载后续内容
     *
     */
    private void getHttpMaxId(int max) {
         Subscription s= getPinsMax(type,max,limit)
                .map(new Func1<ListPinsBean, List<PinsAndUserEntity>>() {
                    @Override
                    public List<PinsAndUserEntity> call(ListPinsBean listPinsBean) {
                        //取出list对象
                        return listPinsBean.getPins();
                    }
                })
                .filter(new Func1<List<PinsAndUserEntity>, Boolean>() {
                    @Override
                    public Boolean call(List<PinsAndUserEntity> pinsEntities) {
                        //检查非空
                        return pinsEntities.size() > 0;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<PinsAndUserEntity>>() {
                    @Override
                    public void onCompleted() {
                        Logger.d();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d();
                        checkException(e);//检查错误 弹出提示
                    }

                    @Override
                    public void onNext(List<PinsAndUserEntity> pinsEntities) {
                        Logger.d();
                        mMaxId = getMaxId(pinsEntities);
                        mAdapter.addList(pinsEntities);
                    }
                });
        addSubscription(s);
    }

    private Observable<ListPinsBean> getPins(String type,int limit){
        return RetrofitAvatarRx.service.httpTypeLimitRx(type, limit);
    }

    private Observable<ListPinsBean> getPinsMax(String type,int max,int limit){
        return RetrofitAvatarRx.service.httpTypeMaxLimitRx(type, max, limit);
    }

    /**
     * 联网得到内容 每次都会清空之前内容
     *
     */
    private void getHttpFirstAndRefresh() {
        Subscription s= getPins(type,limit)
                .filter(new Func1<ListPinsBean, Boolean>() {
                    @Override
                    public Boolean call(ListPinsBean Bean) {
                        //过滤掉数组为0的next
                        return Bean.getPins().size() != 0;
                    }
                })
                .map(new Func1<ListPinsBean, List<PinsAndUserEntity>>() {
                    @Override
                    public List<PinsAndUserEntity> call(ListPinsBean listPinsBean) {
                        return listPinsBean.getPins();
                    }
                })
                .subscribeOn(Schedulers.io())//发布者的运行线程 联网操作属于IO操作
                .observeOn(AndroidSchedulers.mainThread())//订阅者的运行线程 在main线程中才能修改UI
                .subscribe(new Subscriber<List<PinsAndUserEntity>>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        Logger.d();
                        setRecyclerProgressVisibility(false);
                    }

                    @Override
                    public void onCompleted() {
                        Logger.d();
                        mSwipeRefresh.setRefreshing(false);
                        setRecyclerProgressVisibility(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d(e.toString());
                        mSwipeRefresh.setRefreshing(false);
                        setRecyclerProgressVisibility(true);
                        checkException(e);//检查错误 弹出提示
                    }

                    @Override
                    public void onNext(List<PinsAndUserEntity> result) {
                        Logger.d();
                        //保存maxId值 后续加载需要

                        mMaxId = getMaxId(result);
                        mAdapter.setList(result);
                    }
                });

        addSubscription(s);

    }

    /**
     * true 显示recycler 隐藏progress
     *
     * @param isShowRecycler
     */
    private void setRecyclerProgressVisibility(boolean isShowRecycler) {
        if (mRecyclerView != null) {
            mRecyclerView.setVisibility(isShowRecycler ? View.VISIBLE : View.GONE);
        }
        if (mProgressBar != null) {
            mProgressBar.setVisibility(isShowRecycler ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * 从返回联网结果中保存max值 用于下次联网的关键
     *
     * @param result
     * @return
     */
    private int getMaxId(List<PinsAndUserEntity> result) {
        return result.get(result.size() - 1).getPin_id();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPinsFragmentInteractionListener) {
            mListener = (OnPinsFragmentInteractionListener) context;
        } else {
            throwRuntimeException(context);
        }
    }

    private void initListener() {
        //swipeRefresh 控件的滑动监听
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getHttpFirstAndRefresh();
            }
        });


        mAdapter.setOnClickItemListener(new RecyclerPinsHeadCardAdapter.OnAdapterListener() {
            @Override
            public void onClickImage(PinsAndUserEntity bean, View view) {
                Logger.d();
                EventBus.getDefault().postSticky(bean);
                mListener.onClickItemImage(bean, view);
            }

            @Override
            public void onClickTitleInfo(PinsAndUserEntity bean, View view) {
                Logger.d();
                EventBus.getDefault().postSticky(bean);
                mListener.onClickItemText(bean, view);
            }

            @Override
            public void onClickInfoGather(PinsAndUserEntity bean, View view) {
                Logger.d();
            }

            @Override
            public void onClickInfoLike(PinsAndUserEntity bean, View view) {
                Logger.d();
            }

        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter = null;

//        EventBus.getDefault().unregister(this);
    }
}
