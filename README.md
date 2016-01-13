# <center>ExToast

功能点：  
1.拓展toast显示时间，可以自定义任意时间或一直显示  
2.拓展toast出现与消失动画  

简介:  
我们在Android应用开发中经常会需要在界面上弹出一个对界面操作无影响小提示框来提示用户一些信息，一般都会使用Android原生的Toast类  

```java
Toast.makeText(mContext, "消息内容", Toast.LENGTH_SHORT).show();
```

一开始觉得，挺好用的，就有点什么消息都用Toast显示了。
但是用就了就发现，Toast的默认样式有点丑，显示和消失动画也不符合自己的要求，显示时间也只有SHORT和LONG两种选择，限制太多了。  

---

于是，在阅读了Toast的源码后对Toast进行了拓展，原生Toast包含了以下方法给用户修改显示内容:  

```java
setView(View):void
setDuration(int):void
setMargin(float,float):void
setGravity(int,int,int):void
setText(int):void
setText(CharSequence):void
```

分别是直接替换视图、设置显示时长、设置边距属性、设置显示位置、设置显示文字内容。

基于原生Toast拓展了两个方法:

```java
setDuration(int):void
setAnimations(int):void
```

设置显示时长方法拓展为可以自定义显示时间，参数单位秒，提供三个默认值:`LENGTH_SHORT`,`LENGTH_LONG`,`LENGTH_ALWAYS`,分别对应原生Toast的`LENGTH_SHORT`,`LENGTH_LONG`,以及总是显示。要注意的是总是显示需要在合适的时候自己调用hide()方法隐藏，否则会影响其他窗口看的正常显示。

---

ExToast example:

```java
ExToast exToast = ExToast.makeText(context,"message",ExToast.LENGTH_ALWAYS);
exToast.setAnimations(R.style.anim_view);
exToast.show();
//使用LENGTH_ALWAYS注意在合适的时候调用hide()
exToast.hide();
```

上面的代码可以实现自定义xml窗口动画，以及长时间显示Toast的功能。  
下面看一下`R.style.anim_view`的内容，窗口动画可以通过`@android:windowEnterAnimation`和`@android:windowExitAnimation`定义窗口进场及退场效果

##### `style.xml`

```xml
<style name="anim_view">
    <item name="@android:windowEnterAnimation">@anim/anim_in</item>
    <item name="@android:windowExitAnimation">@anim/anim_out</item>
</style>
```

##### `anim_in.xml`  

```xml
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <translate
        android:fromXDelta="0"
        android:fromYDelta="0"
        android:toXDelta="0"
        android:toYDelta="85"
        android:duration="1"
        />
    <translate
        android:fromXDelta="0"
        android:fromYDelta="0"
        android:toXDelta="0"
        android:toYDelta="-105"
        android:duration="350"
        android:fillAfter="true"
        android:interpolator="@android:anim/decelerate_interpolator"
        />
    <alpha
        android:fromAlpha="0"
        android:toAlpha="1"
        android:duration="100"
        />
    <translate
        android:fromXDelta="0"
        android:fromYDelta="0"
        android:toXDelta="0"
        android:toYDelta="20"
        android:duration="80"
        android:fillAfter="true"
        android:startOffset="350"
        />
</set>
```

##### `anim_out.xml`  

```xml
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <alpha
        android:fromAlpha="1"
        android:toAlpha="0"
        android:duration="800"/>
</set>
```

具体效果请运行demo

---

## ExToast原理解析
使用过Toast都知道Toast只提供了两个长度的时间，分别为`LENGTH_SHORT`,`LENGTH_LONG`,它们的时长分别是2秒和大约3秒，在3秒内的Toast，我们都可以通过toast.cancle()取消显示，但如果要显示一个时长大于3秒的Toast时就无能为力了。  

显示时间问题还不是最致命的，最致命的问题，是系统原生的Toast是呈队列显示出来的，必须要等到前一条Toast消失才会显示下一条。  

相信很多同学都遇到过这个问题，比如我做一个按钮，点击的时候显示一个toast，然后做了个小小的压力测试:狂按保存按钮！于是toast队列排了好长一条，一直在显示，等到一两分钟才结束。  

通过阅读Toast源码，可以看到里面的show()方法:

```java
public void show() {
    if (mNextView == null) {
        throw new RuntimeException("setView must have been called");
    }

    INotificationManager service = getService();
    String pkg = mContext.getPackageName();
    TN tn = mTN;
    tn.mNextView = mNextView;

    try {
        service.enqueueToast(pkg, tn, mDuration);
    } catch (RemoteException e) {
        // Empty
    }
}
```

可以看到Toast的核心显示和隐藏是封装在`INotificationManager`的`enqueueToast`方法中，看到enqueue这个词就知道这是一个队列处理的函数，它的参数分别是packageName，tn对象，持续时间。结合Toast的显示效果我们可以猜测这个方法内部实现是队列显示和隐藏每一个传入的Toast。packageName和持续时间我们都很清楚是什么，剩下的重点就在这个tn对象上了。<font color=#0099ff><b>*那tn对象到底是什么？*</b></font>  


继续阅读Toast源码，可以知道Toast其实是系统虚浮窗的一种具体表现形式，它的核心在于它的一个私有静态内部类`class TN`，它处理了Toast的显示以及隐藏。<font color=#0099ff><b>*所以，我们可以通过反射获取这个TN对象，主动处理Toast的显示和隐藏，而不经过系统Service*</b></font>  

TN类源码:

```java
private static class TN extends ITransientNotification.Stub {
    final Runnable mShow = new Runnable() {
        @Override
        public void run() {
            handleShow();
        }
    };
    final Runnable mHide = new Runnable() {
        @Override
        public void run() {
            handleHide();
            // Don't do this in handleHide() because it is also invoked by handleShow()
            mNextView = null;
        }
    };
    ...
    final Handler mHandler = new Handler();
    ...
    View mView;
    View mNextView;
    WindowManager mWM;
    TN() {
    	final WindowManager.LayoutParams params = mParams;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.format = PixelFormat.TRANSLUCENT;
            params.windowAnimations = com.android.internal.R.style.Animation_Toast;
            params.type = WindowManager.LayoutParams.TYPE_TOAST;
            params.setTitle("Toast");
            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
 	 }
    /**
     * schedule handleShow into the right thread
     */
    @Override
    public void show() {
        if (localLOGV) Log.v(TAG, "SHOW: " + this);
        mHandler.post(mShow);
    }
    /**
     * schedule handleHide into the right thread
     */
    @Override
    public void hide() {
        if (localLOGV) Log.v(TAG, "HIDE: " + this);
        mHandler.post(mHide);
    }
    public void handleShow() {
        ...
        if (mView != mNextView) {
            // remove the old view if necessary
            handleHide();
            mView = mNextView;
            Context context = mView.getContext().getApplicationContext();
            if (context == null) {
                context = mView.getContext();
            }
            mWM = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            ...
            if (mView.getParent() != null) {
                if (localLOGV) Log.v(TAG, "REMOVE! " + mView + " in " + this);
                mWM.removeView(mView);
            }
            ...
            mWM.addView(mView, mParams);
            ...
        }
    }
    private void trySendAccessibilityEvent() {...}
    public void handleHide() {
        ...
        if (mView != null) {
            // note: checking parent() just to make sure the view has
            // been added...  i have seen cases where we get here when
            // the view isn't yet added, so let's try not to crash.
            if (mView.getParent() != null) {
                ...
                mWM.removeView(mView);
            }
            mView = null;
        }
    }
}
```

好吧，上面的代码太长不想看，那就把核心的代码挑出来  

```java
public void show(){
	...
	WindowManager mWM = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
	mWN.addView(mView, mParams);
}

public void hide(){
	...
	WindowManager mWM = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
	mWN.removeView(mView);
}
```

所以，Toast的机制就是往WindowManager添加以及移除view，那只要获得TN对象，重新封装一次show()和hide()方法就可以实现自定义显示时间。

```java
private void initTN() {
    try {
        Field tnField = toast.getClass().getDeclaredField("mTN");
        tnField.setAccessible(true);
        mTN = (ITransientNotification) tnField.get(toast);

        /**调用tn.show()之前一定要先设置mNextView*/
        Field tnNextViewField = mTN.getClass().getDeclaredField("mNextView");
        tnNextViewField.setAccessible(true);
        tnNextViewField.set(mTN, toast.getView());

    } catch (Exception e) {
        e.printStackTrace();
    }
}

public show(){
	initTN();
	mTN.show();
}
```

代码中`mTN`就是从Toast中利用反射获取的对象，类型是`ITransientNotification`，这是从android源码中拿出来的`aidl接口`，匹配TN的类型。主动调用`mTN.show()`方法后就会神奇的发现，Toast长时间存在屏幕中，即使离开了app它依然存在，直到调用`mTN.hide()`后才消失。  

---

Toast显示时间问题已经解决了，还有一个自定义动画的问题。现在回过头再看TN类的初始化方法代码，里面初始化了一个`WindowManager.LayoutParams`对象，做过悬浮窗功能的同学应该都接触过它，下面这一句代码就是定义窗口动画的关键，如果能修改`params.windowAnimations`就能够修改窗口动画。

```java
params.windowAnimations = com.android.internal.R.style.Animation_Toast;
```

很不幸的是，`params`并不是一个公有的属性，那就暴力点继续用反射获取并且修改窗口动画  

```java
private void initTN() {
    try {
        Field tnField = toast.getClass().getDeclaredField("mTN");
        tnField.setAccessible(true);
        mTN = (ITransientNotification) tnField.get(toast);

        /**调用tn.show()之前一定要先设置mNextView*/
        Field tnNextViewField = mTN.getClass().getDeclaredField("mNextView");
        tnNextViewField.setAccessible(true);
        tnNextViewField.set(mTN, toast.getView());

        /**获取params后重新定义窗口动画*/
        Field tnParamsField = mTN.getClass().getDeclaredField("mParams");
        tnParamsField.setAccessible(true);
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) tnParamsField.get(mTN);
        params.windowAnimations = R.style.anim_view;
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

---

## Android黑科技:Toast.不需要权限的系统悬浮窗

上面说到过，Toast其实就是系统悬浮窗的一种具体表现形式，那它跟普通的系统悬浮窗有什么区别呢？  

我们看看Android传统实现悬浮窗的代码:

```java
// 获取应用的Context
mContext = context.getApplicationContext();
// 获取WindowManager
mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
mView = setUpView(context);

final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
// 类型
params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

int flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
params.flags = flags;
params.format = PixelFormat.TRANSLUCENT;
params.width = LayoutParams.MATCH_PARENT;
params.height = LayoutParams.MATCH_PARENT;
params.gravity = Gravity.CENTER;
mWindowManager.addView(mView, params);
```

大部分代码都在初始化`WindowManager.LayoutParams`对象上面了，对比一下Toast内部类TN中初始化的`WindowManager.LayoutParams`，不同的地方在于:

```java
// 类型
params.type = WindowManager.LayoutParams.TYPE_TOAST;
```

上面我们已经使用Toast实现了持久显示的悬浮窗，那普通悬浮窗和Toast悬浮窗除了type这个区别外，最大的区别就是Toast不需要权限！我们在应用中使用Toast的时候并没有设置什么额外的权限，但是传统使用悬浮窗的方式需要权限:

```xml
<!-- 显示顶层浮窗 -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

以上这些都是在用户感知外的，只有开发者知道的区别。在用户感知内的区别目前知道的是Toast不能覆盖到系统status bar上面，而其他类型的悬浮窗大部分可以覆盖status bar，更多区别有待补充。  

`更多资料可参考`  
[Android应用Activity、Dialog、PopWindow、Toast窗口添加机制及源码分析](http://www.sxt.cn/info-6165-u-7399.html)

### 有写的不对的地方请看官们指出