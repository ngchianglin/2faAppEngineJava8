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

package sg.nighthour.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
//import javax.security.auth.DestroyFailedException;

import sg.nighthour.app.AppConstants;

public class CryptoUtil 
{
        public static final int PBE_ITERATION = AppConstants.PBE_ITERATION;
        public static final String PBE_KEYALGO = "PBKDF2WithHmacSHA256";
        public static final int PBE_KEYLENGTH = 256;
        public static final int SALT_SIZE = 32;

        /**
         * Derive an encryption key based on a password using PBE
         * 
         * @param pass Byte array of the password, the bytes will be zeroed after
         *            generating the key
         * @param salt Byte array of salt
         * @param count Number of iterations for key stretching
         * @return Derived key
         */
        public static byte[] getPasswordKey(char[] pass, byte[] salt, int count)
        {

            PBEKeySpec pbekeyspec = new PBEKeySpec(pass, salt, count, CryptoUtil.PBE_KEYLENGTH);
            SecretKey pbekey = null;
            try
            {
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(CryptoUtil.PBE_KEYALGO);
                pbekey = keyFactory.generateSecret(pbekeyspec);
                return pbekey.getEncoded();

            }
            catch (NoSuchAlgorithmException e)
            {
                System.err.println("key factory unable to get " + CryptoUtil.PBE_KEYALGO + " algo");
                e.printStackTrace();
            }
            catch (InvalidKeySpecException e)
            {
                System.err.println("key factory unable to generate secret key using keyspec");
                e.printStackTrace();

            }
            finally
            {
                zeroCharArray(pass);
                pbekeyspec.clearPassword();
                
                /* 
                As of Oracle jdk 1.8.0_152 , the default implementation of SecretKey destroy()
                throw DestroyFailedException. 
                Hence commenting this out and not destorying the SecretKey object in memory for now. 
                For future enhancement can consider using other java crypto library instead of the jdk default. 
                try 
                {
                    if (pbekey != null && !pbekey.isDestroyed())
                    {
                        pbekey.destroy();
                    }
                }
                catch (DestroyFailedException e)
                {
                    System.err.println("Unable to destroy secret key");
                    e.printStackTrace();
                }
                
                */

            }

            return null;
        }

        /**
         * Zero the elements in a char array
         * @param buf Character array to be zeroed.
         */
        public static void zeroCharArray(char[] buf)
        {
            for (int i = 0; i < buf.length; i++)
            {
                buf[i] = 0;
            }
        }
        
        /**
         * Zero the elements in a byte array
         * @param buf Byte array to be zeroed
         */
        public static void zeroByteArray(byte[] buf)
        {
            for(int i=0;i<buf.length;i++)
            {
                buf[i] = 0; 
            }
        }

        /**
         * Generate a random byte array for use as a password salt
         * 
         * @param size The number of random bytes
         * @return Byte array
         */
        public static byte[] generateRandomBytes(int size)
        {
            SecureRandom rand = new SecureRandom();
            byte[] ret = new byte[size];
            rand.nextBytes(ret);

            return ret;
        }

        /**
         * Converts a byte array to a hexadecimal string
         * 
         * @param buf Byte array to be converted.
         * @return hexdecimal string
         */
        public static String byteArrayToHexString(byte[] buf)
        {
            char[] hexarr = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

            StringBuffer strbuff = new StringBuffer(50);

            int mask = 0x000f;

            for (int i = 0; i < buf.length; i++)
            {
                int c = buf[i];
                int low = c & mask;
                int high = (c >>> 4) & mask;

                strbuff.append(hexarr[high]);
                strbuff.append(hexarr[low]);
            }

            return strbuff.toString();
        }

        
        /**
         * Convert a hexadecimal string to its a corresponding byte array
         *  
         * @param hexstr hexadecimal string 
         * @return byte array or null if an error occurs
         */
        public static byte[] hexStringToByteArray(String hexstr)
        {
            int len = hexstr.length();
            if( (len % 2) != 0)
            {
                return null;
            }
            
            byte ret[] = new byte[len / 2]; 
            int tmp = 0; 
            int index = 0;
            
            for(int i = 0; i < len ; i+=2)
            {
                char hex[] = new char[2];
                hex[0] = Character.toLowerCase(hexstr.charAt(i));
                hex[1] = Character.toLowerCase(hexstr.charAt(i+1));
                
                for(int j=0; j<2 ;j++)
                {
                    char c = hex[j];
                    switch(c)
                    {
                        case '0':
                            tmp = tmp | 0x00; 
                            break;
                        case '1':
                            tmp = tmp | 0x01;
                            break;
                        case '2':
                            tmp = tmp | 0x02;
                            break;
                        case '3':
                            tmp = tmp | 0x03;
                            break;
                        case '4':
                            tmp = tmp | 0x04;
                            break;
                        case '5':
                            tmp = tmp | 0x05;
                            break;
                        case '6':
                            tmp = tmp | 0x06;
                            break;
                        case '7':
                            tmp = tmp | 0x07;
                            break;
                        case '8':
                            tmp = tmp | 0x08;
                            break;
                        case '9':
                            tmp = tmp | 0x09;
                            break;
                        case 'a':
                            tmp = tmp | 0x0a;
                            break;
                        case 'b':
                            tmp = tmp | 0x0b;
                            break;
                        case 'c':
                            tmp = tmp | 0x0c;
                            break;
                        case 'd':
                            tmp = tmp | 0x0d;
                            break;
                        case 'e':
                            tmp = tmp | 0x0e;
                            break;
                        case 'f':
                            tmp = tmp | 0x0f;
                            break;
                        default:
                            return null;
                          
                    }
                    
                    if(j==0)
                    {
                       tmp = tmp << 4; 
                    }
                    
                }
                
                ret[index] = (byte) (tmp & 0x00ff); 
                index++;
                tmp = 0;
                
            }
           
            return ret;
            
        }
    

}
