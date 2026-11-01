# Giao diện Neon 1.1.0

- Màu dùng chung: nền #0C1120, chữ #F5F7FC, phụ #A8B9CF, cyan #63F5E4, tím #B7A0FF. Font hệ thống hỗ trợ tiếng Việt.
- Màn hình cấp quyền có thẻ hướng dẫn ba bước, logo neon và nút gradient.
- Đăng nhập key có ô nhập bo góc, viền focus, hiện/ẩn mật khẩu, nút Enter đăng nhập và lỗi ngay dưới ô nhập. Key, quy trình cấp quyền và xác thực giữ nguyên.
- Menu nổi thành lưới 2 cột, 6 thẻ có icon vector, phản hồi nhấn và rung hệ thống. Nhãn không bị giới hạn hai dòng khi tăng cỡ chữ.
- Nút nổi vẽ bằng Canvas với vòng cyan/tím và tâm bo góc, giữ vùng chạm 56dp và kéo thả.
- Các bảng có header và nút Đóng cố định, nội dung cuộn giới hạn theo chiều cao màn hình. Bảng bên phải trượt từ phải; bảng bên trái từ trái.
- Hiệu ứng xuất hiện 240ms, thu gọn 160ms, báo key sai 240ms; tôn trọng cài đặt tắt animation. Không thêm hoạt ảnh nền chạy liên tục.
- Tâm ảo có preview lưới riêng, 6 kiểu chia cặp, 4 màu với trạng thái chọn, thanh trượt cyan và switch đồng bộ. Xoay preview chỉ chạy khi người dùng bật.
- RAM/CPU/pin có ba thẻ riêng, giá trị theo màu và biểu đồ có vùng tô; CPU vẫn ghi rõ CPU Assist.
- Thiết bị có khung tối neon và trạng thái chưa có ảnh; các ô FPS/DPI sử dụng cùng mẫu ô nhập mới.
- Giữ cả ba sửa lỗi build: thuộc tính navigation bar trong values-v27; LinearLayout.VERTICAL; ngoại lệ ProtectedPermissions chỉ trên PACKAGE_USAGE_STATS.
- Không thay đổi Capture.java, Tracker.java, TrackerTest.java. Không bổ sung quyền hay thư viện bên ngoài.

## Kiểm thử còn cần trên Android

Đăng nhập key đúng/sai; bật/tắt hiện key; nút Done bàn phím; menu ở dọc/ngang; font 1.0/1.3/2.0; đóng nhanh rồi mở lại; preview tâm khi bật/tắt xoay; chế độ animation scale 0; telemetry; nhập số FPS/DPI. Chưa chạy các kiểm tra này trong phiên.
