//================================================================================================================================
//
//  Copyright (c) 2015-2017 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloar;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import cn.easyar.CameraCalibration;
import cn.easyar.CameraDevice;
import cn.easyar.CameraDeviceFocusMode;
import cn.easyar.CameraDeviceType;
import cn.easyar.CameraFrameStreamer;
import cn.easyar.Frame;
import cn.easyar.FunctorOfVoidFromPointerOfTargetAndBool;
import cn.easyar.ImageTarget;
import cn.easyar.ImageTracker;
import cn.easyar.Renderer;
import cn.easyar.StorageType;
import cn.easyar.Target;
import cn.easyar.TargetInstance;
import cn.easyar.TargetStatus;
import cn.easyar.Vec2I;
import cn.easyar.Vec4I;
import money.MoneyActivity;
import obj.Gl2Utils;
import obj.Obj3D;
import obj.ObjFilter2;
import obj.ObjReader;

public class HelloAR {
    String image;
    /**
     * 退出红包界面后 3s 后可以再次抢红包
     */
    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Constants.flag = 0;
                removeCallbacksAndMessages(0);
            }
        }
    };
    /**
     * 数据操作的对象
     */
    private DataStore dataStorage;
    private Context mContext;
    /**
     * EasyAR相关参数
     */
    private List<ObjFilter2> filters;
    private CameraDevice camera;
    private CameraFrameStreamer streamer;
    private ArrayList<ImageTracker> trackers;
    private Renderer videobg_renderer;
    private BoxRenderer box_renderer;
    private boolean viewport_changed = false;
    private Vec2I view_size = new Vec2I(0, 0);//2x1的int向量。
    private int rotation = 0;
    private Vec4I viewport = new Vec4I(0, 0, 1280, 720);//4x1的int向量

    public HelloAR(Context context) {
        trackers = new ArrayList<ImageTracker>();
        mContext = context;
        dataStorage = new DataStore(mContext);
        List<Obj3D> model = ObjReader.readMultiObj(context, "assets/3dres/pikachu.obj");
        filters = new ArrayList<>();
        for (int i = 0; i < model.size(); i++) {
            ObjFilter2 f = new ObjFilter2(context.getResources());
            f.setObj3D(model.get(i));
            filters.add(f);
        }
    }

    private void loadFromImage(ImageTracker tracker, String path) {
        ImageTarget target = new ImageTarget();
        String jstr = "{\n"
                + "  \"images\" :\n"
                + "  [\n"
                + "    {\n"
                + "      \"image\" : \"" + path + "\",\n"
                + "      \"name\" : \"" + path.substring(0, path.indexOf(".")) + "\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        target.setup(jstr, StorageType.Assets | StorageType.Json, "");
        tracker.loadTarget(target, new FunctorOfVoidFromPointerOfTargetAndBool() {
            @Override
            public void invoke(Target target, boolean status) {
                Log.i("HelloAR", String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
            }
        });
    }

    private void loadFromJsonFile(ImageTracker tracker, String path, String targetname) {
        ImageTarget target = new ImageTarget();
        target.setup(path, StorageType.Assets, targetname);
        tracker.loadTarget(target, new FunctorOfVoidFromPointerOfTargetAndBool() {
            @Override
            public void invoke(Target target, boolean status) {
                Log.i("HelloAR", String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
            }
        });
    }

    /**
     * 加载 asserts 中 json 文件中的所有图片
     *
     * @param tracker
     * @param path
     */
    private void loadAllFromJsonFile(ImageTracker tracker, String path) {
        //ImageTarget.setupAll()方法直接加载并返回了所有图片资源代表的 ImageTarget 对象
        for (ImageTarget target : ImageTarget.setupAll(path, StorageType.Assets)) {
            tracker.loadTarget(target, new FunctorOfVoidFromPointerOfTargetAndBool() {
                @Override
                public void invoke(Target target, boolean status) {
                    Log.i("HelloAR", String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
                }
            });
        }
    }

    /**
     * 加载外部文件路径的图片
     */
    private void loadExternalFromJsonFile(ImageTracker tracker, String path) {
        for (ImageTarget target : ImageTarget.setupAll(path, StorageType.Absolute)) {
            tracker.loadTarget(target, new FunctorOfVoidFromPointerOfTargetAndBool() {
                @Override
                public void invoke(Target target, boolean status) {
                    Log.e("HelloAR", "loadAllFromJsonFile: " +
                            String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
                }
            });
        }
    }

    public boolean initialize() {
        camera = new CameraDevice();
        streamer = new CameraFrameStreamer();
        streamer.attachCamera(camera);

        boolean status = true;
        status &= camera.open(CameraDeviceType.Default);
        camera.setSize(new Vec2I(1280, 720));

        if (!status) {
            return status;
        }
        ImageTracker tracker = new ImageTracker();
        tracker.attachStreamer(streamer);
        loadFromJsonFile(tracker, "targets.json", "argame");
        loadFromJsonFile(tracker, "targets.json", "idback");
        loadAllFromJsonFile(tracker, "targets2.json");
        loadFromImage(tracker, "namecard.jpg");//设置图片判断--一个为模型显示一个为红包
        loadFromImage(tracker, "xiongmao.jpg");
        trackers.add(tracker);

        return status;
    }

    public void dispose() {
        for (ImageTracker tracker : trackers) {
            tracker.dispose();
        }
        trackers.clear();
        box_renderer = null;
        if (videobg_renderer != null) {
            videobg_renderer.dispose();
            videobg_renderer = null;
        }
        if (streamer != null) {
            streamer.dispose();
            streamer = null;
        }
        if (camera != null) {
            camera.dispose();
            camera = null;
        }
    }

    public boolean start() {
        boolean status = true;
        status &= (camera != null) && camera.start();
        status &= (streamer != null) && streamer.start();
        camera.setFocusMode(CameraDeviceFocusMode.Continousauto);
        for (ImageTracker tracker : trackers) {
            status &= tracker.start();
        }
        return status;
    }

    public boolean stop() {
        boolean status = true;
        for (ImageTracker tracker : trackers) {
            status &= tracker.stop();
        }
        status &= (streamer != null) && streamer.stop();
        status &= (camera != null) && camera.stop();
        return status;
    }

    public void initGL() {
        Log.e("TAG", "initGL()");
        if (videobg_renderer != null) {
            videobg_renderer.dispose();
        }
        videobg_renderer = new Renderer();
        box_renderer = new BoxRenderer();
        box_renderer.init(filters);
    }

    /**
     * 重新绘制区域
     *
     * @param width
     * @param height
     */
    public void resizeGL(int width, int height) {
        Log.e("TAG", "resizeGL()");
        view_size = new Vec2I(width, height);
        viewport_changed = true;
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

    private void updateViewport() {
        CameraCalibration calib = camera != null ? camera.cameraCalibration() : null;
        int rotation = calib != null ? calib.rotation() : 0;
        if (rotation != this.rotation) {
            this.rotation = rotation;
            viewport_changed = true;
        }
        if (viewport_changed) {
            Vec2I size = new Vec2I(1, 1);
            if ((camera != null) && camera.isOpened()) {
                size = camera.size();
            }
            if (rotation == 90 || rotation == 270) {
                size = new Vec2I(size.data[1], size.data[0]);
            }
            float scaleRatio = Math.max((float) view_size.data[0] / (float) size.data[0], (float) view_size.data[1] / (float) size.data[1]);
            Vec2I viewport_size = new Vec2I(Math.round(size.data[0] * scaleRatio), Math.round(size.data[1] * scaleRatio));
            viewport = new Vec4I((view_size.data[0] - viewport_size.data[0]) / 2, (view_size.data[1] - viewport_size.data[1]) / 2, viewport_size.data[0], viewport_size.data[1]);

            if ((camera != null) && camera.isOpened())
                viewport_changed = false;
        }
    }

    /**
     * 绘制区域
     */
    public void render() {
        GLES20.glClearColor(1.f, 1.f, 1.f, 1.f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (videobg_renderer != null) {
            Vec4I default_viewport = new Vec4I(0, 0, view_size.data[0], view_size.data[1]);
            GLES20.glViewport(default_viewport.data[0], default_viewport.data[1], default_viewport.data[2], default_viewport.data[3]);
            if (videobg_renderer.renderErrorMessage(default_viewport)) {
                return;
            }
        }

        if (streamer == null) {
            return;
        }
        //Frame 用来存储追踪到的数：包含当前的 Camera 图像，跟踪到的 Target
        Frame frame = streamer.peek();
        try {
            updateViewport();
            GLES20.glViewport(viewport.data[0], viewport.data[1], viewport.data[2], viewport.data[3]);

            if (videobg_renderer != null) {
                videobg_renderer.render(frame, viewport);
            }

            for (TargetInstance targetInstance : frame.targetInstances()) {
                int status = targetInstance.status();
                // 判断是否有 ImageTarget 被追踪到----检测到图像后的响应
                if (status == TargetStatus.Tracked) {
                    //每次追踪到目标，标志量都 +1   && Constants.flag < 1
                    //Constants.flag++;
                    //获取目标的信息
                    Target target = targetInstance.target();
                    //// TODO: 2017/11/9 设置判断不同图片显示不同模型 
                    ImageTarget imagetarget = target instanceof ImageTarget ? (ImageTarget) (target) : null;
                    Log.e("TAG", "识别图片:" + imagetarget.name());
                    /**
                     * 判断标志量，如果是 1 就跳转
                     *
                     * if (Constants.flag == 1) {
                     String fileName = target.name();
                     Intent intent = new Intent(mContext, MoneyActivity.class);
                     intent.putExtra("fileName", fileName);
                     mContext.startActivity(intent);
                     }
                     */
                    if (imagetarget == null) {
                        continue;
                    }
                    if (box_renderer != null) {
                        // TODO: 2017/11/2  这里就是说明，已经识别到了，开始绘制方块的地方---绘制模型
                        //box_renderer.render(camera.projectionGL(0.2f, 500.f), targetInstance.poseGL(), imagetarget.size());绘制长方体
                        box_renderer.render(camera.projectionGL(0.2f, 500.f), targetInstance.poseGL(), imagetarget.size(), filters);//绘制皮卡丘

                        //// TODO: 2017/11/25 绘制md2模型

                    }
                }
            }
        } finally {
            frame.dispose();
        }
    }
}
