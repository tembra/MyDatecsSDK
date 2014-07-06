package br.com.mytdt.print;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

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
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if ((address != null) && (address.length() > 0)) {
                    try {
                        myPrinter.setCordova(cordova);
                        myPrinter.setAddress(address);
                        myPrinter.connect();
                        callbackContext.success();
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error(e.getMessage());
                    }
                } else {
                    callbackContext.error("Informe o endereço do dispositivo.");
                }
            }
        }
    }

    private void disconnect(CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    myPrinter.disconnect();
                    callbackContext.success();
                } catch (Exception e) {
                    e.printStackTrace();
                    callbackContext.error(e.getMessage());
                }
            }
        }
    }

    private void printText(String text, String charset, CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if ((text != null) && (text.length() > 0)) {
                    String myCharset = "CP1252";
                    if ((charset != null) && (charset.length() > 0)) {
                        myCharset = charset;
                    }
                    try {
                        myPrinter.printText(text, myCharset);
                        callbackContext.success();
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error(e.getMessage());
                    }
                } else {
                    callbackContext.error("Informe o texto a ser impresso.");
                }
            }
        }
    }
}
