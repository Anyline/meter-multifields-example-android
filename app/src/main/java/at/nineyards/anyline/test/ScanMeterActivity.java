package at.nineyards.anyline.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.meter.MeterScanMode;
import io.anyline.plugin.meter.MeterScanViewPlugin;
import io.anyline.plugin.meter.MultiMeterScanResult;
import io.anyline.view.ScanView;


public class ScanMeterActivity extends Activity {

    private static final int TIMEOUT_AFTER_MILLISECONDS = 20000;    // if no scan result is received within this time scanning will be stopped
    private static final int RESTART_SCAN_AFTER_MILLISECONDS = 100; // after a successful scan the next scan will occur after this period
    private static final Boolean SHOW_SCANS_IN_RESULT = true;       // show list of all scans in the result panel
    private static final List<Integer> VALID_COUNTERS =             // you might have a set of predefined counters. if yes, add them to prevent having wrongly scanned counters in the result list
            Collections.unmodifiableList(Arrays.asList(161, 162, 180, 181));


    public class ScanElement<T, U, V, W> {

        private final T counter;
        private final U value;
        private final V confidence;
        private final W timestamp;

        ScanElement(T counter, U value, V confidence, W timestamp) {
            this.counter = counter;
            this.value = value;
            this.confidence = confidence;
            this.timestamp = timestamp;
        }

        T getCounter() {
            return counter;
        }

        U getValue() {
            return value;
        }

        V getConfidence() {
            return confidence;
        }

        W getTimestamp() {
            return timestamp;
        }
    }


    private ScanView meterScanView;

    private List<ScanElement<Integer, Float, Integer, Long>> scannedElementsList = new ArrayList<>();

    private Button btnStop;                 // manually stop the scan process
    private TextView tvResult;              // shows the result of the scan process (barcode + meter)
    private TextView tvTimer;               // show the decreasing value of the CountDownTimer defined below
    private LinearLayout llResult;
    private ConstraintLayout clScanview;

    CountDownTimer countDownTimer;          // used to stop after a predefined time if no scan result is reported
    private String barCodeResult;           // value of the barcode scanned in ScanBarcodeActivity
    private Boolean scanStoppedByUser;
    private Boolean timeoutReached;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_meter);

        // get scanned Barcode from ScanBarcodeActivity:
        Bundle b = getIntent().getExtras();
        barCodeResult = "";
        if (b != null) {
            barCodeResult = b.getString(getString(R.string.barcode));
        }
        scanStoppedByUser = false;
        timeoutReached = false;

        meterScanView = findViewById(R.id.scan_view);
        tvResult = findViewById(R.id.tvResult);
        tvResult.setMovementMethod(new ScrollingMovementMethod());
        clScanview = findViewById(R.id.clScanview);
        llResult = findViewById(R.id.llResult);
        tvTimer = findViewById(R.id.tvTimer);

        countDownTimer = new CountDownTimer(TIMEOUT_AFTER_MILLISECONDS, 100) {
            public void onTick(long millisUntilFinished) {
                DecimalFormat df = new DecimalFormat("###.#");
                tvTimer.setText(df.format(millisUntilFinished / 1000.0));
            }

            public void onFinish() {
                timeoutReached = true;
                stopScanning();
            }
        };

        btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                scanStoppedByUser = true;
                stopScanning();
            }
        });

        Button btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        Button btnRescan = findViewById(R.id.btnRescan);
        btnRescan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ScanMeterActivity.this, ScanBarcodeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        try {
            meterScanView.init("energy_view_config.json", getString(R.string.anyline_license_key));
        } catch (Exception e) {
            e.printStackTrace();
        }

        MeterScanViewPlugin meterScanViewPlugin = (MeterScanViewPlugin) meterScanView.getScanViewPlugin();
        meterScanViewPlugin.setScanMode(MeterScanMode.DIGITAL_METER_MULTIFIELD);

        meterScanViewPlugin.addScanResultListener(new ScanResultListener<MultiMeterScanResult>() {
            @Override
            public void onResult(MultiMeterScanResult result) {

                restartCountDownTimer();    // once a result is detected time is restarted
                meterScanView.stop();

                int iCounter = 0;
                float fValue = 0f;
                Integer confidence = 0;

                // get the result:
                try {
                    String sCounter = result.getCounter();
                    String sValue = result.getResult();
                    confidence = result.getConfidence();

                    iCounter = Integer.parseInt(sCounter);
                    fValue = Float.parseFloat(sValue);
                } catch (Exception e) {
                    Log.e("ScanMeter", "Exception: " + e);
                }

                // add scanned result to list as long as loop is not completed:
                if (VALID_COUNTERS.contains(iCounter)) {
                    scannedElementsList.add(new ScanElement<>(iCounter, fValue, confidence, System.currentTimeMillis()));
                    if (scannedElementsList.size() >= 2) {
                        if (scannedElementsList.get(scannedElementsList.size() - 2).getCounter() != iCounter && // counter changed
                            iCounter == scannedElementsList.get(0).getCounter()) {                              // reached first counter, loop completed
                            stopScanning();
                            return;
                        }
                    }
                }

                // start scan again after a short delay:
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        meterScanView.start();
                    }
                }, RESTART_SCAN_AFTER_MILLISECONDS);
            }

        });

        ScanBarcodeActivity.showToast (ScanMeterActivity.this, getString(R.string.scanMeter));
    }


    private void restartCountDownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer.start();
        }
    }


    private void startScanning() {
        meterScanView.start();
        restartCountDownTimer();
        btnStop.setVisibility(View.VISIBLE);
    }


    private void sbAppendLine(StringBuilder sb, String text) {
        String s = text + "<br>";
        sb.append(s);
    }

    private void stopScanning() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        meterScanView.stop();
        StringBuilder sb = new StringBuilder();

        String sBlue = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorAnylineBlue) & 0x00ffffff);
        String sRed = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorAnylineRedShadow) & 0x00ffffff);
        String sBlack = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorAnylineBlack90) & 0x00ffffff);

        sbAppendLine(sb, "<big><big><b><font color=" + sBlue + ">" +
                  getString(R.string.result) +
                  "<font color=" + sBlack + ">" +
                  "</b><small><small>");
        if (scanStoppedByUser) {
            sbAppendLine(sb, "<br><b><font color=" + sRed + ">" +
                      getString(R.string.stoppedByUser) +
                      "<font color=" + sBlack + "></b>");
        }
        if (timeoutReached) {
            sbAppendLine(sb, "<br><b><font color=" + sRed + ">" +
                      getString(R.string.timeout) +
                      "<font color=" + sBlack + "></b>");
        }
        sbAppendLine(sb, "<br><b>" + getString(R.string.barcode) + "</b>");
        sbAppendLine(sb, barCodeResult);

        if (SHOW_SCANS_IN_RESULT) {
            sbAppendLine(sb, "<br><b>" + getString(R.string.scans) + "</b>");
            for (int i = 0; i < scannedElementsList.size(); i++) {
                sbAppendLine(sb, scannedElementsList.get(i).getCounter() + "  " +
                          scannedElementsList.get(i).getValue() + "  " +
                          scannedElementsList.get(i).getConfidence() + "  " +
                          scannedElementsList.get(i).getTimestamp());
            }
        }

        Collections.sort(scannedElementsList, new Comparator<ScanElement<Integer, Float, Integer, Long>>() {
            @Override
            public int compare(final ScanElement<Integer, Float, Integer, Long> o1,
                               final ScanElement<Integer, Float, Integer, Long> o2) {
                // first sort by Counter in ascending order:
                if (o1.getCounter() < o2.getCounter()) {
                    return -1;
                } else if (o1.getCounter() > o2.getCounter()) {
                    return 1;
                }

                // if counter is the same: sort by Confidence in descending order:
                if (o1.getConfidence() > o2.getConfidence()) {
                    return -1;
                } else if (o1.getConfidence() < o2.getConfidence()) {
                    return 1;
                }

                // if Confidence is the same: sort by timestamp in descending order:
                if (o1.getTimestamp() > o2.getTimestamp()) {
                    return -1;
                } else if (o1.getTimestamp() < o2.getTimestamp()) {
                    return 1;
                }

                return 0; //should never happen as there are no duplicate timestamps
            }
        });

        sbAppendLine(sb, "<br><b>" + getString(R.string.counterValues) + "</b>");
        int prevCounter = -1;

        for (int i = 0; i < scannedElementsList.size(); i++) {
            if (scannedElementsList.get(i).getCounter() != prevCounter) {
                prevCounter = scannedElementsList.get(i).getCounter();
                sbAppendLine(sb,scannedElementsList.get(i).getCounter() + "  " +
                          scannedElementsList.get(i).getValue());
            }
        }

        clScanview.setVisibility(View.GONE);
        llResult.setVisibility(View.VISIBLE);
        tvResult.setText(Html.fromHtml(sb.toString()));
    }


    @Override
    protected void onResume() {
        super.onResume();
        startScanning();
    }


    @Override
    protected void onPause() {
        super.onPause();
        meterScanView.stop();
    }
}
