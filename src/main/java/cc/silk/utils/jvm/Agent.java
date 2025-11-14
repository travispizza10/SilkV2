package cc.silk.utils.jvm;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.net.URL;

public class Agent {
    public static void premain(String agentArgs, Instrumentation inst) {
        try {
            injectMod();
        } catch (Exception e) {
            System.err.println("Failed to inject mod via agent: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static void injectMod() throws Exception {
        String jarPath = getCurrentJarPath();
        if (jarPath == null) {
            throw new Exception("Could not determine JAR file path");
        }

        File jarFile = new File(jarPath);
        if (!jarFile.exists()) {
            throw new Exception("JAR file does not exist: " + jarPath);
        }

        String fabricAddMods = System.getProperty("fabric.addMods");

        if (fabricAddMods == null || fabricAddMods.isEmpty()) {
            System.setProperty("fabric.addMods", jarPath);
        } else {
            System.setProperty("fabric.addMods", fabricAddMods + File.pathSeparator + jarPath);
        }

        System.out.println("[Silk Agent] Successfully injected mod: " + jarPath);
    }

    private static String getCurrentJarPath() {
        try {
            URL location = Agent.class.getProtectionDomain().getCodeSource().getLocation();

            String urlString = location.toString();
            URI uri;
            if (urlString.startsWith("jar:file:")) {
                int bangIndex = urlString.indexOf('!');
                if (bangIndex >= 0) {
                    urlString = urlString.substring(9, bangIndex);
                } else {
                    urlString = urlString.substring(9);
                }
                uri = new URI(urlString);
            } else {
                uri = location.toURI();
            }
            File file = new File(uri);

            String absolutePath = file.getAbsolutePath();
            System.out.println("[Silk Agent] Detected JAR path: " + absolutePath);

            return absolutePath;
        } catch (Exception e) {
            System.err.println("[Silk Agent] Error getting JAR path: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}