public class Separator {
    private String key;

    Separator(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }

    public String getSeparator() {
        return key;
    }
    public void setSeparator(String key) {
        this.key = key;
    }
}
