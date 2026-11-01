# Aujunpeak Assist — Android native Java

Key thử nghiệm: `havanhuan` (phân biệt hoa thường).
Android 8 trở lên. Không cần root. Không có kết nối mạng, không đọc bộ nhớ game.

## Build APK bằng GitHub Actions
1. Tạo repo trên GitHub, upload **nội dung** thư mục này vào thư mục gốc repo.
2. Đảm bảo `.github/workflows/android.yml` được upload. Thư mục app và settings.gradle nằm ngay gốc.
3. Mở Actions → Build Android APK → Run workflow. Push vào main/master cũng chạy build.
4. Khi job xanh, tải artifact `Aujunpeak-Assist-debug`, giải nén và cài APK.
5. APK debug được ký tự động bằng debug key của runner; đây chưa phải bản phát hành có khóa ký ổn định. Khi runner thay khóa, cần gỡ bản cũ trước khi cài bản mới (mất dữ liệu app).

Không kèm Gradle wrapper jar: workflow cài Gradle 8.11.1 bằng gradle/actions; không gọi ./gradlew nên không có lỗi thiếu wrapper/quyền executable. Local: cài JDK 17, Android SDK 35 và Gradle 8.11.1, chạy `gradle :app:assembleDebug :app:lintDebug`.

## Cách sử dụng
- Mở app → cho phép hiển thị trên ứng dụng khác trong Settings → quay lại app. App trở về Home, mở bảng key nền đen.
- Nhập key → Đăng nhập. Nút nổi xuất hiện; kéo để đổi vị trí, chạm mở menu.
- Đóng trong bảng key hoặc Dừng trên thông báo sẽ tắt dịch vụ và mọi cửa sổ nổi.
- Nếu máy chặn hoạt động nền, kiểm tra cài đặt pin của nhà sản xuất. Một số app/màn hình bảo mật chặn overlay; không thể cam kết nổi trên mọi ứng dụng.

## Tính năng thực tế
| Mục | Đã triển khai | Giới hạn |
|---|---|---|
| Đăng nhập | Overlay thật, nền đen, đóng/đăng nhập, kiểm tra key | Lần đầu bắt buộc cấp quyền hệ thống; key hardcode chỉ là khóa demo, không phải xác thực bảo mật |
| AssistiveTouch | Ảnh nút được cung cấp, kéo thả, lưu vị trí, animation xuất hiện; menu 6 mục icon Canvas | Menu dựng bằng code theo bố cục tham khảo, không phải sao chép pixel tuyệt đối |
| Hình ảnh | Hỏi quyền MediaProjection từng lần, chụp sau 3 giây, lưu PNG nội bộ, thông báo ✓ xanh fade sau 3 giây; xem ảnh cuối ở Thiết bị | Chụp toàn màn hình, chưa có crop chọn nhân vật. Không ESP/line/name bot bám theo nhân vật, không tải tài nguyên hoặc báo thành công giả. Nội dung FLAG_SECURE có thể bị đen |
| FPS | Ba biểu đồ RAM toàn máy, CPU của app, pin thật, cập nhật mỗi giây | Không đo FPS hay CPU Free Fire/toàn máy; CPU app chuẩn hóa theo số lõi. FPS game ghi rõ không có quyền đo |
| Sensitivity Touch | Con trỏ nổi kéo thả; thanh X/Y; lưu và ẩn | Chỉ lưu tọa độ tham chiếu, không đổi độ nhạy game; thanh X/Y không đồng bộ lại khi kéo con trỏ |
| Tâm ảo | Panel bên phải, 6 kiểu, chỉnh X/Y, kích thước, màu, xoay, sáng, preview, lưu, ẩn | Tâm ảo overlay không tự ngắm; chưa có nhập màu tùy ý hay bộ hiệu ứng mở rộng. Màu chưa lưu qua lần mở app |
| Điều khiển | Panel trái, nhập FPS/DPI có giới hạn, lưu hồ sơ, thông tin màn hình thật | Không ép FPS/DPI hệ thống hay game; chỉ hồ sơ tham chiếu |
| Thiết bị | Khung điện thoại từ ảnh gửi, RAM/CPU app/pin, độ phân giải thật, ảnh chụp cuối | Không stream game trực tiếp trong khung, không tự nhận diện game đang chơi |

Ảnh nằm trong vùng dữ liệu riêng của ứng dụng; gỡ app sẽ xóa. Không yêu cầu quyền bộ nhớ chung. Ảnh chụp cuối chỉ nạp khi mở mục Thiết bị.

## Cấu trúc
- MainActivity.java: cấp quyền và đưa về màn hình Home.
- OverlayService.java: foreground service, login, cửa sổ nổi, panel, telemetry, Canvas icons/crosshair/chart.
- CaptureActivity.java: xin đồng ý chụp qua hệ thống.
- CaptureService.java: foreground MediaProjection, một ảnh/session, cleanup.
- app/src/main/res/drawable: ảnh nút/khung máy gốc.
- .github/workflows/android.yml: SDK 35, Java 17, Gradle 8.11.1, AGP 8.9.2, build APK + lint, upload artifact.

## Trạng thái kiểm tra
Môi trường tạo nguồn không có JDK/Android SDK/Gradle để biên dịch hoặc thiết bị để chạy. Workflow chưa được chạy trên tài khoản GitHub. Không khẳng định APK build thành công hay ứng dụng hoàn thiện tuyệt đối. Cần xác nhận bằng job Actions và kiểm thử thiết bị.

Checklist trên máy thật: cấp/từ chối overlay; key sai/đúng; đóng login; kéo nút; mở/đóng 6 mục; quay ngang/dọc; touch/crosshair không chặn thao tác game; chấp thuận/hủy/dừng capture; screenshot bảo mật; thiếu dung lượng; dừng notification; hệ thống thu hồi quyền; kiểm tra pin và bộ nhớ trong phiên dài.

## Android API tham khảo
https://developer.android.com/reference/android/provider/Settings#canDrawOverlays(android.content.Context)
https://developer.android.com/media/grow/media-projection
https://developer.android.com/develop/background-work/services/fgs/service-types
