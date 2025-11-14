package uj.wmii.pwj.gvt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class VersionController {

    private int head;
    private int active;
    private boolean isInitialized;
    private final Path gvtDir;

    private static final String GVT_DIR_NAME = ".gvt";
    private static final String VERSIONS_DIR = "versions";
    private static final String HEAD_FILE = "HEAD";
    private static final String ACTIVE_FILE = "ACTIVE";

    public VersionController() {
        this.gvtDir = Paths.get(GVT_DIR_NAME).toAbsolutePath();
        init();
    }

    public int getHead() {
        return head;
    }

    public int getActive() {
        return active;
    }

    public Path getGvtDir() {
        return gvtDir;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    private void init() {
        try {
            Path headFile = gvtDir.resolve(HEAD_FILE);
            Path activeFile = gvtDir.resolve(ACTIVE_FILE);
            if (Files.exists(headFile) && Files.exists(activeFile)) {
                head = Integer.parseInt(Files.readString(headFile).trim());
                active = Integer.parseInt(Files.readString(activeFile).trim());
                isInitialized = true;
            }
        } catch (Exception e) {
            System.out.println("Underlying system problem. See ERR for details.");
            e.printStackTrace(System.err);
        }
    }

    public void initialize() throws IOException {
        Files.createDirectory(gvtDir);

        Path versions = gvtDir.resolve(VERSIONS_DIR);
        Files.createDirectory(versions);

        Path version0 = versions.resolve("0");
        Files.createDirectory(version0);
        Files.createDirectory(version0.resolve("files"));

        Path messageFile = version0.resolve("message.txt");
        Files.writeString(messageFile, "GVT initialized.");

        Files.writeString(gvtDir.resolve(HEAD_FILE), "0");
        Files.writeString(gvtDir.resolve(ACTIVE_FILE), "0");

        this.head = 0;
        this.active = 0;
        this.isInitialized = true;
    }

    public boolean checkFile(String fileName) throws IOException {
        Path indexFile = gvtDir.resolve("index.txt");
        if (!Files.exists(indexFile)) {
            return false;
        }
        List<String> trackedFiles = Files.readAllLines(indexFile);
        return trackedFiles.contains(fileName);
    }

    public void addFile(Path filePath) throws IOException{
        String fileName = filePath.getFileName().toString();

        Path indexFile = gvtDir.resolve("index.txt");
        List<String> trackedFiles = Files.exists(indexFile)
            ? new ArrayList<>(Files.readAllLines(indexFile))
            : new ArrayList<>();

        trackedFiles.add(fileName);
        Files.write(indexFile, trackedFiles);
    }

    public void createNewVersion(String message) throws IOException {
        head++;
        Path versionDir = getGvtDir().resolve("versions").resolve(String.valueOf(head));
        Files.createDirectories(versionDir.resolve("files"));
        Path indexFile = getGvtDir().resolve("index.txt");
        if (Files.exists(indexFile)) {
            List<String> trackedFiles = Files.readAllLines(indexFile);
            for (String fileName : trackedFiles) {
                Path source = Paths.get(fileName);
                if (Files.exists(source)) {
                    Path target = versionDir.resolve("files").resolve(fileName);
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        Files.writeString(versionDir.resolve("message.txt"), message);
        Files.writeString(getGvtDir().resolve("HEAD"), String.valueOf(head));
        Files.writeString(getGvtDir().resolve("ACTIVE"), String.valueOf(head));
    }


    public void detachFile(String fileName) throws IOException {
        Path indexFile = getGvtDir().resolve("index.txt");
        if (!Files.exists(indexFile)) return;

        List<String> trackedFiles = new ArrayList<>(Files.readAllLines(indexFile));

        trackedFiles.remove(fileName);
        Files.write(indexFile, trackedFiles);
    }

    public void commitFile(Path filePath) throws IOException {

    }

    public String printHistory(int limit) throws IOException {

        int last = limit == -1 ? -1 : head - limit - 1;
        Path versionsDir = getGvtDir().resolve("versions");
        if (!Files.exists(versionsDir)) return "";
        StringBuilder sb = new StringBuilder();
        for (int dir = head; dir > last; dir--) {
            Path messageFile = versionsDir.resolve(String.valueOf(dir)).resolve("message.txt");
            String message = Files.exists(messageFile)
                ? Files.readString(messageFile).split("\n")[0]
                : "(no message)";
            sb.append(dir + ": " + message + "\n");
        }
        return sb.toString();
    }

    public String printVersionDetails(int version) throws IOException  {
        Path versionDir = getGvtDir().resolve("versions").resolve(String.valueOf(version));
        Path messageFile = versionDir.resolve("message.txt");

        String message = Files.exists(messageFile)
            ? Files.readString(messageFile)
            : "(no message)";
        return "Version: " + version + "\n" + message;        
    }

    public void checkoutVersion(int version) throws IOException{
        Path versionFilesDir = getGvtDir()
            .resolve("versions")
            .resolve(String.valueOf(version))
            .resolve("files");

        if (Files.isDirectory(versionFilesDir)) {
            List<File> files = List.of(versionFilesDir.toFile().listFiles());
            for (File file : files) {
                Path source = file.toPath();
                Path target = Paths.get(file.getName());
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }

    }
}
