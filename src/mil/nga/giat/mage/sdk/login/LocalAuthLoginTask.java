package mil.nga.giat.mage.sdk.login;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.user.User;
import mil.nga.giat.mage.sdk.exceptions.LoginException;
import mil.nga.giat.mage.sdk.exceptions.UserException;
import mil.nga.giat.mage.sdk.utils.DateUtility;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * A Task intended to be used for local authentication only. Testing or off-line
 * modes perhaps.  TODO: throw {@link LoginException}
 * 
 * @author travis
 * 
 */
public class LocalAuthLoginTask extends AbstractAccountTask {

	private static final String LOG_NAME = LocalAuthLoginTask.class.getName();

	public LocalAuthLoginTask(AccountDelegate delegate, Context applicationContext) {
		super(delegate, applicationContext);
	}

	/**
	 * Called from execute
	 * 
	 * @param params
	 *            Should contain username and password; in that order.
	 */
	@Override
	protected AccountStatus doInBackground(String... params) {

		// retrieve the user name.
		String username = params[0];
		String password = params[1];

		try {
			// use a hash of the password as the token
			String md5Password = MessageDigest.getInstance("MD5").digest(password.getBytes("UTF-8")).toString();
			// put the token information in the shared preferences
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
			Editor editor = sharedPreferences.edit();
			editor.putString(mApplicationContext.getString(R.string.tokenKey), md5Password).commit();
			// FIXME : 8 hours for now?
			editor.putString(mApplicationContext.getString(R.string.tokenExpirationDateKey), DateUtility.getISO8601().format(new Date(new Date().getTime() + 8 * 60 * 60 * 1000))).commit();
		} catch (NoSuchAlgorithmException nsae) {
			nsae.printStackTrace();
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}

		// initialize local active user
		try {
			// get active users
			User currentUser = userHelper.readCurrentUser();
			if (currentUser == null) {

				// delete active user(s)
				userHelper.deleteCurrentUsers();

				// create new active user.
				currentUser = new User("NA", "unknown", "unknown", "unknown", username, null);
				currentUser.setCurrentUser(Boolean.TRUE);
				currentUser = userHelper.create(currentUser);
			} else {
				Log.d(LOG_NAME, "A Current Active User exists." + currentUser);
			}

		} catch (UserException e) {
			// for now, treat as a warning. Not a great state to be in.
			Log.w(LOG_NAME, "Unable to initialize a local Active User.");
		}

		return new AccountStatus(AccountStatus.Status.SUCCESSFUL_LOGIN);
	}
}
