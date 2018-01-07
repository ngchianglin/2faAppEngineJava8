/*
* MIT License
*
*Copyright (c) 2018 Ng Chiang Lin
*
*Permission is hereby granted, free of charge, to any person obtaining a copy
*of this software and associated documentation files (the "Software"), to deal
*in the Software without restriction, including without limitation the rights
*to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
*copies of the Software, and to permit persons to whom the Software is
*furnished to do so, subject to the following conditions:
*
*The above copyright notice and this permission notice shall be included in all
*copies or substantial portions of the Software.
*
*THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
*IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
*FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
*AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
*LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
*OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
*SOFTWARE.
*
*/

package sg.nighthour.app;

import java.util.logging.Logger;
import javax.servlet.ServletException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;

import sg.nighthour.crypto.CryptoUtil;



public class LoginDAO
{

    private static final Logger log = Logger.getLogger(LoginDAO.class.getName());

    /**
     * Validates user credential
     * 
     * @param userid 
     * @param password
     * @param remoteip client ip address
     * @return true if user is valid, false otherwise
     */
    public static boolean validateUser(String userid, String password, String remoteip)
    {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key k1 = KeyFactory.createKey(AppConstants.USER_KIND, userid);

        Transaction txn = datastore.beginTransaction();

        try
        {
            Entity user = null;
            try
            {
                user = datastore.get(k1);
            }
            catch (EntityNotFoundException e)
            {
                //For Login functionality
                //don't log non-existent userid to better secure the logs
                //An actual user may accidentially key their password in the userid field
                log.warning("Error: userid not found " + remoteip);
                return false;
            }

            
            boolean account_locked = (Boolean) user.getProperty("AccountLock");

            if (account_locked)
            {
                log.warning("Error: Cannot login Account is locked " + userid + " " + remoteip);
                return false;
            }
            
            String stored_salt = (String) user.getProperty("Salt");
            String stored_password = (String) user.getProperty("Password");

            byte[] stored_salt_bytes = CryptoUtil.hexStringToByteArray(stored_salt);
            char[] user_password_char = password.toCharArray();

            byte[] user_derivekey = CryptoUtil.getPasswordKey(user_password_char, stored_salt_bytes,
                    CryptoUtil.PBE_ITERATION);

            if (user_derivekey == null)
            {
                log.warning("Error: Unable to derive PBKDF2 password using CryptoUtil " + userid + " " + remoteip);
                CryptoUtil.zeroCharArray(user_password_char);
                password = null;
                return false;
            }

            CryptoUtil.zeroCharArray(user_password_char);
            password = null;

            String user_derivekey_string = CryptoUtil.byteArrayToHexString(user_derivekey);
            CryptoUtil.zeroByteArray(user_derivekey);

            if (user_derivekey_string.equals(stored_password))
            {
                user_derivekey_string = null;
                stored_password = null;
                user.setProperty("FailLogin", 0);
                datastore.put(txn, user);
                txn.commit();
                return true;

            }
            else
            {
                log.warning("Error: Fail Login " + userid + " " + remoteip);
                long faillogin = (Long) user.getProperty("FailLogin");
                faillogin++;
                user.setProperty("FailLogin", faillogin);
                if (faillogin >= AppConstants.MAX_FAIL_LOGIN)
                {
                    log.warning("Error: Too many fail logins Account is locked " + userid + " " + remoteip);
                    user.setProperty("AccountLock", true);
                }
                datastore.put(txn, user);
                txn.commit();
                return false;
            }

        }
        finally
        {
            if (txn.isActive())
            {
                txn.rollback();
            }
        }

    }

    /**
     * Check if a user account is locked
     * 
     * @param userid
     * @return true if account is locked or false otherwise
     * @throws ServletException
     */
    public static boolean isAccountLocked(String userid, String remoteip) throws ServletException
    {
        if (userid == null)
        {
            log.warning("Error: userid is null");
            return true;
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key k1 = KeyFactory.createKey(AppConstants.USER_KIND, userid);

        try
        {

            Entity user = datastore.get(k1);
            boolean acctlock = (Boolean) user.getProperty("AccountLock");
            return acctlock;

        }
        catch (EntityNotFoundException e)
        {
            log.warning("Error: user entity not found " + remoteip);
            throw new ServletException("Error: user entity not found");
        }

    }

}
