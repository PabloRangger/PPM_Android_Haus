package com.example.cubemanagehome.utility;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class WebSocket {
	// REQUESTS
	public static final String WSS_REQUEST_COUNT = "C!G:COUNT";
	public static final String WSS_REQUEST_ACTIVE = "C!G:ACTIVE";
	public static final String WSS_REQUEST_VALUE = "C!G:VALUE";
	// RESPONSES
	public static final String WSS_RESPONSE_ACTIVE = "W!R:ADD";
	public static final String WSS_RESPONSE_NEW = "W!R:NEW";
	public static final String WSS_RESPONSE_COUNT = "W!R:COUNT";
	public static final String WSS_RESPONSE_VALUE = "W!R:VALUE";
	public static final String WSS_RESPONSE_REMOVE = "W!R:REMOVE";
	public static final String WSS_SET_TOKEN = "C!S:TOKEN";
	public static final String WSS_RESPONSE_UPDATE = "W!R:UPDATE";
	
	public static final String WSS_RESPONSE_TOKEN_ACCEPT = "W!R:TKACC";
	public static final String WSS_RESPONSE_TOKEN_REFUSE = "W!R:TKREF";
	// ERROR
	public static final String WSS_RESPONSE_ERROR_AUTH = "W!E:AUTH";
	public static final String WSS_RESPONSE_ERROR_API = "W!E:API";
	// Log Tag
	private static final String TAG = "communication";

	// Status
	public enum ConnectionStatus {
		CONNECTED, CLOSED, DISCONNECTED
	}

	// URI
	private String uri;
	private WebSocketConnection mConnection;
	private List<ConnectedToServerEventListener> _connectListeners = new ArrayList<ConnectedToServerEventListener>();
	private List<DisconnectedFromServerEventListener> _disconnectListeners = new ArrayList<DisconnectedFromServerEventListener>();
	private List<MessageReceivedEventListener> _messageReceivedListeners = new ArrayList<WebSocket.MessageReceivedEventListener>();

	public WebSocket() {
		mConnection = new WebSocketConnection();
	}

	public void connect(String ip, int port) throws WebSocketException,
			MalformedURLException {
		this.uri = Utility.getURI(Utility.URI_WEBSOCKET, ip, port);
		mConnection.connect(uri.toString(), new WebSocketHandler() {
			public void onOpen() {
				fireOnOpen();
			}

			public void onTextMessage(String message) {
				try {
					fireOnMessage(new JSONObject(message));
				} catch (JSONException e) {
					Log.w(TAG, e.getMessage());
				}
			}

			public void onClose(int code, String reason) {
				fireOnClose(reason);
			}
		});
	}

	public void disconnect() {
		mConnection.disconnect();
	}

	public void send(String type, HashMap<String, Object> data) {
		HashMap<String, Object> message = new HashMap<String, Object>();
		message.put("type", type);
		if (data != null) {
			JSONObject obj = new JSONObject(data);
			message.put("data", obj);
		}
		JSONObject obj2 = new JSONObject(message);
		Log.d("communication", obj2.toString());
		this.mConnection.sendTextMessage(obj2.toString());

	}

	public void send(String type) {
		send(type, null);
	}

	public void addConnectListener(ConnectedToServerEventListener l) {
		this._connectListeners.add(l);
	}

	public void addDisconnectListener(DisconnectedFromServerEventListener l) {
		this._disconnectListeners.add(l);
	}

	public void addMessageReceivedListener(MessageReceivedEventListener l) {
		this._messageReceivedListeners.add(l);
	}

	public void removeConnectListener(ConnectedToServerEventListener l) {
		this._connectListeners.remove(l);
	}

	public void removeDisconnectListener(DisconnectedFromServerEventListener l) {
		this._disconnectListeners.remove(l);
	}

	public void removeMessageReceivedListener(MessageReceivedEventListener l) {
		this._messageReceivedListeners.remove(l);
	}

	private synchronized void fireOnOpen() {
		for (ConnectedToServerEventListener l : _connectListeners) {
			l.onConnect(new ConnectedToServerEvent(this));
		}
	}

	private synchronized void fireOnClose(String reason) {
		for (DisconnectedFromServerEventListener l : _disconnectListeners) {
			l.onDisconnect(new DisconnectedFromServerEvent(this, reason));
		}
	}

	private synchronized void fireOnMessage(JSONObject message)
			throws JSONException {
		for (MessageReceivedEventListener l : _messageReceivedListeners) {
			l.onMessageReceived(new MessageReceivedEvent(this, message));
		}
	}

	public class ConnectedToServerEvent extends java.util.EventObject {
		private static final long serialVersionUID = 1L;

		public ConnectedToServerEvent(Object source) {
			super(source);
		}
	}

	public class DisconnectedFromServerEvent extends java.util.EventObject {
		private static final long serialVersionUID = 1L;
		private String reason;

		public DisconnectedFromServerEvent(Object source, String reason) {
			super(source);
			this.reason = reason;
		}

		public String getReason() {
			return this.reason;
		}
	}

	public class MessageReceivedEvent extends java.util.EventObject {
		private static final long serialVersionUID = 1L;
		private JSONObject message;
		private String type;
		private Object data;

		public MessageReceivedEvent(Object source, JSONObject message)
				throws JSONException {
			super(source);
			this.message = message;
			this.type = message.getString("type");
			if (message.has("data")) {
				try {
					this.data = message.getJSONObject("data");
				} catch (JSONException e) {
					this.data = message.getJSONArray("data");
				}
			}
		}

		public JSONObject getMessage() {
			return this.message;
		}

		public Object getData() {
			return this.data;
		}

		public String getType() {
			return type;
		}
	}

	public interface ConnectedToServerEventListener {
		public void onConnect(ConnectedToServerEvent e);
	}

	public interface DisconnectedFromServerEventListener {
		public void onDisconnect(DisconnectedFromServerEvent e);
	}

	public interface MessageReceivedEventListener {
		public void onMessageReceived(MessageReceivedEvent e);
	}
}
