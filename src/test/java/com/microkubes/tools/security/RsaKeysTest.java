package com.microkubes.tools.security;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class RsaKeysTest {

    @Test
    public void testGenerateAndReadRsaKeyFile() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        File tmpFile = File.createTempFile("rsa-key","");

        tmpFile.deleteOnExit();

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair keyPair = kpg.generateKeyPair();


        PemWriter writer = new PemWriter(new FileWriter(tmpFile));

        writer.writeObject(new PemObject("RSA", keyPair.getPrivate().getEncoded()));
        writer.close();

        // -- read the RSA file


        PemReader reader = new PemReader(new FileReader(tmpFile));

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(reader.readPemObject().getContent());

        KeyFactory kf = KeyFactory.getInstance("RSA");

        PrivateKey privk = kf.generatePrivate(keySpec);
        PublicKey pubk = kf.generatePublic(keySpec);

        assert privk != null;
        assert  pubk != null;



    }
}
