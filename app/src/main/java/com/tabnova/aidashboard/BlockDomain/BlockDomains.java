package com.tabnova.aidashboard.BlockDomain;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.samsung.android.knox.AppIdentity;
import com.samsung.android.knox.EnterpriseDeviceManager;
import com.samsung.android.knox.net.firewall.DomainFilterRule;
import com.samsung.android.knox.net.firewall.Firewall;
import com.samsung.android.knox.net.firewall.FirewallResponse;
import com.samsung.android.knox.net.firewall.FirewallRule;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.SerializeObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BlockDomains {

    private final Context context;

    private final String chromiumPackageName = "com.android.chrome";

    public BlockDomains(Context context) {
        this.context = context;
    }

    public void addDomainRulesForChrome(
            ArrayList<String> mDenyListForChrome,
            ArrayList<String> mAllowListForChrome
    ) {
        mDenyListForChrome.add("*.googlezip.net");

        EnterpriseDeviceManager enterpriseDeviceManager =
                EnterpriseDeviceManager.getInstance(context);
        Firewall firewall = enterpriseDeviceManager.getFirewall();

        String signature = null;

        try {
            getPackageSignature(chromiumPackageName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // enable firewall IP rules enforcement
        FirewallResponse enableResponse = firewall.enableFirewall(true);
        FirewallResponse.Result enableResult = enableResponse.getResult();

        if (FirewallResponse.Result.FAILED != enableResult) {
            Log.e("BlockDomains", "onCreate: enableFirewall SUCCESS ");
            // get a FirewallRule[] containing rules to block IPv4 and IPv6
            // traffic on port 53 for the chromium app
            FirewallRule[] ipRules = getRulesToBlockIPv4AndIPv6(chromiumPackageName, signature);

            // apply the blocking rules to the firewall
            FirewallResponse[] ipResponse = firewall.addRules(ipRules);
            FirewallResponse.Result ipResult = ipResponse[0].getResult();

            if (FirewallResponse.Result.FAILED != ipResult) {
                Log.e("BlockDomains", "onCreate: addRules SUCCESS ");
                // configure domain filter rules
                // NOTE: mDenyListForChromium app contains an entry for *.googlezip.net
                List<DomainFilterRule> domainRules = new ArrayList<>();
                domainRules.add(
                        new DomainFilterRule(
                                new AppIdentity(chromiumPackageName, signature),
                                mDenyListForChrome, mAllowListForChrome
                        )
                );

                // add domain filter rules to firewall
                FirewallResponse[] domainResponse = firewall.addDomainFilterRules(domainRules);
                FirewallResponse.Result domainResult = domainResponse[0].getResult();

                if (FirewallResponse.Result.FAILED != domainResult) {
                    Log.e("BlockDomains", "onCreate: addDomainFilterRules SUCCESS ");
                } else {
                    Log.e("BlockDomains", "onCreate: addDomainFilterRules FAILED ");
                }
            } else {
                Log.e("BlockDomains", "onCreate: addRules FAILED ");
            }
        } else {
            Log.e("BlockDomains", "onCreate: enableFirewall FAILED ");
        }
    }

    public void removeDomainRulesForChrome(
            ArrayList<String> mDenyListForChrome,
            ArrayList<String> mAllowListForChrome,
            boolean clearAll
    ) {

        if (clearAll) {
            mAllowListForChrome.add("*.googlezip.net");
        }

        EnterpriseDeviceManager enterpriseDeviceManager = EnterpriseDeviceManager.getInstance(context);
        Firewall firewall = enterpriseDeviceManager.getFirewall();

        if (clearAll) {
            try {
                int bitmask = Firewall.FIREWALL_ALL_RULES;
                FirewallResponse[] clrResp = firewall.clearRules(bitmask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        // In order to validate the application's signature related to the rule, fill this
        // variable with the desired signature.
        String signature = null;
        try {
            signature = getPackageSignature(chromiumPackageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        try {
            if (clearAll) {
                FirewallResponse disableResponse = firewall.enableFirewall(false);
                FirewallResponse.Result disableResult = disableResponse.getResult();
                if (FirewallResponse.Result.FAILED != disableResult) {
                    // get a FirewallRule[] containing rules that block IPv4 and IPv6
                    // traffic on port 53 for the chromium app (these rules will be removed)
                    FirewallRule[] ipRules =
                            getRulesToBlockIPv4AndIPv6(chromiumPackageName, signature);
                    FirewallResponse[] ipResponse = firewall.removeRules(ipRules);
                    FirewallResponse.Result ipResult = ipResponse[0].getResult();
                    if (FirewallResponse.Result.FAILED != ipResult) {

                        // configure domain filter rules to remove
                        List<DomainFilterRule> rules = new ArrayList<>();
                        rules.add(
                                new DomainFilterRule(
                                        new AppIdentity(chromiumPackageName, signature),
                                        mDenyListForChrome, mAllowListForChrome
                                )
                        );

                        FirewallResponse[] domainResponse = firewall.removeDomainFilterRules(rules);

                        FirewallResponse.Result domainResult = domainResponse[0].getResult();

                        if (FirewallResponse.Result.FAILED != domainResult) {
                            if (clearAll) {
                                String dir1 = context.getFilesDir().getAbsolutePath();
                                File f01 = new File(dir1, Consts.blockDomainWebsiteFile);
                                if (f01.exists()) {
                                    f01.delete();
                                }

                                SerializeObject.WriteSettings(context, "", Consts.blockDomainWebsiteFile);
                            }

                            Log.e("BlockDomains", "onCreate: removeDomainFilterRules SUCCESS ");
                        } else {
                            Log.e("BlockDomains", "onCreate: removeDomainFilterRules FAILED ");
                        }
                    } else {
                        Log.e("BlockDomains", "onCreate: removeRules FAILED ");
                    }
                } else {
                    Log.e("BlockDomains", "onCreate: enableFirewall FAILED ");
                }
            } else {
                // get a FirewallRule[] containing rules that block IPv4 and IPv6
                // traffic on port 53 for the chromium app (these rules will be removed)
                FirewallRule[] ipRules =
                        getRulesToBlockIPv4AndIPv6(chromiumPackageName, signature);
                FirewallResponse[] ipResponse = firewall.removeRules(ipRules);
                FirewallResponse.Result ipResult = ipResponse[0].getResult();
                if (FirewallResponse.Result.FAILED != ipResult) {

                    // configure domain filter rules to remove
                    List<DomainFilterRule> rules = new ArrayList<>();
                    rules.add(
                            new DomainFilterRule(
                                    new AppIdentity(chromiumPackageName, signature),
                                    mDenyListForChrome, mAllowListForChrome
                            )
                    );

                    FirewallResponse[] domainResponse = firewall.removeDomainFilterRules(rules);

                    FirewallResponse.Result domainResult = domainResponse[0].getResult();

                    if (FirewallResponse.Result.FAILED != domainResult) {
                        if (clearAll) {
                            String dir1 = context.getFilesDir().getAbsolutePath();
                            File f01 = new File(dir1, Consts.blockDomainWebsiteFile);
                            if (f01.exists()) {
                                f01.delete();
                            }

                            SerializeObject.WriteSettings(context, "", Consts.blockDomainWebsiteFile);
                        }

                        Log.e("BlockDomains", "onCreate: removeDomainFilterRules SUCCESS ");
                    } else {
                        Log.e("BlockDomains", "onCreate: removeDomainFilterRules FAILED ");
                    }
                } else {
                    Log.e("BlockDomains", "onCreate: removeRules FAILED ");
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private String getPackageSignature(String packageName) throws PackageManager.NameNotFoundException {
        Signature[] signatures;
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo =
                    packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
            signatures = packageInfo.signingInfo.getSigningCertificateHistory();
        } else {
            packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            signatures = packageInfo.signatures;
        }
        return signatures[0].toCharsString();
    }

    private FirewallRule[] getRulesToBlockIPv4AndIPv6(
            String packageName,
            String signature
    ) {
        int MAX_CHROME_IP_RULES = 2;
        FirewallRule[] ipRules = new FirewallRule[MAX_CHROME_IP_RULES];

        // Block IPv4 traffic on port 53
        ipRules[0] = new FirewallRule(FirewallRule.RuleType.DENY, Firewall.AddressType.IPV4);
        ipRules[0].setIpAddress("*");
        ipRules[0].setPortNumber("53");
        ipRules[0].setApplication(new AppIdentity(packageName, signature));

        // Block IPv6 traffic on port 53
        ipRules[1] = new FirewallRule(FirewallRule.RuleType.DENY, Firewall.AddressType.IPV6);
        ipRules[1].setIpAddress("*");
        ipRules[1].setPortNumber("53");
        ipRules[1].setApplication(new AppIdentity(packageName, signature));
        return ipRules;
    }

    public void addDomainRulesForNonChromiumPackage(String packageName, ArrayList<String> mDenyListForNonChromiumPackage,
                                                    ArrayList<String> mAllowListForNonChromiumPackage) {
        EnterpriseDeviceManager enterpriseDeviceManager =
                EnterpriseDeviceManager.getInstance(context);
        Firewall firewall = enterpriseDeviceManager.getFirewall();

        // In order to validate the application's signature related to the rule, fill this
        // variable with the desired signature.
        String signature = null;

        try {
            signature = getPackageSignature(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        List<DomainFilterRule> rules = new ArrayList<>();
        rules.add(new DomainFilterRule(new AppIdentity(packageName, signature),
                mDenyListForNonChromiumPackage, mAllowListForNonChromiumPackage));

        try {
            FirewallResponse[] response = firewall.addDomainFilterRules(rules);
            if (response != null) {
                FirewallResponse.Result result = response[0].getResult();
                if (FirewallResponse.Result.FAILED != result) {
                    Log.e("BlockDomains", "addDomainRulesForNonChromiumPackage: SUCCESS");
                } else {
                    Log.e("BlockDomains", "addDomainRulesForNonChromiumPackage: FAILED");
                }
            } else {
                Log.e("BlockDomains", "addDomainRulesForNonChromiumPackage: Response NULL");
            }
        } catch (SecurityException e) {
            // missing required MDM permission
            e.printStackTrace();
        }
    }

    public void removeDomainRulesForNonChromiumPackage(String packageName, ArrayList<String> mDenyListForNonChromiumPackage,
                                                       ArrayList<String> mAllowListForNonChromiumPackage) {
        EnterpriseDeviceManager enterpriseDeviceManager =
                EnterpriseDeviceManager.getInstance(context);
        Firewall firewall = enterpriseDeviceManager.getFirewall();

        // In order to validate the application's signature related to the rule, fill this
        // variable with the desired signature.
        String signature = null;

        try {
            signature = getPackageSignature(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        try {
            int bitmask = Firewall.FIREWALL_ALL_RULES;
            FirewallResponse[] clrResp = firewall.clearRules(bitmask);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<DomainFilterRule> rules = new ArrayList<>();
        rules.add(new DomainFilterRule(new AppIdentity(packageName, signature),
                mDenyListForNonChromiumPackage, mAllowListForNonChromiumPackage));

        try {
            FirewallResponse[] response = firewall.removeDomainFilterRules(rules);
            if (response != null) {
                FirewallResponse.Result result = response[0].getResult();
                if (FirewallResponse.Result.FAILED != result) {
                    Log.e("BlockDomains", "removeDomainRulesForNonChromiumPackage: SUCCESS");
                } else {
                    Log.e("BlockDomains", "removeDomainRulesForNonChromiumPackage: FAILED");
                }
            } else {
                Log.e("BlockDomains", "removeDomainRulesForNonChromiumPackage: Response NULL");
            }
        } catch (SecurityException e) {
            // missing required MDM permission
            e.printStackTrace();
        }
    }
}
