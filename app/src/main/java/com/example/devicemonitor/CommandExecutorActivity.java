package com.example.devicemonitor;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandExecutorActivity extends AppCompatActivity {

    private TextView tvOutput;
    private LinearProgressIndicator progressBar;
    private TextInputEditText etCommand;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command_executor);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tvOutput = findViewById(R.id.tvOutput);
        progressBar = findViewById(R.id.progressBar);
        etCommand = findViewById(R.id.etCommand);
        View btnRun = findViewById(R.id.btnRun);

        // Predefined Chip listeners
        findViewById(R.id.chipThirdParty).setOnClickListener(v -> runCommand("pm list packages -3"));
        findViewById(R.id.chipSystem).setOnClickListener(v -> runCommand("pm list packages -s"));
        findViewById(R.id.chipAll).setOnClickListener(v -> runCommand("pm list packages"));
        findViewById(R.id.chipInstaller).setOnClickListener(v -> runCommand("pm list packages -i"));

        // Custom command run button
        btnRun.setOnClickListener(v -> {
            String command = etCommand.getText().toString().trim();
            if (!command.isEmpty()) {
                hideKeyboard();
                runCommand(command);
            } else {
                Toast.makeText(this, "Please enter a command", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle "Enter/Go" key on keyboard
        etCommand.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                btnRun.performClick();
                return true;
            }
            return false;
        });

    }

    private void runCommand(String command) {
        progressBar.setVisibility(View.VISIBLE);
        tvOutput.setText("Executing: " + command + "...");
        
        executorService.execute(() -> {
            String result = executeCommand(command);
            mainHandler.post(() -> {
                tvOutput.setText(result);
                progressBar.setVisibility(View.GONE);
            });
        });
    }

    private String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            reader.close();
            process.waitFor();

            if (output.length() == 0) {
                return "Command executed but returned no output.";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        return output.toString();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
