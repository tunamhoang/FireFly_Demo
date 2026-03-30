package com.firefly.faceEngine.activity;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.firefly.arcterndemo.R;
import com.firefly.faceEngine.App;
import com.firefly.faceEngine.dblib.bean.Person;
import com.firefly.faceEngine.other.FaceInfo;
import com.firefly.faceEngine.utils.MatrixYuvUtils;
import com.firefly.faceEngine.utils.Tools;
import com.firefly.faceEngine.view.FaceView;
import com.firefly.faceEngine.view.GrayInterface;
import com.firefly.faceEngine.view.GraySurfaceView;
import com.firefly.faceEngine.view.LivingInterface;
import com.firefly.faceEngine.view.LivingListener;
import com.intellif.YTLFFaceManager;
import com.intellif.arctern.base.ArcternAttribute;
import com.intellif.arctern.base.ArcternImage;
import com.intellif.arctern.base.ArcternRect;
import com.intellif.arctern.base.AttributeCallBack;
import com.intellif.arctern.base.ExtractCallBack;
import com.intellif.arctern.base.SearchCallBack;
import com.intellif.arctern.base.TrackCallBack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FaceDetectActivity extends BaseActivity implements TrackCallBack, AttributeCallBack, SearchCallBack, ExtractCallBack {
    private ArcternImage irImage = null;
    private ArcternImage rbgImage = null;
    private TextView txt1, txt2, txt3;
    private FaceView faceView;
    private ImageView imgLandmark;
    private GraySurfaceView grayInterface;
    private Map<Long, Person> mMapPeople = new HashMap<>();
    private CountDownTimer mCountDownTimer;
    private YTLFFaceManager YTLFFace = YTLFFaceManager.getInstance();
    private ExecutorService executorService;
    private Future future;
    private FaceInfo faceInfo;

    private int view_width, view_height;
    private int frame_width, frame_height;
    private long lastOnAttributeCallBackTime;
    private long lastOnSearchCallBackTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detect);

        initView();
        getViewWH();
        startCountDownTimer();
        setInfraredFillLight(true); // Bật đèn bù sáng
    }

    private void initView() {
        setActionBarTitle(R.string.app_name);
        txt1 = findViewById(R.id.txt1);
        txt2 = findViewById(R.id.txt2);
        txt3 = findViewById(R.id.txt3);
        faceView = findViewById(R.id.faceview);
        imgLandmark = findViewById(R.id.img_landmark);

        grayInterface = findViewById(R.id.grayInterface);
        grayInterface.setZOrderOnTop(true);
        grayInterface.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        LivingInterface.getInstance().init(this);
        LivingInterface.getInstance().setLivingCallBack(rgbLivingListener);

        GrayInterface.getInstance().init(this);
        GrayInterface.getInstance().setLivingCallBack(irLivingListener);

        YTLFFace.setOnTrackCallBack(this);
        YTLFFace.setOnSearchCallBack(this);
        YTLFFace.setOnAttributeCallBack(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Person> mPeople = App.getInstance().getDbManager().getPersonList();
        for (Person person : mPeople) {
            mMapPeople.put(person.getId(), person);
        }
    }

    @Override
    public void onExtractFeatureListener(ArcternImage arcternImage, byte[][] features, ArcternRect[] arcternRects) {
    }

    @Override
    public void onTrackListener(ArcternImage arcternImage, long[] trackIds, ArcternRect[] arcternRects) {
        if (arcternRects != null) {
            faceView.setFaces(arcternRects, frame_width, frame_height, view_width, view_height);
        }
    }

    // Callback lắng nghe thuộc tính khuôn mặt
    @Override
    public void onAttributeListener(ArcternImage arcternImage, long[] trackIds, ArcternRect[] arcternRects, ArcternAttribute[][] arcternAttributes, int[] landmarks) {
        ArcternAttribute[] attributes = arcternAttributes[0];
        if (attributes.length == 0) {
            return;
        }

        lastOnAttributeCallBackTime = System.currentTimeMillis();
        faceInfo = new FaceInfo(arcternImage, arcternAttributes);
        handleAttribute();
        refreshLogTextView();
    }

    @Override
    public void onSearchListener(ArcternImage arcternImage, long[] trackIds, ArcternRect[] arcternRects, long[] searchIds, int[] landmarks, float[] socre) {
        if (searchIds.length <= 0 || arcternImage == null ||
                faceInfo == null || faceInfo.getFrameId() != arcternImage.frame_id) {
            return;
        }

        lastOnSearchCallBackTime = System.currentTimeMillis();
        faceInfo.setSearchId(searchIds[0]);
        handlePerson();
        handleLandmark(arcternImage, landmarks);
    }

    LivingListener rgbLivingListener = new LivingListener() {
        @Override
        public void livingData(ArcternImage arcternImage) {
            rbgImage = arcternImage;
            frame_width = rbgImage.width;
            frame_height = rbgImage.height;
            if (irImage != null) {
                doDelivery(rbgImage, irImage);
            }
        }
    };

    LivingListener irLivingListener = new LivingListener() {
        @Override
        public void livingData(ArcternImage image) {
            irImage = image;
        }
    };

    // Xử lý bằng thread pool
    private void doDelivery(final ArcternImage rbgImage, final ArcternImage irImage) {
        if (future != null && !future.isDone()) {
            return;
        }

        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }

        future = executorService.submit(new Runnable() {
            @Override
            public void run() {
                LivingInterface.rotateYUV420Degree90(rbgImage); // Xoay 90 độ
                LivingInterface.rotateYUV420Degree90(irImage); // Xoay 90 độ
                MatrixYuvUtils.mirrorForNv21(rbgImage.gdata, rbgImage.width, rbgImage.height);  // Lật gương trái-phải cho dữ liệu RGB
                Tools.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        YTLFFace.doDelivery(rbgImage, irImage);
                    }
                });
            }
        });
    }

    // Xử lý thông tin người dùng
    private void handlePerson() {
        Person person = mMapPeople.get(faceInfo.getSearchId());
        StringBuffer s = new StringBuffer();
        if (person != null) {
            faceView.isRed = false;
            s.append(getTimeShort()).append(person.toString()).append("\n");
        } else {
            faceView.isRed = true;
            s.append(getTimeShort()).append(" ").append(getString(R.string.ytlf_dictionaries12));
        }
        showText(txt2, s);
    }

    // Xử lý thông tin thuộc tính khuôn mặt
    private void handleAttribute() {
        ArcternAttribute[] attributes = faceInfo.getAttributes()[0];

        for (int i = 0; i < attributes.length; i++) {
            ArcternAttribute item = attributes[i];
            switch (i) {
                case ArcternAttribute.ArcternFaceAttrTypeEnum.QUALITY:// Chất lượng khuôn mặt
                    faceInfo.setFaceQualityConfidence(item.confidence);
                    break;

                case ArcternAttribute.ArcternFaceAttrTypeEnum.LIVENESS_IR: // Kiểm tra sống
                    faceInfo.setLiveLabel(item.label);
                    faceInfo.setLivenessConfidence(item.confidence);
                    break;

                case ArcternAttribute.ArcternFaceAttrTypeEnum.FACE_MASK: // Khẩu trang
                    faceInfo.setFaceMask(item.label == ArcternAttribute.LabelFaceMask.MASK);
                    break;

                case ArcternAttribute.ArcternFaceAttrTypeEnum.GENDER: // Giới tính
                    Tools.debugLog("性别:%s", item.toString());
                    faceInfo.setGender(item.label);
                    faceInfo.setGenderConfidence(item.confidence);
                    break;

                case ArcternAttribute.ArcternFaceAttrTypeEnum.AGE: // Tuổi
                    Tools.debugLog("年龄:%s", item.toString());
                    faceInfo.setAge((int) item.confidence);
                    break;
            }
        }
    }

    // Xử lý các điểm mốc khuôn mặt
    private void handleLandmark(ArcternImage arcternImage, int[] landmarks) {
        try {
            Bitmap bitmap = Tools.bgr2Bitmap(arcternImage.gdata, arcternImage.width, arcternImage.height);
            Bitmap landmarksBitmap = Tools.drawPointOnBitmap(bitmap, landmarks);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        imgLandmark.setImageBitmap(landmarksBitmap);
                        imgLandmark.setVisibility(View.VISIBLE);
                    } catch (Throwable e) {
                        Tools.printStackTrace(e);
                    }
                }
            });

            // Lưu bitmap ra bộ nhớ cục bộ
            //saveBitmap2Jpeg(bitmap);
        } catch (Throwable e) {
            Tools.printStackTrace(e);
        }
    }

    // Hiển thị log
    private void refreshLogTextView(){
        StringBuilder attribute = new StringBuilder();

        if (faceInfo.isFaceMask()) {    // Khẩu trang时，不处理人脸质量和活体
            attribute.append(getString(R.string.ytlf_dictionaries8))
                    .append("\n");

        } else {
            attribute.append(getString(R.string.ytlf_dictionaries9))
                    .append("\n");

            attribute.append(getString(R.string.ytlf_dictionaries21))
                    .append(faceInfo.getFaceQualityConfidence())
                    .append("\n");

            if (faceInfo.isLiveness()) {
                attribute.append(getString(R.string.ytlf_dictionaries19))
                        .append(":")
                        .append(faceInfo.getLivenessConfidence())
                        .append("\n");

            } else if (faceInfo.isNotLiveness()) {
                attribute.append(getString(R.string.ytlf_dictionaries20))
                        .append("\n");
                faceView.isRed = true;
                showText(txt2, "--");
                setVisibility(imgLandmark, View.GONE);
            }

            attribute.append(getString(R.string.ytlf_dictionaries45))
                    .append(faceInfo.getGenderString())
                    .append("\n");

            attribute.append(getString(R.string.ytlf_dictionaries46))
                    .append(faceInfo.getAgeString())
                    .append("\n");
        }

        if (!faceInfo.isFaceMask() && faceInfo.getFaceQualityConfidence() < 0.4) {// Không đeo khẩu trang và chất lượng < 0.4
            faceView.isRed = false;
            showText(txt1, "--");
            return;
        }

        showText(txt1, attribute);
    }

    protected void showText(TextView txt, CharSequence msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txt.setText(msg);
            }
        });
    }

    protected void setVisibility(View view, int visibility) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(visibility);
            }
        });
    }

    public static String getTimeShort() {
        return new SimpleDateFormat("HH:mm:ss:SSS ").format(new Date());
    }

    private int times=0;
    // Lưu bitmap ra bộ nhớ cục bộ 10次
    private void saveBitmap2Jpeg(Bitmap bitmap){
        if(times > 10){
            return;
        }

        times ++;
        //"/sdcard/firefly/ytlf_v2/"
        String path = YTLFFaceManager.getInstance().getYTIFFacthPath() + "img/" + System.currentTimeMillis() + ".jpg";
        boolean result = Tools.saveBitmap2Jpeg(bitmap, path);
        Tools.debugLog("result=%s, path=%s", result, path);
    }

    // Lấy chiều cao vùng chứa
    private void getViewWH() {
        ViewTreeObserver vto = faceView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                faceView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                view_width = faceView.getWidth();
                view_height = faceView.getHeight();
            }
        });
    }

    // Làm mới
    private void startCountDownTimer() {
        if (mCountDownTimer != null) {
            return;
        }

        mCountDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (lastOnAttributeCallBackTime + 3000 < System.currentTimeMillis()) {
                    showText(txt1, "--");
                    faceView.isRed = false;
                }

                if (lastOnSearchCallBackTime + 3000 < System.currentTimeMillis()) {
                    showText(txt2, "--");
                    setVisibility(imgLandmark, View.GONE);
                }
            }

            @Override
            public void onFinish() {
                cancel();
            }
        };

        mCountDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            if (mCountDownTimer != null) {
                mCountDownTimer.onFinish();
            }
            setInfraredFillLight(false);
        } catch (Exception e) {
            Tools.printStackTrace(e);
        }
    }
}
