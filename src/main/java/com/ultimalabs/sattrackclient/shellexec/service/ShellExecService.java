package com.ultimalabs.sattrackclient.shellexec.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Executes shell commands
 */
@Slf4j
@Service
public class ShellExecService {

    /**
     * Executes a shell command
     *
     * @param cmd command to execute
     * @return command exit code
     */
    public Integer execShellCmd(String cmd) {

        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows()) {
            builder.command("cmd.exe", "/c", cmd);
        } else {
            builder.command("sh", "-c", cmd);
        }

        Process process;

        try {

            Consumer<String> outputConsumer = log::info;

            process = builder.start();
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), outputConsumer);
            Executors.newSingleThreadExecutor().submit(streamGobbler);
            Integer exitCode = process.waitFor();
            log.info("Executed shell command '" + cmd + "' with exit code: " + exitCode);
            return exitCode;

        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }

        return null;

    }

    /**
     * Checks whether we're running on Windows
     *
     * @return true if we are, indeed, running on windows
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

    /**
     * Hook into the input and output streams of our process
     */
    @RequiredArgsConstructor
    private static class StreamGobbler implements Runnable {

        private final InputStream inputStream;
        private final Consumer<String> consumer;

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
        }
    }
}
