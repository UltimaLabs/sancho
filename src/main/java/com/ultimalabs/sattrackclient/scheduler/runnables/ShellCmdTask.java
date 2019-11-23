package com.ultimalabs.sattrackclient.scheduler.runnables;

import com.ultimalabs.sattrackclient.shellexec.service.ShellExecService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Shell command execution
 */
@Slf4j
@RequiredArgsConstructor
public class ShellCmdTask implements Runnable {

    private final String shellCommand;
    private final ShellExecService shellExecService;

    @Override
    public void run() {

        log.info("Started ShellCmdTask on thread {}", Thread.currentThread().getName());

        if (!shellCommand.equals("")) {
            shellExecService.execShellCmd(shellCommand);
        }

    }
}
