package com.example.todolist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var toDoAdapter: ToDoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toDoAdapter = ToDoAdapter(mutableListOf())

        val rvTodoItems : RecyclerView = findViewById(R.id.rvToDoList)
        rvTodoItems.adapter = toDoAdapter
        rvTodoItems.layoutManager = LinearLayoutManager(this)

        val btnAddTodo : Button = findViewById(R.id.btnAddToDo)
        val etTodoTitle : EditText = findViewById(R.id.etToDoList)
        btnAddTodo.setOnClickListener {
            val todoTitle = etTodoTitle.text.toString()
            if(todoTitle.isNotEmpty()) {
                val todo = Todo(todoTitle)
                toDoAdapter.addToDo(todo)
                etTodoTitle.text.clear()
            }
        }
        val btnDeleteDoneTools : Button = findViewById(R.id.btnDeleteToDo)
        btnDeleteDoneTools.setOnClickListener {
            toDoAdapter.deleteDoneToDos()
        }

    }
}