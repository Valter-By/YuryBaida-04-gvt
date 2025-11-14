package uj.wmii.pwj.gvt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;


public class Gvt {

    private final ExitHandler exitHandler;
    private final VersionController versionController;

    public Gvt(ExitHandler exitHandler) {
        this.exitHandler = exitHandler;
        this.versionController = new VersionController();
    }

    public ExitHandler getExitHandler() {
        return exitHandler;
    }

    public VersionController getVersionController() {
        return versionController;
    }

    public Path getGvtDir() {
        return versionController.getGvtDir();
    }

    public static void main(String... args) {
        Gvt gvt = new Gvt(new ExitHandler());
        gvt.mainInternal(args);
    }

    public void mainInternal(String... args) {
        if (args == null || args.length == 0) {
            exitHandler.exit(1, "Please specify command.");
            return;
        }
        try {
            Command command = Command.valueOf(args[0].toUpperCase());
            String[] arguments = new String[0];
            if (args.length > 1) {
                arguments = Arrays.copyOfRange(args, 1, args.length);
            }
            if (!command.equals(Command.INIT) && !this.isInitialized()) {
                exitHandler.exit(-2, "Current directory is not initialized. Please use init command to initialize.");
                return;
            }
            command.execute(this, arguments);

        } catch (IllegalArgumentException e) {
            exitHandler.exit(1, "Unknown command " + args[0] + ".");
        }
    }

    public void initialize() {
        try {
            versionController.initialize();
        } catch (Exception e) {
            System.out.println("Underlying system problem. See ERR for details.");
            e.printStackTrace(System.err);
        }
    }

    public boolean isInitialized() {
        return versionController.isInitialized();
    }

    public boolean isFileAlreadyAdded(String fileName) {
        try {
            return versionController.checkFile(fileName);
        } catch (Exception e) {
            System.out.println("Underlying system problem. See ERR for details.");
            e.printStackTrace(System.err);
            return false;
        }
    }

    public void addFile(Path filePath) throws IOException {
        versionController.addFile(filePath);
    }

    public void detachFile(String fileName) throws IOException {
        versionController.detachFile(fileName);
    }

    public void commitFile(Path filePath) throws IOException {
        versionController.commitFile(filePath);
    }

    public String printHistory(int limit) throws IOException {
        return versionController.printHistory(limit);
    }

    public int getActiveVersion() {
        try {
            Path activeFile = getGvtDir().resolve("ACTIVE");
            if (!Files.exists(activeFile)) return 0;
            return Integer.parseInt(Files.readString(activeFile).trim());
        } catch (IOException e) {
            return 0;
        }
    }

    public boolean versionExists(int version) {
        Path versionDir = getGvtDir().resolve("versions").resolve(String.valueOf(version));
        return Files.exists(versionDir) && Files.isDirectory(versionDir);
    }

    public String printVersionDetails(int version) throws IOException {
        return versionController.printVersionDetails(version);
    }

    public void checkoutVersion(int version) throws IOException {
        versionController.checkoutVersion(version);
    }
}
