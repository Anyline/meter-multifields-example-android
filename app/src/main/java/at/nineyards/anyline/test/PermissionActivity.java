package at.nineyards.anyline.test;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class PermissionActivity extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 100;


    private void startScanning() {
        Intent intent = new Intent(PermissionActivity.this, ScanBarcodeActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        if (checkPermission()) {
            startScanning();
        } else {
            requestPermission();
        }
    }


    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED);
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanning();
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(PermissionActivity.this).create();
            alertDialog.setTitle(getString(R.string.permissionDenied));
            alertDialog.setMessage(getString(R.string.appWillBeClosed));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok),
                                  new DialogInterface.OnClickListener() {
                                      public void onClick(DialogInterface dialog, int which) {
                                          dialog.dismiss();
                                          finish();
                                      }
                                  });
            alertDialog.show();
        }
    }

}
