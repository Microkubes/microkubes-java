package com.microkubes.tools.security;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Array;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class RsaKeysTest {

    @Test
    public void testGenerateAndReadRsaKeyFile() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        File tmpFile = File.createTempFile("rsa-key","");
        File pubkeyFile = File.createTempFile("pub-key", "");

        tmpFile.deleteOnExit();
        pubkeyFile.deleteOnExit();

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair keyPair = kpg.generateKeyPair();


        PemWriter writer = new PemWriter(new FileWriter(tmpFile));

        writer.writeObject(new PemObject("RSA PRIVATE KEY", keyPair.getPrivate().getEncoded()));
        writer.close();


        writer = new PemWriter(new FileWriter(pubkeyFile));

        writer.writeObject(new PemObject("RSA PUBLIC KEY", keyPair.getPublic().getEncoded()));
        writer.close();


        // -- read the RSA file


        PemReader reader = new PemReader(new FileReader(tmpFile));

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(reader.readPemObject().getContent());

        KeyFactory kf = KeyFactory.getInstance("RSA");

        PrivateKey privk = kf.generatePrivate(keySpec);

        reader = new PemReader(new FileReader(pubkeyFile));

        X509EncodedKeySpec pubkeySpecs = new X509EncodedKeySpec(reader.readPemObject().getContent());

        PublicKey pubk = kf.generatePublic(pubkeySpecs);

        assert privk != null;
        assert  pubk != null;

    }
}
