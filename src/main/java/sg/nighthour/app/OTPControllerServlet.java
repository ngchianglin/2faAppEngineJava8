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

import sg.nighthour.crypto.TimeBaseOTP;
import sg.nighthour.crypto.CryptoUtil;
import sg.nighthour.app.AuthSession;
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
 * Servlet implementation class OTPControllerServlet
 */
@WebServlet("/otpctl")
public class OTPControllerServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(OTPControllerServlet.class.getName());


    /**
     * @see HttpServlet#HttpServlet()
     */
    public OTPControllerServlet()
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
       
        
        // Make sure it has a valid 2fa session from login page
        // userid2fa session attribute must be set
        AuthSession.check2FASession(request, response, "/index.jsp");
        
        HttpSession session = request.getSession(false);
        String userid = (String) session.getAttribute("userid2fa");
        // Remove the userid2fa attribute to prevent multiple submission attempts
        session.removeAttribute("userid2fa");

        String otpvalue = (String) request.getParameter("totp");

        if (otpvalue == null)
        {
            session.invalidate();
            log.warning("Error: Invalid otp value " + request.getRemoteAddr() + " " + userid);
            response.sendRedirect("/error.html");
        }

        String otpsecret = OTPDAO.getOTPSecret(userid, request.getRemoteAddr());

        String otpresult = TimeBaseOTP.generateOTP(CryptoUtil.hexStringToByteArray(otpsecret));
        otpsecret = null;

        if (otpresult == null)
        {
            session.invalidate();
            log.warning("Error: cannot generate otp " + request.getRemoteAddr() + " " + userid);
            response.sendRedirect("/error.html");
        }

        if (otpresult.equals(otpvalue))
        {// Correct OTP value

            session.invalidate();
            session = request.getSession(true);
            session.setAttribute("userid", userid);

            String custsession = "JSESSIONID=" + session.getId() + ";Path=/;Secure;HttpOnly;SameSite=Strict";
            response.setHeader("Set-Cookie", custsession);
            OTPDAO.resetFailLogin(userid, request.getRemoteAddr());
            response.sendRedirect("/success");
        }
        else
        {// Incorrect OTP value
            
            String remoteip = request.getRemoteAddr();

            // Update fail login count. If max fail login is exceeded, lock account
            OTPDAO.incrementFailLogin(userid, remoteip);
            
            //If account is locked reset session and redirect user
            if(OTPDAO.isAccountLocked(userid, remoteip))
            {
                session.invalidate();
                response.sendRedirect("/locked.html");
            }
            else
            {// Send back to the otp input page again
                session.setAttribute("userid2fa", userid);
                session.setAttribute("otperror", "");
                RequestDispatcher rd = request.getRequestDispatcher("otp.jsp");
                rd.forward(request, response);
            }

        }

    }

}
