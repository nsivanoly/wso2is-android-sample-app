package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ClientSecretBasic;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okio.Okio;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HttpsURLConnection;
import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;

public class UserInfoActivity extends AppCompatActivity {

    String idToken;
    String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Disable SSL certificate validation before making any requests
        disableSSLCertificateValidation();

        handleAuthorizationResponse(getIntent());
    }

    // Disable SSL certificate validation (Development only)
    public static void disableSSLCertificateValidation() {
        try {
            // Create a TrustManager that does not perform any checks
            TrustManager[] trustAllCertificates = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null; // Accept all certificates
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            // No validation is performed
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            // No validation is performed
                        }
                    }
            };

            // Set up the SSL context with the trust manager that trusts all certificates
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCertificates, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Accept all hostnames (including invalid hostnames)
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            Log.i("SSL", "SSL certificate validation disabled");

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private void handleAuthorizationResponse(Intent intent) {
        final AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
        String secret = "<<client secre>>";
        Map<String, String> additionalParameters = new HashMap<>();
        additionalParameters.put("client_secret", secret);

        AuthorizationException error = AuthorizationException.fromIntent(intent);
        final AuthState authState = new AuthState(response, error);
        AuthorizationService service = new AuthorizationService(this);
        performTokenRequest(service, response.createTokenExchangeRequest(additionalParameters), this::handleCodeExchangeResponse);
    }

    private void performTokenRequest(AuthorizationService authService, TokenRequest request,
                                     AuthorizationService.TokenResponseCallback callback) {
        String secret = "<<client secre>>";
        ClientAuthentication clientAuthentication = new ClientSecretBasic(secret);
        authService.performTokenRequest(request, clientAuthentication, callback);
    }

    private void handleCodeExchangeResponse(TokenResponse tokenResponse, AuthorizationException authException) {
        idToken = tokenResponse.idToken;
        accessToken = tokenResponse.accessToken;
        callUserInfo();
    }

    private void callUserInfo() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                URL userInfoEndpoint = new URL("https://<your-wso2-is-domain>/oauth2/userinfo");
                HttpURLConnection conn = (HttpURLConnection) userInfoEndpoint.openConnection();
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setInstanceFollowRedirects(false);
                String response = Okio.buffer(Okio.source(conn.getInputStream())).readString(Charset.forName("UTF-8"));
                JSONObject json = new JSONObject(response);

                TextView username = findViewById(R.id.username);
                TextView useremailId = findViewById(R.id.emailid);
                TextView accesstokenview = findViewById(R.id.accesstoken);
                TextView idtokenview = findViewById(R.id.idtoken);
                TextView textviewuser = findViewById(R.id.textView6);

                accesstokenview.setText(accessToken);
                idtokenview.setText(decodeIDToken(idToken));

                username.setText(json.getString("sub"));
                useremailId.setText(json.getString("email"));
                textviewuser.setText(json.getString("sub"));

                TextView textViewName = findViewById(R.id.usernameview);
                TextView textViewEmail = findViewById(R.id.emailview);
                TextView textviewToken = findViewById(R.id.tokentextView);
                TextView textviewidtoken = findViewById(R.id.idtextView);

                textViewEmail.setText("Email Address");
                textViewName.setText("Username");
                textviewToken.setText("Access Token");
                textviewidtoken.setText("Id token");

                Button btnClick = findViewById(R.id.logout);
                btnClick.setOnClickListener(new LogoutListener());

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public class LogoutListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {
            String logout_uri = "https://<your-wso2-is-domain>/oidc/logout";
            String redirect = "com.example.myapplication://oauth";
            StringBuffer url = new StringBuffer();
            url.append(logout_uri);
            url.append("?id_token_hint=");
            url.append(idToken);
            url.append("&post_logout_redirect_uri=");
            url.append(redirect);

            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            customTabsIntent.launchUrl(view.getContext(), Uri.parse(url.toString()));
        }
    }

    private String decodeIDToken(String JWTEncoded) throws Exception {
        String[] split = JWTEncoded.split("\\.");
        Log.i("JWT", "Header: " + split[0]);
        Log.i("JWT", "Header: " + split[1]);
        try {
            Log.i("JWT_DECODED", "Header: " + getJson(split[0]));
            Log.i("JWT_DECODED", "Body: " + getJson(split[1]));
        } catch (UnsupportedEncodingException e) {
            //Error
        }
        return getJson(split[1]);
    }

    private String getJson(String strEncoded) throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }
}
