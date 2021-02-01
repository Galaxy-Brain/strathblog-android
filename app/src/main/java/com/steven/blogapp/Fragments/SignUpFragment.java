package com.steven.blogapp.Fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.steven.blogapp.R;
import com.steven.blogapp.UserInfoActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUpFragment extends Fragment {
    private View view;
    private TextInputLayout layoutEmail,layoutPassword,layoutConfirm;
    private TextInputEditText txtEmail,txtPassword,txtConfirm;
    private TextView txtSignIn;
    private Button btnSignUp;
    private ProgressDialog dialog;

    public SignUpFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_sign_up,container,false);
        init();
        return view;
    }

    private void init() {
        layoutPassword = view.findViewById(R.id.txtLayoutPasswordSignUp);
        layoutEmail = view.findViewById(R.id.txtLayoutEmailSignUp);
        layoutConfirm = view.findViewById(R.id.txtLayoutConfrimSignUp);
        txtPassword = view.findViewById(R.id.txtPasswordSignUp);
        txtConfirm = view.findViewById(R.id.txtConfirmSignUp);
        txtSignIn = view.findViewById(R.id.txtSignIn);
        txtEmail = view.findViewById(R.id.txtEmailSignUp);
        btnSignUp = view.findViewById(R.id.btnSignUp);
        dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);

        txtSignIn.setOnClickListener(v->{
            //change fragments
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frameAuthContainer,new SignInFragment()).commit();
        });

        btnSignUp.setOnClickListener(v->{
            //validate fields first
            if (validate()){
                try {
                    register();
                } catch (JSONException e) {
                    Toast.makeText((AuthActivity)getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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

        txtConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtConfirm.getText().toString().equals(txtPassword.getText().toString())){
                    layoutConfirm.setErrorEnabled(false);
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
        if (!txtConfirm.getText().toString().equals(txtPassword.getText().toString())){
            layoutConfirm.setErrorEnabled(true);
            layoutConfirm.setError("Password does not match");
            return false;
        }


        return true;
    }


    private void register() throws JSONException {
        dialog.setMessage("Registering");
        dialog.show();
        JSONObject data = new JSONObject();
        data.put("email", txtEmail.getText().toString());
        data.put("password", txtPassword.getText().toString());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, Constant.REGISTER, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //we get response if connection success
                try {
                    if (response.getBoolean("success")) {
                        JSONObject user = response.getJSONObject("user");
                        //make shared preference user
                        SharedPreferences userPref = getActivity().getApplicationContext().getSharedPreferences("user", getContext().MODE_PRIVATE);
                        SharedPreferences.Editor editor = userPref.edit();
                        editor.putString("token", response.getString("token"));
                        editor.putString("name", user.getString("name"));
                        editor.putInt("id", user.getInt("id"));
                        editor.putString("photo", user.getString("profile_photo_path"));
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();
                        //if success
                        startActivity(new Intent(((AuthActivity) getContext()), UserInfoActivity.class));
                        ((AuthActivity) getContext()).finish();
                        Toast.makeText(getContext(), "Register Success", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText((AuthActivity)getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText((AuthActivity)getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        //add this request to RequestQueue
        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
        queue.add(request);
    }


}
