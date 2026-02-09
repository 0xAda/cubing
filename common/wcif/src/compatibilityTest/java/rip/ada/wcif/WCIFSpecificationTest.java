package rip.ada.wcif;

import org.junit.jupiter.api.Test;
import rip.ada.wcif.util.AssertUnchanged;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class WCIFSpecificationTest {

    @Test
    public void shouldNotHaveChangedWCIFSpec() throws IOException, NoSuchAlgorithmException, InterruptedException {
        assertUnchanged("specification.md", "8f464d54c3ed95f62d13ddb87d410408841b25d6f36b15347ae61067d9558158");
    }

    private static void assertUnchanged(final String file, final String hash) throws IOException, NoSuchAlgorithmException, InterruptedException {
        AssertUnchanged.assertUnchanged("https://raw.githubusercontent.com/thewca/wcif/refs/heads/stable/", file, hash);
    }

}
