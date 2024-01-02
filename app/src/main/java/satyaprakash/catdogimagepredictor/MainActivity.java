package satyaprakash.catdogimagepredictor;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import satyaprakash.VolleyMultipartRequest;
public class MainActivity extends AppCompatActivity {
    Button buttonUpload,buttonCapture;
    private Bitmap bitmap;
    ProgressBar progressBar;
    ImageView imageView;
    String url ="http://192.168.8.132:5000//uploader";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=findViewById(R.id.imageView);
        progressBar=findViewById(R.id.progressBar);
        buttonUpload =findViewById(R.id.uploadImage);
        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                activityResultLauncherGallery.launch(photoPickerIntent);
            }
        });
        buttonCapture=findViewById(R.id.captureImage);
        buttonCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
                activityResultLauncherCamera.launch(i);
            }
        });
    }
    ActivityResultLauncher<Intent> activityResultLauncherCamera=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK && result.getData()!=null) {
                Bundle bundle = result.getData().getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");
                imageView.setImageBitmap(bitmap);
                uploadBitmap(bitmap);
                progressBar.setVisibility(View.VISIBLE);
            }
        }
    });
    ActivityResultLauncher<Intent> activityResultLauncherGallery=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK && result.getData()!=null) {
                Intent data = result.getData();
                Uri filePath = data.getData();
                try {
                    InputStream image_stream;
                    try {
                        image_stream = getContentResolver().openInputStream(filePath);
                        bitmap = BitmapFactory.decodeStream(image_stream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(bitmap);
                uploadBitmap(bitmap);
                progressBar.setVisibility(View.VISIBLE);
            }
        }
    });
    private void uploadBitmap(final Bitmap bitmap) {
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            String result = obj.getString("Response");
                           // String result1= obj.getString("Matched Features");
                            if(result.equals("I guess this must be a Dog!")){
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                progressBar.setVisibility(View.INVISIBLE);
                                // Set the message show for the Alert time
                                builder.setMessage("I guess this must be a Dog!");
                                // Set Alert Title
                                builder.setTitle("Response !");
                                // Set Cancelable false
                                // for when the user clicks on the outside
                                // the Dialog Box then it will remain show
                                builder.setCancelable(false);
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        // If user click no
                                        // then dialog box is canceled.
                                        dialog.cancel();
                                    }
                                });
                                // Create the Alert dialog
                                AlertDialog alertDialog = builder.create();
                                // Show the Alert Dialog box
                                alertDialog.show();
                            }
                            else if(result.equals("I guess this must be a Cat!")){
                                progressBar.setVisibility(View.INVISIBLE);
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                // Set the message show for the Alert time
                                builder.setMessage("I guess this must be a Cat!");
                                // Set Alert Title
                                builder.setTitle("Response !");
                                // Set Cancelable false
                                // for when the user clicks on the outside
                                // the Dialog Box then it will remain show
                                builder.setCancelable(false);
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        // If user click no
                                        // then dialog box is canceled.
                                        dialog.cancel();
                                    }
                                });

                                // Create the Alert dialog
                                AlertDialog alertDialog = builder.create();
                                // Show the Alert Dialog box
                                alertDialog.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                        Log.e("GotError",""+error.getMessage());
                    }
                })
        {


            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long ImageName = System.currentTimeMillis();
                params.put("file", new DataPart(ImageName + ".JPEG", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };
        //adding the request to volley
        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }
    private byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}