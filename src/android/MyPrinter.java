package br.com.mytdt.print;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CallbackContext;

import com.datecs.api.printer.Printer;
import com.datecs.api.printer.ProtocolAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

import android.os.Build;
import java.lang.reflect.Method;

public class MyPrinter {

	private final ProtocolAdapter.ChannelListener mChannelListener = new ProtocolAdapter.ChannelListener() {
		@Override
		public void onReadEncryptedCard() {
			// TODO: onReadEncryptedCard
		}

		@Override
		public void onReadCard() {
			// TODO: onReadCard            
		}

		@Override
		public void onReadBarcode() {
			// TODO: onReadBarcode
		}

		@Override
		public void onPaperReady(boolean state) {
			if (state) {
				toast("Papel ok");
			} else {
				toast("Sem papel");
			}
		}

		@Override
		public void onOverHeated(boolean state) {
			if (state) {
				toast("Superaquecimento");
			}
		}

		@Override
		public void onLowBattery(boolean state) {
			if (state) {
				toast("Pouca bateria");
			}
		}
	};

	private Printer mPrinter;
	private ProtocolAdapter mProtocolAdapter;
	private BluetoothSocket mBluetoothSocket;
	private boolean mRestart;
	private String mAddress;
	private CordovaInterface mCordova;
	private CallbackContext mConnectCallbackContext;
	private ProgressDialog mDialog;

	public MyPrinter() {
	}

	public void setAddress(String address) {
		mAddress = address;
	}

	public void setCordova(CordovaInterface cordova) {
		mCordova = cordova;
	}

	public synchronized void connect(CallbackContext callbackContext) {
		mRestart = true;
		mConnectCallbackContext = callbackContext;
		closeActiveConnection();
		String address = mAddress;
		if (BluetoothAdapter.checkBluetoothAddress(address)) {
			establishBluetoothConnection(address, callbackContext);
		}
	}

	public synchronized void disconnect(CallbackContext callbackContext) {
		mRestart = false;
		closeActiveConnection();
		callbackContext.success();
	}

	private synchronized void closeBluetoothConnection() {
		BluetoothSocket s = mBluetoothSocket;
		mBluetoothSocket = null;
		if (s != null) {
			try {
				Thread.sleep(50);
				s.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private synchronized void closePrinterConnection() {
		if (mPrinter != null) {
			mPrinter.release();
		}

		if (mProtocolAdapter != null) {
			mProtocolAdapter.release();
		}
	}

	private synchronized void closeActiveConnection() {
		closePrinterConnection();
		closeBluetoothConnection();
	}

	private void error(final String text, boolean resetConnection) {
		mCordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(mCordova.getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
			}
		});
		if (resetConnection) {
			connect(mConnectCallbackContext);
		}
	}

	private void toast(final String text) {
		mCordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!mCordova.getActivity().isFinishing()) {
					Toast.makeText(mCordova.getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void doJob(final Runnable job, final String jobTitle, final String jobName) {
		// Start the job from main thread
		mCordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Progress dialog available due job execution
				final ProgressDialog dialog = createProgressDialog(mCordova);
				dialog.setTitle(jobTitle);
				dialog.setMessage(jobName);
				dialog.setCancelable(false);
				dialog.setCanceledOnTouchOutside(false);
				dialog.show();
				
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							job.run();
						} finally {
							dialog.dismiss();
						}
					}
				});
				t.start();
			}
		});
	}

	private void doPrintJob(final Runnable job) {
		mCordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						job.run();
					}
				});
				t.start();
			}
		});
	}

	private BluetoothSocket createBluetoothSocket(BluetoothDevice device, UUID uuid) throws IOException {
		if (Build.VERSION.SDK_INT >= 10) {
			try {
				final Method m = device.getClass().getMethod("createRfcommSocketToServiceRecord", new Class[] { UUID.class });
				return (BluetoothSocket) m.invoke(device, uuid);
			} catch (Exception e) {
				e.printStackTrace();
				error("Falha ao criar comunicação: " + e.getMessage(), mRestart);
			}
		}
		return device.createRfcommSocketToServiceRecord(uuid);
	}

	private void establishBluetoothConnection(final String address, final CallbackContext callbackContext) {
		doJob(new Runnable() {
			@Override
			public void run() {
				BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
				BluetoothDevice device = adapter.getRemoteDevice(address);
				UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
				InputStream in = null;
				OutputStream out = null;

				adapter.cancelDiscovery();
				
				try {
					mBluetoothSocket = createBluetoothSocket(device, uuid);
					Thread.sleep(50);
					mBluetoothSocket.connect();
					in = mBluetoothSocket.getInputStream();
					out = mBluetoothSocket.getOutputStream();
				} catch (Exception e) {
					e.printStackTrace();
					error("Falha ao conectar: " + e.getMessage(), mRestart);
					return;
				}
				
				try {
					initPrinter(in, out, callbackContext);
				} catch (IOException e) {
					e.printStackTrace();
					error("Falha ao inicializar: " + e.getMessage(), mRestart);
					return;
				}
			}
		}, "Impressora", "Conectando..");
	}

	protected void initPrinter(InputStream inputStream, OutputStream outputStream, CallbackContext callbackContext) throws IOException {
		mProtocolAdapter = new ProtocolAdapter(inputStream, outputStream);

		if (mProtocolAdapter.isProtocolEnabled()) {
			final ProtocolAdapter.Channel channel = mProtocolAdapter.getChannel(ProtocolAdapter.CHANNEL_PRINTER);
			channel.setListener(mChannelListener);
			// Create new event pulling thread
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						try {
							channel.pullEvent();
						} catch (IOException e) {
							e.printStackTrace();
							error(e.getMessage(), mRestart);
							break;
						}
					}
				}
			}).start();
			mPrinter = new Printer(channel.getInputStream(), channel.getOutputStream());
		} else {
			mPrinter = new Printer(mProtocolAdapter.getRawInputStream(), mProtocolAdapter.getRawOutputStream());
		}
		callbackContext.success();
	}

	public void init(final CallbackContext callbackContext) {
		mCordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					final ProgressDialog dialog = createProgressDialog(mCordova);
					dialog.setTitle("Impressora");
					dialog.setMessage("Imprimindo..");
					dialog.setCancelable(false);
					dialog.setCanceledOnTouchOutside(false);
					dialog.show();
					mDialog = dialog;
					callbackContext.success();
				} catch (Exception e) {
					e.printStackTrace();
					error("Falha ao inicializar: " + e.getMessage(), mRestart);
				}
			}
		});
	}

	public void finish(final CallbackContext callbackContext) {
		mCordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					mDialog.dismiss();
					callbackContext.success();
				} catch (Exception e) {
					e.printStackTrace();
					error("Falha ao finalizar: " + e.getMessage(), mRestart);
				}
			}
		});
	}

	public void reset(final CallbackContext callbackContext) {
		doPrintJob(new Runnable() {
			@Override
			public void run() {
				try {
					mPrinter.reset();
					callbackContext.success();
				} catch (IOException e) {
					e.printStackTrace();
					error("Falha reset: " + e.getMessage(), mRestart);
				}
			}
		});
	}

	public void printText(final String myText, final String myCharset, final CallbackContext callbackContext) {
		doPrintJob(new Runnable() {
			@Override
			public void run() {
				try {
					mPrinter.printText(myText, myCharset);
					callbackContext.success();
				} catch (IOException e) {
					e.printStackTrace();
					error("Falha printText: " + e.getMessage(), mRestart);
				}
			}
		});
	}

	public void printTaggedText(final String myText, final String myCharset, final CallbackContext callbackContext) {
		doPrintJob(new Runnable() {
			@Override
			public void run() {
				try {
					mPrinter.printTaggedText(myText, myCharset);
					callbackContext.success();
				} catch (IOException e) {
					e.printStackTrace();
					error("Falha printTaggedText: " + e.getMessage(), mRestart);
				}
			}
		});
	}

	public void feedPaper(final int lines, final CallbackContext callbackContext) {
		doPrintJob(new Runnable() {
			@Override
			public void run() {
				try {
					mPrinter.feedPaper(lines);
					callbackContext.success();
				} catch (IOException e) {
					e.printStackTrace();
					error("Falha feedPaper: " + e.getMessage(), mRestart);
				}
			}
		});
	}

	public void flush(final CallbackContext callbackContext) {
		doPrintJob(new Runnable() {
			@Override
			public void run() {
				try {
					mPrinter.flush();
					callbackContext.success();
				} catch (IOException e) {
					e.printStackTrace();
					error("Falha flush: " + e.getMessage(), mRestart);
				}
			}
		});
	}

	public void selectPageMode(final CallbackContext callbackContext) {
		doPrintJob(new Runnable() {
			@Override
			public void run() {
				try {
					mPrinter.selectPageMode();
					callbackContext.success();
				} catch (IOException e) {
					e.printStackTrace();
					error("Falha selectPageMode: " + e.getMessage(), mRestart);
				}
			}
		});
	}

	public void selectStandardMode(final CallbackContext callbackContext) {
		doPrintJob(new Runnable() {
			@Override
			public void run() {
				try {
					mPrinter.selectStandardMode();
					callbackContext.success();
				} catch (IOException e) {
					e.printStackTrace();
					error("Falha selectStandardMode: " + e.getMessage(), mRestart);
				}
			}
		});
	}

	public void printPage(final CallbackContext callbackContext) {
		doPrintJob(new Runnable() {
			@Override
			public void run() {
				try {
					mPrinter.printPage();
					callbackContext.success();
				} catch (IOException e) {
					e.printStackTrace();
					error("Falha printPage: " + e.getMessage(), mRestart);
				}
			}
		});
	}

	public void setAlign(final int align, final CallbackContext callbackContext) {
		doPrintJob(new Runnable() {
			@Override
			public void run() {
				try {
					mPrinter.setAlign(align);
					callbackContext.success();
				} catch (IOException e) {
					e.printStackTrace();
					error("Falha setAlign: " + e.getMessage(), mRestart);
				}
			}
		});
	}

	public void printSelfTest(final CallbackContext callbackContext) {
		doPrintJob(new Runnable() {
			@Override
			public void run() {
				try {
					mPrinter.printSelfTest();
					callbackContext.success();
				} catch (IOException e) {
					e.printStackTrace();
					error("Falha printSelfTest: " + e.getMessage(), mRestart);
				}
			}
		});
	}

    @SuppressLint("InlinedApi")
    private ProgressDialog createProgressDialog(CordovaInterface cordova) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return new ProgressDialog(cordova.getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        } else {
            return new ProgressDialog(cordova.getActivity());
        }
    }

}