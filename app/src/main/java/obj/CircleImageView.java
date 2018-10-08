package obj;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import cn.easyar.samples.helloar.R;

/**
 * Created by HPA on 2017/11/8.
 */

public class CircleImageView extends android.support.v7.widget.AppCompatImageView{

    private static final ScaleType SCALE_TYPE = ScaleType.CENTER_CROP;

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = 1;

    private static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;

    private final RectF mDrawableRect = new RectF();
    private final RectF mBorderRect = new RectF();

    private final Matrix mShaderMatrix = new Matrix();
    private final Paint mBitmapPaint = new Paint();
    private final Paint mBorderPaint = new Paint();

    private int mBorderColor = DEFAULT_BORDER_COLOR;
    private int mBorderWidth = DEFAULT_BORDER_WIDTH;

    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;
    private int mBitmapWidth;
    private int mBitmapHeight;

    private float mDrawableRadius;
    private float mBorderRadius;

    private boolean mReady;
    private boolean mSetupPending;

    public CircleImageView(Context context) {
        super(context);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setScaleType(SCALE_TYPE);

        TypedArray array=context.obtainStyledAttributes(attrs, R.styleable.CircleImageView,defStyleAttr,0);

        mBorderWidth=array.getDimensionPixelSize(R.styleable.CircleImageView_civ_border_width,DEFAULT_BORDER_WIDTH);
        mBorderColor=array.getColor(R.styleable.CircleImageView_civ_border_color,DEFAULT_BORDER_COLOR);
        array.recycle();

        mReady=true;
        if (mSetupPending){
            setup();
            mSetupPending=false;
        }
    }

    @Override
    public ScaleType getScaleType() {
        return SCALE_TYPE;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType!=SCALE_TYPE){
            throw new IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable()==null){
            return;
        }
        canvas.drawCircle(getWidth()/2,getHeight()/2,mDrawableRadius,mBitmapPaint);
        canvas.drawCircle(getWidth()/2,getHeight()/2,mBorderRadius,mBorderPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }


    private int getBorderColor(){
        return mBorderColor;
    }

    private void serBorderColor(int borderColor){
         if (borderColor==mBorderColor){
             return;
         }
         mBorderColor=borderColor;
        mBorderPaint.setColor(mBorderColor);
        invalidate();
    }
    private int getBorderWidth(){
        return mBorderWidth;
    }
    private void setBorderWidth(int borderWidth){
          if (borderWidth==mBorderWidth){
              return;
          }
          mBorderWidth=borderWidth;
          setup();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mBitmap=bm;
        setup();
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        mBitmap=getBitmapFromDrawable(drawable);
        setup();
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        super.setImageResource(resId);
        mBitmap=getBitmapFromDrawable(getDrawable());
        setup();
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    private void setup(){
        if (!mReady){
            mSetupPending=true;
            return;
        }
        if (mBitmap==null){
            return;
        }
        mBitmapShader=new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setShader(mBitmapShader);

        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);

        mBitmapHeight=mBitmap.getHeight();
        mBitmapWidth=mBitmap.getWidth();

        mBorderRect.set(0,0,getWidth(),getHeight());
        mBorderRadius=Math.min((mBorderRect.height()-mBorderWidth)/2,(mBorderRect.width()-mBorderWidth)/2);

        mDrawableRect.set(mBorderWidth,mBorderWidth,mBorderRect.width()-mBorderWidth,mBorderRect.height()-mBorderWidth);
        mDrawableRadius=Math.min(mDrawableRect.height()/2,mDrawableRect.width()/2);

        updateShaderMatrix();
        invalidate();
    }

    private void updateShaderMatrix() {
        float sclae;
        float dx=0;
        float dy=0;
        mShaderMatrix.set(null);

        if (mBitmapWidth*mDrawableRect.height()>mDrawableRect.width()*mBitmapHeight){
            sclae=mDrawableRect.height()/(float) mBitmapHeight;
            dx=(mDrawableRect.height()-mBitmapWidth*sclae)*5f;
        }else {
            sclae=mDrawableRect.width()/(float) mBitmapWidth;
            dy=(mDrawableRect.width()-mBitmapHeight*sclae)*5f;
        }
        mShaderMatrix.setScale(sclae,sclae);
        mShaderMatrix.postTranslate((int)(dx+5f)+mBorderWidth,(int)(dy+5f)+mBorderWidth);

        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }

}
