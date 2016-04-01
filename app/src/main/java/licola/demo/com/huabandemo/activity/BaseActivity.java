package licola.demo.com.huabandemo.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import licola.demo.com.huabandemo.API.ViewInject;
import licola.demo.com.huabandemo.Util.Logger;
import retrofit.Call;

/**
 * Created by LiYi on 2015/11/4 0004 14:59.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected String TAG = getTAG();

    protected abstract int getLayoutId();

    protected abstract String getTAG();

    protected Context mContext;

    @Override
    public String toString() {
        return getClass().getSimpleName() + " @" + Integer.toHexString(hashCode());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2015/1122/3712.html
         * 在BaseActivity.java里：我们通过判断当前sdk_int大于4.4(kitkat),则通过代码的形式设置status bar为透明
         * (这里其实可以通过values-v19 的sytle.xml里设置windowTranslucentStatus属性为true来进行设置，但是在某些手机会不起效，所以采用代码的形式进行设置)。
         * 还需要注意的是我们这里的AppCompatAcitivity是android.support.v7.app.AppCompatActivity支持包中的AppCompatAcitivity,也是为了在低版本的android系统中兼容toolbar。
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(getLayoutId());
        ButterKnife.bind(this);
        mContext = this;
        Logger.d(TAG);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.d(TAG);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.d(TAG);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d(TAG);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Logger.d(TAG);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.d(TAG);
    }
}