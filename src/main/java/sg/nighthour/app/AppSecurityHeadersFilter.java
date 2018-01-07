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
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet Filter implementation class SecureHeadersFilter
 */
@WebFilter("/*")
public class AppSecurityHeadersFilter implements Filter
{

    /**
     * Default constructor.
     */
    public AppSecurityHeadersFilter()
    {

    }

    /**
     * @see Filter#destroy()
     */
    public void destroy()
    {

    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {

        ResponseWrapper resp = new ResponseWrapper((HttpServletResponse) response);
        // pass the request along the filter chain
        chain.doFilter(request, resp);
    }

    /**
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig fConfig) throws ServletException
    {

    }

    private class ResponseWrapper extends HttpServletResponseWrapper
    {
        public ResponseWrapper(HttpServletResponse response)
        {
            super(response);
            /*
             * Set the security headers. Note these headers may be overwritten by the
             * servlet/resource entity down the chain
             */
            response.setHeader("Strict-Transport-Security", "max-age=31536000;includeSubDomains");
            response.setHeader("Content-Security-Policy", "default-src 'self';");
            response.setHeader("X-Frame-Options", "DENY");
            response.setHeader("X-XSS-Protection", "1; mode=block");
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("Referrer-Policy", "no-referrer");
            response.setHeader("Cache-Control", "no-store");
        }
    }

}
