package com.example.sct_ad_2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText editTextTask;
    private Button buttonAdd;
    private RecyclerView recyclerViewTasks;

    private ArrayList<Task> taskList;
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        editTextTask = findViewById(R.id.editTextText);
        buttonAdd = findViewById(R.id.buttonAdd);
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);

        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTasks.setAdapter(taskAdapter);

        // Add button click
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskText = editTextTask.getText().toString().trim();
                if (!taskText.isEmpty()) {
                    taskList.add(new Task(taskText));
                    taskAdapter.notifyItemInserted(taskList.size() - 1);
                    editTextTask.setText("");
                    saveTasksToPrefs();
                }
            }
        });

        // Tap to Edit/Delete
        taskAdapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Choose action")
                        .setItems(new CharSequence[]{"Edit", "Delete"}, (dialog, which) -> {
                            if (which == 0) {
                                showEditDialog(position);
                            } else if (which == 1) {
                                taskList.remove(position);
                                taskAdapter.notifyItemRemoved(position);
                                saveTasksToPrefs();
                            }
                        })
                        .show();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load saved tasks
        loadTasksFromPrefs();
    }

    private void showEditDialog(int position) {
        Task task = taskList.get(position);
        EditText editText = new EditText(this);
        editText.setText(task.getTaskText());

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Edit Task")
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String updatedText = editText.getText().toString().trim();
                    if (!updatedText.isEmpty()) {
                        task.setTaskText(updatedText);
                        taskAdapter.notifyItemChanged(position);
                        saveTasksToPrefs();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveTasksToPrefs() {
        SharedPreferences prefs = getSharedPreferences("tasks", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(taskList);

        editor.putString("task_list", json);
        editor.apply();
    }

    private void loadTasksFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("tasks", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("task_list", null);

        Type type = new TypeToken<ArrayList<Task>>() {}.getType();
        ArrayList<Task> savedTasks = gson.fromJson(json, type);

        if (savedTasks != null) {
            taskList.addAll(savedTasks);
            taskAdapter.notifyDataSetChanged();
        }
    }
}