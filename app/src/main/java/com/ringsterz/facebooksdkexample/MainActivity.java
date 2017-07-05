package com.ringsterz.facebooksdkexample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_main);

        final LoginButton loginBtn = (LoginButton) findViewById(R.id.login_button);
        final TextView mTextView = (TextView) findViewById(R.id.text_view);
        final Button postBtn = (Button) findViewById(R.id.post_button);
        final Button fetchBtn = (Button) findViewById(R.id.fetch_button);
        final EditText postContent = (EditText) findViewById(R.id.post_content);
        final ListView listView = (ListView) findViewById(R.id.listView);
        loginBtn.setReadPermissions("public_profile");
        LoginManager.getInstance().logInWithPublishPermissions(this,
                Arrays.asList("publish_actions")
        );

        // 로그인 콜백 등록
        loginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = AccessToken.getCurrentAccessToken();

                GraphRequest request = GraphRequest.newMeRequest(
                        accessToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                // 로그인 완료됐을때 콜백
                                try {
                                    mTextView.setText("Logged in as:" + object.getString("name"));
                                } catch (JSONException je) {
                                    Log.e("FB", "No key provided.");
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        // Page에 포스팅 (Community 탭에서 확인가능하다. 데모 시 SANDBOX 끄는 것 잊지 말 것)
        // https://developers.facebook.com/apps/100128243969071/review-status/
        // 앱 공개로 설정해야 Public 에게도 보인다.

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle params = new Bundle();
                params.putString("message", postContent.getText().toString());
                /* make the API call */
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/912605208889540/feed", // 페이지 ID
                        params,
                        HttpMethod.POST,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                            /* handle the result */
                                Toast.makeText(getBaseContext(), "Write Complete", Toast.LENGTH_SHORT).show();
                            }
                        }
                ).executeAsync();
            }
        });


        fetchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* make the API call */
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/912605208889540/feed",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                /* handle the result */
                                JSONObject jsonObj = response.getJSONObject();
                                JSONArray jsonArray = null;
                                try {
                                    jsonArray = jsonObj.getJSONArray("data");
                                } catch (JSONException je) {
                                    Log.e("FB", "Error fetching JSON");
                                }

                                adapter = new ArrayAdapter<>(getBaseContext(),
                                        android.R.layout.simple_list_item_1);

                                for(int i=0; i<jsonArray.length(); i++){
                                    try {
                                        String message = jsonArray.getJSONObject(i).getString("message");
                                        Log.e("FB", message);
                                        adapter.add(message);
                                    } catch (JSONException je) {
                                        Log.e("FB", "Error fetching JSON");
                                    }
                                }

                                listView.setAdapter(adapter);




                            }
                        }
                ).executeAsync();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data); // 콜매니저에 결과 전달
    }
}