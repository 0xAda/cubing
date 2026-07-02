package rip.ada.wcif;

import org.junit.jupiter.api.Test;
import rip.ada.wcif.util.AssertUnchanged;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class WCIFSpecificationTest {

    @Test
    public void shouldNotHaveChangedWCIFSpec() throws IOException, NoSuchAlgorithmException, InterruptedException {
        assertUnchanged("specification.md", "772dadebd2b382c14d4754df4ebf4ae2c8a54c65716a98a8fd1a4422f6a10b0f");
    }

    private static void assertUnchanged(final String file, final String hash) throws IOException, NoSuchAlgorithmException, InterruptedException {
        AssertUnchanged.assertUnchanged("https://raw.githubusercontent.com/thewca/wcif/refs/heads/latest/", file, hash);
    }

}
