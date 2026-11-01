BAN VA LOI LINT DA XAC DINH — KHONG PHAI FULL SOURCE

Giai nen va chep de cac file trong app/ va .github/ vao DUNG vi tri trong repository hien tai. Giu nguyen cac file source khac. Commit cac thay doi de chay workflow moi; Re-run lan cu se van dung source cu.

Da sua: thuoc tinh windowLightNavigationBar chi nam trong values-v27. Android API 26 dung AppTheme ke thua BaseAppTheme, API 27+ them thuoc tinh moi. Khong tang minSdk va khong tat lint.

Workflow moi tach unit test / lint / assemble, in bao cao lint day du vao log, luu ca bao cao text vao Android-check-reports. Chi xuat APK neu cac check thanh cong.

Log da cung cap xac nhan Java compilation, unit test, assembleDebug da chay; lint bao 3 errors / 39 warnings nhung chi hien 1 error dau tien. Hai loi con lai CHUA CO DU LIEU de sua. Warnings deprecated khong phai nguyen nhan fail da hien.

Can gui Android-check-reports.zip cua lan build loi (chua lint-results-debug.html hoac xml), va full source ZIP dang dung, de xu ly day du cac loi con lai. Ban source local truoc do khong con trong thu muc phien hien tai.

Kiem tra da lam: parse XML, inheritance theme cho API26/27, noi dung ZIP. Chua chay lai Gradle/lint hoac test thiet bi cho ban va nay.

Tai lieu Android: https://developer.android.com/guide/topics/resources/providing-resources
