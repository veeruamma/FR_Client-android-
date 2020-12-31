package veer.face.rec.frclient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RecogniseActivity extends AppCompatActivity {

    private static String TAG= "Recognise Activity";

    static {
        if(OpenCVLoader.initDebug()){
            Log.i(TAG, "OpenCV Loaded Successfully ");
        }else{
            Log.i(TAG, "OpenCV not loaded ");
        }
    }

    private Bitmap tempBmp;
    private Bitmap bitmap;
    private String encodedFaceMat;
    private Mat mRgb;

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
            switch (status){
                case BaseLoaderCallback.SUCCESS:
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognise);

        ImageView openCamera = (ImageView) findViewById(R.id.openCamera);
        Button processButton = (Button) findViewById(R.id.processButton);

        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(getApplicationContext())
                        .withPermission(Manifest.permission.CAMERA)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(intent, 111);

                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();

                            }
                        }).check();

            }
        });

        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(encodedFaceMat != null){
                    OkHttpClient okHttpClient = new OkHttpClient();

                    RequestBody requestBody = new FormBody.Builder()
                            .add("encodedFace", encodedFaceMat).build();

//                    Request request = new Request.Builder().url("http://192.168.56.241:5000/recognise")
//                            .post(requestBody).build();

                    Request request = new Request.Builder().url("https://veer-fr-webservice.herokuapp.com/recognise")
                            .post(requestBody).build();

                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            Toast.makeText(RecogniseActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            final TextView textView = findViewById(R.id.recView);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        textView.setText(response.body().string());
                                    }catch (IOException e){
                                        e.printStackTrace();
                                    }
                                }
                            });

                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==111 && resultCode==RESULT_OK){
            final TextView textView = findViewById(R.id.recView);
            textView.setText(" ");
            bitmap = (Bitmap) data.getExtras().get("data");
            mRgb = getCVMat(bitmap);
            encodedFaceMat = encodeFaceMatToJason(mRgb);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private Mat getCVMat(Bitmap bitmap) {
        Mat tempMat = new Mat();
        tempBmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(tempBmp, tempMat);
        Imgproc.cvtColor(tempMat, tempMat, Imgproc.COLOR_BGR2RGB);
        return tempMat;
    }

    private String encodeFaceMatToJason(Mat mat)
    {
        JSONObject jsonObject = new JSONObject();
        if(mat.isContinuous()){
            int cols = mat.cols();
            int rows = mat.rows();
            int elemSize = (int) mat.elemSize();

            byte[] data = new byte[cols * rows * elemSize];
            mat.get(0, 0, data);
            try {
                jsonObject.put("rows", mat.rows());
                jsonObject.put("cols", mat.cols());
                jsonObject.put("type", mat.type());
                String dataString = android.util.Base64.encodeToString(data, Base64.DEFAULT);
                jsonObject.put("data", dataString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Gson gson = new Gson();
            String json = gson.toJson(jsonObject);
            return json;
        }else{
            Log.e(TAG, "Mat not continuous.");
        }

        return "{ }";
    }


}