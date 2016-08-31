package com.car.wirelesscontrol.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

public class BluetoothConnectedThread extends Thread
{
	private static final UUID	MY_UUID				= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	final static int			RECIEVE_MESSAGE		= 1;
	static final String			DefaultDeviceName		= "Unknown";
	private BluetoothAdapter	mmBluetoothAdapter	= null;
	private BluetoothDevice		mmBluetoothDevice	= null;
	private BluetoothSocket		mmSocket			= null;
	private InputStream			mmInStream			= null;
	private OutputStream		mmOutStream			= null;
	private BluetoothHandler	mmHandler			= new BluetoothHandler();

	public interface Callback
	{
		final char	CONNECT_ERROR		= 'Y';
		final char	SOCKET_CLOSED		= 'Z';
		final char	STOP_PERFORMANCE	= 'S';
		final char	STOP_OBSTACLE		= 'O';

		void BluetoothRespose(char c);
	}

	static private BluetoothConnectedThread.Callback mCallback = null;

	public BluetoothConnectedThread(Callback callback)
	{
		mCallback = callback;
	}

	public String DeviceName()
	{
		if (null != mmBluetoothDevice)
		{
			return mmBluetoothDevice.getName();
		}
		return DefaultDeviceName;

	}

	boolean Init(BluetoothAdapter bluetoothAdapter, final String mac)
	{
		ResetConnection();
		if (null == bluetoothAdapter)
		{
			Logger.Log.e("bluetoothAdapter is null");
			return false;
		}
		mmBluetoothAdapter = bluetoothAdapter;
		mmBluetoothDevice = bluetoothAdapter.getRemoteDevice(mac);
		if (null == mmBluetoothDevice)
		{
			Logger.Log.e("bluetoth device is null");
			return false;
		}
		try
		{
			mmSocket = mmBluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
		}
		catch (IOException e)
		{
			Logger.Log.e("device", e.getMessage());
			return false;
		}
		return (null != mmSocket);
	}

	private void SocketStreams()
	{
		InputStream tmpIn = null;
		OutputStream tmpOut = null;
		// Get the input and output streams, using temp objects because
		// member streams are final
		try
		{
			tmpIn = mmSocket.getInputStream();
			tmpOut = mmSocket.getOutputStream();
		}
		catch (IOException e)
		{
			Logger.Log.e("mmSocket.*Stream()", "is failed");
		}
		mmInStream = tmpIn;
		mmOutStream = tmpOut;
	}

	private void ConnectSocket()
	{
		try
		{
			mmSocket.connect();
		}
		catch (IOException connectException)
		{
			Logger.Log.e("mmSocket.connect()", "is failed");
			try
			{
				mmSocket.close();
			}
			catch (IOException closeException)
			{
				Logger.Log.t("mmSocket.close()", "is failed");
			}
			mCallback.BluetoothRespose(Callback.CONNECT_ERROR);
			return;
		}
	}

	@Override
	public void run()
	{
		super.run();
		if (mmBluetoothAdapter.isDiscovering())
		{
			mmBluetoothAdapter.cancelDiscovery();
		}
		ConnectSocket();
		SocketStreams();
		byte[] buffer = new byte[256]; // buffer store for the stream
		int bytes = 0;

		// Keep listening to the InputStream until an exception occurs
		while (true)
		{
			if (null == mmInStream)
			{
				try
				{
					Thread.sleep(10000);
				}
				catch (InterruptedException e)
				{
				}
				continue;
			}
			try
			{
				// Read from the InputStream
				bytes = mmInStream.read(buffer);
				mmHandler.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();
			}
			catch (IOException e)
			{
				Logger.Log.e("Thread END", e.getMessage());
				break;
			}
		}
		mCallback.BluetoothRespose(Callback.SOCKET_CLOSED);
		Logger.Log.t("Thread END");
	}

	private void ResetConnection()
	{
		if (mmInStream != null)
		{
			try
			{
				mmInStream.close();
			}
			catch (Exception e)
			{
			}
			mmInStream = null;
		}

		if (mmOutStream != null)
		{
			try
			{
				mmOutStream.close();
			}
			catch (Exception e)
			{
			}
			mmOutStream = null;
		}

		if (mmSocket != null)
		{
			try
			{
				mmSocket.close();
			}
			catch (Exception e)
			{
			}
			mmSocket = null;
		}
		mmBluetoothDevice = null;
	}

	public boolean isConnected()
	{
		if (null == mmSocket)
		{
			return false;
		}
		return mmSocket.isConnected();
	}

	/* Call this from the main activity to send data to the remote device */
	public void Send(char c)
	{
		if (0 == c)
		{
			return;
		}
		if (null == mmOutStream)
		{
			return;
		}
		try
		{
			mmOutStream.write(c);
		}
		catch (IOException e)
		{
			mCallback.BluetoothRespose(Callback.SOCKET_CLOSED);
			Cancel();
			Logger.Log.t("Send(char)", e.getMessage());
		}
	}

	public void Send(String message)
	{
		if (null == mmOutStream)
		{
			return;
		}
		byte[] msgBuffer = message.getBytes();
		try
		{
			mmOutStream.write(msgBuffer);
		}
		catch (IOException e)
		{
			Logger.Log.t("Send(String)", e.getMessage());
		}
	}

	/* Call this from the main activity to shutdown the connection */
	public void Cancel()
	{
		interrupt();
		ResetConnection();
	}

	private static class BluetoothHandler extends Handler
	{
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.what)
			{
				case BlueToothHelper.RECIEVE_MESSAGE:
					byte[] readBuf = (byte[]) msg.obj;
					String strIncom = new String(readBuf, 0, msg.arg1);
					mCallback.BluetoothRespose(strIncom.charAt(0));
					break;
				default:
					break;
			}
		};
	};

}// class BluetoothConnectedThread