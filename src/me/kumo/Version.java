package me.kumo;

public class Version {
    public static final Version CURRENT = new Version(Version.class.getPackage().getImplementationVersion() == null ? "in-dev" : Version.class.getPackage().getImplementationVersion());
    private final String version;
    private final boolean string;

    public Version(String version) {
        if (version == null) throw new IllegalArgumentException("Version can not be null");
        this.string = !version.matches("v?[0-9]+(\\.[0-9]+)*(?:-(.+))?");
        this.version = version;
    }

    public final String get() {
        return this.version;
    }
}