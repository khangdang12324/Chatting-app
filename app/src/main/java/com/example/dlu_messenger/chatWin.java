package com.example.dlu_messenger;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatWin extends AppCompatActivity {

    String reciverimg, reciverUid, reciverName, SenderUID;
    CircleImageView profile;
    TextView reciverNName;
    FirebaseDatabase database;
    FirebaseAuth firebaseAuth;
    CardView sendbtn;
    EditText textmsg;

    RecyclerView messageRecyclerView;
    ArrayList<msgModelclass> messagesArrayList;
    messagesAdpter mmessagesAdpter;

    String senderImg = "", reciverIImg = "", roomId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_win);

        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Nhận dữ liệu từ Intent
        reciverName = getIntent().getStringExtra("nameeee");
        reciverimg = getIntent().getStringExtra("reciverImg");
        reciverUid = getIntent().getStringExtra("uid");

        messagesArrayList = new ArrayList<>();

        sendbtn = findViewById(R.id.sendbtnn);
        textmsg = findViewById(R.id.textmsg);
        reciverNName = findViewById(R.id.recivername);
        profile = findViewById(R.id.profileimgg);
        messageRecyclerView = findViewById(R.id.msgadpter);

        // Setup RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(linearLayoutManager);

        // Load ảnh và tên người nhận
        Picasso.get().load(reciverimg).into(profile);
        reciverNName.setText(reciverName);

        // Xác định UID người dùng hiện tại và roomId chung
        SenderUID = firebaseAuth.getUid();
        roomId = (SenderUID.compareTo(reciverUid) < 0) ? SenderUID + reciverUid : reciverUid + SenderUID;

        // Tham chiếu ảnh người gửi từ Firebase
        DatabaseReference userReference = database.getReference().child("user").child(SenderUID);
        DatabaseReference chatReference = database.getReference().child("chats").child(roomId).child("messages");

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("profilepic").exists()) {
                    senderImg = snapshot.child("profilepic").getValue(String.class);
                }

                reciverIImg = reciverimg;

                // Khởi tạo adapter khi có đủ ảnh
                mmessagesAdpter = new messagesAdpter(chatWin.this, messagesArrayList, senderImg, reciverIImg);
                messageRecyclerView.setAdapter(mmessagesAdpter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Lắng nghe tin nhắn real-time
        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    msgModelclass messages = dataSnapshot.getValue(msgModelclass.class);
                    messagesArrayList.add(messages);
                }
                if (mmessagesAdpter != null) {
                    mmessagesAdpter.notifyDataSetChanged();
                    messageRecyclerView.scrollToPosition(messagesArrayList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Gửi tin nhắn
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = textmsg.getText().toString().trim();
                if (message.isEmpty()) {
                    Toast.makeText(chatWin.this, "Nhập tin nhắn trước đã", Toast.LENGTH_SHORT).show();
                    return;
                }

                textmsg.setText("");
                Date date = new Date();
                msgModelclass newMessage = new msgModelclass(message, SenderUID, date.getTime());

                database.getReference().child("chats").child(roomId).child("messages")
                        .push().setValue(newMessage)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                messageRecyclerView.scrollToPosition(messagesArrayList.size() - 1);
                            }
                        });
            }
        });
    }
}
