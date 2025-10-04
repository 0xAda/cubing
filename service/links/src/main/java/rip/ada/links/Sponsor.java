package rip.ada.links;

public enum Sponsor {
    KEWBZUK("KewbzUK", "https://kewbz.co.uk/", "https://cdn.shopify.com/s/files/1/0855/0152/files/Kewbz_Global_take_2.png?v=1744303208"),
    KEWBZIE("KewbzIE", "https://kewbz.ie/", "https://cdn.shopify.com/s/files/1/0855/0152/files/KewbzIE_PNG_cd175ebc-6576-4049-b02f-0e2913ec60c9.png?v=1732116274"),
    SPEEDCUBINGDOTORG("Speedcubing.org", "https://speedcubing.org/", "https://speedcubing.org/cdn/shop/files/Screenshot_2024-08-14_at_16-32-34_Speedcubing_-_Speedcubing.pdf_c413c976-04df-4368-8405-e42872ea8ba9.png?v=1723649761"),
    CUBOSS("Cuboss", "https://cuboss.com/", "https://cuboss.com/wp-content/uploads/2020/01/Cubosslogga16.4-1024x191.png"),
    UTWISTCUBES("UTwistCubes", "https://www.utwistcubes.com/", "https://www.utwistcubes.com/images/logo_w_utwistcubes2.png"),
    RUBIKS("Rubik's", "https://www.rubiks.com/", "https://cdn.prod.website-files.com/65849828bf90a121b81dc3a5/65c26184c71d3dd48e4cfaa9_Rubiks-logo.png");

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
