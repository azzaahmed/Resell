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
import android.text.TextUtils;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.app.resell.Data.FireBaseCalls;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mukesh.countrypicker.fragments.CountryPicker;
import com.mukesh.countrypicker.interfaces.CountryPickerListener;
import com.mukesh.countrypicker.models.Country;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SignUp extends AppCompatActivity implements View.OnClickListener {

    // *********************** upload image ***********************************//
    private String profile_pic_path;

    private Uri imageUri = null;

    private StorageReference mStorage;

    private static final int GALLERY_REQUEST = 1;

    private ImageView imageView_circle;

    boolean profilePic_attached = false;

    Bitmap bitmap;

    private static final String TAG = "SignInActivity";

    private TextInputLayout inputLayoutName;
    private TextInputLayout inputLayoutAge;
    private TextInputLayout inputLayoutMobile;
    private TextInputLayout inputLayoutEmail;
    private TextInputLayout inputLayoutPass;
    private TextInputLayout inputLayoutGender;
    private TextInputLayout input_layout_country;

    //defining view objects
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonSignUp;
    private ProgressDialog progressDialog;
    private EditText Name;
    private EditText age;
    private EditText mobile;
    private Spinner gender;


    private FireBaseCalls FireBaseCalls;
    private Activity Activity;

    //select country
    private EditText country_EditText_from;
    private CountryPicker mCountryPicker;
    private ImageView mCountryFlagImageView_from;
    private String country;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // *********************** upload image ***********************************//
        imageView_circle  = (ImageView) findViewById(R.id.buttonChoose);
        imageView_circle.setOnClickListener(this);

        mStorage = FirebaseStorage.getInstance().getReference();

        // ************************************************************************//

        // *********************** validation   ***********************************//
        inputLayoutName = (TextInputLayout) findViewById(R.id.input_layout_name);
        inputLayoutAge = (TextInputLayout) findViewById(R.id.input_layout_age);
        inputLayoutMobile = (TextInputLayout) findViewById(R.id.input_layout_mobile);
        inputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_email);
        inputLayoutPass = (TextInputLayout) findViewById(R.id.input_layout_pass);
        inputLayoutGender = (TextInputLayout) findViewById(R.id.input_layout_Gender);
        input_layout_country=(TextInputLayout) findViewById(R.id.input_layout_country);
        // ************************************************************************//


        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        buttonSignUp = (Button) findViewById(R.id.buttonSignup);

        progressDialog = new ProgressDialog(this);

        age = (EditText) findViewById(R.id.age);
        gender = (Spinner) findViewById(R.id.gender);
        Name = (EditText) findViewById(R.id.name);
        mobile = (EditText) findViewById(R.id.mobile);

        buttonSignUp.setOnClickListener(this);


        FireBaseCalls= new FireBaseCalls();
        Activity = this;


        //select country
        country_EditText_from = (EditText) findViewById(R.id.pick_country_from);
        mCountryFlagImageView_from = (ImageView) findViewById(R.id.row_icon_from);
        mCountryPicker = CountryPicker.newInstance("Select Country");
        setListener();
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSignup:
                if(validForm()) {
                    registerUser();
                }
                break;
            // upload image
            case R.id.buttonChoose:
                showFileChooser();
                break;


        }
    }

    private void registerUser() {


        if (Utility.isOnline(this)) {
            if (profilePic_attached) {
              UploadImage();
            } else {
                FireBaseCalls.fireBaseRegistration(editTextEmail, editTextPassword, age, Name, mobile, gender, country, " ", getApplicationContext(), true, Activity);

            }
        }
        else Toast.makeText(this,"no internet connection",Toast.LENGTH_LONG).show();
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


            Picasso.with(SignUp.this)
                    .load(imageUri).fit().centerCrop()
                    .into(imageView_circle);
            profilePic_attached = true;

        }

    }

    public void UploadImage(){

        progressDialog.setMessage("Registering Please Wait...");
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
                    Toast.makeText(SignUp.this, "Can't upload your image ", Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    profile_pic_path = taskSnapshot.getDownloadUrl()+"";
                    FireBaseCalls. fireBaseRegistration( editTextEmail,editTextPassword,age, Name,mobile, gender,country,profile_pic_path,getApplicationContext(),false,Activity);
                }
            });

        }


    }

    private boolean validForm(){

        int counter = 0;
        if (Name.getText().toString().trim().isEmpty()) {
            inputLayoutName.setError("please enter your name");
            requestFocus(Name);
            counter ++;
        } else {
            inputLayoutName.setErrorEnabled(false);
        }
        if (age.getText().toString().trim().isEmpty()) {
            inputLayoutAge.setError("please enter your age");
            requestFocus(age);
            counter ++;
        } else {
            if(Integer.parseInt(age.getText().toString().trim())<12 || Integer.parseInt(age.getText().toString().trim())> 90) {
                inputLayoutAge.setError("please enter reasonable age");
                requestFocus(age);
                counter++;
            }
            else
                inputLayoutAge.setErrorEnabled(false);
        }
        if (mobile.getText().toString().trim().isEmpty()) {
            inputLayoutMobile.setError("Invalid mobile number");
            requestFocus(mobile);
            counter ++;
        } else {
            inputLayoutMobile.setErrorEnabled(false);
        }
        if (editTextEmail.getText().toString().trim().isEmpty()) {
            inputLayoutEmail.setError("Invalid Email");
            requestFocus(editTextEmail);
            counter ++;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }
        if (editTextPassword.getText().toString().trim().isEmpty()|| editTextPassword.getText().toString().trim().length()<6) {
            inputLayoutPass.setError("password at least 6 characters");
            requestFocus(editTextPassword);
            counter ++;
        } else {
            inputLayoutPass.setErrorEnabled(false);
        }
        if(!isValidEmail(editTextEmail.getText())){
            inputLayoutEmail.setError("Invalid Email");
            requestFocus(editTextEmail);
            counter ++;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }

        if(!isValidPhone(mobile.getText())){
            inputLayoutMobile.setError("Invalid mobile number");
            requestFocus(mobile);
            counter ++;
        } else {
            inputLayoutMobile.setErrorEnabled(false);
        }
        if(gender.getSelectedItem().toString().equals("Gender")){
             inputLayoutGender.setError("Enter your gender");
            requestFocus(gender);
            counter ++;
        }else{
            inputLayoutGender.setErrorEnabled(false);
        }
        if(country_EditText_from.getText().toString().trim().isEmpty()){
            input_layout_country.setError("Enter your country");
            requestFocus(country_EditText_from);
            counter ++;
        }else{
            input_layout_country.setErrorEnabled(false);
        }

        if(counter == 0){
            return true;
        }
        else return false;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
    public final static boolean isValidPhone(CharSequence target) {
        int x = target.length();
        if(x!=11){
            return false;
        }
        return !TextUtils.isEmpty(target) && Patterns.PHONE.matcher(target).matches();
    }



    //select country
    private void setListener() {
        mCountryPicker.setListener(new CountryPickerListener() {
            @Override
            public void onSelectCountry(String name, String code, String dialCode,
                                        int flagDrawableResID) {
                country_EditText_from.setText(name);
                mCountryFlagImageView_from.setImageResource(flagDrawableResID);
               country= name;
                mCountryPicker.getDialog().dismiss();
            }
        });
        country_EditText_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCountryPicker.show(SignUp.this.getSupportFragmentManager(), "COUNTRY_PICKER");
            }
        });

       country= getUserCountryInfo();
    }

    private String getUserCountryInfo() {
        Country country = mCountryPicker.getUserCountryInfo(SignUp.this);
        mCountryFlagImageView_from.setImageResource(country.getFlag());

        country_EditText_from.setText(country.getName());
        return country.getName();
    }


}