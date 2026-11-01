# Huan Assist — Android

Dự án Android native Java, giao diện tiếng Việt theo ba ảnh tham khảo. Android 8.0 trở lên. Key hiện tại: `havanhuan`.

**Trạng thái bàn giao:** đã tạo mã nguồn và script build tại máy; chưa biên dịch APK, chưa chạy kiểm thử JUnit/lint hoặc kiểm thử điện thoại. Máy phiên làm việc không có JDK/Android SDK; kết nối tải công cụ bị từ chối. Không có APK trong gói này. Không tuyên bố ứng dụng không có lỗi.

## Build bằng GitHub Actions

Workflow: `.github/workflows/android-apk.yml`. Đặt **nội dung bên trong thư mục dự án** tại gốc repository, gồm cả thư mục ẩn `.github`; không chỉ upload file ZIP.

Workflow chạy khi push lên `main`/`master`, khi mở pull request hoặc khi chọn **Actions → Build Android APK → Run workflow**. Dùng Java 17, Gradle 8.9, SDK 35, chạy kiểm thử và lint trước khi đóng gói APK có chữ ký debug.

Khi workflow xanh: mở lần chạy → **Artifacts → HuanAssist-APK**, tải ZIP, giải nén lấy `HuanAssist-debug.apk`. Gói có SHA256SUMS.txt. Báo cáo lỗi/kiểm thử nằm trong Android-check-reports. Artifact được giữ 14 ngày. Không cần cung cấp keystore hoặc token trong mã nguồn để tạo bản debug.

**Trạng thái:** mới chuẩn bị workflow; chưa chạy GitHub Actions do chưa có kết nối tài khoản và repository đích. Chưa có APK để tải. Build xanh cũng không thay thế kiểm thử thực tế trên điện thoại.

## Build trực tiếp tại máy

Mở PowerShell trong thư mục dự án, chạy:

```powershell
.\build-local.ps1 -Bootstrap
```

Script tải JDK 17, Gradle 8.9, Android command-line tools và SDK 35 vào `.local-tools`, hỏi điều khoản SDK, chạy `testDebugUnitTest lintDebug assembleDebug`. Cần truy cập mạng tới Microsoft, Google và Gradle/Maven. Không sửa cấu hình hệ thống toàn máy. Nếu tự động hóa và đã đọc/chấp nhận điều khoản Android SDK, có thể thêm `-AcceptAndroidLicenses`.

Có công cụ sẵn: truyền `-JdkPath` và `-SdkPath`; Gradle 8.9 được script đặt trong `.local-tools` (dùng `-Bootstrap` ở lần đầu). Android Studio cũng có thể mở dự án trực tiếp; chọn JDK 17, Gradle 8.9, SDK 35. Gói không kèm Gradle wrapper JAR tải ngoài.

Khi **tất cả bước thành công**, APK có chữ ký debug nằm tại `output/HuanAssist-debug.apk`. Chữ ký debug dành cho cài thử; muốn phát hành cần keystore riêng, không để mật khẩu trong mã nguồn.

## Sử dụng

1. Cài APK, mở Huan Assist, cấp quyền hiển thị trên ứng dụng khác. Có thể cho phép thông báo để thấy nút dừng dịch vụ.
2. Ứng dụng đưa về màn hình chính và hiển thị cửa sổ nền đen. Nhập `havanhuan`, bấm Đăng nhập. Đóng sẽ dừng dịch vụ.
3. Nút tròn xuất hiện; kéo để di chuyển, chạm để mở menu. Vị trí nút được lưu. Dừng toàn bộ bằng menu hoặc thông báo.
4. Hình ảnh → Chụp & chọn vùng → đồng ý chia sẻ toàn màn hình → kéo khung quanh vùng nhân vật nhỏ hơn nửa chiều rộng/cao màn hình. Thời gian chọn tối đa 20 giây. Ảnh được xử lý trong RAM, không tải lên mạng.
5. FPS bật/tắt ba thẻ RAM thiết bị, CPU tiến trình Assist, pin. Chạm các thẻ để đóng. Tâm ảo và các bảng vẫn dùng được ở màn hình ngang.
6. Tâm ảo mở bảng bên phải: 6 kiểu tâm, X/Y, kích thước, 4 màu, phát sáng, xoay, preview trực tiếp, lưu cấu hình và bật/tắt.
7. Sensitivity Touch mở con trỏ kéo thả và thanh chỉnh X/Y; lưu sẽ ẩn con trỏ. Đây là mốc tọa độ, không can thiệp sự kiện chạm của game.
8. Điều khiển mở bên trái: thông tin màn hình, nhập FPS/DPI mục tiêu có kiểm tra giới hạn, lưu hồ sơ và xem số đo UI tham khảo. Có nút mở cài đặt màn hình.
9. Thiết bị mở bên trái: khung điện thoại vẽ bằng giao diện native, RAM/CPU/pin cập nhật mỗi giây, ảnh chụp gần nhất nếu có phiên chia sẻ. Quyền Usage Access là tùy chọn để hiển thị tên gói ứng dụng ở foreground.

## Những giới hạn cần hiểu chính xác

- Android bắt buộc cấp quyền overlay; không thể mở lớp phủ trên launcher ngay lần đầu khi chưa cấp quyền. Một số ứng dụng/màn hình bảo mật chủ động chặn overlay hoặc capture.
- Theo dõi ảnh dùng so khớp màu vùng ảnh trong cửa sổ lân cận, kéo dài 3 giây. Có line, khung và nhãn `bot`; nhãn này **không phân loại bot**. Mất dấu nếu chuyển cảnh, vùng bị che, thay đổi kích thước/tư thế hoặc di chuyển nhanh. Không đọc bộ nhớ hay tài nguyên Free Fire, không có ESP xuyên tường.
- Thanh tiến trình là thời gian theo dõi ảnh, không giả mạo tải cấu hình hoặc tài nguyên game. Thông báo xanh xuất hiện khi toàn bộ phiên giữ được dấu; thất bại hiển thị thông báo khác.
- RAM và pin là số liệu thiết bị; CPU là tiến trình Assist được chuẩn hóa theo số lõi. Không đo FPS của Free Fire, không đo CPU toàn hệ thống. Tên mục “FPS” giữ theo ảnh tham khảo.
- FPS/DPI trong Điều khiển chỉ là hồ sơ tham khảo. App không root không thể tự ý thay đổi các cấu hình này của game khác. Không có tăng FPS hoặc thay đổi độ nhạy giả.
- Preview điện thoại là ảnh màn hình chia sẻ gần nhất, không phải luồng game độc lập theo thời gian thực. Tên ứng dụng foreground có thể bị nhà sản xuất hạn chế hoặc chậm cập nhật.
- Key cứng chỉ phù hợp bản thử nghiệm; không phải hệ thống cấp phép bảo mật. Cần dịch vụ xác thực riêng nếu phát hành thương mại.
- Xoay màn hình sẽ đóng bảng và kết thúc phiên capture để tránh lệch tọa độ. Mở Hình ảnh để cấp lại phiên theo chiều mới. Dừng app/process sẽ yêu cầu đăng nhập lại.

## Kiến trúc

- `MainActivity`: điều hướng cấp quyền và consent chụp màn hình.
- `OverlayService`: vòng đời foreground service, các cửa sổ nổi, menu, telemetry, lưu cấu hình.
- `Ui`: giao diện native, vector icon vẽ bằng Canvas, biểu đồ mẫu lịch sử.
- `Capture`: MediaProjection/ImageReader, xử lý hình trên luồng riêng, không lưu ảnh ra đĩa.
- `Tracker`: thuật toán so khớp ảnh cục bộ, độc lập Android.
- `TrackerTest`: kiểm thử dịch chuyển, mất dấu và cạnh khung hình. Chưa chạy trong phiên bàn giao.

## Kiểm thử trên thiết bị trước khi sử dụng

Xem `DEVICE-TESTS.md`. Kiểm tra ít nhất Android 8, 12, 14, 15; màn hình dọc/ngang; máy có notch; font lớn; quyền bị từ chối/thu hồi; game có lớp phủ riêng. Các kiểm tra này chưa được thực hiện trong phiên.

## Tài liệu nền tảng

- [Android MediaProjection và consent mỗi phiên](https://developer.android.com/media/grow/media-projection)
- [Foreground service types](https://developer.android.com/develop/background-work/services/fgs/service-types)
- [Quy tắc overlay và độ trong suốt khi chuyển tiếp thao tác chạm](https://developer.android.com/reference/android/view/WindowManager.LayoutParams)
- [Microsoft OpenJDK](https://learn.microsoft.com/en-us/java/openjdk/download-major-urls)
