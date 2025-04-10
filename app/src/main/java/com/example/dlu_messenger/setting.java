package com.example.dlu_messenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class setting extends AppCompatActivity {

    private ImageView setprofile;
    private EditText setname, setstatus;
    private Button donebut;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private Uri setImageUri;
    private ProgressDialog progressDialog;

    private String email, password, currentImageUrl; // Lưu ảnh cũ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        setprofile = findViewById(R.id.settingprofile);
        setname = findViewById(R.id.settingname);
        setstatus = findViewById(R.id.settingstatus);
        donebut = findViewById(R.id.donebutt);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);

        DatabaseReference reference = database.getReference().child("user").child(auth.getUid());

        // Tải thông tin người dùng
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                email = snapshot.child("mail").getValue(String.class);
                password = snapshot.child("password").getValue(String.class);
                String name = snapshot.child("userName").getValue(String.class);
                currentImageUrl = snapshot.child("profilepic").getValue(String.class); // Lưu ảnh cũ
                String status = snapshot.child("status").getValue(String.class);

                setname.setText(name);
                setstatus.setText(status);

                if (currentImageUrl != null && !currentImageUrl.equals("default_user")) {
                    Picasso.get().load(currentImageUrl).into(setprofile);
                } else {
                    setprofile.setImageResource(R.drawable.default_user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(setting.this, "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        });

        // Chọn ảnh mới
        setprofile.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
        });

        // Lưu dữ liệu
        donebut.setOnClickListener(view -> {
            String name = setname.getText().toString().trim();
            String status = setstatus.getText().toString().trim();

            if (name.isEmpty() || status.isEmpty()) {
                Toast.makeText(setting.this, "Name and status cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.show();

            // Nếu chọn ảnh mới thì dùng ảnh mới, không thì dùng ảnh cũ
            String imageUrl = (setImageUri != null) ? setImageUri.toString()
                    : (currentImageUrl != null ? currentImageUrl : "default_user");

            Users users = new Users(auth.getUid(), name, email, password, imageUrl, status);

            reference.setValue(users)
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(setting.this, "Saved successfully!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(setting.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(setting.this, "Failed to save data", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(setting.this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    // Nhận ảnh từ thư viện
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK && data != null) {
            setImageUri = data.getData();
            setprofile.setImageURI(setImageUri);
        }
    }
}
