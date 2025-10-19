package rip.ada.links;

public enum Sponsor {
    KEWBZUK("KewbzUK", "https://kewbz.co.uk/", "https://static.ukca.org/KewbzUK_Logo.png"),
    KEWBZIE("KewbzIE", "https://kewbz.ie/", "https://static.ukca.org/KewbzIE_Logo.png"),
    SPEEDCUBINGDOTORG("Speedcubing.org", "https://speedcubing.org/", "https://static.ukca.org/SpeedcubingDotOrg_Logo.png"),
    CUBOSS("Cuboss", "https://cuboss.com/", "https://static.ukca.org/Cuboss_Logo.png"),
    UTWISTCUBES("UTwistCubes", "https://www.utwistcubes.com/", "https://static.ukca.org/UTwistCubes_Logo.png"),
    RUBIKS("Rubik's", "https://www.rubiks.com/", "https://static.ukca.org/Rubiks_Logo.png");

    private final String name;
    private final String website;
    private final String logo;

    Sponsor(final String name, final String website, final String logo) {
        this.name = name;
        this.website = website;
        this.logo = logo;
    }

    public String getName() {
        return name;
    }

    public String getWebsite() {
        return website;
    }

    public String getLogo() {
        return logo;
    }
}
