package com.example.testdrive4;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class GPSTracker implements LocationListener {
    Context context;

    public GPSTracker(Context c) {
        context = c;
    }

    public Location getLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Разрешение не предоставлено", Toast.LENGTH_LONG).show();
            return null;
        }

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

//        if (isGPSEnabled) {
//            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6000, 10, this);
//            Location l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//
//            if (l != null) {
//                Toast.makeText(context, "Получено местоположение", Toast.LENGTH_SHORT).show();
//                return l;
//            } else {
//                Toast.makeText(context, "Ожидание GPS-сигнала", Toast.LENGTH_LONG).show();
//            }
//        } else {
//            Toast.makeText(context, "Пожалуйста, включите GPS!", Toast.LENGTH_LONG).show();
//        }
        boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isNetworkEnabled) {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 6000, 10, this);
            Location l = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (l != null) {
                return l;
            }
        }
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        // Здесь вы можете обработать изменение местоположения, если требуется
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Метод устарел, но его все еще нужно переопределять
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(context, "Поставщик включен: " + provider, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(context, "Поставщик отключен: " + provider, Toast.LENGTH_SHORT).show();
    }
}

