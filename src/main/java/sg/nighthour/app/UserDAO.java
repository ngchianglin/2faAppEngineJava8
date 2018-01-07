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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class UserDAO
{
    private static final Logger log = Logger.getLogger(UserDAO.class.getName());

    /**
     * Retrieves the username for a userid
     * 
     * @param userid
     * @param remoteip
     * @return username if successful, null if there is an error
     */
    public static String getUserName(String userid, String remoteip)
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key k1 = KeyFactory.createKey(AppConstants.USER_KIND, userid);
        
        Entity user = null;
        try
        {
            user = datastore.get(k1);
        }
        catch (EntityNotFoundException e)
        {
            log.warning("Error: user entity not found " + userid + " " + remoteip);
            return null;
        }
        
        String firstname = (String)user.getProperty("First Name");
        String lastname = (String)user.getProperty("Last Name");
        
        return lastname + " " + firstname; 
        
    }
    
    
}
