package systems.xos.car.toastextendview;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;

public class MainActivity extends Activity {

    private MiExToast miToast;
    private ExToast exToast;
    ADToast adToast;
    FloatTips floatTips;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        exToast = ExToast.makeText(this,"Test ExToast",ExToast.LENGTH_ALWAYS);
//        exToast.setAnimations(R.style.anim_view);
//        exToast.show();

        miToast = new MiExToast(getApplicationContext());
        miToast.setDuration(MiExToast.LENGTH_ALWAYS);
        miToast.setAnimations(R.style.anim_view);
        miToast.show();

//        adToast = new ADToast(this.getApplicationContext());
//        adToast.show();

//        floatTips = new FloatTips(this.getApplicationContext());
//        floatTips.setGravity(FloatTips.TOP);
//        floatTips.setText("GPS信号丢失,重新搜索中...");
//        floatTips.setDuration(FloatTips.LENGTH_ALWAYS);
//        floatTips.show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        exToast.hide();
//        adToast.setText("hehehe");
//        floatTips.show();
//        floatTips.setText("xxx");
        return super.onTouchEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        floatTips.hideImmediate();
//        floatTips.hide();
//        floatTips.hide();
//        exToast.hide();
    }

}

