package com.example.academia_firebase;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String DB_URL = "";
    private DatabaseReference messagesRef;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth mAuth;

    private EditText inputMessage;
    private Button btnSend;
    private ListView listView;

    private ArrayAdapter<String> adapter;
    private final List<String> items = new ArrayList<>();
    private final List<Message> messages = new ArrayList<>();

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputMessage = findViewById(R.id.inputMessage);
        btnSend = findViewById(R.id.btnSend);
        listView = findViewById(R.id.listview);

        firebaseDatabase = FirebaseDatabase.getInstance(DB_URL);
        messagesRef = firebaseDatabase.getReference("messages");
        mAuth = FirebaseAuth.getInstance();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());

        loadMessages();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Message msg = messages.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Detalle de nota")
                    .setMessage("Usuario: " + msg.getUserName() + "\nFecha: " + sdf.format(new Date(msg.getInsertedAt())) +
                            "\nContenido: " + msg.getContent())
                    .setPositiveButton("Cerrar", null)
                    .show();
        });

        checkUser();
    }

    private void checkUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            btnSend.setEnabled(false);
            inputMessage.setEnabled(false);
            Toast.makeText(this, "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show();
        } else {
            btnSend.setEnabled(true);
            inputMessage.setEnabled(true);
        }
    }

    private void sendMessage() {
        String text = inputMessage.getText().toString().trim();
        if (text.isEmpty()) {
            inputMessage.setError("El texto está vacío");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show();
            return;
        }

        String key = messagesRef.push().getKey();
        long now = System.currentTimeMillis();

        Map<String, Object> msg = new HashMap<>();
        msg.put("content", text);
        msg.put("userId", user.getUid());
        msg.put("userName", user.getDisplayName());
        msg.put("insertedAt", now);

        if (key != null) {
            messagesRef.child(key).setValue(msg)
                    .addOnSuccessListener(unused -> inputMessage.setText(""))
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Ocurrió un error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void loadMessages() {
        messagesRef.orderByChild("insertedAt").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                Message msg = snapshot.getValue(Message.class);
                if (msg != null) {
                    messages.add(msg);
                    items.add(sdf.format(new Date(msg.getInsertedAt())) + " - " + msg.getContent());
                    adapter.notifyDataSetChanged();
                    listView.smoothScrollToPosition(items.size() - 1);
                }
            }

            @Override public void onChildChanged(DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(DataSnapshot snapshot) {}
            @Override public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(DatabaseError error) {
                Log.w("FB", "Listener cancelled: " + error.getMessage());
            }
        });
    }
}
