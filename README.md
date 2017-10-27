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
  
  * [Print random receipt](#print-random-receipt)

  * [Terminal mangement](#terminal-management)

  * [Activate terminal](#activate-terminal)
  
  * [Update terminal software](#update-terminal-software)
  
  * [Deactivate terminal](#deactivate-terminal)
  
  * [POS Info statuses](#pos-info-statuses)

  
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
    compile 'com.android.support:cardview-v7:25.0.1'
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

Optional settting for default receipt configuration is avaibale:

```Java
POSHandler.setDefaultReceiptConfig(POSHandler.RECEIPT_PRINT_ONLY_MERCHANT_COPY);
```

If set the default receipt configuration can be removed:

```Java
POSHandler.clearDefaultReceiptConfig();
```

Optional setting for the language of the receipts is avaibale(default is Language.ENGLISH):

```Java
POSHandler.setLanguage(Language.GERMAN);
```

isTerminalBusy() returns boolean to check if an operation is being performed at the moment:
```Java
POSHandler.getInstance().isTerminalBusy();
```

## Connect to terminal

Choose connection type:

```Java
POSHandler.setConnectionType(ConnectionType.BLUETOOTH);
```

```Java
POSHandler.getInstance().connectDevice(context);
```

If connection type is set to BLUETOOTH, make sure ACCESS_COARSE_LOCATION permission is given in order to discover available bluetooth devices.

```Java
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, 
	Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_COARSE_LOCATION);
}
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

## Make payment

Once initialization is completed, you can start using the myPOS SDK Android to accept card payments.

Variant 1. 
Payment via internal SDK activity:
Amount and transaction reference are optional parameters and can be set as null.

```Java
 mPOSHandler.openPaymentActivity(
        MainActivity.this /*activity*/,
        REQUEST_CODE_MAKE_PAYMENT /*requestCode*/,
        "10.50" /*amount*/,
        UUID.randomUUID().toString()/*transaction reference*/
);
```

Variant 2. 
Payment via direct SDK method:
Transaction reference is optional parameter and can be set as null.

```Java
mPOSHandler.purchase(
	"10.50" /*amount*/, 
	UUID.randomUUID().toString() /*transaction reference*/, 
	POSHandler.RECEIPT_PRINT_AUTOMATICALLY /*receipt configuration*/ );
	);
```

## Handle payment result

Variant 1. 
Payment via internal SDK activity:

```Java
protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	if( requestCode == REQUEST_CODE_MAKE_PAYMENT && resultCode == RESULT_OK) {
        		TransactionData transactionData = data.getParcelableExtra(POSHandler.INTENT_EXTRA_TRANSACTION_DATA);
        		// Handle the response here
    	}
}
```

Variant 2. 
Payment via direct SDK method:

```Java
mPOSHandler.setPOSInfoListener(new POSInfoListener() {
    @Override
    public void onPOSInfoReceived(final int command, final int status, final String description) {
        // Handle the response here
    }

    @Override
    public void onTransactionComplete(final TransactionData transactionData) {
        // Handle the response here
    }
});
```
See [POS Info statuses](#pos-info-statuses) for more information

## Refund

With refund host application could initiate refund transaction to the customers’ card account with the specified amount.

Variant 1. 
Refund via internal SDK activity:
Amount and transaction reference are optional parameters and can be set as null.

```Java
 mPOSHandler.openPaymentActivity(
        MainActivity.this /*activity*/,
        REQUEST_CODE_MAKE_REFUND /*requestCode*/,
        "10.50" /*amount*/,
        UUID.randomUUID().toString()/*transaction reference*/
);
```

Variant 2. 
Refund via direct SDK method:
Transaction reference is optional parameter and can be set as null.

```Java
mPOSHandler.purchase(
	"10.50" /*amount*/, 
	UUID.randomUUID().toString() /*transaction reference*/, 
	POSHandler.RECEIPT_PRINT_AUTOMATICALLY /*receipt configuration*/ );
	);
```

## Handle refund result

Variant 1. 
Refund via internal SDK activity:

```Java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	if( requestCode == REQUEST_CODE_MAKE_REFUND && resultCode == RESULT_OK) {
        		TransactionData transactionData = data.getParcelableExtra(POSHandler.INTENT_EXTRA_TRANSACTION_DATA);
        		// Handle the response here
    	}
}
```

Variant 2. 
Refund via direct SDK method:

```Java
mPOSHandler.setPOSInfoListener(new POSInfoListener() {
    @Override
    public void onPOSInfoReceived(final int command, final int status, final String description) {
        // Handle the response here
    }

    @Override
    public void onTransactionComplete(final TransactionData transactionData) {
        // Handle the response here
    }
});
```
See [POS Info statuses](#pos-info-statuses) for more information

## Reprint last receipt

With this method host application could request reprint of last transaction slip.

```Java
mPOSHandler.reprintReceipt();
```

## Print random receipt

Check if the connected myPOS device has a printer hardware:

```Java
mPOSHandler.hasPrinter()
```

Printing an external receipt is performed by passing a ReceiptData object to the printReceipt() method

```Java
ReceiptData receiptData = new ReceiptData();
receiptData.addLogo(1 /*Logo index*/);
receiptData.addEmptyRow();
receiptData.addRow(
	"HEAD" /*text*/, 
	ReceiptData.Align.CENTER, /* Enum align setting (LEFT, CENTER, RIGHT) */
	ReceiptData.FontSize.DOUBLE /* Enum font size setting (SINGLE, DOUBLE) */
	);
mPOSHandler.printReceipt(receiptData);
```

# Terminal management

## Activate

Before using terminal for a first time the SDK has to initiate Terminal activation, which will setup terminal for processing transaction, setting up Terminal ID, Merchant ID etc.

```Java
mPOSHandler.activate();
```

## Update terminal software

Each time terminal processing transaction, processor host checks for existing pending updates, and inform terminal if any. In that case by this method software update is activated, and terminal is going in the update mode.

```Java
mPOSHandler.update();
```

## Deactivate terminal

```Java
mPOSHandler.deactivate();
```

## POS Info statuses

 - POS_STATUS_PENDING_USER_INTERACTION
POS terminal received Purchase or Refund operation. Waiting for user to provide card for the operation.

 - POS_STATUS_USER_CANCEL
Current operation is terminated due user has cancelled it manually from the POS terminal.

 - POS_STATUS_INTERNAL_ERROR
Current operation is terminated due internal error has occured.

 - POS_STATUS_TERMINAL_BUSY
Current operation is terminated due POS terminal is busy with another operation.

 - POS_STATUS_UNSUPPORTED_SDK_VERSION
Current operation is terminated due POS terminal version is not compatiable with myPOS SDK version.

 - POS_STATUS_NO_UPDATE_FOUND
Operation Update is terminated due no update is available for the POS terminal.

 - POS_STATUS_MANDATORY_UPDATE
Current operation is terminated due mandatory update is necessary. Update operation is performed automatically.

 - POS_STATUS_OPTIONAL_UPDATE
No operation is performed after this status. An optional update is available for the POS terminal.

 - POS_STATUS_POS_UPDATING
Terminal received an Update operation and started the procedure.

 - POS_STATUS_ACTIVATION_REQUIRED
Current operation is terminated due POS terminal is not activated. It is necessary to activate the POS terminal in order to perform operations.

 - POS_STATUS_PROCESSING
This status informs that a communication with the Host is performed on operation Purchase, Refund, Activate or Deactivate.

 - POS_STATUS_DEACTIVATION_NOT_COMPLETED
Operation Deactivate finished unsuccessfully.

 - POS_STATUS_ACTIVATION_NOT_REQUIRED
Operation Activate is terminated due POS terminal is already activated.

 - POS_STATUS_ACTIVATION_NOT_COMPLETED
Operation Activate finished unsuccessfully.

 - POS_STATUS_WRONG_ACTIVATION_CODE
Operation Activate is terminated due wrong activation code.

 - POS_STATUS_WRONG_DEACTIVATION_CODE
Operation Deactivate is terminated due wrong deactivation code.

 - POS_STATUS_WAIT_ACTIVATION_CODE
POS terminal received Activate operation and is waiting for user to provide an activation code.

 - POS_STATUS_WAIT_DEACTIVATION_CODE
POS terminal received Dectivate operation and is waiting for user to provide an deactivation code.

 - POS_STATUS_UPDATE_NOT_COMPLETED
Operation Update finished unsuccessfully.

 - POS_STATUS_TRANSACTION_NOT_FOUND
When performing operation Reprint last receipt. Last transaction is not found and the operation is terminated.

 - POS_STATUS_NO_PRINTER_AVAILABLE
Current operation(Print or Reprint last receipt) is terminated due POS device has no printer hardware.

 - POS_STATUS_NO_PAPER
Current operation(Print or Reprint last receipt) is terminated due no paper is available in the POS terminal.
Current operation(Purchase or Refund) completed successfully, but receipt won't be printed due no paper is available in the POS terminal.

 - POS_STATUS_WRONG_AMOUNT
Current operation is terminated due wrong amount is provided to Purchase or Refund operation.

 - POS_STATUS_NO_CARD_FOUND
Current operation is terminated due no card is provided.

 - POS_STATUS_NOT_SUPPORTED_CARD
The provided card is not supported. POS terminal is waiting for supported card.

 - POS_STATUS_CARD_CHIP_ERROR
Current operation is terminated due card chip reading failed.

 - POS_STATUS_INVALID_PIN
Invalid PIN. POS Terminal is waiting for another PIN.

 - POS_STATUS_MAX_PIN_COUNT_EXCEEDED
Current operation is terminated due wrong PINs count has exceeded.

 - POS_STATUS_PIN_CHECK_ONLINE
This status informs that PIN validation is performed online. Current operation continue.

 - POS_STATUS_SUCCESS_ACTIVATION
Operation Activate finished with success.

 - POS_STATUS_SUCCESS_DEACTIVATION
Operation Dectivate finished with success.

 - POS_STATUS_SUCCESS_UPDATE
Operation Update finished with success.

 - POS_STATUS_SUCCESS_PURCHASE
Operation Purchase finished with success.

 - POS_STATUS_SUCCESS_REFUND
Operation Refund finished with success.

 - POS_STATUS_SUCCESS_REPRINT_RECEIPT
Operation Reprint last receipt finished with success.

 - POS_STATUS_SUCCESS_PRINT_RECEIPT
Operation Print finished with success.

 - POS_STATUS_DOWNLOADING_CERTIFICATES_IN_PROGRESS
This status informs that the SDK is downloading certificates from the POS Terminal.

 - POS_STATUS_DOWNLOADING_CERTIFICATES_COMPLETED
Certificated downloading is completed successfully.

 - POS_STATUS_INCORRECT_PRINT_DATA
Operation Print is terminated due incorrect print data is provided.

 - POS_STATUS_INCORRECT_LOGO_INDEX
Operation Print is terminated due incorrect logo index is provided.

