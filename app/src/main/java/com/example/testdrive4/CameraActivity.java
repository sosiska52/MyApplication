package com.example.testdrive4;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CameraActivity extends AppCompatActivity {

    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private boolean isCameraReady = false;
    private final int REQUEST_CAMERA_PERMISSION = 100;
    private Size previewSize;
    private SurfaceView surfaceView;
    private long timeStamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        timeStamp = System.currentTimeMillis();
        surfaceView = findViewById(R.id.surfaceView);
        ImageButton buttonCapture = findViewById(R.id.imageButtonCapture);
        surfaceView.getHolder().addCallback(surfaceCallback);

        buttonCapture.setOnClickListener(view -> capturePhoto());

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    }
    public void startDataActivity(){
        Intent intent = new Intent(this, DataActivity.class);
        intent.putExtra("time", timeStamp);
        startActivity(intent);
    }

    private final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                requestCameraPermission();
            }
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {}

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {}
    };

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(CameraActivity.this,
                new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

private void openCamera() {
    try {
        String cameraId = cameraManager.getCameraIdList()[0]; // Присвойте значение переменной cameraId в методе openCamera()
        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
        previewSize = Objects.requireNonNull(characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP))
                .getOutputSizes(SurfaceHolder.class)[0];

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }
        cameraManager.openCamera(cameraId, stateCallback, null);
    } catch (CameraAccessException e) {
        e.printStackTrace();
    }
}

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
            isCameraReady = true;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

private void createCameraPreview() {
    try {
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFixedSize(previewSize.getWidth(), previewSize.getHeight());

        Surface surface = surfaceHolder.getSurface();
        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequestBuilder.addTarget(surface);
        cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                if (cameraDevice == null) {
                    return;
                }
                cameraCaptureSession = session;
                updatePreview();
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                Toast.makeText(CameraActivity.this, "Configuration change failed", Toast.LENGTH_SHORT).show();
            }
        }, null);
    } catch (CameraAccessException e) {
        e.printStackTrace();
    }
}
    private void updatePreview() {
        if (cameraDevice == null) {
            return;
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(CameraActivity.this, "Camera access denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void capturePhoto() {
        if (isCameraReady) {
            try {
                ImageReader reader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.JPEG, 1);
                CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureBuilder.addTarget(reader.getSurface());
                reader.setOnImageAvailableListener(reader1 -> {
                    try (Image image = reader1.acquireLatestImage()) {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        saveImage(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, null);

                // Create a list of Surfaces for the output targets
                List<Surface> outputSurfaces = new ArrayList<>(1);
                outputSurfaces.add(reader.getSurface());

                // Create a new capture session with the output surfaces
                cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        try {
                            // Capture the image
                            session.capture(captureBuilder.build(), null, null);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        // Handle configuration failure
                    }
                }, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(CameraActivity.this, "Camera not ready", Toast.LENGTH_SHORT).show();
        }
    }

private void saveImage(byte[] bytes) throws IOException {
    // Поворачиваем изображение в нужную ориентацию
    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    Matrix matrix = new Matrix();
    matrix.postRotate(90); // Указываем нужный угол поворота

    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

    File pictureFile = getOutputMediaFile();
    if (pictureFile != null) {
        try (FileOutputStream fos = new FileOutputStream(pictureFile)) {
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos); // Сохраняем повернутое изображение
            //Toast.makeText(CameraActivity.this, "Image saved: " + pictureFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        }
    }
    startDataActivity();
}

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        SharedPreferences sharedPreferencesID = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        //long timeStamp = System.currentTimeMillis();
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                sharedPreferencesID.getString("surname","")+"_"+timeStamp + ".jpg");

        SharedPreferences sharedPhoto = getSharedPreferences("Photo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPhoto.edit();
        editor.putString("Photo", mediaFile.getAbsolutePath());
        editor.apply();
        return mediaFile;
    }
}