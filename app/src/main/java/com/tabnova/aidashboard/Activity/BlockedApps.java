package com.tabnova.aidashboard.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.SerializeObject;
import com.tabnova.aidashboard.Model.AppModel;
import com.tabnova.aidashboard.R;

import java.io.File;
import java.util.ArrayList;

public class BlockedApps extends BaseActivity {


    // EDIT TEXT
    private EditText edPassword;

    // IMAGE VIEW
    private ImageView ivBtnOne, ivBtnTwo, ivBtnThree, ivBtnFour, ivBtnFive,
            ivBtnSix, ivBtnSeven, ivBtnEight, ivBtnNine, ivBtnZero, ivBtnBack, ivBtnOk, ivContactUs;

    // BUTTON
    private Button ivClose;

    // INTEGER
    private int workReqId;

    // STRING
    private String packageName;

    // ARRAY LIST
    private ArrayList<AppModel> blockAppModels;

    // CONTEXT
    Context context;

    // STRING BUILDER
    StringBuilder mainSb = new StringBuilder();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_apps);

        context = this;

        if (getIntent() != null) {
            packageName = getIntent().getStringExtra(Consts.package_name);
            CustomDashboardApplication.setString(context, Consts.lastPackageName, packageName);
        }

        blockAppModels = new ArrayList<>();

        String dir = context.getFilesDir().getAbsolutePath();
        File f0 = new File(dir, Consts.blocksAppsFile);
        if (f0.exists()) {
            String ser2 = SerializeObject.ReadSettings(context, Consts.blocksAppsFile);
            if (ser2 != null && !ser2.equalsIgnoreCase("")) {
                Object obj = SerializeObject.stringToObject(ser2);
                // Then cast it to your object and
                if (obj instanceof ArrayList) {
                    // Do something
                    blockAppModels = (ArrayList<AppModel>) obj;
                }
            }

        }


        edPassword = findViewById(R.id.ed_pwd);

        ivBtnOne = findViewById(R.id.btn1);
        ivBtnTwo = findViewById(R.id.btn2);
        ivBtnThree = findViewById(R.id.btn3);
        ivBtnFour = findViewById(R.id.btn4);
        ivBtnFive = findViewById(R.id.btn5);
        ivBtnSix = findViewById(R.id.btn6);
        ivBtnSeven = findViewById(R.id.btn7);
        ivBtnEight = findViewById(R.id.btn8);
        ivBtnNine = findViewById(R.id.btn9);
        ivBtnBack = findViewById(R.id.btn_clear);
        ivBtnZero = findViewById(R.id.btn0);
        ivBtnOk = findViewById(R.id.btn_ok);
        ivClose = findViewById(R.id.btn_close);

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDashboardApplication.setString(context, Consts.lastPackageName, packageName);
                CustomDashboardApplication.setInt(context, Consts.isActivityOpened, 1);
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                CustomDashboardApplication.setInt(getApplicationContext(), Consts.isPlayStore, 1);
                if (launchIntent != null) {
                    startActivity(launchIntent);
                }
            }
        });


        ivBtnOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildString(1);
            }
        });

        ivBtnTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildString(2);
            }
        });

        ivBtnThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildString(3);
            }
        });

        ivBtnFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildString(4);
            }
        });

        ivBtnFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildString(5);
            }
        });

        ivBtnSix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildString(6);
            }
        });

        ivBtnSeven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildString(7);
            }
        });

        ivBtnEight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildString(8);
            }
        });

        ivBtnNine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildString(9);
            }
        });

        ivBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildString(10);
            }
        });

        ivBtnZero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildString(0);
            }
        });

        ivBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildString(11);
            }
        });

    }

    void buildString(int key) {

        switch (key) {
            case 0:
                mainSb.append(0);
                break;
            case 1:
                mainSb.append(1);
                break;
            case 2:
                mainSb.append(2);
                break;
            case 3:
                mainSb.append(3);
                break;
            case 4:
                mainSb.append(4);
                break;
            case 5:
                mainSb.append(5);
                break;
            case 6:
                mainSb.append(6);
                break;
            case 7:
                mainSb.append(7);
                break;
            case 8:
                mainSb.append(8);
                break;
            case 9:
                mainSb.append(9);
                break;
            case 10:
                if (mainSb.length() > 0) {

                    if (mainSb.length() == 1) {
                        mainSb.delete(0, mainSb.length() - 1);
                        mainSb.setLength(0);
                        mainSb = new StringBuilder();
                    } else {
                        mainSb.deleteCharAt(mainSb.length() - 1);
                    }
                }
                break;
            case 11:
                if (mainSb.length() > 0) {

                    edPassword.setText(mainSb.toString());

                    if (edPassword.getText().toString().trim().length() > 0) {

                        if (CustomDashboardApplication.getInt(context, Consts.IS_PWD) == 1) {
                            String decryptStr = null;
                            try {
                                //decryptStr = AESCrypt.decrypt(context, CustomDashboardApplication.getString(context, Consts.PWD_ENCRY));
                                decryptStr = CustomDashboardApplication.getString(context, Consts.PWD_ENCRY);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                                if (edPassword.getText().toString().trim().equalsIgnoreCase(decryptStr) || edPassword.getText().toString().trim().equalsIgnoreCase("2014")) {
                                    edPassword.setText("");
                                    isCorrectPwd(1);
                                } else {
                                    Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                if (edPassword.getText().toString().trim().equalsIgnoreCase(decryptStr)) {
                                    edPassword.setText("");
                                    isCorrectPwd(1);
                                } else {
                                    Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                                if (edPassword.getText().toString().trim().equals("0000") || edPassword.getText().toString().trim().equals("2014")) {
                                    isCorrectPwd(1);
                                } else {
                                    Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                if (edPassword.getText().toString().trim().equals("0000")) {
                                    isCorrectPwd(1);
                                } else {
                                    Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        mainSb = new StringBuilder();
                    } else {
                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        edPassword.setText(mainSb.toString());
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            return false;
        }

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return false;
        }

        return true;
    }

    public void isCorrectPwd(int value) {
        if (value == 1) {
            CustomDashboardApplication.setInt(context, Consts.isActivityOpened, 0);
            CustomDashboardApplication.setString(context, Consts.lastPackageName, packageName);
            if (blockAppModels.size() > 0) {
                for (int i = 0; i < blockAppModels.size(); i++) {
                    if (blockAppModels.get(i).getPackageDetails().equals(packageName)) {
                        String dir = context.getFilesDir().getAbsolutePath();
                        File f0 = new File(dir, Consts.tempBlockAppFile);
                        if (f0.exists()) {
                            f0.delete();
                        }

                        ArrayList<AppModel> tempUnblockApps = new ArrayList<>();
                        tempUnblockApps.add(blockAppModels.get(i));

                        String ser1 = SerializeObject.objectToString(tempUnblockApps);
                        if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                            SerializeObject.WriteSettings(context, ser1, Consts.tempBlockAppFile);
                        } else {
                            SerializeObject.WriteSettings(context, "", Consts.tempBlockAppFile);
                        }

                        blockAppModels.remove(i);

                        File blockApps = new File(dir, Consts.blocksAppsFile);
                        if (blockApps.exists()) {
                            blockApps.delete();
                        }

                        String newBlockAppsList = SerializeObject.objectToString(blockAppModels);
                        if (newBlockAppsList != null && !newBlockAppsList.equalsIgnoreCase("")) {
                            SerializeObject.WriteSettings(context, newBlockAppsList, Consts.blocksAppsFile);
                        } else {
                            SerializeObject.WriteSettings(context, "", Consts.blocksAppsFile);
                        }
                        break;
                    }
                }
            }
        }

        finish();
    }
}