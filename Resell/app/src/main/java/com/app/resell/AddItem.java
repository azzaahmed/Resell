package com.app.resell;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.resell.Data.FireBaseCalls;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
public class AddItem extends AppCompatActivity  implements View.OnClickListener {


    EditText description;
    EditText price;
    EditText size;
    Button post_button;
    TextInputLayout descriptionLayout;
    TextInputLayout priceLayout;
    TextInputLayout sizeLayout;

    // *********************** upload image ***********************************//
    private String profile_pic_path;

    private Uri imageUri = null;

    private StorageReference mStorage;

    private static final int GALLERY_REQUEST = 1;
    private ImageView imageView_circle;
    boolean profilePic_attached = false;
    Bitmap bitmap;

    private ProgressDialog progressDialog;
    private  FireBaseCalls FireBaseCalls;
    Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        activity=this;
        description=(EditText)findViewById(R.id.description);
        price=(EditText)findViewById(R.id.price);
        size=(EditText)findViewById(R.id.size);
        post_button = (Button) findViewById(R.id.post_button);
        priceLayout=(TextInputLayout) findViewById(R.id.priceLayout);
        sizeLayout=(TextInputLayout)findViewById(R.id.sizeLayout);

        progressDialog = new ProgressDialog(this);
        FireBaseCalls= new FireBaseCalls();

        post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Utility.isOnline(activity)) {
                    if(validForm()) {
                        UploadImage();
                    }
                }
                else{
                    Toast toast = Toast.makeText(AddItem.this, "Please check your Internet connection !", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        imageView_circle  = (ImageView) findViewById(R.id.buttonChoose);
        imageView_circle.setOnClickListener(this);
        mStorage = FirebaseStorage.getInstance().getReference();

    }




    // image upload
    private void showFileChooser() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){

            imageUri = data.getData();
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8; // shrink it down otherwise we will use stupid amounts of memory

                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        //    imageView_circle.setImageURI(imageUri);
            Picasso.with(AddItem.this)
                    .load(imageUri).fit().centerCrop()
                    .into(imageView_circle);
            profilePic_attached = true;

        }

    }

    public void UploadImage(){

        progressDialog.setMessage(" Please Wait...");
        progressDialog.show();

        if(imageUri != null){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,20, baos);
            byte[] bytes = baos.toByteArray();
            String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);// no need

            StorageReference filepath = mStorage.child("UsersImages").child(imageUri.getLastPathSegment());
            UploadTask uploadTask = filepath.putBytes(bytes);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    progressDialog.dismiss();
                    Toast.makeText(AddItem.this, "Can't upload your image ", Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    profile_pic_path = taskSnapshot.getDownloadUrl()+"";
                    progressDialog.dismiss();
                    FireBaseCalls.AddItem(description.getText().toString().trim(), price.getText().toString().trim(), size.getText().toString().trim(), profile_pic_path, getApplicationContext(), AddItem.this);

                }
            });

        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // upload image
            case R.id.buttonChoose:
                showFileChooser();
                break;


        }
    }



    private boolean validForm(){

        int check=0;

        if (price.getText().toString().trim().isEmpty()) {
            priceLayout.setError("please enter price");
            requestFocus(price);
            check++;
        } else {
            priceLayout.setErrorEnabled(false);

        }
        if (size.getText().toString().trim().isEmpty()) {
            sizeLayout.setError("please enter size");
            requestFocus(size);
            check++;
        } else {
            sizeLayout.setErrorEnabled(false);

        }
        if(imageUri==null){
            Toast.makeText(getApplicationContext(),"Upload item image",Toast.LENGTH_SHORT).show();
            check++;
        }
        return check == 0;
    }
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
}
