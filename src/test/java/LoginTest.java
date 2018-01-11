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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import sg.nighthour.crypto.TimeBaseOTP;
import sg.nighthour.crypto.CryptoUtil;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class LoginTest
{
    private  String GECKODRIVER_PATH = TestConstants.GECKODRIVER_PATH;
    private String GECKODRIVER = TestConstants.GECKODRIVER;
    private String userid= TestConstants.TESTUSER;
    private String password= TestConstants.TESTPASSWORD;
    private String url = TestConstants.LOGIN;
    private String fullname = TestConstants.FULLNAME;

    @Before
    public void setUp() throws Exception
    {
        System.setProperty(GECKODRIVER,GECKODRIVER_PATH);
    }

    @Test(timeout=60000)
    public void test() throws InterruptedException
    {
        WebDriver driver = new FirefoxDriver();
        driver.get(url);
        Thread.sleep(TestConstants.SLEEP_INTERVAL);
        
        driver.findElement(By.name("userid")).sendKeys(userid);
        WebElement element = driver.findElement(By.name("password"));
        element.sendKeys(password);
        element.submit();
        
        Thread.sleep(TestConstants.SLEEP_INTERVAL);
        
        try
        {
            String otpresult = TimeBaseOTP.generateOTP(CryptoUtil.hexStringToByteArray(TestConstants.TESTOTPSECRET));
            WebElement otpelement = driver.findElement(By.name("totp"));
            otpelement.sendKeys(otpresult);
            otpelement.submit();
            
            Thread.sleep(TestConstants.SLEEP_INTERVAL);
            
            WebElement displayuser = driver.findElement(By.tagName("p"));
            String usertext = displayuser.getText();
            assertTrue("Login failed !",usertext.contains(fullname));
            
        }
        catch(NoSuchElementException e)
        {
            fail("Login failed !");
        }
        finally
        {
            Thread.sleep(TestConstants.SLEEP_INTERVAL);
            driver.close();
        }
        
    }

}
