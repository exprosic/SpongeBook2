package com.example.exprosic.spongebook2;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.exprosic.spongebook2.booklist.BookshelfItem;
import com.example.exprosic.spongebook2.utils.InputMethodUtils;
import com.example.exprosic.spongebook2.utils.net.StringFailureJsonResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity {
    public static final String TAG = LoginActivity.class.getSimpleName();

    @Bind(R.id.username_edit) EditText mUsernameEdit;
    @Bind(R.id.password_edit) EditText mPasswordEdit;
    @Bind(R.id.login_button) Button mLoginButton;

    public static void startAlone(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MyApplication.getAuthorizeToken() != null) {
            MainActivity.start(this);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mUsernameEdit.setOnFocusChangeListener(onFocusChangeListener);
        mPasswordEdit.setOnFocusChangeListener(onFocusChangeListener);
        showInputMethod();
    }

    @OnClick(R.id.login_button)
    void login() {
        Log.d(TAG, "loggin in");
        if (mUsernameEdit.length()==0 || mPasswordEdit.length()==0) {
            Toast.makeText(this, R.string.username_password_empty_msg, Toast.LENGTH_SHORT).show();
            return;
        }

        mLoginButton.setText(R.string.loggingin);
        mLoginButton.setClickable(false);
        String username = mUsernameEdit.getText().toString();
        String password = mPasswordEdit.getText().toString();
        MyApplication.getUnauthorizedClient().post(this, URLManager.login.URL, URLManager.login.params(username, password), new StringFailureJsonResponseHandler() {
           @Override
           public void onSuccess(int status, Header[] headers, JSONObject jsonObject) {
               try {
                   String token = jsonObject.getString("token");
                   int userId = jsonObject.getInt("userId");
                   MyApplication.setAuthorizeToken(token);
                   MyApplication.setMyUserId(userId);

                   List<BookshelfItem> bookshelfItems = BookshelfItem.getCollectionFromJson(jsonObject.getJSONObject("books"));
                   MyApplication.getBookListProvider().syncDb(bookshelfItems);

                   MainActivity.start(LoginActivity.this);
                   finish();
               } catch (JSONException e) {
                   Log.e(TAG, "wrong login response format", new Throwable());
                   throw new AssertionError();
               }
           }

            @Override
            public void onFailure(int status, Header[] headers, String response, Throwable throwable) {
                String msg = status == HttpURLConnection.HTTP_BAD_REQUEST
                        ? getResources().getString(R.string.username_password_wrong)
                        : String.format(Locale.CHINESE, getResources().getString(R.string.format__login_fail_with_status), status);
                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                mLoginButton.setText(R.string.login);
                mLoginButton.setClickable(true);
            }
        });
    }

    private void showInputMethod() {
        if (!mUsernameEdit.requestFocus())
            return;
        InputMethodUtils.show(mUsernameEdit);
    }

    private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                InputMethodUtils.hide(v);
            } else {
                InputMethodUtils.show(v);
            }
        }
    };
}
