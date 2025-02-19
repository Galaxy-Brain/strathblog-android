package com.steven.blogapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.steven.blogapp.Fragments.HomeFragment;
import com.steven.blogapp.Models.Post;
import com.steven.blogapp.Models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {
    private Button btnPost;
    private ImageView imgPost;
    private EditText txtDesc, txtTitle;
    private Bitmap bitmap = null;
    private static final  int GALLERY_CHANGE_POST = 3;
    private ProgressDialog dialog;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        init();
    }

    private void init() {
        preferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        btnPost = findViewById(R.id.btnAddPost);
        imgPost = findViewById(R.id.imgAddPost);
        txtDesc = findViewById(R.id.txtDescAddPost);
        txtTitle = findViewById(R.id.txtTitleAddPost);
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);

        imgPost.setImageURI(getIntent().getData());
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),getIntent().getData());
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        btnPost.setOnClickListener(v->{
            if(!txtDesc.getText().toString().isEmpty()){
                try {
                    post();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                Toast.makeText(this, "Post description is required", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void post() throws JSONException {
        dialog.setMessage("Posting");
        dialog.show();

        JSONObject data = new JSONObject();
        data.put("title", txtTitle);
        data.put("desc", txtDesc);
        data.put("photo", imgPost);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, Constant.ADD_POST, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")){
                        JSONObject postObject = response.getJSONObject("post");
                        JSONObject userObject = postObject.getJSONObject("user");

                        User user = new User();
                        user.setId(userObject.getInt("id"));
                        user.setUserName(userObject.getString("name")+" "+userObject.getString("lastname"));
                        user.setPhoto(userObject.getString("photo"));

                        Post post = new Post();
                        post.setUser(user);
                        post.setId(postObject.getInt("id"));
                        post.setSelfLike(false);
                        post.setPhoto(postObject.getString("photo"));
                        post.setDesc(postObject.getString("desc"));
                        post.setComments(0);
                        post.setLikes(0);
                        post.setDate(postObject.getString("created_at"));

                        HomeFragment.arrayList.add(0,post);
                        HomeFragment.recyclerView.getAdapter().notifyItemInserted(0);
                        HomeFragment.recyclerView.getAdapter().notifyDataSetChanged();
                        Toast.makeText(AddPostActivity.this, "Posted", Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                        Toast.makeText(AddPostActivity.this, "Something happened", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AddPostActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                header.put("Authorization", preferences.getString("token",""));
                return header;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(AddPostActivity.this);
        queue.add(request);

    }

    private String bitmapToString(Bitmap bitmap) {
        if (bitmap!=null){
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            byte [] array = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(array,Base64.DEFAULT);
        }

        return "";
    }


    public void cancelPost(View view) {
        super.onBackPressed();
    }

    public void changePhoto(View view) {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i,GALLERY_CHANGE_POST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_CHANGE_POST && resultCode==RESULT_OK){
            Uri imgUri = data.getData();
            imgPost.setImageURI(imgUri);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imgUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
