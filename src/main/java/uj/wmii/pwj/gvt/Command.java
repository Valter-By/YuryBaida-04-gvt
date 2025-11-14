package uj.wmii.pwj.gvt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public enum Command {
    INIT {
        @Override
        public void execute(Gvt gvt, String[] arguments) {
            if (gvt.isInitialized()) {
                gvt.getExitHandler().exit(10, "Current directory is already initialized.");
                return;
            }
            gvt.initialize();
            gvt.getExitHandler().exit(0,"Current directory initialized successfully.");
        }
    },
    ADD {
        @Override
        public void execute(Gvt gvt, String[] arguments) {
            if (!gvt.isInitialized()) {
                gvt.getExitHandler().exit(-2, "Current directory is not initialized. Please use \"init\" command to initialize.");
                return;
            }

            if (arguments.length == 0 || arguments[0].startsWith("-")) {
                gvt.getExitHandler().exit(20, "Please specify file to add.");
                return;
            }

            String fileName = arguments[0];
            String userMessage = extractMessage(arguments);
            Path filePath = Paths.get(fileName);

            if (!Files.exists(filePath)) {
                gvt.getExitHandler().exit(21, "File not found. File: " + fileName);
                return;
            }

            if (gvt.isFileAlreadyAdded(fileName)) {
                gvt.getExitHandler().exit(0,"File already added. File: " + fileName);
                return;
            }

            try {
                gvt.addFile(filePath);
                String baseMessage = "File added successfully. File: " + fileName;
                String fullMessage = userMessage == null ? baseMessage : userMessage;
                gvt.getVersionController().createNewVersion(fullMessage);
                gvt.getExitHandler().exit(0, baseMessage);
            } catch (IOException e) {
                gvt.getExitHandler().exit(22, "File cannot be added. See ERR for details. File: " + fileName);
                e.printStackTrace(System.err);
            }
        
        }

    }, 
    DETACH {
        @Override
        public void execute(Gvt gvt, String[] arguments) {
        if (!gvt.isInitialized()) {
                gvt.getExitHandler().exit(-2, "Current directory is not initialized. Please use \"init\" command to initialize.");
                return;
            }

        if (arguments.length == 0 || arguments[0].startsWith("-")) {
                gvt.getExitHandler().exit(30, "Please specify file to detach.");
                return;
            }

        String fileName = arguments[0];
        String userMessage = extractMessage(arguments);

        if (!gvt.isFileAlreadyAdded(fileName)) {
            gvt.getExitHandler().exit(0,"File is not added to gvt. File: " + fileName);
            return;
            }

        try {
            gvt.detachFile(fileName);
            String fullMessage = "File detached successfully. File: " + fileName;
            if (userMessage != null) fullMessage += "\n" + userMessage;
            gvt.getVersionController().createNewVersion(fullMessage);
            gvt.getExitHandler().exit(0,fullMessage);
        } catch (IOException e) {
            gvt.getExitHandler().exit(31, "File cannot be detached, see ERR for details. File: " + fileName);
            e.printStackTrace(System.err);
            }
        }

    }, 
    COMMIT {
        @Override
        public void execute(Gvt gvt, String[] arguments) {
            if (!gvt.isInitialized()) {
                gvt.getExitHandler().exit(-2, "Current directory is not initialized. Please use \"init\" command to initialize.");
                return;
            }

            if (arguments.length == 0 || arguments[0].startsWith("-")) {
                gvt.getExitHandler().exit(50, "Please specify file to commit.");
                return;
            }

            String fileName = arguments[0];
            String userMessage = extractMessage(arguments);
            Path filePath = Paths.get(fileName);

            if (!Files.exists(filePath)) {
                gvt.getExitHandler().exit(51, "File not found. File: " + fileName);
                return;
            }

            if (!gvt.isFileAlreadyAdded(fileName)) {
                gvt.getExitHandler().exit(0,"File is not added to gvt. File: " + fileName);
                return;
            }


            try {
                gvt.commitFile(filePath);
                String baseMessage = "File committed successfully. File: " + fileName;
                String fullMessage = userMessage == null ? baseMessage : userMessage;
                gvt.getVersionController().createNewVersion(fullMessage);
                gvt.getExitHandler().exit(0, baseMessage);
            } catch (IOException e) {
                gvt.getExitHandler().exit(52, "File cannot be committed, see ERR for details. File: " + fileName);
                e.printStackTrace(System.err);
            }
        }
    },
    HISTORY {
        @Override
        public void execute(Gvt gvt, String[] arguments) {
            if (!gvt.isInitialized()) {
                gvt.getExitHandler().exit(-2, "Current directory is not initialized. Please use \"init\" command to initialize.");
                return;
            }

            int limit = -1;
            if (arguments.length >= 2 && arguments[0].equals("-last")) {
                try {
                    limit = Integer.parseInt(arguments[1]) - 1;
                } catch (NumberFormatException ignored) {
                    limit = -1;
                }
            }
            try {
                String ans = gvt.printHistory(limit);
                gvt.getExitHandler().exit(0, ans);
            } catch (IOException e) {
                System.out.println("Underlying system problem. See ERR for details.");
                e.printStackTrace(System.err);
            }
        }
    },
    VERSION {
        @Override
        public void execute(Gvt gvt, String[] arguments) {
        if (!gvt.isInitialized()) {
                gvt.getExitHandler().exit(-2, "Current directory is not initialized. Please use \"init\" command to initialize.");
                return;
            }

            int version;
            if (arguments.length == 0) {
                version = gvt.getActiveVersion();
            } else {
                try {
                    version = Integer.parseInt(arguments[0]);
                } catch (NumberFormatException e) {
                    gvt.getExitHandler().exit(60, "Invalid version number: " + arguments[0] + ".");
                    return;
                }
            }

            if (!gvt.versionExists(version)) {
                gvt.getExitHandler().exit(60, "Invalid version number: " + version);
                return;
            }
            try {
                String ans = gvt.printVersionDetails(version);
                gvt.getExitHandler().exit(0, ans);
            } catch (IOException e) {
                System.out.println("Underlying system problem. See ERR for details.");
                e.printStackTrace(System.err);
            }
        }
    },
    CHECKOUT {
    @Override
    public void execute(Gvt gvt, String[] arguments) {
        if (!gvt.isInitialized()) {
            gvt.getExitHandler().exit(-2, "Current directory is not initialized. Please use \"init\" command to initialize.");
        }

        if (arguments.length == 0) {
            gvt.getExitHandler().exit(60, "Invalid version number: (none)");
        }

        int version;
        try {
            version = Integer.parseInt(arguments[0]);
        } catch (NumberFormatException e) {
                gvt.getExitHandler().exit(60, "Invalid version number: " + arguments[0]);
                return;
        }

        if (!gvt.versionExists(version)) {
            gvt.getExitHandler().exit(60, "Invalid version number: " + version);
            return;
        }

        try {
            gvt.checkoutVersion(version);
            gvt.getExitHandler().exit(0, "Checkout successful for version: " + version);
        } catch (IOException e) {
            gvt.getExitHandler().exit(-3, "Underlying system problem. See ERR for details.");
            e.printStackTrace(System.err);
        }
        }
    };
    
    public abstract void execute(Gvt gvt, String[] arguments);
    
    public static String extractMessage(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals("-m")) {
                String raw = args[i + 1];
                return raw.replaceAll("^\"|\"$", "").trim();
            }
        }
        return null;
    }
}
