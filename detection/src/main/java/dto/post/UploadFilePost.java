package dto.post;

public class UploadFilePost {

    String fileName;

    public UploadFilePost() {
    }

    public UploadFilePost(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
