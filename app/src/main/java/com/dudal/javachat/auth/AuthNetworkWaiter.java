package com.dudal.javachat.auth;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

final class AuthNetworkWaiter {
    private AuthNetworkWaiter() {
    }

    static boolean awaitValidated(Context context, long timeoutMs) throws InterruptedException {
        ConnectivityManager connectivity =
                context.getSystemService(ConnectivityManager.class);
        if (connectivity == null) {
            return false;
        }
        if (isValidated(connectivity, connectivity.getActiveNetwork())) {
            return true;
        }

        CountDownLatch ready = new CountDownLatch(1);
        ConnectivityManager.NetworkCallback callback =
                new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        if (isValidated(connectivity, network)) {
                            ready.countDown();
                        }
                    }

                    @Override
                    public void onCapabilitiesChanged(Network network,
                                                      NetworkCapabilities capabilities) {
                        if (hasValidatedInternet(capabilities)) {
                            ready.countDown();
                        }
                    }
                };

        boolean registered = false;
        try {
            connectivity.registerDefaultNetworkCallback(callback);
            registered = true;
            if (isValidated(connectivity, connectivity.getActiveNetwork())) {
                return true;
            }
            return ready.await(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (RuntimeException ignored) {
            return false;
        } finally {
            if (registered) {
                try {
                    connectivity.unregisterNetworkCallback(callback);
                } catch (RuntimeException ignored) {
                    // The callback may already have been removed during a network transition.
                }
            }
        }
    }

    private static boolean isValidated(ConnectivityManager connectivity, Network network) {
        return network != null
                && hasValidatedInternet(connectivity.getNetworkCapabilities(network));
    }

    private static boolean hasValidatedInternet(NetworkCapabilities capabilities) {
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }
}
