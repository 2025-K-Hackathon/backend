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
            pb.directory(base); // python/ 폴더에서 실행
            pb.redirectErrorStream(false); // stderr 따로 분리!

            pb.environment().put("PYTHONIOENCODING", "utf-8");

            System.out.println("[PY] cwd=" + pb.directory());
            System.out.println("[PY] cmd=" + String.join(" ", cmd));

            Process p = pb.start();

            // JSON 입력 전달
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(p.getOutputStream(), StandardCharsets.UTF_8))) {
                w.write(input);
                w.flush();
            }

            // stdout: JSON 결과만 읽기
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    sb.append(line);  // 줄 바꿈 없이 JSON 통째로 붙이기
                }
            }

            // stderr: 로그만 출력
            try (BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = err.readLine()) != null) {
                    System.err.println("[PY-ERR] " + line);
                }
            }

            int code = p.waitFor();
            System.out.println("[PY] exit=" + code);
            System.out.println("[PY] output=" + sb);

            if (code != 0) {
                throw new RuntimeException("Python exited with " + code + "\n" + sb);
            }

            return sb.toString().trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to call python wrapper: " + wrapperFileName, e);
        }
    }
}
