BAN VA V3 - GOP CA 3 LOI CHAN BUILD DA XUAT HIEN TRONG LOG

Giai nen, chep de app/ va .github/ vao dung vi tri repository; giu cac file source khac. Commit thay doi de chay build MOI. Re-run commit cu khong ap dung ban va.

1. NewApi: windowLightNavigationBar nam trong values-v27, giu API26.
2. WrongConstant: Ui.column dung LinearLayout.VERTICAL thay so 1.
3. ProtectedPermissions: PACKAGE_USAGE_STATS la quyen AppOps, duoc nguoi dung bat qua Usage Access. Khai bao manifest van can thiet. tools:ignore chi dat tren dung uses-permission nay, khong tat lint toan du an. Code goc da kiem tra AppOpsManager va mo ACTION_USAGE_ACCESS_SETTINGS.

Tai lieu chinh thuc: https://developer.android.com/reference/android/app/usage/UsageStatsManager
https://developer.android.com/reference/android/Manifest.permission#PACKAGE_USAGE_STATS

Log moi nhat chi con 1 error ProtectedPermissions va 39 warnings. Ban va xu ly ca 3 error da biet. Chua chay lai Gradle tai may nay; chi da kiem tra XML, pham vi suppression, constant fix va ZIP. Chua chung nhan runtime khong loi. 39 warnings chua duoc cung cap day du va khong duoc coi la da sua.

Sau khi workflow thanh cong: Actions > lan chay moi > Artifacts > HuanAssist-APK, giai nen APK de cai thu.
