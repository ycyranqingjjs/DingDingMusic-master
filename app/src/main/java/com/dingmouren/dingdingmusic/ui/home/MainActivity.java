package com.dingmouren.dingdingmusic.ui.home;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dingmouren.dingdingmusic.Constant;
import com.dingmouren.dingdingmusic.MyApplication;
import com.dingmouren.dingdingmusic.R;
import com.dingmouren.dingdingmusic.base.BaseActivity;
import com.dingmouren.dingdingmusic.bean.MusicBean;
import com.dingmouren.dingdingmusic.service.MediaPlayerService;
import com.dingmouren.dingdingmusic.ui.jk.JKActivity;
import com.dingmouren.dingdingmusic.ui.musicplay.PlayingActivity;
import com.dingmouren.dingdingmusic.ui.personal.PersonalCenterActivity;
import com.dingmouren.dingdingmusic.ui.rock.RockActivity;
import com.dingmouren.dingdingmusic.ui.search.SearchActivity;
import com.dingmouren.dingdingmusic.ui.volkslied.VolksliedActivity;
import com.dingmouren.greendao.MusicBeanDao;
import com.jiongbull.jlog.JLog;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getName();
    @BindView(R.id.fab_user)FloatingActionButton mFabUser;
    @BindView(R.id.fab_music)FloatingActionButton mFabMusic;
    @BindView(R.id.search_bar) MaterialSearchBar mSearchBar;

    private Messenger mServiceMessenger;
    private long exitTime ;//双击退出时用的时间标记

    @Override
    public int setLayoutResourceID() {
        return R.layout.activity_main;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        bindService(new Intent(this, MediaPlayerService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void initView() {
        setupWindowAnimation();


    }

    @Override
    public void initListener() {
        mFabMusic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Intent intent = new Intent(MainActivity.this, PlayingActivity.class);
                intent.putExtra("x", (int) (view.getX() + view.getWidth() / 2));
                intent.putExtra("y", (int) (view.getY() + view.getHeight() / 2));
                startActivity(intent);
                return false;
            }
        });

        mFabUser.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Intent intent = new Intent(MainActivity.this, PersonalCenterActivity.class);
                intent.putExtra("x", (int) (view.getX() + view.getWidth() / 2));
                intent.putExtra("y", (int) (view.getY() + view.getHeight() / 2));
                startActivity(intent);
                return false;
            }
        });

        mSearchBar.setOnClickListener((view -> startActivity(new Intent(MainActivity.this, SearchActivity.class))));
    }

    @Override
    public void initData() {
    }


    @OnClick({R.id.img_randomn, R.id.img_jk, R.id.img_rock, R.id.img_volkslied, R.id.fab_user, R.id.fab_music})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_randomn:
                playRandom();
                break;
            case R.id.img_jk:
                turnToJK(view);
                break;
            case R.id.img_rock:
                turnToRock(view);
                break;
            case R.id.img_volkslied:
                turnToVolkslied(view);
                break;
        }
    }

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mServiceMessenger = new Messenger(iBinder);
            //连接到服务
            if (null != mServiceMessenger) {
                Message msgToService = Message.obtain();
                msgToService.replyTo = mMessengerClient;
                msgToService.what = Constant.MAIN_ACTIVITY;
                try {
                    mServiceMessenger.send(msgToService);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    Messenger mMessengerClient = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msgFromService) {
            switch (msgFromService.what) {
                case Constant.MEDIA_PLAYER_SERVICE_IS_PLAYING://正在播放
                    JLog.e(TAG, "收到消息了");
                    if (1 == msgFromService.arg1) {
                        mFabMusic.setVisibility(View.VISIBLE);
                        Glide.with(MainActivity.this).load(R.mipmap.playing).asGif().diskCacheStrategy(DiskCacheStrategy.NONE).into(mFabMusic);
                    } else if (0 == msgFromService.arg1) {
                        mFabMusic.setVisibility(View.GONE);
                    }
                    break;
            }
            super.handleMessage(msgFromService);
        }
    });


    /**
     * 随心听
     */
    private void playRandom() {
        Intent intent = new Intent(this, PlayingActivity.class);
        intent.putExtra("flag", Constant.MAIN_RANDOM);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    /**
     * 日韩歌曲
     */
    private void turnToJK(View view) {
        startActivity(new Intent(this, JKActivity.class), ActivityOptions.makeSceneTransitionAnimation(this, view, "share_jk").toBundle());
    }

    /**
     * 摇滚歌曲
     */
    private void turnToRock(View view) {
        startActivity(new Intent(this, RockActivity.class), ActivityOptions.makeSceneTransitionAnimation(this, view, "share_rock").toBundle());
    }

    /**
     * 民谣歌曲
     */
    private void turnToVolkslied(View view) {
        startActivity(new Intent(this, VolksliedActivity.class), ActivityOptions.makeSceneTransitionAnimation(this, view, "share_volkslied").toBundle());
    }


    private void setupWindowAnimation() {
        Explode explode = new Explode();
        explode.setDuration(1000);
        Fade fade = new Fade();
        fade.setDuration(500);


        getWindow().setReenterTransition(fade);
        getWindow().setExitTransition(explode);

        getWindow().setSharedElementExitTransition(new ChangeImageTransform());
        getWindow().setSharedElementReenterTransition(new ChangeImageTransform());

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis()-exitTime) > 2000){
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
//        MyApplication.getRefWatcher().watch(this);
    }
}
