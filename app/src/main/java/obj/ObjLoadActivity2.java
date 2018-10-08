package obj;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import cn.easyar.samples.helloar.R;

/**
 * Created by wuwang on 2017/2/23
 */

public class ObjLoadActivity2 extends AppCompatActivity implements SensorEventListener {

    private GLSurfaceView mGLView;
    private List<ObjFilter2> filters;
    private float xAngle;
    private float yAngle;
    private float zAngle;
    private SensorManager sensorManager = null;
    private Sensor sensor = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obj);
        mGLView = (GLSurfaceView) findViewById(R.id.mGLView);
        mGLView.setEGLContextClientVersion(2);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        List<Obj3D> model = ObjReader.readMultiObj(this, "assets/3dres/pikachu.obj");
        filters = new ArrayList<>();
        for (int i = 0; i < model.size(); i++) {
            ObjFilter2 f = new ObjFilter2(getResources());
            f.setObj3D(model.get(i));
            filters.add(f);
        }
        mGLView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                for (ObjFilter2 f : filters) {
                    f.create();
                }
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                for (ObjFilter2 f : filters) {
                    f.onSizeChanged(width, height);
                    float[] matrix = Gl2Utils.getOriginalMatrix();
                    //模型变换
                    Matrix.translateM(matrix, 0, 0, -0.3f, 0);//物体平移
                    Matrix.scaleM(matrix, 0, 0.008f, 0.008f * width / height, 0.008f);//物体按坐标比例缩放
                    Matrix.rotateM(matrix, 0, 180, 0, 1, 0);//物体旋转
                    f.setMatrix(matrix);
                }
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

                for (ObjFilter2 f : filters) {
                    Matrix.rotateM(f.getMatrix(), 0, 1f, 0, 1, 0);
                    //Matrix.rotateM(f.getMatrix(), 0, xAngle, -1, 0, 0);
                    //Matrix.rotateM(f.getMatrix(), 0, yAngle, 0, 1, 0);
                    //Matrix.rotateM(f.getMatrix(), 0, zAngle, 0, 0, 1);
                    f.draw();
                }

            }
        });
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        xAngle = sensorEvent.values[1];
        yAngle = sensorEvent.values[2];
        zAngle = sensorEvent.values[0];
        //Log.e("TAG", "x角度:" + xAngle + "\n" + "y角度:" + yAngle + "\n" + "z角度:" + zAngle);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
