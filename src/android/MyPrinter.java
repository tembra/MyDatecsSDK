package br.com.mytdt.print;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.datecs.api.printer.Printer;
import com.datecs.api.printer.ProtocolAdapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

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

	public MyPrinter() {
	}

	public void setAddress(String address) {
		mAddress = address;
	}

	public synchronized void connect() {
		mRestart = true;
		closeActiveConnection();
		String address = mAddress;
		if (BluetoothAdapter.checkBluetoothAddress(address)) {
			establishBluetoothConnection(address);
		}
	}

	public synchronized void disconnect() {
		mRestart = false;
		closeActiveConnection();
	}

	private synchronized void closeBluetoothConnection() {
		BluetoothSocket s = mBluetoothSocket;
		mBluetoothSocket = null;
		if (s != null) {
			try {
				s.close();
			} catch (IOException e) {
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
		if (resetConnection) {
			cordova.getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(cordova.getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
				}
			});
				
			connect();
		}
	}

	private void toast(final String text) {
		cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!isFinishing()) {
					Toast.makeText(cordova.getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void doJob(final Runnable job, final String jobTitle, final String jobName) {
		// Start the job from main thread
		cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Progress dialog available due job execution
				final ProgressDialog dialog = new ProgressDialog(cordova.getActivity());
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

	private void establishBluetoothConnection(final String address) {
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
					mBluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
					mBluetoothSocket.connect();
					in = mBluetoothSocket.getInputStream();
					out = mBluetoothSocket.getOutputStream();
				} catch (IOException e) {
					e.printStackTrace();
					error("Falha ao conectar. " +  e.getMessage(), mRestart);
					return;
				}
				
				try {
					initPrinter(in, out);
				} catch (IOException e) {
					e.printStackTrace();
					error("Falha ao inicializar. " +  e.getMessage(), mRestart);
					return;
				}
			}
		}, "título", "Conectando a impressora..");
	}

	protected void initPrinter(InputStream inputStream, OutputStream outputStream) throws IOException {
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
	}

	private void printText(String myText, String myCharset) {
		final String printText = myText;
		final String printCharset = myCharset;
		doJob(new Runnable() {
			@Override
			public void run() {
				try {
					mPrinter.reset();
					mPrinter.printText(printText, printCharset);
					mPrinter.feedPaper(100);
					mPrinter.flush();
				} catch (IOException e) {
					e.printStackTrace();
					error("Falha ao imprimir texto. " + e.getMessage(), mRestart);
				}
			}
		}, "título", "Imprimindo texto..");
	}

}