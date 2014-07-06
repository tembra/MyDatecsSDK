package br.com.mytdt.print;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONException;

public class MyDatecsSDK extends CordovaPlugin {
    public static final String ACTION_CONNECT = "connect";
    public static final String ACTION_DISCONNECT = "disconnect";
    public static final String ACTION_PRINT_TEXT = "printText";

    private static final MyPrinter myPrinter = new MyPrinter();

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (ACTION_CONNECT.equals(action)) {
            String address = args.getString(0);
            this.connect(address, callbackContext);
            return true;
        } else if (ACTION_DISCONNECT.equals(action)) {
            this.disconnect(callbackContext);
            return true;
        } else if (ACTION_PRINT_TEXT.equals(action)) {
            String text = args.getString(0);
            String charset = args.getString(1);
            this.printText(text, charset, callbackContext);
            return true;
        }
        return false;
    }

    private void connect(String address, CallbackContext callbackContext) {
        final String myAddress = address;
        final CallbackContext myCallbackContext = callbackContext;
        final CordovaInterface myCordova = cordova;
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if ((myAddress != null) && (myAddress.length() > 0)) {
                    try {
                        myPrinter.setCordova(myCordova);
                        myPrinter.setAddress(myAddress);
                        myPrinter.connect();
                        myCallbackContext.success();
                    } catch (Exception e) {
                        e.printStackTrace();
                        myCallbackContext.error(e.getMessage());
                    }
                } else {
                    myCallbackContext.error("Informe o endereço do dispositivo.");
                }
            }
        });
    }

    private void disconnect(CallbackContext callbackContext) {
        final CallbackContext myCallbackContext = callbackContext;
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    myPrinter.disconnect();
                    myCallbackContext.success();
                } catch (Exception e) {
                    e.printStackTrace();
                    myCallbackContext.error(e.getMessage());
                }
            }
        });
    }

    private void printText(String text, String charset, CallbackContext callbackContext) {
        final String myText = text;
        final String myCharset = charset;
        final CallbackContext myCallbackContext = callbackContext;
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if ((myText != null) && (myText.length() > 0)) {
                    String defCharset = "CP1252";
                    if ((myCharset != null) && (myCharset.length() > 0)) {
                        defCharset = myCharset;
                    }
                    try {
                        myPrinter.printText(myText, defCharset);
                        myCallbackContext.success();
                    } catch (Exception e) {
                        e.printStackTrace();
                        myCallbackContext.error(e.getMessage());
                    }
                } else {
                    myCallbackContext.error("Informe o texto a ser impresso.");
                }
            }
        });
    }
}
