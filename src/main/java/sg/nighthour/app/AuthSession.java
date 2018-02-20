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

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;


public class AuthSession
{

    private static final Logger log = Logger.getLogger(AuthSession.class.getName());
    
    
    /**
     * Validate if a session has been authenticated successfully and is still valid
     * Redirect to login page if session is not authenticated or invalid
     * 
     * @param req
     * @param resp
     * @throws IOException
     * @throws ServletException
     */
    
    /**
     * Validate if a session has been authenticated successfully and is still valid
     * Redirect to login page if session is not authenticated or invalid
     * 
     * @param req
     * @param resp
     * @return true if session is authenticated successfully, false otherwise
     * @throws IOException
     * @throws ServletException
     */
    public static boolean validate(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException
    {
        if (req == null || resp == null)
        {
            log.warning("Error: null arguments " + req.getRemoteAddr() );
            throw new ServletException("null arguments");
        }

      
        HttpSession sess = req.getSession(false);
        
        if(sess == null)
        {
            log.warning("Error: Null Session " + req.getRemoteAddr());
            resp.sendRedirect("/index.jsp");
            return false;
        }
        
       
        if (sess.getAttribute("userid") == null)
        { // not authenticated
            log.warning("Error: Invalid Authentication Session " + req.getRemoteAddr());
            resp.sendRedirect("/index.jsp");
            return false;
        }
        
        return true;
        
    }
    
    
   
    
    /**
     * Check if 2fa userid attribute is set. If it is not, redirect to specified error url
     * 
     * @param req
     * @param resp
     * @param redirecturl
     * @return true if 2fa userid attribute is properly set, false otherwise
     * @throws IOException
     * @throws ServletException
     */
    public static boolean check2FASession(HttpServletRequest req, HttpServletResponse resp, String redirecturl)
            throws IOException, ServletException
    {
        if (req == null || resp == null || redirecturl == null)
        {
            log.warning("Error: null arguments " + req.getRemoteAddr());
            throw new ServletException("null arguments");
        }

        HttpSession session = req.getSession(false);
        
        if(session == null)
        {
            log.warning("Error: Null Session " + req.getRemoteAddr());
            resp.sendRedirect(redirecturl);
            return false;
        }
       
        String userid2fa = (String) session.getAttribute("userid2fa");
        if (userid2fa == null)
        {
            log.warning("Error: 2FA not set " + req.getRemoteAddr() );
            resp.sendRedirect(redirecturl);
            return false;
        }
        
        return true; 

    }
}
