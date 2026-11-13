package com.tabnova.aidashboard.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.gson.JsonObject;
import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.Adapter.HistoryListAdapter;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.APIManager;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.DialogLoader;
import com.tabnova.aidashboard.Extra.NetConnectivity;
import com.tabnova.aidashboard.Extra.RetrofitExtra;
import com.tabnova.aidashboard.Extra.RetrofitService;
import com.tabnova.aidashboard.Extra.SerializeObject;
import com.tabnova.aidashboard.Model.HistoryUrlModel;
import com.tabnova.aidashboard.Model.WebHistoryModel;
import com.tabnova.aidashboard.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SecureWebView extends BaseActivity {

    //WEB VIEW
    private WebView webView;

    // EDIT TEXT
    private AutoCompleteTextView edPath;

    // ADAPTER
    private ArrayAdapter<WebHistoryModel> adapter;
    private ArrayAdapter<HistoryUrlModel> webAdapter;

    // ARRAY LIST
    private ArrayList<WebHistoryModel> webHistoryModels;
    private ArrayList<HistoryUrlModel> historyUrlModels;

    // IMAGE VIEW
    private ImageView ivSearch;
    private ImageView ivClose;
    private ImageView ivBack;

    // STRING
    private String webPath;
    private String defaultUrl = "https://www.google.com/search?q=";
    String domainName;

    // INTEGER
    private int isFilter = 0;
    private int counter = 0;
    private int isShortCut = 0;

    // PROGRESS DIALOG
    private DialogLoader dialogLoader;

    // API MANAGER
    private APIManager apiManager;

    //CONTEXT
    private Context context;

    ValueCallback<Uri[]> mFilePathCallback;
    private Uri mCapturedImageURI = null;
    private ValueCallback<Uri> mUploadMessage;

    int INPUT_FILE_REQUEST_CODE = 1111;
    int FILECHOOSER_RESULTCODE = 2222;
    int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 3333;
    int MY_PERMISSIONS_REQUEST_RECORD_VIDEO = 4444;

    String mCameraPhotoPath = "";

    PermissionRequest webViewRequest;

    @Override
    protected boolean useLightStatusBarIcons() {
        return true;
    }

    @Override
    protected boolean useLightNavigationBarIcons() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_web_view);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.copyBackForwardList().getCurrentIndex() > 0) {
                    edPath.setText(webView.getUrl());
                    webView.goBack();
                } else {
                    // Your exit alert code, or alternatively line below to finish
                    String ser = SerializeObject.objectToString(webHistoryModels);
                    if (ser != null && !ser.equalsIgnoreCase("")) {
                        SerializeObject.WriteSettings(context, ser, Consts.webHistoryFile);
                    }


                    if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                        apiManager.uploadWebHistoryToServer();
                    }
                    finish();
                }
            }
        });

        context = this;

        apiManager = new APIManager(context);
        dialogLoader = new DialogLoader(context);


        //webPath = Consts.ServiceUrl+"";

        webView = findViewById(R.id.webview);
        edPath = findViewById(R.id.ed_path);
        ivSearch = findViewById(R.id.iv_search);
        ivClose = findViewById(R.id.iv_cancel);
        ivBack = findViewById(R.id.iv_back);

        if (getIntent() != null) {
            isShortCut = getIntent().getIntExtra(Consts.is_shortcut, 0);
            if (isShortCut == 1) {
                webPath = getIntent().getStringExtra(Consts.web_url);
                edPath.setEnabled(false);
                ivClose.setEnabled(false);
                ivSearch.setEnabled(false);
            } else {
                edPath.setEnabled(true);
                ivClose.setEnabled(true);
                ivSearch.setEnabled(true);
            }
        } else {
            edPath.setEnabled(true);
            ivClose.setEnabled(true);
            ivSearch.setEnabled(true);
        }


        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.getSettings().setSupportMultipleWindows(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (NetConnectivity.isOnline(SecureWebView.this)) {
                    if (request != null && request.getUrl() != null) {
                        Log.e("SecureWebView", request.getUrl().toString());
                    }
                }
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                webViewRequest = request;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    for (int permission = 0; permission < request.getResources().length; permission++) {
                        if (request.getResources()[permission].equalsIgnoreCase(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                            askForPermission(
                                    request.getOrigin().toString(),
                                    Manifest.permission.RECORD_AUDIO,
                                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO
                            );
                        } else if (request.getResources()[permission].equalsIgnoreCase(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                            askForPermission(
                                    request.getOrigin().toString(),
                                    Manifest.permission.CAMERA,
                                    MY_PERMISSIONS_REQUEST_RECORD_VIDEO
                            );
                        }
                    }
                }
            }

            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, FileChooserParams fileChooserParams) {
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                    mFilePathCallback = null;
                }
                mFilePathCallback = filePath;
                Intent i = fileChooserParams.createIntent();
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = Utils.createImageFile(context);
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.e("", "Unable to create Image File", ex);
                        ex.printStackTrace();
                    }

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        // App-specific Pictures/ shared through the FileProvider (file:// URIs crash on API 24+).
                        Uri photoUri = FileProvider.getUriForFile(context,
                                context.getPackageName() + ".provider", photoFile);
                        mCameraPhotoPath = photoUri.toString();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } else {
                        takePictureIntent = null;
                    }
                }
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("image/*");
                Intent[] intentArray;
                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, i);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(contentSelectionIntent, INPUT_FILE_REQUEST_CODE);

                return true;
            }
        });

        webHistoryModels = new ArrayList<>();
        historyUrlModels = new ArrayList<>();

        String ser = SerializeObject.ReadSettings(context, Consts.webHistoryFile);
        if (ser != null && !ser.equalsIgnoreCase("")) {
            Object obj = SerializeObject.stringToObject(ser);
            // Then cast it to your object and
            if (obj instanceof ArrayList) {
                // Do something
                webHistoryModels = (ArrayList<WebHistoryModel>) obj;
            }
        }

        if (webHistoryModels.size() > 0) {
            for (int i = 0; i < webHistoryModels.size(); i++) {
                if (webHistoryModels.get(i).getLinkStr().equals(webPath)) {
                    String endTime = new SimpleDateFormat("hh:mm:ss aa", Locale.getDefault()).format(new Date());
                    WebHistoryModel webHistoryModel = new WebHistoryModel();
                    webHistoryModel.setId(webHistoryModels.get(i).getId());
                    webHistoryModel.setLinkStr(webHistoryModels.get(i).getLinkStr());
                    webHistoryModel.setDate(webHistoryModels.get(i).getDate());
                    webHistoryModel.setTime(webHistoryModels.get(i).getTime());
                    webHistoryModel.setStartTime(webHistoryModels.get(i).getStartTime());
                    webHistoryModel.setEndTime(endTime);
                    webHistoryModels.set(i, webHistoryModel);
                }
            }
        }


        edPath.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    ivSearch.performClick();
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                } else if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    ivSearch.performClick();
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });


        webView.setWebViewClient(new WebViewClient() {


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                try {
                    URL newUrl = new URL(url);
                    if (domainName != null && !domainName.equals(newUrl.getHost())) {
                        if (webHistoryModels.size() > 0) {
                            int i = webHistoryModels.size() - 1;
                            if (!webHistoryModels.get(i).getLinkStr().equals(url)) {
                                String endTime = new SimpleDateFormat("hh:mm:ss aa", Locale.getDefault()).format(new Date());
                                WebHistoryModel webHistoryModel = new WebHistoryModel();
                                webHistoryModel.setId(webHistoryModels.get(i).getId());
                                webHistoryModel.setLinkStr(webHistoryModels.get(i).getLinkStr());
                                webHistoryModel.setDate(webHistoryModels.get(i).getDate());
                                webHistoryModel.setStartTime(webHistoryModels.get(i).getStartTime());
                                webHistoryModel.setEndTime(endTime);
                                webHistoryModel.setTime(CustomDashboardApplication.calculateMinutes(webHistoryModels.get(i).getStartTime(), endTime));
                                webHistoryModels.set(i, webHistoryModel);

                                String date = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault()).format(new Date());
                                String startTime = new SimpleDateFormat("hh:mm:ss aa", Locale.getDefault()).format(new Date());
                                webHistoryModels.add(new WebHistoryModel(url, startTime, "", "", date));
                            }
                        } else {
                            String date = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault()).format(new Date());
                            String startTime = new SimpleDateFormat("hh:mm:ss aa", Locale.getDefault()).format(new Date());
                            webHistoryModels.add(new WebHistoryModel(url, startTime, "", "", date));
                        }
                    }

                    webPath = url;
                    edPath.setText(url);
                    view.loadUrl(url);
                    //ivSearch.performClick();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                try {
                    URL newUrl = new URL(url);
                    domainName = newUrl.getHost();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
            getWebHistory();
        }

        webView.loadUrl(webPath);
        edPath.setText(webPath);


        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webPath = "";
                edPath.setText("");
            }
        });

        edPath.setSelection(edPath.getText().toString().length());

        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                if (edPath.getText().toString().trim().startsWith("http")) {
                    webPath = edPath.getText().toString().trim();
                } else if (edPath.getText().toString().trim().contains(defaultUrl)) {
                    webPath = defaultUrl + edPath.getText().toString().trim();
                } else {
                    webPath = defaultUrl + edPath.getText().toString().trim();
                }

                webView.loadUrl(webPath);
                edPath.setText(webPath);

                if (webHistoryModels.size() > 0) {
                    for (int i = 0; i < webHistoryModels.size(); i++) {
                        if (webHistoryModels.get(i).getEndTime().equals("")) {
                            String endTime = new SimpleDateFormat("hh:mm:ss aa", Locale.getDefault()).format(new Date());
                            WebHistoryModel webHistoryModel = new WebHistoryModel();
                            webHistoryModel.setId(webHistoryModels.get(i).getId());
                            webHistoryModel.setLinkStr(webHistoryModels.get(i).getLinkStr());
                            webHistoryModel.setDate(webHistoryModels.get(i).getDate());
                            webHistoryModel.setStartTime(webHistoryModels.get(i).getStartTime());
                            webHistoryModel.setEndTime(endTime);
                            webHistoryModel.setTime(CustomDashboardApplication.calculateMinutes(webHistoryModels.get(i).getStartTime(), endTime));
                            webHistoryModels.set(i, webHistoryModel);
                        }
                    }

                    String ser = SerializeObject.objectToString(webHistoryModels);
                    if (ser != null && !ser.equalsIgnoreCase("")) {
                        SerializeObject.WriteSettings(context, ser, Consts.webHistoryFile);
                    }

                    if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                        apiManager.uploadWebHistoryToServer();
                    }
                } else {
                    String date = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault()).format(new Date());
                    String startTime = new SimpleDateFormat("hh:mm:ss aa", Locale.getDefault()).format(new Date());
                    webHistoryModels.add(new WebHistoryModel(webPath, startTime, "", "", date));
                }
            }
        });

        edPath.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                webView.loadUrl(historyUrlModels.get(position).getUrl());
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                ivSearch.performClick();
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

    }


    public void getWebHistory() {
        try {
            if (NetConnectivity.isOnline(context)) {

                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // API CALL
                Call<JsonObject> calls = feedApi.getWebHistory(CustomDashboardApplication.getString(context, Consts.token));

                Log.e("serverReq", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.body() != null) {
                            Log.e("serverReq", response.body().toString());
                            try {
                                JSONObject responseObj = new JSONObject(response.body().toString());

                                Object dataObj = responseObj.get(Consts.data);
                                if (dataObj instanceof JSONArray) {
                                    JSONArray dataArray = responseObj.getJSONArray(Consts.data);
                                    if (dataArray.length() > 0) {

                                        historyUrlModels = new ArrayList<>();
                                        historyUrlModels.clear();
                                        for (int i = 0; i < dataArray.length(); i++) {
                                            JSONObject urlObj = dataArray.getJSONObject(i);
                                            historyUrlModels.add(i, new HistoryUrlModel(i, urlObj.getString(Consts.domain_url)));
                                        }

                                        webAdapter = new HistoryListAdapter(SecureWebView.this, historyUrlModels);
                                        //edPath.setThreshold(1);
                                        edPath.setAdapter(webAdapter);
                                    }
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }


                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                });
            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void askForPermission(
            String origin,
            String permission,
            int requestCode
    ) {
        Log.e("WebView", "inside askForPermission for" + origin + "with" + permission);
        if (ContextCompat.checkSelfPermission(
                SecureWebView.this,
                permission
        )
                != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    permission
            )
            ) {

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                        this, new String[]{permission},
                        requestCode
                );
            }
        } else {
            try {
                if (webViewRequest != null)
                    webViewRequest.grant(webViewRequest.getResources());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
