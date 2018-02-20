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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet("/login")
public class LoginControllerServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(LoginControllerServlet.class.getName());

    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginControllerServlet()
    {
        super();

    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(false);
        if(session == null)
        {//no existing session
            log.warning("Error: Session not present redirect to login.jsp " + request.getRemoteAddr());
            response.sendRedirect("/index.jsp");
            return;
        }
        

        String userid = request.getParameter("userid");
        String password = request.getParameter("password");

        if (userid == null || password == null)
        {
            log.warning("Error: Invalid userid or password " + request.getRemoteAddr());
            response.sendRedirect("/index.jsp");
            return; 
        }

        if (LoginDAO.validateUser(userid, password, request.getRemoteAddr()))
        {
            password = null;
            //Prevent Session fixation, invalidate and assign a new session
            session.invalidate();
            session = request.getSession(true);
            session.setAttribute("userid2fa", userid);

            //Set the session id cookie with HttpOnly, secure and samesite flags
            String custsession = "JSESSIONID=" + session.getId() + ";Path=/;Secure;HttpOnly;SameSite=Strict";
            response.setHeader("Set-Cookie", custsession);

            RequestDispatcher rd = request.getRequestDispatcher("otp.jsp");
            rd.forward(request, response);
        }
        else
        {//If user credential is invalid, account is locked or user doesn't exist redirect back to login page
            
            if(LoginDAO.isAccountLocked(userid, request.getRemoteAddr()))
            {
                log.warning("Error: Account is locked " + userid  + " " + request.getRemoteAddr());
                
            }
            else
            {
              //For Log in functionality, don't log non-existent userid or 
              //invalid credentials to better secure the logs
              //An actual user may accidentially key their password in the userid field  
               log.warning("Error: Invalid userid or password " + request.getRemoteAddr());
            }
           
            response.sendRedirect("/index.jsp");
            
        }

    }

}
