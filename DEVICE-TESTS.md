# Checklist kiểm thử — tất cả đang CHƯA CHẠY trên Android

| Luồng | Kết quả cần đạt |
|---|---|
| Cài mới, không cấp overlay | Giữ màn hình hướng dẫn, không crash |
| Cấp overlay, cho/từ chối thông báo | Về launcher, login ở lớp phủ |
| Key sai, trống, đúng | Báo lỗi gần ô nhập; đúng mới hiện nút nổi |
| Đóng login | Dịch vụ và mọi cửa sổ biến mất |
| Chạm/kéo orb | Chạm mở menu; kéo không vô tình mở menu; giữ trong màn hình |
| Bảng menu ở landscape nhỏ | Cuộn được, nút đóng luôn tới được |
| FPS | Dữ liệu thực; CPU ghi rõ Assist; tap đóng |
| Tâm ảo | 6 kiểu, vị trí, kích thước, màu, xoay, glow đúng; game vẫn nhận touch |
| Giảm animation hệ thống | Không chạy animation trang trí liên tục |
| Sensitivity | Kéo hoặc thanh trượt; lưu ẩn con trỏ; mở lại giữ vị trí |
| Điều khiển | Từ chối số ngoài giới hạn; không báo tăng FPS thực |
| Chụp ảnh từ chối | Không crash; nút nổi còn hoạt động |
| Chọn vùng, hủy, timeout | Luôn có lối thoát; không còn overlay chặn touch |
| Theo dõi dịch chuyển chậm | Bám vùng 3 giây, thanh tiến trình, thông báo thành công |
| Mất vùng/cắt cảnh | Không bịa vị trí; báo mất dấu và kết thúc thất bại |
| Nội dung FLAG_SECURE | Không tuyên bố nhận diện thành công trên khung đen |
| Dừng chia sẻ từ Android | Giải phóng ImageReader, thông báo, selector |
| Xoay màn hình khi chọn/theo dõi | Kết thúc phiên cũ, không còn tọa độ/line cũ |
| Thu hồi overlay khi chạy | Dịch vụ dừng sạch |
| Dừng từ notification/menu | Không còn cửa sổ, luồng capture, handler |
| RAM/CPU chạy lâu | Không tăng bộ nhớ vô hạn; lịch sử biểu đồ giới hạn 40 mẫu |
| Usage Access từ chối/đồng ý | Tên gói hiện hoặc báo chưa cấp quyền |
| Font lớn/TalkBack | Nhãn đọc được, cuộn được, không bị cắt hành động chính |
