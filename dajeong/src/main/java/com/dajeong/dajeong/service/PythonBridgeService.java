package com.dajeong.dajeong.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@Service
public class PythonBridgeService {

    @Value("${dajeong.python.executable}")
    private String pythonExecutable;

    @Value("${dajeong.python.wrapper-dir}")
    private String wrapperDir;

    @Value("${dajeong.python.base-dir:./python}")
    private String baseDir;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String callWrapper(String wrapperFileName, Object jsonInput) {
        try {
            String input = objectMapper.writeValueAsString(jsonInput);

            File base = new File(baseDir).getAbsoluteFile();
            File wrapper = new File(wrapperDir, wrapperFileName).getAbsoluteFile();

            List<String> cmd = new ArrayList<>();
            cmd.add(pythonExecutable);
            cmd.add(wrapper.getPath());

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(base); // ★ python/ 폴더에서 실행
            pb.redirectErrorStream(true);
            pb.environment().put("PYTHONIOENCODING", "utf-8");

            System.out.println("[PY] cwd=" + pb.directory());
            System.out.println("[PY] cmd=" + String.join(" ", cmd));

            Process p = pb.start();

            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(p.getOutputStream(), StandardCharsets.UTF_8))) {
                w.write(input);
                w.flush();
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) sb.append(line).append("\n");
            }

            int code = p.waitFor();
            System.out.println("[PY] exit=" + code);
            System.out.println("[PY] output=\n" + sb);

            if (code != 0) throw new RuntimeException("Python exited with " + code + "\n" + sb);
            return sb.toString().trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to call python wrapper: " + wrapperFileName, e);
        }
    }
}
