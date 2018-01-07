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

public class OTPDAO
{

    private static final Logger log = Logger.getLogger(OTPDAO.class.getName());

    /**
     * Retrieves the otp secret hexadecimal string from the userid
     * 
     * @param userid
     * @param remoteip
     * @return hexadecimal secret string 
     * @throws ServletException
     */
    public static String getOTPSecret(String userid, String remoteip) throws ServletException
    {
        if (userid == null || remoteip == null)
        {
            log.warning("Error: null arguments");
            throw new ServletException("Error: null arguments");
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key userkey = KeyFactory.createKey(AppConstants.USER_KIND, userid);

        try
        {
            Entity user = datastore.get(userkey);
            String otphexstring = (String) user.getProperty("TOTP");
            return otphexstring;

        }
        catch (EntityNotFoundException e)
        {
            log.warning("Error: user entity not found " + userid + " " + remoteip);
            throw new ServletException("Error: user entity not found");
        }

    }

    /**
     * Check if a user account is locked
     * 
     * @param userid
     * @param remoteip
     * @return true if account is locked, false otherwise
     * @throws ServletException
     */
    public static boolean isAccountLocked(String userid, String remoteip) throws ServletException
    {
        if (userid == null || remoteip == null)
        {
            log.warning("Error: null arguments " + remoteip);
            throw new ServletException("Error: null arguments");
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key userkey = KeyFactory.createKey(AppConstants.USER_KIND, userid);

        try
        {
            Entity user = datastore.get(userkey);
            boolean locked = (Boolean) user.getProperty("AccountLock");
            return locked;

        }
        catch (EntityNotFoundException e)
        {
            log.warning("Error: user entity not found " + userid + " " + remoteip);
            throw new ServletException("Error: user entity not found");
        }

    }

    
    
    /**
     * Increments the failed login count for a user
     * Locked the user account if fail logins exceed threshold.
     * 
     * @param userid
     * @param remoteip 
     * @throws ServletException
     */
    public static void incrementFailLogin(String userid, String remoteip)
            throws ServletException
    {
        if (userid == null || remoteip == null)
        {
            log.warning("Error: null arguments " + remoteip);
            throw new ServletException("Error: null arguments");
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key userkey = KeyFactory.createKey(AppConstants.USER_KIND, userid);
       
        Transaction txn = datastore.beginTransaction();
        Entity user;
        try
        {
            try
            {
                user = datastore.get(userkey);
            }
            catch (EntityNotFoundException e)
            {
                log.warning("Error: user entity not found " + userid + " " + remoteip);
                throw new ServletException("Error: user entity not found");
            }
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
     * Reset the failed login counts of a user to zero 
     * If an account is locked an exception will be thrown
     * 
     * @param userid
     * @param remoteip
     * @throws ServletException
     */
    public static void resetFailLogin(String userid, String remoteip) throws ServletException
    {
        if (userid == null || remoteip == null)
        {
            log.warning("Error: null arguments");
            throw new ServletException("Error: null arguments");
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key userkey = KeyFactory.createKey(AppConstants.USER_KIND, userid);
        Transaction txn = datastore.beginTransaction();
        Entity user;

        try
        {
            try
            {
                user = datastore.get(userkey);
            }
            catch (EntityNotFoundException e)
            {
                log.warning("Error: user entity not found " + userid + " " + remoteip);
                throw new ServletException("Error: user entity not found");
            }

            boolean locked = (Boolean) user.getProperty("AccountLock");

            if (locked)
            {// Account is locked
                log.warning("Error: Cannot reset failed login Account is locked " + userid + " " + remoteip);
                throw new ServletException("Error: Cannot reset failed login Account is locked");
            }
            else
            {// Set fail login to 0
                user.setProperty("FailLogin", 0);
                datastore.put(txn, user);
                txn.commit();
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

}
