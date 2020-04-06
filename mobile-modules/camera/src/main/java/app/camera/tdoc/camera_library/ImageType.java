package app.camera.tdoc.camera_library;

/**
 * @author Sergey Zhmura
 */
public enum ImageType {
    HQ_SCAN(1, true),
    MQ_SCAN(2, true),
    SCENE_PHOTO(3, false);

    private int id;
    private boolean isForDoc;

    ImageType(int id, boolean isForDoc) {
        this.id = id;
        this.isForDoc = isForDoc;
    }

    public static ImageType getById(int id) {
        for (ImageType imageType : values()) {
            if (imageType.getId() == id) {
                return imageType;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public boolean isForDoc() {
        return isForDoc;
    }
}
