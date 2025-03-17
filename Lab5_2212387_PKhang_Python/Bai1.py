import pyodbc

# Kết nối đến CSDL SQL Server
conn = pyodbc.connect(
    "DRIVER={SQL Server};"
    "SERVER=localhost;"  # Đổi nếu server khác
    "DATABASE=QLSinhVien;"
    "Trusted_Connection=yes;"
)
cursor = conn.cursor()

# Hiển thị phiên bản SQL Server
def show_sql_version():
    cursor.execute("SELECT @@VERSION")
    row = cursor.fetchone()
    print("🔹 Phiên bản SQL Server:", row[0])

# Lấy danh sách lớp học
def get_classes():
    print("\n🔸 Danh sách lớp học:")
    cursor.execute("SELECT * FROM Lop")
    for lop in cursor.fetchall():
        print(f"  - Mã lớp: {lop[0]}, Tên lớp: {lop[1]}")

# Lấy danh sách sinh viên
def get_students():
    print("\n🔸 Danh sách sinh viên:")
    cursor.execute("SELECT * FROM SinhVien")
    for sv in cursor.fetchall():
        print(f"  - Mã SV: {sv[0]}, Họ tên: {sv[1]}, Mã lớp: {sv[2]}")

# Tìm kiếm sinh viên theo mã hoặc tên
def search_student(ma_sv=None, ten_sv=None):
    query = "SELECT * FROM SinhVien WHERE "
    params = []

    if ma_sv:
        query += "ID = ?"
        params.append(ma_sv)
    elif ten_sv:
        query += "HoTen LIKE ?"
        params.append(f"%{ten_sv}%")

    cursor.execute(query, params)
    result = cursor.fetchall()

    if result:
        print("\n🔹 Kết quả tìm kiếm:")
        for sv in result:
            print(f"  - Mã SV: {sv[0]}, Họ tên: {sv[1]}, Mã lớp: {sv[2]}")
    else:
        print("⚠️ Không tìm thấy sinh viên!")

# Thêm sinh viên mới
def add_student(ma_sv, ho_ten, ma_lop):
    try:
        cursor.execute("INSERT INTO SinhVien (ID, HoTen, MaLop) VALUES (?, ?, ?)", (ma_sv, ho_ten, ma_lop))
        conn.commit()
        print("✅ Thêm sinh viên thành công!")
    except Exception as e:
        print("❌ Lỗi khi thêm sinh viên:", e)

# Cập nhật thông tin sinh viên
def update_student(ma_sv, ho_ten_moi):
    try:
        cursor.execute("UPDATE SinhVien SET HoTen = ? WHERE ID = ?", (ho_ten_moi, ma_sv))
        conn.commit()
        print("✅ Cập nhật sinh viên thành công!")
    except Exception as e:
        print("❌ Lỗi khi cập nhật:", e)

# Xóa sinh viên
def delete_student(ma_sv):
    try:
        cursor.execute("DELETE FROM SinhVien WHERE ID = ?", (ma_sv,))
        conn.commit()
        print("✅ Xóa sinh viên thành công!")
    except Exception as e:
        print("❌ Lỗi khi xóa:", e)

# Chạy chương trình chính
if __name__ == "__main__":
    show_sql_version()
    get_classes()
    get_students()

    # Thử các chức năng
    search_student(ma_sv=1)
    add_student(1003, "Trần Văn A", 2)
    update_student(1003, "Trần Văn B")
    delete_student(1003)

    # Đóng kết nối
    conn.close()
