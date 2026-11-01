BAN VA CAC LOI DA XAC DINH - KHONG PHAI FULL SOURCE

Chep de cac file app/ va .github/ vao dung vi tri repository, giu source khac, commit de chay build moi.

Da sua:
1. windowLightNavigationBar chuyen sang values-v27, giu ho tro API26.
2. Ui.column: setOrientation(LinearLayout.VERTICAL) thay cho setOrientation(1).
3. Ui.java dung hang so co ten cho paint flags va layout sizes; giu chu ky ham va giao dien cu.
4. Workflow tach cac buoc va in TOAN BO lint report trong buoc Print complete lint report.

Log moi nhat bao 2 errors / 39 warnings, nhung chi hien WrongConstant. Loi con lai chua duoc cung cap. Chua xac nhan lint sach va chua chay Gradle tai may nay. Khong tat lint, khong baseline che loi.

De sua het trong mot luot: gui Android-check-reports.zip (Artifacts) va full source ZIP hien tai. Hoac copy TOAN BO buoc Print complete lint report, khong phai buoc Android lint chi hien First failure.
