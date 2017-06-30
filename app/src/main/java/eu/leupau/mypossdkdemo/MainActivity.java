package eu.leupau.mypossdkdemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import eu.leupau.icardpossdk.BluetoothDevicesDialog;
import eu.leupau.icardpossdk.ConnectionListener;
import eu.leupau.icardpossdk.Currency;
import eu.leupau.icardpossdk.POSHandler;
import eu.leupau.icardpossdk.TransactionData;

public class MainActivity extends Activity implements View.OnClickListener{
    private static final int    REQUEST_CODE_MAKE_PAYMENT  = 1;
    private static final int    REQUEST_CODE_MAKE_REFUND   = 2;


    private static final int PERMISSION_COARSE_LOCATION     = 1;

    private TextView    mStatus;
    private TextView    mTerminalType;
    private Button      mPurchaseBtn;
    private Button      mRefundBtn;
    private Button      mDeactivateBtn;
    private Button      mActivateBtn;
    private Button      mUpdateBtn;
    private Button      mReprintBtn;

    private POSHandler mPOSHandler;

    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStatus         = (TextView) findViewById(R.id.status);
        mTerminalType   = (TextView) findViewById(R.id.terminal_connection_type);
        mPurchaseBtn    = (Button) findViewById(R.id.purchase_btn);
        mRefundBtn      = (Button) findViewById(R.id.refund_btn);
        mDeactivateBtn  = (Button) findViewById(R.id.deactivate_btn);
        mActivateBtn    = (Button) findViewById(R.id.activate_btn);
        mUpdateBtn      = (Button) findViewById(R.id.update_btn);
        mReprintBtn     = (Button) findViewById(R.id.reprint_receipt_btn);

        mPurchaseBtn.setOnClickListener(this);
        mRefundBtn.setOnClickListener(this);
        mDeactivateBtn.setOnClickListener(this);
        mActivateBtn.setOnClickListener(this);
        mUpdateBtn.setOnClickListener(this);
        mReprintBtn.setOnClickListener(this);
        findViewById(R.id.connect_btn).setOnClickListener(this);

        POSHandler.setCurrency(Currency.EUR);
        mPOSHandler = POSHandler.getInstance();

        setEnabled(false);

        checkPermissions();

        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));

        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }

    @Override
    public void onResume(){
        super.onResume();
        setConnectionListener();
    }

    @Override
    public void onDestroy(){
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void checkPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,  Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_COARSE_LOCATION:
                if(grantResults.length < 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    finish();
                }
                break;
        }
    }

    private void setEnabled(boolean enabled){
        mPurchaseBtn.setEnabled(enabled);
        mRefundBtn.setEnabled(enabled);
        mDeactivateBtn.setEnabled(enabled);
        mActivateBtn.setEnabled(enabled);
        mUpdateBtn.setEnabled(enabled);
        mReprintBtn.setEnabled(enabled);
    }

    private void setConnectionListener(){
        mPOSHandler.setConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected(final BluetoothDevice device) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStatus.setText("Connected");

                        mTerminalType.setVisibility(View.VISIBLE);

                        mTerminalType.setText(device.getName().equalsIgnoreCase("") ? device.getAddress() : device.getName());
                        setEnabled(true);
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View view) {
        if( view.getId() == R.id.connect_btn ){
            BluetoothDevicesDialog dialog = new BluetoothDevicesDialog(this);
            dialog.show();
            return;
        }

        if( !POSHandler.getInstance().isConnected()){
            mToast.setText("No terminal connected to this device.");
            mToast.show();
            return;
        }
        if( mTerminalType.getVisibility() != View.VISIBLE ){
            mToast.setText("Terminal is not ready for operations yet. Please wait.");
            mToast.show();
            return;
        }

        if( view.getId() == mPurchaseBtn.getId() ){
            mPOSHandler.openPaymentActivity(
                    MainActivity.this /*activity*/,
                    REQUEST_CODE_MAKE_PAYMENT /*requestCode*/,
                    "10.50" /*amount*/,
                    UUID.randomUUID().toString()/*transaction reference*/
            );
        }
        else if( view.getId() == mRefundBtn.getId() ){
            mPOSHandler.openRefundActivity(
                    MainActivity.this /*activity*/,
                    REQUEST_CODE_MAKE_REFUND /*requestCode*/,
                    "10.50" /*amount*/,
                    UUID.randomUUID().toString()/*transaction reference*/
            );
        }
        else if( view.getId() == mDeactivateBtn.getId() ){
            mPOSHandler.deactivate();
        }
        else if( view.getId() == mActivateBtn.getId() ){
            mPOSHandler.activate();
        }
        else if( view.getId() == mUpdateBtn.getId() ){
            mPOSHandler.update();
        }
        else if( view.getId() == mReprintBtn.getId() ){
            mPOSHandler.reprintReceipt();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if( requestCode == REQUEST_CODE_MAKE_PAYMENT && resultCode == RESULT_OK) {
            TransactionData transactionData = data.getParcelableExtra(POSHandler.INTENT_EXTRA_TRANSACTION_DATA);
            showTransactionDataAlert(transactionData);
        }
        else if( requestCode == REQUEST_CODE_MAKE_REFUND  && resultCode == RESULT_OK){

        }
    }

    private void showTransactionDataAlert(TransactionData transactionData){
        String message = "Auth code: " + transactionData.getAuthCode() + "\n";
        message += "Approval: " + transactionData.getApproval() + "\n";
        message += "Transaction Local Date: " + transactionData.getTransactionDateLocal() + "\n";
        message += "RRN: " + transactionData.getRRN() + "\n";
        message += "Amount: " + transactionData.getAmount() + "\n";
        message += "Currency: " + transactionData.getCurrencyIsoCode() + "\n";
        message += "Terminal ID: " + transactionData.getTerminalID() + "\n";
        message += "Merchant ID: " + transactionData.getMerchantID() + "\n";
        message += "Merchant Name: " + transactionData.getMerchantName() + "\n";
        message += "Merchant Address Line 1: " + transactionData.getMerchantAddressLine1() + "\n";
        message += "Merchant Address Line 2: " + transactionData.getMerchantAddressLine2() + "\n";
        message += "PAN Masked: " + transactionData.getPANMasked() + "\n";
        message += "Emboss Name: " + transactionData.getEmbossName() + "\n";
        message += "AID: " + transactionData.getAID() + "\n";
        message += "AID Name: " + transactionData.getAIDName() + "\n";
        message += "STAN: " + transactionData.getStan() + "\n";
        message += "Is Signature Required: " + transactionData.isSignatureRequired();


        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Transaction data");
        alertBuilder.setPositiveButton("OK", null);
        alertBuilder.setMessage(message);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog alertDialog = alertBuilder.create();
                alertDialog.show();
            }
        });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if( POSHandler.getInstance().getConnectedDevice() != null && device != null &&
                        POSHandler.getInstance().getConnectedDevice().getAddress().equalsIgnoreCase(device.getAddress())){

                    mStatus.setText("Not connected");
                    mTerminalType.setText("");
                    mTerminalType.setVisibility(View.INVISIBLE);
                    setEnabled(false);
                }
            }
        }
    };
}
