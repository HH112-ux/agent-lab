package com.caspian.agent.devtools.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DevTools {

    /** 端口查询/杀进程*/
    public record PortRequest(int port) {}

    /**空请求*/
    public record EmptyRequest(String dummy){}

    /**Maven 命令执行*/
    public record  MavenRequest(String command, String projectPath) {}

    /**文件编码请求*/
    public record EncodingRequest(String directory, String fromEncoding) {}

    public String checkPort(PortRequest request) {
        try {
            ProcessBuilder pb = new ProcessBuilder("lsof", "-i", ":" + request.port());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            process.waitFor();
            
            if (output.isBlank()) {
                return "端口：" + request.port() + " 当前没有被任何进程占用";
            }
            return "端口：" + request.port() + " 占用情况：\n" + output;
        } catch (Exception e) {
            return "查询端口失败：" + e.getMessage();
        }
    }

    public String killPort(PortRequest request) {
        try {
            ProcessBuilder findPb = new ProcessBuilder("lsof", "-t", "-i", ":" + request.port());
            findPb.redirectErrorStream(true);
            Process findProcess = findPb.start();
            String pidOutput = new String(findProcess.getInputStream().readAllBytes()).trim();
            findProcess.waitFor();

            if (pidOutput.isBlank()) {
                return "端口 "  + request.port() + " 没有被占用，无需杀进程";
            }

            String [] pids = pidOutput.split("\n");
            StringBuilder result = new StringBuilder();
            for (String pid : pids) {
                ProcessBuilder killPb = new ProcessBuilder("kill", "-9", pid.trim());
                killPb.start().waitFor();
                result.append("已终止进程 PID：").append(pid.trim()).append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            return "杀掉端口进程失败：" + e.getMessage();
        }
    }

    public String listJavaProcesses(EmptyRequest request) {
        try {
            ProcessBuilder pb = new ProcessBuilder("jps", "-lv");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            if (exitCode != 0 || output.isBlank()) {
                return "当前没有运行中的Java进程";
            }
            return "当前运行的Java进程： \n" + output;
        } catch (IOException e) {
           return  listJavaProcessesFallback();
        } catch (InterruptedException e) {
           return listJavaProcessesFallback();
        }
    }

    private String listJavaProcessesFallback() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ps", "aux");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            process.waitFor();

            String javaProcs = Arrays.stream(output.split("\n"))
                    .filter(line -> line.contains("java") && !line.contains("grep"))
                    .collect(Collectors.joining("\n"));

            if (javaProcs.isBlank()) {
                return "当前没有运行中的Java进程";
            }
            return "当前运行的Java进程： \n" + javaProcs;
        } catch (Exception e) {
            return "查询Java进程失败:" + e.getMessage();
        }
    }

    public String runMaven(MavenRequest request) {
        try {
            String [] args = request.command.trim().split("\\s+");
            ArrayList<String> command = new ArrayList<>();
            command.add("mvn");
            command.addAll(Arrays.asList(args));

            ProcessBuilder pb = new ProcessBuilder(command);
            if (request.projectPath() != null && !request.projectPath.isBlank()) {
                pb.directory(new File(request.projectPath()));
            }
            pb.redirectErrorStream(true);

            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());

            int exitCode = process.waitFor();

            String status = exitCode == 0 ? "成功" : "失败(退出码: " + exitCode + ")";
            return "Maven命令 mvn " + request.command() + " 执行" + status + ": \n" + output;
         } catch (Exception e) {
            return "执行 Maven 命令出错：" + e.getMessage();
        }
    }

    public String convertEncoding(EncodingRequest request) {
        try {
            Path dir = Path.of(request.directory());
            if (!Files.exists(dir) || !Files.isDirectory((dir))) {
                return "目录不存在或者不是有效目录：" + request.directory();
            }

            Charset sourceCharset = Charset.forName(request.fromEncoding());
            ArrayList<String> converted = new ArrayList<>();
            ArrayList<String> failed = new ArrayList<>();

            try (var stream = Files.walk(dir)) {
                stream.filter(p -> p.toString().endsWith(".java"))
                        .forEach(p->{
                            try {
                                byte[] bytes = Files.readAllBytes(p);
                                String content = new String(bytes, sourceCharset);
                                Files.write(p, content.getBytes(StandardCharsets.UTF_8));
                                converted.add(dir.relativize(p).toString());
                            } catch (Exception e) {
                                failed.add(p.getFileName() + ": " + e.getMessage());
                            }
                        });
            }

            StringBuilder result = new StringBuilder();
            result.append("编码转换完成(").append(request.fromEncoding())
                    .append(" -> UTF-8), 共转换 ").append(converted.size()).append(" 个文件");
            if (!converted.isEmpty()) {
                result.append(":\n").append(String.join("\n", converted));
            }
            if (!failed.isEmpty()) {
                result.append("\n转换失败的文件：\n").append(String.join("\n", failed));
            }
            return result.toString();
        } catch (Exception e) {
            return "编码转换失败：" + e.getMessage();
        }
    }
}
