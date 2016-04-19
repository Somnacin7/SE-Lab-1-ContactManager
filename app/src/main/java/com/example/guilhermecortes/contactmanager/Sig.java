package com.example.guilhermecortes.contactmanager;

import android.content.Context;

import java.io.*;
import java.security.*;
import java.security.spec.*;

public class Sig {

    public static void Sign(String fileName, Context context)
    {

        try
        {
            // Get key generator
            KeyPairGenerator keyGen =  KeyPairGenerator.getInstance("DSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGen.initialize(1024,random);

            // Get key pair
            KeyPair pair = keyGen.generateKeyPair();
            PrivateKey privKey = pair.getPrivate();
            PublicKey pub = pair.getPublic();

            // Get signature obj
            Signature dsa = Signature.getInstance("SHA1withDSA");
            dsa.initSign(privKey);

            // Supply data to signature
            FileInputStream fis = context.openFileInput(fileName);
            BufferedInputStream bufin = new BufferedInputStream(fis);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bufin.read(buffer)) >= 0) {
                dsa.update(buffer, 0, len);
            };
            bufin.close();

            // Generate signature
            byte[] realSig = dsa.sign();

            /* save the signature in a file */
            FileOutputStream sigfos =  context.openFileOutput("sig", Context.MODE_PRIVATE);
            sigfos.write(realSig);
            sigfos.close();

            /* save the public key in a file */
            byte[] key = pub.getEncoded();
            FileOutputStream keyfos = context.openFileOutput("suepk",Context.MODE_PRIVATE);
            keyfos.write(key);
            keyfos.close();

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean verSig(String fileName, Context context)
    {
        try{
            // load key
            FileInputStream keyfis = context.openFileInput("suepk");
            byte[] encKey = new byte[keyfis.available()];
            keyfis.read(encKey);

            keyfis.close();

            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);

            KeyFactory keyFactory = KeyFactory.getInstance("DSA");
            PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);

            // load sig
            FileInputStream sigfis = context.openFileInput("sig");
            byte[] sigToVerify = new byte[sigfis.available()];
            sigfis.read(sigToVerify );

            sigfis.close();

            Signature sig = Signature.getInstance("SHA1withDSA");
            sig.initVerify(pubKey);

            // load and verify data
            FileInputStream datafis = context.openFileInput(fileName);;
            BufferedInputStream bufin = new BufferedInputStream(datafis);

            byte[] buffer = new byte[1024];
            int len;
            while (bufin.available() != 0) {
                len = bufin.read(buffer);
                sig.update(buffer, 0, len);
            };

            bufin.close();

            boolean verifies = sig.verify(sigToVerify);

            System.out.println("signature verifies: " + verifies);

            return verifies;

        } catch (Exception e) {
            System.err.println("Caught exception " + e.toString());
        };
        return false;
    }
}
