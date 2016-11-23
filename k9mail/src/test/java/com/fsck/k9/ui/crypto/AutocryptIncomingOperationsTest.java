package com.fsck.k9.ui.crypto;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.internet.BinaryTempFileBody;
import com.fsck.k9.mail.internet.MimeMessage;
import com.fsck.k9.ui.crypto.AutocryptIncomingOperations.AutocryptHeader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class AutocryptIncomingOperationsTest {
    AutocryptIncomingOperations autocryptOperations = new AutocryptIncomingOperations();

    @Before
    public void setUp() throws Exception {
        BinaryTempFileBody.setTempDirectory(RuntimeEnvironment.application.getCacheDir());
    }

    // Test cases taken from: https://github.com/mailencrypt/autocrypt/tree/master/src/tests/data

    @Test
    public void getValidAutocryptHeader__withNoHeader__shouldReturnNull() throws Exception {
        MimeMessage message = parseFromResource("autocrypt/no_autocrypt.eml");

        AutocryptHeader autocryptHeader = autocryptOperations.getValidAutocryptHeader(message);

        assertNull(autocryptHeader);
    }

    @Test
    public void getValidAutocryptHeader__withBrokenBase64__shouldReturnNull() throws Exception {
        MimeMessage message = parseFromResource("autocrypt/rsa2048-broken-base64.eml");

        AutocryptHeader autocryptHeader = autocryptOperations.getValidAutocryptHeader(message);

        assertNull(autocryptHeader);
    }

    @Test
    public void getValidAutocryptHeader__withSimpleAutocrypt() throws Exception {
        MimeMessage message = parseFromResource("autocrypt/rsa2048-simple.eml");

        AutocryptHeader autocryptHeader = autocryptOperations.getValidAutocryptHeader(message);

        assertNotNull(autocryptHeader);
        assertEquals("alice@testsuite.autocrypt.org", autocryptHeader.to);
        assertEquals(0, autocryptHeader.parameters.size());
        assertEquals(1225, autocryptHeader.keyData.length);
    }

    @Test
    public void getValidAutocryptHeader__withExplicitType() throws Exception {
        MimeMessage message = parseFromResource("autocrypt/rsa2048-explicit-type.eml");

        AutocryptHeader autocryptHeader = autocryptOperations.getValidAutocryptHeader(message);

        assertNotNull(autocryptHeader);
        assertEquals("alice@testsuite.autocrypt.org", autocryptHeader.to);
        assertEquals(0, autocryptHeader.parameters.size());
    }

    @Test
    public void getValidAutocryptHeader__withUnknownType__shouldReturnNull() throws Exception {
        MimeMessage message = parseFromResource("autocrypt/unknown-type.eml");

        AutocryptHeader autocryptHeader = autocryptOperations.getValidAutocryptHeader(message);

        assertNull(autocryptHeader);
    }

    @Test
    public void getValidAutocryptHeader__withUnknownCriticalHeader__shouldReturnNull() throws Exception {
        MimeMessage message = parseFromResource("autocrypt/rsa2048-unknown-critical.eml");

        AutocryptHeader autocryptHeader = autocryptOperations.getValidAutocryptHeader(message);

        assertNull(autocryptHeader);
    }

    @Test
    public void getValidAutocryptHeader__withUnknownNonCriticalHeader() throws Exception {
        MimeMessage message = parseFromResource("autocrypt/rsa2048-unknown-non-critical.eml");

        AutocryptHeader autocryptHeader = autocryptOperations.getValidAutocryptHeader(message);

        assertNotNull(autocryptHeader);
        assertEquals("alice@testsuite.autocrypt.org", autocryptHeader.to);
        assertEquals(1, autocryptHeader.parameters.size());
        assertEquals("ignore", autocryptHeader.parameters.get("_monkey"));
    }

    private MimeMessage parseFromResource(String resourceName) throws IOException, MessagingException {
        InputStream inputStream = readFromResourceFile(resourceName);
        return MimeMessage.parseMimeMessage(inputStream, false);
    }

    private InputStream readFromResourceFile(String name) throws FileNotFoundException {
        return new FileInputStream(RuntimeEnvironment.application.getPackageResourcePath() + "/src/test/resources/" + name);
    }


}