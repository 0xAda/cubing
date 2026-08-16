package rip.ada.wcif;

import org.junit.jupiter.api.Test;
import rip.ada.wcif.util.AssertUnchanged;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class WCIFSpecificationTest {

    @Test
    public void shouldNotHaveChangedWCIFSpec() throws IOException, NoSuchAlgorithmException, InterruptedException {
        assertUnchanged("specification.md", "0a702af3f4e07b47002be0f078346f8d1f8fb8d51afde75713607d1e2afb5b22");
    }

    private static void assertUnchanged(final String file, final String hash) throws IOException, NoSuchAlgorithmException, InterruptedException {
        AssertUnchanged.assertUnchanged("https://raw.githubusercontent.com/thewca/wcif/refs/heads/latest/", file, hash);
    }

}
