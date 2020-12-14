package com.example.fish_camera;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import cz.msebera.android.httpclient.entity.mime.Header;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    String mPath = "";//設置高畫質的照片位址
    public static final String TAG = "test";
    public static final int CAMERA_PERMISSION = 100;//檢測相機權限用
    public static final int REQUEST_HIGH_IMAGE = 101;//檢測高畫質相機回傳
    public static final int REQUEST_LOW_IMAGE = 102;//檢測低畫質相機回傳
    public static final int READ_REQUEST_CODE = 42;//讀取相簿
    public static final String URL_FEED = "123";//URL
    ImageView fish_img_h;
    TextView fish_name_h;
    String Identify_result="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button takepic_h = findViewById(R.id.takepic_h);
        fish_name_h = findViewById(R.id.fish_name_h);
        fish_img_h = findViewById(R.id.fish_img_h);

        ImageButton select_img = findViewById(R.id.select_img);

        /**取得相機權限*/
        if (checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION);
        }



        /**按下高畫質照相之拍攝按鈕*/
        takepic_h.setOnClickListener(v -> {
            Intent highIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //檢查是否已取得權限
            if (highIntent.resolveActivity(getPackageManager()) == null) return;
            //取得相片檔案的URI位址及設定檔案名稱
            File imageFile = getImageFile();
            if (imageFile == null) return;
            //取得相片檔案的URI位址
            Uri imageUri = FileProvider.getUriForFile(
                    getApplicationContext(),
                    "com.example.fish_camera",//記得要跟AndroidManifest.xml中的authorities 一致
                    imageFile
            );
            highIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
            startActivityForResult(highIntent,REQUEST_HIGH_IMAGE);//開啟相機
        });

        /**按下相簿讀取按鈕*/
        select_img.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(intent, READ_REQUEST_CODE);
        });

        /**搜尋食譜*/
        Button recipe_search = findViewById(R.id.recipe_search);
        recipe_search.setOnClickListener(v -> {
            if(!Identify_result.equals("")&&!Identify_result.equals("failed"))
            {
                Intent to_webview = new Intent(MainActivity.this,recipe.class);
                to_webview.putExtra("Identify_result",Identify_result);
                startActivity(to_webview);
            }
            else
            {
                Toast.makeText(MainActivity.this,"請重新辨識魚類",Toast.LENGTH_LONG).show();
            }


        });

    }

    /**取得相片檔案的URI位址及設定檔案名稱*/
    private File getImageFile()  {
        String time = new SimpleDateFormat("yyMMdd").format(new Date());
        String fileName = time+"_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            //給予檔案命名及檔案格式
            File imageFile = File.createTempFile(fileName,".jpg",dir);
            //給予全域變數中的照片檔案位置，方便後面取得
            mPath = imageFile.getAbsolutePath();
            return imageFile;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 讀取圖片屬性:旋轉的角度
     * @param path 圖片絕對路徑
     * @return degree旋轉的角度
     */
    public static int readPictureDegree(String path) {
        int degree  = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 上传图片
     * @param url
     * @return 新图片的路径
     * @throws IOException
     * @throws JSONException
     */

    public  String uploadImage(String url) throws IOException, JSONException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Log.d("imagePath", mPath);
        File file = new File( mPath);
        RequestBody image = RequestBody.create(MediaType.parse("image/jpg"), file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image",  file.getName(), image)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
//        try (Response response = okHttpClient.newCall(request).execute()) {
//            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//
//
//            System.out.println(response.body().string());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        Response response = okHttpClient.newCall(request).execute();
        String response_str = response.body().string();
        //JSONObject jsonObject = new JSONObject(response.body().string());
        Log.v(TAG,"result string:"+response_str);
        //return jsonObject.optString("image");
        return response_str;
    }

    //讀取相簿用，上面的getAbsolutePath方法根據選取的圖片的uri取得其絕對路徑,實現如下:
    private String getAbsolutePath(Context context, Uri uri) {
        ContentResolver localContentResolver = context.getContentResolver();
        Cursor localCursor = localContentResolver.query(uri, null, null, null, null);
        localCursor.moveToFirst();
        Log.v(TAG,localCursor.toString());
        return localCursor.getString(localCursor.getColumnIndex("_data"));

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /**可在此檢視回傳為哪個相片，requestCode為上述自定義，resultCode為-1就是有拍照，0則是使用者沒拍照**/
        Log.v(TAG, "onActivityResult: requestCode: "+requestCode+", resultCode "+resultCode);

        /**如果是高畫質的相片回傳**/
        if(requestCode == REQUEST_HIGH_IMAGE && resultCode == -1){
            ImageView imageHigh = findViewById(R.id.fish_img_h);
            new Thread(()->{
                int degree = readPictureDegree(mPath);
                //在BitmapFactory中以檔案URI路徑取得相片檔案，並處理為AtomicReference<Bitmap>，方便後續旋轉圖片
                AtomicReference<Bitmap> getHighImage = new AtomicReference<>(BitmapFactory.decodeFile(mPath));
                Matrix matrix = new Matrix();
                matrix.postRotate(degree);
                //matrix.setRotate(90f);//轉90度
                getHighImage.set(Bitmap.createBitmap(getHighImage.get()
                        ,0,0
                        ,getHighImage.get().getWidth()
                        ,getHighImage.get().getHeight()
                        ,matrix,true));
                runOnUiThread(()->{
                    //以Glide設置圖片(因為旋轉圖片屬於耗時處理，故會LAG一下，且必須使用Thread執行緒)
                    Glide.with(this)
                            .load(getHighImage.get())
                            //.centerCrop()
                            .into(imageHigh);
                });
            }).start();


            File file = new File( mPath);
            urlConnect uc = new urlConnect("http://192.168.43.99:8000/predict2",file);
            uc.start();

            showWaitingDialog dialog=new showWaitingDialog(MainActivity.this);
            if(uc.getLockStatus())
            {
                Log.v(TAG,"uc.getLockStatus: "+uc.getLockStatus());
                dialog.show();
                while (uc.getLockStatus()) { }

            }
            Log.v(TAG,"uc.getLockStatus2: "+uc.getLockStatus());
            dialog.cancel();
            Identify_result = uc.getResult();
            fish_name_h.setText(Identify_result);
        }

        /**讀取相簿**/
        else if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {

            if (data.getData()!=null) {      // select one image
                Uri selectedImage = data.getData();
                fish_img_h.setImageURI(selectedImage);  //showImage

                Log.v(TAG,"selected Img: "+selectedImage);

                FileUtils fileUtils = new FileUtils(getApplicationContext());
                String path = fileUtils.getPath(selectedImage);
                Log.v(TAG,"Path: "+path);
                //呼叫server
                File file = new File(path);
                urlConnect uc2 = new urlConnect("http://192.168.43.99:8000/predict2",file);
                uc2.start();

                showWaitingDialog dialog=new showWaitingDialog(MainActivity.this);
                if(uc2.getLockStatus())
                {
                    Log.v(TAG,"uc.getLockStatus: "+uc2.getLockStatus());
                    dialog.show();
                    while (uc2.getLockStatus()) { }

                }
                Log.v(TAG,"uc.getLockStatus2: "+uc2.getLockStatus());
                dialog.cancel();
                Log.v(TAG,"uc.getResult(): "+uc2.getResult());
                Identify_result = uc2.getResult();
                fish_name_h.setText(Identify_result);


            }
            else if (data.getClipData() != null) { // select multiple images
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri selectedImages = data.getClipData().getItemAt(i).getUri();
                    fish_img_h.setImageURI(selectedImages);  //showImage
                    // Log.i(TAG, "Uri: " + selectedImages.toString());
                }
            }
        }
        else {
            Toast.makeText(this, "未作任何拍攝", Toast.LENGTH_SHORT).show();
        }

    }

    private Dialog showWaitingDialog(Context context) {
        /* 等待Dialog具有屏蔽其他控件的交互能力
         * @setCancelable 为使屏幕不可点击，设置为不可取消(false)
         * 下载等事件完成后，主动调用函数关闭该Dialog
         */
        ProgressDialog waitingDialog=
                new ProgressDialog(context);
        waitingDialog.setTitle("");
        waitingDialog.setMessage("等待中...");
        waitingDialog.setIndeterminate(true);
        waitingDialog.setCancelable(false);
        return  waitingDialog;
    }


}

