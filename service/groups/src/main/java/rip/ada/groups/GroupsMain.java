package rip.ada.groups;

public class GroupsMain {

    public static void main(final String[] args) {
        final Config config = Config.fromProperties();
        new GroupsService(config).start();
    }
}
