package com.example.fish_camera;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
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
import java.io.IOException;
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

    private String mPath = "";//設置高畫質的照片位址
    public static final String TAG = "test";
    public static final int CAMERA_PERMISSION = 100;//檢測相機權限用
    public static final int REQUEST_HIGH_IMAGE = 101;//檢測高畫質相機回傳
    public static final int REQUEST_LOW_IMAGE = 102;//檢測低畫質相機回傳
    public static final int READ_REQUEST_CODE = 42;//讀取相簿
    public static final String URL_FEED = "123";//URL
    ImageView fish_img,fish_img_h;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button takepic = findViewById(R.id.takepic);
        TextView fish_name = findViewById(R.id.fish_name);
        fish_img = findViewById(R.id.fish_img);

        Button takepic_h = findViewById(R.id.takepic_h);
        TextView fish_name_h = findViewById(R.id.fish_name_h);
        fish_img_h = findViewById(R.id.fish_img_h);

        ImageButton select_img = findViewById(R.id.select_img);

        /**取得相機權限*/
        if (checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION);
        }
        /**按下低畫質照相之拍攝按鈕*/
        takepic.setOnClickListener(v -> {
            Intent lowIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //檢查是否已取得權限
            if (lowIntent.resolveActivity(getPackageManager()) == null) return;
            startActivityForResult(lowIntent,REQUEST_LOW_IMAGE);
        });
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
                    "com.jetec.cameraexample.CameraEx",//記得要跟AndroidManifest.xml中的authorities 一致
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

    public void get_image()
    {
        // 建立 OkHttpClient
        OkHttpClient client = new OkHttpClient().newBuilder().build();

// 建立 Request，設定連線資訊
        Request request = new Request.Builder()
                .url("https://jsonplaceholder.typicode.com/posts")
                .build();

// 建立 Call
        Call call = client.newCall(request);

// 執行 Call 連線
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 連線成功
                String result = response.body().string();
                //使用 Gson 解析 Json 資料


            }

            @Override
            public void onFailure(Call call, IOException e) {
                // 連線失敗
                Log.d("HKT", e.toString());
            }
        });
    }

    /**
     * 上传图片
     * @param url
     * @param imagePath 图片路径
     * @return 新图片的路径
     * @throws IOException
     * @throws JSONException
     */

    public static String uploadImage(String url, String imagePath) throws IOException, JSONException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Log.d("imagePath", imagePath);
        File file = new File(imagePath);
        RequestBody image = RequestBody.create(MediaType.parse("image/png"), file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", imagePath, image)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        JSONObject jsonObject = new JSONObject(response.body().string());
        return jsonObject.optString("image");
    }

    //上面的getAbsolutePath方法根據選取的圖片的uri取得其絕對路徑,實現如下:
    private String getAbsolutePath(Context context, Uri uri) {
        ContentResolver localContentResolver = context.getContentResolver();
        Cursor localCursor = localContentResolver.query(uri, null, null, null, null);
        localCursor.moveToFirst();
        Log.v(TAG,localCursor.toString());
        return localCursor.getString(localCursor.getColumnIndex("_data"));

    }


    private String getRealPathFromURI(Uri contentUri) { //傳入圖片uri地址
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(MainActivity.this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /**可在此檢視回傳為哪個相片，requestCode為上述自定義，resultCode為-1就是有拍照，0則是使用者沒拍照**/
        Log.v(TAG, "onActivityResult: requestCode: "+requestCode+", resultCode "+resultCode);

        /**如果是低畫質的相片回傳**/
        if (requestCode == REQUEST_LOW_IMAGE && resultCode == -1) {
            ImageView imageLow = findViewById(R.id.fish_img);
            Bundle getImage = data.getExtras();
            Bitmap getLowImage = (Bitmap) getImage.get("data");
            Matrix matrix = new Matrix();
            matrix.setRotate(90f);//轉90度


            getLowImage = Bitmap.createBitmap
            (Bitmap.createBitmap(getLowImage
                    ,0,0
                    ,getLowImage.getWidth()
                    ,getLowImage.getHeight()
                    ,matrix,true));

            //以Glide設置圖片
            Glide.with(this)
                    .load(getLowImage)
                    //.centerCrop()
                    .into(imageLow);
        }
        /**如果是高畫質的相片回傳**/
        else if(requestCode == REQUEST_HIGH_IMAGE && resultCode == -1){
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
        }
        /**讀取相簿**/
        else if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {

            if (data.getData()!=null) {      // select one image
                Uri selectedImage = data.getData();
                fish_img_h.setImageURI(selectedImage);  //showImage
                Log.i(TAG, "Uri: " + getAbsolutePath(MainActivity.this,selectedImage));
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
}

