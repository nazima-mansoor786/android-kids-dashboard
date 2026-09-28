package com.tabnova.aidashboard.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProfileData {

    @SerializedName("data")
    @Expose
    private Data data;
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("responseMessage")
    @Expose
    private String responseMessage;


    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }


    public class Data {

        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("email")
        @Expose
        private String email;
        @SerializedName("role_id")
        @Expose
        private Integer roleId;
        @SerializedName("api_auth_token")
        @Expose
        private String apiAuthToken;
        @SerializedName("master_user")
        @Expose
        private Object masterUser;
        @SerializedName("company_name")
        @Expose
        private Object companyName;
        @SerializedName("age")
        @Expose
        private Object age;
        @SerializedName("address")
        @Expose
        private Object address;
        @SerializedName("user_status")
        @Expose
        private Integer userStatus;
        @SerializedName("reminder_email_status")
        @Expose
        private Integer reminderEmailStatus;
        @SerializedName("created_at")
        @Expose
        private Object createdAt;
        @SerializedName("updated_at")
        @Expose
        private Object updatedAt;
        @SerializedName("expiration_date")
        @Expose
        private Object expirationDate;
        @SerializedName("enterprise_id")
        @Expose
        private Object enterpriseId;
        @SerializedName("company_id")
        @Expose
        private Object companyId;
        @SerializedName("user_profiles")
        @Expose
        private List<UserProfile> userProfiles = null;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Integer getRoleId() {
            return roleId;
        }

        public void setRoleId(Integer roleId) {
            this.roleId = roleId;
        }

        public String getApiAuthToken() {
            return apiAuthToken;
        }

        public void setApiAuthToken(String apiAuthToken) {
            this.apiAuthToken = apiAuthToken;
        }

        public Object getMasterUser() {
            return masterUser;
        }

        public void setMasterUser(Object masterUser) {
            this.masterUser = masterUser;
        }

        public Object getCompanyName() {
            return companyName;
        }

        public void setCompanyName(Object companyName) {
            this.companyName = companyName;
        }

        public Object getAge() {
            return age;
        }

        public void setAge(Object age) {
            this.age = age;
        }

        public Object getAddress() {
            return address;
        }

        public void setAddress(Object address) {
            this.address = address;
        }

        public Integer getUserStatus() {
            return userStatus;
        }

        public void setUserStatus(Integer userStatus) {
            this.userStatus = userStatus;
        }

        public Integer getReminderEmailStatus() {
            return reminderEmailStatus;
        }

        public void setReminderEmailStatus(Integer reminderEmailStatus) {
            this.reminderEmailStatus = reminderEmailStatus;
        }

        public Object getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Object createdAt) {
            this.createdAt = createdAt;
        }

        public Object getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(Object updatedAt) {
            this.updatedAt = updatedAt;
        }

        public Object getExpirationDate() {
            return expirationDate;
        }

        public void setExpirationDate(Object expirationDate) {
            this.expirationDate = expirationDate;
        }

        public Object getEnterpriseId() {
            return enterpriseId;
        }

        public void setEnterpriseId(Object enterpriseId) {
            this.enterpriseId = enterpriseId;
        }

        public Object getCompanyId() {
            return companyId;
        }

        public void setCompanyId(Object companyId) {
            this.companyId = companyId;
        }

        public List<UserProfile> getUserProfiles() {
            return userProfiles;
        }

        public void setUserProfiles(List<UserProfile> userProfiles) {
            this.userProfiles = userProfiles;
        }

    }


    public class UserProfile {

        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("profile_name")
        @Expose
        private String profileName;
        @SerializedName("profile_id")
        @Expose
        private String profileId;
        @SerializedName("parent_profile_id")
        @Expose
        private Object parentProfileId;
        @SerializedName("region")
        @Expose
        private Object region;
        @SerializedName("country")
        @Expose
        private Object country;
        @SerializedName("city")
        @Expose
        private Object city;
        @SerializedName("company")
        @Expose
        private Object company;
        @SerializedName("product")
        @Expose
        private Object product;
        @SerializedName("segment")
        @Expose
        private Object segment;
        @SerializedName("category")
        @Expose
        private Object category;
        @SerializedName("mdm_display_status")
        @Expose
        private Object mdmDisplayStatus;
        @SerializedName("status_bar_status")
        @Expose
        private Object statusBarStatus;
        @SerializedName("home_key_status")
        @Expose
        private Object homeKeyStatus;
        @SerializedName("back_key_status")
        @Expose
        private Object backKeyStatus;
        @SerializedName("recents_key_status")
        @Expose
        private Object recentsKeyStatus;
        @SerializedName("power_key_status")
        @Expose
        private Object powerKeyStatus;
        @SerializedName("firmware_upgrade_status")
        @Expose
        private Object firmwareUpgradeStatus;
        @SerializedName("factory_reset_status")
        @Expose
        private Object factoryResetStatus;
        @SerializedName("s_voice_status")
        @Expose
        private Object sVoiceStatus;
        @SerializedName("android_settings_status")
        @Expose
        private Object androidSettingsStatus;
        @SerializedName("volume_key_status")
        @Expose
        private Object volumeKeyStatus;
        @SerializedName("multi_window_status")
        @Expose
        private Object multiWindowStatus;
        @SerializedName("notifications_status")
        @Expose
        private Object notificationsStatus;
        @SerializedName("lock_screen_status")
        @Expose
        private Object lockScreenStatus;
        @SerializedName("bluetooth_status")
        @Expose
        private Object bluetoothStatus;
        @SerializedName("wifi_status")
        @Expose
        private Object wifiStatus;
        @SerializedName("gps_status")
        @Expose
        private Object gpsStatus;
        @SerializedName("device_timeout")
        @Expose
        private Object deviceTimeout;
        @SerializedName("pull_interval")
        @Expose
        private Object pullInterval;
        @SerializedName("brightness_percentage")
        @Expose
        private Object brightnessPercentage;
        @SerializedName("volume_percentage")
        @Expose
        private Object volumePercentage;
        @SerializedName("sleep_percentage")
        @Expose
        private Object sleepPercentage;
        @SerializedName("deep_sleep_percentage")
        @Expose
        private Object deepSleepPercentage;
        @SerializedName("orientation")
        @Expose
        private Object orientation;
        @SerializedName("backup_reset_menu_status")
        @Expose
        private Object backupResetMenuStatus;
        @SerializedName("developer_mode_menu_status")
        @Expose
        private Object developerModeMenuStatus;
        @SerializedName("airplane_mode_menu_status")
        @Expose
        private Object airplaneModeMenuStatus;
        @SerializedName("language_menu_status")
        @Expose
        private Object languageMenuStatus;
        @SerializedName("multi_window_menu_status")
        @Expose
        private Object multiWindowMenuStatus;
        @SerializedName("users_settings_menu_status")
        @Expose
        private Object usersSettingsMenuStatus;
        @SerializedName("bluetooth_settings_menu_status")
        @Expose
        private Object bluetoothSettingsMenuStatus;
        @SerializedName("lockscreen_menu_status")
        @Expose
        private Object lockscreenMenuStatus;
        @SerializedName("wifi_settings_menu_status")
        @Expose
        private Object wifiSettingsMenuStatus;
        @SerializedName("power_on_boot_status")
        @Expose
        private Object powerOnBootStatus;
        @SerializedName("disable_all_bloatware_status")
        @Expose
        private Object disableAllBloatwareStatus;
        @SerializedName("unknown_source_status")
        @Expose
        private Object unknownSourceStatus;
        @SerializedName("developer_mode_status")
        @Expose
        private Object developerModeStatus;
        @SerializedName("screenshot_status")
        @Expose
        private Object screenshotStatus;
        @SerializedName("usb_debugging_status")
        @Expose
        private Object usbDebuggingStatus;
        @SerializedName("lte_mode_status")
        @Expose
        private Object lteModeStatus;
        @SerializedName("browser_heading_kiosk")
        @Expose
        private Object browserHeadingKiosk;
        @SerializedName("full_screen_mode_status")
        @Expose
        private Object fullScreenModeStatus;
        @SerializedName("clear_cache_browser_status")
        @Expose
        private Object clearCacheBrowserStatus;
        @SerializedName("hyperlink_play_sound_status")
        @Expose
        private Object hyperlinkPlaySoundStatus;
        @SerializedName("page_zoom_status")
        @Expose
        private Object pageZoomStatus;
        @SerializedName("clear_cookies_status")
        @Expose
        private Object clearCookiesStatus;
        @SerializedName("auto_refresh_webpage_status")
        @Expose
        private Object autoRefreshWebpageStatus;
        @SerializedName("application_password")
        @Expose
        private Object applicationPassword;
        @SerializedName("placeholder1")
        @Expose
        private Object placeholder1;
        @SerializedName("placeholder2")
        @Expose
        private Object placeholder2;
        @SerializedName("placeholder3")
        @Expose
        private Object placeholder3;
        @SerializedName("global_proxy")
        @Expose
        private Object globalProxy;
        @SerializedName("global_port")
        @Expose
        private Object globalPort;
        @SerializedName("kiosk_flag")
        @Expose
        private Object kioskFlag;
        @SerializedName("kiosk_type")
        @Expose
        private Object kioskType;
        @SerializedName("kiosk_password")
        @Expose
        private Object kioskPassword;
        @SerializedName("single_app_kiosk")
        @Expose
        private Object singleAppKiosk;
        @SerializedName("browser_kiosk")
        @Expose
        private Object browserKiosk;
        @SerializedName("registration_password")
        @Expose
        private Object registrationPassword;
        @SerializedName("advanced_command")
        @Expose
        private Object advancedCommand;
        @SerializedName("profile_type")
        @Expose
        private Integer profileType;
        @SerializedName("default_profile_status")
        @Expose
        private Integer defaultProfileStatus;
        @SerializedName("profile_status")
        @Expose
        private Integer profileStatus;
        @SerializedName("json_string")
        @Expose
        private Object jsonString;
        @SerializedName("disable_script")
        @Expose
        private Object disableScript;
        @SerializedName("profile_counter")
        @Expose
        private Integer profileCounter;
        @SerializedName("created_by")
        @Expose
        private Integer createdBy;
        @SerializedName("created_at")
        @Expose
        private Object createdAt;
        @SerializedName("updated_at")
        @Expose
        private Object updatedAt;
        @SerializedName("esa")
        @Expose
        private Object esa;
        @SerializedName("profile_dsc")
        @Expose
        private Object profileDsc;
        @SerializedName("profile_rppm")
        @Expose
        private Object profileRppm;
        @SerializedName("profile_rpgs")
        @Expose
        private Object profileRpgs;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getProfileName() {
            return profileName;
        }

        public void setProfileName(String profileName) {
            this.profileName = profileName;
        }

        public String getProfileId() {
            return profileId;
        }

        public void setProfileId(String profileId) {
            this.profileId = profileId;
        }

        public Object getParentProfileId() {
            return parentProfileId;
        }

        public void setParentProfileId(Object parentProfileId) {
            this.parentProfileId = parentProfileId;
        }

        public Object getRegion() {
            return region;
        }

        public void setRegion(Object region) {
            this.region = region;
        }

        public Object getCountry() {
            return country;
        }

        public void setCountry(Object country) {
            this.country = country;
        }

        public Object getCity() {
            return city;
        }

        public void setCity(Object city) {
            this.city = city;
        }

        public Object getCompany() {
            return company;
        }

        public void setCompany(Object company) {
            this.company = company;
        }

        public Object getProduct() {
            return product;
        }

        public void setProduct(Object product) {
            this.product = product;
        }

        public Object getSegment() {
            return segment;
        }

        public void setSegment(Object segment) {
            this.segment = segment;
        }

        public Object getCategory() {
            return category;
        }

        public void setCategory(Object category) {
            this.category = category;
        }

        public Object getMdmDisplayStatus() {
            return mdmDisplayStatus;
        }

        public void setMdmDisplayStatus(Object mdmDisplayStatus) {
            this.mdmDisplayStatus = mdmDisplayStatus;
        }

        public Object getStatusBarStatus() {
            return statusBarStatus;
        }

        public void setStatusBarStatus(Object statusBarStatus) {
            this.statusBarStatus = statusBarStatus;
        }

        public Object getHomeKeyStatus() {
            return homeKeyStatus;
        }

        public void setHomeKeyStatus(Object homeKeyStatus) {
            this.homeKeyStatus = homeKeyStatus;
        }

        public Object getBackKeyStatus() {
            return backKeyStatus;
        }

        public void setBackKeyStatus(Object backKeyStatus) {
            this.backKeyStatus = backKeyStatus;
        }

        public Object getRecentsKeyStatus() {
            return recentsKeyStatus;
        }

        public void setRecentsKeyStatus(Object recentsKeyStatus) {
            this.recentsKeyStatus = recentsKeyStatus;
        }

        public Object getPowerKeyStatus() {
            return powerKeyStatus;
        }

        public void setPowerKeyStatus(Object powerKeyStatus) {
            this.powerKeyStatus = powerKeyStatus;
        }

        public Object getFirmwareUpgradeStatus() {
            return firmwareUpgradeStatus;
        }

        public void setFirmwareUpgradeStatus(Object firmwareUpgradeStatus) {
            this.firmwareUpgradeStatus = firmwareUpgradeStatus;
        }

        public Object getFactoryResetStatus() {
            return factoryResetStatus;
        }

        public void setFactoryResetStatus(Object factoryResetStatus) {
            this.factoryResetStatus = factoryResetStatus;
        }

        public Object getSVoiceStatus() {
            return sVoiceStatus;
        }

        public void setSVoiceStatus(Object sVoiceStatus) {
            this.sVoiceStatus = sVoiceStatus;
        }

        public Object getAndroidSettingsStatus() {
            return androidSettingsStatus;
        }

        public void setAndroidSettingsStatus(Object androidSettingsStatus) {
            this.androidSettingsStatus = androidSettingsStatus;
        }

        public Object getVolumeKeyStatus() {
            return volumeKeyStatus;
        }

        public void setVolumeKeyStatus(Object volumeKeyStatus) {
            this.volumeKeyStatus = volumeKeyStatus;
        }

        public Object getMultiWindowStatus() {
            return multiWindowStatus;
        }

        public void setMultiWindowStatus(Object multiWindowStatus) {
            this.multiWindowStatus = multiWindowStatus;
        }

        public Object getNotificationsStatus() {
            return notificationsStatus;
        }

        public void setNotificationsStatus(Object notificationsStatus) {
            this.notificationsStatus = notificationsStatus;
        }

        public Object getLockScreenStatus() {
            return lockScreenStatus;
        }

        public void setLockScreenStatus(Object lockScreenStatus) {
            this.lockScreenStatus = lockScreenStatus;
        }

        public Object getBluetoothStatus() {
            return bluetoothStatus;
        }

        public void setBluetoothStatus(Object bluetoothStatus) {
            this.bluetoothStatus = bluetoothStatus;
        }

        public Object getWifiStatus() {
            return wifiStatus;
        }

        public void setWifiStatus(Object wifiStatus) {
            this.wifiStatus = wifiStatus;
        }

        public Object getGpsStatus() {
            return gpsStatus;
        }

        public void setGpsStatus(Object gpsStatus) {
            this.gpsStatus = gpsStatus;
        }

        public Object getDeviceTimeout() {
            return deviceTimeout;
        }

        public void setDeviceTimeout(Object deviceTimeout) {
            this.deviceTimeout = deviceTimeout;
        }

        public Object getPullInterval() {
            return pullInterval;
        }

        public void setPullInterval(Object pullInterval) {
            this.pullInterval = pullInterval;
        }

        public Object getBrightnessPercentage() {
            return brightnessPercentage;
        }

        public void setBrightnessPercentage(Object brightnessPercentage) {
            this.brightnessPercentage = brightnessPercentage;
        }

        public Object getVolumePercentage() {
            return volumePercentage;
        }

        public void setVolumePercentage(Object volumePercentage) {
            this.volumePercentage = volumePercentage;
        }

        public Object getSleepPercentage() {
            return sleepPercentage;
        }

        public void setSleepPercentage(Object sleepPercentage) {
            this.sleepPercentage = sleepPercentage;
        }

        public Object getDeepSleepPercentage() {
            return deepSleepPercentage;
        }

        public void setDeepSleepPercentage(Object deepSleepPercentage) {
            this.deepSleepPercentage = deepSleepPercentage;
        }

        public Object getOrientation() {
            return orientation;
        }

        public void setOrientation(Object orientation) {
            this.orientation = orientation;
        }

        public Object getBackupResetMenuStatus() {
            return backupResetMenuStatus;
        }

        public void setBackupResetMenuStatus(Object backupResetMenuStatus) {
            this.backupResetMenuStatus = backupResetMenuStatus;
        }

        public Object getDeveloperModeMenuStatus() {
            return developerModeMenuStatus;
        }

        public void setDeveloperModeMenuStatus(Object developerModeMenuStatus) {
            this.developerModeMenuStatus = developerModeMenuStatus;
        }

        public Object getAirplaneModeMenuStatus() {
            return airplaneModeMenuStatus;
        }

        public void setAirplaneModeMenuStatus(Object airplaneModeMenuStatus) {
            this.airplaneModeMenuStatus = airplaneModeMenuStatus;
        }

        public Object getLanguageMenuStatus() {
            return languageMenuStatus;
        }

        public void setLanguageMenuStatus(Object languageMenuStatus) {
            this.languageMenuStatus = languageMenuStatus;
        }

        public Object getMultiWindowMenuStatus() {
            return multiWindowMenuStatus;
        }

        public void setMultiWindowMenuStatus(Object multiWindowMenuStatus) {
            this.multiWindowMenuStatus = multiWindowMenuStatus;
        }

        public Object getUsersSettingsMenuStatus() {
            return usersSettingsMenuStatus;
        }

        public void setUsersSettingsMenuStatus(Object usersSettingsMenuStatus) {
            this.usersSettingsMenuStatus = usersSettingsMenuStatus;
        }

        public Object getBluetoothSettingsMenuStatus() {
            return bluetoothSettingsMenuStatus;
        }

        public void setBluetoothSettingsMenuStatus(Object bluetoothSettingsMenuStatus) {
            this.bluetoothSettingsMenuStatus = bluetoothSettingsMenuStatus;
        }

        public Object getLockscreenMenuStatus() {
            return lockscreenMenuStatus;
        }

        public void setLockscreenMenuStatus(Object lockscreenMenuStatus) {
            this.lockscreenMenuStatus = lockscreenMenuStatus;
        }

        public Object getWifiSettingsMenuStatus() {
            return wifiSettingsMenuStatus;
        }

        public void setWifiSettingsMenuStatus(Object wifiSettingsMenuStatus) {
            this.wifiSettingsMenuStatus = wifiSettingsMenuStatus;
        }

        public Object getPowerOnBootStatus() {
            return powerOnBootStatus;
        }

        public void setPowerOnBootStatus(Object powerOnBootStatus) {
            this.powerOnBootStatus = powerOnBootStatus;
        }

        public Object getDisableAllBloatwareStatus() {
            return disableAllBloatwareStatus;
        }

        public void setDisableAllBloatwareStatus(Object disableAllBloatwareStatus) {
            this.disableAllBloatwareStatus = disableAllBloatwareStatus;
        }

        public Object getUnknownSourceStatus() {
            return unknownSourceStatus;
        }

        public void setUnknownSourceStatus(Object unknownSourceStatus) {
            this.unknownSourceStatus = unknownSourceStatus;
        }

        public Object getDeveloperModeStatus() {
            return developerModeStatus;
        }

        public void setDeveloperModeStatus(Object developerModeStatus) {
            this.developerModeStatus = developerModeStatus;
        }

        public Object getScreenshotStatus() {
            return screenshotStatus;
        }

        public void setScreenshotStatus(Object screenshotStatus) {
            this.screenshotStatus = screenshotStatus;
        }

        public Object getUsbDebuggingStatus() {
            return usbDebuggingStatus;
        }

        public void setUsbDebuggingStatus(Object usbDebuggingStatus) {
            this.usbDebuggingStatus = usbDebuggingStatus;
        }

        public Object getLteModeStatus() {
            return lteModeStatus;
        }

        public void setLteModeStatus(Object lteModeStatus) {
            this.lteModeStatus = lteModeStatus;
        }

        public Object getBrowserHeadingKiosk() {
            return browserHeadingKiosk;
        }

        public void setBrowserHeadingKiosk(Object browserHeadingKiosk) {
            this.browserHeadingKiosk = browserHeadingKiosk;
        }

        public Object getFullScreenModeStatus() {
            return fullScreenModeStatus;
        }

        public void setFullScreenModeStatus(Object fullScreenModeStatus) {
            this.fullScreenModeStatus = fullScreenModeStatus;
        }

        public Object getClearCacheBrowserStatus() {
            return clearCacheBrowserStatus;
        }

        public void setClearCacheBrowserStatus(Object clearCacheBrowserStatus) {
            this.clearCacheBrowserStatus = clearCacheBrowserStatus;
        }

        public Object getHyperlinkPlaySoundStatus() {
            return hyperlinkPlaySoundStatus;
        }

        public void setHyperlinkPlaySoundStatus(Object hyperlinkPlaySoundStatus) {
            this.hyperlinkPlaySoundStatus = hyperlinkPlaySoundStatus;
        }

        public Object getPageZoomStatus() {
            return pageZoomStatus;
        }

        public void setPageZoomStatus(Object pageZoomStatus) {
            this.pageZoomStatus = pageZoomStatus;
        }

        public Object getClearCookiesStatus() {
            return clearCookiesStatus;
        }

        public void setClearCookiesStatus(Object clearCookiesStatus) {
            this.clearCookiesStatus = clearCookiesStatus;
        }

        public Object getAutoRefreshWebpageStatus() {
            return autoRefreshWebpageStatus;
        }

        public void setAutoRefreshWebpageStatus(Object autoRefreshWebpageStatus) {
            this.autoRefreshWebpageStatus = autoRefreshWebpageStatus;
        }

        public Object getApplicationPassword() {
            return applicationPassword;
        }

        public void setApplicationPassword(Object applicationPassword) {
            this.applicationPassword = applicationPassword;
        }

        public Object getPlaceholder1() {
            return placeholder1;
        }

        public void setPlaceholder1(Object placeholder1) {
            this.placeholder1 = placeholder1;
        }

        public Object getPlaceholder2() {
            return placeholder2;
        }

        public void setPlaceholder2(Object placeholder2) {
            this.placeholder2 = placeholder2;
        }

        public Object getPlaceholder3() {
            return placeholder3;
        }

        public void setPlaceholder3(Object placeholder3) {
            this.placeholder3 = placeholder3;
        }

        public Object getGlobalProxy() {
            return globalProxy;
        }

        public void setGlobalProxy(Object globalProxy) {
            this.globalProxy = globalProxy;
        }

        public Object getGlobalPort() {
            return globalPort;
        }

        public void setGlobalPort(Object globalPort) {
            this.globalPort = globalPort;
        }

        public Object getKioskFlag() {
            return kioskFlag;
        }

        public void setKioskFlag(Object kioskFlag) {
            this.kioskFlag = kioskFlag;
        }

        public Object getKioskType() {
            return kioskType;
        }

        public void setKioskType(Object kioskType) {
            this.kioskType = kioskType;
        }

        public Object getKioskPassword() {
            return kioskPassword;
        }

        public void setKioskPassword(Object kioskPassword) {
            this.kioskPassword = kioskPassword;
        }

        public Object getSingleAppKiosk() {
            return singleAppKiosk;
        }

        public void setSingleAppKiosk(Object singleAppKiosk) {
            this.singleAppKiosk = singleAppKiosk;
        }

        public Object getBrowserKiosk() {
            return browserKiosk;
        }

        public void setBrowserKiosk(Object browserKiosk) {
            this.browserKiosk = browserKiosk;
        }

        public Object getRegistrationPassword() {
            return registrationPassword;
        }

        public void setRegistrationPassword(Object registrationPassword) {
            this.registrationPassword = registrationPassword;
        }

        public Object getAdvancedCommand() {
            return advancedCommand;
        }

        public void setAdvancedCommand(Object advancedCommand) {
            this.advancedCommand = advancedCommand;
        }

        public Integer getProfileType() {
            return profileType;
        }

        public void setProfileType(Integer profileType) {
            this.profileType = profileType;
        }

        public Integer getDefaultProfileStatus() {
            return defaultProfileStatus;
        }

        public void setDefaultProfileStatus(Integer defaultProfileStatus) {
            this.defaultProfileStatus = defaultProfileStatus;
        }

        public Integer getProfileStatus() {
            return profileStatus;
        }

        public void setProfileStatus(Integer profileStatus) {
            this.profileStatus = profileStatus;
        }

        public Object getJsonString() {
            return jsonString;
        }

        public void setJsonString(Object jsonString) {
            this.jsonString = jsonString;
        }

        public Object getDisableScript() {
            return disableScript;
        }

        public void setDisableScript(Object disableScript) {
            this.disableScript = disableScript;
        }

        public Integer getProfileCounter() {
            return profileCounter;
        }

        public void setProfileCounter(Integer profileCounter) {
            this.profileCounter = profileCounter;
        }

        public Integer getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(Integer createdBy) {
            this.createdBy = createdBy;
        }

        public Object getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Object createdAt) {
            this.createdAt = createdAt;
        }

        public Object getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(Object updatedAt) {
            this.updatedAt = updatedAt;
        }

        public Object getEsa() {
            return esa;
        }

        public void setEsa(Object esa) {
            this.esa = esa;
        }

        public Object getProfileDsc() {
            return profileDsc;
        }

        public void setProfileDsc(Object profileDsc) {
            this.profileDsc = profileDsc;
        }

        public Object getProfileRppm() {
            return profileRppm;
        }

        public void setProfileRppm(Object profileRppm) {
            this.profileRppm = profileRppm;
        }

        public Object getProfileRpgs() {
            return profileRpgs;
        }

        public void setProfileRpgs(Object profileRpgs) {
            this.profileRpgs = profileRpgs;
        }
    }

}
