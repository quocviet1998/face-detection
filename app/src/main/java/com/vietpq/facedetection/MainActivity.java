package com.vietpq.facedetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ImageView img;
    Button selectImageButton;
    Bitmap finalBitmap; //

//    private static final int PICK_FROM_CAMERA = 1;
//    private static final int PICK_FROM_GALLARY = 2;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    int PICK_IMAGE_REQUEST = 111;
//    Uri outPutfileUri;
    Uri filePath;
    ProgressDialog progress ;

    private static final String TAG = "MyActivity";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    List<FaceAnnotation> faces = null; // list of faces

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectImageButton = (Button) findViewById(R.id.btnSelectImage);
        img = (ImageView) findViewById(R.id.imgView);
        progress = new ProgressDialog(this);


        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            try {
                //getting image from gallery

//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
finalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

//                //Setting image to ImageView
                img.setImageBitmap(finalBitmap);

                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    upload();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showProgress(){
        progress.setMessage("Sending image");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(true);
        progress.setProgress(0);
        progress.show();
    }

    public void hideProgress(){
        progress.hide();
    }


    public void upload() {
        //create visionBuilder
        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null);

        //init visionBuilder with API key
        visionBuilder.setVisionRequestInitializer(
                new VisionRequestInitializer("AIzaSyA4_1RyQZafCYS9ynE4tuRNPVPVE9a5VC8"));

        final Vision vision = visionBuilder.build();
        final Image inputImage = new Image();
        Bitmap bitmap = getBitmapFromImage(img);
        if (bitmap == null) return;

        // convert bitmap into bytes
        byte[] photoData = getBytesFromBitmap(bitmap);

        if (photoData == null) {
            Log.d("Error", "photodata is null");
            return;
        }

        inputImage.encodeContent(photoData);

        // Create new thread
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Feature desiredFeature = new Feature();
                desiredFeature.setType("FACE_DETECTION");
                AnnotateImageRequest request = new AnnotateImageRequest();
                request.setImage(inputImage);
                request.setFeatures(Arrays.asList(desiredFeature));
                BatchAnnotateImagesRequest batchRequest =
                        new BatchAnnotateImagesRequest();
                batchRequest.setRequests(Arrays.asList(request));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgress();
                    }
                });

//                try {
//
//                    Toast.makeText(getApplicationContext(), "Sending request...", Toast.LENGTH_SHORT).show();
//                } catch (Exception e) {
//                    Toast.makeText(getApplicationContext(), "Error when sending request", Toast.LENGTH_SHORT).show();
//                }

                BatchAnnotateImagesResponse batchResponse = new BatchAnnotateImagesResponse();
                try {
                    batchResponse = vision.images().annotate(batchRequest).execute();

//                    Toast.makeText(getApplicationContext(), "Response received!", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                    }
                });


                faces = batchResponse.getResponses().get(0).getFaceAnnotations();

                if (faces == null) {
                    Toast.makeText(getApplicationContext(), "There is no face in this picture!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Count faces
                int numberOfFaces = faces.size();

// Get joy likelihood for each face
                String likelihoods = "";
                for (int i = 0; i < numberOfFaces; i++) {
                    likelihoods += "\n It is " + faces.get(i).getJoyLikelihood() + " that face " + i + " is happy";
                }

// Concatenate everything
                final String message = "This photo has " + numberOfFaces + " faces" + likelihoods;

// Display toast on UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        drawOnImageView(finalBitmap, faces);
                    }
                });
            }
        });

    }


    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * this method request to permission asked.
     */

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE);

        if (shouldProvideRationale) {
        } else {
            Log.i("Error", "Requesting permission");
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i("Error", "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
            }
        }

    }


    public Bitmap getBitmapFromImage(ImageView view) {

        Bitmap bitmap = ((BitmapDrawable) view.getDrawable()).getBitmap(); //BitmapFactory.decodeResource(getResources(),R.id.imgView);

        if (bitmap == null) {
            Log.d("tag", "bitmap is null");
            return null;
        }
        return bitmap;
    }

    //Gallery storage permission required for Marshmallow version
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    // convert from bitmap to byte array
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        if (bitmap == null)
            return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

    public void drawOnImageView(Bitmap bitmap, List<FaceAnnotation> faces) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inMutable = true; // set image inmutable to draw in

        Paint paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);

        Bitmap tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(tempBitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);
        img.setImageBitmap(tempBitmap);
        draw(canvas, tempBitmap, paint, faces);
    }

    public void draw(Canvas c, Bitmap b, Paint p, List<FaceAnnotation> faces){
        if(faces == null)
            return;
        for(int i = 0; i < faces.size(); i++){
            c.drawRoundRect(
                    new RectF(
                            faces.get(i).getBoundingPoly().getVertices().get(0).getX() == null ? 0 : faces.get(i).getBoundingPoly().getVertices().get(0).getX(), // x of the left
                            faces.get(i).getBoundingPoly().getVertices().get(1).getY() == null ? 0 : faces.get(i).getBoundingPoly().getVertices().get(1).getY(), // y of the top
                            faces.get(i).getBoundingPoly().getVertices().get(2).getX() == null ? 0 : faces.get(i).getBoundingPoly().getVertices().get(2).getX(), // x of the right
                            faces.get(i).getBoundingPoly().getVertices().get(3).getY() == null ? 0 : faces.get(i).getBoundingPoly().getVertices().get(3).getY() // y of the bottom
                    ), 2, 2, p
            );

            img.setImageDrawable(new BitmapDrawable(getResources(), b));
        }
    }

    public void log(String s){
        int chunkSize = 2048;
        for (int i = 0; i < s.length(); i += chunkSize) {
            Log.d(TAG, s.substring(i, Math.min(s.length(), i + chunkSize)));
        }
    }

}
