import pyodbc
import tkinter as tk
from tkinter import ttk, messagebox

# Kết nối SQL Server
conn_str = (
    "DRIVER={ODBC Driver 17 for SQL Server};"
    "SERVER=LAPTOP-KHANGDAN;"  # Thay tên server của bạn
    "DATABASE=QLMonAn;"
    "Trusted_Connection=yes;"
)

try:
    conn = pyodbc.connect(conn_str)
    cursor = conn.cursor()
except Exception as e:
    messagebox.showerror("Lỗi", f"Lỗi kết nối SQL Server: {e}")
    exit()

# Hàm tải danh sách nhóm món ăn
def load_groups():
    cursor.execute("SELECT DISTINCT Nhom FROM MonAn")
    groups = [row[0] for row in cursor.fetchall()]
    return groups

# Hàm tải danh sách món ăn theo nhóm
def load_data(group=None):
    for row in tree.get_children():
        tree.delete(row)

    query = "SELECT MaMonAn, TenMonAn, DonViTinh, DonGia, Nhom FROM MonAn"
    if group:
        query += " WHERE Nhom = ?"
        cursor.execute(query, (group,))
    else:
        cursor.execute(query)

    for row in cursor.fetchall():
        tree.insert("", "end", values=row)

# Hàm thêm món ăn
def add_item():
    ten = entry_ten.get()
    dvt = entry_dvt.get()
    gia = entry_gia.get()
    nhom = combo_nhom.get()
    
    if not (ten and dvt and gia and nhom):
        messagebox.showwarning("Cảnh báo", "Vui lòng nhập đầy đủ thông tin!")
        return

    try:
        cursor.execute("INSERT INTO MonAn (TenMonAn, DonViTinh, DonGia, Nhom) VALUES (?, ?, ?, ?)",
                       (ten, dvt, gia, nhom))
        conn.commit()
        load_data()
        messagebox.showinfo("Thành công", "Thêm món ăn thành công!")
    except Exception as e:
        messagebox.showerror("Lỗi", f"Lỗi thêm món ăn: {e}")

# Hàm sửa món ăn
def update_item():
    selected = tree.selection()
    if not selected:
        messagebox.showwarning("Cảnh báo", "Vui lòng chọn món ăn cần sửa!")
        return
    
    item = tree.item(selected[0])['values']
    ma_mon = item[0]
    ten = entry_ten.get()
    dvt = entry_dvt.get()
    gia = entry_gia.get()
    nhom = combo_nhom.get()

    if not (ten and dvt and gia and nhom):
        messagebox.showwarning("Cảnh báo", "Vui lòng nhập đầy đủ thông tin!")
        return

    try:
        cursor.execute("UPDATE MonAn SET TenMonAn=?, DonViTinh=?, DonGia=?, Nhom=? WHERE MaMonAn=?",
                       (ten, dvt, gia, nhom, ma_mon))
        conn.commit()
        load_data()
        messagebox.showinfo("Thành công", "Cập nhật món ăn thành công!")
    except Exception as e:
        messagebox.showerror("Lỗi", f"Lỗi cập nhật món ăn: {e}")

# Hàm xóa món ăn
def delete_item():
    selected = tree.selection()
    if not selected:
        messagebox.showwarning("Cảnh báo", "Vui lòng chọn món ăn cần xóa!")
        return

    item = tree.item(selected[0])['values']
    ma_mon = item[0]

    try:
        cursor.execute("DELETE FROM MonAn WHERE MaMonAn=?", (ma_mon,))
        conn.commit()
        load_data()
        messagebox.showinfo("Thành công", "Xóa món ăn thành công!")
    except Exception as e:
        messagebox.showerror("Lỗi", f"Lỗi xóa món ăn: {e}")

# Tạo giao diện Tkinter
root = tk.Tk()
root.title("Quản lý Món Ăn")
root.geometry("600x500")

# Nhãn và Combobox chọn nhóm món ăn
frame_top = tk.Frame(root)
frame_top.pack(pady=10)

lbl_nhom = tk.Label(frame_top, text="Nhóm món ăn:")
lbl_nhom.pack(side=tk.LEFT, padx=5)

combo_nhom = ttk.Combobox(frame_top, state="readonly")
combo_nhom["values"] = load_groups()
combo_nhom.pack(side=tk.LEFT, padx=5)

btn_filter = tk.Button(frame_top, text="Lọc", command=lambda: load_data(combo_nhom.get()))
btn_filter.pack(side=tk.LEFT, padx=5)

# Bảng hiển thị danh sách món ăn
cols = ("Mã Món Ăn", "Tên Món Ăn", "Đơn Vị Tính", "Đơn Giá", "Nhóm")
tree = ttk.Treeview(root, columns=cols, show="headings")
for col in cols:
    tree.heading(col, text=col)
    tree.column(col, width=100)

tree.pack(pady=10, fill=tk.BOTH, expand=True)

# Form nhập liệu
frame_bottom = tk.Frame(root)
frame_bottom.pack(pady=10)

lbl_ten = tk.Label(frame_bottom, text="Tên món:")
lbl_ten.grid(row=0, column=0)
entry_ten = tk.Entry(frame_bottom)
entry_ten.grid(row=0, column=1, padx=5)

lbl_dvt = tk.Label(frame_bottom, text="Đơn vị tính:")
lbl_dvt.grid(row=1, column=0)
entry_dvt = tk.Entry(frame_bottom)
entry_dvt.grid(row=1, column=1, padx=5)

lbl_gia = tk.Label(frame_bottom, text="Đơn giá:")
lbl_gia.grid(row=2, column=0)
entry_gia = tk.Entry(frame_bottom)
entry_gia.grid(row=2, column=1, padx=5)

lbl_nhom2 = tk.Label(frame_bottom, text="Nhóm:")
lbl_nhom2.grid(row=3, column=0)
combo_nhom = ttk.Combobox(frame_bottom, state="readonly")
combo_nhom["values"] = load_groups()
combo_nhom.grid(row=3, column=1, padx=5)

# Các nút chức năng
frame_buttons = tk.Frame(root)
frame_buttons.pack(pady=10)

btn_add = tk.Button(frame_buttons, text="Thêm", command=add_item)
btn_add.grid(row=0, column=0, padx=5)

btn_update = tk.Button(frame_buttons, text="Sửa", command=update_item)
btn_update.grid(row=0, column=1, padx=5)

btn_delete = tk.Button(frame_buttons, text="Xóa", command=delete_item)
btn_delete.grid(row=0, column=2, padx=5)

# Tải dữ liệu ban đầu
load_data()

root.mainloop()
