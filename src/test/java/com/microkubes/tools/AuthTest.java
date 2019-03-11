package com.microkubes.tools;

import com.microkubes.tools.security.Auth;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class AuthTest {

    @Test
    public void testAuthObj(){
        Auth auth = new Auth(
                "john.doe@example.com",
                "abcdef",
                Arrays.asList(new String[]{"user", "admin"}),
                Arrays.asList(new String[]{"testorg"}),
                Arrays.asList(new String[]{"testns"}));
        Assert.assertSame("john.doe@example.com", auth.getEmail());
        Assert.assertSame("abcdef", auth.getUserId());
        Assert.assertArrayEquals(new String[]{"user", "admin"}, auth.getRoles().toArray());
    }
}
