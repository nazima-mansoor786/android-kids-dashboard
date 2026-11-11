package com.tabnova.aidashboard.Extra;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.CancellationSignal;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import com.google.android.gms.auth.api.identity.AuthorizationRequest;
import com.google.android.gms.auth.api.identity.AuthorizationResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException;
import com.tabnova.aidashboard.R;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Google sign-in (Credential Manager + Sign in with Google) and OAuth access-token retrieval
 * (AuthorizationClient), replacing the deprecated GoogleSignIn / One Tap / GoogleAuthUtil APIs.
 * <p>
 * All callbacks are delivered on the main thread. Never logs tokens or emails.
 * Callers should check {@code isFinishing()} before touching UI in callbacks.
 */
public final class GoogleAuthManager {

    /** Result of {@link #signIn(Activity, SignInCallback)}. */
    public interface SignInCallback {
        /** @param email the Google account email (GoogleIdTokenCredential.getId()). */
        void onSuccess(@NonNull String email, @NonNull String idToken);

        /** User dismissed the Sign in with Google UI. */
        void onCancelled();

        /** No Google account is available on the device. */
        void onNoAccount();

        void onError(@NonNull String message);
    }

    /** Result of {@link #getAccessToken(ComponentActivity, ConsentLauncher, String, AccessTokenCallback)}. */
    public interface AccessTokenCallback {
        void onSuccess(@NonNull String accessToken);

        /** User declined or dismissed the consent screen. */
        void onCancelled();

        void onError(@NonNull String message);
    }

    /** Same scopes as the former GoogleAuthUtil.getToken(..., "oauth2:profile email"). */
    private static final List<Scope> SCOPES = Arrays.asList(new Scope("profile"), new Scope("email"));

    private GoogleAuthManager() {
    }

    /**
     * Launches Sign in with Google (button flow) and returns the account email and ID token.
     */
    public static void signIn(@NonNull Activity activity, @NonNull SignInCallback callback) {
        GetSignInWithGoogleOption option =
                new GetSignInWithGoogleOption.Builder(activity.getString(R.string.default_web_client_id)).build();
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build();

        CredentialManager.create(activity).getCredentialAsync(
                activity,
                request,
                new CancellationSignal(),
                mainExecutor(activity),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse response) {
                        handleCredential(response.getCredential(), callback);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        if (e instanceof GetCredentialCancellationException) {
                            callback.onCancelled();
                        } else if (e instanceof NoCredentialException) {
                            callback.onNoAccount();
                        } else {
                            callback.onError("Sign-in failed: " + e.getType());
                        }
                    }
                });
    }

    private static void handleCredential(@NonNull Credential credential, @NonNull SignInCallback callback) {
        if (credential instanceof CustomCredential
                && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
            try {
                GoogleIdTokenCredential googleCredential = GoogleIdTokenCredential.createFrom(credential.getData());
                String email = googleCredential.getId();
                String idToken = googleCredential.getIdToken();
                if (email.isEmpty() || idToken.isEmpty()) {
                    callback.onError("Sign-in returned an incomplete credential");
                    return;
                }
                callback.onSuccess(email, idToken);
            } catch (GoogleIdTokenParsingException e) {
                callback.onError("Could not read the Google credential");
            }
        } else {
            callback.onError("Unexpected credential type");
        }
    }

    /**
     * Registers the consent-screen launcher. Must be created before the Activity is STARTED,
     * i.e. in onCreate (typically as a field initialiser or in onCreate itself).
     */
    public static final class ConsentLauncher {
        private final WeakReference<ComponentActivity> activityRef;
        private final ActivityResultLauncher<IntentSenderRequest> launcher;
        @Nullable
        private AccessTokenCallback pendingCallback;

        public ConsentLauncher(@NonNull ComponentActivity activity) {
            this.activityRef = new WeakReference<>(activity);
            this.launcher = activity.registerForActivityResult(
                    new ActivityResultContracts.StartIntentSenderForResult(),
                    result -> {
                        AccessTokenCallback callback = pendingCallback;
                        pendingCallback = null;
                        if (callback == null) {
                            return;
                        }
                        ComponentActivity target = activityRef.get();
                        Intent data = result.getData();
                        if (result.getResultCode() != Activity.RESULT_OK || data == null || target == null) {
                            callback.onCancelled();
                            return;
                        }
                        try {
                            AuthorizationResult authorizationResult =
                                    Identity.getAuthorizationClient(target).getAuthorizationResultFromIntent(data);
                            deliverAccessToken(authorizationResult, callback);
                        } catch (ApiException e) {
                            callback.onError("Authorization failed: " + e.getStatusCode());
                        }
                    });
        }

        void launch(@NonNull IntentSenderRequest request, @NonNull AccessTokenCallback callback) {
            pendingCallback = callback;
            launcher.launch(request);
        }
    }

    /**
     * Requests an OAuth access token for the profile and email scopes (backend field
     * google_access_token). Shows the consent screen through {@code consentLauncher} if needed.
     *
     * @param email the signed-in account email. Currently unused: the documented API has no
     *              account setter on AuthorizationRequest; after Credential Manager sign-in the
     *              selected account is used by default, so the user is not asked to pick again.
     */
    public static void getAccessToken(@NonNull ComponentActivity activity,
                                      @NonNull ConsentLauncher consentLauncher,
                                      @NonNull String email,
                                      @NonNull AccessTokenCallback callback) {
        AuthorizationRequest request = AuthorizationRequest.builder()
                .setRequestedScopes(SCOPES)
                .build();

        Identity.getAuthorizationClient(activity)
                .authorize(request)
                .addOnSuccessListener(activity, authorizationResult -> {
                    if (authorizationResult.hasResolution()) {
                        if (authorizationResult.getPendingIntent() == null) {
                            callback.onError("Authorization needs consent but no screen was provided");
                            return;
                        }
                        IntentSenderRequest senderRequest = new IntentSenderRequest.Builder(
                                authorizationResult.getPendingIntent().getIntentSender()).build();
                        consentLauncher.launch(senderRequest, callback);
                    } else {
                        deliverAccessToken(authorizationResult, callback);
                    }
                })
                .addOnFailureListener(activity, e -> callback.onError("Authorization failed"));
    }

    private static void deliverAccessToken(@NonNull AuthorizationResult result, @NonNull AccessTokenCallback callback) {
        String accessToken = result.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            callback.onError("No access token returned");
        } else {
            callback.onSuccess(accessToken);
        }
    }

    /**
     * Clears the Credential Manager sign-in state (replaces GoogleSignInClient.signOut()).
     * {@code onDone} runs on the main thread whether clearing succeeds or fails.
     */
    public static void signOut(@NonNull Context context, @Nullable Runnable onDone) {
        CredentialManager.create(context).clearCredentialStateAsync(
                new ClearCredentialStateRequest(),
                new CancellationSignal(),
                mainExecutor(context),
                new CredentialManagerCallback<Void, ClearCredentialException>() {
                    @Override
                    public void onResult(Void result) {
                        if (onDone != null) {
                            onDone.run();
                        }
                    }

                    @Override
                    public void onError(@NonNull ClearCredentialException e) {
                        if (onDone != null) {
                            onDone.run();
                        }
                    }
                });
    }

    private static Executor mainExecutor(@NonNull Context context) {
        return ContextCompat.getMainExecutor(context);
    }
}
