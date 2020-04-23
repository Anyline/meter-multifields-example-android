package at.nineyards.anyline.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.barcode.BarcodeScanPlugin;
import io.anyline.plugin.barcode.BarcodeScanResult;
import io.anyline.plugin.barcode.BarcodeScanViewPlugin;
import io.anyline.view.ScanView;


public class ScanBarcodeActivity extends Activity {

    private ScanView scanView = null;

    private void initScanView() {
        scanView = findViewById(R.id.scan_view);

        scanView.setScanConfig("barcode_view_config.json");
        BarcodeScanPlugin scanPlugin = new BarcodeScanPlugin(getApplicationContext(), "barcode", getString(R.string.anyline_license_key));
        BarcodeScanViewPlugin scanViewPlugin = new BarcodeScanViewPlugin(getApplicationContext(), scanPlugin, scanView.getScanViewPluginConfig());
        scanView.setScanViewPlugin(scanViewPlugin);

        scanViewPlugin.addScanResultListener(new ScanResultListener<BarcodeScanResult>() {
            @Override
            public void onResult(BarcodeScanResult result) {
                Intent intent = new Intent(ScanBarcodeActivity.this, ScanMeterActivity.class);
                Bundle b = new Bundle();
                b.putString(getString(R.string.barcode), result.getResult());
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
        });
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);
        initScanView();
        showToast(ScanBarcodeActivity.this, getString(R.string.scanBarcode));
    }


    @Override
    protected void onResume() {
        super.onResume();
            scanView.start();
    }


    @Override
    protected void onPause() {
        super.onPause();
            scanView.stop();
            scanView.releaseCameraInBackground();
    }


    public static void showToast(Context context, String message) {
        Toast toast = Toast.makeText(context, Html.fromHtml(message), Toast.LENGTH_LONG);
        View view = toast.getView();
        view.setBackgroundResource(R.drawable.background);
        GradientDrawable background = (GradientDrawable) view.getBackground().getCurrent();
        background.setColor(context.getResources().getColor(R.color.colorAnylineBlueLight));
        TextView textView = view.findViewById(android.R.id.message);
        textView.setTextColor(context.getResources().getColor(R.color.colorAlmostWhite));
        toast.setGravity(Gravity.CENTER, 0, 100);
        toast.show();
    }

}
