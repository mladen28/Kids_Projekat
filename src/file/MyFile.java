package file;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class MyFile implements Serializable {

    @Serial
    private static final long serialVersionUID = 463426265374700139L;

    private final String path;
    private final String content;
    private final List<String> subFiles;
    private final int originalNode;
    private final String sharingType;
    private final int fileKey;

    public MyFile(String path, String content, List<String> subFiles, int originalNode, String sharingType, int fileKey) {
        this.path = path;
        this.content = content;
        this.subFiles = List.copyOf(subFiles);
        this.originalNode = originalNode;
        this.sharingType = sharingType;
        this.fileKey = fileKey;
    }

    public MyFile(String path, String content, int originalNode, String sharingType, int fileKey) {
        this(path, content, List.of(), originalNode, sharingType, fileKey);
    }

    public MyFile(String path, List<String> subFiles, int originalNode, int fileKey) {
        this(path, "", subFiles, originalNode, "public", fileKey);
    }

    public MyFile(MyFile myFile, String sharingType) {
        this(myFile.getPath(), myFile.getContent(), myFile.getSubFiles(), myFile.getOriginalNode(), sharingType, myFile.fileKey);
    }

    public String getPath() {
        return path;
    }


    public int getOriginalNode() {
        return originalNode;
    }

    public String getContent() {
        return content;
    }

    public List<String> getSubFiles() {
        return List.copyOf(subFiles);
    }

    public String getSharingType() {
        return sharingType;
    }

    public int getFileKey() {
        return fileKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyFile myFile = (MyFile) o;
        return hashCode() == myFile.hashCode();
    }
}