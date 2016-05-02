package licola.demo.com.huabandemo.SearchResult;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import java.util.List;

import de.greenrobot.event.EventBus;
import licola.demo.com.huabandemo.API.OnPinsFragmentInteractionListener;
import licola.demo.com.huabandemo.Adapter.RecyclerPinsHeadCardAdapter;
import licola.demo.com.huabandemo.Base.BaseRecyclerHeadFragment;
import licola.demo.com.huabandemo.Bean.PinsAndUserEntity;
import licola.demo.com.huabandemo.HttpUtils.RetrofitService;
import licola.demo.com.huabandemo.Util.Logger;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by LiCola on  2016/04/05  16:48
 */
public class ResultPinsFragment extends BaseRecyclerHeadFragment<RecyclerPinsHeadCardAdapter, List<PinsAndUserEntity>> {
    private static final String TAG = "ResultPinsFragment";

    private int mIndex = 1;//联网的起始页 默认1


    private OnPinsFragmentInteractionListener mListener;

    @Override
    protected String getTAG() {
        return this.toString();
    }

    //只需要一个Key作为关键字联网
    public static ResultPinsFragment newInstance(String key) {
        ResultPinsFragment fragment = new ResultPinsFragment();
        Bundle args = new Bundle();
        args.putString(TYPE_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected Subscription getHttpFirst() {
        return RetrofitService.createAvatarService()
                .httpsImageSearchRx(mAuthorization,mKey, mIndex, mLimit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<SearchImageBean, List<PinsAndUserEntity>>() {
                    @Override
                    public List<PinsAndUserEntity> call(SearchImageBean searchImageBean) {
                        return searchImageBean.getPins();//取出list对象
                    }
                })
                .filter(getFilterFunc1())
                .subscribe(new Subscriber<List<PinsAndUserEntity>>() {
                    @Override
                    public void onCompleted() {
                        Logger.d();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d(e.toString());
                        checkException(e);
                    }

                    @Override
                    public void onNext(List<PinsAndUserEntity> pinsAndUserEntities) {
                        Logger.d();
                        mAdapter.addListNotify(pinsAndUserEntities);
                        mIndex++;
                    }
                });
    }

    @Override
    protected Subscription getHttpScroll() {
        return getHttpFirst();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPinsFragmentInteractionListener) {
            mListener = (OnPinsFragmentInteractionListener) context;
        } else {
            throwRuntimeException(context);
        }

        if (context instanceof SearchResultActivity){
            mAuthorization=((SearchResultActivity) context).mAuthorization;
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        mAdapter.setOnClickItemListener(new RecyclerPinsHeadCardAdapter.OnAdapterListener() {
            @Override
            public void onClickImage(PinsAndUserEntity bean, View view) {
                EventBus.getDefault().postSticky(bean);
                mListener.onClickPinsItemImage(bean, view);
            }

            @Override
            public void onClickTitleInfo(PinsAndUserEntity bean, View view) {
                EventBus.getDefault().postSticky(bean);
                mListener.onClickPinsItemText(bean, view);
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
    protected View getHeadView() {
        return null;
    }

    @Override
    protected int getAdapterPosition() {
        return mAdapter.getAdapterPosition();
    }

    @Override
    protected RecyclerPinsHeadCardAdapter setAdapter() {
        return new RecyclerPinsHeadCardAdapter(mRecyclerView);
    }

}