# NEXORA AI V2 ⚡
### Autonomous AI Personal Operating System for Android
### نظام التشغيل الذكي المستقل المتقدم للأندرويد

---

## 🌟 مميزات النظام (Key Features)

1. **إدارة وتشفير مفاتيح API من داخل التطبيق (In-App Multi-API Key Manager)**:
   - يمكنك إضافة أي مفتاح Gemini API مباشرة من واجهة التطبيق مع تسمية مخصصة.
   - تشفير عتادي محلي كامل عبر **Android Keystore (AES-256 GCM)** لحماية المفاتيح.
   - زر **فحص واختبار المفتاح الفوري (Test Key)** للتحقق من صلاحية المفتاح وعرض النماذج المدعومة من Google.
   - إمكانية التبديل بين نماذج Gemini المتعددة (`gemini-2.0-flash`, `gemini-1.5-flash`, `gemini-1.5-flash-latest`, `gemini-2.5-flash`, `gemini-1.5-pro`).

2. **محرك الوكيل المستقل والتخطيط (Autonomous Agent Engine)**:
   - تحليل الأهداف باللغة الطبيعية وتقسيمها إلى مهام فرعية (Subtasks).
   - بوابة الموافقة البشرية (Human-in-the-Loop Approval) للعمليات الحساسة قبل تنفيذها.
   - زر الإيقاف الطارئ الفوري (Global Emergency Stop).

3. **التكامل مع عتاد الأندرويد (Device OS & Hardware Integration)**:
   - التحكم في فلاش الكاميرا (Flashlight / Torch).
   - إرسال نبضات لمسية تنبيهية (Haptic Feedback Vibrations).
   - قراءة مستشعرات الطاقة ومستوى البطارية والحرارة والشحن في الوقت الفعلي.
   - عرض تفاصيل ومواصفات الجهاز.

4. **مساعد ذكي للمحادثة (Multi-turn Gemini AI Chat)**:
   - محادثات سياقية ذكية تدعم العربية والإنجليزية.
   - نسخ الرسائل وسجل المحادثة واقتراحات الأسئلة السريعة.

5. **البناء التلقائي عبر GitHub Actions**:
   - بناء ملف `APK` الجاهز للتثبيت تلقائياً عند الدفع إلى GitHub عبر Workflow `.github/workflows/android-apk.yml`.

---

## 📱 دليل التثبيت والبناء عبر GitHub لمستخدمي الهاتف

1. **رفع المشروع إلى مستودع GitHub الخاص بك**:
   - أنشئ مستودعاً جديداً (New Repository) على GitHub باسم `NEXORA-AI`.
   - ارفع ملفات المشروع إلى المستودع.
2. **تشغيل البناء التلقائي (GitHub Actions)**:
   - افتح تبويب **Actions** في صفحة المستودع.
   - اختر سير العمل: **Build NEXORA AI Android APK**.
   - اضغط على **Run workflow** -> **Branch: main** -> **Run workflow**.
3. **تحميل ملف الـ APK**:
   - بعد انتهاء البناء وظهور علامة النجاح الخضراء ✅، انقر على اسم التشغيل (Run).
   - في قسم **Artifacts** بالأسفل، اضغط على **NEXORA-AI-APK** لتحميل ملف الـ ZIP الذي يحتوي على ملف `app-debug.apk`.
4. **تثبيت التطبيق على هاتفك**:
   - فك الضغط عن الملف واضغط على `app-debug.apk` للتثبيت.
   - افتح التطبيق، واذهب إلى تبويب **المفاتيح (Keys)** وأدخل مفتاحك الخاص للبدء فوراً!

---

## 🔒 أمان البيانات والخصوصية (Security & Privacy)

- **Android Keystore**: جميع المفاتيح المدخلة من التطبيق يتم تشفيرها وتخزينها محلياً على الجهاز.
- **Zero External Leak**: لا يتم إرسال المفاتيح إلى أي وسيط أو خادم خارجي سوى واجهة Google الرسمية `generativelanguage.googleapis.com`.

---

Developed with ❤️ for NEXORA AI V2.
