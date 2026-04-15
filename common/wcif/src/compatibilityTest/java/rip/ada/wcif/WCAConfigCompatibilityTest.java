package rip.ada.wcif;

import org.junit.jupiter.api.Test;
import rip.ada.wcif.util.AssertUnchanged;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class WCAConfigCompatibilityTest {

    @Test
    public void shouldNotHaveChangedCountries() throws IOException, NoSuchAlgorithmException, InterruptedException {
        assertUnchanged("lib/static_data/countries.real.json", "896f4ae4816e1e2652cbb1e4c9cd61288b63af77c6a3c5f23846565f434c036f");
        assertUnchanged("lib/static_data/countries.fictive.json", "5b66046327428df976668294fdd5a2fb5b1fc05a38ac07cfffd487814d26cfff");
    }

    @Test
    public void shouldNotHaveChangedRoundTypes() throws IOException, NoSuchAlgorithmException, InterruptedException {
        assertUnchanged("lib/static_data/round_types.json", "d52f35ee056461a461055db6f7d192cbe791115ec0effe5cb99e84695b7c4987");
    }

    @Test
    public void shouldNotHaveChangedPreferredFormats() throws IOException, NoSuchAlgorithmException, InterruptedException {
        assertUnchanged("lib/static_data/preferred_formats.json", "8c624eae39071f13be7db62091a3ec4a2b476bf728b1cb15e5329d27bc9a8343");
    }

    @Test
    public void shouldNotHaveChangedFormats() throws IOException, NoSuchAlgorithmException, InterruptedException {
        assertUnchanged("lib/static_data/formats.json", "5e0258c68bbd410ed08e18a5049e38d1c722c61c2617e30a527b9f34c864128b");
    }

    @Test
    public void shouldNotHaveChangedEvents() throws IOException, NoSuchAlgorithmException, InterruptedException {
        assertUnchanged("lib/static_data/events.json", "5ce0faa4739c3cd4ed2bc422bd38ea4a5f91b43fee62ef90984ad2dee0a7fec1");
    }

    @Test
    public void shouldNotHaveChangedContinents() throws IOException, NoSuchAlgorithmException, InterruptedException {
        assertUnchanged("lib/static_data/continents.json", "eecf8eeccedd57a89b029bcd3aa15b5079ceedc64bc929dbe30aceb9f6686089");
    }

    private static void assertUnchanged(final String file, final String hash) throws IOException, NoSuchAlgorithmException, InterruptedException {
        AssertUnchanged.assertUnchanged("https://raw.githubusercontent.com/thewca/worldcubeassociation.org/refs/heads/main/", file, hash);
    }

}
