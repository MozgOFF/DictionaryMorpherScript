public class Extention {
    private String key;

    Extention(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }

    public String getExtention() {
        return key;
    }
    public void setExtention(String key) {
        this.key = key;
    }
}
