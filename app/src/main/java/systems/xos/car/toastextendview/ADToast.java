package systems.xos.car.toastextendview;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Created by ykj on 15-12-25.
 */
public class ADToast implements View.OnTouchListener {

    Context mContext;
    WindowManager.LayoutParams params;
    WindowManager mWM;
    View mView;

    private float mTouchStartX;
    private float mTouchStartY;
    private float x;
    private float y;

    public ADToast(Context context){
        this.mContext = context;
        params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.format = PixelFormat.TRANSLUCENT;
        params.windowAnimations = R.style.anim_view;
        params.type = WindowManager.LayoutParams.TYPE_TOAST;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.setTitle("Toast");
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWM = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflate = (LayoutInflater)
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflate.inflate(R.layout.float_tips_layout, null);
        mView.setOnTouchListener(this);
    }

    public void show(){
        TextView tv = (TextView)mView.findViewById(R.id.message);
        tv.setText("黑科技");
        if (mView.getParent() != null) {
            mWM.removeView(mView);
        }
        mWM.addView(mView, params);
    }

    public void hide(){
        if(mView!=null){
            mWM.removeView(mView);
        }
    }

    public void setText(String text){
        TextView tv = (TextView)mView.findViewById(R.id.message);
        tv.setText(text);
    }

    private void updateViewPosition(){
        //更新浮动窗口位置参数
        params.x=(int) (x-mTouchStartX);
        params.y=(int) (y-mTouchStartY);
        mWM.updateViewLayout(mView, params);  //刷新显示
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //获取相对屏幕的坐标，即以屏幕左上角为原点
        x = event.getRawX();
        y = event.getRawY();
        Log.i("currP", "currX"+x+"====currY"+y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:    //捕获手指触摸按下动作
                //获取相对View的坐标，即以此View左上角为原点
                mTouchStartX =  event.getX();
                mTouchStartY =  event.getY();
                Log.i("startP","startX"+mTouchStartX+"====startY"+mTouchStartY);
                break;
            case MotionEvent.ACTION_MOVE:   //捕获手指触摸移动动作
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:    //捕获手指触摸离开动作
                updateViewPosition();
                break;
        }
        return true;
    }
}
