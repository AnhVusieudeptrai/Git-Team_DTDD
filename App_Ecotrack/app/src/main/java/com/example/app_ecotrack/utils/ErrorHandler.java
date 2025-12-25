package com.example.app_ecotrack.utils;

import android.content.Context;
import android.view.View;

import com.example.app_ecotrack.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.HttpException;

/**
 * ErrorHandler - Utility class for handling API errors and displaying user-friendly messages
 */
public class ErrorHandler {

    /**
     * Get user-friendly error message from throwable
     */
    public static String getErrorMessage(Context context, Throwable error) {
        if (error == null) {
            return context.getString(R.string.error_unknown);
        }

        if (error instanceof HttpException) {
            return handleHttpError(context, (HttpException) error);
        } else if (error instanceof SocketTimeoutException) {
            return context.getString(R.string.error_timeout);
        } else if (error instanceof UnknownHostException) {
            return context.getString(R.string.error_no_internet);
        } else if (error instanceof IOException) {
            return context.getString(R.string.error_network);
        }

        return context.getString(R.string.error_unknown);
    }

    /**
     * Handle HTTP errors based on status code
     */
    private static String handleHttpError(Context context, HttpException error) {
        int code = error.code();
        switch (code) {
            case 400:
                return context.getString(R.string.error_unknown);
            case 401:
                return context.getString(R.string.error_session_expired);
            case 403:
                return context.getString(R.string.error_unknown);
            case 404:
                return context.getString(R.string.no_data);
            case 500:
            case 502:
            case 503:
                return context.getString(R.string.error_server);
            default:
                return context.getString(R.string.error_unknown);
        }
    }

    /**
     * Show error snackbar with retry action
     */
    public static void showErrorSnackbar(View view, String message, View.OnClickListener retryAction) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        if (retryAction != null) {
            snackbar.setAction(R.string.retry, retryAction);
        }
        snackbar.show();
    }

    /**
     * Show error snackbar without retry action
     */
    public static void showErrorSnackbar(View view, String message) {
        showErrorSnackbar(view, message, null);
    }

    /**
     * Show success snackbar
     */
    public static void showSuccessSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Check if error is network related
     */
    public static boolean isNetworkError(Throwable error) {
        return error instanceof IOException ||
               error instanceof SocketTimeoutException ||
               error instanceof UnknownHostException;
    }

    /**
     * Check if error is authentication related
     */
    public static boolean isAuthError(Throwable error) {
        if (error instanceof HttpException) {
            int code = ((HttpException) error).code();
            return code == 401 || code == 403;
        }
        return false;
    }
}
