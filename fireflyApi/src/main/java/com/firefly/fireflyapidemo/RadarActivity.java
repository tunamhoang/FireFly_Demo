package com.firefly.fireflyapidemo;

import android.os.Bundle;
import android.view.KeyEvent;

import com.firefly.api.face.radar.RadarUtil;

public class RadarActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int ret = RadarUtil.handleEvent(event);
        if (ret == RadarUtil.EVENT_HANDLE_RADAR_IN) {
            // Radar báo có vật thể đang hoạt động
            setText(R.id.txt, getTimeShort() + getString(R.string.firefly_api_dictionaries8));
            return true;
        } else if (ret == RadarUtil.EVENT_HANDLE_RADAR_OUT) {
            // Radar báo không có vật thể hoạt động
            setText(R.id.txt, getTimeShort() + getString(R.string.firefly_api_dictionaries9));
            return true;
        } else if (ret == RadarUtil.EVENT_HANDLE_NOTHING_HANDLED) {
            setText(R.id.txt, "");
            return true;
        }

        return super.dispatchKeyEvent(event);
    }
}
