# myPOS SDK Android

This repository provides a native Android SDK, which enables to integrate your Mobile App with myPOS Card terminals, processed by its platform, to accept card payments (including but not limited to VISA, Mastercard, UnionPay International, JCB, Bancontact). myPOS SDK Android communicates transparently to the card terminal(s) via Bluetooth. To process checkout SDK provides management of the terminal to complete all the steps for transaction processing, return or refund, card storage for recurring transactions, and communicates to the application transaction status, card token, card masked PAN.

No sensitive card data is ever passed through to or stored on the merchant&#39;s phone. All data is encrypted by the card terminal, which has been fully certified to the highest industry standards (PCI, EMV I &amp; II, Visa, MasterCard &amp; Amex).

### Prerequisites

1. Merchant account on [www.myPOS.eu](https://www.mypos.eu/) (or received a test account).
2. Received myPOS terminal
3. Requested Access   [Developers myPOS](http://developers.mypos.eu) site.
4.	Deployment Target Android 4.0.3 or later.
5.	Android SDK Platform 25 or later.
6.	Android SDK Build-tools version 25.0.0 or later.
7.	Android Device.

### Table of Contents

* [Integration](#integration)

  * [Dependency](#dependency)
  
  * [Initialization](#initialization)

  * [Connect to terminal](#connect-to-terminal)
  
  * [Attach connection listener](#attach-connection-listener)
  
  * [Make payment](#make-payment)
  
  * [Handle payment result](#handle-payment-result)
  
  * [Refund](#refund)
  
  * [Handle refund result](#handle-refund-result)
  
  * [Reprint last receipt](#reprint-last-receipt)

* [Terminal mangement](#terminal-management)

  * [Activate terminal](#activate-terminal)
  
  * [Update terminal software](#update-terminal-software)
  
  * [Deactivate terminal](#deactivate-terminal)
  
  
# Integration

As example of integration, please use the sample app provided in this repository as a reference

## Dependency

In Android Studio open Project Structure and choose to add new Module, then 'Import .JAR/.AAR package' and navigate to your copy of icardpossdk.aar library.

Add the dependency to a module in your build.gradle:
```Java
    compile project(':icardpossdk')
```

Add the following dependencies:
```Java
    compile 'com.android.support:appcompat-v7:25.0.1'
    compile 'com.android.support:design:25.0.1'
    compile 'com.android.support:support-v4:25.0.1'
    compile 'com.android.support:gridlayout-v7:25.0.1'
```

## Initialization

Initialize the MyPos components in your app:

```Java
public class SampleApplication extends Application {
	
private POSHandler  mPOSHandler;

@Override
public void onCreate() {
	super.onCreate();
	POSHandler.setCurrency(Currency.EUR);
 	mPOSHandler = POSHandler.getInstance();
}
```

## Connect to terminal

Make sure ACCESS_COARSE_LOCATION permission is given in order to discover available bluetooth devices.

```Java
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, 
	Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_COARSE_LOCATION);
}
```

Launch this dialog to search and show available bluetooth devices.

```Java
BluetoothDevicesDialog dialog = new BluetoothDevicesDialog(this);
dialog.show();
```

## Attach connection listener

```Java
mPOSHandler.setConnectionListener(new ConnectionListener() {
    @Override
    public void onConnected(final BluetoothDevice device) {
        // handle connected event here
    }
});
```

## Make a payment

Once initialization is completed, you can start using the myPOS SDK Android to accept card payments.
Host application has to specify amount of the transaction, with automated print of the slip after transaction. 

```Java
 mPOSHandler.openPaymentActivity(
        MainActivity.this /*activity*/,
        REQUEST_CODE_MAKE_PAYMENT /*requestCode*/,
        "10.50" /*amount*/,
        UUID.randomUUID().toString()/*transaction reference*/
);
```

Amount and transaction reference are optional parameters and can be set as null.

## Handle payment result

```Java
protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	if( requestCode == REQUEST_CODE_MAKE_PAYMENT && resultCode == RESULT_OK) {
        		TransactionData transactionData = data.getParcelableExtra(POSHandler.INTENT_EXTRA_TRANSACTION_DATA);
        		// Handle the response here
    	}
}
```

## Refund

With refund host application could initiate refund transaction to the customers’ card account with the specified amount.

```Java
 mPOSHandler.openRefundActivity(
        MainActivity.this /*activity*/,
        REQUEST_CODE_MAKE_REFUND /*requestCode*/,
        "10.50" /*amount*/,
        UUID.randomUUID().toString()/*transaction reference*/
);
```
Amount and transaction reference are optional parameters and can be set as null.

## Handle refund result

```Java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	if( requestCode == REQUEST_CODE_MAKE_REFUND && resultCode == RESULT_OK) {
        		TransactionData transactionData = data.getParcelableExtra(POSHandler.INTENT_EXTRA_TRANSACTION_DATA);
        		// Handle the response here
    	}
}
```

## Reprint last receipt

With this method host application could request reprint of last transaction slip.

```Java
mPOSHandler.reprintReceipt();
```

# Terminal management

## Activate

Before using terminal for a first time the SDK has to initiate Terminal activation, which will setup terminal for processing transaction, setting up Terminal ID, Merchant ID etc.

```Java
mPOSHandler.reprintReceipt();
```

## Update terminal software

Each time terminal processing transaction, processor host checks for existing pending updates, and inform terminal if any. In that case by this method software update is activated, and terminal is going in the update mode.

```Java
mPOSHandler.reprintReceipt();
```

## Deactivate terminal

```Java
mPOSHandler.reprintReceipt();
```
