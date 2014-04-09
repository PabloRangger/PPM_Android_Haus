package com.example.cubemanagehome.utility;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.content.Context;
import android.util.Log;

public class API {
	public static final String API_LOCATION = "/api/location/%s";
	public static final String API_DEVICE = "/api/device/%s";
	public static final String API_USER = "/api/user/%s";
	public static final String API_USER_AVATAR = "/api/user/%s/avatar";
	public static final String API_PLUGIN = "/api/plugin/%s";
	public static final String API_PLUGIN_ICON = "/api/plugin/%s/icon";
	public static final String API_VENDOR = "/api/vendor/%s";
	public static final String API_STATUS = "/status";
	public static final String API_AUTH = "/auth";

	public enum HTTP_METHOD {
		GET, POST, PUT, DELETE
	}

	private String url;
	private static API api = null;
	private User user;
	private Token token;
	// private Context context;
	private String uuid;
	private Context context;

	/**
	 * Creates a new Instance of the API if there hasn't been set one already
	 * before.
	 * 
	 * @param host
	 *            (String): The IP Address or the Host-Name of a Server the API
	 *            should be bound to
	 * @return new API Instance
	 */
	public static API getApiInstance() {
		if (api == null) {
			api = new API();
		}
		return api;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Token getTk() {
		return this.token;
	}

	/**
	 * Binds the API to a new Host
	 * 
	 * @param host
	 *            (String): Either the IP Address or the Host-Name of a Server
	 * @throws APIServerNotReachableException
	 */
	public String bind(String host) {
		this.url = "http://" + host;
		this.user = null;
		return this.url;
	}

	private boolean isTokenSet() {
		return (user != null && token != null);
	}

	/**
	 * Calls a Auth Request with the given Username and Password to the API
	 * 
	 * @param (Credentials): A Credentials Object containing the
	 *        Username/Password of the User who should be authenticated
	 * @return (boolean): true if the auth was successfull, false if not
	 * @throws APIUuidNotSetException
	 */
	public boolean authenticate(Credentials c) throws APIUuidNotSetException {
		return this.authenticate(c.getUsername(), c.getPassword());
	}

	public void asyncAuthenticate(final Credentials c,
			final APIAuthCallback callback) {
		new Thread(new Runnable() {

			public void run() {
				try {
					boolean result = authenticate(c);
					if (callback != null) {
						if (result && !token.getToken().isEmpty()
								&& user != null) {
							callback.onSuccess(user, token);
						} else {
							callback.onError("Error on Request!");
						}
					}
				} catch (Exception e) {
					callback.onError(e.getMessage());
				}
			}
		}).start();
	}

	/**
	 * Calls a Auth Request with the given Username and Password to the API
	 * 
	 * @param (String) username (String): The username of the user which should
	 *        be authenticated
	 * @param (String) password (String): The password of the user which should
	 *        be authenticated
	 * @return (boolean): true if the auth was successfull, false if not
	 * @throws APIUuidNotSetException
	 */
	public boolean authenticate(String username, String password)
			throws APIUuidNotSetException {

		File Pathuuid = PathManager.getAbsoluteFilePath(context,
				PathManager.FILE_UUID);

		if (!Pathuuid.exists()) {
			try {
				uuid = UUID.randomUUID().toString();
				XML.Write(uuid, Pathuuid);
			} catch (Exception e) {
				Log.d("HELLOACT", "Unable to write UUID");
			}
		} else {
			try {
				uuid = (String) XML.Read(String.class, Pathuuid);
			} catch (Exception e) {
				Log.d("HELLOACT", "Unable to read UUID");
			}
		}

		filterRequiresUUID();
		List<NameValuePair> parameter = new ArrayList<NameValuePair>();
		parameter.add(new BasicNameValuePair("username", username));
		parameter.add(new BasicNameValuePair("password", password));
		parameter.add(new BasicNameValuePair("uuid", uuid));
		parameter.add(new BasicNameValuePair("name", android.os.Build.MODEL));
		parameter.add(new BasicNameValuePair("system", "Android")); // TODO
		parameter
				.add(new BasicNameValuePair("model", android.os.Build.PRODUCT));
		parameter.add(new BasicNameValuePair("version",
				android.os.Build.VERSION.RELEASE));

		try {
			JSONObject result = (JSONObject) this.postApiRequest(API_AUTH,
					parameter);
			this.user = new User(result.getJSONObject("user"));
			this.token = new Token(result.getString("token"));
			return true;
		} catch (Exception e) {
			this.user = null;
			this.token = null;
			return false;
		}
	}

	public boolean isAuthenticated() {
		return (this.user != null && this.token != null);
	}

	public void login(final String tk, final APIAuthCallback callback) {
		new Thread(new Runnable() {
			Token t = new Token(tk);

			public void run() {
				try {

					JSONObject result = request(API_AUTH, HTTP_METHOD.GET,
							null, t.getToken());
					user = new User(result);
					token = t;
					if (callback != null)
						callback.onSuccess(user, t);

				} catch (Exception e) {
					if (callback != null)
						callback.onError(e.getMessage());
				}
			}

		}).start();
	}

	public void asyncIsReachable(final APIReachableCallback callback) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				boolean result = isReachable();
				if (result) {
					callback.onSuccess();
				} else {
					callback.onError();
				}
			}

		}).start();
	}

	/**
	 * Tests if the Requested API is Reachable AND if it's a valid system
	 * 
	 * @return (boolean): true if reachable, false if not
	 */
	public boolean isReachable() {
		try {
			return ((JSONObject) this.getServerInformation()).getString(
					"status").equals("online");
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Returns the API User
	 * 
	 * @return (User) The API User
	 * @throws APIUserNotSetException
	 */
	public User getUser() throws APIUserNotSetException {
		if (this.user != null)
			return this.user;
		else
			throw new APIUserNotSetException();
	}

	/**
	 * Returns the Hostname/IP of the Server
	 * 
	 * @return (String): Hostname/IP
	 * @throws APIServerNotSetException
	 */
	public String getURL() throws APIServerNotSetException {
		if (this.url != null)
			return this.url;
		else
			throw new APIServerNotSetException();
	}

	public void asyncServerInformation(final APIResponseCallback callback,
			final APIErrorCallback errorCallback) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					JSONObject result = getServerInformation();
					if (callback != null)
						callback.onResult(result);
				} catch (Exception e) {
					if (errorCallback != null)
						errorCallback.onError(e.getMessage());
				}
			}
		}).start();
	}

	/**
	 * Returns Information about the Server the API is connected to.
	 * 
	 * @return JSONObject: The Server Information as JSONObject
	 * @throws APIServerNotReachableException
	 */
	public JSONObject getServerInformation()
			throws APIServerNotReachableException {
		try {
			return (JSONObject) this.getApiRequest(API_STATUS);
		} catch (Exception e) {
			throw new APIServerNotReachableException();
		}
	}

	/**
	 * Checks if there runs a valid Server on the given Host
	 * 
	 * @param host
	 *            (String): Hostname or IP-Address of a Server
	 * @return (boolean): true if Server is valid, false if not
	 */
	public static boolean isValidServer(String host) {
		API temp = new API();
		temp.bind(host);
		return temp.isReachable();
	}

	/**
	 * Builds a RequestURI from the given arguments
	 * 
	 * @param api
	 *            (String): The API which should be used
	 * @param method
	 *            (HTTP_METHOD): The HTTP Method which should be used for this
	 *            Request
	 * @param identifier
	 *            (String): The Identifier which is used to determine the path
	 *            to the Requested Object
	 * @return (URI): The URI which can be used for Requests
	 * @throws Exception
	 */
	private URI buildApiRequestURI(String api, HTTP_METHOD method,
			String identifier, String tk) throws Exception {
		if (api.contains("%s"))
			if (identifier != null)
				api = String.format(api, identifier);
			else
				throw new Exception("Invalid Identifier!");
		String t = null;
		if (isTokenSet())
			t = this.token.getToken();
		else if (tk != null)
			t = tk;
		return new URI(this.url + api + ((t != null) ? "?auth_token=" + t : ""));
	}

	public JSONObject postApiRequest(String api, List<NameValuePair> args)
			throws Exception {
		return this.request(api, HTTP_METHOD.POST, args, null);
	}

	public JSONObject getApiRequest(String api) throws Exception {
		return this.request(api, HTTP_METHOD.GET, null, null);
	}

	public JSONObject getApiRequest(String api, String identifier)
			throws Exception {
		return this.request(api, HTTP_METHOD.GET, identifier, null);
	}

	@SuppressWarnings("unchecked")
	private JSONObject request(String api, HTTP_METHOD method, Object args,
			String tk) throws Exception {
		try {
			if (url == null)
				throw new APIServerNotSetException();
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
			DefaultHttpClient client = new DefaultHttpClient(httpParams);
			client.getParams().setParameter(
					"http.protocol.single-cookie-header", true);
			HttpRequestBase request;
			URI uri;
			if (args instanceof String)
				uri = buildApiRequestURI(api, method, (String) args,
						(tk != null) ? tk : null);
			else
				uri = buildApiRequestURI(api, method, null, (tk != null) ? tk
						: null);
			switch (method) {
			case GET:
				if (api != API_STATUS && api != API_PLUGIN_ICON
						&& api != API_USER_AVATAR && api != API_AUTH)
					filterRequiresAuth();
				request = new HttpGet(uri);
				break;
			case POST:
				if (api != API_AUTH)
					filterRequiresAuth();
				request = new HttpPost(uri);
				if (args instanceof List) {
					List<NameValuePair> nameValuePairs = (List<NameValuePair>) args;
					((HttpPost) request).setEntity(new UrlEncodedFormEntity(
							nameValuePairs));
				}
				break;
			case PUT:
				filterRequiresAuth();
				request = new HttpPut(uri);
				if (args instanceof List) {
					List<NameValuePair> nameValuePairs = (List<NameValuePair>) args;
					((HttpPost) request).setEntity(new UrlEncodedFormEntity(
							nameValuePairs));
				}
				break;
			case DELETE:
				filterRequiresAuth();
				request = new HttpDelete(uri);
			default:
				throw new APIInvalidMethodException();
			}
			if (isTokenSet())
				request.setHeader("X-Auth-Token", this.token.getToken());
			HttpResponse response = null;
			response = client.execute(request);
			HttpEntity responseEntity = response.getEntity();
			if (responseEntity != null) {
				String s = EntityUtils.toString(responseEntity);
				return new JSONObject(s);
			} else
				throw new Exception("Something's wrong in here..");
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	public void asyncPostApiRequest(String api, List<NameValuePair> args,
			APIResponseCallback callback) {
		this.asyncRequest(api, HTTP_METHOD.POST, args, callback, null);
	}

	public void asyncPostApiRequest(String api, List<NameValuePair> args,
			APIResponseCallback callback, APIErrorCallback errorCallback) {
		this.asyncRequest(api, HTTP_METHOD.POST, args, callback, errorCallback);
	}

	public void asyncGetApiRequest(String api, APIResponseCallback callback) {
		this.asyncRequest(api, HTTP_METHOD.GET, null, callback, null);
	}

	public void asyncGetApiRequest(String api, APIResponseCallback callback,
			APIErrorCallback errorCallback) {
		this.asyncRequest(api, HTTP_METHOD.GET, null, callback, errorCallback);
	}

	public void asyncGetApiRequest(String api, String identifier,
			APIResponseCallback callback, APIErrorCallback errorCallback) {
		this.asyncRequest(api, HTTP_METHOD.GET, identifier, callback,
				errorCallback);
	}

	public void asyncImageRequest(final String api, final String identifier,
			final APIImageCallback callback, final APIErrorCallback errorCallback) {
		new Thread(new Runnable() {
			public void run() {
				try {
					URL url = buildApiRequestURI(api, HTTP_METHOD.GET, identifier, null).toURL();
					InputStream is = url.openConnection().getInputStream();
					if(callback!=null)
						callback.onResult(BitmapFactory.decodeStream(is));
				} catch (Exception e) {
					if(errorCallback!=null)
						errorCallback.onError("");
				}

			}
		}).start();
	}

	private void asyncRequest(final String api, final HTTP_METHOD method,
			final Object args, final APIResponseCallback callback,
			final APIErrorCallback errorCallback) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					JSONObject o = request(api, method, args, null);
					if (callback != null)
						callback.onResult(o);
				} catch (Exception e) {
					if (errorCallback != null)
						errorCallback.onError(e.getMessage());
				}
			}
		}).start();
	}

	private void filterRequiresAuth() throws APITokenNotSetException {
		if (this.user == null || this.token == null)
			throw new APITokenNotSetException();
	}

	private void filterRequiresUUID() throws APIUuidNotSetException {
		if (this.uuid == null)
			throw new APIUuidNotSetException();
	}

	public class APINotBoundException extends Exception {

		private static final long serialVersionUID = 1L;

		public String getMessage() {
			return "The API isn't bound to a host yet!";
		}
	}

	public class APIUuidNotSetException extends Exception {

		private static final long serialVersionUID = 1L;

		public String getMessage() {
			return "There is no UUID set!";
		}
	}

	public class APITokenNotSetException extends Exception {

		private static final long serialVersionUID = 1L;

		public String getMessage() {
			return "The API doesn't have a Token assigned!";
		}
	}

	public class APITokenNotValidException extends Exception {

		private static final long serialVersionUID = 1L;

		public String getMessage() {
			return "The given API Token isn't valid!";
		}
	}

	public class APIServerNotReachableException extends Exception {

		private static final long serialVersionUID = 1L;

		public String getMessgae() {
			return "The Server isn't reachable or isn't valid!";
		}
	}

	public class APIServerNotSetException extends Exception {

		private static final long serialVersionUID = 1L;

		public String getMessage() {
			return "The URL isn't set yet!";
		}
	}

	public class APIInvalidMethodException extends Exception {

		private static final long serialVersionUID = 1L;

		public String getMessage() {
			return "This Method isn't a valid HTTP Method!";
		}
	}

	public class APIUserNotSetException extends Exception {

		private static final long serialVersionUID = 1L;

		public String getMessage() {
			return "The API User isn't set!";
		}
	}

	public interface APIAuthCallback {
		public void onSuccess(User u, Token t);

		public void onError(String error);
	}

	public interface APIResponseCallback {
		public void onResult(JSONObject o);
	}

	public interface APIImageCallback {
		public void onResult(Bitmap b);
	}

	public interface APIErrorCallback {
		public void onError(String msg);
	}

	public interface APIReachableCallback {
		public void onError();

		public void onSuccess();
	}
}
