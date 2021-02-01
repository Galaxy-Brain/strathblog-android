package com.steven.blogapp.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.steven.blogapp.AuthActivity;
import com.steven.blogapp.Constant;
import com.steven.blogapp.HomeActivity;
import com.steven.blogapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignInFragment extends Fragment {
    private View view;
    private TextInputLayout layoutEmail,layoutPassword;
    private TextInputEditText txtEmail,txtPassword;
    private TextView txtSignUp;
    private Button btnSignIn;
    private ProgressDialog dialog;
    private String device_name;

    public SignInFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_sign_in,container,false);
        init();
        return view;
    }

    private void init() {
        layoutPassword = view.findViewById(R.id.txtLayoutPasswordSignIn);
        layoutEmail = view.findViewById(R.id.txtLayoutEmailSignIn);
        txtPassword = view.findViewById(R.id.txtPasswordSignIn);
        txtSignUp = view.findViewById(R.id.txtSignUp);
        txtEmail = view.findViewById(R.id.txtEmailSignIn);
        device_name = Build.DEVICE;
        btnSignIn = view.findViewById(R.id.btnSignIn);
        dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);

        txtSignUp.setOnClickListener(v->{
            //change fragments
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameAuthContainer,new SignUpFragment()).commit();
        });

        btnSignIn.setOnClickListener(v->{
            //validate fields first
            if (validate()){
                try {
                    login();
                } catch (JSONException e) {
                    Toast.makeText((AuthActivity)getContext(), "Unaccounted For Error", Toast.LENGTH_SHORT).show();
                }
            }
        });


        txtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!txtEmail.getText().toString().isEmpty()){
                    layoutEmail.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtPassword.getText().toString().length()>7){
                    layoutPassword.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    private boolean validate (){
        if (txtEmail.getText().toString().isEmpty()){
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError("Email is Required");
            return false;
        }
        if (txtPassword.getText().toString().length()<8){
            layoutPassword.setErrorEnabled(true);
            layoutPassword.setError("Required at least 8 characters");
            return false;
        }
        return true;
    }


    private void login () throws JSONException {
        dialog.setMessage("Logging in");
        dialog.show();
        JSONObject data = new JSONObject();
        data.put("email", txtEmail.getText().toString());
        data.put("password", txtPassword.getText().toString());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, Constant.LOGIN, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        JSONObject user = response.getJSONObject("user");
                        //make shared preference user
                        SharedPreferences userPref = getActivity().getApplicationContext().getSharedPreferences("user", getContext().MODE_PRIVATE);
                        SharedPreferences.Editor editor = userPref.edit();
                        editor.putString("token", response.getString("token"));
                        editor.putString("name", user.getString("name"));
                        editor.putInt("id", user.getInt("id"));
                        editor.putString("lastname", user.getString("lastname"));
                        editor.putString("photo", user.getString("profile_photo_path"));
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();
                        //if success
                        startActivity(new Intent(getContext(), HomeActivity.class));
                        ((AuthActivity) getContext()).finish();
                        Toast.makeText(getContext(), "Login Success", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText((AuthActivity) getContext(), "JSON Exception", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error if connection not success
                Toast.makeText((AuthActivity)getContext(), "Volley Error", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        //add this request to requestqueue
        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);
    }














}
